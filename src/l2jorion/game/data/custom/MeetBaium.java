package l2jorion.game.data.custom;

import l2jorion.game.managers.GrandBossManager;
import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;

/**
 * Meet Baium custom script (8003).<br>
 * NPC 31862 - Angelic Vortex. Shows special dialog if Baium is not dead and player has the item.
 */
public class MeetBaium extends Quest
{
	private static final int NPC_ID = 31862;
	private static final int BAIUM = 29020;
	private static final int BLOODED_FABRIC = 4295;
	
	public MeetBaium()
	{
		super(-1, "8003_MeetBaium", "custom");
		
		var created = new State("Start", this);
		new State("Started", this);
		setInitialState(created);
		
		addStartNpc(NPC_ID);
		addFirstTalkId(NPC_ID);
		addTalkId(NPC_ID);
	}
	
	@Override
	public String onFirstTalk(L2NpcInstance npc, L2PcInstance player)
	{
		var st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		var baiumStatus = GrandBossManager.getInstance().getBossStatus(BAIUM);
		if (baiumStatus != 2 && st.getQuestItemsCount(BLOODED_FABRIC) > 0)
		{
			st.exitQuest(true);
			return "31862.htm";
		}
		npc.showChatWindow(player);
		st.exitQuest(true);
		return null;
	}


	public static void main(String[] args)
	{
		new MeetBaium();
	}
}
