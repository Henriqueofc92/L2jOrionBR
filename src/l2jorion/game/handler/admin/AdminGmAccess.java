package l2jorion.game.handler.admin;

import java.util.StringTokenizer;

import l2jorion.game.datatables.GmAccessProfile;
import l2jorion.game.datatables.xml.GmAccessTable;
import l2jorion.game.handler.IAdminCommandHandler;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.network.serverpackets.NpcHtmlMessage;

/**
 * Admin command handler for managing GM access profiles.
 * Commands:
 *   admin_gmaccess - show current GM access info
 *   admin_gmaccess_list - list all available profiles
 *   admin_gmaccess_info [profileName] - show details of a profile
 *   admin_gmaccess_reload - reload all GMAccess XML files
 */
public class AdminGmAccess implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_gmaccess",
		"admin_gmaccess_list",
		"admin_gmaccess_info"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command);
		String cmd = st.nextToken();
		
		switch (cmd)
		{
			case "admin_gmaccess":
				showCurrentAccess(activeChar);
				break;
			
			case "admin_gmaccess_list":
				showProfileList(activeChar);
				break;
			
			case "admin_gmaccess_info":
				if (st.hasMoreTokens())
				{
					showProfileInfo(activeChar, st.nextToken());
				}
				else
				{
					activeChar.sendMessage("Usage: //gmaccess_info <profileName>");
				}
				break;
		}
		
		return true;
	}
	
	private void showCurrentAccess(L2PcInstance activeChar)
	{
		L2PcInstance target = activeChar;
		if (activeChar.getTarget() instanceof L2PcInstance)
		{
			target = (L2PcInstance) activeChar.getTarget();
		}
		
		GmAccessProfile profile = target.getGmAccessProfile();
		
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body>");
		sb.append("<center><font color=\"LEVEL\">GM Access - " + target.getName() + "</font></center><br>");
		sb.append("Access Level: " + target.getAccessLevel().getLevel() + " (" + target.getAccessLevel().getName() + ")<br>");
		sb.append("Is GM: " + target.getAccessLevel().isGm() + "<br><br>");
		
		if (profile != null)
		{
			sb.append("<font color=\"LEVEL\">GMAccess Profile: " + profile.getProfileName() + "</font><br>");
			sb.append("Profile Access Level: " + profile.getAccessLevel() + "<br><br>");
			sb.append("<font color=\"LEVEL\">Permissions:</font><br>");
			sb.append("CanUseGMCommand: " + colorBool(profile.canUseGMCommand()) + "<br>");
			sb.append("CanAnnounce: " + colorBool(profile.canAnnounce()) + "<br>");
			sb.append("CanBan: " + colorBool(profile.canBan()) + "<br>");
			sb.append("CanBanChat: " + colorBool(profile.canBanChat()) + "<br>");
			sb.append("CanKick: " + colorBool(profile.canKick()) + "<br>");
			sb.append("CanJail: " + colorBool(profile.canJail()) + "<br>");
			sb.append("GodMode: " + colorBool(profile.hasGodMode()) + "<br>");
			sb.append("CanTeleport: " + colorBool(profile.canTeleport()) + "<br>");
			sb.append("CanReload: " + colorBool(profile.canReload()) + "<br>");
			sb.append("CanRestart: " + colorBool(profile.canRestart()) + "<br>");
			sb.append("CanEditChar: " + colorBool(profile.canEditChar()) + "<br>");
			sb.append("CanEditCharAll: " + colorBool(profile.canEditCharAll()) + "<br>");
			sb.append("CanEditNPC: " + colorBool(profile.canEditNPC()) + "<br>");
			sb.append("UseGMShop: " + colorBool(profile.canUseGMShop()) + "<br>");
			sb.append("CanDelete: " + colorBool(profile.canDelete()) + "<br>");
			sb.append("CanRename: " + colorBool(profile.canRename()) + "<br>");
			sb.append("CanPolymorph: " + colorBool(profile.canPolymorph()) + "<br>");
			sb.append("IsEventGm: " + colorBool(profile.isEventGm()) + "<br>");
			sb.append("BlockInventory: " + colorBool(profile.isBlockInventory()) + "<br>");
		}
		else
		{
			sb.append("<font color=\"FF0000\">No GMAccess profile assigned.</font><br>");
			sb.append("Player has standard user access.<br>");
		}
		
		sb.append("<br><button value=\"Profile List\" action=\"bypass -h admin_gmaccess_list\" width=120 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		sb.append("<button value=\"Reload\" action=\"bypass -h admin_reload gmaccess\" width=120 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		sb.append("</body></html>");
		
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		activeChar.sendPacket(html);
	}
	
	private void showProfileList(L2PcInstance activeChar)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body>");
		sb.append("<center><font color=\"LEVEL\">GMAccess Profiles</font></center><br>");
		sb.append("Total profiles: " + GmAccessTable.getInstance().getProfileNames().size() + "<br>");
		sb.append("Total player assignments: " + GmAccessTable.getInstance().getPlayerCount() + "<br><br>");
		
		for (String name : GmAccessTable.getInstance().getProfileNames())
		{
			GmAccessProfile p = GmAccessTable.getInstance().getTemplateProfile(name);
			sb.append("<a action=\"bypass -h admin_gmaccess_info " + name + "\">" + name + "</a>");
			sb.append(" (Level " + p.getAccessLevel() + " - " + p.getLevelName() + ")<br>");
		}
		
		sb.append("<br><button value=\"Back\" action=\"bypass -h admin_gmaccess\" width=120 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		sb.append("<button value=\"Reload\" action=\"bypass -h admin_reload gmaccess\" width=120 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		sb.append("</body></html>");
		
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		activeChar.sendPacket(html);
	}
	
	private void showProfileInfo(L2PcInstance activeChar, String profileName)
	{
		GmAccessProfile profile = GmAccessTable.getInstance().getTemplateProfile(profileName);
		if (profile == null)
		{
			activeChar.sendMessage("Profile '" + profileName + "' not found.");
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body>");
		sb.append("<center><font color=\"LEVEL\">Profile: " + profileName + "</font></center><br>");
		sb.append("Access Level: " + profile.getAccessLevel() + "<br>");
		sb.append("Level Name: " + profile.getLevelName() + "<br><br>");
		
		sb.append("<font color=\"LEVEL\">Core:</font><br>");
		sb.append("IsGM: " + colorBool(profile.isGM()) + " | CanUseGMCommand: " + colorBool(profile.canUseGMCommand()) + "<br><br>");
		
		sb.append("<font color=\"LEVEL\">Actions:</font><br>");
		sb.append("CanBan: " + colorBool(profile.canBan()) + " | CanKick: " + colorBool(profile.canKick()) + "<br>");
		sb.append("CanJail: " + colorBool(profile.canJail()) + " | CanBanChat: " + colorBool(profile.canBanChat()) + "<br>");
		sb.append("CanAnnounce: " + colorBool(profile.canAnnounce()) + " | CanDelete: " + colorBool(profile.canDelete()) + "<br>");
		sb.append("GodMode: " + colorBool(profile.hasGodMode()) + " | Menu: " + colorBool(profile.hasMenu()) + "<br><br>");
		
		sb.append("<font color=\"LEVEL\">Edit:</font><br>");
		sb.append("CanEditChar: " + colorBool(profile.canEditChar()) + " | CanEditCharAll: " + colorBool(profile.canEditCharAll()) + "<br>");
		sb.append("CanEditNPC: " + colorBool(profile.canEditNPC()) + " | CanEditPledge: " + colorBool(profile.canEditPledge()) + "<br>");
		sb.append("CanViewChar: " + colorBool(profile.canViewChar()) + " | CanViewNPC: " + colorBool(profile.canViewNPC()) + "<br><br>");
		
		sb.append("<font color=\"LEVEL\">Movement:</font><br>");
		sb.append("CanTeleport: " + colorBool(profile.canTeleport()) + " | CanRestart: " + colorBool(profile.canRestart()) + "<br>");
		sb.append("CanReload: " + colorBool(profile.canReload()) + " | Door: " + colorBool(profile.canDoor()) + "<br><br>");
		
		sb.append("<font color=\"LEVEL\">Special:</font><br>");
		sb.append("UseGMShop: " + colorBool(profile.canUseGMShop()) + " | CanRename: " + colorBool(profile.canRename()) + "<br>");
		sb.append("CanPolymorph: " + colorBool(profile.canPolymorph()) + " | IsEventGm: " + colorBool(profile.isEventGm()) + "<br>");
		sb.append("BlockInventory: " + colorBool(profile.isBlockInventory()) + "<br><br>");
		
		sb.append("<font color=\"LEVEL\">Player Actions:</font><br>");
		sb.append("CanAttack: " + colorBool(profile.canAttack()) + " | UseTrade: " + colorBool(profile.canUseTrade()) + "<br>");
		sb.append("PeaceAttack: " + colorBool(profile.canPeaceAttack()) + " | CanJoinClan: " + colorBool(profile.canJoinClan()) + "<br>");
		
		sb.append("<br><button value=\"Back\" action=\"bypass -h admin_gmaccess_list\" width=120 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		sb.append("</body></html>");
		
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		activeChar.sendPacket(html);
	}
	
	private String colorBool(boolean val)
	{
		return val ? "<font color=\"00FF00\">true</font>" : "<font color=\"FF0000\">false</font>";
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
