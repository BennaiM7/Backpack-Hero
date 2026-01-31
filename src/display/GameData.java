package display;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.Objects;
import actions.Action;
import backpack.BackPack;
import backpack.Position;
import backpack.object.Armor;
import backpack.object.Consumable;
import backpack.object.Curse;
import backpack.object.Equipment;
import backpack.object.Gear;
import backpack.object.ManaStone;
import backpack.object.Rarity;
import backpack.object.WeaponType;
import dungeon.BaladeDonjon;
import dungeon.Coord;
import dungeon.Dungeon;
import dungeon.Floor;
import dungeon.GenerationItem;
import dungeon.RandomArmor;
import dungeon.RandomArrow;
import dungeon.RandomGear;
import dungeon.RandomGem;
import dungeon.RandomManaStone;
import dungeon.RandomPotion;
import dungeon.Room;
import dungeon.RoomType;
import fighter.Enemy;
import fighter.EnemyCreation;
import fighter.Hero;

public class GameData {
	private final Hero hero;
	private final Dungeon dungeon;
	private Gear pendingItem;
	private BaladeDonjon balade;
	private Curse pendingCurse;
	private int curseRefusalCount = 0;
	private boolean inFight = false;
	private boolean inHealer = false;
	private boolean inMerchant = false;
	private List<Enemy> enemies = new ArrayList<>();
	private int selectedEnemyIndex = 0;
	private List<Action> nextEnemyActions = new ArrayList<>();
	private RoomType currentRoom;
	private boolean showDungeon = false; // mettre true pour forcer l'afficahage et voir ça ressemble à quoi
	private Equipment draggingItem; // Item en cours de drag
	private boolean isDragging = false;
	private Position mousePosition; // Position de la souris
	private Position originalPosition; // Position d'origine dans le backpack (si drag depuis backpack)
	private boolean originalRotation; // Rotation d'origine de l'item
	private boolean unlockingSlots = false;
	private int slotsToUnlock = 0;
	private List<Position> selectedSlots = new ArrayList<>();
	private boolean gameOver = false;
	private boolean victory = false;
	private static final int PRICE_COMMON = 6;
	private static final int PRICE_UNCOMMON = 8;
	private static final int PRICE_RARE = 12;
	private static final int PRICE_LEGENDARY = 20;
	private int merchantSelectedIndex = -1;
	private List<Equipment> merchantSlots = List.of();
	private List<Equipment> combatRewards = List.of();

	public GameData() {
		hero = new Hero(40);
		dungeon = new Dungeon();
		hero.initPos(dungeon.posHero());
		balade = new BaladeDonjon(dungeon.currentFloor(), hero);
	}

	public Hero hero() {
		return hero;
	}

	public BackPack backpack() {
		return hero.getBackpack();
	}

	public Dungeon dungeon() {
		return dungeon;
	}

	public Floor floor() {
		return dungeon.currentFloor();
	}

	public BaladeDonjon balade() {
		return balade;
	}

	public boolean showDungeon() {
		return showDungeon;
	}

	public void changeShowDungeon() {
		showDungeon = !showDungeon;
	}

	public boolean inFight() {
		return inFight;
	}

	public List<Enemy> enemies() {
		return List.copyOf(enemies);
	}

	public Enemy selectedEnemy() {
		if (enemies.isEmpty() || selectedEnemyIndex >= enemies.size()) {
			return null;
		}
		return enemies.get(selectedEnemyIndex);
	}

	public int selectedEnemyIndex() {
		return selectedEnemyIndex;
	}

	public void selectEnemy(int index) {
		if (index < 0 || index >= enemies.size()) {
			throw new IllegalArgumentException();
		}
		selectedEnemyIndex = index;
	}

	public List<Action> nextEnemyActions() {
		return List.copyOf(nextEnemyActions);
	}

	/**
	 * Start a fight with a list of enemies and their actions.
	 */
	public void startFight(List<Enemy> enemyList, List<Action> actions) {
		Objects.requireNonNull(enemyList);
		Objects.requireNonNull(actions);
		enemies = new ArrayList<>(enemyList);
		selectedEnemyIndex = 0;
		inFight = true;
		nextEnemyActions = actions;
	}

