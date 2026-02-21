package l2jorion.game.data.village_master;

import java.util.Map;
import java.util.Set;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;

/**
 * Dark Elven 1st Class Change.<br>
 * NPCs: Xenos (30290), Tobias (30297), Tronix (30462).
 */
public class DarkElvenChange1 extends Quest
{
	// Items
	private static final int GAZE_OF_ABYSS = 1244;
	private static final int IRON_HEART = 1252;
	private static final int JEWEL_OF_DARKNESS = 1261;
	private static final int ORB_OF_ABYSS = 1270;
	private static final int SHADOW_WEAPON_COUPON_DGRADE = 8869;
	
	private static final Set<Integer> NPCS = Set.of(30290, 30297, 30462);
	
	// [newclass, req_class, req_race, low_ni, low_i, ok_ni, ok_i, req_item]
	private record ClassData(int newClass, int reqClass, int reqRace, String lowNi, String lowI, String okNi, String okI, int reqItem) {}
	
	private static final Map<String, ClassData> CLASSES = Map.of(
		"PK", new ClassData(32, 31, 2, "15", "16", "17", "18", GAZE_OF_ABYSS),
		"AS", new ClassData(35, 31, 2, "19", "20", "21", "22", IRON_HEART),
		"DW", new ClassData(39, 38, 2, "23", "24", "25", "26", JEWEL_OF_DARKNESS),
		"SO", new ClassData(42, 38, 2, "27", "28", "29", "30", ORB_OF_ABYSS)
	);
	
	public DarkElvenChange1()
	{
		super(99997, "dark_elven_change_1", "village_master");
		
		var created = new State("Start", this);
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
			if (race == 2)
			{
				if (classId.level() == 1)
				{
					return htmltext + "-32.htm";
				}
				if (classId.level() >= 2)
				{
					return htmltext + "-31.htm";
				}
				if (id == 31)
				{
					return htmltext + "-01.htm";
				}
				if (id == 38)
				{
					return htmltext + "-08.htm";
				}
			}
			htmltext += "-33.htm";
			st.exitQuest(true);
			return htmltext;
		}
		st.exitQuest(true);
		return "No Quest";
	}


	public static void main(String[] args)
	{
		new DarkElvenChange1();
	}
}
