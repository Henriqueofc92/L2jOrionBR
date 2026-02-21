package l2jorion.game.data.village_master;

import java.util.Map;
import java.util.Set;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.QuestState;
import l2jorion.game.model.quest.State;

/**
 * Orc 2nd Class Change.<br>
 * NPCs: Penatus(30513), Karia(30681), Garvarentz(30704), Ladanza(30865),
 * Tushku(30913), Aklan(31288), Lambac(31326), Shaka(31977).
 */
public class OrcOccupationChange2 extends Quest
{
	private static final int MARK_OF_CHALLENGER = 2627;
	private static final int MARK_OF_PILGRIM = 2721;
	private static final int MARK_OF_DUELIST = 2762;
	private static final int MARK_OF_WARSPIRIT = 2879;
	private static final int MARK_OF_GLORY = 3203;
	private static final int MARK_OF_CHAMPION = 3276;
	private static final int MARK_OF_LORD = 3390;
	private static final int SHADOW_WEAPON_COUPON_CGRADE = 8870;
	
	private static final Set<Integer> NPCS = Set.of(30513, 30681, 30704, 30865, 30913, 31288, 31326, 31977);
	
	private record ClassData(int newClass, int reqClass, int reqRace, String lowNi, String lowI, String okNi, String okI, int[] reqItems) {}
	
	private static final Map<String, ClassData> CLASSES = Map.of(
		"TY", new ClassData(48, 47, 3, "16", "17", "18", "19", new int[]{ MARK_OF_CHALLENGER, MARK_OF_GLORY, MARK_OF_DUELIST }),
		"DE", new ClassData(46, 45, 3, "20", "21", "22", "23", new int[]{ MARK_OF_CHALLENGER, MARK_OF_GLORY, MARK_OF_CHAMPION }),
		"OL", new ClassData(51, 50, 3, "24", "25", "26", "27", new int[]{ MARK_OF_PILGRIM, MARK_OF_GLORY, MARK_OF_LORD }),
		"WC", new ClassData(52, 50, 3, "28", "29", "30", "31", new int[]{ MARK_OF_PILGRIM, MARK_OF_GLORY, MARK_OF_WARSPIRIT })
	);
	
	public OrcOccupationChange2()
	{
		super(99993, "orc_occupation_change_2", "village_master");
		
		var created = new State("Start", this);
		setInitialState(created);
		
		for (var npc : NPCS) { addStartNpc(npc); addTalkId(npc); }
	}
	
	private boolean hasAllItems(QuestState st, int[] items)
	{
		for (var item : items)
		{
			if (st.getQuestItemsCount(item) == 0) return false;
		}
		return true;
	}
	
	private void doClassChange(QuestState st, L2PcInstance player, int newClass, int[] items)
	{
		for (var item : items) st.takeItems(item, 1);
		st.playSound("ItemSound.quest_fanfare_2");
		player.setClassId(newClass);
		player.setBaseClass(newClass);
		player.broadcastUserInfo();
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		var st = player.getQuestState(getName());
		if (st == null || !NPCS.contains(npc.getNpcId()))
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
			boolean hasItems = hasAllItems(st, data.reqItems);
			if (level < 40)
			{
				suffix = hasItems ? data.lowI : data.lowNi;
			}
			else
			{
				if (!hasItems)
				{
					suffix = data.okNi;
				}
				else
				{
					suffix = data.okI;
					st.giveItems(SHADOW_WEAPON_COUPON_CGRADE, 15);
					doClassChange(st, player, data.newClass, data.reqItems);
				}
			}
		}
		st.exitQuest(true);
		return "30513-" + suffix + ".htm";
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		var st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		int race = player.getRace().ordinal();
		var classId = player.getClassId();
		int id = classId.getId();
		
		if (player.isSubClassActive())
		{
			st.exitQuest(true);
			return "No Quest";
		}
		
		if (NPCS.contains(npc.getNpcId()))
		{
			String htmltext = "30513";
			if (race == 3)
			{
				if (id == 47) return htmltext + "-01.htm";
				if (id == 45) return htmltext + "-05.htm";
				if (id == 50) return htmltext + "-09.htm";
				if (classId.level() == 0) { st.exitQuest(true); return htmltext + "-33.htm"; }
				if (classId.level() >= 2) { st.exitQuest(true); return htmltext + "-32.htm"; }
			}
			st.exitQuest(true);
			return htmltext + "-34.htm";
		}
		st.exitQuest(true);
		return "No Quest";
	}


	public static void main(String[] args)
	{
		new OrcOccupationChange2();
	}
}
