package l2jorion.game.datatables.xml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2jorion.Config;
import l2jorion.game.datatables.SkillTable;
import l2jorion.game.model.L2Augmentation;
import l2jorion.game.model.L2Skill;
import l2jorion.game.model.actor.instance.L2ItemInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.network.serverpackets.ExShowScreenMessage;
import l2jorion.game.network.serverpackets.MagicSkillUser;
import l2jorion.game.skills.Stats;
import l2jorion.game.skills.holders.IntIntHolder;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;
import l2jorion.util.random.Rnd;

public class AugmentationData
{
	private static final Logger LOG = LoggerFactory.getLogger(AugmentationData.class);
	private static AugmentationData _instance;
	
	private static final int STAT_START = 1;
	private static final int STAT_END = 14560;
	private static final int STAT_BLOCKSIZE = 3640;
	private static final int STAT_SUBBLOCKSIZE = 91;
	private static final int BLUE_START = 14561;
	private static final int SKILLS_BLOCKSIZE = 178;
	
	private static final int BASESTAT_STR = 16341;
	private static final int BASESTAT_CON = 16342;
	private static final int BASESTAT_INT = 16343;
	private static final int BASESTAT_MEN = 16344;
	
	private final List<AugmentationStat>[] _augmentationStats;
	private final Map<Integer, List<AugmentationSkill>> _blueSkills = new ConcurrentHashMap<>();
	private final Map<Integer, List<AugmentationSkill>> _purpleSkills = new ConcurrentHashMap<>();
	private final Map<Integer, List<AugmentationSkill>> _redSkills = new ConcurrentHashMap<>();
	private final Map<Integer, IntIntHolder> _allSkills = new ConcurrentHashMap<>();
	
	public static AugmentationData getInstance()
	{
		if (_instance == null)
		{
			_instance = new AugmentationData();
		}
		return _instance;
	}
	
	@SuppressWarnings("unchecked")
	private AugmentationData()
	{
		_augmentationStats = new ArrayList[10];
		for (int i = 0; i < _augmentationStats.length; i++)
		{
			_augmentationStats[i] = new ArrayList<>();
		}
		
		for (int i = 1; i <= 10; i++)
		{
			_blueSkills.put(i, new ArrayList<>());
			_purpleSkills.put(i, new ArrayList<>());
			_redSkills.put(i, new ArrayList<>());
		}
		
		load();
		
		LOG.info("AugmentationData: Loaded total augmentation stats.");
	}
	
	// --- CLASSES INTERNAS ---
	public class AugmentationSkill
	{
		private final int _skillId;
		private final int _maxSkillLevel;
		private final int _augmentationSkillId;
		
		public AugmentationSkill(final int skillId, final int maxSkillLevel, final int augmentationSkillId)
		{
			_skillId = skillId;
			_maxSkillLevel = maxSkillLevel;
			_augmentationSkillId = augmentationSkillId;
		}
		
		public L2Skill getSkill(final int level)
		{
			return (level > _maxSkillLevel) ? SkillTable.getInstance().getInfo(_skillId, _maxSkillLevel) : SkillTable.getInstance().getInfo(_skillId, level);
		}
		
		public int getAugmentationSkillId()
		{
			return _augmentationSkillId;
		}
	}
	
	public class AugmentationStat
	{
		private final Stats _stat;
		private final float[] _singleValues;
		private final float[] _combinedValues;
		
		public AugmentationStat(final Stats stat, final float[] sValues, final float[] cValues)
		{
			_stat = stat;
			_singleValues = sValues;
			_combinedValues = cValues;
		}
		
		public float getSingleStatValue(final int i)
		{
			if (i >= _singleValues.length || i < 0)
			{
				return _singleValues[_singleValues.length - 1];
			}
			return _singleValues[i];
		}
		
		public float getCombinedStatValue(final int i)
		{
			if (i >= _combinedValues.length || i < 0)
			{
				return _combinedValues[_combinedValues.length - 1];
			}
			return _combinedValues[i];
		}
		
		public Stats getStat()
		{
			return _stat;
		}
	}
	
	public class AugStat
	{
		private final Stats _stat;
		private final float _value;
		
		public AugStat(final Stats stat, final float value)
		{
			_stat = stat;
			_value = value;
		}
		
		public Stats getStat()
		{
			return _stat;
		}
		
		public float getValue()
		{
			return _value;
		}
	}
	
