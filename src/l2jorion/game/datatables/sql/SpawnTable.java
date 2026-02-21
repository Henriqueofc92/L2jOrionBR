package l2jorion.game.datatables.sql;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2jorion.Config;
import l2jorion.game.managers.DayNightSpawnManager;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.spawn.L2Spawn;
import l2jorion.game.model.spawn.SpawnTerritory;
import l2jorion.game.templates.L2NpcTemplate;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;

public class SpawnTable
{
	private final static Logger LOG = LoggerFactory.getLogger(SpawnTable.class);
	
	private static final SpawnTable _instance = new SpawnTable();
	
	private Map<Integer, L2Spawn> spawntable = new HashMap<>();
	private int customSpawnCount;
	private int _highestId;
	
	private static final String SPAWN_XML_FOLDER = "./data/xml/spawns/";
	private static final String CUSTOM_SPAWN_FILE = "./data/xml/spawns/custom/custom_spawns.xml";
	
	public static SpawnTable getInstance()
	{
		return _instance;
	}
	
	private SpawnTable()
	{
		if (!Config.ALT_DEV_NO_SPAWNS)
		{
			loadXmlSpawns();
		}
	}
	
	public Map<Integer, L2Spawn> getSpawnTable()
	{
		return spawntable;
	}
	
	private void loadXmlSpawns()
	{
		File dir = new File(SPAWN_XML_FOLDER);
		if (!dir.exists())
		{
			LOG.warn("SpawnTable: Folder " + SPAWN_XML_FOLDER + " does not exist!");
			return;
		}
		AtomicInteger loadedFiles = new AtomicInteger(0);
		
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			parseDirectory(dir, builder, loadedFiles);
		}
		catch (Exception e)
		{
			LOG.error("SpawnTable: Critical Error initializing XML parser", e);
		}
		
