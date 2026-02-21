package l2jorion.game.data.village_master;

import java.util.Map;
import java.util.Set;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;

/**
 * Dwarven Occupation Change.<br>
 * Handles Scavenger, Artisan, Bounty Hunter, and Warsmith class changes.
 */
public class DwarvenOccupationChange extends Quest
{
	// Marks
	private static final int MARK_OF_SEARCHER = 2809;
	private static final int MARK_OF_GUILDSMAN = 3119;
	private static final int MARK_OF_PROSPERITY = 3238;
	private static final int MARK_OF_MAESTRO = 2867;
	private static final int RING_OF_RAVEN = 1642;
	private static final int FINAL_PASS = 1635;
	
	private static final int SHADOW_WEAPON_COUPON_DGRADE = 8869;
	private static final int SHADOW_WEAPON_COUPON_CGRADE = 8870;
	
	// NPC groups
	private static final Set<Integer> BH_NPCS = Set.of(30511, 30676, 30685, 30845, 30894, 31269, 31314, 31958);
	private static final Set<Integer> WS_NPCS = Set.of(30512, 30677, 30687, 30847, 30897, 31272, 31317, 31961);
	private static final Set<Integer> SCAV_NPCS = Set.of(30503, 30594, 30498, 32092, 32093);
	private static final Set<Integer> ARTI_NPCS = Set.of(30504, 30595, 30499);
	
	private static final Set<Integer> UNIQUE_DIALOGS = Set.of(30594, 30595, 30498, 30499);
	
	private record ClassData(String defaultPrefix, int intendedRace, int[] requiredClass, int[] denial1, int[] denial2, int[] requiredMarks, int requiredLevel, int newClass, int reward) {}
	
	private static final Map<String, ClassData> CLASSES = Map.of(
		"BH", new ClassData("30511-", 4, new int[]{0x36}, new int[]{0x35}, new int[]{0x37, 0x39, 0x75, 0x76}, new int[]{MARK_OF_SEARCHER, MARK_OF_GUILDSMAN, MARK_OF_PROSPERITY}, 40, 0x37, SHADOW_WEAPON_COUPON_CGRADE),
		"WS", new ClassData("30512-", 4, new int[]{0x38}, new int[]{0x35}, new int[]{0x37, 0x39, 0x75, 0x76}, new int[]{MARK_OF_MAESTRO, MARK_OF_GUILDSMAN, MARK_OF_PROSPERITY}, 40, 0x39, SHADOW_WEAPON_COUPON_CGRADE),
		"SC", new ClassData("30503-", 4, new int[]{0x35}, new int[]{0x36, 0x38}, new int[]{0x37, 0x39, 0x75, 0x76}, new int[]{RING_OF_RAVEN}, 20, 0x36, SHADOW_WEAPON_COUPON_DGRADE),
		"AR", new ClassData("30504-", 4, new int[]{0x35}, new int[]{0x36, 0x38}, new int[]{0x37, 0x39, 0x75, 0x76}, new int[]{FINAL_PASS}, 20, 0x38, SHADOW_WEAPON_COUPON_DGRADE)
	);
	
	private static boolean contains(int[] arr, int val)
	{
		for (var v : arr)
		{
			if (v == val) return true;
		}
		return false;
	}
	
	public DwarvenOccupationChange()
	{
		super(99999, "dwarven_occupation_change", "village_master");
		
		var created = new State("Start", this);
		setInitialState(created);
		
		for (var npc : SCAV_NPCS) { addStartNpc(npc); addTalkId(npc); }
		for (var npc : ARTI_NPCS) { addStartNpc(npc); addTalkId(npc); }
		for (var npc : BH_NPCS) { addStartNpc(npc); addTalkId(npc); }
		for (var npc : WS_NPCS) { addStartNpc(npc); addTalkId(npc); }
	}
	
	private String getKey(int npcId)
	{
		if (BH_NPCS.contains(npcId)) return "BH";
		if (WS_NPCS.contains(npcId)) return "WS";
		if (SCAV_NPCS.contains(npcId)) return "SC";
		if (ARTI_NPCS.contains(npcId)) return "AR";
		return null;
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		int npcId = npc.getNpcId();
		var st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		if (!CLASSES.containsKey(event))
		{
			return event;
		}
		
		int race = player.getRace().ordinal();
		int classid = player.getClassId().getId();
		int level = player.getLevel();
		var data = CLASSES.get(event);
		
		String prefix = UNIQUE_DIALOGS.contains(npcId) ? npcId + "-" : data.defaultPrefix;
		
		if (contains(data.requiredClass, classid) && race == data.intendedRace)
		{
			int marksCount = 0;
			for (var item : data.requiredMarks)
			{
				if (st.getQuestItemsCount(item) > 0
				)
				{
					marksCount++;
				}
			}
			
			if (level < data.requiredLevel)
			{
				return prefix + (marksCount < data.requiredMarks.length ? "05.htm" : "06.htm");
			}
			
			if (marksCount < data.requiredMarks.length)
			{
				return prefix + "07.htm";
			}
			
			for (var item : data.requiredMarks)
			{
				st.takeItems(item, 1);
			}
			if (data.reward > 0)
			{
				st.giveItems(data.reward, 15);
			}
			player.setClassId(data.newClass);
			player.setBaseClass(data.newClass);
			player.broadcastUserInfo();
			st.playSound("ItemSound.quest_fanfare_2");
			st.exitQuest(true);
			return prefix + "08.htm";
		}
		
		st.exitQuest(true);
		return "No Quest";
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		var st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		int npcId = npc.getNpcId();
		int race = player.getRace().ordinal();
		int classid = player.getClassId().getId();
		
		if (player.isSubClassActive())
		{
			st.exitQuest(true);
			return "No Quest";
		}
		
		String key = getKey(npcId);
		if (key == null)
		{
			return "No Quest";
		}
		
		var data = CLASSES.get(key);
		String prefix = UNIQUE_DIALOGS.contains(npcId) ? npcId + "-" : data.defaultPrefix;
		
		if (race != data.intendedRace)
		{
			st.exitQuest(true);
			return prefix + "11.htm";
		}
		
		if (contains(data.requiredClass, classid))
		{
			return prefix + "01.htm";
		}
		if (contains(data.denial1, classid))
		{
			st.exitQuest(true);
			return prefix + "09.htm";
		}
		if (contains(data.denial2, classid))
		{
			st.exitQuest(true);
			return prefix + "10.htm";
		}
		
		st.exitQuest(true);
		return "No Quest";
	}


	public static void main(String[] args)
	{
		new DwarvenOccupationChange();
	}
}
