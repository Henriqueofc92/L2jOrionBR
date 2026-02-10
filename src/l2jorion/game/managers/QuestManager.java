/* L2jOrion Project - www.l2jorion.com 
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package l2jorion.game.managers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import l2jorion.Config;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.scripting.L2ScriptEngineManager;
import l2jorion.game.scripting.ScriptManager;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;

public class QuestManager extends ScriptManager<Quest>
{
	private static final Logger LOG = LoggerFactory.getLogger(QuestManager.class);
	private final Map<String, Quest> quests = new HashMap<>();
	private static QuestManager instance;
	
	public static QuestManager getInstance()
	{
		if (instance == null)
		{
			instance = new QuestManager();
		}
		return instance;
	}
	
	private QuestManager()
	{
	}
	
	public final boolean reload(final String questFolder)
	{
		final Quest q = getQuest(questFolder);
		return q != null && q.reload();
	}
	
	public final boolean reload(final int questId)
	{
		final Quest q = getQuest(questId);
		return q != null && q.reload();
	}
	
	public final void reloadAllQuests() throws IOException
	{
		LOG.info("Reloading Server Scripts");
		
		// Unload all scripts
		for (final Quest quest : quests.values())
		{
			if (quest != null)
			{
				quest.unload();
			}
		}
		
		// Now load all scripts
		final File scripts = new File(Config.DATAPACK_ROOT, "data/scripts/scripts.cfg");
		L2ScriptEngineManager.getInstance().executeScriptList(scripts);
		QuestManager.getInstance().report();
	}
	
	public final void report()
	{
		LOG.info("QuestManager: Loaded: " + quests.size() + " quests");
	}
	
	public final void save()
	{
		for (final Quest q : getQuests().values())
		{
			q.saveGlobalData();
		}
	}
	
	public final Quest getQuest(String name)
	{
		return getQuests().get(name);
	}
	
	public final Quest getQuest(final int questId)
	{
		for (final Quest q : getQuests().values())
		{
			if (q.getQuestIntId() == questId)
			{
				return q;
			}
		}
		return null;
	}
	
	public final void addQuest(Quest newQuest)
	{
		if (newQuest == null)
		{
			throw new IllegalArgumentException("Quest argument cannot be null");
		}
		Quest old = quests.get(newQuest.getName());
		
		if (old != null && old.isRealQuest())
		{
			old.unload();
			LOG.info("Replaced: (" + old.getName() + ") with a new version (" + newQuest.getName() + ")");
		}
		
		quests.put(newQuest.getName(), newQuest);
	}
	
	public final Map<String, Quest> getQuests()
	{
		return quests;
	}
	
	public static void reload()
	{
		instance = new QuestManager();
	}
	
	@Override
	public Iterable<Quest> getAllManagedScripts()
	{
		return quests.values();
	}
	
	@Override
	public boolean unload(final Quest ms)
	{
		ms.saveGlobalData();
		return removeQuest(ms);
	}
	
	@Override
	public String getScriptManagerName()
	{
		return "QuestManager";
	}
	
	public final boolean removeQuest(final Quest q)
	{
		return quests.remove(q.getName()) != null;
	}
	
	public final void unloadAllQuests()
	{
		LOG.info("Unloading Server Quests");
		
		for (final Quest quest : quests.values())
		{
			if (quest != null)
			{
				quest.unload();
			}
		}
		
		QuestManager.getInstance().report();
	}
}
