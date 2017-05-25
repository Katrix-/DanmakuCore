/*
 * This class was created by <Katrix>. It's distributed as
 * part of the DanmakuCore Mod. Get the Source Code in github:
 * https://github.com/Katrix-/DanmakuCore
 *
 * DanmakuCore is Open Source and distributed under the
 * the DanmakuCore license: https://github.com/Katrix-/DanmakuCore/blob/master/LICENSE.md
 */
package net.katsstuff.danmakucore.impl.subentity;

import net.katsstuff.danmakucore.data.RotationData;
import net.katsstuff.danmakucore.data.ShotData;
import net.katsstuff.danmakucore.data.Vector3;
import net.katsstuff.danmakucore.entity.danmaku.EntityDanmaku;
import net.katsstuff.danmakucore.entity.danmaku.subentity.SubEntity;
import net.katsstuff.danmakucore.entity.danmaku.subentity.SubEntityType;
import net.katsstuff.danmakucore.helper.LogHelper;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.world.World;

public class SubEntityTypeDefault extends SubEntityType {

	public SubEntityTypeDefault(String name) {
		super(name);
	}

	@Override
	public SubEntity instantiate(World world, EntityDanmaku entityDanmaku) {
		return new SubEntityDefault(world, entityDanmaku);
	}

	@SuppressWarnings("WeakerAccess")
	public static class SubEntityDefault extends SubEntityAbstract {

		public SubEntityDefault(World world, EntityDanmaku danmaku) {
			super(world, danmaku);
		}

		@Override
		public void subEntityTick() {
			ShotData shot = danmaku.getShotData();
			int delay = shot.delay();
			if(delay > 0) {
				danmaku.ticksExisted--;
				delay--;

				if(delay == 0) {
					if(shot.end() == 1) {
						danmaku.delete();
						return;
					}
					else if(!danmaku.world.isRemote) {
						danmaku.resetMotion();
					}
				}
				else {
					danmaku.motionX = 0;
					danmaku.motionY = 0;
					danmaku.motionZ = 0;
				}

				shot = shot.setDelay(delay);
				danmaku.setShotData(shot);
			}
			else {
				if(!world.isRemote) {
					RotationData rotationData = danmaku.getRotationData();
					if(rotationData.isEnabled() && danmaku.ticksExisted < rotationData.getEndTime()) {
						rotate();
					}

					danmaku.accelerate(danmaku.getCurrentSpeed());

					updateMotionWithGravity();
					hitCheck(entity -> entity != danmaku.getUser().orElse(null) && entity != danmaku.getSource().orElse(null));
				}

				rotateTowardsMovement();

				if(danmaku.isInWater()) {
					waterMovement();
				}
			}
		}
	}
}
