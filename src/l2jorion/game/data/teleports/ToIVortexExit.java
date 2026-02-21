package l2jorion.game.data.teleports;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;
import l2jorion.util.random.Rnd;

/**
 * Tower of Insolence Vortex Exit (2400).<br>
 * Randomly teleports player to one of 3 exit points.
 */
public class ToIVortexExit extends Quest
{
	private static final int NPC = 29055;
	
	private static final int[][] EXITS =
	{
		{ 108784, 16000, -4928 },
		{ 113824, 10448, -5164 },
		{ 115488, 22096, -5168 }
	};
	
	public ToIVortexExit()
	{
		super(2400, "2400_toivortex_exit", "Teleports");
		
		var created = new State("Start", this);
		setInitialState(created);
		
		addStartNpc(NPC);
		addTalkId(NPC);
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		var st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		var exit = EXITS[Rnd.get(3)];
		int x = exit[0] + Rnd.get(100);
		int y = exit[1] + Rnd.get(100);
		int z = exit[2];
		
		player.teleToLocation(x, y, z);
		st.exitQuest(true);
		return null;
	}


	public static void main(String[] args)
	{
		new ToIVortexExit();
	}
}
