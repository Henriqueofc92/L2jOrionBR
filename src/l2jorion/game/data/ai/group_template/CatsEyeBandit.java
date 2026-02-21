package l2jorion.game.data.ai.group_template;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;
import l2jorion.game.network.serverpackets.CreatureSay;

public class CatsEyeBandit extends Quest {
    private static final int CATS_EYE_BANDIT = 27038;

    public CatsEyeBandit() {
        super(-1, "CatsEyeBandit", "ai");
        new State("Start", this);
        addKillId(CATS_EYE_BANDIT);
    }

    @Override
    public String onKill(L2NpcInstance npc, L2PcInstance player, boolean isPet) {
        if (npc.getNpcId() == CATS_EYE_BANDIT) {
            npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(),
                    "I must do something about this shameful incident..."));
        }
        return null;
    }


	public static void main(String[] args)
	{
		new CatsEyeBandit();
	}
}
