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
package l2jorion.game.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import l2jorion.Config;
import l2jorion.game.datatables.sql.ItemTable;
import l2jorion.game.templates.L2Item;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;

public class L2Manor
{
	private static final Logger LOG = LoggerFactory.getLogger(L2Manor.class);
	
	private static L2Manor _instance;
	
	private static Map<Integer, SeedData> _seeds;
	
	private L2Manor()
	{
		_seeds = new HashMap<>();
		parseData();
	}
	
	public static L2Manor getInstance()
	{
		if (_instance == null)
		{
			_instance = new L2Manor();
		}
		return _instance;
	}
	
	public List<Integer> getAllCrops()
	{
		List<Integer> crops = new ArrayList<>();
		for (SeedData seed : _seeds.values())
		{
			if (!crops.contains(seed.getCrop()) && seed.getCrop() != 0)
			{
				crops.add(seed.getCrop());
			}
		}
		return crops;
	}
	
	public int getSeedBasicPrice(int seedId)
	{
		L2Item seedItem = ItemTable.getInstance().getTemplate(seedId);
		return (seedItem != null) ? seedItem.getReferencePrice() : 0;
	}
	
	public int getSeedBasicPriceByCrop(int cropId)
	{
		for (SeedData seed : _seeds.values())
		{
			if (seed.getCrop() == cropId)
			{
				return getSeedBasicPrice(seed.getId());
			}
		}
		return 0;
	}
	
	public int getCropBasicPrice(int cropId)
	{
		L2Item cropItem = ItemTable.getInstance().getTemplate(cropId);
		return (cropItem != null) ? cropItem.getReferencePrice() : 0;
	}
	
	public int getMatureCrop(int cropId)
	{
		for (SeedData seed : _seeds.values())
		{
			if (seed.getCrop() == cropId)
			{
				return seed.getMature();
			}
		}
		return 0;
	}
	
	public int getSeedBuyPrice(int seedId)
	{
		int buyPrice = getSeedBasicPrice(seedId) / 10;
		return Math.max(buyPrice, 1);
	}
	
	public int getSeedMinLevel(int seedId)
	{
		SeedData seed = _seeds.get(seedId);
		return (seed != null) ? seed.getLevel() - 5 : -1;
	}
	
	public int getSeedMaxLevel(int seedId)
	{
		SeedData seed = _seeds.get(seedId);
		return (seed != null) ? seed.getLevel() + 5 : -1;
	}
	
	public int getSeedLevelByCrop(int cropId)
	{
		for (SeedData seed : _seeds.values())
		{
			if (seed.getCrop() == cropId)
			{
				return seed.getLevel();
			}
		}
		return 0;
	}
	
	public int getSeedLevel(int seedId)
	{
		SeedData seed = _seeds.get(seedId);
		return (seed != null) ? seed.getLevel() : -1;
	}
	
	public boolean isAlternative(int seedId)
	{
		for (SeedData seed : _seeds.values())
		{
			if (seed.getId() == seedId)
			{
				return seed.isAlternative();
			}
		}
		return false;
	}
	
	public int getCropType(int seedId)
	{
		SeedData seed = _seeds.get(seedId);
		return (seed != null) ? seed.getCrop() : -1;
	}
	
	public synchronized int getRewardItem(int cropId, int type)
	{
		for (SeedData seed : _seeds.values())
		{
			if (seed.getCrop() == cropId)
			{
				return seed.getReward(type);
			}
		}
		return -1;
	}
	
	public synchronized int getRewardItemBySeed(int seedId, int type)
	{
		SeedData seed = _seeds.get(seedId);
		return (seed != null) ? seed.getReward(type) : 0;
	}
	
	public List<Integer> getCropsForCastle(int castleId)
	{
		List<Integer> crops = new ArrayList<>();
		for (SeedData seed : _seeds.values())
		{
			if (seed.getManorId() == castleId && !crops.contains(seed.getCrop()))
			{
				crops.add(seed.getCrop());
			}
		}
		return crops;
	}
	
	public List<Integer> getSeedsForCastle(int castleId)
	{
		List<Integer> seedsID = new ArrayList<>();
		for (SeedData seed : _seeds.values())
		{
			if (seed.getManorId() == castleId && !seedsID.contains(seed.getId()))
			{
				seedsID.add(seed.getId());
			}
		}
		return seedsID;
	}
	
