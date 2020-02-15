package carpetclient.hack;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class BucketHack {

	private EntityPlayerSP player;
	private Vec3d playerVec;
	private Vec3d destroyerVec;
	private Vec3d pickupVec;
	private ArrayList<BlockPos> blockList;
	private World world;
	private Minecraft minecraft;
	private boolean placeBlockStateHackFix;
	private boolean lavaDestroyed;
	private HashMap<BlockPos, IBlockState> map = new HashMap<>();

	public BucketHack(Minecraft minecraft) {
		this.minecraft = minecraft;
		this.player = minecraft.player;
		this.world = minecraft.world;
	}
	
	public void useBucketer() {
		playerVec = player.getPositionVector().add(0,(double)player.getEyeHeight(),0);
		blockList = genBlockList();

		ArrayList<BlockPos> lavaList = getList();
		if(lavaList.size() == 0) return;

		getDestroyerBlock();
		if (destroyerVec == null || pickupVec == null) {
			return;
		}

		Item item = player.getHeldItem(EnumHand.MAIN_HAND).getItem();
		if(item == Items.LAVA_BUCKET || item == Items.BUCKET) {
			placeBlockStateHackFix = true;
			if(item == Items.LAVA_BUCKET) {
				placeOnDestroyerBlock(destroyerVec);
			}
			destroyLava(lavaList);
			placeOnDestroyerBlock(pickupVec);
			if(lavaDestroyed) player.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
		}
	}
	
	private ArrayList<BlockPos> getList() {
		ArrayList<BlockPos> lavaList = new ArrayList<>();
		for(BlockPos pos : blockList) {
			IBlockState iblockstate = world.getBlockState(pos);
			setBlockState(pos, iblockstate);
			Block block = iblockstate.getBlock();
			if(block == Blocks.LAVA && block.getMetaFromState(iblockstate) == 0 ) {
				lavaList.add(pos);
			}else if(block == Blocks.FLOWING_LAVA && block.getMetaFromState(iblockstate) == 0 ) {
				lavaList.add(pos);
			}
		}
		return lavaList;
	}

	public ArrayList<BlockPos> genBlockList() {
		BlockPos playerPos = player.getPosition();
		ArrayList<BlockPos> list = new ArrayList<BlockPos>();
		
		for(int x = -5; x < 6; x++) {
			for(int y = -5; y < 6; y++) {
				for(int z = -5; z < 6; z++) {
					list.add(new BlockPos(playerPos.getX() + x, playerPos.getY() + y, playerPos.getZ() + z));
				}
			}
		}
		
		Collections.sort(list, (BlockPos b1, BlockPos b2) -> {
				Vec3d b1vec = new Vec3d(b1).add(0.5, 0.5, 0.5);
				Vec3d b2vec = new Vec3d(b2).add(0.5, 0.5, 0.5);
				double b1dist = b1vec.distanceTo(playerVec);
				double b2dist = b2vec.distanceTo(playerVec);
				
				if(b1dist == b2dist) {
					return 0;
				}else if(b1dist > b2dist) {
					return 1;
				}else {
					return -1;
				}
			}
		);
		
		return list;
	}
	
	private void getDestroyerBlock() {
		for(int i = blockList.size() - 1; i >= 0; i--) {
			BlockPos pos = blockList.get(i);
			IBlockState iblockstate = world.getBlockState(pos);
			Block block = iblockstate.getBlock();
			if(block.canCollideCheck(iblockstate, false)) {
				Vec3d dest = getBlockSpot(pos, playerVec, iblockstate.getSelectedBoundingBox(this.world, pos), false);
				
				if(dest != null){
					float yaw = getYaw(dest);
					float pitch = getPitch(dest);
					RayTraceResult ray = rayTrace(world, pitch, yaw, false);
					if(ray == null) continue;
					pos = ray.getBlockPos().offset(ray.sideHit);
					IBlockState blockState = getBlockState(pos);
					setBlockState(pos, Blocks.STONE.getDefaultState());
					pickupVec = getBlockSpot(pos, playerVec, new AxisAlignedBB(pos), false);
					setBlockState(pos, blockState);
					destroyerVec = dest;
					return;
				}
			}
		}
	}
	
	private void destroyLava(ArrayList<BlockPos> lavaList){
		for(BlockPos pos : lavaList) {
			lavaDestroyed = true;
			Vec3d v = getBlockSpot(pos, playerVec, new AxisAlignedBB(pos), true);
			if(v == null) continue;
			pickupLava(pos, v);
			placeOnDestroyerBlock(destroyerVec);
		}
	}
	
	private void pickupLava(BlockPos pos, Vec3d v) {
		if(v == null) return;

		player.connection.sendPacket(new CPacketPlayer.PositionRotation(player.posX, player.posY, player.posZ, (float)getYaw(v), (float)getPitch(v), player.onGround));
		player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
		setBlockState(pos, Blocks.AIR.getDefaultState());
	}

	private void placeOnDestroyerBlock(Vec3d v) {
		if(destroyerVec == null) return;

		player.connection.sendPacket(new CPacketPlayer.PositionRotation(player.posX, player.posY, player.posZ, getYaw(v), getPitch(v), player.onGround));
		player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
		
		setLavaOnes();
	}

	private void setLavaOnes() {
		if(placeBlockStateHackFix){
			RayTraceResult ray = rayTrace(world, getPitch(destroyerVec), getYaw(destroyerVec), false);
			if(ray != null){
				BlockPos pos = ray.getBlockPos().offset(ray.sideHit);
				setBlockState(pos, Blocks.FLOWING_LAVA.getDefaultState());
			}
		}
		placeBlockStateHackFix = false;
	}

	private float getYaw(Vec3d v) {
		double x = v.x - playerVec.x;
		double z = v.z - playerVec.z;
		if(x == z) return 0;
		return (float) (Math.atan2(x, z) * 180 / Math.PI * -1);
	}

	private float getPitch(Vec3d v) {
		double x = v.x - playerVec.x;
		double y = v.y - playerVec.y;
		double z = v.z - playerVec.z;

		return (float) (Math.atan(y / Math.sqrt(x*x+z*z)) * 180 / Math.PI * -1);
	}

	public Vec3d getBlockSpot(BlockPos pos, Vec3d vec, AxisAlignedBB bb, boolean useLiquids)
	{
		double d0 = 1.0D / ((bb.maxX - bb.minX) * 2.0D + 1.0D);
		double d1 = 1.0D / ((bb.maxY - bb.minY) * 2.0D + 1.0D);
		double d2 = 1.0D / ((bb.maxZ - bb.minZ) * 2.0D + 1.0D);
		double d3 = (1.0D - Math.floor(1.0D / d0) * d0) / 2.0D;
		double d4 = (1.0D - Math.floor(1.0D / d2) * d2) / 2.0D;

		if (d0 >= 0.0D && d1 >= 0.0D && d2 >= 0.0D)
		{
			int i = 0;
			int j = 0;

			for (float f = 0.0F; f <= 1.0F; f = (float)((double)f + d0))
			{
				for (float f1 = 0.0F; f1 <= 1.0F; f1 = (float)((double)f1 + d1))
				{
					for (float f2 = 0.0F; f2 <= 1.0F; f2 = (float)((double)f2 + d2))
					{
						double d5 = bb.minX + (bb.maxX - bb.minX) * (double)f;
						double d6 = bb.minY + (bb.maxY - bb.minY) * (double)f1;
						double d7 = bb.minZ + (bb.maxZ - bb.minZ) * (double)f2;
						Vec3d spot = new Vec3d(d5 + d3, d6, d7 + d4);

						float yaw = getYaw(spot);
						float pitch = getPitch(spot);
						RayTraceResult ray = rayTrace(world, pitch, yaw, useLiquids);
						if(ray != null && ray.getBlockPos().equals(pos)) {
							return spot;
						}
					}
				}
			}

			return null;
		}
		else
		{
			return null;
		}
	}

	protected RayTraceResult rayTrace(World worldIn, float rotationPitch, float rotationYaw, boolean useLiquids)
	{
		float f = rotationPitch;
		float f1 = rotationYaw;
		double d0 = playerVec.x;
		double d1 = playerVec.y;
		double d2 = playerVec.z;
		Vec3d vec3d = new Vec3d(d0, d1, d2);
		float f2 = MathHelper.cos(-f1 * 0.017453292F - (float)Math.PI);
		float f3 = MathHelper.sin(-f1 * 0.017453292F - (float)Math.PI);
		float f4 = -MathHelper.cos(-f * 0.017453292F);
		float f5 = MathHelper.sin(-f * 0.017453292F);
		float f6 = f3 * f4;
		float f7 = f2 * f4;
		double d3 = 5.0D;
		Vec3d vec3d1 = vec3d.add((double)f6 * d3, (double)f5 * d3, (double)f7 * d3);
		return rayTraceBlocks(vec3d, vec3d1, useLiquids, !useLiquids, false);
	}

	private void setBlockState(BlockPos pos, IBlockState iblockstate) {
		map.put(pos, iblockstate);
	}

	private IBlockState getBlockState(BlockPos pos) {
		return map.get(pos);
	}

	public RayTraceResult rayTraceBlocks(Vec3d start, Vec3d end, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock)
	{
		if (!Double.isNaN(start.x) && !Double.isNaN(start.y) && !Double.isNaN(start.z))
		{
			if (!Double.isNaN(end.x) && !Double.isNaN(end.y) && !Double.isNaN(end.z))
			{
				int endX = MathHelper.floor(end.x);
				int endY = MathHelper.floor(end.y);
				int endZ = MathHelper.floor(end.z);
				int startX = MathHelper.floor(start.x);
				int startY = MathHelper.floor(start.y);
				int startZ = MathHelper.floor(start.z);
				BlockPos blockpos = new BlockPos(startX, startY, startZ);
				IBlockState iblockstate = getBlockState(blockpos);
				Block block = iblockstate.getBlock();

				if ((!ignoreBlockWithoutBoundingBox || iblockstate.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB) && block.canCollideCheck(iblockstate, stopOnLiquid))
				{
					RayTraceResult raytraceresult = iblockstate.collisionRayTrace(world, blockpos, start, end);

					if (raytraceresult != null)
					{
						return raytraceresult;
					}
				}

				RayTraceResult raytraceresult2 = null;
				int k1 = 200;

				while (k1-- >= 0)
				{
					if (Double.isNaN(start.x) || Double.isNaN(start.y) || Double.isNaN(start.z))
					{
						return null;
					}

					if (startX == endX && startY == endY && startZ == endZ)
					{
						return returnLastUncollidableBlock ? raytraceresult2 : null;
					}

					boolean flag2 = true;
					boolean flag = true;
					boolean flag1 = true;
					double d0 = 999.0D;
					double d1 = 999.0D;
					double d2 = 999.0D;

					if (endX > startX)
					{
						d0 = (double)startX + 1.0D;
					}
					else if (endX < startX)
					{
						d0 = (double)startX + 0.0D;
					}
					else
					{
						flag2 = false;
					}

					if (endY > startY)
					{
						d1 = (double)startY + 1.0D;
					}
					else if (endY < startY)
					{
						d1 = (double)startY + 0.0D;
					}
					else
					{
						flag = false;
					}

					if (endZ > startZ)
					{
						d2 = (double)startZ + 1.0D;
					}
					else if (endZ < startZ)
					{
						d2 = (double)startZ + 0.0D;
					}
					else
					{
						flag1 = false;
					}

					double d3 = 999.0D;
					double d4 = 999.0D;
					double d5 = 999.0D;
					double dx = end.x - start.x;
					double dy = end.y - start.y;
					double dz = end.z - start.z;

					if (flag2)
					{
						d3 = (d0 - start.x) / dx;
					}

					if (flag)
					{
						d4 = (d1 - start.y) / dy;
					}

					if (flag1)
					{
						d5 = (d2 - start.z) / dz;
					}

					if (d3 == -0.0D)
					{
						d3 = -1.0E-4D;
					}

					if (d4 == -0.0D)
					{
						d4 = -1.0E-4D;
					}

					if (d5 == -0.0D)
					{
						d5 = -1.0E-4D;
					}

					EnumFacing enumfacing;

					if (d3 < d4 && d3 < d5)
					{
						enumfacing = endX > startX ? EnumFacing.WEST : EnumFacing.EAST;
						start = new Vec3d(d0, start.y + dy * d3, start.z + dz * d3);
					}
					else if (d4 < d5)
					{
						enumfacing = endY > startY ? EnumFacing.DOWN : EnumFacing.UP;
						start = new Vec3d(start.x + dx * d4, d1, start.z + dz * d4);
					}
					else
					{
						enumfacing = endZ > startZ ? EnumFacing.NORTH : EnumFacing.SOUTH;
						start = new Vec3d(start.x + dx * d5, start.y + dy * d5, d2);
					}

					startX = MathHelper.floor(start.x) - (enumfacing == EnumFacing.EAST ? 1 : 0);
					startY = MathHelper.floor(start.y) - (enumfacing == EnumFacing.UP ? 1 : 0);
					startZ = MathHelper.floor(start.z) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
					blockpos = new BlockPos(startX, startY, startZ);
					IBlockState iblockstate1 = getBlockState(blockpos);
					if(iblockstate1 == null) return null;
					Block block1 = iblockstate1.getBlock();

					if (!ignoreBlockWithoutBoundingBox || iblockstate1.getMaterial() == Material.PORTAL || iblockstate1.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB)
					{
						if (block1.canCollideCheck(iblockstate1, stopOnLiquid))
						{
							RayTraceResult raytraceresult1 = iblockstate1.collisionRayTrace(world, blockpos, start, end);

							if (raytraceresult1 != null)
							{
								return raytraceresult1;
							}
						}
						else
						{
							raytraceresult2 = new RayTraceResult(RayTraceResult.Type.MISS, start, enumfacing, blockpos);
						}
					}
				}

				return returnLastUncollidableBlock ? raytraceresult2 : null;
			}
			else
			{
				return null;
			}
		}
		else
		{
			return null;
		}
	}
}
