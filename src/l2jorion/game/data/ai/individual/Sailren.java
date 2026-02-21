package l2jorion.game.data.ai.individual;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.network.serverpackets.SocialAction;
import l2jorion.game.network.serverpackets.SpecialCamera;

public class Sailren extends Quest
{
	private static final int SAILREN = 29065;
	private static final int VELO = 22196;
	private static final int PTERO = 22199;
	private static final int TREX = 22215;
	
	private L2NpcInstance vlc, ptr, trx, slrn;
	
	public Sailren()
	{
		super(-1, "Sailren", "individual ai");
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		switch (event)
		{
			case "start" -> {
				vlc = addSpawn(VELO, 27845, -5567, -1982, 45000, false, 0);
				startQuestTimer("camera", 2000, vlc, player);
				cancelQuestTimer("start", npc, null);
			}
			case "round2" -> {
				ptr = addSpawn(PTERO, 27838, -5578, -1982, 45000, false, 0);
				startQuestTimer("camera", 2000, ptr, player);
				cancelQuestTimer("round2", npc, null);
			}
			case "round3" -> {
				trx = addSpawn(TREX, 27838, -5578, -1982, 45000, false, 0);
				startQuestTimer("camera", 2000, trx, player);
				cancelQuestTimer("round3", npc, null);
			}
			case "sailren" -> {
				slrn = addSpawn(SAILREN, 27489, -6223, -1982, 45000, false, 0);
				startQuestTimer("camera", 2000, slrn, player);
				startQuestTimer("vkrovatku", 1200000, slrn, null);
				cancelQuestTimer("round4", npc, null);
			}
			case "camera" -> {
				if (player != null && npc != null)
				{
					player.broadcastPacket(new SpecialCamera(npc.getObjectId(), 400, -75, 3, -150, 5000));
					npc.broadcastPacket(new SocialAction(npc.getObjectId(), 1));
				}
			}
			case "open" -> {
				deleteGlobalQuestVar("close");
				cancelQuestTimer("open", npc, null);
			}
			case "vkrovatku" -> {
				if (npc != null)
				{
					npc.deleteMe();
				}
				deleteGlobalQuestVar("close");
				cancelQuestTimer("open", npc, null);
				cancelQuestTimer("vkrovatku", npc, null);
			}
		}
		return null;
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		return null;
	}


	public static void main(String[] args)
	{
		new Sailren();
	}
}
