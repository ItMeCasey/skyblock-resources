package com.itmecasey.skyblockresources.devtest;

import com.itmecasey.skyblockresources.SkyblockResourcesMod;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * A development-only check of the recipe viewer plugins. It is compiled only with {@code -PviewerCheck} (see
 * build.gradle) and does nothing unless the environment variable {@code SKYRES_SHOTS} is {@code bei}. It opens a fresh
 * creative superflat world, opens the inventory with the viewer's browser, searches for items, opens recipe popups and
 * saves a screenshot of each into {@code run/screenshots} as {@code skyres_<viewer>_*.png}, then quits. The viewer is
 * reached through reflection and the game's own mouse handler, so nothing here touches the desktop cursor or keyboard.
 * It always quits the client: when the plan finishes, when a step fails, and after a time limit.
 */
@Mod.EventBusSubscriber(modid = SkyblockResourcesMod.MODID, value = Dist.CLIENT)
public final class BeiTour {
	private static final boolean ENABLED = "bei".equalsIgnoreCase(System.getenv("SKYRES_SHOTS"));
	private static final String WORLD = "skyres_shots";
	/** Set SKYRES_WORLD to the name of a world in run/saves to open it instead of making a fresh one (Forge needs a fire-free flat world). */
	private static final String OPEN_WORLD = System.getenv("SKYRES_WORLD");
	/** About eight minutes of client ticks: after this the client quits whatever happened. */
	private static final int LIMIT_TICKS = 9600;

	private enum Stage {
		WAITING_FOR_MENU, LOADING_WORLD, TOURING, DONE
	}

	/** One thing to do, after waiting the given number of ticks. */
	private record Step(int delay, Runnable action) {
	}

	private static final Deque<Step> STEPS = new ArrayDeque<>();
	private static Stage stage = Stage.WAITING_FOR_MENU;
	private static int idle;
	private static int wait;
	private static int ticks;

