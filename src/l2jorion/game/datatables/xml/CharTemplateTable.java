package l2jorion.game.datatables.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import l2jorion.Config;
import l2jorion.game.model.base.ClassId;
import l2jorion.game.templates.L2PcTemplate;
import l2jorion.game.templates.StatsSet;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;

public class CharTemplateTable
{
	private static final Logger _log = LoggerFactory.getLogger(CharTemplateTable.class);
	
	private static CharTemplateTable _instance;
	
	private static final String[] CHAR_CLASSES =
	{
		"Human Fighter",
		"Warrior",
		"Gladiator",
		"Warlord",
		"Human Knight",
		"Paladin",
		"Dark Avenger",
		"Rogue",
		"Treasure Hunter",
		"Hawkeye",
		"Human Mystic",
		"Human Wizard",
		"Sorceror",
		"Necromancer",
		"Warlock",
		"Cleric",
		"Bishop",
		"Prophet",
		"Elven Fighter",
		"Elven Knight",
		"Temple Knight",
		"Swordsinger",
		"Elven Scout",
		"Plainswalker",
		"Silver Ranger",
		"Elven Mystic",
		"Elven Wizard",
		"Spellsinger",
		"Elemental Summoner",
		"Elven Oracle",
		"Elven Elder",
		"Dark Fighter",
		"Palus Knight",
		"Shillien Knight",
		"Bladedancer",
		"Assassin",
		"Abyss Walker",
		"Phantom Ranger",
		"Dark Elven Mystic",
		"Dark Elven Wizard",
		"Spellhowler",
		"Phantom Summoner",
		"Shillien Oracle",
		"Shillien Elder",
		"Orc Fighter",
		"Orc Raider",
		"Destroyer",
		"Orc Monk",
		"Tyrant",
		"Orc Mystic",
		"Orc Shaman",
		"Overlord",
		"Warcryer",
		"Dwarven Fighter",
		"Dwarven Scavenger",
		"Bounty Hunter",
		"Dwarven Artisan",
		"Warsmith",
		"dummyEntry1",
		"dummyEntry2",
		"dummyEntry3",
		"dummyEntry4",
		"dummyEntry5",
		"dummyEntry6",
		"dummyEntry7",
		"dummyEntry8",
		"dummyEntry9",
		"dummyEntry10",
		"dummyEntry11",
		"dummyEntry12",
		"dummyEntry13",
		"dummyEntry14",
		"dummyEntry15",
		"dummyEntry16",
		"dummyEntry17",
		"dummyEntry18",
		"dummyEntry19",
		"dummyEntry20",
		"dummyEntry21",
		"dummyEntry22",
		"dummyEntry23",
		"dummyEntry24",
		"dummyEntry25",
		"dummyEntry26",
		"dummyEntry27",
		"dummyEntry28",
		"dummyEntry29",
		"dummyEntry30",
		"Duelist",
		"DreadNought",
		"Phoenix Knight",
		"Hell Knight",
		"Sagittarius",
		"Adventurer",
		"Archmage",
		"Soultaker",
		"Arcana Lord",
		"Cardinal",
		"Hierophant",
		"Eva Templar",
		"Sword Muse",
		"Wind Rider",
		"Moonlight Sentinel",
		"Mystic Muse",
		"Elemental Master",
		"Eva's Saint",
		"Shillien Templar",
		"Spectral Dancer",
		"Ghost Hunter",
		"Ghost Sentinel",
		"Storm Screamer",
		"Spectral Master",
		"Shillien Saint",
		"Titan",
		"Grand Khauatari",
		"Dominator",
		"Doomcryer",
		"Fortune Seeker",
		"Maestro"
	};
	
	private final Map<Integer, L2PcTemplate> _templates = new HashMap<>();
	
