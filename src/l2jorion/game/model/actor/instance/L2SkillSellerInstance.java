package l2jorion.game.model.actor.instance;

import l2jorion.game.handler.custom.CustomBypassHandler;
import l2jorion.game.model.entity.SkillSeller;
import l2jorion.game.templates.L2NpcTemplate;

public class L2SkillSellerInstance extends L2NpcInstance
{
	public L2SkillSellerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("custom_"))
		{
			CustomBypassHandler.getInstance().handleBypass(player, command);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		SkillSeller.getInstance().showWelcome(player);
	}
}