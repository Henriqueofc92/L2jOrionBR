package l2jorion.game.data.ai.group_template;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import l2jorion.game.ai.CtrlIntention;
import l2jorion.game.datatables.sql.NpcTable;
import l2jorion.game.enums.AchType;
import l2jorion.game.idfactory.IdFactory;
import l2jorion.game.model.L2Attackable;
import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.actor.instance.L2TamedBeastInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.model.quest.QuestState;
import l2jorion.game.model.quest.State;
import l2jorion.game.network.serverpackets.CreatureSay;
import l2jorion.game.network.serverpackets.SocialAction;
import l2jorion.game.templates.L2NpcTemplate;
import l2jorion.util.random.Rnd;

public class FeedableBeasts extends Quest
{
	private static final int GOLDEN_SPICE = 6643;
	private static final int CRYSTAL_SPICE = 6644;
	private static final int SKILL_GOLDEN_SPICE = 2188;
	private static final int SKILL_CRYSTAL_SPICE = 2189;
	private static final Map<Integer, Integer> FOOD_SKILL = Map.of(GOLDEN_SPICE, SKILL_GOLDEN_SPICE, CRYSTAL_SPICE, SKILL_CRYSTAL_SPICE);
	
	private final Set<Integer> tamedBeasts = Set.of(16013, 16014, 16015, 16016, 16017, 16018);
	private final Set<Integer> feedableBeasts = new HashSet<>();
	
	// growthCapableMobs: npcId -> [growthLevel, {food -> [list of possible next npc ids]}, chanceOfGrowth]
	// For level 2, the lists are nested: [[normalIds],[tamedIds]]
	private record GrowthData(int level, Map<Integer, int[][]> foodData, int chance)
	{
	}
	
	private final Map<Integer, GrowthData> growthCapableMobs = new HashMap<>();
	
	private final Map<Integer, Integer> madCowPolymorph = Map.of(21824, 21468, 21825, 21469, 21826, 21487, 21827, 21488, 21828, 21506, 21829, 21507);
	
	private final String[][] text =
	{
		{
			"What did you just do to me?",
			"You want to tame me, huh?",
			"Do not give me this. Perhaps you will be in danger.",
			"Bah bah. What is this unpalatable thing?",
			"My belly has been complaining. This hit the spot.",
			"What is this? Can I eat it?",
			"You don't need to worry about me.",
			"Delicious food, thanks.",
			"I am starting to like you!",
			"Gulp"
		},
		{
			"I do not think you have given up on the idea of taming me.",
			"That is just food to me. Perhaps I can eat your hand too.",
			"Will eating this make me fat? Ha ha",
			"Why do you always feed me?",
			"Do not trust me. I may betray you"
		},
		{
			"Destroy",
			"Look what you have done!",
			"Strange feeling...! Evil intentions grow in my heart...!",
			"It is happenning!",
			"This is sad...Good is sad...!"
		}
	};
	
	private final Map<Integer, Integer> feedInfo = new HashMap<>();
	
	public FeedableBeasts()
	{
		super(-1, "FeedableBeasts", "ai");
		new State("Start", this);
		
		// Build feedableBeasts set
		IntStream.rangeClosed(21451, 21507).forEach(feedableBeasts::add);
		IntStream.rangeClosed(21824, 21829).forEach(feedableBeasts::add);
		feedableBeasts.addAll(tamedBeasts);
		
		initGrowthData();
		
		for (int id : feedableBeasts)
		{
			addSkillUseId(id);
			addKillId(id);
		}
	}
	
