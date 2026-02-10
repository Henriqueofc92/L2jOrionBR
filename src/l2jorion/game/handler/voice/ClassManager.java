package l2jorion.game.handler.voice;

import l2jorion.Config;
import l2jorion.game.handler.ICustomByPassHandler;
import l2jorion.game.handler.IVoicedCommandHandler;
import l2jorion.game.model.actor.instance.L2ClassMasterInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;

public class ClassManager implements IVoicedCommandHandler, ICustomByPassHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"class"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance player, String target)
	{
		if (player == null)
		{
			return false;
		}
		
		if ("class".equals(command))
		{
			if (Config.ALLOW_CLASS_MASTERS && Config.ALLOW_REMOTE_CLASS_MASTERS)
			{
				L2ClassMasterInstance master_instance = L2ClassMasterInstance.getInstance();
				if (master_instance != null)
				{
					L2ClassMasterInstance.getInstance().showMainWindow(player);
				}
			}
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
	
	@Override
	public String[] getByPassCommands()
	{
		return new String[]
		{
			"class_menu"
		};
	}
	
	@Override
	public void handleCommand(String command, L2PcInstance player, String parameters)
	{
		if ("class_menu".equals(command))
		{
			if (Config.ALLOW_CLASS_MASTERS)
			{
				L2ClassMasterInstance master_instance = L2ClassMasterInstance.getInstance();
				if (master_instance != null)
				{
					L2ClassMasterInstance.getInstance().showMainWindow(player);
				}
			}
		}
	}
}