	public static CharTemplateTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new CharTemplateTable();
		}
		return _instance;
	}
	
	private CharTemplateTable()
	{
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		
		final File f = new File(Config.DATAPACK_ROOT, "/data/xml/player/chartemplate.xml");
		if (!f.exists())
		{
			_log.warn("char_template.xml could not be loaded: file not found");
			return;
		}
		
		try (FileInputStream fis = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8"))
		{
			final InputSource in = new InputSource(isr);
			final Document doc = factory.newDocumentBuilder().parse(in);
			
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("class".equalsIgnoreCase(d.getNodeName()))
						{
							final StatsSet set = new StatsSet();
							final int ID = Integer.parseInt(d.getAttributes().getNamedItem("Id").getNodeValue());
							final String NAME = d.getAttributes().getNamedItem("name").getNodeValue();
							final int race_id = Integer.parseInt(d.getAttributes().getNamedItem("RaceId").getNodeValue());
							L2PcTemplate ct = null;
							
							for (Node t = d.getFirstChild(); t != null; t = t.getNextSibling())
							{
								if ("stats".equalsIgnoreCase(t.getNodeName()))
								{
									set.set("classId", ID);
									set.set("className", NAME);
									set.set("raceId", race_id);
									set.set("baseSTR", Integer.parseInt(t.getAttributes().getNamedItem("str").getNodeValue()));
									set.set("baseCON", Integer.parseInt(t.getAttributes().getNamedItem("con").getNodeValue()));
									set.set("baseDEX", Integer.parseInt(t.getAttributes().getNamedItem("dex").getNodeValue()));
									set.set("baseINT", Integer.parseInt(t.getAttributes().getNamedItem("_int").getNodeValue()));
									set.set("baseWIT", Integer.parseInt(t.getAttributes().getNamedItem("wit").getNodeValue()));
									set.set("baseMEN", Integer.parseInt(t.getAttributes().getNamedItem("men").getNodeValue()));
									set.set("basePAtk", Integer.parseInt(t.getAttributes().getNamedItem("p_atk").getNodeValue()));
									set.set("basePDef", Integer.parseInt(t.getAttributes().getNamedItem("p_def").getNodeValue()));
									set.set("baseMAtk", Integer.parseInt(t.getAttributes().getNamedItem("m_atk").getNodeValue()));
									set.set("baseMDef", Integer.parseInt(t.getAttributes().getNamedItem("m_def").getNodeValue()));
									set.set("basePAtkSpd", Integer.parseInt(t.getAttributes().getNamedItem("p_spd").getNodeValue()));
									set.set("baseMAtkSpd", Integer.parseInt(t.getAttributes().getNamedItem("m_spd").getNodeValue()));
									set.set("baseCritRate", Integer.parseInt(t.getAttributes().getNamedItem("critical").getNodeValue()) / 10);
									set.set("baseRunSpd", Integer.parseInt(t.getAttributes().getNamedItem("move_spd").getNodeValue()));
									set.set("spawnX", Integer.parseInt(t.getAttributes().getNamedItem("x").getNodeValue()));
									set.set("spawnY", Integer.parseInt(t.getAttributes().getNamedItem("y").getNodeValue()));
									set.set("spawnZ", Integer.parseInt(t.getAttributes().getNamedItem("z").getNodeValue()));
									set.set("collision_radius", (int) Double.parseDouble(t.getAttributes().getNamedItem("m_col_r").getNodeValue()));
									set.set("collision_height", (int) Double.parseDouble(t.getAttributes().getNamedItem("m_col_h").getNodeValue()));
									set.set("collision_radius_female", (int) Double.parseDouble(t.getAttributes().getNamedItem("f_col_r").getNodeValue()));
									set.set("collision_height_female", (int) Double.parseDouble(t.getAttributes().getNamedItem("f_col_h").getNodeValue()));
									
									set.set("baseHpReg", 1.5);
									set.set("baseMpReg", 0.9);
									set.set("baseWalkSpd", 0);
									set.set("baseShldDef", 0);
									set.set("baseShldRate", 0);
									set.set("baseAtkRange", 40);
									for (Node h = t.getFirstChild(); h != null; h = h.getNextSibling())
									{
										if ("lvlup".equalsIgnoreCase(h.getNodeName()))
										{
											set.set("baseHpMax", Float.parseFloat(h.getAttributes().getNamedItem("hpbase").getNodeValue()));
											set.set("lvlHpAdd", Float.parseFloat(h.getAttributes().getNamedItem("hpadd").getNodeValue()));
											set.set("lvlHpMod", Float.parseFloat(h.getAttributes().getNamedItem("hpmod").getNodeValue()));
											set.set("baseMpMax", Float.parseFloat(h.getAttributes().getNamedItem("mpbase").getNodeValue()));
											set.set("baseCpMax", Float.parseFloat(h.getAttributes().getNamedItem("cpbase").getNodeValue()));
											set.set("lvlCpAdd", Float.parseFloat(h.getAttributes().getNamedItem("cpadd").getNodeValue()));
											set.set("lvlCpMod", Float.parseFloat(h.getAttributes().getNamedItem("cpmod").getNodeValue()));
											set.set("lvlMpAdd", Float.parseFloat(h.getAttributes().getNamedItem("mpadd").getNodeValue()));
											set.set("lvlMpMod", Float.parseFloat(h.getAttributes().getNamedItem("mpmod").getNodeValue()));
											set.set("classBaseLevel", Integer.parseInt(h.getAttributes().getNamedItem("class_lvl").getNodeValue()));
										}
									}
									ct = new L2PcTemplate(set);
								}
								else if ("items".equalsIgnoreCase(t.getNodeName()) && ct != null)
								{
									for (Node i = t.getFirstChild(); i != null; i = i.getNextSibling())
									{
										if ("item".equalsIgnoreCase(i.getNodeName()))
										{
											int itemId = Integer.parseInt(i.getAttributes().getNamedItem("id").getNodeValue());
											int count = Integer.parseInt(i.getAttributes().getNamedItem("count").getNodeValue());
											
											boolean equipped = false;
											Node equippedNode = i.getAttributes().getNamedItem("equipped");
											if (equippedNode != null)
											{
												equipped = Boolean.parseBoolean(equippedNode.getNodeValue());
											}
											ct.addItem(itemId, count, equipped);
										}
									}
								}
							}
							
							if (ct != null)
							{
								_templates.put(ct.classId.getId(), ct);
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.error("Error while loading char templates: " + e.getMessage(), e);
		}
		
		_log.info("CharTemplateTable: Loaded " + _templates.size() + " character templates.");
	}
	
	public L2PcTemplate getTemplate(ClassId classId)
	{
		return getTemplate(classId.getId());
	}
	
	public L2PcTemplate getTemplate(int classId)
	{
		return _templates.get(classId);
	}
	
	public static final String getClassNameById(int classId)
	{
		if (classId >= 0 && classId < CHAR_CLASSES.length)
		{
			return CHAR_CLASSES[classId];
		}
		return "";
	}
	
	public static final int getClassIdByName(String className)
	{
		for (int i = 0; i < CHAR_CLASSES.length; i++)
		{
			if (CHAR_CLASSES[i].equalsIgnoreCase(className))
			{
				return i;
			}
		}
		return -1;
	}
}