	private void initGrowthData()
	{
		// Alpen Kookabura
		addGrowth(21451, 0, new int[]
		{
			21452,
			21453,
			21454,
			21455
		}, new int[]
		{
			21456,
			21457,
			21458,
			21459
		}, 100);
		addGrowth(21452, 1, new int[]
		{
			21460,
			21462
		}, new int[] {}, 40);
		addGrowth(21453, 1, new int[]
		{
			21461,
			21463
		}, new int[] {}, 40);
		addGrowth(21454, 1, new int[]
		{
			21460,
			21462
		}, new int[] {}, 40);
		addGrowth(21455, 1, new int[]
		{
			21461,
			21463
		}, new int[] {}, 40);
		addGrowth(21456, 1, new int[] {}, new int[]
		{
			21464,
			21466
		}, 40);
		addGrowth(21457, 1, new int[] {}, new int[]
		{
			21465,
			21467
		}, 40);
		addGrowth(21458, 1, new int[] {}, new int[]
		{
			21464,
			21466
		}, 40);
		addGrowth(21459, 1, new int[] {}, new int[]
		{
			21465,
			21467
		}, 40);
		addGrowthL2(21460, new int[]
		{
			21468,
			21824
		}, new int[]
		{
			16017,
			16018
		}, true, 25);
		addGrowthL2(21461, new int[]
		{
			21469,
			21825
		}, new int[]
		{
			16017,
			16018
		}, true, 25);
		addGrowthL2(21462, new int[]
		{
			21468,
			21824
		}, new int[]
		{
			16017,
			16018
		}, true, 25);
		addGrowthL2(21463, new int[]
		{
			21469,
			21825
		}, new int[]
		{
			16017,
			16018
		}, true, 25);
		addGrowthL2(21464, new int[]
		{
			21468,
			21824
		}, new int[]
		{
			16017,
			16018
		}, false, 25);
		addGrowthL2(21465, new int[]
		{
			21469,
			21825
		}, new int[]
		{
			16017,
			16018
		}, false, 25);
		addGrowthL2(21466, new int[]
		{
			21468,
			21824
		}, new int[]
		{
			16017,
			16018
		}, false, 25);
		addGrowthL2(21467, new int[]
		{
			21469,
			21825
		}, new int[]
		{
			16017,
			16018
		}, false, 25);
		
		// Alpen Buffalo
		addGrowth(21470, 0, new int[]
		{
			21471,
			21472,
			21473,
			21474
		}, new int[]
		{
			21475,
			21476,
			21477,
			21478
		}, 100);
		addGrowth(21471, 1, new int[]
		{
			21479,
			21481
		}, new int[] {}, 40);
		addGrowth(21472, 1, new int[]
		{
			21481,
			21482
		}, new int[] {}, 40);
		addGrowth(21473, 1, new int[]
		{
			21479,
			21481
		}, new int[] {}, 40);
		addGrowth(21474, 1, new int[]
		{
			21480,
			21482
		}, new int[] {}, 40);
		addGrowth(21475, 1, new int[] {}, new int[]
		{
			21483,
			21485
		}, 40);
		addGrowth(21476, 1, new int[] {}, new int[]
		{
			21484,
			21486
		}, 40);
		addGrowth(21477, 1, new int[] {}, new int[]
		{
			21483,
			21485
		}, 40);
		addGrowth(21478, 1, new int[] {}, new int[]
		{
			21484,
			21486
		}, 40);
		addGrowthL2(21479, new int[]
		{
			21487,
			21826
		}, new int[]
		{
			16013,
			16014
		}, true, 25);
		addGrowthL2(21480, new int[]
		{
			21488,
			21827
		}, new int[]
		{
			16013,
			16014
		}, true, 25);
		addGrowthL2(21481, new int[]
		{
			21487,
			21826
		}, new int[]
		{
			16013,
			16014
		}, true, 25);
		addGrowthL2(21482, new int[]
		{
			21488,
			21827
		}, new int[]
		{
			16013,
			16014
		}, true, 25);
		addGrowthL2(21483, new int[]
		{
			21487,
			21826
		}, new int[]
		{
			16013,
			16014
		}, false, 25);
		addGrowthL2(21484, new int[]
		{
			21488,
			21827
		}, new int[]
		{
			16013,
			16014
		}, false, 25);
		addGrowthL2(21485, new int[]
		{
			21487,
			21826
		}, new int[]
		{
			16013,
			16014
		}, false, 25);
		addGrowthL2(21486, new int[]
		{
			21488,
			21827
		}, new int[]
		{
			16013,
			16014
		}, false, 25);
		
		// Alpen Cougar
		addGrowth(21489, 0, new int[]
		{
			21490,
			21491,
			21492,
			21493
		}, new int[]
		{
			21494,
			21495,
			21496,
			21497
		}, 100);
		addGrowth(21490, 1, new int[]
		{
			21498,
			21500
		}, new int[] {}, 40);
		addGrowth(21491, 1, new int[]
		{
			21499,
			21501
		}, new int[] {}, 40);
		addGrowth(21492, 1, new int[]
		{
			21498,
			21500
		}, new int[] {}, 40);
		addGrowth(21493, 1, new int[]
		{
			21499,
			21501
		}, new int[] {}, 40);
		addGrowth(21494, 1, new int[] {}, new int[]
		{
			21502,
			21504
		}, 40);
		addGrowth(21495, 1, new int[] {}, new int[]
		{
			21503,
			21505
		}, 40);
		addGrowth(21496, 1, new int[] {}, new int[]
		{
			21502,
			21504
		}, 40);
		addGrowth(21497, 1, new int[] {}, new int[]
		{
			21503,
			21505
		}, 40);
		addGrowthL2(21498, new int[]
		{
			21506,
			21828
		}, new int[]
		{
			16015,
			16016
		}, true, 25);
		addGrowthL2(21499, new int[]
		{
			21507,
			21829
		}, new int[]
		{
			16015,
			16016
		}, true, 25);
		addGrowthL2(21500, new int[]
		{
			21506,
			21828
		}, new int[]
		{
			16015,
			16016
		}, true, 25);
		addGrowthL2(21501, new int[]
		{
			21507,
			21829
		}, new int[]
		{
			16015,
			16016
		}, true, 25);
		addGrowthL2(21502, new int[]
		{
			21506,
			21828
		}, new int[]
		{
			16015,
			16016
		}, false, 25);
		addGrowthL2(21503, new int[]
		{
			21507,
			21829
		}, new int[]
		{
			16015,
			16016
		}, false, 25);
		addGrowthL2(21504, new int[]
		{
			21506,
			21828
		}, new int[]
		{
			16015,
			16016
		}, false, 25);
		addGrowthL2(21505, new int[]
		{
			21507,
			21829
		}, new int[]
		{
			16015,
			16016
		}, false, 25);
	}
	
