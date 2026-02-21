package l2jorion.game.powerpack.other;

import l2jorion.Config;
import l2jorion.game.datatables.SkillTable;
import l2jorion.game.handler.ICustomByPassHandler;
import l2jorion.game.managers.CoupleManager;
import l2jorion.game.model.Inventory;
import l2jorion.game.model.L2Skill;
import l2jorion.game.model.L2World;
import l2jorion.game.model.actor.instance.L2ItemInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.entity.Announcements;
import l2jorion.game.model.entity.Wedding;
import l2jorion.game.network.serverpackets.MagicSkillUse;
import l2jorion.game.network.serverpackets.NpcHtmlMessage;
import l2jorion.game.templates.L2Item;

public class WeddingPanel implements ICustomByPassHandler
{
	private static final int COST_ITEM_ID = 4037;
	private static final int COST_AMOUNT = 300;
	private static final int PCBANG_COST = 300;
	private static final int FORMAL_WEAR_ID = 6408;
	
	@Override
	public String[] getByPassCommands()
	{
		return new String[]
		{
			"wedding",
			"AskWedding",
			"AcceptWedding",
			"DeclineWedding"
		};
	}
	
	@Override
	public void handleCommand(String command, L2PcInstance player, String parameters)
	{
		if (player == null)
		{
			return;
		}
		
		// Basic check for partner
		L2PcInstance partner = null;
		if (player.getPartnerId() != 0)
		{
			partner = (L2PcInstance) L2World.getInstance().findObject(player.getPartnerId());
		}
		
		switch (command)
		{
			case "wedding":
				showWelcomeScreen(player);
				break;
			
			case "AskWedding":
				handleAskWedding(player, partner);
				break;
			
			case "AcceptWedding":
				handleAcceptWedding(player, partner);
				break;
			
			case "DeclineWedding":
				handleDeclineWedding(player, partner);
				break;
		}
	}
	
	private void showWelcomeScreen(L2PcInstance player)
	{
		String filename = "data/html/gmshop/wedding/Wedding_start.htm";
		sendHtmlMessage(player, filename, String.valueOf(Config.L2JMOD_WEDDING_PRICE));
	}
	
	private void handleAskWedding(L2PcInstance player, L2PcInstance partner)
	{
		if (partner == null || partner.isOnline() == 0)
		{
			player.sendMessage("Your partner is not found or is offline.");
			return;
		}
		
		if (!player.isInsideRadius(partner, 300, false, false))
		{
			player.sendMessage("Your partner is too far away.");
			return;
		}
		
		// 1. Check Formal Wear
		if (Config.L2JMOD_WEDDING_FORMALWEAR)
		{
			Inventory inv = player.getInventory();
			// Slot 10 is usually chest/fullbody in L2J
			L2ItemInstance chestItem = inv.getPaperdollItem(Inventory.PAPERDOLL_CHEST);
			
			boolean isWearingFormal = false;
			if (chestItem != null && chestItem.getItemId() == FORMAL_WEAR_ID)
			{
				isWearingFormal = true;
			}
			
			player.setIsWearingFormalWear(isWearingFormal);
			
			if (!isWearingFormal)
			{
				sendHtmlMessage(player, "data/html/gmshop/wedding/Wedding_noformal.htm", "");
				return;
			}
		}
		
		// 2. Check Costs (PC Bang & Items)
		if (player.getPcBangScore() < PCBANG_COST)
		{
			player.sendMessage("You don't have enough PC Bang Points (Required: " + PCBANG_COST + ").");
			return;
		}
		
		L2ItemInstance item = player.getInventory().getItemByItemId(COST_ITEM_ID);
		if (item == null || item.getCount() < COST_AMOUNT)
		{
			player.sendMessage("You don't have enough " + L2Item.getItemNameById(COST_ITEM_ID) + " (Required: " + COST_AMOUNT + ").");
			return;
		}
		
		// 3. Process Logic
		if (player.isMaryRequest())
		{
			player.setMaryRequest(false);
			partner.setMaryRequest(false);
			
			sendHtmlMessage(player, "data/html/gmshop/wedding/Wedding_ask.htm", partner.getName());
		}
		else
		{
			// New Request - Consume items here
			player.reducePcBangScore(PCBANG_COST);
			player.destroyItem("WeddingCost", item.getObjectId(), COST_AMOUNT, null, true);
			
			player.setMarryAccepted(true);
			partner.setMaryRequest(true);
			
			sendHtmlMessage(player, "data/html/gmshop/wedding/Wedding_requested.htm", partner.getName());
			partner.sendMessage(player.getName() + " wants to marry you. Please talk to the Wedding Manager.");
		}
	}
	
