package l2jorion.game.data.ai.group_template;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;
import l2jorion.game.network.serverpackets.CreatureSay;
import l2jorion.util.random.Rnd;

public class TimakOrcOverlord extends Quest {
    private static final int TIMAK_ORC_OVERLORD = 20588;
    private boolean firstAttacked = false;

    public TimakOrcOverlord() {
        super(-1, "TimakOrcOverlord", "ai");
        new State("Start", this);
        addKillId(TIMAK_ORC_OVERLORD);
        addAttackId(TIMAK_ORC_OVERLORD);
    }

    @Override
    public String onAttack(L2NpcInstance npc, L2PcInstance player, int damage, boolean isPet) {
        if (firstAttacked) {
            if (Rnd.get(50) != 0) return null;
            npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "Dear ultimate power!!!"));
        } else {
            firstAttacked = true;
        }
        return null;
    }

    @Override
    public String onKill(L2NpcInstance npc, L2PcInstance player, boolean isPet) {
        if (npc.getNpcId() == TIMAK_ORC_OVERLORD) {
            firstAttacked = false;
        } else if (firstAttacked) {
            addSpawn(npc.getNpcId(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
        }
        return null;
    }


	public static void main(String[] args)
	{
		new TimakOrcOverlord();
	}
}
