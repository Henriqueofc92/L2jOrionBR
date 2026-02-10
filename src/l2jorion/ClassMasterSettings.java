package l2jorion;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class ClassMasterSettings
{
	private final Map<Integer, Map<Integer, Integer>> claimItems;
	private final Map<Integer, Map<Integer, Integer>> rewardItems;
	private final Map<Integer, Boolean> allowedClassChange;
	
	public ClassMasterSettings(final String configLine)
	{
		claimItems = new HashMap<>();
		rewardItems = new HashMap<>();
		allowedClassChange = new HashMap<>();
		
		if (configLine != null)
		{
			parseConfigLine(configLine.trim());
		}
	}
	
	private void parseConfigLine(final String configLine)
	{
		StringTokenizer st = new StringTokenizer(configLine, ";");
		
		while (st.hasMoreTokens())
		{
			int job = Integer.parseInt(st.nextToken());
			
			allowedClassChange.put(job, true);
			
			Map<Integer, Integer> items = new HashMap<>();
			
			if (st.hasMoreTokens())
			{
				StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
				
				while (st2.hasMoreTokens())
				{
					StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
					int itemId = Integer.parseInt(st3.nextToken());
					int quantity = Integer.parseInt(st3.nextToken());
					items.put(itemId, quantity);
				}
			}
			
			claimItems.put(job, items);
			items = new HashMap<>();
			
			if (st.hasMoreTokens())
			{
				StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
				
				while (st2.hasMoreTokens())
				{
					StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
					int itemId = Integer.parseInt(st3.nextToken());
					int quantity = Integer.parseInt(st3.nextToken());
					items.put(itemId, quantity);
				}
			}
			rewardItems.put(job, items);
		}
	}
	
	public boolean isAllowed(final int job)
	{
		return allowedClassChange != null && allowedClassChange.getOrDefault(job, false);
	}
	
	public Map<Integer, Integer> getRewardItems(final int job)
	{
		return rewardItems.getOrDefault(job, null);
	}
	
	public Map<Integer, Integer> getRequireItems(final int job)
	{
		return claimItems.getOrDefault(job, null);
	}
}
