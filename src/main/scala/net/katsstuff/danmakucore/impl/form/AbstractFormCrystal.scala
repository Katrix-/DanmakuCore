/*
 * This class was created by <Katrix>. It's distributed as
 * part of the DanmakuCore Mod. Get the Source Code in github:
 * https://github.com/Katrix-/DanmakuCore
 *
 * DanmakuCore is Open Source and distributed under the
 * the DanmakuCore license: https://github.com/Katrix-/DanmakuCore/blob/master/LICENSE.md
 */
package net.katsstuff.danmakucore.impl.form

import org.lwjgl.opengl.GL11

import net.katsstuff.danmakucore.client.helper.RenderHelper
import net.katsstuff.danmakucore.entity.danmaku.EntityDanmaku
import net.katsstuff.danmakucore.entity.danmaku.form.IRenderForm
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

private[danmakucore] abstract class AbstractFormCrystal(name: String) extends FormGeneric(name) {

  //noinspection ConvertExpressionToSAM
  @SideOnly(Side.CLIENT)
  override protected def createRenderer: IRenderForm = new IRenderForm() {
    @SideOnly(Side.CLIENT)
    override def renderForm(
        danmaku: EntityDanmaku,
        x: Double,
        y: Double,
        z: Double,
        entityYaw: Float,
        partialTicks: Float,
        rendermanager: RenderManager
    ): Unit = {
      val shotData = danmaku.shotData
      val sizeX    = shotData.getSizeX
      val sizeY    = shotData.getSizeY
      val sizeZ    = shotData.getSizeZ
      val color    = shotData.getColor

      RenderHelper.rotateDanmaku(danmaku)
      GL11.glScalef((sizeX / 3) * 2, (sizeY / 3) * 2, sizeZ)

      createCrystal(0xFFFFFF, 1F)

      GlStateManager.enableBlend()
      GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_ONE)
      GlStateManager.depthMask(false)
      GlStateManager.scale(1.2F, 1.2F, 1.2F)

      createCrystal(color, 0.3F)

      GlStateManager.depthMask(true)
      GlStateManager.disableBlend()
    }
  }

  @SideOnly(Side.CLIENT)
  protected def createCrystal(color: Int, alpha: Float): Unit
}