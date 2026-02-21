package l2jorion.game.data.village_master;

import java.util.Map;
import java.util.Set;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.QuestState;
import l2jorion.game.model.quest.State;

/**
 * Elven/Human Mystics 2nd Class Change.<br>
 * NPCs: Jurek(30115), Arkenias(30174), Valleria(30176), Scraide(30694),
 * Drikiyan(30854), Javier(31996).
 */
public class ElvenHumanMystics2 extends Quest
{
	private static final int MARK_OF_SCHOLAR = 2674;
	private static final int MARK_OF_TRUST = 2734;
	private static final int MARK_OF_MAGUS = 2840;
	private static final int MARK_OF_LIFE = 3140;
	private static final int MARK_OF_WITCHCRAFT = 3307;
	private static final int MARK_OF_SUMMONER = 3336;
	private static final int SHADOW_WEAPON_COUPON_CGRADE = 8870;
	
	private static final Set<Integer> NPCS = Set.of(30115, 30174, 30176, 30694, 30854, 31996);
	
	private record ClassData(int newClass, int reqClass, int reqRace, String lowNi, String lowI, String okNi, String okI, int[] reqItems) {}
	
	private static final Map<String, ClassData> CLASSES = Map.of(
		"EW", new ClassData(27, 26, 1, "18", "19", "20", "21", new int[]{ MARK_OF_SCHOLAR, MARK_OF_LIFE, MARK_OF_MAGUS }),
		"ES", new ClassData(28, 26, 1, "22", "23", "24", "25", new int[]{ MARK_OF_SCHOLAR, MARK_OF_LIFE, MARK_OF_SUMMONER }),
		"HS", new ClassData(12, 11, 0, "26", "27", "28", "29", new int[]{ MARK_OF_SCHOLAR, MARK_OF_TRUST, MARK_OF_MAGUS }),
		"HN", new ClassData(13, 11, 0, "30", "31", "32", "33", new int[]{ MARK_OF_SCHOLAR, MARK_OF_TRUST, MARK_OF_WITCHCRAFT }),
		"HW", new ClassData(14, 11, 0, "34", "35", "36", "37", new int[]{ MARK_OF_SCHOLAR, MARK_OF_TRUST, MARK_OF_SUMMONER })
	);
	
	public ElvenHumanMystics2()
	{
		super(99994, "elven_human_mystics_2", "village_master");
		
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
		return "30115-" + suffix + ".htm";
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
			String htmltext = "30115";
			if (race == 0 || race == 1)
			{
				if (id == 26) return htmltext + "-01.htm";
				if (id == 11) return htmltext + "-08.htm";
				if (!classId.isMage()) { st.exitQuest(true); return htmltext + "-40.htm"; }
				if (classId.level() == 0) { st.exitQuest(true); return htmltext + "-38.htm"; }
				if (classId.level() == 1) { st.exitQuest(true); return htmltext + "-40.htm"; }
				if (classId.level() >= 2) { st.exitQuest(true); return htmltext + "-39.htm"; }
			}
			st.exitQuest(true);
			return htmltext + "-40.htm";
		}
		st.exitQuest(true);
		return "No Quest";
	}


	public static void main(String[] args)
	{
		new ElvenHumanMystics2();
	}
}
