package dungeon;


import java.util.Objects;


import backpack.object.Armor;
import backpack.object.Equipment;
import backpack.object.Rarity;

public enum RandomArmor implements GenerationItem {
	TROLL_ARMOR("Troll Armor", 4, new boolean[][] { { true, true }, { true, true } }, Rarity.COMMON),
	CHAINMAIL_ARMOR("Chainmail Armor", 7, new boolean[][] { { true, true, true }, { true, true, true } }, Rarity.UNCOMMON),
	KNIGHT_PLATE_ARMOR("Knight Plate Armor", 11, new boolean[][] { { true, true }, { true, true }, { true, true } }, Rarity.RARE),
	DRAGON_SCALE_ARMOR("Dragon Scale Armor", 15, new boolean[][] { { true, true, true }, { true, true, true } }, Rarity.LEGENDARY);

	private final String name;
	private final int protection;
	private final boolean[][] shape;
	private final Rarity rarity;

	RandomArmor(String name, int protection, boolean[][] shape, Rarity rarity) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(rarity);
		this.name = name;
		this.protection = protection;
		this.shape = shape;
		this.rarity = rarity;
	}

	@Override
	public Rarity rarity() {
		return rarity;
	}

	@Override
	public Equipment create() {
		return new Armor(name, protection, shape, rarity);
	}

}
