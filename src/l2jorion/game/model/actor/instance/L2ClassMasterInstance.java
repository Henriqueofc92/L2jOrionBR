package l2jorion.game.model.actor.instance;

import java.util.Map;

import l2jorion.Config;
import l2jorion.game.ai.CtrlIntention;
import l2jorion.game.datatables.xml.CharTemplateTable;
import l2jorion.game.datatables.sql.ItemTable;
import l2jorion.game.model.L2Character;
import l2jorion.game.model.base.ClassId;
import l2jorion.game.model.base.ClassLevel;
import l2jorion.game.model.base.PlayerClass;
import l2jorion.game.network.SystemMessageId;
import l2jorion.game.network.serverpackets.ActionFailed;
import l2jorion.game.network.serverpackets.MoveToPawn;
import l2jorion.game.network.serverpackets.NpcHtmlMessage;
import l2jorion.game.network.serverpackets.SocialAction;
import l2jorion.game.network.serverpackets.SystemMessage;
import l2jorion.game.templates.L2NpcTemplate;
import l2jorion.game.util.Util;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;
import l2jorion.util.random.Rnd;

public final class L2ClassMasterInstance extends L2FolkInstance
{
	protected static final Logger LOG = LoggerFactory.getLogger(L2ClassMasterInstance.class);
	private static final String HTML_FOLDER = "data/html/classmaster/";
	
	private static L2ClassMasterInstance _instance;
	
