package l2jorion.game.autofarm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import l2jorion.game.ai.CtrlEvent;
import l2jorion.game.ai.CtrlIntention;
import l2jorion.game.ai.NextAction;
import l2jorion.game.geo.GeoData;
import l2jorion.game.handler.IItemHandler;
import l2jorion.game.handler.ItemHandler;
import l2jorion.game.handler.voice.AutoFarm;
import l2jorion.game.model.L2Character;
import l2jorion.game.model.L2Object;
import l2jorion.game.model.L2ShortCut;
import l2jorion.game.model.L2Skill;
import l2jorion.game.model.L2Skill.SkillType;
import l2jorion.game.model.L2Summon;
import l2jorion.game.model.actor.instance.L2ChestInstance;
import l2jorion.game.model.actor.instance.L2ItemInstance;
import l2jorion.game.model.actor.instance.L2MonsterInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.entity.event.dungeon.DungeonManager;
import l2jorion.game.model.zone.ZoneId;
import l2jorion.util.random.Rnd;

public class AutofarmPlayerRoutine
{
	private final L2PcInstance player;
	private L2Character committedTarget = null;
	private String _originalTitle = null;
	private int _originalTitleColor = 0;
	private long _startTime = 0;
	
	private static final int HP_POTION_ID = 1539;
	private static final int MP_POTION_ID = 728;
	private static final int SWEEPER_SKILL_ID = 42;
	
	private static final List<Integer> SUMMON_SKILLS = Arrays.asList(4261, 4068, 4137, 4260, 4708, 4709, 4710, 4712, 5135, 5138, 5141, 5442, 5444, 6095, 6096, 6041, 6044);
	
	private long _targetAttackTime = 0;
	private int _lastTargetId = 0;
	private int _lastX = 0;
	private int _lastY = 0;
	private long _lastMoveCheckTime = 0;
	private static final long STUCK_TIMEOUT = 12000;
	private static final int STUCK_MOVE_INTERVAL = 3000;
	
	public AutofarmPlayerRoutine(L2PcInstance player)
	{
		this.player = Objects.requireNonNull(player);
	}
	
	public L2PcInstance getPlayer()
	{
		return player;
	}
	
	public void start()
	{
		_originalTitle = player.getTitle();
		_originalTitleColor = player.getAppearance().getTitleColor();
		player.setTitle("Auto Farm");
		player.getAppearance().setTitleColor(0x00FF00);
		_startTime = System.currentTimeMillis();
		
		player.sendMessage("Auto Farm started.");
		_lastMoveCheckTime = System.currentTimeMillis();
		player.broadcastUserInfo();
	}
	
	public long getStartTime()
	{
		return _startTime;
	}
	
	public void stop()
	{
		committedTarget = null;
		_startTime = 0;
		if (_originalTitle != null)
		{
			player.setTitle(_originalTitle);
			player.getAppearance().setTitleColor(_originalTitleColor);
			_originalTitle = null;
			_originalTitleColor = 0;
		}
		player.setAutoFarm(false);
		player.broadcastUserInfo();
	}
	
	public long getFarmDuration()
	{
		if (_startTime == 0)
		{
			return 0;
		}
		return System.currentTimeMillis() - _startTime;
	}
	
