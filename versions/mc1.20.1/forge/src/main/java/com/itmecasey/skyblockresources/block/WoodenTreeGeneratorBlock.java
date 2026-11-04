package com.itmecasey.skyblockresources.block;

import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Containers;
import net.minecraft.world.Container;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import com.itmecasey.skyblockresources.procedures.WoodenTreeGeneratorOnTickUpdateProcedure;
import com.itmecasey.skyblockresources.procedures.TreeGeneratorOnBlockRightclickedProcedure;
import com.itmecasey.skyblockresources.procedures.TreeGeneratorBlockAddedProcedure;
import com.itmecasey.skyblockresources.block.entity.WoodenTreeGeneratorBlockEntity;

public class WoodenTreeGeneratorBlock extends Block implements EntityBlock {
	private static final VoxelShape SHAPE = Shapes.or(box(1, 2, 1, 15, 3, 2), box(1, 0, 1, 15, 2, 15), box(1, 2, 2, 15, 3, 15));

	public WoodenTreeGeneratorBlock(BlockBehaviour.Properties properties) {
		super(properties.sound(SoundType.WOOD).strength(1f, 10f).noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	public void onPlace(BlockState blockstate, Level world, BlockPos pos, BlockState oldState, boolean moving) {
		super.onPlace(blockstate, world, pos, oldState, moving);
		if (com.itmecasey.skyblockresources.machine.GeneratorTiers.isUpgrading())
			return;
		world.scheduleTick(pos, this, 1);
		TreeGeneratorBlockAddedProcedure.execute(world, pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public void tick(BlockState blockstate, ServerLevel world, BlockPos pos, RandomSource random) {
		super.tick(blockstate, world, pos, random);
		WoodenTreeGeneratorOnTickUpdateProcedure.execute(world, pos.getX(), pos.getY(), pos.getZ());
		world.scheduleTick(pos, this, 1);
	}

	@Override
	public InteractionResult use(BlockState blockstate, Level world, BlockPos pos, Player entity, InteractionHand hand, BlockHitResult hit) {
		super.use(blockstate, world, pos, entity, hand, hit);
		TreeGeneratorOnBlockRightclickedProcedure.execute(world, pos.getX(), pos.getY(), pos.getZ(), hit.getDirection(), entity);
		return InteractionResult.SUCCESS;
	}

	@Override
	public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
		BlockEntity tileEntity = worldIn.getBlockEntity(pos);
		return tileEntity instanceof MenuProvider menuProvider ? menuProvider : null;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new WoodenTreeGeneratorBlockEntity(pos, state);
	}

	@Override
	public boolean triggerEvent(BlockState state, Level world, BlockPos pos, int eventID, int eventParam) {
		super.triggerEvent(state, world, pos, eventID, eventParam);
		BlockEntity blockEntity = world.getBlockEntity(pos);
		return blockEntity != null && blockEntity.triggerEvent(eventID, eventParam);
	}

	@Override
	public void onRemove(BlockState blockstate, Level world, BlockPos blockpos, BlockState newState, boolean flag) {
		if (!blockstate.is(newState.getBlock()) && !com.itmecasey.skyblockresources.machine.GeneratorTiers.isUpgrading()) {
			BlockEntity blockEntity = world.getBlockEntity(blockpos);
			if (blockEntity instanceof Container container) {
				Containers.dropContents(world, blockpos, container);
				world.updateNeighbourForOutputSignal(blockpos, this);
			}
		}
		super.onRemove(blockstate, world, blockpos, newState, flag);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos) {
		BlockEntity tileentity = world.getBlockEntity(pos);
		if (tileentity instanceof WoodenTreeGeneratorBlockEntity be)
			return AbstractContainerMenu.getRedstoneSignalFromContainer(be);
		else
			return 0;
	}
}