	/**
	 * End the current fight and clear enemies.
	 */
	public void endFight() {
		var temp = backpack().findArrowInBackpack();
		if (temp != null) {
			var pos = backpack().getPos(temp);
			backpack().withdraw(pos.getFirst().line(), pos.getFirst().col());
		}
		inFight = false;
		enemies.clear();
		selectedEnemyIndex = 0;
	}

	public void startHealer() {
		inHealer = true;
	}

	public void endHealer() {
		inHealer = false;
	}

	public boolean inHealer() {
		return inHealer;
	}

	public void startMerchant() {
		inMerchant = true;
	}

	public void endMerchant() {
		inMerchant = false;
	}

	public boolean inMerchant() {
		return inMerchant;
	}

	public List<Equipment> merchantSlot() {
		return List.copyOf(merchantSlots);
	}

	/**
	 * Initialize merchant slots with random items.
	 */
	public void initMerchantSlots(RandomGenerator rand) {
		Objects.requireNonNull(rand);
		var rarity = GenerationItem.generation(rand);
		merchantSlots = new ArrayList<>(List.of(GenerationItem.randomByRarity(rand, rarity, List.of(RandomGear.values())),
				GenerationItem.randomByRarity(rand, rarity, List.of(RandomPotion.values())),
				GenerationItem.randomByRarity(rand, rarity, List.of(RandomGem.values())),
				GenerationItem.randomByRarity(rand, rarity, List.of(RandomArrow.values())),
				GenerationItem.randomByRarity(rand, rarity, List.of(RandomManaStone.values())),
				GenerationItem.randomByRarity(rand, rarity, List.of(RandomArmor.values()))));
	}

	public void setMerchantSelectedIndex(int idx) {
		merchantSelectedIndex = idx;
	}

	public int merchantSelectedIndex() {
		return merchantSelectedIndex;
	}

	public void clearMerchantSelection() {
		merchantSelectedIndex = -1;
	}

	/**
	 * Give a reward item after combat.
	 */
	public void giveReward(Equipment item) {
		Objects.requireNonNull(item);
		combatRewards = new ArrayList<>(List.of(item));
	}

	public List<Equipment> combatRewards() {
		return List.copyOf(combatRewards);
	}

	public void clearCombatRewards() {
		combatRewards = List.of();
	}

	public int getPriceFromRarity(Rarity rare) {
		Objects.requireNonNull(rare);
		return switch (rare) {
		case COMMON -> PRICE_COMMON;
		case UNCOMMON -> PRICE_UNCOMMON;
		case RARE -> PRICE_RARE;
		case LEGENDARY -> PRICE_LEGENDARY;
		default -> -1;
		};
	}

	public void updateEnemyActions(List<Action> actions) {
		Objects.requireNonNull(actions);
		nextEnemyActions = actions;
	}

	/**
	 * Go to the next room or floor if possible.
	 */
	public void nextRoom() {
		if (balade.nextRoom()) {
			accesNextFloor();
			return;
		}
		enterRoom(balade.getRoom(hero.pos()));
	}

	public void enterRoom(RoomType room) {
		currentRoom = room;
	}

	/**
	 * Access the next floor in the dungeon.
	 */
	public void accesNextFloor() {
		boolean ok = dungeon.accessTopFloor(hero);
		if (!ok) {
			triggerVictory();
			return;
		}
		balade = new BaladeDonjon(dungeon.currentFloor(), hero);
		hero.initPos(dungeon.posHero());
	}

	public RoomType currentRoom() {
		return currentRoom;
	}

	public boolean hasPendingCurse() {
		return pendingCurse != null;
	}

	/**
	 * Apply a random penalty to the hero.
	 */
	public void penalize() {
		RandomGenerator rand = RandomGenerator.getDefault();
		var temp = rand.nextInt(3) + 1;
		switch (temp) {
		case 1 -> hero.takeDamage(5);
		case 2 -> backpack().pay(2);
		}
	}

	public Curse pendingCurse() {
		return pendingCurse;
	}

	public void setPendingCurse(Curse curse) {
		Objects.requireNonNull(curse);
		this.pendingCurse = curse;
	}

	public void clearPendingCurse() {
		this.pendingCurse = null;
	}

	public int curseRefusalCount() {
		return curseRefusalCount;
	}

	/**
	 * Refuse a curse and take damage.
	 */
	public void refuseCurse() {
		curseRefusalCount++;
		hero.takeDamage(curseRefusalCount);
		clearPendingCurse();
	}

