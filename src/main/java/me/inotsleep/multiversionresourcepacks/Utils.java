package me.inotsleep.multiversionresourcepacks;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Objects;

public class Utils {
    public static String calcSHA1(File file) throws IOException, NoSuchAlgorithmException {

        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        try (InputStream input = new FileInputStream(file)) {

            byte[] buffer = new byte[8192];
            int len = input.read(buffer);

            while (len != -1) {
                sha1.update(buffer, 0, len);
                len = input.read(buffer);
            }

            return byteArray2Hex(sha1.digest());
        }
    }

    public static String byteArray2Hex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    public static int getProtocolFromVersionString(String versionString) {
        final ProtocolVersion[] version = {null};
        ProtocolVersion.getProtocols().forEach(i -> version[0] = Objects.equals(i.getName(), versionString) ? i : version[0]);
        return version[0] == null ? -1 : version[0].getVersion();
    }
}
