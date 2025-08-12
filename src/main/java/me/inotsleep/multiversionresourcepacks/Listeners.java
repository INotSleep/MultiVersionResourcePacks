package me.inotsleep.multiversionresourcepacks;

import com.viaversion.viaversion.api.ViaAPI;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Listeners implements Listener {
    public static Map<String, Pack> keys = new HashMap<>();
    public static List<String> acceptedKeys = new ArrayList<>();
    public static List<String> requestedKeys = new ArrayList<>();

    private static final Map<UUID, String> playerToKey = new HashMap<>();
    private static final Map<UUID, Pack>  playerToPack = new HashMap<>();

    private final ViaAPI<Player> viaAPI;
    public Listeners(ViaAPI<Player> viaAPI) {
        this.viaAPI = viaAPI;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        int protocol = viaAPI.getPlayerVersion(p);
        Pack pack = getPackByProtocol(protocol);

        if (pack != null && pack.url != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    String url = pack.url;

                    playerToPack.put(p.getUniqueId(), pack);

                    if (pack.fileName != null) {
                        String key = UUID.randomUUID().toString();

                        playerToKey.put(p.getUniqueId(), key);
                        keys.put(key, pack);
                        List.of(1);

                        url += (url.contains("?") ? "&" : "?") + "key=" + key;
                    }

                    p.setResourcePack(url, pack.hash);
                }
            }.runTaskLater(MultiVersionResourcePacks.getInstance(), 1L);
        }
    }

    @EventHandler
    public void onPlayerResourcePack(PlayerResourcePackStatusEvent event) {
        System.out.println(event.getStatus());

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Pack pack = playerToPack.get(uuid);
        String key = playerToKey.get(uuid);

        System.out.println(key);
        System.out.println(pack);

        PlayerResourcePackStatusEvent.Status status = event.getStatus();
        switch (status) {
            case DECLINED: {
                if (pack != null && pack.required) {
                    player.kick(Component.text(MultiVersionResourcePacks.config.declinedMessage));
                    cleanupPlayerState(uuid);
                }
                break;
            }

            case ACCEPTED: {
                if (key != null && !acceptedKeys.contains(key)) {
                    acceptedKeys.add(key);
                }
                break;
            }

            case FAILED_DOWNLOAD: {
                if (pack != null && pack.required) {
                    player.kick(Component.text(MultiVersionResourcePacks.config.failedMessage));
                    cleanupPlayerState(uuid);
                } else {
                    player.sendMessage(Component.text(MultiVersionResourcePacks.config.failedMessage));
                    cleanupPlayerState(uuid);
                }
                break;
            }

            case SUCCESSFULLY_LOADED: {
                if (pack != null && pack.fileName != null) {
                    if (key == null) {
                        player.kick(Component.text(MultiVersionResourcePacks.config.declinedMessage));
                        cleanupPlayerState(uuid);
                        break;
                    }
                    boolean requested = requestedKeys.contains(key);
                    boolean accepted  = acceptedKeys.contains(key);

                    if (!requested || !accepted) {
                        player.kick(Component.text(MultiVersionResourcePacks.config.declinedMessage));
                        cleanupPlayerState(uuid);
                        break;
                    }
                }

                cleanupPlayerState(uuid);
                break;
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cleanupPlayerState(event.getPlayer().getUniqueId());
    }

    private void cleanupPlayerState(UUID uuid) {
        String key = playerToKey.remove(uuid);

        if (key != null) {
            playerToPack.remove(uuid);
            acceptedKeys.remove(key);
            requestedKeys.remove(key);
            keys.remove(key);
        }
    }

    public Pack getPackByProtocol(int protocol) {
        Pattern pattern = Pattern.compile("[0-9]+");

        final Pack[] rpack = {null};
        AtomicBoolean equals = new AtomicBoolean(false);

        MultiVersionResourcePacks.sortedKeyList.forEach((prt) -> {
            Matcher m = pattern.matcher(prt);
            if (!m.find()) return;
            if (equals.get()) return;

            int protocolNumber = Integer.parseInt(m.group());
            String operator = prt.replace(String.valueOf(protocolNumber), "");


            Pack pack = MultiVersionResourcePacks.config.resourcePackMap.getOrDefault(
                    operator + protocolNumber,
                    MultiVersionResourcePacks.config.resourcePackMap.get(
                            operator + ProtocolVersion.getProtocol(protocolNumber).getName().replace(".", "_")
                    )
            );

            if (pack == null) return;

            switch (operator) {
                case "==":
                    if (protocol == protocolNumber) {
                        rpack[0] = pack;
                        equals.set(true);
                    }
                    break;
                case ">=":
                    if (protocol >= protocolNumber) rpack[0] = pack;
                    break;
                case "<=":
                    if (protocol <= protocolNumber) rpack[0] = pack;
                    break;
                case ">":
                    if (protocol > protocolNumber) rpack[0] = pack;
                    break;
                case "<":
                    if (protocol < protocolNumber) rpack[0] = pack;
                    break;
            }
        });

        return rpack[0];
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                checkForActions(event, player);
            }
        }.runTask(MultiVersionResourcePacks.getInstance());
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        checkForActions(event, player);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getHand() == EquipmentSlot.HAND) checkForActions(event, player);
    }

    public void checkForActions(Cancellable event, Player player) {
        if (!MultiVersionResourcePacks.config.kickUnfishedAction) return;

        String key = playerToKey.get(player.getUniqueId());

        if (key == null) {
            return;
        }
        boolean requested = requestedKeys.contains(key);
        boolean accepted  = acceptedKeys.contains(key);

        if (!requested || !accepted) {
            if (player.isOnline()) player.kick(Component.text(MultiVersionResourcePacks.config.declinedMessage));
            event.setCancelled(true);
            cleanupPlayerState(player.getUniqueId());
        }
    }
}
