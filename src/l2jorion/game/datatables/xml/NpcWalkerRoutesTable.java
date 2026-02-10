package l2jorion.game.datatables.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import l2jorion.Config;
import l2jorion.game.datatables.sql.SpawnTable;
import l2jorion.game.model.L2NpcWalkerNode;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;

public class NpcWalkerRoutesTable
{
	private static final Logger LOG = LoggerFactory.getLogger(SpawnTable.class.getName());
	private static NpcWalkerRoutesTable _instance;
	private List<L2NpcWalkerNode> _routes = new ArrayList<>();
	
	public static NpcWalkerRoutesTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new NpcWalkerRoutesTable();
		}
		return _instance;
	}
	
	private NpcWalkerRoutesTable()
	{
		// not here
	}
	
	public void load()
	{
		_routes.clear();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File f = new File(Config.DATAPACK_ROOT + "/data/xml/world/walkerRoutes.xml");
		if (!f.exists())
		{
			LOG.warn("walker_routes.xml could not be loaded: file not found");
			return;
		}
		
		try (FileInputStream fis = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8"))
		{
			InputSource in = new InputSource(isr);
			in.setEncoding("UTF-8");
			Document doc = factory.newDocumentBuilder().parse(in);
			L2NpcWalkerNode route;
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if (n.getNodeName().equalsIgnoreCase("list"))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if (d.getNodeName().equalsIgnoreCase("walker_route"))
						{
							route = new L2NpcWalkerNode();
							
							int route_id = Integer.parseInt(d.getAttributes().getNamedItem("route_id").getNodeValue());
							int npc_id = Integer.parseInt(d.getAttributes().getNamedItem("npc_id").getNodeValue());
							String move_point = d.getAttributes().getNamedItem("move_point").getNodeValue();
							String chatText = d.getAttributes().getNamedItem("chatText").getNodeValue();
							int move_x = Integer.parseInt(d.getAttributes().getNamedItem("move_x").getNodeValue());
							int move_y = Integer.parseInt(d.getAttributes().getNamedItem("move_y").getNodeValue());
							int move_z = Integer.parseInt(d.getAttributes().getNamedItem("move_z").getNodeValue());
							int delay = Integer.parseInt(d.getAttributes().getNamedItem("delay").getNodeValue());
							boolean running = Boolean.parseBoolean(d.getAttributes().getNamedItem("running").getNodeValue());
							
							route.setRouteId(route_id);
							route.setNpcId(npc_id);
							route.setMovePoint(move_point);
							route.setChatText(chatText);
							route.setMoveX(move_x);
							route.setMoveY(move_y);
							route.setMoveZ(move_z);
							route.setDelay(delay);
							route.setRunning(running);
							
							_routes.add(route);
							route = null;
						}
					}
				}
			}
		}
		catch (SAXException | IOException | ParserConfigurationException e)
		{
			LOG.error("Error while creating table", e);
		}
		
		LOG.info("WalkerRoutesTable: Loaded " + _routes.size() + " npc walker routes.");
	}
	
	public List<L2NpcWalkerNode> getRouteForNpc(int id)
	{
		List<L2NpcWalkerNode> _return = new ArrayList<>();
		
		for (L2NpcWalkerNode node : _routes)
		{
			if (node.getNpcId() == id)
			{
				_return.add(node);
			}
		}
		
		return _return;
	}
}
