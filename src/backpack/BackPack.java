package backpack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import backpack.object.Arrow;
import backpack.object.Curse;
import backpack.object.Equipment;
import backpack.object.Gem;
import backpack.object.GemType;
import backpack.object.Gold;

public class BackPack {
	private static final int MAX_LINE = 5;
	private static final int MAX_COL = 7;
	private int line;
	private int col;
	private final Equipment[][] grid = new Equipment[MAX_LINE][MAX_COL];
	private final boolean[][] unlocked = new boolean[MAX_LINE][MAX_COL];
	private final Map<Equipment, Boolean> rotations = new HashMap<>();

	public BackPack() {
		line = 3;
		col = 3;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				unlocked[i][j] = true;
			}
		}
	}

	public int maxLine() {
		return MAX_LINE;
	}

	public int maxCol() {
		return MAX_COL;
	}


	public Equipment getEquipment(int line, int col) {
		if (line < 0 || line >= this.line) {
			throw new IllegalArgumentException();
		}
		if (col < 0 || col >= this.col) {
			throw new IllegalArgumentException();
		}
		return grid[line][col];
	}

	public int line() {
		return line;
	}

	public int col() {
		return col;
	}

	public boolean getRotation(Equipment item) {
		Objects.requireNonNull(item);
		return rotations.getOrDefault(item, false);
	}

	private boolean rangeMax(int x, int y) {
		return x >= 0 && y >= 0 && x < MAX_LINE && y < MAX_COL;
	}

	public boolean isUnlocked(int x, int y) {
		return rangeMax(x, y) && unlocked[x][y];
	}
	public Arrow findArrowInBackpack() {
		for (int i = 0; i < line; i++) {
			for (int j = 0; j < col; j++) {
				var eq = grid[i][j];
				if (eq != null) {
					return switch (eq) {
						case Arrow arrow -> arrow;
						default -> null;
					};
				}
			}
		}
		return null;
	}
	/**
	 * unlock new slot in the bag
	 *
	 * @param x Position line
	 * @param y Position column
	 */
	public boolean unlockSlot(int x, int y) {
		if (!rangeMax(x, y)) { // position invalide
			return false;
		}
		if (unlocked[x][y]) { // case déjà débloquée
			return false;
		}
		// vérifier qu'au moins une case adjacente est débloquée
		boolean hasAdjacentUnlocked = false;
		if (x > 0 && unlocked[x - 1][y]) { // haut
			hasAdjacentUnlocked = true;
		}
		if (x < MAX_LINE - 1 && unlocked[x + 1][y]) { // bas
			hasAdjacentUnlocked = true;
		}
		if (y > 0 && unlocked[x][y - 1]) { // gauche
			hasAdjacentUnlocked = true;
		}
		if (y < MAX_COL - 1 && unlocked[x][y + 1]) { // droite
			hasAdjacentUnlocked = true;
		}
		if (!hasAdjacentUnlocked) { // aucune case adjacente débloquée
			return false;
		}
		unlocked[x][y] = true;
		updateDimensions();
		return true;
	}

	/**
	 * Update informations maxCol and maxLine allow for the backpack
	 *
	 */
	private void updateDimensions() {
		int maxLine = 0;
		int maxCol = 0;
		for (int i = 0; i < MAX_LINE; i++) {
			for (int j = 0; j < MAX_COL; j++) {
				if (unlocked[i][j]) {
					if (i >= maxLine) {
						maxLine = i + 1;
					}
					if (j >= maxCol) {
						maxCol = j + 1;
					}
				}
			}
		}
		this.line = maxLine;
		this.col = maxCol;
	}
	/**
	 * Give the object gold from the backpack
	 *
	 * @return Gold give the object or null
	 */
	private Gold goldCell() {
	  for (int i = 0; i < line; i++) {
	    for (int j = 0; j < col; j++) {
	      var eq = grid[i][j];
	      if (eq != null) {
	        switch (eq) {
	          case Gold g -> {
	            return g;
	          }
	          default -> {
	          }
	        }
	      }
	    }
	  }
	  return null;
	}
	
	/**
	 * Add gold if present or add gold in empty case
	 *
	 * @param amount amount of gold add
	 */
	public void addGold(int amount) {
		if (amount <= 0)
			throw new IllegalArgumentException();
		var g = goldCell();
		if(g != null) {
			g.add(amount);
			return;
		}
		// on ajoute le gold sur une case vide on en avait pas déjà dasn le sac
		for (int i = 0; i < line; i++) {
			for (int j = 0; j < col; j++) {
				if (grid[i][j] == null) {
					grid[i][j] = new Gold(amount);
					return;
				}
			}
		}
	}

	/**
	 * Give amount of gold present in the bag
	 *
	 */
	public int getGold() {
		var g = goldCell();
		if(g != null) {
			return g.amount();
		}
		return 0;
	}

	/**
	 * Withdraw gold from the bag
	 *
	 * @param amount amount of gold for paying
	 * @return boolean 1 success 0 fail
	 */
	public boolean pay(int amount) {
		if (amount <= 0)
			throw new IllegalArgumentException();
		var g = goldCell();
		if(g != null) {
			return g.paid(amount);
		}
		return false;
	}

	/**
	 * Get all position in the bag from one item
	 *
	 * @param Equipment item that we need to find all is position in the bag
	 * @return List<Position> give all position from the item in a list
	 */
	public List<Position> getPos(Equipment gear) {
		Objects.requireNonNull(gear);
		var temp = new ArrayList<Position>();
		for (int i = 0; i < line; i++) {
			for (int j = 0; j < col; j++) {
				if (grid[i][j] == gear) {
					temp.add(new Position(i, j));
				}
			}
		}
		return List.copyOf(temp);
	}

	/**
	 * checking if a equipment is near a gem to obtain boost if adjacent
	 *
	 * @param Equipment gear receive boost from gem
	 * @param Equipment gem give boost for gear
	 */
	public boolean adjacent(Equipment gear, Equipment gem) {
		Objects.requireNonNull(gear);
		Objects.requireNonNull(gem);
		var posGear = getPos(gear);
		var posGem = getPos(gem);
		for (var i : posGear) {
			if (posGem.contains(new Position(i.line() + 1, i.col())) || posGem.contains(new Position(i.line(), i.col() + 1))
					|| posGem.contains(new Position(i.line() - 1, i.col()))
					|| posGem.contains(new Position(i.line(), i.col() - 1))) {
				return true;
			}
		}
		return false;
	}

	public int bonusDamage(Equipment gear) {
	  Objects.requireNonNull(gear);
	  int bonus = 0;
	  for (var p : getPos(gear)) {
	    bonus += dmgFromGemAt(p.line() + 1, p.col());
	    bonus += dmgFromGemAt(p.line() - 1, p.col());
	    bonus += dmgFromGemAt(p.line(), p.col() + 1);
	    bonus += dmgFromGemAt(p.line(), p.col() - 1);
	  }
	  return bonus;
	}

	private int dmgFromGemAt(int x, int y) {
	  if (!rangeMax(x, y)) return 0;
	  var eq = grid[x][y];
	  if (eq == null) return 0;
	  switch (eq) {
	    case Gem gem -> {
	      if (gem.type() == GemType.WEAPON_DAMAGE) {
	        return gem.bonus();
	      }
	    }
	    default -> {
	    }
	  }
	  return 0;
	}
	
	/**
	 * Give the item in the bag from line and col
	 *
	 * @param int line Position line
	 * @param int col Position column
	 */

	public Equipment detection(int line, int col) {
		if (!rangeMax(line, col)) {
			throw new IllegalArgumentException();
		}
		if (line >= this.line || col >= this.col) {
			return null;
		}
		return grid[line][col];
	}

	/**
	 * rotate item 90 degrees
	 *
	 * @param boolean[][] matrix 2D tab from the shape of the item
	 * @return boolean[][] give the new shape of the item
	 */
	public static boolean[][] rotation90(boolean[][] matrix) {
		if (matrix == null || matrix.length == 0 || matrix[0].length == 0) {
			throw new IllegalArgumentException();
		}
		int rows = matrix.length;
		int cols = matrix[0].length;

		boolean[][] rotated = new boolean[cols][rows];

		for (int i = 0; i < rows; i++) {
			if (matrix[i].length != cols) {
				throw new IllegalArgumentException();
			}
			for (int j = 0; j < cols; j++) {
				rotated[j][rows - 1 - i] = matrix[i][j];
			}
		}

		return rotated;
	}

	/**
	 * Sets up item rotation
	 *
	 * @param Equipment gear item that we need to rotate
	 * @param boolean   rotation give information if we rotate or not the item
	 * @param int       x Position line
	 * @param int       y Position column
	 * @return boolean[][] give the new shape of the item
	 */
	public boolean[][] setRotation(Equipment gear, boolean rotation, int x, int y) {
		Objects.requireNonNull(gear);
		if (gear.getClass() == Curse.class) {// bloquer la rotation d'un curse
			rotation = false;
		}
		boolean[][] shape = gear.getShape();
		if (shape == null || shape.length == 0 || shape[0].length == 0) {
			throw new IllegalArgumentException();
		}
		if (rotation) {
			shape = rotation90(shape);
		}
		int shapeRows = shape.length;
		int shapeCols = shape[0].length;
		if (x < 0 || y < 0) {
			throw new IllegalArgumentException();
		}
		if (x + shapeRows > MAX_LINE || y + shapeCols > MAX_COL) {
			throw new IllegalArgumentException();
		}
		return shape;
	}

	/**
	 * Checking if we can put item in the bag
	 *
	 * @param Equipment gear item that we need to place in the bag
	 * @param boolean   rotation give information if we rotate or not the item
	 * @param int       x Position line
	 * @param int       y Position column
	 * @return boolean success of fail
	 */
	public boolean canPlace(Equipment gear, int x, int y, boolean rotation) {
		Objects.requireNonNull(gear);
		boolean[][] shape;
		try {
			shape = setRotation(gear, rotation, x, y);
		} catch (IllegalArgumentException e) {
			return false;
		}
		int shapeRows = shape.length;
		int shapeCols = shape[0].length;

		for (int i = 0; i < shapeRows; i++) {
			for (int j = 0; j < shapeCols; j++) {
				if (grid[x + i][y + j] != null && shape[i][j]) {
					return false; // déjà un équipement ici
				}
				if (!isUnlocked(x + i, y + j)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Place item in the bag
	 *
	 * @param Equipment gear item that we need to rotate
	 * @param boolean   rotation give information if we rotate or not the item
	 * @param int       x Position line
	 * @param int       y Position column
	 */
	public void place(Equipment gear, int x, int y, boolean rotation) {
		Objects.requireNonNull(gear);
		var shape = setRotation(gear, rotation, x, y);
		if (canPlace(gear, x, y, rotation) == false || shape == null) {
			throw new IllegalArgumentException();
		}
		int shapeRows = shape.length;
		int shapeCols = shape[0].length;

		for (int i = 0; i < shapeRows; i++) {
			for (int j = 0; j < shapeCols; j++) {
				if (grid[x + i][y + j] == null && shape[i][j] == true) {
					grid[x + i][y + j] = gear;

				}
			}
		}
		rotations.put(gear, rotation);
	}

	/**
	 * Place Curse in the bags
	 *
	 * @param Curse curse The curse that we need to place
	 * @param int   x Position line
	 * @param int   y Position column
	 * @return boolean success or fail
	 */
	public boolean placeCurse(Curse curse, int x, int y) {
		Objects.requireNonNull(curse);
		boolean[][] shape = curse.shape();
		int shapeRows = shape.length;
		int shapeCols = shape[0].length;
		if (x + shapeRows > MAX_LINE || y + shapeCols > MAX_COL) {
			return false;
		}
		for (int i = 0; i < shapeRows; i++) { // les verifs si on peut placer la malediction
			for (int j = 0; j < shapeCols; j++) {
				if (shape[i][j]) {
					if (!isUnlocked(x + i, y + j)) { // si les cases sont débloqués ou non
						return false;
					}
					if (detection(x + i, y + j) != null) { // si il y a d'autres items dans la position
						return false;
					}
				}
			}
		}
		for (int i = 0; i < shapeRows; i++) { // on place la malédiction
			for (int j = 0; j < shapeCols; j++) {
				if (shape[i][j]) {
					grid[x + i][y + j] = curse;
				}
			}
		}
		rotations.put(curse, false); // ne peuvent pas être rotate
		return true;
	}

	/**
	 * withdraw item from the bag
	 *
	 * @param int x Position line
	 * @param int y Position column
	 */
	public void withdraw(int x, int y) {
		var temp = detection(x, y);
		if (temp == null) {
			throw new IllegalArgumentException();
		}
		for (int i = 0; i < line; i++) {
			for (int j = 0; j < col; j++) {
				if (grid[i][j] == temp) {
					grid[i][j] = null;
				}
			}
		}
		rotations.remove(temp);
	}

	/**
	 * move item in the bag
	 *
	 * @param Position oldPlace old position from the item
	 * @param Position newPlcae new position from the item
	 * @param boolean  rotation rotation or not
	 * @return boolean success or fail
	 */
	public boolean move(Position oldPlace, Position newPlace, boolean rotation) {
		Objects.requireNonNull(oldPlace);
		Objects.requireNonNull(newPlace);
		Equipment temp = detection(oldPlace.line(), oldPlace.col());
		if (temp == null) {
			throw new IllegalArgumentException();
		}
		boolean oldRotation = rotations.getOrDefault(temp, false);
		withdraw(oldPlace.line(), oldPlace.col());

		if (canPlace(temp, newPlace.line(), newPlace.col(), rotation)) {
			place(temp, newPlace.line(), newPlace.col(), rotation);
		} else {
			place(temp, oldPlace.line(), oldPlace.col(), oldRotation);
			throw new IllegalArgumentException();
		}
		return true;
	}
}
