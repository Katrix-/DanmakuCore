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
package net.katsstuff.teamnightclipse.danmakucore.item

import java.util

import javax.annotation.Nullable

import scala.collection.JavaConverters._
import net.katsstuff.teamnightclipse.danmakucore.{DanmakuCore, DanmakuCreativeTab}
import net.katsstuff.teamnightclipse.danmakucore.danmaku.{DanmakuState, DanmakuTemplate, DanmakuVariant}
import net.katsstuff.teamnightclipse.danmakucore.danmaku.form.Form
import net.katsstuff.teamnightclipse.danmakucore.data.ShotData
import net.katsstuff.teamnightclipse.danmakucore.helper.LogHelper
import net.katsstuff.teamnightclipse.danmakucore.helper.MathUtil._
import net.katsstuff.teamnightclipse.danmakucore.lib.data.{LibDanmakuVariants, LibItems, LibSubEntities}
import net.katsstuff.teamnightclipse.danmakucore.lib.{LibColor, LibItemName}
import net.katsstuff.teamnightclipse.danmakucore.misc._
import net.katsstuff.teamnightclipse.danmakucore.registry.{DanmakuRegistry, RegistryValueWithItemModel}
import net.katsstuff.teamnightclipse.danmakucore.scalastuff.DanmakuCreationHelper
import net.katsstuff.teamnightclipse.mirror.client.helper.Tooltip
import net.katsstuff.teamnightclipse.mirror.data.{Quat, Vector3}
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.{ActionResult, EnumActionResult, EnumHand, NonNullList, ResourceLocation}
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

object ItemDanmaku {
  val GravityX: DoubleNBTProperty[ItemStack] = DoubleNBTProperty.ofStack("gravityx")
  val GravityY: DoubleNBTProperty[ItemStack] = DoubleNBTProperty.ofStack("gravityy")
  val GravityZ: DoubleNBTProperty[ItemStack] = DoubleNBTProperty.ofStack("gravityz")
  val Speed: DoubleNBTProperty[ItemStack]    = DoubleNBTProperty.ofStack("speed", 0.4D)
  val DanPattern: NBTProperty[Pattern, ItemStack] = ByteNBTProperty
    .ofStack("pattern")
    .modify(Pattern.fromId(_).get, Pattern.idOf)
  val Amount: IntNBTProperty[ItemStack]       = IntNBTProperty.ofStack("amount", 1)
  val Infinity: BooleanNBTProperty[ItemStack] = BooleanNBTProperty.ofStack("infinity")
  val Custom: BooleanNBTProperty[ItemStack]   = BooleanNBTProperty.ofStack("custom")
  val Variant: NBTProperty[ResourceLocation, ItemStack] = StringNBTProperty
    .ofStack("variant", () => LibDanmakuVariants.DEFAULT_TYPE.fullNameString)
    .modify(new ResourceLocation(_), _.toString)

  def shootDanmaku(
      stack: ItemStack,
      world: World,
      user: Option[EntityLivingBase],
      hand: Option[EnumHand],
      alternateMode: Boolean,
      pos: Vector3,
      direction: Vector3,
      offset: Double
  ): Boolean = {
    val amount    = Amount.get(stack)
    val shotSpeed = Speed.get(stack)
    val gravity   = getGravity(stack)
    val shot      = ShotData.fromNBTItemStack(stack)

    val orientation    = user.fold(Quat.fromAxisAngle(direction, 0D))(Quat.orientationOf(_))
    val danmakuPattern = DanPattern.get(stack)

    if (Custom.get(stack)) {
      val canRun = user match {
        case Some(player: EntityPlayer) => getForm(stack).canRightClick(player, hand.getOrElse(player.getActiveHand))
        case _                          => true
      }
      if (canRun) {

        val template = DanmakuTemplate.builder
          .setUser(user)
          .setWorld(world)
          .setShot(shot)
          .setMovementData(shotSpeed, gravity)
          .setPos(pos)
          .setDirection(direction)
          .setOrientation(orientation)
          .build
        danmakuPattern.makeDanmaku(template, amount, shotSpeed, alternateMode, offset)
        true
      } else false
    } else {
      getVariant(stack).create(world, user, alternateMode, pos, direction, hand).exists { template =>
        val newTemplate =
          template.toBuilder
            .clearBoundingBoxes()
            .setShot(shot)
            .setMovementData(shotSpeed, gravity)
            .build
        danmakuPattern.makeDanmaku(newTemplate, amount, shotSpeed, alternateMode, offset)
        true
      }
    }
  }

  def getGravity(stack: ItemStack): Vector3 = {
    val gravityX = GravityX.get(stack)
    val gravityY = GravityY.get(stack)
    val gravityZ = GravityZ.get(stack)
    new Vector3(gravityX, gravityY, gravityZ)
  }

