package l2jorion.game.data.ai.group_template;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;
import l2jorion.util.random.Rnd;

public class AshurasOfDestruction extends Quest {
    private static final int ASHURAS = 21390;
    private static final int ASHURAS_B = 21656;

    public AshurasOfDestruction() {
        super(-1, "AshurasOfDestruction", "ai");
        new State("Start", this);
        addKillId(ASHURAS);
    }

    @Override
    public String onKill(L2NpcInstance npc, L2PcInstance player, boolean isPet) {
        if (npc.getNpcId() == ASHURAS && Rnd.get(100) <= 20) {
            for (int i = 0; i < 5; i++) {
                addSpawn(ASHURAS_B, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0);
            }
        }
        return null;
    }


	public static void main(String[] args)
	{
		new AshurasOfDestruction();
	}
}
