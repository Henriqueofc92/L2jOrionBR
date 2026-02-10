package l2jorion.game.datatables.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import l2jorion.Config;
import l2jorion.game.model.L2TeleportLocation;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;

public class TeleportLocationTable
{
	private static final Logger LOG = LoggerFactory.getLogger(TeleportLocationTable.class.getName());
	
	private static TeleportLocationTable _instance;
	
	private Map<Integer, L2TeleportLocation> _teleports;
	
	public static TeleportLocationTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new TeleportLocationTable();
		}
		
		return _instance;
	}
	
	private TeleportLocationTable()
	{
		reloadAll();
	}
	
	public void reloadAll()
	{
		_teleports = new ConcurrentHashMap<>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File f = new File(Config.DATAPACK_ROOT + "/data/xml/world/teleports.xml");
		if (!f.exists())
		{
			LOG.error("teleports.xml could not be loaded: file not found");
			return;
		}
		try (FileInputStream fis = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8"))
		{
			
			InputSource in = new InputSource(isr);
			in.setEncoding("UTF-8");
			Document doc = factory.newDocumentBuilder().parse(in);
			L2TeleportLocation teleport;
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if (n.getNodeName().equalsIgnoreCase("list"))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if (d.getNodeName().equalsIgnoreCase("teleport"))
						{
							teleport = new L2TeleportLocation();
							int id = Integer.valueOf(d.getAttributes().getNamedItem("id").getNodeValue());
							int loc_x = Integer.valueOf(d.getAttributes().getNamedItem("loc_x").getNodeValue());
							int loc_y = Integer.valueOf(d.getAttributes().getNamedItem("loc_y").getNodeValue());
							int loc_z = Integer.valueOf(d.getAttributes().getNamedItem("loc_z").getNodeValue());
							int price = Integer.valueOf(d.getAttributes().getNamedItem("price").getNodeValue());
							int fornoble = Integer.valueOf(d.getAttributes().getNamedItem("fornoble").getNodeValue());
							
							teleport.setTeleId(id);
							teleport.setLocX(loc_x);
							teleport.setLocY(loc_y);
							teleport.setLocZ(loc_z);
							teleport.setPrice(price);
							teleport.setIsForNoble(fornoble == 1);
							
							_teleports.put(teleport.getTeleId(), teleport);
							teleport = null;
						}
					}
				}
			}
		}
		catch (SAXException | IOException e)
		{
			LOG.error("Error while creating table", e);
		}
		catch (Exception e)
		{
			LOG.error("Error while creating table", e);
		}
		
		LOG.info("TeleportLocationTable: Loaded " + _teleports.size() + " teleport location templates.");
	}
	
	public L2TeleportLocation getTemplate(int id)
	{
		return _teleports.get(id);
	}
}
