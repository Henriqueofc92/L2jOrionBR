package l2jorion.game.data.ai.group_template;

import java.util.List;
import java.util.Map;

import l2jorion.game.ai.CtrlIntention;
import l2jorion.game.model.L2Attackable;
import l2jorion.game.model.Location;
import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;
import l2jorion.game.network.serverpackets.CreatureSay;
import l2jorion.util.random.Rnd;

public class RetreatOnAttack extends Quest {
    private record MobData(int hpPercent, int chance) {}

    private static final Map<Integer, MobData> MOB_SPAWNS = Map.of(
            20432, new MobData(100, 100), // Elpy
            20058, new MobData(50, 10)    // Ol Mahum Guard
    );

    private static final Map<Integer, List<String>> MOB_TEXTS = Map.of(
            20058, List.of("I'll be back", "You are stronger than expected")
    );

    public RetreatOnAttack() {
        super(-1, "RetreatOnAttack", "ai");
        new State("Start", this);
        for (int id : MOB_SPAWNS.keySet()) {
            addAttackId(id);
        }
    }

    @Override
    public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player) {
        if ("Retreat".equals(event) && npc != null && player != null) {
            npc.setIsAfraid(false);
            ((L2Attackable) npc).addDamageHate(player, 0, 100);
            npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
        }
        return null;
    }

    @Override
    public String onAttack(L2NpcInstance npc, L2PcInstance player, int damage, boolean isPet) {
        int npcId = npc.getNpcId();
        var data = MOB_SPAWNS.get(npcId);
        if (data == null) return null;

        if (npc.getStatus().getCurrentHp() <= npc.getMaxHp() * data.hpPercent() / 100 && Rnd.get(100) < data.chance()) {
            var texts = MOB_TEXTS.get(npcId);
            if (texts != null) {
                npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), texts.get(Rnd.get(texts.size()))));
            }
            int signX = npc.getX() > player.getX() ? 500 : -500;
            int signY = npc.getY() > player.getY() ? 500 : -500;
            npc.setIsAfraid(true);
            npc.setRunning();
            npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(npc.getX() + signX, npc.getY() + signY, npc.getZ(), 0));
            startQuestTimer("Retreat", 10000, npc, player);
        }
        return null;
    }


	public static void main(String[] args)
	{
		new RetreatOnAttack();
	}
}
