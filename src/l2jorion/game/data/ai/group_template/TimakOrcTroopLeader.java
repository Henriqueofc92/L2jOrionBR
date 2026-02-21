package l2jorion.game.data.ai.group_template;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;
import l2jorion.game.network.serverpackets.CreatureSay;
import l2jorion.util.random.Rnd;

public class TimakOrcTroopLeader extends Quest {
    private static final int TIMAK_ORC_TROOP_LEADER = 20767;
    private boolean firstAttacked = false;

    public TimakOrcTroopLeader() {
        super(-1, "TimakOrcTroopLeader", "ai");
        new State("Start", this);
        addKillId(TIMAK_ORC_TROOP_LEADER);
        addAttackId(TIMAK_ORC_TROOP_LEADER);
    }

    @Override
    public String onAttack(L2NpcInstance npc, L2PcInstance player, int damage, boolean isPet) {
        if (firstAttacked) {
            if (Rnd.get(50) != 0) return null;
            npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "Destroy the enemy, my brothers!"));
        } else {
            firstAttacked = true;
        }
        return null;
    }

    @Override
    public String onKill(L2NpcInstance npc, L2PcInstance player, boolean isPet) {
        if (npc.getNpcId() == TIMAK_ORC_TROOP_LEADER) {
            firstAttacked = false;
        } else if (firstAttacked) {
            addSpawn(npc.getNpcId(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
        }
        return null;
    }


	public static void main(String[] args)
	{
		new TimakOrcTroopLeader();
	}
}
