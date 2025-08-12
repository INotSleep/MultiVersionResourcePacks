package me.inotsleep.multiversionresourcepacks;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

public class HttpPackHost {
    public static void stop() {
        if (server == null) return;

        server.stop(0);
        server = null;
    }

    private static HttpServer server;

    public static void startServer(MultiVersionResourcePacks plugin) {
        String ipAddress = MultiVersionResourcePacks.config.fileHostBindIP;
        int port = MultiVersionResourcePacks.config.fileHostPort;

        InetSocketAddress address = new InetSocketAddress(ipAddress, port);
        try {
            server = HttpServer.create(address, 0);
            server.createContext("/", new ZipHandler(MultiVersionResourcePacks.config.packFolder));
            server.setExecutor(Executors.newFixedThreadPool(MultiVersionResourcePacks.config.threads));
            server.start();

            plugin.getLogger().info("HTTPServer started at " + ipAddress + ":" + port+ " and available at " + MultiVersionResourcePacks.config.fileHostPublic);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static class ZipHandler implements HttpHandler {
        String zipDirectoryPath;
        public ZipHandler(File packFolder) {
            this.zipDirectoryPath = packFolder.getAbsolutePath();
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String range = exchange.getRequestHeaders().getFirst("Range");
            if (range != null) {
                exchange.getResponseHeaders().set("Accept-Ranges", "none");
                exchange.sendResponseHeaders(416, -1);
                return;
            }

            String key = getQueryParam(exchange, "key");
            Pack pack = Listeners.keys.get(key);
            boolean hasKey = key != null && pack != null;

            if (!hasKey) {
                exchange.sendResponseHeaders(401, -1);
                return;
            }

            Path zipFilePath = Paths.get(zipDirectoryPath, pack.fileName);

            if (Files.exists(zipFilePath) && Files.isRegularFile(zipFilePath)) {
                exchange.getResponseHeaders().set("Content-Type", "application/zip");

                long size = Files.size(zipFilePath);

                exchange.sendResponseHeaders(200, size);
                long sent = 0;

                try (OutputStream os = exchange.getResponseBody()) {
                    sent = Files.copy(zipFilePath, os);
                } catch (IOException ignored) {

                }

                if (sent == size) {
                    Listeners.requestedKeys.add(key);
                }

            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        }
    }

    static String getQueryParam(HttpExchange exchange, String name) {
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null || query.isEmpty()) return null;

        for (String pair : query.split("&")) {
            int index = pair.indexOf('=');
            String queryName = index >= 0 ? pair.substring(0, index) : pair;
            String value = index >= 0 ? pair.substring(index + 1) : "";

            String decodedName = URLDecoder.decode(queryName, StandardCharsets.UTF_8);
            if (decodedName.equals(name)) {
                return URLDecoder.decode(value, StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}