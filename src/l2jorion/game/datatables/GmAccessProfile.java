package l2jorion.game.datatables;

/**
 * Represents a merged GM permission profile loaded from config/GMAccess/ XML files. Combines the old AccessLevel system with granular GMAccess permissions. When a PlayerID is defined, the player automatically receives the access level on login.
 */
public class GmAccessProfile
{
	private final String _profileName;
	
	// Access Level properties (merged from old accessLevels.xml)
	private int _accessLevel = 0;
	private String _levelName = "";
	private int _nameColor = 0xFFFFFF;
	private int _titleColor = 0x77FFFF;
	private boolean _useNameColor = true;
	private boolean _useTitleColor = false;
	private boolean _giveDamage = true;
	private boolean _takeAggro = true;
	private boolean _gainExp = true;
	private boolean _canDisableGmStatus = false;
	
	// Core GM flags
	private boolean _isGM = false;
	private boolean _canUseGMCommand = false;
	
	// Action permissions
	private boolean _canAnnounce = false;
	private boolean _canBan = false;
	private boolean _canBanChat = false;
	private boolean _canUnBanChat = false;
	private boolean _useGMShop = false;
	private boolean _canDelete = false;
	private boolean _canKick = false;
	private boolean _menu = false;
	private boolean _godMode = false;
	
	// Character editing
	private boolean _canEditCharAll = false;
	private boolean _canEditChar = false;
	private boolean _canEditPledge = false;
	private boolean _canViewChar = false;
	
	// NPC editing
	private boolean _canEditNPC = false;
	private boolean _canViewNPC = false;
	
	// Movement & world
	private boolean _canTeleport = false;
	private boolean _canRestart = false;
	private boolean _monsterRace = false;
	private boolean _rider = false;
	private boolean _fastUnstuck = false;
	private boolean _resurectFixed = false;
	private boolean _door = false;
	private boolean _res = false;
	
	// Combat & interaction
	private boolean _peaceAttack = false;
	private boolean _heal = false;
	private boolean _unblock = false;
	private boolean _canChangeClass = false;
	
	// Inventory & trade
	private boolean _blockInventory = false;
	private boolean _canDropAnyItems = false;
	private boolean _canTradeAnyItem = false;
	
	// GM special
	private boolean _canGmEdit = false;
	private boolean _isEventGm = false;
	private boolean _canReload = false;
	private boolean _canRename = false;
	private boolean _canJail = false;
	private boolean _canPolymorph = false;
	
	// Player actions (default true for normal players)
	private boolean _useInventory = true;
	private boolean _useTrade = true;
	private boolean _canAttack = true;
	private boolean _canEvaluate = true;
	private boolean _canJoinParty = true;
	private boolean _canJoinClan = true;
	private boolean _useWarehouse = true;
	private boolean _useShop = true;
	
	public GmAccessProfile(String profileName)
	{
		_profileName = profileName;
	}
	
	public AccessLevel toAccessLevel()
	{
		return new AccessLevel(_accessLevel, _levelName, _nameColor, _titleColor, _isGM, _peaceAttack, // allowPeaceAttack
			_resurectFixed, // allowFixedRes
			_useTrade, // allowTransaction
			_menu, // allowAltG
			_giveDamage, _takeAggro, _gainExp, _useNameColor, _useTitleColor, _canDisableGmStatus);
	}
	
	public String getProfileName()
	{
		return _profileName;
	}
	
	// --- Access Level properties ---
	public int getAccessLevel()
	{
		return _accessLevel;
	}
	
	public void setAccessLevel(int val)
	{
		_accessLevel = val;
	}
	
	public String getLevelName()
	{
		return _levelName;
	}
	
	public void setLevelName(String val)
	{
		_levelName = val;
	}
	
	public int getNameColor()
	{
		return _nameColor;
	}
	
	public void setNameColor(int val)
	{
		_nameColor = val;
	}
	
	public int getTitleColor()
	{
		return _titleColor;
	}
	
	public void setTitleColor(int val)
	{
		_titleColor = val;
	}
	
	public boolean useNameColor()
	{
		return _useNameColor;
	}
	
	public void setUseNameColor(boolean val)
	{
		_useNameColor = val;
	}
	
	public boolean useTitleColor()
	{
		return _useTitleColor;
	}
	
	public void setUseTitleColor(boolean val)
	{
		_useTitleColor = val;
	}
	
	public boolean giveDamage()
	{
		return _giveDamage;
	}
	
	public void setGiveDamage(boolean val)
	{
		_giveDamage = val;
	}
	
	public boolean takeAggro()
	{
		return _takeAggro;
	}
	
