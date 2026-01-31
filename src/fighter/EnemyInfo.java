package fighter;

import java.util.List;
import java.util.Objects;

import backpack.object.Curse;
import backpack.object.MalusType;

public record EnemyInfo(String name, int maxHp, int minDamage, int maxDamage, int minProtection, int maxProtection, int healMin, int healMax, int xpReward, List<Curse> curses) {
	public EnemyInfo {
		Objects.requireNonNull(name);
		if (maxHp <= 0) {
			throw new IllegalArgumentException();
		}
		if (minDamage < 0 || maxDamage < 0 || minDamage > maxDamage) {
			throw new IllegalArgumentException();
		}
		if (minProtection < 0 || maxProtection < 0 || minProtection > maxProtection) {
			throw new IllegalArgumentException();
		}
		if (healMin < 0 || healMax < 0 || healMin > healMax) {
			throw new IllegalArgumentException();
		}
		if (xpReward < 0) {
			throw new IllegalArgumentException();
		}
		Objects.requireNonNull(curses);
	}
	/**
	 * Check if this enemy can use curses.
	 */
	public boolean canCurse() {
      return !curses.isEmpty();
  }
  
	private static final EnemyInfo PETIT_RAT_LOUP = new EnemyInfo("Petit Rat-Loup", 32, 6, 7, 7, 9, 0, 0, 9, List.of());
	private static final EnemyInfo RAT_LOUP = new EnemyInfo("Rat-Loup", 45, 7, 9, 6, 8, 0, 0, 6, List.of());
	private static final EnemyInfo SORCIER_GRENOUILLE = new EnemyInfo("Sorcier-grenouille", 45, 0, 0, 0, 0, 0, 0, 8, List.of(new Curse(new boolean[][]{{true}}, MalusType.HEAL, "Tiny Sickness", 2)));
	private static final EnemyInfo REINE_ABEILLES = new EnemyInfo("Reine des abeilles", 74, 15, 15, 0, 0, 0, 0, 20, List.of(new Curse(new boolean[][]{{true, true}, {true, true}}, MalusType.DAMAGE, "Bleed Plate", 6)));
	private static final EnemyInfo OMBRE_VIVANTE = new EnemyInfo("Ombre vivante", 50, 0, 0, 0, 0, 0, 0, 25, List.of(new Curse(new boolean[][]{{true, true}}, MalusType.HEAL, "Sickness Bar", 4)));

	public static EnemyInfo PETIT_RAT_LOUP() { return PETIT_RAT_LOUP; }
	public static EnemyInfo RAT_LOUP() { return RAT_LOUP; }
	public static EnemyInfo SORCIER_GRENOUILLE() { return SORCIER_GRENOUILLE; }
	public static EnemyInfo REINE_ABEILLES() { return REINE_ABEILLES; }
	public static EnemyInfo OMBRE_VIVANTE() { return OMBRE_VIVANTE; }
}