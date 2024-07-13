package me.inotsleep.multiversionresourcepacks;

import com.viaversion.viaversion.api.ViaAPI;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Listeners implements Listener {
    ViaAPI<Player> viaAPI;
    public Listeners(ViaAPI<Player> viaAPI) {
        this.viaAPI = viaAPI;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

    }

    @EventHandler
    public void onPlayerSpawn(PlayerJoinEvent event) {
        int protocol = viaAPI.getPlayerVersion(event.getPlayer());
        Pack pack = getPackByProtocol(protocol);
        if (pack != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    event.getPlayer().setResourcePack(pack.url, pack.hash);
                }
            }.runTaskLater(MultiVersionResourcePacks.getInstance(), 1L);
        }
    }

    public Pack getPackByProtocol(int protocol) {
        Pattern pattern = Pattern.compile("[0-9]+");

        final Pack[] rpack = {null};
        AtomicBoolean equals = new AtomicBoolean(false);

        MultiVersionResourcePacks.sortedKeyList.forEach((prt) -> {
            Matcher m = pattern.matcher(prt);
            m.find();
            int protocolNumber = Integer.parseInt(m.group());
            String operator = prt.replace(String.valueOf(protocolNumber), "");
            if (equals.get()) return;

            Pack pack = MultiVersionResourcePacks.config.resourcePackMap.getOrDefault(operator+protocolNumber, MultiVersionResourcePacks.config.resourcePackMap.get(operator+ProtocolVersion.getProtocol(protocolNumber).getName()));

            switch (operator) {
                case "==":
                    rpack[0] = protocolNumber == protocol ? pack : rpack[0];
                    equals.set(true);
                    break;
                case ">=":
                    rpack[0] = protocolNumber <= protocol ? pack : rpack[0];
                    System.out.println(protocolNumber + " " + protocol);
                    break;
                case "<=":
                    rpack[0] = protocolNumber >= protocol ? pack : rpack[0];
                    break;
                case ">":
                    rpack[0] = protocolNumber < protocol ? pack : rpack[0];
                    break;
                case "<":
                    rpack[0] = protocolNumber > protocol ? pack : rpack[0];
                    break;
            }
        });

        return rpack[0];
    }
}
