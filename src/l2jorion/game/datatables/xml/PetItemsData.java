package l2jorion.game.datatables.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import l2jorion.Config;
import l2jorion.game.model.L2SummonItem;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;

public class PetItemsData
{
	private static final Logger LOG = LoggerFactory.getLogger(PetItemsData.class.getName());
	
	private final HashMap<Integer, L2SummonItem> _summonItems;
	
	private static PetItemsData _instance;
	
	public static PetItemsData getInstance()
	{
		if (_instance == null)
		{
			_instance = new PetItemsData();
		}
		
		return _instance;
	}
	
	private PetItemsData()
	{
		_summonItems = new HashMap<>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File f = new File(Config.DATAPACK_ROOT + "/data/xml/player/summonItems.xml");
		if (!f.exists())
		{
			LOG.warn("summon_items.xml could not be loaded: file not found");
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
						if ("summon_item".equalsIgnoreCase(d.getNodeName()))
						{
							int itemID = Integer.parseInt(d.getAttributes().getNamedItem("itemID").getNodeValue());
							int npcID = Integer.parseInt(d.getAttributes().getNamedItem("npcID").getNodeValue());
							byte summonType = Byte.parseByte(d.getAttributes().getNamedItem("summonType").getNodeValue());
							
							L2SummonItem summonItem = new L2SummonItem(itemID, npcID, summonType);
							_summonItems.put(itemID, summonItem);
						}
					}
				}
			}
		}
		catch (SAXException | IOException | ParserConfigurationException e)
		{
			LOG.error("Error while creating table: " + e.getMessage());
		}
		
		LOG.info("Summon: Loaded " + _summonItems.size() + " summon items.");
	}
	
	public L2SummonItem getSummonItem(int itemId)
	{
		return _summonItems.get(itemId);
	}
	
	public int[] itemIDs()
	{
		int size = _summonItems.size();
		int[] result = new int[size];
		int i = 0;
		
		for (L2SummonItem si : _summonItems.values())
		{
			result[i] = si.getItemId();
			i++;
		}
		return result;
	}
}
