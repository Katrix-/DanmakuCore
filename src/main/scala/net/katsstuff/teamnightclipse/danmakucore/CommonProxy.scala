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
package net.katsstuff.teamnightclipse.danmakucore

import scala.collection.JavaConverters._
import scala.reflect.ClassTag

import net.katsstuff.teamnightclipse.danmakucore.danmaku._
import net.katsstuff.teamnightclipse.danmakucore.danmaku.form.Form
import net.katsstuff.teamnightclipse.danmakucore.danmaku.subentity.SubEntityType
import net.katsstuff.teamnightclipse.danmakucore.danmodel.DanModelReader
import net.katsstuff.teamnightclipse.danmakucore.entity.living.boss.EntityDanmakuBoss
import net.katsstuff.teamnightclipse.danmakucore.entity.living.phase.PhaseType
import net.katsstuff.teamnightclipse.danmakucore.entity.spellcard.{EntitySpellcard, Spellcard}
import net.katsstuff.teamnightclipse.danmakucore.entity.{EntityFallingData, EntityInfo}
import net.katsstuff.teamnightclipse.danmakucore.impl.danmakuvariant.DanmakuVariantGeneric
import net.katsstuff.teamnightclipse.danmakucore.impl.form._
import net.katsstuff.teamnightclipse.danmakucore.impl.phase._
import net.katsstuff.teamnightclipse.danmakucore.impl.spellcard.SpellcardDelusionEnlightenment
import net.katsstuff.teamnightclipse.danmakucore.impl.subentity._
import net.katsstuff.teamnightclipse.danmakucore.item.{ItemDanmaku, ItemSpellcard}
import net.katsstuff.teamnightclipse.danmakucore.lib._
import net.katsstuff.teamnightclipse.danmakucore.lib.data.LibShotData
import net.katsstuff.teamnightclipse.danmakucore.network.SpellcardInfoPacket
import net.katsstuff.teamnightclipse.mirror.client.particles.GlowTexture
import net.katsstuff.teamnightclipse.mirror.data.Vector3
import net.katsstuff.teamnightclipse.mirror.misc.IdState
import net.minecraft.entity.Entity
import net.minecraft.init.SoundEvents
import net.minecraft.item.Item
import net.minecraft.util.{IThreadListener, ResourceLocation, SoundEvent}
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.event.{FMLServerStartingEvent, FMLServerStoppedEvent}
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.{EntityEntry, EntityEntryBuilder}
import net.minecraftforge.registries.{IForgeRegistryEntry, RegistryBuilder}

object CommonProxy {

  @SubscribeEvent
  def registerItems(event: RegistryEvent.Register[Item]): Unit =
    event.getRegistry.registerAll(new ItemDanmaku, new ItemSpellcard)

  @SubscribeEvent
  def registerForms(event: RegistryEvent.Register[Form]): Unit = {
    def noteForm(name: String, sound: SoundEvent, location: String) =
      new FormNote(name, sound, DanmakuCore.resource(location))

    event.getRegistry
      .registerAll(
        new FormCrystal1,
        new FormCrystal2,
        new FormTexturedStatic(LibFormName.KUNAI, 1D, 2D, DanmakuCore.resource("textures/entity/danmaku/kunai.png")),
        new FormPointedSphere,
        new FormScale,
        new FormSphere,
        new FormSphereCircle,
        new FormStar,
        new FormControl,
        new FormFire,
        new FormLaser,
        DanModelReader.createForm(new ResourceLocation(LibMod.Id, "models/form/heart"), LibFormName.HEART),
        noteForm(LibFormName.NOTE1, SoundEvents.BLOCK_NOTE_HARP, "models/form/note1"),
        new FormBubble,
        new FormTexturedStatic(
          LibFormName.TALISMAN,
          1D,
          20D / 16D,
          DanmakuCore.resource("textures/entity/danmaku/talisman.png")
        ),
        new FormTexturedStatic(LibFormName.HEART_CARD, 1D, 1D, DanmakuCore.resource("textures/entity/danmaku/heart_card.png")),
        new FormTexturedStatic(LibFormName.RHOMB_CARD, 1D, 1D, DanmakuCore.resource("textures/entity/danmaku/rhomb_card.png"))
      )
  }

