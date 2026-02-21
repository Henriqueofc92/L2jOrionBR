package l2jorion.game.data.village_master;

import java.util.Set;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.base.ClassId;
import l2jorion.game.model.base.Race;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.QuestState;
import l2jorion.game.model.quest.State;

/**
 * Warehouse Chief Reed - Dwarf 1st occupation change (30520).
 */
public class ReedOccupationChange extends Quest
{
	private static final int WAREHOUSE_CHIEF_REED = 30520;
	
	private static final Set<String> VALID_EVENTS = Set.of(
		"30520-01.htm", "30520-02.htm", "30520-03.htm", "30520-04.htm"
	);
	
	private State STARTED;
	private State COMPLETED;
	
	public ReedOccupationChange()
	{
		super(30520, "30520_reed_occupation_change", "village_master");
		
		var created = new State("Start", this);
		STARTED = new State("Started", this);
		COMPLETED = new State("Completed", this);
		setInitialState(created);
		
		addStartNpc(WAREHOUSE_CHIEF_REED);
		addTalkId(WAREHOUSE_CHIEF_REED);
	}
	
	@Override
	public String onEvent(String event, QuestState st)
	{
		if (VALID_EVENTS.contains(event))
		{
			return event;
		}
		st.setState(COMPLETED);
		st.exitQuest(true);
		return "No Quest";
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
		
		if (npc.getNpcId() == WAREHOUSE_CHIEF_REED && race == Race.dwarf)
		{
			if (classId == ClassId.dwarvenFighter)
			{
				st.setState(STARTED);
				return "30520-01.htm";
			}
			if (classId == ClassId.scavenger || classId == ClassId.artisan)
			{
				st.setState(COMPLETED);
				st.exitQuest(true);
				return "30520-05.htm";
			}
			if (classId == ClassId.bountyHunter || classId == ClassId.warsmith)
			{
				st.setState(COMPLETED);
				st.exitQuest(true);
				return "30520-06.htm";
			}
		}
		
		st.setState(COMPLETED);
		st.exitQuest(true);
		return "30520-07.htm";
	}


	public static void main(String[] args)
	{
		new ReedOccupationChange();
	}
}
