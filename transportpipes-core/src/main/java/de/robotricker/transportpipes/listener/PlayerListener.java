package de.robotricker.transportpipes.listener;

import de.robotricker.transportpipes.PlayerSettingsService;
import de.robotricker.transportpipes.config.GeneralConf;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctRegister;
import de.robotricker.transportpipes.duct.manager.DuctManager;
import de.robotricker.transportpipes.duct.manager.GlobalDuctManager;
import de.robotricker.transportpipes.duct.manager.PipeManager;
import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.duct.types.DuctType;
import de.robotricker.transportpipes.items.ItemService;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class PlayerListener implements Listener {

    @Inject
    private GlobalDuctManager globalDuctManager;

    @Inject
    private DuctRegister ductRegister;

    @Inject
    private ItemService itemService;

    @Inject
    private GeneralConf generalConf;
    
    @Inject
    private PlayerSettingsService playerSettingsService;

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        globalDuctManager.clearPlayerDucts(e.getPlayer());
        ((PipeManager) (DuctManager<? extends Duct>) ductRegister.baseDuctTypeOf("pipe").getDuctManager()).clearPlayerPipeItems(e.getPlayer());
        playerSettingsService.clearSettingsConf(e.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        //make sure that all duct that were visible to the player get removed, so they will spawn again when the player is nearby
        globalDuctManager.getPlayerDucts(e.getPlayer()).clear();
        ((PipeManager) (DuctManager<? extends Duct>) ductRegister.baseDuctTypeOf("pipe").getDuctManager()).getPlayerPipeItems(e.getPlayer()).clear();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (generalConf.isCraftingEnabled()) {
            List<NamespacedKey> keys = new ArrayList<>();
            for (BaseDuctType<? extends Duct> bdt : ductRegister.baseDuctTypes()) {
                for (DuctType type : bdt.ductTypes()) {
                    if (type.getDuctRecipe() != null) {
                        NamespacedKey key = ((Keyed) type.getDuctRecipe()).getKey();
                        keys.add(key);
                    }
                }
                if (bdt.is("pipe")) {
                    keys.add(((PipeManager) bdt.getDuctManager()).getWrenchRecipe().getKey());
                }
            }
            event.getPlayer().discoverRecipes(keys);
        }
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe() == null) {
            return;
        }
        if (event.getView().getPlayer() instanceof Player player) {
            ItemStack result = event.getRecipe().getResult();
            SkullMeta resultMeta = result.getType() == Material.PLAYER_HEAD ? (SkullMeta) result.getItemMeta() : null;
            for (BaseDuctType<? extends Duct> baseDuctType : ductRegister.baseDuctTypes()) {
                for (DuctType ductType : baseDuctType.ductTypes()) {
                    ItemStack duct = baseDuctType.getItemManager().getItem(ductType);
                    // For some reason, ItemStack.isSimilar() does not work properly with custom player heads, so we have to do it this way, instead
                    if (resultMeta != null && duct.getType() == Material.PLAYER_HEAD) {
                        SkullMeta ductMeta = (SkullMeta) duct.getItemMeta();
                        // SkullMeta shouldn't be null for player_head material, but we'll check just in case since getItemMeta can technically return null
                        if (ductMeta != null) {
                            if (resultMeta.getOwningPlayer() == ductMeta.getOwningPlayer()) {
                                if (!ductType.hasPlayerCraftingPermission(player)) {
                                    event.getInventory().setResult(null);
                                    return;
                                }
                            }
                        }
                    }
                    else if (result.isSimilar(duct)) {
                        if (!ductType.hasPlayerCraftingPermission(player)) {
                            event.getInventory().setResult(null);
                            return;
                        }
                    }
                }
            }
            if (itemService.isWrench(event.getRecipe().getResult())) {
                if (!player.hasPermission("transportpipes.craft.wrench")) {
                    event.getInventory().setResult(null);
                }
            }
        }
    }

}