	public void resetCurseRefusalCount() {
		curseRefusalCount = 0;
	}

	public Gear pendingItem() {
		return pendingItem;
	}

	public void setPendingItem(Gear item) {
		Objects.requireNonNull(item);
		this.pendingItem = item;
	}

	public void clearPendingItem() {
		this.pendingItem = null;
	}

	public boolean hasPendingItem() {
		return pendingItem != null;
	}

	public Equipment draggingItem() {
		return draggingItem;
	}

	/**
	 * Start dragging an item (not from backpack).
	 */
	public void startDragging(Equipment item, Position mousePos, boolean rotation) {
		Objects.requireNonNull(item);
		Objects.requireNonNull(mousePos);
		this.draggingItem = item;
		this.isDragging = true;
		this.mousePosition = mousePos;
		this.originalPosition = null;
		this.originalRotation = rotation;
	}

	/**
	 * Start dragging an item from the backpack.
	 */
	public void startDraggingFromBackpack(Equipment item, Position mousePos, Position cellPos, boolean rotation) {
		Objects.requireNonNull(item);
		Objects.requireNonNull(mousePos);
		Objects.requireNonNull(cellPos);
		this.draggingItem = item;
		this.isDragging = true;
		this.mousePosition = mousePos;
		this.originalPosition = cellPos;
		this.originalRotation = rotation;
	}

	public void updateMousePosition(Position mousePos) {
		Objects.requireNonNull(mousePos);
		this.mousePosition = mousePos;
	}

	public void endDragging() {
		this.draggingItem = null;
		this.isDragging = false;
		this.mousePosition = null;
		this.originalPosition = null;
		this.originalRotation = false;
	}

	public boolean isDragging() {
		return isDragging;
	}

	public Position mousePosition() {
		return mousePosition;
	}

	public Position originalPosition() {
		return originalPosition;
	}

	public boolean originalRotation() {
		return originalRotation;
	}

	public void rotationDuringDrag() {
		if (isDragging) {
			this.originalRotation = !this.originalRotation;
		}
	}

	public boolean isUnlockingSlots() {
		return unlockingSlots;
	}

	public void startUnlockingSlots(int count) {
		this.unlockingSlots = true;
		this.slotsToUnlock = count;
		this.selectedSlots.clear();
	}

	public int slotsToUnlock() {
		return slotsToUnlock - selectedSlots.size();
	}

	public List<Position> selectedSlots() {
		return List.copyOf(selectedSlots);
	}

	public boolean selectSlotToUnlock(Position pos) {
		Objects.requireNonNull(pos);
		if (selectedSlots.contains(pos)) { // on a déjà sélectionné cette case
			return false;
		}
		if (selectedSlots.size() >= slotsToUnlock) { // on a déjà sélectionné le nombre max de cases qu'on doit choisir
			return false;
		}
		selectedSlots.add(pos);
		return true;
	}

	public boolean deselectSlot(Position pos) {
		Objects.requireNonNull(pos);
		return selectedSlots.remove(pos);
	}

	/**
	 * Confirm and unlock selected slots in the backpack.
	 */
	public void confirmUnlock() {
		for (Position pos : selectedSlots) {
			backpack().unlockSlot(pos.line(), pos.col());
		}
		unlockingSlots = false;
		slotsToUnlock = 0;
		selectedSlots.clear();
	}
	/**
	 * Recalculate and add armor protection bonus to the hero.
	 */
	public void recomputeArmorBonus() {
		int sum = 0;
		var seen = new ArrayList<Equipment>();
		var backpack = backpack();
		for (int i = 0; i < backpack.line(); i++) {
			for (int j = 0; j < backpack.col(); j++) {
				var eq = backpack.getEquipment(i, j);
				if (eq != null && !seen.contains(eq)) {
					switch (eq) {
						case Armor armor -> {
							seen.add(eq);
							sum += armor.protection();
						}
						default -> {
						}
					}
				}
			}
		}
		hero.addProtection(sum);
	}

	public boolean isGameOver() {
		return gameOver;
	}

	public boolean isVictory() {
		return victory;
	}

	/**
	 * Trigger game over, save the score, and end the fight.
	 */
	public void triggerGameOver() {
		this.gameOver = true;
		this.inFight = false;
		int finalScore = calculateFinalScore();
		Path file = Path.of(System.getProperty("user.dir"), "data", "save.txt");
		addFileScore(file, finalScore);
	}

