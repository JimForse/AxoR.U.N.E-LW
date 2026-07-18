package rw.modden.combat.path;

import java.util.EnumMap;
import java.util.Map;

public class PathFactory {
    private static final Map<PathesName, Path> PATHS = new EnumMap<>(PathesName.class);

    static {
        PATHS.put(PathesName.CREATE, new CreatePath());
        PATHS.put(PathesName.ERUDITION, new EruditionPath());
        PATHS.put(PathesName.CONTROL, new ControlPath());
        PATHS.put(PathesName.HUNTING, new HuntingPath());
        PATHS.put(PathesName.HARMONY, new HarmonyPath());
        PATHS.put(PathesName.CHAOS, new ChaosPath());
        PATHS.put(PathesName.NIHILITY, new NihilityPath());
        PATHS.put(PathesName.MEMORY, new MemoryPath());
        PATHS.put(PathesName.ABUNDANCE, new AbundancePath());
        PATHS.put(PathesName.PRESERVATION, new PreservationPath());
    }

    private PathFactory() {}

    public static Path get(PathesName name) {
        Path path = PATHS.get(name);
        if (path == null)
            throw new IllegalStateException("No Path registered for " + name);
        return path;
    }
}
