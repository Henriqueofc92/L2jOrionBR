package l2jorion.game.handler.voice;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import l2jorion.game.autofarm.AutofarmManager;
import l2jorion.game.autofarm.AutofarmPlayerRoutine;
import l2jorion.game.handler.ICustomByPassHandler;
import l2jorion.game.handler.IVoicedCommandHandler;
import l2jorion.game.model.L2Object;
import l2jorion.game.model.actor.instance.L2MonsterInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.entity.event.dungeon.DungeonManager;
import l2jorion.game.model.zone.ZoneId;
import l2jorion.game.network.serverpackets.NpcHtmlMessage;

public class AutoFarm implements IVoicedCommandHandler, ICustomByPassHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"autofarm",
		"enableAutoFarm",
		"radiusAutoFarm",
		"pageAutoFarm",
		"enableBuffProtect",
		"healAutoFarm",
		"hpAutoFarm",
		"mpAutoFarm",
		"enableAntiKs",
		"enableSummonAttack",
		"summonSkillAutoFarm",
		"ignoreMonster",
		"enableLoot",
		"enableRest",
		"activeMonster"
	};
	
	@Override
	public boolean useVoicedCommand(final String command, final L2PcInstance activeChar, final String args)
	{
		if (command.startsWith("autofarm"))
		{
			showAutoFarm(activeChar);
			return true;
		}
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage("This function is not allowed in Olympiad.");
			return false;
		}
		
		if (activeChar.isDead())
		{
			activeChar.sendMessage("This function is not allowed while dead.");
			return false;
		}
		
		boolean isDungeonEvent = (activeChar.getInstanceId() == DungeonManager.INSTANCE_PVP || activeChar.getInstanceId() == DungeonManager.INSTANCE_SOLO);
		if (activeChar.isInsideZone(ZoneId.ZONE_PEACE) && !isDungeonEvent)
		{
			activeChar.sendMessage("This function is not allowed inside peace zones.");
			return false;
		}
		
		try
		{
			if (command.startsWith("radiusAutoFarm"))
			{
				StringTokenizer st = new StringTokenizer(args, " ");
				if (st.hasMoreTokens())
				{
					String param = st.nextToken();
					if (param.startsWith("inc_radius"))
					{
						activeChar.setAutoFarmRadius(activeChar.getRadius() + 200);
					}
					else if (param.startsWith("dec_radius"))
					{
						activeChar.setAutoFarmRadius(activeChar.getRadius() - 200);
					}
				}
			}
			else if (command.startsWith("pageAutoFarm"))
			{
				StringTokenizer st = new StringTokenizer(args, " ");
				if (st.hasMoreTokens())
				{
					String param = st.nextToken();
					if (param.startsWith("inc_page"))
					{
						activeChar.setPage(activeChar.getPage() + 1);
					}
					else if (param.startsWith("dec_page"))
					{
						activeChar.setPage(activeChar.getPage() - 1);
					}
				}
			}
			else if (command.startsWith("healAutoFarm"))
			{
				StringTokenizer st = new StringTokenizer(args, " ");
				if (st.hasMoreTokens())
				{
					String param = st.nextToken();
					if (param.startsWith("inc_heal"))
					{
						activeChar.setHealPercent(activeChar.getHealPercent() + 10);
					}
					else if (param.startsWith("dec_heal"))
					{
						activeChar.setHealPercent(activeChar.getHealPercent() - 10);
					}
				}
			}
			else if (command.startsWith("hpAutoFarm"))
			{
				StringTokenizer st = new StringTokenizer(args, " ");
				if (st.hasMoreTokens())
				{
					String param = st.nextToken();
					if (param.contains("inc_hp_pot"))
					{
						activeChar.setHpPotionPercentage(activeChar.getHpPotionPercentage() + 5);
					}
					else if (param.contains("dec_hp_pot"))
					{
						activeChar.setHpPotionPercentage(activeChar.getHpPotionPercentage() - 5);
					}
				}
			}
			else if (command.startsWith("mpAutoFarm"))
			{
				StringTokenizer st = new StringTokenizer(args, " ");
				if (st.hasMoreTokens())
				{
					String param = st.nextToken();
					if (param.contains("inc_mp_pot"))
					{
						activeChar.setMpPotionPercentage(activeChar.getMpPotionPercentage() + 5);
					}
					else if (param.contains("dec_mp_pot"))
					{
						activeChar.setMpPotionPercentage(activeChar.getMpPotionPercentage() - 5);
					}
				}
			}
			else if (command.startsWith("enableAutoFarm"))
			{
				if (activeChar.isAutoFarm())
				{
					// CORREÇÃO: Usar o Manager para parar
					AutofarmManager.getInstance().stopFarm(activeChar);
				}
				else
				{
					// CORREÇÃO: Usar o Manager para iniciar
					AutofarmManager.getInstance().startFarm(activeChar);
				}
			}
			else if (command.startsWith("enableBuffProtect"))
			{
				activeChar.setNoBuffProtection(!activeChar.isNoBuffProtected());
			}
			else if (command.startsWith("enableAntiKs"))
			{
				activeChar.setAntiKsProtection(!activeChar.isAntiKsProtected());
			}
			else if (command.startsWith("enableSummonAttack"))
			{
				activeChar.setSummonAttack(!activeChar.isSummonAttack());
			}
			else if (command.startsWith("summonSkillAutoFarm"))
			{
				StringTokenizer st = new StringTokenizer(args, " ");
				if (st.hasMoreTokens())
				{
					String param = st.nextToken();
					if (param.startsWith("inc_summonSkill"))
					{
						activeChar.setSummonSkillPercent(activeChar.getSummonSkillPercent() + 10);
					}
					else if (param.startsWith("dec_summonSkill"))
					{
						activeChar.setSummonSkillPercent(activeChar.getSummonSkillPercent() - 10);
					}
				}
			}
			else if (command.startsWith("ignoreMonster"))
			{
				L2Object target = activeChar.getTarget();
				if (target instanceof L2MonsterInstance)
				{
					activeChar.addIgnoredMonster(((L2MonsterInstance) target).getNpcId());
					activeChar.sendMessage(target.getName() + " added to ignored list.");
				}
				else
				{
					activeChar.sendMessage("You must select a monster.");
				}
			}
			else if (command.startsWith("activeMonster"))
			{
				L2Object target = activeChar.getTarget();
				if (target instanceof L2MonsterInstance)
				{
					activeChar.activeMonster(((L2MonsterInstance) target).getNpcId());
					activeChar.sendMessage(target.getName() + " removed from ignored list.");
				}
				else
				{
					activeChar.sendMessage("You must select a monster.");
				}
			}
			else if (command.startsWith("enableLoot"))
			{
				activeChar.setLooting(!activeChar.isLooting());
			}
			else if (command.startsWith("enableRest"))
			{
				activeChar.setResting(!activeChar.isResting());
			}
			
			showAutoFarm(activeChar);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return true;
	}
	
	@Override
	public void handleCommand(String command, L2PcInstance player, String parameters)
	{
		useVoicedCommand(command, player, parameters);
	}
	
	@Override
	public String[] getByPassCommands()
	{
		return VOICED_COMMANDS;
	}
	
	private static final String ACTIVED = "<font color=00FF00>ON</font>";
	private static final String DESATIVED = "<font color=FF0000>OFF</font>";
	private static final String STOP = "STOP";
	private static final String START = "START";
	
	public static void showAutoFarm(L2PcInstance activeChar)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/autofarm/AutoFarm.htm");
		html.replace("%player%", activeChar.getName());
		html.replace("%page%", String.valueOf(activeChar.getPage() + 1));
		html.replace("%heal%", String.valueOf(activeChar.getHealPercent()));
		html.replace("%radius%", String.valueOf(activeChar.getRadius()));
		html.replace("%summonSkill%", String.valueOf(activeChar.getSummonSkillPercent()));
		html.replace("%hpPotion%", String.valueOf(activeChar.getHpPotionPercentage()));
		html.replace("%mpPotion%", String.valueOf(activeChar.getMpPotionPercentage()));
		html.replace("%noBuff%", activeChar.isNoBuffProtected() ? "back=L2UI.CheckBox_checked fore=L2UI.CheckBox_checked" : "back=L2UI.CheckBox fore=L2UI.CheckBox");
		html.replace("%summonAtk%", activeChar.isSummonAttack() ? "back=L2UI.CheckBox_checked fore=L2UI.CheckBox_checked" : "back=L2UI.CheckBox fore=L2UI.CheckBox");
		html.replace("%antiKs%", activeChar.isAntiKsProtected() ? "back=L2UI.CheckBox_checked fore=L2UI.CheckBox_checked" : "back=L2UI.CheckBox fore=L2UI.CheckBox");
		html.replace("%loot%", activeChar.isLooting() ? "back=L2UI.CheckBox_checked fore=L2UI.CheckBox_checked" : "back=L2UI.CheckBox fore=L2UI.CheckBox");
		html.replace("%rest%", activeChar.isResting() ? "back=L2UI.CheckBox_checked fore=L2UI.CheckBox_checked" : "back=L2UI.CheckBox fore=L2UI.CheckBox");
		html.replace("%autofarm%", activeChar.isAutoFarm() ? ACTIVED : DESATIVED);
		html.replace("%status_color%", activeChar.isAutoFarm() ? "00FF00" : "FF0000");
		html.replace("%button%", activeChar.isAutoFarm() ? STOP : START);
		
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		html.replace("%server_time%", sdf.format(new Date()));
		
		String uptime = "00:00:00";
		if (activeChar.isAutoFarm())
		{
			AutofarmPlayerRoutine bot = AutofarmManager.getInstance().getRoutine(activeChar.getObjectId());
			if (bot != null)
			{
				long startTime = bot.getStartTime();
				if (startTime > 0)
				{
					long duration = System.currentTimeMillis() - startTime;
					uptime = getDurationString(duration);
				}
			}
		}
		
		html.replace("%useautofarm_time%", uptime);
		
		activeChar.sendPacket(html);
	}
	
	private static String getDurationString(long durationInMillis)
	{
		long hours = TimeUnit.MILLISECONDS.toHours(durationInMillis);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(durationInMillis) % 60;
		long seconds = TimeUnit.MILLISECONDS.toSeconds(durationInMillis) % 60;
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}