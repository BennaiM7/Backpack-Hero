package backpack.object;

import java.util.Objects;

public record Arrow (String name, int damage ,boolean[][] shape,Rarity rarity) implements Equipment{
	
	public Arrow {
		Objects.requireNonNull(name);
		Objects.requireNonNull(shape);
		Objects.requireNonNull(rarity);
		if(damage < 0) {throw new IllegalArgumentException("ne peut etre negatif");
		}
	}
	public ItemType getType() {
		return ItemType.ARROW;
	}
	
}
