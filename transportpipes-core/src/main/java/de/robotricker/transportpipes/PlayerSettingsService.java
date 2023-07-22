package de.robotricker.transportpipes;

import de.robotricker.transportpipes.config.GeneralConf;
import de.robotricker.transportpipes.config.PlayerSettingsConf;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class PlayerSettingsService {

    @Inject
    private TransportPipes transportPipes;
    @Inject
    private GeneralConf generalConf;

    private final Map<Player, PlayerSettingsConf> cachedSettingsConfs;

    public PlayerSettingsService() {
        this.cachedSettingsConfs = new HashMap<>();
    }

    public PlayerSettingsConf getOrCreateSettingsConf(Player p) {
        if (!cachedSettingsConfs.containsKey(p)) {
            cachedSettingsConfs.put(p, new PlayerSettingsConf(transportPipes, generalConf, p));
        }
        return cachedSettingsConfs.get(p);
    }
    
    public void clearSettingsConf(Player p) {
    	cachedSettingsConfs.remove(p);
    }

}
