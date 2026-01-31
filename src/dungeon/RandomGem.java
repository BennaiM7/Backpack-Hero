package dungeon;

import java.util.Objects;

import backpack.object.Equipment;
import backpack.object.Gem;
import backpack.object.GemType;
import backpack.object.Rarity;

public enum RandomGem implements GenerationItem {
	SMALL_HEART("Small Heart Gem", GenerationItem.shape1x1(), GemType.HEART, 2, Rarity.COMMON),
	SMALL_DAMAGE("Small Damage Gem", GenerationItem.shape1x1(), GemType.WEAPON_DAMAGE, 1, Rarity.COMMON),
	SMALL_BLOCK("Small Block Gem", GenerationItem.shape1x1(), GemType.ARMOR_BLOCK, 1, Rarity.COMMON),
	HEART_BAR("Heart Bar Gem", GenerationItem.shape1x2(), GemType.HEART, 5, Rarity.UNCOMMON),
	DAMAGE_BAR("Damage Bar Gem", GenerationItem.shape1x2(), GemType.WEAPON_DAMAGE, 2, Rarity.UNCOMMON),
	BLOCK_BAR("Block Bar Gem", GenerationItem.shape1x2(), GemType.ARMOR_BLOCK, 2, Rarity.UNCOMMON),
	HEART_PLATE("Heart Plate Gem", GenerationItem.shape2x2Full(), GemType.HEART, 10, Rarity.RARE),
	DAMAGE_PLATE("Damage Plate Gem", GenerationItem.shape2x2Full(), GemType.WEAPON_DAMAGE, 4, Rarity.RARE),
	BLOCK_PLATE("Block Plate Gem", GenerationItem.shape2x2Full(), GemType.ARMOR_BLOCK, 4, Rarity.RARE),
	HEART_CROWN("Heart Crown Gem", GenerationItem.shape2x3Full(), GemType.HEART, 18, Rarity.LEGENDARY),
	DAMAGE_CROWN("Damage Crown Gem", GenerationItem.shape2x3Full(), GemType.WEAPON_DAMAGE, 7, Rarity.LEGENDARY),
	BLOCK_CROWN("Block Crown Gem", GenerationItem.shape2x3Full(), GemType.ARMOR_BLOCK, 7, Rarity.LEGENDARY);

	private final String name;
	private final boolean[][] shape;
	private final GemType type;
	private final int bonus;
	private final Rarity rarity;

	RandomGem(String name, boolean[][] shape, GemType type, int bonus, Rarity rarity) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(type);
		Objects.requireNonNull(rarity);
		this.name = name;
		this.shape = shape;
		this.type = type;
		this.bonus = bonus;
		this.rarity = rarity;
	}

	public Gem createGem() {
		return new Gem(name, shape, type, bonus,rarity);
	}
	
	@Override
	public Equipment create() {
	  return createGem();
	}
	
	public Rarity rarity() {
		return rarity;
	}


}
