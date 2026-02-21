package l2jorion.game.model.spawn;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2jorion.Config;
import l2jorion.game.datatables.sql.NpcTable;
import l2jorion.game.datatables.sql.SpawnTable;
import l2jorion.game.datatables.xml.MapRegionTable;
import l2jorion.game.idfactory.IdFactory;
import l2jorion.game.model.Location;
import l2jorion.game.model.actor.instance.AutoSpawnInstance;
import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.entity.Announcements;
import l2jorion.game.templates.StatsSet;
import l2jorion.game.thread.ThreadPoolManager;
import l2jorion.log.Log;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;
import l2jorion.util.random.Rnd;

public class AutoSpawn
{
	protected static final Logger LOG = LoggerFactory.getLogger(AutoSpawn.class);
	
	private static final int DEFAULT_INITIAL_SPAWN = 30000;
	private static final int DEFAULT_RESPAWN = 3600000;
	private static final int DEFAULT_DESPAWN = 3600000;
	private static final String AUTO_SPAWN_XML = "./data/xml/auto_spawns.xml";
	
	protected static final Map<Integer, AutoSpawnInstance> _mobs = new ConcurrentHashMap<>();
	protected static final Map<Integer, L2Spawn> _spawns = new ConcurrentHashMap<>();
	protected static final Map<Integer, StatsSet> _storedInfo = new ConcurrentHashMap<>();
	protected static final Map<Integer, ScheduledFuture<?>> _schedules = new ConcurrentHashMap<>();
	
	protected final SimpleDateFormat date = new SimpleDateFormat("H:mm:ss yyyy/MM/dd");
	protected boolean _activeState = true;
	
	private static final AutoSpawn _instance = new AutoSpawn();
	
	public static AutoSpawn getInstance()
	{
		return _instance;
	}
	
	protected AutoSpawn()
	{
		restoreSpawnData();
	}
	
	public int size()
	{
		return _mobs.size();
	}
	
	private void restoreSpawnData()
	{
		var file = new File(AUTO_SPAWN_XML);
		if (!file.exists())
		{
			if (Config.DEBUG)
			{
				LOG.info("AutoSpawn: File " + AUTO_SPAWN_XML + " not found/empty.");
			}
			return;
		}
		
		int numLoaded = 0;
		try
		{
			var factory = DocumentBuilderFactory.newInstance();
			var builder = factory.newDocumentBuilder();
			var doc = builder.parse(file);
			
			for (Node listNode = doc.getFirstChild(); listNode != null; listNode = listNode.getNextSibling())
			{
				if ("list".equalsIgnoreCase(listNode.getNodeName()))
				{
					for (Node spawnNode = listNode.getFirstChild(); spawnNode != null; spawnNode = spawnNode.getNextSibling())
					{
						if ("autoSpawn".equalsIgnoreCase(spawnNode.getNodeName()))
						{
							parseAutoSpawn(spawnNode);
							numLoaded++;
						}
					}
				}
			}
			if (numLoaded > 0)
			{
				LOG.info("AutoSpawnHandler: Loaded " + numLoaded + " auto spawn(s).");
			}
		}
		catch (Exception e)
		{
			LOG.warn("AutoSpawnHandler: Error loading XML: " + e.getMessage());
		}
	}
	
	private void parseAutoSpawn(Node spawnNode)
	{
		NamedNodeMap attrs = spawnNode.getAttributes();
		int npcId = Integer.parseInt(attrs.getNamedItem("npcId").getNodeValue());
		int initialDelay = parseInteger(attrs, "initialDelay", -1);
		int respawnDelay = parseInteger(attrs, "respawnDelay", -1);
		int despawnDelay = parseInteger(attrs, "despawnDelay", -1);
		int count = parseInteger(attrs, "count", 1);
		boolean broadcast = Boolean.parseBoolean(attrs.getNamedItem("broadcast").getNodeValue());
		boolean random = Boolean.parseBoolean(attrs.getNamedItem("random").getNodeValue());
		var spawnInst = registerSpawn(npcId, initialDelay, respawnDelay, despawnDelay);
		spawnInst.setSpawnCount(count);
		spawnInst.setBroadcast(broadcast);
		spawnInst.setRandomSpawn(random);
		StatsSet info = new StatsSet();
		info.set("respawnTime", 0L);
		_storedInfo.put(npcId, info);
		
		// Locations
		for (Node locNode = spawnNode.getFirstChild(); locNode != null; locNode = locNode.getNextSibling())
		{
			if ("loc".equalsIgnoreCase(locNode.getNodeName()))
			{
				NamedNodeMap locAttrs = locNode.getAttributes();
				int x = Integer.parseInt(locAttrs.getNamedItem("x").getNodeValue());
				int y = Integer.parseInt(locAttrs.getNamedItem("y").getNodeValue());
				int z = Integer.parseInt(locAttrs.getNamedItem("z").getNodeValue());
				int h = parseInteger(locAttrs, "heading", 0);
				spawnInst.addSpawnLocation(x, y, z, h);
			}
		}
	}
	
