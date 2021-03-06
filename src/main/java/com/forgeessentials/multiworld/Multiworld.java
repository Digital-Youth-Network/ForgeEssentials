package com.forgeessentials.multiworld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.forgeessentials.commons.selections.WarpPoint;
import com.forgeessentials.core.misc.TeleportHelper;
import com.forgeessentials.data.v2.DataManager;
import com.forgeessentials.util.ServerUtil;
import com.forgeessentials.util.WorldUtil;
import com.google.gson.annotations.Expose;

import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;

/**
 *
 * @author Olee
 */
public class Multiworld {

	/**
	 * Teleport the player to the multiworld
	 *
	 * @throws CommandException
	 */
	public static void teleport(EntityPlayerMP player, WorldServer world, boolean instant) throws CommandException {
		teleport(player, world, player.posX, player.posY, player.posZ, instant);
	}

	/**
	 * Teleport the player to the multiworld
	 *
	 * @throws CommandException
	 */
	public static void teleport(EntityPlayerMP player, WorldServer world, double x, double y, double z, boolean instant)
			throws CommandException {
		y = WorldUtil.placeInWorld(world, (int) x, (int) y, (int) z);
		WarpPoint target = new WarpPoint(world.provider.getDimensionId(), x, y, z, player.rotationPitch,
				player.rotationYaw);
		if (instant) {
			TeleportHelper.checkedTeleport(player, target);
		} else {
			TeleportHelper.teleport(player, target);
		}
	}

	protected String name;

	protected int dimensionId;

	protected String provider;

	protected String worldType;

	protected List<String> biomes = new ArrayList<>();

	protected long seed;

	protected boolean mapFeaturesEnabled = true;

	@Expose(serialize = false)
	protected boolean worldLoaded;

	@Expose(serialize = false)
	protected boolean error;

	@Expose(serialize = false)
	protected int providerId;

	@Expose(serialize = false)
	protected WorldType worldTypeObj;

	public Multiworld(String name, String provider, String worldType) {
		this(name, provider, worldType, new Random().nextLong());
	}

	public Multiworld(String name, String provider, String worldType, long seed) {
		this.name = name;
		this.provider = provider;
		this.worldType = worldType;

		this.seed = seed;
		// this.gameType = MinecraftServer.getServer().getGameType();
		// this.difficulty = MinecraftServer.getServer().func_147135_j();
		// this.allowHostileCreatures = true;
		// this.allowPeacefulCreatures = true;
	}

	protected void delete() {
		DataManager.getInstance().delete(this.getClass(), name);
	}

	public List<String> getBiomes() {
		return biomes;
	}

	public int getDimensionId() {
		return dimensionId;
	}

	public String getName() {
		return name;
	}

	public String getProvider() {
		return provider;
	}

	public int getProviderId() {
		return providerId;
	}

	public long getSeed() {
		return seed;
	}

	public WorldServer getWorldServer() {
		return MinecraftServer.getServer().worldServerForDimension(dimensionId);
	}

	public boolean isError() {
		return error;
	}

	public boolean isLoaded() {
		return worldLoaded;
	}

	public void removeAllPlayersFromWorld() {
		WorldServer overworld = MinecraftServer.getServer().worldServerForDimension(0);
		for (EntityPlayerMP player : ServerUtil.getPlayerList()) {
			if (player.dimension == dimensionId) {
				BlockPos playerPos = player.getPosition();
				int y = WorldUtil.placeInWorld(player.worldObj, playerPos.getX(), playerPos.getY(), playerPos.getZ());
				WarpPoint point = new WarpPoint(overworld, playerPos.getX(), y, playerPos.getZ(), 0, 0);
				TeleportHelper.doTeleport(player, point);
			}
		}
	}

	protected void save() {
		DataManager.getInstance().save(this, name);
	}

	/**
	 * Teleport the player to the multiworld
	 *
	 * @throws CommandException
	 */
	public void teleport(EntityPlayerMP player, boolean instant) throws CommandException {
		teleport(player, getWorldServer(), instant);
	}

	public void teleport(EntityPlayerMP player, double x, double y, double z, boolean instant) throws CommandException {
		teleport(player, getWorldServer(), x, y, z, instant);
	}
}