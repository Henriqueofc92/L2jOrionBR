package l2jorion.game.data.custom;

import java.util.HashMap;
import java.util.Map;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;
import l2jorion.game.network.serverpackets.RadarControl;

/**
 * Raidboss Info custom script (8000).<br>
 * Shows raidboss locations on radar via NPCs 31729-31841.
 */
public class RaidbossInfo extends Quest
{
	// NPC range 31729-31841
	private static final Map<Integer, int[]> RADAR = new HashMap<>();
	
	static
	{
		// lvl20 list
		RADAR.put(25001, new int[]{-54464, 146572, -2400});
		RADAR.put(25019, new int[]{7352, 169433, -3172});
		RADAR.put(25038, new int[]{-57366, 186276, -4804});
		RADAR.put(25060, new int[]{-60427, 188266, -4352});
		RADAR.put(25076, new int[]{-61041, 127347, -2512});
		RADAR.put(25095, new int[]{-37799, 198120, -2200});
		RADAR.put(25127, new int[]{-47634, 219274, -1936});
		RADAR.put(25146, new int[]{-13698, 213796, -3300});
		RADAR.put(25149, new int[]{-12652, 138200, -3120});
		RADAR.put(25166, new int[]{-21778, 152065, -2636});
		RADAR.put(25272, new int[]{49194, 127999, -3161});
		RADAR.put(25357, new int[]{-3451, 112819, -3032});
		RADAR.put(25360, new int[]{29064, 179362, -3128});
		RADAR.put(25362, new int[]{-55791, 186903, -2856});
		RADAR.put(25365, new int[]{-62171, 190489, -3160});
		RADAR.put(25366, new int[]{-62342, 179572, -3088});
		RADAR.put(25369, new int[]{-45713, 111186, -3280});
		RADAR.put(25372, new int[]{48003, 243395, -6562});
		RADAR.put(25373, new int[]{9661, 76976, -3652});
		RADAR.put(25375, new int[]{22523, 80431, -2772});
		RADAR.put(25378, new int[]{-53970, 84334, -3048});
		RADAR.put(25380, new int[]{-47412, 51647, -5659});
		RADAR.put(25426, new int[]{-18053, -101274, -1580});
		RADAR.put(25429, new int[]{172122, -214776, -3064});
		
		// lvl30 list
		RADAR.put(25004, new int[]{-94208, 100240, -3520});
		RADAR.put(25020, new int[]{90384, 125568, -2128});
		RADAR.put(25023, new int[]{27280, 101744, -3696});
		RADAR.put(25041, new int[]{10416, 126880, -3676});
		RADAR.put(25063, new int[]{-91024, 116304, -3466});
		RADAR.put(25079, new int[]{53712, 102656, -1072});
		RADAR.put(25082, new int[]{88512, 140576, -3483});
		RADAR.put(25098, new int[]{-5937, 175004, -2940});
		RADAR.put(25112, new int[]{116128, 139392, -3640});
		RADAR.put(25118, new int[]{50896, 146576, -3645});
		RADAR.put(25128, new int[]{17696, 179056, -3520});
		RADAR.put(25152, new int[]{43872, 123968, -2928});
		RADAR.put(25169, new int[]{-54464, 170288, -3136});
		RADAR.put(25170, new int[]{26064, 121808, -3738});
		RADAR.put(25185, new int[]{88123, 166312, -3412});
		RADAR.put(25188, new int[]{88256, 176208, -3488});
		RADAR.put(25189, new int[]{68832, 203024, -3547});
		RADAR.put(25211, new int[]{76352, 193216, -3648});
		RADAR.put(25223, new int[]{43152, 152352, -2848});
		RADAR.put(25352, new int[]{-16912, 174912, -3264});
		RADAR.put(25354, new int[]{-16096, 184288, -3817});
		RADAR.put(25383, new int[]{51632, 153920, -3552});
		RADAR.put(25385, new int[]{53600, 143472, -3872});
		RADAR.put(25388, new int[]{40128, 101920, -1241});
		RADAR.put(25391, new int[]{45600, 120592, -2455});
		RADAR.put(25392, new int[]{29928, 107160, -3708});
		RADAR.put(25394, new int[]{101888, 200224, -3680});
		RADAR.put(25398, new int[]{5000, 189000, -3728});
		RADAR.put(25401, new int[]{117808, 102880, -3600});
		RADAR.put(25404, new int[]{35992, 191312, -3104});
		
		// lvl40 list
		RADAR.put(25007, new int[]{124240, 75376, -2800});
		RADAR.put(25026, new int[]{92976, 7920, -3914});
		RADAR.put(25044, new int[]{107792, 27728, -3488});
		RADAR.put(25047, new int[]{116352, 27648, -3319});
		RADAR.put(25057, new int[]{107056, 168176, -3456});
		RADAR.put(25064, new int[]{92528, 84752, -3703});
		RADAR.put(25085, new int[]{66944, 67504, -3704});
		RADAR.put(25088, new int[]{90848, 16368, -5296});
		RADAR.put(25099, new int[]{64048, 16048, -3536});
		RADAR.put(25102, new int[]{113840, 84256, -2480});
		RADAR.put(25115, new int[]{94000, 197500, -3300});
		RADAR.put(25134, new int[]{87536, 75872, -3591});
		RADAR.put(25155, new int[]{73520, 66912, -3728});
		RADAR.put(25158, new int[]{77104, 5408, -3088});
		RADAR.put(25173, new int[]{75968, 110784, -2512});
		RADAR.put(25192, new int[]{125920, 190208, -3291});
		RADAR.put(25208, new int[]{73776, 201552, -3760});
		RADAR.put(25214, new int[]{112112, 209936, -3616});
		RADAR.put(25260, new int[]{93120, 19440, -3607});
		RADAR.put(25395, new int[]{15000, 119000, -11900});
		RADAR.put(25410, new int[]{72192, 125424, -3657});
		RADAR.put(25412, new int[]{81920, 113136, -3056});
		RADAR.put(25415, new int[]{128352, 138464, -3467});
		RADAR.put(25418, new int[]{62416, 8096, -3376});
		RADAR.put(25420, new int[]{42032, 24128, -4704});
		RADAR.put(25431, new int[]{79648, 18320, -5232});
		RADAR.put(25437, new int[]{67296, 64128, -3723});
		RADAR.put(25438, new int[]{107000, 92000, -2272});
		RADAR.put(25441, new int[]{111440, 82912, -2912});
		RADAR.put(25456, new int[]{133632, 87072, -3623});
		RADAR.put(25487, new int[]{83056, 183232, -3616});
		RADAR.put(25490, new int[]{86528, 216864, -3584});
		RADAR.put(25498, new int[]{126624, 174448, -3056});
		
		// lvl50 list
		RADAR.put(25010, new int[]{113920, 52960, -3735});
		RADAR.put(25013, new int[]{169744, 11920, -2732});
		RADAR.put(25029, new int[]{54941, 206705, -3728});
		RADAR.put(25032, new int[]{88532, 245798, -10376});
		RADAR.put(25050, new int[]{125520, 27216, -3632});
		RADAR.put(25067, new int[]{94992, -23168, -2176});
		RADAR.put(25070, new int[]{125600, 50100, -3600});
		RADAR.put(25089, new int[]{165424, 93776, -2992});
		RADAR.put(25103, new int[]{135872, 94592, -3735});
		RADAR.put(25119, new int[]{121872, 64032, -3536});
		RADAR.put(25122, new int[]{86300, -8200, -3000});
		RADAR.put(25131, new int[]{75488, -9360, -2720});
		RADAR.put(25137, new int[]{125280, 102576, -3305});
		RADAR.put(25159, new int[]{124984, 43200, -3625});
		RADAR.put(25176, new int[]{92544, 115232, -3200});
		RADAR.put(25182, new int[]{41966, 215417, -3728});
		RADAR.put(25217, new int[]{89904, 105712, -3292});
		RADAR.put(25230, new int[]{66672, 46704, -3920});
		RADAR.put(25238, new int[]{155000, 85400, -3200});
		RADAR.put(25241, new int[]{165984, 88048, -2384});
		RADAR.put(25259, new int[]{42050, 208107, -3752});
		RADAR.put(25273, new int[]{23800, 119500, -8976});
		RADAR.put(25277, new int[]{54651, 180269, -4976});
		RADAR.put(25280, new int[]{85622, 88766, -5120});
		RADAR.put(25434, new int[]{104096, -16896, -1803});
		RADAR.put(25460, new int[]{150304, 67776, -3688});
		RADAR.put(25463, new int[]{166288, 68096, -3264});
		RADAR.put(25473, new int[]{175712, 29856, -3776});
		RADAR.put(25475, new int[]{183568, 24560, -3184});
		RADAR.put(25481, new int[]{53517, 205413, -3728});
		RADAR.put(25484, new int[]{43160, 220463, -3680});
		RADAR.put(25493, new int[]{83174, 254428, -10873});
		RADAR.put(25496, new int[]{88300, 258000, -10200});
		
		// lvl60 list
		RADAR.put(25016, new int[]{76787, 245775, -10376});
		RADAR.put(25051, new int[]{117760, -9072, -3264});
		RADAR.put(25073, new int[]{143265, 110044, -3944});
		RADAR.put(25106, new int[]{173880, -11412, -2880});
		RADAR.put(25125, new int[]{170656, 85184, -2000});
		RADAR.put(25140, new int[]{191975, 56959, -7616});
		RADAR.put(25162, new int[]{194107, 53884, -4368});
		RADAR.put(25179, new int[]{181814, 52379, -4344});
		RADAR.put(25226, new int[]{104240, -3664, -3392});
		RADAR.put(25233, new int[]{185800, -26500, -2000});
		RADAR.put(25234, new int[]{120080, 111248, -3047});
		RADAR.put(25255, new int[]{170048, -24896, -3440});
		RADAR.put(25256, new int[]{170320, 42640, -4832});
		RADAR.put(25263, new int[]{144400, -28192, -1920});
		RADAR.put(25322, new int[]{93296, -75104, -1824});
		RADAR.put(25407, new int[]{115072, 112272, -3018});
		RADAR.put(25423, new int[]{113600, 47120, -4640});
		RADAR.put(25444, new int[]{113232, 17456, -4384});
		RADAR.put(25467, new int[]{186192, 61472, -4160});
		RADAR.put(25470, new int[]{186896, 56276, -4576});
		RADAR.put(25478, new int[]{168288, 28368, -3632});
		
		// lvl70 list
		RADAR.put(25035, new int[]{180968, 12035, -2720});
		RADAR.put(25054, new int[]{113432, 16403, 3960});
		RADAR.put(25092, new int[]{116151, 16227, 1944});
		RADAR.put(25109, new int[]{152660, 110387, -5520});
		RADAR.put(25126, new int[]{116263, 15916, 6992});
		RADAR.put(25143, new int[]{113102, 16002, 6992});
		RADAR.put(25163, new int[]{130500, 59098, 3584});
		RADAR.put(25198, new int[]{102656, 157424, -3735});
		RADAR.put(25199, new int[]{108096, 157408, -3688});
		RADAR.put(25202, new int[]{119760, 157392, -3744});
		RADAR.put(25205, new int[]{123808, 153408, -3671});
		RADAR.put(25220, new int[]{113551, 17083, -2120});
		RADAR.put(25229, new int[]{137568, -19488, -3552});
		RADAR.put(25235, new int[]{116400, -62528, -3264});
		RADAR.put(25244, new int[]{187360, 45840, -5856});
		RADAR.put(25245, new int[]{172000, 55000, -5400});
		RADAR.put(25248, new int[]{127903, -13399, -3720});
		RADAR.put(25249, new int[]{147104, -20560, -3377});
		RADAR.put(25252, new int[]{192376, 22087, -3608});
		RADAR.put(25266, new int[]{188983, 13647, -2672});
		RADAR.put(25269, new int[]{123504, -23696, -3481});
		RADAR.put(25276, new int[]{154088, -14116, -3736});
		RADAR.put(25281, new int[]{151053, 88124, -5424});
		RADAR.put(25282, new int[]{179311, -7632, -4896});
		RADAR.put(25293, new int[]{134672, -115600, -1216});
		RADAR.put(25325, new int[]{91008, -85904, -2736});
		RADAR.put(25328, new int[]{59331, -42403, -3003});
		RADAR.put(25447, new int[]{113200, 17552, -1424});
		RADAR.put(25450, new int[]{113600, 15104, 9559});
		RADAR.put(25453, new int[]{156704, -6096, -4185});
		RADAR.put(25524, new int[]{144143, -5731, -4722});
		
		// lvl80 list
		RADAR.put(25299, new int[]{148154, -73782, -4364});
		RADAR.put(25302, new int[]{145553, -81651, -5464});
		RADAR.put(25305, new int[]{144997, -84948, -5712});
		RADAR.put(25309, new int[]{115537, -39046, -1940});
		RADAR.put(25312, new int[]{109296, -36103, -648});
		RADAR.put(25315, new int[]{105654, -42995, -1240});
		RADAR.put(25319, new int[]{185700, -106066, -6184});
		RADAR.put(25514, new int[]{79635, -55612, -5980});
		RADAR.put(25517, new int[]{112793, -76080, 286});
		RADAR.put(29062, new int[]{-16373, -53562, -10197});
		RADAR.put(25283, new int[]{185060, -9622, -5104});
		RADAR.put(25286, new int[]{185065, -12612, -5104});
		RADAR.put(25306, new int[]{142368, -82512, -6487});
		RADAR.put(25316, new int[]{105452, -36775, -1050});
		RADAR.put(25527, new int[]{3776, -6768, -3276});
		RADAR.put(29065, new int[]{26528, -8244, -2007});
	}
	
	public RaidbossInfo()
	{
		super(8000, "8000_RaidbossInfo", "custom");
		
		var created = new State("Start", this);
		setInitialState(created);
		
		for (int npcId = 31729; npcId <= 31841; npcId++)
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
			var rbId = Integer.parseInt(event);
			var loc = RADAR.get(rbId);
			if (loc != null)
			{
				player.sendPacket(new RadarControl(2, 2, loc[0], loc[1], loc[2]));
				player.sendPacket(new RadarControl(0, 1, loc[0], loc[1], loc[2]));
			}
			st.exitQuest(true);
			return null;
		}
		
		return event;
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		return "info.htm";
	}


	public static void main(String[] args)
	{
		new RaidbossInfo();
	}
}
