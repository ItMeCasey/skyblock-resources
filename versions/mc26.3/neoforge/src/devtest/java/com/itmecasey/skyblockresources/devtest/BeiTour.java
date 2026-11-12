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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * A development-only check of the recipe viewer plugins. It is compiled only with {@code -PviewerCheck} (see
 * build.gradle) and does nothing unless the environment variable {@code SKYRES_SHOTS} is {@code bei}. It opens a fresh
 * creative superflat world, opens the inventory with the viewer's browser, searches for items, opens recipe popups and
 * saves a screenshot of each into {@code run/screenshots} as {@code skyres_<viewer>_*.png}, then quits. The viewer is
 * reached through reflection and the game's own mouse handler, so nothing here touches the desktop cursor or keyboard.
 * It always quits the client: when the plan finishes, when a step fails, and after a time limit.
 */
@EventBusSubscriber(modid = SkyblockResourcesMod.MODID, value = Dist.CLIENT)
public final class BeiTour {
	private static final String MODE = String.valueOf(System.getenv("SKYRES_SHOTS")).toLowerCase();
	/** "bei" opens the viewer's recipe popups; "overlay" looks at our generators for an in-world info overlay. */
	private static final boolean ENABLED = MODE.equals("bei") || MODE.equals("overlay") || MODE.equals("viewer") || MODE.equals("work");
	/** Names the frames: skyres_<label>_<view>.png (the viewer or overlay mod being checked). */
	private static final String LABEL = System.getenv("SKYRES_LABEL") == null ? MODE : System.getenv("SKYRES_LABEL");
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
	public static void onClientTick(ClientTickEvent.Post event) {
		if (!ENABLED || stage == Stage.DONE) {
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
		if (minecraft.gui.overlay() != null || minecraft.level != null || minecraft.gui.screen() == null || ++idle < 20) {
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
		LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE, new LevelSettings.DifficultySettings(Difficulty.PEACEFUL, false, false), true, WorldDataConfiguration.DEFAULT);
		stage = Stage.LOADING_WORLD;
		idle = 0;
		SkyblockResourcesMod.LOGGER.info("SKYRES-SHOTS creating world");
		minecraft.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(0L, false, false), registries -> registries.lookupOrThrow(net.minecraft.core.registries.Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), new TitleScreen());
	}

