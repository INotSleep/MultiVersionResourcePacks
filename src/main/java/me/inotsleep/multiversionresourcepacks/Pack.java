package me.inotsleep.multiversionresourcepacks;

import me.inotsleep.utils.config.Path;
import me.inotsleep.utils.config.SerializableObject;

public class Pack extends SerializableObject {
    @Path("file-name")
    public String fileName;

    @Path("file-hash")
    public String hash;

    @Path("file-url")
    public String url;

    @Path("required")
    public boolean required;

    public Pack() {}

    public Pack(String fileName, String hash, String url, boolean required) {
        this.fileName = fileName;
        this.hash = hash;
        this.url = url;
        this.required = required;
    }
}
