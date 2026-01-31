package backpack.object;

public interface Equipment {
	public ItemType getType();
	boolean[][] shape();
	Rarity rarity();
	default boolean[][] getShape() {
		return shape();
	}
}
