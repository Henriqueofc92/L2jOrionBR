package l2jorion.game.data.quests;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.QuestState;

public class Q612_WarWithKetraOrcs extends Quest
{
	private static final String qn = "Q612_WarWithKetraOrcs";
	
	// Items
	private static final int Seed = 7187;
	private static final int Molar = 7234;
	
	public Q612_WarWithKetraOrcs()
	{
		super(612, qn, "War with Ketra Orcs");
		
		setItemsIds(Molar);
		
		addStartNpc(31377); // Ashas Varka Durai
		addTalkId(31377);
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31377-03.htm"))
		{
			if (player.getLevel() >= 74 && player.getAllianceWithVarkaKetra() <= -1)
			{
				st.set("cond", "1");
				st.setState(STATE_STARTED);
				st.playSound(QuestState.SOUND_ACCEPT);
			}
			else
			{
				htmltext = "31377-02.htm";
				st.exitQuest(true);
			}
		}
		else if (event.equalsIgnoreCase("31377-07.htm"))
		{
			if (st.getQuestItemsCount(Molar) >= 100)
			{
				st.takeItems(Molar, 100);
				st.giveItems(Seed, 20);
				st.playSound(QuestState.SOUND_ITEMGET);
			}
			else
				htmltext = "31377-08.htm";
		}
		else if (event.equalsIgnoreCase("31377-09.htm"))
		{
			st.takeItems(Molar, -1);
			st.exitQuest(true);
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
				htmltext = "31377-01.htm";
				break;
			
			case STATE_STARTED:
				if (st.getQuestItemsCount(Molar) > 0)
					htmltext = "31377-04.htm";
				else
					htmltext = "31377-05.htm";
				break;
		}
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q612_WarWithKetraOrcs();
	}
}