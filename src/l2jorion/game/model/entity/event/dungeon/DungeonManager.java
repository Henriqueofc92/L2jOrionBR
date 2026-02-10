package l2jorion.game.model.entity.event.dungeon;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

import l2jorion.game.ai.CtrlIntention;
import l2jorion.game.cache.HtmCache;
import l2jorion.game.datatables.sql.ItemTable;
import l2jorion.game.datatables.sql.NpcTable;
import l2jorion.game.datatables.sql.SpawnTable;
import l2jorion.game.handler.ICustomByPassHandler;
import l2jorion.game.handler.IVoicedCommandHandler;
import l2jorion.game.model.L2Character;
import l2jorion.game.model.L2Effect;
import l2jorion.game.model.L2World;
import l2jorion.game.model.Location;
import l2jorion.game.model.actor.instance.L2MonsterInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.entity.Announcements;
import l2jorion.game.model.entity.event.manager.EventTask;
import l2jorion.game.model.entity.event.manager.EventsGlobalTask;
import l2jorion.game.model.olympiad.OlympiadManager;
import l2jorion.game.model.spawn.L2Spawn;
import l2jorion.game.model.zone.ZoneId;
import l2jorion.game.network.serverpackets.ExShowScreenMessage;
import l2jorion.game.network.serverpackets.MagicSkillUser;
import l2jorion.game.network.serverpackets.NpcHtmlMessage;
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

public class DungeonManager implements IVoicedCommandHandler, ICustomByPassHandler
{
	protected static final Logger LOG = LoggerFactory.getLogger(DungeonManager.class);
	private static final String CONFIG_FILE = "./config/mods/events/dungeon.ini";
	private static DungeonManager _instance;
	
	public static boolean DG_EVENT_ENABLED = false;
	public static String EVENT_NAME = "Bloody Dungeon";
	public static boolean HIDE_PLAYER_INFO = false;
	public static String PLAYER_FAKE_NAME = "Player";
	public static String TITLE_PLAYER = "Player";
	public static int PLAYER_NAME_COLOR = 0xFFFFFF;
	public static int PLAYER_TITLE_COLOR = 0xFFFF00;
	
	public static int NPC_ID = 0, NPC_X = 0, NPC_Y = 0, NPC_Z = 0, NPC_HEADING = 0;
	private static L2Spawn _npcSpawn;
	
	public static int DURATION = 25;
	public static List<String> DG_TIMES_LIST = new ArrayList<>();
	
	public static int TELEPORT_DELAY = 20;
	public static int ENTRY_FEE_ID = 57, ENTRY_FEE_COUNT = 500000;
	public static boolean ALLOW_POTIONS = true, ALLOW_SUMMON = false;
	public static List<Integer> PROHIBITED_CLASS_IDS = new ArrayList<>();
	public static int MIN_LEVEL = 76, MAX_LEVEL = 85;
	public static int INSTANCE_PVP = 200, INSTANCE_SOLO = 201;
	public static int REENTRY_COOLDOWN_SECONDS = 0;
	
	public static String MOB_TITLE = "Lost Soul";
	public static boolean MOB_ENABLE_EFFECT = true;
	public static String MOB_EFFECT_NAME = "FLAME";
	public static int MOB_RESPAWN_DELAY = 20;
	public static final List<Integer> TARGET_MOBS = new ArrayList<>();
	
	public static double SOLO_HP_MUL = 1.0, SOLO_PATK_MUL = 1.0, SOLO_MATK_MUL = 1.0, SOLO_PDEF_MUL = 1.0, SOLO_MDEF_MUL = 1.0, SOLO_ATKSPD_MUL = 1.0, SOLO_CASTSPD_MUL = 1.0;
	public static double PVP_HP_MUL = 1.0, PVP_PATK_MUL = 1.0, PVP_MATK_MUL = 1.0, PVP_PDEF_MUL = 1.0, PVP_MDEF_MUL = 1.0, PVP_ATKSPD_MUL = 1.0, PVP_CASTSPD_MUL = 1.0;
	
	public static int DROP_SOLO_ID = 57, DROP_SOLO_COUNT = 10000, DROP_SOLO_CHANCE = 100;
	public static int DROP_PVP_ID = 57, DROP_PVP_COUNT = 20000, DROP_PVP_CHANCE = 100;
	
	public static int REWARD_SOLO_TOP1_ID = 57, REWARD_SOLO_TOP1_COUNT = 100;
	public static int REWARD_SOLO_TOP2_ID = 57, REWARD_SOLO_TOP2_COUNT = 50;
	public static int REWARD_SOLO_TOP3_ID = 57, REWARD_SOLO_TOP3_COUNT = 25;
	
	public static int REWARD_PVP_TOP1_ID = 57, REWARD_PVP_TOP1_COUNT = 100;
	public static int REWARD_PVP_TOP2_ID = 57, REWARD_PVP_TOP2_COUNT = 50;
	public static int REWARD_PVP_TOP3_ID = 57, REWARD_PVP_TOP3_COUNT = 25;
	
	public static String REWARD_SOUND = "ItemSound.quest_fanfare_1";
	
	private static final int SPAWN_RADIUS = 6000;
	private static final List<L2Spawn> _spawnedEventMobs = new ArrayList<>();
	private static final Location EXIT_LOCATION = new Location(83431, 148331, -3400);
	
	private static final Map<Integer, Location> _originalLocations = new ConcurrentHashMap<>();
	private static final List<DungeonLocation> LOCATIONS = new ArrayList<>();
	private static DungeonLocation _currentLocation = null;
	
	private static final List<L2Spawn> _cachedMirrorSpawns = new ArrayList<>();
	
	private static boolean _inProgress = false;
	private static ScheduledFuture<?> _endTask = null, _announceTask = null, _zoneCheckTask = null, _monitorTask = null;
	private static long _eventEndTime = 0;
	
	private static final Map<Integer, PlayerStats> _playerStats = new ConcurrentHashMap<>();
	private static final List<L2PcInstance> _playersInside = new CopyOnWriteArrayList<>();
	private static final Map<Integer, Long> _leaveCooldowns = new ConcurrentHashMap<>();
	
	public static class DungeonLocation
	{
		String name;
		List<Location> spawnPoints = new ArrayList<>();
		
		DungeonLocation(String name)
		{
			this.name = name;
		}
		
		void addPoint(int x, int y, int z)
		{
			spawnPoints.add(new Location(x, y, z));
		}
		
		Location getRandomPoint()
		{
			return spawnPoints.isEmpty() ? new Location(83431, 148331, -3400) : spawnPoints.get(Rnd.get(spawnPoints.size()));
		}
		
		Location getCenter()
		{
			return spawnPoints.isEmpty() ? new Location(83431, 148331, -3400) : spawnPoints.get(0);
		}
	}
	
