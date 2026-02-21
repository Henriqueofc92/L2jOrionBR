package l2jorion.game.model.spawn;

import java.util.ArrayList;
import java.util.List;

import l2jorion.util.random.Rnd;

public class SpawnTerritory
{
	private final String _name;
	private final List<int[]> _points = new ArrayList<>();
	private int _minZ = 32000;
	private int _maxZ = -32000;
	
	public SpawnTerritory(String name)
	{
		_name = name;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public void addPoint(int x, int y, int minz, int maxz)
	{
		_points.add(new int[]
		{
			x,
			y
		});
		if (minz < _minZ)
		{
			_minZ = minz;
		}
		if (maxz > _maxZ)
		{
			_maxZ = maxz;
		}
	}
	
	public int[] getRandomPoint()
	{
		if (_points.isEmpty())
		{
			return new int[]
			{
				0,
				0,
				0
			};
		}
		
		int minX = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;
		for (int[] p : _points)
		{
			if (p[0] < minX)
			{
				minX = p[0];
			}
			if (p[0] > maxX)
			{
				maxX = p[0];
			}
			if (p[1] < minY)
			{
				minY = p[1];
			}
			if (p[1] > maxY)
			{
				maxY = p[1];
			}
		}
		for (int i = 0; i < 100; i++)
		{
			int x = Rnd.get(minX, maxX);
			int y = Rnd.get(minY, maxY);
			
			if (isInside(x, y))
			{
				int z = Rnd.get(_minZ, _maxZ);
				return new int[]
				{
					x,
					y,
					z
				};
			}
		}
		
		return new int[]
		{
			_points.get(0)[0],
			_points.get(0)[1],
			_minZ
		};
	}
	
	public boolean isInside(int x, int y)
	{
		boolean inside = false;
		for (int i = 0, j = _points.size() - 1; i < _points.size(); j = i++)
		{
			if (((_points.get(i)[1] > y) != (_points.get(j)[1] > y)) && (x < (_points.get(j)[0] - _points.get(i)[0]) * (y - _points.get(i)[1]) / (double) (_points.get(j)[1] - _points.get(i)[1]) + _points.get(i)[0]))
			{
				inside = !inside;
			}
		}
		return inside;
	}
}