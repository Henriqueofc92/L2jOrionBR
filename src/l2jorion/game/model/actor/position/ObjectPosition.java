package l2jorion.game.model.actor.position;

import l2jorion.Config;
import l2jorion.game.model.L2Character;
import l2jorion.game.model.L2Object;
import l2jorion.game.model.L2World;
import l2jorion.game.model.L2WorldRegion;
import l2jorion.game.model.Location;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;

public class ObjectPosition
{
	
	private static final Logger LOG = LoggerFactory.getLogger(ObjectPosition.class);
	
	private final L2Object activeObject;
	private int heading = 0;
	private Location worldPosition;
	private L2WorldRegion worldRegion;
	private final Object lock = new Object();
	
	public ObjectPosition(L2Object activeObject)
	{
		this.activeObject = activeObject;
		setWorldRegion(L2World.getInstance().getRegion(getWorldPosition()));
	}
	
	public final void setXYZ(int x, int y, int z)
	{
		if (Config.ASSERT)
		{
			assert getWorldRegion() != null;
		}
		
		setWorldPosition(x, y, z);
		
		try
		{
			if (L2World.getInstance().getRegion(getWorldPosition()) != getWorldRegion())
			{
				updateWorldRegion();
			}
		}
		catch (Exception e)
		{
			handleInvalidCoords(e);
		}
	}
	
	public final void setXYZInvisible(int x, int y, int z)
	{
		if (Config.ASSERT)
		{
			assert getWorldRegion() == null;
		}
		
		x = clampCoordinate(x, L2World.MAP_MIN_X, L2World.MAP_MAX_X);
		y = clampCoordinate(y, L2World.MAP_MIN_Y, L2World.MAP_MAX_Y);
		
		setWorldPosition(x, y, z);
		getActiveObject().setIsVisible(false);
	}
	
	private int clampCoordinate(int value, int min, int max)
	{
		return Math.max(min, Math.min(max, value));
	}
	
	private void updateWorldRegion()
	{
		if (!getActiveObject().isVisible())
		{
			return;
		}
		
		L2WorldRegion newRegion = L2World.getInstance().getRegion(getWorldPosition());
		if (newRegion != getWorldRegion())
		{
			getWorldRegion().removeVisibleObject(getActiveObject());
			setWorldRegion(newRegion);
			getWorldRegion().addVisibleObject(getActiveObject());
		}
	}
	
	private void handleInvalidCoords(Exception e)
	{
		if (Config.ENABLE_ALL_EXCEPTIONS)
		{
			e.printStackTrace();
		}
		
		LOG.warn("Object Id at bad coords: (x: " + getX() + ", y: " + getY() + ", z: " + getZ() + ").");
		
		if (getActiveObject() instanceof L2PcInstance)
		{
			handleInvalidCoordsForPlayer();
		}
		else if (getActiveObject() instanceof L2Character)
		{
			getActiveObject().decayMe();
		}
	}
	
	private void handleInvalidCoordsForPlayer()
	{
		((L2PcInstance) getActiveObject()).teleToLocation(0, 0, 0, false);
		((L2PcInstance) getActiveObject()).sendMessage("Error with your coords. Please ask a GM for help!");
	}
	
	public L2Object getActiveObject()
	{
		return activeObject;
	}
	
	public int getHeading()
	{
		return heading;
	}
	
	public void setHeading(int value)
	{
		heading = value;
	}
	
	public int getX()
	{
		return getWorldPosition().getX();
	}
	
	public void setX(int value)
	{
		getWorldPosition().setX(value);
	}
	
	public int getY()
	{
		return getWorldPosition().getY();
	}
	
	public void setY(int value)
	{
		getWorldPosition().setY(value);
	}
	
	public int getZ()
	{
		return getWorldPosition().getZ();
	}
	
	public void setZ(int value)
	{
		getWorldPosition().setZ(value);
	}
	
	public Location getWorldPosition()
	{
		if (worldPosition == null)
		{
			worldPosition = new Location(0, 0, 0);
		}
		return worldPosition;
	}
	
	public void setWorldPosition(int x, int y, int z)
	{
		getWorldPosition().setXYZ(x, y, z);
	}
	
	public void setWorldPosition(Location newPosition)
	{
		setWorldPosition(newPosition.getX(), newPosition.getY(), newPosition.getZ());
	}
	
	public L2WorldRegion getWorldRegion()
	{
		synchronized (lock)
		{
			return worldRegion;
		}
	}
	
	public void setWorldRegion(L2WorldRegion value)
	{
		synchronized (lock)
		{
			worldRegion = value;
		}
	}
}