	private BeiTour() {
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (!ENABLED || stage == Stage.DONE || event.phase != TickEvent.Phase.END) {
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		try {
			if (++ticks > LIMIT_TICKS) {
				SkyblockResourcesMod.LOGGER.warn("SKYRES-SHOTS time limit reached, quitting");
				quit();
				return;
			}
			if (minecraft.mouseHandler.isMouseGrabbed()) {
				minecraft.mouseHandler.releaseMouse();
			}
			switch (stage) {
				case WAITING_FOR_MENU -> waitForMenu(minecraft);
				case LOADING_WORLD -> waitForWorld(minecraft);
				case TOURING -> runSteps();
				default -> {
				}
			}
		} catch (Throwable t) {
			SkyblockResourcesMod.LOGGER.error("SKYRES-SHOTS failed, quitting", t);
			quit();
		}
	}

	private static void quit() {
		stage = Stage.DONE;
		Minecraft.getInstance().stop();
	}

	private static void waitForMenu(Minecraft minecraft) {
		if (minecraft.getOverlay() != null || minecraft.level != null || minecraft.screen == null || ++idle < 20) {
			return;
		}
		minecraft.options.pauseOnLostFocus = false;
		LevelStorageSource source = minecraft.getLevelSource();
		if (OPEN_WORLD != null && !OPEN_WORLD.isBlank() && source.levelExists(OPEN_WORLD)) {
			stage = Stage.LOADING_WORLD;
			idle = 0;
			SkyblockResourcesMod.LOGGER.info("SKYRES-SHOTS opening world {}", OPEN_WORLD);
			minecraft.createWorldOpenFlows().openWorld(OPEN_WORLD, () -> {
			});
			return;
		}
		if (source.levelExists(WORLD)) {
			try (LevelStorageSource.LevelStorageAccess access = source.createAccess(WORLD)) {
				access.deleteLevel();
			} catch (IOException e) {
				SkyblockResourcesMod.LOGGER.warn("SKYRES-SHOTS could not delete the old world", e);
			}
		}
		LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE, false, Difficulty.PEACEFUL, true, new GameRules(), WorldDataConfiguration.DEFAULT);
		stage = Stage.LOADING_WORLD;
		idle = 0;
		SkyblockResourcesMod.LOGGER.info("SKYRES-SHOTS creating world");
		minecraft.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(0L, false, false),
				registries -> registries.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions());
	}

	private static void waitForWorld(Minecraft minecraft) {
		if (minecraft.level == null || minecraft.player == null || minecraft.screen != null || minecraft.getSingleplayerServer() == null || ++idle < 40) {
			return;
		}
		SkyblockResourcesMod.LOGGER.info("SKYRES-SHOTS in the world");
		plan();
		stage = Stage.TOURING;
	}

	private static void runSteps() {
		if (wait > 0) {
			wait--;
			return;
		}
		Step step = STEPS.poll();
		if (step == null) {
			return;
		}
		step.action().run();
		Step next = STEPS.peek();
		wait = next == null ? 0 : next.delay();
	}

	// --- The check ---------------------------------------------------------------------------

	private static void plan() {
		then(0, () -> server("gamerule doFireTick false", "gamerule doDaylightCycle false", "time set noon", "gamemode survival @a"));
		then(20, () -> Minecraft.getInstance().setScreen(new InventoryScreen(player())));
		then(40, () -> search("@skyblock_resources"));
		hoverFirst();
		shoot("browser", 20);
		openPopup("skyblock_resources:wooden_generator", null, "popup_wooden_generator");
		openPopup("skyblock_resources:netherite_generator", null, "popup_netherite_generator");
		openPopup("minecraft:cobblestone", "skyblock_resources:resource_generator", "popup_cobblestone_resource_generator");
		openPopup("minecraft:apple", "skyblock_resources:tree_drops", "popup_apple_tree_drops");
		openPopup("minecraft:carrot", "skyblock_resources:crop_drops", "popup_carrot_crop_drops");
		openPopup("minecraft:farmland", "skyblock_resources:soil_type", "popup_farmland_soil_type");
		then(40, () -> {
			SkyblockResourcesMod.LOGGER.info("SKYRES-SHOTS DONE");
			quit();
		});
	}

	/** Opens BEI's recipe popup for an item directly, on one category's tab when a category is given. */
	private static void openPopup(String itemId, String categoryId, String shot) {
		then(20, () -> {
			try {
				Class<?> popup = Class.forName("com.itmecasey.bei.client.RecipePopup");
				popup.getMethod("close").invoke(null);
				net.minecraft.world.item.Item found = null;
				for (net.minecraft.world.item.Item item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
					if (net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).toString().equals(itemId)) {
						found = item;
						break;
					}
				}
				popup.getMethod("open", net.minecraft.world.item.Item.class, boolean.class).invoke(null, found, false);
				if (categoryId != null) {
					java.lang.reflect.Field key = popup.getDeclaredField("selectedKey");
					key.setAccessible(true);
					key.set(null, "cat:" + categoryId);
				}
			} catch (ReflectiveOperationException e) {
				SkyblockResourcesMod.LOGGER.warn("SKYRES-SHOTS could not open the popup for {}", itemId, e);
			}
		});
		then(30, () -> SkyblockResourcesMod.LOGGER.info("SKYRES-SHOTS BEI '{}' popup open={}", itemId, popupOpen()));
		shoot(shot, 10);
	}

	private static void hoverFirst() {
		then(30, BeiTour::moveToFirstCell);
		then(10, BeiTour::moveToFirstCell);
	}

	private static Object field(String name) throws ReflectiveOperationException {
		java.lang.reflect.Field field = Class.forName("com.itmecasey.bei.client.BrowserPanel").getDeclaredField(name);
		field.setAccessible(true);
		return field.get(null);
	}

	private static void search(String text) {
		try {
			((net.minecraft.client.gui.components.EditBox) field("box")).setValue(text);
		} catch (ReflectiveOperationException e) {
			SkyblockResourcesMod.LOGGER.warn("SKYRES-SHOTS could not reach BEI's search box", e);
		}
	}

	private static boolean popupOpen() {
		try {
			return (Boolean) Class.forName("com.itmecasey.bei.client.RecipePopup").getMethod("isOpen").invoke(null);
		} catch (ReflectiveOperationException e) {
			return false;
		}
	}

	/** Moves the game's own cursor over the first cell of BEI's grid (twice: the game ignores a first move). */
	private static void moveToFirstCell() {
		try {
			Minecraft game = Minecraft.getInstance();
			int gx = (Integer) field("gridX") + 9;
			int gy = (Integer) field("gridY") + 9;
			double sx = gx * (double) game.getWindow().getScreenWidth() / game.getWindow().getGuiScaledWidth();
			double sy = gy * (double) game.getWindow().getScreenHeight() / game.getWindow().getGuiScaledHeight();
			java.lang.reflect.Method move = net.minecraft.client.MouseHandler.class.getDeclaredMethod("onMove", long.class, double.class, double.class);
			move.setAccessible(true);
			move.invoke(game.mouseHandler, game.getWindow().getWindow(), sx + 3, sy);
			move.invoke(game.mouseHandler, game.getWindow().getWindow(), sx, sy);
		} catch (ReflectiveOperationException e) {
			SkyblockResourcesMod.LOGGER.warn("SKYRES-SHOTS could not move the game cursor", e);
		}
	}

	/** A left click through the game's own mouse handler, at wherever the game cursor is. */
	private static void mouseClick() {
		try {
			Minecraft game = Minecraft.getInstance();
			java.lang.reflect.Method press = net.minecraft.client.MouseHandler.class.getDeclaredMethod("onPress", long.class, int.class, int.class, int.class);
			press.setAccessible(true);
			press.invoke(game.mouseHandler, game.getWindow().getWindow(), 0, 1, 0);
			press.invoke(game.mouseHandler, game.getWindow().getWindow(), 0, 0, 0);
		} catch (ReflectiveOperationException e) {
			SkyblockResourcesMod.LOGGER.warn("SKYRES-SHOTS could not click", e);
		}
	}

	private static void shoot(String name, int delay) {
		then(delay, () -> {
			Minecraft minecraft = Minecraft.getInstance();
			minecraft.gui.getChat().clearMessages(false);
			minecraft.getToasts().clear();
		});
		then(2, () -> {
			Minecraft minecraft = Minecraft.getInstance();
			Screenshot.grab(minecraft.gameDirectory, "skyres_bei_" + name + ".png", minecraft.getMainRenderTarget(), message -> SkyblockResourcesMod.LOGGER.info("SKYRES-SHOTS saved {}", name));
		});
	}

	private static void then(int delay, Runnable action) {
		STEPS.add(new Step(delay, action));
	}

	private static LocalPlayer player() {
		return Minecraft.getInstance().player;
	}

	private static void server(String... commands) {
		MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
		if (server != null) {
			Consumer<MinecraftServer> work = s -> {
				for (String command : commands) {
					s.getCommands().performPrefixedCommand(s.createCommandSourceStack(), command);
				}
			};
			server.execute(() -> work.accept(server));
		}
	}
}
