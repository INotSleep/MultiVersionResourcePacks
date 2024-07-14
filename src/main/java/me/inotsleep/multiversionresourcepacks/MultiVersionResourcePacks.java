package me.inotsleep.multiversionresourcepacks;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import me.inotsleep.utils.AbstractPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.inotsleep.multiversionresourcepacks.Utils.calcSHA1;

public final class MultiVersionResourcePacks extends AbstractPlugin {
    public static List<String> sortedKeyList;
    public static Config config;
    public static ViaAPI<Player> viaAPI;
    @Override
    public void doDisable() {

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
        config.reload();

        Pattern pattern = Pattern.compile("[0-9_]+");

        sortedKeyList = config
                .resourcePackMap
                .keySet()
                .stream()
                .sorted(
                        Comparator.comparingInt(s ->
                        {
                            Matcher m = pattern.matcher(s);
                            m.find();
                            String version = m.group();
                            int protocol;
                            try {
                                protocol = Integer.parseInt(version);
                            } catch (NumberFormatException e) {
                                protocol = Utils.getProtocolFromVersionString(version);
                            }
                            return protocol;
                        })
                ).map(s ->
                {
                    Matcher m = pattern.matcher(s);
                    m.find();
                    String version = m.group();
                    int protocol;
                    try {
                        protocol = Integer.parseInt(version);
                    } catch (NumberFormatException e) {
                        protocol = Utils.getProtocolFromVersionString(version);
                    }
                    return s.replace(version, String.valueOf(protocol));
                })
                .toList();

        List<String> toRemove = new ArrayList<>();

        config.resourcePackMap.forEach((protocol, pack) -> {
            if (pack.isUrl) return;

            try {
                pack.hash = calcSHA1(new File(config.packFolder, pack.fileName));
                pack.url = URI.create(config.fileHostPublic).resolve(pack.fileName).toString();
            } catch (NullPointerException | FileNotFoundException e) {
                printError("Pack with file name "+pack.fileName+" do not exists!", false);
                toRemove.add(protocol);
            }
            catch (Exception e) {
                toRemove.add(protocol);
                e.printStackTrace();
            }
        });

        toRemove.forEach(config.resourcePackMap::remove);
    }
}
