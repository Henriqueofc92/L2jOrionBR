package l2jorion.game.data.teleports;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;

/**
 * Oracle Teleport (1103).<br>
 * Dawn/Dusk oracle teleporters with return via Temple Priests.
 */
public class OracleTeleport extends Quest
{
	private static final int[] TOWN_DAWN = { 31078, 31079, 31080, 31081, 31083, 31084, 31082, 31692, 31694, 31997, 31168 };
	private static final int[] TOWN_DUSK = { 31085, 31086, 31087, 31088, 31090, 31091, 31089, 31693, 31695, 31998, 31169 };
	private static final int[] TEMPLE_PRIEST = { 31127, 31128, 31129, 31130, 31131, 31137, 31138, 31139, 31140, 31141 };
	
	private static final Map<Integer, Integer> TELEPORTERS = Map.ofEntries(
		Map.entry(31078, 1), Map.entry(31079, 2), Map.entry(31080, 3), Map.entry(31081, 4),
		Map.entry(31083, 5), Map.entry(31084, 6), Map.entry(31082, 7), Map.entry(31692, 8),
		Map.entry(31694, 9), Map.entry(31997, 10), Map.entry(31168, 11),
		Map.entry(31085, 12), Map.entry(31086, 13), Map.entry(31087, 14), Map.entry(31088, 15),
		Map.entry(31090, 16), Map.entry(31091, 17), Map.entry(31089, 18), Map.entry(31693, 19),
		Map.entry(31695, 20), Map.entry(31998, 21), Map.entry(31169, 22)
	);
	
	private static final int[][] RETURN_LOCS =
	{
		{-80555, 150337, -3040}, {-13953, 121404, -2984}, {16354, 142820, -2696}, {83369, 149253, -3400},
		{83106, 53965, -1488}, {146983, 26595, -2200}, {111386, 220858, -3544}, {148256, -55454, -2779},
		{45664, -50318, -800}, {86795, -143078, -1341}, {115136, 74717, -2608}, {-82368, 151568, -3120},
		{-14748, 123995, -3112}, {18482, 144576, -3056}, {81623, 148556, -3464}, {82819, 54607, -1520},
		{147570, 28877, -2264}, {112486, 220123, -3592}, {149888, -56574, -2979}, {44528, -48370, -800},
		{85129, -142103, -1542}, {116642, 77510, -2688}
	};
	
	private static final Set<Integer> DAWN_SET = Arrays.stream(TOWN_DAWN).boxed().collect(Collectors.toUnmodifiableSet());
	private static final Set<Integer> DUSK_SET = Arrays.stream(TOWN_DUSK).boxed().collect(Collectors.toUnmodifiableSet());
	private static final Set<Integer> PRIEST_SET = Arrays.stream(TEMPLE_PRIEST).boxed().collect(Collectors.toUnmodifiableSet());
	
	private State CREATED_STATE;
	private State STARTED_STATE;
	
	public OracleTeleport()
	{
		super(1103, "1103_OracleTeleport", "Teleports");
		
		CREATED_STATE = new State("Start", this);
		STARTED_STATE = new State("Started", this);
		setInitialState(CREATED_STATE);
		
		for (var npcId : TELEPORTERS.keySet())
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}
		for (var npcId : TEMPLE_PRIEST)
		{
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
		
		if (DAWN_SET.contains(npcId))
		{
			st.setState(STARTED_STATE);
			st.set("id", String.valueOf(TELEPORTERS.get(npcId)));
			player.teleToLocation(-80157, 111344, -4901);
		}
		else if (DUSK_SET.contains(npcId))
		{
			st.setState(STARTED_STATE);
			st.set("id", String.valueOf(TELEPORTERS.get(npcId)));
			player.teleToLocation(-81261, 86531, -5157);
		}
		else if (PRIEST_SET.contains(npcId) && st.getState() == STARTED_STATE)
		{
			var returnId = st.getInt("id") - 1;
			if (returnId >= 0 && returnId < RETURN_LOCS.length)
			{
				player.teleToLocation(RETURN_LOCS[returnId][0], RETURN_LOCS[returnId][1], RETURN_LOCS[returnId][2]);
			}
			st.setState(CREATED_STATE);
		}
		
		return null;
	}


	public static void main(String[] args)
	{
		new OracleTeleport();
	}
}
