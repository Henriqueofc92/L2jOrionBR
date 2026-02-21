package l2jorion.game.data.ai.group_template;

import java.util.Set;

import l2jorion.game.model.L2Skill;
import l2jorion.game.model.actor.instance.L2GourdInstance;
import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;
import l2jorion.game.network.serverpackets.CreatureSay;
import l2jorion.util.random.Rnd;

public class Squash extends Quest {
    private static final int SKILL_NECTAR = 9998;
    private static final Set<Integer> WATERED_SQUASH = Set.of(12774, 12775, 12776, 12777, 12778, 12779);
    private static final Set<Integer> ADULT_SMALL_SQUASH = Set.of(12775, 12776);
    private static final Set<Integer> ADULT_LARGE_SQUASH = Set.of(12778, 12779);

    public Squash() {
        super(-1, "Squash", "ai");
        new State("Start", this);
        for (int id : WATERED_SQUASH) {
            addSkillUseId(id);
            addAttackId(id);
            addKillId(id);
        }
    }

    @Override
    public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player) {
        if (npc == null || player == null) return null;
        int objId = npc.getObjectId();
        switch (event) {
            case "Good By" -> { npc.broadcastPacket(new CreatureSay(objId, 0, npc.getName(), "Good By!! LOL.")); npc.onDecay(); }
            case "Good By1" -> { npc.broadcastPacket(new CreatureSay(objId, 0, npc.getName(), "Goodbye everyone... The big pumpkin said goodbye...")); npc.onDecay(); }
            case "Good By2" -> npc.broadcastPacket(new CreatureSay(objId, 0, npc.getName(), "Can you go faster? I'll run away in 30 seconds..."));
            case "Good By3" -> npc.broadcastPacket(new CreatureSay(objId, 0, npc.getName(), "I'll break relations with you in 20 seconds!"));
            case "Good By4" -> npc.broadcastPacket(new CreatureSay(objId, 0, npc.getName(), "I only have 10 seconds left! 9. 8. 7..!"));
            case "Good By5" -> npc.broadcastPacket(new CreatureSay(objId, 0, npc.getName(), "Hey! Stay happy! Idiot, forget about me!"));
        }
        return null;
    }

    @Override
    public String onSkillUse(L2NpcInstance npc, L2PcInstance player, L2Skill skill) {
        int npcId = npc.getNpcId();
        if (skill.getId() != SKILL_NECTAR || !WATERED_SQUASH.contains(npcId)) return null;
        int objectId = npc.getObjectId();

        final L2GourdInstance gourd = (L2GourdInstance) npc;
        int nectar = gourd.getNectar();
        boolean success = Rnd.get(2) == 1;

        if (nectar <= 3) {
            if (success) gourd.addGood();
            gourd.addNectar();

            String[] msgs = switch (nectar) {
                case 0 -> success
                        ? new String[]{"To be able to grow, I must drink only nectar... more often", "If you pour nectar faster I'll grow faster!", "Well, believe me, spray nectar! I can become a big pumpkin!", "Bring nectar to grow the pumpkin!"}
                        : new String[]{"Don't rush! Too often, I can't keep up!", "I'm not a machine, you can't water me that fast", "Where are you rushing! Too often, I can't keep up!"};
                case 1 -> success
                        ? new String[]{"I wish to become a big pumpkin!", "Yum, yum, yum! Here it goes! Good care!", "Am I ripe or rotten?", "Nectar - only the best! Ha! Ha! Ha!"}
                        : new String[]{"Oh! Missed again! Maybe spending nectar too fast?", "If I die like this, you'll only get a young pumpkin..."};
                case 2 -> success
                        ? new String[]{"The pumpkin is starving! Quench its thirst!", "Well finally... this is really tasty! More?"}
                        : new String[]{"Aren't you adding water? What's the taste?", "Master, save me... I have no nectar aroma, I must die..."};
                case 3 -> success
                        ? new String[]{"Very good, doing extremely well! Know what you should do next?", "If you catch me, I'll give you 10 million adena! Agreed?"}
                        : new String[]{"I'm hungry, do you want me to dry out?", "I need nectar to grow a little faster."};
                default -> new String[]{};
            };

            if (msgs.length > 0) {
                npc.broadcastPacket(new CreatureSay(objectId, 0, npc.getName(), msgs[Rnd.get(msgs.length)]));
            }
        } else if (nectar == 4) {
            if (success) gourd.addGood();
            handleFifthWatering(npc, player, npcId, objectId);
        }
        return null;
    }

    private void handleFifthWatering(L2NpcInstance npc, L2PcInstance player, int npcId, int objectId) {
        final L2GourdInstance gourd = (L2GourdInstance) npc;
        if (gourd.getGood() >= 3) {
            if (npcId == 12774) {
                L2GourdInstance newGourd = (L2GourdInstance) addSpawn(12775, npc);
                newGourd.setOwner(player.getName());
                scheduleGoodbye("Good By", 120000, newGourd, player);
                npc.broadcastPacket(new CreatureSay(objectId, 0, npc.getName(), "I'll run away in 2 minutes"));
            } else {
                L2GourdInstance newGourd = (L2GourdInstance) addSpawn(12778, npc);
                newGourd.setOwner(player.getName());
                scheduleGoodbye("Good By1", 120000, newGourd, player);
                npc.broadcastPacket(new CreatureSay(objectId, 0, npc.getName(), "I'll run away in 2 minutes"));
            }
            npc.onDecay();
        } else {
            String[] badMsgs = {"Hey! Here goes! Eat! Now! Can't you take proper care? I'll rot!", "Come on, stop? What to thank you for", "Thirsty for nectar oh..."};
            if (npcId == 12774) {
                L2GourdInstance newGourd = (L2GourdInstance) addSpawn(12776, npc);
                newGourd.setOwner(player.getName());
            } else if (npcId == 12777) {
                L2GourdInstance newGourd = (L2GourdInstance) addSpawn(12779, npc);
                newGourd.setOwner(player.getName());
            }
            npc.broadcastPacket(new CreatureSay(objectId, 0, npc.getName(), badMsgs[Rnd.get(badMsgs.length)]));
            npc.onDecay();
        }
    }

    private void scheduleGoodbye(String mainEvent, int mainDelay, L2NpcInstance newGourd, L2PcInstance player) {
        startQuestTimer(mainEvent, mainDelay, newGourd, player);
        startQuestTimer("Good By2", mainDelay - 30000, newGourd, player);
        startQuestTimer("Good By3", mainDelay - 20000, newGourd, player);
        startQuestTimer("Good By4", mainDelay - 10000, newGourd, player);
    }

    @Override
    public String onAttack(L2NpcInstance npc, L2PcInstance player, int damage, boolean isPet) {
        int npcId = npc.getNpcId();
        if (!WATERED_SQUASH.contains(npcId)) return null;

        if (ADULT_LARGE_SQUASH.contains(npcId) && Rnd.get(30) < 2) {
            String[] msgs = {
                    "Ha ha, grown up! Completely for everyone!",
                    "Can't you all aim? Watch out so it doesn't run away...",
                    "I count your hits! Oh, reminds me of another hit!",
                    "Don't waste your time!",
                    "Ha, does that sound really nice to hear?",
                    "I consume your attacks to grow!",
                    "Time to hit again! Hit one more time!",
                    "Only useful music can open a big pumpkin... You can't open me with a weapon!"
            };
            npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), msgs[Rnd.get(msgs.length)]));
        }
        return null;
    }

    @Override
    public String onKill(L2NpcInstance npc, L2PcInstance player, boolean isPet) {
        int npcId = npc.getNpcId();
        if (!WATERED_SQUASH.contains(npcId)) return null;

        int objId = npc.getObjectId();
        if (ADULT_SMALL_SQUASH.contains(npcId) || ADULT_LARGE_SQUASH.contains(npcId)) {
            npc.broadcastPacket(new CreatureSay(objId, 0, npc.getName(), "The pumpkin opens!!"));
            npc.broadcastPacket(new CreatureSay(objId, 0, npc.getName(), "Yay! It opens! Many good things..."));
        } else {
            npc.broadcastPacket(new CreatureSay(objId, 0, npc.getName(), "Why, master?!"));
            npc.broadcastPacket(new CreatureSay(objId, 0, npc.getName(), "Ouch, my guts spilled out!!"));
        }
        return null;
    }


	public static void main(String[] args)
	{
		new Squash();
	}
}
