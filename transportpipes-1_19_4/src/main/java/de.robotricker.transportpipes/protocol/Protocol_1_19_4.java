package de.robotricker.transportpipes.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.Vector3F;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.google.common.collect.Lists;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.duct.Duct;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.AmethystCluster;
import org.bukkit.block.data.type.LightningRod;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Protocol_1_19_4 implements ProtocolProvider {

    @Override
    public int getMaskIndex() {
        return 15;
    }

    @Override
    public int getHeadRotIndex() {
        return 16;
    }

    @Override
    public int getRightArmRotIndex() {
        return 19;
    }

    @Override
    public void removeASD(Player p, List<ArmorStandData> armorStandData, ProtocolManager protocolManager) {
        PacketContainer entityDestroyContainer = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        List<Integer> ids = armorStandData.stream().mapToInt(ArmorStandData::getEntityID).boxed().collect(Collectors.toList());
        entityDestroyContainer.getIntLists().write(0, ids);
        protocolManager.sendServerPacket(p, entityDestroyContainer);
    }

    @Override
    public Recipe calculateRecipe(TransportPipes transportPipes, Inventory inventory, Duct duct) {
        ItemStack[] craftingMatrix = new ItemStack[9];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (inventory.getItem(10 + row * 9 + col) != null) {
                    craftingMatrix[row * 3 + col] = inventory.getItem(10 + row * 9 + col);
                } else {
                    craftingMatrix[row * 3 + col] = new ItemStack(Material.AIR);
                }
            }
        }

        return Bukkit.getCraftingRecipe(craftingMatrix, duct.getWorld());
    }

    @Override
    public StructureModifier setASDYaw(PacketContainer spawnEntityLivingContainer, double yaw) {
        return spawnEntityLivingContainer.getBytes().write(1, (byte) (yaw * 256 / 360));
    }

    @Override
    public boolean isClickedFaceDirectional(BlockData blockData) {
        clickedFaceDirectionals.add(AmethystCluster.class);
        clickedFaceDirectionals.add(LightningRod.class);
        clickedFaceMaterials.add(Material.DEEPSLATE);
        clickedFaceMaterials.add(Material.INFESTED_DEEPSLATE);
        clickedFaceMaterials.add(Material.OCHRE_FROGLIGHT);
        clickedFaceMaterials.add(Material.PEARLESCENT_FROGLIGHT);
        clickedFaceMaterials.add(Material.VERDANT_FROGLIGHT);
        clickedFaceMaterials.add(Material.MUDDY_MANGROVE_ROOTS);
        return ProtocolProvider.super.isClickedFaceDirectional(blockData);
    }

    @Override
    public PacketContainer setEntityMetadata(ProtocolManager protocolManager, ArmorStandData asd) {
        PacketContainer entityMetadataContainer = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        entityMetadataContainer.getModifier().writeDefaults();
        entityMetadataContainer.getIntegers().write(0, asd.getEntityID()); // Entity ID

        byte bitMask = (byte) ((asd.isSmall() ? 0x01 : 0x00) | 0x04 | 0x08 | 0x10); // Is Small + Has Arms + No BasePlate + Marker

        WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
        WrappedDataWatcher.WrappedDataWatcherObject entityMask = new WrappedDataWatcher.WrappedDataWatcherObject(0, BYTE_SERIALIZER);
        WrappedDataWatcher.WrappedDataWatcherObject nameVisible = new WrappedDataWatcher.WrappedDataWatcherObject(3, BOOLEAN_SERIALIZER);
        WrappedDataWatcher.WrappedDataWatcherObject asMask = new WrappedDataWatcher.WrappedDataWatcherObject(getMaskIndex(), BYTE_SERIALIZER);
        WrappedDataWatcher.WrappedDataWatcherObject headRot = new WrappedDataWatcher.WrappedDataWatcherObject(getHeadRotIndex(), VECTOR_SERIALIZER);
        WrappedDataWatcher.WrappedDataWatcherObject rArmRot = new WrappedDataWatcher.WrappedDataWatcherObject(getRightArmRotIndex(), VECTOR_SERIALIZER);

        dataWatcher.setObject(entityMask, (byte) (0x20 | 0x01)); // Invisible and on fire (to fix lighting issues)
        dataWatcher.setObject(nameVisible, false); // Custom Name Visible
        dataWatcher.setObject(asMask, bitMask); // Armor Stand Data
        dataWatcher.setObject(headRot, new Vector3F((float) asd.getHeadRotation().getX(), (float) asd.getHeadRotation().getY(), (float) asd.getHeadRotation().getZ())); // Head Rotation
        dataWatcher.setObject(rArmRot, new Vector3F((float) asd.getArmRotation().getX(), (float) asd.getArmRotation().getY(), (float) asd.getArmRotation().getZ())); // Right Arm Rotation

        final List<WrappedDataValue> wrappedDataValueList = Lists.newArrayList();
        dataWatcher.getWatchableObjects().stream().filter(Objects::nonNull).forEach(entry -> {
            final WrappedDataWatcher.WrappedDataWatcherObject dataWatcherObject = entry.getWatcherObject();
            wrappedDataValueList.add(new WrappedDataValue(dataWatcherObject.getIndex(), dataWatcherObject.getSerializer(), entry.getRawValue()));
        });
        entityMetadataContainer.getDataValueCollectionModifier().write(0, wrappedDataValueList);

        return entityMetadataContainer;
    }
}