	/**
	 * Trigger victory save score and set victory flag.
	 */
	public void triggerVictory() {
		this.victory = true;
		this.inFight = false;
		int finalScore = calculateFinalScore();
		Path file = Path.of(System.getProperty("user.dir"), "data", "save.txt");
		addFileScore(file, finalScore);
	}

	/**
	 * Calculate the final score based on hero level and equipment.
	 */
	public int calculateFinalScore() {
		int heroLevel = hero.getLevel();
		int equipmentValue = 0;
		var seen = new ArrayList<Equipment>();
		var backpack = backpack();
		for (int i = 0; i < backpack.line(); i++) {
			for (int j = 0; j < backpack.col(); j++) {
				var eq = backpack.getEquipment(i, j);
				if (eq != null && !seen.contains(eq)) {
					seen.add(eq);
					equipmentValue += getPriceFromRarity(eq.rarity());
				}
			}
		}
		int baseScore = heroLevel * 100;
		return baseScore + equipmentValue;
	}

	/**
	 * Add a score to the save file.
	 */
	public static void addFileScore(Path path, int score) {
		Objects.requireNonNull(path);
		String line = String.valueOf(score);
		try {
			var lst = new ArrayList<String>();
			if (Files.exists(path)) {
				lst.addAll(Files.readAllLines(path, StandardCharsets.UTF_8));
			}
			lst.add(line);

			try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				for (var l : lst) {
					writer.write(l);
					writer.newLine();
				}
			}
		} catch (IOException e) {
			System.err.println("Erreur sauvegarde");
		}
	}

	/**
	 * Get the top 3 scores from the save file.
	 */
	public List<Integer> top3(Path path) {
		Objects.requireNonNull(path);
		var scores = new ArrayList<Integer>();
		try (var reader = Files.newBufferedReader(path)) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#"))
					continue;
				try {
					scores.add(Integer.parseInt(line));
				} catch (NumberFormatException e) {
				}
			}
		} catch (IOException e) {
			return List.of();
		}
		return scores.stream().sorted(Comparator.reverseOrder()).limit(3).toList();
	}

	/**
	 * End combat, give rewards, and unlock slots if level up.
	 */
	public void endCombat(RandomGenerator rand) {
		Objects.requireNonNull(rand);
		// donner xp pour tous les ennemis vaincus
		int totalXp = enemies().stream().mapToInt(e -> e.enemyInfo().xpReward()).sum();
		int oldLevel = hero().getLevel();
		hero().earnXp(totalXp);
		int newLevel = hero().getLevel();
		Coord heroPos = hero().pos();
		Room room = floor().room(heroPos.line(), heroPos.col());
		if (room != null) {
			room.complete(); // on a visité la salle on la marque completer sinon en revenant dans la case de
												// ce combat on a un nouveau combat
			if (newLevel > oldLevel) { // on a gagner un niveau donc on propose les cases a selectionner
				int levelsGained = newLevel - oldLevel;
				int slotsToUnlock = levelsGained * 3; // 3 cases par niveau
				startUnlockingSlots(slotsToUnlock);
			}
			endFight();
			backpack().addGold(5);
			Equipment randomItem = RandomGear.randomGear(rand);
			giveReward(randomItem);
		}
	}
	
	/**
	 * Use an item in combat (gear, mana stone, or consumable).
	 */
	public boolean useItemInCombat(Equipment item) {
		Objects.requireNonNull(item);
		var hero = hero();
		var enemy = selectedEnemy();
		if (enemy == null)
			return false;
		switch (item) {
		case Gear gear -> {
			return useGearInCombat(gear, hero, enemy);
		}
		case ManaStone manaStone -> {
			useManaStoneInCombat(manaStone);
		}
		case Consumable consumable -> {
			useConsumableInCombat(consumable);
		}
		default -> {
		}
		}
		return false; // Combat continue
	}

	/**
	 * Use a gear item (weapon or armor) in combat.
	 */
	private boolean useGearInCombat(Gear gear, Hero hero, Enemy enemy) {
		if (gear.basicDamage() > 0) {
			return useWeaponInCombat(gear, hero, enemy);
		} else if (gear.protectionBasic() > 0) {
			return useArmorInCombat(gear, hero);
		}
		return false;
	}

	private int useArrow(Gear gear) {
		var arrow = backpack().findArrowInBackpack();
		if (arrow == null) {
			return -1;
		}
		return arrow.damage() / 2;

	}

	/**
	 * Use a weapon to attack the selected enemy.
	 */
	private boolean useWeaponInCombat(Gear gear, Hero hero, Enemy enemy) {
		int base = gear.basicDamage();
		if (gear.getWeaponType() == WeaponType.BOW) {
			var arrow = useArrow(gear);
			if (arrow == -1) {
				return false;
			}
			base *= arrow;
		}
		if (hero.energyPenalty() != 0) {
			hero.useEnergy(hero.energyPenalty());
		}
		if (!hero.useEnergy(gear.cost())) {
			return false;
		}
		if (!checkManaRequirements(gear, hero)) {
			hero.addEnergy(gear.cost());
			return false;
		}
		int bonus = backpack().bonusDamage(gear);
		int degats = hero.applyOutgoingDamage(base + bonus);
		enemy.takeDamage(degats);
		if (!enemy.isAlive()) {
			handleEnemyDeathInCombat();
			return true;
		}
		return false;
	}

	/**
	 * Check if hero has enough mana for magical gear.
	 */
	private boolean checkManaRequirements(Gear gear, Hero hero) {
		if (gear.typeW() == WeaponType.MAGIC && gear.mana() > 0) {
			return hero.useMana(gear.mana());
		}
		return true;
	}

	/**
	 * Use armor to gain protection.
	 */
	private boolean useArmorInCombat(Gear gear, Hero hero) {
		if (!hero.useEnergy(gear.cost())) {
			return false;
		}
		hero.addProtection(gear.protectionBasic());
		return false;
	}

	/**
	 * Use a mana stone to restore mana.
	 */
	private void useManaStoneInCombat(ManaStone manaStone) {
		int manaDisponible = manaStone.getManaFight();
		if (manaDisponible > 0) {
			manaStone.useMana(1);
			hero().addMana(1);
		}
	}

	/**
	 * Use a consumable item.
	 */
	private void useConsumableInCombat(Consumable consumable) {
		var positions = backpack().getPos(consumable);
		if (!positions.isEmpty()) {
			consumable.use(hero());
			Position firstPos = positions.get(0);
			backpack().withdraw(firstPos.line(), firstPos.col());
		}
	}

	/**
	 * Handle enemy death and end combat if all are dead.
	 */
	private void handleEnemyDeathInCombat() {
		boolean allDead = enemies().stream().allMatch(e -> !e.isAlive());
		if (allDead) {
			endCombat(RandomGenerator.getDefault());
		} else {
			selectNextAliveEnemy();
		}
	}

	private void selectNextAliveEnemy() {
		for (int i = 0; i < enemies().size(); i++) {
			if (enemies().get(i).isAlive()) {
				selectEnemy(i);
				break;
			}
		}
	}

	/**
	 * Execute all enemy actions for their turn.
	 */
	public void executeEnemyTurn() {
		RandomGenerator rand = RandomGenerator.getDefault();
		// Reset protection de tous les ennemis au début du tour
		for (Enemy enemy : enemies()) {
			if (enemy.isAlive()) {
				enemy.resetProtection();
			}
		}
		hero().resetProtection();

		// Chaque ennemi fait ses actions
		List<Action> allActions = new ArrayList<>();
		for (Enemy enemy : enemies()) {
			if (enemy.isAlive()) {
				allActions.addAll(enemy.randomActions(rand));
			}
		}
		updateEnemyActions(allActions);

		for (Action action : allActions) {
			executeAction(action);
		}

		if (!hero().isAlive()) {
			triggerGameOver();
			return;
		}
		hero().resetEnergie();
	}

	private void executeAction(Action action) {
		switch (action.getType()) {
		case ATTACK -> hero().takeDamage(action.getVal());
		case PROTECTION -> {
			for (Enemy enemy : enemies()) {
				if (enemy.isAlive()) {
					enemy.addProtection(action.getVal());
					break;
				}
			}
		}
		case HEAL -> {
			for (Enemy enemy : enemies()) {
				if (enemy.isAlive()) {
					enemy.heal(action.getVal());
					break;
				}
			}
		}
		case CURSE -> {
			if (!hasPendingCurse()) {
				setPendingCurse(action.getGear());
			}
		}
		}
	}

	/**
	 * Apply a curse effect to the hero.
	 */
	public void applyCurse(Curse curse) {
		Objects.requireNonNull(curse);
		switch (curse.malusType()) {
		case HEAL -> hero().takeDamage(curse.malus());
		case DAMAGE -> hero().setWeakDamageMalus(curse.malus());
		case WEAK -> hero().setEnergyPenalty(curse.malus());
		}
	}

	/**
	 * Handle the logic when entering a new room.
	 */
	public void handleRoomEntry() {
		if (currentRoom == null)
			return;
		Coord heroPos = hero().pos();
		Room room = floor().room(heroPos.line(), heroPos.col());
		if (room != null && room.isCompleted())
			return;
		RandomGenerator rand = RandomGenerator.getDefault();
		switch (currentRoom) {
		case ENEMIES -> handleEnemyRoomEntry(rand);
		case TREASURE -> handleTreasureRoomEntry(room, rand);
		case HEALER -> handleHealerRoomEntry(room);
		case MERCHANT -> handleMerchantRoomEntry(room, rand);
		case EXIT -> accesNextFloor();
		case CORRIDOR, START -> {
		}
		}
	}

	/**
	 * Handle enemy room entry and start combat.
	 */
	private void handleEnemyRoomEntry(RandomGenerator rand) {
		if (!hero().isAlive()) {
			triggerGameOver();
			return;
		}
		int enemyCount = (rand.nextInt(4) + 1 == 4) ? 2 : 1;
		List<Enemy> enemies = createEnemies(enemyCount, rand);
		List<Action> allActions = getEnemyActions(enemies, rand);
		hero().resetEnergie();
		hero().resetMana();
		hero().rechargerManaStones();
		startFight(enemies, allActions);
		recomputeArmorBonus();
	}

	/**
	 * Create random enemies.
	 */
	private List<Enemy> createEnemies(int count, RandomGenerator rand) {
		List<Enemy> enemies = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			enemies.add(EnemyCreation.createRandom(rand));
		}
		return enemies;
	}

	/**
	 * Get all enemy actions for the round.
	 */
	private List<Action> getEnemyActions(List<Enemy> enemies, RandomGenerator rand) {
		List<Action> allActions = new ArrayList<>();
		for (var enemy : enemies) {
			allActions.addAll(enemy.randomActions(rand));
		}
		return allActions;
	}

	/**
	 * Handle treasure room entry.
	 */
	private void handleTreasureRoomEntry(Room room, RandomGenerator rand) {
		Gear item = RandomGear.randomGear(rand);
		setPendingItem(item);
		if (room != null)
			room.complete();
	}

	/**
	 * Handle healer room entry.
	 */
	private void handleHealerRoomEntry(Room room) {
		startHealer();
		if (room != null)
			room.complete();
	}

	/**
	 * Handle merchant room entry.
	 */
	private void handleMerchantRoomEntry(Room room, RandomGenerator rand) {
		startMerchant();
		initMerchantSlots(rand);
		if (room != null)
			room.complete();
	}

	/**
	 * Try to place an item in the backpack at the given position and rotation.
	 */
	public boolean tryPlaceItem(Equipment item, int line, int col, boolean rotation) {
		Objects.requireNonNull(item);
		if (backpack().canPlace(item, line, col, rotation)) {
			backpack().place(item, line, col, rotation);
			clearPendingItem();
			// Retirer des récompenses si c'était une récompense
			if (!combatRewards.isEmpty() && originalPosition() == null) {
				var tmp = new ArrayList<Equipment>(combatRewards);
				tmp.remove(0);
				combatRewards = tmp;
			}
			// Retirer du marchand si c'était du marchand
			if (merchantSelectedIndex() != -1 && originalPosition() == null) {
				var merchantItem = merchantSlots.get(merchantSelectedIndex());
				int price = getPriceFromRarity(merchantItem.rarity());
				if (backpack().pay(price)) {
					var tmp = new ArrayList<Equipment>(merchantSlots);
					tmp.remove(merchantSelectedIndex());
					merchantSlots = tmp;
				}
				clearMerchantSelection();
			}
			return true;
		}
		return false;
	}

}
