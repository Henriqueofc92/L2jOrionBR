/*
 * L2jOrion Project - www.l2jorion.com 
 * * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 */
package l2jorion.game.templates;

import java.util.ArrayList;
import java.util.List;

import l2jorion.game.model.base.ClassId;
import l2jorion.game.model.base.Race;

public class L2PcTemplate extends L2CharTemplate
{
	public final Race race;
	public final ClassId classId;
	
	public final int _currentCollisionRadius;
	public final int _currentCollisionHeight;
	public final int _currentCollisionRadiusFemale;
	public final int _currentCollisionHeightFemale;
	
	public final String className;
	
	public final int spawnX;
	public final int spawnY;
	public final int spawnZ;
	
	public final int classBaseLevel;
	public final float lvlHpAdd;
	public final float lvlHpMod;
	public final float lvlCpAdd;
	public final float lvlCpMod;
	public final float lvlMpAdd;
	public final float lvlMpMod;
	
	private final List<PcTemplateItem> _items = new ArrayList<>();
	
	public L2PcTemplate(final StatsSet set)
	{
		super(set);
		
		classId = ClassId.values()[set.getInteger("classId")];
		race = Race.values()[set.getInteger("raceId")];
		className = set.getString("className");
		
		_currentCollisionRadius = set.getInteger("collision_radius");
		_currentCollisionHeight = set.getInteger("collision_height");
		
		_currentCollisionRadiusFemale = set.getInteger("collision_radius_female");
		_currentCollisionHeightFemale = set.getInteger("collision_height_female");
		
		spawnX = set.getInteger("spawnX");
		spawnY = set.getInteger("spawnY");
		spawnZ = set.getInteger("spawnZ");
		
		classBaseLevel = set.getInteger("classBaseLevel");
		lvlHpAdd = set.getFloat("lvlHpAdd");
		lvlHpMod = set.getFloat("lvlHpMod");
		lvlCpAdd = set.getFloat("lvlCpAdd");
		lvlCpMod = set.getFloat("lvlCpMod");
		lvlMpAdd = set.getFloat("lvlMpAdd");
		lvlMpMod = set.getFloat("lvlMpMod");
	}
	
	public final ClassId getClassId()
	{
		return classId;
	}
	
	public void addItem(int itemId, int amount, boolean equipped)
	{
		PcTemplateItem newItem = new PcTemplateItem(itemId, amount, equipped);
		if (newItem.getItem() != null)
		{
			_items.add(newItem);
		}
	}
	
	public List<PcTemplateItem> getItems()
	{
		return _items;
	}
	
	public int getCollisionRadiusFemale()
	{
		return _currentCollisionRadiusFemale;
	}
	
	public int getCollisionHeightFemale()
	{
		return _currentCollisionHeightFemale;
	}
	
	@Override
	public int getCollisionRadius()
	{
		return _currentCollisionRadius;
	}
	
	@Override
	public int getCollisionHeight()
	{
		return _currentCollisionHeight;
	}
	
	public int getBaseFallSafeHeight(final boolean female)
	{
		if (classId.getRace() == Race.darkelf || classId.getRace() == Race.elf)
		{
			return classId.isMage() ? (female ? 330 : 300) : female ? 380 : 350;
		}
		else if (classId.getRace() == Race.dwarf)
		{
			return female ? 200 : 180;
		}
		else if (classId.getRace() == Race.human)
		{
			return classId.isMage() ? (female ? 220 : 200) : female ? 270 : 250;
		}
		else if (classId.getRace() == Race.orc)
		{
			return classId.isMage() ? (female ? 280 : 250) : female ? 220 : 200;
		}
		
		return 400;
	}
	
	public final int getFallHeight()
	{
		return 333;
	}
}