package l2jorion.game.data.village_master;

import java.util.Map;
import java.util.Set;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;

/**
 * Orc 1st Class Change.<br>
 * NPCs: Osborn(30500), Drikus(30505), Castor(30508).
 */
public class OrcOccupationChange1 extends Quest
{
	private static final int MARK_OF_RAIDER = 1592;
	private static final int KHAVATARI_TOTEM = 1615;
	private static final int MASK_OF_MEDIUM = 1631;
	private static final int SHADOW_WEAPON_COUPON_DGRADE = 8869;
	
	private static final Set<Integer> NPCS = Set.of(30500, 30505, 30508);
	
	private record ClassData(int newClass, int reqClass, int reqRace, String lowNi, String lowI, String okNi, String okI, int reqItem) {}
	
	private static final Map<String, ClassData> CLASSES = Map.of(
		"OR", new ClassData(45, 44, 3, "09", "10", "11", "12", MARK_OF_RAIDER),
		"OM", new ClassData(47, 44, 3, "13", "14", "15", "16", KHAVATARI_TOTEM),
		"OS", new ClassData(50, 49, 3, "17", "18", "19", "20", MASK_OF_MEDIUM)
	);
	
	public OrcOccupationChange1()
	{
		super(99996, "orc_occupation_change_1", "village_master");
		
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
			if (race == 3)
			{
				if (classId.level() == 1) return htmltext + "-21.htm";
				if (classId.level() >= 2) return htmltext + "-22.htm";
				if (id == 44) return htmltext + "-01.htm";
				if (id == 49) return htmltext + "-06.htm";
			}
			else
			{
				htmltext += "-23.htm";
			}
			st.exitQuest(true);
			return htmltext;
		}
		st.exitQuest(true);
		return "No Quest";
	}


	public static void main(String[] args)
	{
		new OrcOccupationChange1();
	}
}
