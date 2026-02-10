package l2jorion.game.model.actor.instance;

import l2jorion.game.model.L2Character;
import l2jorion.game.model.L2Object;
import l2jorion.game.network.L2GameClient;
import l2jorion.game.network.serverpackets.ActionFailed;
import l2jorion.game.network.serverpackets.ExColosseumFenceInfoPacket;

public final class L2FenceInstance extends L2Object
{
	private int _type;
	private int _width;
	private int _length;
	private int xMin;
	private int xMax;
	private int yMin;
	private int yMax;
	
	public L2FenceInstance(int objectId, int type, int width, int length, int x, int y)
	{
		super(objectId);
		_type = type;
		_width = width;
		_length = length;
		xMin = x - width / 2;
		xMax = x + width / 2;
		yMin = y - length / 2;
		yMax = y + length / 2;
	}
	
	public boolean isBetween(int x, int y, int tx, int ty)
	{
		if (x < xMin && tx < xMin || x > xMax && tx > xMax || y < yMin && ty < yMin || y > yMax && ty > yMax || (x > xMin && tx > xMin && x < xMax && tx < xMax && y > yMin && ty > yMin && y < yMax && ty < yMax))
		{
			return false;
		}
		
		return !crossLinePart(xMin, yMin, xMax, yMin, x, y, tx, ty) && !crossLinePart(xMax, yMin, xMax, yMax, x, y, tx, ty) && !crossLinePart(xMax, yMax, xMin, yMax, x, y, tx, ty) && !crossLinePart(xMin, yMax, xMin, yMin, x, y, tx, ty);
	}
	
	private boolean crossLinePart(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4)
	{
		float[] result = intersection(x1, y1, x2, y2, x3, y3, x4, y4);
		
		return result != null && (result[0] <= xMax && result[0] >= xMin || result[1] <= yMax && result[1] >= yMin);
	}
	
	private static float[] intersection(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4)
	{
		float d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
		
		if (d == 0)
		{
			return null;
		}
		
		float xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
		float yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;
		
		return new float[]
		{
			xi,
			yi
		};
	}
	
	public int getType()
	{
		return _type;
	}
	
	public int getWidth()
	{
		return _width;
	}
	
	public int getLength()
	{
		return _length;
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}
	
	@Override
	public void onActionShift(L2GameClient client)
	{
		L2PcInstance player = client.getActiveChar();
		if (player != null && player.isGM())
		{
			player.setTarget(this);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		activeChar.sendPacket(new ExColosseumFenceInfoPacket(this));
	}
}
