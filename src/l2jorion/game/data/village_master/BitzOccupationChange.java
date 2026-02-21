package l2jorion.game.data.village_master;

import java.util.Set;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.base.Race;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.QuestState;
import l2jorion.game.model.quest.State;

/**
 * Grand Master Bitz - Human Fighter occupation change (30026).
 */
public class BitzOccupationChange extends Quest
{
	private static final int GRAND_MASTER_BITZ = 30026;
	
	private static final Set<String> VALID_EVENTS = Set.of(
		"30026-01.htm", "30026-02.htm", "30026-03.htm", "30026-04.htm",
		"30026-05.htm", "30026-06.htm", "30026-07.htm"
	);
	
	private State STARTED;
	
	public BitzOccupationChange()
	{
		super(30026, "30026_bitz_occupation_change", "village_master");
		
		var created = new State("Start", this);
		STARTED = new State("Started", this);
		new State("Completed", this);
		setInitialState(created);
		
		addStartNpc(GRAND_MASTER_BITZ);
		addTalkId(GRAND_MASTER_BITZ);
	}
	
	@Override
	public String onEvent(String event, QuestState st)
	{
		if (VALID_EVENTS.contains(event))
		{
			return event;
		}
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
		int pcId = player.getClassId().getId();
		
		if (npc.getNpcId() == GRAND_MASTER_BITZ && race == Race.human && (pcId <= 9 || (pcId >= 88 && pcId <= 93)))
		{
			st.setState(STARTED);
			return switch (pcId)
			{
				case 0 -> "30026-01.htm";
				case 1, 4, 7 -> "30026-08.htm";
				default -> "30026-09.htm";
			};
		}
		
		st.exitQuest(true);
		return "30026-10.htm";
	}


	public static void main(String[] args)
	{
		new BitzOccupationChange();
	}
}
