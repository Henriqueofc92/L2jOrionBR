package l2jorion.game.model.actor.instance;

import l2jorion.game.ai.CtrlIntention;
import l2jorion.game.model.entity.event.dungeon.DungeonManager;
import l2jorion.game.network.serverpackets.ActionFailed;
import l2jorion.game.network.serverpackets.MoveToPawn;
import l2jorion.game.network.serverpackets.MyTargetSelected;
import l2jorion.game.network.serverpackets.NpcHtmlMessage;
import l2jorion.game.network.serverpackets.ValidateLocation;
import l2jorion.game.templates.L2NpcTemplate;

public class L2DungeonInstance extends L2NpcInstance
{
	public L2DungeonInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
		{
			return;
		}
		
		if (this != player.getTarget())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			if (!canInteract(player))
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				player.broadcastPacket(new MoveToPawn(player, this, L2NpcInstance.INTERACTION_DISTANCE));
				showChatWindow(player);
			}
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("dgevent"))
		{
			if (DungeonManager.DG_EVENT_ENABLED)
			{
				DungeonManager.getInstance().showHtml(player);
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		if (DungeonManager.DG_EVENT_ENABLED)
		{
			DungeonManager.getInstance().showHtml(player);
		}
		else
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setHtml("<html><body><title>Dungeon Event</title><center><br>O evento Dungeon esta desativado no momento.</center></body></html>");
			player.sendPacket(html);
		}
	}
}