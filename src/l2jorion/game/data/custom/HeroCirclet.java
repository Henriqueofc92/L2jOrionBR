package l2jorion.game.data.custom;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;

/**
 * Hero Circlet custom script (6999).<br>
 * Gives Wings of Destiny Circlet to Heroes.
 */
public class HeroCirclet extends Quest
{
	private static final int[] MONUMENTS = { 31690, 31769, 31770, 31771, 31772 };
	private static final int CIRCLET = 6842;
	
	public HeroCirclet()
	{
		super(-1, "6999_HeroCirclet", "custom");
		
		var created = new State("Start", this);
		setInitialState(created);
		
		for (var npcId : MONUMENTS)
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
		
		String htmltext;
		if (player.isHero())
		{
			if (st.getQuestItemsCount(CIRCLET) > 0)
			{
				htmltext = "You can't have more than one circlet.";
			}
			else
			{
				st.giveItems(CIRCLET, 1);
				htmltext = "Enjoy your Wings of Destiny Circlet.";
			}
			st.exitQuest(true);
		}
		else
		{
			htmltext = "<html><body>Monument of Heroes:<br>You're not a Hero and aren't eligible to receive the Wings of Destiny Circlet. Better luck next time.<br><a action=\"bypass -h npc_" + npc.getObjectId() + "_Chat 0\">Return</a></body></html>";
			st.exitQuest(true);
		}
		
		return htmltext;
	}


	public static void main(String[] args)
	{
		new HeroCirclet();
	}
}
