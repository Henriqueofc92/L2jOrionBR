package l2jorion.game.data.teleports;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;

/**
 * ToI Vortex Blue (1102).<br>
 * Consumes Blue Dimension Stone (4402) to teleport to Tower of Insolence.
 */
public class ToIVortexBlue extends Quest
{
	private static final int BLUE_DIMENSION_STONE = 4402;
	private static final int DIMENSION_VORTEX_1 = 30952;
	private static final int DIMENSION_VORTEX_3 = 30954;
	
	public ToIVortexBlue()
	{
		super(1102, "1102_toivortex_blue", "Teleports");
		
		var created = new State("Start", this);
		setInitialState(created);
		
		for (var npcId : new int[]{ DIMENSION_VORTEX_1, DIMENSION_VORTEX_3 })
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
		
		if (st.getQuestItemsCount(BLUE_DIMENSION_STONE) >= 1)
		{
			st.takeItems(BLUE_DIMENSION_STONE, 1);
			player.teleToLocation(114097, 19935, 935);
			st.exitQuest(true);
			return null;
		}
		
		st.exitQuest(true);
		return "1.htm";
	}


	public static void main(String[] args)
	{
		new ToIVortexBlue();
	}
}
