/*
 * This class was created by <Katrix>. It's distributed as
 * part of the DanmakuCore Mod. Get the Source Code in github:
 * https://github.com/Katrix-/DanmakuCore
 *
 * DanmakuCore is Open Source and distributed under the
 * the DanmakuCore license: https://github.com/Katrix-/DanmakuCore/blob/master/LICENSE.md
 */
package net.katsstuff.danmakucore.lib.data;

import net.katsstuff.danmakucore.entity.danmaku.subentity.SubEntityType;
import net.katsstuff.danmakucore.entity.danmaku.subentity.SubEntityTypeDummy;
import net.katsstuff.danmakucore.lib.LibModJ;
import net.katsstuff.danmakucore.lib.LibSubEntityName;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder(LibModJ.ID)
public final class LibSubEntities {

	@ObjectHolder(LibSubEntityName.DEFAULT)
	public static final SubEntityType DEFAULT_TYPE = SubEntityTypeDummy.instance();
	@ObjectHolder(LibSubEntityName.FIRE)
	public static final SubEntityType FIRE = SubEntityTypeDummy.instance();
	@ObjectHolder(LibSubEntityName.EXPLODE)
	public static final SubEntityType EXPLOSION = SubEntityTypeDummy.instance();
	@ObjectHolder(LibSubEntityName.TELEPORT)
	public static final SubEntityType TELEPORT = SubEntityTypeDummy.instance();
}
