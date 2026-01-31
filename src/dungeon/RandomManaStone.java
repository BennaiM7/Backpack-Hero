package dungeon;

import java.util.Objects;


import backpack.object.Equipment;
import backpack.object.ManaStone;
import backpack.object.Rarity;

public enum RandomManaStone  implements GenerationItem{
	PEBBLE("Mana Pebble", 1, "MANA", Rarity.COMMON), SMALL_STONE("Small Mana Stone", 2, "MANA", Rarity.COMMON),
	MANA_STONE("Mana Stone", 3, "MANA", Rarity.UNCOMMON), CHARGED_STONE("Charged Stone", 4, "MANA", Rarity.UNCOMMON),
	CRYSTAL("Mana Crystal", 6, "ARCANE", Rarity.RARE),
	CORE("Mana Core", 9, "ARCANE", Rarity.LEGENDARY);
	private final String name;
	private final int manaBasic;
	private final String type;
	private final Rarity rarity;

	RandomManaStone(String name, int manaBasic, String type, Rarity rarity) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(type);
		Objects.requireNonNull(rarity);
		this.name = name;
		this.manaBasic = manaBasic;
		this.type = type;
		this.rarity = rarity;
	}

	public ManaStone createManaStone() {
		return new ManaStone(name, manaBasic, type, rarity);
	}

	public Rarity rarity() {
		return rarity;
	}

	@Override
	public Equipment create() {
	  return createManaStone();
	}

}
