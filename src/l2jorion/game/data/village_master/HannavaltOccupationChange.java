package l2jorion.game.data.village_master;

import java.util.Map;
import java.util.Set;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.base.ClassId;
import l2jorion.game.model.base.Race;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.QuestState;
import l2jorion.game.model.quest.State;

/**
 * Grand Master Hannavalt - 2nd occupation change (old style) (30109).<br>
 * NPCs: Hannavalt(30109), Blackbird(30187), Siria(30689), Sedrick(30849),
 * Marcus(30900), Hector(31965), Schule(32094).
 */
public class HannavaltOccupationChange extends Quest
{
	// Marks
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
	
	// All the HTM events that just return themselves
	private static final Set<String> HTM_EVENTS = Set.of(
		"30109-01.htm", "30109-02.htm", "30109-03.htm", "30109-04.htm", "30109-05.htm",
		"30109-06.htm", "30109-07.htm", "30109-08.htm", "30109-09.htm", "30109-10.htm",
		"30109-11.htm", "30109-12.htm", "30109-13.htm", "30109-14.htm", "30109-15.htm",
		"30109-16.htm", "30109-17.htm", "30109-18.htm", "30109-19.htm", "30109-20.htm",
		"30109-21.htm", "30109-22.htm", "30109-23.htm", "30109-24.htm", "30109-25.htm",
		"30109-26.htm", "30109-27.htm", "30109-28.htm", "30109-29.htm", "30109-30.htm",
		"30109-31.htm", "30109-32.htm", "30109-33.htm", "30109-34.htm", "30109-35.htm"
	);
	
	// Class change data: reqClassId -> [newClass, mark1, mark2, mark3, low_noItem, low_hasItem, ok_noItem, ok_hasItem]
	private record ChangeData(ClassId reqClass, int newClass, int[] marks, String lowNo, String lowYes, String okNo, String okYes) {}
	
	private static final Map<String, ChangeData> CHANGES = Map.ofEntries(
		Map.entry("class_change_20", new ChangeData(ClassId.elvenKnight, 20, new int[]{ MARK_OF_DUTY, MARK_OF_LIFE, MARK_OF_HEALER }, "30109-36.htm", "30109-37.htm", "30109-38.htm", "30109-39.htm")),
		Map.entry("class_change_21", new ChangeData(ClassId.elvenKnight, 21, new int[]{ MARK_OF_CHALLENGER, MARK_OF_LIFE, MARK_OF_DUELIST }, "30109-40.htm", "30109-41.htm", "30109-42.htm", "30109-43.htm")),
		Map.entry("class_change_5",  new ChangeData(ClassId.knight, 5, new int[]{ MARK_OF_DUTY, MARK_OF_TRUST, MARK_OF_HEALER }, "30109-44.htm", "30109-45.htm", "30109-46.htm", "30109-47.htm")),
		Map.entry("class_change_6",  new ChangeData(ClassId.knight, 6, new int[]{ MARK_OF_DUTY, MARK_OF_TRUST, MARK_OF_WITCHCRAFT }, "30109-48.htm", "30109-49.htm", "30109-50.htm", "30109-51.htm")),
		Map.entry("class_change_8",  new ChangeData(ClassId.rogue, 8, new int[]{ MARK_OF_SEEKER, MARK_OF_TRUST, MARK_OF_SEARCHER }, "30109-52.htm", "30109-53.htm", "30109-54.htm", "30109-55.htm")),
		Map.entry("class_change_9",  new ChangeData(ClassId.rogue, 9, new int[]{ MARK_OF_SEEKER, MARK_OF_TRUST, MARK_OF_SAGITTARIUS }, "30109-56.htm", "30109-57.htm", "30109-58.htm", "30109-59.htm")),
		Map.entry("class_change_23", new ChangeData(ClassId.elvenScout, 23, new int[]{ MARK_OF_SEEKER, MARK_OF_LIFE, MARK_OF_SEARCHER }, "30109-60.htm", "30109-61.htm", "30109-62.htm", "30109-63.htm")),
		Map.entry("class_change_24", new ChangeData(ClassId.elvenScout, 24, new int[]{ MARK_OF_SEEKER, MARK_OF_LIFE, MARK_OF_SAGITTARIUS }, "30109-64.htm", "30109-65.htm", "30109-66.htm", "30109-67.htm")),
		Map.entry("class_change_2",  new ChangeData(ClassId.warrior, 2, new int[]{ MARK_OF_CHALLENGER, MARK_OF_TRUST, MARK_OF_DUELIST }, "30109-68.htm", "30109-69.htm", "30109-70.htm", "30109-71.htm")),
		Map.entry("class_change_3",  new ChangeData(ClassId.warrior, 3, new int[]{ MARK_OF_CHALLENGER, MARK_OF_TRUST, MARK_OF_CHAMPION }, "30109-72.htm", "30109-73.htm", "30109-74.htm", "30109-75.htm"))
	);
	
