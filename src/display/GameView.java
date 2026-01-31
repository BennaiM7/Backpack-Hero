package display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.github.forax.zen.ApplicationContext;

import actions.Action;
import backpack.BackPack;
import backpack.Position;
import backpack.object.Armor;
import backpack.object.Arrow;
import backpack.object.Consumable;
import backpack.object.Curse;
import backpack.object.Equipment;
import backpack.object.Gear;
import backpack.object.Gem;
import backpack.object.Gold;
import backpack.object.ManaStone;
import dungeon.Coord;
import dungeon.Floor;
import dungeon.Room;

/**
 * GameView class manages the display of the backpack hero game. Adapts to
 * screen size and handles rendering of game elements.
 */
public class GameView {
	private final int xOrigin;
	private final int yOrigin;
	private final int width;
	private final int height;
	private final ImageLoader loader;
	private final int bHeight;
	private final int iHeight;
	private static final int MERCHANT_SLOT_W = 180;
	private static final int MERCHANT_SLOT_H = 100;
	private static final int MERCHANT_GAP = 15;
	private static final int MERCHANT_COLS = 2;

	/**
	 * Creates a GameView that fills the entire screen.
	 * 
	 * @param screenWidth  Width of the screen
	 * @param screenHeight Height of the screen
	 * @param loader       ImageLoader for game images
	 */
	public GameView(int screenWidth, int screenHeight, ImageLoader loader) {
		Objects.requireNonNull(loader);
		this.xOrigin = 0;
		this.yOrigin = 0;
		this.width = screenWidth;
		this.height = screenHeight;
		this.loader = loader;
		bHeight = (int) (height * 0.40);// zone d'affichage du backpack 40%
		iHeight = (int) (height * 0.60);// zone d'affichage interaction 60%
	}

	/**
	 * Displays an image in a given part of the display area.
	 * 
	 * @param graphics Graphics engine that will display the image
	 * @param image    Image to be displayed
	 * @param x        Base x coordinate
	 * @param y        Base y coordinate
	 * @param dimX     Width of the display area
	 * @param dimY     Height of the display area
	 */
	private void drawImage(Graphics2D graphics, BufferedImage image, float x, float y, float dimX, float dimY) {
		Objects.requireNonNull(graphics);
		Objects.requireNonNull(image);
		var imageWidth = image.getWidth();
		var imageHeight = image.getHeight();
		var scale = Math.min(dimX / imageWidth, dimY / imageHeight);
		var transform = new AffineTransform(scale, 0, 0, scale, x + (dimX - scale * imageWidth) / 2,
				y + (dimY - scale * imageHeight) / 2);
		graphics.drawImage(image, transform, null);
	}

	/**
	 * Draws the game board using the Graphics2D object.
	 * 
	 * @param graphics Graphics engine
	 * @param data     Game data to display
	 */
	/**
	 * Draw the main game view, including backpack, dungeon, and overlays.
	 */
	private void draw(Graphics2D graphics, GameData data) {
		Objects.requireNonNull(graphics);
		Objects.requireNonNull(data);

		graphics.setColor(Color.BLACK);
		graphics.fill(new Rectangle2D.Float(xOrigin, yOrigin, width, height));
		if (data.isVictory()) {
			drawEndScreen(graphics, data, "Victoire");
			return;
		}
		if (data.isGameOver()) {
			drawEndScreen(graphics, data, "Game Over");
			return;
		}
		if (data.showDungeon()) {
			drawDungeon(graphics, data);
		} else {
			drawBackPack(graphics, data);
			drawInteraction(graphics, data);
		}
		if (data.isDragging() && data.mousePosition() != null) {
			drawDraggingItem(graphics, data);
		}
	}

	/**
	 * Calculate the cell size to fit the backpack grid on screen.
	 */
	private float calculateCellSize(BackPack backpack) {
		return Math.min((float) width / backpack.maxCol(), (float) bHeight / backpack.maxLine());
	}

	/**
	 * Calculates backpack horizontal offset for centering.
	 */
	private float calculateOffsetX(BackPack backpack, float cellSize) {
		float backpackWidth = cellSize * backpack.maxCol();
		return xOrigin + (width - backpackWidth) / 2;
	}

	/**
	 * Calculates backpack vertical offset for centering.
	 */
	private float calculateOffsetY(BackPack backpack, float cellSize) {
		float backpackHeight = cellSize * backpack.maxLine();
		return yOrigin + (bHeight - backpackHeight) / 2;
	}

	/**
	 * Draw the item currently being dragged by the mouse.
	 */
	private void drawDraggingItem(Graphics2D graphics, GameData data) {
		Equipment item = data.draggingItem();
		Position mousePos = data.mousePosition();
		// Récupère la forme avec rotation si nécessaire
		boolean[][] shape = item.getShape();
		if (data.originalRotation()) {
			shape = backpack.BackPack.rotation90(shape);
		}

		int shapeRows = shape.length;
		int shapeCols = shape[0].length;
		var backPack = data.backpack();
		float cellSize = calculateCellSize(backPack);
		// Centrer l'item sur la souris
		float itemX = mousePos.line() - (shapeCols * cellSize) / 2;
		float itemY = mousePos.col() - (shapeRows * cellSize) / 2;
		for (int i = 0; i < shapeRows; i++) {
			for (int j = 0; j < shapeCols; j++) {
				if (shape[i][j]) {
					float x = itemX + j * cellSize;
					float y = itemY + i * cellSize;
					// on va dessiner l'image
					String imageKey = getItemImageKey(item);
					BufferedImage image = (imageKey == null) ? null : loader.image(imageKey);

					if (image != null) {
						drawImage(graphics, image, x + 2, y + 2, cellSize - 4, cellSize - 4);
					}
					graphics.setColor(new Color(255, 255, 255, 180));
					graphics.draw(new Rectangle2D.Float(x, y, cellSize, cellSize));
				}
			}
		}
	}

