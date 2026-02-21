package l2jorion.game.data.custom;

import java.util.Map;

import l2jorion.game.datatables.SkillTable;
import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;
import l2jorion.game.network.serverpackets.ActionFailed;
import l2jorion.game.network.serverpackets.WareHouseWithdrawalList;

/**
 * Ketra Orc Support custom script (6050).<br>
 * Faction support: buffs, warehouse, teleport for Ketra alliance players.
 */
public class KetraOrcSupport extends Quest
{
	// NPCs
	private static final int KADUN = 31370;  // Hierarch
	private static final int WAHKAN = 31371; // Messenger
	private static final int ASEFA = 31372;  // Soul Guide
	private static final int ATAN = 31373;   // Grocer
	private static final int JAFF = 31374;   // Warehouse Keeper
	private static final int JUMARA = 31375; // Trader
	private static final int KURFA = 31376;  // Gate Keeper
	
	private static final int HORN = 7186;
	
	// buffKey -> [skillId, skillLevel, hornCost]
	private record BuffData(int skillId, int level, int cost) {}
	
	private static final Map<String, BuffData> BUFF = Map.of(
		"1", new BuffData(4359, 1, 2), // Focus
		"2", new BuffData(4360, 1, 2), // Death Whisper
		"3", new BuffData(4345, 1, 3), // Might
		"4", new BuffData(4355, 1, 3), // Acumen
		"5", new BuffData(4352, 1, 3), // Berserker
		"6", new BuffData(4354, 1, 3), // Vampiric Rage
		"7", new BuffData(4356, 1, 6), // Empower
		"8", new BuffData(4357, 1, 6)  // Haste
	);
	
	public KetraOrcSupport()
	{
		super(6050, "6050_KetraOrcSupport", "custom");
		
		var created = new State("Start", this);
		new State("Started", this);
		setInitialState(created);
		
		for (int npcId = 31370; npcId <= 31376; npcId++)
		{
			addFirstTalkId(npcId);
		}
		addTalkId(ASEFA);
		addTalkId(KURFA);
		addTalkId(JAFF);
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		var htmltext = event;
		var st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		var alevel = player.getAllianceWithVarkaKetra();
		var buff = BUFF.get(event);
		
		if (buff != null)
		{
			if (st.getQuestItemsCount(HORN) >= buff.cost())
			{
				st.takeItems(HORN, buff.cost());
				npc.setTarget(player);
				npc.doCast(SkillTable.getInstance().getInfo(buff.skillId(), buff.level()));
				npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
				htmltext = "31372-4.htm";
			}
		}
		else if ("Withdraw".equals(event))
		{
			if (player.getWarehouse().getSize() == 0)
			{
				htmltext = "31374-0.htm";
			}
			else
			{
				player.sendPacket(new ActionFailed());
				player.setActiveWarehouse(player.getWarehouse());
				player.sendPacket(new WareHouseWithdrawalList(player, 1));
			}
		}
		else if ("Teleport".equals(event))
		{
			if (alevel == 4)
			{
				htmltext = "31376-4.htm";
			}
			else if (alevel == 5)
			{
				htmltext = "31376-5.htm";
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2NpcInstance npc, L2PcInstance player)
	{
		var st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		var npcId = npc.getNpcId();
		var alevel = player.getAllianceWithVarkaKetra();
		var horns = st.getQuestItemsCount(HORN);
		
		if (npcId == KADUN)
		{
			return alevel > 0 ? "31370-friend.htm" : "31370-no.htm";
		}
		else if (npcId == WAHKAN)
		{
			return alevel > 0 ? "31371-friend.htm" : "31371-no.htm";
		}
		else if (npcId == ASEFA)
		{
			st.setState(getStateByName("Started"));
			if (alevel < 1)
			{
				return "31372-3.htm";
			}
			else if (alevel < 3)
			{
				return "31372-1.htm";
			}
			else
			{
				return horns > 0 ? "31372-4.htm" : "31372-2.htm";
			}
		}
		else if (npcId == ATAN)
		{
			if (player.getKarma() >= 1)
			{
				return "31373-pk.htm";
			}
			else if (alevel <= 0)
			{
				return "31373-no.htm";
			}
			else if (alevel == 1 || alevel == 2)
			{
				return "31373-1.htm";
			}
			else
			{
				return "31373-2.htm";
			}
		}
		else if (npcId == JAFF)
		{
			if (alevel <= 0)
			{
				return "31374-no.htm";
			}
			else if (alevel == 1)
			{
				return "31374-1.htm";
			}
			else if (player.getWarehouse().getSize() == 0)
			{
				return "31374-3.htm";
			}
			else if (alevel == 2 || alevel == 3)
			{
				return "31374-2.htm";
			}
			else
			{
				return "31374-4.htm";
			}
		}
		else if (npcId == JUMARA)
		{
			if (alevel == 2)
			{
				return "31375-1.htm";
			}
			else if (alevel == 3 || alevel == 4)
			{
				return "31375-2.htm";
			}
			else if (alevel == 5)
			{
				return "31375-3.htm";
			}
			else
			{
				return "31375-no.htm";
			}
		}
		else if (npcId == KURFA)
		{
			if (alevel <= 0)
			{
				return "31376-no.htm";
			}
			else if (alevel < 4)
			{
				return "31376-1.htm";
			}
			else if (alevel == 4)
			{
				return "31376-2.htm";
			}
			else
			{
				return "31376-3.htm";
			}
		}
		
		return "<html><body>You are either not carrying out your quest or don't meet the criteria.</body></html>";
	}


	public static void main(String[] args)
	{
		new KetraOrcSupport();
	}
}
