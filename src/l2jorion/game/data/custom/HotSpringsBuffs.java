package l2jorion.game.data.custom;

import java.util.Map;

import l2jorion.game.datatables.SkillTable;
import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.util.random.Rnd;

/**
 * Hot Springs Buffs custom script (8009).<br>
 * Applies disease debuffs on attack in the Hot Springs area.
 */
public class HotSpringsBuffs extends Quest
{
	// Disease skill IDs
	private static final int RHEUMATISM = 4551;
	private static final int CHOLERA = 4552;
	private static final int FLU = 4553;
	private static final int MALARIA = 4554;
	
	// NPC -> [disease1, chance1, disease2, chance2]
	// Diseases applied: first or second randomly
	private record DiseaseData(int disease1, int chance1, int disease2, int chance2) {}
	
	private static final Map<Integer, DiseaseData> NPC_DISEASES = Map.of(
		21316, new DiseaseData(CHOLERA, 30, MALARIA, 15),
		21317, new DiseaseData(MALARIA, 15, FLU, 15),
		21319, new DiseaseData(CHOLERA, 30, MALARIA, 15),
		21321, new DiseaseData(RHEUMATISM, 30, MALARIA, 15),
		21314, new DiseaseData(RHEUMATISM, 30, MALARIA, 15),
		21322, new DiseaseData(MALARIA, 15, FLU, 15)
	);
	
	public HotSpringsBuffs()
	{
		super(8009, "8009_HotSpringsBuffs", "custom");
		
		for (var npcId : NPC_DISEASES.keySet())
		{
			addAttackId(npcId);
		}
	}
	
	@Override
	public String onAttack(L2NpcInstance npc, L2PcInstance player, int damage, boolean isPet)
	{
		var npcId = npc.getNpcId();
		var data = NPC_DISEASES.get(npcId);
		if (data == null)
		{
			return null;
		}
		
		if (Rnd.get(2) == 1)
		{
			if (Rnd.get(2) == 1)
			{
				applyDisease(npc, player, data.disease1(), data.chance1());
			}
			else
			{
				applyDisease(npc, player, data.disease2(), data.chance2());
			}
		}
		
		return null;
	}
	
	private void applyDisease(L2NpcInstance npc, L2PcInstance player, int skillId, int chance)
	{
		var effect = player.getFirstEffect(skillId);
		if (effect != null)
		{
			var currentLevel = effect.getLevel();
			if (Rnd.get(100) < chance && currentLevel < 10)
			{
				npc.setTarget(player);
				npc.doCast(SkillTable.getInstance().getInfo(skillId, currentLevel + 1));
			}
		}
		else
		{
			npc.setTarget(player);
			npc.doCast(SkillTable.getInstance().getInfo(skillId, 1));
		}
	}


	public static void main(String[] args)
	{
		new HotSpringsBuffs();
	}
}
