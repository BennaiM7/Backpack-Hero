package backpack.object;

import java.util.Objects;

public class Curse implements Equipment {
	private final boolean[][] shape;
	private final MalusType malusType;
	private int malus;
	private Rarity rarity = Rarity.COMMON;
	private final String name;

	public Curse(boolean[][] shape, MalusType malusType, String name, int malus) {
		Objects.requireNonNull(shape);
		Objects.requireNonNull(malusType);
		Objects.requireNonNull(name);
		if (malus <= 0) {
			throw new IllegalArgumentException();
		}
		this.malus = malus;
		this.shape = shape;
		this.malusType = malusType;
		this.name = name;
	}

	public String name() {
		return name;
	}

	public Rarity rarity() {
		return rarity;
	}

	public int malus() {
		return malus;
	}

	public MalusType malusType() {
		return malusType;
	}

	public ItemType getType() {
		return ItemType.CURSE;
	}

	@Override
	public boolean[][] shape() {
		return shape;
	}
}
