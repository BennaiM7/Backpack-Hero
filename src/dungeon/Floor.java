package dungeon;

import java.util.ArrayList;
import java.util.Collections;

import java.util.List;

import java.util.Objects;

import java.util.Random;
import java.util.random.RandomGenerator;

public class Floor {
	private final int line = 5;
	private final int col = 11;
	private Coord posStart;
	private Room[][] grid;
	private ArrayList<Coord> path;

	public Floor(Coord posStart, Coord exit) {
		Objects.requireNonNull(posStart);
		Objects.requireNonNull(exit);
		this.posStart = posStart;
		this.grid = new Room[line][col];
		this.path = new ArrayList<>();
		for (int i = 0; i < line; i++) {
			for (int j = 0; j < col; j++) {
				grid[i][j] = null;
			}
		}
	}

	public Coord posStart() {
		return posStart;
	}

	public Room room(int line, int col) {
		if (line < 0 || line >= this.line || col < 0 || col >= this.col)
			return null;
		return grid[line][col];
	}

	public Coord availableNext(Coord fluent) {
		Objects.requireNonNull(fluent);
		int temp = path.indexOf(fluent);
		if (temp == -1 || temp >= path.size() - 1)
			return null;
		return path.get(temp + 1);

	}

	private List<Coord> generatePath(Coord start, int depth, RandomGenerator rand) {
		Objects.requireNonNull(start);
		Objects.requireNonNull(rand);
		ArrayList<Coord> path = new ArrayList<Coord>();
		int[] x = { 1, -1, 0, 0 };
		int[] y = { 0, 0, -1, 1 };
		path.add(start);
		for (int i = 0; i < depth; i++) {
			List<Integer> order = new ArrayList<>(List.of(0, 1, 2, 3));
			Coord cur = path.get(path.size() - 1);
			for (int j = 0; j < 4; j++) {
				if (order.isEmpty())
					break;
				int index = rand.nextInt(order.size());
				int direction = order.get(index);
				int l = cur.line() + x[direction];
				int c = cur.col() + y[direction];
				if (l >= 0 && l < line && c >= 0 && c < col) {
					Coord next = new Coord(l, c);
					if (!path.contains(next)) {
						path.add(next);
						break;
					}
				}
				order.remove(index);
			}
		}
		return List.copyOf(path);
	}

	public List<RoomType> shuffleRoomType(int depth, RandomGenerator rand) {
		Objects.requireNonNull(rand);
		var shuffle = new ArrayList<RoomType>();
		int roomCount = depth - 2;
		int enemyCount = Math.max(3, roomCount / 3);
		int corridorCount = roomCount - enemyCount - 3;
		if (corridorCount < 0) {
			enemyCount = Math.max(3, roomCount - 3);
			corridorCount = 0;
		}
		shuffle.add(RoomType.MERCHANT);
		shuffle.add(RoomType.HEALER);
		shuffle.add(RoomType.TREASURE);
		for (int i = 0; i < enemyCount; i++) {
			shuffle.add(RoomType.ENEMIES);
		}
		for (int i = 0; i < corridorCount; i++) {
			shuffle.add(RoomType.CORRIDOR);
		}
		Collections.shuffle(shuffle, rand);
		return List.copyOf(shuffle);
	}

	public Floor generator() {
		RandomGenerator rand = new Random();
		int depth = rand.nextInt(15 - 9 + 1) + 9;
		Coord start = new Coord(rand.nextInt(line), rand.nextInt(col));
		List<Coord> path = generatePath(start, depth, rand);
		Coord exit = path.get(path.size() - 1);
		var floor = new Floor(start, exit);

		var type = shuffleRoomType(path.size(), rand);
		floor.grid[path.get(0).line()][path.get(0).col()] = new Room(path.get(0), RoomType.START);
		floor.grid[path.get(path.size() - 1).line()][path.get(path.size() - 1).col()] = new Room(path.get(path.size() - 1),
				RoomType.EXIT);

		for (int i = 1; i < path.size() - 1; i++) {
			Coord c = path.get(i);
			floor.grid[c.line()][c.col()] = new Room(c, type.get(i - 1));
		}
		floor.path.addAll(path);
		return floor;
	}

}
