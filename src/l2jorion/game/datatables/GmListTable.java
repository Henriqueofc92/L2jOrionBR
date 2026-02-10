package l2jorion.game.datatables;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.network.PacketServer;
import l2jorion.game.network.SystemMessageId;
import l2jorion.game.network.serverpackets.SystemMessage;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;

public class GmListTable
{
	protected static final Logger LOG = LoggerFactory.getLogger(GmListTable.class);
	
	private static GmListTable _instance;
	private final Map<L2PcInstance, Boolean> _gmList;
	
	public static GmListTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new GmListTable();
		}
		
		return _instance;
	}
	
	public static void reload()
	{
		_instance = null;
		getInstance();
	}
	
	private GmListTable()
	{
		_gmList = new ConcurrentHashMap<>();
	}
	
	public ArrayList<L2PcInstance> getAllGms(final boolean includeHidden)
	{
		final ArrayList<L2PcInstance> tmpGmList = new ArrayList<>();
		for (Map.Entry<L2PcInstance, Boolean> entry : _gmList.entrySet())
		{
			boolean isHidden = entry.getValue();
			
			if (includeHidden || !isHidden)
			{
				tmpGmList.add(entry.getKey());
			}
		}
		return tmpGmList;
	}
	
	public ArrayList<String> getAllGmNames(final boolean includeHidden)
	{
		final ArrayList<String> tmpGmList = new ArrayList<>();
		
		for (Map.Entry<L2PcInstance, Boolean> entry : _gmList.entrySet())
		{
			boolean isHidden = entry.getValue();
			String name = entry.getKey().getName();
			
			if (!isHidden)
			{
				tmpGmList.add(name);
			}
			else if (includeHidden)
			{
				tmpGmList.add(name + " (invis)");
			}
		}
		return tmpGmList;
	}
	
	public void addGm(final L2PcInstance player, final boolean hidden)
	{
		_gmList.put(player, hidden);
	}
	
	public void deleteGm(final L2PcInstance player)
	{
		_gmList.remove(player);
	}
	
	public void showGm(final L2PcInstance player)
	{
		if (_gmList.containsKey(player))
		{
			_gmList.put(player, false);
		}
	}
	
	public void hideGm(final L2PcInstance player)
	{
		if (_gmList.containsKey(player))
		{
			_gmList.put(player, true);
		}
	}
	
	public boolean isGmOnline(final boolean includeHidden)
	{
		for (final boolean isHidden : _gmList.values())
		{
			if (includeHidden || !isHidden)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public void sendListToPlayer(final L2PcInstance player)
	{
		if (isGmOnline(player.isGM()))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.GM_LIST);
			player.sendPacket(sm);
			
			for (final String name : getAllGmNames(player.isGM()))
			{
				final SystemMessage sm1 = new SystemMessage(SystemMessageId.GM_S1);
				sm1.addString(name);
				player.sendPacket(sm1);
			}
		}
		else
		{
			SystemMessage sm2 = new SystemMessage(SystemMessageId.NO_GM_PROVIDING_SERVICE_NOW);
			player.sendPacket(sm2);
		}
	}
	
	public static void broadcastToGMs(final PacketServer packet)
	{
		for (final L2PcInstance gm : getInstance().getAllGms(true))
		{
			gm.sendPacket(packet);
		}
	}
	
	public static void broadcastMessageToGMs(final String message)
	{
		for (final L2PcInstance gm : getInstance().getAllGms(true))
		{
			if (gm != null)
			{
				gm.sendPacket(SystemMessage.sendString(message));
			}
		}
	}
}