	private static class PlayerStats
	{
		int pvpKills = 0;
		int mobKills = 0;
		long entryTime;
		String name;
		int instanceId;
		
		PlayerStats(String name, int instanceId)
		{
			this.name = name;
			this.instanceId = instanceId;
			this.entryTime = System.currentTimeMillis();
		}
	}
	
	public static DungeonManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new DungeonManager();
		}
		return _instance;
	}
	
	public DungeonManager()
	{
		loadConfig();
	}
	
	public static class DungeonSchedule implements EventTask
	{
		private final String _time;
		
		public DungeonSchedule(String time)
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
			DungeonManager.getInstance().startEvent();
		}
	}
	
	public static void registerDG()
	{
		EventsGlobalTask.getInstance().clearEventTasksByEventName(DungeonManager.EVENT_NAME);
		DungeonManager.getInstance().loadConfig();
		if (!DungeonManager.DG_EVENT_ENABLED)
		{
			LOG.info("EventManager: Dungeon Event is disabled in dungeon.ini");
			return;
		}
		if (DungeonManager.DG_TIMES_LIST.isEmpty())
		{
			LOG.warn("EventManager: No start times defined for Dungeon Event in dungeon.ini");
			return;
		}
		for (String time : DungeonManager.DG_TIMES_LIST)
		{
			DungeonManager.DungeonSchedule task = new DungeonManager.DungeonSchedule(time);
			EventsGlobalTask.getInstance().registerNewEventTask(task);
			LOG.info("EventManager: Scheduled Dungeon Event at " + time);
		}
	}
	
	public void loadConfig()
	{
		try (InputStream is = new FileInputStream(new File(CONFIG_FILE)))
		{
			Properties settings = new Properties();
			settings.load(is);
			
			DG_EVENT_ENABLED = Boolean.parseBoolean(settings.getProperty("EventDG_EVENT_ENABLED", "true"));
			EVENT_NAME = settings.getProperty("EventName", "Bloody Dungeon");
			DURATION = Integer.parseInt(settings.getProperty("EventDurationMinutes", "25"));
			
			NPC_ID = Integer.parseInt(settings.getProperty("NpcId", "0"));
			NPC_X = Integer.parseInt(settings.getProperty("NpcX", "0"));
			NPC_Y = Integer.parseInt(settings.getProperty("NpcY", "0"));
			NPC_Z = Integer.parseInt(settings.getProperty("NpcZ", "0"));
			NPC_HEADING = Integer.parseInt(settings.getProperty("NpcHeading", "0"));
			
			String times = settings.getProperty("EventStartTimes", "19:05");
			DG_TIMES_LIST.clear();
			for (String t : times.split(";"))
			{
				if (!t.trim().isEmpty())
				{
					DG_TIMES_LIST.add(t.trim());
				}
			}
			
			TELEPORT_DELAY = Integer.parseInt(settings.getProperty("TeleportDelaySeconds", "20"));
			ENTRY_FEE_ID = Integer.parseInt(settings.getProperty("EntryFeeId", "57"));
			ENTRY_FEE_COUNT = Integer.parseInt(settings.getProperty("EntryFeeAmount", "500000"));
			ALLOW_POTIONS = Boolean.parseBoolean(settings.getProperty("AllowPotions", "true"));
			ALLOW_SUMMON = Boolean.parseBoolean(settings.getProperty("AllowSummon", "false"));
			
			String bannedClasses = settings.getProperty("ProhibitClassIds", "97,105,112");
			PROHIBITED_CLASS_IDS.clear();
			if (bannedClasses != null && !bannedClasses.trim().isEmpty())
			{
				for (String id : bannedClasses.split(","))
				{
					try
					{
						PROHIBITED_CLASS_IDS.add(Integer.parseInt(id.trim()));
					}
					catch (NumberFormatException e)
					{
					}
				}
			}
			
			MIN_LEVEL = Integer.parseInt(settings.getProperty("MinLevel", "76"));
			MAX_LEVEL = Integer.parseInt(settings.getProperty("MaxLevel", "85"));
			INSTANCE_PVP = Integer.parseInt(settings.getProperty("InstanceIdPvP", "200"));
			INSTANCE_SOLO = Integer.parseInt(settings.getProperty("InstanceIdSolo", "201"));
			REENTRY_COOLDOWN_SECONDS = Integer.parseInt(settings.getProperty("ReentryCooldownSeconds", "600"));
			
			String locsData = settings.getProperty("DungeonLocations", "");
			LOCATIONS.clear();
			if (!locsData.isEmpty())
			{
				for (String mapBlock : locsData.split(";"))
				{
					String[] parts = mapBlock.split(",");
					if (parts.length >= 4)
					{
						DungeonLocation dLoc = new DungeonLocation(parts[0].trim());
						for (int i = 1; i < parts.length - 2; i += 3)
						{
							try
							{
								dLoc.addPoint(Integer.parseInt(parts[i].trim()), Integer.parseInt(parts[i + 1].trim()), Integer.parseInt(parts[i + 2].trim()));
							}
							catch (Exception e)
							{
							}
						}
						LOCATIONS.add(dLoc);
					}
				}
			}
			
			HIDE_PLAYER_INFO = Boolean.parseBoolean(settings.getProperty("HidePlayerInfo", "true"));
			PLAYER_FAKE_NAME = settings.getProperty("PlayerFakeName", "Unknown");
			TITLE_PLAYER = settings.getProperty("PlayerFakeTitle", "Survivor");
			
			try
			{
				PLAYER_NAME_COLOR = Integer.decode("0x" + settings.getProperty("PlayerNameColor", "FFFFFF"));
				PLAYER_TITLE_COLOR = Integer.decode("0x" + settings.getProperty("PlayerTitleColor", "FFFF00"));
			}
			catch (Exception e)
			{
			}
			
			MOB_TITLE = settings.getProperty("MonsterTitle", "Lost Soul");
			MOB_ENABLE_EFFECT = Boolean.parseBoolean(settings.getProperty("EnableMobEffect", "true"));
			MOB_EFFECT_NAME = settings.getProperty("MobEffectName", "FLAME");
			MOB_RESPAWN_DELAY = Integer.parseInt(settings.getProperty("RespawnDelaySeconds", "20"));
			
			String mobs = settings.getProperty("MonsterTargetIds", "");
			TARGET_MOBS.clear();
			for (String id : mobs.split(","))
			{
				if (!id.trim().isEmpty())
				{
					TARGET_MOBS.add(Integer.parseInt(id.trim()));
				}
			}
			
			SOLO_HP_MUL = Double.parseDouble(settings.getProperty("SoloMobHpMultiplier", "1.0"));
			SOLO_PATK_MUL = Double.parseDouble(settings.getProperty("SoloMobPAtkMultiplier", "1.0"));
			SOLO_MATK_MUL = Double.parseDouble(settings.getProperty("SoloMobMAtkMultiplier", "1.0"));
			SOLO_PDEF_MUL = Double.parseDouble(settings.getProperty("SoloMobPDefMultiplier", "1.0"));
			SOLO_MDEF_MUL = Double.parseDouble(settings.getProperty("SoloMobMDefMultiplier", "1.0"));
			SOLO_ATKSPD_MUL = Double.parseDouble(settings.getProperty("SoloMobAtkSpdMultiplier", "1.0"));
			SOLO_CASTSPD_MUL = Double.parseDouble(settings.getProperty("SoloMobCastSpdMultiplier", "1.0"));
			
			PVP_HP_MUL = Double.parseDouble(settings.getProperty("PvPMobHpMultiplier", "2.0"));
			PVP_PATK_MUL = Double.parseDouble(settings.getProperty("PvPMobPAtkMultiplier", "1.5"));
			PVP_MATK_MUL = Double.parseDouble(settings.getProperty("PvPMobMAtkMultiplier", "1.5"));
			PVP_PDEF_MUL = Double.parseDouble(settings.getProperty("PvPMobPDefMultiplier", "1.2"));
			PVP_MDEF_MUL = Double.parseDouble(settings.getProperty("PvPMobMDefMultiplier", "1.2"));
			PVP_ATKSPD_MUL = Double.parseDouble(settings.getProperty("PvPMobAtkSpdMultiplier", "1.1"));
			PVP_CASTSPD_MUL = Double.parseDouble(settings.getProperty("PvPMobCastSpdMultiplier", "1.1"));
			
			String[] dpSolo = settings.getProperty("SoloDropReward", "57,10000,100").split(",");
			DROP_SOLO_ID = Integer.parseInt(dpSolo[0].trim());
			DROP_SOLO_COUNT = Integer.parseInt(dpSolo[1].trim());
			if (dpSolo.length > 2)
			{
				DROP_SOLO_CHANCE = Integer.parseInt(dpSolo[2].trim());
			}
			
			String[] dpPvp = settings.getProperty("PvPDropReward", "57,20000,100").split(",");
			DROP_PVP_ID = Integer.parseInt(dpPvp[0].trim());
			DROP_PVP_COUNT = Integer.parseInt(dpPvp[1].trim());
			if (dpPvp.length > 2)
			{
				DROP_PVP_CHANCE = Integer.parseInt(dpPvp[2].trim());
			}
			
			String[] rwSolo1 = settings.getProperty("RewardSoloTop1", "57,100").split(",");
			REWARD_SOLO_TOP1_ID = Integer.parseInt(rwSolo1[0]);
			REWARD_SOLO_TOP1_COUNT = Integer.parseInt(rwSolo1[1]);
			
			String[] rwSolo2 = settings.getProperty("RewardSoloTop2", "57,50").split(",");
			REWARD_SOLO_TOP2_ID = Integer.parseInt(rwSolo2[0]);
			REWARD_SOLO_TOP2_COUNT = Integer.parseInt(rwSolo2[1]);
			
			String[] rwSolo3 = settings.getProperty("RewardSoloTop3", "57,25").split(",");
			REWARD_SOLO_TOP3_ID = Integer.parseInt(rwSolo3[0]);
			REWARD_SOLO_TOP3_COUNT = Integer.parseInt(rwSolo3[1]);
			
			String[] rwPvp1 = settings.getProperty("RewardPvPTop1", "57,200").split(",");
			REWARD_PVP_TOP1_ID = Integer.parseInt(rwPvp1[0]);
			REWARD_PVP_TOP1_COUNT = Integer.parseInt(rwPvp1[1]);
			
			String[] rwPvp2 = settings.getProperty("RewardPvPTop2", "57,100").split(",");
			REWARD_PVP_TOP2_ID = Integer.parseInt(rwPvp2[0]);
			REWARD_PVP_TOP2_COUNT = Integer.parseInt(rwPvp2[1]);
			
			String[] rwPvp3 = settings.getProperty("RewardPvPTop3", "57,50").split(",");
			REWARD_PVP_TOP3_ID = Integer.parseInt(rwPvp3[0]);
			REWARD_PVP_TOP3_COUNT = Integer.parseInt(rwPvp3[1]);
			
			REWARD_SOUND = settings.getProperty("RewardSound", "ItemSound.quest_fanfare_1");
			cacheMirrorSpawns();
		}
		catch (Exception e)
		{
			LOG.warn("DungeonManager: Error loading config: " + e.getMessage());
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
			
			for (DungeonLocation loc : LOCATIONS)
			{
				Location center = loc.getCenter();
				double dist = Math.sqrt(Math.pow(originalSpawn.getLocx() - center.getX(), 2) + Math.pow(originalSpawn.getLocy() - center.getY(), 2));
				
				if (dist <= SPAWN_RADIUS)
				{
					_cachedMirrorSpawns.add(originalSpawn);
					cachedCount++;
					break;
				}
			}
		}
		LOG.info("DungeonManager: Performance Cache -> " + cachedCount + " spawns ready for mirror.");
	}
	
	public void startEvent()
	{
		if (_inProgress)
		{
			return;
		}
		loadConfig();
		if (!DG_EVENT_ENABLED)
		{
			return;
		}
		
		if (LOCATIONS.isEmpty())
		{
			_currentLocation = new DungeonLocation("Fallback");
			_currentLocation.addPoint(83431, 148331, -3400);
		}
		else
		{
			_currentLocation = LOCATIONS.get(Rnd.get(LOCATIONS.size()));
		}
		
		_inProgress = true;
		_playersInside.clear();
		_playerStats.clear();
		_eventEndTime = System.currentTimeMillis() + (DURATION * 60 * 1000);
		
		spawnEventMonsters();
		spawnEventNpc();
		
		Announcements.getInstance().gameAnnounceToAll(EVENT_NAME + ": OPENED at " + _currentLocation.name + "!");
		Announcements.getInstance().gameAnnounceToAll("Dungeon: Choose: Peace Farming or PvP High Rewards!");
		Announcements.getInstance().gameAnnounceToAll("Dungeon: Type .dgevent to join now!");
		
		if (_endTask != null)
		{
			_endTask.cancel(false);
		}
		_endTask = ThreadPoolManager.getInstance().scheduleGeneral(this::stopEvent, DURATION * 60 * 1000);
		
		startAnnounceTask();
		startZoneCheckTask();
		startMonitorTask();
		
		LOG.info("Dungeon Event: Started successfully at " + _currentLocation.name);
	}
	
	public void stopEvent()
	{
		if (!_inProgress)
		{
			return;
		}
		_inProgress = false;
		
		if (_endTask != null)
		{
			_endTask.cancel(false);
			_endTask = null;
		}
		if (_announceTask != null)
		{
			_announceTask.cancel(false);
			_announceTask = null;
		}
		if (_zoneCheckTask != null)
		{
			_zoneCheckTask.cancel(false);
			_zoneCheckTask = null;
		}
		if (_monitorTask != null)
		{
			_monitorTask.cancel(false);
			_monitorTask = null;
		}
		
		processRewards();
		despawnEventMonsters();
		unspawnEventNpc();
		Announcements.getInstance().gameAnnounceToAll(EVENT_NAME + ": Event Finished!");
		Announcements.getInstance().gameAnnounceToAll("Dungeon: Thanks for participating!");
		
		List<L2PcInstance> playersToRemove = new ArrayList<>(_playersInside);
		
		for (L2PcInstance player : playersToRemove)
		{
			if (player != null)
			{
				player.sendPacket(new ExShowScreenMessage("EVENT FINISHED!", 5000));
				player.abortAttack();
				player.abortCast();
				player.stopMove(null);
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				exitDungeon(player, true);
			}
		}
		_playersInside.clear();
		_playerStats.clear();
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return new String[]
		{
			"dgevent",
			"dgstart",
			"dgstop",
			"dgreload",
			"dgpeace",
			"dgpvp",
			"dgleave",
			"dginfo"
		};
	}
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance player, String target)
	{
		if (player == null)
		{
			return false;
		}
		boolean alreadyInEvent = _playersInside.contains(player) || player.getInstanceId() == INSTANCE_PVP || player.getInstanceId() == INSTANCE_SOLO;
		
		if (command.equalsIgnoreCase("dgevent"))
		{
			if (validatePlayerCondition(player))
			{
				showHtml(player);
			}
			return true;
		}
		else if (command.equalsIgnoreCase("dginfo"))
		{
			showRankingHtml(player);
			return true;
		}
		else if (command.equalsIgnoreCase("dgpeace"))
		{
			if (alreadyInEvent)
			{
				player.sendMessage("[Dungeon] You are already participating!");
				return false;
			}
			handleEntryRequest(player, false);
			return true;
		}
		else if (command.equalsIgnoreCase("dgpvp"))
		{
			if (alreadyInEvent)
			{
				player.sendMessage("[Dungeon] You are already participating!");
				return false;
			}
			handleEntryRequest(player, true);
			return true;
		}
		else if (command.equalsIgnoreCase("leave") || command.equalsIgnoreCase("dgleave"))
		{
			handleLeaveCommand(player);
			return true;
		}
		
		if (player.isGM())
		{
			if (command.equalsIgnoreCase("dgstart"))
			{
				startEvent();
				return true;
			}
			if (command.equalsIgnoreCase("dgstop"))
			{
				stopEvent();
				return true;
			}
			if (command.equalsIgnoreCase("dgreload"))
			{
				loadConfig();
				player.sendMessage("[Dungeon] Config reloaded.");
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String[] getByPassCommands()
	{
		return new String[]
		{
			"dg"
		};
	}
	
	@Override
	public void handleCommand(String command, L2PcInstance player, String parameters)
	{
		if (!validatePlayerCondition(player))
		{
			return;
		}
		if (parameters == null || parameters.trim().isEmpty())
		{
			showHtml(player);
			return;
		}
		String action = parameters.trim();
		if (action.equalsIgnoreCase("join_solo"))
		{
			handleEntryRequest(player, false);
		}
		else if (action.equalsIgnoreCase("join_pvp"))
		{
			handleEntryRequest(player, true);
		}
		else if (action.equalsIgnoreCase("exit"))
		{
			handleLeaveCommand(player);
		}
		else if (action.equalsIgnoreCase("info"))
		{
			showRankingHtml(player);
		}
		else if (action.equalsIgnoreCase("main") || action.equalsIgnoreCase("dgevent"))
		{
			showHtml(player);
		}
	}
	
	public void showHtml(L2PcInstance player)
	{
		NpcHtmlMessage htm = new NpcHtmlMessage(0);
		String htmlText = HtmCache.getInstance().getHtm("data/html/mods/dungeon/main.htm");
		if (htmlText == null)
		{
			player.sendMessage("Error: HTML file missing.");
			return;
		}
		
		String statusColor = _inProgress ? "<font color=\"00FF00\">OPEN</font>" : "<font color=\"FF0000\">CLOSED</font>";
		String formattedFee = formatAmount(ENTRY_FEE_COUNT) + " " + getItemName(ENTRY_FEE_ID);
		String formattedReward = formatAmount(REWARD_PVP_TOP1_COUNT) + " " + getItemName(REWARD_PVP_TOP1_ID);
		
		htmlText = htmlText.replace("%eventName%", EVENT_NAME);
		htmlText = htmlText.replace("%status%", statusColor);
		htmlText = htmlText.replace("%registered%", String.valueOf(_playersInside.size()));
		htmlText = htmlText.replace("%fee%", formattedFee);
		htmlText = htmlText.replace("%reward%", formattedReward);
		
		PlayerStats topSolo = getTopPlayer(INSTANCE_SOLO, false);
		PlayerStats topPvP = getTopPlayer(INSTANCE_PVP, true);
		
		htmlText = htmlText.replace("%topSolo%", topSolo != null ? topSolo.name + " (" + topSolo.mobKills + ")" : "None");
		htmlText = htmlText.replace("%topPvP%", topPvP != null ? topPvP.name + " (" + topPvP.pvpKills + ")" : "None");
		
		htm.setHtml(htmlText);
		player.sendPacket(htm);
	}
	
	public void showRankingHtml(L2PcInstance player)
	{
		String htmlText = HtmCache.getInstance().getHtm("data/html/mods/dungeon/info.htm");
		
		if (htmlText == null)
		{
			player.sendMessage("Error: HTML file data/html/mods/dungeon/info.htm missing.");
			return;
		}
		StringBuilder soloSb = new StringBuilder();
		List<PlayerStats> soloStats = getSortedStats(INSTANCE_SOLO, false);
		int limitSolo = Math.min(5, soloStats.size());
		
		if (limitSolo == 0)
		{
			soloSb.append("<table width=270><tr><td align=center>No participants yet.</td></tr></table>");
		}
		else
		{
			for (int i = 0; i < limitSolo; i++)
			{
				PlayerStats ps = soloStats.get(i);
				soloSb.append("<table width=270><tr>");
				soloSb.append("<td width=20>").append(i + 1).append("</td>");
				soloSb.append("<td width=150>").append(ps.name).append("</td>");
				soloSb.append("<td width=100 align=right>").append(ps.mobKills).append("</td>");
				soloSb.append("</tr></table>");
			}
		}
		StringBuilder pvpSb = new StringBuilder();
		List<PlayerStats> pvpStats = getSortedStats(INSTANCE_PVP, true);
		int limitPvp = Math.min(5, pvpStats.size());
		
		if (limitPvp == 0)
		{
			pvpSb.append("<table width=270><tr><td align=center>No participants yet.</td></tr></table>");
		}
		else
		{
			for (int i = 0; i < limitPvp; i++)
			{
				PlayerStats ps = pvpStats.get(i);
				pvpSb.append("<table width=270><tr>");
				pvpSb.append("<td width=20>").append(i + 1).append("</td>");
				pvpSb.append("<td width=150>").append(ps.name).append("</td>");
				pvpSb.append("<td width=100 align=right>").append(ps.pvpKills).append(" / ").append(ps.mobKills).append("</td>");
				pvpSb.append("</tr></table>");
			}
		}
		htmlText = htmlText.replace("%soloRanking%", soloSb.toString());
		htmlText = htmlText.replace("%pvpRanking%", pvpSb.toString());
		
		NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setHtml(htmlText);
		player.sendPacket(msg);
	}
	
	private List<PlayerStats> getSortedStats(int instanceId, boolean sortByPvP)
	{
		List<PlayerStats> list = new ArrayList<>();
		for (PlayerStats ps : _playerStats.values())
		{
			if (ps.instanceId == instanceId)
			{
				list.add(ps);
			}
		}
		
		if (sortByPvP)
		{
			list.sort(Comparator.comparingInt((PlayerStats p) -> p.pvpKills).reversed().thenComparingInt(p -> p.mobKills).reversed().thenComparingLong(p -> p.entryTime));
		}
		else
		{
			list.sort(Comparator.comparingInt((PlayerStats p) -> p.mobKills).reversed().thenComparingLong(p -> p.entryTime));
		}
		return list;
	}
	
	private PlayerStats getTopPlayer(int instanceId, boolean sortByPvP)
	{
		List<PlayerStats> list = getSortedStats(instanceId, sortByPvP);
		return list.isEmpty() ? null : list.get(0);
	}
	
	private String getItemName(int itemId)
	{
		L2Item item = ItemTable.getInstance().getTemplate(itemId);
		return (item != null) ? item.getName() : "Item " + itemId;
	}
	
	private String formatAmount(long count)
	{
		if (count >= 1000000000)
		{
			return (count / 1000000000) + "kkk";
		}
		if (count >= 1000000)
		{
			return (count / 1000000) + "kk";
		}
		if (count >= 1000)
		{
			return (count / 1000) + "k";
		}
		return String.valueOf(count);
	}
	
	private void spawnEventNpc()
	{
		if (NPC_ID == 0)
		{
			return;
		}
		L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(NPC_ID);
		if (tmpl == null)
		{
			return;
		}
		try
		{
			_npcSpawn = new L2Spawn(tmpl);
			_npcSpawn.setLocx(NPC_X);
			_npcSpawn.setLocy(NPC_Y);
			_npcSpawn.setLocz(NPC_Z);
			_npcSpawn.setHeading(NPC_HEADING);
			_npcSpawn.setAmount(1);
			_npcSpawn.setRespawnDelay(1);
			SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);
			_npcSpawn.init();
			_npcSpawn.getLastSpawn().getStatus().setCurrentHp(999999999);
			_npcSpawn.getLastSpawn().setTitle(EVENT_NAME);
			_npcSpawn.getLastSpawn().spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());
			_npcSpawn.getLastSpawn().broadcastPacket(new MagicSkillUser(_npcSpawn.getLastSpawn(), _npcSpawn.getLastSpawn(), 1034, 1, 1, 1));
		}
		catch (Exception e)
		{
			LOG.warn("Dungeon: Error spawning event NPC: " + e.getMessage());
		}
	}
	
	private void unspawnEventNpc()
	{
		if (_npcSpawn == null)
		{
			return;
		}
		if (_npcSpawn.getLastSpawn() != null)
		{
			_npcSpawn.getLastSpawn().deleteMe();
		}
		_npcSpawn.stopRespawn();
		SpawnTable.getInstance().deleteSpawn(_npcSpawn, true);
		_npcSpawn = null;
	}
	
	// ==========================================
	// SPAWN ENGINE ATUALIZADA (USE CACHE)
	// ==========================================
	private void spawnEventMonsters()
	{
		if (TARGET_MOBS.isEmpty() || _currentLocation == null)
		{
			return;
		}
		
		// Se o cache estiver vazio (ex: reload não funcionou), tentamos cachear agora
		if (_cachedMirrorSpawns.isEmpty())
		{
			cacheMirrorSpawns();
		}
		
		int count = 0;
		Location center = _currentLocation.getCenter();
		
		// OTIMIZAÇÃO: Itera SOMENTE sobre os mobs pré-cacheados (fast mode)
		for (L2Spawn worldSpawn : _cachedMirrorSpawns)
		{
			// Verificação dupla: o mob do cache está perto da LOCALIZAÇÃO ATUAL do evento?
			// (Porque o cache pode ter mobs de outras DungeonLocations se houver mais de uma configurada)
			double distance = Math.sqrt(Math.pow(center.getX() - worldSpawn.getLocx(), 2) + Math.pow(center.getY() - worldSpawn.getLocy(), 2));
			if (distance <= SPAWN_RADIUS)
			{
				int randomMobId = TARGET_MOBS.get(Rnd.get(TARGET_MOBS.size()));
				spawnCustomMobAtLocation(worldSpawn, randomMobId, INSTANCE_PVP);
				spawnCustomMobAtLocation(worldSpawn, randomMobId, INSTANCE_SOLO);
				count++;
			}
		}
		LOG.info("Dungeon: " + count + " monsters spawned (Fast Mode).");
	}
	
	private void spawnCustomMobAtLocation(L2Spawn anchorSpawn, int mobId, int instanceId)
	{
		try
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(mobId);
			if (template == null)
			{
				return;
			}
			L2Spawn newSpawn = new L2Spawn(template);
			newSpawn.setLocx(anchorSpawn.getLocx());
			newSpawn.setLocy(anchorSpawn.getLocy());
			newSpawn.setLocz(anchorSpawn.getLocz());
			newSpawn.setHeading(anchorSpawn.getHeading());
			newSpawn.setAmount(1);
			newSpawn.setRespawnDelay(MOB_RESPAWN_DELAY);
			newSpawn.setInstanceId(instanceId);
			L2MonsterInstance mob = (L2MonsterInstance) newSpawn.doSpawn();
			mob.setTitle(MOB_TITLE);
			applyMonsterStats(mob, instanceId == INSTANCE_PVP);
			newSpawn.startRespawn();
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
			_spawnedEventMobs.add(newSpawn);
		}
		catch (Exception e)
		{
			LOG.warn("Dungeon: Error spawning mob: " + e.getMessage());
		}
	}
	
	private void applyMonsterStats(L2MonsterInstance mob, boolean isPvP)
	{
		double hpMul = isPvP ? PVP_HP_MUL : SOLO_HP_MUL;
		double pAtkMul = isPvP ? PVP_PATK_MUL : SOLO_PATK_MUL;
		double mAtkMul = isPvP ? PVP_MATK_MUL : SOLO_MATK_MUL;
		double pDefMul = isPvP ? PVP_PDEF_MUL : SOLO_PDEF_MUL;
		double mDefMul = isPvP ? PVP_MDEF_MUL : SOLO_MDEF_MUL;
		double atkSpdMul = isPvP ? PVP_ATKSPD_MUL : SOLO_ATKSPD_MUL;
		double castSpdMul = isPvP ? PVP_CASTSPD_MUL : SOLO_CASTSPD_MUL;
		
		if (hpMul != 1.0)
		{
			mob.addStatFunc(new FuncMul(Stats.MAX_HP, 0x40, mob, new LambdaConst(hpMul)));
		}
		if (pAtkMul != 1.0)
		{
			mob.addStatFunc(new FuncMul(Stats.POWER_ATTACK, 0x40, mob, new LambdaConst(pAtkMul)));
		}
		if (mAtkMul != 1.0)
		{
			mob.addStatFunc(new FuncMul(Stats.MAGIC_ATTACK, 0x40, mob, new LambdaConst(mAtkMul)));
		}
		if (pDefMul != 1.0)
		{
			mob.addStatFunc(new FuncMul(Stats.POWER_DEFENCE, 0x40, mob, new LambdaConst(pDefMul)));
		}
		if (mDefMul != 1.0)
		{
			mob.addStatFunc(new FuncMul(Stats.MAGIC_DEFENCE, 0x40, mob, new LambdaConst(mDefMul)));
		}
		if (atkSpdMul != 1.0)
		{
			mob.addStatFunc(new FuncMul(Stats.POWER_ATTACK_SPEED, 0x40, mob, new LambdaConst(atkSpdMul)));
		}
		if (castSpdMul != 1.0)
		{
			mob.addStatFunc(new FuncMul(Stats.MAGIC_ATTACK_SPEED, 0x40, mob, new LambdaConst(castSpdMul)));
		}
		
		if (hpMul != 1.0)
		{
			mob.setCurrentHp(mob.getMaxHp());
			mob.setCurrentMp(mob.getMaxMp());
		}
	}
	
	private int getAbnormalMask(String name)
	{
		if (name == null)
		{
			return 0;
		}
		switch (name.toUpperCase())
		{
			case "BLEEDING":
				return 0x0001;
			case "POISON":
				return 0x0002;
			case "REDCIRCLE":
				return 0x0004;
			case "ICE":
				return 0x0008;
			case "STUN":
				return 0x0040;
			case "SLEEP":
				return 0x0080;
			case "ROOT":
				return 0x0200;
			case "BIG_HEAD":
				return 0x2000;
			case "FLAME":
				return 0x4000;
			case "STEALTH":
				return 0x40000;
			case "MAGIC_CIRCLE":
				return 0x200000;
			case "VP_UP":
				return 0x400000;
			default:
				return 0x4000;
		}
	}
	
	private void despawnEventMonsters()
	{
		for (L2Spawn spawn : _spawnedEventMobs)
		{
			if (spawn == null)
			{
				continue;
			}
			spawn.stopRespawn();
			if (spawn.getLastSpawn() != null)
			{
				spawn.getLastSpawn().deleteMe();
			}
		}
		_spawnedEventMobs.clear();
	}
	
	public void notifyKill(L2PcInstance killer, L2Character target)
	{
		if (!_inProgress || killer == null || target == null)
		{
			return;
		}
		if (!_playersInside.contains(killer))
		{
			return;
		}
		
		PlayerStats stats = _playerStats.get(killer.getObjectId());
		if (stats == null)
		{
			stats = new PlayerStats(killer.getName(), killer.getInstanceId());
			_playerStats.put(killer.getObjectId(), stats);
		}
		
		if (target instanceof L2MonsterInstance)
		{
			L2MonsterInstance mob = (L2MonsterInstance) target;
			if (TARGET_MOBS.contains(mob.getNpcId()) && (mob.getInstanceId() == INSTANCE_PVP || mob.getInstanceId() == INSTANCE_SOLO))
			{
				stats.mobKills++;
				
				int dropId = (mob.getInstanceId() == INSTANCE_SOLO) ? DROP_SOLO_ID : DROP_PVP_ID;
				int dropCount = (mob.getInstanceId() == INSTANCE_SOLO) ? DROP_SOLO_COUNT : DROP_PVP_COUNT;
				int dropChance = (mob.getInstanceId() == INSTANCE_SOLO) ? DROP_SOLO_CHANCE : DROP_PVP_CHANCE;
				
				if (Rnd.get(100) < dropChance)
				{
					killer.addItem("DungeonDrop", dropId, dropCount, mob, true);
					if (REWARD_SOUND != null && !REWARD_SOUND.isEmpty())
					{
						killer.sendPacket(new PlaySound(REWARD_SOUND));
					}
				}
			}
		}
		else if (target instanceof L2PcInstance)
		{
			if (killer.getInstanceId() == INSTANCE_PVP)
			{
				stats.pvpKills++;
				killer.sendPacket(new ExShowScreenMessage("You killed " + target.getName() + "!", 3000));
			}
		}
	}
	
	private boolean validatePlayerCondition(L2PcInstance player)
	{
		if (player == null || player.isOnline() == 0)
		{
			return false;
		}
		if (player.isInOfflineMode() || player.getClient() == null || player.getClient().isDetached())
		{
			return false;
		}
		
		if (player.isInsideZone(ZoneId.ZONE_RANDOM) || player.isInJail() || player.isInOlympiadMode() || player.getOlympiadGameId() > -1)
		{
			player.sendMessage("[Dungeon] You cannot participate from this zone or state.");
			return false;
		}
		if (OlympiadManager.getInstance().isRegistered(player))
		{
			player.sendMessage("[Dungeon] You are registered in Olympiad. Unregister first.");
			return false;
		}
		if (player.isInDuel())
		{
			player.sendMessage("[Dungeon] You cannot participate during a duel.");
			return false;
		}
		if (player.getKarma() > 0)
		{
			player.sendMessage("[Dungeon] PKs cannot participate.");
			return false;
		}
		if (player.isDead() || player.isAlikeDead() || player.isFakeDeath())
		{
			player.sendMessage("[Dungeon] You cannot participate while dead.");
			return false;
		}
		if (player.isInCombat() || player.isCastingNow() || player.isAttackingNow())
		{
			player.sendMessage("[Dungeon] You cannot participate while in combat.");
			return false;
		}
		if (player.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_NONE || player.isProcessingTransaction())
		{
			player.sendMessage("[Dungeon] Close stores and cancel trades before entering.");
			return false;
		}
		if (player.isCursedWeaponEquiped())
		{
			player.sendMessage("[Dungeon] Cursed Weapon owners cannot enter.");
			return false;
		}
		if (player.isMounted() || (player.getPet() != null))
		{
			if (!ALLOW_SUMMON)
			{
				player.sendMessage("[Dungeon] Unsummon your pet or dismount before entering.");
				return false;
			}
		}
		return true;
	}
	
	private void removeRestrictedBuffs(L2PcInstance player)
	{
		int[] restrictedSkills =
		{
			406,
			139,
			176,
			420,
			1323,
			1325
		};
		for (L2Effect effect : player.getAllEffects())
		{
			if (effect == null)
			{
				continue;
			}
			for (int id : restrictedSkills)
			{
				if (effect.getSkill().getId() == id)
				{
					player.stopSkillEffects(id);
				}
			}
		}
	}
	
	private void preparePlayerForEvent(L2PcInstance player)
	{
		player.abortAttack();
		player.abortCast();
		player.setTarget(null);
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.stopMove(null);
		if (player.getCubics() != null && !player.getCubics().isEmpty())
		{
			player.getCubics().clear();
		}
		if (player.isMounted())
		{
			player.dismount();
		}
		if (!ALLOW_SUMMON && player.getPet() != null)
		{
			player.getPet().unSummon(player);
		}
		if (player.getParty() != null)
		{
			player.leaveParty();
		}
		
		removeRestrictedBuffs(player);
		player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
		player.setCurrentCp(player.getMaxCp());
	}
	
	private void handleEntryRequest(L2PcInstance player, boolean isPvP)
	{
		if (!_inProgress)
		{
			player.sendMessage("[Dungeon] Event is closed.");
			return;
		}
		
		if (!player.getInventory().validateCapacity(1))
		{
			player.sendMessage("[Dungeon] Inventory full.");
			return;
		}
		if (!player.getInventory().validateWeight(100))
		{
			player.sendMessage("[Dungeon] Inventory too heavy.");
			return;
		}
		
		if (_playersInside.contains(player) || player.getInstanceId() == INSTANCE_PVP || player.getInstanceId() == INSTANCE_SOLO)
		{
			player.sendMessage("[Dungeon] You are already in the Dungeon.");
			return;
		}
		if (_leaveCooldowns.containsKey(player.getObjectId()))
		{
			long unlockTime = _leaveCooldowns.get(player.getObjectId());
			if (System.currentTimeMillis() < unlockTime)
			{
				long secondsLeft = (unlockTime - System.currentTimeMillis()) / 1000;
				player.sendMessage(String.format("[Dungeon] You can re-enter in %d min and %d sec.", secondsLeft / 60, secondsLeft % 60));
				return;
			}
			_leaveCooldowns.remove(player.getObjectId());
		}
		if (player.getLevel() < MIN_LEVEL || player.getLevel() > MAX_LEVEL)
		{
			player.sendMessage("[Dungeon] Invalid Level.");
			return;
		}
		if (!PROHIBITED_CLASS_IDS.isEmpty() && PROHIBITED_CLASS_IDS.contains(player.getClassId().getId()))
		{
			player.sendMessage("[Dungeon] Your class is restricted.");
			return;
		}
		if (ENTRY_FEE_COUNT > 0)
		{
			if (player.getInventory().getInventoryItemCount(ENTRY_FEE_ID, -1) < ENTRY_FEE_COUNT)
			{
				player.sendMessage("[Dungeon] Requires " + ENTRY_FEE_COUNT + " of " + ENTRY_FEE_ID);
				return;
			}
			player.destroyItemByItemId("DungeonEntry", ENTRY_FEE_ID, ENTRY_FEE_COUNT, player, true);
		}
		
		preparePlayerForEvent(player);
		player.disableAllSkills();
		player.broadcastPacket(new MagicSkillUser(player, player, 1050, 1, TELEPORT_DELAY * 1000, 0));
		player.sendPacket(new SetupGauge(0, TELEPORT_DELAY * 1000));
		
		player.sendPacket(new ExShowScreenMessage("Teleporting in " + TELEPORT_DELAY + " seconds...", 3000));
		
		int instanceId = isPvP ? INSTANCE_PVP : INSTANCE_SOLO;
		ThreadPoolManager.getInstance().scheduleGeneral(() -> executeTeleport(player, instanceId, isPvP), TELEPORT_DELAY * 1000);
	}
	
	private void executeTeleport(L2PcInstance player, int instanceId, boolean isPvP)
	{
		if (player == null || player.isOnline() == 0 || _currentLocation == null)
		{
			return;
		}
		
		_originalLocations.put(player.getObjectId(), new Location(player.getX(), player.getY(), player.getZ()));
		player.setIsInvul(true);
		player.setIsParalyzed(true);
		
		player.sendPacket(new ExShowScreenMessage("PREPARE YOURSELF! 10 SECONDS!", 5000));
		
		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			if (player.isOnline() == 1)
			{
				player.setIsInvul(false);
				player.setIsParalyzed(false);
				player.sendPacket(new ExShowScreenMessage("FIGHT !!!", 3000));
			}
		}, 10000);
		
		player.enableAllSkills();
		player.setTarget(null);
		player.setInstanceId(instanceId);
		
		if (isPvP)
		{
			player.setInsideZone(ZoneId.ZONE_PVP, true);
		}
		else
		{
			player.setInsideZone(ZoneId.ZONE_PEACE, true);
		}
		
		Location loc = _currentLocation.getRandomPoint();
		player.teleToLocation(loc.getX() + Rnd.get(-50, 50), loc.getY() + Rnd.get(-50, 50), loc.getZ(), true);
		
		if (!_playersInside.contains(player))
		{
			_playersInside.add(player);
			_playerStats.put(player.getObjectId(), new PlayerStats(player.getName(), instanceId));
		}
		player.broadcastUserInfo();
	}
	
	public void onDeath(L2PcInstance victim, L2Character killer)
	{
		if (!_inProgress || victim == null || !_playersInside.contains(victim))
		{
			return;
		}
		
		notifyKill((killer instanceof L2PcInstance) ? (L2PcInstance) killer : null, victim);
		
		int respawnDelay = 10;
		victim.sendPacket(new ExShowScreenMessage("Respawn in " + respawnDelay + " seconds...", 5000));
		
		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			if (victim.isOnline() == 1 && _playersInside.contains(victim))
			{
				victim.doRevive();
				victim.setCurrentCp(victim.getMaxCp());
				victim.setCurrentHp(victim.getMaxHp());
				victim.setCurrentMp(victim.getMaxMp());
				Location loc = _currentLocation.getRandomPoint();
				victim.teleToLocation(loc.getX(), loc.getY(), loc.getZ(), true);
				victim.setIsInvul(true);
				ThreadPoolManager.getInstance().scheduleGeneral(() -> victim.setIsInvul(false), 3000);
			}
		}, respawnDelay * 1000);
	}
	
	private void handleLeaveCommand(L2PcInstance player)
	{
		if (!_playersInside.contains(player))
		{
			player.sendMessage("[Dungeon] You are not in the Dungeon.");
			return;
		}
		if (player.isAttackingNow() || player.isCastingNow() || player.isInCombat())
		{
			player.sendMessage("[Dungeon] Cannot leave during combat.");
			return;
		}
		int delayInSeconds = TELEPORT_DELAY;
		player.sendMessage("[Dungeon] Leaving in " + delayInSeconds + " seconds...");
		player.disableAllSkills();
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setTarget(player);
		player.broadcastPacket(new MagicSkillUser(player, player, 2099, 1, delayInSeconds * 1000, 0));
		player.sendPacket(new SetupGauge(0, delayInSeconds * 1000));
		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			if (player.isOnline() == 1 && _playersInside.contains(player))
			{
				player.enableAllSkills();
				exitDungeon(player, true);
				if (REENTRY_COOLDOWN_SECONDS > 0)
				{
					_leaveCooldowns.put(player.getObjectId(), System.currentTimeMillis() + (REENTRY_COOLDOWN_SECONDS * 1000L));
					long minutes = REENTRY_COOLDOWN_SECONDS / 60;
					player.sendMessage(minutes > 0 ? "[Dungeon] Re-entry blocked for " + minutes + " minutes." : "[Dungeon] Re-entry blocked for " + REENTRY_COOLDOWN_SECONDS + " seconds.");
				}
			}
			else
			{
				player.enableAllSkills();
			}
		}, delayInSeconds * 1000);
	}
	
	private static void exitDungeon(L2PcInstance player, boolean removeFromList)
	{
		if (player == null)
		{
			return;
		}
		if (player.getInstanceId() == INSTANCE_PVP)
		{
			player.setInsideZone(ZoneId.ZONE_PVP, false);
		}
		else if (player.getInstanceId() == INSTANCE_SOLO)
		{
			player.setInsideZone(ZoneId.ZONE_PEACE, false);
		}
		
		player.setInstanceId(0);
		
		Location orig = _originalLocations.remove(player.getObjectId());
		if (orig != null)
		{
			player.teleToLocation(83400, 148000, -3400);
		}
		else
		{
			player.teleToLocation(EXIT_LOCATION, true);
		}
		
		if (removeFromList)
		{
			_playersInside.remove(player);
		}
		if (player.isOnline() == 1)
		{
			player.broadcastUserInfo();
		}
	}
	
	private static void processRewards()
	{
		if (_playerStats.isEmpty())
		{
			return;
		}
		List<PlayerStats> soloList = new ArrayList<>();
		for (PlayerStats ps : _playerStats.values())
		{
			if (ps.instanceId == INSTANCE_SOLO && ps.mobKills > 0)
			{
				soloList.add(ps);
			}
		}
		
		soloList.sort(Comparator.comparingInt((PlayerStats p) -> p.mobKills).reversed());
		
		if (!soloList.isEmpty())
		{
			giveReward(soloList.get(0), REWARD_SOLO_TOP1_ID, REWARD_SOLO_TOP1_COUNT, "Top 1 Solo");
		}
		if (soloList.size() > 1)
		{
			giveReward(soloList.get(1), REWARD_SOLO_TOP2_ID, REWARD_SOLO_TOP2_COUNT, "Top 2 Solo");
		}
		if (soloList.size() > 2)
		{
			giveReward(soloList.get(2), REWARD_SOLO_TOP3_ID, REWARD_SOLO_TOP3_COUNT, "Top 3 Solo");
		}
		
		List<PlayerStats> pvpList = new ArrayList<>();
		for (PlayerStats ps : _playerStats.values())
		{
			if (ps.instanceId == INSTANCE_PVP && ps.pvpKills > 0)
			{
				pvpList.add(ps);
			}
		}
		
		pvpList.sort(Comparator.comparingInt((PlayerStats p) -> p.pvpKills).reversed().thenComparingInt(p -> p.mobKills).reversed());
		
		if (!pvpList.isEmpty())
		{
			giveReward(pvpList.get(0), REWARD_PVP_TOP1_ID, REWARD_PVP_TOP1_COUNT, "Top 1 PvP");
		}
		if (pvpList.size() > 1)
		{
			giveReward(pvpList.get(1), REWARD_PVP_TOP2_ID, REWARD_PVP_TOP2_COUNT, "Top 2 PvP");
		}
		if (pvpList.size() > 2)
		{
			giveReward(pvpList.get(2), REWARD_PVP_TOP3_ID, REWARD_PVP_TOP3_COUNT, "Top 3 PvP");
		}
	}
	
	private static void giveReward(PlayerStats stats, int itemId, int count, String title)
	{
		L2PcInstance player = L2World.getInstance().getPlayer(stats.name);
		if (player != null && count > 0)
		{
			player.addItem("DungeonReward", itemId, count, player, true);
			player.sendPacket(new ExShowScreenMessage(title + " WINNER!", 6000));
			Announcements.getInstance().gameAnnounceToAll(EVENT_NAME + " " + title + ": " + stats.name);
		}
	}
	
	private static void startZoneCheckTask()
	{
		if (_zoneCheckTask != null)
		{
			_zoneCheckTask.cancel(false);
		}
		_zoneCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
		{
			if (!_inProgress || _currentLocation == null)
			{
				return;
			}
			Location center = _currentLocation.getCenter();
			for (L2PcInstance player : _playersInside)
			{
				if (player == null || player.isOnline() == 0)
				{
					continue;
				}
				if (!player.isInsideRadius(center.getX(), center.getY(), SPAWN_RADIUS, false))
				{
					player.sendMessage("[Dungeon] Out of zone! Returning...");
					Location loc = _currentLocation.getRandomPoint();
					player.teleToLocation(loc.getX(), loc.getY(), loc.getZ(), true);
				}
			}
		}, 3000, 3000);
	}
	
	private static void startMonitorTask()
	{
		if (_monitorTask != null)
		{
			_monitorTask.cancel(false);
		}
		_monitorTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
		{
			if (!_inProgress)
			{
				return;
			}
			for (L2PcInstance player : _playersInside)
			{
				if (player == null || player.isOnline() == 0 || player.getClient() == null || player.getClient().isDetached())
				{
					exitDungeon(player, true);
				}
			}
		}, 2000, 2000);
	}
	
	private static void startAnnounceTask()
	{
		if (_announceTask != null)
		{
			_announceTask.cancel(false);
		}
		_announceTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
		{
			if (!_inProgress)
			{
				return;
			}
			long timeLeft = (_eventEndTime - System.currentTimeMillis()) / 1000;
			if (timeLeft <= 0)
			{
				return;
			}
			if (timeLeft == 600 || timeLeft == 300 || timeLeft == 60)
			{
				Announcements.getInstance().gameAnnounceToAll(EVENT_NAME + ": Closing in " + (timeLeft / 60) + " min!");
			}
		}, 1000, 1000);
	}
	
	public static java.util.List<L2PcInstance> getPlayers()
	{
		return _playersInside;
	}
}