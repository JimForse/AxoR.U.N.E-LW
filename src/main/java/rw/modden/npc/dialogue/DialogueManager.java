package rw.modden.npc.dialogue;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rw.modden.npc.NpcEntity;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static rw.modden.Axorunelostworlds.LOGGER;

public class DialogueManager {
    private static final Gson GSON = new Gson();
    private static final Path DIR = FabricLoader.getInstance().getConfigDir().resolve("arlw/dialogues");
    private static final Map<String, Dialogue> dialogues = new HashMap<>();
    private static final Map<UUID, Session> sessions = new HashMap<>();
    private static int nextStep;

    private record Session(String dialogueId, String lineId, int step, String speaker) {}

    public static void initialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> reload());
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> sessions.remove(handler.player.getUuid()));
    }

    public static Set<String> getIds() { return dialogues.keySet(); }
    public static boolean exists(String id) { return dialogues.containsKey(id); }

    public static List<String> reload() {
        dialogues.clear();
        sessions.clear();
        List<String> errors = new ArrayList<>();
        try {
            Files.createDirectories(DIR);
            try (Stream<Path> files = Files.list(DIR)) {
                for(Path file: files.filter(f -> f.toString().endsWith(".json")).toList()) load(file, errors);
            }
        } catch (IOException e) {
            errors.add("Cannot read " + DIR + ": " + e.getMessage());
        }
        for(String error: errors) LOGGER.error("Dialogues: {}", error);
        return errors;
    }

    private static void load(Path file, List<String> errors) {
        String id = file.getFileName().toString().replace(".json", "");
        try (Reader reader = Files.newBufferedReader(file)) {
            Dialogue dialogue = GSON.fromJson(reader, Dialogue.class);
            String error = validate(dialogue);
            if (error != null) errors.add(id + ": " + error);
            else dialogues.put(id, dialogue);
        } catch (IOException | JsonParseException e) {
            errors.add(id + ": " + e.getMessage());
        }
    }

    private static String validate(Dialogue dialogue) {
        if (dialogue == null || dialogue.lines() == null) return "no lines";
        if (!dialogue.lines().containsKey(dialogue.start())) return "start line '" + dialogue.start() + "' not found";
        for(Map.Entry<String, Dialogue.Line> entry: dialogue.lines().entrySet()) {
            if (entry.getValue().text() == null) return "line '" + entry.getKey() + "' has no text";
            if (entry.getValue().answers() == null) continue;
            for(Dialogue.Answer answer: entry.getValue().answers())
                if (answer.next() != null && !dialogue.lines().containsKey(answer.next()))
                    return "line '" + entry.getKey() + "': answer leads to missing line '" + answer.next() + "'";
        }
        return null;
    }

    public static void start(ServerPlayerEntity player, String dialogueId, String speaker) {
        Dialogue dialogue = dialogues.get(dialogueId);
        if (dialogue == null) {
            LOGGER.warn("Dialogue '{}' not loaded", dialogueId);
            return;
        }
        show(player, dialogueId, dialogue.start(), speaker);
    }

    public static void answer(ServerPlayerEntity player, int step, int number) {
        Session session = sessions.get(player.getUuid());
        if (session == null || session.step() != step) return;
        Dialogue dialogue = dialogues.get(session.dialogueId());
        Dialogue.Line line = dialogue == null?null:dialogue.lines().get(session.lineId());
        if (line == null || line.answers() == null || number<1 || number>line.answers().size()) return;
        Dialogue.Answer answer = line.answers().get(number - 1);
        sessions.remove(player.getUuid());
        player.sendMessage(Text.literal("> " + answer.text()).formatted(Formatting.GRAY));
        NpcEntity.runCommands(player, answer.commands());
        if (answer.next() != null) show(player, session.dialogueId(), answer.next(), session.speaker());
    }

    private static void show(ServerPlayerEntity player, String dialogueId, String lineId, String speaker) {
        Dialogue.Line line = dialogues.get(dialogueId).lines().get(lineId);
        String name = line.speaker() != null?line.speaker():speaker;
        player.sendMessage(Text.literal("[" + name + "] ").formatted(Formatting.GOLD).append(Text.literal(line.text()).formatted(Formatting.WHITE)));
        NpcEntity.runCommands(player, line.commands());
        if (line.answers() == null || line.answers().isEmpty()) {
            sessions.remove(player.getUuid());
            return;
        }
        int step = ++nextStep;
        sessions.put(player.getUuid(), new Session(dialogueId, lineId, step, speaker));
        for(int i = 0; i<line.answers().size(); i++) {
            int number = i + 1;
            String command = "/dialogue " + step + " " + number;
            player.sendMessage(Text.literal("  [" + number + "] " + line.answers().get(i).text()).styled(style -> style
                    .withColor(Formatting.AQUA)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to answer")))));
        }
    }
}
