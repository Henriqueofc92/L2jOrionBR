package l2jorion.game.data.custom;

import java.util.Map;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;

/**
 * Echo Crystals custom script (3995).<br>
 * Exchange music score + adena for echo crystals.
 */
public class Echo extends Quest
{
	private static final int[] NPCS = { 31042, 31043 };
	private static final int ADENA = 57;
	private static final int COST = 200;
	
	// score -> [crystal, msg_ok, msg_noadena, msg_noscore]
	private record ScoreData(int crystal, String ok, String noAdena, String noScore) {}
	
	private static final Map<Integer, ScoreData> LIST = Map.of(
		4410, new ScoreData(4411, "01", "02", "03"),
		4409, new ScoreData(4412, "04", "05", "06"),
		4408, new ScoreData(4413, "07", "08", "09"),
		4420, new ScoreData(4414, "10", "11", "12"),
		4421, new ScoreData(4415, "13", "14", "15"),
		4419, new ScoreData(4417, "16", "05", "06"),
		4418, new ScoreData(4416, "17", "05", "06")
	);
	
	public Echo()
	{
		super(3995, "3995_echo", "custom");
		
		new State("Start", this);
		
		for (var npcId : NPCS)
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		var st = player.getQuestState(getName());
		if (st == null || !event.matches("\\d+"))
		{
			return null;
		}
		
		var score = Integer.parseInt(event);
		var data = LIST.get(score);
		if (data == null)
		{
			st.exitQuest(true);
			return "";
		}
		
		var npcId = String.valueOf(npc.getNpcId());
		String htmltext;
		
		if (st.getQuestItemsCount(score) == 0)
		{
			htmltext = npcId + "-" + data.noScore() + ".htm";
		}
		else if (st.getQuestItemsCount(ADENA) < COST)
		{
			htmltext = npcId + "-" + data.noAdena() + ".htm";
		}
		else
		{
			st.takeItems(ADENA, COST);
			st.giveItems(data.crystal(), 1);
			htmltext = npcId + "-" + data.ok() + ".htm";
		}
		
		st.exitQuest(true);
		return htmltext;
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		return "1.htm";
	}


	public static void main(String[] args)
	{
		new Echo();
	}
}
