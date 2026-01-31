package dungeon;
import java.util.Objects;

public class Room {
	private final Coord coord;
	private final RoomType roomType;
	private  boolean visit; //CHEMIN,ENNEMIS,TRESOR,MARCHAND,GUERISSEUR,SORTIE
	private boolean completed; // pour savoir si on a déjà visité une salle
	
	public Room(Coord coord,RoomType roomType) {
		Objects.requireNonNull(coord);
		Objects.requireNonNull(roomType);
		this.coord = coord;
		this.roomType = roomType;
		this.visit = false;
		this.completed = false;
	}
	//getters
	public Coord coord() {return coord;}
	public RoomType roomType() {return roomType;}
	public boolean visit() {return visit;}
	public void toVisit() {this.visit = true;}
	public boolean isCompleted() {
		return completed;
	}
	public void complete() {
		this.completed = true;
	}
	public boolean shouldTriggerEvent() {
		return !completed;
	}
}