package rw.modden;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static rw.modden.Axorunelostworlds.LOGGER;

public final class PlayerRegistry {
    private PlayerRegistry() {}

    private static final String RESOURCE = "/arlw_players.json";
    private static final Gson GSON = new Gson();

    private static Map<String, String> nickByUuid;

    public static synchronized String findNick(UUID uuid) {
        if (nickByUuid == null) load();
        return nickByUuid.get(uuid.toString().toLowerCase());
    }

    private static void load() {
        Map<String, String> result = new HashMap<>();
        try (InputStream in = PlayerRegistry.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                LOGGER.error("[ARLW] Resource {} not found - nobody will get a personal character", RESOURCE);
            } else {
                // TypeToken нужен, чтобы Gson знал, что значения - строки (а не UUID/Object): раньше из-за
                // этого был ClassCastException
                Map<String, String> raw = GSON.fromJson(
                        new InputStreamReader(in, StandardCharsets.UTF_8),
                        new TypeToken<Map<String, String>>() {}.getType());
                if (raw != null) {
                    for (Map.Entry<String, String> entry: raw.entrySet()) {
                        result.put(entry.getValue().toLowerCase(), entry.getKey());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("[ARLW] Can't read {}: {}", RESOURCE, e.toString());
        }

        nickByUuid = result;
        LOGGER.info("[ARLW] Loaded {} player(s) from {}", result.size(), RESOURCE);
    }
}