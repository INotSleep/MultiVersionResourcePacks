package me.inotsleep.multiversionresourcepacks;

import me.inotsleep.utils.config.AbstractConfig;
import me.inotsleep.utils.config.Comment;
import me.inotsleep.utils.config.Path;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Config extends AbstractConfig {
    File packFolder;

    @Comment("To specify protocol version, you can use >=, <=, >, < and ==")
    @Comment("Recommended to use only >=, > and ==, because protocols sorted from lowest to highest")
    @Comment("If >, <, >=, <= finds any pack, it counties finding pack for next protocols.")
    @Comment("If == finds any pack, it will stop finding and send player this pack")
    @Comment("You can also specify version string. See:")
    @Comment("https://github.com/ViaVersion/ViaVersion/blob/master/api/src/main/java/com/viaversion/viaversion/api/protocol/version/ProtocolVersion.java#L45")
    @Comment("Use name in register method, not the field name")

    @Path("packs")
    public Map<String, Pack> resourcePackMap = new HashMap<>(Map.of(
            ">=1_16_4-1_16_5", new Pack(null, "8c96d8084fa706661d0a7cf9b084bef4161d520b", "https://mediafilez.forgecdn.net/files/5505/931/Faithful%2032x%20-%201.16.5.zip", false),
            "<753", new Pack("myPack.zip", null, null, false),
            "<=1_12_2", new Pack(null, "8c96d8084fa706661d0a7cf9b084bef4161d520b", "https://mediafilez.forgecdn.net/files/5505/931/Faithful%2032x%20-%201.16.5.zip", true)
    ));

    @Path("port")
    public int fileHostPort = 3000;

    @Path("bind-ip")
    public String fileHostBindIP = "0.0.0.0";

    @Comment("Here you need to specify pack host external ip,")
    @Comment("because plugin cannot know yours machine ip. Also it can be dangerous to show your ip to players,")
    @Comment("so you can connect nginx, cloudflare, etc.")

    @Path("public-link")
    public String fileHostPublic = "http://localhost:3000/";

    @Path("declined-message")
    public String declinedMessage = "You must accept resource pack in order to play on this server!";

    @Path("failed-message")
    public String failedMessage = "Unable to download resource pack. Try again!";

    @Comment("If enabled, will kick player, that got resourcepack and didn't accepted it or fully downloaded")
    @Comment("But he able to play")
    @Comment("Player will be kicked on chat, command sending and any interaction")
    @Path("kick-unfished-action")
    public boolean kickUnfishedAction = true;

    public Config(MultiVersionResourcePacks plugin) {
        super(plugin.getDataFolder(), "config.yml");
        packFolder = new File(plugin.getDataFolder(), "packs");

        if (!packFolder.exists()) {
            packFolder.mkdirs();
        }
    }

    @Comment("Amount of threads for http handler")
    @Path("http-handler-threads")
    public int threads = 2;
}
