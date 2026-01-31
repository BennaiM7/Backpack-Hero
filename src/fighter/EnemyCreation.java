package fighter;

import java.util.Objects;
import java.util.random.RandomGenerator;

public class EnemyCreation {
	/**
	 * Create a random enemy from available enemy types.
	 */
	public static Enemy createRandom(RandomGenerator rand) {
		Objects.requireNonNull(rand);
		int type = rand.nextInt(8);
		if (type <= 2) {
			return new Enemy(EnemyInfo.RAT_LOUP());
		} else if (type >= 2 && type <= 5) {
			return new Enemy(EnemyInfo.PETIT_RAT_LOUP());
		} else if (type == 6) {
			return new Enemy(EnemyInfo.SORCIER_GRENOUILLE());
		} else if (type == 7) {
			return new Enemy(EnemyInfo.OMBRE_VIVANTE());
		} else {
			return new Enemy(EnemyInfo.REINE_ABEILLES());
		}
	}
}