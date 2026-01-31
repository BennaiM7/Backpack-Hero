package display;

import java.util.random.RandomGenerator;

import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;
import backpack.Position;
import backpack.object.Curse;
import backpack.object.Equipment;
import dungeon.Coord;
import dungeon.Room;

import java.util.Objects;

public class GameController {
	private boolean rotation = false;
	public boolean gameLoop(ApplicationContext context, GameData data, GameView view) {
		Objects.requireNonNull(context);
		Objects.requireNonNull(data);
		Objects.requireNonNull(view);
		var event = context.pollOrWaitEvent(10);
		switch (event) {
		case null -> {
			return true;
		}
		case KeyboardEvent ke -> {
			return handleKeyboard(ke, data, view, context);
		}
		case PointerEvent pe -> {
			return handlePointer(pe, data, view, context);
		}
		}
	}
	/**
	 * Handle mouse pointer events for all game interactions.
	 */
	private boolean handlePointer(PointerEvent pe, GameData data, GameView view, ApplicationContext context) {
		var loc = pe.location();
		var location = new Position(loc.x(), loc.y());
		if (data.isDragging()) { // maj de la pos de la souris quand on drag
			data.updateMousePosition(location);
			GameView.draw(context, data, view);
			if (pe.action() == PointerEvent.Action.POINTER_UP) {
				handleDrop(location, data, view, context);
				return true;
			}
			return true;
		}
		if (pe.action() != PointerEvent.Action.POINTER_DOWN) { // gestion des clics de base
			return true;
		}
		if (data.isUnlockingSlots()) { // pour débloquer les cases
			handleUnlockClick(location, data, view, context);
			return true;
		}
		if (data.inFight()) {
			handleFightClick(location, data, view, context);
		} else if (!data.combatRewards().isEmpty()) {
			handleRewards(location, data, view, context);
		} else if (data.inHealer()) { // des else if car sinon avec les if dans les combats sa continue de drag les
																	// items et c'est dérangeant comme on doit cliquer dessus pour attaquer sa drag
																	// a chaque fois
			handleHealer(location, data, view, context);
		} else if (data.inMerchant()) {
			handleMerchant(location, data, view, context);
		} else {
			handleExplorationClick(location, data, view, context);
		}

		GameView.draw(context, data, view);
		return true;
	}

	/**
	 * Handle clicks during slot unlocking mode in the backpack.
	 */
	private void handleUnlockClick(Position location, GameData data, GameView view, ApplicationContext context) {
		if (data.slotsToUnlock() == 0 && view.isClickingUnlockValidation(location)) { // vérif si on clique sur le bouton e
																																									// validation
			data.confirmUnlock();
			GameView.draw(context, data, view);
			return;
		}
		if (BackpackArea(location, view)) { // Vérifier si on clique dans le backpack
			Position cell = view.screenToBackpackCell(location, data.backpack());
			if (cell == null) {
				return;
			}
			if (data.backpack().isUnlocked(cell.line(), cell.col())) { // vérif que la case n'est pas déjà débloquée
				return;
			}
			if (data.selectedSlots().contains(cell)) { // pour selectionner
				data.deselectSlot(cell);
			} else {
				if (!data.selectSlotToUnlock(cell)) {
					System.out.println("Vous avez déjà sélectionné assez de cases");
				}
			}
			GameView.draw(context, data, view);
		}
	}

	/**
	 * Wait for a second click or ESC key from the user.
	 */
	private Position waitSecondClick(ApplicationContext context) {
		Objects.requireNonNull(context, "context");
		for (;;) {
			var e = context.pollOrWaitEvent(16);
			if (e == null) {
				continue;
			}
			switch (e) {
			case PointerEvent pe -> {
				if (pe.action() == PointerEvent.Action.POINTER_DOWN) {
					return new Position(pe.location().x(), pe.location().y());
				}
			}
			case KeyboardEvent ke -> {
				if (ke.key() == KeyboardEvent.Key.ESCAPE) {
					return null;
				}
			}
			default -> {
				return null;
			}
			}
		}
	}
	/**
	 * Handle keyboard inputs for game controls (quit, show dungeon, rotate, etc).
	 */
	private boolean handleKeyboard(KeyboardEvent ke, GameData data, GameView view, ApplicationContext context) {
		Objects.requireNonNull(ke, "ke");
		Objects.requireNonNull(data, "data");
		Objects.requireNonNull(view, "view");
		Objects.requireNonNull(context, "context");

		if (ke.action() != KeyboardEvent.Action.KEY_PRESSED) {
			return true;
		}
		switch (ke.key()) {
		case Q -> {
			System.exit(0);
			return false;
		}
		case D -> {
			data.changeShowDungeon(); // on laisse pouvoir voir le dongon même en combat si jamais on veut voir si on
																// a d'autre ennemi à venir etc par ex
			GameView.draw(context, data, view);
		}
		case R -> {
			if (data.isDragging()) { // pour le cas où on est en train de drag un item qu'on puisse quand même faire
																// une rotation
				data.rotationDuringDrag();
			} else {
				rotation = !rotation;
			}
			GameView.draw(context, data, view);
		}
		case C -> {
			dropItem(data, view, context);
		}
		case A -> {
			if (data.inFight()) {
				data.selectEnemy(0);
				GameView.draw(context, data, view);
			}
		}
		case Z -> {
			if (data.inFight()) {
				data.selectEnemy(1);
				GameView.draw(context, data, view);
			}
		}
		default -> {
		}
		}
		return true;
	}

