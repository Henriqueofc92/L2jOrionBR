/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 */
package l2jorion.game.handler.skill;

import l2jorion.Config;
import l2jorion.game.ai.CtrlIntention;
import l2jorion.game.geo.GeoData;
import l2jorion.game.handler.ISkillHandler;
import l2jorion.game.model.L2Character;
import l2jorion.game.model.L2Object;
import l2jorion.game.model.L2Skill;
import l2jorion.game.model.L2Skill.SkillType;
import l2jorion.game.model.Location;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.network.SystemMessageId;
import l2jorion.game.network.serverpackets.FlyToLocation;
import l2jorion.game.network.serverpackets.FlyToLocation.FlyType;
import l2jorion.game.network.serverpackets.SystemMessage;
import l2jorion.game.network.serverpackets.ValidateLocation;
import l2jorion.game.skills.Formulas;
import l2jorion.game.util.Util;

public class InstantJump implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.INSTANT_JUMP
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (targets.length == 0 || !(targets[0] instanceof L2Character))
		{
			return;
		}
		
		L2Character target = (L2Character) targets[0];
		if (Formulas.calcPhysicalSkillEvasion(target, skill))
		{
			if (activeChar instanceof L2PcInstance)
			{
				((L2PcInstance) activeChar).sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DODGES_ATTACK).addCharName(target));
			}
			return;
		}
		int px = target.getX();
		int py = target.getY();
		double ph = Util.convertHeadingToDegree(target.getHeading());
		
		ph += 180;
		if (ph > 360)
		{
			ph -= 360;
		}
		
		ph = (Math.PI * ph) / 180;
		
		double distance = target.getTemplate().getCollisionRadius() + 5;
		
		int x = (int) (px + (distance * Math.cos(ph)));
		int y = (int) (py + (distance * Math.sin(ph)));
		int z = target.getZ();
		if (Config.GEODATA)
		{
			boolean canGo = GeoData.getInstance().canMove(activeChar.getX(), activeChar.getY(), activeChar.getZ(), x, y, z, activeChar.getInstanceId());
			if (!canGo)
			{
				return;
			}
		}
		Location loc = new Location(x, y, z);
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		activeChar.broadcastPacket(new FlyToLocation(activeChar, loc.getX(), loc.getY(), loc.getZ(), FlyType.DUMMY));
		activeChar.abortAttack();
		activeChar.abortCast();
		activeChar.setXYZ(loc.getX(), loc.getY(), loc.getZ());
		activeChar.broadcastPacket(new ValidateLocation(activeChar));
		if (skill.hasEffects())
		{
			target.stopSkillEffects(skill.getId());
			skill.getEffects(activeChar, target);
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}