package rw.modden.characters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import rw.modden.components.ModComponents;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import static rw.modden.Axorunelostworlds.LOGGER;

public final class CharacterTemplates {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path DIR = FabricLoader.getInstance().getConfigDir().resolve("arlw/characters");
    private static final Map<CharacterName, CharacterTemplate> templates = new TreeMap<>((a, b) -> a.name().compareTo(b.name()));
    private static MinecraftServer server;

    private CharacterTemplates() {}

    public static void initialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(started -> {
            server = started;
            reload();
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(stopped -> server = null);
    }

    public static CharacterTemplate get(CharacterName name) { return templates.get(name); }
    public static boolean exists(CharacterName name) { return templates.containsKey(name); }
    public static Set<CharacterName> names() { return templates.keySet(); }

    public static List<String> reload() {
        templates.clear();
        List<String> errors = new ArrayList<>();
        try {
            Files.createDirectories(DIR);
            try (Stream<Path> files = Files.list(DIR)) {
                for(Path file: files.filter(f -> f.toString().endsWith(".json")).toList()) load(file, errors);
            }
        } catch (IOException e) {
            errors.add("Cannot read " + DIR + ": " + e.getMessage());
        }
        for(String error: errors) LOGGER.error("[ARLW-CHAR] Templates: {}", error);
        LOGGER.info("[ARLW-CHAR] Loaded {} character template(s) from {}: {}", templates.size(), DIR, templates.keySet());
        refreshLocks();
        return errors;
    }

    private static void load(Path file, List<String> errors) {
        String id = file.getFileName().toString().replace(".json", "");
        if (!CharacterName.isValid(id)) {
            errors.add(id + ": name may contain only a-z, 0-9 and _");
            return;
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            CharacterTemplate template = GSON.fromJson(reader, CharacterTemplate.class);
            String error = validate(template);
            if (error != null) {
                errors.add(id + ": " + error);
                return;
            }
            if (template.path() == null) {
                template = template.withPath(CharacterTemplate.randomPath());
                LOGGER.info("[ARLW-CHAR] Template {} had no path, random {} chosen and saved", id, template.path());
                write(CharacterName.of(id), template);
            }
            templates.put(CharacterName.of(id), template);
        } catch (IOException | JsonParseException e) {
            errors.add(id + ": " + e.getMessage());
        }
    }

    private static String validate(CharacterTemplate template) {
        if (template == null) return "empty file";
        if (template.levelOrDefault()<1 || template.levelOrDefault()>AbilitiesImprovement.maxLevel(AbilitiesImprovement.MAX_ASCENSION)) return "level must be 1.." + AbilitiesImprovement.maxLevel(AbilitiesImprovement.MAX_ASCENSION);
        if (template.starsOrDefault()<0) return "stars must not be negative";
        return null;
    }

    public static boolean put(CharacterName name, CharacterTemplate template) {
        if (!write(name, template)) return false;
        boolean isNew = !templates.containsKey(name);
        templates.put(name, template);
        LOGGER.info("[ARLW-CHAR] Template {} {}: {}", name, isNew?"created":"saved", template);
        if (isNew) refreshLocks();
        return true;
    }

    private static boolean write(CharacterName name, CharacterTemplate template) {
        try {
            Files.createDirectories(DIR);
            try (Writer writer = Files.newBufferedWriter(DIR.resolve(name.fileName() + ".json"))) {
                GSON.toJson(template, writer);
            }
            return true;
        } catch (IOException e) {
            LOGGER.error("[ARLW-CHAR] Cannot save template {}: {}", name, e.getMessage());
            return false;
        }
    }

    public static boolean delete(CharacterName name) {
        if (templates.remove(name) == null) return false;
        try {
            Files.deleteIfExists(DIR.resolve(name.fileName() + ".json"));
        } catch (IOException e) {
            LOGGER.error("[ARLW-CHAR] Cannot delete template file {}: {}", name, e.getMessage());
        }
        LOGGER.info("[ARLW-CHAR] Template {} deleted", name);
        refreshLocks();
        return true;
    }

    private static void refreshLocks() {
        if (server == null) return;
        for(ServerPlayerEntity player: server.getPlayerManager().getPlayerList()) {
            ModComponents.CHARACTERS.get(player).refreshLocks();
            ModComponents.CHARACTERS.sync(player);
        }
    }
}
