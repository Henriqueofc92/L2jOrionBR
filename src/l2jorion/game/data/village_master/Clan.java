package l2jorion.game.data.village_master;

import java.util.Set;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;

/**
 * Clan Management (9000).<br>
 * Clan hall-related dialogs for village masters.
 */
public class Clan extends Quest
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
	
	// Events requiring clan leader check
	private static final Set<String> LEADER_EVENTS = Set.of(
		"9000-03.htm", "9000-04.htm", "9000-05.htm", "9000-07.htm",
		"9000-06a.htm", "9000-12a.htm", "9000-12b.htm",
		"9000-13a.htm", "9000-13b.htm", "9000-14a.htm", "9000-14b.htm", "9000-15.htm"
	);
	
	private State STARTED;
	
	public Clan()
	{
		super(9000, "9000_clan", "village_master");
		
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
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		if (LEADER_EVENTS.contains(event))
		{
			if (player.getClan() == null || !player.isClanLeader())
			{
				return event.equals("9000-03.htm") ? "9000-03-no.htm"
					: event.equals("9000-04.htm") ? "9000-04-no.htm"
					: event.equals("9000-05.htm") ? "9000-05-no.htm"
					: "9000-07-no.htm";
			}
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
			return "9000-01.htm";
		}
		return null;
	}


	public static void main(String[] args)
	{
		new Clan();
	}
}
