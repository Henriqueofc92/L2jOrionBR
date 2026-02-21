package l2jorion.game.data.ai.group_template;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;
import l2jorion.game.network.serverpackets.CreatureSay;
import l2jorion.util.random.Rnd;

public class OlMahumGeneral extends Quest {
    private static final int OL_MAHUM_GENERAL = 20438;
    private boolean firstAttacked = false;

    public OlMahumGeneral() {
        super(-1, "OlMahumGeneral", "ai");
        new State("Start", this);
        addKillId(OL_MAHUM_GENERAL);
        addAttackId(OL_MAHUM_GENERAL);
    }

    @Override
    public String onAttack(L2NpcInstance npc, L2PcInstance player, int damage, boolean isPet) {
        int objId = npc.getObjectId();
        if (firstAttacked) {
            if (Rnd.get(100) != 0) return null;
            npc.broadcastPacket(new CreatureSay(objId, 0, npc.getName(), "We shall see about that!"));
        } else {
            firstAttacked = true;
            npc.broadcastPacket(new CreatureSay(objId, 0, npc.getName(), "I will definitely repay this humiliation!"));
        }
        return null;
    }

    @Override
    public String onKill(L2NpcInstance npc, L2PcInstance player, boolean isPet) {
        if (npc.getNpcId() == OL_MAHUM_GENERAL) {
            firstAttacked = false;
        } else if (firstAttacked) {
            addSpawn(npc.getNpcId(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
        }
        return null;
    }


	public static void main(String[] args)
	{
		new OlMahumGeneral();
	}
}