		LOG.info("SpawnTable: Spawning completed. Loaded " + spawntable.size() + " spawns from " + loadedFiles.get() + " files.");
		if (customSpawnCount > 0)
		{
			LOG.info("SpawnTable: Custom/Event/DayNight spawns processed: " + customSpawnCount);
		}
	}
	
	private void parseDirectory(File dir, DocumentBuilder builder, AtomicInteger loadedFiles)
	{
		File[] files = dir.listFiles();
		if (files == null)
		{
			return;
		}
		
		for (File file : files)
		{
			if (file.isDirectory())
			{
				parseDirectory(file, builder, loadedFiles);
			}
			else if (file.getName().endsWith(".xml"))
			{
				try
				{
					parseFile(builder, file);
					loadedFiles.incrementAndGet();
				}
				catch (Exception e)
				{
					LOG.warn("SpawnTable: Error loading file " + file.getName() + ": " + e.getMessage());
				}
			}
		}
	}
	
	private void parseFile(DocumentBuilder builder, File file) throws Exception
	{
		Document doc = builder.parse(file);
		for (Node listNode = doc.getFirstChild(); listNode != null; listNode = listNode.getNextSibling())
		{
			if ("list".equalsIgnoreCase(listNode.getNodeName()))
			{
				for (Node spawnNode = listNode.getFirstChild(); spawnNode != null; spawnNode = spawnNode.getNextSibling())
				{
					if ("spawn".equalsIgnoreCase(spawnNode.getNodeName()))
					{
						processSpawnGroup(spawnNode, file.getName());
					}
				}
			}
		}
	}
	
	private void processSpawnGroup(Node spawnNode, String fileName)
	{
		NamedNodeMap spawnAttrs = spawnNode.getAttributes();
		boolean spawnByDefault = parseBoolean(spawnAttrs, "spawn_bydefault", true);
		String eventName = parseString(spawnAttrs, "event_name", "");
		String spawnName = parseString(spawnAttrs, "name", "unnamed");
		
		SpawnTerritory territory = null;
		
		for (Node child = spawnNode.getFirstChild(); child != null; child = child.getNextSibling())
		{
			if ("territory".equalsIgnoreCase(child.getNodeName()))
			{
				territory = new SpawnTerritory(spawnName);
				for (Node loc = child.getFirstChild(); loc != null; loc = loc.getNextSibling())
				{
					if ("location".equalsIgnoreCase(loc.getNodeName()))
					{
						NamedNodeMap locAttrs = loc.getAttributes();
						int x = parseInteger(locAttrs, "x", 0);
						int y = parseInteger(locAttrs, "y", 0);
						int minz = parseInteger(locAttrs, "minz", -32000);
						int maxz = parseInteger(locAttrs, "maxz", 32000);
						territory.addPoint(x, y, minz, maxz);
					}
				}
			}
		}
		
		for (Node child = spawnNode.getFirstChild(); child != null; child = child.getNextSibling())
		{
			if ("npc".equalsIgnoreCase(child.getNodeName()))
			{
				NamedNodeMap npcAttrs = child.getAttributes();
				int npcId = parseInteger(npcAttrs, "id", 0);
				int count = parseInteger(npcAttrs, "count", 1);
				int respawn = parseInteger(npcAttrs, "respawn", 60);
				String pos = parseString(npcAttrs, "pos", null);
				
				L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
				
				if (template == null)
				{
					continue;
				}
				if (template.type.equalsIgnoreCase("L2SiegeGuard") || template.type.equalsIgnoreCase("L2RaidBoss") || template.type.equalsIgnoreCase("L2GrandBoss") || (!Config.ALLOW_CLASS_MASTERS && template.type.equals("L2ClassMaster")))
				{
					continue;
				}
				
				try
				{
					L2Spawn spawn = new L2Spawn(template);
					spawn.setAmount(count);
					spawn.setRespawnDelay(respawn);
					spawn.setCustom(!spawnByDefault);
					
					if (pos != null && !pos.isEmpty())
					{
						String[] coords = pos.split(" ");
						spawn.setLocx(Integer.parseInt(coords[0]));
						spawn.setLocy(Integer.parseInt(coords[1]));
						spawn.setLocz(Integer.parseInt(coords[2]));
						spawn.setHeading(coords.length > 3 ? Integer.parseInt(coords[3]) : 0);
					}
					else if (territory != null)
					{
						spawn.setTerritory(territory);
						int[] p = territory.getRandomPoint();
						spawn.setLocx(p[0]);
						spawn.setLocy(p[1]);
						spawn.setLocz(p[2]);
						spawn.setHeading(-1);
					}
					else
					{
						continue;
					}
					if (_highestId < spawn.getId())
					{
						_highestId = spawn.getId();
					}
					
					_highestId++;
					spawn.setId(_highestId);
					
					spawntable.put(spawn.getId(), spawn);
					
					boolean isEvent = false;
					if (eventName != null && !eventName.isEmpty())
					{
						if (eventName.toUpperCase().contains("DAY"))
						{
							DayNightSpawnManager.getInstance().addDayCreature(spawn);
							customSpawnCount++;
							isEvent = true;
						}
						else if (eventName.toUpperCase().contains("NIGHT"))
						{
							DayNightSpawnManager.getInstance().addNightCreature(spawn);
							customSpawnCount++;
							isEvent = true;
						}
					}
					
					if (spawnByDefault && !isEvent)
					{
						spawn.init();
					}
					else if (!spawnByDefault)
					{
						customSpawnCount++;
					}
				}
				catch (Exception e)
				{
					LOG.warn("SpawnTable: Error creating spawn for NPC " + npcId + ": " + e.getMessage());
				}
			}
		}
	}
	
	public void addNewSpawn(final L2Spawn spawn, final boolean storeInDb)
	{
		_highestId++;
		spawn.setId(_highestId);
		spawntable.put(_highestId, spawn);
		if (storeInDb)
		{
			saveSpawnToXml(spawn);
		}
	}
	
	public void deleteSpawn(final L2Spawn spawn, final boolean updateDb)
	{
		spawntable.remove(spawn.getId());
		
		if (updateDb)
		{
			deleteSpawnFromXml(spawn);
		}
	}
	
	private void saveSpawnToXml(L2Spawn spawn)
	{
		File file = new File(CUSTOM_SPAWN_FILE);
		Document doc = null;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		
		try
		{
			if (!file.exists())
			{
				file.getParentFile().mkdirs();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				doc = dBuilder.newDocument();
				Element rootElement = doc.createElement("list");
				doc.appendChild(rootElement);
			}
			else
			{
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				doc = dBuilder.parse(file);
			}
			
			Element root = doc.getDocumentElement();
			Element spawnElement = doc.createElement("spawn");
			spawnElement.setAttribute("name", "custom_spawn");
			spawnElement.setAttribute("spawn_bydefault", "true");
			Element npcElement = doc.createElement("npc");
			npcElement.setAttribute("id", String.valueOf(spawn.getNpcid()));
			npcElement.setAttribute("count", String.valueOf(spawn.getAmount()));
			npcElement.setAttribute("respawn", String.valueOf(spawn.getRespawnDelay() / 1000));
			String pos = spawn.getLocx() + " " + spawn.getLocy() + " " + spawn.getLocz() + " " + spawn.getHeading();
			npcElement.setAttribute("pos", pos);
			spawnElement.appendChild(npcElement);
			root.appendChild(spawnElement);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(file);
			transformer.transform(source, result);
			LOG.info("SpawnTable: New GM Spawn saved to " + CUSTOM_SPAWN_FILE);
			
		}
		catch (Exception e)
		{
			LOG.warn("SpawnTable: Could not save spawn to XML.", e);
		}
	}
	
	private void deleteSpawnFromXml(L2Spawn spawn)
	{
		File file = new File(CUSTOM_SPAWN_FILE);
		if (!file.exists())
		{
			return;
		}
		
		try
		{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			
			Node listNode = doc.getFirstChild();
			if (listNode == null)
			{
				return;
			}
			
			boolean changed = false;
			String targetPos = spawn.getLocx() + " " + spawn.getLocy() + " " + spawn.getLocz() + " " + spawn.getHeading();
			for (Node spawnNode = listNode.getFirstChild(); spawnNode != null; spawnNode = spawnNode.getNextSibling())
			{
				if ("spawn".equalsIgnoreCase(spawnNode.getNodeName()))
				{
					for (Node npcNode = spawnNode.getFirstChild(); npcNode != null; npcNode = npcNode.getNextSibling())
					{
						if ("npc".equalsIgnoreCase(npcNode.getNodeName()))
						{
							NamedNodeMap attrs = npcNode.getAttributes();
							if (attrs == null)
							{
								continue;
							}
							
							Node idNode = attrs.getNamedItem("id");
							Node posNode = attrs.getNamedItem("pos");
							
							if (idNode != null && posNode != null)
							{
								String id = idNode.getNodeValue();
								String pos = posNode.getNodeValue();
								
								if (id.equals(String.valueOf(spawn.getNpcid())) && pos.equals(targetPos))
								{
									listNode.removeChild(spawnNode);
									changed = true;
									break;
								}
							}
						}
					}
				}
				if (changed)
				{
					break;
				}
			}
			
			if (changed)
			{
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(file);
				transformer.transform(source, result);
				LOG.info("SpawnTable: GM Spawn removed from " + CUSTOM_SPAWN_FILE);
			}
			
		}
		catch (Exception e)
		{
			LOG.warn("SpawnTable: Could not delete spawn from XML.", e);
		}
	}
	
	private int parseInteger(NamedNodeMap attrs, String name, int defaultValue)
	{
		Node node = attrs.getNamedItem(name);
		return (node != null) ? Integer.parseInt(node.getNodeValue()) : defaultValue;
	}
	
	private boolean parseBoolean(NamedNodeMap attrs, String name, boolean defaultValue)
	{
		Node node = attrs.getNamedItem(name);
		return (node != null) ? Boolean.parseBoolean(node.getNodeValue()) : defaultValue;
	}
	
	private String parseString(NamedNodeMap attrs, String name, String defaultValue)
	{
		Node node = attrs.getNamedItem(name);
		return (node != null) ? node.getNodeValue() : defaultValue;
	}
	
	public L2Spawn getTemplate(final int id)
	{
		return spawntable.get(id);
	}
	
	public void reloadAll()
	{
		spawntable.clear();
		customSpawnCount = 0;
		loadXmlSpawns();
	}
	
	public void findNPCInstances(final L2PcInstance activeChar, final int npcId, final int teleportIndex)
	{
		int index = 0;
		for (final L2Spawn spawn : spawntable.values())
		{
			if (npcId == spawn.getNpcid())
			{
				index++;
				if (teleportIndex > -1)
				{
					if (teleportIndex == index)
					{
						activeChar.teleToLocation(spawn.getLocx(), spawn.getLocy(), spawn.getLocz(), true);
					}
				}
				else
				{
					activeChar.sendMessage(index + " - " + spawn.getTemplate().name + " (" + spawn.getId() + "): " + spawn.getLocx() + " " + spawn.getLocy() + " " + spawn.getLocz());
				}
			}
		}
		if (index == 0)
		{
			activeChar.sendMessage("No current spawns found.");
		}
	}
	
	public Map<Integer, L2Spawn> getAllTemplates()
	{
		return spawntable;
	}
}