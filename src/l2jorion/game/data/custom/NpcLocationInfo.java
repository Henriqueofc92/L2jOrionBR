package l2jorion.game.data.custom;

import java.util.HashMap;
import java.util.Map;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;

/**
 * NPC Location Info custom script (8001).<br>
 * Shows NPC locations on radar via guide NPCs.
 */
public class NpcLocationInfo extends Quest
{
	private static final int[] NPC = { 30598, 30599, 30600, 30601, 30602 };
	
	private static final Map<Integer, int[]> RADAR = new HashMap<>();
	
	static
	{
		// Talking Island
		RADAR.put(30006, new int[]{-84108, 244604, -3729});
		RADAR.put(30039, new int[]{-82236, 241573, -3728});
		RADAR.put(30040, new int[]{-82515, 241221, -3728});
		RADAR.put(30041, new int[]{-82319, 244709, -3727});
		RADAR.put(30042, new int[]{-82659, 244992, -3717});
		RADAR.put(30043, new int[]{-86114, 244682, -3727});
		RADAR.put(30044, new int[]{-86328, 244448, -3724});
		RADAR.put(30045, new int[]{-86322, 241215, -3727});
		RADAR.put(30046, new int[]{-85964, 240947, -3727});
		RADAR.put(30283, new int[]{-85026, 242689, -3729});
		RADAR.put(30003, new int[]{-83789, 240799, -3717});
		RADAR.put(30004, new int[]{-84204, 240403, -3717});
		RADAR.put(30001, new int[]{-86385, 243267, -3717});
		RADAR.put(30002, new int[]{-86733, 242918, -3717});
		RADAR.put(30031, new int[]{-84516, 245449, -3714});
		RADAR.put(30033, new int[]{-84729, 245001, -3726});
		RADAR.put(30035, new int[]{-84965, 245222, -3726});
		RADAR.put(30032, new int[]{-84981, 244764, -3726});
		RADAR.put(30036, new int[]{-85186, 245001, -3726});
		RADAR.put(30026, new int[]{-83326, 242964, -3718});
		RADAR.put(30027, new int[]{-83020, 242553, -3718});
		RADAR.put(30029, new int[]{-83175, 243065, -3718});
		RADAR.put(30028, new int[]{-82809, 242751, -3718});
		RADAR.put(30054, new int[]{-81895, 243917, -3721});
		RADAR.put(30055, new int[]{-81840, 243534, -3721});
		RADAR.put(30005, new int[]{-81512, 243424, -3720});
		RADAR.put(30048, new int[]{-84436, 242793, -3729});
		RADAR.put(30312, new int[]{-78939, 240305, -3443});
		RADAR.put(30368, new int[]{-85301, 244587, -3725});
		RADAR.put(30049, new int[]{-83163, 243560, -3728});
		RADAR.put(30047, new int[]{-97131, 258946, -3622});
		RADAR.put(30497, new int[]{-114685, 222291, -2925});
		RADAR.put(30050, new int[]{-84057, 242832, -3729});
		RADAR.put(30311, new int[]{-100332, 238019, -3573});
		RADAR.put(30051, new int[]{-82041, 242718, -3725});
		
		// Dark Elf Village
		RADAR.put(30134, new int[]{9670, 15537, -4499});
		RADAR.put(30224, new int[]{15120, 15656, -4301});
		RADAR.put(30348, new int[]{17306, 13592, -3649});
		RADAR.put(30355, new int[]{15272, 16310, -4302});
		RADAR.put(30347, new int[]{6449, 19619, -3619});
		RADAR.put(30432, new int[]{-15404, 71131, -3370});
		RADAR.put(30356, new int[]{7490, 17397, -4378});
		RADAR.put(30349, new int[]{17102, 13002, -3668});
		RADAR.put(30346, new int[]{6532, 19903, -3618});
		RADAR.put(30433, new int[]{-15648, 71405, -3376});
		RADAR.put(30357, new int[]{7634, 18047, -4378});
		RADAR.put(30431, new int[]{-1301, 75883, -3491});
		RADAR.put(30430, new int[]{-1152, 76125, -3491});
		RADAR.put(30307, new int[]{10584, 17574, -4557});
		RADAR.put(30138, new int[]{12009, 15704, -4555});
		RADAR.put(30137, new int[]{11951, 15661, -4555});
		RADAR.put(30135, new int[]{10761, 17970, -4558}); // also Magister Harne
		RADAR.put(30136, new int[]{10823, 18013, -4558});
		RADAR.put(30143, new int[]{11283, 14226, -4167});
		RADAR.put(30360, new int[]{10447, 14620, -4167});
		RADAR.put(30145, new int[]{11258, 14431, -4167});
		RADAR.put(30144, new int[]{10344, 14445, -4167});
		RADAR.put(30358, new int[]{10775, 14190, -4167});
		RADAR.put(30359, new int[]{11235, 14078, -4167});
		RADAR.put(30141, new int[]{11012, 14128, -4167});
		RADAR.put(30139, new int[]{13380, 17430, -4544});
		RADAR.put(30140, new int[]{13464, 17751, -4544});
		RADAR.put(30350, new int[]{13763, 17501, -4544});
		RADAR.put(30421, new int[]{-44225, 79721, -3577});
		RADAR.put(30419, new int[]{-44015, 79683, -3577});
		RADAR.put(30130, new int[]{25856, 10832, -3649});
		RADAR.put(30351, new int[]{12328, 14947, -4499});
		RADAR.put(30353, new int[]{13081, 18444, -4498});
		RADAR.put(30354, new int[]{12311, 17470, -4499});
		
		// Elven Village
		RADAR.put(30146, new int[]{46926, 51511, -2977});
		RADAR.put(30285, new int[]{44995, 51706, -2803});
		RADAR.put(30284, new int[]{45727, 51721, -2803});
		RADAR.put(30221, new int[]{42812, 51138, -2996});
		RADAR.put(30217, new int[]{45487, 46511, -2996});
		RADAR.put(30219, new int[]{47401, 51764, -2996});
		RADAR.put(30220, new int[]{42971, 51372, -2996});
		RADAR.put(30218, new int[]{47595, 51569, -2996});
		RADAR.put(30216, new int[]{45778, 46534, -2996});
		RADAR.put(30363, new int[]{44476, 47153, -2984});
		RADAR.put(30149, new int[]{42700, 50057, -2984});
		RADAR.put(30150, new int[]{42766, 50037, -2984});
		RADAR.put(30148, new int[]{44683, 46952, -2981});
		RADAR.put(30147, new int[]{44667, 46896, -2982});
		RADAR.put(30155, new int[]{45725, 52105, -2795});
		RADAR.put(30156, new int[]{44823, 52414, -2795});
		RADAR.put(30157, new int[]{45000, 52101, -2795});
		RADAR.put(30158, new int[]{45919, 52414, -2795});
		RADAR.put(30154, new int[]{44692, 52261, -2795});
		RADAR.put(30153, new int[]{47780, 49568, -2983});
		RADAR.put(30152, new int[]{47912, 50170, -2983});
		RADAR.put(30151, new int[]{47868, 50167, -2983});
		RADAR.put(30423, new int[]{28928, 74248, -3773});
		RADAR.put(30414, new int[]{43673, 49683, -3046});
		RADAR.put(31853, new int[]{50592, 54896, -3376});
		RADAR.put(30223, new int[]{42978, 49115, -2994});
		RADAR.put(30362, new int[]{46475, 50495, -3058});
		RADAR.put(30222, new int[]{45859, 50827, -3058});
		RADAR.put(30371, new int[]{51210, 82474, -3283});
		RADAR.put(31852, new int[]{49262, 53607, -3216});
		
		// Dwarven Village
		RADAR.put(30540, new int[]{115072, -178176, -906});
		RADAR.put(30541, new int[]{117847, -182339, -1537});
		RADAR.put(30542, new int[]{116617, -184308, -1569});
		RADAR.put(30543, new int[]{117826, -182576, -1537});
		RADAR.put(30544, new int[]{116378, -184308, -1571});
		RADAR.put(30545, new int[]{115183, -176728, -791});
		RADAR.put(30546, new int[]{114969, -176752, -790});
		RADAR.put(30547, new int[]{117366, -178725, -1118});
		RADAR.put(30548, new int[]{117378, -178914, -1120});
		RADAR.put(30531, new int[]{116226, -178529, -948});
		RADAR.put(30532, new int[]{116190, -178441, -948});
		RADAR.put(30533, new int[]{116016, -178615, -948});
		RADAR.put(30534, new int[]{116190, -178615, -948});
		RADAR.put(30535, new int[]{116103, -178407, -948});
		RADAR.put(30536, new int[]{116103, -178653, -948});
		RADAR.put(30525, new int[]{115468, -182446, -1434});
		RADAR.put(30526, new int[]{115315, -182155, -1444});
		RADAR.put(30527, new int[]{115271, -182692, -1445});
		RADAR.put(30518, new int[]{115900, -177316, -915});
		RADAR.put(30519, new int[]{116268, -177524, -914});
		RADAR.put(30516, new int[]{115741, -181645, -1344});
		RADAR.put(30517, new int[]{116192, -181072, -1344});
		RADAR.put(30520, new int[]{115205, -180024, -870});
		RADAR.put(30521, new int[]{114716, -180018, -871});
		RADAR.put(30522, new int[]{114832, -179520, -871});
		RADAR.put(30523, new int[]{115717, -183488, -1483});
		RADAR.put(30524, new int[]{115618, -183265, -1483});
		RADAR.put(30537, new int[]{114348, -178537, -813});
		RADAR.put(30650, new int[]{114990, -177294, -854});
		RADAR.put(30538, new int[]{114426, -178672, -812});
		RADAR.put(30539, new int[]{114409, -178415, -812});
		RADAR.put(30671, new int[]{117061, -181867, -1413});
		RADAR.put(30651, new int[]{116164, -184029, -1507});
		RADAR.put(30550, new int[]{115563, -182923, -1448});
		RADAR.put(30554, new int[]{112656, -174864, -611});
		RADAR.put(30553, new int[]{116852, -183595, -1566});
		
		// Orc Village
		RADAR.put(30576, new int[]{-45264, -112512, -235});
		RADAR.put(30577, new int[]{-46576, -117311, -242});
		RADAR.put(30578, new int[]{-47360, -113791, -237});
		RADAR.put(30579, new int[]{-47360, -113424, -235});
		RADAR.put(30580, new int[]{-45744, -117165, -236});
		RADAR.put(30581, new int[]{-46528, -109968, -250});
		RADAR.put(30582, new int[]{-45808, -110055, -255});
		RADAR.put(30583, new int[]{-45731, -113844, -237});
		RADAR.put(30584, new int[]{-45728, -113360, -237});
		RADAR.put(30569, new int[]{-45952, -114784, -199});
		RADAR.put(30570, new int[]{-45952, -114496, -199});
		RADAR.put(30571, new int[]{-45863, -112621, -200});
		RADAR.put(30572, new int[]{-45864, -112540, -199});
		RADAR.put(30564, new int[]{-43264, -112532, -220});
		RADAR.put(30560, new int[]{-43910, -115518, -194});
		RADAR.put(30561, new int[]{-43950, -115457, -194});
		RADAR.put(30558, new int[]{-44416, -111486, -222});
		RADAR.put(30559, new int[]{-43926, -111794, -222});
		RADAR.put(30562, new int[]{-43109, -113770, -221});
		RADAR.put(30563, new int[]{-43114, -113404, -221});
		RADAR.put(30565, new int[]{-46768, -113610, -3});
		RADAR.put(30566, new int[]{-46802, -114011, -112});
		RADAR.put(30567, new int[]{-46247, -113866, -21});
		RADAR.put(30568, new int[]{-46808, -113184, -112});
		RADAR.put(30585, new int[]{-45328, -114736, -237});
		RADAR.put(30587, new int[]{-44624, -111873, -238});
	}
	
	public NpcLocationInfo()
	{
		super(8001, "8001_NpcLocationInfo", "custom");
		
		for (var npcId : NPC)
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		var st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		if (event.matches("\\d+"))
		{
			var npcId = Integer.parseInt(event);
			var loc = RADAR.get(npcId);
			if (loc != null)
			{
				st.addRadar(loc[0], loc[1], loc[2]);
				st.exitQuest(true);
				return "MoveToLoc.htm";
			}
			st.exitQuest(true);
			return null;
		}
		
		return event;
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		return npc.getNpcId() + ".htm";
	}


	public static void main(String[] args)
	{
		new NpcLocationInfo();
	}
}
