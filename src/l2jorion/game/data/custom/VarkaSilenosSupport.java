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
 * Varka Silenos Support custom script (6051).<br>
 * Faction support: buffs, warehouse, teleport for Varka alliance players.
 */
public class VarkaSilenosSupport extends Quest
{
	// NPCs
	private static final int ASHAS = 31377;  // Hierarch
	private static final int NARAN = 31378;  // Messenger
	private static final int UDAN = 31379;   // Buffer
	private static final int DIYABU = 31380; // Grocer
	private static final int HAGOS = 31381;  // Warehouse Keeper
	private static final int SHIKON = 31382; // Trader
	private static final int TERANU = 31383; // Teleporter
	
	private static final int SEED = 7187;
	
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
	
	public VarkaSilenosSupport()
	{
		super(6051, "6051_VarkaSilenosSupport", "custom");
		
		var created = new State("Start", this);
		new State("Started", this);
		setInitialState(created);
		
		for (int npcId = 31377; npcId <= 31383; npcId++)
		{
			addFirstTalkId(npcId);
		}
		addTalkId(UDAN);
		addTalkId(HAGOS);
		addTalkId(TERANU);
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
			if (st.getQuestItemsCount(SEED) >= buff.cost())
			{
				st.takeItems(SEED, buff.cost());
				npc.setTarget(player);
				npc.doCast(SkillTable.getInstance().getInfo(buff.skillId(), buff.level()));
				npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
				htmltext = "31379-4.htm";
			}
		}
		else if ("Withdraw".equals(event))
		{
			if (player.getWarehouse().getSize() == 0)
			{
				htmltext = "31381-0.htm";
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
			if (alevel == -4)
			{
				htmltext = "31383-4.htm";
			}
			else if (alevel == -5)
			{
				htmltext = "31383-5.htm";
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
		var seeds = st.getQuestItemsCount(SEED);
		
		if (npcId == ASHAS)
		{
			return alevel < 0 ? "31377-friend.htm" : "31377-no.htm";
		}
		else if (npcId == NARAN)
		{
			return alevel < 0 ? "31378-friend.htm" : "31378-no.htm";
		}
		else if (npcId == UDAN)
		{
			st.setState(getStateByName("Started"));
			if (alevel > -1)
			{
				return "31379-3.htm";
			}
			else if (alevel > -3)
			{
				return "31379-1.htm";
			}
			else
			{
				return seeds > 0 ? "31379-4.htm" : "31379-2.htm";
			}
		}
		else if (npcId == DIYABU)
		{
			if (player.getKarma() >= 1)
			{
				return "31380-pk.htm";
			}
			else if (alevel >= 0)
			{
				return "31380-no.htm";
			}
			else if (alevel == -1 || alevel == -2)
			{
				return "31380-1.htm";
			}
			else
			{
				return "31380-2.htm";
			}
		}
		else if (npcId == HAGOS)
		{
			if (alevel > 0)
			{
				return "31381-no.htm";
			}
			else if (alevel == -1)
			{
				return "31381-1.htm";
			}
			else if (player.getWarehouse().getSize() == 0)
			{
				return "31381-3.htm";
			}
			else if (alevel == -2 || alevel == -3)
			{
				return "31381-2.htm";
			}
			else
			{
				return "31381-4.htm";
			}
		}
		else if (npcId == SHIKON)
		{
			if (alevel == -2)
			{
				return "31382-1.htm";
			}
			else if (alevel == -3 || alevel == -4)
			{
				return "31382-2.htm";
			}
			else if (alevel == -5)
			{
				return "31382-3.htm";
			}
			else
			{
				return "31382-no.htm";
			}
		}
		else if (npcId == TERANU)
		{
			if (alevel >= 0)
			{
				return "31383-no.htm";
			}
			else if (alevel > -4)
			{
				return "31383-1.htm";
			}
			else if (alevel == -4)
			{
				return "31383-2.htm";
			}
			else
			{
				return "31383-3.htm";
			}
		}
		
		return "<html><body>You are either not carrying out your quest or don't meet the criteria.</body></html>";
	}


	public static void main(String[] args)
	{
		new VarkaSilenosSupport();
	}
}
