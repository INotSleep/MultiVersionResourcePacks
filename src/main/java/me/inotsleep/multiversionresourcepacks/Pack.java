package me.inotsleep.multiversionresourcepacks;

import me.inotsleep.utils.config.Serializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class Pack implements Serializable {
    public String fileName;
    public String hash;
    public String url;
    public boolean isUrl;

    public Pack(String fileName, String hash, String url, boolean isUrl) {
        this.fileName = fileName;
        this.hash = hash;
        this.url = url;
        this.isUrl = isUrl;
    }

    @Override
    public ConfigurationSection serialize() {
        ConfigurationSection section = new YamlConfiguration();
        section.set("fileName", fileName);
        section.set("hash", hash);
        section.set("url", url);
        section.set("isUrl", isUrl);

        return section;
    }

    public static Pack deserialize(ConfigurationSection section) {
        return new Pack(section.getString("fileName"), section.getString("hash"), section.getString("url"), section.getBoolean("isUrl"));
    }
}
