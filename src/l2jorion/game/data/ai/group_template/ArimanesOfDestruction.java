package l2jorion.game.data.ai.group_template;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;
import l2jorion.util.random.Rnd;

public class ArimanesOfDestruction extends Quest {
    private static final int ARIMANES = 21387;
    private static final int ARIMANES_B = 21655;

    public ArimanesOfDestruction() {
        super(-1, "ArimanesOfDestruction", "ai");
        new State("Start", this);
        addKillId(ARIMANES);
    }

    @Override
    public String onKill(L2NpcInstance npc, L2PcInstance player, boolean isPet) {
        if (npc.getNpcId() == ARIMANES && Rnd.get(100) <= 20) {
            for (int i = 0; i < 5; i++) {
                addSpawn(ARIMANES_B, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0);
            }
        }
        return null;
    }


	public static void main(String[] args)
	{
		new ArimanesOfDestruction();
	}
}
