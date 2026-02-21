package l2jorion.game.data.village_master;

import java.util.Map;
import java.util.Set;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;

/**
 * Elven/Human Fighters 1st Class Change.<br>
 * NPCs: Pabris(30066), Rains(30288), Ramos(30373).
 */
public class ElvenHumanFighters1 extends Quest
{
	private static final int MEDALLION_OF_WARRIOR = 1145;
	private static final int SWORD_OF_RITUAL = 1161;
	private static final int BEZIQUES_RECOMMENDATION = 1190;
	private static final int ELVEN_KNIGHT_BROOCH = 1204;
	private static final int REORIA_RECOMMENDATION = 1217;
	private static final int SHADOW_WEAPON_COUPON_DGRADE = 8869;
	
	private static final Set<Integer> NPCS = Set.of(30066, 30288, 30373);
	
	private record ClassData(int newClass, int reqClass, int reqRace, String lowNi, String lowI, String okNi, String okI, int reqItem) {}
	
	private static final Map<String, ClassData> CLASSES = Map.of(
		"EK", new ClassData(19, 18, 1, "18", "19", "20", "21", ELVEN_KNIGHT_BROOCH),
		"ES", new ClassData(22, 18, 1, "22", "23", "24", "25", REORIA_RECOMMENDATION),
		"HW", new ClassData(1, 0, 0, "26", "27", "28", "29", MEDALLION_OF_WARRIOR),
		"HK", new ClassData(4, 0, 0, "30", "31", "32", "33", SWORD_OF_RITUAL),
		"HR", new ClassData(7, 0, 0, "34", "35", "36", "37", BEZIQUES_RECOMMENDATION)
	);
	
	public ElvenHumanFighters1()
	{
		super(99995, "elven_human_fighters_1", "village_master");
		
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
				if (classId.level() == 1) return htmltext + "-38.htm";
				if (classId.level() >= 2) return htmltext + "-39.htm";
				if (id == 18) return htmltext + "-01.htm";
				if (id == 0) return htmltext + "-08.htm";
				htmltext += "-40.htm";
			}
			else
			{
				htmltext += "-40.htm";
			}
			st.exitQuest(true);
			return htmltext;
		}
		st.exitQuest(true);
		return "No Quest";
	}


	public static void main(String[] args)
	{
		new ElvenHumanFighters1();
	}
}
