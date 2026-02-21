package l2jorion.game.data.ai.group_template;

import java.util.Set;

import l2jorion.game.datatables.sql.ItemTable;
import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;
import l2jorion.util.random.Rnd;

public class EvaBox extends Quest {
    private static final Set<Integer> KISS_OF_EVA = Set.of(1073, 3141, 3252);
    private static final int BOX = 32342;
    private static final int[] REWARDS = {9692, 9693};

    public EvaBox() {
        super(-1, "EvaBox", "ai");
        new State("Start", this);
        addKillId(BOX);
    }

    private static void dropItem(L2NpcInstance npc, int itemId, int count, L2PcInstance player) {
        var ditem = ItemTable.getInstance().createItem("Loot", itemId, count, player);
        ditem.dropMe(npc, npc.getX(), npc.getY(), npc.getZ());
    }

    @Override
    public String onKill(L2NpcInstance npc, L2PcInstance player, boolean isPet) {
        boolean found = player.getAllEffects() != null &&
                java.util.Arrays.stream(player.getAllEffects())
                        .anyMatch(effect -> KISS_OF_EVA.contains(effect.getSkill().getId()));
        if (found) {
            int dropId = Rnd.get(REWARDS.length);
            dropItem(npc, REWARDS[dropId], 1, player);
        }
        return null;
    }


	public static void main(String[] args)
	{
		new EvaBox();
	}
}
