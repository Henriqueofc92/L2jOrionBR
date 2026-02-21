package l2jorion.game.data.custom;

import java.util.LinkedHashMap;
import java.util.Map;

import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.QuestState;

/**
 * Hero Items custom script (7000).<br>
 * Allows Heroes to select and obtain a hero weapon.
 */
public class HeroItems extends Quest
{
	private static final int[] MONUMENTS =
	{
		31690,
		31769,
		31770,
		31771,
		31772
	};
	
	private record HeroWeapon(String icon, String name, String description, String stats, String type)
	{
	}
	
	private static final Map<Integer, HeroWeapon> HERO_ITEMS = new LinkedHashMap<>();
	
	static
	{
		HERO_ITEMS.put(6611, new HeroWeapon("weapon_the_sword_of_hero_i00", "Infinity Blade", "During a critical attack, decreases one's P. Def and increases de-buff casting ability, damage shield effect, Max HP, Max MP, Max CP, and shield defense power. Also enhances damage to target during PvP.", "379/169", "Sword"));
		HERO_ITEMS.put(6612, new HeroWeapon("weapon_the_two_handed_sword_of_hero_i00", "Infinity Cleaver", "Increases Max HP, Max CP, critical power and critical chance. Inflicts extra damage when a critical attack occurs and has possibility of reflecting the skill back on the player. Also enhances damage to target during PvP.", "461/169", "Double Handed Sword"));
		HERO_ITEMS.put(6613, new HeroWeapon("weapon_the_axe_of_hero_i00", "Infinity Axe", "During a critical attack, it bestows one the ability to cause internal conflict to one's opponent. Damage shield function, Max HP, Max MP, Max CP as well as one's shield defense rate are increased. It also enhances damage to one's opponent during PvP.", "379/169", "Blunt"));
		HERO_ITEMS.put(6614, new HeroWeapon("weapon_the_mace_of_hero_i00", "Infinity Rod", "When good magic is casted upon a target, increases MaxMP, MaxCP, Casting Spd, and MP regeneration rate. Also recovers HP 100% and enhances damage to target during PvP.", "303/226", "Blunt"));
		HERO_ITEMS.put(6615, new HeroWeapon("weapon_the_hammer_of_hero_i00", "Infinity Crusher", "Increases MaxHP, MaxCP, and Atk. Spd. Stuns a target when a critical attack occurs and has possibility of reflecting the skill back on the player. Also enhances damage to target during PvP.", "461/169", "Blunt"));
		HERO_ITEMS.put(6616, new HeroWeapon("weapon_the_staff_of_hero_i00", "Infinity Scepter", "When casting good magic, it can recover HP by 100% at a certain rate, increases MAX MP, MaxCP, M. Atk., lower MP Consumption, increases the Magic Critical rate, and reduce the Magic Cancel. Enhances damage to target during PvP.", "369/226", "Blunt"));
		HERO_ITEMS.put(6617, new HeroWeapon("weapon_the_dagger_of_hero_i00", "Infinity Stinger", "Increases MaxMP, MaxCP, Atk. Spd., MP regen rate, and the success rate of Mortal and Deadly Blow from the back of the target. Silences the target when a critical attack occurs and has Vampiric Rage effect. Also enhances damage to target during PvP.", "332/169", "Dagger"));
		HERO_ITEMS.put(6618, new HeroWeapon("weapon_the_fist_of_hero_i00", "Infinity Fang", "Increases MaxHP, MaxMP, MaxCP and evasion. Stuns a target when a critical attack occurs and has possibility of reflecting the skill back on the player at a certain probability rate. Also enhances damage to target during PvP.", "461/169", "Dual Fist"));
		HERO_ITEMS.put(6619, new HeroWeapon("weapon_the_bow_of_hero_i00", "Infinity Bow", "Increases MaxMP/MaxCP and decreases re-use delay of a bow. Slows target when a critical attack occurs and has Cheap Shot effect. Also enhances damage to target during PvP.", "707/169", "Bow"));
		HERO_ITEMS.put(6620, new HeroWeapon("weapon_the_dualsword_of_hero_i00", "Infinity Wing", "When a critical attack occurs, increases MaxHP, MaxMP, MaxCP and critical chance. Silences the target and has possibility of reflecting the skill back on the target. Also enhances damage to target during PvP.", "461/169", "Dual Sword"));
		HERO_ITEMS.put(6621, new HeroWeapon("weapon_the_pole_of_hero_i00", "Infinity Spear", "During a critical attack, increases MaxHP, Max CP, Atk. Spd. and Accuracy. Casts dispel on a target and has possibility of reflecting the skill back on the target. Also enhances damage to target during PvP.", "379/169", "Pole"));
	}
	
