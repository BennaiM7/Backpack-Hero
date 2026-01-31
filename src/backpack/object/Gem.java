package backpack.object;

import java.util.Objects;

public record Gem(String name, boolean[][] shape, GemType type, int bonus,Rarity rarity) implements Equipment {

	public Gem {
		Objects.requireNonNull(name);
		Objects.requireNonNull(shape);
		Objects.requireNonNull(type);
		Objects.requireNonNull(rarity);
		if (bonus <= 0) {
			throw new IllegalArgumentException();
		}
	}
	public GemType type() {
		return type;
	}
	public boolean[][] shape() {
		return shape;
	}
	public ItemType getType() {
		return ItemType.GEM;
	}

}
