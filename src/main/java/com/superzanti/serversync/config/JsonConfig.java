package com.superzanti.serversync.config;

import com.eclipsesource.json.*;
import com.superzanti.serversync.files.DirectoryEntry;
import com.superzanti.serversync.files.EDirectoryMode;
import com.superzanti.serversync.files.FileRedirect;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.stream.Collectors;

public class JsonConfig {
    private static final String CAT_GENERAL = "general";
    private static final String CAT_CONNECTION = "connection";
    private static final String CAT_RULES = "rules";
    private static final String CAT_MISC = "misc";

    private static final String PROP_PUSH_CLIENT_MODS = "push_client_mods";
    private static final String PROP_REFUSE_CLIENT_MODS = "refuse_client_mods";
    private static final String PROP_SYNC_MODE = "sync_mode";
    private static final String PROP_PORT = "port";
    private static final String PROP_ADDRESS = "address";
    private static final String PROP_DIRECTORY_INCLUDE_LIST = "directory_include_list";
    private static final String PROP_FILE_IGNORE_LIST = "file_ignore_list";
    private static final String PROP_FILE_REDIRECT_LIST = "redirect_files";
    private static final String PROP_LOCALE = "locale";

    public static void forServer(Path json) throws IOException {
        try (Reader reader = Files.newBufferedReader(json)) {
            SyncConfig config = SyncConfig.getConfig();
            JsonObject root = Json.parse(reader).asObject();
            if (root.isNull()) {
                throw new IOException("Invalid configuration file");
            }

            JsonObject general = getCategory(root, CAT_GENERAL);
            JsonObject connection = getCategory(root, CAT_CONNECTION);
            JsonObject rules = getCategory(root, CAT_RULES);
            JsonObject misc = getCategory(root, CAT_MISC);

            config.PUSH_CLIENT_MODS = getBoolean(general, PROP_PUSH_CLIENT_MODS);
            config.SYNC_MODE = getInt(general, PROP_SYNC_MODE);
            config.SERVER_PORT = getInt(connection, PROP_PORT);

            JsonArray directoryIncludeList = getArray(rules, PROP_DIRECTORY_INCLUDE_LIST);
            config.DIRECTORY_INCLUDE_LIST = directoryIncludeList
                .values()
                .stream()
                .map(v -> {
                    if (v.isObject()) {
                        return new DirectoryEntry(
                            v.asObject().get("path").asString(),
                            EDirectoryMode.valueOf(v.asObject().get("mode").asString().toLowerCase())
                        );
                    }
                    return new DirectoryEntry(v.asString(), EDirectoryMode.mirror);
                })
                .collect(Collectors.toList());

            JsonArray fileIgnoreList = getArray(rules, PROP_FILE_IGNORE_LIST);
            config.FILE_IGNORE_LIST = fileIgnoreList
                .values()
                .stream()
                .map(v -> {
                    if (v.isObject()) {
                        // Ditching description as we don't use it for anything
                        return v.asObject().get("pattern").asString();
                    }
                    return v.asString();
                })
                .collect(Collectors.toList());

            JsonArray fileRedirectList = getArray(rules, PROP_FILE_REDIRECT_LIST);
            config.REDIRECT_FILES_LIST = fileRedirectList
                .values()
                .stream()
                .map(v -> FileRedirect.from(v.asObject()))
                .collect(Collectors.toList());

            String[] localeParts = getString(misc, PROP_LOCALE).split("_");
            config.LOCALE = new Locale(localeParts[0], localeParts[1]);

        }
    }

    public static void forClient(Path json) throws IOException {
        try (Reader reader = Files.newBufferedReader(json)) {
            SyncConfig config = SyncConfig.getConfig();
            JsonObject root = Json.parse(reader).asObject();
            if (root.isNull()) {
                throw new IOException("Invalid configuration file");
            }

            JsonObject general = getCategory(root, CAT_GENERAL);
            JsonObject connection = getCategory(root, CAT_CONNECTION);
            JsonObject rules = getCategory(root, CAT_RULES);
            JsonObject misc = getCategory(root, CAT_MISC);

            config.REFUSE_CLIENT_MODS = getBoolean(general, PROP_REFUSE_CLIENT_MODS);
            config.SERVER_IP = getString(connection, PROP_ADDRESS);
            config.SERVER_PORT = getInt(connection, PROP_PORT);

            JsonArray fileIgnoreList = getArray(rules, PROP_FILE_IGNORE_LIST);
            config.FILE_IGNORE_LIST = fileIgnoreList
                .values()
                .stream()
                .map(v -> {
                    if (v.isObject()) {
                        // Ditching description as we don't use it for anything
                        return v.asObject().get("pattern").asString();
                    }
                    return v.asString();
                })
                .collect(Collectors.toList());

            String[] localeParts = getString(misc, PROP_LOCALE).split("_");
            config.LOCALE = new Locale(localeParts[0], localeParts[1]);

        }
    }

