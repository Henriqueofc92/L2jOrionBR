package l2jorion.game.data.custom;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;

/**
 * Miss Queen custom script (5000).<br>
 * Distributes newbie and traveler coupons.
 */
public class MissQueen extends Quest
{
	private static final int COUPON_ONE = 7832;
	private static final int COUPON_TWO = 7833;
	
	// NPCs 31760-31766
	private static final int[] NPCS =
	{
		31760,
		31761,
		31762,
		31763,
		31764,
		31765,
		31766
	};
	
	private static final boolean QUEEN_ENABLED = true;
	
	public MissQueen()
	{
		super(-1, "5000_MissQueen", "custom");
		
		var created = new State("Start", this);
		new State("Started", this);
		setInitialState(created);
		
		for (var npcId : NPCS)
		{
			addStartNpc(npcId);
			addFirstTalkId(npcId);
			addTalkId(npcId);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		if (!QUEEN_ENABLED)
		{
			return null;
		}
		
		var st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		var level = player.getLevel();
		var occupationLevel = player.getClassId().level();
		var pkKills = player.getPkKills();
		var cond = st.getInt("cond");
		var cond2 = st.getInt("cond2");
		
		return switch (event)
		{
			case "newbie_give_coupon" -> {
				if (level >= 6 && level <= 25 && pkKills == 0 && occupationLevel == 0)
				{
					if (cond == 0)
					{
						st.set("cond", "1");
						st.giveItems(COUPON_ONE, 1);
						yield "31760-2.htm";
					}
					yield "31760-1.htm";
				}
				yield "31760-3.htm";
			}
			case "traveller_give_coupon" -> {
				if (level >= 6 && level <= 25 && pkKills == 0 && occupationLevel == 1)
				{
					if (cond2 == 0)
					{
						st.set("cond2", "1");
						st.giveItems(COUPON_TWO, 1);
						yield "31760-5.htm";
					}
					yield "31760-4.htm";
				}
				yield "31760-6.htm";
			}
			default -> null;
		};
	}
	
	@Override
	public String onFirstTalk(L2NpcInstance npc, L2PcInstance player)
	{
		var st = player.getQuestState(getName());
		if (st == null)
		{
			newQuestState(player);
		}
		return "31760.htm";
	}


	public static void main(String[] args)
	{
		new MissQueen();
	}
}