	private static void waitForWorld(Minecraft minecraft) {
		if (minecraft.level == null || minecraft.player == null || minecraft.gui.screen() != null || minecraft.getSingleplayerServer() == null || ++idle < 40) {
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
		if (MODE.equals("overlay")) {
			overlayPlan();
			return;
		}
		if (MODE.equals("viewer")) {
			viewerPlan();
			return;
		}
		if (MODE.equals("work")) {
			workPlan();
			return;
		}
		then(0, () -> server("difficulty peaceful", "time set noon", "gamemode survival @a"));
		then(20, () -> Minecraft.getInstance().gui.setScreen(new InventoryScreen(player())));
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

	/**
	 * Builds a small stone platform with a generator of each family in front of the player with vanilla commands, then looks at each
	 * through the game's own camera (a teleport with a rotation) and shoots the overlay the in-world info mod draws.
	 */
	private static void overlayPlan() {
		final int[] at = new int[3];
		// Wait for the player to land before reading the position (a world with terrain drops the player from the spawn height).
		then(0, () -> SkyblockResourcesMod.LOGGER.info("SKYRES-SHOTS waiting for the player to land"));
		then(120, () -> {
			at[0] = (int) Math.floor(player().getX());
			at[1] = (int) Math.floor(player().getY());
			at[2] = (int) Math.floor(player().getZ());
			server("difficulty peaceful", "time set noon", "gamemode survival @a");
		});
		then(10, () -> {
			String box = (at[0] - 4) + " " + at[1] + " " + at[2] + " " + (at[0] + 4) + " " + (at[1] + 4) + " " + (at[2] + 5);
			String floor = (at[0] - 4) + " " + (at[1] - 1) + " " + at[2] + " " + (at[0] + 4) + " " + (at[1] - 1) + " " + (at[2] + 5);
			server("fill " + box + " air", "fill " + floor + " stone", "setblock " + (at[0] - 3) + " " + at[1] + " " + (at[2] + 3) + " skyblock_resources:wooden_generator",
					"setblock " + at[0] + " " + at[1] + " " + (at[2] + 3) + " skyblock_resources:wooden_tree_generator",
					"setblock " + (at[0] + 3) + " " + at[1] + " " + (at[2] + 3) + " skyblock_resources:wooden_crop_generator");
		});
		clickWith(at, -3, "minecraft:cobblestone");
		clickWith(at, 0, "minecraft:oak_sapling");
		clickWith(at, 3, "minecraft:wheat_seeds");
		lookAndShoot(at, -3, "generator");
		lookAndShoot(at, 0, "tree_generator");
		lookAndShoot(at, 3, "crop_generator");
		then(40, () -> {
			SkyblockResourcesMod.LOGGER.info("SKYRES-SHOTS DONE");
			quit();
		});
	}

	// --- Do the generators work: SKYRES_SHOTS=work ----------------------------------------------------------------

	/**
	 * Builds a netherite resource generator and a netherite crop generator on a stone platform, feeds them the way a player
	 * does (right-click with the item), puts a chest above each, waits, and logs what is in each chest as SKYRES-WORK lines.
	 */
	private static void workPlan() {
		final int[] at = new int[3];
		then(0, () -> SkyblockResourcesMod.LOGGER.info("SKYRES-SHOTS waiting for the player to land"));
		then(120, () -> {
			at[0] = (int) Math.floor(player().getX());
			at[1] = (int) Math.floor(player().getY());
			at[2] = (int) Math.floor(player().getZ());
			server("difficulty peaceful", "time set noon", "gamemode survival @a");
		});
		then(10, () -> {
			String box = (at[0] - 4) + " " + at[1] + " " + at[2] + " " + (at[0] + 4) + " " + (at[1] + 4) + " " + (at[2] + 5);
			String floor = (at[0] - 4) + " " + (at[1] - 1) + " " + at[2] + " " + (at[0] + 4) + " " + (at[1] - 1) + " " + (at[2] + 5);
			server("fill " + box + " air", "fill " + floor + " stone", "setblock " + (at[0] - 3) + " " + at[1] + " " + (at[2] + 3) + " skyblock_resources:netherite_generator",
					"setblock " + (at[0] + 3) + " " + at[1] + " " + (at[2] + 3) + " skyblock_resources:netherite_crop_generator");
		});
		clickWith(at, -3, "minecraft:cobblestone");
		clickWith(at, 3, "minecraft:farmland");
		clickWith(at, 3, "minecraft:wheat_seeds");
		then(10, () -> {
			logMachine(at[0] - 3, at[1], at[2] + 3, "resource generator after feeding");
			logMachine(at[0] + 3, at[1], at[2] + 3, "crop generator after feeding");
			server("data merge block " + (at[0] - 3) + " " + at[1] + " " + (at[2] + 3) + " {NeoForgeData:{output_direction:\"UP\",BlockDirections:{item_touched_up:1b,item_out_up:1b}}}", "data merge block " + (at[0] + 3) + " " + at[1] + " " + (at[2] + 3) + " {NeoForgeData:{output_direction:\"UP\",BlockDirections:{item_touched_up:1b,item_out_up:1b}}}");
			server("setblock " + (at[0] - 3) + " " + (at[1] + 1) + " " + (at[2] + 3) + " minecraft:chest", "setblock " + (at[0] + 3) + " " + (at[1] + 1) + " " + (at[2] + 3) + " minecraft:chest");
		});
		then(400, () -> {
			logChest(at[0] - 3, at[1] + 1, at[2] + 3, "resource generator chest");
			logChest(at[0] + 3, at[1] + 1, at[2] + 3, "crop generator chest");
			logMachine(at[0] + 3, at[1], at[2] + 3, "crop generator after waiting");
			logMachine(at[0] - 3, at[1], at[2] + 3, "resource generator after waiting");
		});
		then(700, () -> {
			logChest(at[0] + 3, at[1] + 1, at[2] + 3, "crop generator chest (later)");
		});
		then(20, () -> server("tp @p " + (at[0] + 0.5) + " " + at[1] + " " + (at[2] + 0.5) + " 0 28"));
		shoot("work", 60);
		then(40, () -> {
			SkyblockResourcesMod.LOGGER.info("SKYRES-SHOTS DONE");
			quit();
		});
	}

	private static void logChest(int x, int y, int z, String name) {
		MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
		if (server == null)
			return;
		server.execute(() -> {
			net.minecraft.world.level.block.entity.BlockEntity be = server.overworld().getBlockEntity(new BlockPos(x, y, z));
			StringBuilder out = new StringBuilder();
			if (be instanceof net.minecraft.world.Container container) {
				for (int i = 0; i < container.getContainerSize(); i++) {
					net.minecraft.world.item.ItemStack stack = container.getItem(i);
					if (!stack.isEmpty())
						out.append(stack.getCount()).append(' ').append(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem())).append("; ");
				}
			} else {
				out.append("no container: ").append(be);
			}
			SkyblockResourcesMod.LOGGER.info("SKYRES-WORK {}: {}", name, out.length() == 0 ? "(empty)" : out.toString());
		});
	}

	private static void logMachine(int x, int y, int z, String name) {
		MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
		if (server == null)
			return;
		server.execute(() -> {
			net.minecraft.world.level.block.entity.BlockEntity be = server.overworld().getBlockEntity(new BlockPos(x, y, z));
			SkyblockResourcesMod.LOGGER.info("SKYRES-WORK {}: block={} data={}", name, server.overworld().getBlockState(new BlockPos(x, y, z)).getBlock(), be == null ? "no block entity" : be.getPersistentData());
		});
	}

	// --- Other recipe viewers (JEI, REI, EMI): SKYRES_SHOTS=viewer, SKYRES_LABEL=jei|rei|emi ---------------------------

	private static void viewerPlan() {
		then(0, () -> server("difficulty peaceful", "time set noon", "gamemode survival @a"));
		then(20, () -> Minecraft.getInstance().gui.setScreen(new InventoryScreen(player())));
		shoot("browser", 60);
		viewerShow("minecraft:cobblestone", false, "cobblestone_resource_generator");
		viewerShow("minecraft:apple", false, "apple_tree_drops");
		viewerShow("minecraft:carrot", false, "carrot_crop_drops");
		viewerShow("minecraft:farmland", true, "farmland_soil_type");
		viewerShow("skyblock_resources:wooden_generator", true, "wooden_generator");
		then(40, () -> {
			SkyblockResourcesMod.LOGGER.info("SKYRES-SHOTS DONE");
			quit();
		});
	}

	private static void viewerShow(String itemId, boolean uses, String shot) {
		then(30, () -> {
			net.minecraft.world.item.Item item = itemById(itemId);
			try {
				boolean ok = ViewerShow.show(LABEL, item, uses);
				SkyblockResourcesMod.LOGGER.info("SKYRES-SHOTS {} '{}' uses={} shown={} screen={}", LABEL, itemId, uses, ok, Minecraft.getInstance().gui.screen() == null ? "none" : Minecraft.getInstance().gui.screen().getClass().getName());
			} catch (Throwable t) {
				SkyblockResourcesMod.LOGGER.warn("SKYRES-SHOTS could not show {} in {}", itemId, LABEL, t);
			}
		});
		shoot(shot, 40);
	}

	private static net.minecraft.world.item.Item itemById(String id) {
		for (net.minecraft.world.item.Item item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
			if (net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).toString().equals(id)) {
				return item;
			}
		}
		throw new IllegalStateException("no item " + id);
	}

