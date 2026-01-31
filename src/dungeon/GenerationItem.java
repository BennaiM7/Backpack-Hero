package dungeon;

import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

import backpack.object.Equipment;
import backpack.object.Rarity;

public sealed interface GenerationItem permits RandomGear,RandomGem,RandomManaStone,RandomPotion,RandomArmor,RandomArrow {
	Rarity rarity();
	Equipment create();
	
	public static Equipment randomByRarity(RandomGenerator rand, Rarity rarity,List<GenerationItem> list) {
		Objects.requireNonNull(rand);
		Objects.requireNonNull(rarity);
		Objects.requireNonNull(list);
		List<GenerationItem> itemRarity = getByRarity(rarity,list);
		if (itemRarity.isEmpty()) {
			throw new IllegalArgumentException();
		}
		return itemRarity.get(rand.nextInt(itemRarity.size())).create();
	}
	
	private static List<GenerationItem> getByRarity(Rarity rarity,List<GenerationItem> list) {
		return list.stream()
				.filter(g -> g.rarity() == rarity)
				.toList();
	}
	
	public static Rarity generation(RandomGenerator rand) {
		Objects.requireNonNull(rand);
	  int roll = rand.nextInt(100);
	  if (roll < 55) return Rarity.COMMON;
	  if (roll < 85) return Rarity.UNCOMMON;
	  if (roll < 97) return Rarity.RARE;
	  return Rarity.LEGENDARY;
	}
	
	static boolean[][] shape1x1() {
		return new boolean[][] { { true } };
	}

	static boolean[][] shape1x2() {
		return new boolean[][] { { true, true } };
	}

	static boolean[][] shape2x2Full() {
		return new boolean[][] { { true, true }, { true, true } };
	}

	static boolean[][] shape2x3Full() {
		return new boolean[][] { { true, true, true }, { true, true, true } };
	}
	
  static boolean[][] shape2x1() {
    return new boolean[][] { { true }, { true } };
  }

  static boolean[][] shape2x2L() {
    return new boolean[][] { { true, true }, { true, false } };
  }
  
  static boolean[][] shape1x3() {
    return new boolean[][] { { true, true, true } };
  }

  static boolean[][] shape3x1() {
    return new boolean[][] { { true }, { true }, { true } };
  }

  static boolean[][] shape3x2Full() {
    return new boolean[][] { { true, true }, { true, true }, { true, true } };
  }

  static boolean[][] shape2x2LReverse() {
    return new boolean[][] { { true, true }, { false, true } };
  }
	
}
