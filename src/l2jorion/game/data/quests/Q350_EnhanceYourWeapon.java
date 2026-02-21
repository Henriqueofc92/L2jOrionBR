package l2jorion.game.data.quests;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.QuestState;

/**
 * Enhance Your Weapon - Soul Crystal quest. Only handles NPC dialog and giving the initial crystal. The actual crystal leveling is handled natively by L2Attackable.levelSoulCrystals() and the SoulCrystals item handler.
 */
public class Q350_EnhanceYourWeapon extends Quest
{
	private static final String qn = "Q350_EnhanceYourWeapon";
	
	// NPCs
	private static final int JUREK = 30115;
	private static final int GIDEON = 30194;
	private static final int WINONIN = 30856;
	
	// Initial Soul Crystals
	private static final int RED_SOUL_CRYSTAL0 = 4629;
	private static final int GREEN_SOUL_CRYSTAL0 = 4640;
	private static final int BLUE_SOUL_CRYSTAL0 = 4651;
	
	public Q350_EnhanceYourWeapon()
	{
		super(350, qn, "Enhance Your Weapon");
		
		addStartNpc(JUREK, GIDEON, WINONIN);
		addTalkId(JUREK, GIDEON, WINONIN);
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.endsWith("-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.endsWith("-09.htm"))
		{
			st.giveItems(RED_SOUL_CRYSTAL0, 1);
		}
		else if (event.endsWith("-10.htm"))
		{
			st.giveItems(GREEN_SOUL_CRYSTAL0, 1);
		}
		else if (event.endsWith("-11.htm"))
		{
			st.giveItems(BLUE_SOUL_CRYSTAL0, 1);
		}
		else if (event.equalsIgnoreCase("exit.htm"))
		{
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
		{
			return htmltext;
		}
		
		final String npcId = String.valueOf(npc.getNpcId());
		
		switch (st.getStateByte())
		{
			case STATE_CREATED:
				st.set("cond", "0");
				htmltext = npcId + "-01.htm";
				break;
			
			case STATE_STARTED:
				if (hasAnyCrystal(st))
				{
					htmltext = npcId + "-03.htm";
				}
				else if (st.getQuestItemsCount(RED_SOUL_CRYSTAL0) == 0 && st.getQuestItemsCount(GREEN_SOUL_CRYSTAL0) == 0 && st.getQuestItemsCount(BLUE_SOUL_CRYSTAL0) == 0)
				{
					htmltext = npcId + "-21.htm";
				}
				break;
		}
		
		return htmltext;
	}
	
	private static boolean hasAnyCrystal(QuestState st)
	{
		for (int i = 4629; i <= 4664; i++)
		{
			if (st.getQuestItemsCount(i) > 0)
			{
				return true;
			}
		}
		return false;
	}
	
	public static void main(String[] args)
	{
		new Q350_EnhanceYourWeapon();
	}
}