	public int getCastleIdForSeed(int seedId)
	{
		SeedData seed = _seeds.get(seedId);
		return (seed != null) ? seed.getManorId() : 0;
	}
	
	public int getSeedSaleLimit(int seedId)
	{
		SeedData seed = _seeds.get(seedId);
		return (seed != null) ? seed.getSeedLimit() : 0;
	}
	
	public int getCropPuchaseLimit(int cropId)
	{
		for (SeedData seed : _seeds.values())
		{
			if (seed.getCrop() == cropId)
			{
				return seed.getCropLimit();
			}
		}
		return 0;
	}
	
	private class SeedData
	{
		private int _id;
		private int _level;
		private int _crop;
		private int _mature;
		private int _type1;
		private int _type2;
		private int _manorId;
		private int _isAlternative;
		private int _limitSeeds;
		private int _limitCrops;
		
		public SeedData(int level, int crop, int mature)
		{
			_level = level;
			_crop = crop;
			_mature = mature;
		}
		
		public void setData(int id, int t1, int t2, int manorId, int isAlt, int lim1, int lim2)
		{
			_id = id;
			_type1 = t1;
			_type2 = t2;
			_manorId = manorId;
			_isAlternative = isAlt;
			_limitSeeds = lim1;
			_limitCrops = lim2;
		}
		
		public int getManorId()
		{
			return _manorId;
		}
		
		public int getId()
		{
			return _id;
		}
		
		public int getCrop()
		{
			return _crop;
		}
		
		public int getMature()
		{
			return _mature;
		}
		
		public int getReward(int type)
		{
			return (type == 1) ? _type1 : _type2;
		}
		
		public int getLevel()
		{
			return _level;
		}
		
		public boolean isAlternative()
		{
			return _isAlternative == 1;
		}
		
		public int getSeedLimit()
		{
			return _limitSeeds * Config.RATE_DROP_MANOR;
		}
		
		public int getCropLimit()
		{
			return _limitCrops * Config.RATE_DROP_MANOR;
		}
	}
	
	private void parseData()
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File f = new File(Config.DATAPACK_ROOT + "/data/xml/world/seeds.xml");
		
		if (!f.exists())
		{
			LOG.warn("seeds.xml could not be loaded: file not found");
			return;
		}
		
		try (FileInputStream fileInputStream = new FileInputStream(f);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8"))
		{
			
			InputSource in = new InputSource(inputStreamReader);
			in.setEncoding("UTF-8");
			Document doc = factory.newDocumentBuilder().parse(in);
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if (n.getNodeName().equalsIgnoreCase("list"))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if (d.getNodeName().equalsIgnoreCase("seed"))
						{
							int seedId = Integer.valueOf(d.getAttributes().getNamedItem("seed_id").getNodeValue());
							int level = Integer.valueOf(d.getAttributes().getNamedItem("seed_level").getNodeValue());
							int cropId = Integer.valueOf(d.getAttributes().getNamedItem("crop_id").getNodeValue());
							int matureId = Integer.valueOf(d.getAttributes().getNamedItem("mature_id").getNodeValue());
							int type1R = Integer.valueOf(d.getAttributes().getNamedItem("reward1_id").getNodeValue());
							int type2R = Integer.valueOf(d.getAttributes().getNamedItem("reward2_id").getNodeValue());
							int manorId = Integer.valueOf(d.getAttributes().getNamedItem("manor_id").getNodeValue());
							int isAlt = Integer.valueOf(d.getAttributes().getNamedItem("is_alternative").getNodeValue());
							int limitSeeds = Integer.valueOf(d.getAttributes().getNamedItem("limit_for_seeds").getNodeValue());
							int limitCrops = Integer.valueOf(d.getAttributes().getNamedItem("limit_for_crops").getNodeValue());
							
							SeedData seed = new SeedData(level, cropId, matureId);
							seed.setData(seedId, type1R, type2R, manorId, isAlt, limitSeeds, limitCrops);
							_seeds.put(seed.getId(), seed);
						}
					}
				}
			}
		}
		catch (SAXException | IOException | ParserConfigurationException e)
		{
			LOG.error("Error while parsing seeds.xml", e);
		}
		
		LOG.info("ManorManager: Loaded " + _seeds.size() + " seeds.");
	}
}
