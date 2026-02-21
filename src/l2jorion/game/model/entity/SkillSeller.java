package l2jorion.game.model.entity;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import l2jorion.game.datatables.SkillTable;
import l2jorion.game.datatables.sql.NpcTable;
import l2jorion.game.managers.GrandBossManager;
import l2jorion.game.model.L2Skill;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.network.SystemMessageId;
import l2jorion.game.network.serverpackets.ActionFailed;
import l2jorion.game.network.serverpackets.MagicSkillUse;
import l2jorion.game.network.serverpackets.NpcHtmlMessage;
import l2jorion.game.network.serverpackets.SocialAction;
import l2jorion.game.network.serverpackets.StatusUpdate;
import l2jorion.game.network.serverpackets.SystemMessage;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;
import l2jorion.util.database.L2DatabaseFactory;

public class SkillSeller
{
	private static final Logger LOG = LoggerFactory.getLogger(SkillSeller.class);
	private static final int SKILLS_PER_PAGE = 5;
	
	private boolean _enabled;
	private int _minLevel;
	private int _requiredBossId;
	private final Map<Integer, List<SkillEntry>> _skillData = new HashMap<>();
	private static final Map<String, Integer> _classMap = Map.ofEntries(Map.entry("Duelist", 88), Map.entry("Dreadnought", 89), Map.entry("PhoenixKnight", 90), Map.entry("HellKnight", 91), Map.entry("Sagittarius", 92), Map.entry("Adventurer", 93), Map.entry("Archmage", 94), Map.entry("Soultaker", 95), Map.entry("ArcanaLord", 96), Map.entry("Cardinal", 97), Map.entry("Hierophant", 98), Map.entry("EvaTemplar", 99), Map.entry("SwordMuse", 100), Map.entry("WindRider", 101), Map.entry("MoonlightSentinel", 102), Map.entry("MysticMuse", 103), Map.entry("ElementalMaster", 104), Map.entry("EvaSaint", 105), Map.entry("ShillienTemplar", 106), Map.entry("SpectralDancer", 107), Map.entry("GhostHunter", 108), Map.entry("GhostSentinel", 109), Map.entry("StormScreamer", 110), Map.entry("SpectralMaster", 111), Map.entry("ShillienSaint", 112), Map.entry("Titan", 113), Map.entry("GrandKhavatari", 114), Map.entry("Dominator", 115), Map.entry("Doomcryer", 116), Map.entry("FortuneSeeker", 117), Map.entry("Maestro", 118));
	
	private record SkillEntry(int costId, int costCount, int skillId, int skillLvl)
	{
	}
	
