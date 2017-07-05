/*
 * This class was created by <Katrix>. It's distributed as
 * part of the DanmakuCore Mod. Get the Source Code in github:
 * https://github.com/Katrix-/DanmakuCore
 *
 * DanmakuCore is Open Source and distributed under the
 * the DanmakuCore license: https://github.com/Katrix-/DanmakuCore/blob/master/LICENSE.md
 */
package net.katsstuff.danmakucore.entity.living.ai.pathfinding;

import net.katsstuff.danmakucore.entity.living.EntityDanmakuCreature;
import net.katsstuff.danmakucore.entity.living.EntityDanmakuMob;
import net.minecraft.entity.EntityCreature;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

//Some code taken from PathNavigateSwimmer
public class PathNavigateFlyer extends PathNavigateGround {

	private final EntityCreature danmakuEntity;

	public PathNavigateFlyer(EntityDanmakuMob danmakuMob, World worldIn) {
		super(danmakuMob, worldIn);
		danmakuEntity = danmakuMob;
	}

	public PathNavigateFlyer(EntityDanmakuCreature danmakuCreature, World worldIn) {
		super(danmakuCreature, worldIn);
		danmakuEntity = danmakuCreature;
	}

	@Override
	protected PathFinder getPathFinder() {
		WalkNodeProcessor walkNodeProcessor = new WalkNodeProcessor();
		walkNodeProcessor.setCanEnterDoors(true);
		nodeProcessor = new DualNodeProcessor(new FlyNodeProcessor(), walkNodeProcessor,
				living -> living instanceof EntityDanmakuMob && ((EntityDanmakuMob)living).isFlying());
		return new PathFinder(new DualNodeProcessor(new FlyNodeProcessor(), walkNodeProcessor,
				living -> living instanceof EntityDanmakuMob && ((EntityDanmakuMob)living).isFlying()));
	}

	@Override
	protected boolean canNavigate() {
		return isEntityFlying() || super.canNavigate();
	}

	@Override
	protected Vec3d getEntityPosition() {
		if(isEntityFlying()) {
			return new Vec3d(this.entity.posX, this.entity.posY + this.entity.height * 0.5D, this.entity.posZ);
		}
		else {
			return super.getEntityPosition();
		}
	}

	@Override
	protected void pathFollow() {
		if(isEntityFlying()) {
			Vec3d vec3d = this.getEntityPosition();
			float f = this.entity.width * this.entity.width;

			if(vec3d.squareDistanceTo(this.currentPath.getVectorFromIndex(this.entity, this.currentPath.getCurrentPathIndex())) < f) {
				this.currentPath.incrementPathIndex();
			}

			for(int j = Math.min(this.currentPath.getCurrentPathIndex() + 6, this.currentPath.getCurrentPathLength() - 1);
					j > this.currentPath.getCurrentPathIndex(); --j) {
				Vec3d vec3d1 = this.currentPath.getVectorFromIndex(this.entity, j);

				if(vec3d1.squareDistanceTo(vec3d) <= 36.0D && this.isDirectPathBetweenPoints(vec3d, vec3d1, 0, 0, 0)) {
					this.currentPath.setCurrentPathIndex(j);
					break;
				}
			}

			this.checkForStuck(vec3d);
		}
		else {
			super.pathFollow();
		}
	}

	@Override
	protected boolean isDirectPathBetweenPoints(Vec3d posVec31, Vec3d posVec32, int sizeX, int sizeY, int sizeZ) {
		if(isEntityFlying()) {
			RayTraceResult raytraceresult = this.world.rayTraceBlocks(posVec31,
					new Vec3d(posVec32.x, posVec32.y + (double)this.entity.height * 0.5D, posVec32.z), false, true, false);
			return raytraceresult == null || raytraceresult.typeOfHit == RayTraceResult.Type.MISS;
		}
		else {
			return super.isDirectPathBetweenPoints(posVec31, posVec32, sizeX, sizeY, sizeZ);
		}
	}

	@Override
	public boolean canEntityStandOnPos(BlockPos pos) {
		if(isEntityFlying()) {
			return !this.world.getBlockState(pos).isFullBlock();
		}
		else {
			return super.canEntityStandOnPos(pos);
		}
	}

	private boolean isEntityFlying() {
		if(danmakuEntity instanceof EntityDanmakuMob) {
			return ((EntityDanmakuMob)danmakuEntity).isFlying();
		}
		else return danmakuEntity instanceof EntityDanmakuCreature && ((EntityDanmakuCreature)danmakuEntity).isFlying();
	}
}