  def setGravity(gravity: Vector3, stack: ItemStack): Unit = {
    GravityX.set(gravity.x, stack)
    GravityY.set(gravity.y, stack)
    GravityZ.set(gravity.z, stack)
  }

  def getVariant(stack: ItemStack): DanmakuVariant = {
    val variant = DanmakuRegistry.DanmakuVariant.getValue(Variant.get(stack))
    if (variant == null) {
      val default = LibDanmakuVariants.DEFAULT_TYPE
      LogHelper.warn("Found null variant. Changing to default")
      Variant.set(default.fullName, stack)
      default
    } else variant
  }

  def getForm(stack: ItemStack): Form = ShotData.fromNBTItemStack(stack).getForm

  def getController(stack: ItemStack): RegistryValueWithItemModel[_] =
    if (!Custom.get(stack)) getVariant(stack)
    else getForm(stack)

  def createStack(variant: DanmakuVariant): ItemStack = {
    val shot = variant.getShotData.setMainColor(LibColor.randomSaturatedColor)

    val stack = new ItemStack(LibItems.DANMAKU, 1)

    setGravity(variant.getMovementData.gravity, stack)
    Variant.set(variant.fullName, stack)
    Speed.set(variant.getMovementData.speedOriginal, stack)
    Custom.set(value = false, stack)
    ShotData.serializeNBTItemStack(stack, shot)
  }

  sealed trait Pattern {
    def makeDanmaku(
        template: DanmakuTemplate,
        amount: Int,
        shotSpeed: Double,
        alternateMode: Boolean,
        offset: Double
    ): Set[DanmakuState]
  }
  object Pattern {
    def idOf(pattern: Pattern): Byte = pattern match {
      case Line       => 0
      case RandomRing => 1
      case Wide       => 2
      case Circle     => 3
      case Ring       => 4
      case Sphere     => 5
    }

    def fromId(id: Byte): Option[Pattern] = id match {
      case 0 => Some(Line)
      case 1 => Some(RandomRing)
      case 2 => Some(Wide)
      case 3 => Some(Circle)
      case 4 => Some(Ring)
      case 5 => Some(Sphere)
      case _ => None
    }
  }
  case object Line extends Pattern {
    override def makeDanmaku(
        template: DanmakuTemplate,
        amount: Int,
        shotSpeed: Double,
        alternateMode: Boolean,
        offset: Double
    ): Set[DanmakuState] = {
      val danmaku = template.toBuilder
      danmaku.pos = danmaku.pos.offset(danmaku.direction, offset)
      val res = for (i <- 1 to amount) yield {
        danmaku.setMovementData(shotSpeed / amount * i)
        danmaku.build.asEntity
      }
      DanmakuCore.proxy.spawnDanmaku(res)
      res.toSet
    }
  }
  def line: Pattern = Line

  case object RandomRing extends Pattern {
    override def makeDanmaku(
        template: DanmakuTemplate,
        amount: Int,
        shotSpeed: Double,
        alternateMode: Boolean,
        offset: Double
    ): Set[DanmakuState] = {
      val wide = if (alternateMode) 60F else 120F
      DanmakuCreationHelper.createRandomRingShot(template, amount, wide, offset, spawnDanmaku = true)
    }
  }
  def randomRing: Pattern = RandomRing

  case object Wide extends Pattern {
    override def makeDanmaku(
        template: DanmakuTemplate,
        amount: Int,
        shotSpeed: Double,
        alternateMode: Boolean,
        offset: Double
    ): Set[DanmakuState] = {
      val wide = if (alternateMode) amount * 4F else amount * 8F
      DanmakuCreationHelper.createWideShot(template, amount, wide, 0F, offset, spawnDanmaku = true)
    }
  }
  def wide: Pattern = Wide

  case object Circle extends Pattern {
    override def makeDanmaku(
        template: DanmakuTemplate,
        amount: Int,
        shotSpeed: Double,
        alternateMode: Boolean,
        offset: Double
    ): Set[DanmakuState] = DanmakuCreationHelper.createCircleShot(template, amount, 0F, offset, spawnDanmaku = true)
  }
  def circle: Pattern = Circle

  case object Ring extends Pattern {
    override def makeDanmaku(
        template: DanmakuTemplate,
        amount: Int,
        shotSpeed: Double,
        alternateMode: Boolean,
        offset: Double
    ): Set[DanmakuState] = {
      val wide = if (alternateMode) 7.5F else 15F
      DanmakuCreationHelper.createRingShot(template, amount, wide, template.world.rand.nextFloat * 360, offset)
    }
  }
  def ring: Pattern = Ring

  case object Sphere extends Pattern {
    override def makeDanmaku(
        template: DanmakuTemplate,
        amount: Int,
        shotSpeed: Double,
        alternateMode: Boolean,
        offset: Double
    ): Set[DanmakuState] =
      DanmakuCreationHelper.createSphereShot(template, amount, amount / 2, 0F, offset, spawnDanmaku = true)
  }
  def sphere: Pattern = Sphere
}
class ItemDanmaku extends ItemBase(LibItemName.DANMAKU) {
  setMaxDamage(0)
  setCreativeTab(DanmakuCreativeTab)