  @SubscribeEvent
  def registerSubEntities(event: RegistryEvent.Register[SubEntityType]): Unit = {
    event.getRegistry
      .registerAll(
        new SubEntityTypeDefault(LibSubEntityName.DEFAULT),
        new SubEntityTypeFire(LibSubEntityName.FIRE, 2F),
        new SubEntityTypeExplosion(LibSubEntityName.EXPLODE, 3F),
        new SubEntityTypeTeleport(LibSubEntityName.TELEPORT),
        new SubEntityTypeDanmakuExplosion(LibSubEntityName.DANMAKU_EXPLODE),
        new SubEntityTypeRainbow(LibSubEntityName.RAINBOW),
        new SubEntityTypeShiftingRainbow(LibSubEntityName.SHIFTING_RAINBOW)
      )
  }

  @SubscribeEvent
  def registerSpellcards(event: RegistryEvent.Register[Spellcard]): Unit =
    event.getRegistry.registerAll(new SpellcardDelusionEnlightenment)

  @SubscribeEvent
  def registerPhases(event: RegistryEvent.Register[PhaseType]): Unit = {
    event.getRegistry.registerAll(
      new PhaseTypeFallback().setRegistryName(LibPhaseName.FALLBACK),
      new PhaseTypeSpellcard().setRegistryName(LibPhaseName.SPELLCARD),
      new PhaseTypeShapeCircle().setRegistryName(LibPhaseName.SHAPE_CIRCLE),
      new PhaseTypeShapeRing().setRegistryName(LibPhaseName.SHAPE_RING),
      new PhaseTypeShapeWide().setRegistryName(LibPhaseName.SHAPE_WIDE)
    )
  }

  @SubscribeEvent
  def registerVariants(event: RegistryEvent.Register[DanmakuVariant]): Unit = {
    import LibDanmakuVariantName._

    event.getRegistry
      .registerAll(
        DanmakuVariantGeneric.withSpeed(DEFAULT, () => LibShotData.SHOT_MEDIUM, 0.3D),
        DanmakuVariantGeneric.withSpeed(CIRCLE, () => LibShotData.SHOT_CIRCLE, 0.3D),
        DanmakuVariantGeneric.withSpeed(CRYSTAL1, () => LibShotData.SHOT_CRYSTAL1, 0.4D),
        DanmakuVariantGeneric.withSpeed(CRYSTAL2, () => LibShotData.SHOT_CRYSTAL2, 0.4D),
        DanmakuVariantGeneric.withSpeed(OVAL, () => LibShotData.SHOT_OVAL, 0.2D),
        DanmakuVariantGeneric.withSpeed(SPHERE_DARK, () => LibShotData.SHOT_SPHERE_DARK, 0.3D),
        DanmakuVariantGeneric.withSpeed(PELLET, () => LibShotData.pellet(LibColor.COLOR_VANILLA_RED), 0.4D),
        DanmakuVariantGeneric.withSpeed(STAR_SMALL, () => LibShotData.SHOT_SMALLSTAR, 0.3D),
        DanmakuVariantGeneric.withSpeed(STAR, () => LibShotData.SHOT_STAR, 0.2D),
        DanmakuVariantGeneric.withSpeed(TINY, () => LibShotData.SHOT_TINY, 0.4D),
        DanmakuVariantGeneric.withSpeed(SMALL, () => LibShotData.SHOT_SMALL, 0.4D),
        DanmakuVariantGeneric.withSpeed(KUNAI, () => LibShotData.SHOT_KUNAI, 0.4D),
        DanmakuVariantGeneric.withSpeed(SCALE, () => LibShotData.SHOT_SCALE, 0.4D),
        DanmakuVariantGeneric.withSpeed(RICE, () => LibShotData.SHOT_RICE, 0.4D),
        DanmakuVariantGeneric.withSpeed(POINTED_LASER, () => LibShotData.SHOT_POINTED_LASER, 0.35D),
        DanmakuVariantGeneric.withSpeed(POINTED_LASER_SHORT, () => LibShotData.SHOT_POINTED_LASER_SHORT, 0.4D),
        DanmakuVariantGeneric.withSpeed(POINTED_LASER_LONG, () => LibShotData.SHOT_POINTED_LASER_LONG, 0.3D),
        DanmakuVariantGeneric.withSpeed(FIRE, () => LibShotData.SHOT_FIRE, 0.4D),
        DanmakuVariantGeneric.withSpeed(LASER, () => LibShotData.SHOT_LASER, 0D),
        DanmakuVariantGeneric.withSpeed(HEART, () => LibShotData.SHOT_HEART, 0.4D),
        DanmakuVariantGeneric.withSpeed(NOTE1, () => LibShotData.SHOT_NOTE1, 0.4D),
        DanmakuVariantGeneric.withSpeed(BUBBLE, () => LibShotData.SHOT_BUBBLE, 0.4D),
        DanmakuVariantGeneric.withSpeed(TALISMAN, () => LibShotData.SHOT_TALISMAN, 0.4D),
        DanmakuVariantGeneric.withSpeed(HEART_CARD, () => LibShotData.SHOT_HEART_CARD, 0.4D),
        DanmakuVariantGeneric.withSpeed(RHOMB_CARD, () => LibShotData.SHOT_RHOMB_CARD, 0.4D)
      )
  }

