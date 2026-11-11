package rw.modden.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import rw.modden.AxorunelostworldsClient;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntPredicate;

// Читает размеры текстуры из файла и находит, где в ней реально нарисовано что-то (не прозрачное).
// Нужно, чтобы не прописывать размеры холста руками и резать заполнение по видимой части полоски.
public final class TextureInfo {
    private TextureInfo() {}

    // width/height - размер всего холста в пикселях файла;
    // minX/maxX и minY/maxY - границы области, где есть непрозрачные пиксели
    public record Info(int width, int height, int minX, int maxX, int minY, int maxY) {
        public int visibleWidth() {
            return maxX - minX + 1;
        }
    }

    private static final Map<Identifier, Info> CACHE = new HashMap<>(), DARK = new HashMap<>();
    public static Info get(Identifier id) {
        return CACHE.computeIfAbsent(id, key -> load(key, color -> ((color >>> 24) & 0xFF) > 0));
    }

    public static Info dark(Identifier id) {
        return DARK.computeIfAbsent(id, key -> {
            Info info = load(key, color -> ((color >>> 24) & 0xFF) > 200 && (color & 0xFF) < 20 && ((color >>> 8) & 0xFF) < 20 && ((color >>> 16) & 0xFF) < 20);
            AxorunelostworldsClient.LOGGER.info("[ARLW-HEAD] Dark area of {}: x {}..{}, y {}..{}", key, info.minX(), info.maxX(), info.minY(), info.maxY());
            return info;
        });
    }

    public static void clear() {
        CACHE.clear();
        DARK.clear();
    }

    private static Info load(Identifier id, IntPredicate counts) {
        Optional<Resource> resource = MinecraftClient.getInstance().getResourceManager().getResource(id);
        if (resource.isEmpty()) {
            AxorunelostworldsClient.LOGGER.error("[ARLW] HUD texture not found: {}", id);
            return new Info(64, 64, 0, 63, 0, 63);
        }

        try (InputStream in = resource.get().getInputStream(); NativeImage image = NativeImage.read(in)) {
            int w = image.getWidth();
            int h = image.getHeight();
            int minX = w, maxX = -1;
            int minY = h, maxY = -1;

            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    if (counts.test(image.getColor(x, y))) {
                        if (x < minX) minX = x;
                        if (x > maxX) maxX = x;
                        if (y < minY) minY = y;
                        if (y > maxY) maxY = y;
                    }
                }
            }
            if (maxX < 0) {
                minX = 0;
                maxX = w - 1;
                minY = 0;
                maxY = h - 1;
            }
            return new Info(w, h, minX, maxX, minY, maxY);
        } catch (Exception e) {
            AxorunelostworldsClient.LOGGER.error("[ARLW] Can't read HUD texture {}: {}", id, e.toString());
            return new Info(64, 64, 0, 63, 0, 63);
        }
    }
}