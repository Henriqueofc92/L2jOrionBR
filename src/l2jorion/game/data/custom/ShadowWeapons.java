package l2jorion.game.data.custom;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;

/**
 * Shadow Weapons coupon exchange (4000).<br>
 * Exchange D-grade / C-grade coupons for shadow weapons via multisell.
 */
public class ShadowWeapons extends Quest
{
	private static final int D_COUPON = 8869;
	private static final int C_COUPON = 8870;
	
	private static final int[] NPC =
	{
		30026, 30037, 30066, 30070, 30109, 30115, 30120, 30174, 30175, 30176,
		30187, 30191, 30195, 30288, 30289, 30290, 30297, 30373, 30462, 30474,
		30498, 30499, 30500, 30503, 30504, 30505, 30511, 30512, 30513, 30676,
		30677, 30681, 30685, 30687, 30689, 30694, 30699, 30704, 30845, 30847,
		30849, 30854, 30857, 30862, 30865, 30894, 30897, 30900, 30905, 30910,
		30913, 31269, 31272, 31288, 31314, 31317, 31321, 31324, 31326, 31328,
		31331, 31334, 31336, 31965, 31974, 31276, 31285, 31996, 32094, 32096,
		32098
	};
	
	public ShadowWeapons()
	{
		super(4000, "4000_ShadowWeapons", "Custom");
		
		new State("Start", this);
		
		for (var npcId : NPC)
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		var st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		var hasD = st.getQuestItemsCount(D_COUPON);
		var hasC = st.getQuestItemsCount(C_COUPON);
		
		String htmltext;
		if (hasD > 0 || hasC > 0)
		{
			int multisell;
			if (hasD == 0)
			{
				multisell = 306893002; // C-grade only
			}
			else if (hasC == 0)
			{
				multisell = 306893001; // D-grade only
			}
			else
			{
				multisell = 306893003; // Both
			}
			
			htmltext = st.showHtmlFile("exchange.htm").replace("%msid%", String.valueOf(multisell));
		}
		else
		{
			htmltext = "exchange-no.htm";
		}
		
		st.exitQuest(true);
		return htmltext;
	}


	public static void main(String[] args)
	{
		new ShadowWeapons();
	}
}
