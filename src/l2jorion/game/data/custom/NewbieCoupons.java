package l2jorion.game.data.custom;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.multisell.L2Multisell;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;

public class NewbieCoupons extends Quest
{
	private static final int COUPON_ONE = 7832;
	private static final int COUPON_TWO = 7833;
	
	// NPCs 30598-30602 + 31076, 31077, 32135
	private static final int[] NPCS =
	{
		30598,
		30599,
		30600,
		30601,
		30602,
		31076,
		31077,
		32135
	};
	
	private static final int WEAPON_MULTISELL = 305986001;
	private static final int ACCESORIES_MULTISELL = 305986002;
	
	private static final boolean NEWBIE_COUPONS_ENABLED = true;
	
	public NewbieCoupons()
	{
		super(-1, "5001_NewbieCoupons", "custom");
		
		var created = new State("Start", this);
		new State("Started", this);
		setInitialState(created);
		
		for (var npcId : NPCS)
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		if (!NEWBIE_COUPONS_ENABLED)
		{
			return null;
		}
		
		var st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		var newbie = player.isNewbie();
		var level = player.getLevel();
		var occupationLevel = player.getClassId().level();
		var pkKills = player.getPkKills();
		
		return switch (event)
		{
			case "newbie_give_weapon_coupon" -> {
				if (level >= 6 && level <= 39 && pkKills == 0 && occupationLevel == 0)
				{
					if (!newbie)
					{
						player.setNewbie(true);
						st.giveItems(COUPON_ONE, 5);
						yield "30598-2.htm";
					}
					yield "30598-1.htm";
				}
				yield "30598-3.htm";
			}
			case "newbie_give_armor_coupon" -> {
				if (level >= 6 && level <= 39 && pkKills == 0 && occupationLevel == 1)
				{
					if (!newbie)
					{
						player.setNewbie(true);
						st.giveItems(COUPON_TWO, 1);
						yield "30598-5.htm";
					}
					yield "30598-4.htm";
				}
				yield "30598-6.htm";
			}
			case "newbie_show_weapon" -> {
				if (level >= 6 && level <= 39 && pkKills == 0 && occupationLevel == 0)
				{
					L2Multisell.getInstance().SeparateAndSend(WEAPON_MULTISELL, player, false, 0.0);
					yield null;
				}
				yield "30598-7.htm";
			}
			case "newbie_show_armor" -> {
				if (level >= 6 && level <= 39 && pkKills == 0 && occupationLevel > 0)
				{
					L2Multisell.getInstance().SeparateAndSend(ACCESORIES_MULTISELL, player, false, 0.0);
					yield null;
				}
				yield "30598-8.htm";
			}
			default -> null;
		};
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		var st = player.getQuestState(getName());
		if (st == null)
		{
			newQuestState(player);
		}
		return "30598.htm";
	}


	public static void main(String[] args)
	{
		new NewbieCoupons();
	}
}