	/**
	 * Handle clicks during exploration mode (not in fight, merchant, or healer).
	 */
	private void handleExplorationClick(Position location, GameData data, GameView view, ApplicationContext context) {
		if (data.showDungeon()) {
			handleDungeonClick(location, data, view, context);
		} else {
			// Si on a un trésor et qu'on clique dessus
			if (data.hasPendingItem() && InteractionArea(location, view)) {
				if (view.isClickingOnTreasureItem(location, data)) {
					data.startDragging(data.pendingItem(), location, rotation);
					return;
				}
			}
			if (BackpackArea(location, view)) { // les autres cas
				dragItem(location, data, view, context);
			} else if (InteractionArea(location, view)) {
				handleInteractionClick(location, data, view, context);
			}
		}
	}

	private void dropItem(GameData data, GameView view, ApplicationContext context) {
		var newpos = waitSecondClick(context);
		if (newpos == null) {
			return;
		}
		if (BackpackArea(newpos, view)) {
			Position cell = view.screenToBackpackCell(newpos, data.backpack());
			if (cell == null || data.backpack().detection(cell.line(), cell.col()) == null) {
				return;
			}
			var item = data.backpack().detection(cell.line(), cell.col());
			switch (item) {
			case Curse _ -> {
				data.hero().clearCombatMaluses();
				data.penalize();
			}
			default -> {
			}
			}
			data.backpack().withdraw(cell.line(), cell.col());
			GameView.draw(context, data, view);
		}
	}

	private Position originCell(GameData data, Equipment item) {
		var pos = data.backpack().getPos(item);
		int minLine = Integer.MAX_VALUE;
		int minCol = Integer.MAX_VALUE;
		for (var p : pos) {
			if (p.line() < minLine)
				minLine = p.line();
			if (p.col() < minCol)
				minCol = p.col();
		}
		return new Position(minLine, minCol);
	}

	/**
	 * Start dragging an item from the backpack or rewards area.
	 */
	private boolean dragItem(Position location, GameData data, GameView view, ApplicationContext context) {
		// empêcher le drag si on a une malédiction en attente
		if (data.hasPendingCurse()) {
			return false;
		}
		// priorité à la zone des récompenses avant le backpack
		if (!data.combatRewards().isEmpty()) {
			int idx = view.screenToRewardIndex(location, data.combatRewards().size());
			if (idx != -1) {
				Equipment item = data.combatRewards().get(idx);
				data.startDragging(item, location, rotation);
				GameView.draw(context, data, view);
				return true;
			}
		}
		if (BackpackArea(location, view)) {
			Position cell = view.screenToBackpackCell(location, data.backpack());
			if (cell == null || data.backpack().detection(cell.line(), cell.col()) == null) {
				return false;
			}
			Equipment temp = data.backpack().detection(cell.line(), cell.col());// pour voir on a quel item à la position
			Position origin = originCell(data, temp);
			var oldRotation = data.backpack().getRotation(temp);
			data.backpack().withdraw(cell.line(), cell.col()); // on retire l'item du backpack quand on le déplace sinon sa
																													// bloquerait les cases alors qu'on veut le bouger
			data.startDraggingFromBackpack(temp, location, origin, oldRotation);
			GameView.draw(context, data, view);
			return true;
		}
		return false;
	}
	/**
	 * Handle clicks on the dungeon map to move the hero.
	 */
	private void handleDungeonClick(Position location, GameData data, GameView view, ApplicationContext context) {
		var dungeonCoord = view.screenToDungeonCell(location);
		if (dungeonCoord == null) {
			return;
		}
		Room clickedRoom = data.floor().room(dungeonCoord.line(), dungeonCoord.col());
		if (clickedRoom == null) { // qu'on appuie sur une case vide
			return;
		}
		Coord heroPos = data.hero().pos();
		if (!isAdjacent(heroPos, dungeonCoord)) { // on vérifie si on se déplace à c$ôté
			return;
		}
		// Se déplacer vers la case adjacente
		data.hero().initPos(dungeonCoord);
		clickedRoom.toVisit();
		if (data.hasPendingItem()) { // comme on bouge on retire l'item qu'on avait en attente
			data.clearPendingItem();
		}
		data.enterRoom(clickedRoom.roomType());
		data.handleRoomEntry();
		data.changeShowDungeon(); // une fois qu'on a appuyé sur notre salle on ferme le donjon
	}
	/**
	 * Check if two positions are adjacent in the dungeon.
	 */
	private boolean isAdjacent(Coord heroPos, Coord targetPos) {
		int lineDiff = Math.abs(heroPos.line() - targetPos.line());
		int colDiff = Math.abs(heroPos.col() - targetPos.col());
		return (lineDiff == 1 && colDiff == 0) || (lineDiff == 0 && colDiff == 1);
	}

