package l2jorion.game.model.entity.event.partyzone;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

import l2jorion.game.cache.HtmCache;
import l2jorion.game.datatables.sql.ItemTable;
import l2jorion.game.datatables.sql.NpcTable;
import l2jorion.game.datatables.sql.SpawnTable;
import l2jorion.game.handler.AdminCommandHandler;
import l2jorion.game.handler.IAdminCommandHandler;
import l2jorion.game.handler.ICustomByPassHandler;
import l2jorion.game.handler.IVoicedCommandHandler;
import l2jorion.game.model.L2Character;
import l2jorion.game.model.L2Party;
import l2jorion.game.model.L2World;
import l2jorion.game.model.Location;
import l2jorion.game.model.actor.instance.L2MonsterInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.entity.Announcements;
import l2jorion.game.model.entity.event.manager.EventTask;
import l2jorion.game.model.entity.event.manager.EventsGlobalTask;
import l2jorion.game.model.spawn.L2Spawn;
import l2jorion.game.model.zone.ZoneId;
import l2jorion.game.network.clientpackets.Say2;
import l2jorion.game.network.serverpackets.CreatureSay;
import l2jorion.game.network.serverpackets.ExShowScreenMessage;
import l2jorion.game.network.serverpackets.MagicSkillUser;
import l2jorion.game.network.serverpackets.NpcHtmlMessage;
import l2jorion.game.network.serverpackets.NpcInfo;
import l2jorion.game.network.serverpackets.PlaySound;
import l2jorion.game.network.serverpackets.SetupGauge;
import l2jorion.game.skills.Stats;
import l2jorion.game.skills.funcs.FuncMul;
import l2jorion.game.skills.funcs.LambdaConst;
import l2jorion.game.templates.L2Item;
import l2jorion.game.templates.L2NpcTemplate;
import l2jorion.game.thread.ThreadPoolManager;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;
import l2jorion.util.random.Rnd;

public class PartyZone implements IVoicedCommandHandler, ICustomByPassHandler, IAdminCommandHandler
{
	protected static final Logger LOG = LoggerFactory.getLogger(PartyZone.class);
	private static final String CONFIG_FILE = "./config/mods/events/partyzone.ini";
	private static PartyZone _instance;
	
	// Configs
	public static boolean EVENT_ENABLED = false;
	public static String EVENT_NAME = "Party Zone";
	public static int DURATION_MINUTES = 25;
	public static int REGISTRATION_MINUTES = 5;
	public static List<String> EVENT_TIMES = new ArrayList<>();
	
	public static int POINTS_PER_MOB = 1;
	public static int POINTS_PER_PVP = 5;
	public static double BOUNTY_MULTIPLIER = 2.0;
	public static boolean CHECK_DUALBOX = true;
	
	public static int NPC_ID = 0, NPC_X = 0, NPC_Y = 0, NPC_Z = 0, NPC_HEADING = 0;
	private static L2Spawn _npcSpawn;
	
	public static int TELEPORT_DELAY = 20;
	public static int TELEPORT_DELAY_REJOIN = 50;
	public static int SPAWN_PROTECTION_SEC = 5;
	public static int ENTRY_FEE_ID = 57, ENTRY_FEE_COUNT = 50;
	public static int MIN_LEVEL = 76;
	public static int INSTANCE_ID = 201;
	public static int REENTRY_COOLDOWN = 80;
	public static int RADIUS_TO_REWARD = 1500;
	
	public static final Map<Integer, Integer> CLASS_LIMITS = new HashMap<>();
	public static final List<Integer> MONSTER_IDS = new ArrayList<>();
	public static String MOB_TITLE = "Party Zone";
	public static int MOB_RESPAWN = 7;
	public static boolean MOB_ENABLE_EFFECT = true;
	public static String MOB_EFFECT_NAME = "FLAME";
	
	public static int BOSS_ID = 0;
	public static int BOSS_SPAWN_DELAY = 15;
	public static int BOSS_POINTS = 50;
	public static int MIRROR_SPAWN_RADIUS = 2000;
	
	public static double MOB_HP_MUL = 1.0, MOB_PATK_MUL = 1.0, MOB_PATK_SPEED_MUL = 1.0, MOB_MATK_MUL = 1.0;
	public static double MOB_PDEF_MUL = 1.0, MOB_MDEF_MUL = 1.0;
	
	public static final List<DropData> DROP_LIST = new ArrayList<>();
	public static String REWARD_SOUND = "ItemSound.quest_fanfare_1";
	
	public static int REWARD_TOP1_ID = 57, REWARD_TOP1_COUNT = 100;
	public static int REWARD_TOP2_ID = 57, REWARD_TOP2_COUNT = 50;
	public static int REWARD_TOP3_ID = 57, REWARD_TOP3_COUNT = 25;
	private static String _locationName = "Unknown Area";
	private static final List<Location> LOCATIONS = new ArrayList<>();
	private static final List<L2Spawn> _spawnedMobs = new CopyOnWriteArrayList<>();
	private static final List<L2Spawn> _cachedMirrorSpawns = new ArrayList<>();
	
	private static final Map<Integer, Location> _originalLocations = new ConcurrentHashMap<>();
	private static final List<L2PcInstance> _playersInside = new CopyOnWriteArrayList<>();
	private static final Map<Integer, Long> _reentryCooldowns = new ConcurrentHashMap<>();
	private static final Map<Integer, ScheduledFuture<?>> _deadTimers = new ConcurrentHashMap<>();
	private static final Map<Integer, PartyStats> _partyStats = new ConcurrentHashMap<>();
	
	public enum EventState
	{
		INACTIVE,
		REGISTRATION,
		RUNNING,
		FINISHING
	}
	
	private static EventState _state = EventState.INACTIVE;
	private static ScheduledFuture<?> _task = null, _announceTask = null, _monitorTask = null, _bossTask = null;
	private static long _eventEndTime = 0;
	private static long _registrationEndTime = 0;
	
	public static class DropData
	{
		int id, count, chance;
		
		public DropData(int id, int count, int chance)
		{
			this.id = id;
			this.count = count;
			this.chance = chance;
		}
	}
	
	private static class PartyStats
	{
		String leaderName;
		int mobKills = 0;
		int pvpKills = 0;
		int score = 0;
		
		PartyStats(String name)
		{
			this.leaderName = name;
		}
	}
	
	public static PartyZone getInstance()
	{
		if (_instance == null)
		{
			_instance = new PartyZone();
		}
		return _instance;
	}
	
	public PartyZone()
	{
		loadConfig();
	}
	
