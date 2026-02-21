package l2jorion.game.data.ai.group_template;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;
import l2jorion.util.random.Rnd;

public class MagmaDrake extends Quest {
    private static final int MAGMA_DRAKE = 21393;
    private static final int MAGMA_DRAKE_B = 21657;

    public MagmaDrake() {
        super(-1, "MagmaDrake", "ai");
        new State("Start", this);
        addKillId(MAGMA_DRAKE);
    }

    @Override
    public String onKill(L2NpcInstance npc, L2PcInstance player, boolean isPet) {
        if (npc.getNpcId() == MAGMA_DRAKE && Rnd.get(100) <= 20) {
            for (int i = 0; i < 5; i++) {
                addSpawn(MAGMA_DRAKE_B, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0);
            }
        }
        return null;
    }


	public static void main(String[] args)
	{
		new MagmaDrake();
	}
}
