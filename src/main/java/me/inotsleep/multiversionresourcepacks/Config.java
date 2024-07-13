package me.inotsleep.multiversionresourcepacks;

import me.inotsleep.utils.AbstractPlugin;
import me.inotsleep.utils.config.AbstractConfig;
import me.inotsleep.utils.config.Path;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Config extends AbstractConfig {
    File packFolder;

    @Path(path = "packs")
    public Map<String, Pack> resourcePackMap;

    @Path(path="settings.port")
    public int fileHostPort = 3000;

    @Path(path="settings.bind-ip")
    public String fileHostBindIP = "0.0.0.0";

    @Path(path="settings.public-link")
    public String fileHostPublic = "http://localhost:3000/";

    @Path(path = "settings.httpHandlerThreads")
    public int threads = 10;

    public Config(AbstractPlugin plugin) {
        super(plugin, "config.yml");
        packFolder = new File(plugin.getDataFolder(), "packs");
    }

    @Override
    public String getHeader() {
        return "You can define 2 types of packs:" + "\n"+
                "1. External pack: isUrl: true, url: url to pack, hash: hash of pack" +"\n"+
                "2. Internal pack: isUrl: false, fileName: name of pack" +"\n"+
                "\n"+
                "Internal packs must be in plugins/MultiVersionResourcePacks/packs folder." +"\n"+
                "\n"+
                "Protocols:" +"\n"+
                "To specify protocol version, you can use >=, <=, >, < and ==" +"\n"+
                "Recommended to use only >=, > and ==, because protocols sorted from lowest to highest" +"\n"+
                "If >, <, >=, <= finds any pack, it counties finding pack for next protocols." +"\n"+
                "If == finds any pack, it will stop finding and send player this pack" +"\n"+
                "\n"+
                "In settings.public-link you need to specify pack host external ip," +"\n"+
                "because plugin cannot know yours machine ip. Also it can be dangerous to show your ip to players," +"\n"+
                "so you can connect nginx, cloudflare, etc." +  "\n" +
                "\n" +
                "If you want specify by version, not by protocol number,\n" +
                "you need to use full version string.\n" +
                "You can find them here: https://github.com/ViaVersion/ViaVersion/blob/master/api/src/main/java/com/viaversion/viaversion/api/protocol/version/ProtocolVersion.java#L45\n" +
                "Use value, that specified in register method, not field name.";
    }

    @Override
    public void saveDefaults() {
        if (!packFolder.exists()) {
            packFolder.mkdir();
        }

        resourcePackMap = new HashMap<>();

        resourcePackMap.put(">=1.16.4-1.16.5", new Pack(null, "8c96d8084fa706661d0a7cf9b084bef4161d520b", "https://mediafilez.forgecdn.net/files/5505/931/Faithful%2032x%20-%201.16.5.zip", true));
        resourcePackMap.put("<753", new Pack("myPack.zip", null, null, false));
        resourcePackMap.put("<=1.12.2", new Pack(null, "8c96d8084fa706661d0a7cf9b084bef4161d520b", "https://mediafilez.forgecdn.net/files/5505/931/Faithful%2032x%20-%201.16.5.zip", true));
    }
}