	public static class EventSchedule implements EventTask
	{
		private final String _time;
		
		public EventSchedule(String time)
		{
			_time = time;
		}
		
		@Override
		public String getEventIdentifier()
		{
			return EVENT_NAME;
		}
		
		@Override
		public String getEventStartTime()
		{
			return _time;
		}
		
		@Override
		public void run()
		{
			PartyZone.getInstance().startRegistration();
		}
	}
	
	public static void register()
	{
		EventsGlobalTask.getInstance().clearEventTasksByEventName(PartyZone.EVENT_NAME);
		PartyZone.getInstance().loadConfig();
		AdminCommandHandler.getInstance().registerAdminCommandHandler(PartyZone.getInstance());
		
		if (!PartyZone.EVENT_ENABLED)
		{
			return;
		}
		
		for (String time : PartyZone.EVENT_TIMES)
		{
			EventsGlobalTask.getInstance().registerNewEventTask(new PartyZone.EventSchedule(time));
		}
	}
	
	public void loadConfig()
	{
		try (InputStream is = new FileInputStream(new File(CONFIG_FILE)))
		{
			Properties set = new Properties();
			set.load(is);
			
			EVENT_ENABLED = Boolean.parseBoolean(set.getProperty("EventEnabled", "true"));
			EVENT_NAME = set.getProperty("EventName", "Party Zone");
			DURATION_MINUTES = Integer.parseInt(set.getProperty("EventDurationMinutes", "25"));
			REGISTRATION_MINUTES = Integer.parseInt(set.getProperty("RegistrationTimeMinutes", "5"));
			
			String times = set.getProperty("EventStartTimes", "19:00");
			EVENT_TIMES.clear();
			for (String t : times.split(";"))
			{
				if (!t.trim().isEmpty())
				{
					EVENT_TIMES.add(t.trim());
				}
			}
			
			NPC_ID = Integer.parseInt(set.getProperty("NpcId", "0"));
			NPC_X = Integer.parseInt(set.getProperty("NpcX", "0"));
			NPC_Y = Integer.parseInt(set.getProperty("NpcY", "0"));
			NPC_Z = Integer.parseInt(set.getProperty("NpcZ", "0"));
			NPC_HEADING = Integer.parseInt(set.getProperty("NpcHeading", "0"));
			
			TELEPORT_DELAY = Integer.parseInt(set.getProperty("TeleportDelaySeconds", "20"));
			TELEPORT_DELAY_REJOIN = Integer.parseInt(set.getProperty("TeleportDelaySecondsTryLeader", "50"));
			
			ENTRY_FEE_ID = Integer.parseInt(set.getProperty("EntryFeeId", "57"));
			ENTRY_FEE_COUNT = Integer.parseInt(set.getProperty("EntryFeeAmount", "50"));
			MIN_LEVEL = Integer.parseInt(set.getProperty("MinLevel", "76"));
			
			POINTS_PER_MOB = Integer.parseInt(set.getProperty("PointperMonsters", "1"));
			POINTS_PER_PVP = Integer.parseInt(set.getProperty("PointperPlayers", "5"));
			BOUNTY_MULTIPLIER = Double.parseDouble(set.getProperty("BountyMultiplier", "2.0"));
			CHECK_DUALBOX = Boolean.parseBoolean(set.getProperty("CheckDualBox", "True"));
			
			SPAWN_PROTECTION_SEC = Integer.parseInt(set.getProperty("SpawnProtection", "5"));
			
			String classLimits = set.getProperty("MaxClassIdsPerPT", "");
			CLASS_LIMITS.clear();
			if (!classLimits.isEmpty())
			{
				for (String pair : classLimits.split(";"))
				{
					String[] val = pair.split(",");
					if (val.length == 2)
					{
						CLASS_LIMITS.put(Integer.parseInt(val[0]), Integer.parseInt(val[1]));
					}
				}
			}
			
			INSTANCE_ID = Integer.parseInt(set.getProperty("InstanceId", "201"));
			REENTRY_COOLDOWN = Integer.parseInt(set.getProperty("ReentryCooldownSeconds", "80"));
			RADIUS_TO_REWARD = Integer.parseInt(set.getProperty("RadiusToPartyZone", "1500"));
			
			String locs = set.getProperty("PTZoneLocations", "").replace(";", "").trim();
			LOCATIONS.clear();
			
			if (!locs.isEmpty())
			{
				String[] parts = locs.split(",");
				if (parts.length > 0)
				{
					_locationName = parts[0].trim(); // Pega o nome "Beast Farm"
					
					for (int i = 1; i < parts.length - 2; i += 3)
					{
						try
						{
							LOCATIONS.add(new Location(Integer.parseInt(parts[i].trim()), Integer.parseInt(parts[i + 1].trim()), Integer.parseInt(parts[i + 2].trim())));
						}
						catch (Exception e)
						{
							LOG.warn("PartyZone: Error parsing location at index " + i);
						}
					}
				}
			}
			
			String mobs = set.getProperty("MonsterIds", "");
			MONSTER_IDS.clear();
			for (String id : mobs.split(","))
			{
				if (!id.trim().isEmpty())
				{
					MONSTER_IDS.add(Integer.parseInt(id.trim()));
				}
			}
			
			MOB_TITLE = set.getProperty("MonsterTitle", "Party Zone");
			MOB_RESPAWN = Integer.parseInt(set.getProperty("RespawnDelaySeconds", "7"));
			MOB_ENABLE_EFFECT = Boolean.parseBoolean(set.getProperty("EnableMobEffect", "True"));
			MOB_EFFECT_NAME = set.getProperty("MobEffectName", "FLAME");
			MIRROR_SPAWN_RADIUS = Integer.parseInt(set.getProperty("MirrorSpawnRadius", "2000"));
			BOSS_ID = Integer.parseInt(set.getProperty("EventBossId", "0"));
			BOSS_SPAWN_DELAY = Integer.parseInt(set.getProperty("EventBossSpawnTime", "12"));
			BOSS_POINTS = Integer.parseInt(set.getProperty("EventBossPoints", "50"));
			
			MOB_HP_MUL = Double.parseDouble(set.getProperty("PTZoneMobHpMultiplier", "1.0"));
			MOB_PATK_MUL = Double.parseDouble(set.getProperty("PTZoneMobPAtkMultiplier", "1.0"));
			MOB_PATK_SPEED_MUL = Double.parseDouble(set.getProperty("PTZoneMobPAtkSpeedMultiplier", "1.0"));
			MOB_MATK_MUL = Double.parseDouble(set.getProperty("PTZoneMobMAtkMultiplier", "1.0"));
			MOB_PDEF_MUL = Double.parseDouble(set.getProperty("PTZoneMobPDefMultiplier", "1.0"));
			MOB_MDEF_MUL = Double.parseDouble(set.getProperty("PTZoneMobMDefMultiplier", "1.0"));
			
			String drops = set.getProperty("PTZoneDropReward", "");
			DROP_LIST.clear();
			if (!drops.isEmpty())
			{
				for (String d : drops.split(";"))
				{
					String[] v = d.split(",");
					if (v.length == 3)
					{
						DROP_LIST.add(new DropData(Integer.parseInt(v[0]), Integer.parseInt(v[1]), Integer.parseInt(v[2])));
					}
				}
			}
			
			REWARD_SOUND = set.getProperty("RewardSound", "ItemSound.quest_fanfare_1");
			
			String[] rw1 = set.getProperty("RewardPTZoneTop1", "57,100").split(",");
			REWARD_TOP1_ID = Integer.parseInt(rw1[0]);
			REWARD_TOP1_COUNT = Integer.parseInt(rw1[1]);
			
			String[] rw2 = set.getProperty("RewardPTZoneTop2", "57,50").split(",");
			REWARD_TOP2_ID = Integer.parseInt(rw2[0]);
			REWARD_TOP2_COUNT = Integer.parseInt(rw2[1]);
			
			String[] rw3 = set.getProperty("RewardPTZoneTop3", "57,25").split(",");
			REWARD_TOP3_ID = Integer.parseInt(rw3[0]);
			REWARD_TOP3_COUNT = Integer.parseInt(rw3[1]);
			cacheMirrorSpawns();
		}
		catch (Exception e)
		{
			LOG.warn("PartyZone: Config load failed: " + e.getMessage());
		}
	}
	
