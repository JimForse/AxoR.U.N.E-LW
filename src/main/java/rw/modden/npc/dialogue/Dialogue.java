package rw.modden.npc.dialogue;

import java.util.List;
import java.util.Map;

public record Dialogue(String start, Map<String, Line> lines) {
    public record Line(String speaker, String text, List<String> commands, List<Answer> answers) {}
    public record Answer(String text, String next, List<String> commands) {}
}
