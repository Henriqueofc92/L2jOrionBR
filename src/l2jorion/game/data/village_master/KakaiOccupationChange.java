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
 * Kakai Lord of Flame - Orc 1st occupation change (30565).
 */
public class KakaiOccupationChange extends Quest
{
	private static final int KAKAI_LORD_OF_FLAME = 30565;
	
	private static final Set<String> VALID_EVENTS = Set.of(
		"30565-01.htm", "30565-02.htm", "30565-03.htm", "30565-04.htm",
		"30565-05.htm", "30565-06.htm", "30565-07.htm", "30565-08.htm"
	);
	
	private State STARTED;
	private State COMPLETED;
	
	public KakaiOccupationChange()
	{
		super(30565, "30565_kakai_occupation_change", "village_master");
		
		var created = new State("Start", this);
		STARTED = new State("Started", this);
		COMPLETED = new State("Completed", this);
		setInitialState(created);
		
		addStartNpc(KAKAI_LORD_OF_FLAME);
		addTalkId(KAKAI_LORD_OF_FLAME);
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
		
		if (npc.getNpcId() == KAKAI_LORD_OF_FLAME && race == Race.orc)
		{
			if (classId == ClassId.orcFighter)
			{
				st.setState(STARTED);
				return "30565-01.htm";
			}
			if (classId == ClassId.orcMage)
			{
				st.setState(STARTED);
				return "30565-06.htm";
			}
			if (classId == ClassId.orcRaider || classId == ClassId.orcMonk || classId == ClassId.orcShaman)
			{
				st.setState(COMPLETED);
				st.exitQuest(true);
				return "30565-09.htm";
			}
			if (classId == ClassId.destroyer || classId == ClassId.tyrant || classId == ClassId.overlord || classId == ClassId.warcryer)
			{
				st.setState(COMPLETED);
				st.exitQuest(true);
				return "30565-10.htm";
			}
		}
		
		st.setState(COMPLETED);
		st.exitQuest(true);
		return "30565-11.htm";
	}


	public static void main(String[] args)
	{
		new KakaiOccupationChange();
	}
}
