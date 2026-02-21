package l2jorion.game.data.teleports;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;

/**
 * Teleport with Charm (1100).<br>
 * Consumes Orc Gatekeeper Charm or Dwarf Gatekeeper Token to teleport.
 */
public class TeleportWithCharm extends Quest
{
	private static final int ORC_GATEKEEPER_CHARM = 1658;
	private static final int DWARF_GATEKEEPER_TOKEN = 1659;
	private static final int WHIRPY = 30540;
	private static final int TAMIL = 30576;
	
	public TeleportWithCharm()
	{
		super(1100, "1100_teleport_with_charm", "Teleports");
		
		var created = new State("Start", this);
		setInitialState(created);
		
		for (var npcId : new int[]{ WHIRPY, TAMIL })
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
		
		var npcId = npc.getNpcId();
		if (npcId == TAMIL)
		{
			if (st.getQuestItemsCount(ORC_GATEKEEPER_CHARM) >= 1)
			{
				st.takeItems(ORC_GATEKEEPER_CHARM, 1);
				player.teleToLocation(-80826, 149775, -3043);
				st.exitQuest(true);
				return null;
			}
			st.exitQuest(true);
			return "30576-01.htm";
		}
		else if (npcId == WHIRPY)
		{
			if (st.getQuestItemsCount(DWARF_GATEKEEPER_TOKEN) >= 1)
			{
				st.takeItems(DWARF_GATEKEEPER_TOKEN, 1);
				player.teleToLocation(-80826, 149775, -3043);
				st.exitQuest(true);
				return null;
			}
			st.exitQuest(true);
			return "30540-01.htm";
		}
		
		return null;
	}


	public static void main(String[] args)
	{
		new TeleportWithCharm();
	}
}
