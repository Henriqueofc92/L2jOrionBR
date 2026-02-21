package l2jorion.game.data.quests;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.QuestState;

public class Q121_PavelTheGiant extends Quest
{
	private static final String qn = "Q121_PavelTheGiant";
	
	// NPCs
	private static final int NEWYEAR = 31961;
	private static final int YUMI = 32041;
	
	public Q121_PavelTheGiant()
	{
		super(121, qn, "Pavel the Giant");
		
		addStartNpc(NEWYEAR);
		addTalkId(NEWYEAR, YUMI);
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31961-2.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32041-2.htm"))
		{
			st.rewardExpAndSp(10000, 0);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
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
				htmltext = (player.getLevel() < 46) ? "31961-1a.htm" : "31961-1.htm";
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case NEWYEAR:
						htmltext = "31961-2a.htm";
						break;
					
					case YUMI:
						htmltext = "32041-1.htm";
						break;
				}
				break;
			
			case STATE_COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q121_PavelTheGiant();
	}
}