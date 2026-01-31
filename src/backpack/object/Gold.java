package backpack.object;


public class Gold implements Equipment {
	private int amount;
	private final boolean[][] shape;
	private Rarity rarity = Rarity.COMMON;
	
	public Gold(int montant) {
		if (montant < 0)
			throw new IllegalArgumentException("Montant initial negatif impossible");
		this.amount = montant;
		shape = new boolean[1][1];
		shape[0][0] = true;

	}
	
	public Rarity rarity() {
		return rarity;
	}
	
	public boolean[][] shape() {
		return shape;
	}
	
	public ItemType getType() {
		return ItemType.GOLD;
	}
	
	public void add(int positif) {
		if (positif < 0)
			throw new IllegalArgumentException("Impossible d'ajouter une somme negative");
		amount = amount + positif;
	}

	public boolean paid(int negatif) {
		if (amount - negatif < 0)
			return false;
		amount = amount - negatif;
		return true;
	}

	public int amount() {
		return amount;
	}
}
