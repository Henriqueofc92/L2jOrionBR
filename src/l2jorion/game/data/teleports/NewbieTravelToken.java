package l2jorion.game.data.teleports;

import java.util.Map;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.QuestState;
import l2jorion.game.model.quest.State;

/**
 * Newbie Travel Token (1104).<br>
 * Consumes token (8542) to teleport newbies (< lv20) to starting villages.
 */
public class NewbieTravelToken extends Quest
{
	private static final int TOKEN = 8542;
	
	private static final Map<Integer, int[]> DATA = Map.of(
		30600, new int[]{ 12160, 16554, -4583 },   // DE
		30601, new int[]{ 115594, -177993, -912 },  // DW
		30599, new int[]{ 45470, 48328, -3059 },    // EV
		30602, new int[]{ -45067, -113563, -199 },  // OV
		30598, new int[]{ -84053, 243343, -3729 }   // TI
	);
	
	public NewbieTravelToken()
	{
		super(1104, "1104_NewbieTravelToken", "Teleports");
		
		var created = new State("Start", this);
		setInitialState(created);
		
		for (var npcId : DATA.keySet())
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}
	}
	
	@Override
	public String onEvent(String event, QuestState st)
	{
		if (event.matches("\\d+"))
		{
			var dest = Integer.parseInt(event);
			var loc = DATA.get(dest);
			if (loc != null)
			{
				if (st.getQuestItemsCount(TOKEN) > 0)
				{
					st.takeItems(TOKEN, 1);
					st.getPlayer().teleToLocation(loc[0], loc[1], loc[2]);
				}
				else
				{
					st.exitQuest(true);
					return "Incorrect item count";
				}
			}
		}
		st.exitQuest(true);
		return null;
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		var st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		if (player.getLevel() >= 20)
		{
			st.exitQuest(true);
			return "1.htm";
		}
		
		return npc.getNpcId() + ".htm";
	}


	public static void main(String[] args)
	{
		new NewbieTravelToken();
	}
}
