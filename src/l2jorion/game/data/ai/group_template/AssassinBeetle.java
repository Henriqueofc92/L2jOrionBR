package l2jorion.game.data.ai.group_template;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;
import l2jorion.util.random.Rnd;

public class AssassinBeetle extends Quest {
    private static final int ASSASSIN_BEETLE = 21381;
    private static final int ASSASSIN_BEETLE_B = 21653;

    public AssassinBeetle() {
        super(-1, "AssassinBeetle", "ai");
        new State("Start", this);
        addKillId(ASSASSIN_BEETLE);
    }

    @Override
    public String onKill(L2NpcInstance npc, L2PcInstance player, boolean isPet) {
        if (npc.getNpcId() == ASSASSIN_BEETLE && Rnd.get(100) <= 20) {
            for (int i = 0; i < 5; i++) {
                addSpawn(ASSASSIN_BEETLE_B, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0);
            }
        }
        return null;
    }


	public static void main(String[] args)
	{
		new AssassinBeetle();
	}
}
