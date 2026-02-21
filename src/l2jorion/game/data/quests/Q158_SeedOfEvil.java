package l2jorion.game.data.quests;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.QuestState;

public class Q158_SeedOfEvil extends Quest
{
	private static final String qn = "Q158_SeedOfEvil";
	
	// Item
	private static final int CLAY_TABLET = 1025;
	
	// Reward
	private static final int ENCHANT_ARMOR_D = 956;
	
	public Q158_SeedOfEvil()
	{
		super(158, qn, "Seed of Evil");
		
		setItemsIds(CLAY_TABLET);
		
		addStartNpc(30031); // Biotin
		addTalkId(30031);
		
		addKillId(27016); // Nerkas
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30031-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg();
		if (st == null)
			return htmltext;
		
		switch (st.getStateByte())
		{
			case STATE_CREATED:
				htmltext = (player.getLevel() < 21) ? "30031-02.htm" : "30031-03.htm";
				break;
			
			case STATE_STARTED:
				if (!st.hasQuestItems(CLAY_TABLET))
					htmltext = "30031-05.htm";
				else
				{
					htmltext = "30031-06.htm";
					st.takeItems(CLAY_TABLET, 1);
					st.giveItems(ENCHANT_ARMOR_D, 1);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(false);
				}
				break;
			
			case STATE_COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = checkPlayerCondition(player, npc, "cond", "1");
		if (st == null)
			return null;
		
		st.set("cond", "2");
		st.playSound(QuestState.SOUND_MIDDLE);
		st.giveItems(CLAY_TABLET, 1);
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q158_SeedOfEvil();
	}
}