  @SubscribeEvent
  def registerEntities(event: RegistryEvent.Register[EntityEntry]): Unit = {
    def registerEntity[A <: Entity](implicit classTag: ClassTag[A], info: EntityInfo[A]): IdState[EntityEntry] = {
      val clazz = classTag.runtimeClass.asInstanceOf[Class[A]]
      IdState { id =>
        (id + 1, {
          val builder = EntityEntryBuilder
            .create[A]
            .name(info.name)
            .id(info.name, id)
            .entity(clazz)
            .factory(world => info.create(world))
            .tracker(info.tracking.range, info.tracking.updateFrequency, info.tracking.sendVelocityUpdates)

          info.spawn.foreach(s => builder.spawn(s.creatureType, s.weight, s.min, s.max, s.biomes.asJava))
          info.egg.foreach(egg => builder.egg(egg.primary, egg.secondary))

          builder.build()
        })
      }
    }

    event.getRegistry.registerAll(IdState.run0 {
      for {
        spellcard <- registerEntity[EntitySpellcard]
        falling   <- registerEntity[EntityFallingData]
      } yield Seq(spellcard, falling)
    }: _*)

  }

  @SubscribeEvent
  def registerSounds(event: RegistryEvent.Register[SoundEvent]): Unit = {
    event.getRegistry.registerAll(
      LibSounds.ENEMY_POWER,
      LibSounds.DAMAGE,
      LibSounds.DAMAGE_LOW,
      LibSounds.BOSS_EXPLODE,
      LibSounds.TIMEOUT,
      LibSounds.SHADOW,
      LibSounds.HIDDEN,
      LibSounds.SUDDEN,
      LibSounds.LASER1,
      LibSounds.LASER2,
      LibSounds.SHOT1,
      LibSounds.SHOT2,
      LibSounds.SHOT3,
      LibSounds.GRAZE,
      LibSounds.SCORE
    )
  }

  @SubscribeEvent
  def createRegistries(event: RegistryEvent.NewRegistry): Unit = {
    createRegistry[Form](LibRegistryName.FORMS, Some(DanmakuCore.resource(LibFormName.DEFAULT)))
    createRegistry[SubEntityType](LibRegistryName.SUB_ENTITIES, Some(DanmakuCore.resource(LibSubEntityName.DEFAULT)))
    createRegistry[DanmakuVariant](LibRegistryName.VARIANTS, Some(DanmakuCore.resource(LibDanmakuVariantName.DEFAULT)))
    createRegistry[Spellcard](LibRegistryName.SPELLCARDS, None)
    createRegistry[PhaseType](LibRegistryName.PHASES, Some(DanmakuCore.resource(LibPhaseName.FALLBACK)))
  }

  private def createRegistry[I <: IForgeRegistryEntry[I]](
      name: ResourceLocation,
      defaultValue: Option[ResourceLocation]
  )(implicit classTag: ClassTag[I]) =
    new RegistryBuilder[I]()
      .setName(name)
      .setType(classTag.runtimeClass.asInstanceOf[Class[I]])
      .setIDRange(0, Short.MaxValue)
      .setDefaultKey(defaultValue.orNull)
      .create
}
class CommonProxy {

  def defaultWorld: World = FMLCommonHandler.instance().getMinecraftServerInstance.getEntityWorld

  def scheduler: IThreadListener = FMLCommonHandler.instance().getMinecraftServerInstance

  protected var serverDanmakuHandler: ServerDanmakuHandler = _

  def serverStarting(event: FMLServerStartingEvent): Unit = {
    serverDanmakuHandler = new ServerDanmakuHandler
    MinecraftForge.EVENT_BUS.register(serverDanmakuHandler)
  }

  def serverStopped(event: FMLServerStoppedEvent): Unit = {
    MinecraftForge.EVENT_BUS.unregister(serverDanmakuHandler)
    serverDanmakuHandler = null
  }