	public void setTakeAggro(boolean val)
	{
		_takeAggro = val;
	}
	
	public boolean gainExp()
	{
		return _gainExp;
	}
	
	public void setGainExp(boolean val)
	{
		_gainExp = val;
	}
	
	public boolean canDisableGmStatus()
	{
		return _canDisableGmStatus;
	}
	
	public void setCanDisableGmStatus(boolean val)
	{
		_canDisableGmStatus = val;
	}
	
	// --- Core GM ---
	public boolean isGM()
	{
		return _isGM;
	}
	
	public void setIsGM(boolean val)
	{
		_isGM = val;
	}
	
	public boolean canUseGMCommand()
	{
		return _canUseGMCommand;
	}
	
	public void setCanUseGMCommand(boolean val)
	{
		_canUseGMCommand = val;
	}
	
	// --- Action permissions ---
	public boolean canAnnounce()
	{
		return _canAnnounce;
	}
	
	public void setCanAnnounce(boolean val)
	{
		_canAnnounce = val;
	}
	
	public boolean canBan()
	{
		return _canBan;
	}
	
	public void setCanBan(boolean val)
	{
		_canBan = val;
	}
	
	public boolean canBanChat()
	{
		return _canBanChat;
	}
	
	public void setCanBanChat(boolean val)
	{
		_canBanChat = val;
	}
	
	public boolean canUnBanChat()
	{
		return _canUnBanChat;
	}
	
	public void setCanUnBanChat(boolean val)
	{
		_canUnBanChat = val;
	}
	
	public boolean canUseGMShop()
	{
		return _useGMShop;
	}
	
	public void setUseGMShop(boolean val)
	{
		_useGMShop = val;
	}
	
	public boolean canDelete()
	{
		return _canDelete;
	}
	
	public void setCanDelete(boolean val)
	{
		_canDelete = val;
	}
	
	public boolean canKick()
	{
		return _canKick;
	}
	
	public void setCanKick(boolean val)
	{
		_canKick = val;
	}
	
	public boolean hasMenu()
	{
		return _menu;
	}
	
	public void setMenu(boolean val)
	{
		_menu = val;
	}
	
	public boolean hasGodMode()
	{
		return _godMode;
	}
	
	public void setGodMode(boolean val)
	{
		_godMode = val;
	}
	
	// --- Character editing ---
	public boolean canEditCharAll()
	{
		return _canEditCharAll;
	}
	
	public void setCanEditCharAll(boolean val)
	{
		_canEditCharAll = val;
	}
	
	public boolean canEditChar()
	{
		return _canEditChar;
	}
	
	public void setCanEditChar(boolean val)
	{
		_canEditChar = val;
	}
	
	public boolean canEditPledge()
	{
		return _canEditPledge;
	}
	
	public void setCanEditPledge(boolean val)
	{
		_canEditPledge = val;
	}
	
	public boolean canViewChar()
	{
		return _canViewChar;
	}
	
	public void setCanViewChar(boolean val)
	{
		_canViewChar = val;
	}
	
	// --- NPC editing ---
	public boolean canEditNPC()
	{
		return _canEditNPC;
	}
	
	public void setCanEditNPC(boolean val)
	{
		_canEditNPC = val;
	}
	
	public boolean canViewNPC()
	{
		return _canViewNPC;
	}
	
	public void setCanViewNPC(boolean val)
	{
		_canViewNPC = val;
	}
	
	// --- Movement & world ---
	public boolean canTeleport()
	{
		return _canTeleport;
	}
	
	public void setCanTeleport(boolean val)
	{
		_canTeleport = val;
	}
	
	public boolean canRestart()
	{
		return _canRestart;
	}
	
	public void setCanRestart(boolean val)
	{
		_canRestart = val;
	}
	
	public boolean canMonsterRace()
	{
		return _monsterRace;
	}
	
	public void setMonsterRace(boolean val)
	{
		_monsterRace = val;
	}
	
	public boolean canRide()
	{
		return _rider;
	}
	
	public void setRider(boolean val)
	{
		_rider = val;
	}
	
	public boolean hasFastUnstuck()
	{
		return _fastUnstuck;
	}
	
	public void setFastUnstuck(boolean val)
	{
		_fastUnstuck = val;
	}
	
	public boolean hasResurectFixed()
	{
		return _resurectFixed;
	}
	
	public void setResurectFixed(boolean val)
	{
		_resurectFixed = val;
	}
	
	public boolean canDoor()
	{
		return _door;
	}
	
	public void setDoor(boolean val)
	{
		_door = val;
	}
	
	public boolean canRes()
	{
		return _res;
	}
	