	private void addGrowth(int npcId, int level, int[] golden, int[] crystal, int chance)
	{
		var foodData = new HashMap<Integer, int[][]>();
		foodData.put(GOLDEN_SPICE, new int[][]
		{
			golden
		});
		foodData.put(CRYSTAL_SPICE, new int[][]
		{
			crystal
		});
		growthCapableMobs.put(npcId, new GrowthData(level, foodData, chance));
	}
	
	private void addGrowthL2(int npcId, int[] normalIds, int[] tamedIds, boolean isGolden, int chance)
	{
		var foodData = new HashMap<Integer, int[][]>();
		if (isGolden)
		{
			foodData.put(GOLDEN_SPICE, new int[][]
			{
				normalIds,
				tamedIds
			});
			foodData.put(CRYSTAL_SPICE, new int[][] {});
		}
		else
		{
			foodData.put(GOLDEN_SPICE, new int[][] {});
			foodData.put(CRYSTAL_SPICE, new int[][]
			{
				normalIds,
				tamedIds
			});
		}
		growthCapableMobs.put(npcId, new GrowthData(2, foodData, chance));
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		if ("polymorph Mad Cow".equals(event) && npc != null && player != null)
		{
			if (madCowPolymorph.containsKey(npc.getNpcId()))
			{
				if (feedInfo.getOrDefault(npc.getObjectId(), -1) == player.getObjectId())
				{
					feedInfo.remove(npc.getObjectId());
				}
				npc.deleteMe();
				var nextNpc = addSpawn(madCowPolymorph.get(npc.getNpcId()), npc);
				feedInfo.put(nextNpc.getObjectId(), player.getObjectId());
				nextNpc.setRunning();
				((L2Attackable) nextNpc).addDamageHate(player, 0, 99999);
				nextNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			}
		}
		return null;
	}
	
	private void spawnNext(L2NpcInstance npc, int growthLevel, L2PcInstance player, int food)
	{
		int npcId = npc.getNpcId();
		var data = growthCapableMobs.get(npcId);
		if (data == null)
		{
			return;
		}
		
		int[][] foodArr = data.foodData().getOrDefault(food, new int[][] {});
		if (foodArr.length == 0)
		{
			return;
		}
		
		int nextNpcId;
		if (growthLevel == 2 && foodArr.length >= 2)
		{
			int rand = Rnd.get(2);
			if (rand == 1)
			{
				// tamed: depends on class type (fighter/mage)
				nextNpcId = player.getClassId().isMage() ? foodArr[1][1] : foodArr[1][0];
			}
			else
			{
				// not tamed: small chance of "mad cow"
				nextNpcId = Rnd.get(5) == 0 ? foodArr[0][1] : foodArr[0][0];
			}
		}
		else
		{
			nextNpcId = foodArr[0][Rnd.get(foodArr[0].length)];
		}
		
		// Remove feed info
		if (feedInfo.getOrDefault(npc.getObjectId(), -1) == player.getObjectId())
		{
			feedInfo.remove(npc.getObjectId());
		}
		
		// Despawn old mob
		if (data.level() == 0)
		{
			npc.onDecay();
		}
		else
		{
			npc.deleteMe();
		}
		
		// If tamed beast
		if (tamedBeasts.contains(nextNpcId))
		{
			var oldTrained = player.getTrainedBeast();
			if (oldTrained != null)
			{
				oldTrained.doDespawn();
			}
			
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(nextNpcId);
			var nextNpc = new L2TamedBeastInstance(IdFactory.getInstance().getNextId(), template, player, FOOD_SKILL.getOrDefault(food, 0), npc.getX(), npc.getY(), npc.getZ());
			nextNpc.setRunning();
			int objectId = nextNpc.getObjectId();
			
			QuestState st = player.getQuestState("20_BringUpWithLove");
			if (st != null && Rnd.get(100) <= 5 && st.getQuestItemsCount(7185) == 0)
			{
				st.giveItems(7185, 1);
				st.set("cond", "2");
			}
			
			// Rare random chat
			int rand = Rnd.get(20);
			String chatMsg = switch (rand)
			{
				case 0 -> player.getName() + ", will you show me your hideaway?";
				case 1 -> player.getName() + ", whenever I look at spice, I think about you.";
				case 2 -> player.getName() + ", you do not need to return to the village. I will give you strength";
				case 3 -> "Thanks, " + player.getName() + ". I hope I can help you";
				case 4 -> player.getName() + ", what can I do to help you?";
				default -> null;
			};
			if (chatMsg != null)
			{
				npc.broadcastPacket(new CreatureSay(objectId, 0, nextNpc.getName(), chatMsg));
			}
		}
		else
		{
			var nextNpc = addSpawn(nextNpcId, npc);
			if (madCowPolymorph.containsKey(nextNpcId))
			{
				startQuestTimer("polymorph Mad Cow", 10000, nextNpc, player);
			}
			feedInfo.put(nextNpc.getObjectId(), player.getObjectId());
			nextNpc.setRunning();
			((L2Attackable) nextNpc).addDamageHate(player, 0, 99999);
			nextNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		}
	}
	
