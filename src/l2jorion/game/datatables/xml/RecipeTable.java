package l2jorion.game.datatables.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import l2jorion.Config;
import l2jorion.game.controllers.RecipeController;
import l2jorion.game.model.L2RecipeList;
import l2jorion.game.model.actor.instance.L2RecipeInstance;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;

public class RecipeTable extends RecipeController
{
	private static final Logger LOG = LoggerFactory.getLogger(RecipeTable.class);
	
	private Map<Integer, L2RecipeList> _lists;
	
	private static RecipeTable instance;
	
	public static RecipeTable getInstance()
	{
		if (instance == null)
		{
			instance = new RecipeTable();
		}
		
		return instance;
	}
	
	private RecipeTable()
	{
		_lists = new HashMap<>();
		
		try
		{
			this.ParseXML();
			LOG.info("RecipeTable: Loaded " + _lists.size() + " recipes.");
		}
		catch (Exception e)
		{
			LOG.error("RecipeTable: Failed loading recipe list", e);
		}
	}
	
	private void ParseXML() throws SAXException, IOException, ParserConfigurationException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File file = new File(Config.DATAPACK_ROOT + "/data/xml/world/recipes.xml");
		if (file.exists())
		{
			try (InputStream inputStream = new FileInputStream(file))
			{
				Document doc = factory.newDocumentBuilder().parse(inputStream);
				List<L2RecipeInstance> recipePartList = new ArrayList<>();
				
				for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
				{
					if ("list".equalsIgnoreCase(n.getNodeName()))
					{
						String recipeName;
						int id;
						for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							if ("item".equalsIgnoreCase(d.getNodeName()))
							{
								recipePartList.clear();
								NamedNodeMap attrs = d.getAttributes();
								Node att;
								att = attrs.getNamedItem("id");
								if (att == null)
								{
									LOG.error("RecipeTable: Missing id for recipe item, skipping");
									continue;
								}
								id = Integer.parseInt(att.getNodeValue());
								
								att = attrs.getNamedItem("name");
								if (att == null)
								{
									LOG.error("RecipeTable: Missing name for recipe item id: " + id + ", skipping");
									continue;
								}
								recipeName = att.getNodeValue();
								
								int recipeId = -1;
								int level = -1;
								boolean isDwarvenRecipe = true;
								int mpCost = -1;
								int successRate = -1;
								int prodId = -1;
								int count = -1;
								for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
								{
									if ("recipe".equalsIgnoreCase(c.getNodeName()))
									{
										NamedNodeMap atts = c.getAttributes();
										
										recipeId = Integer.parseInt(atts.getNamedItem("id").getNodeValue());
										level = Integer.parseInt(atts.getNamedItem("level").getNodeValue());
										isDwarvenRecipe = atts.getNamedItem("type").getNodeValue().equalsIgnoreCase("dwarven");
									}
									else if ("mpCost".equalsIgnoreCase(c.getNodeName()))
									{
										mpCost = Integer.parseInt(c.getTextContent());
									}
									else if ("successRate".equalsIgnoreCase(c.getNodeName()))
									{
										successRate = Integer.parseInt(c.getTextContent());
									}
									else if ("ingredient".equalsIgnoreCase(c.getNodeName()))
									{
										int ingId = Integer.parseInt(c.getAttributes().getNamedItem("id").getNodeValue());
										int ingCount = Integer.parseInt(c.getAttributes().getNamedItem("count").getNodeValue());
										recipePartList.add(new L2RecipeInstance(ingId, ingCount));
									}
									else if ("production".equalsIgnoreCase(c.getNodeName()))
									{
										prodId = Integer.parseInt(c.getAttributes().getNamedItem("id").getNodeValue());
										count = Integer.parseInt(c.getAttributes().getNamedItem("count").getNodeValue());
									}
								}
								L2RecipeList recipeList = new L2RecipeList(id, level, recipeId, recipeName, successRate, mpCost, prodId, count, isDwarvenRecipe);
								for (L2RecipeInstance recipePart : recipePartList)
								{
									recipeList.addRecipe(recipePart);
								}
								
								_lists.put(_lists.size(), recipeList);
							}
						}
					}
				}
			}
		}
		else
		{
			LOG.error("RecipeTable: recipes.xml is missing in data folder.");
		}
	}
	
	public int getRecipesCount()
	{
		return _lists.size();
	}
	
	public L2RecipeList getRecipeList(int listId)
	{
		return _lists.get(listId);
	}
	
	public L2RecipeList getRecipeByItemId(int itemId)
	{
		for (L2RecipeList recipeList : _lists.values())
		{
			if (recipeList.getRecipeId() == itemId)
			{
				return recipeList;
			}
		}
		return null;
	}
	
	public L2RecipeList getRecipeById(int recId)
	{
		for (L2RecipeList recipeList : _lists.values())
		{
			if (recipeList.getId() == recId)
			{
				return recipeList;
			}
		}
		return null;
	}
}