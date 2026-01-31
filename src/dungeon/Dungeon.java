package dungeon;

import java.util.Objects;

import fighter.Hero;

public class Dungeon {
	private final Floor[] etages;
	private int fluent;
	private Coord posHero;

	public Dungeon() {
		etages = new Floor[3];
		Floor builder = new Floor(new Coord(0, 0), new Coord(0, 0));
		for (int i = 0; i < etages.length; i++) {
			etages[i] = builder.generator();
		}
		fluent = 0;
		posHero = etages[0].posStart();
	}

	public Floor currentFloor() {
		return etages[fluent];
	}

	public int fluent() {
		return fluent;
	}

	public Coord posHero() {
		return posHero;
	}

	public boolean accessTopFloor(Hero hero) {
		Objects.requireNonNull(hero);
		if (fluent >= etages.length - 1)
			return false;
		fluent++;
		posHero = etages[fluent].posStart();
		hero.initPos(posHero);
		return true;
	}
}