	public HeroItems()
	{
		super(7000, "7000_HeroItems", "Hero Items");
		
		for (var npcId : MONUMENTS)
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}
	}
	
	private String renderList(String mode, int item)
	{
		var html = new StringBuilder("<html><body><font color=\"LEVEL\">List of Hero Items:</font><table border=0 width=300>");
		
		if ("list".equals(mode))
		{
			for (var entry : HERO_ITEMS.entrySet())
			{
				var id = entry.getKey();
				var w = entry.getValue();
				html.append("<tr><td width=35 height=45><img src=icon.").append(w.icon()).append(" width=32 height=32 align=left></td><td valign=top><a action=\"bypass -h Quest 7000_HeroItems ").append(id).append("\"><font color=\"FFFFFF\">").append(w.name()).append("</font></a></td></tr>");
			}
		}
		else
		{
			var w = HERO_ITEMS.get(item);
			if (w == null)
			{
				return "";
			}
			
			html.append("<tr><td align=left><font color=\"LEVEL\">Item Information</font></td><td align=right>").append("<button value=Back action=\"bypass -h Quest 7000_HeroItems buy\" width=80 height=27 back=L2UI_ch3.Btn1_normalOn fore=L2UI_ch3.Btn1_normal>").append("</td><td width=5><br></td></tr></table><table border=0 bgcolor=\"000000\" width=500 height=160><tr><td valign=top>").append("<table border=0><tr><td valign=top width=35><img src=icon.").append(w.icon()).append(" width=32 height=32 align=left></td><td valign=top width=400><table border=0 width=100%><tr><td><font color=\"FFFFFF\">").append(w.name()).append("</font></td></tr></table></td></tr></table><br><font color=\"LEVEL\">Item info:</font>").append("<table border=0 bgcolor=\"000000\" width=290 height=220><tr><td valign=top><font color=\"B09878\">").append(w.description()).append("</font></td></tr><tr><td><br>Type: ").append(w.type()).append("<br><br>Patk/Matk: ").append(w.stats()).append("<br><br>").append("<table border=0 width=300><tr><td align=center><button value=Obtain action=\"bypass -h Quest 7000_HeroItems _").append(item).append("\" width=80 height=27 back=L2UI_ch3.Btn1_normalOn fore=L2UI_ch3.Btn1_normal></td></tr></table></td></tr></table></td></tr>");
		}
		
		html.append("</table></body></html>");
		return html.toString();
	}
	
	@Override
	public String onEvent(String event, QuestState st)
	{
		if (!st.getPlayer().isHero())
		{
			return null;
		}
		
		if ("buy".equals(event))
		{
			return renderList("list", 0);
		}
		else if (event.matches("\\d+"))
		{
			var itemId = Integer.parseInt(event);
			if (HERO_ITEMS.containsKey(itemId))
			{
				return renderList("item", itemId);
			}
		}
		else if (event.startsWith("_"))
		{
			var itemId = Integer.parseInt(event.substring(1));
			// Check if hero already has any hero weapon
			for (int i = 6611; i <= 6621; i++)
			{
				if (st.getQuestItemsCount(i) > 0)
				{
					st.exitQuest(true);
					return "You already have an " + HERO_ITEMS.get(i).name();
				}
			}
			st.giveItems(itemId, 1);
			var htmltext = "Enjoy your " + HERO_ITEMS.get(itemId).name();
			st.playSound("ItemSound.quest_fanfare_2");
			st.exitQuest(true);
			return htmltext;
		}
		
		return null;
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		var st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		if (player.isHero())
		{
			return renderList("list", 0);
		}
		var html = "<html><body>Monument of Heroes:<br>You do not meet the requirements. You must become a Hero first!<br><a action=\"bypass -h npc_" + npc.getObjectId() + "_Chat 0\">Back</a></body></html>";
		st.exitQuest(true);
		return html;
	}


	public static void main(String[] args)
	{
		new HeroItems();
	}
}
