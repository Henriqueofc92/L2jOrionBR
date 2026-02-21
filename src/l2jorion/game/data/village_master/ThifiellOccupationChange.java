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
 * Tetrarch Thifiell - Dark Elf 1st occupation change (30358).
 */
public class ThifiellOccupationChange extends Quest
{
	private static final int TETRARCH_THIFIELL = 30358;
	
	private static final Set<String> VALID_EVENTS = Set.of(
		"30358-01.htm", "30358-02.htm", "30358-03.htm", "30358-04.htm", "30358-05.htm",
		"30358-06.htm", "30358-07.htm", "30358-08.htm", "30358-09.htm", "30358-10.htm"
	);
	
	private State STARTED;
	private State COMPLETED;
	
	public ThifiellOccupationChange()
	{
		super(30358, "30358_thifiell_occupation_change", "village_master");
		
		var created = new State("Start", this);
		STARTED = new State("Started", this);
		COMPLETED = new State("Completed", this);
		setInitialState(created);
		
		addStartNpc(TETRARCH_THIFIELL);
		addTalkId(TETRARCH_THIFIELL);
	}
	
	@Override
	public String onEvent(String event, QuestState st)
	{
		if (VALID_EVENTS.contains(event))
		{
			return event;
		}
		return event;
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
		
		if (npc.getNpcId() == TETRARCH_THIFIELL && race == Race.darkelf)
		{
			if (classId == ClassId.darkFighter)
			{
				st.setState(STARTED);
				return "30358-01.htm";
			}
			if (classId == ClassId.darkMage)
			{
				st.setState(STARTED);
				return "30358-02.htm";
			}
			if (classId == ClassId.darkWizard || classId == ClassId.shillienOracle || classId == ClassId.palusKnight || classId == ClassId.assassin)
			{
				st.setState(COMPLETED);
				st.exitQuest(true);
				return "30358-12.htm";
			}
			st.setState(COMPLETED);
			st.exitQuest(true);
			return "30358-13.htm";
		}
		
		st.setState(COMPLETED);
		st.exitQuest(true);
		return "30358-11.htm";
	}


	public static void main(String[] args)
	{
		new ThifiellOccupationChange();
	}
}
