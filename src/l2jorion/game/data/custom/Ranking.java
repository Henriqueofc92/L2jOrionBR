package l2jorion.game.data.custom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.State;
import l2jorion.game.model.quest.QuestState;
import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.util.database.L2DatabaseFactory;

/**
 * Ranking custom script (2003).<br>
 * Top 10 rankings: PvP, PK, Enchants, Boss Jewelry, Level, Clan, Castles, 24h PvP, 24h PK.
 */
public class Ranking extends Quest
{
	private static final int[] NPC = { 14 };
	private static final int PRICE_ID = 57;
	private static final int PRICE_COUNT = 0;
	
	private static final String NO_ITEMS_HTML = "<html><body><center><title>TOP 10</title><br><br><img src=\"l2font-e.replay_logo-e\" width=255 height=60><br><br><img src=\"L2UI_CH3.onscrmsg_pattern01_2\" width=300 height=32><br><br>Sorry!<br><br>You do not have the necessary items.<br><br><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32><font color=\"222222\"></center></body></html>";
	
	public Ranking()
	{
		super(2003, "2003_Ranking", "custom");
		
		var created = new State("Start", this);
		new State("Started", this);
		new State("Completed", this);
		setInitialState(created);
		
		for (var npcId : NPC)
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		return "1.htm";
	}
	
	@Override
	public String onEvent(String event, QuestState st)
	{
		var price = st.getQuestItemsCount(PRICE_ID);
		
		return switch (event)
		{
			case "1" -> price >= PRICE_COUNT ? buildPvpRanking(st) : NO_ITEMS_HTML;
			case "2" -> price >= PRICE_COUNT ? buildPkRanking(st) : NO_ITEMS_HTML;
			case "3" -> price >= PRICE_COUNT ? buildEnchantRanking(st) : NO_ITEMS_HTML;
			case "4" -> price >= PRICE_COUNT ? buildLevelRanking(st) : NO_ITEMS_HTML;
			case "5" -> price >= PRICE_COUNT ? buildClanRanking(st) : NO_ITEMS_HTML;
			case "6" -> buildCastleInfo();
			case "7" -> price >= PRICE_COUNT ? buildBossJewelryRanking(st) : NO_ITEMS_HTML;
			case "8" -> price >= PRICE_COUNT ? buildPvp24hRanking(st) : NO_ITEMS_HTML;
			case "9" -> price >= PRICE_COUNT ? buildPk24hRanking(st) : NO_ITEMS_HTML;
			default -> event;
		};
	}
	
