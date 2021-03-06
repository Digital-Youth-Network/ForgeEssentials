package com.forgeessentials.teleport;

import com.forgeessentials.commons.selections.WarpPoint;
import com.forgeessentials.core.commands.ForgeEssentialsCommandBase;
import com.forgeessentials.core.misc.TeleportHelper;
import com.forgeessentials.core.misc.TranslatedCommandException;
import com.forgeessentials.util.PlayerUtil;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.permission.PermissionLevel;
import net.minecraftforge.permission.PermissionManager;

public class CommandJump extends ForgeEssentialsCommandBase {

	public static void jump(EntityPlayerMP player) throws CommandException {
		MovingObjectPosition mo = PlayerUtil.getPlayerLookingSpot(player, 500);
		if (mo == null) {
			throw new TranslatedCommandException("The spot you are looking at is too far away to teleport.");
		}
		BlockPos pos = mo.getBlockPos();
		TeleportHelper.teleport(player, new WarpPoint(player.getEntityWorld().provider.getDimensionId(), pos.getX(),
				pos.getY() + 1, pos.getZ(), player.rotationPitch, player.rotationYaw));
	}

	public CommandJump() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public boolean canConsoleUseCommand() {
		return false;
	}

	@Override
	public String getCommandName() {
		return "fejump";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/jump Teleport to the location you are looking at";
	}

	@Override
	public String[] getDefaultAliases() {
		return new String[] { "jump" };
	}

	@Override
	public PermissionLevel getPermissionLevel() {
		return PermissionLevel.OP;
	}

	@Override
	public String getPermissionNode() {
		return TeleportModule.PERM_JUMP;
	}

	@SubscribeEvent
	public void playerInteractEvent(PlayerInteractEvent event) {
		if (!(event.entityPlayer instanceof EntityPlayerMP)) {
			return;
		}
		if ((event.action != Action.RIGHT_CLICK_AIR) && (event.action != Action.RIGHT_CLICK_BLOCK)) {
			return;
		}
		ItemStack stack = event.entityPlayer.getCurrentEquippedItem();
		if ((stack == null) || (stack.getItem() != Items.compass)) {
			return;
		}
		if (!PermissionManager.checkPermission(event.entityPlayer, TeleportModule.PERM_JUMP_TOOL)) {
			return;
		}

		try {
			jump((EntityPlayerMP) event.entityPlayer);
		} catch (CommandException ce) {
			ChatComponentTranslation msg = new ChatComponentTranslation(ce.getMessage(), ce.getErrorObjects());
			msg.getChatStyle().setColor(EnumChatFormatting.RED);
			event.entityPlayer.addChatMessage(msg);
		}
	}

	@Override
	public void processCommandPlayer(EntityPlayerMP player, String[] args) throws CommandException {
		jump(player);
	}

}