  override def getSubItems(tab: CreativeTabs, subItems: NonNullList[ItemStack]): Unit =
    if (isInCreativeTab(tab)) {
      subItems.addAll(
        DanmakuRegistry.DanmakuVariant.getValuesCollection.asScala.toSeq.sorted.map(ItemDanmaku.createStack).asJava
      )
    }

  @SideOnly(Side.CLIENT) override def hasEffect(stack: ItemStack): Boolean = ItemDanmaku.Infinity.get(stack)

  override def getUnlocalizedName(stack: ItemStack): String =
    s"${getUnlocalizedName()}.${ItemDanmaku.getController(stack).unlocalizedName}"

  override def onItemRightClick(world: World, player: EntityPlayer, hand: EnumHand): ActionResult[ItemStack] = {
    val stack   = player.getHeldItem(hand)
    var success = false

    val shot = ShotData.fromNBTItemStack(stack)
    if (!world.isRemote) {
      if (player.capabilities.isCreativeMode) ItemDanmaku.Infinity.set(value = true, stack)

      success = ItemDanmaku.shootDanmaku(
        stack = stack,
        world = world,
        user = Some(player),
        hand = Some(hand),
        alternateMode = player.isSneaking,
        pos = new Vector3(player.posX, player.posY + player.eyeHeight - shot.sizeY / 2, player.posZ),
        direction = new Vector3(player.getLookVec),
        offset = (shot.sizeZ / 3) * 2
      )

      if (!ItemDanmaku.Infinity.get(stack) && success) stack.shrink(1)
    }

    shot.form.playShotSound(player, shot)
    new ActionResult[ItemStack](
      if (success || world.isRemote) EnumActionResult.SUCCESS
      else EnumActionResult.FAIL,
      stack
    )
  }

  @SideOnly(Side.CLIENT)
  override def addInformation(
      stack: ItemStack,
      @Nullable world: World,
      list: util.List[String],
      flagIn: ITooltipFlag
  ): Unit = {
    super.addInformation(stack, world, list, flagIn)
    val shot           = ShotData.fromNBTItemStack(stack)
    val amount         = ItemDanmaku.Amount.get(stack)
    val shotSpeed      = ItemDanmaku.Speed.get(stack)
    val danmakuPattern = ItemDanmaku.DanPattern.get(stack)
    val gravity        = ItemDanmaku.getGravity(stack)
    val isInfinity     = ItemDanmaku.Infinity.get(stack)
    val custom         = ItemDanmaku.Custom.get(stack)

    val item = "item.danmaku"

    // format: off

    Tooltip
        .addI18n(s"$item.damage").add(" : ").addNum(shot.damage).newline //TODO: Show power adjusted damage
        .addI18n(s"$item.size").add(" : ").addNum(shot.sizeX).space.addNum(shot.sizeY).space.addNum(shot.sizeZ).newline
        .addI18n(s"$item.amount").add(" : ").addNum(amount).newline
        .when(custom).ifTrue(_.addI18n(s"$item.form").add(" : ").addI18n(shot.form.unlocalizedName).newline)
        .addI18n(s"$item.pattern").add(" : ").addI18n(s"$item.pattern.$danmakuPattern").newline
        .addI18n(s"$item.speed").add(" : ").addNum(shotSpeed).newline
        .addI18n(s"$item.gravity").add(" : ")
          .when(gravity.x !=~ 0D || gravity.y !=~ 0D || gravity.z !=~ 0D)
            .ifTrue(_.addNum(gravity.x).space.addNum(gravity.y).space.addNum(gravity.z))
            .orElse(_.addI18n(s"$item.noGravity"))
          .newline
        .addI18n(s"$item.edgeColor").add(" : ")
          .when(LibColor.isNormalColor(shot.edgeColor))
            .ifTrue(_.addI18n(s"$item.color.${shot.edgeColor}"))
            .orElse(_.addI18n(s"$item.color.custom"))
          .newline
        .addI18n(s"$item.coreColor").add(" : ")
          .when(LibColor.isNormalColor(shot.coreColor))
            .ifTrue(_.addI18n(s"$item.color.${shot.coreColor}"))
            .orElse(_.addI18n(s"$item.color.custom"))
          .newline
        .when(shot.subEntity != LibSubEntities.DEFAULT_TYPE).ifTrue(_.addI18n(s"$item.subentity").add(" : ").addI18n(shot.subEntity.unlocalizedName).newline)
        .when(isInfinity).ifTrue(_.addI18n(s"$item.infinity").newline)
        .when(custom).ifTrue(_.addI18n(s"$item.custom").newline)
        .build(list)
    
    // format: on
  }
}