	private void handleInteractionClick(Position location, GameData data, GameView view, ApplicationContext context) {
		handleMerchant(location, data, view, context);
	}

	/**
	 * Handle clicks in the healer room (heal, increase max HP, exit).
	 */
	private void handleHealer(Position location, GameData data, GameView view, ApplicationContext context) {
		if (InteractionArea(location, view)) {
			int choice = view.screenToHealerchoice(location);
			switch (choice) {
			case 0 -> { // heal de 25hp
				if (data.hero().getCurrentHp() == data.hero().getMaxHp()) { // déjà full vie
					return;
				}
				if (!data.backpack().pay(4)) { // pas assez d'or
					return;
				}
				data.hero().heal(25);
				GameView.draw(context, data, view);
			}
			case 1 -> { // augmente les hp max de 5
				if (!data.backpack().pay(10)) {
					return;
				}
				data.hero().increaseMaxHp(5);
				GameView.draw(context, data, view);
			}
			case 2 -> { // pour sortir
				data.endHealer();
				GameView.draw(context, data, view);
			}
			default -> {
			}
			}
		}
	}

	/**
	 * Handle clicks on combat rewards (take or leave rewards).
	 */
	private void handleRewards(Position location, GameData data, GameView view, ApplicationContext context) {
		if (view.isClickingRewardLeave(location)) {
			data.clearCombatRewards();
			data.clearMerchantSelection();
			data.endDragging();
			return;
		}
		int i = view.screenToRewardIndex(location, data.combatRewards().size());
		if (i != -1) {
			Equipment item = data.combatRewards().get(i);
			data.startDragging(item, location, rotation);
			GameView.draw(context, data, view);
		}
	}

	private boolean leaveMerchant(Position location, GameData data, GameView view) {
		if (view.isClickingMerchantLeave(location)) {
			data.endMerchant();
			data.clearMerchantSelection();
			data.endDragging();
			return true;
		}
		return false;
	}

	private void selectItemMerchant(Position location, GameData data, GameView view) {
		int idx = view.screenToMerchantIndex(location, data.merchantSlot().size());
		if (idx != -1) {
			Equipment item = data.merchantSlot().get(idx);
			int price = data.getPriceFromRarity(item.rarity());
			// check Gold
			if (data.backpack().getGold() < price) {
				System.out.println("Pas assez d'or");
				return;
			}
			data.setMerchantSelectedIndex(idx);
			data.startDragging(item, location, rotation);
		}
	}

	/**
	 * Handle clicks and item purchases in the merchant room.
	 */
	private void handleMerchant(Position location, GameData data, GameView view, ApplicationContext context) {
		if (leaveMerchant(location, data, view)) {
			return;
		}
		selectItemMerchant(location, data, view);
		if (data.isDragging() && data.merchantSelectedIndex() != -1) {
			Position cell = view.screenToBackpackCell(location, data.backpack());
			if (cell == null)
				return;
			Equipment item = data.draggingItem();
			// Vérif placement
			if (!data.backpack().canPlace(item, cell.line(), cell.col(), data.originalRotation())) {
				return;
			}
			// Vérif + paiement
			int price = data.getPriceFromRarity(item.rarity());
			if (data.backpack().getGold() < price) {
				return;
			}
			data.backpack().pay(price);
			data.backpack().place(item, cell.line(), cell.col(), data.originalRotation());
			data.merchantSlot().set(data.merchantSelectedIndex(), null); // retirer du marchand
			data.clearMerchantSelection();
			data.endMerchant();
			GameView.draw(context, data, view);
		}
	}

	/**
	 * Handle clicks during a fight.
	 */
	private void handleFightClick(Position location, GameData data, GameView view, ApplicationContext context) {
		// on appelle juste chaque fonctions annexe car la fonction était trop longue
		if (handlePendingCurseInFight(location, data, view)) {
			return;
		}
		if (handleEnemySelectionInFight(location, data, view)) {
			return;
		}
		if (handleEndTurnButtonInFight(location, data, view)) {
			return;
		}
		if (handleBackpackClickInFight(location, data, view, context)) {
			return;
		}
	}

