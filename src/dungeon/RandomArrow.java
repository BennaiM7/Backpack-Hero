package dungeon;

import java.util.Objects;

import backpack.object.Arrow;
import backpack.object.Equipment;
import backpack.object.Rarity;

public enum RandomArrow implements GenerationItem {

	ARROW_COMMON("arrow_common", 1, GenerationItem.shape1x1(), Rarity.COMMON),
	ARROW_UNCOMMON("arrow_uncommon", 2, GenerationItem.shape1x2(), Rarity.UNCOMMON),
	ARROW_RARE("arrow_rare", 3, GenerationItem.shape2x1(), Rarity.RARE),
	ARROW_LEGENDARY("arrow_legendary", 5, GenerationItem.shape2x2L(), Rarity.LEGENDARY);

	private final boolean[][] shape;
	private final Rarity rarity;
	private final int damage;
	private final String name;

	RandomArrow(String name, int damage, boolean[][] shape, Rarity rarity) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(rarity);
		this.name = name;
		this.shape = shape;
		this.rarity = rarity;
		this.damage = damage;
	}

	public Rarity rarity() {
		return rarity;
	}

	public Arrow createArrow() {
		return new Arrow(name, damage, shape, rarity);
	}

	@Override
	public Equipment create() {
		return createArrow();
	}

}
