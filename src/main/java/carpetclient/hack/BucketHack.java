package carpetclient.hack;

import carpetclient.mixins.IMixinMinecraft;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;

public class BucketHack {

	private EntityPlayerSP player;
	private Vec3d playerVec;
	private Vec3d destroyerVec;
	private ArrayList<BlockPos> blockList;
	private World world;
	private Minecraft minecraft;
	
	public BucketHack(Minecraft minecraft) {
		this.minecraft = minecraft;
		this.player = minecraft.player;
		this.world = minecraft.world;
	}
	
	public void useBucketer() {
		playerVec = player.getPositionVector().addVector(0,(double)player.getEyeHeight(),0);
		blockList = genBlockList();
		destroyerVec = getDestroyerBlock();
		
		if (destroyerVec == null) {
			return;
		}
		ArrayList<BlockPos> lavaList = getList();
		if(lavaList.size() == 0) return;
		
		Item item = player.getHeldItem(EnumHand.MAIN_HAND).getItem();
		if(item == Items.LAVA_BUCKET || item == Items.BUCKET) {
			if(item == Items.LAVA_BUCKET) {
				placeOnDestroyerBlock();
			}
			getLava(lavaList);
			placeOnDestroyerBlock();
		}

	}
	
	private ArrayList<BlockPos> getList() {
		ArrayList<BlockPos> lavaList = new ArrayList<>();
		for(BlockPos pos : blockList) {
			IBlockState iblockstate = world.getBlockState(pos);
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
				Vec3d b1vec = new Vec3d(b1).addVector(0.5, 0.5, 0.5);
				Vec3d b2vec = new Vec3d(b2).addVector(0.5, 0.5, 0.5);
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
	
	private Vec3d getDestroyerBlock() {
		for(int i = blockList.size() - 1; i >= 0; i--) {
			BlockPos pos = blockList.get(i);
			IBlockState iblockstate = world.getBlockState(pos);
			Block block = iblockstate.getBlock();
			if(block.canCollideCheck(iblockstate, false)) {
				Vec3d dest = getBlockSpot(playerVec, iblockstate.getSelectedBoundingBox(this.world, pos));
				
				if(dest != null) return dest;
			}
		}
		
		return null;
	}
	
	private void getLava(ArrayList<BlockPos> lavaList){
		for(BlockPos pos : lavaList) {
			IBlockState iblockstate = world.getBlockState(pos);
			Vec3d v = getBlockSpot(playerVec, iblockstate.getSelectedBoundingBox(this.world, pos));
			if(v == null) continue;
			pickupLava(v);
			placeOnDestroyerBlock();
		}
	}
	
	private void pickupLava(Vec3d v) {
		if(v == null) return;
		
		player.connection.sendPacket(new CPacketPlayer.PositionRotation(player.posX, player.posY, player.posZ, (float)getYaw(v), (float)getPitch(v), player.onGround));
		((IMixinMinecraft)Minecraft.getMinecraft()).rightClick();
	}

	private void placeOnDestroyerBlock() {
		if(destroyerVec == null) return;
		
		player.connection.sendPacket(new CPacketPlayer.PositionRotation(player.posX, player.posY, player.posZ, (float)getYaw(destroyerVec), (float)getPitch(destroyerVec), player.onGround));
		((IMixinMinecraft)Minecraft.getMinecraft()).rightClick();
	}

	private double getYaw(Vec3d v) {
		double x = v.x - playerVec.x;
		double z = v.z - playerVec.z;
		if(x == z) return 0;
		return Math.atan2(x, z) * 180 / Math.PI * -1;
	}
	
	private double getPitch(Vec3d v) {
		double x = v.x - playerVec.x;
		double y = v.y - playerVec.y;
		double z = v.z - playerVec.z;
		
		return Math.atan(y / Math.sqrt(x*x+z*z)) * 180 / Math.PI * -1;
	}
	
    public Vec3d getBlockSpot(Vec3d vec, AxisAlignedBB bb)
    {
    	Vec3d p = null;
    	double dist = 0;
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
                        if (world.rayTraceBlocks(spot, vec) == null)
                        {
                        	double d = vec.distanceTo(spot);
                        	if(d > 4.5) continue;
                        	
                            if(p == null) {
                            	p = spot;
                            	dist = d;
                            } else if(d < dist) {
                            	p = spot;
                            	dist = d;
                            }
                        }
                    }
                }
            }

            return p;
        }
        else
        {
            return null;
        }
    }
}
