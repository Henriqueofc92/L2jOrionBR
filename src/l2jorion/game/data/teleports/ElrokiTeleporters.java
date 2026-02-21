package l2jorion.game.data.teleports;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;

public class ElrokiTeleporters extends Quest
{
	private static final int NPC_1 = 32111;
	private static final int NPC_2 = 32112;
	
	public ElrokiTeleporters()
	{
		super(6111, "6111_ElrokiTeleporters", "Teleports");
		
		var created = new State("Start", this);
		setInitialState(created);
		
		addStartNpc(NPC_1);
		addTalkId(NPC_1);
		addStartNpc(NPC_2);
		addTalkId(NPC_2);
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		switch (npc.getNpcId())
		{
			case NPC_1 -> player.teleToLocation(4990, -1879, -3178);
			case NPC_2 -> player.teleToLocation(7557, -5513, -3221);
		}
		return null;
	}


	public static void main(String[] args)
	{
		new ElrokiTeleporters();
	}
}
