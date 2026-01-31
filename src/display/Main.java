package display;

import java.awt.Color;
import java.util.Objects;
import java.nio.file.Path;

import com.github.forax.zen.Application;

public class Main {
	public static void main(String[] args) {
		Objects.requireNonNull(args);
		System.out.println(Path.of("").toAbsolutePath());

		Application.run(Color.BLACK, context -> {
			var screenInfo = context.getScreenInfo();
			var loader = new ImageLoader("data/image", "reine_abeilles.png", "sorcier_grenouille.png",
					"ombre_vivante.png", "battle_axe.png", "bleed_bar.png", "bleed_plate.png", "block_bar_gem.png", "start.png",
					"block_crown_gem.png", "block_plate_gem.png", "charged_stone.png", "corridor.png", "crossbow.png",
					"dagger.png", "damage_bar_gem.png", "damage_crown_gem.png", "damage_plate_gem.png", "dragon_scale_armor.png",
					"enchanted_staff.png", "endturn.png", "enemies.png", "exit.png", "gold.png", "grand_bleed_curse.png",
					"grand_sickness_curse.png", "grand_tonique_dénergie.png", "grand_weakness_curse.png",
					"grand_élixir_de_mana.png", "grande_potion_de_soin.png", "healer.png", "heart_bar_gem.png",
					"heart_crown_gem.png", "heart_plate_gem.png", "hero.png", "heropiece.png", "iron_shield.png",
					"jagged_blade.png", "leather_armor.png", "magic_wand.png", "mana_core.png", "mana_crystal.png",
					"mana_pebble.png", "mana_stone.png", "merchant.png", "paladins_sword.png", "petit_rat_loup.png",
					"petit_tonique_dénergie.png", "petit_élixir_de_mana.png", "petite_potion_de_soin.png", "potion_de_soin.png",
					"rat_loup.png", "short_bow.png", "sickness_bar.png", "sickness_plate.png", "small_block_gem.png",
					"small_damage_gem.png", "small_heart_gem.png", "small_mana_stone.png", "steel_sword.png", "tiny_bleed.png",
					"tiny_sickness.png", "tiny_weakness.png", "tonique_dénergie.png", "tower_shield.png", "treasure.png",
					"weakness_bar.png", "weakness_plate.png", "wooden_shield.png", "wooden_sword.png", "élixir_arcanique.png",
					"élixir_de_mana.png", "élixir_de_vie.png", "élixir_de_vitesse.png", "troll_armor.png", "chainmail_armor.png",
					"knight_plate_armor.png", "dragon_scale_armor_minimal.png","arrow_common.png","arrow_uncommon.png","arrow_rare.png","arrow_legendary.png");

			var data = new GameData();
			var view = new GameView(screenInfo.width(), screenInfo.height(), loader);
			var controller = new GameController();
			GameView.draw(context, data, view);
			// Game loop
			while (controller.gameLoop(context, data, view)) {
			}
		});
	}
}