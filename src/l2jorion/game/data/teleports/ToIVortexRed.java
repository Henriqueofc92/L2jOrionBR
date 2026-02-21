package l2jorion.game.data.teleports;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;

/**
 * ToI Vortex Red (1102).<br>
 * Consumes Red Dimension Stone (4403) to teleport to Tower of Insolence.
 */
public class ToIVortexRed extends Quest
{
	private static final int RED_DIMENSION_STONE = 4403;
	private static final int DIMENSION_VORTEX_1 = 30952;
	private static final int DIMENSION_VORTEX_2 = 30953;
	
	public ToIVortexRed()
	{
		super(1102, "1102_toivortex_red", "Teleports");
		
		var created = new State("Start", this);
		setInitialState(created);
		
		for (var npcId : new int[]{ DIMENSION_VORTEX_1, DIMENSION_VORTEX_2 })
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
		
		if (st.getQuestItemsCount(RED_DIMENSION_STONE) >= 1)
		{
			st.takeItems(RED_DIMENSION_STONE, 1);
			player.teleToLocation(118558, 16659, 5987);
			st.exitQuest(true);
			return null;
		}
		
		st.exitQuest(true);
		return "1.htm";
	}


	public static void main(String[] args)
	{
		new ToIVortexRed();
	}
}
