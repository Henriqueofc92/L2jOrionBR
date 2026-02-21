package l2jorion.game.data.village_master;

import java.util.Map;
import java.util.Set;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;

/**
 * Elven/Human Mystics 1st Class Change.<br>
 * NPCs: Sylvain(30070), Raymond(30289), Levian(30037).
 */
public class ElvenHumanMystics1 extends Quest
{
	private static final int MARK_OF_FAITH = 1201;
	private static final int ETERNITY_DIAMOND = 1230;
	private static final int LEAF_OF_ORACLE = 1235;
	private static final int BEAD_OF_SEASON = 1292;
	private static final int SHADOW_WEAPON_COUPON_DGRADE = 8869;
	
	private static final Set<Integer> NPCS = Set.of(30070, 30289, 30037);
	
	private record ClassData(int newClass, int reqClass, int reqRace, String lowNi, String lowI, String okNi, String okI, int reqItem) {}
	
	private static final Map<String, ClassData> CLASSES = Map.of(
		"EW", new ClassData(26, 25, 1, "15", "16", "17", "18", ETERNITY_DIAMOND),
		"EO", new ClassData(29, 25, 1, "19", "20", "21", "22", LEAF_OF_ORACLE),
		"HW", new ClassData(11, 10, 0, "23", "24", "25", "26", BEAD_OF_SEASON),
		"HC", new ClassData(15, 10, 0, "27", "28", "29", "30", MARK_OF_FAITH)
	);
	
	public ElvenHumanMystics1()
	{
		super(99998, "elven_human_mystics_1", "village_master");
		
		var created = new State("Start", this);
		setInitialState(created);
		
		for (var npc : NPCS) { addStartNpc(npc); addTalkId(npc); }
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		int npcId = npc.getNpcId();
		var st = player.getQuestState(getName());
		if (st == null || !NPCS.contains(npcId))
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
		
		String suffix = "";
		if (race == data.reqRace && classid == data.reqClass)
		{
			long item = st.getQuestItemsCount(data.reqItem);
			if (level < 20)
			{
				suffix = item > 0 ? "-" + data.lowI + ".htm" : "-" + data.lowNi + ".htm";
			}
			else
			{
				if (item == 0)
				{
					suffix = "-" + data.okNi + ".htm";
				}
				else
				{
					suffix = "-" + data.okI + ".htm";
					st.giveItems(SHADOW_WEAPON_COUPON_DGRADE, 15);
					st.takeItems(data.reqItem, 1);
					st.playSound("ItemSound.quest_fanfare_2");
					player.setClassId(data.newClass);
					player.setBaseClass(data.newClass);
					player.broadcastUserInfo();
				}
			}
		}
		st.exitQuest(true);
		return npcId + suffix;
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
		var classId = player.getClassId();
		int id = classId.getId();
		
		if (player.isSubClassActive())
		{
			st.exitQuest(true);
			return "No Quest";
		}
		
		if (NPCS.contains(npcId))
		{
			String htmltext = String.valueOf(npcId);
			if (race == 0 || race == 1)
			{
				if (!classId.isMage()) return htmltext + "-33.htm";
				if (classId.level() == 1) return htmltext + "-31.htm";
				if (classId.level() >= 2) return htmltext + "-32.htm";
				if (id == 0x19) return htmltext + "-01.htm";
				if (id == 0x0a) return htmltext + "-08.htm";
			}
			else
			{
				htmltext += "-33.htm";
			}
			st.exitQuest(true);
			return htmltext;
		}
		st.exitQuest(true);
		return "No Quest";
	}


	public static void main(String[] args)
	{
		new ElvenHumanMystics1();
	}
}
