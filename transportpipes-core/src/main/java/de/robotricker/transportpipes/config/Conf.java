package de.robotricker.transportpipes.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public class Conf {

    private final Plugin configPlugin;
    private Path configFile;
    private final YamlConfiguration yamlConf;
    private final Map<String, Object> cachedValues = new HashMap<>();

    /**
     * @param onlyOverwriteExistingProperties when true: only sets oldconf properties if the property exists in the new conf
     */
    public Conf(Plugin configPlugin, String jarConfigName, String finalConfigPath, boolean onlyOverwriteExistingProperties) {
        this.configPlugin = configPlugin;

        // copy configuration from jar file
        try {
            Path finalConfigFile = Paths.get(configPlugin.getDataFolder().getPath(), finalConfigPath);

            Map<String, Object> valuesBefore = new LinkedHashMap<>();

            if (Files.isRegularFile(finalConfigFile)) {
                YamlConfiguration oldConf = YamlConfiguration.loadConfiguration(finalConfigFile.toFile());
                valuesBefore.putAll(oldConf.getValues(true));
                try {
                    Files.delete(finalConfigFile);
                }
                catch(NoSuchFileException e) {
                    Bukkit.getLogger().log(Level.SEVERE, "Unable to delete file " + finalConfigPath);
                }
            }

            InputStream is = configPlugin.getResource(jarConfigName);
            Files.createDirectories(finalConfigFile.getParent());

            if (is == null) {
                Bukkit.getLogger().log(Level.SEVERE, "InputStream is null for config " + jarConfigName + ".");
                Bukkit.getLogger().log(Level.SEVERE, "Unable to copy bytes to " + finalConfigFile + ".");
            }
            else {
                Files.copy(is, finalConfigFile, StandardCopyOption.REPLACE_EXISTING);
            }

            YamlConfiguration newConf = YamlConfiguration.loadConfiguration(finalConfigFile.toFile());
            Map<String, Object> valuesAfter = newConf.getValues(true);
            if (onlyOverwriteExistingProperties) {
                for (String key : valuesAfter.keySet()) {
                    if (valuesAfter.get(key) instanceof ConfigurationSection) {
                        continue;
                    }
                    if (valuesBefore.containsKey(key)) {
                        newConf.set(key, valuesBefore.get(key));
                    }
                }
            } else {
                for (String key : valuesBefore.keySet()) {
                    if (valuesBefore.get(key) instanceof ConfigurationSection) {
                        continue;
                    }
                    newConf.set(key, valuesBefore.get(key));
                }
            }

            newConf.save(finalConfigFile.toFile());

            this.configFile = finalConfigFile;
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.yamlConf = YamlConfiguration.loadConfiguration(configFile.toFile());
    }

    protected YamlConfiguration getYamlConf() {
        return yamlConf;
    }

    public void overrideSync(String key, Object value) {
        cachedValues.put(key, value);
        yamlConf.set(key, value);
        saveToFileSync();
    }

    public void overrideAsync(String key, Object value) {
        cachedValues.put(key, value);
        yamlConf.set(key, value);
        Bukkit.getScheduler().runTaskAsynchronously(configPlugin, this::saveToFileSync);
    }

    public Object read(String key) {
        if (cachedValues.containsKey(key)) {
            return cachedValues.get(key);
        }
        if (yamlConf.contains(key)) {
            Object val = yamlConf.get(key);
            cachedValues.put(key, val);
            return val;
        }
        return null;
    }

    public void reloadSync() {
        cachedValues.clear();
        try {
            yamlConf.load(configFile.toFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveToFileSync() {
        try {
            yamlConf.save(configFile.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