	public void executeRoutine()
	{
		if (player.isDead() || player.isTeleporting() || player.isOnline() == 0)
		{
			AutofarmManager.getInstance().stopFarm(player);
			return;
		}
		
		if (isInsideRestrictedZone())
		{
			player.sendMessage("Auto Farm stopped: Restricted Zone.");
			AutofarmManager.getInstance().stopFarm(player);
			return;
		}
		if (player.getPkKills() > 1 || player.getPvpFlag() > 0)
		{
			player.sendMessage("Auto Farm stopped: PVP/PK status detected.");
			AutofarmManager.getInstance().stopFarm(player);
			return;
		}
		
		if (player.isCastingNow())
		{
			L2Skill skill = player.getLastSkillCast();
			if (skill != null && skill.getSkillType() == SkillType.RECALL)
			{
				player.sendMessage("Auto Farm stopped: Escape detected.");
				AutofarmManager.getInstance().stopFarm(player);
				return;
			}
		}
		if (player.isMoving() && player.getAI().getIntention() == CtrlIntention.AI_INTENTION_MOVE_TO)
		{
			committedTarget = null;
			return;
		}
		
		if (player.getWeightPenalty() >= 3)
		{
			player.sendMessage("Auto Farm stopped: Weight limit exceeded.");
			AutofarmManager.getInstance().stopFarm(player);
			return;
		}
		
		if (player.isResting() && checkRestingState())
		{
			return;
		}
		
		if (player.isOutOfControl())
		{
			return;
		}
		
		if (player.isNoBuffProtected() && player.getAllEffects().length <= 8)
		{
			player.sendMessage("Auto Farm stopped: Not enough buffs.");
			AutofarmManager.getInstance().stopFarm(player);
			AutoFarm.showAutoFarm(player);
			return;
		}
		
		calculatePotions();
		
		if (player.isLooting() && pickUpLootNearby())
		{
			return;
		}
		
		checkTargetHealth();
		targetEligibleCreature();
		
		if (tryCastFromList(getLowLifeSpells(), AutofarmSpellType.LowLife))
		{
			return;
		}
		
		if (tryCastFromList(getChanceSpells(), AutofarmSpellType.Chance))
		{
			return;
		}
		
		if (player.isMageClass())
		{
			tryCastFromList(getAttackSpells(), AutofarmSpellType.Attack);
		}
		else
		{
			tryCastFromList(getAttackSpells(), AutofarmSpellType.Attack);
			attack();
		}
		
		checkSpoil();
	}
	
	private boolean isInsideRestrictedZone()
	{
		int instancePvP = DungeonManager.INSTANCE_PVP;
		int instanceSolo = DungeonManager.INSTANCE_SOLO;
		
		if (player.getInstanceId() == instancePvP || player.getInstanceId() == instanceSolo)
		{
			return player.isInsideZone(ZoneId.ZONE_WATER) || player.isInsideZone(ZoneId.ZONE_JAIL) || player.isInJail();
		}
		return player.isInsideZone(ZoneId.ZONE_PEACE) || player.isInsideZone(ZoneId.ZONE_WATER) || player.isInsideZone(ZoneId.ZONE_OLY) || player.isInsideZone(ZoneId.ZONE_NOSTORE) || player.isInsideZone(ZoneId.ZONE_SIEGE) || player.isInsideZone(ZoneId.ZONE_JAIL)
			|| player.isInsideZone(ZoneId.ZONE_MONSTERTRACK) || player.isInsideZone(ZoneId.ZONE_BOSS) || player.isInJail() || player.isInsideZone(ZoneId.ZONE_CASTLE);
	}
	
	private boolean checkRestingState()
	{
		if (getHpPct() < 20 || getMpPct() < 5)
		{
			List<L2MonsterInstance> attackers = getKnownMonsters(600, m -> m.getTarget() == player);
			if (attackers.isEmpty() && !player.isSitting())
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
				player.sitDown();
				return true;
			}
		}
		