	/** Gives the player an item and right-clicks the top of the generator, as the crosshair would, so the generator starts working on it. */
	private static void clickWith(int[] at, int dx, String item) {
		then(10, () -> server("clear @p", "give @p " + item + " 64"));
		then(10, () -> {
			BlockPos pos = new BlockPos(at[0] + dx, at[1], at[2] + 3);
			Vec3 hit = new Vec3(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
			Minecraft.getInstance().gameMode.useItemOn(player(), InteractionHand.MAIN_HAND, new BlockHitResult(hit, Direction.UP, pos, false));
		});
	}

	private static void lookAndShoot(int[] at, int dx, String name) {
		then(20, () -> server("tp @p " + (at[0] + dx + 0.5) + " " + at[1] + " " + (at[2] + 0.5) + " 0 28"));
		then(80, () -> SkyblockResourcesMod.LOGGER.info("SKYRES-SHOTS looking at {}", name));
		shoot(name, 30);
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
			move.invoke(game.mouseHandler, game.getWindow().handle(), sx + 3, sy);
			move.invoke(game.mouseHandler, game.getWindow().handle(), sx, sy);
		} catch (ReflectiveOperationException e) {
			SkyblockResourcesMod.LOGGER.warn("SKYRES-SHOTS could not move the game cursor", e);
		}
	}

	/** A left click through the game's own mouse handler, at wherever the game cursor is. */
	private static void mouseClick() {
		try {
			Minecraft game = Minecraft.getInstance();
			java.lang.reflect.Method press = net.minecraft.client.MouseHandler.class.getDeclaredMethod("onButton", long.class, net.minecraft.client.input.MouseButtonInfo.class, int.class);
			press.setAccessible(true);
			press.invoke(game.mouseHandler, game.getWindow().handle(), new net.minecraft.client.input.MouseButtonInfo(0, 0), 1);
			press.invoke(game.mouseHandler, game.getWindow().handle(), new net.minecraft.client.input.MouseButtonInfo(0, 0), 0);
		} catch (ReflectiveOperationException e) {
			SkyblockResourcesMod.LOGGER.warn("SKYRES-SHOTS could not click", e);
		}
	}

	private static void shoot(String name, int delay) {
		then(delay, () -> {
			Minecraft minecraft = Minecraft.getInstance();
			minecraft.gui.hud.getChat().clearMessages(false);
			minecraft.gui.toastManager().clear();
		});
		then(2, () -> {
			Minecraft minecraft = Minecraft.getInstance();
			Screenshot.grab(minecraft.gameDirectory, "skyres_" + LABEL + "_" + name + ".png", minecraft.gameRenderer.mainRenderTarget(), 1, message -> SkyblockResourcesMod.LOGGER.info("SKYRES-SHOTS saved {}", name));
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
