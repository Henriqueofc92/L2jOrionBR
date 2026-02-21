package l2jorion.game.powerpack.bossInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import l2jorion.Config;
import l2jorion.game.cache.HtmCache;
import l2jorion.game.datatables.sql.ItemTable;
import l2jorion.game.datatables.sql.NpcTable;
import l2jorion.game.handler.ICustomByPassHandler;
import l2jorion.game.handler.IVoicedCommandHandler;
import l2jorion.game.managers.GrandBossManager;
import l2jorion.game.managers.RaidBossSpawnManager;
import l2jorion.game.model.L2DropCategory;
import l2jorion.game.model.L2DropData;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.actor.instance.L2RaidBossInstance;
import l2jorion.game.model.zone.ZoneId;
import l2jorion.game.network.serverpackets.NpcHtmlMessage;
import l2jorion.game.powerpack.PowerPackConfig;
import l2jorion.game.templates.L2Item;
import l2jorion.game.templates.L2NpcTemplate;
import l2jorion.game.templates.StatsSet;

public class RaidInfoHandler implements IVoicedCommandHandler, ICustomByPassHandler
{
	private final String ROOT = "data/html/mods/boss/";
	
	private final int BOSSES_PER_PAGE = 14;
	private final int DROPS_PER_PAGE = 7;
	