	/**
	 * Check if clicking on the treasure item display
	 */
	public boolean isClickingOnTreasureItem(Position location, GameData data) {
		Objects.requireNonNull(location);
		Objects.requireNonNull(data);
		Gear item = data.pendingItem();
		if (item == null)
			return false;

		boolean[][] shape = item.shape();
		int shapeRow = shape.length;
		int shapeCol = shape[0].length;

		float cellSize = 80;
		float itemWidth = shapeCol * cellSize;
		float itemHeight = shapeRow * cellSize;
		float itemX = width / 2 - itemWidth / 2;
		float itemY = yOrigin + bHeight + 100;

		return location.line() >= itemX && location.line() <= itemX + itemWidth && location.col() >= itemY
				&& location.col() <= itemY + itemHeight;
	}

	private void drawButtonLeaveMerchant(Graphics2D g, int buttonY) {
		int w = 140, h = 50;
		int x = width / 2 - w / 2;
		int y = buttonY + 100;
		drawButton(g, x, y, w, h, new Color(200, 60, 60), "Leave");
	}

	public boolean isClickingMerchantLeave(Position screenPos) {
		Objects.requireNonNull(screenPos);
		int w = 140;
		int h = 50;
		int x = width / 2 - w / 2;
		int buttonY = yOrigin + bHeight + 400 + 100;
		return isInRect(screenPos, x, buttonY, w, h);
	}

	private void drawMerchant(Graphics2D g, GameData data) {
		var items = data.merchantSlot();
		for (int i = 0; i < items.size(); i++) {
			var p = items.get(i);
			if (p == null)
				continue;
			Rectangle rect = merchantSlotRect(i);
			g.setColor(Color.BLACK);
			g.drawRect(rect.x, rect.y, rect.width, rect.height);
			switch (p) {
			case Gear gear -> drawGear(g, rect, gear);
			case Gem gem -> drawGem(g, rect, gem);
			case ManaStone m -> drawManaStone(g, rect, m);
			case Consumable c -> drawConsumable(g, rect, c);
			case Armor a -> drawArmor(g, rect, a);
			case Arrow arrow -> drawArrow(g,rect,arrow);
			default -> {
			}
			}
		}
		drawButtonLeaveMerchant(g, yOrigin + bHeight + 400);
	}

	private void drawCombatRewards(Graphics2D g, GameData data) {
		var items = data.combatRewards();

		for (int i = 0; i < items.size(); i++) {
			var p = items.get(i);
			if (p == null)
				continue;
			Rectangle rect = merchantSlotRect(i); // réutilise le même format que le marchand
			g.setColor(Color.WHITE);
			g.drawRect(rect.x, rect.y, rect.width, rect.height);

			switch (p) {
			case Gear gear -> drawGear(g, rect, gear);
			case Gem gem -> drawGem(g, rect, gem);
			case ManaStone ms -> drawManaStone(g, rect, ms);
			case Consumable con -> drawConsumable(g, rect, con);
			case Armor armor -> drawArmor(g, rect, armor);
			case Arrow arrow -> drawArrow(g,rect,arrow);
			default -> {}
			}
		}
		drawButtonLeaveRewards(g, yOrigin + bHeight + 400);
	}

	/**
	 * Draws a simple button.
	 */
	private void drawButton(Graphics2D g, int x, int y, int w, int h, Color bgColor, String text) {
		g.setColor(bgColor);
		g.fillRect(x, y, w, h);
		g.setColor(Color.WHITE);
		g.drawRect(x, y, w, h);
		// Centre le texte
		var metrics = g.getFontMetrics();
		int textWidth = metrics.stringWidth(text);
		int textHeight = metrics.getAscent();
		g.setColor(Color.BLACK);
		g.drawString(text, x + (w - textWidth) / 2, y + (h + textHeight) / 2 - 2);
	}

	private void drawButtonLeaveRewards(Graphics2D g, int y) {
		int x = (int) ((width - 200) / 2.0);
		int w = 200;
		int h = 50;
		drawButton(g, x, y, w, h, Color.WHITE, "Continuer");
	}

	/**
	 * Draw armor stats in a rectangle.
	 */
	private void drawArmor(Graphics2D g, Rectangle rect, Armor armor) {
		int x = rect.x + 10;
		int y = rect.y + 18;
		g.setColor(Color.WHITE);
		g.drawString(armor.name(), x, y);
		y += 16;
		g.drawString("Protection : " + armor.protection(), x, y);
		y += 16;
		g.drawString("rarity : " + armor.rarity(), x, y);

	}

	/**
	 * Draw gear stats in a rectangle.
	 */
	private void drawGear(Graphics2D g, Rectangle rect, Gear gear) {
		int x = rect.x + 10;
		int y = rect.y + 18;

		g.setColor(Color.WHITE);
		g.drawString(gear.name(), x, y);

		y += 16;
		if (gear.typeW() != null) {
			g.drawString("Dégats : " + gear.basicDamage(), x, y);
			y += 16;
			g.drawString("Type d'items: " + gear.typeW(), x, y);
			y += 16;
		} else {
			g.drawString("Protection : " + gear.protectionBasic(), x, y);
			y += 16;
			g.drawString("Type d'items: " + gear.typeS(), x, y);
			y += 16;
		}
		g.drawString("Coût d'énergie: " + gear.cost(), x, y);
		y += 16;
		g.drawString("Rareté: " + gear.rarity(), x, y);
	}

