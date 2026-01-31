package dungeon;

import java.util.Objects;

import backpack.object.Consumable;
import backpack.object.Equipment;
import backpack.object.PotionType;
import backpack.object.Rarity;

public enum RandomPotion implements GenerationItem {
	HEAL_SMALL("Petite Potion de soin", GenerationItem.shape1x1(), PotionType.HEAL, 3, 8, Rarity.COMMON),
	MANA_SMALL("Petit Élixir de mana", GenerationItem.shape1x1(), PotionType.MANA, 3, 8, Rarity.COMMON),
	ENERGY_SMALL("Petit Tonique d'énergie", GenerationItem.shape1x1(), PotionType.ENERGY, 2, 8, Rarity.COMMON),
	HEAL_MEDIUM("Potion de soin", GenerationItem.shape1x2(), PotionType.HEAL, 6, 12, Rarity.UNCOMMON),
	MANA_MEDIUM("Élixir de mana", GenerationItem.shape1x2(), PotionType.MANA, 6, 12, Rarity.UNCOMMON),
	ENERGY_MEDIUM("Tonique d'énergie", GenerationItem.shape1x2(), PotionType.ENERGY, 4, 12, Rarity.UNCOMMON),
	HEAL_LARGE("Grande Potion de soin", GenerationItem.shape2x1(), PotionType.HEAL, 10, 20, Rarity.RARE),
	MANA_LARGE("Grand Élixir de mana", GenerationItem.shape2x1(), PotionType.MANA, 10, 20, Rarity.RARE),
	ENERGY_LARGE("Grand Tonique d'énergie", GenerationItem.shape2x1(), PotionType.ENERGY, 7, 20, Rarity.RARE),
	HEAL_ELIXIR("Élixir de vie", GenerationItem.shape2x2Full(), PotionType.HEAL, 18, 30, Rarity.LEGENDARY),
	MANA_ELIXIR("Élixir arcanique", GenerationItem.shape2x2Full(), PotionType.MANA, 18, 30, Rarity.LEGENDARY),
	ENERGY_ELIXIR("Élixir de vitesse", GenerationItem.shape2x2Full(), PotionType.ENERGY, 12, 30, Rarity.LEGENDARY);

	private final String name;
	private final boolean[][] shape;
	private final PotionType type;
	private final int amount;
	private final Rarity rarity;

	RandomPotion(String name, boolean[][] shape, PotionType type, int amount, int basePrice, Rarity rarity) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(type);
		Objects.requireNonNull(rarity);
		this.name = name;
		this.shape = shape;
		this.type = type;
		this.amount = amount;
		this.rarity = rarity;
	}

	public Consumable createPotion() {
		return new Consumable(name, shape, type, amount, rarity);
	}

	public Rarity rarity() {
		return rarity;
	}

	@Override
	public Equipment create() {
		return createPotion();
	}

}