	@Override
	public String[] getVoicedCommandList()
	{
		return new String[]
		{
			"boss"
		};
	}
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance player, String target)
	{
		if (player == null)
		{
			return false;
		}
		
		if (Config.L2LIMIT_CUSTOM && player.getPremiumService() == 0 && !player.isInsideZone(ZoneId.ZONE_PEACE))
		{
			player.sendMessage("You can't use this command outside town.");
			return false;
		}
		
		if (command.equalsIgnoreCase("boss"))
		{
			showHtm(player);
		}
		return true;
	}
	
	@Override
	public String[] getByPassCommands()
	{
		return new String[]
		{
			"bosses_gb_list",
			"bosses_rb_list",
			"bosses_rb_bylevels",
			"bosses_index",
			"boss_drop"
		};
	}
	
	@Override
	public void handleCommand(String command, L2PcInstance player, String parameters)
	{
		StringTokenizer st = new StringTokenizer(command);
		String cmd = st.nextToken();
		
		if (cmd.equals("bosses_gb_list"))
		{
			sendGrandBossesInfo(player);
		}
		else if (cmd.equals("bosses_rb_list"))
		{
			showRbListHtm(player);
		}
		else if (cmd.equals("bosses_rb_bylevels"))
		{
			StringTokenizer st2 = new StringTokenizer(parameters);
			if (st2.countTokens() >= 3)
			{
				int min = Integer.parseInt(st2.nextToken());
				int max = Integer.parseInt(st2.nextToken());
				int page = Integer.parseInt(st2.nextToken());
				
				if (page < 1)
				{
					page = 1;
				}
				
				sendRaidBossesInfo(player, min, max, page);
			}
		}
		else if (cmd.equals("boss_drop"))
		{
			StringTokenizer st2 = new StringTokenizer(parameters);
			if (st2.countTokens() >= 2)
			{
				int npcId = Integer.parseInt(st2.nextToken());
				int page = Integer.parseInt(st2.nextToken());
				
				if (page < 1)
				{
					page = 1;
				}
				
				sendDropInfo(player, npcId, page);
			}
		}
		else if (cmd.equals("bosses_index"))
		{
			showHtm(player);
		}
	}
	
	private void sendGrandBossesInfo(L2PcInstance activeChar)
	{
		NpcHtmlMessage htm = new NpcHtmlMessage(5);
		htm.setFile(ROOT + "gb_list.htm");
		StringBuilder t = new StringBuilder();
		
		for (int bossId : PowerPackConfig.RAID_INFO_IDS_LIST)
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(bossId);
			if (template == null)
			{
				continue;
			}
			
			StatsSet info = GrandBossManager.getInstance().getStatsSet(bossId);
			long delay = (info != null) ? info.getLong("respawn_time") : 0;
			boolean isAlive = delay <= System.currentTimeMillis();
			
			t.append("<table width=300 border=0 bgcolor=000000><tr>");
			t.append("<td width=200><a action=\"bypass -h custom_boss_drop " + bossId + " 1\">" + template.getName() + "</a></td>");
			t.append("<td width=100 align=right>" + (isAlive ? "<font color=\"009900\">Alive</font>" : "<font color=\"FF0000\">Dead</font>") + "</td>");
			t.append("</tr></table><img src=\"L2UI.SquareGray\" width=300 height=1>");
		}
		htm.replace("%bosses%", t.toString());
		activeChar.sendPacket(htm);
	}
	
	private void sendRaidBossesInfo(L2PcInstance activeChar, int minLv, int maxLv, int page)
	{
		if (page < 1)
		{
			page = 1;
		}
		
		List<L2RaidBossInstance> bosses = new ArrayList<>();
		for (L2RaidBossInstance rb : RaidBossSpawnManager._bossesForCommand.values())
		{
			if (rb.getLevel() >= minLv && rb.getLevel() <= maxLv)
			{
				bosses.add(rb);
			}
		}
		
		bosses.sort((o1, o2) -> Integer.compare(o1.getLevel(), o2.getLevel()));
		int totalBosses = bosses.size();
		int totalPages = (int) Math.ceil((double) totalBosses / BOSSES_PER_PAGE);
		
		if (page > totalPages && totalPages > 0)
		{
			page = totalPages;
		}
		
		int start = (page - 1) * BOSSES_PER_PAGE;
		int end = Math.min(start + BOSSES_PER_PAGE, totalBosses);
		
		NpcHtmlMessage html = new NpcHtmlMessage(6);
		html.setFile(ROOT + "rb_list_bylevels.htm");
		StringBuilder th = new StringBuilder();
		
		for (int i = start; i < end; i++)
		{
			L2RaidBossInstance rb = bosses.get(i);
			int rbossId = rb.getNpcId();
			StatsSet rInfo = RaidBossSpawnManager.getInstance().getStatsSet(rbossId);
			long delay = rInfo != null ? rInfo.getLong("respawnTime") : 0;
			boolean isAlive = delay <= System.currentTimeMillis();
			
			th.append("<table width=300 border=0><tr>");
			th.append("<td width=200><a action=\"bypass -h custom_boss_drop " + rbossId + " 1\">" + rb.getName() + " (" + rb.getLevel() + ")</a></td>");
			th.append("<td width=100 align=right>" + (isAlive ? "<font color=\"009900\">Alive</font>" : "<font color=\"FF0000\">Dead</font>") + "</td>");
			th.append("</tr></table><img src=\"L2UI.SquareGray\" width=300 height=1>");
		}
		StringBuilder pg = new StringBuilder("<center><table border=0><tr>");
		for (int x = 1; x <= totalPages; x++)
		{
			if (x == page)
			{
				pg.append("<td width=25><font color=\"LEVEL\">[" + x + "]</font></td>");
			}
			else
			{
				pg.append("<td width=25><a action=\"bypass -h custom_bosses_rb_bylevels " + minLv + " " + maxLv + " " + x + "\">" + x + "</a></td>");
			}
		}
		pg.append("</tr></table></center>");
		
		html.replace("%raidbosses%", th.toString());
		html.replace("%pages%", pg.toString());
		activeChar.sendPacket(html);
	}
	
	private void sendDropInfo(L2PcInstance activeChar, int npcId, int page)
	{
		if (page < 1)
		{
			page = 1;
		}
		
		L2NpcTemplate temp = NpcTable.getInstance().getTemplate(npcId);
		if (temp == null)
		{
			return;
		}
		
		List<L2DropData> allDrops = new ArrayList<>();
		for (L2DropCategory cat : temp.getDropData())
		{
			allDrops.addAll(cat.getAllDrops());
		}
		int totalDrops = allDrops.size();
		int totalPages = (int) Math.ceil((double) totalDrops / DROPS_PER_PAGE);
		
		if (page > totalPages && totalPages > 0)
		{
			page = totalPages;
		}
		
		int start = (page - 1) * DROPS_PER_PAGE;
		int end = Math.min(start + DROPS_PER_PAGE, totalDrops);
		
		StringBuilder sb = new StringBuilder();
		for (int i = start; i < end; i++)
		{
			L2DropData drop = allDrops.get(i);
			double chance = calculateChance(activeChar, npcId, drop);
			String icon = L2Item.getItemIcon(drop.getItemId());
			String name = ItemTable.getInstance().getTemplate(drop.getItemId()).getName();
			if (name.length() > 25)
			{
				name = name.substring(0, 22) + "...";
			}
			sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
			sb.append("<table width=300 bgcolor=" + (i % 2 == 0 ? "000000" : "000000") + "><tr>");
			sb.append("<td width=34><img src=\"" + icon + "\" width=32 height=32></td>");
			sb.append("<td width=246><font color=\"ae6c51\">" + name + "</font><br1>");
			sb.append("<font color=\"LEVEL\">" + String.format("%,d", drop.getMinDrop()) + "-" + String.format("%,d", drop.getMaxDrop()) + "</font> | Chance: <font color=\"00FF00\">" + String.format("%.2f", chance) + "%</font></td>");
			sb.append("</tr></table>");
			sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
		}
		
		StringBuilder pg = new StringBuilder("<center><table border=0><tr>");
		for (int x = 1; x <= totalPages; x++)
		{
			if (x == page)
			{
				pg.append("<td width=25><font color=\"LEVEL\">[" + x + "]</font></td>");
			}
			else
			{
				pg.append("<td width=25><a action=\"bypass -h custom_boss_drop " + npcId + " " + x + "\">" + x + "</a></td>");
			}
		}
		pg.append("</tr></table><button value=\"Back\" action=\"bypass -h custom_bosses_index\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">");
		
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
		String content = "<html><body><center>" + "<table width=310 bgcolor=\"000000\" cellspacing=\"0\" cellpadding=\"0\">" + "<tr>" + "<td align=\"center\">" + "<font color=\"FE9B4F\">" + temp.getName() + "</font><br1>" + "</td>" + "</tr>" + "</table>" + "" + sb.toString() + pg.toString()
			+ "</center></body></html>";
		sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
		html.setHtml(content);
		activeChar.sendPacket(html);
	}
	
	private double calculateChance(L2PcInstance player, int npcId, L2DropData drop)
	{
		double chance = drop.getChance() / 10000.0;
		L2NpcTemplate temp = NpcTable.getInstance().getTemplate(npcId);
		if (temp == null)
		{
			return chance;
		}
		
		boolean isRaid = temp.type.equalsIgnoreCase("L2RaidBoss") || RaidBossSpawnManager.getInstance().isDefined(npcId);
		boolean isGrandBoss = temp.type.equalsIgnoreCase("L2GrandBoss") || GrandBossManager.getInstance().getBoss(npcId) != null;
		
		if (drop.getItemId() == 57)
		{
			if (isGrandBoss)
			{
				chance *= Config.ADENA_BOSS;
			}
			else if (isRaid)
			{
				chance *= Config.ADENA_RAID;
			}
			else
			{
				chance *= Config.RATE_DROP_ADENA;
			}
			
			if (player.getPremiumService() > 0)
			{
				chance *= Config.PREMIUM_ADENA_RATE;
			}
		}
		else if (drop.getItemId() >= 6360 && drop.getItemId() <= 6362)
		{
			chance *= Config.RATE_DROP_SEAL_STONES;
			if (player.getPremiumService() > 0)
			{
				chance *= Config.PREMIUM_SS_RATE;
			}
		}
		else
		{
			if (isGrandBoss)
			{
				chance *= Config.ITEMS_BOSS;
			}
			else if (isRaid)
			{
				chance *= Config.ITEMS_RAID;
			}
			else
			{
				chance *= Config.RATE_DROP_ITEMS;
			}
			
			if (player.getPremiumService() > 0)
			{
				chance *= Config.PREMIUM_DROP_RATE;
			}
		}
		return Math.min(chance, 100.0);
	}
	
	private void showHtm(L2PcInstance player)
	{
		NpcHtmlMessage htm = new NpcHtmlMessage(1);
		htm.setHtml(HtmCache.getInstance().getHtm(ROOT + "index.htm"));
		player.sendPacket(htm);
	}
	
	private void showRbListHtm(L2PcInstance player)
	{
		NpcHtmlMessage htm = new NpcHtmlMessage(1);
		htm.setHtml(HtmCache.getInstance().getHtm(ROOT + "rb_list.htm"));
		player.sendPacket(htm);
	}
	
	public static RaidInfoHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final RaidInfoHandler INSTANCE = new RaidInfoHandler();
	}
}