    public static void saveServer(Path file) throws IOException {
        SyncConfig config = SyncConfig.getConfig();
        JsonObject root = new JsonObject();

        JsonObject general = new JsonObject();
        general.add(PROP_PUSH_CLIENT_MODS, config.PUSH_CLIENT_MODS);
        general.add(PROP_SYNC_MODE, config.SYNC_MODE);
        root.add(CAT_GENERAL, general);

        JsonObject connection = new JsonObject();
        connection.add(PROP_PORT, config.SERVER_PORT);
        root.add(CAT_CONNECTION, connection);

        JsonObject rules = new JsonObject();
        JsonArray dirIncludeList = new JsonArray();
        config.DIRECTORY_INCLUDE_LIST.forEach(d -> {
            dirIncludeList.add(d.toJson());
        });
        rules.add(PROP_DIRECTORY_INCLUDE_LIST, dirIncludeList);
        JsonArray fileIgnoreList = new JsonArray();
        config.FILE_IGNORE_LIST.forEach(fileIgnoreList::add);
        rules.add(PROP_FILE_IGNORE_LIST, fileIgnoreList);
        JsonArray redirectFilesList = new JsonArray();
        config.REDIRECT_FILES_LIST.forEach(f -> redirectFilesList.add(f.toJson()));
        rules.add(PROP_FILE_REDIRECT_LIST, redirectFilesList);
        root.add(CAT_RULES, rules);

        JsonObject misc = new JsonObject();
        misc.add(PROP_LOCALE, config.LOCALE.toString());
        root.add(CAT_MISC, misc);

        writeTo(Files.newBufferedWriter(file), root);
    }

    public static void saveClient(Path file) throws IOException {
        SyncConfig config = SyncConfig.getConfig();
        JsonObject root = new JsonObject();

        JsonObject general = new JsonObject();
        general.add(PROP_REFUSE_CLIENT_MODS, config.REFUSE_CLIENT_MODS);
        general.add(PROP_SYNC_MODE, config.SYNC_MODE);
        root.add(CAT_GENERAL, general);

        JsonObject connection = new JsonObject();
        connection.add(PROP_ADDRESS, config.SERVER_IP);
        connection.add(PROP_PORT, config.SERVER_PORT);
        root.add(CAT_CONNECTION, connection);

        JsonObject rules = new JsonObject();
        JsonArray fileIgnoreList = new JsonArray();
        config.FILE_IGNORE_LIST.forEach(fileIgnoreList::add);
        rules.add(PROP_FILE_IGNORE_LIST, fileIgnoreList);
        root.add(CAT_RULES, rules);

        JsonObject misc = new JsonObject();
        misc.add(PROP_LOCALE, config.LOCALE.toString());
        root.add(CAT_MISC, misc);

        writeTo(Files.newBufferedWriter(file), root);
    }

    private static void writeTo(Writer out, JsonObject obj) throws IOException {
        //TODO minimal-json seems to have a bug in its writeTo method
        String str = obj.toString(WriterConfig.PRETTY_PRINT);
        out.write(str);
        out.close();
    }

    private static JsonObject getCategory(JsonObject root, String name) throws IOException {
        JsonObject jso = root.get(name).asObject();
        if (jso.isNull()) {
            throw new IOException(String.format("No %s category present in configuration file", name));
        }
        return jso;
    }

    private static String getString(JsonObject root, String name) throws IOException {
        JsonValue jsv = root.get(name);
        if (jsv.isNull()) {
            throw new IOException(String.format("No %s value present in configuration file", name));
        }
        if (!jsv.isString()) {
            throw new IOException(String.format("Invalid value for %s, expected string", name));
        }
        return jsv.asString();
    }

    private static boolean getBoolean(JsonObject root, String name) throws IOException {
        JsonValue jsv = root.get(name);
        if (jsv.isNull()) {
            throw new IOException(String.format("No %s value present in configuration file", name));
        }
        if (!jsv.isBoolean()) {
            throw new IOException(String.format("Invalid value for %s, expected boolean", name));
        }
        return jsv.asBoolean();
    }

    private static int getInt(JsonObject root, String name) throws IOException {
        JsonValue jsv = root.get(name);
        if (jsv.isNull()) {
            throw new IOException(String.format("No %s value present in configuration file", name));
        }
        if (!jsv.isNumber()) {
            throw new IOException(String.format("Invalid value for %s, expected integer", name));
        }
        return jsv.asInt();
    }

    private static JsonArray getArray(JsonObject root, String name) throws IOException {
        JsonValue jsv = root.get(name);
        if (jsv.isNull()) {
            throw new IOException(String.format("No %s value present in configuration file", name));
        }
        if (!jsv.isArray()) {
            throw new IOException(String.format("Invalid value for %s, expected array", name));
        }
        return jsv.asArray();
    }
}
