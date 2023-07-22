package de.robotricker.transportpipes.utils.ProtectionUtils;

import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R1.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v1_20_R1.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.loot.LootTable;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class FakeHopper_1_20 implements Hopper {

    private final World world;
    private final Location location;
    private final Block block;
    private final Inventory inventory;
    private final IBlockData data;
    private final CraftPersistentDataContainer persistentDataContainer;

    public FakeHopper_1_20(World world, Location location, Block block) {
        this.world = world;
        this.location = location;
        this.block = block;
        this.inventory = Bukkit.createInventory(null, InventoryType.HOPPER);
        this.data = CraftMagicNumbers.getBlock(Material.HOPPER).n();
        this.persistentDataContainer = new CraftPersistentDataContainer(new CraftPersistentDataTypeRegistry());
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @NotNull
    @Override
    public Inventory getSnapshotInventory() {
        return inventory;
    }

    @Nullable
    @Override
    public String getCustomName() {
        return null;
    }

    @Override
    public void setCustomName(@Nullable String s) {}

    @NotNull
    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        return persistentDataContainer;
    }

    @NotNull
    @Override
    public Block getBlock() {
        return block;
    }

    @NotNull
    @Override
    public MaterialData getData() {
        return CraftMagicNumbers.getMaterial(this.data);
    }

    @NotNull
    @Override
    public BlockData getBlockData() {
        return CraftBlockData.fromData(this.data);
    }

    @NotNull
    @Override
    public Material getType() {
        return Material.HOPPER;
    }

    @Override
    public byte getLightLevel() {
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
        if (loc != null) {
            loc.setWorld(this.world);
            loc.setX(this.getX());
            loc.setY(this.getY());
            loc.setZ(this.getZ());
            loc.setYaw(0.0F);
            loc.setPitch(0.0F);
        }

        return loc;
    }

    @NotNull
    @Override
    public Chunk getChunk() {
        return location.getChunk();
    }

    @Override
    public void setData(@NotNull MaterialData materialData) {}

    @Override
    public void setBlockData(@NotNull BlockData blockData) {}

    @Override
    public void setType(@NotNull Material material) {}

    @Override
    public boolean update() {
        return false;
    }

    @Override
    public boolean update(boolean b) {
        return false;
    }

    @Override
    public boolean update(boolean b, boolean b1) {
        return false;
    }

    @Override
    public byte getRawData() {
        return CraftMagicNumbers.toLegacyData(this.data);
    }

    @Override
    public void setRawData(byte b) {}

    @Override
    public boolean isPlaced() {
        return true;
    }

    @Override
    public boolean isLocked() {
        return false;
    }

    @NotNull
    @Override
    public String getLock() {
        return "";
    }

    @Override
    public void setLock(@Nullable String s) {}

    @Override
    public void setLootTable(@Nullable LootTable lootTable) {}

    @Nullable
    @Override
    public LootTable getLootTable() {
        return null;
    }

    @Override
    public void setSeed(long l) {}

    @Override
    public long getSeed() {
        return 0;
    }

    @Override
    public void setMetadata(@NotNull String s, @NotNull MetadataValue metadataValue) {}

    @NotNull
    @Override
    public List<MetadataValue> getMetadata(@NotNull String s) {
        return Collections.emptyList();
    }

    @Override
    public boolean hasMetadata(@NotNull String s) {
        return false;
    }

    @Override
    public void removeMetadata(@NotNull String s, @NotNull Plugin plugin) {}
}
