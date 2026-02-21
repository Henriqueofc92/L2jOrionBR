package l2jorion.game.data.ai.group_template;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;
import l2jorion.game.network.serverpackets.CreatureSay;
import l2jorion.util.random.Rnd;

public class DeluLizardmanSpecialAgent extends Quest {
    private static final int DELU_LIZARDMAN_SPECIAL_AGENT = 21105;
    private boolean firstAttacked = false;

    public DeluLizardmanSpecialAgent() {
        super(-1, "DeluLizardmanSpecialAgent", "ai");
        new State("Start", this);
        addKillId(DELU_LIZARDMAN_SPECIAL_AGENT);
        addAttackId(DELU_LIZARDMAN_SPECIAL_AGENT);
    }

    @Override
    public String onAttack(L2NpcInstance npc, L2PcInstance player, int damage, boolean isPet) {
        int objId = npc.getObjectId();
        if (firstAttacked) {
            if (Rnd.get(40) != 0) return null;
            npc.broadcastPacket(new CreatureSay(objId, 0, npc.getName(), "Hey! Were having a duel here!"));
        } else {
            firstAttacked = true;
            npc.broadcastPacket(new CreatureSay(objId, 0, npc.getName(), "How dare you interrupt our fight! Hey guys, help!"));
        }
        return null;
    }

    @Override
    public String onKill(L2NpcInstance npc, L2PcInstance player, boolean isPet) {
        if (npc.getNpcId() == DELU_LIZARDMAN_SPECIAL_AGENT) {
            firstAttacked = false;
        } else if (firstAttacked) {
            addSpawn(npc.getNpcId(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
        }
        return null;
    }


	public static void main(String[] args)
	{
		new DeluLizardmanSpecialAgent();
	}
}
