package dungeon;


import java.util.Objects;

import backpack.object.Curse;
import backpack.object.MalusType;
import backpack.object.Rarity;

public enum RandomCurse {
	TINY_WEAK("Tiny Weakness", GenerationItem.shape1x1(), MalusType.WEAK, Rarity.COMMON),
	TINY_DAMAGE("Tiny Bleed", GenerationItem.shape1x1(), MalusType.DAMAGE, Rarity.COMMON),
	TINY_HEAL("Tiny Sickness", GenerationItem.shape1x1(), MalusType.HEAL, Rarity.COMMON),
	DAMAGE_BAR("Bleed Bar", GenerationItem.shape1x2(), MalusType.DAMAGE, Rarity.UNCOMMON),
	HEAL_BAR("Sickness Bar", GenerationItem.shape1x2(), MalusType.HEAL, Rarity.UNCOMMON),
	DAMAGE_PLATE("Bleed Plate", GenerationItem.shape2x2Full(), MalusType.DAMAGE, Rarity.RARE),
	HEAL_PLATE("Sickness Plate", GenerationItem.shape2x2Full(), MalusType.HEAL, Rarity.RARE),
	DAMAGE_CURSE("Grand Bleed Curse", GenerationItem.shape2x3Full(), MalusType.DAMAGE, Rarity.LEGENDARY),
	HEAL_CURSE("Grand Sickness Curse", GenerationItem.shape2x3Full(), MalusType.HEAL, Rarity.LEGENDARY);

	private final String name;
	private final boolean[][] shape;
	private final MalusType malusType;
	private final Rarity rarity;

	RandomCurse(String name, boolean[][] shape, MalusType malusType, Rarity rarity) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(malusType);
		Objects.requireNonNull(rarity);
		this.name = name;
		this.shape = shape;
		this.malusType = malusType;
		this.rarity = rarity;
	}

	public Curse createCurse() {
		return new Curse(shape, malusType,name,malus(this.rarity));
	}

	private int malus(Rarity rarity) {
		Objects.requireNonNull(rarity);
	  return switch (rarity) {
	    case COMMON -> 2;
	    case UNCOMMON -> 4;
	    case RARE -> 6;
	    case LEGENDARY -> 8;
	  };
	}
	public Rarity rarity() {
		return rarity;
	}


}