	private void handleAcceptWedding(L2PcInstance player, L2PcInstance partner)
	{
		if (partner == null || partner.isOnline() == 0)
		{
			player.sendMessage("Your partner is currently offline.");
			return;
		}
		
		player.setMarryAccepted(true);
		
		// Determine Wedding Type
		int type = 0; // Normal
		if (player.getAppearance().getSex() && partner.getAppearance().getSex())
		{
			type = 1; // Lesbian
		}
		else if (!player.getAppearance().getSex() && !partner.getAppearance().getSex())
		{
			type = 2; // Gay
		}
		
		Wedding wedding = CoupleManager.getInstance().getCouple(player.getCoupleId());
		if (wedding != null)
		{
			wedding.marry(type);
		}
		
		// Finalize Status
		player.setMarried(true);
		player.setMaryRequest(false);
		player.setmarriedType(type);
		
		partner.setMarried(true);
		partner.setMaryRequest(false);
		partner.setmarriedType(type);
		
		player.sendMessage("Congratulations! You are now married!");
		partner.sendMessage("Congratulations! You are now married!");
		
		// Give Rewards
		if (Config.WEDDING_GIVE_CUPID_BOW)
		{
			player.addItem("Cupids Bow", 9140, 1, player, true);
			partner.addItem("Cupids Bow", 9140, 1, partner, true);
		}
		
		// Update Skills (in case items/skills changed)
		player.sendSkillList();
		partner.sendSkillList();
		
		// Animations & Effects
		performWeddingEffects(player);
		performWeddingEffects(partner);
		
		// Announcements
		if (Config.ANNOUNCE_WEDDING)
		{
			Announcements.getInstance().announceToAll("Congratulations to " + player.getName() + " and " + partner.getName() + "! They have just been married.");
		}
		
		sendHtmlMessage(partner, "data/html/gmshop/wedding/Wedding_accepted.htm", player.getName());
	}
	
	private void handleDeclineWedding(L2PcInstance player, L2PcInstance partner)
	{
		player.setMaryRequest(false);
		player.setMarryAccepted(false);
		
		player.sendMessage("You declined the wedding.");
		
		if (partner != null && partner.isOnline() != 0)
		{
			partner.setMaryRequest(false);
			partner.setMarryAccepted(false);
			partner.sendMessage("Your partner declined the wedding.");
			sendHtmlMessage(partner, "data/html/gmshop/wedding/Wedding_declined.htm", player.getName());
		}
	}
	
	private void performWeddingEffects(L2PcInstance player)
	{
		// Wedding March
		MagicSkillUse msu = new MagicSkillUse(player, player, 2230, 1, 1, 0);
		player.broadcastPacket(msu);
		
		// Fireworks
		L2Skill skill = SkillTable.getInstance().getInfo(2025, 1);
		if (skill != null)
		{
			msu = new MagicSkillUse(player, player, 2025, 1, 1, 0);
			player.sendPacket(msu);
			player.broadcastPacket(msu);
			player.useMagic(skill, false, false);
		}
	}
	
	private void sendHtmlMessage(L2PcInstance player, String filename, String replace)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		if (replace != null)
		{
			html.replace("%replace%", replace);
		}
		player.sendPacket(html);
	}
}