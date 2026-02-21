package l2jorion.game.data.village_master;

import java.util.Map;
import java.util.Set;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.QuestState;
import l2jorion.game.model.quest.State;

/**
 * Dark Elven 2nd Class Change.<br>
 * NPCs: Innocentin(31328), Brecson(30195), Medown(30699), Angus(30474),
 * Andromeda(31324), Oltlin(30862), Xairakin(30910), Samael(31285),
 * Valdis(31331), Tifaren(31334), Drizzit(31974), Helminter(32096).
 */
public class DarkElvenChange2 extends Quest
{
	// Marks
	private static final int MARK_OF_CHALLENGER = 2627;
	private static final int MARK_OF_DUTY = 2633;
	private static final int MARK_OF_SEEKER = 2673;
	private static final int MARK_OF_SCHOLAR = 2674;
	private static final int MARK_OF_PILGRIM = 2721;
	private static final int MARK_OF_DUELIST = 2762;
	private static final int MARK_OF_SEARCHER = 2809;
	private static final int MARK_OF_REFORMER = 2821;
	private static final int MARK_OF_MAGUS = 2840;
	private static final int MARK_OF_FATE = 3172;
	private static final int MARK_OF_SAGITTARIUS = 3293;
	private static final int MARK_OF_WITCHCRAFT = 3307;
	private static final int MARK_OF_SUMMONER = 3336;
	private static final int SHADOW_WEAPON_COUPON_CGRADE = 8870;
	
	private static final Set<Integer> NPCS = Set.of(31328, 30195, 30699, 30474, 31324, 30862, 30910, 31285, 31331, 31334, 31974, 32096);
	
	private record ClassData(int newClass, int reqClass, int reqRace, String lowNi, String lowI, String okNi, String okI, int[] reqItems) {}
	
	private static final Map<String, ClassData> CLASSES = Map.of(
		"SK", new ClassData(33, 32, 2, "26", "27", "28", "29", new int[]{ MARK_OF_DUTY, MARK_OF_FATE, MARK_OF_WITCHCRAFT }),
		"BD", new ClassData(34, 32, 2, "30", "31", "32", "33", new int[]{ MARK_OF_CHALLENGER, MARK_OF_FATE, MARK_OF_DUELIST }),
		"SE", new ClassData(43, 42, 2, "34", "35", "36", "37", new int[]{ MARK_OF_PILGRIM, MARK_OF_FATE, MARK_OF_REFORMER }),
		"AW", new ClassData(36, 35, 2, "38", "39", "40", "41", new int[]{ MARK_OF_SEEKER, MARK_OF_FATE, MARK_OF_SEARCHER }),
		"PR", new ClassData(37, 35, 2, "42", "43", "44", "45", new int[]{ MARK_OF_SEEKER, MARK_OF_FATE, MARK_OF_SAGITTARIUS }),
		"SH", new ClassData(40, 39, 2, "46", "47", "48", "49", new int[]{ MARK_OF_SCHOLAR, MARK_OF_FATE, MARK_OF_MAGUS }),
		"PS", new ClassData(41, 39, 2, "50", "51", "52", "53", new int[]{ MARK_OF_SCHOLAR, MARK_OF_FATE, MARK_OF_SUMMONER })
	);
	
	public DarkElvenChange2()
	{
		super(99993, "dark_elven_change_2", "village_master");
		
		var created = new State("Start", this);
		setInitialState(created);
		
		for (var npcId : NPCS)
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}
	}
	
	private boolean hasAllItems(QuestState st, int[] items)
	{
		for (var item : items)
		{
			if (st.getQuestItemsCount(item) == 0)
			{
				return false;
			}
		}
		return true;
	}
	
	private void doClassChange(QuestState st, L2PcInstance player, int newClass, int[] items)
	{
		for (var item : items)
		{
			st.takeItems(item, 1);
		}
		st.playSound("ItemSound.quest_fanfare_2");
		player.setClassId(newClass);
		player.setBaseClass(newClass);
		player.broadcastUserInfo();
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
		return "30474-" + suffix + ".htm";
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
			String htmltext = "30474";
			if (race == 2)
			{
				if (id == 32) return htmltext + "-01.htm";
				if (id == 42) return htmltext + "-08.htm";
				if (id == 35) return htmltext + "-12.htm";
				if (id == 39) return htmltext + "-19.htm";
				if (classId.level() == 0) return htmltext + "-55.htm";
				if (classId.level() >= 2) return htmltext + "-54.htm";
				return htmltext + "-56.htm";
			}
			st.exitQuest(true);
			return htmltext + "-56.htm";
		}
		st.exitQuest(true);
		return "No Quest";
	}


	public static void main(String[] args)
	{
		new DarkElvenChange2();
	}
}
