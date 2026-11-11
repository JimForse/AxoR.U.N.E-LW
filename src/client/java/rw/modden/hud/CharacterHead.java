package rw.modden.hud;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import rw.modden.Axorunelostworlds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static rw.modden.AxorunelostworldsClient.LOGGER;

public final class CharacterHead {
    public interface Renderer {
        boolean draw(DrawContext context, String character, int x, int y, int size);
    }

    private static final List<Renderer> RENDERERS = new ArrayList<>();
    private static final Map<String, Optional<Identifier>> ICONS = new HashMap<>();

    static { RENDERERS.add(CharacterHead::drawIcon); }

    private CharacterHead() {}

    public static void register(Renderer renderer) { RENDERERS.add(0, renderer); }

    public static void registerReload() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() { return Identifier.of(Axorunelostworlds.MOD_ID, "hud_textures"); }

            @Override
            public void reload(ResourceManager manager) {
                ICONS.clear();
                TextureInfo.clear();
                LOGGER.info("[ARLW-HEAD] Resources reloaded, HUD texture caches cleared");
            }
        });
    }

    public static void draw(DrawContext context, String character, int x, int y, int size) {
        for(Renderer renderer: RENDERERS)
            if (renderer.draw(context, character, x, y, size)) return;
    }

    private static boolean drawIcon(DrawContext context, String character, int x, int y, int size) {
        Optional<Identifier> icon = ICONS.computeIfAbsent(character, CharacterHead::findIcon);
        if (icon.isEmpty()) return false;
        TextureInfo.Info info = TextureInfo.get(icon.get());
        context.drawTexture(icon.get(), x, y, size, size, 0.0F, 0.0F, info.width(), info.height(), info.width(), info.height());
        return true;
    }

    private static Optional<Identifier> findIcon(String character) {
        Identifier id = Identifier.of(Axorunelostworlds.MOD_ID, "textures/gui/hud/heads/" + character.toLowerCase(Locale.ROOT) + ".png");
        boolean exists = MinecraftClient.getInstance().getResourceManager().getResource(id).isPresent();
        if (exists) LOGGER.info("[ARLW-HEAD] Head icon for {}: {}", character, id);
        else LOGGER.warn("[ARLW-HEAD] No head icon for {}, expected {}", character, id);
        return exists?Optional.of(id):Optional.empty();
    }
}
