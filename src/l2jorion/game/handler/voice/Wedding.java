package l2jorion.game.handler.voice;

import l2jorion.Config;
import l2jorion.game.ai.CtrlIntention;
import l2jorion.game.controllers.GameTimeController;
import l2jorion.game.datatables.SkillTable;
import l2jorion.game.handler.IVoicedCommandHandler;
import l2jorion.game.managers.CoupleManager;
import l2jorion.game.managers.GrandBossManager;
import l2jorion.game.model.L2Skill;
import l2jorion.game.model.L2World;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.entity.event.CTF;
import l2jorion.game.model.entity.event.DM;
import l2jorion.game.model.entity.event.TvT;
import l2jorion.game.model.entity.event.VIP;
import l2jorion.game.model.zone.ZoneId;
import l2jorion.game.network.SystemMessageId;
import l2jorion.game.network.serverpackets.ConfirmDlg;
import l2jorion.game.network.serverpackets.MagicSkillUse;
import l2jorion.game.network.serverpackets.SetupGauge;
import l2jorion.game.network.serverpackets.SystemMessage;
import l2jorion.game.thread.ThreadPoolManager;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;

public class Wedding implements IVoicedCommandHandler
{
	protected static final Logger LOG = LoggerFactory.getLogger(Wedding.class.getName());
	