	/**
	 * Handle pending curse acceptance or refusal.
	 */
	private boolean handlePendingCurseInFight(Position location, GameData data, GameView view) {
		if (!data.hasPendingCurse()) {
			return false;
		}
		if (view.isClickingCurseAccept(location)) {
			data.startDragging(data.pendingCurse(), location, false);
			return true;
		}
		if (view.isClickingCurseRefuse(location)) {
			data.refuseCurse();
			return true;
		}
		return true;
	}

	/**
	 * Handle enemy selection clicks.
	 */
	private boolean handleEnemySelectionInFight(Position location, GameData data, GameView view) {
		if (!InteractionArea(location, view)) {
			return false;
		}
		int enemyIdx = view.screenToEnemyIndex(location, data);
		if (enemyIdx != -1) {
			data.selectEnemy(enemyIdx);
			return true;
		}
		return false;
	}

	/**
	 * Handle end turn button clicks.
	 */
	private boolean handleEndTurnButtonInFight(Position location, GameData data, GameView view) {
		if (InteractionArea(location, view) && view.isClickingEndTurnButton(location)) {
			data.executeEnemyTurn();
			return true;
		}
		return false;
	}

	/**
	 * Handle backpack area clicks (items or attack).
	 */
	private boolean handleBackpackClickInFight(Position location, GameData data, GameView view,
			ApplicationContext context) {
		if (!BackpackArea(location, view)) {
			return false;
		}
		Position cell = view.screenToBackpackCell(location, data.backpack());
		if (cell == null) {
			return false;
		}
		var item = data.backpack().detection(cell.line(), cell.col());
		if (item != null) {
			data.useItemInCombat(item);
		} else {
			performMeleeAttack(data, view);
		}
		return true;
	}

	/**
	 * Perform a melee attack on the selected enemy.
	 */
	private void performMeleeAttack(GameData data, GameView view) {
		var hero = data.hero();
		var enemy = data.selectedEnemy();
		if (enemy != null && hero.useEnergy(1)) {
			int degats = hero.applyOutgoingDamage(4);
			enemy.takeDamage(degats);
			if (!enemy.isAlive()) {
				handleEnemyDeath(data);
			}
		}
	}

	/**
	 * Handle enemy death and select next alive enemy.
	 */
	private void handleEnemyDeath(GameData data) {
		boolean allDead = data.enemies().stream().allMatch(e -> !e.isAlive());
		if (allDead) {
			data.endCombat(RandomGenerator.getDefault());
		} else {
			for (int i = 0; i < data.enemies().size(); i++) {
				if (data.enemies().get(i).isAlive()) {
					data.selectEnemy(i);
					break;
				}
			}
		}
	}

	private void handleDrop(Position location, GameData data, GameView view, ApplicationContext context) {
		Equipment item = data.draggingItem();
		boolean dropSuccess = false; // qu'on a réussi a bien placer l'item ou non
		if (BackpackArea(location, view)) {
			Position cell = view.screenToBackpackCell(location, data.backpack());
			if (cell != null && cell.line() != -1) {
				switch (item) {
				case Curse curse -> { // pour le cas des malédictions
					if (data.backpack().placeCurse(curse, cell.line(), cell.col())) { // on place la malédiction dans le sac
						data.applyCurse(curse);
						data.clearPendingCurse();
						dropSuccess = true;
					}
				}
				default -> {
					boolean rotationToUse = (data.originalPosition() != null) ? data.originalRotation() : rotation;
					dropSuccess = data.tryPlaceItem(item, cell.line(), cell.col(), rotationToUse);
				}
				}
			}
		}
		if (!dropSuccess && data.originalPosition() != null) { // le cas où on a pas réussi à le drop et qu'on l'avait pris
																														// du backpack
			Position posOriginal = data.originalPosition();
			boolean originalRotation = data.originalRotation();
			if (data.backpack().canPlace(item, posOriginal.line(), posOriginal.col(), originalRotation)) {
				data.backpack().place(item, posOriginal.line(), posOriginal.col(), originalRotation);
			}
		}
		data.endDragging();
		GameView.draw(context, data, view);
	}

	/**
	 * Check if position is in backpack area.
	 */
	private boolean BackpackArea(Position pos, GameView view) {
		return pos.line() >= view.xOrigin() && pos.line() < view.width() && pos.col() >= view.yOrigin()
				&& pos.col() < view.yOrigin() + view.bHeight();
	}

	/**
	 * Check if position is in interaction area.
	 */
	private boolean InteractionArea(Position pos, GameView view) {
		return pos.line() >= view.xOrigin() && pos.line() < view.width() && pos.col() >= view.yOrigin() + view.bHeight()
				&& pos.col() < view.height();
	}
}
