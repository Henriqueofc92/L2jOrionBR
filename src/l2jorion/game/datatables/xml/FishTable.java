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
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import l2jorion.Config;
import l2jorion.game.datatables.sql.SkillTreeTable;
import l2jorion.game.model.FishData;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;

public class FishTable
{
	private static final Logger LOG = LoggerFactory.getLogger(SkillTreeTable.class.getName());
	
	static class Instance
	{
		static final FishTable _instance = new FishTable();
	}
	
	private static List<FishData> _fishsNormal, _fishsEasy, _fishsHard;
	
	public static FishTable getInstance()
	{
		return Instance._instance;
	}
	
	private FishTable()
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		int count = 0;
		File f = new File(Config.DATAPACK_ROOT + "/data/xml/world/fish.xml");
		if (!f.exists())
		{
			LOG.warn("fish.xml could not be loaded: file not found");
			return;
		}
		try
		{
			_fishsEasy = new ArrayList<>();
			_fishsNormal = new ArrayList<>();
			_fishsHard = new ArrayList<>();
			FishData fish;
			
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
							if (d.getNodeName().equalsIgnoreCase("fish"))
							{
								int id = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
								int lvl = Integer.parseInt(d.getAttributes().getNamedItem("level").getNodeValue());
								String name = d.getAttributes().getNamedItem("name").getNodeValue();
								int hp = Integer.parseInt(d.getAttributes().getNamedItem("hp").getNodeValue());
								int hpreg = Integer.parseInt(d.getAttributes().getNamedItem("hpregen").getNodeValue());
								int type = Integer.parseInt(d.getAttributes().getNamedItem("fish_type").getNodeValue());
								int group = Integer.parseInt(d.getAttributes().getNamedItem("fish_group").getNodeValue());
								int fish_guts = Integer.parseInt(d.getAttributes().getNamedItem("fish_guts").getNodeValue());
								int guts_check_time = Integer.parseInt(d.getAttributes().getNamedItem("guts_check_time").getNodeValue());
								int wait_time = Integer.parseInt(d.getAttributes().getNamedItem("wait_time").getNodeValue());
								int combat_time = Integer.parseInt(d.getAttributes().getNamedItem("combat_time").getNodeValue());
								
								fish = new FishData(id, lvl, name, hp, hpreg, type, group, fish_guts, guts_check_time, wait_time, combat_time);
								switch (fish.getGroup())
								{
									case 0:
										_fishsEasy.add(fish);
										break;
									case 1:
										_fishsNormal.add(fish);
										break;
									case 2:
										_fishsHard.add(fish);
								}
							}
							
							count = _fishsEasy.size() + _fishsNormal.size() + _fishsHard.size();
						}
					}
				}
			}
		}
		catch (SAXException | IOException | ParserConfigurationException e)
		{
			LOG.error("Error while creating table", e);
		}
		
		LOG.info("FishTable: Loaded " + _fishsEasy.size() + " easy fishes.");
		LOG.info("FishTable: Loaded " + _fishsNormal.size() + " normal fishes.");
		LOG.info("FishTable: Loaded " + _fishsHard.size() + " hard fishes.");
		LOG.info("FishTable: Loaded " + count + " fishes.");
	}
	
	public List<FishData> getfish(int lvl, int type, int group)
	{
		List<FishData> result = new ArrayList<>();
		List<FishData> _Fishs = null;
		
		switch (group)
		{
			case 0:
				_Fishs = _fishsEasy;
				break;
			case 1:
				_Fishs = _fishsNormal;
				break;
			case 2:
				_Fishs = _fishsHard;
		}
		
		if (_Fishs == null)
		{
			LOG.warn("Fish are not defined!");
			return null;
		}
		
		for (FishData f : _Fishs)
		{
			if (f.getLevel() != lvl)
			{
				continue;
			}
			
			if (f.getType() != type)
			{
				continue;
			}
			
			result.add(f);
		}
		
		if (result.isEmpty())
		{
			LOG.warn("Can't Find Any Fish!? - Lvl: " + lvl + " Type: " + type);
		}
		
		_Fishs = null;
		
		return result;
	}
}
