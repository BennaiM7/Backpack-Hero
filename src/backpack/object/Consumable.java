package backpack.object;

import java.util.Objects;

import fighter.Hero;

public record Consumable(String name, boolean[][] shape, PotionType type, int amount,Rarity rarity) implements Equipment {
	public Consumable {
		Objects.requireNonNull(name);
		Objects.requireNonNull(shape);
		Objects.requireNonNull(type);
		Objects.requireNonNull(rarity);
		if (shape.length == 0 || shape[0].length == 0) {
			throw new IllegalArgumentException("Shape vide");
		}
		if (amount <= 0) {
			throw new IllegalArgumentException("amount doit être > 0");
		}
	}

	@Override
	public ItemType getType() {
		return ItemType.POTION;
	}

	@Override
	public boolean[][] shape() {
		return shape;
	}

	public void use(Hero hero) {
		Objects.requireNonNull(hero);
		switch (type) {
		case HEAL -> hero.heal(amount);
		case MANA -> hero.addMana(amount);
		case ENERGY -> hero.addEnergy(amount);
		}
	}
}