  private[danmakucore] def bakeDanmakuVariant(variant: DanmakuVariant): Unit = {}

  private[danmakucore] def initForm(form: Form): Unit = {}

  private[danmakucore] def bakeSpellcard(`type`: Spellcard): Unit = {}

  private[danmakucore] def registerRenderers(): Unit = {}

  private[danmakucore] def bakeRenderModels(): Unit = {}

  private[danmakucore] def registerColors(): Unit = {
    LibColor.registerColor(LibColor.COLOR_VANILLA_WHITE)
    LibColor.registerColor(LibColor.COLOR_VANILLA_ORANGE)
    LibColor.registerColor(LibColor.COLOR_SATURATED_BLUE)
    LibColor.registerColor(LibColor.COLOR_VANILLA_MAGENTA)
    LibColor.registerColor(LibColor.COLOR_VANILLA_LIGHT_BLUE)
    LibColor.registerColor(LibColor.COLOR_VANILLA_YELLOW)
    LibColor.registerColor(LibColor.COLOR_VANILLA_LIME)
    LibColor.registerColor(LibColor.COLOR_VANILLA_PINK)
    LibColor.registerColor(LibColor.COLOR_VANILLA_GRAY)
    LibColor.registerColor(LibColor.COLOR_VANILLA_SILVER)
    LibColor.registerColor(LibColor.COLOR_VANILLA_CYAN)
    LibColor.registerColor(LibColor.COLOR_VANILLA_PURPLE)
    LibColor.registerColor(LibColor.COLOR_VANILLA_BLUE)
    LibColor.registerColor(LibColor.COLOR_VANILLA_BROWN)
    LibColor.registerColor(LibColor.COLOR_VANILLA_GREEN)
    LibColor.registerColor(LibColor.COLOR_VANILLA_RED)
    LibColor.registerColor(LibColor.COLOR_VANILLA_BLACK)
    LibColor.registerColor(LibColor.COLOR_SATURATED_RED)
    LibColor.registerColor(LibColor.COLOR_SATURATED_BLUE)
    LibColor.registerColor(LibColor.COLOR_SATURATED_GREEN)
    LibColor.registerColor(LibColor.COLOR_SATURATED_YELLOW)
    LibColor.registerColor(LibColor.COLOR_SATURATED_MAGENTA)
    LibColor.registerColor(LibColor.COLOR_SATURATED_CYAN)
    LibColor.registerColor(LibColor.COLOR_SATURATED_ORANGE)
    LibColor.registerColor(LibColor.COLOR_WHITE)

    LibColor.registerColor(0xFFFFFF)
    LibColor.registerColor(0x000000)
  }

  private[danmakucore] def registerItemColors(): Unit = {}

  /**
    * Add a [[EntityDanmakuBoss]] to the boss bar render handler.
    */
  private[danmakucore] def addDanmakuBoss(boss: EntityDanmakuBoss): Unit = {}

  /**
    * Removes a [[EntityDanmakuBoss]] from the boss bar render handler.
    */
  private[danmakucore] def removeDanmakuBoss(boss: EntityDanmakuBoss): Unit = {}

  /**
    * Adds a spellcard name to the spellcard renderer
    */
  private[danmakucore] def handleSpellcardInfo(packet: SpellcardInfoPacket): Unit = {}

  def createParticleGlow(
      world: World,
      pos: Vector3,
      motion: Vector3,
      r: Float,
      g: Float,
      b: Float,
      scale: Float,
      lifetime: Int,
      `type`: GlowTexture
  ): Unit = {}

  def createChargeSphere(
      entity: Entity,
      amount: Int,
      offset: Double,
      divSpeed: Double,
      r: Float,
      g: Float,
      b: Float,
      lifetime: Int
  ): Unit = {}

  def updateDanmaku(changes: DanmakuChanges): Unit  = serverDanmakuHandler.updateDanmaku(changes)
  def spawnDanmaku(states: Seq[DanmakuState]): Unit = serverDanmakuHandler.spawnDanmaku(states)

  private[danmakucore] def forceUpdateDanmakuClient(state: DanmakuState): Unit = ()
  private[danmakucore] def updateDanmakuClient(changes: DanmakuChanges): Unit  = ()
  private[danmakucore] def spawnDanmakuClient(states: Seq[DanmakuState]): Unit = ()

  def danmakuHandler: DanmakuHandler = serverDanmakuHandler
}
