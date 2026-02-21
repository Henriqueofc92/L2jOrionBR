package l2jorion.game.data.quests;

import l2jorion.game.model.L2Clan;
import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.QuestState;
import l2jorion.game.network.SystemMessageId;
import l2jorion.game.network.serverpackets.PledgeShowInfoUpdate;
import l2jorion.game.network.serverpackets.SystemMessage;

public class Q510_AClansReputation extends Quest
{
	private static final String qn = "Q510_AClansReputation";
	
	// NPC
	private static final int Valdis = 31331;
	
	// Quest Item
	private static final int Claw = 8767;
	
	// Reward
	private static final int CLAN_POINTS_REWARD = 50; // Quantity of points
	
	public Q510_AClansReputation()
	{
		super(510, qn, "A Clan's Reputation");
		
		setItemsIds(Claw);
		
		addStartNpc(Valdis);
		addTalkId(Valdis);
		
		addKillId(22215, 22216, 22217);
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31331-3.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31331-6.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
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
				if (!player.isClanLeader())
				{
					st.exitQuest(true);
					htmltext = "31331-0.htm";
				}
				else if (player.getClan().getLevel() < 5)
				{
					st.exitQuest(true);
					htmltext = "31331-0.htm";
				}
				else
					htmltext = "31331-1.htm";
				break;
			
			case STATE_STARTED:
				if (st.getInt("cond") == 1)
				{
					int count = st.getQuestItemsCount(Claw);
					if (count > 0)
					{
						int reward = (CLAN_POINTS_REWARD * count);
						st.takeItems(Claw, -1);
						L2Clan clan = player.getClan();
						clan.addReputationScore(reward);
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_QUEST_COMPLETED_AND_S1_POINTS_GAINED).addNumber(reward));
						clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
						
						htmltext = "31331-7.htm";
					}
					else
						htmltext = "31331-4.htm";
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance player, boolean isPet)
	{
		// Retrieve the qs of the clan leader.
		QuestState st = getClanLeaderQuestState(player, npc);
		if (st == null || !st.isStarted())
			return null;
		
		st.giveItems(Claw, 1);
		st.playSound(QuestState.SOUND_MIDDLE);
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q510_AClansReputation();
	}
}