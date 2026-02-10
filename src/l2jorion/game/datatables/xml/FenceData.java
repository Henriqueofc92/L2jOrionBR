package l2jorion.game.datatables.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import l2jorion.game.model.L2Object;
import l2jorion.game.model.actor.instance.L2FenceInstance;

public class FenceData
{
	private static List<L2FenceInstance> fences = new ArrayList<>();
	
	public static void addFence(L2FenceInstance fence)
	{
		fences.add(fence);
	}
	
	public static List<L2FenceInstance> getAllFences()
	{
		return new ArrayList<>(fences);
	}
	
	public static void removeFence(L2FenceInstance fence)
	{
		fences.remove(fence);
	}
	
	public static boolean canSeeTarget(L2Object source, int x, int y)
	{
		Collection<L2Object> objects = source.getKnownList().getKnownObjects().values();
		
		for (L2Object obj : objects)
		{
			if (obj instanceof L2FenceInstance fence && fence.isBetween(source.getX(), source.getY(), x, y))
			{
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean canSeeTarget(int x, int y, int tx, int ty)
	{
		return fences.stream().noneMatch(fence -> fence.isBetween(x, y, tx, ty));
	}
}
