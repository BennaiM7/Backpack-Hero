package dungeon;

import java.util.Objects;

import fighter.Hero;

public class BaladeDonjon {
	private Hero hero;
	private Floor floor;

	public BaladeDonjon(Floor floor, Hero hero) {
		Objects.requireNonNull(floor);
		Objects.requireNonNull(hero);
		this.hero = hero;
		this.floor = floor;
	}

	public Hero hero() {
		return hero;
	}

	/**
	 * Return the current position of the hero.
	 *
	 * @return hero coordinates
	 */
	public Coord posHero() {
		return hero.pos();
	}

	/**
	 * Try to move to the next available room.
	 *
	 * @return true if the next room is an exit
	 */
	public boolean nextRoom() {
		Coord temp = floor.availableNext(hero.pos());
		if (temp != null) {
			// Vérifier d'abord si c'est une EXIT avant de déplacer le hero
			Room nextRoom = floor.room(temp.line(), temp.col());
			if (nextRoom.roomType() == RoomType.EXIT) { // on veut d'abord vérifie si c'est une EXIT car sinon on se déplace
																									// et on entre dans la salle normalement
				nextRoom.toVisit();
				return true; // comme c'est une sortie on veut pas faire bouger le héros, on va juste passer
											// à l'étage suivant
			}
			hero.initPos(temp);
			return false;
		}
		return false;
	}

	/**
	 * Case of entering in a room, send to healer, merchant, combat, treasure or
	 * exit.
	 *
	 * @param pos position of the room
	 * @return true if the room is an exit
	 */
	public RoomType getRoom(Coord pos) {
		Objects.requireNonNull(pos);
		Room room = floor.room(pos.line(), pos.col());
		return room.roomType();
	}
}