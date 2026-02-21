package l2jorion.game.datatables.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import l2jorion.Config;
import l2jorion.game.idfactory.IdFactory;
import l2jorion.game.model.actor.instance.L2StaticObjectInstance;

public class StaticObjects
{
	private static final Logger LOG = Logger.getLogger(StaticObjects.class.getName());
	
	private static StaticObjects _instance;
	private final ConcurrentMap<Integer, L2StaticObjectInstance> _staticObjects;
	
	public static StaticObjects getInstance()
	{
		if (_instance == null)
		{
			_instance = new StaticObjects();
		}
		
		return _instance;
	}
	
	public StaticObjects()
	{
		_staticObjects = new ConcurrentHashMap<>();
		parseData();
		LOG.info("StaticObject: Loaded " + _staticObjects.size() + " static object templates.");
	}
	
	private void parseData()
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File f = new File(Config.DATAPACK_ROOT + "/data/xml/world/staticObjects.xml");
		if (!f.exists())
		{
			LOG.warning("staticobjects.xml could not be loaded: file not found");
			return;
		}
		
		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))
		{
			Document doc = factory.newDocumentBuilder().parse(new InputSource(reader));
			
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("staticobject".equalsIgnoreCase(d.getNodeName()))
						{
							int id = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
							int x = Integer.parseInt(d.getAttributes().getNamedItem("x").getNodeValue());
							int y = Integer.parseInt(d.getAttributes().getNamedItem("y").getNodeValue());
							int z = Integer.parseInt(d.getAttributes().getNamedItem("z").getNodeValue());
							int type = Integer.parseInt(d.getAttributes().getNamedItem("type").getNodeValue());
							String texture = d.getAttributes().getNamedItem("texture").getNodeValue();
							int map_x = Integer.parseInt(d.getAttributes().getNamedItem("map_x").getNodeValue());
							int map_y = Integer.parseInt(d.getAttributes().getNamedItem("map_y").getNodeValue());
							
							L2StaticObjectInstance obj = new L2StaticObjectInstance(IdFactory.getInstance().getNextId());
							obj.setType(type);
							obj.setStaticObjectId(id);
							obj.setXYZ(x, y, z);
							obj.setMap(texture, map_x, map_y);
							obj.spawnMe();
							_staticObjects.put(obj.getStaticObjectId(), obj);
						}
					}
				}
			}
		}
		catch (SAXException | IOException | ParserConfigurationException e)
		{
			LOG.severe("Error while creating StaticObjects table: " + e.getMessage());
		}
	}
}
