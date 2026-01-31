package backpack.object;

import java.util.Objects;

public record Armor(String name, int protection, boolean [][] shape, Rarity rarity) implements Equipment{
	
	public Armor {
		Objects.requireNonNull(name);
		if(protection < 0) {
			throw new IllegalArgumentException("la protection ne peut etre negatif");
		}
		Objects.requireNonNull(rarity);
		Objects.requireNonNull(shape);
	}
	
	public ItemType getType() {
		return ItemType.ARMOR;
	}
}


