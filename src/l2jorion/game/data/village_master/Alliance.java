package l2jorion.game.data.village_master;

import java.util.Set;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.QuestState;
import l2jorion.game.model.quest.State;

/**
 * Alliance Management (9001).<br>
 * Alliance dialogs for village masters.
 */
public class Alliance extends Quest
{
	private static final Set<Integer> NPCS = Set.of(
		30026, 30031, 30037, 30066, 30070, 30109, 30115, 30120, 30154, 30174,
		30175, 30176, 30187, 30191, 30195, 30288, 30289, 30290, 30297, 30358,
		30373, 30462, 30474, 30498, 30499, 30500, 30503, 30504, 30505, 30508,
		30511, 30512, 30513, 30520, 30525, 30565, 30594, 30595, 30676, 30677,
		30681, 30685, 30687, 30689, 30694, 30699, 30704, 30845, 30847, 30849,
		30854, 30857, 30862, 30865, 30894, 30897, 30900, 30905, 30910, 30913,
		31269, 31272, 31276, 31279, 31285, 31288, 31314, 31317, 31321, 31324,
		31326, 31328, 31331, 31334, 31755, 31958, 31961, 31965, 31968, 31974,
		31977, 31996, 32092, 32093, 32094, 32095, 32096, 32097, 32098
	);
	
	private State STARTED;
	
	public Alliance()
	{
		super(9001, "9001_alliance", "village_master");
		
		var created = new State("Start", this);
		STARTED = new State("Started", this);
		new State("Completed", this);
		setInitialState(created);
		
		for (var npcId : NPCS)
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}
	}
	
	@Override
	public String onEvent(String event, QuestState st)
	{
		if ("9001-01.htm".equals(event))
		{
			return "9001-01.htm";
		}
		
		if (st.getPlayer().getClanId() == 0)
		{
			st.exitQuest(true);
			return "<html><body>You must be in Clan.</body></html>";
		}
		
		if ("9001-02.htm".equals(event))
		{
			return "9001-02.htm";
		}
		return event;
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		var st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		if (NPCS.contains(npc.getNpcId()))
		{
			st.set("cond", "0");
			st.setState(STARTED);
			return "9001-01.htm";
		}
		return null;
	}


	public static void main(String[] args)
	{
		new Alliance();
	}
}
