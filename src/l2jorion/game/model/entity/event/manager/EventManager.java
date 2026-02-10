package l2jorion.game.model.entity.event.manager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import l2jorion.game.model.entity.event.CTF;
import l2jorion.game.model.entity.event.DM;
import l2jorion.game.model.entity.event.TvT;
import l2jorion.game.model.entity.event.tournament.Tournament;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;

public class EventManager
{
	private static final Logger LOG = LoggerFactory.getLogger(EventManager.class);
	
	private static final String EVENT_MANAGER_CONFIGURATION_FILE = "./config/mods/events/eventmanager.ini";
	
	public static boolean TVT_EVENT_ENABLED;
	public static List<String> TVT_TIMES_LIST;
	
	public static boolean CTF_EVENT_ENABLED;
	public static List<String> CTF_TIMES_LIST;
	
	public static boolean DM_EVENT_ENABLED;
	public static List<String> DM_TIMES_LIST;
	
	public static boolean TM_EVENT_ENABLED;
	public static List<String> TM_TIMES_LIST;
	
	public static boolean POLL_ENABLED;
	
	private static EventManager instance = null;
	
	private EventManager()
	{
		loadConfiguration();
	}
	
	public static EventManager getInstance()
	{
		if (instance == null)
		{
			instance = new EventManager();
		}
		return instance;
	}
	
	public static void loadConfiguration()
	{
		try (InputStream is = Files.newInputStream(Path.of(EVENT_MANAGER_CONFIGURATION_FILE)))
		{
			Properties eventSettings = new Properties();
			eventSettings.load(is);
			
			TVT_EVENT_ENABLED = Boolean.parseBoolean(eventSettings.getProperty("TVTEventEnabled", "false"));
			TVT_TIMES_LIST = new ArrayList<>(List.of(eventSettings.getProperty("TVTStartTime", "").split(";")));
			
			CTF_EVENT_ENABLED = Boolean.parseBoolean(eventSettings.getProperty("CTFEventEnabled", "false"));
			CTF_TIMES_LIST = new ArrayList<>(List.of(eventSettings.getProperty("CTFStartTime", "").split(";")));
			
			DM_EVENT_ENABLED = Boolean.parseBoolean(eventSettings.getProperty("DMEventEnabled", "false"));
			DM_TIMES_LIST = new ArrayList<>(List.of(eventSettings.getProperty("DMStartTime", "").split(";")));
			
			TM_EVENT_ENABLED = Boolean.parseBoolean(eventSettings.getProperty("TournamentEventEnabled", "false"));
			TM_TIMES_LIST = new ArrayList<>(List.of(eventSettings.getProperty("TournamentStartTime", "").split(";")));
			
			POLL_ENABLED = Boolean.parseBoolean(eventSettings.getProperty("PollEnabled", "false"));
		}
		catch (IOException e)
		{
			LOG.error("Error loading configuration", e);
		}
	}
	
	public void startEventRegistration()
	{
		if (TVT_EVENT_ENABLED)
		{
			registerTvT();
		}
		
		if (CTF_EVENT_ENABLED)
		{
			registerCTF();
		}
		
		if (DM_EVENT_ENABLED)
		{
			registerDM();
		}
		
		if (TM_EVENT_ENABLED)
		{
			registerTM();
		}
	}
	
	public static void registerTvT()
	{
		TvT.loadData();
		
		if (!TvT.checkStartJoinOk())
		{
			LOG.warn("registerTvT: TvT Event is not setted Properly");
		}
		
		EventsGlobalTask.getInstance().clearEventTasksByEventName(TvT.get_eventName());
		
		for (String time : TVT_TIMES_LIST)
		{
			if (time == null || time.trim().isEmpty())
			{
				continue;
			}
			
			TvT newInstance = TvT.getNewInstance();
			newInstance.setEventStartTime(time);
			EventsGlobalTask.getInstance().registerNewEventTask(newInstance);
		}
	}
	
	public static void registerCTF()
	{
		CTF.loadData();
		
		if (!CTF.checkStartJoinOk())
		{
			LOG.warn("registerCTF: CTF Event is not setted Properly");
		}
		
		EventsGlobalTask.getInstance().clearEventTasksByEventName(CTF.get_eventName());
		
		for (String time : CTF_TIMES_LIST)
		{
			if (time == null || time.trim().isEmpty())
			{
				continue;
			}
			
			CTF newInstance = CTF.getNewInstance();
			newInstance.setEventStartTime(time);
			EventsGlobalTask.getInstance().registerNewEventTask(newInstance);
		}
	}
	
	public static void registerDM()
	{
		DM.loadData();
		
		if (!DM.checkStartJoinOk())
		{
			LOG.warn("registerDM: DM Event is not setted Properly");
		}
		
		EventsGlobalTask.getInstance().clearEventTasksByEventName(DM.get_eventName());
		
		for (String time : DM_TIMES_LIST)
		{
			if (time == null || time.trim().isEmpty())
			{
				continue;
			}
			
			DM newInstance = DM.getNewInstance();
			newInstance.setEventStartTime(time);
			EventsGlobalTask.getInstance().registerNewEventTask(newInstance);
		}
	}
	
	public static void registerTM()
	{
		EventsGlobalTask.getInstance().clearEventTasksByEventName(Tournament.get_eventName());
		
		for (String time : TM_TIMES_LIST)
		{
			if (time == null || time.trim().isEmpty())
			{
				continue;
			}
			
			Tournament newInstance = Tournament.getNewInstance();
			newInstance.setEventStartTime(time);
			EventsGlobalTask.getInstance().registerNewEventTask(newInstance);
		}
	}
}