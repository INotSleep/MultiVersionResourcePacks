package me.inotsleep.multiversionresourcepacks;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import me.inotsleep.utils.AbstractBukkitPlugin;
import me.inotsleep.utils.logging.LoggingManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.inotsleep.multiversionresourcepacks.Utils.calcSHA1;

public final class MultiVersionResourcePacks extends AbstractBukkitPlugin<MultiVersionResourcePacks> {
    public static List<String> sortedKeyList;
    public static Config config;
    public static ViaAPI<Player> viaAPI;

    public static @NotNull Plugin getInstance() {
        return getInstanceByClazz(MultiVersionResourcePacks.class);
    }

    @Override
    public void doDisable() {
        HttpPackHost.stop();
    }

    @Override
    public void doEnable() {
        config = new Config(this);
        reloadConfig();
        HttpPackHost.startServer(this);

        viaAPI = Via.getAPI();

        Bukkit.getPluginManager().registerEvents(new Listeners(viaAPI), this);
    }

    @Override
    public void reloadConfig() {
        HttpPackHost.stop();
        config.reload();

        sortedKeyList = config
                .resourcePackMap
                .keySet()
                .stream()
                .sorted(
                        Comparator.comparingInt(s ->
                                extractVersion(s).protocol)
                ).map(
                        s -> {
                            Result result = extractVersion(s);
                            return s.replace(result.version(), String.valueOf(result.protocol()));
                        }
                    )
                .toList();

        List<String> toRemove = new ArrayList<>();

        URI baseUri = URI.create(config.fileHostPublic);

        config.resourcePackMap.forEach((protocol, pack) -> {
            if (pack.fileName == null) return;

            try {
                pack.hash = calcSHA1(new File(config.packFolder, pack.fileName));
                pack.url = baseUri.resolve(pack.fileName).toString();
            } catch (NullPointerException | FileNotFoundException e) {
                LoggingManager.error("Pack with file name "+pack.fileName+" do not exists!");
                toRemove.add(protocol);
            }
            catch (Exception e) {
                toRemove.add(protocol);
                e.printStackTrace();
            }
        });

        toRemove.forEach(config.resourcePackMap::remove);
    }

    private static @NotNull Result extractVersion(String s) {
        Pattern pattern = Pattern.compile("[0-9_\\-]+");
        Matcher m = pattern.matcher(s);
        m.find();
        String version = m.group();

        int protocol;
        try {
            protocol = Integer.parseInt(version);
        } catch (NumberFormatException e) {
            protocol = Utils.getProtocolFromVersionString(version);
        }
        Result result = new Result(version, protocol);
        return result;
    }

    private record Result(String version, int protocol) {
    }
}