	private String buildPvpRanking(QuestState st)
	{
		st.takeItems(PRICE_ID, PRICE_COUNT);
		var sb = new StringBuilder();
		sb.append("<html><head><center><title>TOP 10</title></head><body><img src=\"l2font-e.replay_logo-e\" width=255 height=60><br><img src=\"L2UI_CH3.onscrmsg_pattern01_2\" width=300 height=32><table width=200>");
		sb.append("<tr><td><font color =\"0066CC\">Rank</td><td><center><font color =\"0066CC\">Name</color></center></td><td><center><font color =\"0066CC\">PvP's</color></center></td></tr><tr></tr>");
		
		int total = 0, pos = 0;
		boolean color = true;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement ps = con.prepareStatement("SELECT char_name,pvpkills FROM characters WHERE pvpkills>0 order by pvpkills desc limit 10");
			ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				var charName = rs.getString("char_name");
				var pvpKills = rs.getString("pvpkills");
				total += Integer.parseInt(pvpKills);
				pos++;
				var rankColor = color ? "LEVEL" : "FFFF00";
				color = !color;
				sb.append("<tr><td><center><font color =\"").append(rankColor).append("\">").append(pos)
					.append("</td><td><center><font color =\"FFFFFF\">").append(charName)
					.append("</center></td><td><center>").append(pvpKills).append("</center></td></tr>");
			}
		}
		catch (Exception e)
		{
			// ignore
		}
		
		sb.append("</table><center><br><font color=\"0066CC\">Total:</font> ").append(total)
			.append("<font color=\"FFFFFF\"> PvP's</font><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32></center></body></html>");
		return sb.toString();
	}
	
	private String buildPkRanking(QuestState st)
	{
		st.takeItems(PRICE_ID, PRICE_COUNT);
		var sb = new StringBuilder();
		sb.append("<html><head><center><title>TOP 10</title></head><body><img src=\"l2font-e.replay_logo-e\" width=255 height=60><br><img src=\"L2UI_CH3.onscrmsg_pattern01_2\" width=300 height=32><table width=200>");
		sb.append("<tr><td><font color =\"0066CC\">Rank</td><td><center><font color =\"0066CC\">Name</color></center></td><td><center><font color =\"0066CC\">PK's</color></center></td></tr><tr></tr>");
		
		int total = 0, pos = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement ps = con.prepareStatement("SELECT char_name,pkkills FROM characters WHERE pkkills>0 order by pkkills desc limit 10");
			ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				var charName = rs.getString("char_name");
				var pkKills = rs.getString("pkkills");
				total += Integer.parseInt(pkKills);
				pos++;
				var rankColor = "FFFF00";
				sb.append("<tr><td><center><font color =\"").append(rankColor).append("\">").append(pos)
					.append("</td><td><center><font color =\"FFFFFF\">").append(charName)
					.append("</center></td><td><center>").append(pkKills).append("</center></td></tr>");
			}
		}
		catch (Exception e)
		{
			// ignore
		}
		
		sb.append("</table><center><br><font color=\"0066CC\">Total:</font> ").append(total)
			.append("<font color=\"FFFFFF\"> PK's</font><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32></center></body></html>");
		return sb.toString();
	}
	
	private String buildEnchantRanking(QuestState st)
	{
		st.takeItems(PRICE_ID, PRICE_COUNT);
		var sb = new StringBuilder();
		sb.append("<html><head><center><title>TOP 10</title></head><body><img src=\"l2font-e.replay_logo-e\" width=255 height=60><br><img src=\"L2UI_CH3.onscrmsg_pattern01_2\" width=300 height=32><table width=300>");
		sb.append("<tr><td><font color =\"0066CC\">Rank</font></td><td><center><font color =\"0066CC\">Name</font></center></td><td><center><font color =\"0066CC\">Enchants</font></center></td></tr><tr></tr>");
		
		int pos = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement ps = con.prepareStatement("SELECT characters.char_name, characters.title, characters.obj_Id, characters.clanid, characters.base_class, char_templates.ClassId, char_templates.ClassName, items.owner_id, items.item_id, items.enchant_level, weapon.item_id, weapon.name, weapon.soulshots, weapon.crystal_type, clan_data.clan_id, clan_data.clan_name, clan_data.ally_id, clan_data.ally_name, clan_data.crest_id, clan_data.ally_crest_id FROM characters INNER JOIN items ON characters.obj_Id=items.owner_id INNER JOIN char_templates ON characters.base_class=char_templates.ClassId INNER JOIN weapon ON items.item_id=weapon.item_id LEFT JOIN clan_data ON characters.clanid=clan_data.clan_id WHERE (weapon.crystal_type!='none' AND weapon.soulshots>'0') ORDER BY items.enchant_level DESC LIMIT 10");
			ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				var charName = rs.getString("char_name");
				var name = rs.getString("name");
				var enchants = rs.getString("enchant_level");
				pos++;
				sb.append("<tr><td><center><font color =\"FFFF00\">").append(pos)
					.append("</font></td><td><center><font color =\"FFFFFF\">").append(charName)
					.append("</font></center></td><td><center>").append(name)
					.append(" <font color=\"3399ff\">+").append(enchants).append("</font></center></td></tr>");
			}
		}
		catch (Exception e)
		{
			// ignore
		}
		
		sb.append("</table><center><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32></center></body></html>");
		return sb.toString();
	}
	
	private String buildBossJewelryRanking(QuestState st)
	{
		st.takeItems(PRICE_ID, PRICE_COUNT);
		var sb = new StringBuilder();
		sb.append("<html><head><center><title>TOP 10</title></head><body><img src=\"l2font-e.replay_logo-e\" width=255 height=60><br><img src=\"L2UI_CH3.onscrmsg_pattern01_2\" width=300 height=32><table width=300>");
		sb.append("<tr><td><font color =\"0066CC\">Rank</font></td><td><center><font color =\"0066CC\">Name</font></center></td><td><center><font color =\"0066CC\">Boss Jewelry</font></center></td></tr><tr></tr>");
		
		int pos = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement ps = con.prepareStatement("SELECT characters.char_name, characters.title, characters.obj_Id, characters.clanid, items.owner_id, items.item_id, items.enchant_level, char_templates.ClassId, char_templates.ClassName, armor.name, clan_data.clan_id, clan_data.clan_name, clan_data.ally_id, clan_data.ally_name, clan_data.crest_id, clan_data.ally_crest_id FROM characters INNER JOIN items ON characters.obj_Id=items.owner_id INNER JOIN char_templates ON characters.base_class=char_templates.ClassId INNER JOIN armor ON items.item_id=armor.item_id LEFT JOIN clan_data ON characters.clanid=clan_data.clan_id WHERE ((armor.item_id BETWEEN 6656 AND 6662) OR armor.item_id='8191') ORDER BY items.enchant_level DESC LIMIT 10");
			ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				var charName = rs.getString("char_name");
				var name = rs.getString("name");
				var enchants = rs.getString("enchant_level");
				pos++;
				sb.append("<tr><td><center><font color =\"FFFF00\">").append(pos)
					.append("</font></td><td><center><font color =\"FFFFFF\">").append(charName)
					.append("</font></center></td><td><center>").append(name)
					.append(" <font color=\"3399ff\">+").append(enchants).append("</font></center></td></tr>");
			}
		}
		catch (Exception e)
		{
			// ignore
		}
		
		sb.append("</table><center><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32></center></body></html>");
		return sb.toString();
	}
	
	private String buildLevelRanking(QuestState st)
	{
		st.takeItems(PRICE_ID, PRICE_COUNT);
		var sb = new StringBuilder();
		sb.append("<html><head><center><title>TOP 10</title></head><body><img src=\"l2font-e.replay_logo-e\" width=255 height=60><br><img src=\"L2UI_CH3.onscrmsg_pattern01_2\" width=300 height=32><table width=200>");
		sb.append("<tr><td><font color =\"0066CC\">Rank</td><td><center><font color =\"0066CC\">Name</color></center></td><td><center><font color =\"0066CC\">Level</color></center></td></tr><tr></tr>");
		
		int pos = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement ps = con.prepareStatement("SELECT char_name,level FROM characters WHERE level>1 order by level desc limit 10");
			ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				var charName = rs.getString("char_name");
				var level = rs.getString("level");
				pos++;
				sb.append("<tr><td><center><font color =\"FFFF00\">").append(pos)
					.append("</td><td><center><font color =\"FFFFFF\">").append(charName)
					.append("</center></td><td><center>").append(level).append("</center></td></tr>");
			}
		}
		catch (Exception e)
		{
			// ignore
		}
		
		sb.append("</table><center><br><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32><font color=\"222222\"></center></body></html>");
		return sb.toString();
	}
	
	private String buildClanRanking(QuestState st)
	{
		st.takeItems(PRICE_ID, PRICE_COUNT);
		var sb = new StringBuilder();
		sb.append("<html><head><center><title>TOP 10</title></head><body><img src=\"l2font-e.replay_logo-e\" width=255 height=60><br><img src=\"L2UI_CH3.onscrmsg_pattern01_2\" width=300 height=32><table width=230>");
		sb.append("<tr><td><font color =\"0066CC\">Rank</font></td><td><center><font color =\"0066CC\">Level</font></center></td><td><center><font color =\"0066CC\">Name</font></center></td><td><center><font color =\"0066CC\">Reputation</font></center></td></tr>");
		
		int pos = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement ps = con.prepareStatement("SELECT clan_name,clan_level,reputation_score,hasCastle FROM clan_data WHERE clan_level>0 order by reputation_score desc limit 10");
			ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				var clanName = rs.getString("clan_name");
				var clanLevel = rs.getString("clan_level");
				var clanScore = rs.getString("reputation_score");
				pos++;
				sb.append("<tr><td><center><font color =\"FFFF00\">").append(pos)
					.append("</font></center></td><td><center>").append(clanLevel)
					.append("</center></td><td><center>").append(clanName)
					.append("</center></td><td><center>").append(clanScore).append("</center></td></tr>");
			}
		}
		catch (Exception e)
		{
			// ignore
		}
		
		sb.append("</table><center><br><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32><font color=\"222222\"></center></body></html>");
		return sb.toString();
	}
	
	private String buildCastleInfo()
	{
		var sb = new StringBuilder();
		sb.append("<html><head><center><title>TOP 10</title></head><body><img src=\"l2font-e.replay_logo-e\" width=255 height=60><br><img src=\"L2UI_CH3.onscrmsg_pattern01_2\" width=300 height=32><table width=200>");
		sb.append("<tr><td><center><font color =\"0066CC\">Castle</font></center></td><td><center><font color =\"0066CC\">Tax</font></center></td><td><center><font color =\"0066CC\">Owner</font></center></td></tr>");
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement ps = con.prepareStatement("SELECT id,name,taxPercent FROM `castle`");
			ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				var castleId = rs.getString("id");
				var castleName = rs.getString("name");
				var tax = rs.getString("taxPercent");
				
				try (PreparedStatement ps2 = con.prepareStatement("SELECT clan_name,hasCastle FROM clan_data WHERE hasCastle = " + castleId);
					ResultSet rs2 = ps2.executeQuery())
				{
					while (rs2.next())
					{
						var clanName = rs2.getString("clan_name");
						sb.append("<tr><td><center>").append(castleName)
							.append("</center></td><td><center>").append(tax)
							.append("</center></td><td><center>").append(clanName).append("</center></td></tr>");
					}
				}
			}
		}
		catch (Exception e)
		{
			// ignore
		}
		
		sb.append("</table><center><br><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32><font color=\"222222\"></center></body></html>");
		return sb.toString();
	}
	
	private String buildPvp24hRanking(QuestState st)
	{
		st.takeItems(PRICE_ID, PRICE_COUNT);
		var sb = new StringBuilder();
		sb.append("<html><head><center><title>TOP 10</title></head><body><img src=\"l2font-e.replay_logo-e\" width=255 height=60><br><img src=\"L2UI_CH3.onscrmsg_pattern01_2\" width=300 height=32><table width=200>");
		sb.append("<tr><td><font color =\"0066CC\">Rank</td><td><center><font color =\"0066CC\">Name</color></center></td><td><center><font color =\"0066CC\">PvP's</color></center></td></tr><tr></tr>");
		
		int total = 0, pos = 0;
		boolean color = true;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement ps = con.prepareStatement("SELECT char_name,pvpkills FROM 24hPvpPk WHERE pvpkills>0 order by pvpkills desc limit 10");
			ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				var charName = rs.getString("char_name");
				var pvpKills = rs.getString("pvpkills");
				total += Integer.parseInt(pvpKills);
				pos++;
				var rankColor = color ? "LEVEL" : "FFFF00";
				color = !color;
				sb.append("<tr><td><center><font color =\"").append(rankColor).append("\">").append(pos)
					.append("</td><td><center><font color =\"FFFFFF\">").append(charName)
					.append("</center></td><td><center>").append(pvpKills).append("</center></td></tr>");
			}
		}
		catch (Exception e)
		{
			// ignore
		}
		
		sb.append("</table><center><br><font color=\"0066CC\">Total:</font> ").append(total)
			.append("<font color=\"FFFFFF\"> PvP's</font><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32></center></body></html>");
		return sb.toString();
	}
	
	private String buildPk24hRanking(QuestState st)
	{
		st.takeItems(PRICE_ID, PRICE_COUNT);
		var sb = new StringBuilder();
		sb.append("<html><head><center><title>TOP 10</title></head><body><img src=\"l2font-e.replay_logo-e\" width=255 height=60><br><img src=\"L2UI_CH3.onscrmsg_pattern01_2\" width=300 height=32><table width=200>");
		sb.append("<tr><td><font color =\"0066CC\">Rank</td><td><center><font color =\"0066CC\">Name</color></center></td><td><center><font color =\"0066CC\">PK's</color></center></td></tr><tr></tr>");
		
		int total = 0, pos = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement ps = con.prepareStatement("SELECT char_name,pkkills FROM 24hPvpPk WHERE pkkills>0 order by pkkills desc limit 10");
			ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				var charName = rs.getString("char_name");
				var pkKills = rs.getString("pkkills");
				total += Integer.parseInt(pkKills);
				pos++;
				sb.append("<tr><td><center><font color =\"FFFF00\">").append(pos)
					.append("</td><td><center><font color =\"FFFFFF\">").append(charName)
					.append("</center></td><td><center>").append(pkKills).append("</center></td></tr>");
			}
		}
		catch (Exception e)
		{
			// ignore
		}
		
		sb.append("</table><center><br><font color=\"0066CC\">Total:</font> ").append(total)
			.append("<font color=\"FFFFFF\"> PK's</font><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32></center></body></html>");
		return sb.toString();
	}


	public static void main(String[] args)
	{
		new Ranking();
	}
}
