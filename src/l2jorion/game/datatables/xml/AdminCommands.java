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

public class AdminCommands
{
	private static final Logger LOG = LoggerFactory.getLogger(AdminCommands.class.getName());
	
	private static AdminCommands _instance = null;
	
	private final Map<String, Integer> _adminCommandAccessRights = new HashMap<>();
	
	private AdminCommands()
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		String adminCommand;
		int accessLevels = 1;
		File f = new File(Config.DATAPACK_ROOT + "/data/xml/player/adminCommands.xml");
		if (!f.exists())
		{
			LOG.warn("adminCommands.xml could not be loaded: file not found");
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
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("adm_cmd".equalsIgnoreCase(d.getNodeName()))
						{
							adminCommand = String.valueOf(d.getAttributes().getNamedItem("adminCommand").getNodeValue());
							accessLevels = Integer.valueOf(d.getAttributes().getNamedItem("accessLevel").getNodeValue());
							_adminCommandAccessRights.put(adminCommand, accessLevels);
						}
					}
				}
			}
		}
		catch (SAXException | IOException | ParserConfigurationException e)
		{
			LOG.error("Admin Access Rights: Error loading from database", e);
		}
		
		LOG.info("AdminAccessRights: Loaded " + _adminCommandAccessRights.size() + " access rights from xml.");
	}
	
	public static AdminCommands getInstance()
	{
		return _instance == null ? (_instance = new AdminCommands()) : _instance;
	}
	
	public boolean hasAccess(String adminCommand, AccessLevel accessLevel)
	{
		if (accessLevel.getLevel() <= 0 || !accessLevel.isGm())
		{
			return false;
		}
		
		if (accessLevel.getLevel() == Config.MASTERACCESS_LEVEL)
		{
			return true;
		}
		
		String command = adminCommand;
		
		if (adminCommand.contains(" "))
		{
			command = adminCommand.substring(0, adminCommand.indexOf(" "));
		}
		
		int acar = _adminCommandAccessRights.getOrDefault(command, 0);
		
		if (acar == 0)
		{
			LOG.info("Admin Access Rights: No rights defined for admin command " + command + ".");
			return false;
		}
		
		return acar >= accessLevel.getLevel();
	}
	
	public int accessRightForCommand(final String command)
	{
		return _adminCommandAccessRights.getOrDefault(command, -1);
	}
	
	public static void reload()
	{
		_instance = null;
		getInstance();
	}
}
