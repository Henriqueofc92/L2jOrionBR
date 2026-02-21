package l2jorion.game.data.ai.group_template;

import l2jorion.game.datatables.SkillTable;
import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;

public class AncientEgg extends Quest {
    private static final int EGG = 18344;

    public AncientEgg() {
        super(-1, "AncientEgg", "ai");
        new State("Start", this);
        addAttackId(EGG);
    }

    @Override
    public String onAttack(L2NpcInstance npc, L2PcInstance player, int damage, boolean isPet) {
        player.setTarget(player);
        player.doCast(SkillTable.getInstance().getInfo(5088, 1));
        return null;
    }


	public static void main(String[] args)
	{
		new AncientEgg();
	}
}
