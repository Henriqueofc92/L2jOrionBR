package l2jorion.game.datatables.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import l2jorion.Config;
import l2jorion.game.datatables.AccessLevel;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;

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
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File f = new File(Config.DATAPACK_ROOT + "/data/xml/player/accessLevels.xml");
		
		if (!f.exists())
		{
			LOG.error("access_levels.xml could not be loaded: file not found");
			return;
		}
		
		try (FileInputStream fis = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8"))
		{
			
			InputSource in = new InputSource(isr);
			in.setEncoding("UTF-8");
			Document doc = factory.newDocumentBuilder().parse(in);
			
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if (n.getNodeName().equalsIgnoreCase("list"))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if (d.getNodeName().equalsIgnoreCase("acessLevel"))
						{
							int accessLevel = Integer.valueOf(d.getAttributes().getNamedItem("level").getNodeValue());
							String name = String.valueOf(d.getAttributes().getNamedItem("name").getNodeValue());
							
							if (accessLevel == _userAccessLevelNum || accessLevel == _masterAccessLevelNum || accessLevel < 0)
							{
								continue;
							}
							
							int nameColor = getColor(d, "nameColor", 0xFFFFFF);
							int titleColor = getColor(d, "titleColor", 0x77FFFF);
							
							boolean isGm = Boolean.valueOf(d.getAttributes().getNamedItem("isGm").getNodeValue());
							boolean allowPeaceAttack = Boolean.valueOf(d.getAttributes().getNamedItem("allowPeaceAttack").getNodeValue());
							boolean allowFixedRes = Boolean.valueOf(d.getAttributes().getNamedItem("allowFixedRes").getNodeValue());
							boolean allowTransaction = Boolean.valueOf(d.getAttributes().getNamedItem("allowTransaction").getNodeValue());
							boolean allowAltG = Boolean.valueOf(d.getAttributes().getNamedItem("allowAltg").getNodeValue());
							boolean giveDamage = Boolean.valueOf(d.getAttributes().getNamedItem("giveDamage").getNodeValue());
							boolean takeAggro = Boolean.valueOf(d.getAttributes().getNamedItem("takeAggro").getNodeValue());
							boolean gainExp = Boolean.valueOf(d.getAttributes().getNamedItem("gainExp").getNodeValue());
							boolean useNameColor = Boolean.valueOf(d.getAttributes().getNamedItem("useNameColor").getNodeValue());
							boolean useTitleColor = Boolean.valueOf(d.getAttributes().getNamedItem("useTitleColor").getNodeValue());
							boolean canDisableGmStatus = Boolean.valueOf(d.getAttributes().getNamedItem("canDisableGmStatus").getNodeValue());
							
							_accessLevels.put(accessLevel, new AccessLevel(accessLevel, name, nameColor, titleColor, isGm, allowPeaceAttack, allowFixedRes, allowTransaction, allowAltG, giveDamage, takeAggro, gainExp, useNameColor, useTitleColor, canDisableGmStatus));
						}
					}
				}
			}
		}
		catch (SAXException | IOException | ParserConfigurationException e)
		{
			LOG.error("Error while loading admin command data: ", e);
		}
		
		LOG.info("AccessLevels: Loaded " + _accessLevels.size() + " access from xml.");
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
		_instance = null;
		getInstance();
	}
	
	private int getColor(Node node, String attributeName, int defaultValue)
	{
		try
		{
			return Integer.decode("0x" + String.valueOf(node.getAttributes().getNamedItem(attributeName).getNodeValue()));
		}
		catch (NumberFormatException | NullPointerException e)
		{
			return defaultValue;
		}
	}
}