	private void cacheMirrorSpawns()
	{
		_cachedMirrorSpawns.clear();
		if (LOCATIONS.isEmpty())
		{
			return;
		}
		
		Map<Integer, L2Spawn> allSpawns = SpawnTable.getInstance().getSpawnTable();
		int cachedCount = 0;
		
		for (L2Spawn originalSpawn : allSpawns.values())
		{
			if (originalSpawn == null)
			{
				continue;
			}
			if (originalSpawn.getTemplate() == null || !originalSpawn.getTemplate().isType("L2Monster"))
			{
				continue;
			}
			
			for (Location loc : LOCATIONS)
			{
				double dist = Math.sqrt(Math.pow(originalSpawn.getLocx() - loc.getX(), 2) + Math.pow(originalSpawn.getLocy() - loc.getY(), 2));
				if (dist <= MIRROR_SPAWN_RADIUS)
				{
					_cachedMirrorSpawns.add(originalSpawn);
					cachedCount++;
					break;
				}
			}
		}
		LOG.info("PartyZone: Performance Cache -> " + cachedCount + " spawns ready for mirror.");
	}
	
	public void startRegistration()
	{
		if (_state != EventState.INACTIVE)
		{
			return;
		}
		loadConfig();
		if (!EVENT_ENABLED)
		{
			return;
		}
		
		_state = EventState.REGISTRATION;
		_playersInside.clear();
		_partyStats.clear();
		_deadTimers.clear();
		
		_registrationEndTime = System.currentTimeMillis() + (REGISTRATION_MINUTES * 60 * 1000);
		
		spawnEventNpc();
		
		Announcements.getInstance().gameAnnounceToAll("====================================");
		Announcements.getInstance().gameAnnounceToAll(EVENT_NAME + ": REGISTRATION OPENED!");
		Announcements.getInstance().gameAnnounceToAll("Location: " + _locationName);
		Announcements.getInstance().gameAnnounceToAll("Battle starts in " + REGISTRATION_MINUTES + " minutes!");
		Announcements.getInstance().gameAnnounceToAll("Use .ptzjoin or visit the NPC to register.");
		Announcements.getInstance().gameAnnounceToAll("====================================");
		
		startCountdownTask(REGISTRATION_MINUTES * 60);
		
		_task = ThreadPoolManager.getInstance().scheduleGeneral(this::startRunningPhase, REGISTRATION_MINUTES * 60 * 1000);
	}
	
	public void startRunningPhase()
	{
		if (_state != EventState.REGISTRATION)
		{
			return;
		}
		
		if (_playersInside.size() < 2)
		{
			Announcements.getInstance().gameAnnounceToAll(EVENT_NAME + ": Event cancelled due to lack of players.");
			stopEvent();
			return;
		}
		
		_state = EventState.RUNNING;
		_eventEndTime = System.currentTimeMillis() + (DURATION_MINUTES * 60 * 1000);
		
		spawnMonsters();
		
		if (BOSS_ID > 0)
		{
			long delay = BOSS_SPAWN_DELAY * 60 * 1000;
			if (delay < (DURATION_MINUTES * 60 * 1000))
			{
				_bossTask = ThreadPoolManager.getInstance().scheduleGeneral(this::spawnBoss, delay);
			}
		}
		
		massTeleportPlayers();
		
		Announcements.getInstance().gameAnnounceToAll(EVENT_NAME + ": BATTLE STARTED AT " + _locationName + "!");
		Announcements.getInstance().gameAnnounceToAll(EVENT_NAME + ": Late Join is Enabled! Type .ptzjoin to enter.");
		
		if (_task != null)
		{
			_task.cancel(false);
		}
		_task = ThreadPoolManager.getInstance().scheduleGeneral(this::stopEvent, DURATION_MINUTES * 60 * 1000);
		
		startAnnounceTask();
		startMonitorTask();
	}
	
	public void stopEvent()
	{
		_state = EventState.INACTIVE;
		
		cancelTasks();
		processRewards();
		despawnMonsters();
		unspawnEventNpc();
		
		Announcements.getInstance().gameAnnounceToAll(EVENT_NAME + ": Ended!");
		
		for (L2PcInstance player : new ArrayList<>(_playersInside))
		{
			exitEvent(player);
		}
		
		_playersInside.clear();
		_partyStats.clear();
		_deadTimers.clear();
	}
	
	private void cancelTasks()
	{
		if (_task != null)
		{
			_task.cancel(false);
		}
		if (_announceTask != null)
		{
			_announceTask.cancel(false);
		}
		if (_monitorTask != null)
		{
			_monitorTask.cancel(false);
		}
		if (_bossTask != null)
		{
			_bossTask.cancel(false);
		}
		
		_task = null;
		_announceTask = null;
		_monitorTask = null;
		_bossTask = null;
		
		for (ScheduledFuture<?> f : _deadTimers.values())
		{
			if (f != null && !f.isDone())
			{
				f.cancel(false);
			}
		}
	}
	
