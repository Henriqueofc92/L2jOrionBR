package l2jorion.game.templates;

import l2jorion.game.datatables.sql.ItemTable;

public class PcTemplateItem
{
	private final L2Item _item;
	private final int _amount;
	private final boolean _equipped;
	
	public PcTemplateItem(int itemId, int amount, boolean equipped)
	{
		_item = ItemTable.getInstance().getTemplate(itemId);
		_amount = amount;
		_equipped = equipped;
	}
	
	public L2Item getItem()
	{
		return _item;
	}
	
	public int getAmount()
	{
		return _amount;
	}
	
	public boolean isEquipped()
	{
		return _equipped;
	}
}