package l2jorion.game.data.teleports;

import java.util.Set;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;

/**
 * Noblesse Teleport (2000).<br>
 * Shows noblesse teleport list to noble players.
 */
public class NoblesseTeleport extends Quest
{
	private static final Set<Integer> NPCS = Set.of(
		30006, 30059, 30080, 30134, 30146, 30177, 30233, 30256,
		30320, 30540, 30576, 30836, 30848, 30878, 30899, 31275,
		31320, 31964
	);
	
	public NoblesseTeleport()
	{
		super(2000, "2000_NoblesseTeleport", "Teleports");
		
		var created = new State("Start", this);
		setInitialState(created);
		
		for (var npcId : NPCS)
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		var st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		var htmltext = player.isNoble() ? "noble.htm" : "nobleteleporter-no.htm";
		st.exitQuest(true);
		return htmltext;
	}


	public static void main(String[] args)
	{
		new NoblesseTeleport();
	}
}