	private void requestEntry(L2PcInstance player)
	{
		if (_state == EventState.INACTIVE || _state == EventState.FINISHING)
		{
			player.sendMessage("Event is not active.");
			return;
		}
		
		if (_playersInside.contains(player) || player.getInstanceId() == INSTANCE_ID)
		{
			player.sendMessage("You are already registered/inside.");
			return;
		}
		
		L2Party party = player.getParty();
		if (party == null)
		{
			player.sendMessage("You must be in a Party to join.");
			return;
		}
		
		if (!party.isLeader(player))
		{
			player.sendMessage("Only the Party Leader can register/join.");
			return;
		}
		L2PcInstance alreadyInsideMember = null;
		for (L2PcInstance m : party.getPartyMembers())
		{
			if (_playersInside.contains(m) && m.getInstanceId() == INSTANCE_ID)
			{
				alreadyInsideMember = m;
				break;
			}
		}
		
		boolean isRejoin = (alreadyInsideMember != null);
		
		if (party.getMemberCount() < 2 && !isRejoin)
		{
			player.sendMessage("Party must have at least 2 members.");
			return;
		}
		if (checkCooldown(player))
		{
			return;
		}
		if (!validateClassLimits(party))
		{
			player.sendMessage("Class limits exceeded.");
			return;
		}
		for (L2PcInstance member : party.getPartyMembers())
		{
			if (isRejoin && _playersInside.contains(member))
			{
				continue;
			}
			
			if (member == null || member.isOnline() == 0)
			{
				continue;
			}
			
			if (!validateBasicConditions(member))
			{
				player.sendMessage(member.getName() + " does not meet entry conditions (Level, Olympiad, etc).");
				return;
			}
		}
		List<L2PcInstance> toTeleport = new ArrayList<>();
		
		if (ENTRY_FEE_COUNT > 0)
		{
			for (L2PcInstance member : party.getPartyMembers())
			{
				if (isRejoin && _playersInside.contains(member))
				{
					continue;
				}
				
				if (member.getInventory().getInventoryItemCount(ENTRY_FEE_ID, -1) < ENTRY_FEE_COUNT)
				{
					player.sendMessage(member.getName() + " does not have enough items.");
					return;
				}
			}
			for (L2PcInstance member : party.getPartyMembers())
			{
				if (isRejoin && _playersInside.contains(member))
				{
					continue;
				}
				
				member.destroyItemByItemId("PartyZoneReg", ENTRY_FEE_ID, ENTRY_FEE_COUNT, member, true);
				toTeleport.add(member);
			}
		}
		else
		{
			for (L2PcInstance member : party.getPartyMembers())
			{
				if (isRejoin && _playersInside.contains(member))
				{
					continue;
				}
				toTeleport.add(member);
			}
		}
		if (_state == EventState.REGISTRATION)
		{
			int registeredCount = 0;
			for (L2PcInstance member : toTeleport)
			{
				if (!_playersInside.contains(member))
				{
					_playersInside.add(member);
					registeredCount++;
					int secondsLeft = (int) ((_registrationEndTime - System.currentTimeMillis()) / 1000);
					member.sendMessage("You are REGISTERED! Teleport in approx " + (secondsLeft / 60) + " min " + (secondsLeft % 60) + " sec.");
					member.sendPacket(new ExShowScreenMessage("Registered! Wait for start!", 5000));
				}
			}
			_partyStats.putIfAbsent(party.getPartyLeaderOID(), new PartyStats(party.getLeader().getName()));
			player.sendMessage(registeredCount + " members registered successfully.");
		}
		else if (_state == EventState.RUNNING)
		{
			Location destLoc;
			if (isRejoin && alreadyInsideMember != null)
			{
				destLoc = new Location(alreadyInsideMember.getX(), alreadyInsideMember.getY(), alreadyInsideMember.getZ());
			}
			else
			{
				if (LOCATIONS.isEmpty())
				{
					return;
				}
				destLoc = LOCATIONS.get(Rnd.get(LOCATIONS.size()));
			}
			
			for (L2PcInstance member : toTeleport)
			{
				member.disableAllSkills();
				member.broadcastPacket(new MagicSkillUser(member, member, 1050, 1, TELEPORT_DELAY * 1000, 0));
				member.sendPacket(new SetupGauge(0, TELEPORT_DELAY * 1000));
				member.sendMessage("Teleporting to Party Zone in " + TELEPORT_DELAY + " seconds...");
				
				final Location finalLoc = destLoc;
				ThreadPoolManager.getInstance().scheduleGeneral(() -> executeTeleportPlayer(member, finalLoc), TELEPORT_DELAY * 1000);
			}
			if (!isRejoin)
			{
				_partyStats.putIfAbsent(party.getPartyLeaderOID(), new PartyStats(party.getLeader().getName()));
			}
		}
	}
	
	private void massTeleportPlayers()
	{
		if (LOCATIONS.isEmpty())
		{
			LOG.warn("PartyZone: No Locations defined!");
			return;
		}
		
		Location locBase = LOCATIONS.get(Rnd.get(LOCATIONS.size()));
		
		for (L2PcInstance player : _playersInside)
		{
			if (player != null && player.isOnline() == 1)
			{
				if (player.getParty() == null)
				{
					player.sendMessage("You left the party during registration. Entry failed.");
					_playersInside.remove(player);
					continue;
				}
				executeTeleportPlayer(player, locBase);
			}
		}
	}
	
	private void executeTeleportPlayer(L2PcInstance player, Location targetLoc)
	{
		if (player == null || !(player.isOnline() == 1))
		{
			return;
		}
		
		_originalLocations.put(player.getObjectId(), new Location(player.getX(), player.getY(), player.getZ()));
		player.setInstanceId(INSTANCE_ID);
		player.setInsideZone(ZoneId.ZONE_PVP, true);
		player.teleToLocation(targetLoc.getX() + Rnd.get(-100, 100), targetLoc.getY() + Rnd.get(-100, 100), targetLoc.getZ(), true);
		applySpawnProtection(player);
		player.enableAllSkills();
		player.sendPacket(new ExShowScreenMessage("Party Zone: FIGHT!", 5000));
		
		if (!_playersInside.contains(player))
		{
			_playersInside.add(player);
		}
	}
	
