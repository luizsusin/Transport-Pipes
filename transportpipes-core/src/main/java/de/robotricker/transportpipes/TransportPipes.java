package de.robotricker.transportpipes;

import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import co.aikar.commands.PaperCommandManager;
import de.robotricker.transportpipes.api.TransportPipesAPI;
import de.robotricker.transportpipes.commands.TPCommand;
import de.robotricker.transportpipes.config.GeneralConf;
import de.robotricker.transportpipes.config.LangConf;
import de.robotricker.transportpipes.config.PlayerSettingsConf;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctRegister;
import de.robotricker.transportpipes.duct.factory.PipeFactory;
import de.robotricker.transportpipes.duct.manager.GlobalDuctManager;
import de.robotricker.transportpipes.duct.manager.PipeManager;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.inventory.PlayerSettingsInventory;
import de.robotricker.transportpipes.items.ItemService;
import de.robotricker.transportpipes.items.PipeItemManager;
import de.robotricker.transportpipes.listener.DuctListener;
import de.robotricker.transportpipes.listener.PlayerListener;
import de.robotricker.transportpipes.listener.TPContainerListener;
import de.robotricker.transportpipes.listener.WorldListener;
import de.robotricker.transportpipes.log.LoggerService;
import de.robotricker.transportpipes.protocol.ProtocolProvider;
import de.robotricker.transportpipes.protocol.ProtocolService;
import de.robotricker.transportpipes.rendersystems.RenderSystem;
import de.robotricker.transportpipes.rendersystems.pipe.modelled.ModelledPipeRenderSystem;
import de.robotricker.transportpipes.rendersystems.pipe.vanilla.VanillaPipeRenderSystem;
import de.robotricker.transportpipes.saving.DiskService;
import de.robotricker.transportpipes.utils.LWCUtils;
import de.robotricker.transportpipes.utils.ProtectionUtils.ProtectionUtils;
import de.robotricker.transportpipes.utils.WorldEditUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TransportPipes extends JavaPlugin {

    private Injector injector;

    private ThreadService thread;
    private DiskService diskService;

    private static ProtocolProvider protocolProvider;
    private static Class<?> fakeBlockClass;

    @Override
    public void onEnable() {
        // Load protocol-specific classes
        String version = Bukkit.getBukkitVersion().split("-")[0];
        String protocolProviderClassName = TransportPipes.class.getPackage().getName() + ".protocol.Protocol_";
        String fakeBlockClassName = TransportPipes.class.getPackage().getName() + ".utils.ProtectionUtils.FakeBlock_";

        switch(version) {
            case "1.16.5":
                try {
                    protocolProvider = (ProtocolProvider) Class.forName(protocolProviderClassName + "1_16_5").getDeclaredConstructor().newInstance();
                    fakeBlockClass = Class.forName(fakeBlockClassName + "1_16_5");
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
                    Bukkit.getLogger().log(Level.SEVERE, "TransportPipes could not find a valid implementation for this server version.");
                }
                break;
            case "1.17":
                try {
                    protocolProvider = (ProtocolProvider) Class.forName(protocolProviderClassName + "1_17").getDeclaredConstructor().newInstance();
                    fakeBlockClass = Class.forName(fakeBlockClassName + "1_17");
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
                    Bukkit.getLogger().log(Level.SEVERE, "TransportPipes could not find a valid implementation for this server version.");
                }
                break;
            case "1.17.1":
            case "1.18":
            case "1.18.1":
            case "1.18.2":
                try {
                    protocolProvider = (ProtocolProvider) Class.forName(protocolProviderClassName + "1_17_1").getDeclaredConstructor().newInstance();
                    fakeBlockClass = Class.forName(fakeBlockClassName + "1_17_1");
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
                    Bukkit.getLogger().log(Level.SEVERE, "TransportPipes could not find a valid implementation for this server version.");
                }
                break;
            case "1.19":
            case "1.19.1":
            case "1.19.2":
                try {
                    protocolProvider = (ProtocolProvider) Class.forName(protocolProviderClassName + "1_19").getDeclaredConstructor().newInstance();
                    fakeBlockClass = Class.forName(fakeBlockClassName + "1_19");
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
                    Bukkit.getLogger().log(Level.SEVERE, "TransportPipes could not find a valid implementation for this server version.");
                }
                break;
            case "1.19.3":
                try {
                    protocolProvider = (ProtocolProvider) Class.forName(protocolProviderClassName + "1_19_3").getDeclaredConstructor().newInstance();
                    fakeBlockClass = Class.forName(fakeBlockClassName + "1_19_3");
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
                    Bukkit.getLogger().log(Level.SEVERE, "TransportPipes could not find a valid implementation for this server version.");
                }
                break;
            case "1.19.4":
                try {
                    protocolProvider = (ProtocolProvider) Class.forName(protocolProviderClassName + "1_19_4").getDeclaredConstructor().newInstance();
                    fakeBlockClass = Class.forName(fakeBlockClassName + "1_19_4");
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
                    Bukkit.getLogger().log(Level.SEVERE, "TransportPipes could not find a valid implementation for this server version.");
                }
                break;
            default:
                getLogger().log(Level.SEVERE, "------------------------------------------");
                getLogger().log(Level.SEVERE, "TransportPipes currently only works with Minecraft 1.16.5 through 1.19.4. You are running version " + version + ".");
                getLogger().log(Level.SEVERE, "------------------------------------------");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
        }

        if (Files.isRegularFile(Paths.get(getDataFolder().getPath(), "recipes.yml"))) {
            getLogger().log(Level.SEVERE, "------------------------------------------");
            getLogger().log(Level.SEVERE, "Please delete the old plugins/TransportPipes directory so TransportPipes can recreate it with a bunch of new config values");
            getLogger().log(Level.SEVERE, "------------------------------------------");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        //Initialize dependency injector
        injector = new InjectorBuilder().addDefaultHandlers("de.robotricker.transportpipes").create();
        injector.register(Logger.class, getLogger());
        injector.register(Plugin.class, this);
        injector.register(JavaPlugin.class, this);
        injector.register(TransportPipes.class, this);

        //Initialize logger
        LoggerService logger = injector.getSingleton(LoggerService.class);

        //Initialize configs
        injector.getSingleton(GeneralConf.class);
        injector.register(LangConf.class, new LangConf(this, injector.getSingleton(GeneralConf.class).getLanguage()));

        //Initialize API
        injector.getSingleton(TransportPipesAPI.class);

        //Initialize thread
        thread = injector.getSingleton(ThreadService.class);
        thread.start();
        
        injector.getSingleton(ItemService.class);

        //Register pipe
        BaseDuctType<Pipe> baseDuctType = injector.getSingleton(DuctRegister.class).registerBaseDuctType("Pipe", PipeManager.class, PipeFactory.class, PipeItemManager.class);
        baseDuctType.setModelledRenderSystem(injector.newInstance(ModelledPipeRenderSystem.class));
        baseDuctType.setVanillaRenderSystem(injector.newInstance(VanillaPipeRenderSystem.class));

        //Register listeners
        Bukkit.getPluginManager().registerEvents(injector.getSingleton(TPContainerListener.class), this);
        Bukkit.getPluginManager().registerEvents(injector.getSingleton(PlayerListener.class), this);
        Bukkit.getPluginManager().registerEvents(injector.getSingleton(DuctListener.class), this);
        Bukkit.getPluginManager().registerEvents(injector.getSingleton(WorldListener.class), this);
        Bukkit.getPluginManager().registerEvents(injector.getSingleton(PlayerSettingsInventory.class), this);
        Bukkit.getPluginManager().registerEvents(injector.getSingleton(ResourcepackService.class), this);
        Bukkit.getPluginManager().registerEvents(injector.getSingleton(ProtectionUtils.class), this);

        //Register commands
        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.enableUnstableAPI("help");
        commandManager.registerCommand(injector.getSingleton(TPCommand.class));
        commandManager.getCommandCompletions().registerCompletion("baseDuctType", c -> injector.getSingleton(DuctRegister.class).baseDuctTypes().stream().map(BaseDuctType::getName).collect(Collectors.toList()));

        diskService = injector.getSingleton(DiskService.class);

        TPContainerListener tpContainerListener = injector.getSingleton(TPContainerListener.class);
        runTaskSync(() -> {
            for (World world : Bukkit.getWorlds()) {
                for (Chunk loadedChunk : world.getLoadedChunks()) {
                    tpContainerListener.handleChunkLoadSync(loadedChunk, true);
                }
                diskService.loadDuctsSync(world);
            }
        });

        if (Bukkit.getPluginManager().isPluginEnabled("LWC")) {
            try {
                com.griefcraft.scripting.Module module = injector.getSingleton(LWCUtils.class);
                com.griefcraft.lwc.LWC.getInstance().getModuleLoader().registerModule(this, module);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
            WorldEditUtils weUtils = injector.getSingleton(WorldEditUtils.class);
            com.sk89q.worldedit.WorldEdit.getInstance().getEventBus().register(weUtils);
        }

    }

    @Override
    public void onDisable() {
        if (thread != null) {
            // Stop tpThread gracefully
            try {
                thread.stopRunning();
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (World world : Bukkit.getWorlds()) {
                saveWorld(world);
            }
        }
    }

    public void saveWorld(World world) {
        diskService.saveDuctsSync(world);
    }

    public void runTaskSync(Runnable task) {
        if (isEnabled()) {
            Bukkit.getScheduler().runTask(this, task);
        }
    }

    public void runTaskAsync(Runnable runnable, long delay) {
        thread.getTasks().put(runnable, delay);
    }

    public Injector getInjector() {
        return injector;
    }

    public ProtocolProvider getProtocolProvider() {
        return protocolProvider;
    }

    public Block getFakeBlock(World world, Location location, Material material) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> [] paramTypes = { World.class, Location.class, Material.class };
        Object [] paramValues = { world, location, material };

        return (Block) fakeBlockClass.getConstructor(paramTypes).newInstance(paramValues);
    }

    public void changeRenderSystem(Player p, String newRenderSystemName) {
        PlayerSettingsConf playerSettingsConf = injector.getSingleton(PlayerSettingsService.class).getOrCreateSettingsConf(p);

        // change render system
        String oldRenderSystemName = playerSettingsConf.getRenderSystemName();
        if (oldRenderSystemName.equalsIgnoreCase(newRenderSystemName)) {
            return;
        }
        playerSettingsConf.setRenderSystemName(newRenderSystemName);

        DuctRegister ductRegister = injector.getSingleton(DuctRegister.class);
        GlobalDuctManager globalDuctManager = injector.getSingleton(GlobalDuctManager.class);
        ProtocolService protocolService = injector.getSingleton(ProtocolService.class);

        for (BaseDuctType<? extends Duct> baseDuctType : ductRegister.baseDuctTypes()) {
            RenderSystem oldRenderSystem = RenderSystem.getRenderSystem(oldRenderSystemName, baseDuctType);

            // switch render system
            Iterator<Duct> ductIt = globalDuctManager.getPlayerDucts(p).iterator();
            while (ductIt.hasNext()) {
                Duct nextDuct = ductIt.next();
                protocolService.removeASD(p, Objects.requireNonNull(oldRenderSystem).getASDForDuct(nextDuct));
                ductIt.remove();
            }

        }
    }

    public long convertVersionToLong(String version) {
        long versionLong = 0;
        try {
            if (version.contains("-")) {
                for (String subVersion : version.split("-")) {
                    if (subVersion.startsWith("b")) {
                        int buildNumber = 0;
                        String buildNumberString = subVersion.substring(1);
                        if (!buildNumberString.equalsIgnoreCase("CUSTOM")) {
                            buildNumber = Integer.parseInt(buildNumberString);
                        }
                        versionLong |= buildNumber;
                    } else if (!subVersion.equalsIgnoreCase("SNAPSHOT")) {
                        versionLong |= (long) convertMainVersionStringToInt(subVersion) << 32;
                    }
                }
            } else {
                versionLong = (long) convertMainVersionStringToInt(version) << 32;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionLong;
    }

    private int convertMainVersionStringToInt(String mainVersion) {
        int versionInt = 0;
        if (mainVersion.contains(".")) {
            // shift for every version number 1 byte to the left
            int leftShift = (mainVersion.split("\\.").length - 1) * 8;
            for (String subVersion : mainVersion.split("\\.")) {
                byte v = Byte.parseByte(subVersion);
                versionInt |= ((int) v << leftShift);
                leftShift -= 8;
            }
        } else {
            versionInt = Byte.parseByte(mainVersion);
        }
        return versionInt;
    }

}
