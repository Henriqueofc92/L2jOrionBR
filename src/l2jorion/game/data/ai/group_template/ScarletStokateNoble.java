package l2jorion.game.data.ai.group_template;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;
import l2jorion.util.random.Rnd;

/**
 * Spawn variant B mobs on kill with 20% chance.
 * Pattern used by: ScarletStokateNoble, AssassinBeetle, NecromancerOfDestruction, MagmaDrake, AshurasOfDestruction, ArimanesOfDestruction
 */
public class ScarletStokateNoble extends Quest {
    private static final int SCARLET_STOKATE_NOBLE = 21378;
    private static final int SCARLET_STOKATE_NOBLE_B = 21652;

    public ScarletStokateNoble() {
        super(-1, "ScarletStokateNoble", "ai");
        new State("Start", this);
        addKillId(SCARLET_STOKATE_NOBLE);
    }

    @Override
    public String onKill(L2NpcInstance npc, L2PcInstance player, boolean isPet) {
        if (npc.getNpcId() == SCARLET_STOKATE_NOBLE && Rnd.get(100) <= 20) {
            for (int i = 0; i < 5; i++) {
                addSpawn(SCARLET_STOKATE_NOBLE_B, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0);
            }
        }
        return null;
    }


	public static void main(String[] args)
	{
		new ScarletStokateNoble();
	}
}