	public L2ClassMasterInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
		_instance = this;
		setName(template.getName());
	}
	
	public static L2ClassMasterInstance getInstance()
	{
		return _instance;
	}
	
	@Override
	public void onAction(final L2PcInstance player)
	{
		if (!canTarget(player))
		{
			return;
		}
		
		if (this != player.getTarget())
		{
			player.setTarget(this);
		}
		else
		{
			if (!canInteract(player))
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				if (player.isMoving())
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, this);
				}
				
				player.broadcastPacket(new MoveToPawn(player, this, L2NpcInstance.INTERACTION_DISTANCE));
				broadcastPacket(new SocialAction(getObjectId(), Rnd.get(8)));
				
				showMainWindow(player);
			}
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void showMainWindow(final L2PcInstance player)
	{
		ClassId classId = player.getClassId();
		int jobLevel = 0;
		int level = player.getLevel();
		ClassLevel lvl = PlayerClass.values()[classId.getId()].getLevel();
		
		switch (lvl)
		{
			case First:
				jobLevel = 1;
				break;
			case Second:
				jobLevel = 2;
				break;
			case Third:
				jobLevel = 3;
				break;
			default:
				jobLevel = 4;
		}
		boolean canChangeClass = false;
		if ((level >= 20 && jobLevel == 1 && Config.ALLOW_CLASS_MASTERS_FIRST_CLASS) || (level >= 40 && jobLevel == 2 && Config.ALLOW_CLASS_MASTERS_SECOND_CLASS) || (level >= 76 && jobLevel == 3 && Config.ALLOW_CLASS_MASTERS_THIRD_CLASS))
		{
			canChangeClass = true;
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		
		if (canChangeClass)
		{
			html.setFile(HTML_FOLDER + "change_class.htm");
			StringBuilder sbClasses = new StringBuilder();
			for (ClassId child : ClassId.values())
			{
				if (child.childOf(classId) && child.level() == jobLevel)
				{
					String className = CharTemplateTable.getClassNameById(child.getId());
					sbClasses.append("<button value=\"").append(className).append("\" action=\"bypass -h npc_").append(getObjectId()).append("_change_class ").append(child.getId()).append("\" width=134 height=21 back=\"L2UI_CH3.bigbutton3_down\" fore=\"L2UI_CH3.bigbutton3\"><br>");
				}
			}
			html.replace("%class_list%", sbClasses.toString());
			StringBuilder sbCost = new StringBuilder();
			Map<Integer, Integer> requiredItems = player.isMageClass() ? Config.CLASS_MASTER_SETTINGS_MAGE.getRequireItems(jobLevel) : Config.CLASS_MASTER_SETTINGS_FIGHT.getRequireItems(jobLevel);
			
			if (requiredItems != null && !requiredItems.isEmpty())
			{
				sbCost.append("<table width=220>");
				for (Map.Entry<Integer, Integer> entry : requiredItems.entrySet())
				{
					String itemName = ItemTable.getInstance().getTemplate(entry.getKey()).getName();
					String count = Util.formatAdena(entry.getValue());
					sbCost.append("<tr><td><font color=\"LEVEL\">").append(count).append("</font></td><td>").append(itemName).append("</td></tr>");
				}
				sbCost.append("</table>");
			}
			else
			{
				sbCost.append("<font color=\"00FF00\">Free of Charge</font>");
			}
			html.replace("%cost_list%", sbCost.toString());
		}
		else
		{
			if (jobLevel == 4)
			{
				html.setFile(HTML_FOLDER + "nomore.htm");
			}
			else
			{
				html.setFile(HTML_FOLDER + "index.htm");
				int nextLvl = (jobLevel == 1) ? 20 : (jobLevel == 2) ? 40 : 76;
				html.replace("%req_level%", String.valueOf(nextLvl));
			}
		}
		if (Config.CLASS_MASTER_STRIDER_UPDATE)
		{
			html.replace("%strider_option%", "<br><button value=\"Upgrade Hatchling\" action=\"bypass -h npc_" + getObjectId() + "_upgrade_hatchling\" width=134 height=21 back=\"L2UI_CH3.bigbutton3_down\" fore=\"L2UI_CH3.bigbutton3\">");
		}
		else
		{
			html.replace("%strider_option%", "");
		}
		
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	@Override
	public void onBypassFeedback(final L2PcInstance player, final String command)
	{
		if (command.startsWith("change_class"))
		{
			int val = Integer.parseInt(command.substring(13));
			
			ClassId classId = player.getClassId();
			int level = player.getLevel();
			int jobLevel = 0;
			int newJobLevel = 0;
			
			ClassLevel lvlnow = PlayerClass.values()[classId.getId()].getLevel();
			switch (lvlnow)
			{
				case First:
					jobLevel = 1;
					break;
				case Second:
					jobLevel = 2;
					break;
				case Third:
					jobLevel = 3;
					break;
				default:
					jobLevel = 4;
			}
			
			if (jobLevel == 4)
			{
				return;
			}
			
			ClassLevel lvlnext = PlayerClass.values()[val].getLevel();
			switch (lvlnext)
			{
				case First:
					newJobLevel = 1;
					break;
				case Second:
					newJobLevel = 2;
					break;
				case Third:
					newJobLevel = 3;
					break;
				default:
					newJobLevel = 4;
			}
			if (newJobLevel != jobLevel + 1)
			{
				return;
			}
			if (level < 20 && newJobLevel > 1)
			{
				return;
			}
			if (level < 40 && newJobLevel > 2)
			{
				return;
			}
			if (level < 76 && newJobLevel > 3)
			{
				return;
			}
			
			if (newJobLevel == 2 && !Config.ALLOW_CLASS_MASTERS_FIRST_CLASS)
			{
				return;
			}
			if (newJobLevel == 3 && !Config.ALLOW_CLASS_MASTERS_SECOND_CLASS)
			{
				return;
			}
			if (newJobLevel == 4 && !Config.ALLOW_CLASS_MASTERS_THIRD_CLASS)
			{
				return;
			}
			Map<Integer, Integer> requiredItems;
			Map<Integer, Integer> rewardItems;
			
			if (player.isMageClass())
			{
				requiredItems = Config.CLASS_MASTER_SETTINGS_MAGE.getRequireItems(jobLevel);
				rewardItems = Config.CLASS_MASTER_SETTINGS_MAGE.getRewardItems(jobLevel);
			}
			else
			{
				requiredItems = Config.CLASS_MASTER_SETTINGS_FIGHT.getRequireItems(jobLevel);
				rewardItems = Config.CLASS_MASTER_SETTINGS_FIGHT.getRewardItems(jobLevel);
			}
			for (Map.Entry<Integer, Integer> entry : requiredItems.entrySet())
			{
				if (player.getInventory().getInventoryItemCount(entry.getKey(), -1) < entry.getValue())
				{
					player.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					return;
				}
			}
			for (Map.Entry<Integer, Integer> entry : requiredItems.entrySet())
			{
				player.destroyItemByItemId("ClassMaster", entry.getKey(), entry.getValue(), player, true);
			}
			for (Map.Entry<Integer, Integer> entry : rewardItems.entrySet())
			{
				player.addItem("ClassMaster", entry.getKey(), entry.getValue(), player, true);
			}
			changeClass(player, val);
			player.rewardSkills();
			
			if (!Config.ALT_GAME_SKILL_LEARN)
			{
				player.checkAllowedSkills();
			}
			
			player.sendPacket(new SystemMessage(val >= 88 ? SystemMessageId.THIRD_CLASS_TRANSFER : SystemMessageId.CLASS_TRANSFER));
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(HTML_FOLDER + "ok.htm");
			html.replace("%new_class%", CharTemplateTable.getClassNameById(player.getClassId().getId()));
			player.sendPacket(html);
			
			if (Config.ALLOW_REMOTE_CLASS_MASTERS_HEAL)
			{
				player.getStatus().setCurrentCp(player.getMaxCp());
				player.getStatus().setCurrentMp(player.getMaxMp());
				player.getStatus().setCurrentHp(player.getMaxHp());
			}
		}
		else if (command.startsWith("upgrade_hatchling") && Config.CLASS_MASTER_STRIDER_UPDATE)
		{
			boolean canUpgrade = false;
			if (player.getPet() != null)
			{
				int npcId = player.getPet().getNpcId();
				if (npcId == 12311 || npcId == 12312 || npcId == 12313)
				{
					if (player.getPet().getLevel() >= 55)
					{
						canUpgrade = true;
					}
					else
					{
						player.sendMessage("The level of your hatchling is too low.");
					}
				}
				else
				{
					player.sendMessage("Summon your hatchling.");
				}
			}
			else
			{
				player.sendMessage("Summon your hatchling first.");
			}
			
			if (!canUpgrade)
			{
				return;
			}
			
			int[] hatchCollar =
			{
				3500,
				3501,
				3502
			};
			int[] striderCollar =
			{
				4422,
				4423,
				4424
			};
			
			for (int i = 0; i < 3; i++)
			{
				if (player.destroyItemByItemId("ClassMaster", hatchCollar[i], 1, player, true))
				{
					player.getPet().unSummon(player);
					player.addItem("ClassMaster", striderCollar[i], 1, player, true);
					return;
				}
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	private void changeClass(L2PcInstance player, int val)
	{
		player.setClassId(val);
		if (player.isSubClassActive())
		{
			player.getSubClasses().get(player.getClassIndex()).setClassId(player.getActiveClass());
		}
		else
		{
			player.setBaseClass(ClassId.getClassIdByOrdinal(player.getActiveClass()));
		}
		player.broadcastUserInfo();
		player.broadcastClassIcon();
	}
	
	@Override
	public boolean isAutoAttackable(final L2Character attacker)
	{
		return false;
	}
}