package cn.superiormc.ultimateshop.managers;

import cn.superiormc.ultimateshop.UltimateShop;
import cn.superiormc.ultimateshop.objects.ObjectShop;
import cn.superiormc.ultimateshop.objects.items.shbobjects.ObjectRandomPlaceholder;
import cn.superiormc.ultimateshop.objects.menus.ObjectMenu;
import cn.superiormc.ultimateshop.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.ClickType;

import java.io.File;
import java.util.*;

public class ConfigManager {

    public static ConfigManager configManager;

    public FileConfiguration config;

    public Map<String, ObjectShop> shopConfigs = new HashMap<>();

    public Map<String, ObjectRandomPlaceholder> randomPlaceholders = new HashMap<>();

    public ConfigManager() {
        configManager = this;
        UltimateShop.instance.saveDefaultConfig();
        this.config = UltimateShop.instance.getConfig();
        initShopConfigs();
        initMenuConfigs();
        if (!UltimateShop.freeVersion) {
            initRandomPlaceholder();
        }
    }

    private void initShopConfigs() {
        this.shopConfigs = new HashMap<>();
        File dir = new File(UltimateShop.instance.getDataFolder(), "shops");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File[] files = dir.listFiles();
        if (!Objects.nonNull(files) && files.length != 0) {
            return;
        }
        for (File file : files) {
            String fileName = file.getName();
            if (fileName.endsWith(".yml")) {
                String substring = fileName.substring(0, fileName.length() - 4);
                shopConfigs.put(substring,
                        new ObjectShop(substring, YamlConfiguration.loadConfiguration(file)));
                Bukkit.getConsoleSender().sendMessage("§x§9§8§F§B§9§8[UltimateShop] §fLoaded shop: " +
                        fileName + "!");
            }
        }
    }

    private void initMenuConfigs() {
        File dir = new File(UltimateShop.instance.getDataFolder(), "menus");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File[] files = dir.listFiles();
        if (!Objects.nonNull(files) && files.length != 0) {
            return;
        }
        for (File file : files) {
            String fileName = file.getName();
            if (fileName.endsWith(".yml")) {
                String substring = fileName.substring(0, fileName.length() - 4);
                if (ObjectMenu.shopMenuNames.contains(substring)) {
                    continue;
                }
                if (ObjectMenu.buyMoreMenuNames.contains(substring)) {
                    continue;
                }
                new ObjectMenu(substring);
            }
        }
    }

    private void initRandomPlaceholder() {
        this.randomPlaceholders = new HashMap<>();
        ConfigurationSection tempVal1 = config.getConfigurationSection("placeholder.random");
        if (tempVal1 == null) {
            return;
        }
        for (String key : tempVal1.getKeys(false)) {
            randomPlaceholders.put(key, new ObjectRandomPlaceholder(key, tempVal1.getConfigurationSection(key)));
        }
    }

    public ObjectShop getShop(String fileName) {
        return shopConfigs.get(fileName);
    }

    public List<ObjectShop> getShopList() {
        List<ObjectShop> resultShops = new ArrayList<>();
        for (String key : shopConfigs.keySet()) {
            resultShops.add(shopConfigs.get(key));
        }
        return resultShops;
    }

    public ObjectRandomPlaceholder getRandomPlaceholder(String id) {
        return randomPlaceholders.get(id);
    }

    public List<String> getStringListWithColor(String... args) {
        List<String> resultList = new ArrayList<>();
        for (String s : config.getStringList(args[0])) {
            for (int i = 1 ; i < args.length ; i += 2) {
                String var = "{" + args[i] + "}";
                if (args[i + 1] == null) {
                    s = s.replace(var, "");
                }
                else {
                    s = s.replace(var, args[i + 1]);
                }
            }
            resultList.add(TextUtil.parse(s));
        }
        return resultList;
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public List<Integer> getIntList(String path) {
        return config.getIntegerList(path);
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path, false);
    }

    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }

    public long getLong(String path, long defaultValue) {
        return config.getLong(path, defaultValue);
    }

    public ConfigurationSection getSection(String path) {
        return config.getConfigurationSection(path);
    }

    public String getString(String path, String... args) {
        String s = config.getString(path);
        if (s == null) {
            if (args.length == 0) {
                return null;
            }
            s = args[0];
        }
        for (int i = 1 ; i < args.length ; i += 2) {
            String var = "{" + args[i] + "}";
            if (args[i + 1] == null) {
                s = s.replace(var, "");
            }
            else {
                s = s.replace(var, args[i + 1]);
            }
        }
        return s.replace("{plugin_folder}", String.valueOf(UltimateShop.instance.getDataFolder()));
    }

    public String getClickAction(ClickType type) {
        ConfigurationSection tempVal1 = config.getConfigurationSection("menu.click-event");
        if (tempVal1 == null) {
            return "none";
        }
        for (String s : tempVal1.getKeys(false)) {
            for (String t : tempVal1.getString(s).split(";;")) {
                if (t.equals(type.name())){
                    return s;
                }
            }
        }
        return "none";
    }

}
