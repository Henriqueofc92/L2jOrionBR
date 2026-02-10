package l2jorion.game.datatables.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import l2jorion.Config;
import l2jorion.game.templates.L2HelperBuff;
import l2jorion.game.templates.StatsSet;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;

public class HelperBuffTable
{
	private static final Logger _log = LoggerFactory.getLogger(HelperBuffTable.class.getName());
	
	private List<L2HelperBuff> _helperBuff;
	
	private int _magicClassLowestLevel = 100;
	private int _physicClassLowestLevel = 100;
	
	private int _magicClassHighestLevel = 1;
	private int _physicClassHighestLevel = 1;
	
	public static HelperBuffTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private HelperBuffTable()
	{
		_helperBuff = new ArrayList<>();
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File f = new File(Config.DATAPACK_ROOT + "/data/xml/player/newbieBuffs.xml");
		if (!f.exists())
		{
			_log.warn("HelperBuffTable: newbieBuffs.xml could not be loaded: file not found");
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
						if (d.getNodeName().equalsIgnoreCase("buff"))
						{
							Node idAttribute = d.getAttributes().getNamedItem("id");
							Node skillIdAttribute = d.getAttributes().getNamedItem("skill_id");
							Node skillLevelAttribute = d.getAttributes().getNamedItem("skill_level");
							Node lowerLevelAttribute = d.getAttributes().getNamedItem("lower_level");
							Node upperLevelAttribute = d.getAttributes().getNamedItem("upper_level");
							Node isMagicClassAttribute = d.getAttributes().getNamedItem("is_magic_class");
							
							if (idAttribute != null && skillIdAttribute != null && skillLevelAttribute != null && lowerLevelAttribute != null && upperLevelAttribute != null && isMagicClassAttribute != null)
							{
								
								int id = Integer.valueOf(idAttribute.getNodeValue());
								int skill_id = Integer.valueOf(skillIdAttribute.getNodeValue());
								int skill_level = Integer.valueOf(skillLevelAttribute.getNodeValue());
								int lower_level = Integer.valueOf(lowerLevelAttribute.getNodeValue());
								int upper_level = Integer.valueOf(upperLevelAttribute.getNodeValue());
								boolean is_magic_class = Boolean.parseBoolean(isMagicClassAttribute.getNodeValue());
								
								StatsSet helperBuffDat = new StatsSet();
								
								helperBuffDat.set("id", id);
								helperBuffDat.set("skillID", skill_id);
								helperBuffDat.set("skillLevel", skill_level);
								helperBuffDat.set("lowerLevel", lower_level);
								helperBuffDat.set("upperLevel", upper_level);
								helperBuffDat.set("isMagicClass", is_magic_class);
								
								if (!is_magic_class)
								{
									if (lower_level < _physicClassLowestLevel)
									{
										_physicClassLowestLevel = lower_level;
									}
									
									if (upper_level > _physicClassHighestLevel)
									{
										_physicClassHighestLevel = upper_level;
									}
								}
								else
								{
									if (lower_level < _magicClassLowestLevel)
									{
										_magicClassLowestLevel = lower_level;
									}
									
									if (upper_level > _magicClassHighestLevel)
									{
										_magicClassHighestLevel = upper_level;
									}
								}
								
								// Add this Helper Buff to the Helper Buff List
								L2HelperBuff template = new L2HelperBuff(helperBuffDat);
								_helperBuff.add(template);
							}
							else
							{
								_log.error("Incomplete attributes in 'buff' node. Skipping.");
							}
						}
					}
				}
			}
			
		}
		catch (Exception e)
		{
			_log.error("Error while creating table", e);
		}
		
		_log.info("HelperBuffTable: Loaded " + _helperBuff.size() + " buffs.");
	}
	
	public List<L2HelperBuff> getHelperBuffTable()
	{
		return _helperBuff;
	}
	
	public int getMagicClassHighestLevel()
	{
		return _magicClassHighestLevel;
	}
	
	public int getMagicClassLowestLevel()
	{
		return _magicClassLowestLevel;
	}
	
	public int getPhysicClassHighestLevel()
	{
		return _physicClassHighestLevel;
	}
	
	public int getPhysicClassLowestLevel()
	{
		return _physicClassLowestLevel;
	}
	
	private static class SingletonHolder
	{
		protected static final HelperBuffTable _instance = new HelperBuffTable();
	}
}
