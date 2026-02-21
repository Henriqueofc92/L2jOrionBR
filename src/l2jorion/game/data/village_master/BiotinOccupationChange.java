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
 * High Priest Biotin - Human Mystic occupation change (30031).
 */
public class BiotinOccupationChange extends Quest
{
	private static final int HIGH_PRIEST_BIOTIN = 30031;
	
	private static final Set<String> VALID_EVENTS = Set.of(
		"30031-01.htm", "30031-02.htm", "30031-03.htm", "30031-04.htm", "30031-05.htm"
	);
	
	private State STARTED;
	private State COMPLETED;
	
	public BiotinOccupationChange()
	{
		super(30031, "30031_biotin_occupation_change", "village_master");
		
		var created = new State("Start", this);
		STARTED = new State("Started", this);
		COMPLETED = new State("Completed", this);
		setInitialState(created);
		
		addStartNpc(HIGH_PRIEST_BIOTIN);
		addTalkId(HIGH_PRIEST_BIOTIN);
	}
	
	@Override
	public String onEvent(String event, QuestState st)
	{
		if (VALID_EVENTS.contains(event))
		{
			return event;
		}
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
		
		if (npc.getNpcId() != HIGH_PRIEST_BIOTIN)
		{
			return null;
		}
		
		if (race == Race.human)
		{
			String htmltext = "30031-01.htm";
			
			if (classId == ClassId.fighter || classId == ClassId.warrior || classId == ClassId.knight || classId == ClassId.rogue)
			{
				htmltext = "30031-08.htm";
			}
			if (classId == ClassId.warlord || classId == ClassId.paladin || classId == ClassId.treasureHunter)
			{
				htmltext = "30031-08.htm";
			}
			if (classId == ClassId.gladiator || classId == ClassId.darkAvenger || classId == ClassId.hawkeye)
			{
				htmltext = "30031-08.htm";
			}
			if (classId == ClassId.wizard || classId == ClassId.cleric)
			{
				htmltext = "30031-06.htm";
			}
			if (classId == ClassId.sorceror || classId == ClassId.necromancer || classId == ClassId.warlock || classId == ClassId.bishop || classId == ClassId.prophet)
			{
				htmltext = "30031-07.htm";
			}
			
			st.setState(STARTED);
			return htmltext;
		}
		
		st.setState(COMPLETED);
		st.exitQuest(true);
		return "30031-08.htm";
	}


	public static void main(String[] args)
	{
		new BiotinOccupationChange();
	}
}
