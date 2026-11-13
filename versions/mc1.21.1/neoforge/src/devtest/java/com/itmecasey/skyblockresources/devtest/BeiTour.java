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
	private static final boolean ENABLED = MODE.equals("bei") || MODE.equals("overlay") || MODE.equals("viewer") || MODE.equals("tiers") || MODE.equals("tooltip");
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
				registries -> registries.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), new TitleScreen());
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
		if (MODE.equals("overlay")) {
			overlayPlan();
			return;
		}
		if (MODE.equals("viewer")) {
			viewerPlan();
			return;
		}
		if (MODE.equals("tooltip")) {
			tooltipPlan();
			return;
		}
		if (MODE.equals("tiers")) {
			tiersPlan();
			return;
		}
		then(0, () -> server("difficulty peaceful", "gamerule doFireTick false", "gamerule doDaylightCycle false", "time set noon", "gamemode survival @a"));
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
			server("difficulty peaceful", "gamerule doFireTick false", "gamerule doDaylightCycle false", "time set noon", "gamemode survival @a");
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

	// --- Item tooltips: SKYRES_SHOTS=tooltip ------------------------------------------------------------

	private static void tooltipPlan() {
		then(0, () -> server("difficulty peaceful", "time set noon", "gamemode survival @a", "give @p skyblock_resources:screwdriver", "give @p skyblock_resources:stone_upgrade",
				"give @p skyblock_resources:wooden_generator", "give @p skyblock_resources:wooden_tree_generator", "give @p skyblock_resources:wooden_crop_generator"));
		then(30, () -> Minecraft.getInstance().setScreen(new InventoryScreen(player())));
		hoverItem("skyblock_resources:screwdriver", "tooltip_screwdriver");
		hoverItem("skyblock_resources:stone_upgrade", "tooltip_upgrade_card");
		hoverItem("skyblock_resources:wooden_generator", "tooltip_wooden_generator");
		hoverItem("skyblock_resources:wooden_tree_generator", "tooltip_wooden_tree_generator");
		hoverItem("skyblock_resources:wooden_crop_generator", "tooltip_wooden_crop_generator");
		then(40, () -> {
			SkyblockResourcesMod.LOGGER.info("SKYRES-SHOTS DONE");
			quit();
		});
	}

	/** Moves the game's own cursor over the inventory slot that holds the item, so its tooltip is drawn. */
	private static void hoverItem(String itemId, String shot) {
		then(20, () -> hoverSlot(itemId));
		then(10, () -> hoverSlot(itemId));
		shoot(shot, 20);
	}

	private static void hoverSlot(String itemId) {
		try {
			Minecraft game = Minecraft.getInstance();
			net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> screen = (net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?>) game.screen;
			java.lang.reflect.Field left = net.minecraft.client.gui.screens.inventory.AbstractContainerScreen.class.getDeclaredField("leftPos");
			java.lang.reflect.Field top = net.minecraft.client.gui.screens.inventory.AbstractContainerScreen.class.getDeclaredField("topPos");
			left.setAccessible(true);
			top.setAccessible(true);
			for (net.minecraft.world.inventory.Slot slot : screen.getMenu().slots) {
				if (net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(slot.getItem().getItem()).toString().equals(itemId)) {
					double gx = left.getInt(screen) + slot.x + 8;
					double gy = top.getInt(screen) + slot.y + 8;
					double sx = gx * (double) game.getWindow().getScreenWidth() / game.getWindow().getGuiScaledWidth();
					double sy = gy * (double) game.getWindow().getScreenHeight() / game.getWindow().getGuiScaledHeight();
					java.lang.reflect.Method move = net.minecraft.client.MouseHandler.class.getDeclaredMethod("onMove", long.class, double.class, double.class);
					move.setAccessible(true);
					move.invoke(game.mouseHandler, game.getWindow().getWindow(), sx + 2, sy);
					move.invoke(game.mouseHandler, game.getWindow().getWindow(), sx, sy);
					return;
				}
			}
			SkyblockResourcesMod.LOGGER.warn("SKYRES-SHOTS no slot holds {}", itemId);
		} catch (ReflectiveOperationException | RuntimeException e) {
			SkyblockResourcesMod.LOGGER.warn("SKYRES-SHOTS could not hover {}", itemId, e);
		}
	}

	// --- Other recipe viewers (JEI, REI, EMI): SKYRES_SHOTS=viewer, SKYRES_LABEL=jei|rei|emi ---------------------------

	private static void viewerPlan() {
		then(0, () -> server("difficulty peaceful", "gamerule doFireTick false", "gamerule doDaylightCycle false", "time set noon", "gamemode survival @a"));
		then(20, () -> Minecraft.getInstance().setScreen(new InventoryScreen(player())));
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
				SkyblockResourcesMod.LOGGER.info("SKYRES-SHOTS {} '{}' uses={} shown={} screen={}", LABEL, itemId, uses, ok, Minecraft.getInstance().screen == null ? "none" : Minecraft.getInstance().screen.getClass().getName());
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

	// --- Crop generator tiers in a live world: SKYRES_SHOTS=tiers (add -PjadeCheck for Jade frames) -----------------

	private static final String[] TIERS = {"wooden", "stone", "iron", "diamond", "netherite"};

	private static void tiersPlan() {
		final int[] at = new int[3];
		then(0, () -> SkyblockResourcesMod.LOGGER.info("SKYRES-SHOTS waiting for the player to land"));
		then(120, () -> {
			at[0] = (int) Math.floor(player().getX());
			at[1] = (int) Math.floor(player().getY());
			at[2] = (int) Math.floor(player().getZ());
			server("difficulty peaceful", "gamerule doFireTick false", "gamerule doDaylightCycle false", "time set noon", "gamemode survival @a");
		});
		then(10, () -> {
			String box = (at[0] - 8) + " " + at[1] + " " + at[2] + " " + (at[0] + 8) + " " + (at[1] + 4) + " " + (at[2] + 5);
			String floor = (at[0] - 8) + " " + (at[1] - 1) + " " + at[2] + " " + (at[0] + 8) + " " + (at[1] - 1) + " " + (at[2] + 5);
			server("fill " + box + " air", "fill " + floor + " stone");
			for (int i = 0; i < TIERS.length; i++) {
				int x = at[0] + (i - 2) * 3;
				server("setblock " + x + " " + at[1] + " " + (at[2] + 3) + " skyblock_resources:" + TIERS[i] + "_crop_generator", "setblock " + (x + 1) + " " + at[1] + " " + (at[2] + 3) + " minecraft:chest");
			}
		});
		// A crop generator takes its soil first (right-click with the soil item), then the seed.
		for (int i = 0; i < TIERS.length; i++) {
			final int dx = (i - 2) * 3;
			// Stand next to each generator: the outer ones are beyond reach from the middle.
			then(10, () -> server("tp @p " + (at[0] + dx + 0.5) + " " + at[1] + " " + (at[2] + 0.5)));
			clickWith(at, (i - 2) * 3, "minecraft:farmland");
			clickWith(at, (i - 2) * 3, "minecraft:wheat_seeds");
		}
		then(10, () -> tierReport(at, "after soil and seed"));
		// Side config: a sneak-click with the screwdriver on the east face makes it the output side (the chest sits there).
		then(10, () -> serverWork(s -> {
			net.minecraft.server.level.ServerPlayer sp = s.getPlayerList().getPlayers().get(0);
			net.minecraft.server.level.ServerLevel level = sp.serverLevel();
			net.minecraft.world.item.Item screwdriver = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse("skyblock_resources:screwdriver"));
			sp.setShiftKeyDown(true);
			for (int i = 0; i < TIERS.length; i++) {
				BlockPos pos = new BlockPos(at[0] + (i - 2) * 3, at[1], at[2] + 3);
				net.minecraft.world.item.ItemStack tool = new net.minecraft.world.item.ItemStack(screwdriver);
				BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos).add(0.5, 0, 0), Direction.EAST, pos, false);
				var result = tool.useOn(new net.minecraft.world.item.context.UseOnContext(level, sp, InteractionHand.MAIN_HAND, tool, hit));
				SkyblockResourcesMod.LOGGER.info("SKYRES-TIERS {} screwdriver east -> {} output_direction={}", TIERS[i], result, level.getBlockEntity(pos) == null ? "?" : level.getBlockEntity(pos).getPersistentData().getString("output_direction"));
			}
			sp.setShiftKeyDown(false);
		}));
		// Let the server run 3000 ticks as fast as it can, then read each generator and its chest.
		then(10, () -> server("tick sprint 3000"));
		then(400, () -> tierReport(at, "after 3000 ticks"));
		for (int i = 0; i < TIERS.length; i++) {
			lookAndShoot(at, (i - 2) * 3, "crop_" + TIERS[i]);
		}
		// Break each generator the way a player does (with drops) and count what lands on the ground.
		then(20, () -> serverWork(s -> {
			net.minecraft.server.level.ServerPlayer sp = s.getPlayerList().getPlayers().get(0);
			net.minecraft.server.level.ServerLevel level = sp.serverLevel();
			for (int i = 0; i < TIERS.length; i++) {
				BlockPos pos = new BlockPos(at[0] + (i - 2) * 3, at[1], at[2] + 3);
				boolean broke = level.destroyBlock(pos, true, sp);
				int stacks = 0;
				int items = 0;
				boolean gen = false;
				for (net.minecraft.world.entity.item.ItemEntity e : level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(pos).inflate(1.5))) {
					stacks++;
					items += e.getItem().getCount();
					gen |= e.getItem().getItem().toString().contains("crop_generator");
					e.discard();
				}
				SkyblockResourcesMod.LOGGER.info("SKYRES-TIERS {} break broke={} droppedStacks={} droppedItems={} generatorItemDropped={}", TIERS[i], broke, stacks, items, gen);
			}
		}));
		then(40, () -> {
			SkyblockResourcesMod.LOGGER.info("SKYRES-SHOTS DONE");
			quit();
		});
	}

	private static void tierReport(int[] at, String label) {
		serverWork(s -> {
			net.minecraft.server.level.ServerLevel level = s.getPlayerList().getPlayers().get(0).serverLevel();
			for (int i = 0; i < TIERS.length; i++) {
				BlockPos pos = new BlockPos(at[0] + (i - 2) * 3, at[1], at[2] + 3);
				SkyblockResourcesMod.LOGGER.info("SKYRES-TIERS {} {} generator={} chest={}", TIERS[i], label, contents(level, pos), contents(level, pos.east()));
			}
		});
	}

	private static String contents(net.minecraft.server.level.ServerLevel level, BlockPos pos) {
		if (!(level.getBlockEntity(pos) instanceof net.minecraft.world.Container container)) {
			return "none";
		}
		java.util.Map<String, Integer> sum = new java.util.TreeMap<>();
		for (int slot = 0; slot < container.getContainerSize(); slot++) {
			net.minecraft.world.item.ItemStack stack = container.getItem(slot);
			if (!stack.isEmpty()) {
				sum.merge(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), stack.getCount(), Integer::sum);
			}
		}
		return sum + " slots=" + container.getContainerSize();
	}

	private static void serverWork(Consumer<MinecraftServer> work) {
		MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
		if (server != null) {
			server.execute(() -> {
				try {
					work.accept(server);
				} catch (Throwable t) {
					SkyblockResourcesMod.LOGGER.error("SKYRES-TIERS server step failed", t);
				}
			});
		}
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
			Screenshot.grab(minecraft.gameDirectory, "skyres_" + LABEL + "_" + name + ".png", minecraft.getMainRenderTarget(), message -> SkyblockResourcesMod.LOGGER.info("SKYRES-SHOTS saved {}", name));
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
