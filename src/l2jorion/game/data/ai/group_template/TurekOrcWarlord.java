package l2jorion.game.data.ai.group_template;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;
import l2jorion.game.network.serverpackets.CreatureSay;
import l2jorion.util.random.Rnd;

public class TurekOrcWarlord extends Quest {
    private static final int TUREK_ORC_WARLORD = 20495;
    private boolean firstAttacked = false;

    public TurekOrcWarlord() {
        super(-1, "TurekOrcWarlord", "ai");
        new State("Start", this);
        addKillId(TUREK_ORC_WARLORD);
        addAttackId(TUREK_ORC_WARLORD);
    }

    @Override
    public String onAttack(L2NpcInstance npc, L2PcInstance player, int damage, boolean isPet) {
        int objId = npc.getObjectId();
        if (firstAttacked) {
            if (Rnd.get(40) != 0) return null;
            npc.broadcastPacket(new CreatureSay(objId, 0, npc.getName(), "You wont take me down easily."));
        } else {
            firstAttacked = true;
            npc.broadcastPacket(new CreatureSay(objId, 0, npc.getName(), "The battle has just begun!"));
        }
        return null;
    }

    @Override
    public String onKill(L2NpcInstance npc, L2PcInstance player, boolean isPet) {
        if (npc.getNpcId() == TUREK_ORC_WARLORD) {
            firstAttacked = false;
        } else if (firstAttacked) {
            addSpawn(npc.getNpcId(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
        }
        return null;
    }


	public static void main(String[] args)
	{
		new TurekOrcWarlord();
	}
}
