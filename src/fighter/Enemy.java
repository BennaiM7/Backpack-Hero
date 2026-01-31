package fighter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

import actions.Action;
import actions.ActionType;
import backpack.object.Curse;

public final class Enemy implements Fighter {
	private final EnemyInfo typeEnemy;
	private int currentHp;
	private int currentProtection;

	/**
	 * Create a new enemy with the given type and set its HP.
	 */
	public Enemy(EnemyInfo typeEnemy) {
		Objects.requireNonNull(typeEnemy);
		this.typeEnemy = typeEnemy;
		this.currentHp = typeEnemy.maxHp();
		this.currentProtection = 0;
	}

	/**
	 * Get a random integer between min and max (inclusive).
	 */
	private int randomNumberBetween(RandomGenerator rand, int min, int max) {
		Objects.requireNonNull(rand);
		return rand.nextInt((max - min + 1)) + min;
	}

	@Override
	public void addProtection(int protectionPoint) {
		if (protectionPoint < 0) {
			throw new IllegalArgumentException("Protection donnée < 0");
		}
		currentProtection += protectionPoint;
	}

	/**
	 * Reset the enemy's protection to zero.
	 */
	public void resetProtection() {
		currentProtection = 0;
	}

	@Override
	/**
	 * Take damage, reducing protection first, then HP.
	 */
	public void takeDamage(int damage) {
		if (damage < 0) {
			throw new IllegalArgumentException("Dégats négatifs");
		}
		int remainingDamage = damage - currentProtection;
		if (remainingDamage < 0) {
			remainingDamage = 0;
		}
		currentProtection -= damage;
		if (currentProtection < 0) {
			currentProtection = 0;
		}
		currentHp -= remainingDamage;
		if (currentHp < 0) {
			currentHp = 0;
		}

	}

	@Override
	/**
	 * Heal the enemy by a certain amount, up to max HP.
	 */
	public void heal(int healPoints) {
		if (healPoints < 0) {
			throw new IllegalArgumentException("Heal négatif");
		}
		currentHp += healPoints;
		if (currentHp > typeEnemy.maxHp()) {
			currentHp = typeEnemy.maxHp();
		}
	}

	/**
	 * Get a random heal value for this enemy.
	 */
	public int randomHeal(RandomGenerator rand) {
		Objects.requireNonNull(rand);
		int healPoint = randomNumberBetween(rand, typeEnemy.healMin(), typeEnemy.healMax());
		if (healPoint < 0) {
			throw new IllegalArgumentException();
		}
		return healPoint; // pour après heal avec ces points là
	}

	/**
	 * Get a random attack value for this enemy.
	 */
	public int randomAttack(RandomGenerator rand) {
		Objects.requireNonNull(rand);
		return randomNumberBetween(rand, typeEnemy.minDamage(), typeEnemy.maxDamage());
	}

	/**
	 * Get a random protection value for this enemy.
	 */
	public int randomProtect(RandomGenerator rand) {
		Objects.requireNonNull(rand);
		return randomNumberBetween(rand, typeEnemy.minProtection(), typeEnemy.maxProtection());

	}

	/**
	 * Get a random curse from this enemy, if possible.
	 */
	public Curse randomCurse(RandomGenerator rand) {
		Objects.requireNonNull(rand);
		if (!typeEnemy.canCurse()) {
			return null;
		}
		var curses = typeEnemy.curses();
		return curses.get(rand.nextInt(curses.size()));
	}

	/**
	 * Generate a list of random actions for this enemy's turn.
	 */
	public List<Action> randomActions(RandomGenerator rand) {
		Objects.requireNonNull(rand);
		List<Action> actions = new ArrayList<>();
		int nbAction = rand.nextInt(3) + 1;
		for (int i = 0; i < nbAction; i++) {
			int action = selectAction(rand);
			Action selectedAction = executeAction(action, rand);
			if (selectedAction != null) {
				actions.add(selectedAction);
			}
		}
		return actions;
	}

	/**
	 * Select a random action type for this enemy (attack, protect, heal, curse).
	 */
	private int selectAction(RandomGenerator rand) {
		int randomValue = rand.nextInt(20) + 1;
		boolean canHeal = typeEnemy.healMax() > 0;
		boolean canCurse = typeEnemy.canCurse();
		boolean canProtect = typeEnemy.maxProtection() > 0;
		boolean canAttack = typeEnemy.maxDamage() > 0;
		
		if (canHeal) { // peut heal
			if (canCurse && !canProtect) { // peut curse mais pas protéger et peut heal
				return randomValue == 1 ? 3 : (randomValue <= 7 ? 1 : (randomValue <= 14 ? 2 : 4));
			} else { // peut heal et attaquer/protéger normalement
				return randomValue <= 7 ? 1 : (randomValue <= 14 ? 2 : 4);
			}
		} else if (canCurse && canAttack && !canProtect) { // curse et attaque mais pas heal/protect
			return randomValue == 1 ? 3 : (randomValue <= 11 ? 1 : 2);
		} else if (canCurse && !canAttack) { // peut curse et rien d'autre
			return 3;
		} else if (canCurse) { // curse et attaque/protect mais pas heal
			return randomValue == 1 ? 3 : (randomValue <= 11 ? 1 : 2);
		} else { // peut juste attaquer/protéger
			return randomValue <= 11 ? 1 : 2;
		}
	}

	/**
	 * Execute the selected action and return the Action object.
	 */
	private Action executeAction(int action, RandomGenerator rand) {
		return switch(action) {
			case 1 -> new Action(ActionType.ATTACK, randomAttack(rand), null);
			case 2 -> new Action(ActionType.PROTECTION, randomProtect(rand), null);
			case 3 -> {
				Curse curse = randomCurse(rand);
				yield curse != null ? new Action(ActionType.CURSE, -1, curse) : null;
			}
			case 4 -> new Action(ActionType.HEAL, randomHeal(rand), null);
			default -> null;
		};
	}

	@Override
	public boolean isAlive() {
		return currentHp > 0;
	}

	public EnemyInfo enemyInfo() {
		return typeEnemy;
	}

	public int getCurrentHp() {
		return currentHp;
	}

}
