package l2jorion.game.data.teleports;

import java.util.Map;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;

/**
 * Hunting Grounds Teleport (2211).<br>
 * Dawn/Dusk hunting ground teleporters per city.
 */
public class HuntingGroundsTeleport extends Quest
{
	// Dawn NPCs (31078-31084)
	private static final int GLUDIN_DAWN = 31078;
	private static final int GLUDIO_DAWN = 31079;
	private static final int DION_DAWN = 31080;
	private static final int GIRAN_DAWN = 31081;
	private static final int HEINE_DAWN = 31082;
	private static final int OREN_DAWN = 31083;
	private static final int ADEN_DAWN = 31084;
	
	// Dusk NPCs (31085-31091)
	private static final int GLUDIN_DUSK = 31085;
	private static final int GLUDIO_DUSK = 31086;
	private static final int DION_DUSK = 31087;
	private static final int GIRAN_DUSK = 31088;
	private static final int HEINE_DUSK = 31089;
	private static final int OREN_DUSK = 31090;
	private static final int ADEN_DUSK = 31091;
	
	// Hunter's Village
	private static final int HW_DAWN = 31168;
	private static final int HW_DUSK = 31169;
	
	// Goddard / Rune
	private static final int GODDARD_DAWN = 31692;
	private static final int GODDARD_DUSK = 31693;
	private static final int RUNE_DAWN = 31694;
	private static final int RUNE_DUSK = 31695;
	
	// Schuttgart
	private static final int SCHUTTGART_DAWN = 31997;
	private static final int SCHUTTGART_DUSK = 31998;
	
	// NPC â†’ HTM mapping
	private static final Map<Integer, String> HTM_MAP = Map.ofEntries(
		Map.entry(GLUDIN_DAWN, "hg_gludin.htm"),
		Map.entry(GLUDIN_DUSK, "hg_gludin.htm"),
		Map.entry(GLUDIO_DAWN, "hg_gludio.htm"),
		Map.entry(GLUDIO_DUSK, "hg_gludio.htm"),
		Map.entry(DION_DAWN, "hg_dion.htm"),
		Map.entry(DION_DUSK, "hg_dion.htm"),
		Map.entry(GIRAN_DAWN, "hg_giran.htm"),
		Map.entry(GIRAN_DUSK, "hg_giran.htm"),
		Map.entry(HEINE_DAWN, "hg_heine.htm"),
		Map.entry(HEINE_DUSK, "hg_heine.htm"),
		Map.entry(OREN_DAWN, "hg_oren.htm"),
		Map.entry(OREN_DUSK, "hg_oren.htm"),
		Map.entry(ADEN_DAWN, "hg_aden.htm"),
		Map.entry(ADEN_DUSK, "hg_aden.htm"),
		Map.entry(HW_DAWN, "hg_hw.htm"),
		Map.entry(HW_DUSK, "hg_hw.htm"),
		Map.entry(GODDARD_DAWN, "hg_goddard.htm"),
		Map.entry(GODDARD_DUSK, "hg_goddard.htm"),
		Map.entry(RUNE_DAWN, "hg_rune.htm"),
		Map.entry(RUNE_DUSK, "hg_rune.htm"),
		Map.entry(SCHUTTGART_DAWN, "hg_schuttgart.htm"),
		Map.entry(SCHUTTGART_DUSK, "hg_schuttgart.htm")
	);
	
	public HuntingGroundsTeleport()
	{
		super(2211, "2211_HuntingGroundsTeleport", "Teleports");
		
		var created = new State("Start", this);
		new State("Started", this);
		setInitialState(created);
		
		for (var npcId : HTM_MAP.keySet())
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		return HTM_MAP.getOrDefault(npc.getNpcId(), "hg_wrong.htm");
	}


	public static void main(String[] args)
	{
		new HuntingGroundsTeleport();
	}
}
