package com.forgeessentials.core.preloader.mixin.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.C12PacketUpdateSign;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ITickable;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fe.event.world.SignEditEvent;
import net.minecraftforge.permission.PermissionManager;

@Mixin(NetHandlerPlayServer.class)
public abstract class MixinNetHandlerPlayServer implements INetHandlerPlayServer, ITickable {

	@Shadow
	public MinecraftServer serverController;

	@Shadow
	public EntityPlayerMP playerEntity;

	/**
	 * Check if the player has permission to use command blocks.
	 *
	 * @param player
	 *            the player
	 * @param level
	 *            the permission level
	 * @param command
	 *            the command
	 * @return {@code true} if the player has permission
	 */
	@Redirect(method = "processVanilla250Packet", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;canCommandSenderUseCommand(ILjava/lang/String;)Z"), require = 1)
	private boolean checkCommandBlockPermission(EntityPlayerMP player, int level, String command) {
		return PermissionManager.checkPermission(player, PermissionManager.PERM_COMMANDBLOCK);
	}

	/**
	 * Check if the player is in {@link WorldSettings.GameType#CREATIVE}.
	 *
	 * @param capabilities
	 *            the player capabilities
	 * @return always {@code true}
	 */
	@Redirect(method = "processVanilla250Packet", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerCapabilities;isCreativeMode:Z", ordinal = 0), require = 1)
	private boolean isCreativeMode(PlayerCapabilities capabilities) {
		// It's safe to always return true here because we only want to check if
		// the player has
		// permission, which we've done above.
		return true;
	}

	private IChatComponent[] onSignEditEvent(C12PacketUpdateSign data, EntityPlayerMP player) {
		SignEditEvent e = new SignEditEvent(data.getPosition(), data.getLines(), player);
		if (MinecraftForge.EVENT_BUS.post(e)) {
			return null;
		}
		return e.text;

	}

	@Override
	@Overwrite
	public void processUpdateSign(C12PacketUpdateSign packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, playerEntity.getServerForPlayer());
		playerEntity.markPlayerActive();
		WorldServer worldserver = serverController.worldServerForDimension(playerEntity.dimension);
		BlockPos blockpos = packetIn.getPosition();

		if (worldserver.isBlockLoaded(blockpos)) {
			net.minecraft.tileentity.TileEntity tileentity = worldserver.getTileEntity(blockpos);

			if (!(tileentity instanceof TileEntitySign)) {
				return;
			}

			TileEntitySign tileentitysign = (TileEntitySign) tileentity;

			if (!tileentitysign.getIsEditable() || (tileentitysign.getPlayer() != playerEntity)) {
				serverController
						.logWarning("Player " + playerEntity.getName() + " just tried to change non-editable sign");
				return;
			}

			IChatComponent[] aichatcomponent = onSignEditEvent(packetIn, playerEntity);
			if (aichatcomponent == null) {
				return;
			} // FE: sign edit event

			for (int i = 0; i < aichatcomponent.length; ++i) {
				tileentitysign.signText[i] = new ChatComponentText(
						EnumChatFormatting.getTextWithoutFormattingCodes(aichatcomponent[i].getUnformattedText()));
			}

			tileentitysign.markDirty();
			worldserver.markBlockForUpdate(blockpos);
		}
	}
	// /**
	// * Copy the {@link #signLines} to the {@link TileEntitySign}.
	// *
	// * @param src
	// * the source array
	// * @param srcPos
	// * starting position in the source array
	// * @param dest
	// * the destination array
	// * @param destPos
	// * starting position in the destination array
	// * @param length
	// * the number of array elements to be copied
	// */
	// @Redirect(method = "processUpdateSign", at = @At(value = "INVOKE", target
	// =
	// "Ljava/lang/System;arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V"),
	// require = 1)
	// private void copyLinesToBlockEntity(Object src, int srcPos, Object dest,
	// int destPos, int length) {
	// if (signLines.length == 0) {
	// return;
	// }
	// // You may get a warning that `dest` is not Object[] - don't change
	// // this, or Mixin will yell at you.
	// System.arraycopy(signLines, srcPos, dest, destPos, length);
	// signLines = null;
	// }
	//
	// /**
	// * Post {@link SignEditEvent} to the event bus.
	// *
	// * @param packet
	// * the update sign packet
	// * @param ci
	// * the callback info
	// */
	// @Redirect(method = "processUpdateSign", at = @At(value = "INVOKE", target
	// = "Lnet/minecraft/network/play/client/C12PacketUpdateSign;getLines()V"),
	// require = 1)
	// private IChatComponent[] getLines(C12PacketUpdateSign packet,
	// CallbackInfo ci) {
	// SignEditEvent event = new SignEditEvent(packet.getPosition(),
	// packet.getLines(), playerEntity);
	// if (MinecraftForge.EVENT_BUS.post(event)) {
	// // We will replace this with an empty array
	// signLines = new IChatComponent[] {};
	// return signLines;
	// } else {
	// signLines = event.text;
	// return event.text;
	// }
	// }

}