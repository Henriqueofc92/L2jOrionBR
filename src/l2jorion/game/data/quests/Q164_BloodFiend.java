package l2jorion.game.data.quests;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.base.Race;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.QuestState;

public class Q164_BloodFiend extends Quest
{
	private static final String qn = "Q164_BloodFiend";
	
	// Item
	private static final int KIRUNAK_SKULL = 1044;
	
	public Q164_BloodFiend()
	{
		super(164, qn, "Blood Fiend");
		
		setItemsIds(KIRUNAK_SKULL);
		
		addStartNpc(30149);
		addTalkId(30149);
		
		addKillId(27021);
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30149-04.htm"))
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
		String htmltext = getNoQuestMsg();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		switch (st.getStateByte())
		{
			case STATE_CREATED:
				if (player.getRace() == Race.darkelf)
					htmltext = "30149-00.htm";
				else if (player.getLevel() < 21)
					htmltext = "30149-02.htm";
				else
					htmltext = "30149-03.htm";
				break;
			
			case STATE_STARTED:
				if (st.hasQuestItems(KIRUNAK_SKULL))
				{
					htmltext = "30149-06.htm";
					st.takeItems(KIRUNAK_SKULL, 1);
					st.rewardItems(57, 42130);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(false);
				}
				else
					htmltext = "30149-05.htm";
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
		
		if (!st.hasQuestItems(KIRUNAK_SKULL))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(KIRUNAK_SKULL, 1);
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q164_BloodFiend();
	}
}