	public void setRes(boolean val)
	{
		_res = val;
	}
	
	// --- Combat & interaction ---
	public boolean canPeaceAttack()
	{
		return _peaceAttack;
	}
	
	public void setPeaceAttack(boolean val)
	{
		_peaceAttack = val;
	}
	
	public boolean canHeal()
	{
		return _heal;
	}
	
	public void setHeal(boolean val)
	{
		_heal = val;
	}
	
	public boolean canUnblock()
	{
		return _unblock;
	}
	
	public void setUnblock(boolean val)
	{
		_unblock = val;
	}
	
	public boolean canChangeClass()
	{
		return _canChangeClass;
	}
	
	public void setCanChangeClass(boolean val)
	{
		_canChangeClass = val;
	}
	
	// --- Inventory & trade ---
	public boolean isBlockInventory()
	{
		return _blockInventory;
	}
	
	public void setBlockInventory(boolean val)
	{
		_blockInventory = val;
	}
	
	public boolean canDropAnyItems()
	{
		return _canDropAnyItems;
	}
	
	public void setCanDropAnyItems(boolean val)
	{
		_canDropAnyItems = val;
	}
	
	public boolean canTradeAnyItem()
	{
		return _canTradeAnyItem;
	}
	
	public void setCanTradeAnyItem(boolean val)
	{
		_canTradeAnyItem = val;
	}
	
	// --- GM special ---
	public boolean canGmEdit()
	{
		return _canGmEdit;
	}
	
	public void setCanGmEdit(boolean val)
	{
		_canGmEdit = val;
	}
	
	public boolean isEventGm()
	{
		return _isEventGm;
	}
	
	public void setIsEventGm(boolean val)
	{
		_isEventGm = val;
	}
	
	public boolean canReload()
	{
		return _canReload;
	}
	
	public void setCanReload(boolean val)
	{
		_canReload = val;
	}
	
	public boolean canRename()
	{
		return _canRename;
	}
	
	public void setCanRename(boolean val)
	{
		_canRename = val;
	}
	
	public boolean canJail()
	{
		return _canJail;
	}
	
	public void setCanJail(boolean val)
	{
		_canJail = val;
	}
	
	public boolean canPolymorph()
	{
		return _canPolymorph;
	}
	
	public void setCanPolymorph(boolean val)
	{
		_canPolymorph = val;
	}
	
	// --- Player actions ---
	public boolean canUseInventory()
	{
		return _useInventory;
	}
	
	public void setUseInventory(boolean val)
	{
		_useInventory = val;
	}
	
	public boolean canUseTrade()
	{
		return _useTrade;
	}
	
	public void setUseTrade(boolean val)
	{
		_useTrade = val;
	}
	
	public boolean canAttack()
	{
		return _canAttack;
	}
	
	public void setCanAttack(boolean val)
	{
		_canAttack = val;
	}
	
	public boolean canEvaluate()
	{
		return _canEvaluate;
	}
	
	public void setCanEvaluate(boolean val)
	{
		_canEvaluate = val;
	}
	
	public boolean canJoinParty()
	{
		return _canJoinParty;
	}
	
	public void setCanJoinParty(boolean val)
	{
		_canJoinParty = val;
	}
	
	public boolean canJoinClan()
	{
		return _canJoinClan;
	}
	
	public void setCanJoinClan(boolean val)
	{
		_canJoinClan = val;
	}
	
	public boolean canUseWarehouse()
	{
		return _useWarehouse;
	}
	
	public void setUseWarehouse(boolean val)
	{
		_useWarehouse = val;
	}
	
	public boolean canUseShop()
	{
		return _useShop;
	}
	
	public void setUseShop(boolean val)
	{
		_useShop = val;
	}
	
