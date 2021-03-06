/*
 * Copyright (C) 2018  Katrix
 * This file is part of DanmakuCore.
 *
 * DanmakuCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DanmakuCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with DanmakuCore.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.katsstuff.teamnightclipse.danmakucore.client.helper

import net.katsstuff.teamnightclipse.danmakucore.DanmakuCore
import net.katsstuff.teamnightclipse.danmakucore.danmaku.form.IRenderForm
import net.katsstuff.teamnightclipse.danmakucore.data.ShotData
import net.katsstuff.teamnightclipse.mirror.client.shaders._
import net.katsstuff.teamnightclipse.mirror.data.Quat
import net.minecraft.client.renderer.{GlStateManager, OpenGlHelper}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

//Many render methods adopted from GLU classes
@SideOnly(Side.CLIENT)
object DanCoreRenderHelper {

  val OverwriteColorEdge = 0xFF0000
  private val ocer       = (OverwriteColorEdge >> 16 & 255) / 255F
  private val oceg       = (OverwriteColorEdge >> 8 & 255) / 255F
  private val oceb       = (OverwriteColorEdge & 255) / 255F

  val OverwriteColorCore = 0x00FF00
  private val occr       = (OverwriteColorCore >> 16 & 255) / 255F
  private val occg       = (OverwriteColorCore >> 8 & 255) / 255F
  private val occb       = (OverwriteColorCore & 255) / 255F

  private val swizzleRegex = """^.+\.[rgbaxyzwstpq]$"""

  val baseDanmakuShaderLoc: ResourceLocation  = DanmakuCore.resource("shaders/danmaku")
  val fancyDanmakuShaderLoc: ResourceLocation = DanmakuCore.resource("shaders/danmaku_fancy")
  val texturedDanmakuShaderLoc: ResourceLocation = DanmakuCore.resource("shaders/danmaku_textured")

  def initialize(): Unit = {
    if (OpenGlHelper.shadersSupported) {
      ShaderManager.initProgram(
        baseDanmakuShaderLoc,
        Seq(ShaderType.Vertex),
        Map(
          "overwriteColorEdge" -> UniformBase(UniformType.Vec3, 1),
          "overwriteColorCore" -> UniformBase(UniformType.Vec3, 1),
          "coreColor"          -> UniformBase(UniformType.Vec3, 1),
          "edgeColor"          -> UniformBase(UniformType.Vec3, 1)
        ),
        shader => {
          shader.begin()
          shader.getUniformS("overwriteColorEdge").foreach { uniform =>
            uniform.set(ocer, oceg, oceb)
            uniform.upload()
          }

          shader.getUniformS("overwriteColorCore").foreach { uniform =>
            uniform.set(occr, occg, occb)
            uniform.upload()
          }
          shader.end()
        }
      )

      ShaderManager.initProgram(
        fancyDanmakuShaderLoc,
        Seq(ShaderType.Vertex, ShaderType.Fragment),
        Map(
          "coreColor"    -> UniformBase(UniformType.Vec3, 1),
          "edgeColor"    -> UniformBase(UniformType.Vec3, 1),
          "coreSize"     -> UniformBase(UniformType.UnFloat, 1),
          "coreHardness" -> UniformBase(UniformType.UnFloat, 1),
          "edgeHardness" -> UniformBase(UniformType.UnFloat, 1),
          "edgeGlow"     -> UniformBase(UniformType.UnFloat, 1)
        ),
        shader => {
          shader.begin()
          shader.getUniformS("coreColor").foreach { uniform =>
            uniform.set(1F, 1F, 1F)
            uniform.upload()
          }
          shader.getUniformS("edgeColor").foreach { uniform =>
            uniform.set(1F, 0F, 0F)
            uniform.upload()
          }
          shader.end()
        }
      )

      ShaderManager.initProgram(
        texturedDanmakuShaderLoc,
        Seq(ShaderType.Vertex, ShaderType.Fragment),
        Map(
          "coreColor"    -> UniformBase(UniformType.Vec3, 1),
          "edgeColor"    -> UniformBase(UniformType.Vec3, 1)
        ),
        shader => {
          shader.begin()
          shader.getUniformS("coreColor").foreach { uniform =>
            uniform.set(1F, 1F, 1F)
            uniform.upload()
          }
          shader.getUniformS("edgeColor").foreach { uniform =>
            uniform.set(1F, 0F, 0F)
            uniform.upload()
          }
          shader.end()
        }
      )
    }
  }

  def transformDanmaku(shot: ShotData, orientation: Quat): Unit = {
    GlStateManager.rotate(orientation.toQuaternion)
    GlStateManager.scale(shot.getSizeX, shot.getSizeY, shot.getSizeZ)
  }

  def danmakuShaderProgram: Option[MirrorShaderProgram] = ShaderManager.getProgram(baseDanmakuShaderLoc)

  def updateDanmakuShaderAttributes(shaderProgram: MirrorShaderProgram, form: IRenderForm, shot: ShotData): Unit = {
    val edgeColor = shot.edgeColor
    val er        = (edgeColor >> 16 & 255) / 255F
    val eg        = (edgeColor >> 8 & 255) / 255F
    val eb        = (edgeColor & 255) / 255F

    val coreColor = shot.coreColor
    val cr        = (coreColor >> 16 & 255) / 255F
    val cg        = (coreColor >> 8 & 255) / 255F
    val cb        = (coreColor & 255) / 255F

    val attributeMap = form.defaultAttributeValues.keys.map { k =>
      val isSwizzle   = k.matches(swizzleRegex)
      val newKey      = if (isSwizzle) k.dropRight(2) else k
      val swizzleChar = if (isSwizzle) Option(k.last) else None
      newKey -> (swizzleChar, shot.renderProperties.getOrElse(k, form.defaultAttributeValues(k).default))
    }.toMap

    attributeMap
      .flatMap { t =>
        val res: Option[((Option[Char], Float), MirrorUniform[_ <: UniformType])] =
          shaderProgram.getUniformS(t._1).map(t._2 -> _)
        res
      }
      .foreach {
        case ((swizzleChar, value), uniform) =>
          swizzleChar match {
            case Some('r' | 'x' | 's') => uniform.setIdx(value, 0)
            case Some('g' | 'y' | 's') => uniform.setIdx(value, 1)
            case Some('b' | 'z' | 'p') => uniform.setIdx(value, 2)
            case Some('a' | 'w' | 'q') => uniform.setIdx(value, 3)
            case _                     => uniform.set(value)
          }

          uniform.upload()
      }

    shaderProgram.getUniformS("coreColor").foreach { uniform =>
      uniform.set(cr, cg, cb)
      uniform.upload()
    }

    shaderProgram.getUniformS("edgeColor").foreach { uniform =>
      uniform.set(er, eg, eb)
      uniform.upload()
    }
  }
}
