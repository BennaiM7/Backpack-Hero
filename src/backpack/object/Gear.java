package backpack.object;

import java.util.Objects;

public record Gear(String name, int basicDamage, boolean[][] shape, int cost, int protectionBasic, Rarity rarity,
		WeaponType typeW, ShieldType typeS, int mana) implements Equipment {
	/**
	 * Create a normal weapon (no mana).
	 */
	public Gear(String name, int basicDamage, boolean[][] shape, int cost, Rarity rarity, WeaponType typeW) {
		this(name, basicDamage, shape, cost, 0, rarity, typeW, null, 0);
	}
	/**
	 * Create a magic weapon (with mana).
	 */
	public Gear(String name, int basicDamage, boolean[][] shape, int cost, Rarity rarity, WeaponType typeW, int mana) {
		this(name, basicDamage, shape, cost, 0, rarity, typeW, null, mana);
	}
	/**
	 * Create a magic shield (with mana).
	 */
	public Gear(String name, int basicDamage, boolean[][] shape, int cost, Rarity rarity, ShieldType typeS, int mana) {
		this(name, basicDamage, shape, cost, 0, rarity, null, typeS, mana);
	}
	/**
	 * Create a normal shield (no mana).
	 */
	public Gear(String name, boolean[][] shape, int cost, int protectionBasic, Rarity rarity, ShieldType typeS) {
		this(name, 0, shape, cost, protectionBasic, rarity, null, typeS, 0);
	}

	public Gear {
		Objects.requireNonNull(name);
		Objects.requireNonNull(rarity);
		Objects.requireNonNull(shape);
		if (protectionBasic < 0)
			throw new IllegalArgumentException("ne peut avoir moin de 0 de protection");
		if (basicDamage < 0)
			throw new IllegalArgumentException("debatB ne peut avoir < 0 degat");
		if (cost < 0)
			throw new IllegalArgumentException("cout negatif impossible");
		if (mana < 0)
			throw new IllegalArgumentException("mana negatif impossible");

	}

	/**
	 * Get the item type (always GEAR).
	 */
	public ItemType getType() {
		return ItemType.GEAR;
	}

	/**
	 * Get the weapon type if this gear is a weapon.
	 */
	public WeaponType getWeaponType() {
		return typeW;
	}

}
