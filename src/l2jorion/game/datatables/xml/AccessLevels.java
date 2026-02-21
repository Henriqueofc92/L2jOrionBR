package l2jorion.game.datatables.xml;

import java.util.HashMap;
import java.util.Map;

import l2jorion.Config;
import l2jorion.game.datatables.AccessLevel;
import l2jorion.game.datatables.GmAccessProfile;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;

/**
 * Access level manager. Now loads access levels from GmAccessTable (config/GMAccess/*.xml)
 * instead of the old accessLevels.xml file.
 */
public class AccessLevels
{
	private final static Logger LOG = LoggerFactory.getLogger(AccessLevels.class.getName());
	
	private static final int _masterAccessLevelNum = Config.MASTERACCESS_LEVEL;
	private static final int _userAccessLevelNum = 0;
	
	private static AccessLevels _instance = null;
	
	private final Map<Integer, AccessLevel> _accessLevels = new HashMap<>();
	
	public static final AccessLevel _masterAccessLevel = new AccessLevel(_masterAccessLevelNum, "Master Access", Config.MASTERACCESS_NAME_COLOR, Config.MASTERACCESS_TITLE_COLOR, true, true, true, true, true, true, true, true, true, true, true);
	public static final AccessLevel _userAccessLevel = new AccessLevel(_userAccessLevelNum, "User", 0xFFFFFF, 0xFFFFFF, false, false, false, true, false, true, true, true, true, true, false);
	
	private AccessLevels()
	{
		// Load access levels from GmAccessTable profiles
		for (GmAccessProfile profile : GmAccessTable.getInstance().getProfiles())
		{
			int level = profile.getAccessLevel();
			
			if (level == _userAccessLevelNum || level == _masterAccessLevelNum || level <= 0)
			{
				continue;
			}
			
			_accessLevels.put(level, profile.toAccessLevel());
		}
		
		LOG.info("AccessLevels: Loaded " + _accessLevels.size() + " access levels from GMAccess profiles.");
	}
	
	public static AccessLevels getInstance()
	{
		return _instance == null ? (_instance = new AccessLevels()) : _instance;
	}
	
	public AccessLevel getAccessLevel(int accessLevelNum)
	{
		return _accessLevels.get(accessLevelNum);
	}
	
	public void addBanAccessLevel(int accessLevel)
	{
		synchronized (_accessLevels)
		{
			if (accessLevel > -1)
			{
				return;
			}
			
			_accessLevels.put(accessLevel, new AccessLevel(accessLevel, "Banned", 0x000000, 0x000000, false, false, false, false, false, false, false, false, false, false, false));
		}
	}
	
	public static void reload()
	{
		GmAccessTable.reload();
		_instance = null;
		getInstance();
	}
}