	private State STARTED;
	private State COMPLETED;
	
	public HannavaltOccupationChange()
	{
		super(30109, "30109_hannavalt_occupation_change", "village_master");
		
		var created = new State("Start", this);
		STARTED = new State("Started", this);
		COMPLETED = new State("Completed", this);
		setInitialState(created);
		
		for (var npcId : NPCS)
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}
	}
	
	private boolean hasAllMarks(QuestState st, int[] marks)
	{
		for (var mark : marks)
		{
			if (st.getQuestItemsCount(mark) == 0) return false;
		}
		return true;
	}
	
	@Override
	public String onEvent(String event, QuestState st)
	{
		if (HTM_EVENTS.contains(event))
		{
			return event;
		}
		
		var change = CHANGES.get(event);
		if (change == null)
		{
			return "No Quest";
		}
		
		var player = st.getPlayer();
		var classId = player.getClassId();
		int level = player.getLevel();
		String htmltext = "No Quest";
		
		if (classId == change.reqClass)
		{
			boolean hasMarks = hasAllMarks(st, change.marks);
			if (level <= 39)
			{
				htmltext = hasMarks ? change.lowYes : change.lowNo;
			}
			else
			{
				if (!hasMarks)
				{
					htmltext = change.okNo;
				}
				else
				{
					for (var mark : change.marks) st.takeItems(mark, 1);
					player.setClassId(change.newClass);
					player.setBaseClass(change.newClass);
					player.broadcastUserInfo();
					st.giveItems(SHADOW_WEAPON_COUPON_CGRADE, 15);
					st.playSound("ItemSound.quest_fanfare_2");
					htmltext = change.okYes;
				}
			}
		}
		
		st.setState(COMPLETED);
		st.exitQuest(true);
		return htmltext;
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		var st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		var race = player.getRace();
		var classId = player.getClassId();
		
		if (NPCS.contains(npc.getNpcId()) && (race == Race.elf || race == Race.human))
		{
			if (classId == ClassId.elvenKnight)
			{
				st.setState(STARTED);
				return "30109-01.htm";
			}
			if (classId == ClassId.knight)
			{
				st.setState(STARTED);
				return "30109-08.htm";
			}
			if (classId == ClassId.rogue)
			{
				st.setState(STARTED);
				return "30109-15.htm";
			}
			if (classId == ClassId.elvenScout)
			{
				st.setState(STARTED);
				return "30109-22.htm";
			}
			if (classId == ClassId.warrior)
			{
				st.setState(STARTED);
				return "30109-29.htm";
			}
			if (classId == ClassId.elvenFighter || classId == ClassId.fighter)
			{
				st.setState(COMPLETED);
				st.exitQuest(true);
				return "30109-76.htm";
			}
			if (classId == ClassId.templeKnight || classId == ClassId.plainsWalker || classId == ClassId.swordSinger || classId == ClassId.silverRanger
				|| classId == ClassId.warlord || classId == ClassId.paladin || classId == ClassId.treasureHunter
				|| classId == ClassId.gladiator || classId == ClassId.darkAvenger || classId == ClassId.hawkeye)
			{
				st.setState(COMPLETED);
				st.exitQuest(true);
				return "30109-77.htm";
			}
			st.setState(COMPLETED);
			st.exitQuest(true);
			return "30109-78.htm";
		}
		
		st.setState(COMPLETED);
		st.exitQuest(true);
		return "30109-78.htm";
	}


	public static void main(String[] args)
	{
		new HannavaltOccupationChange();
	}
}
