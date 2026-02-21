package l2jorion.game.data.village_master;

import java.util.Map;
import java.util.Set;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.QuestState;
import l2jorion.game.model.quest.State;

/**
 * Elven/Human Fighters 2nd Class Change.<br>
 * NPCs: Hannavalt(30109), Blackbird(30187), Siria(30689), Sedrick(30849),
 * Marcus(30900), Hector(31965), Schule(32094).
 */
public class ElvenHumanFighters2 extends Quest
{
	private static final int MARK_OF_CHALLENGER = 2627;
	private static final int MARK_OF_DUTY = 2633;
	private static final int MARK_OF_SEEKER = 2673;
	private static final int MARK_OF_TRUST = 2734;
	private static final int MARK_OF_DUELIST = 2762;
	private static final int MARK_OF_SEARCHER = 2809;
	private static final int MARK_OF_HEALER = 2820;
	private static final int MARK_OF_LIFE = 3140;
	private static final int MARK_OF_CHAMPION = 3276;
	private static final int MARK_OF_SAGITTARIUS = 3293;
	private static final int MARK_OF_WITCHCRAFT = 3307;
	private static final int SHADOW_WEAPON_COUPON_CGRADE = 8870;
	
	private static final Set<Integer> NPCS = Set.of(30109, 30187, 30689, 30849, 30900, 31965, 32094);
	
	private record ClassData(int newClass, int reqClass, int reqRace, String lowNi, String lowI, String okNi, String okI, int[] reqItems) {}
	
	private static final Map<String, ClassData> CLASSES = Map.ofEntries(
		Map.entry("TK", new ClassData(20, 19, 1, "36", "37", "38", "39", new int[]{ MARK_OF_DUTY, MARK_OF_LIFE, MARK_OF_HEALER })),
		Map.entry("SS", new ClassData(21, 19, 1, "40", "41", "42", "43", new int[]{ MARK_OF_CHALLENGER, MARK_OF_LIFE, MARK_OF_DUELIST })),
		Map.entry("PL", new ClassData(5, 4, 0, "44", "45", "46", "47", new int[]{ MARK_OF_DUTY, MARK_OF_TRUST, MARK_OF_HEALER })),
		Map.entry("DA", new ClassData(6, 4, 0, "48", "49", "50", "51", new int[]{ MARK_OF_DUTY, MARK_OF_TRUST, MARK_OF_WITCHCRAFT })),
		Map.entry("TH", new ClassData(8, 7, 0, "52", "53", "54", "55", new int[]{ MARK_OF_SEEKER, MARK_OF_TRUST, MARK_OF_SEARCHER })),
		Map.entry("HE", new ClassData(9, 7, 0, "56", "57", "58", "59", new int[]{ MARK_OF_SEEKER, MARK_OF_TRUST, MARK_OF_SAGITTARIUS })),
		Map.entry("PW", new ClassData(23, 22, 1, "60", "61", "62", "63", new int[]{ MARK_OF_SEEKER, MARK_OF_LIFE, MARK_OF_SEARCHER })),
		Map.entry("SR", new ClassData(24, 22, 1, "64", "65", "66", "67", new int[]{ MARK_OF_SEEKER, MARK_OF_LIFE, MARK_OF_SAGITTARIUS })),
		Map.entry("GL", new ClassData(2, 1, 0, "68", "69", "70", "71", new int[]{ MARK_OF_CHALLENGER, MARK_OF_TRUST, MARK_OF_DUELIST })),
		Map.entry("WL", new ClassData(3, 1, 0, "72", "73", "74", "75", new int[]{ MARK_OF_CHALLENGER, MARK_OF_TRUST, MARK_OF_CHAMPION }))
	);
	
	public ElvenHumanFighters2()
	{
		super(99991, "elven_human_fighters_2", "village_master");
		
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
		st.giveItems(SHADOW_WEAPON_COUPON_CGRADE, 15);
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
					doClassChange(st, player, data.newClass, data.reqItems);
				}
			}
		}
		st.exitQuest(true);
		return "30109-" + suffix + ".htm";
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
			String htmltext = "30109";
			if (race == 0 || race == 1)
			{
				if (id == 19) return htmltext + "-01.htm";
				if (id == 4) return htmltext + "-08.htm";
				if (id == 7) return htmltext + "-15.htm";
				if (id == 22) return htmltext + "-22.htm";
				if (id == 1) return htmltext + "-29.htm";
				if (classId.level() == 0) { st.exitQuest(true); return htmltext + "-76.htm"; }
				if (classId.level() >= 2) { st.exitQuest(true); return htmltext + "-77.htm"; }
				st.exitQuest(true);
				return htmltext + "-78.htm";
			}
			st.exitQuest(true);
			return htmltext + "-78.htm";
		}
		st.exitQuest(true);
		return "No Quest";
	}


	public static void main(String[] args)
	{
		new ElvenHumanFighters2();
	}
}