	private int parseInteger(NamedNodeMap attrs, String name, int def)
	{
		Node n = attrs.getNamedItem(name);
		return (n != null) ? Integer.parseInt(n.getNodeValue()) : def;
	}
	
	public AutoSpawnInstance registerSpawn(int npcId, int initialDelay, int respawnDelay, int despawnDelay)
	{
		if (initialDelay < 0)
		{
			initialDelay = DEFAULT_INITIAL_SPAWN;
		}
		if (respawnDelay < 0)
		{
			respawnDelay = DEFAULT_RESPAWN;
		}
		if (despawnDelay < 0)
		{
			despawnDelay = DEFAULT_DESPAWN;
		}
		
		var newSpawn = new AutoSpawnInstance(npcId, initialDelay, respawnDelay, despawnDelay, 0);
		int newId = IdFactory.getInstance().getNextId();
		newSpawn._objectId = newId;
		
		_mobs.put(newId, newSpawn);
		setSpawnActive(newSpawn, true);
		
		return newSpawn;
	}
	
	public boolean removeSpawn(AutoSpawnInstance spawnInst)
	{
		if (!_mobs.containsValue(spawnInst))
		{
			return false;
		}
		_mobs.remove(spawnInst._objectId);
		
		var respawnTask = _schedules.remove(spawnInst._objectId);
		if (respawnTask != null)
		{
			respawnTask.cancel(false);
		}
		
		return true;
	}
	
	public void setSpawnActive(AutoSpawnInstance spawnInst, boolean isActive)
	{
		if (spawnInst == null)
		{
			return;
		}
		int objectId = spawnInst._objectId;
		
		if (_mobs.containsKey(objectId))
		{
			ScheduledFuture<?> spawnTask;
			if (isActive)
			{
				Runnable spawner = () -> runSpawner(objectId);
				if (spawnInst.getDespawnDelay() > 0)
				{
					spawnTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(spawner, spawnInst.getInitialDelay(), spawnInst.getRespawnDelay());
				}
				else
				{
					spawnTask = ThreadPoolManager.getInstance().scheduleEffect(spawner, spawnInst.getInitialDelay());
				}
				
				_schedules.put(objectId, spawnTask);
			}
			else
			{
				spawnTask = _schedules.remove(objectId);
				if (spawnTask != null)
				{
					spawnTask.cancel(false);
				}
				ThreadPoolManager.getInstance().scheduleEffect(() -> runDespawner(objectId), 0);
			}
			spawnInst.setSpawnActive(isActive);
		}
	}
	
	public AutoSpawnInstance getAutoSpawnInstance(int id, boolean isObjectId)
	{
		if (isObjectId)
		{
			return _mobs.get(id);
		}
		
		return _mobs.values().stream().filter(s -> s.getNpcId() == id).findFirst().orElse(null);
	}
	
	public long getTimeToNextSpawn(AutoSpawnInstance spawnInst)
	{
		if (spawnInst == null)
		{
			return -1;
		}
		ScheduledFuture<?> task = _schedules.get(spawnInst._objectId);
		return (task != null) ? task.getDelay(TimeUnit.MILLISECONDS) : -1;
	}
	
	public static void updateStatus(L2NpcInstance mob, boolean isMobDead)
	{
		if (!_storedInfo.containsKey(mob.getNpcId()))
		{
			return;
		}
		
		if (isMobDead)
		{
			for (AutoSpawnInstance spawnInst : _mobs.values())
			{
				if (spawnInst.getNpcId() == mob.getNpcId())
				{
					long respawnDelay = spawnInst.getRespawnDelay();
					long respawnTime = Calendar.getInstance().getTimeInMillis() + respawnDelay;
					
					StatsSet info = _storedInfo.get(spawnInst.getNpcId());
					if (info == null)
					{
						info = new StatsSet();
					}
					info.set("respawnTime", respawnTime);
					
					GregorianCalendar gc = new GregorianCalendar();
					gc.setTimeInMillis(respawnTime);
					
					String nextSpawn = DateFormat.getDateTimeInstance().format(gc.getTime());
					Log.add(mob.getName() + " killed. Next respawn: " + nextSpawn, "RaidBosses");
					
					_storedInfo.put(mob.getNpcId(), info);
					if (!_schedules.containsKey(spawnInst._objectId))
					{
						ScheduledFuture<?> futureSpawn = ThreadPoolManager.getInstance().scheduleGeneral(() ->
						{
							L2Spawn spawn = _spawns.get(spawnInst.getNpcId());
							if (spawn != null)
							{
								L2NpcInstance newMob = spawn.doSpawn();
								if (newMob != null)
								{
									StatsSet newInfo = new StatsSet();
									newInfo.set("respawnTime", 0L);
									_storedInfo.put(spawnInst.getNpcId(), newInfo);
								}
							}
							_schedules.remove(spawnInst._objectId);
						}, respawnDelay);
						_schedules.put(spawnInst._objectId, futureSpawn);
					}
				}
			}
		}
	}
	
