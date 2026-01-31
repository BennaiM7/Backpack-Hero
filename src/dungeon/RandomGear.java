package dungeon;

import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

import backpack.object.Equipment;
import backpack.object.Gear;
import backpack.object.Rarity;
import backpack.object.ShieldType;
import backpack.object.WeaponType;

public enum RandomGear implements GenerationItem {
	WOODEN_SWORD("Wooden Sword", 4, GenerationItem.shape1x2(), 1, Rarity.COMMON, WeaponType.MELEE),
	WOODEN_SHIELD("Wooden Shield", GenerationItem.shape2x1(), 1, 3, Rarity.COMMON, ShieldType.SHIELD),
	DAGGER("Dagger", 3, GenerationItem.shape1x1(), 1, Rarity.COMMON, WeaponType.MELEE),
	SHORT_BOW("Short Bow", 3, GenerationItem.shape1x2(), 2, Rarity.COMMON, WeaponType.BOW),
	LEATHER_ARMOR("Leather Armor", GenerationItem.shape2x2Full(), 1, 4, Rarity.COMMON, ShieldType.ARMOR),
	STEEL_SWORD("Steel Sword", 7, GenerationItem.shape1x3(), 2, Rarity.UNCOMMON, WeaponType.MELEE),
	BATTLE_AXE("Battle Axe", 9, GenerationItem.shape2x2LReverse(), 3, Rarity.UNCOMMON, WeaponType.MELEE),
	IRON_SHIELD("Iron Shield", GenerationItem.shape2x2Full(), 1, 6, Rarity.UNCOMMON, ShieldType.SHIELD),
	MAGIC_WAND("Magic Wand", 5, GenerationItem.shape2x1(), 1, Rarity.UNCOMMON, WeaponType.MAGIC, 3),
	CROSSBOW("Crossbow", 6, GenerationItem.shape2x2L(), 2, Rarity.UNCOMMON, WeaponType.BOW),
	PALADIN_SWORD("Paladin's Sword", 10, GenerationItem.shape1x3(), 2, Rarity.RARE, WeaponType.MELEE),
	ENCHANTED_STAFF("Enchanted Staff", 8, GenerationItem.shape3x1(), 2, Rarity.RARE, WeaponType.MAGIC, 5),
	TOWER_SHIELD("Tower Shield", GenerationItem.shape3x2Full(), 2, 10, Rarity.RARE, ShieldType.SHIELD),
	JAGGED_BLADE("Jagged Blade", 12, GenerationItem.shape2x2Full(), 2, Rarity.LEGENDARY, WeaponType.MELEE),
	DRAGON_SCALE_ARMOR("Dragon Scale Armor", GenerationItem.shape2x3Full(), 2, 15, Rarity.LEGENDARY, ShieldType.ARMOR);


	private final String name;
	private final int basicDamage;
	private final boolean[][] shape;
	private final int cost;
	private final int protectionBasic;
	private final Rarity rarity;
	private final WeaponType typeW;
	private final ShieldType typeS;
	private final int mana;

	// armes normales sans mana
	RandomGear(String name, int basicDamage, boolean[][] shape, int cost, Rarity rarity, WeaponType typeW) {
		this(name, basicDamage, shape, cost, 0, rarity, typeW, null, 0);
	}

	// les armes magiques
	RandomGear(String name, int basicDamage, boolean[][] shape, int cost, Rarity rarity, WeaponType typeW, int mana) {
		this(name, basicDamage, shape, cost, 0, rarity, typeW, null, mana);
	}

	// boucliers magiques avec mana
	RandomGear(String name, int basicDamage, boolean[][] shape, int cost, Rarity rarity, ShieldType typeS, int mana) {
		this(name, basicDamage, shape, cost, 0, rarity, null, typeS, mana);
	}

	// oucliers sans mana
	RandomGear(String name, boolean[][] shape, int cost, int protectionBasic, Rarity rarity, ShieldType typeS) {
		this(name, 0, shape, cost, protectionBasic, rarity, null, typeS, 0);
	}

	RandomGear(String name, int basicDamage, boolean[][] shape, int cost, int protectionBasic, Rarity rarity,
			WeaponType typeW, ShieldType typeS, int mana) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(rarity);
		this.name = name;
		this.basicDamage = basicDamage;
		this.shape = shape;
		this.cost = cost;
		this.protectionBasic = protectionBasic;
		this.rarity = rarity;
		this.typeW = typeW;
		this.typeS = typeS;
		this.mana = mana;
	}

	/**
	 * Create a Gear object from this enum constant's stats.
	 */
	public Gear createGear() {
		if (typeW != null && mana > 0) {
			return new Gear(name, basicDamage, shape, cost, rarity, typeW, mana); // arme magique
		} else if (typeW != null) {
			return new Gear(name, basicDamage, shape, cost, rarity, typeW); // arme normal
		} else if (typeS != null && mana > 0) {
			return new Gear(name, basicDamage, shape, cost, rarity, typeS, mana); // bouclier magique
		} else {
			return new Gear(name, shape, cost, protectionBasic, rarity, typeS); // bouclier normal
		}
	}


	@Override
	public Equipment create() {
		return createGear();
	}

	@Override
	public Rarity rarity() {
		return rarity;
	}

	/**
	 * Get a random Gear item from all available types.
	 */
	public static Gear randomGear(RandomGenerator rand) {
		Objects.requireNonNull(rand);
		List<RandomGear> gears = List.of(values());
		return gears.get(rand.nextInt(gears.size())).createGear();
	}

}
