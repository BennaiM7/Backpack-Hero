package backpack.object;

import java.util.Objects;

public class ManaStone implements Equipment {

	private final String name;
	private final int manaBasic;
	private int manaFight;
	private final boolean[][] shape;
	private final Rarity rarity;
	private final String type;

	public ManaStone(String name, int manaBasic, String type, Rarity rarity) {
		if (manaBasic <= 0){
			throw new IllegalArgumentException("La pierre doit avoir au moins 1 point de mana");
		}
		Objects.requireNonNull(name);
		Objects.requireNonNull(rarity);
		Objects.requireNonNull(type);

		shape = new boolean[1][1];
		shape[0][0] = true;
		this.manaBasic = manaBasic;
		this.type = type;
		this.name = name;
		this.rarity = rarity;
	}

	public String name() {
		return name;
	}

	public String type() {
		return type;
	}

	public ItemType getType() {
		return ItemType.MANASTONE;
	}

	public void useMana(int utilisation) {
		if (utilisation < 0){
			throw new IllegalArgumentException();
		}
		if (manaFight - utilisation < 0){
			throw new IllegalArgumentException();
		}
		if (utilisation > manaFight){
			throw new IllegalArgumentException();
		}
		manaFight -= utilisation;
	}

	public void rechargeMana() {
		manaFight = manaBasic;
	}

	public boolean[][] shape() {
		return shape;
	}

	public Rarity rarity() {
		return rarity;
	}

	public int getManaFight() {
		return manaFight;
	}

}