	@Override
	public String onSkillUse(L2NpcInstance npc, L2PcInstance player, l2jorion.game.model.L2Skill skill)
	{
		int npcId = npc.getNpcId();
		int skillId = skill.getId();
		if (!feedableBeasts.contains(npcId))
		{
			return null;
		}
		if (skillId != SKILL_GOLDEN_SPICE && skillId != SKILL_CRYSTAL_SPICE)
		{
			return null;
		}
		
		int objectId = npc.getObjectId();
		int growthLevel = growthCapableMobs.containsKey(npcId) ? growthCapableMobs.get(npcId).level() : 3;
		
		// Prevent exploit: lock to first feeder at level 0
		if (growthLevel == 0 && feedInfo.containsKey(objectId))
		{
			return null;
		}
		feedInfo.put(objectId, player.getObjectId());
		
		int food = (skillId == SKILL_GOLDEN_SPICE) ? GOLDEN_SPICE : CRYSTAL_SPICE;
		
		// Social action: beast eating food
		npc.broadcastPacket(new SocialAction(objectId, 2));
		
		if (growthCapableMobs.containsKey(npcId))
		{
			var data = growthCapableMobs.get(npcId);
			int[][] foodArr = data.foodData().getOrDefault(food, new int[][] {});
			if (foodArr.length == 0 || foodArr[0].length == 0)
			{
				return null;
			}
			
			// Rare random talk
			if (Rnd.get(20) == 0)
			{
				String[] msgs = text[growthLevel];
				npc.broadcastPacket(new CreatureSay(objectId, 0, npc.getName(), msgs[Rnd.get(msgs.length)]));
			}
			
			if (growthLevel > 0 && feedInfo.getOrDefault(objectId, -1) != player.getObjectId())
			{
				return null;
			}
			
			// Polymorph
			if (Rnd.get(100) < data.chance())
			{
				spawnNext(npc, growthLevel, player, food);
				player.getAchievement().increase(AchType.FEED_BEAST);
			}
		}
		else if (tamedBeasts.contains(npcId))
		{
			final L2TamedBeastInstance tamed = (L2TamedBeastInstance) npc;
			if (skillId == tamed.getFoodType())
			{
				tamed.onReceiveFood();
				String[] mytext =
				{
					"Refills! Yeah!",
					"I am such a gluttonous beast, it is embarrassing! Ha ha",
					"Your cooperative feeling has been getting better and better.",
					"I will help you!",
					"The weather is really good. Wanna go for a picnic?",
					"I really like you! This is tasty...",
					"If you do not have to leave this place, then I can help you.",
					"What can I help you with?",
					"I am not here only for food!",
					"Yam, yam, yam, yam, yam!"
				};
				npc.broadcastPacket(new CreatureSay(objectId, 0, npc.getName(), mytext[Rnd.get(mytext.length)]));
			}
		}
		return null;
	}
	
	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance player, boolean isPet)
	{
		feedInfo.remove(npc.getObjectId());
		return null;
	}


	public static void main(String[] args)
	{
		new FeedableBeasts();
	}
}
