/*
 * L2jOrion Project - www.l2jorion.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package l2jorion.game.skills.effects;

import l2jorion.game.ai.CtrlIntention;
import l2jorion.game.model.L2Effect;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.network.SystemMessageId;
import l2jorion.game.network.serverpackets.SystemMessage;
import l2jorion.game.skills.Env;

/**
 * Relax effect — forces the player to sit and regenerate HP while draining MP.
 * <ul>
 *   <li>On start: sits the player down (or sets REST intention for NPCs).</li>
 *   <li>On tick: checks if still sitting, HP is not full, and MP is sufficient.</li>
 *   <li>On exit: clears the relax flag.</li>
 * </ul>
 * Improvements from reference (l2jmega):
 * <ul>
 *   <li>Proper HP full system message (SKILL_DEACTIVATED_HP_FULL).</li>
 *   <li>Proper MP exhaustion system message (SKILL_REMOVED_DUE_LACK_MP).</li>
 *   <li>Standing up cancels the effect immediately.</li>
 * </ul>
 */
class EffectRelax extends L2Effect
{
	public EffectRelax(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.RELAXING;
	}
	
	@Override
	public void onStart()
	{
		if (getEffected() instanceof L2PcInstance)
		{
			final L2PcInstance player = (L2PcInstance) getEffected();
			player.setRelax(true);
			player.sitDown();
		}
		else
		{
			getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
		}
		super.onStart();
	}
	
	@Override
	public void onExit()
	{
		if (getEffected() instanceof L2PcInstance)
		{
			((L2PcInstance) getEffected()).setRelax(false);
		}
		super.onExit();
	}
	
	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead())
		{
			return false;
		}
		
		// If player stood up, cancel the effect
		if (getEffected() instanceof L2PcInstance)
		{
			if (!((L2PcInstance) getEffected()).isSitting())
			{
				return false;
			}
		}
		
		// If HP is full and this is a toggle, deactivate
		if (getEffected().getCurrentHp() + 1 > getEffected().getMaxHp())
		{
			if (getSkill().isToggle())
			{
				getEffected().sendPacket(new SystemMessage(SystemMessageId.SKILL_DEACTIVATED_HP_FULL));
				return false;
			}
		}
		
		// Drain MP
		final double manaDam = calc();
		
		if (manaDam > getEffected().getCurrentMp())
		{
			if (getSkill().isToggle())
			{
				getEffected().sendPacket(new SystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
				return false;
			}
		}
		
		getEffected().reduceCurrentMp(manaDam);
		return true;
	}
}