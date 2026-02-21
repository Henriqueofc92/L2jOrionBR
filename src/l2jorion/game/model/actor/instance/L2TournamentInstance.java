package l2jorion.game.model.actor.instance;

import l2jorion.game.ai.CtrlIntention;
import l2jorion.game.model.entity.event.tournament.Tournament;
import l2jorion.game.network.serverpackets.ActionFailed;
import l2jorion.game.network.serverpackets.MoveToPawn;
import l2jorion.game.network.serverpackets.MyTargetSelected;
import l2jorion.game.network.serverpackets.NpcHtmlMessage;
import l2jorion.game.network.serverpackets.ValidateLocation;
import l2jorion.game.templates.L2NpcTemplate;

public class L2TournamentInstance extends L2NpcInstance
{
	public L2TournamentInstance(int objectId, L2NpcTemplate template)
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
		super.onBypassFeedback(player, command);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		if (Tournament.is_inProgress())
		{
			Tournament.showMessageWindow(player);
		}
		else
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setHtml("<html><body><title>Tournament Event</title><center><br>O Tournament Event não está ativo no momento.<br>Aguarde o próximo horário.</center></body></html>");
			player.sendPacket(html);
		}
	}
}