package l2jorion.game.data.teleports;

import l2jorion.Config;
import l2jorion.game.ai.additional.invidual.Antharas;
import l2jorion.game.datatables.xml.DoorTable;
import l2jorion.game.managers.GrandBossManager;
import l2jorion.game.managers.QuestManager;
import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;
import l2jorion.util.random.Rnd;

/**
 * Grand Boss Teleporters (6000).<br>
 * Manages entry/exit for Antharas and Valakas lairs.
 */
public class GrandBossTeleporters extends Quest
{
	// NPCs
	private static final int HEART_OF_WARDING = 13001;
	private static final int ANTHARAS_CUBIC = 31859;
	private static final int GATEKEEPER_FIRE_DRAGON = 31384;
	private static final int HEART_OF_VOLCANO = 31385;
	private static final int KLEIN = 31540;
	private static final int GATEKEEPER_DOOR1 = 31686;
	private static final int GATEKEEPER_DOOR2 = 31687;
	private static final int VALAKAS_CUBIC = 31759;
	
	// Bosses
	private static final int ANTHARAS = 29019;
	private static final int VALAKAS = 29028;
	
	// Items
	private static final int PORTAL_STONE = 3865;
	private static final int FLOATING_STONE = 7267;
	
	// Doors
	private static final int DOOR_FIRE_DRAGON = 24210004;
	private static final int DOOR_VOLCANO_1 = 24210006;
	private static final int DOOR_VOLCANO_2 = 24210005;
	
	private Quest antharasAI;
	private Quest valakasAI;
	private int count;
	
	public GrandBossTeleporters()
	{
		super(-1, "6000_GrandBossTeleporters", "Teleports");
		
		antharasAI = QuestManager.getInstance().getQuest("antharas");
		valakasAI = QuestManager.getInstance().getQuest("valakas");
		count = 0;
		
		var created = new State("Start", this);
		setInitialState(created);
		
		for (var npcId : new int[]{ HEART_OF_WARDING, ANTHARAS_CUBIC, GATEKEEPER_FIRE_DRAGON, HEART_OF_VOLCANO, KLEIN, GATEKEEPER_DOOR1, GATEKEEPER_DOOR2, VALAKAS_CUBIC })
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		var st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		if ("31540".equals(event))
		{
			if (st.getQuestItemsCount(FLOATING_STONE) > 0)
			{
				st.takeItems(FLOATING_STONE, 1);
				player.teleToLocation(183813, -115157, -3303);
				st.set("allowEnter", "1");
				return null;
			}
			return "31540-06.htm";
		}
		return null;
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		int npcId = npc.getNpcId();
		
		switch (npcId)
		{
			case HEART_OF_WARDING ->
			{
				if (antharasAI != null)
				{
					int status = GrandBossManager.getInstance().getBossStatus(ANTHARAS);
					if (status == 2)
					{
						return "13001-02.htm";
					}
					if (status == 3)
					{
						return "13001-01.htm";
					}
					if (status == 0 || status == 1)
					{
						var st = player.getQuestState(getName());
						if (st != null && st.getQuestItemsCount(PORTAL_STONE) > 0)
						{
							st.takeItems(PORTAL_STONE, 1);
							var zone = GrandBossManager.getInstance().getZone(179700, 113800, -7709);
							if (zone != null)
							{
								zone.allowPlayerEntry(player, 30);
							}
							int x = 179700 + Rnd.get(700);
							int y = 113800 + Rnd.get(2100);
							player.teleToLocation(x, y, -7709);
							if (status == 0)
							{
								((Antharas) antharasAI).setAntharasSpawnTask();
							}
							return null;
						}
						return "13001-03.htm";
					}
				}
			}
			case ANTHARAS_CUBIC ->
			{
				int x = 79800 + Rnd.get(600);
				int y = 151200 + Rnd.get(1100);
				player.teleToLocation(x, y, -3534);
				return null;
			}
			case HEART_OF_VOLCANO ->
			{
				if (valakasAI != null)
				{
					int status = GrandBossManager.getInstance().getBossStatus(VALAKAS);
					if (status == 0 || status == 1)
					{
						var st = player.getQuestState(getName());
						if (st == null)
						{
							return null;
						}
						if (count >= 200)
						{
							return "31385-03.htm";
						}
						if (st.getInt("allowEnter") == 1)
						{
							st.unset("allowEnter");
							var zone = GrandBossManager.getInstance().getZone(212852, -114842, -1632);
							if (zone != null)
							{
								zone.allowPlayerEntry(player, 30);
							}
							int x = 204328 + Rnd.get(600);
							int y = -111874 + Rnd.get(600);
							player.teleToLocation(x, y, 70);
							count++;
							if (status == 0)
							{
								var valakas = GrandBossManager.getInstance().getBoss(VALAKAS);
								valakasAI.startQuestTimer("lock_entry_and_spawn_valakas", 60000L * Config.VALAKAS_WAIT_TIME, valakas, null);
								GrandBossManager.getInstance().setBossStatus(VALAKAS, 1);
							}
							return null;
						}
						return "31385-04.htm";
					}
					if (status == 2)
					{
						return "31385-02.htm";
					}
					return "31385-01.htm";
				}
				return "31385-01.htm";
			}
			case GATEKEEPER_FIRE_DRAGON ->
			{
				DoorTable.getInstance().getDoor(DOOR_FIRE_DRAGON).openMe();
				return null;
			}
			case GATEKEEPER_DOOR1 ->
			{
				DoorTable.getInstance().getDoor(DOOR_VOLCANO_1).openMe();
				return null;
			}
			case GATEKEEPER_DOOR2 ->
			{
				DoorTable.getInstance().getDoor(DOOR_VOLCANO_2).openMe();
				return null;
			}
			case KLEIN ->
			{
				if (count < 50)
				{
					return "31540-01.htm";
				}
				if (count < 100)
				{
					return "31540-02.htm";
				}
				if (count < 150)
				{
					return "31540-03.htm";
				}
				if (count < 200)
				{
					return "31540-04.htm";
				}
				return "31540-05.htm";
			}
			case VALAKAS_CUBIC ->
			{
				int x = 150037 + Rnd.get(500);
				int y = -57720 + Rnd.get(500);
				player.teleToLocation(x, y, -2976);
				return null;
			}
		}
		return null;
	}


	public static void main(String[] args)
	{
		new GrandBossTeleporters();
	}
}