	private void load()
	{
		Path folderPath = Path.of(Config.DATAPACK_ROOT.getAbsolutePath(), "data", "xml", "stats", "augmentation");
		
		if (!Files.exists(folderPath))
		{
			LOG.warn("AugmentationData: Folder not found at " + folderPath);
			return;
		}
		
		try (Stream<Path> stream = Files.list(folderPath))
		{
			stream.filter(Files::isRegularFile).forEach(path ->
			{
				String fileName = path.getFileName().toString();
				if (fileName.equalsIgnoreCase("augmentation_skillmap.xml"))
				{
					loadSkills(path.toFile());
				}
				else if (fileName.startsWith("augmentation_stats") && fileName.endsWith(".xml"))
				{
					loadStats(path.toFile(), fileName);
				}
			});
		}
		catch (IOException e)
		{
			LOG.error("AugmentationData: Error walking through directory.", e);
		}
	}
	
	private void loadSkills(File file)
	{
		try
		{
			var factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			
			Document doc = factory.newDocumentBuilder().parse(file);
			int badAugmentData = 0;
			
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("augmentation".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							int augmentationId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
							int skillId = 0;
							int skillLvL = 0;
							String type = "blue";
							
							for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
							{
								var nodeName = cd.getNodeName();
								if ("skillId".equalsIgnoreCase(nodeName))
								{
									skillId = Integer.parseInt(cd.getAttributes().getNamedItem("val").getNodeValue());
								}
								else if ("skillLevel".equalsIgnoreCase(nodeName))
								{
									skillLvL = Integer.parseInt(cd.getAttributes().getNamedItem("val").getNodeValue());
								}
								else if ("type".equalsIgnoreCase(nodeName))
								{
									type = cd.getAttributes().getNamedItem("val").getNodeValue();
								}
							}
							
							if (skillId == 0 || skillLvL == 0)
							{
								LOG.warn("AugmentationData: Bad skill data for augmentationId: " + augmentationId);
								badAugmentData++;
								continue;
							}
							
							int k = 1;
							while (augmentationId - k * SKILLS_BLOCKSIZE >= BLUE_START)
							{
								k++;
							}
							
							var skillData = new AugmentationSkill(skillId, skillLvL, augmentationId);
							
							if ("blue".equalsIgnoreCase(type))
							{
								_blueSkills.get(k).add(skillData);
							}
							else if ("purple".equalsIgnoreCase(type))
							{
								_purpleSkills.get(k).add(skillData);
							}
							else
							{
								_redSkills.get(k).add(skillData);
							}
							
							_allSkills.put(augmentationId, new IntIntHolder(skillId, skillLvL));
						}
					}
				}
			}
			if (badAugmentData > 0)
			{
				LOG.info("AugmentationData: " + badAugmentData + " bad skills skipped.");
			}
			LOG.info("AugmentationData: Skills loaded successfully.");
		}
		catch (Exception e)
		{
			LOG.error("AugmentationData: Error parsing skillmap.", e);
		}
	}
	
	private void loadStats(File file, String fileName)
	{
		int index = 0;
		Pattern pattern = Pattern.compile("stats(\\d+)\\.xml");
		Matcher matcher = pattern.matcher(fileName);
		if (matcher.find())
		{
			index = Integer.parseInt(matcher.group(1));
		}
		
		if (index <= 0 || index > _augmentationStats.length)
		{
			LOG.warn("AugmentationData: File " + fileName + " skipped. Invalid index or out of bounds.");
			return;
		}
		
		try
		{
			var factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			Document doc = factory.newDocumentBuilder().parse(file);
			
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("stat".equalsIgnoreCase(d.getNodeName()))
						{
							String statName = d.getAttributes().getNamedItem("name").getNodeValue();
							float[] soloValues = null;
							float[] combinedValues = null;
							
							for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
							{
								if ("table".equalsIgnoreCase(cd.getNodeName()))
								{
									String tableName = cd.getAttributes().getNamedItem("name").getNodeValue();
									var st = new StringTokenizer(cd.getFirstChild().getNodeValue());
									var valuesList = new ArrayList<Float>();
									
									while (st.hasMoreTokens())
									{
										valuesList.add(Float.parseFloat(st.nextToken()));
									}
									
									float[] floatArray = new float[valuesList.size()];
									for (int x = 0; x < valuesList.size(); x++)
									{
										floatArray[x] = valuesList.get(x);
									}
									
									if ("#soloValues".equalsIgnoreCase(tableName))
									{
										soloValues = floatArray;
									}
									else
									{
										combinedValues = floatArray;
									}
								}
							}
							_augmentationStats[index - 1].add(new AugmentationStat(Stats.valueOfXml(statName), soloValues, combinedValues));
						}
					}
				}
			}
			LOG.info("AugmentationData: Loaded stats from " + fileName);
		}
		catch (Exception e)
		{
			LOG.error("AugmentationData: Error parsing " + fileName, e);
		}
	}
	
	public static void reload()
	{
		_instance = null;
		getInstance();
	}
	
	// --- MÉTODOS DE GERAÇÃO (LÓGICA RETAIL + MODS) ---
	
	// Método restaurado para o Market (PowerPack)
	public L2Augmentation generateAugmentationForMarket(final L2ItemInstance item, int effectId, L2Skill skill)
	{
		return new L2Augmentation(item, effectId, skill, true);
	}
	
	public L2Augmentation generateAugmentationWithSkill(L2ItemInstance item, int id, int level)
	{
		int stat34 = 0;
		L2Skill skill = null;
		
		for (var entry : _allSkills.entrySet())
		{
			IntIntHolder holder = entry.getValue();
			if (holder.getId() == id && holder.getValue() == level)
			{
				stat34 = entry.getKey();
				skill = SkillTable.getInstance().getInfo(holder.getId(), holder.getValue());
				break;
			}
		}
		
		if (skill == null)
		{
			return null;
		}
		
		int lifeStoneLevel = 9;
		int lifeStoneGrade = 3;
		int resultColor = 3;
		
		int offset = (lifeStoneLevel * STAT_SUBBLOCKSIZE) + Rnd.get(0, 1) * STAT_BLOCKSIZE + (lifeStoneGrade + resultColor) / 2 * (10 * STAT_SUBBLOCKSIZE) + 1;
		int stat12 = Rnd.get(offset, offset + STAT_SUBBLOCKSIZE - 1);
		
		return new L2Augmentation(item, ((stat34 << 16) + stat12), skill, true);
	}
	
	// Método overload restaurado para o RequestRefine
	public L2Augmentation generateRandomAugmentation(final L2ItemInstance item, final int lifeStoneLevel, final int lifeStoneGrade)
	{
		return generateRandomAugmentation(item, lifeStoneLevel, lifeStoneGrade, null, false);
	}
	
	public L2Augmentation generateRandomAugmentation(final L2ItemInstance item, final int lifeStoneLevel, final int lifeStoneGrade, L2PcInstance player, boolean effect)
	{
		int skillChance = switch (lifeStoneGrade)
		{
			case 0 -> Config.AUGMENTATION_NG_SKILL_CHANCE;
			case 1 -> Config.AUGMENTATION_MID_SKILL_CHANCE;
			case 2 -> Config.AUGMENTATION_HIGH_SKILL_CHANCE;
			case 3 -> Config.AUGMENTATION_TOP_SKILL_CHANCE;
			default -> 0;
		};
		
		boolean generateGlow = switch (lifeStoneGrade)
		{
			case 0 -> Rnd.get(1, 100) <= Config.AUGMENTATION_NG_GLOW_CHANCE;
			case 1 -> Rnd.get(1, 100) <= Config.AUGMENTATION_MID_GLOW_CHANCE;
			case 2 -> Rnd.get(1, 100) <= Config.AUGMENTATION_HIGH_GLOW_CHANCE;
			case 3 -> Rnd.get(1, 100) <= Config.AUGMENTATION_TOP_GLOW_CHANCE;
			default -> false;
		};
		
		boolean generateSkill = Rnd.get(1, 100) <= skillChance;
		int stat34 = 0;
		
		if (!generateSkill && Rnd.get(1, 100) <= Config.AUGMENTATION_BASESTAT_CHANCE)
		{
			stat34 = Rnd.get(BASESTAT_STR, BASESTAT_MEN);
		}
		
		int resultColor;
		if (stat34 == 0 && !generateSkill)
		{
			int rnd = Rnd.get(0, 100);
			resultColor = (rnd <= 15 * lifeStoneGrade + 40) ? 1 : 0;
		}
		else
		{
			int rnd = Rnd.get(0, 100);
			if (rnd <= 10 * lifeStoneGrade + 5 || stat34 != 0)
			{
				resultColor = 3;
			}
			else if (rnd <= 10 * lifeStoneGrade + 10)
			{
				resultColor = 1;
			}
			else
			{
				resultColor = 2;
			}
		}
		
		int stat12;
		if (stat34 == 0 && !generateSkill)
		{
			int temp = Rnd.get(2, 3);
			int colorOffset = resultColor * 10 * STAT_SUBBLOCKSIZE + temp * STAT_BLOCKSIZE + 1;
			int offset = (lifeStoneLevel - 1) * STAT_SUBBLOCKSIZE + colorOffset;
			stat34 = Rnd.get(offset, offset + STAT_SUBBLOCKSIZE - 1);
			
			if (generateGlow && lifeStoneGrade >= 2)
			{
				offset = (lifeStoneLevel - 1) * STAT_SUBBLOCKSIZE + (temp - 2) * STAT_BLOCKSIZE + lifeStoneGrade * 10 * STAT_SUBBLOCKSIZE + 1;
			}
			else
			{
				offset = (lifeStoneLevel - 1) * STAT_SUBBLOCKSIZE + (temp - 2) * STAT_BLOCKSIZE + Rnd.get(0, 1) * 10 * STAT_SUBBLOCKSIZE + 1;
			}
			
			stat12 = Rnd.get(offset, offset + STAT_SUBBLOCKSIZE - 1);
		}
		else
		{
			int offset;
			if (!generateGlow)
			{
				offset = (lifeStoneLevel - 1) * STAT_SUBBLOCKSIZE + Rnd.get(0, 1) * STAT_BLOCKSIZE + 1;
			}
			else
			{
				offset = (lifeStoneLevel - 1) * STAT_SUBBLOCKSIZE + Rnd.get(0, 1) * STAT_BLOCKSIZE + (lifeStoneGrade + resultColor) / 2 * 10 * STAT_SUBBLOCKSIZE + 1;
			}
			
			stat12 = Rnd.get(offset, offset + STAT_SUBBLOCKSIZE - 1);
		}
		
		L2Skill skill = null;
		if (generateSkill)
		{
			AugmentationSkill temp = null;
			var list = switch (resultColor)
			{
				case 1 -> _blueSkills.get(lifeStoneLevel);
				case 2 -> _purpleSkills.get(lifeStoneLevel);
				case 3 -> _redSkills.get(lifeStoneLevel);
				default -> null;
			};
			
			if (list != null && !list.isEmpty())
			{
				temp = list.get(Rnd.get(0, list.size() - 1));
				skill = temp.getSkill(lifeStoneLevel);
				stat34 = temp.getAugmentationSkillId();
			}
			
			if (effect && player != null && skill != null)
			{
				player.sendPacket(new MagicSkillUser(player, player, 2024, 1, 1, 0));
				player.useMagic(SkillTable.getInstance().getInfo(2024, 1), false, false);
				
				String type = (skill.isActive() || skill.isChance()) ? "(Active)" : "(Passive)";
				player.sendPacket(new ExShowScreenMessage("You've got a skill: " + skill.getName() + " " + type, 3000, 2, false));
				player.sendMessage("You've got a skill: " + skill.getName() + " " + type);
			}
		}
		
		return new L2Augmentation(item, ((stat34 << 16) + stat12), skill, true);
	}
	
	public ArrayList<AugStat> getAugStatsById(final int augmentationId)
	{
		final ArrayList<AugStat> temp = new ArrayList<>();
		final int[] stats =
		{
			0x0000FFFF & augmentationId,
			augmentationId >> 16
		};
		
		for (int i = 0; i < 2; i++)
		{
			if (stats[i] >= STAT_START && stats[i] <= STAT_END)
			{
				int block = 0;
				while (stats[i] > STAT_BLOCKSIZE)
				{
					stats[i] -= STAT_BLOCKSIZE;
					block++;
				}
				
				int subblock = 0;
				while (stats[i] > STAT_SUBBLOCKSIZE)
				{
					stats[i] -= STAT_SUBBLOCKSIZE;
					subblock++;
				}
				
				if (block >= _augmentationStats.length)
				{
					continue;
				}
				
				if (stats[i] < 14)
				{
					final AugmentationStat as = _augmentationStats[block].get((stats[i] - 1));
					temp.add(new AugStat(as.getStat(), as.getSingleStatValue(subblock)));
				}
				else
				{
					stats[i] -= 13;
					int x = 12;
					int rescales = 0;
					
					while (stats[i] > x)
					{
						stats[i] -= x;
						x--;
						rescales++;
					}
					
					AugmentationStat as = _augmentationStats[block].get(rescales);
					if (rescales == 0)
					{
						temp.add(new AugStat(as.getStat(), as.getCombinedStatValue(subblock)));
					}
					else
					{
						temp.add(new AugStat(as.getStat(), as.getCombinedStatValue(subblock * 2 + 1)));
					}
					
					as = _augmentationStats[block].get(rescales + stats[i]);
					if (as.getStat() == Stats.CRITICAL_DAMAGE)
					{
						temp.add(new AugStat(as.getStat(), as.getCombinedStatValue(subblock)));
					}
					else
					{
						temp.add(new AugStat(as.getStat(), as.getCombinedStatValue(subblock * 2)));
					}
				}
			}
			else if (stats[i] >= BASESTAT_STR && stats[i] <= BASESTAT_MEN)
			{
				switch (stats[i])
				{
					case BASESTAT_STR -> temp.add(new AugStat(Stats.STAT_STR, 1.0f));
					case BASESTAT_CON -> temp.add(new AugStat(Stats.STAT_CON, 1.0f));
					case BASESTAT_INT -> temp.add(new AugStat(Stats.STAT_INT, 1.0f));
					case BASESTAT_MEN -> temp.add(new AugStat(Stats.STAT_MEN, 1.0f));
				}
			}
		}
		return temp;
	}
}