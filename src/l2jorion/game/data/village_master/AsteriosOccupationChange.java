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
 * Hierarch Asterios - Elf 1st occupation change (30154).
 */
public class AsteriosOccupationChange extends Quest
{
	private static final int HIERARCH_ASTERIOS = 30154;
	
	private static final Set<String> VALID_EVENTS = Set.of(
		"30154-01.htm", "30154-02.htm", "30154-03.htm", "30154-04.htm", "30154-05.htm",
		"30154-06.htm", "30154-07.htm", "30154-08.htm", "30154-09.htm", "30154-10.htm"
	);
	
	private State STARTED;
	private State COMPLETED;
	
	public AsteriosOccupationChange()
	{
		super(30154, "30154_asterios_occupation_change", "village_master");
		
		var created = new State("Start", this);
		STARTED = new State("Started", this);
		COMPLETED = new State("Completed", this);
		setInitialState(created);
		
		addStartNpc(HIERARCH_ASTERIOS);
		addTalkId(HIERARCH_ASTERIOS);
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
		
		if (npc.getNpcId() == HIERARCH_ASTERIOS && race == Race.elf)
		{
			if (classId == ClassId.elvenFighter)
			{
				st.setState(STARTED);
				return "30154-01.htm";
			}
			if (classId == ClassId.elvenMage)
			{
				st.setState(STARTED);
				return "30154-02.htm";
			}
			if (classId == ClassId.elvenWizard || classId == ClassId.oracle || classId == ClassId.elvenKnight || classId == ClassId.elvenScout)
			{
				st.setState(COMPLETED);
				st.exitQuest(true);
				return "30154-12.htm";
			}
			st.setState(COMPLETED);
			st.exitQuest(true);
			return "30154-13.htm";
		}
		
		st.setState(COMPLETED);
		st.exitQuest(true);
		return "30154-11.htm";
	}


	public static void main(String[] args)
	{
		new AsteriosOccupationChange();
	}
}
