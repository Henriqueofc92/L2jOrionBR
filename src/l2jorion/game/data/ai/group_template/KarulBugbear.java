package l2jorion.game.data.ai.group_template;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;
import l2jorion.game.network.serverpackets.CreatureSay;
import l2jorion.util.random.Rnd;

public class KarulBugbear extends Quest {
    private static final int KARUL_BUGBEAR = 20600;
    private boolean firstAttacked = false;

    public KarulBugbear() {
        super(-1, "KarulBugbear", "ai");
        new State("Start", this);
        addKillId(KARUL_BUGBEAR);
        addAttackId(KARUL_BUGBEAR);
    }

    @Override
    public String onAttack(L2NpcInstance npc, L2PcInstance player, int damage, boolean isPet) {
        int objId = npc.getObjectId();
        if (firstAttacked) {
            if (Rnd.get(4) != 0) return null;
            npc.broadcastPacket(new CreatureSay(objId, 0, npc.getName(), "Your rear is practically unguarded!"));
        } else {
            firstAttacked = true;
            if (Rnd.get(4) != 0) return null;
            npc.broadcastPacket(new CreatureSay(objId, 0, npc.getName(), "Watch your back!"));
        }
        return null;
    }

    @Override
    public String onKill(L2NpcInstance npc, L2PcInstance player, boolean isPet) {
        if (npc.getNpcId() == KARUL_BUGBEAR) {
            firstAttacked = false;
        } else if (firstAttacked) {
            addSpawn(npc.getNpcId(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
        }
        return null;
    }


	public static void main(String[] args)
	{
		new KarulBugbear();
	}
}
