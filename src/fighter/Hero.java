package fighter;

import java.util.Objects;

import backpack.BackPack;
import backpack.object.Gold;
import backpack.object.ManaStone;
import dungeon.Coord;

public final class Hero implements Fighter {
	private int maxHp;
	private int currentHp;
	private final int turnEnergy;
	private int currentEnergy;
	private int protection;
	private int level;
	private int xp;
	private double xpNextLevel;
	private int maxMana;
	private int currentMana;
	private Gold gold;
	private BackPack backpack;
	private Coord pos;
	private int weakDamageMalus = 0;
	private int energyPenalty = 0;

	/**
	 * Create a new hero with the given max HP and default stats.
	 */
	public Hero(int maxHp) {
		this.maxHp = maxHp;
		this.currentHp = maxHp;
		this.turnEnergy = 3;
		this.currentEnergy = turnEnergy;
		this.protection = 0; // protection de base, sera augmenté si il utilise un bouclier
		this.level = 1;
		this.xp = 0;
		this.xpNextLevel = 10.0;
		this.maxMana = 10; // mana de base
		this.currentMana = 0; // commence sans mana, doit utiliser les pierres
		this.gold = new Gold(0);
		this.backpack = new BackPack();
	}

	/**
	 * Return the hero current position.
	 *
	 * @return hero position
	 */
	public Coord pos() {
		return pos;
	}

	/**
	 * Set the hero position.
	 *
	 * @param posHero new position
	 */
	public void initPos(Coord posHero) {
		Objects.requireNonNull(posHero);
		pos = posHero;
	}

	/**
	 * Add protection to the hero that will be reset in the next turn.
	 *
	 * @param p amount of protection to add
	 */
	@Override
	public void addProtection(int p) {
		if (p<0) {
			throw new IllegalArgumentException("protection négative");
		}
		protection += p;
	}

	/**
	 * Use energy if available.
	 *
	 * @param usedEnergy energy to use
	 * @return true if the energy was used
	 */
	public boolean useEnergy(int usedEnergy) {
		if ((currentEnergy - usedEnergy) >= 0) {
			currentEnergy -= usedEnergy;
			return true;
		}
		return false;
	}

	/**
	 * Add energy to the hero max 3 per turn
	 *
	 * @param energy energy to add
	 */
	public void addEnergy(int energy) {
		if (energy < 0) {
			throw new IllegalArgumentException("energie négatif");
		}
		currentEnergy += energy;
		if (currentEnergy > turnEnergy)
			currentEnergy = turnEnergy;
	}

	/**
	 * Apply damage we take substact the protection with the damage
	 *
	 * @param damage damage the enemy gives
	 */
	@Override
	public void takeDamage(int damage) {
		if (damage < 0) {
			throw new IllegalArgumentException("Dégats négatifs");			
		}
		int remainingDamage = damage - protection;
		if (remainingDamage < 0)
			remainingDamage = 0;
		protection -= damage;
		if (protection < 0)
			protection = 0;
		currentHp = currentHp - remainingDamage;
		if (currentHp < 0)
			currentHp = 0;
	}

	/**
	 * Heal the hero by the given amount.
	 *
	 * @param hp to add
	 */
	@Override
	public void heal(int hp) {
		if (hp < 0) {
			throw new IllegalArgumentException();
		}
		currentHp += hp;
		if (currentHp > maxHp) {
			currentHp = maxHp;
		}
	}

	/**
	 * Increase the hero maximum HP.
	 *
	 * @param sup value to add to max HP
	 */
	public void increaseMaxHp(int sup) {
		if (sup < 0) {
			throw new IllegalArgumentException();
		}
		maxHp = maxHp + sup;
	}

	/**
	 * Return if the hero is alive.
	 *
	 * @return true if HP > 0
	 */
	@Override
	public boolean isAlive() {
		return currentHp > 0;
	}

	/**
	 * Add experience and verify if we can level up.
	 *
	 * @param earned XP gained
	 */
	public void earnXp(int earned) {
		if (earned < 0) {
			throw new IllegalArgumentException();
		}
		xp += earned;
		while (xp >= xpNextLevel) { // car on pourrait gagner par exemple 50xp d'un coup et passer plusieurs level
																// d'un coup
			xp -= xpNextLevel;
			earnLevel();
		}
	}

	/**
	 * Recharge all mana stones in the hero's backpack.
	 */
	public void rechargerManaStones() {
		Objects.requireNonNull(backpack);
		for (int i = 0; i < backpack.line(); i++) {
			for (int j = 0; j < backpack.col(); j++) {
				var eq = backpack.detection(i, j);
				if (eq != null) {
					switch (eq) {
					case ManaStone m -> m.rechargeMana();
					default -> {
					}
					}
				}
			}
		}
	}

	/**
	 * Level up the hero and increase max HP and XP needed for next level.
	 */
	public void earnLevel() {
		level++;
		xpNextLevel *= 1.5; // augmenter l'xp prochain level à voir si 1.5 est assez ou pas assez
		maxHp += 5;
	}

	/**
	 * Add mana to the hero, up to the maximum.
	 */
	public void addMana(int m) {
		if (m < 0) {
			throw new IllegalArgumentException("Impossible d'ajouter une quantité négative de mana");
		}
		currentMana += m;
		if (currentMana > maxMana)
			currentMana = maxMana;
	}

	/**
	 * Use mana if available, return true if successful.
	 */
	public boolean useMana(int manaUsed) {
		if (manaUsed < 0)
			throw new IllegalArgumentException("Impossible d'utiliser une quantité négative de mana");
		if (currentMana >= manaUsed) {
			currentMana -= manaUsed;
			return true;
		}
		return false;
	}

	public BackPack getBackpack() {
		return backpack;
	}

	public int getMaxHp() {
		return maxHp;
	}

	public int getCurrentHp() {
		return currentHp;
	}

	public int getCurrentEnergy() {
		return currentEnergy;
	}

	public Gold getGold() {
		return gold;
	}

	public int getProtection() {
		return protection;
	}

	/**
	 * Reset the hero's mana to zero.
	 */
	public void resetMana() {
		currentMana = 0;
	}

	public int getCurrentMana() {
		return currentMana;
	}

	/**
	 * Reset the hero's energy to the maximum for a new turn.
	 */
	public void resetEnergie() {
		currentEnergy = turnEnergy;
	}

	/**
	 * Reset the hero's protection to zero.
	 */
	public void resetProtection() {
		protection = 0;
	}

	public int getLevel() {
		return level;
	}

	/**
	 * Set the hero's weak damage malus (penalty).
	 */
	public void setWeakDamageMalus(int malus) {
		this.weakDamageMalus = Math.max(0, malus);
	}

	/**
	 * Apply outgoing damage, reduced by weak damage malus.
	 */
	public int applyOutgoingDamage(int baseDamage) {
		return Math.max(0, baseDamage - weakDamageMalus);
	}

	public int weakDamageMalus() {
		return weakDamageMalus;
	}

	/**
	 * Set the hero's energy penalty (for curses).
	 */
	public void setEnergyPenalty(int penalty) {
		this.energyPenalty = Math.max(0, penalty);
	}

	/**
	 * Clear all combat maluses (penalties) from the hero.
	 */
	public void clearCombatMaluses() {
		energyPenalty = 0;
		weakDamageMalus = 0;
	}

	public int energyPenalty() {
		return energyPenalty;
	}
}
