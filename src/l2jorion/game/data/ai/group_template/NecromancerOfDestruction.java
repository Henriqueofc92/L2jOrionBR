package l2jorion.game.data.ai.group_template;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;
import l2jorion.util.random.Rnd;

public class NecromancerOfDestruction extends Quest {
    private static final int NECROMANCER = 21384;
    private static final int NECROMANCER_B = 21654;

    public NecromancerOfDestruction() {
        super(-1, "NecromancerOfDestruction", "ai");
        new State("Start", this);
        addKillId(NECROMANCER);
    }

    @Override
    public String onKill(L2NpcInstance npc, L2PcInstance player, boolean isPet) {
        if (npc.getNpcId() == NECROMANCER && Rnd.get(100) <= 20) {
            for (int i = 0; i < 5; i++) {
                addSpawn(NECROMANCER_B, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0);
            }
        }
        return null;
    }


	public static void main(String[] args)
	{
		new NecromancerOfDestruction();
	}
}