	/**
	 * Draw gem stats in a rectangle.
	 */
	private void drawGem(Graphics2D g, Rectangle rect, Gem gem) {
		drawTitle(g, rect, gem.name());
		int y = rect.y + 38;
		drawLine(g, rect.x + 10, y, "Type: " + gem.type());
		y += 16;
		drawLine(g, rect.x + 10, y, "Bonus: +" + gem.bonus());
	}

	/**
	 * Draw mana stone stats in a rectangle.
	 */
	private void drawManaStone(Graphics2D g, Rectangle rect, ManaStone ms) {
		drawTitle(g, rect, ms.name());

		int y = rect.y + 38;
		drawLine(g, rect.x + 10, y, "Type: " + ms.type());
		y += 16;
		drawLine(g, rect.x + 10, y, "Rarity: " + ms.rarity());
		
	}
	
	/**
	 * Draw arrow stats in a rectangle.
	 */
	private void drawArrow(Graphics2D g, Rectangle rect, Arrow ms) {
		drawTitle(g, rect, ms.name());
		int y = rect.y + 38;
		drawLine(g, rect.x + 10, y, "Damage: " + ms.damage());
		y += 16;
		drawLine(g, rect.x + 10, y, "Rarity: " + ms.rarity());
		
	}

	/**
	 * Draw consumable stats in a rectangle.
	 */
	private void drawConsumable(Graphics2D g, Rectangle rect, Consumable c) {
		int x = rect.x + 10;
		int y = rect.y + 18;
		g.setColor(Color.WHITE);
		g.drawString(c.name(), x, y);
		y += 16;
		g.drawString("Effect: " + c.type(), x, y);
		y += 16;
		g.drawString("Value: +" + c.amount(), x, y);
		y += 16;
		g.drawString("Rarity: " + c.rarity(), x, y);
	}

	/**
	 * Draw a title string in a rectangle.
	 */
	private void drawTitle(Graphics2D g, Rectangle rect, String title) {
		g.setColor(Color.WHITE);
		g.drawString(title, rect.x + 10, rect.y + 18);
	}

	/**
	 * Draw a single line of text at a position.
	 */
	private void drawLine(Graphics2D g, int x, int y, String text) {
		g.setColor(Color.WHITE);
		g.drawString(text, x, y);
	}

	/**
	 * Get the rectangle for a merchant slot by index.
	 */
	private Rectangle merchantSlotRect(int index) {
		int iY = yOrigin + bHeight;
		int baseX = xOrigin + width / 3 + 75;
		int baseY = iY + 100;
		int r = index / MERCHANT_COLS;
		int c = index % MERCHANT_COLS;
		int x = baseX + c * (MERCHANT_SLOT_W + MERCHANT_GAP);
		int y = baseY + r * (MERCHANT_SLOT_H + MERCHANT_GAP);

		return new Rectangle(x, y, MERCHANT_SLOT_W, MERCHANT_SLOT_H);
	}

