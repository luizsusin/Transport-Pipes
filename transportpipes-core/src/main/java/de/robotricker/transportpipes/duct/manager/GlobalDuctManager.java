package de.robotricker.transportpipes.duct.manager;

import de.robotricker.transportpipes.PlayerSettingsService;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.config.PlayerSettingsConf;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctRegister;
import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.duct.types.DuctType;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.location.TPDirection;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.protocol.ProtocolService;
import de.robotricker.transportpipes.rendersystems.RenderSystem;
import de.robotricker.transportpipes.utils.WorldUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class GlobalDuctManager {

    protected final TransportPipes transportPipes;
    protected final ProtocolService protocolService;
    protected final DuctRegister ductRegister;
    protected final PlayerSettingsService playerSettingsService;

    /**
     * ThreadSafe
     **/
    private final ConcurrentHashMap<World, ConcurrentSkipListMap<BlockLocation, Duct>> ducts;
    /**
     * ThreadSafe
     **/
    private final ConcurrentHashMap<Player, Set<Duct>> playerDucts;

    @Inject
    public GlobalDuctManager(TransportPipes transportPipes, ProtocolService protocolService, DuctRegister ductRegister, PlayerSettingsService playerSettingsService) {
        this.transportPipes = transportPipes;
        this.protocolService = protocolService;
        this.ductRegister = ductRegister;
        this.playerSettingsService = playerSettingsService;
        this.ducts = new ConcurrentHashMap<>();
        this.playerDucts = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<World, ConcurrentSkipListMap<BlockLocation, Duct>> getDucts() {
        return ducts;
    }

    public ConcurrentSkipListMap<BlockLocation, Duct> getDucts(World world) {
        return ducts.computeIfAbsent(world, v -> new ConcurrentSkipListMap<>());
    }

    public Duct getDuctAtLoc(World world, BlockLocation blockLoc) {
        return getDucts(world).get(blockLoc);
    }

    public Duct getDuctAtLoc(Location location) {
        return getDuctAtLoc(location.getWorld(), new BlockLocation(location));
    }

    public Set<Duct> getPlayerDucts(Player player) {
        return playerDucts.computeIfAbsent(player, p -> ConcurrentHashMap.newKeySet());
    }
    
    public void clearPlayerDucts(Player player) {
    	playerDucts.remove(player);
    }

    public RenderSystem getPlayerRenderSystem(Player player, BaseDuctType<? extends Duct> baseDuctType) {
        PlayerSettingsConf conf = playerSettingsService.getOrCreateSettingsConf(player);
        return conf.getRenderSystem(baseDuctType);
    }

    public Duct createDuctObject(DuctType ductType, BlockLocation blockLoc, World world, Chunk chunk) {
        return ductType.getBaseDuctType().getDuctFactory().createDuct(ductType, blockLoc, world, chunk);
    }

    public void registerDuct(Duct duct) {
        getDucts(duct.getWorld()).put(duct.getBlockLoc(), duct);
    }

    public void unregisterDuct(Duct duct) {
        getDucts(duct.getWorld()).remove(duct.getBlockLoc());
    }

    public void registerDuctInRenderSystems(Duct duct, boolean updateForPlayers) {
        RenderSystem renderSystem;
        for (int i = 0; i < 2; i++) {
            renderSystem = i == 0 ? duct.getDuctType().getBaseDuctType().getVanillaRenderSystem() : duct.getDuctType().getBaseDuctType().getModelledRenderSystem();
            if (renderSystem != null) {

                renderSystem.createDuctASD(duct, duct.getAllConnections());

                if (updateForPlayers) {
                    for (Player p : WorldUtils.getPlayerList(duct.getWorld())) {
                        PlayerSettingsConf conf = playerSettingsService.getOrCreateSettingsConf(p);
                        if (duct.getBlockLoc().toLocation(p.getWorld()).distance(p.getLocation()) <= conf.getRenderDistance()) {
                            if (conf.getRenderSystem(duct.getDuctType().getBaseDuctType()).equals(renderSystem)) {
                                getPlayerDucts(p).add(duct);
                                protocolService.sendASD(p, duct.getBlockLoc(), renderSystem.getASDForDuct(duct));
                            }
                        }
                    }
                }
            }
        }
    }

    public void updateDuctInRenderSystems(Duct duct, boolean updateForPlayers) {
        RenderSystem renderSystem;
        for (int i = 0; i < 2; i++) {
            renderSystem = i == 0 ? duct.getDuctType().getBaseDuctType().getVanillaRenderSystem() : duct.getDuctType().getBaseDuctType().getModelledRenderSystem();
            if (renderSystem != null) {

                List<ArmorStandData> removeASD = new ArrayList<>();
                List<ArmorStandData> addASD = new ArrayList<>();
                renderSystem.updateDuctASD(duct, duct.getAllConnections(), removeASD, addASD);

                if (updateForPlayers) {
                    for (Player p : WorldUtils.getPlayerList(duct.getWorld())) {
                        PlayerSettingsConf conf = playerSettingsService.getOrCreateSettingsConf(p);
                        if (conf.getRenderSystem(duct.getDuctType().getBaseDuctType()).equals(renderSystem)) {
                            if (getPlayerDucts(p).contains(duct)) {
                                protocolService.removeASD(p, removeASD);
                                protocolService.sendASD(p, duct.getBlockLoc(), addASD);
                            }
                        }
                    }
                }
            }
        }
    }

    public void unregisterDuctInRenderSystem(Duct duct, boolean updateForPlayers) {
        RenderSystem renderSystem;
        for (int i = 0; i < 2; i++) {
            renderSystem = i == 0 ? duct.getDuctType().getBaseDuctType().getVanillaRenderSystem() : duct.getDuctType().getBaseDuctType().getModelledRenderSystem();
            if (renderSystem != null) {
                if (updateForPlayers) {
                    for (Player p : WorldUtils.getPlayerList(duct.getWorld())) {
                        PlayerSettingsConf conf = playerSettingsService.getOrCreateSettingsConf(p);
                        if (conf.getRenderSystem(duct.getDuctType().getBaseDuctType()).equals(renderSystem)) {
                            if (getPlayerDucts(p).remove(duct)) {
                                protocolService.removeASD(p, renderSystem.getASDForDuct(duct));
                            }
                        }
                    }
                }

                renderSystem.destroyDuctASD(duct);

            }
        }
    }

    public void playDuctDestroyActions(Duct duct, Player destroyer) {
        List<ItemStack> dropItems = duct.destroyed(transportPipes, duct.getDuctType().getBaseDuctType().getDuctManager(), destroyer);
        transportPipes.runTaskSync(() -> {
            for (ItemStack is : dropItems) {
                duct.getWorld().dropItem(duct.getBlockLoc().toLocation(duct.getWorld()), is);
            }
        });
    }

    /**
     * recalculates the duct connections (in case of pipes, updates the container connections, too) of this duct and saves them.
     * Also notifies the duct about this change
     */
    public void updateDuctConnections(Duct duct) {
        //update duct connections
        duct.getDuctConnections().clear();
        for (TPDirection tpDir : TPDirection.values()) {
            Duct neighborDuct = getDuctAtLoc(duct.getWorld(), duct.getBlockLoc().getNeighbor(tpDir));
            if (neighborDuct != null && duct.getDuctType().connectsTo(neighborDuct.getDuctType()) && !duct.getBlockedConnections().contains(tpDir)
                    && !neighborDuct.getBlockedConnections().contains(tpDir.getOpposite())) {
                duct.getDuctConnections().put(tpDir, neighborDuct);
            }
        }
        //update baseDuctType specific other connections
        duct.getDuctType().getBaseDuctType().getDuctManager().updateNonDuctConnections(duct);
        //notify connections change
        duct.notifyConnectionChange();
    }

    public void updateNeighborDuctsConnections(Duct duct) {
        for (TPDirection ductConn : duct.getDuctConnections().keySet()) {
            updateDuctConnections(duct.getDuctConnections().get(ductConn));
        }
    }

    public void updateNeighborDuctsInRenderSystems(Duct duct, boolean updateForPlayers) {
        for (TPDirection ductConn : duct.getDuctConnections().keySet()) {
            updateDuctInRenderSystems(duct.getDuctConnections().get(ductConn), updateForPlayers);
        }
    }

    public void tick() {
        for (BaseDuctType<? extends Duct> baseDuctType : ductRegister.baseDuctTypes()) {
            baseDuctType.getDuctManager().tick();
        }
    }

}