		if (player.isSitting())
		{
			if (getHpPct() > 85 && getMpPct() > 85)
			{
				player.standUp();
				return false;
			}
			return true;
		}
		return false;
	}
	
	private boolean pickUpLootNearby()
	{
		List<L2MonsterInstance> attackers = getKnownMonsters(600, m -> m.getTarget() == player || (player.getPet() != null && m.getTarget() == player.getPet()));
		if (!attackers.isEmpty())
		{
			return false;
		}
		
		if (player.getAI().getIntention() == CtrlIntention.AI_INTENTION_PICK_UP)
		{
			return true;
		}
		
		L2ItemInstance closestItem = null;
		double minDistance = Double.MAX_VALUE;
		
		for (L2Object obj : player.getKnownList().getKnownObjects().values())
		{
			if (obj instanceof L2ItemInstance)
			{
				L2ItemInstance item = (L2ItemInstance) obj;
				double dist = player.getDistanceSq(item);
				if (dist < 500 * 500 && dist < minDistance)
				{
					if (GeoData.getInstance().canSeeTarget(player, item))
					{
						minDistance = dist;
						closestItem = item;
					}
				}
			}
		}
		
		if (closestItem != null)
		{
			player.abortAttack();
			player.abortCast();
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, closestItem);
			return true;
		}
		return false;
	}
	
	public void targetEligibleCreature()
	{
		if (player.getAI().getIntention() == CtrlIntention.AI_INTENTION_PICK_UP)
		{
			return;
		}
		
		if (committedTarget != null)
		{
			long currentTime = System.currentTimeMillis();
			
			if (currentTime - _lastMoveCheckTime > STUCK_MOVE_INTERVAL)
			{
				boolean isMoving = player.isMoving();
				if (player.getX() == _lastX && player.getY() == _lastY && isMoving)
				{
					committedTarget = null;
					player.setTarget(null);
					return;
				}
				_lastX = player.getX();
				_lastY = player.getY();
				_lastMoveCheckTime = currentTime;
			}
			
			if (committedTarget.getObjectId() != _lastTargetId)
			{
				_lastTargetId = committedTarget.getObjectId();
				_targetAttackTime = currentTime;
			}
			if (currentTime - _targetAttackTime > STUCK_TIMEOUT)
			{
				committedTarget = null;
				player.setTarget(null);
				return;
			}
			if (!committedTarget.isDead() && committedTarget.isVisible() && canSee(committedTarget))
			{
				return;
			}
		}
		
		selectNewTarget();
	}
	
	private void selectNewTarget()
	{
		List<L2MonsterInstance> targets = getKnownMonsters(player.getRadius(), m -> m != null && !m.isDead() && m.isVisible() && m.getCurrentHp() > 0 && !m.isRaid() && !m.isRaidMinion() && !(m instanceof L2ChestInstance) && canSee(m) && !player.ignoredMonsterContain(m.getNpcId()));
		
		if (targets.isEmpty())
		{
			return;
		}
		
		L2MonsterInstance targetToSet = null;
		double minDistance = Double.MAX_VALUE;
		for (L2MonsterInstance m : targets)
		{
			if (m.getTarget() == player || (player.getPet() != null && m.getTarget() == player.getPet()))
			{
				double dist = player.getDistanceSq(m);
				if (dist < minDistance)
				{
					minDistance = dist;
					targetToSet = m;
				}
			}
		}
		if (targetToSet == null)
		{
			for (L2MonsterInstance m : targets)
			{
				if (!player.isAntiKsProtected() || m.getTarget() == null || m.getTarget() == player)
				{
					double dist = player.getDistanceSq(m);
					if (dist < minDistance)
					{
						minDistance = dist;
						targetToSet = m;
					}
				}
			}
		}
		if (targetToSet != null)
		{
			committedTarget = targetToSet;
			player.setTarget(targetToSet);
			_targetAttackTime = System.currentTimeMillis();
			_lastTargetId = targetToSet.getObjectId();
		}
	}
	
	private void checkTargetHealth()
	{
		L2Object target = player.getTarget();
		if (target == null || !(target instanceof L2Character) || ((L2Character) target).isDead() || !target.isVisible())
		{
			committedTarget = null;
			if (target != null)
			{
				player.setTarget(null);
			}
		}
	}
	
	private void attack()
	{
		if (shortcutsContainAttack() && player.getTarget() instanceof L2MonsterInstance)
		{
			L2MonsterInstance target = (L2MonsterInstance) player.getTarget();
			if (target.isAutoAttackable(player) && target.isVisible() && canSee(target))
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				handleSummonAttack(target);
			}
		}
	}
	
	private void handleSummonAttack(L2MonsterInstance target)
	{
		if (player.isSummonAttack() && player.getPet() != null)
		{
			L2Summon summon = player.getPet();
			int npcId = summon.getNpcId();
			if ((npcId >= 14702 && npcId <= 14798) || (npcId >= 14839 && npcId <= 14869))
			{
				return;
			}
			
			summon.setTarget(target);
			summon.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			
			if (Rnd.get(100) < player.getSummonSkillPercent())
			{
				for (int id : SUMMON_SKILLS)
				{
					useMagicSkillBySummon(id, target);
				}
			}
		}
	}
	
	private boolean tryCastFromList(List<Integer> skillIds, AutofarmSpellType type)
	{
		L2Skill skill = nextAvailableSkill(skillIds, type);
		if (skill != null)
		{
			if (isSkillTargetVisible(skill))
			{
				useMagicSkill(skill, type == AutofarmSpellType.LowLife && skill.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF);
				return true;
			}
		}
		return false;
	}
	
	private boolean isSkillTargetVisible(L2Skill skill)
	{
		if (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF || skill.getTargetType() == L2Skill.SkillTargetType.TARGET_AURA)
		{
			return true;
		}
		
		L2Object target = player.getTarget();
		if (target != null)
		{
			return target.isVisible() && canSee(target);
		}
		return false;
	}
	
	private boolean canSee(L2Object target)
	{
		if (target == null)
		{
			return false;
		}
		return GeoData.getInstance().canSeeTarget(player, target);
	}
	
	private List<L2MonsterInstance> getKnownMonsters(int radius, Predicate<L2MonsterInstance> condition)
	{
		List<L2MonsterInstance> result = new ArrayList<>();
		for (L2Object obj : player.getKnownList().getKnownObjects().values())
		{
			if (obj instanceof L2MonsterInstance)
			{
				L2MonsterInstance monster = (L2MonsterInstance) obj;
				if (player.getDistanceSq(monster) <= radius * radius && condition.test(monster))
				{
					result.add(monster);
				}
			}
		}
		return result;
	}
	
	private void calculatePotions()
	{
		if (getHpPct() < player.getHpPotionPercentage())
		{
			forceUseItem(HP_POTION_ID);
		}
		if (getMpPct() < player.getMpPotionPercentage())
		{
			forceUseItem(MP_POTION_ID);
		}
	}
	
	private void forceUseItem(int itemId)
	{
		L2ItemInstance item = player.getInventory().getItemByItemId(itemId);
		if (item != null)
		{
			IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());
			if (handler != null)
			{
				handler.useItem(player, item);
			}
		}
	}
	
	private double getHpPct()
	{
		return player.getCurrentHp() * 100.0 / player.getMaxHp();
	}
	
	private double getMpPct()
	{
		return player.getCurrentMp() * 100.0 / player.getMaxMp();
	}
	
	private void checkSpoil()
	{
		if (canBeSweepedByMe())
		{
			L2Skill sweeper = player.getSkill(SWEEPER_SKILL_ID);
			if (sweeper != null && !player.isSkillDisabled(sweeper))
			{
				useMagicSkill(sweeper, false);
			}
		}
	}
	
	private boolean canBeSweepedByMe()
	{
		L2Object target = player.getTarget();
		if (target instanceof L2MonsterInstance)
		{
			L2MonsterInstance monster = (L2MonsterInstance) target;
			return monster.isDead() && monster.isSweepActive() && monster.getSpoilerId() == player.getObjectId();
		}
		return false;
	}
	
	private void useMagicSkill(L2Skill skill, boolean forceOnSelf)
	{
		if (player.isOutOfControl() || player.isCastingNow())
		{
			return;
		}
		
		if (player.isAttackingNow())
		{
			player.getAI().setNextAction(new NextAction(CtrlEvent.EVT_READY_TO_ACT, CtrlIntention.AI_INTENTION_CAST, () -> castSpell(skill, forceOnSelf)));
		}
		else
		{
			castSpell(skill, forceOnSelf);
		}
	}
	
	private void castSpell(L2Skill skill, boolean forceOnSelf)
	{
		if (forceOnSelf)
		{
			L2Object old = player.getTarget();
			player.setTarget(player);
			player.useMagic(skill, false, false);
			player.setTarget(old);
		}
		else
		{
			player.useMagic(skill, false, false);
		}
	}
	
	private boolean shortcutsContainAttack()
	{
		for (L2ShortCut sc : player.getAllShortCuts())
		{
			if (sc.getPage() == player.getPage() && sc.getType() == L2ShortCut.TYPE_ACTION && (sc.getId() == 2 || (player.isSummonAttack() && sc.getId() == 22)))
			{
				return true;
			}
		}
		return false;
	}
	
	public L2Skill nextAvailableSkill(List<Integer> skillIds, AutofarmSpellType spellType)
	{
		List<Integer> ids = new ArrayList<>(skillIds);
		if (spellType == AutofarmSpellType.Attack)
		{
			Collections.shuffle(ids);
		}
		
		for (int skillId : ids)
		{
			L2Skill skill = player.getSkill(skillId);
			if (skill == null || isSignet(skill))
			{
				continue;
			}
			
			if (player.isSkillDisabled(skill) || player.getCurrentMp() < skill.getMpConsume())
			{
				continue;
			}
			
			if (skill.getItemConsume() > 0)
			{
				if (player.getInventory().getItemByItemId(skill.getItemConsumeId()) == null || player.getInventory().getItemByItemId(skill.getItemConsumeId()).getCount() < skill.getItemConsume())
				{
					continue;
				}
			}
			
			if (!skill.getWeaponDependancy(player))
			{
				continue;
			}
			
			if (!skill.checkCondition(player, player.getTarget(), false))
			{
				continue;
			}
			
			L2Object target = player.getTarget();
			
			if (target instanceof L2MonsterInstance)
			{
				L2MonsterInstance monster = (L2MonsterInstance) target;
				
				if (spellType == AutofarmSpellType.Chance)
				{
					if (monster.getFirstEffect(skillId) != null)
					{
						continue;
					}
				}
				
				if (isSpoil(skillId))
				{
					if (monster.isSweepActive() || monster.isDead() || monster.getSpoilerId() > 0)
					{
						continue;
					}
				}
			}
			
			if (spellType == AutofarmSpellType.LowLife)
			{
				double hp = getHpPct();
				SkillType st = skill.getSkillType();
				
				if (st == SkillType.HEAL || st == SkillType.HOT || st == SkillType.HEAL_PERCENT || st == SkillType.BALANCE_LIFE)
				{
					if (hp > player.getHealPercent())
					{
						continue;
					}
				}
				if (skillId == 34 && hp > 10)
				{
					continue;
				}
				if ((skillId == 176 || skillId == 139) && hp > 30)
				{
					continue;
				}
			}
			return skill;
		}
		return null;
	}
	
	private static boolean isSpoil(int id)
	{
		return id == 254 || id == 302;
	}
	
	private static boolean isSignet(L2Skill s)
	{
		return s.getSkillType() == SkillType.SIGNET || s.getSkillType() == SkillType.SIGNET_CASTTIME;
	}
	
	private List<Integer> getSpellsInSlots(List<Integer> slots)
	{
		List<Integer> result = new ArrayList<>();
		for (L2ShortCut sc : player.getAllShortCuts())
		{
			if (sc.getPage() == player.getPage() && sc.getType() == L2ShortCut.TYPE_SKILL && slots.contains(sc.getSlot()))
			{
				result.add(sc.getId());
			}
		}
		return result;
	}
	
	private List<Integer> getAttackSpells()
	{
		return getSpellsInSlots(AutofarmConstants.attackSlots);
	}
	
	private List<Integer> getChanceSpells()
	{
		return getSpellsInSlots(AutofarmConstants.chanceSlots);
	}
	
	private List<Integer> getLowLifeSpells()
	{
		return getSpellsInSlots(AutofarmConstants.lowLifeSlots);
	}
	
	private void useMagicSkillBySummon(int skillId, L2Object target)
	{
		if (player.getPet() != null)
		{
			L2Summon summon = player.getPet();
			L2Skill skill = summon.getSkill(skillId);
			if (skill != null && !summon.isOutOfControl())
			{
				summon.setTarget(target);
				summon.useMagic(skill, false, false);
			}
		}
	}
}