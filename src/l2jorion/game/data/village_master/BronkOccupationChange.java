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
 * Head Blacksmith Bronk - Dwarf Artisan/Warsmith occupation change (30525).
 */
public class BronkOccupationChange extends Quest
{
	private static final int HEAD_BLACKSMITH_BRONK = 30525;
	
	private static final Set<String> VALID_EVENTS = Set.of(
		"30525-01.htm", "30525-02.htm", "30525-03.htm", "30525-04.htm"
	);
	
	private State STARTED;
	private State COMPLETED;
	
	public BronkOccupationChange()
	{
		super(30525, "30525_bronk_occupation_change", "village_master");
		
		var created = new State("Start", this);
		STARTED = new State("Started", this);
		COMPLETED = new State("Completed", this);
		setInitialState(created);
		
		addStartNpc(HEAD_BLACKSMITH_BRONK);
		addTalkId(HEAD_BLACKSMITH_BRONK);
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
		
		if (npc.getNpcId() == HEAD_BLACKSMITH_BRONK && race == Race.dwarf)
		{
			if (classId == ClassId.dwarvenFighter)
			{
				st.setState(STARTED);
				return "30525-01.htm";
			}
			if (classId == ClassId.artisan)
			{
				st.setState(COMPLETED);
				st.exitQuest(true);
				return "30525-05.htm";
			}
			if (classId == ClassId.warsmith)
			{
				st.setState(COMPLETED);
				st.exitQuest(true);
				return "30525-06.htm";
			}
			if (classId == ClassId.scavenger || classId == ClassId.bountyHunter)
			{
				st.setState(COMPLETED);
				st.exitQuest(true);
				return "30525-07.htm";
			}
		}
		
		st.setState(COMPLETED);
		st.exitQuest(true);
		return "30525-07.htm";
	}


	public static void main(String[] args)
	{
		new BronkOccupationChange();
	}
}