	public boolean isCommandAllowed(String command)
	{
		if (!_canUseGMCommand)
		{
			return false;
		}
		
		// Ban commands
		if (command.startsWith("admin_ban") || command.startsWith("admin_unban"))
		{
			if (command.contains("chat"))
			{
				return _canBanChat;
			}
			return _canBan;
		}
		
		// Chat ban
		if (command.startsWith("admin_banchat") || command.startsWith("admin_unbanchat"))
		{
			return _canBanChat;
		}
		
		// Kick
		if (command.startsWith("admin_kick") || command.equals("admin_character_disconnect"))
		{
			return _canKick;
		}
		
		// Jail
		if (command.startsWith("admin_jail") || command.startsWith("admin_unjail") || command.startsWith("admin_massjail"))
		{
			return _canJail;
		}
		
		// Announcements
		if (command.startsWith("admin_announce") || command.contains("announcement") || command.contains("autoannounce"))
		{
			return _canAnnounce;
		}
		
		// Teleport
		if (command.startsWith("admin_teleport") || command.startsWith("admin_recall") || command.equals("admin_move_to") || command.startsWith("admin_go") || command.startsWith("admin_tele") || command.equals("admin_sendhome") || command.equals("admin_instant_move"))
		{
			return _canTeleport;
		}
		
		// Server restart/shutdown
		if (command.startsWith("admin_server_restart") || command.startsWith("admin_server_shutdown") || command.startsWith("admin_server_abort"))
		{
			return _canRestart;
		}
		
		// Reload
		if (command.startsWith("admin_reload") || command.startsWith("admin_cache") || command.equals("admin_config_reload") || command.equals("admin_config_reload_menu") || command.startsWith("admin_script_load") || command.startsWith("admin_quest_reload")
			|| command.startsWith("admin_spawn_reload") || command.startsWith("admin_zone_reload") || command.startsWith("admin_teleport_reload"))
		{
			return _canReload;
		}
		
		// God mode / invulnerability
		if (command.equals("admin_invul") || command.equals("admin_setinvul"))
		{
			return _godMode;
		}
		
		// Invisibility
		if (command.equals("admin_invis") || command.equals("admin_hide"))
		{
			return _isGM;
		}
		
		// Delete
		if (command.equals("admin_delete"))
		{
			return _canDelete;
		}
		
		// GM Shop
		if (command.equals("admin_gmshop") || command.equals("admin_buy"))
		{
			return _useGMShop;
		}
		
		// Create items
		if (command.startsWith("admin_create_item") || command.equals("admin_itemcreate") || command.startsWith("admin_create_adena") || command.startsWith("admin_create_coin") || command.startsWith("admin_give_item"))
		{
			return _useGMShop;
		}
		
		// Heal / Resurrect
		if (command.startsWith("admin_heal") || command.equals("admin_res") || command.equals("admin_massress") || command.equals("admin_setcp") || command.equals("admin_sethp") || command.equals("admin_setmp"))
		{
			return _heal || _res;
		}
		
		// Polymorph
		if (command.startsWith("admin_polymorph") || command.startsWith("admin_unpolymorph") || command.startsWith("admin_polyself") || command.startsWith("admin_unpolyself") || command.startsWith("admin_transform") || command.startsWith("admin_untransform"))
		{
			return _canPolymorph;
		}
		
		// Rename
		if (command.equals("admin_changename") || command.equals("admin_setname") || command.equals("admin_changename_menu"))
		{
			return _canRename;
		}
		
		// Character editing
		if (command.equals("admin_edit_character") || command.equals("admin_edit_stats") || command.equals("admin_edit_class") || command.equals("admin_edit_quest") || command.startsWith("admin_setchar") || command.equals("admin_setclass") || command.startsWith("admin_add_exp")
			|| command.startsWith("admin_remove_exp") || command.startsWith("admin_add_level") || command.equals("admin_addlevel") || command.equals("admin_set_level") || command.equals("admin_remlevel") || command.startsWith("admin_setkarma") || command.startsWith("admin_nokarma")
			|| command.equals("admin_setcolor") || command.equals("admin_settitle") || command.equals("admin_setsex") || command.equals("admin_setew") || command.equals("admin_sethero") || command.equals("admin_setnoble") || command.equals("admin_setdonator"))
		{
			return _canEditChar || _canEditCharAll;
		}
		
		// View character
		if (command.equals("admin_character_info") || command.equals("admin_character_list") || command.equals("admin_current_player") || command.equals("admin_show_characters") || command.equals("admin_find_character") || command.equals("admin_find_ip") || command.equals("admin_find_account"))
		{
			return _canViewChar;
		}
		
		// NPC editing
		if (command.startsWith("admin_edit_npc") || command.startsWith("admin_save_npc") || command.startsWith("admin_edit_drop") || command.startsWith("admin_add_drop") || command.startsWith("admin_del_drop") || command.startsWith("admin_edit_skill_npc") || command.startsWith("admin_add_skill_npc")
			|| command.startsWith("admin_del_skill_npc") || command.startsWith("admin_addShop") || command.startsWith("admin_delShop") || command.startsWith("admin_editShop") || command.startsWith("admin_addCustomShop") || command.startsWith("admin_delCustomShop")
			|| command.startsWith("admin_editCustomShop"))
		{
			return _canEditNPC;
		}
		
		// View NPC
		if (command.startsWith("admin_show_droplist") || command.startsWith("admin_showShop") || command.startsWith("admin_showCustomShop") || command.startsWith("admin_show_skilllist_npc"))
		{
			return _canViewNPC;
		}
		
		// Spawn
		if (command.startsWith("admin_spawn") || command.startsWith("admin_cspawn") || command.equals("admin_respawnall") || command.equals("admin_unspawnall") || command.startsWith("admin_list_spawns") || command.startsWith("admin_spawnlist") || command.startsWith("admin_spawnday")
			|| command.startsWith("admin_spawnnight") || command.startsWith("admin_mammon"))
		{
			return _canEditNPC;
		}
		
		// Enchant
		if (command.startsWith("admin_enchant") || command.startsWith("admin_sete") || command.startsWith("admin_setle") || command.startsWith("admin_setre") || command.startsWith("admin_setlf") || command.startsWith("admin_setrf") || command.startsWith("admin_seten")
			|| command.startsWith("admin_setun") || command.startsWith("admin_setba") || command.startsWith("admin_setl"))
		{
			return _canEditChar || _canEditCharAll;
		}
		
		// Pledge/Clan
		if (command.startsWith("admin_pledge") || command.startsWith("admin_setclanlv") || command.startsWith("admin_remclanwait"))
		{
			return _canEditPledge;
		}
		
		// Door control
		if (command.startsWith("admin_open") || command.startsWith("admin_close"))
		{
			return _door;
		}
		
		// Ride
		if (command.startsWith("admin_ride") || command.startsWith("admin_unride"))
		{
			return _rider;
		}
		
		// Class change
		if (command.equals("admin_setclass"))
		{
			return _canChangeClass;
		}
		
		// Event GM
		if (command.equals("admin_event") || command.equals("admin_start") || command.equals("admin_abort"))
		{
			return _isEventGm;
		}
		
		// Siege
		if (command.startsWith("admin_siege") || command.startsWith("admin_add_attacker") || command.startsWith("admin_add_defender") || command.startsWith("admin_add_guard") || command.startsWith("admin_list_siege") || command.startsWith("admin_clear_siege")
			|| command.startsWith("admin_move_defenders") || command.startsWith("admin_spawn_doors") || command.startsWith("admin_endsiege") || command.startsWith("admin_startsiege") || command.startsWith("admin_setcastle") || command.startsWith("admin_removecastle")
			|| command.startsWith("admin_setsiegetime") || command.startsWith("admin_remaining_time"))
		{
			return _canEditCharAll;
		}
		
		// Admin menu / generic admin
		if (command.startsWith("admin_admin") || command.startsWith("admin_config") || command.equals("admin_edit") || command.equals("admin_debug") || command.startsWith("admin_effect") || command.startsWith("admin_social") || command.startsWith("admin_play_sound")
			|| command.startsWith("admin_abnormal") || command.startsWith("admin_clearteams") || command.startsWith("admin_setteam") || command.startsWith("admin_diet") || command.startsWith("admin_fullfood") || command.startsWith("admin_bookmark") || command.startsWith("admin_close_window")
			|| command.startsWith("admin_target") || command.startsWith("admin_silence") || command.equals("admin_gmspeed") || command.equals("admin_gmspeed_menu") || command.equals("admin_gm") || command.startsWith("admin_gmchat") || command.startsWith("admin_gmlist"))
		{
			return _menu || _isGM;
		}
		
		// Petition
		if (command.startsWith("admin_accept_petition") || command.startsWith("admin_reject_petition") || command.startsWith("admin_reset_petition") || command.startsWith("admin_view_petition") || command.startsWith("admin_force_peti") || command.startsWith("admin_add_peti"))
		{
			return _isGM;
		}
		
		// Snoop
		if (command.startsWith("admin_snoop") || command.startsWith("admin_unsnoop"))
		{
			return _canViewChar;
		}
		
		// Skills
		if (command.startsWith("admin_show_skills") || command.startsWith("admin_remove_skill") || command.startsWith("admin_skill_") || command.startsWith("admin_add_skill") || command.startsWith("admin_add_clan_skill") || command.startsWith("admin_get_skills")
			|| command.startsWith("admin_reset_skills") || command.startsWith("admin_give_all_skills") || command.startsWith("admin_remove_all_skills") || command.startsWith("admin_ench_skills") || command.startsWith("admin_cast_skill"))
		{
			return _canEditChar || _canEditCharAll;
		}
		
		// Kill
		if (command.equals("admin_kill") || command.equals("admin_kill_monster") || command.equals("admin_masskill"))
		{
			return _canAttack;
		}
		
		// GmAccess management (only master access)
		if (command.startsWith("admin_gmaccess"))
		{
			return _canEditCharAll;
		}
		
		// Everything else: allow if GM
		return _isGM;
	}
}
