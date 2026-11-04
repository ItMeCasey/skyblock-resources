package com.itmecasey.skyblockresources.network;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

import com.itmecasey.skyblockresources.SkyblockResourcesMod;
import com.itmecasey.skyblockresources.init.SkyblockResourcesModRecipeTypes;

/**
 * The mod's network channel on Forge: the saved-data sync that the machines' variables use, and the sync of this mod's
 * recipe types to clients (NeoForge's {@code OnDatapackSyncEvent.sendRecipes}, which Forge does not have).
 */
public final class ForgeNetwork {
	private static SimpleChannel channel;

	private ForgeNetwork() {
	}

	/** Builds the channel. Forge wants channels made while mods load, so the mod constructor calls this. */
	public static void register() {
		channel = ChannelBuilder.named(Identifier.fromNamespaceAndPath(SkyblockResourcesMod.MODID, "main")).networkProtocolVersion(1).simpleChannel()
				.messageBuilder(SkyblockResourcesModVariables.SavedDataSyncMessage.class, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(SkyblockResourcesModVariables.SavedDataSyncMessage::write)
				.decoder(SkyblockResourcesModVariables.SavedDataSyncMessage::read)
				.consumerMainThread((message, context) -> {
					SkyblockResourcesModVariables.SavedDataSyncMessage.handleData(message);
					context.setPacketHandled(true);
				})
				.add()
				.messageBuilder(RecipeSyncMessage.class, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(RecipeSyncMessage::write)
				.decoder(RecipeSyncMessage::read)
				.consumerMainThread((message, context) -> {
					SkyblockResourcesModRecipeTypes.RecipeSync.receive(message.recipes());
					context.setPacketHandled(true);
				})
				.add();
	}

	public static void sendToPlayer(ServerPlayer player, Object message) {
		channel.send(message, PacketDistributor.PLAYER.with(player));
	}

	public static void sendToAll(Object message) {
		channel.send(message, PacketDistributor.ALL.noArg());
	}

	public static void sendToDimension(net.minecraft.server.level.ServerLevel level, Object message) {
		channel.send(message, PacketDistributor.DIMENSION.with(level.dimension()));
	}

	/** This mod's recipes (all four types), sent to a client so it can look them up without the server's recipe manager. */
	public record RecipeSyncMessage(List<RecipeHolder<?>> recipes) {
		@SuppressWarnings({"rawtypes", "unchecked"})
		public void write(RegistryFriendlyByteBuf buffer) {
			buffer.writeVarInt(recipes.size());
			for (RecipeHolder<?> holder : recipes) {
				RecipeSerializer serializer = holder.value().getSerializer();
				buffer.writeIdentifier(holder.id().identifier());
				buffer.writeIdentifier(BuiltInRegistries.RECIPE_SERIALIZER.getKey(serializer));
				serializer.streamCodec().encode(buffer, holder.value());
			}
		}

		@SuppressWarnings({"rawtypes", "unchecked"})
		public static RecipeSyncMessage read(RegistryFriendlyByteBuf buffer) {
			int count = buffer.readVarInt();
			List<RecipeHolder<?>> recipes = new ArrayList<>(count);
			for (int i = 0; i < count; i++) {
				Identifier id = buffer.readIdentifier();
				RecipeSerializer serializer = BuiltInRegistries.RECIPE_SERIALIZER.getValue(buffer.readIdentifier());
				Recipe<?> recipe = (Recipe<?>) serializer.streamCodec().decode(buffer);
				recipes.add(new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, id), recipe));
			}
			return new RecipeSyncMessage(recipes);
		}
	}
}