	private void runSpawner(int objectId)
	{
		try
		{
			var spawnInst = _mobs.get(objectId);
			if (spawnInst == null || !spawnInst.isSpawnActive())
			{
				return;
			}
			
			Location[] locationList = spawnInst.getLocationList();
			if (locationList.length == 0)
			{
				return;
			}
			
			int locationIndex = Rnd.nextInt(locationList.length);
			if (!spawnInst.isRandomSpawn())
			{
				locationIndex = (spawnInst._lastLocIndex + 1) % locationList.length;
				spawnInst._lastLocIndex = locationIndex;
			}
			
			var loc = locationList[locationIndex];
			var npcTemp = NpcTable.getInstance().getTemplate(spawnInst.getNpcId());
			if (npcTemp == null)
			{
				return;
			}
			
			var spawnDat = new L2Spawn(npcTemp);
			spawnDat.setLocx(loc.getX());
			spawnDat.setLocy(loc.getY());
			spawnDat.setLocz(loc.getZ());
			if (loc.getHeading() != -1)
			{
				spawnDat.setHeading(loc.getHeading());
			}
			spawnDat.setAmount(spawnInst.getSpawnCount());
			
			SpawnTable.getInstance().addNewSpawn(spawnDat, false);
			
			for (int i = 0; i < spawnInst.getSpawnCount(); i++)
			{
				L2NpcInstance mob = spawnDat.doSpawn();
				if (mob == null)
				{
					continue;
				}
				
				if (spawnInst.getSpawnCount() > 1)
				{
					mob.setXYZ(mob.getX() + Rnd.nextInt(50), mob.getY() + Rnd.nextInt(50), mob.getZ());
				}
				
				spawnInst.addNpcInstance(mob);
				
				if (i == 0 && spawnInst.isBroadcasting())
				{
					String town = MapRegionTable.getInstance().getClosestTownName(mob);
					Announcements.getInstance().announceToAll("The " + mob.getName() + " has spawned near " + town + "!");
				}
			}
			
			_spawns.put(spawnInst.getNpcId(), spawnDat);
			
			if (spawnInst.getDespawnDelay() > 0)
			{
				ThreadPoolManager.getInstance().scheduleAi(() -> runDespawner(objectId), spawnInst.getDespawnDelay() - 1000);
			}
		}
		catch (Exception e)
		{
			LOG.warn("AutoSpawn: Error in spawner: " + e.getMessage());
		}
	}
	
	private void runDespawner(int objectId)
	{
		try
		{
			var spawnInst = _mobs.get(objectId);
			if (spawnInst == null)
			{
				return;
			}
			
			var npcInstances = spawnInst.getNPCInstanceList();
			if (npcInstances == null)
			{
				return;
			}
			
			for (var npc : npcInstances)
			{
				if (npc != null)
				{
					npc.deleteMe();
					spawnInst.removeNpcInstance(npc);
				}
			}
		}
		catch (Exception e)
		{
			LOG.warn("AutoSpawn: Error in despawner: " + e.getMessage());
		}
	}
	
	public void setAllActive(boolean isActive)
	{
		if (_activeState == isActive)
		{
			return;
		}
		_mobs.values().forEach(s -> setSpawnActive(s, isActive));
		_activeState = isActive;
	}
	
	public Map<Integer, AutoSpawnInstance> getAutoSpawnInstances(int npcId)
	{
		var result = new HashMap<Integer, AutoSpawnInstance>();
		_mobs.values().stream().filter(s -> s.getNpcId() == npcId).forEach(s -> result.put(s._objectId, s));
		return result;
	}
	
	public void cleanUp()
	{
		_mobs.clear();
		_schedules.values().forEach(f -> f.cancel(true));
		_schedules.clear();
		_storedInfo.clear();
		_spawns.clear();
	}
}