	private static final String[] _voicedCommands =
	{
		"divorce",
		"engage",
		"gotolove"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.startsWith("engage"))
		{
			return engage(activeChar);
		}
		else if (command.startsWith("divorce"))
		{
			return divorce(activeChar);
		}
		else if (command.startsWith("gotolove"))
		{
			return goToLove(activeChar);
		}
		return false;
	}
	
	public boolean divorce(L2PcInstance activeChar)
	{
		if (activeChar.getPartnerId() == 0)
		{
			return false;
		}
		
		int partnerId = activeChar.getPartnerId();
		int coupleId = activeChar.getCoupleId();
		int adenaAmount = 0;
		
		if (activeChar.isMarried())
		{
			activeChar.sendMessage("You are now divorced.");
			adenaAmount = (activeChar.getAdena() / 100) * Config.L2JMOD_WEDDING_DIVORCE_COSTS;
			activeChar.getInventory().reduceAdena("Wedding", adenaAmount, activeChar, null);
		}
		else
		{
			activeChar.sendMessage("You have broken up as a couple.");
		}
		
		L2PcInstance partner = (L2PcInstance) L2World.getInstance().findObject(partnerId);
		
		if (partner != null)
		{
			partner.setPartnerId(0);
			if (partner.isMarried())
			{
				partner.sendMessage("Your spouse has decided to divorce you.");
			}
			else
			{
				partner.sendMessage("Your fiance has decided to break the engagement with you.");
			}
			
			// Give partial adena back to partner (Alimony?)
			if (adenaAmount > 0)
			{
				partner.addAdena("WEDDING", adenaAmount, null, false);
			}
		}
		
		CoupleManager.getInstance().deleteCouple(coupleId);
		return true;
	}
	
	public boolean engage(L2PcInstance activeChar)
	{
		// 1. Check Target Validity
		if (activeChar.getTarget() == null || !(activeChar.getTarget() instanceof L2PcInstance))
		{
			activeChar.sendMessage("You can only ask another player to engage you.");
			return false;
		}
		
		L2PcInstance ptarget = (L2PcInstance) activeChar.getTarget();
		
		// 2. Check Self
		if (ptarget.getObjectId() == activeChar.getObjectId())
		{
			activeChar.sendMessage("You cannot engage with yourself.");
			return false;
		}
		
		// 3. Check Infidelity (Are you already engaged?)
		if (activeChar.getPartnerId() != 0)
		{
			activeChar.sendMessage("You are already engaged.");
			punishInfidelity(activeChar);
			return false;
		}
		
		// 4. Check Target Status
		if (ptarget.getPartnerId() != 0)
		{
			activeChar.sendMessage("Target is already engaged with someone else.");
			return false;
		}
		
		if (ptarget.isEngageRequest())
		{
			activeChar.sendMessage("Player already has a pending engagement request.");
			return false;
		}
		
		// 5. Check Gender
		if (ptarget.getAppearance().getSex() == activeChar.getAppearance().getSex() && !Config.L2JMOD_WEDDING_SAMESEX)
		{
			activeChar.sendMessage("Same-sex marriage is not allowed on this server configuration.");
			return false;
		}
		
		// 6. Check Friendlist
		if (!activeChar.getFriendList().contains(ptarget.getObjectId()))
		{
			activeChar.sendMessage("The player must be on your friendlist to engage.");
			return false;
		}
		
		// 7. Send Request
		ptarget.setEngageRequest(true, activeChar.getObjectId());
		ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.S1_S2.getId());
		dlg.addString(activeChar.getName() + " is asking to engage you. Do you want to start a new relationship?");
		ptarget.sendPacket(dlg);
		
		return true;
	}
	
	public boolean goToLove(L2PcInstance activeChar)
	{
		if (!activeChar.isMarried())
		{
			activeChar.sendMessage("You are not married.");
			return false;
		}
		
		if (activeChar.getPartnerId() == 0)
		{
			activeChar.sendMessage("Couldn't find your partner in the Database.");
			return false;
		}
		
		L2PcInstance partner = (L2PcInstance) L2World.getInstance().findObject(activeChar.getPartnerId());
		
		// Basic Checks
		if (partner == null || partner.isOnline() == 0)
		{
			activeChar.sendMessage("Your partner is not online.");
			return false;
		}
		
		// Instance Check (Critical for preventing bugs)
		if (activeChar.getInstanceId() != partner.getInstanceId())
		{
			activeChar.sendMessage("You cannot teleport to a different instance ID.");
			return false;
		}
		
		// Check conditions for Player
		if (checkRestrictedCondition(activeChar, true))
		{
			return false;
		}
		
		// Check conditions for Partner
		if (checkRestrictedCondition(partner, false))
		{
			activeChar.sendMessage("Your partner is in a restricted area/state.");
			return false;
		}
		
		// Payment
		if (activeChar.getAdena() < Config.L2JMOD_WEDDING_TELEPORT_PRICE)
		{
			activeChar.sendMessage("You don't have enough adena.");
			return false;
		}
		
		// Start Teleport Sequence
		int teleportTimer = Config.L2JMOD_WEDDING_TELEPORT_DURATION * 1000;
		
		activeChar.sendMessage("After " + teleportTimer / 1000 + " seconds you will be teleported.");
		activeChar.getInventory().reduceAdena("Wedding", Config.L2JMOD_WEDDING_TELEPORT_PRICE, activeChar, null);
		
		// Stop movement and actions
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		activeChar.setTarget(activeChar);
		activeChar.disableAllSkills(); // Prevent casting other skills
		
		// Animation
		MagicSkillUse msk = new MagicSkillUse(activeChar, 1050, 1, teleportTimer, 0);
		activeChar.broadcastPacket(msk);
		
		SetupGauge sg = new SetupGauge(0, teleportTimer);
		activeChar.sendPacket(sg);
		
		// Schedule Finalizer
		EscapeFinalizer ef = new EscapeFinalizer(activeChar, partner);
		activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, teleportTimer));
		activeChar.setSkillCastEndTime(10 + GameTimeController.getInstance().getGameTicks() + teleportTimer / GameTimeController.MILLIS_IN_TICK);
		
		return true;
	}
	
	/**
	 * Centralized method to check if a player is in a condition that prevents wedding teleport.
	 * @param player The player to check
	 * @param isSourcePlayer True if this is the player initiating the command (for custom messages)
	 * @return true if restricted
	 */
	private boolean checkRestrictedCondition(L2PcInstance player, boolean isSourcePlayer)
	{
		if (player.isInJail())
		{
			if (isSourcePlayer)
			{
				player.sendMessage("You are in Jail.");
			}
			return true;
		}
		if (player.isInOlympiadMode())
		{
			if (isSourcePlayer)
			{
				player.sendMessage("You are in the Olympiad.");
			}
			return true;
		}
		if (player.isInDuel())
		{
			if (isSourcePlayer)
			{
				player.sendMessage("You are in a Duel.");
			}
			return true;
		}
		if (player.isCursedWeaponEquiped())
		{
			if (isSourcePlayer)
			{
				player.sendMessage("You possess a Cursed Weapon.");
			}
			return true;
		}
		if (player.isInCombat())
		{
			if (isSourcePlayer)
			{
				player.sendMessage("You are holding a Combat Flag.");
			}
			return true;
		}
		if (player.isInFunEvent() || player.atEvent || (player._inEventTvT && TvT.is_started()) || (player._inEventCTF && CTF.is_started()) || (player._inEventDM && DM.is_started()) || (player._inEventVIP && VIP._started))
		{
			if (isSourcePlayer)
			{
				player.sendMessage("You are in an Event.");
			}
			return true;
		}
		if (player.isFestivalParticipant())
		{
			if (isSourcePlayer)
			{
				player.sendMessage("You are in a Festival.");
			}
			return true;
		}
		if (player.isInParty() && player.getParty().isInDimensionalRift())
		{
			if (isSourcePlayer)
			{
				player.sendMessage("You are in the Dimensional Rift.");
			}
			return true;
		}
		if (player.inObserverMode())
		{
			if (isSourcePlayer)
			{
				player.sendMessage("You are in Observer Mode.");
			}
			return true;
		}
		// Generic Zone Checks
		if (player.isInsideZone(ZoneId.ZONE_NOSUMMONFRIEND))
		{
			if (isSourcePlayer)
			{
				player.sendMessage("You are in a No-Summon zone.");
			}
			return true;
		}
		if (GrandBossManager.getInstance().getZone(player) != null)
		{
			if (isSourcePlayer)
			{
				player.sendMessage("You are inside a Grand Boss Zone.");
			}
			return true;
		}
		// BETTER SIEGE CHECK
		if (player.isInsideZone(ZoneId.ZONE_SIEGE))
		{
			if (isSourcePlayer)
			{
				player.sendMessage("You are inside a Siege Zone.");
			}
			return true;
		}
		
		return false;
	}
	
	private void punishInfidelity(L2PcInstance activeChar)
	{
		if (Config.L2JMOD_WEDDING_PUNISH_INFIDELITY)
		{
			activeChar.startAbnormalEffect((short) 0x2000); // Big Head
			
			int skillId = activeChar.isMageClass() ? 4361 : 4362;
			int skillLevel = activeChar.getLevel() > 40 ? 2 : 1;
			
			L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
			if (activeChar.getFirstEffect(skill) == null)
			{
				skill.getEffects(activeChar, activeChar, false, false, false);
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
				sm.addSkillName(skillId);
				activeChar.sendPacket(sm);
			}
		}
	}
	
	static class EscapeFinalizer implements Runnable
	{
		private final L2PcInstance _activeChar;
		private final L2PcInstance _partner;
		private final boolean _to7sDungeon;
		
		EscapeFinalizer(L2PcInstance activeChar, L2PcInstance partner)
		{
			_activeChar = activeChar;
			_partner = partner;
			_to7sDungeon = partner.isIn7sDungeon();
		}
		
		@Override
		public void run()
		{
			if (_activeChar.isDead())
			{
				return;
			}
			
			_activeChar.enableAllSkills();
			
			// RE-CHECK: Validate conditions again before final teleport.
			// The player might have run into a siege zone or event while casting.
			if (_activeChar.isInsideZone(ZoneId.ZONE_SIEGE) || _activeChar.isInsideZone(ZoneId.ZONE_NOSUMMONFRIEND) || _activeChar.isInFunEvent() || _activeChar.isInDuel() || _activeChar.isCursedWeaponEquiped())
			{
				_activeChar.sendMessage("Teleport cancelled: Restricted condition detected.");
				return;
			}
			
			// Validate Partner again (Partner might have disconnected or moved to boss zone)
			if (_partner == null || _partner.isOnline() == 0 || _partner.isInsideZone(ZoneId.ZONE_SIEGE) || GrandBossManager.getInstance().getZone(_partner) != null)
			{
				_activeChar.sendMessage("Teleport cancelled: Partner is in a restricted area or offline.");
				return;
			}
			
			_activeChar.setIsIn7sDungeon(_to7sDungeon);
			
			try
			{
				_activeChar.teleToLocation(_partner.getX(), _partner.getY(), _partner.getZ());
				_activeChar.sendMessage("You have been teleported to your partner.");
			}
			catch (Throwable e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				LOG.warn(e.getMessage(), e);
			}
		}
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}