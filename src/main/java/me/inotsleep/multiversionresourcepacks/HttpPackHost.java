package me.inotsleep.multiversionresourcepacks;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
public class HttpPackHost {
    public static void startServer(MultiVersionResourcePacks plugin) {
        String ipAddress = MultiVersionResourcePacks.config.fileHostBindIP;
        int port = MultiVersionResourcePacks.config.fileHostPort;

        InetSocketAddress address = new InetSocketAddress(ipAddress, port);
        try {
            HttpServer server = HttpServer.create(address, 0);
            server.createContext("/", new ZipHandler(MultiVersionResourcePacks.config.packFolder));
            server.setExecutor(Executors.newFixedThreadPool(MultiVersionResourcePacks.config.threads));
            server.start();

            plugin.getLogger().info("HTTPServer started at " + ipAddress + ":" + port+ " and available at "+ MultiVersionResourcePacks.config.fileHostPublic);
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
            String requestedFileName = exchange.getRequestURI().getPath().substring(1); // Remove the leading slash

            Path zipFilePath = Paths.get(zipDirectoryPath, requestedFileName);

            if (Files.exists(zipFilePath) && Files.isRegularFile(zipFilePath)) {
                // Set response headers
                exchange.getResponseHeaders().set("Content-Type", "application/zip");
                exchange.sendResponseHeaders(200, Files.size(zipFilePath));

                // Write the ZIP file to the response
                try (OutputStream os = exchange.getResponseBody()) {
                    Files.copy(zipFilePath, os);
                }
            } else {
                // File not found
                String response = "File not found: " + requestedFileName;
                exchange.sendResponseHeaders(404, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
    }
}