package rw.modden.characters;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class CharacterName {
    private static final Map<String, CharacterName> NAMES = new LinkedHashMap<>();
    private static final Pattern VALID = Pattern.compile("[A-Z0-9_]+");
    private final String name;

    private CharacterName(String name) { this.name = name; }

    public static synchronized CharacterName of(String name) {
        return NAMES.computeIfAbsent(name.toUpperCase(Locale.ROOT), CharacterName::new);
    }

    public static boolean isValid(String name) { return name != null && VALID.matcher(name.toUpperCase(Locale.ROOT)).matches(); }
    public static Collection<CharacterName> values() { return Collections.unmodifiableCollection(NAMES.values()); }

    public String name() { return name; }
    public String fileName() { return name.toLowerCase(Locale.ROOT); }

    @Override
    public String toString() { return name; }
}
