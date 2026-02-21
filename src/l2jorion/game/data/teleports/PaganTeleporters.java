package l2jorion.game.data.teleports;

import l2jorion.game.datatables.xml.DoorTable;
import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;

/**
 * Pagan Temple Teleporters (1630).<br>
 * Controls doors in Pagan Temple based on marks.
 */
public class PaganTeleporters extends Quest
{
	// Items
	private static final int VISITORS_MARK = 8064;
	private static final int FADED_VISITORS_MARK = 8065;
	private static final int PAGANS_MARK = 8067;
	
	// Doors
	private static final int DOOR_1 = 19160001;
	private static final int DOOR_2A = 19160010;
	private static final int DOOR_2B = 19160011;
	
	public PaganTeleporters()
	{
		super(-1, "1630_PaganTeleporters", "Teleports");
		
		var created = new State("Start", this);
		setInitialState(created);
		
		for (int npcId = 32034; npcId <= 32037; npcId++)
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}
		addFirstTalkId(32039);
		addFirstTalkId(32040);
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		switch (event)
		{
			case "Close_Door1" ->
			{
				DoorTable.getInstance().getDoor(DOOR_1).closeMe();
			}
			case "Close_Door2" ->
			{
				DoorTable.getInstance().getDoor(DOOR_2A).closeMe();
				DoorTable.getInstance().getDoor(DOOR_2B).closeMe();
			}
		}
		return null;
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		var st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		int npcId = npc.getNpcId();
		String htmltext = null;
		
		switch (npcId)
		{
			case 32034 ->
			{
				if (st.getQuestItemsCount(VISITORS_MARK) == 0 && st.getQuestItemsCount(FADED_VISITORS_MARK) == 0 && st.getQuestItemsCount(PAGANS_MARK) == 0)
				{
					return "<html><body>The Temple Gatekeeper:<br>You have nothing that would cover the holes.<br>(You must have a Visitor's Mark, a Faded Visitor's Mark, or a Pagan's Mark in order to open this door.)</body></html>";
				}
				if (st.getQuestItemsCount(VISITORS_MARK) > 0)
				{
					st.takeItems(VISITORS_MARK, 1);
					st.giveItems(FADED_VISITORS_MARK, 1);
				}
				htmltext = "FadedMark.htm";
				DoorTable.getInstance().getDoor(DOOR_1).openMe();
				startQuestTimer("Close_Door1", 10000, null, null);
			}
			case 32035 ->
			{
				DoorTable.getInstance().getDoor(DOOR_1).openMe();
				startQuestTimer("Close_Door1", 10000, null, null);
				htmltext = "FadedMark.htm";
			}
			case 32036 ->
			{
				if (st.getQuestItemsCount(PAGANS_MARK) == 0)
				{
					htmltext = "<html><body>The Temple Gatekeeper:<br>Show your Mark or be gone from my sight!<br>Only those who possess the Pagan's Mark may pass through this gate!</body></html>";
				}
				else
				{
					htmltext = "<html><body>The Temple Gatekeeper:<br>On seeing the Pagan's Mark, the statue's probing eyes go blank.<br>With the quiet whir of an engine, the gate swings open...</body></html>";
					startQuestTimer("Close_Door2", 10000, null, null);
					DoorTable.getInstance().getDoor(DOOR_2A).openMe();
					DoorTable.getInstance().getDoor(DOOR_2B).openMe();
				}
			}
			case 32037 ->
			{
				DoorTable.getInstance().getDoor(DOOR_2A).openMe();
				DoorTable.getInstance().getDoor(DOOR_2B).openMe();
				startQuestTimer("Close_Door2", 10000, null, null);
				htmltext = "FadedMark.htm";
			}
		}
		
		st.exitQuest(true);
		return htmltext;
	}


	public static void main(String[] args)
	{
		new PaganTeleporters();
	}
}