	public static SkillSeller getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SkillSeller _instance = new SkillSeller();
	}
	
	private SkillSeller()
	{
		loadConfig();
	}
	
	private void loadConfig()
	{
		File configFile = new File("./config/mods/skillseller.ini");
		if (!configFile.exists())
		{
			configFile = new File("./game/config/mods/skillseller.ini");
		}
		if (!configFile.exists())
		{
			_enabled = false;
			return;
		}
		try (var is = new FileInputStream(configFile))
		{
			Properties settings = new Properties();
			settings.load(is);
			_enabled = Boolean.parseBoolean(settings.getProperty("NewSkillEnable", "true"));
			_minLevel = Integer.parseInt(settings.getProperty("NewSkillMinLevel", "80"));
			_requiredBossId = Integer.parseInt(settings.getProperty("NewSkillRequiredBossId", "0"));
			_skillData.clear();
			_classMap.forEach((name, id) ->
			{
				String raw = settings.getProperty(name, "");
				if (!raw.isEmpty())
				{
					List<SkillEntry> list = new ArrayList<>();
					for (String s : raw.split(";"))
					{
						String[] p = s.trim().split(",");
						if (p.length == 4)
						{
							list.add(new SkillEntry(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3])));
						}
					}
					_skillData.put(id, list);
				}
			});
		}
		catch (Exception e)
		{
			LOG.error("SkillSeller Error", e);
		}
	}
	
	public void restoreSkills(L2PcInstance player)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT skillId, skillLevel FROM character_skills_bought WHERE charId = ?"))
		{
			ps.setInt(1, player.getObjectId());
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					L2Skill s = SkillTable.getInstance().getInfo(rs.getInt("skillId"), rs.getInt("skillLevel"));
					if (s != null)
					{
						player.addSkill(s, false);
					}
				}
			}
			player.sendSkillList();
		}
		catch (Exception e)
		{
			LOG.error("SkillSeller: Restore Error", e);
		}
	}
	
	private void saveSkillToDb(int charId, int skillId, int lvl)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("REPLACE INTO character_skills_bought (charId, skillId, skillLevel) VALUES (?,?,?)"))
		{
			ps.setInt(1, charId);
			ps.setInt(2, skillId);
			ps.setInt(3, lvl);
			ps.execute();
		}
		catch (Exception e)
		{
			LOG.error("SkillSeller: Save Error", e);
		}
	}
	
	public void showSkillList(L2PcInstance player, int page)
	{
		if (!_enabled)
		{
			return;
		}
		List<SkillEntry> skills = _skillData.get(player.getClassId().getId());
		if (skills == null || skills.isEmpty())
		{
			return;
		}
		int totalPages = (int) Math.ceil((double) skills.size() / SKILLS_PER_PAGE);
		int currentPage = Math.clamp(page, 1, totalPages);
		int start = (currentPage - 1) * SKILLS_PER_PAGE;
		int end = Math.min(start + SKILLS_PER_PAGE, skills.size());
		String bossName = "None";
		int bossStatus = 1;
		if (_requiredBossId > 0)
		{
			var bt = NpcTable.getInstance().getTemplate(_requiredBossId);
			bossName = bt != null ? bt.getName() : "Boss";
			bossStatus = GrandBossManager.getInstance().getBossStatus(_requiredBossId) == 0 ? 0 : 1;
		}
		StringBuilder sb = new StringBuilder("<html><body><center><table width=300><tr><td align=center><font color=FF9900>Skill Seller List</font></td></tr></table><img src=L2UI.SquareGray width=300 height=1></center>");
		for (int i = start; i < end; i++)
		{
			SkillEntry entry = skills.get(i);
			L2Skill skill = SkillTable.getInstance().getInfo(entry.skillId, entry.skillLvl);
			if (skill == null)
			{
				continue;
			}
			String icon = getSkillIcon(entry.skillId);
			String req = "%d/%d Boss: %s %d/1".formatted(player.getLevel(), _minLevel, bossName, bossStatus);
			sb.append("""
				<table width=300><tr>
				<td width=40><button action="bypass -h custom_skillseller_buy_%d_%d_%d" width=32 height=32 back="icon.skill%s" fore="icon.skill%s"></td>
				<td width=260><table><tr><td><font color=ffffff>%s</font> <font color=c3c3c3>Lv %d</font></td></tr>
				<tr><td><font color=ae9977>Req: %s</font></td></tr><tr><td><font color=B09878>Cost: %d</font></td></tr></table></td>
				</tr></table><center><img src=L2UI.SquareGray width=300 height=1></center>
				""".formatted(player.getClassId().getId(), i, currentPage, icon, icon, skill.getName(), entry.skillLvl, req, entry.costCount));
		}
		if (totalPages > 1)
		{
			sb.append("<br><center><table border=0><tr>");
			for (int x = 1; x <= totalPages; x++)
			{
				sb.append(x == currentPage ? "<td width=25><font color=LEVEL>[%d]</font></td>".formatted(x) : "<td width=25><a action=\"bypass -h custom_skillseller_list %d\">%d</a></td>".formatted(x, x));
			}
			sb.append("</tr></table></center>");
		}
		player.sendPacket(new NpcHtmlMessage(0, sb.append("</body></html>").toString()));
	}
	
	private String getSkillIcon(int id)
	{
		String s = String.valueOf(id);
		if (id < 1000)
		{
			s = "0" + s;
		}
		if (id < 100)
		{
			s = "0" + s;
		}
		return switch (id)
		{
			case 4551, 4552, 4553, 4554 -> "1164";
			case 4702, 4703 -> "1332";
			case 4699, 4700 -> "1331";
			default -> s;
		};
	}
	
	public void buySkill(L2PcInstance player, int classId, int index, int page)
	{
		if (!_enabled || player.getClassId().getId() != classId)
		{
			return;
		}
		if (player.getLevel() < _minLevel)
		{
			sendHtml(player, "skillseller-level.htm");
			return;
		}
		if (_requiredBossId > 0 && GrandBossManager.getInstance().getBossStatus(_requiredBossId) == 0)
		{
			sendHtml(player, "skillseller-boss.htm");
			return;
		}
		List<SkillEntry> skills = _skillData.get(classId);
		if (skills == null || index < 0 || index >= skills.size())
		{
			return;
		}
		SkillEntry entry = skills.get(index);
		if (player.getSkillLevel(entry.skillId) >= entry.skillLvl)
		{
			sendHtml(player, "skillseller-max.htm");
			return;
		}
		if (player.destroyItemByItemId("SkillBuy", entry.costId, entry.costCount, player, true))
		{
			player.addSkill(SkillTable.getInstance().getInfo(entry.skillId, entry.skillLvl), true);
			saveSkillToDb(player.getObjectId(), entry.skillId, entry.skillLvl);
			player.sendSkillList();
			player.broadcastPacket(new MagicSkillUse(player, player, 2025, 1, 1, 0));
			player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LEARNED_SKILL_S1).addSkillName(entry.skillId));
			StatusUpdate su = new StatusUpdate(player);
			su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
			player.sendPacket(su);
			sendHtml(player, "skillseller-successfully.htm");
		}
		else
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
		}
	}
	
	private void sendHtml(L2PcInstance p, String f)
	{
		NpcHtmlMessage h = new NpcHtmlMessage(0);
		h.setFile("data/html/mods/skillseller/" + f);
		p.sendPacket(h);
		p.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void showWelcome(L2PcInstance p)
	{
		sendHtml(p, "skillseller.htm");
	}
}