	/** Retourne l'index du slot cliqué, sinon -1 */
	public int screenToMerchantIndex(Position screenPos, int slotCount) {
		Objects.requireNonNull(screenPos);
		float sx = screenPos.line();
		float sy = screenPos.col();
		for (int i = 0; i < slotCount; i++) {
			Rectangle rect = merchantSlotRect(i);
			if (rect.contains(sx, sy)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Tests if a position is inside a rectangle.
	 */
	private boolean isInRect(Position pos, int x, int y, int width, int height) {
		float px = pos.line();
		float py = pos.col();
		return px >= x && px <= x + width && py >= y && py <= y + height;
	}

	/** Retourne l'item cliqué, sinon null */
	public Equipment screenToMerchantItem(Position screenPos, List<Equipment> items) {
		Objects.requireNonNull(screenPos);
		int i = screenToMerchantIndex(screenPos, items.size());
		return (i == -1) ? null : items.get(i);
	}

	public int screenToRewardIndex(Position screenPos, int slotCount) {
		Objects.requireNonNull(screenPos);
		return screenToMerchantIndex(screenPos, slotCount); // même format
	}

	public boolean isClickingRewardLeave(Position screenPos) {
		Objects.requireNonNull(screenPos);
		int x = (int) ((width - 200) / 2.0);
		int w = 200;
		int h = 50;
		int buttonY = yOrigin + bHeight + 400;
		return isInRect(screenPos, x, buttonY, w, h);
	}

	private Rectangle pendingCurseRect(GameData data) {
		var curse = data.pendingCurse();
		if (curse == null) {
			return new Rectangle(0, 0, 0, 0);
		}
		int baseY = yOrigin + bHeight;
		boolean[][] shape = curse.getShape();
		int rows = shape.length;
		int cols = shape[0].length;
		int cellSize = 80;
		int w = cols * cellSize;
		int h = rows * cellSize;
		int x = width / 2 - w / 2;
		int y = baseY + 200;
		return new Rectangle(x, y, w, h);
	}

	/**
	 * Draw the pending curse overlay and its buttons.
	 */
	private void drawPendingCurse(Graphics2D g, GameData data) {
		var curse = data.pendingCurse();
		int baseY = yOrigin + bHeight;
		g.setColor(Color.WHITE);
		g.drawString("Vous pouvez accepter ou refuser votre malédiction mais vous avez refusez "
				+ (data.curseRefusalCount() + 1) + " fois donc vous prendrez " + (data.curseRefusalCount() + 1) + " dégâts",
				width / 2 - 250, baseY + 50);
		// texte malus
		g.drawString("Effet: " + curse.malusType() + "  (" + curse.malus() + ")", width / 2 - 90, baseY + 70);
		drawCurseButtons(g, baseY);
		// dessin de la shape
		Rectangle rect = pendingCurseRect(data);
		boolean[][] shape = curse.getShape();
		int cellSize = rect.width / shape[0].length;
		for (int r = 0; r < shape.length; r++) {
			for (int c = 0; c < shape[r].length; c++) {
				if (!shape[r][c])
					continue;
				int x = rect.x + c * cellSize;
				int y = rect.y + r * cellSize;
				// image curse
				BufferedImage img = loader.image("curse");
				if (img != null) {
					drawImage(g, img, x + 2, y + 2, cellSize - 4, cellSize - 4);
				}
				g.setColor(new Color(180, 180, 180));
				g.drawRect(x, y, cellSize, cellSize);
			}
		}
		g.setColor(new Color(255, 255, 255, 120));
		g.drawRect(rect.x, rect.y, rect.width, rect.height);
	}

	private void drawCurseButtons(Graphics2D g, int baseY) {
		int y = baseY + 160;
		drawButton(g, width / 2 - 160, y, 140, 50, new Color(60, 200, 60), "Accepter");
		drawButton(g, width / 2 + 20, y, 140, 50, new Color(200, 60, 60), "Refuser");
	}

	public boolean isClickingCurseAccept(Position screenPos) {
		Objects.requireNonNull(screenPos);
		int baseY = yOrigin + bHeight;
		return isInRect(screenPos, width / 2 - 160, baseY + 160, 140, 50);
	}

	public boolean isClickingCurseRefuse(Position screenPos) {
		Objects.requireNonNull(screenPos);
		return isInRect(screenPos, width / 2 + 20, yOrigin + bHeight + 160, 140, 50);
	}

	public boolean screenToCurse(Position screenPos, GameData data) {
		Objects.requireNonNull(screenPos);
		Objects.requireNonNull(data);
		if (!data.hasPendingCurse())
			return false;
		var r = pendingCurseRect(data);
		return r.contains(screenPos.line(), screenPos.col());
	}

	/**
	 * Draw the dungeon map and all rooms.
	 */
	private void drawDungeon(Graphics2D graphics, GameData data) {
		Floor floor = data.floor();
		int dungeonLines = 5;
		int dungeonCols = 11;
		float cellWidth = (float) width / dungeonCols;
		float cellHeight = (float) height / dungeonLines;
		// Dessiner chaque case du donjon
		for (int i = 0; i < dungeonLines; i++) {
			for (int j = 0; j < dungeonCols; j++) {
				Room room = floor.room(i, j);
				float x = xOrigin + j * cellWidth;
				float y = yOrigin + i * cellHeight;
				if (room == null) {
					// Case vide
					graphics.setColor(Color.BLACK);
					graphics.fill(new Rectangle2D.Float(x, y, cellWidth, cellHeight));
				} else {
					// Dessiner la salle
					drawDungeonRoom(graphics, room, x, y, cellWidth, cellHeight, data);
				}
				// Grille pour mieux voir
				graphics.setColor(new Color(50, 50, 50));
				graphics.draw(new Rectangle2D.Float(x, y, cellWidth, cellHeight));
			}
		}

	}

	/**
	 * Draw instructions for unlocking slots in the backpack.
	 */
	private void drawUnlockInstructions(Graphics2D graphics, GameData data) {
		int remaining = data.slotsToUnlock();
		graphics.setColor(new Color(255, 255, 255, 230));
		String text = "Sélectionnez " + remaining + " cases à débloquer";
		int textWidth = graphics.getFontMetrics().stringWidth(text);
		graphics.drawString(text, width / 2 - textWidth / 2, yOrigin + bHeight - 15);
		if (remaining == 0) {
			int buttonX = width / 2 - 60;
			int buttonY = yOrigin + bHeight - 80;
			graphics.setColor(Color.WHITE);
			graphics.drawString("Confirmer", buttonX + 28, buttonY + 20);
		}
	}

	/**
	 * Print all item images in the backpack.
	 */
	private void printImage(Graphics2D graphics, GameData data, float cellSize, float offsetX, float offsetY) {
		var imageList = getAllItem(data);
		for (var item : imageList) {
			getBound(graphics, item, data, cellSize, offsetX, offsetY);
		}
	}

	private void drawImageAutoRotate(Graphics2D g, BufferedImage img, float x, float y, float w, float h) {
		int iw = img.getWidth();
		int ih = img.getHeight();
		boolean boxVertical = h > w;
		boolean imgHorizontal = iw > ih;
		// On tourne si le rectangle est vertical alors que l'image est horizontale (cas
		// épées 3x1 tournées)
		boolean rotate90 = boxVertical && imgHorizontal;
		if (!rotate90) {
			drawImage(g, img, x, y, w, h);
			return;
		}
		var old = g.getTransform();
		float cx = x + w / 2f;
		float cy = y + h / 2f;
		float scale = Math.min(w / ih, h / iw);
		var at = new AffineTransform();
		at.translate(cx, cy);
		at.rotate(Math.PI / 2);
		at.scale(scale, scale);
		at.translate(-iw / 2f, -ih / 2f);
		g.drawImage(img, at, null);
		g.setTransform(old);
	}

	private List<Equipment> getAllItem(GameData data) {
		var temp = new ArrayList<Equipment>();
		var backpack = data.backpack();
		var line = backpack.line();
		var col = backpack.col();
		for (int i = 0; i < line; i++) {
			for (int j = 0; j < col; j++) {
				var eq = backpack.getEquipment(i, j);
				if (eq == null || temp.contains(eq)) {
					continue;
				}
				temp.add(eq);
			}
		}
		return List.copyOf(temp);
	}

	private void getBound(Graphics2D graphics, Equipment item, GameData data, float cellSize, float offsetX,
			float offsetY) {
		var pos = data.backpack().getPos(item);
		if (pos.isEmpty()) {
			return;
		}
		var first = pos.get(0);
		var minLine = first.line();
		var minCol = first.col();
		var maxCol = first.col();
		var maxLine = first.line();
		for (var p : pos) {
			if (p.line() < minLine)
				minLine = p.line();
			if (p.line() > maxLine)
				maxLine = p.line();
			if (p.col() < minCol)
				minCol = p.col();
			if (p.col() > maxCol)
				maxCol = p.col();
		}
		float x = offsetX + minCol * cellSize;
		float y = offsetY + minLine * cellSize;
		float w = (maxCol - minCol + 1) * cellSize;
		float h = (maxLine - minLine + 1) * cellSize;
		drawItemImage(graphics, item, x, y, w, h, data);
	}

	private String getItemImageKey(Equipment item) {
		if (item == null) {
			return null;
		}
		String temp;
		switch (item) {
		case Gold _ -> temp = "gold";
		case Curse curse -> temp = curse.name();
		case Gear gear -> temp = gear.name();
		case Gem gem -> temp = gem.name();
		case ManaStone ms -> temp = ms.name();
		case Consumable v -> temp = v.name();
		case Armor armor -> temp = armor.name();
		case Arrow arrow -> temp = arrow.name();
		default -> {
			return null;
		}
		}
		;
		return temp.toLowerCase().replace(" ", "_").replace("'", "");
	}

	/**
	 * Draw an item's image and extra info (like gold amount).
	 */
	private void drawItemImage(Graphics2D graphics, Equipment item, float x, float y, float width, float height,
			  GameData data) {
		String imageKey = getItemImageKey(item);
		BufferedImage image = loader.image(imageKey);
		if (image != null) {
			drawImageAutoRotate(graphics, image, x, y, width, height);
		}
		switch (item) {
		case Gold gold -> {
			graphics.setColor(Color.WHITE);
			graphics.setFont(graphics.getFont().deriveFont(14f));
			String amount = String.valueOf(gold.amount());
			var metrics = graphics.getFontMetrics();
			int textWidth = metrics.stringWidth(amount);
			graphics.drawString(amount, x + width - textWidth - 3, y + height - 3);
		}
		default -> {
		}
		}
	}

	/**
	 * Draws a single room in the dungeon.
	 */
	private void drawDungeonRoom(Graphics2D graphics, Room room, float x, float y, float width, float height,
			GameData data) {
		// Récupérer l'image correspondant au type de salle
		String imageKey = getRoomImageKey(room);
		BufferedImage image = loader.image(imageKey);
		if (image != null) {
			// Dessiner l'image de la salle
			drawImage(graphics, image, x, y, width, height);
		}
		// Marquer la position du héros
		Coord heroPos = data.hero().pos();
		if (room.coord().equals(heroPos)) {
			// Dessiner un cercle pour le héros
			graphics.setColor(new Color(0, 255, 0));
			float heroSize = Math.min(width, height) * 0.3f;
			float heroX = x + (width - heroSize) / 2;
			float heroY = y + (height - heroSize) / 2;
			graphics.fillOval((int) heroX, (int) heroY, (int) heroSize, (int) heroSize);
		}
	}

	/**
	 * Gets the image key for a room type.
	 */
	private String getRoomImageKey(Room room) {
		return switch (room.roomType()) {
		case CORRIDOR -> "corridor";
		case ENEMIES -> "enemies";
		case TREASURE -> "treasure";
		case MERCHANT -> "merchant";
		case HEALER -> "healer";
		case START -> "start";
		case EXIT -> "exit";
		};
	}

	/**
	 * Draw the backpack grid, items, and unlock instructions.
	 */
	private void drawBackPack(Graphics2D graphics, GameData data) {
		var backPack = data.backpack();
		float cellSize = calculateCellSize(backPack);
		float offsetX = calculateOffsetX(backPack, cellSize);
		float offsetY = calculateOffsetY(backPack, cellSize);
		drawBackpackBackground(graphics, backPack, cellSize, offsetX, offsetY);
		drawBackpackCells(graphics, data, backPack, cellSize, offsetX, offsetY);
		printImage(graphics, data, cellSize, offsetX, offsetY);
		if (data.isUnlockingSlots()) {
			drawUnlockInstructions(graphics, data);
		}
	}

	private void drawBackpackBackground(Graphics2D g, BackPack backPack, float cellSize, float offsetX, float offsetY) {
		int maxCols = backPack.maxCol();
		int maxLines = backPack.maxLine();
		float backpackWidth = cellSize * maxCols;
		float backpackHeight = cellSize * maxLines;
		g.setColor(new Color(20, 15, 10));
		g.fill(new Rectangle2D.Float(offsetX, offsetY, backpackWidth, backpackHeight));
	}

	private void drawBackpackCells(Graphics2D g, GameData data, BackPack backPack, float cellSize, float offsetX, float offsetY) {
		int maxLines = backPack.maxLine();
		int maxCols = backPack.maxCol();
		for (int i = 0; i < maxLines; i++) {
			for (int j = 0; j < maxCols; j++) {
				float x = offsetX + j * cellSize;
				float y = offsetY + i * cellSize;
				if (backPack.isUnlocked(i, j)) {
					g.setColor(new Color(60, 45, 30));
					g.fill(new Rectangle2D.Float(x, y, cellSize, cellSize));
					g.setColor(new Color(100, 90, 80));
					g.draw(new Rectangle2D.Float(x, y, cellSize, cellSize));
				} else {
					if (data.isUnlockingSlots()) {
						Position pos = new Position(i, j);
						g.setColor(data.selectedSlots().contains(pos) ? new Color(80, 80, 80) : new Color(25, 25, 25));
					} else {
						g.setColor(new Color(25, 25, 25));
					}
					g.fill(new Rectangle2D.Float(x, y, cellSize, cellSize));
					g.setColor(new Color(100, 90, 80));
					g.draw(new Rectangle2D.Float(x, y, cellSize, cellSize));
				}
			}
		}
	}

	/**
	 * Draw the interaction area (combat, merchant, healer, rewards, etc).
	 */
	private void drawInteraction(Graphics2D graphics, GameData data) {
		graphics.setColor(new Color(20, 20, 40));
		graphics.fill(new Rectangle2D.Float(xOrigin, yOrigin + bHeight, width, iHeight));
		if (data.hasPendingCurse()) {
			drawPendingCurse(graphics, data);
			return;
		}
		if (data.hasPendingItem()) {
			drawTreasureItem(graphics, data);
		} else if (data.inFight()) {
			drawCombat(graphics, data);
		} else if (!data.combatRewards().isEmpty()) {
			drawCombatRewards(graphics, data);
		} else if (data.inHealer()) {
			drawHealer(graphics, data);
		} else if (data.inMerchant()) {
			drawMerchant(graphics, data);
		}
	}

	/**
	 * Draw the treasure item display in the interaction area.
	 */
	private void drawTreasureItem(Graphics2D graphics, GameData data) {
		Gear item = data.pendingItem();
		int baseY = yOrigin + bHeight;
		drawTreasureItemShape(graphics, item, baseY + 100);
		drawTreasureStats(graphics, item, baseY + 100);
	}

	/**
	 * Draw the shape and image of the treasure item.
	 */
	private void drawTreasureItemShape(Graphics2D graphics, Gear item, int startY) {
		boolean[][] shape = item.shape();
		float cellSize = 80;
		float itemX = width / 2 - (shape[0].length * cellSize) / 2;
		for (int i = 0; i < shape.length; i++) {
			for (int j = 0; j < shape[0].length; j++) {
				if (shape[i][j]) {
					float x = itemX + j * cellSize;
					float y = startY + i * cellSize;
					BufferedImage image = loader.image(getItemImageKey(item));
					if (image != null) {
						drawImage(graphics, image, x + 2, y + 2, cellSize - 4, cellSize - 4);
					}
					graphics.setColor(new Color(100, 100, 100));
					graphics.draw(new Rectangle2D.Float(x, y, cellSize, cellSize));
				}
			}
		}
	}

	/**
	 * Draw the stats of the treasure item.
	 */
	private void drawTreasureStats(Graphics2D graphics, Gear item, int startY) {
		float statsY = startY + item.shape().length * 80 + 20;
		graphics.setColor(Color.WHITE);
		graphics.drawString(item.name(), width / 2 - 50, (int) statsY);
		graphics.drawString("Dégâts: " + item.basicDamage(), width / 2 - 50, (int) statsY + 20);
		graphics.drawString("Coût: " + item.cost() + " énergie", width / 2 - 50, (int) statsY + 40);
	}

	public void drawHealer(Graphics2D graphics, GameData data) {
		Objects.requireNonNull(graphics);
		Objects.requireNonNull(data);
		int xcenter = width / 2;
		int ycenter = yOrigin + bHeight + (iHeight / 2);
		int w = 120, h = 50, spacing = 10;
		// Stats du héros
		graphics.setColor(Color.WHITE);
		String hpText = data.hero().getCurrentHp() + "/" + data.hero().getMaxHp();
		graphics.drawString(hpText, xcenter - 150, ycenter + 100);
		// Boutons
		int x = xcenter - w / 2;
		int y = ycenter - 70;
		drawButton(graphics, x, y, w, h, Color.GREEN, "Heal 25 HP : 4 or");
		drawButton(graphics, x, y + h + spacing, w, h, new Color(100, 200, 255), "+5 Max HP : 10 or");
		drawButton(graphics, x, y + 2 * (h + spacing), w, h, Color.RED, "Leave");
	}

	/**
	 * Draw the combat interface: hero, enemies, actions, and end turn button.
	 */
	private void drawCombat(Graphics2D graphics, GameData data) {
		int combatY = yOrigin + bHeight;
		int combatHeight = iHeight;
		drawCombatHero(graphics, data, combatY, combatHeight);
		drawCombatEnemy(graphics, data, combatY, combatHeight);
		drawAllEnemyActions(graphics, data, combatY, combatHeight);
		drawEndTurnButton(graphics, combatY, combatHeight);
	}

	/**
	 * Draw the hero's stats and image in combat.
	 */
	private void drawCombatHero(Graphics2D graphics, GameData data, int combatY, int combatHeight) {
		var hero = data.hero();
		int heroWidth = (int) (width * 0.30); // 30% de la largeur
		int heroX = xOrigin + 20;
		int heroY = combatY + 30;
		// Calculer la hauteur maximale pour l'image (80% de combatHeight)
		int maxImageHeight = (int) (combatHeight * 0.80);
		int imageSize = Math.min(heroWidth - 40, maxImageHeight - 150); // -150 pour laisser de la place aux stats
		// Image du héro
		BufferedImage heroImage = loader.image("hero");
		if (heroImage != null) {
			drawImage(graphics, heroImage, heroX, heroY, imageSize, imageSize);
			heroY += imageSize + 15;
		}
		// Stats en dessous de l'image
		graphics.setColor(Color.WHITE);
		graphics.setColor(new Color(255, 100, 100));
		graphics.drawString("PV: " + hero.getCurrentHp() + "/" + hero.getMaxHp(), heroX, heroY);
		heroY += 20;
		graphics.setColor(new Color(100, 200, 255));
		graphics.drawString("Protection: " + hero.getProtection(), heroX, heroY);
		heroY += 20;
		graphics.setColor(new Color(255, 215, 0));
		graphics.drawString("Energie: " + hero.getCurrentEnergy() + "/3", heroX, heroY);
		heroY += 20;
		graphics.setColor(new Color(150, 100, 255));
		graphics.drawString("Mana: " + hero.getCurrentMana(), heroX, heroY);
	}

	/**
	 * Draw the enemy's stats and image in combat.
	 */
	private void drawCombatEnemy(Graphics2D graphics, GameData data, int combatY, int combatHeight) {
		var enemies = data.enemies();
		if (enemies.isEmpty()) return;
		int enemyWidth = (int) (width * 0.30);
		int baseEnemyX = width - enemyWidth - 5;
		for (int i = 0; i < enemies.size(); i++) {
			var enemy = enemies.get(i);
			int enemyX = baseEnemyX - (i * 300);
			int enemyY = combatY + 30;
			int maxImageHeight = (int) (combatHeight * 0.50); // calculer la hauteur maximale pour l'image
			int imageSize = Math.min(enemyWidth - 40, maxImageHeight);
			String enemyImageKey = enemy.enemyInfo().name().toLowerCase().replace(" ", "_").replace("-", "_"); // image de l'ennemi
			BufferedImage enemyImage = loader.image(enemyImageKey);
			if (enemyImage != null) {
				drawImage(graphics, enemyImage, enemyX, enemyY, imageSize, imageSize);
				enemyY += imageSize + 15;
			} // stats en dessous de l'image
			graphics.setColor(Color.WHITE);
			graphics.drawString(enemy.enemyInfo().name(), enemyX + 10, enemyY);
			enemyY += 25;
			graphics.setColor(new Color(255, 100, 100));
			graphics.drawString("PV: " + enemy.getCurrentHp() + "/" + enemy.enemyInfo().maxHp(), enemyX, enemyY);
		}
	}

	public Rectangle getEnemyRect(int index, GameData data, int combatY, int combatHeight) {
		Objects.requireNonNull(data);
		var enemies = data.enemies();
		if (index < 0 || index >= enemies.size())
			return null;
		int enemyWidth = (int) (width * 0.30);
		int baseEnemyX = width - enemyWidth - 5;
		int enemyX = baseEnemyX - (index * 300);
		return new Rectangle(enemyX - 5, combatY + 25, enemyWidth - 10, 100);
	}

	public int screenToEnemyIndex(Position screenPos, GameData data) {
		Objects.requireNonNull(screenPos);
		Objects.requireNonNull(data);
		int combatY = yOrigin + bHeight + 100;
		int combatHeight = iHeight - 150;
		for (int i = 0; i < data.enemies().size(); i++) {
			Rectangle rect = getEnemyRect(i, data, combatY, combatHeight);
			if (rect != null && rect.contains(screenPos.line(), screenPos.col())) {
				return i;
			}
		}
		return -1;
	}
	/**
	 * Draw all enemy actions for the next turn in combat.
	 */
	private void drawAllEnemyActions(Graphics2D graphics, GameData data, int combatY, int combatHeight) {
		int x = width / 2 - 150;
		int y = combatY + combatHeight - 120;
		
		graphics.setColor(new Color(200, 200, 200));
		graphics.drawString("Prochaines actions:", x, y);
		y += 20;

		for (Action action : data.nextEnemyActions()) {
			String actionText = switch (action.getType()) {
			case ATTACK -> "Va attaquer et vous infligez : " + action.getVal() + " dégats";
			case PROTECTION -> "Va se protéger de : " + action.getVal();
			case HEAL -> "Va se soigner de : " + action.getVal() + " HP";
			case CURSE -> "Va vous infliger une malédiction " ;
			};
			graphics.setColor(new Color(255, 200, 100));
			graphics.drawString(actionText, x, y);
			y += 18;
		}
	}

	/**
	 * Draw the end turn button in combat.
	 */
	private void drawEndTurnButton(Graphics2D graphics, int combatY, int combatHeight) {
		int buttonWidth = 180;
		int buttonHeight = 80;
		int buttonX = width / 2 - buttonWidth / 2;
		int buttonY = combatY + combatHeight / 2 - buttonHeight / 2;
		BufferedImage buttonImage = loader.image("endturn");
		if (buttonImage != null) {
			drawImage(graphics, buttonImage, buttonX, buttonY, buttonWidth, buttonHeight);
		}
	}

	/**
	 * Renders the game using the ApplicationContext.
	 * 
	 * @param context ApplicationContext of the game
	 * @param data    Game data to display
	 * @param view    GameView
	 */
	public static void draw(ApplicationContext context, GameData data, GameView view) {
		Objects.requireNonNull(context);
		Objects.requireNonNull(data);
		Objects.requireNonNull(view);
		context.renderFrame(graphics -> view.draw(graphics, data));
	}

	public Position screenToBackpackCell(Position m, BackPack b) {
		Objects.requireNonNull(m);
		Objects.requireNonNull(b);
		// Calculer la taille des cases et les offsets
		float cellSize = calculateCellSize(b);
		float offsetX = calculateOffsetX(b, cellSize);
		float offsetY = calculateOffsetY(b, cellSize);
		// Convertir les coordonnées écran en coordonnées grille
		int col = (int) ((m.line() - offsetX) / cellSize);
		int line = (int) ((m.col() - offsetY) / cellSize);
		// Vérifier si dans les limites de la grille affichée (maxLine/maxCol)
		if (col < 0 || col >= b.maxCol() || line < 0 || line >= b.maxLine()) {
			return null;
		}
		return new Position(line, col);
	}

	public int screenToHealerchoice(Position screenPos) {
		Objects.requireNonNull(screenPos);
		int xcenter = width / 2;
		int ycenter = yOrigin + bHeight + (iHeight / 2);
		int w = 120, h = 50, spacing = 10;
		int healX = xcenter - w / 2;
		int healY = ycenter - 70;
		if (isInRect(screenPos, healX, healY, w, h))
			return 0;
		if (isInRect(screenPos, healX, healY + h + spacing, w, h))
			return 1;
		if (isInRect(screenPos, healX, healY + 2 * (h + spacing), w, h))
			return 2;
		return 3;
	}

	/**
	 * Check if the position is on the end turn button in combat.
	 * 
	 * @param screenPos Screen position
	 * @return true if clicking on end turn button
	 */
	public boolean isClickingEndTurnButton(Position screenPos) {
		Objects.requireNonNull(screenPos);
		int combatY = yOrigin + bHeight;
		int combatHeight = iHeight;
		int buttonWidth = 180;
		int buttonHeight = 80;
		int buttonX = width / 2 - buttonWidth / 2;
		int buttonY = combatY + combatHeight / 2 - buttonHeight / 2;
		return isInRect(screenPos, buttonX, buttonY, buttonWidth, buttonHeight);
	}

	/**
	 * Converts screen coordinates to dungeon grid coordinates.
	 * 
	 * @param screenPos Screen position
	 * @return Dungeon coordinates or null if outside
	 */
	public Coord screenToDungeonCell(Position screenPos) {
		Objects.requireNonNull(screenPos);
		int dungeonLines = 5;
		int dungeonCols = 11;
		float cellWidth = (float) width / dungeonCols;
		float cellHeight = (float) height / dungeonLines;
		int col = (int) (screenPos.line() / cellWidth);
		int line = (int) (screenPos.col() / cellHeight);
		if (line < 0 || line >= dungeonLines || col < 0 || col >= dungeonCols) {
			return null;
		}
		return new Coord(line, col);
	}

	/**
	 * Checks if clicking on the validation button in unlock mode.
	 */
	public boolean isClickingUnlockValidation(Position screenPos) {
		Objects.requireNonNull(screenPos);
		int buttonX = width / 2 - 60;
		int buttonY = yOrigin + bHeight - 80;
		int buttonW = 120;
		int buttonH = 30;
		return isInRect(screenPos, buttonX, buttonY, buttonW, buttonH);
	}

	private void drawEndScreen(Graphics2D g, GameData data, String title) {
		g.setColor(new Color(0, 0, 0, 220));
		g.fillRect(0, 0, width, height);
		g.setColor(Color.WHITE);
		g.setFont(g.getFont().deriveFont(40f));
		int textWidth = g.getFontMetrics().stringWidth(title);
		g.drawString(title, width / 2 - textWidth / 2, 80);
		Path path = Path.of(System.getProperty("user.dir"), "data", "save.txt");
		hallOfFame(g, data, path);
	}

	private void hallOfFame(Graphics2D g, GameData data, Path path) {
		var list = data.top3(path);
		System.out.println(list);
		int x = 50;
		int y = 120;
		int dy = 30;
		g.drawString("HALL OF FAME TOP 3", x, y);
		y += dy;
		for (int i = 0; i < 3; i++) {
			int xp = (i < list.size()) ? list.get(i) : 0; // si moins de 3 scores
			g.drawString((i + 1) + xp + " XP", x, y);
			y += dy;
		}
	}

	public int xOrigin() {
		return xOrigin;
	}

	public int yOrigin() {
		return yOrigin;
	}

	public int width() {
		return width;
	}

	public int height() {
		return height;
	}

	public int bHeight() {
		return bHeight;
	}

	public int iHeight() {
		return iHeight;
	}
}