	private boolean checkCooldown(L2PcInstance player)
	{
		if (_reentryCooldowns.containsKey(player.getObjectId()))
		{
			long diff = _reentryCooldowns.get(player.getObjectId()) - System.currentTimeMillis();
			if (diff > 0)
			{
				player.sendMessage("Re-entry cooldown: " + (diff / 1000) + " seconds.");
				return true;
			}
			_reentryCooldowns.remove(player.getObjectId());
		}
		return false;
	}
	
	private boolean validateClassLimits(L2Party party)
	{
		if (CLASS_LIMITS.isEmpty())
		{
			return true;
		}
		Map<Integer, Integer> currentCounts = new HashMap<>();
		for (L2PcInstance member : party.getPartyMembers())
		{
			if (member == null)
			{
				continue;
			}
			int cid = member.getClassId().getId();
			currentCounts.put(cid, currentCounts.getOrDefault(cid, 0) + 1);
		}
		for (Map.Entry<Integer, Integer> limit : CLASS_LIMITS.entrySet())
		{
			if (currentCounts.getOrDefault(limit.getKey(), 0) > limit.getValue())
			{
				return false;
			}
		}
		return true;
	}
	
	@SuppressWarnings("null")
	private void applySpawnProtection(L2PcInstance player)
	{
		if (SPAWN_PROTECTION_SEC <= 0)
		{
			return;
		}
		player.setIsInvul(true);
		player.startAbnormalEffect(L2Character.ABNORMAL_EFFECT_IMPRISIONING_2);
		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			if (player != null && player.isOnline() == 1)
			{
				player.setIsInvul(false);
				player.stopAbnormalEffect(L2Character.ABNORMAL_EFFECT_IMPRISIONING_2);
				player.sendMessage("Spawn protection expired.");
			}
		}, SPAWN_PROTECTION_SEC * 1000);
	}
	
	private void requestLeave(L2PcInstance player)
	{
		if (!_playersInside.contains(player))
		{
			return;
		}
		if (player.isInCombat() || player.isAttackingNow())
		{
			player.sendMessage("Cannot leave during combat.");
			return;
		}
		
		player.sendMessage("Leaving in 5 seconds...");
		player.disableAllSkills();
		player.sendPacket(new SetupGauge(0, 5000));
		ThreadPoolManager.getInstance().scheduleGeneral(() -> exitEvent(player), 5000);
	}
	
	private void exitEvent(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		ScheduledFuture<?> task = _deadTimers.remove(player.getObjectId());
		if (task != null)
		{
			task.cancel(false);
		}
		player.setInstanceId(0);
		player.setInsideZone(ZoneId.ZONE_PVP, false);
		player.setIsInvul(false);
		player.stopAbnormalEffect(L2Character.ABNORMAL_EFFECT_IMPRISIONING_2);
		_originalLocations.remove(player.getObjectId());
		player.teleToLocation(81183, 148584, -3472, true);
		
		_playersInside.remove(player);
		
		if (REENTRY_COOLDOWN > 0)
		{
			_reentryCooldowns.put(player.getObjectId(), System.currentTimeMillis() + (REENTRY_COOLDOWN * 1000));
		}
		player.enableAllSkills();
	}
	
	private void startCountdownTask(int totalSeconds)
	{
		startCountDown(totalSeconds);
	}
	
	public void startCountDown(int time)
	{
		if (_state != EventState.REGISTRATION)
		{
			return;
		}
		
		Collection<L2PcInstance> players = L2World.getInstance().getAllPlayers().values();
		
		String msg = null;
		if (time > 60 && time % 60 == 0)
		{
			msg = "Party Zone: Starts in " + (time / 60) + " minute(s)!";
		}
		else if (time <= 10 && time > 0)
		{
			msg = "Party Zone: Starts in " + time + " second(s)!";
		}
		
		if (msg != null)
		{
			for (L2PcInstance player : players)
			{
				player.sendPacket(new ExShowScreenMessage(msg, 3000));
				player.sendPacket(new CreatureSay(0, Say2.CRITICAL_ANNOUNCE, "[PartyZone]", msg));
			}
		}
		
		if (time > 0)
		{
			int nextDelay = 1000;
			int nextTime = time - 1;
			
			if (time > 60)
			{
				nextDelay = 60000;
				nextTime = time - 60;
			}
			else if (time > 10)
			{
				nextDelay = (time - 10) * 1000;
				nextTime = 10;
			}
			
			final int fNextTime = nextTime;
			ThreadPoolManager.getInstance().scheduleGeneral(() -> startCountDown(fNextTime), nextDelay);
		}
	}
	
	public void onDeath(L2Character victim, L2Character killer)
	{
		if (_state != EventState.RUNNING)
		{
			return;
		}
		if (victim instanceof L2PcInstance)
		{
			L2PcInstance playerVictim = (L2PcInstance) victim;
			if (_playersInside.contains(playerVictim))
			{
				handleKillStats(killer, playerVictim);
				playerVictim.sendMessage("You will be kicked in 5 minutes if you don't revive.");
				ScheduledFuture<?> kickTask = ThreadPoolManager.getInstance().scheduleGeneral(() ->
				{
					if (playerVictim.isDead() && _playersInside.contains(playerVictim))
					{
						exitEvent(playerVictim);
					}
				}, 5 * 60 * 1000);
				_deadTimers.put(playerVictim.getObjectId(), kickTask);
			}
		}
		else if (victim instanceof L2MonsterInstance)
		{
			if (killer != null && killer.getActingPlayer() != null && _playersInside.contains(killer.getActingPlayer()))
			{
				handleKillStats(killer, victim);
			}
		}
	}
	
	public void onRevive(L2PcInstance player)
	{
		ScheduledFuture<?> task = _deadTimers.remove(player.getObjectId());
		if (task != null)
		{
			task.cancel(false);
		}
	}
	
	private void handleKillStats(L2Character killerChar, L2Character victim)
	{
		if (killerChar == null || !(killerChar instanceof L2PcInstance))
		{
			return;
		}
		L2PcInstance killer = (L2PcInstance) killerChar;
		
		if (killer.getParty() == null)
		{
			return;
		}
		
		int leaderId = killer.getParty().getPartyLeaderOID();
		PartyStats stats = _partyStats.computeIfAbsent(leaderId, k -> new PartyStats(killer.getParty().getLeader().getName()));
		int pointsEarned = 0;
		String msgSuffix = "";
		if (victim instanceof L2MonsterInstance)
		{
			L2MonsterInstance mob = (L2MonsterInstance) victim;
			
			// Boss
			if (mob.getNpcId() == BOSS_ID)
			{
				stats.score += BOSS_POINTS;
				Announcements.getInstance().gameAnnounceToAll(EVENT_NAME + ": Boss killed by party " + stats.leaderName + "!");
				processDrops(killer, mob);
				return;
			}
			if (MONSTER_IDS.contains(mob.getNpcId()))
			{
				stats.mobKills++;
				stats.score += POINTS_PER_MOB;
				pointsEarned = POINTS_PER_MOB;
				processDrops(killer, mob);
			}
		}
		else if (victim instanceof L2PcInstance)
		{
			L2PcInstance vPlayer = (L2PcInstance) victim;
			if (vPlayer.getParty() != null && vPlayer.getParty() == killer.getParty())
			{
				return;
			}
			if (vPlayer.getClanId() > 0 && vPlayer.getClanId() == killer.getClanId())
			{
				return;
			}
			if (CHECK_DUALBOX)
			{
				String kIP = killer.getClient().getConnection().getInetAddress().getHostAddress();
				String vIP = vPlayer.getClient().getConnection().getInetAddress().getHostAddress();
				if (kIP.equals(vIP))
				{
					killer.sendMessage("Anti-Feed: No points for same IP.");
					return;
				}
			}
			
			stats.pvpKills++;
			boolean isBounty = false;
			PartyStats topParty = getTopParty();
			if (topParty != null && vPlayer.getParty() != null && vPlayer.getParty().getPartyLeaderOID() == getPartyLeaderOIDByName(topParty.leaderName))
			{
				isBounty = true;
			}
			
			int pvpPoints = (int) (isBounty ? POINTS_PER_PVP * BOUNTY_MULTIPLIER : POINTS_PER_PVP);
			stats.score += pvpPoints;
			pointsEarned = pvpPoints;
			
			if (isBounty)
			{
				msgSuffix = " (Bounty!)";
			}
		}
		if (pointsEarned > 0)
		{
			killer.sendPacket(new ExShowScreenMessage("+" + pointsEarned + " Party Points" + msgSuffix + "!", 2000));
			killer.sendMessage("Party Zone: You earned " + pointsEarned + " points.");
		}
	}
	
	private PartyStats getTopParty()
	{
		if (_partyStats.isEmpty())
		{
			return null;
		}
		return _partyStats.values().stream().max(Comparator.comparingInt(p -> p.score)).orElse(null);
	}
	
	private int getPartyLeaderOIDByName(String name)
	{
		L2PcInstance p = L2World.getInstance().getPlayer(name);
		return (p != null && p.getParty() != null) ? p.getParty().getPartyLeaderOID() : 0;
	}
	
	private void processDrops(L2PcInstance killer, L2MonsterInstance mob)
	{
		for (DropData drop : DROP_LIST)
		{
			if (Rnd.get(100) < drop.chance)
			{
				killer.addItem("PartyZoneDrop", drop.id, drop.count, mob, true);
			}
		}
	}
	
	private void spawnEventNpc()
	{
		if (NPC_ID == 0)
		{
			return;
		}
		try
		{
			L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(NPC_ID);
			if (tmpl != null)
			{
				_npcSpawn = new L2Spawn(tmpl);
				_npcSpawn.setLocx(NPC_X);
				_npcSpawn.setLocy(NPC_Y);
				_npcSpawn.setLocz(NPC_Z);
				_npcSpawn.setHeading(NPC_HEADING);
				_npcSpawn.setAmount(1);
				_npcSpawn.setRespawnDelay(10);
				
				SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);
				_npcSpawn.init();
				
				if (_npcSpawn.getLastSpawn() != null)
				{
					_npcSpawn.getLastSpawn().setTitle(EVENT_NAME);
					_npcSpawn.getLastSpawn().broadcastPacket(new MagicSkillUser(_npcSpawn.getLastSpawn(), _npcSpawn.getLastSpawn(), 1034, 1, 1, 1));
					_npcSpawn.getLastSpawn().broadcastPacket(new NpcInfo(_npcSpawn.getLastSpawn(), null));
				}
				else
				{
					LOG.warn("PartyZone: init() returned null spawn, forcing doSpawn()...");
					_npcSpawn.stopRespawn();
					_npcSpawn.doSpawn();
					
					if (_npcSpawn.getLastSpawn() != null)
					{
						_npcSpawn.getLastSpawn().setTitle(EVENT_NAME);
						_npcSpawn.getLastSpawn().broadcastPacket(new NpcInfo(_npcSpawn.getLastSpawn(), null));
					}
					else
					{
						LOG.error("PartyZone: Failed to spawn Event NPC even after force.");
					}
				}
			}
			else
			{
				LOG.warn("PartyZone: NPC ID " + NPC_ID + " not found in NpcTable.");
			}
		}
		catch (Exception e)
		{
			LOG.error("PartyZone: Error spawning NPC: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void unspawnEventNpc()
	{
		if (_npcSpawn != null)
		{
			_npcSpawn.getLastSpawn().deleteMe();
			SpawnTable.getInstance().deleteSpawn(_npcSpawn, true);
			_npcSpawn = null;
		}
	}
	
	private void spawnBoss()
	{
		if (BOSS_ID == 0 || LOCATIONS.isEmpty())
		{
			return;
		}
		try
		{
			Location loc = LOCATIONS.get(Rnd.get(LOCATIONS.size()));
			L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(BOSS_ID);
			if (tmpl != null)
			{
				L2Spawn s = new L2Spawn(tmpl);
				s.setLocx(loc.getX());
				s.setLocy(loc.getY());
				s.setLocz(loc.getZ());
				s.setAmount(1);
				s.setInstanceId(INSTANCE_ID);
				s.setRespawnDelay(0);
				
				L2MonsterInstance boss = (L2MonsterInstance) s.doSpawn();
				boss.setTitle("Event Boss");
				boss.broadcastPacket(new MagicSkillUser(boss, boss, 2036, 1, 1000, 0)); // Visual Effect
				Announcements.getInstance().gameAnnounceToAll(EVENT_NAME + ": The Event Boss has spawned! Hunt it down!");
				_spawnedMobs.add(s);
			}
		}
		catch (Exception e)
		{
			LOG.warn("Error spawning Boss: " + e.getMessage());
		}
	}
	
	private void spawnMonsters()
	{
		if (MONSTER_IDS.isEmpty())
		{
			return;
		}
		
		if (_cachedMirrorSpawns.isEmpty())
		{
			cacheMirrorSpawns();
		}
		
		int count = 0;
		for (L2Spawn originalSpawn : _cachedMirrorSpawns)
		{
			int eventMobId = MONSTER_IDS.get(Rnd.get(MONSTER_IDS.size()));
			L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(eventMobId);
			if (tmpl == null)
			{
				continue;
			}
			
			try
			{
				L2Spawn spawn = new L2Spawn(tmpl);
				spawn.setLocx(originalSpawn.getLocx());
				spawn.setLocy(originalSpawn.getLocy());
				spawn.setLocz(originalSpawn.getLocz());
				spawn.setHeading(originalSpawn.getHeading());
				spawn.setAmount(1);
				spawn.setRespawnDelay(MOB_RESPAWN);
				spawn.setInstanceId(INSTANCE_ID);
				
				L2MonsterInstance mob = (L2MonsterInstance) spawn.doSpawn();
				mob.setTitle(MOB_TITLE);
				applyMobStats(mob);
				spawn.startRespawn();
				
				if (MOB_ENABLE_EFFECT)
				{
					try
					{
						mob.startAbnormalEffect(getAbnormalMask(MOB_EFFECT_NAME));
					}
					catch (Exception e)
					{
					}
				}
				
				_spawnedMobs.add(spawn);
				count++;
			}
			catch (Exception e)
			{
				LOG.warn("PartyZone: Spawn Error: " + e.getMessage());
			}
		}
		LOG.info("PartyZone: " + count + " monsters mirrored (Fast Mode).");
	}
	
	private void applyMobStats(L2MonsterInstance mob)
	{
		if (MOB_HP_MUL != 1.0)
		{
			mob.addStatFunc(new FuncMul(Stats.MAX_HP, 0x40, mob, new LambdaConst(MOB_HP_MUL)));
		}
		if (MOB_PATK_MUL != 1.0)
		{
			mob.addStatFunc(new FuncMul(Stats.POWER_ATTACK, 0x40, mob, new LambdaConst(MOB_PATK_MUL)));
		}
		if (MOB_PATK_SPEED_MUL != 1.0)
		{
			mob.addStatFunc(new FuncMul(Stats.POWER_ATTACK_SPEED, 0x40, mob, new LambdaConst(MOB_PATK_SPEED_MUL)));
		}
		if (MOB_MATK_MUL != 1.0)
		{
			mob.addStatFunc(new FuncMul(Stats.MAGIC_ATTACK, 0x40, mob, new LambdaConst(MOB_MATK_MUL)));
		}
		mob.setCurrentHp(mob.getMaxHp());
	}
	
	private void despawnMonsters()
	{
		for (L2Spawn s : _spawnedMobs)
		{
			s.stopRespawn();
			if (s.getLastSpawn() != null)
			{
				s.getLastSpawn().deleteMe();
			}
		}
		_spawnedMobs.clear();
	}
	
	private void startMonitorTask()
	{
		if (_monitorTask != null)
		{
			_monitorTask.cancel(false);
		}
		_monitorTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
		{
			if (_state != EventState.RUNNING)
			{
				return;
			}
			
			for (L2PcInstance player : _playersInside)
			{
				if (player == null || player.isOnline() == 0)
				{
					exitEvent(player);
					continue;
				}
				if (player.getInstanceId() != INSTANCE_ID)
				{
					exitEvent(player);
					continue;
				}
				if (!player.isDead() && _deadTimers.containsKey(player.getObjectId()))
				{
					onRevive(player);
				}
				if (player.getParty() == null)
				{
					player.sendMessage("You have no party. Leaving event.");
					exitEvent(player);
				}
			}
		}, 3000, 3000);
	}
	
	private void startAnnounceTask()
	{
		if (_announceTask != null)
		{
			_announceTask.cancel(false);
		}
		_announceTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
		{
			if (_state != EventState.RUNNING)
			{
				return;
			}
			long left = (_eventEndTime - System.currentTimeMillis()) / 1000;
			if (left == 300 || left == 60)
			{
				Announcements.getInstance().gameAnnounceToAll(EVENT_NAME + " ends in " + (left / 60) + " minutes!");
			}
			
			PartyStats top = getTopParty();
			if (top != null)
			{
				for (L2PcInstance p : _playersInside)
				{
					p.sendPacket(new ExShowScreenMessage("Current Leader: " + top.leaderName + " (" + top.score + " pts)", 3000));
				}
			}
		}, 60000, 60000);
	}
	
	private void processRewards()
	{
		if (_partyStats.isEmpty())
		{
			return;
		}
		
		List<PartyStats> ranking = new ArrayList<>(_partyStats.values());
		ranking.sort(Comparator.comparingInt((PartyStats p) -> p.score).reversed().thenComparingInt(p -> p.pvpKills).reversed());
		
		if (ranking.size() > 0)
		{
			distributeReward(ranking.get(0), REWARD_TOP1_ID, REWARD_TOP1_COUNT, "Top 1");
		}
		if (ranking.size() > 1)
		{
			distributeReward(ranking.get(1), REWARD_TOP2_ID, REWARD_TOP2_COUNT, "Top 2");
		}
		if (ranking.size() > 2)
		{
			distributeReward(ranking.get(2), REWARD_TOP3_ID, REWARD_TOP3_COUNT, "Top 3");
		}
	}
	
	private void distributeReward(PartyStats pStats, int itemId, int count, String rank)
	{
		L2PcInstance leader = L2World.getInstance().getPlayer(pStats.leaderName);
		if (leader == null || leader.getParty() == null)
		{
			return;
		}
		
		L2Party party = leader.getParty();
		for (L2PcInstance member : party.getPartyMembers())
		{
			if (_playersInside.contains(member) && member.isInsideRadius(leader, RADIUS_TO_REWARD, false, false))
			{
				member.addItem("PartyZoneReward", itemId, count, null, true);
				member.sendPacket(new ExShowScreenMessage(rank + "! Reward Received!", 6000));
				if (REWARD_SOUND != null)
				{
					member.sendPacket(new PlaySound(REWARD_SOUND));
				}
			}
		}
		Announcements.getInstance().gameAnnounceToAll(EVENT_NAME + " " + rank + ": " + pStats.leaderName + " (Score: " + pStats.score + ")");
	}
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_partyzone_start"))
		{
			startRegistration();
			activeChar.sendMessage("Event registration started.");
		}
		else if (command.startsWith("admin_partyzone_force"))
		{
			startRunningPhase();
			activeChar.sendMessage("Event force started.");
		}
		else if (command.startsWith("admin_partyzone_stop"))
		{
			stopEvent();
			activeChar.sendMessage("Event stopped.");
		}
		else if (command.startsWith("admin_partyzone_reload"))
		{
			loadConfig();
			activeChar.sendMessage("Config reloaded.");
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return new String[]
		{
			"admin_partyzone_start",
			"admin_partyzone_force",
			"admin_partyzone_stop",
			"admin_partyzone_reload"
		};
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return new String[]
		{
			"ptz",
			"ptzjoin",
			"ptzleave",
			"ptzinfo",
			"ptzstart",
			"ptzstop",
			"ptzreload"
		};
	}
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance player, String target)
	{
		
		if (command.equalsIgnoreCase("ptz"))
		{
			showHtml(player);
		}
		else if (command.equalsIgnoreCase("ptzjoin"))
		{
			requestEntry(player);
		}
		else if (command.equalsIgnoreCase("ptzleave"))
		{
			requestLeave(player);
		}
		else if (command.equalsIgnoreCase("ptzinfo"))
		{
			showRanking(player);
		}
		else if (command.equalsIgnoreCase("ptzstart"))
		{
			if (!player.isGM())
			{
				return false;
			}
			startRegistration();
			player.sendMessage("Admin: Event started.");
		}
		else if (command.equalsIgnoreCase("ptzstop"))
		{
			if (!player.isGM())
			{
				return false;
			}
			stopEvent();
			player.sendMessage("Admin: Event stopped.");
		}
		else if (command.equalsIgnoreCase("ptzreload"))
		{
			if (!player.isGM())
			{
				return false;
			}
			loadConfig();
			player.sendMessage("Admin: Party Zone reloaded.");
		}
		
		return true;
	}
	
	@Override
	public String[] getByPassCommands()
	{
		return new String[]
		{
			"pz"
		};
	}
	
	@Override
	public void handleCommand(String command, L2PcInstance player, String parameters)
	{
		if (parameters == null || parameters.trim().isEmpty())
		{
			showHtml(player);
			return;
		}
		String action = parameters.trim();
		if (action.equals("join"))
		{
			requestEntry(player);
		}
		else if (action.equals("leave"))
		{
			requestLeave(player);
		}
		else if (action.equals("info"))
		{
			showRanking(player);
		}
		else if (action.equals("main"))
		{
			showHtml(player);
		}
	}
	
	public void showHtml(L2PcInstance player)
	{
		NpcHtmlMessage htm = new NpcHtmlMessage(0);
		String text = HtmCache.getInstance().getHtm("data/html/mods/partyzone/main.htm");
		if (text == null)
		{
			text = "<html><body>Main HTML Missing (data/html/mods/partyzone/main.htm)</body></html>";
		}
		
		String statusStr = "<font color=FF0000>Closed</font>";
		if (_state == EventState.REGISTRATION)
		{
			int secLeft = (int) ((_registrationEndTime - System.currentTimeMillis()) / 1000);
			statusStr = "<font color=00FF00>Reg Open (" + (secLeft / 60) + "m)</font>";
		}
		else if (_state == EventState.RUNNING)
		{
			statusStr = "<font color=FF9900>Running</font>";
		}
		
		String formattedFee = getItemName(ENTRY_FEE_ID) + " x" + ENTRY_FEE_COUNT;
		String formattedReward = getItemName(REWARD_TOP1_ID) + " x" + REWARD_TOP1_COUNT;
		
		text = text.replace("%eventName%", EVENT_NAME);
		text = text.replace("%status%", statusStr);
		text = text.replace("%registered%", String.valueOf(_playersInside.size()));
		text = text.replace("%fee%", formattedFee);
		text = text.replace("%reward%", formattedReward);
		
		List<PartyStats> ranking = new ArrayList<>(_partyStats.values());
		ranking.sort(Comparator.comparingInt((PartyStats p) -> p.score).reversed());
		
		String top1 = "None";
		String top2 = "None";
		
		if (!ranking.isEmpty())
		{
			top1 = ranking.get(0).leaderName + " (" + ranking.get(0).score + ")";
		}
		if (ranking.size() > 1)
		{
			top2 = ranking.get(1).leaderName + " (" + ranking.get(1).score + ")";
		}
		
		text = text.replace("%top1%", top1);
		text = text.replace("%top2%", top2);
		
		htm.setHtml(text);
		player.sendPacket(htm);
	}
	
	private void showRanking(L2PcInstance player)
	{
		NpcHtmlMessage htm = new NpcHtmlMessage(0);
		String text = HtmCache.getInstance().getHtm("data/html/mods/partyzone/info.htm");
		if (text == null)
		{
			text = "<html><body>Info HTML Missing</body></html>";
		}
		
		List<PartyStats> ranking = new ArrayList<>(_partyStats.values());
		ranking.sort(Comparator.comparingInt((PartyStats p) -> p.score).reversed().thenComparingInt(p -> p.pvpKills).reversed().thenComparingInt(p -> p.mobKills).reversed());
		
		StringBuilder sb = new StringBuilder();
		int limit = Math.min(10, ranking.size());
		
		if (limit == 0)
		{
			sb.append("<table width=270><tr><td align=center>No parties participating yet.</td></tr></table>");
		}
		else
		{
			for (int i = 0; i < limit; i++)
			{
				PartyStats ps = ranking.get(i);
				sb.append("<table width=270><tr>");
				sb.append("<td width=20>").append(i + 1).append("</td>");
				sb.append("<td width=120>").append(ps.leaderName).append("</td>");
				sb.append("<td width=50 align=center>").append(ps.score).append("</td>");
				sb.append("<td width=80 align=right>").append(ps.pvpKills).append(" / ").append(ps.mobKills).append("</td>");
				sb.append("</tr></table>");
				sb.append("<img src=\"L2UI.SquareGray\" width=270 height=1>");
			}
		}
		
		text = text.replace("%ranking%", sb.toString());
		htm.setHtml(text);
		player.sendPacket(htm);
	}
	
	private boolean validateBasicConditions(L2PcInstance player)
	{
		if (player.getLevel() < MIN_LEVEL)
		{
			player.sendMessage("Level too low.");
			return false;
		}
		if (player.isInOlympiadMode() || player.isInDuel() || player.isCursedWeaponEquiped())
		{
			player.sendMessage("Forbidden condition.");
			return false;
		}
		return true;
	}
	
	private int getAbnormalMask(String name)
	{
		return name.equalsIgnoreCase("FLAME") ? 0x4000 : 0x0001;
	}
	
	private String getItemName(int itemId)
	{
		L2Item item = ItemTable.getInstance().getTemplate(itemId);
		return (item != null) ? item.getName() : "Id " + itemId;
	}
}