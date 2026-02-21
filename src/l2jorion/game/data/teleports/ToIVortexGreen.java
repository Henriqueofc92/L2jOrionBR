package l2jorion.game.data.teleports;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;

/**
 * ToI Vortex Green (1102).<br>
 * Consumes Green Dimension Stone (4401) to teleport to Tower of Insolence.
 */
public class ToIVortexGreen extends Quest
{
	private static final int GREEN_DIMENSION_STONE = 4401;
	private static final int DIMENSION_VORTEX_2 = 30953;
	private static final int DIMENSION_VORTEX_3 = 30954;
	
	public ToIVortexGreen()
	{
		super(1102, "1102_toivortex_green", "Teleports");
		
		var created = new State("Start", this);
		setInitialState(created);
		
		for (var npcId : new int[]{ DIMENSION_VORTEX_2, DIMENSION_VORTEX_3 })
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
		
		if (st.getQuestItemsCount(GREEN_DIMENSION_STONE) >= 1)
		{
			st.takeItems(GREEN_DIMENSION_STONE, 1);
			player.teleToLocation(110930, 15963, -4378);
			st.exitQuest(true);
			return null;
		}
		
		st.exitQuest(true);
		return "1.htm";
	}


	public static void main(String[] args)
	{
		new ToIVortexGreen();
	}
}
