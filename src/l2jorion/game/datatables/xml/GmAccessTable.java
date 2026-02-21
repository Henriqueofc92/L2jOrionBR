package l2jorion.game.datatables.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import l2jorion.Config;
import l2jorion.game.datatables.GmAccessProfile;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;

/**
 * Loads GM access profiles from config/GMAccess/*.xml files. Each XML defines a profile with access level properties and granular permissions. PlayerIDs in the XML automatically receive the defined access level on login. Replaces the old accessLevels.xml system.
 */
public class GmAccessTable
{
	private static final Logger LOG = LoggerFactory.getLogger(GmAccessTable.class.getName());
	
	private static GmAccessTable _instance = null;
	
	/** Maps player objectId to their GM access profile */
	private final Map<Integer, GmAccessProfile> _playerProfiles = new HashMap<>();
	
	/** Maps profile name (filename without .xml) to template profile */
	private final Map<String, GmAccessProfile> _templateProfiles = new HashMap<>();
	
	/** Maps access level number to a profile (for AccessLevels integration) */
	private final Map<Integer, GmAccessProfile> _levelProfiles = new HashMap<>();
	
	private GmAccessTable()
	{
		load();
	}
	
	private void load()
	{
		_playerProfiles.clear();
		_templateProfiles.clear();
		_levelProfiles.clear();
		
		File dir = new File(Config.DATAPACK_ROOT, "config/GMAccess");
		if (!dir.exists() || !dir.isDirectory())
		{
			LOG.warn("GmAccessTable: config/GMAccess/ directory not found.");
			return;
		}
		
		File[] files = dir.listFiles(f -> f.getName().endsWith(".xml"));
		if (files == null || files.length == 0)
		{
			LOG.warn("GmAccessTable: No XML files found in config/GMAccess/.");
			return;
		}
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		
		int profileCount = 0;
		int playerCount = 0;
		
		for (File file : files)
		{
			try (FileInputStream fis = new FileInputStream(file);
				InputStreamReader isr = new InputStreamReader(fis, "UTF-8"))
			{
				String profileName = file.getName().replace(".xml", "");
				
				InputSource in = new InputSource(isr);
				in.setEncoding("UTF-8");
				Document doc = factory.newDocumentBuilder().parse(in);
				
				for (Node listNode = doc.getFirstChild(); listNode != null; listNode = listNode.getNextSibling())
				{
					if (!"list".equalsIgnoreCase(listNode.getNodeName()))
					{
						continue;
					}
					
					for (Node charNode = listNode.getFirstChild(); charNode != null; charNode = charNode.getNextSibling())
					{
						if (!"char".equalsIgnoreCase(charNode.getNodeName()))
						{
							continue;
						}
						
						GmAccessProfile profile = new GmAccessProfile(profileName);
						List<Integer> playerIds = new ArrayList<>();
						
						// Parse all child elements of <char>
						for (Node propNode = charNode.getFirstChild(); propNode != null; propNode = propNode.getNextSibling())
						{
							if (propNode.getNodeType() != Node.ELEMENT_NODE)
							{
								continue;
							}
							
							String nodeName = propNode.getNodeName();
							Node setAttr = propNode.getAttributes().getNamedItem("set");
							if (setAttr == null)
							{
								continue;
							}
							
							String value = setAttr.getNodeValue();
							
							switch (nodeName)
							{
								// Player binding
								case "PlayerID":
									int pid = Integer.parseInt(value);
									if (pid > 0)
									{
										playerIds.add(pid);
									}
									break;
								
								// Access Level properties (merged from accessLevels.xml)
								case "AccessLevel":
									profile.setAccessLevel(Integer.parseInt(value));
									break;
								case "LevelName":
									profile.setLevelName(value);
									break;
								case "NameColor":
									profile.setNameColor(Integer.decode("0x" + value));
									break;
								case "TitleColor":
									profile.setTitleColor(Integer.decode("0x" + value));
									break;
								case "UseNameColor":
									profile.setUseNameColor(Boolean.parseBoolean(value));
									break;
								case "UseTitleColor":
									profile.setUseTitleColor(Boolean.parseBoolean(value));
									break;
								case "GiveDamage":
									profile.setGiveDamage(Boolean.parseBoolean(value));
									break;
								case "TakeAggro":
									profile.setTakeAggro(Boolean.parseBoolean(value));
									break;
								case "GainExp":
									profile.setGainExp(Boolean.parseBoolean(value));
									break;
								case "CanDisableGmStatus":
									profile.setCanDisableGmStatus(Boolean.parseBoolean(value));
									break;
								
								// Core GM flags
								case "IsGM":
									profile.setIsGM(Boolean.parseBoolean(value));
									break;
								case "CanUseGMCommand":
									profile.setCanUseGMCommand(Boolean.parseBoolean(value));
									break;
								
								// Action permissions
								case "CanAnnounce":
									profile.setCanAnnounce(Boolean.parseBoolean(value));
									break;
								case "CanBan":
									profile.setCanBan(Boolean.parseBoolean(value));
									break;
								case "CanBanChat":
									profile.setCanBanChat(Boolean.parseBoolean(value));
									break;
								case "CanUnBanChat":
									profile.setCanUnBanChat(Boolean.parseBoolean(value));
									break;
								case "UseGMShop":
									profile.setUseGMShop(Boolean.parseBoolean(value));
									break;
								case "CanDelete":
									profile.setCanDelete(Boolean.parseBoolean(value));
									break;
								case "CanKick":
									profile.setCanKick(Boolean.parseBoolean(value));
									break;
								case "Menu":
									profile.setMenu(Boolean.parseBoolean(value));
									break;
								case "GodMode":
									profile.setGodMode(Boolean.parseBoolean(value));
									break;
								
								// Character editing
								case "CanEditCharAll":
									profile.setCanEditCharAll(Boolean.parseBoolean(value));
									break;
								case "CanEditChar":
									profile.setCanEditChar(Boolean.parseBoolean(value));
									break;
								case "CanEditPledge":
									profile.setCanEditPledge(Boolean.parseBoolean(value));
									break;
								case "CanViewChar":
									profile.setCanViewChar(Boolean.parseBoolean(value));
									break;
								
								// NPC editing
								case "CanEditNPC":
									profile.setCanEditNPC(Boolean.parseBoolean(value));
									break;
								case "CanViewNPC":
									profile.setCanViewNPC(Boolean.parseBoolean(value));
									break;
								
								// Movement & world
								case "CanTeleport":
									profile.setCanTeleport(Boolean.parseBoolean(value));
									break;
								case "CanRestart":
									profile.setCanRestart(Boolean.parseBoolean(value));
									break;
								case "MonsterRace":
									profile.setMonsterRace(Boolean.parseBoolean(value));
									break;
								case "Rider":
									profile.setRider(Boolean.parseBoolean(value));
									break;
								case "FastUnstuck":
									profile.setFastUnstuck(Boolean.parseBoolean(value));
									break;
								case "ResurectFixed":
									profile.setResurectFixed(Boolean.parseBoolean(value));
									break;
								case "Door":
									profile.setDoor(Boolean.parseBoolean(value));
									break;
								case "Res":
									profile.setRes(Boolean.parseBoolean(value));
									break;
								
								// Combat & interaction
								case "PeaceAttack":
									profile.setPeaceAttack(Boolean.parseBoolean(value));
									break;
								case "Heal":
									profile.setHeal(Boolean.parseBoolean(value));
									break;
								case "Unblock":
									profile.setUnblock(Boolean.parseBoolean(value));
									break;
								case "CanChangeClass":
									profile.setCanChangeClass(Boolean.parseBoolean(value));
									break;
								
								// Inventory & trade
								case "BlockInventory":
									profile.setBlockInventory(Boolean.parseBoolean(value));
									break;
								case "CanDropAnyItems":
									profile.setCanDropAnyItems(Boolean.parseBoolean(value));
									break;
								case "CanTradeAnyItem":
									profile.setCanTradeAnyItem(Boolean.parseBoolean(value));
									break;
								
								// GM special
								case "CanGmEdit":
									profile.setCanGmEdit(Boolean.parseBoolean(value));
									break;
								case "IsEventGm":
									profile.setIsEventGm(Boolean.parseBoolean(value));
									break;
								case "CanReload":
									profile.setCanReload(Boolean.parseBoolean(value));
									break;
								case "CanRename":
									profile.setCanRename(Boolean.parseBoolean(value));
									break;
								case "CanJail":
									profile.setCanJail(Boolean.parseBoolean(value));
									break;
								case "CanPolymorph":
									profile.setCanPolymorph(Boolean.parseBoolean(value));
									break;
								
								// Player actions
								case "UseInventory":
									profile.setUseInventory(Boolean.parseBoolean(value));
									break;
								case "UseTrade":
									profile.setUseTrade(Boolean.parseBoolean(value));
									break;
								case "CanAttack":
									profile.setCanAttack(Boolean.parseBoolean(value));
									break;
								case "CanEvaluate":
									profile.setCanEvaluate(Boolean.parseBoolean(value));
									break;
								case "CanJoinParty":
									profile.setCanJoinParty(Boolean.parseBoolean(value));
									break;
								case "CanJoinClan":
									profile.setCanJoinClan(Boolean.parseBoolean(value));
									break;
								case "UseWarehouse":
									profile.setUseWarehouse(Boolean.parseBoolean(value));
									break;
								case "UseShop":
									profile.setUseShop(Boolean.parseBoolean(value));
									break;
								
								// Silently skip unknown fields (e.g., moderator-specific like BanChatDelay)
								default:
									break;
							}
						}
						
						// Store the template profile
						_templateProfiles.put(profileName, profile);
						
						// Store by access level number (for AccessLevels integration)
						if (profile.getAccessLevel() > 0)
						{
							_levelProfiles.put(profile.getAccessLevel(), profile);
						}
						
						// Store player-to-profile mappings
						for (int playerId : playerIds)
						{
							_playerProfiles.put(playerId, profile);
							playerCount++;
						}
						
						profileCount++;
					}
				}
			}
			catch (SAXException | IOException | ParserConfigurationException | NumberFormatException e)
			{
				LOG.error("GmAccessTable: Error loading " + file.getName(), e);
			}
		}
		
		LOG.info("GmAccessTable: Loaded " + profileCount + " profiles with " + playerCount + " player assignments.");
	}
	
