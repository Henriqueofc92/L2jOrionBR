package l2jorion.game.data.ai.group_template;

import java.util.Set;

import l2jorion.game.ai.CtrlIntention;
import l2jorion.game.enums.AchType;
import l2jorion.game.model.actor.instance.L2ChestInstance;
import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;
import l2jorion.util.random.Rnd;

public class Chests extends Quest {
    private static final int SKILL_DELUXE_KEY = 2229;
    private static final int BASE_CHANCE = 100;
    private static final int LEVEL_DECREASE = 40;
    private static final int IS_BOX = 40;

    private static final Set<Integer> CHEST_IDS = Set.of(
            18265, 18266, 18267, 18268, 18269, 18270, 18271, 18272, 18273, 18274,
            18275, 18276, 18277, 18278, 18279, 18280, 18281, 18282, 18283, 18284,
            18285, 18286, 18287, 18288, 18289, 18290, 18291, 18292, 18293, 18294,
            18295, 18296, 18297, 18298, 21671, 21694, 21717, 21740, 21763, 21786,
            21801, 21802, 21803, 21804, 21805, 21806, 21807, 21808, 21809, 21810,
            21811, 21812, 21813, 21814, 21815, 21816, 21817, 21818, 21819, 21820,
            21821, 21822
    );

    public Chests() {
        super(-1, "Chests", "ai");
        new State("Start", this);
        for (int id : CHEST_IDS) {
            addSkillUseId(id);
            addAttackId(id);
        }
    }

    @Override
    public String onSkillUse(L2NpcInstance npc, L2PcInstance player, l2jorion.game.model.L2Skill skill) {
        int npcId = npc.getNpcId();
        int skillId = skill.getId();
        int skillLevel = skill.getLevel();

        if (!CHEST_IDS.contains(npcId)) return null;

        final L2ChestInstance chest = (L2ChestInstance) npc;
        if (!chest.isInteracted()) {
            chest.setInteracted();
            if (Rnd.get(100) < IS_BOX) {
                if (skillId == SKILL_DELUXE_KEY) {
                    int keyLevelNeeded = npc.getLevel() / 10;
                    int levelDiff = Math.abs(keyLevelNeeded - skillLevel);
                    int chance = BASE_CHANCE - levelDiff * LEVEL_DECREASE;

                    if (Rnd.get(100) < chance) {
                        chest.setMustRewardExpSp(false);
                        chest.setSpecialDrop();
                        chest.reduceCurrentHp(99999999, player);
                        player.getAchievement().increase(AchType.OPEN_CHEST);
                        return null;
                    }
                }
                npc.onDecay();
            } else {
                var attacker = (npc.getAttackByList().contains(player.getPet())) ? player.getPet() : player;
                npc.setRunning();
                chest.addDamageHate(attacker, 0, 999);
                npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
            }
        }
        return null;
    }

    @Override
    public String onAttack(L2NpcInstance npc, L2PcInstance player, int damage, boolean isPet) {
        int npcId = npc.getNpcId();
        if (!CHEST_IDS.contains(npcId)) return null;

        final L2ChestInstance chest = (L2ChestInstance) npc;
        if (!chest.isInteracted()) {
            chest.setInteracted();
            if (Rnd.get(100) < IS_BOX) {
                npc.onDecay();
            } else {
                var attacker = isPet ? player.getPet() : player;
                npc.setRunning();
                chest.addDamageHate(attacker, 0, (damage * 100) / (npc.getLevel() + 7));
                npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
            }
        }
        return null;
    }


	public static void main(String[] args)
	{
		new Chests();
	}
}
