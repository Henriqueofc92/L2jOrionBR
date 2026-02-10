/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2jorion.game.datatables.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import l2jorion.Config;
import l2jorion.game.model.L2ArmorSet;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;

public class ArmorSetsTable
{
	private static final Logger LOG = LoggerFactory.getLogger(ArmorSetsTable.class.getName());
	
	private static ArmorSetsTable _instance;
	
	public HashMap<Integer, L2ArmorSet> _armorSets;
	private final HashMap<Integer, ArmorDummy> _cusArmorSets;
	
	public static ArmorSetsTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new ArmorSetsTable();
		}
		
		return _instance;
	}
	
	private ArmorSetsTable()
	{
		_armorSets = new HashMap<>();
		_cusArmorSets = new HashMap<>();
		loadData();
	}
	
	private void loadData()
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File f = new File(Config.DATAPACK_ROOT + "/data/xml/player/armorSets.xml");
		if (!f.exists())
		{
			LOG.error("armorsets.xml could not be loaded: file not found");
			return;
		}
		try
		{
			InputSource in = new InputSource(new InputStreamReader(new FileInputStream(f), "UTF-8"));
			in.setEncoding("UTF-8");
			Document doc = factory.newDocumentBuilder().parse(in);
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if (n.getNodeName().equalsIgnoreCase("list"))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if (d.getNodeName().equalsIgnoreCase("armorset"))
						{
							int id = Integer.valueOf(d.getAttributes().getNamedItem("id").getNodeValue());
							int chest = Integer.valueOf(d.getAttributes().getNamedItem("chest").getNodeValue());
							int legs = Integer.valueOf(d.getAttributes().getNamedItem("legs").getNodeValue());
							int head = Integer.valueOf(d.getAttributes().getNamedItem("head").getNodeValue());
							int gloves = Integer.valueOf(d.getAttributes().getNamedItem("gloves").getNodeValue());
							int feet = Integer.valueOf(d.getAttributes().getNamedItem("feet").getNodeValue());
							int skill_id = Integer.valueOf(d.getAttributes().getNamedItem("skill_id").getNodeValue());
							int shield = Integer.valueOf(d.getAttributes().getNamedItem("shield").getNodeValue());
							int shield_skill_id = Integer.valueOf(d.getAttributes().getNamedItem("shield_skill_id").getNodeValue());
							int enchant6skill = Integer.valueOf(d.getAttributes().getNamedItem("enchant6skill").getNodeValue());
							
							_armorSets.put(chest, new L2ArmorSet(chest, legs, head, gloves, feet, skill_id, shield, shield_skill_id, enchant6skill));
							_cusArmorSets.put(id, new ArmorDummy(chest, legs, head, gloves, feet, skill_id, shield));
						}
					}
				}
			}
		}
		catch (SAXException e)
		{
			LOG.error("Error while creating table", e);
		}
		catch (IOException | ParserConfigurationException e)
		{
			LOG.error("Error while creating table", e);
		}
		
		LOG.info("ArmorSetsTable: Loaded " + _armorSets.size() + " armor sets.");
	}
	
	public boolean setExists(int chestId)
	{
		return _armorSets.containsKey(chestId);
	}
	
	public L2ArmorSet getSet(int chestId)
	{
		return _armorSets.get(chestId);
	}
	
	public ArmorDummy getCusArmorSets(int id)
	{
		return _cusArmorSets.get(id);
	}
	
	public class ArmorDummy
	{
		private final int _chest;
		private final int _legs;
		private final int _head;
		private final int _gloves;
		private final int _feet;
		private final int _skill_id;
		private final int _shield;
		
		public ArmorDummy(int chest, int legs, int head, int gloves, int feet, int skill_id, int shield)
		{
			_chest = chest;
			_legs = legs;
			_head = head;
			_gloves = gloves;
			_feet = feet;
			_skill_id = skill_id;
			_shield = shield;
		}
		
		public int getChest()
		{
			return _chest;
		}
		
		public int getLegs()
		{
			return _legs;
		}
		
		public int getHead()
		{
			return _head;
		}
		
		public int getGloves()
		{
			return _gloves;
		}
		
		public int getFeet()
		{
			return _feet;
		}
		
		public int getSkill_id()
		{
			return _skill_id;
		}
		
		public int getShield()
		{
			return _shield;
		}
	}
	
	public void addObj(int v, L2ArmorSet s)
	{
		_armorSets.put(v, s);
	}
}
