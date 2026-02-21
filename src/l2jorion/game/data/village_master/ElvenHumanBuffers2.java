package l2jorion.game.data.village_master;

import java.util.Map;
import java.util.Set;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.QuestState;
import l2jorion.game.model.quest.State;

/**
 * Elven/Human Buffers 2nd Class Change.<br>
 * Bishop, Prophet, Elven Elder.
 */
public class ElvenHumanBuffers2 extends Quest
{
	private static final int MARK_OF_PILGRIM = 2721;
	private static final int MARK_OF_TRUST = 2734;
	private static final int MARK_OF_HEALER = 2820;
	private static final int MARK_OF_REFORMER = 2821;
	private static final int MARK_OF_LIFE = 3140;
	private static final int SHADOW_WEAPON_COUPON_CGRADE = 8870;
	
	// Maximilian, Hollint, Orven, Squillari, Bernhard, Siegmund, Gregory, Halaster, Baryl, Marie, Rahoraki
	private static final Set<Integer> NPCS = Set.of(30120, 30191, 30857, 30905, 31276, 31321, 31279, 31755, 31968, 32095, 31336);
	
	private record ClassData(int newClass, int reqClass, int reqRace, String lowNi, String lowI, String okNi, String okI, int[] reqItems) {}
	
	private static final Map<String, ClassData> CLASSES = Map.of(
		"BI", new ClassData(16, 15, 0, "16", "17", "18", "19", new int[]{ MARK_OF_PILGRIM, MARK_OF_TRUST, MARK_OF_HEALER }),
		"PH", new ClassData(17, 15, 0, "20", "21", "22", "23", new int[]{ MARK_OF_PILGRIM, MARK_OF_TRUST, MARK_OF_REFORMER }),
		"EE", new ClassData(30, 29, 1, "12", "13", "14", "15", new int[]{ MARK_OF_PILGRIM, MARK_OF_LIFE, MARK_OF_HEALER })
	);
	
	public ElvenHumanBuffers2()
	{
		super(99992, "elven_human_buffers_2", "village_master");
		
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
		return "30120-" + suffix + ".htm";
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
			String htmltext = "30120";
			if (race == 0 || race == 1)
			{
				if (id == 29) return htmltext + "-01.htm";
				if (id == 15) return htmltext + "-05.htm";
				if (classId.level() == 0) { st.exitQuest(true); return htmltext + "-24.htm"; }
				if (classId.level() >= 2) { st.exitQuest(true); return htmltext + "-25.htm"; }
				st.exitQuest(true);
				return htmltext + "-26.htm";
			}
			st.exitQuest(true);
			return htmltext + "-26.htm";
		}
		st.exitQuest(true);
		return "No Quest";
	}


	public static void main(String[] args)
	{
		new ElvenHumanBuffers2();
	}
}
