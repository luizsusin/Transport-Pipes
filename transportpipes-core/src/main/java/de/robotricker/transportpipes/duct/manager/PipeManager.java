package de.robotricker.transportpipes.duct.manager;

import de.robotricker.transportpipes.PlayerSettingsService;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.api.TransportPipesContainer;
import de.robotricker.transportpipes.config.GeneralConf;
import de.robotricker.transportpipes.config.LangConf;
import de.robotricker.transportpipes.config.PlayerSettingsConf;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctRegister;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.duct.pipe.items.PipeItem;
import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.duct.types.DuctType;
import de.robotricker.transportpipes.duct.types.pipetype.ColoredPipeType;
import de.robotricker.transportpipes.duct.types.pipetype.PipeType;
import de.robotricker.transportpipes.items.ItemService;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.location.TPDirection;
import de.robotricker.transportpipes.protocol.ProtocolService;
import de.robotricker.transportpipes.utils.WorldUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class PipeManager extends DuctManager<Pipe> {

    private static final long BIG_TICK_COUNT = 10;

    private final PlayerSettingsService playerSettingsService;
    private final GeneralConf generalConf;

    /**
     * ThreadSafe
     **/
    private final ConcurrentHashMap<World, ConcurrentSkipListMap<BlockLocation, TransportPipesContainer>> containers;

    /**
     * THREAD-SAFE
     */
    private final ConcurrentHashMap<Player, Set<PipeItem>> playerItems;

    private ShapedRecipe wrenchRecipe;

    private long tickCounter;

    @Inject
    public PipeManager(TransportPipes transportPipes, DuctRegister ductRegister, GlobalDuctManager globalDuctManager, ProtocolService protocolService, ItemService itemService, PlayerSettingsService playerSettingsService, GeneralConf generalConf) {
        super(transportPipes, ductRegister, globalDuctManager, protocolService, itemService);
        this.playerSettingsService = playerSettingsService;
        this.generalConf = generalConf;
        playerItems = new ConcurrentHashMap<>();
        containers = new ConcurrentHashMap<>();
        tickCounter = 0;
    }

    public ConcurrentHashMap<World, ConcurrentSkipListMap<BlockLocation, TransportPipesContainer>> getContainers() {
        return containers;
    }

    public ConcurrentSkipListMap<BlockLocation, TransportPipesContainer> getContainers(World world) {
        return containers.computeIfAbsent(world, v -> new ConcurrentSkipListMap<>());
    }

    public TransportPipesContainer getContainerAtLoc(World world, BlockLocation blockLoc) {
        Map<BlockLocation, TransportPipesContainer> containerMap = getContainers(world);
        return containerMap.get(blockLoc);
    }

    public TransportPipesContainer getContainerAtLoc(Location location) {
        return getContainerAtLoc(location.getWorld(), new BlockLocation(location));
    }

    @Override
    public void updateNonDuctConnections(Duct duct) {
        Pipe pipe = (Pipe) duct;
        pipe.getContainerConnections().clear();
        for (TPDirection tpDir : TPDirection.values()) {
            TransportPipesContainer neighborContainer = getContainerAtLoc(pipe.getWorld(), pipe.getBlockLoc().getNeighbor(tpDir));
            if (neighborContainer != null && !duct.getBlockedConnections().contains(tpDir)) {
                pipe.getContainerConnections().put(tpDir, neighborContainer);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void registerDuctTypes() {
        PipeType pipeType;
        BaseDuctType<Pipe> pipeBaseDuctType = ductRegister.baseDuctTypeOf("pipe");

        pipeType = new ColoredPipeType(pipeBaseDuctType, "White", LangConf.Key.PIPES_WHITE.get(), Material.WHITE_DYE, "transportpipes.craft.coloredpipe");
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new ColoredPipeType(pipeBaseDuctType, "Blue", LangConf.Key.PIPES_BLUE.get(), Material.BLUE_DYE, "transportpipes.craft.coloredpipe");
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new ColoredPipeType(pipeBaseDuctType, "Red", LangConf.Key.PIPES_RED.get(), Material.RED_DYE, "transportpipes.craft.coloredpipe");
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new ColoredPipeType(pipeBaseDuctType, "Yellow", LangConf.Key.PIPES_YELLOW.get(), Material.YELLOW_DYE, "transportpipes.craft.coloredpipe");
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new ColoredPipeType(pipeBaseDuctType, "Green", LangConf.Key.PIPES_GREEN.get(), Material.GREEN_DYE, "transportpipes.craft.coloredpipe");
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new ColoredPipeType(pipeBaseDuctType, "Black", LangConf.Key.PIPES_BLACK.get(), Material.BLACK_DYE, "transportpipes.craft.coloredpipe");
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new PipeType(pipeBaseDuctType, "Golden", LangConf.Key.PIPES_GOLDEN.get(), "transportpipes.craft.goldenpipe");
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new PipeType(pipeBaseDuctType, "Iron", LangConf.Key.PIPES_IRON.get(), "transportpipes.craft.ironpipe");
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new PipeType(pipeBaseDuctType, "Ice", LangConf.Key.PIPES_ICE.get(), "transportpipes.craft.icepipe");
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new PipeType(pipeBaseDuctType, "Void", LangConf.Key.PIPES_VOID.get(), "transportpipes.craft.voidpipe");
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new PipeType(pipeBaseDuctType, "Extraction", LangConf.Key.PIPES_EXTRACTION.get(), "transportpipes.craft.extractionpipe");
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new PipeType(pipeBaseDuctType, "Crafting", LangConf.Key.PIPES_CRAFTING.get(), "transportpipes.craft.craftingpipe");
        pipeBaseDuctType.registerDuctType(pipeType);

        //connect correctly
        pipeBaseDuctType.ductTypeOf("White").connectToAll();
        pipeBaseDuctType.ductTypeOf("Blue").connectToAll().disconnectFromClasses(ColoredPipeType.class).connectTo("White", "Blue");
        pipeBaseDuctType.ductTypeOf("Red").connectToAll().disconnectFromClasses(ColoredPipeType.class).connectTo("White", "Red");
        pipeBaseDuctType.ductTypeOf("Yellow").connectToAll().disconnectFromClasses(ColoredPipeType.class).connectTo("White", "Yellow");
        pipeBaseDuctType.ductTypeOf("Green").connectToAll().disconnectFromClasses(ColoredPipeType.class).connectTo("White", "Green");
        pipeBaseDuctType.ductTypeOf("Black").connectToAll().disconnectFromClasses(ColoredPipeType.class).connectTo("White", "Black");
        pipeBaseDuctType.ductTypeOf("Golden").connectToAll();
        pipeBaseDuctType.ductTypeOf("Iron").connectToAll();
        pipeBaseDuctType.ductTypeOf("Ice").connectToAll();
        pipeBaseDuctType.ductTypeOf("Void").connectToAll();
        pipeBaseDuctType.ductTypeOf("Extraction").connectToAll();
        pipeBaseDuctType.ductTypeOf("Crafting").connectToAll();

    }

    @Override
    public void registerRecipes() {
        BaseDuctType<Pipe> pipeBaseDuctType = ductRegister.baseDuctTypeOf("pipe");

        DuctType ductType;

        ductType = pipeBaseDuctType.ductTypeOf("White");
        Objects.requireNonNull(ductType).setDuctRecipe(itemService.createShapedRecipe(transportPipes, "white_pipe", pipeBaseDuctType.getItemManager().getClonedItem(ductType), new String[]{" a ", "a a", " a "}, 'a', Material.GLASS));
        ductType = pipeBaseDuctType.ductTypeOf("Blue");
        Objects.requireNonNull(ductType).setDuctRecipe(itemService.createShapedRecipe(transportPipes, "blue_pipe", pipeBaseDuctType.getItemManager().getClonedItem(ductType), new String[]{" a ", "aba", " a "}, 'a', Material.GLASS, 'b', Material.BLUE_DYE));
        ductType = pipeBaseDuctType.ductTypeOf("Red");
        Objects.requireNonNull(ductType).setDuctRecipe(itemService.createShapedRecipe(transportPipes, "red_pipe", pipeBaseDuctType.getItemManager().getClonedItem(ductType), new String[]{" a ", "aba", " a "}, 'a', Material.GLASS, 'b', Material.RED_DYE));
        ductType = pipeBaseDuctType.ductTypeOf("Yellow");
        Objects.requireNonNull(ductType).setDuctRecipe(itemService.createShapedRecipe(transportPipes, "yellow_pipe", pipeBaseDuctType.getItemManager().getClonedItem(ductType), new String[]{" a ", "aba", " a "}, 'a', Material.GLASS, 'b', Material.YELLOW_DYE));
        ductType = pipeBaseDuctType.ductTypeOf("Green");
        Objects.requireNonNull(ductType).setDuctRecipe(itemService.createShapedRecipe(transportPipes, "green_pipe", pipeBaseDuctType.getItemManager().getClonedItem(ductType), new String[]{" a ", "aba", " a "}, 'a', Material.GLASS, 'b', Material.GREEN_DYE));
        ductType = pipeBaseDuctType.ductTypeOf("Black");
        Objects.requireNonNull(ductType).setDuctRecipe(itemService.createShapedRecipe(transportPipes, "black_pipe", pipeBaseDuctType.getItemManager().getClonedItem(ductType), new String[]{" a ", "aba", " a "}, 'a', Material.GLASS, 'b', Material.BLACK_DYE));
        ductType = pipeBaseDuctType.ductTypeOf("Golden");
        Objects.requireNonNull(ductType).setDuctRecipe(itemService.createShapedRecipe(transportPipes, "golden_pipe", pipeBaseDuctType.getItemManager().getClonedItem(ductType), new String[]{" a ", "aba", " a "}, 'a', Material.GLASS, 'b', Material.GOLD_BLOCK));
        ductType = pipeBaseDuctType.ductTypeOf("Iron");
        Objects.requireNonNull(ductType).setDuctRecipe(itemService.createShapedRecipe(transportPipes, "iron_pipe", pipeBaseDuctType.getItemManager().getClonedItem(ductType), new String[]{" a ", "aba", " a "}, 'a', Material.GLASS, 'b', Material.IRON_BLOCK));
        ductType = pipeBaseDuctType.ductTypeOf("Ice");
        Objects.requireNonNull(ductType).setDuctRecipe(itemService.createShapedRecipe(transportPipes, "ice_pipe", pipeBaseDuctType.getItemManager().getClonedItem(ductType), new String[]{" a ", "aba", " a "}, 'a', Material.GLASS, 'b', Material.SNOW_BLOCK));
        ductType = pipeBaseDuctType.ductTypeOf("Void");
        Objects.requireNonNull(ductType).setDuctRecipe(itemService.createShapedRecipe(transportPipes, "void_pipe", pipeBaseDuctType.getItemManager().getClonedItem(ductType), new String[]{" a ", "aba", " a "}, 'a', Material.GLASS, 'b', Material.OBSIDIAN));
        ductType = pipeBaseDuctType.ductTypeOf("Extraction");
        Objects.requireNonNull(ductType).setDuctRecipe(itemService.createShapedRecipe(transportPipes, "extraction_pipe", pipeBaseDuctType.getItemManager().getClonedItem(ductType), new String[]{" a ", "aba", " a "}, 'a', Material.GLASS, 'b', Tag.PLANKS.getValues()));
        ductType = pipeBaseDuctType.ductTypeOf("Crafting");
        Objects.requireNonNull(ductType).setDuctRecipe(itemService.createShapedRecipe(transportPipes, "crafting_pipe", pipeBaseDuctType.getItemManager().getClonedItem(ductType), new String[]{" a ", "aba", " a "}, 'a', Material.GLASS, 'b', Material.CRAFTING_TABLE));

        wrenchRecipe = itemService.createShapedRecipe(transportPipes, "wrench", itemService.getWrench(), new String[]{" a ", "aba", " a "}, 'a', Material.REDSTONE, 'b', Material.STICK);
        Iterator<Recipe> iterator = Bukkit.recipeIterator();
        while (iterator.hasNext()) {
        	if (iterator.next().getResult().equals(wrenchRecipe.getResult())) return;
        }
        Bukkit.addRecipe(wrenchRecipe);
    }

    public ShapedRecipe getWrenchRecipe() {
        return wrenchRecipe;
    }

    @Override
    public void tick() {
        tickCounter++;
        tickCounter %= BIG_TICK_COUNT;
        boolean bigTick = tickCounter == 0;

        if(bigTick) {
            transportPipes.runTaskSync(() -> {
                Set<World> worlds = globalDuctManager.getDucts().keySet();
                for (World world : worlds) {
                    ConcurrentSkipListMap<BlockLocation, Duct> ductMap = globalDuctManager.getDucts().get(world);
                    if (ductMap != null) {
                        for (Duct duct : ductMap.values()) {
                            if (duct instanceof Pipe && duct.isInLoadedChunk()) {
                                duct.syncBigTick(this);
                            }
                        }
                    }
                }
            });
        }

        Set<World> worlds = globalDuctManager.getDucts().keySet();
        for (World world : worlds) {
            ConcurrentSkipListMap<BlockLocation, Duct> ductMap = globalDuctManager.getDucts().get(world);
            if (ductMap != null) {
                for (Duct duct : ductMap.values()) {
                    if (duct instanceof Pipe && duct.isInLoadedChunk()) {
                        duct.tick(bigTick, transportPipes, this, generalConf);
                    }
                }
                for (Duct duct : ductMap.values()) {
                    if (duct instanceof Pipe && duct.isInLoadedChunk()) {
                        duct.postTick(bigTick, transportPipes, this, generalConf);
                    }
                }
            }
        }

    }

    public Set<PipeItem> getPlayerPipeItems(Player player) {
        return playerItems.computeIfAbsent(player, p -> ConcurrentHashMap.newKeySet());
    }
    
    public void clearPlayerPipeItems(Player player) {
    	playerItems.remove(player);
    }

    public void spawnPipeItem(PipeItem pipeItem) {
        List<Player> playerList = WorldUtils.getPlayerList(pipeItem.getWorld());
        for (Player player : playerList) {
            PlayerSettingsConf conf = playerSettingsService.getOrCreateSettingsConf(player);
            int renderDistance = conf.getRenderDistance();
            if (conf.isShowItems() && player.getLocation().distance(pipeItem.getBlockLoc().toLocation(pipeItem.getWorld())) <= renderDistance) {
                getPlayerPipeItems(player).add(pipeItem);
                protocolService.sendPipeItem(player, pipeItem);
            }
        }
    }

    public void putPipeItemInPipe(PipeItem pipeItem) {
        Pipe pipeAtBlockLoc = (Pipe) globalDuctManager.getDuctAtLoc(pipeItem.getWorld(), pipeItem.getBlockLoc());
        if (pipeAtBlockLoc == null) {
            throw new IllegalStateException("pipe item can't be created because on the given location is no pipe");
        }
        pipeAtBlockLoc.putPipeItem(pipeItem);
    }

    public void updatePipeItemPosition(PipeItem pipeItem) {
        List<Player> playerList = WorldUtils.getPlayerList(pipeItem.getWorld());
        for (Player p : playerList) {
            if (getPlayerPipeItems(p).contains(pipeItem)) {
                protocolService.updatePipeItem(p, pipeItem);
            }
        }
    }

    public void despawnPipeItem(PipeItem pipeItem) {
        List<Player> playerList = WorldUtils.getPlayerList(pipeItem.getWorld());
        for (Player p : playerList) {
            if (getPlayerPipeItems(p).remove(pipeItem)) {
                protocolService.removePipeItem(p, pipeItem);
            }
        }
    }

    @Override
    public void notifyDuctShown(Duct duct, Player p) {
        super.notifyDuctShown(duct, p);
        if (!playerSettingsService.getOrCreateSettingsConf(p).isShowItems()) {
            return;
        }
        Pipe pipe = (Pipe) duct;
        Set<PipeItem> playerPipeItems = getPlayerPipeItems(p);
        for (PipeItem pipeItem : pipe.getItems()) {
            if (playerPipeItems.add(pipeItem)) {
                protocolService.sendPipeItem(p, pipeItem);
            }
        }
    }

    @Override
    public void notifyDuctHidden(Duct duct, Player p) {
        super.notifyDuctHidden(duct, p);
        Pipe pipe = (Pipe) duct;
        Set<PipeItem> playerPipeItems = getPlayerPipeItems(p);
        for (PipeItem pipeItem : pipe.getItems()) {
            if (playerPipeItems.remove(pipeItem)) {
                protocolService.removePipeItem(p, pipeItem);
            }
        }
        for (PipeItem pipeItem : pipe.getFutureItems()) {
            if (playerPipeItems.remove(pipeItem)) {
                protocolService.removePipeItem(p, pipeItem);
            }
        }
        for (PipeItem pipeItem : pipe.getUnloadedItems()) {
            if (playerPipeItems.remove(pipeItem)) {
                protocolService.removePipeItem(p, pipeItem);
            }
        }
    }
}
