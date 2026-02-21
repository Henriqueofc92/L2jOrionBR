/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2jorion.game.autofarm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.thread.ThreadPoolManager;

public class AutofarmManager
{
	private final Map<Integer, AutofarmPlayerRoutine> _activeAutofarms = new ConcurrentHashMap<>();
	private ScheduledFuture<?> _globalTask;
	
	protected AutofarmManager()
	{
		
		_globalTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this::onGlobalFarmTask, 1000, 333);
	}
	
	public void shutdown()
	{
		if (_globalTask != null)
		{
			_globalTask.cancel(false);
			_globalTask = null;
		}
		_activeAutofarms.clear();
	}
	
	public AutofarmPlayerRoutine getRoutine(int objectId)
	{
		return _activeAutofarms.get(objectId);
	}
	
	private void onGlobalFarmTask()
	{
		if (_activeAutofarms.isEmpty())
		{
			return;
		}
		
		for (AutofarmPlayerRoutine routine : _activeAutofarms.values())
		{
			if (routine.getPlayer() == null || routine.getPlayer().isOnline() == 0)
			{
				stopFarm(routine.getPlayer());
				continue;
			}
			
			try
			{
				routine.executeRoutine();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				stopFarm(routine.getPlayer());
			}
		}
	}
	
	public void startFarm(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		if (_activeAutofarms.containsKey(player.getObjectId()))
		{
			stopFarm(player);
		}
		
		AutofarmPlayerRoutine routine = new AutofarmPlayerRoutine(player);
		
		// Importante: Adicionar ao mapa ANTES de chamar start(),
		// para o loop global pegar ele
		_activeAutofarms.put(player.getObjectId(), routine);
		
		player.setAutoFarm(true);
		routine.start();
	}
	
	public void stopFarm(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		AutofarmPlayerRoutine routine = _activeAutofarms.remove(player.getObjectId());
		if (routine != null)
		{
			routine.stop();
		}
		
		player.setAutoFarm(false);
	}
	
	public boolean isAutofarming(L2PcInstance player)
	{
		return player != null && _activeAutofarms.containsKey(player.getObjectId());
	}
	
	public static AutofarmManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AutofarmManager _instance = new AutofarmManager();
	}
}