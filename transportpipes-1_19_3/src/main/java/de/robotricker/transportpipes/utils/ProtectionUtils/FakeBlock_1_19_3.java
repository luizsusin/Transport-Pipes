package de.robotricker.transportpipes.utils.ProtectionUtils;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.bukkit.util.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FakeBlock_1_19_3 extends FakeBlock implements Block {

    public FakeBlock_1_19_3(World world, Location location, Material material) {
        this.world = world;
        this.location = location;
        this.material = Material.HOPPER;
        this.blockData = this.material.createBlockData();
        this.hopper = new FakeHopper_1_19_3(world, location, this);
    }

    public BlockState getState(boolean ignoredUseSnapshot) {
        return getState();
    }

    @Override
    public byte getData() {
        return hopper.getRawData();
    }

    @NotNull
    @Override
    public BlockData getBlockData() {
        return blockData;
    }

    @NotNull
    @Override
    public Block getRelative(int modX, int modY, int modZ) {
        return location.add(modX, modY, modZ).getBlock();
    }

    @NotNull
    @Override
    public Block getRelative(@NotNull BlockFace face) {
        return getRelative(face, 1);
    }

    @NotNull
    @Override
    public Block getRelative(@NotNull BlockFace face, int distance) {
        return location.getBlock().getRelative(face, distance);
    }

    @NotNull
    @Override
    public Material getType() {
        return material;
    }

    @Override
    public byte getLightLevel() {
        return 0;
    }

    @Override
    public byte getLightFromSky() {
        return 0;
    }

    @Override
    public byte getLightFromBlocks() {
        return 0;
    }

    @NotNull
    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public int getX() {
        return location.getBlockX();
    }

    @Override
    public int getY() {
        return location.getBlockY();
    }

    @Override
    public int getZ() {
        return location.getBlockZ();
    }

    @NotNull
    @Override
    public Location getLocation() {
        return location;
    }

    @Nullable
    @Override
    public Location getLocation(@Nullable Location loc) {
        if (loc == null) {
            return null;
        }
        loc.setDirection(location.getDirection());
        loc.setPitch(location.getPitch());
        loc.setYaw(location.getYaw());
        loc.setWorld(location.getWorld());
        loc.setX(location.getX());
        loc.setY(location.getY());
        loc.setZ(location.getZ());
        return loc;
    }

    @NotNull
    @Override
    public Chunk getChunk() {
        return location.getChunk();
    }

    @Override
    public void setBlockData(@NotNull BlockData data) {}

    @Override
    public void setBlockData(@NotNull BlockData data, boolean applyPhysics) {}

    @Override
    public void setType(@NotNull Material type) {}

    @Override
    public void setType(@NotNull Material type, boolean applyPhysics) {}

    @Nullable
    @Override
    public BlockFace getFace(@NotNull Block block) {
        BlockFace face = block.getFace(location.getBlock());
        return face == null ? null : face.getOppositeFace();
    }

    @NotNull
    @Override
    public BlockState getState() {
        return hopper;
    }

    @NotNull
    @Override
    public Biome getBiome() {
        return location.getBlock().getBiome();
    }

    @Override
    public void setBiome(@NotNull Biome bio) {}

    @Override
    public boolean isBlockPowered() {
        return false;
    }

    @Override
    public boolean isBlockIndirectlyPowered() {
        return false;
    }

    @Override
    public boolean isBlockFacePowered(@NotNull BlockFace face) {
        return false;
    }

    @Override
    public boolean isBlockFaceIndirectlyPowered(@NotNull BlockFace face) { return false; }

    @Override
    public int getBlockPower(@NotNull BlockFace face) {
        return 0;
    }

    @Override
    public int getBlockPower() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isLiquid() {
        return false;
    }

    @Override
    public double getTemperature() {
        return 0.0;
    }

    @Override
    public double getHumidity() {
        return 0.0;
    }

    @NotNull
    @Override
    public PistonMoveReaction getPistonMoveReaction() {
        return location.getBlock().getPistonMoveReaction();
    }

    @Override
    public boolean breakNaturally() {
        return false;
    }

    @Override
    public boolean breakNaturally(@Nullable ItemStack tool) {
        return false;
    }

    @Override
    public boolean applyBoneMeal(@NotNull BlockFace face) {
        return false;
    }

    @NotNull
    @Override
    public Collection<ItemStack> getDrops() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public Collection<ItemStack> getDrops(@Nullable ItemStack tool) {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public Collection<ItemStack> getDrops(@NotNull ItemStack tool, @Nullable Entity entity) {
        return Collections.emptyList();
    }

    @Override
    public boolean isPreferredTool(@NotNull ItemStack tool) {
        return false;
    }

    @Override
    public float getBreakSpeed(@NotNull Player player) {
        return 0;
    }

    @Override
    public boolean isPassable() {
        return false;
    }

    @Nullable
    @Override
    public RayTraceResult rayTrace(@NotNull Location start, @NotNull Vector direction, double maxDistance, @NotNull FluidCollisionMode fluidCollisionMode) {
        return null;
    }

    @NotNull
    @Override
    public BoundingBox getBoundingBox() {
        return location.getBlock().getBoundingBox();
    }

    @NotNull
    @Override
    public VoxelShape getCollisionShape() {
        return location.getBlock().getCollisionShape();
    }

    @Override
    public boolean canPlace(@NotNull BlockData blockData) {
        return false;
    }

    @Override
    public void setMetadata(@NotNull String metadataKey, @NotNull MetadataValue newMetadataValue) {}

    @NotNull
    @Override
    public List<MetadataValue> getMetadata(@NotNull String metadataKey) {
        return Collections.emptyList();
    }

    @Override
    public boolean hasMetadata(@NotNull String metadataKey) {
        return false;
    }

    @Override
    public void removeMetadata(@NotNull String metadataKey, @NotNull Plugin owningPlugin) {}

    @Override
    public String getTranslationKey() {
        return location.getBlock().getType().getBlockTranslationKey();
    }
}