	public static GmAccessTable getInstance()
	{
		return _instance == null ? (_instance = new GmAccessTable()) : _instance;
	}
	
	/**
	 * Get the GM access profile for a specific player by their objectId.
	 * @param objectId the player's character objectId
	 * @return the GmAccessProfile, or null if no profile is assigned
	 */
	public GmAccessProfile getProfile(int objectId)
	{
		return _playerProfiles.get(objectId);
	}
	
	/**
	 * Check if a player has a GM access profile assigned.
	 * @param objectId the player's character objectId
	 * @return true if the player has an assigned profile
	 */
	public boolean hasProfile(int objectId)
	{
		return _playerProfiles.containsKey(objectId);
	}
	
	/**
	 * Get a template profile by name (filename without .xml).
	 * @param profileName the profile name
	 * @return the GmAccessProfile template, or null if not found
	 */
	public GmAccessProfile getTemplateProfile(String profileName)
	{
		return _templateProfiles.get(profileName);
	}
	
	/**
	 * Get a profile by its access level number (for AccessLevels integration).
	 * @param level the access level number
	 * @return the GmAccessProfile, or null if no profile defines that level
	 */
	public GmAccessProfile getProfileByLevel(int level)
	{
		return _levelProfiles.get(level);
	}
	
	/**
	 * Get all template profiles.
	 * @return collection of all template profiles
	 */
	public Collection<GmAccessProfile> getProfiles()
	{
		return _templateProfiles.values();
	}
	
	/**
	 * Get all template profile names.
	 * @return collection of profile names
	 */
	public Collection<String> getProfileNames()
	{
		return _templateProfiles.keySet();
	}
	
	/**
	 * Get the number of player assignments.
	 * @return count of players with assigned profiles
	 */
	public int getPlayerCount()
	{
		return _playerProfiles.size();
	}
	
	/**
	 * Reload all profiles from XML files.
	 */
	public static void reload()
	{
		_instance = null;
		getInstance();
	}
}
