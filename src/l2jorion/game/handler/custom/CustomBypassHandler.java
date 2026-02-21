/*
 * L2jOrion Project - www.l2jorion.com 
 * * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 */
package l2jorion.game.handler.custom;

import java.util.HashMap;
import java.util.Map;

import l2jorion.game.handler.ICustomByPassHandler;
import l2jorion.game.handler.voice.AutoFarm;
import l2jorion.game.handler.voice.DressMe;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.entity.SkillSeller;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;

public class CustomBypassHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(CustomBypassHandler.class);
	
	private static CustomBypassHandler _instance = null;
	private final Map<String, ICustomByPassHandler> _handlers;
	
	private CustomBypassHandler()
	{
		_handlers = new HashMap<>();
		registerCustomBypassHandler(new DressMe());
		registerCustomBypassHandler(new AutoFarm());
	}
	
	public static CustomBypassHandler getInstance()
	{
		if (_instance == null)
		{
			_instance = new CustomBypassHandler();
		}
		
		return _instance;
	}
	
	public void registerCustomBypassHandler(final ICustomByPassHandler handler)
	{
		for (final String s : handler.getByPassCommands())
		{
			_handlers.put(s, handler);
		}
	}
	
	public void handleBypass(final L2PcInstance player, final String command)
	{
		String cmd = "";
		String params = "";
		final int iPos = command.indexOf(" ");
		
		if (iPos != -1)
		{
			cmd = command.substring(7, iPos);
			params = command.substring(iPos + 1);
		}
		else
		{
			cmd = command.substring(7);
		}
		
		final ICustomByPassHandler ch = _handlers.get(cmd);
		
		if (ch != null)
		{
			ch.handleCommand(cmd, player, params);
		}
		else
		{
			if (command.startsWith("custom_skillseller"))
			{
				if (command.startsWith("custom_skillseller_buy_"))
				{
					try
					{
						String[] parts = command.split("_");
						int cId = Integer.parseInt(parts[3]);
						int idx = Integer.parseInt(parts[4]);
						int page = (parts.length > 5) ? Integer.parseInt(parts[5]) : 1;
						
						SkillSeller.getInstance().buySkill(player, cId, idx, page);
					}
					catch (Exception e)
					{
						LOG.warn("Invalid SkillSeller buy bypass: " + command);
					}
				}
				else if (command.startsWith("custom_skillseller_list"))
				{
					int page = 1;
					try
					{
						if (!params.isEmpty())
						{
							page = Integer.parseInt(params.trim());
						}
					}
					catch (Exception e)
					{
					}
					
					SkillSeller.getInstance().showSkillList(player, page);
				}
				else
				{
					SkillSeller.getInstance().showWelcome(player);
				}
			}
		}
	}
}