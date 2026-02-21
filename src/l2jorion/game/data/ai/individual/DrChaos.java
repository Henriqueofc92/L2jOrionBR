package l2jorion.game.data.ai.individual;

import java.time.Instant;

import l2jorion.game.ai.CtrlIntention;
import l2jorion.game.datatables.sql.SpawnTable;
import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.network.serverpackets.SocialAction;
import l2jorion.game.network.serverpackets.SpecialCamera;

public class DrChaos extends Quest {
    private static final int DOCTOR_CHAOS = 32033;
    private static final int STRANGE_MACHINE = 32032;

    public DrChaos() {
        super(-1, "DrChaos", "individual ai");
        var test = loadGlobalQuestVar("Chaos_Golem");
        if (test != null && test.matches("\\d+")) {
            long remain = Long.parseLong(test) - Instant.now().toEpochMilli();
            if (remain <= 0) {
                addSpawn(DOCTOR_CHAOS, 96471, -111425, -3334, 0, false, 0);
            } else {
                startQuestTimer("spawn_npc", remain, null, null);
            }
        } else {
            addSpawn(DOCTOR_CHAOS, 96471, -111425, -3334, 0, false, 0);
        }
    }

    private L2NpcInstance findTemplate(int npcId) {
        return SpawnTable.getInstance().getSpawnTable().values().stream()
                .filter(spawn -> spawn != null && spawn.getNpcid() == npcId)
                .map(spawn -> spawn.getLastSpawn())
                .findFirst().orElse(null);
    }

    @Override
    public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player) {
        switch (event) {
            case "1" -> {
                var machineInstance = findTemplate(STRANGE_MACHINE);
                if (machineInstance != null) {
                    npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, machineInstance);
                    machineInstance.broadcastPacket(new SpecialCamera(machineInstance.getObjectId(), 1, -200, 15, 10000, 20000));
                } else {
                    System.out.println("Dr Chaos AI: problema ao encontrar Strange Machine (npcid = " + STRANGE_MACHINE + "). Erro: nÃ£o spawnado!");
                }
                startQuestTimer("2", 2000, npc, player);
                startQuestTimer("3", 10000, npc, player);
            }
            case "spawn_npc" -> addSpawn(DOCTOR_CHAOS, 96471, -111425, -3334, 0, false, 0);
            case "2" -> npc.broadcastPacket(new SocialAction(npc.getObjectId(), 3));
            case "3" -> npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1, -150, 10, 3000, 20000));
        }
        return null;
    }


	public static void main(String[] args)
	{
		new DrChaos();
	}
}
