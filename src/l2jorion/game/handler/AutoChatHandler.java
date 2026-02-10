package l2jorion.game.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import l2jorion.Config;
import l2jorion.game.model.L2Character;
import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.actor.instance.L2SiegeGuardInstance;
import l2jorion.game.model.spawn.L2Spawn;
import l2jorion.game.model.spawn.SpawnListener;
import l2jorion.game.network.serverpackets.CreatureSay;
import l2jorion.game.thread.ThreadPoolManager;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;
import l2jorion.util.random.Rnd;

public class AutoChatHandler implements SpawnListener
{
	
	private static final Logger LOG = LoggerFactory.getLogger(AutoChatHandler.class);
	private static final int DEFAULT_CHAT_DELAY = 30000;
	private static final String AUTO_CHAT_FILE = Config.DATAPACK_ROOT + "/data/xml/world/autoChat.xml";
	
	private static AutoChatHandler _instance;
	
	private final Map<Integer, AutoChatInstance> _registeredChats;
	
	private AutoChatHandler()
	{
		_registeredChats = new HashMap<>();
		restoreChatData();
		L2Spawn.addSpawnListener(this);
	}
	
	private void restoreChatData()
	{
		int numLoaded = 0;
		int numLoaded1 = 0;
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		
		File file = new File(AUTO_CHAT_FILE);
		if (!file.exists())
		{
			LOG.warn("autochat.xml could not be loaded: file not found");
			return;
		}
		
		try (FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8"))
		{
			
			InputSource in = new InputSource(isr);
			in.setEncoding("UTF-8");
			Document doc = factory.newDocumentBuilder().parse(in);
			
			String[] messages = new String[8];
			
			for (Node node = doc.getFirstChild(); node != null; node = node.getNextSibling())
			{
				if (node.getNodeName().equalsIgnoreCase("list"))
				{
					for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
					{
						if (child.getNodeName().equalsIgnoreCase("autochat"))
						{
							int npcId = Integer.parseInt(child.getAttributes().getNamedItem("npcId").getNodeValue());
							int chatDelay = Integer.parseInt(child.getAttributes().getNamedItem("chatDelay").getNodeValue());
							numLoaded++;
							
							int i = 0;
							for (Node textNode = child.getFirstChild(); textNode != null; textNode = textNode.getNextSibling())
							{
								if (textNode.getNodeName().equalsIgnoreCase("chatText"))
								{
									if (textNode.getTextContent() != null)
									{
										messages[i] = textNode.getTextContent();
									}
									i++;
									numLoaded1++;
								}
							}
							
							registerGlobalChat(npcId, messages, chatDelay);
						}
					}
				}
			}
		}
		catch (SAXException | IOException | ParserConfigurationException e)
		{
			LOG.error("Error while creating table", e);
		}
		
		LOG.info("AutoChatHandler: Loaded " + numLoaded + " chat groups.");
		LOG.info("AutoChatHandler: Loaded " + numLoaded1 + " chat texts.");
	}
	
	public static AutoChatHandler getInstance()
	{
		if (_instance == null)
		{
			_instance = new AutoChatHandler();
		}
		
		return _instance;
	}
	
	public int size()
	{
		return _registeredChats.size();
	}
	
	public AutoChatInstance registerGlobalChat(int npcId, String[] chatTexts, int chatDelay)
	{
		return registerChat(npcId, null, chatTexts, chatDelay);
	}
	
	public AutoChatInstance registerChat(L2NpcInstance npcInst, String[] chatTexts, int chatDelay)
	{
		return registerChat(npcInst.getNpcId(), npcInst, chatTexts, chatDelay);
	}
	
	private AutoChatInstance registerChat(int npcId, L2NpcInstance npcInst, String[] chatTexts, int chatDelay)
	{
		AutoChatInstance chatInst = null;
		
		if (chatDelay < 0)
		{
			chatDelay = DEFAULT_CHAT_DELAY;
		}
		
		if (_registeredChats.containsKey(npcId))
		{
			chatInst = _registeredChats.get(npcId);
		}
		else
		{
			chatInst = new AutoChatInstance(npcId, chatTexts, chatDelay, (npcInst == null));
		}
		
		if (npcInst != null)
		{
			chatInst.addChatDefinition(npcInst);
		}
		
		_registeredChats.put(npcId, chatInst);
		
		return chatInst;
	}
	
	public boolean removeChat(int npcId)
	{
		AutoChatInstance chatInst = _registeredChats.get(npcId);
		
		return removeChat(chatInst);
	}
	
	public boolean removeChat(AutoChatInstance chatInst)
	{
		if (chatInst == null)
		{
			return false;
		}
		
		_registeredChats.remove(chatInst.getNPCId());
		chatInst.setActive(false);
		
		return true;
	}
	
	public AutoChatInstance getAutoChatInstance(int id, boolean byObjectId)
	{
		if (!byObjectId)
		{
			return _registeredChats.get(id);
		}
		
		for (AutoChatInstance chatInst : _registeredChats.values())
		{
			if (chatInst.getChatDefinition(id) != null)
			{
				return chatInst;
			}
		}
		
		return null;
	}
	
	public void setAutoChatActive(boolean isActive)
	{
		for (AutoChatInstance chatInst : _registeredChats.values())
		{
			chatInst.setActive(isActive);
		}
	}
	
	public void setAutoChatActive(int npcId, boolean isActive)
	{
		for (AutoChatInstance chatInst : _registeredChats.values())
		{
			if (chatInst.getNPCId() == npcId)
			{
				chatInst.setActive(isActive);
			}
		}
	}
	
	@Override
	public void npcSpawned(L2NpcInstance npc)
	{
		synchronized (_registeredChats)
		{
			if (npc == null)
			{
				return;
			}
			
			int npcId = npc.getNpcId();
			
			if (_registeredChats.containsKey(npcId))
			{
				AutoChatInstance chatInst = _registeredChats.get(npcId);
				
				if (chatInst != null && chatInst.isGlobal())
				{
					chatInst.addChatDefinition(npc);
				}
			}
		}
	}
	
	public class AutoChatInstance
	{
		protected int _npcId;
		private long _defaultDelay = DEFAULT_CHAT_DELAY;
		private String[] _defaultTexts;
		private boolean _defaultRandom = false;
		
		private boolean _globalChat = false;
		private boolean _isActive;
		
		private Map<Integer, AutoChatDefinition> _chatDefinitions = new HashMap<>();
		protected ScheduledFuture<?> _chatTask;
		
		protected AutoChatInstance(int npcId, String[] chatTexts, long chatDelay, boolean isGlobal)
		{
			_defaultTexts = chatTexts;
			_npcId = npcId;
			_defaultDelay = chatDelay;
			_globalChat = isGlobal;
			
			setActive(true);
		}
		
		protected AutoChatDefinition getChatDefinition(int objectId)
		{
			return _chatDefinitions.get(objectId);
		}
		
		protected AutoChatDefinition[] getChatDefinitions()
		{
			return _chatDefinitions.values().toArray(new AutoChatDefinition[0]);
		}
		
		public int addChatDefinition(L2NpcInstance npcInst)
		{
			return addChatDefinition(npcInst, null, 0);
		}
		
		public int addChatDefinition(L2NpcInstance npcInst, String[] chatTexts, long chatDelay)
		{
			int objectId = npcInst.getObjectId();
			
			AutoChatDefinition chatDef = new AutoChatDefinition(this, npcInst, chatTexts, chatDelay);
			
			if (npcInst instanceof L2SiegeGuardInstance)
			{
				chatDef.setRandomChat(true);
			}
			
			_chatDefinitions.put(objectId, chatDef);
			
			chatDef = null;
			
			return objectId;
		}
		
		public boolean removeChatDefinition(int objectId)
		{
			if (!_chatDefinitions.containsKey(objectId))
			{
				return false;
			}
			
			AutoChatDefinition chatDefinition = _chatDefinitions.get(objectId);
			chatDefinition.setActive(false);
			
			_chatDefinitions.remove(objectId);
			
			chatDefinition = null;
			
			return true;
		}
		
		public boolean isActive()
		{
			return _isActive;
		}
		
		public boolean isGlobal()
		{
			return _globalChat;
		}
		
		public boolean isDefaultRandom()
		{
			return _defaultRandom;
		}
		
		public boolean isRandomChat(int objectId)
		{
			if (!_chatDefinitions.containsKey(objectId))
			{
				return false;
			}
			
			return _chatDefinitions.get(objectId).isRandomChat();
		}
		
		public int getNPCId()
		{
			return _npcId;
		}
		
		public int getDefinitionCount()
		{
			return _chatDefinitions.size();
		}
		
		public L2NpcInstance[] getNPCInstanceList()
		{
			List<L2NpcInstance> npcInsts = new ArrayList<>();
			
			for (AutoChatDefinition chatDefinition : _chatDefinitions.values())
			{
				npcInsts.add(chatDefinition._npcInstance);
			}
			
			return npcInsts.toArray(new L2NpcInstance[0]);
		}
		
		public long getDefaultDelay()
		{
			return _defaultDelay;
		}
		
		public String[] getDefaultTexts()
		{
			return _defaultTexts;
		}
		
		public void setDefaultChatDelay(long delayValue)
		{
			_defaultDelay = delayValue;
		}
		
		public void setDefaultChatTexts(String[] textsValue)
		{
			_defaultTexts = textsValue;
		}
		
		public void setDefaultRandom(boolean randValue)
		{
			_defaultRandom = randValue;
		}
		
		public void setChatDelay(int objectId, long delayValue)
		{
			AutoChatDefinition chatDef = getChatDefinition(objectId);
			
			if (chatDef != null)
			{
				chatDef.setChatDelay(delayValue);
			}
			
			chatDef = null;
		}
		
		public void setChatTexts(int objectId, String[] textsValue)
		{
			AutoChatDefinition chatDef = getChatDefinition(objectId);
			
			if (chatDef != null)
			{
				chatDef.setChatTexts(textsValue);
			}
			
			chatDef = null;
		}
		
		public void setRandomChat(int objectId, boolean randValue)
		{
			AutoChatDefinition chatDef = getChatDefinition(objectId);
			
			if (chatDef != null)
			{
				chatDef.setRandomChat(randValue);
			}
			
			chatDef = null;
		}
		
		public void setActive(boolean activeValue)
		{
			if (_isActive == activeValue)
			{
				return;
			}
			
			_isActive = activeValue;
			
			if (!isGlobal())
			{
				for (AutoChatDefinition chatDefinition : _chatDefinitions.values())
				{
					chatDefinition.setActive(activeValue);
				}
				
				return;
			}
			
			if (isActive())
			{
				AutoChatRunner acr = new AutoChatRunner(_npcId, -1);
				_chatTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(acr, _defaultDelay, _defaultDelay);
				acr = null;
			}
			else
			{
				_chatTask.cancel(false);
			}
		}
		
		private class AutoChatDefinition
		{
			protected int _chatIndex = 0;
			protected L2NpcInstance _npcInstance;
			
			protected AutoChatInstance _chatInstance;
			
			private long _chatDelay = 0;
			private String[] _chatTexts = null;
			private boolean _isActiveDefinition;
			private boolean _randomChat;
			
			protected AutoChatDefinition(AutoChatInstance chatInst, L2NpcInstance npcInst, String[] chatTexts, long chatDelay)
			{
				_npcInstance = npcInst;
				
				_chatInstance = chatInst;
				_randomChat = chatInst.isDefaultRandom();
				
				_chatDelay = chatDelay;
				_chatTexts = chatTexts;
				
				if (!chatInst.isGlobal())
				{
					setActive(true);
				}
			}
			
			protected String[] getChatTexts()
			{
				return (_chatTexts != null) ? _chatTexts : _chatInstance.getDefaultTexts();
			}
			
			private long getChatDelay()
			{
				return (_chatDelay > 0) ? _chatDelay : _chatInstance.getDefaultDelay();
			}
			
			private boolean isActive()
			{
				return _isActiveDefinition;
			}
			
			boolean isRandomChat()
			{
				return _randomChat;
			}
			
			void setRandomChat(boolean randValue)
			{
				_randomChat = randValue;
			}
			
			void setChatDelay(long delayValue)
			{
				_chatDelay = delayValue;
			}
			
			void setChatTexts(String[] textsValue)
			{
				_chatTexts = textsValue;
			}
			
			void setActive(boolean activeValue)
			{
				if (isActive() == activeValue)
				{
					return;
				}
				
				if (activeValue)
				{
					AutoChatRunner acr = new AutoChatRunner(_npcId, _npcInstance.getObjectId());
					
					if (getChatDelay() == 0)
					{
						_chatTask = ThreadPoolManager.getInstance().scheduleGeneral(acr, Rnd.nextInt(30000));
					}
					else
					{
						_chatTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(acr, ThreadLocalRandom.current().nextLong(30000L), getChatDelay());
					}
					
					acr = null;
				}
				
				else
				{
					if (_chatTask != null)
					{
						_chatTask.cancel(false);
						_chatTask = null;
					}
				}
				
				_isActiveDefinition = activeValue;
			}
		}
	}
	
	private class AutoChatRunner implements Runnable
	{
		private final int _npcId;
		private final int _objectId;
		
		AutoChatRunner(int npcId, int objectId)
		{
			_npcId = npcId;
			_objectId = objectId;
		}
		
		@Override
		public void run()
		{
			AutoChatInstance chatInst = _registeredChats.get(_npcId);
			
			if (chatInst != null && chatInst.isActive())
			{
				if (_objectId >= 0)
				{
					AutoChatInstance.AutoChatDefinition chatDef = chatInst.getChatDefinition(_objectId);
					
					if (chatDef != null && chatDef.isActive())
					{
						if (chatDef.isRandomChat())
						{
							String[] texts = chatDef.getChatTexts();
							if (texts != null && texts.length > 0)
							{
								int randomIndex = Rnd.get(texts.length);
								String randomText = texts[randomIndex];
								if (randomText != null && !randomText.isEmpty())
								{
									L2Character target = chatDef._npcInstance;
									if (target instanceof L2PcInstance)
									{
										L2PcInstance player = (L2PcInstance) target;
										if (player.isOnline() == 1)
										{
											player.sendPacket(new CreatureSay(chatDef._npcInstance.getObjectId(), 0, chatDef._npcInstance.getName(), randomText));
										}
									}
								}
							}
						}
						else
						{
							String[] texts = chatDef.getChatTexts();
							if (texts != null && texts.length > 0)
							{
								String text = texts[chatDef._chatIndex];
								if (text != null && !text.isEmpty())
								{
									L2Character target = chatDef._npcInstance;
									if (target instanceof L2PcInstance)
									{
										L2PcInstance player = (L2PcInstance) target;
										if (player.isOnline() == 1)
										{
											player.sendPacket(new CreatureSay(chatDef._npcInstance.getObjectId(), 0, chatDef._npcInstance.getName(), text));
										}
									}
									chatDef._chatIndex++;
									if (chatDef._chatIndex >= texts.length)
									{
										chatDef._chatIndex = 0;
									}
								}
							}
						}
					}
				}
				else
				{
					AutoChatInstance.AutoChatDefinition[] chatDefs = chatInst.getChatDefinitions();
					
					if (chatDefs != null && chatDefs.length > 0)
					{
						for (AutoChatInstance.AutoChatDefinition chatDef : chatDefs)
						{
							if (chatDef != null && chatDef.isActive())
							{
								if (chatDef.isRandomChat())
								{
									String[] texts = chatDef.getChatTexts();
									if (texts != null && texts.length > 0)
									{
										int randomIndex = Rnd.get(texts.length);
										String randomText = texts[randomIndex];
										if (randomText != null && !randomText.isEmpty())
										{
											L2Character target = chatDef._npcInstance;
											if (target instanceof L2PcInstance)
											{
												L2PcInstance player = (L2PcInstance) target;
												if (player.isOnline() == 1)
												{
													player.sendPacket(new CreatureSay(chatDef._npcInstance.getObjectId(), 0, chatDef._npcInstance.getName(), randomText));
												}
												
											}
										}
									}
								}
								else
								{
									String[] texts = chatDef.getChatTexts();
									if (texts != null && texts.length > 0)
									{
										String text = texts[chatDef._chatIndex];
										if (text != null && !text.isEmpty())
										{
											L2Character target = chatDef._npcInstance;
											if (target instanceof L2PcInstance)
											{
												L2PcInstance player = (L2PcInstance) target;
												if (player.isOnline() == 1)
												{
													player.sendPacket(new CreatureSay(chatDef._npcInstance.getObjectId(), 0, chatDef._npcInstance.getName(), text));
												}
											}
											chatDef._chatIndex++;
											if (chatDef._chatIndex >= texts.length)
											{
												chatDef._chatIndex = 0;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
