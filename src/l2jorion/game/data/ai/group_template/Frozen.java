package l2jorion.game.data.ai.group_template;

import java.util.Map;

import l2jorion.game.ai.CtrlIntention;
import l2jorion.game.model.L2Attackable;
import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;

public class Frozen extends Quest {
    private record MinionData(int minionId, int count) {}

    private static final Map<Integer, MinionData> FROZEN_IDS = Map.of(
            22094, new MinionData(22093, 5),
            22088, new MinionData(22087, 5)
    );

    private boolean alwaysSpawn = false;

    public Frozen() {
        super(-1, "Frozen", "ai");
        new State("Start", this);
        for (int id : FROZEN_IDS.keySet()) {
            addAttackId(id);
            addKillId(id);
        }
    }

    @Override
    public String onAttack(L2NpcInstance npc, L2PcInstance player, int damage, boolean isPet) {
        int npcId = npc.getNpcId();
        var data = FROZEN_IDS.get(npcId);
        if (data == null) return null;

        if (!alwaysSpawn && player.isAttackingNow()) {
            alwaysSpawn = true;
            for (int i = 0; i < data.count(); i++) {
                L2Attackable spawned = (L2Attackable) addSpawn(data.minionId(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
                spawned.addDamageHate(player, 0, 999);
                spawned.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
            }
        }
        return null;
    }

    @Override
    public String onKill(L2NpcInstance npc, L2PcInstance player, boolean isPet) {
        if (FROZEN_IDS.containsKey(npc.getNpcId())) {
            alwaysSpawn = false;
        }
        return null;
    }


	public static void main(String[] args)
	{
		new Frozen();
	}
}
