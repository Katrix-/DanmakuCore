/*
 * This class was created by <Katrix>. It's distributed as
 * part of the DanmakuCore Mod. Get the Source Code in github:
 * https://github.com/Katrix-/DanmakuCore
 *
 * DanmakuCore is Open Source and distributed under the
 * the DanmakuCore license: https://github.com/Katrix-/DanmakuCore/blob/master/LICENSE.md
 */
package net.katsstuff.danmakucore.impl.form;

import org.lwjgl.opengl.GL11;

import net.katsstuff.danmakucore.client.helper.RenderHelper;
import net.katsstuff.danmakucore.data.ShotData;
import net.katsstuff.danmakucore.entity.danmaku.EntityDanmaku;
import net.katsstuff.danmakucore.entity.danmaku.form.IRenderForm;
import net.katsstuff.danmakucore.lib.LibFormName;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FormLaser extends FormGeneric {

	public FormLaser() {
		super(LibFormName.LASER);
	}

	@SuppressWarnings("Convert2Lambda")
	@Override
	@SideOnly(Side.CLIENT)
	protected IRenderForm createRenderer() {
		return new IRenderForm() {

			@Override
			@SideOnly(Side.CLIENT)
			public void renderForm(EntityDanmaku danmaku, double x, double y, double z, float entityYaw, float partialTicks,
					RenderManager rendermanager) {
				float pitch = danmaku.rotationPitch;
				float yaw = danmaku.rotationYaw;
				float roll = danmaku.getRoll();
				ShotData shotData = danmaku.getShotData();
				float sizeX = shotData.getSizeX();
				float sizeY = shotData.getSizeY();
				float sizeZ = shotData.getSizeZ();
				int color = shotData.getColor();

				GL11.glRotatef(-yaw, 0F, 1F, 0F);
				GL11.glRotatef(pitch, 1F, 0F, 0F);
				GL11.glRotatef(roll, 0F, 0F, 1F);
				GL11.glScalef(sizeX, sizeY, sizeZ);

				if(shotData.delay() > 0) {
					float scale = 0.025F * Math.min(shotData.delay(), 20);

					GlStateManager.enableBlend();
					GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_ONE);
					GlStateManager.depthMask(false);
					GlStateManager.scale(scale, scale, 1F);
					createCylinder(color, 0.6F);
					GlStateManager.depthMask(true);
					GlStateManager.disableBlend();
				}
				else {
					createCylinder(0xFFFFFF, 1F);

					GlStateManager.enableBlend();
					GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_ONE);
					GlStateManager.depthMask(false);
					GlStateManager.scale(1.2F, 1.2F, 1.2F);
					createCylinder(color, 0.3F);
					GlStateManager.depthMask(true);
					GlStateManager.disableBlend();
				}
			}

			@SideOnly(Side.CLIENT)
			private void createCylinder(int color, float alpha) {
				GL11.glPushMatrix();

				GL11.glTranslatef(0F, 0F, -0.5F);
				RenderHelper.drawDisk(color, alpha);
				GL11.glTranslatef(0F, 0F, 0.5F);
				RenderHelper.drawCylinder(color, alpha);
				GL11.glTranslatef(0F, 0F, 0.5F);
				GL11.glRotatef(180, 0F, 1F, 0F);
				RenderHelper.drawDisk(color, alpha);

				GL11.glPopMatrix();
			}
		};
	}
}