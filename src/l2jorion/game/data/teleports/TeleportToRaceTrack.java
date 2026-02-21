package l2jorion.game.data.teleports;

import java.util.Map;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;

/**
 * Teleport to Race Track (1101).<br>
 * Teleports to Monster Derby Race Track and stores return location.
 */
public class TeleportToRaceTrack extends Quest
{
	private static final int RACE_MANAGER = 30995;
	
	private static final Map<Integer, Integer> TELEPORTERS = Map.ofEntries(
		Map.entry(30059, 3), Map.entry(30080, 4), Map.entry(30177, 6), Map.entry(30233, 8),
		Map.entry(30256, 2), Map.entry(30320, 1), Map.entry(30848, 7), Map.entry(30899, 5),
		Map.entry(31320, 9), Map.entry(31275, 10), Map.entry(30727, 11), Map.entry(30836, 12),
		Map.entry(31964, 13), Map.entry(31210, 14)
	);
	
	private static final int[][] RETURN_LOCS =
	{
		{-80826, 149775, -3043}, {-12672, 122776, -3116}, {15670, 142983, -2705}, {83400, 147943, -3404},
		{111409, 219364, -3545}, {82956, 53162, -1495}, {146331, 25762, -2018}, {116819, 76994, -2714},
		{43835, -47749, -792}, {147930, -55281, -2728}, {85335, 16177, -3694}, {105857, 109763, -3202},
		{87386, -143246, -1293}, {12882, 181053, -3560}
	};
	
	private State STARTED;
	
	public TeleportToRaceTrack()
	{
		super(1101, "1101_teleport_to_race_track", "Teleports");
		
		var created = new State("Start", this);
		STARTED = new State("Started", this);
		setInitialState(created);
		
		for (var npcId : TELEPORTERS.keySet())
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}
		addTalkId(RACE_MANAGER);
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
		
		if (TELEPORTERS.containsKey(npcId))
		{
			player.teleToLocation(12661, 181687, -3560);
			st.setState(STARTED);
			st.set("id", String.valueOf(TELEPORTERS.get(npcId)));
		}
		else if (st.getState() == STARTED && npcId == RACE_MANAGER)
		{
			var returnId = st.getInt("id") - 1;
			if (returnId >= 0 && returnId < RETURN_LOCS.length)
			{
				player.teleToLocation(RETURN_LOCS[returnId][0], RETURN_LOCS[returnId][1], RETURN_LOCS[returnId][2]);
			}
			st.exitQuest(true);
		}
		
		return null;
	}


	public static void main(String[] args)
	{
		new TeleportToRaceTrack();
	}
}
