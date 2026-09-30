package rw.modden.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import rw.modden.Axorunelostworlds;
import rw.modden.network.ClientNetwork;

public class StaminaHealthHud {
    private static final Identifier HEART_FULL = Identifier.of(Axorunelostworlds.MOD_ID, "textures/gui/hud/heart_full.png");
    private static final Identifier HEART_NULL = Identifier.of(Axorunelostworlds.MOD_ID, "textures/gui/hud/heart_null.png");
    private static final Identifier ENDURANCE_FULL = Identifier.of(Axorunelostworlds.MOD_ID, "textures/gui/hud/endurance_full.png");
    private static final Identifier ENDURANCE_NULL = Identifier.of(Axorunelostworlds.MOD_ID, "textures/gui/hud/endurance_null.png");

    private static final int HEALTH_BAR_WIDTH = 128;
    private static final int HEALTH_BAR_HEIGHT = 32;

    private static final int STAMINA_BAR_WIDTH = 94;
    private static final int STAMINA_BAR_HEIGHT = 32;

    // Аватарка персонажа 64x64 в левом верхнем углу, бары начинаются правее неё.

    private static final int BAR_X = 97;
    private static final int HEALTH_Y = 0;
    private static final int STAMINA_Y = HEALTH_Y;

    public static void register() {
        HudRenderCallback.EVENT.register(StaminaHealthHud::render);
    }

    private static void render(DrawContext context, float tickDelta) {
        if (!ClientNetwork.getBattle()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        drawBar(context, BAR_X, HEALTH_Y, HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT,
                client.player.getHealth(), client.player.getMaxHealth(), HEART_FULL, HEART_NULL);
        drawBar(context, BAR_X, STAMINA_Y, STAMINA_BAR_WIDTH, STAMINA_BAR_HEIGHT,
                ClientNetwork.getStamina(), ClientNetwork.getMaxStamina(), ENDURANCE_FULL, ENDURANCE_NULL);
    }

    private static void drawBar(DrawContext context, int x, int y, int barWidth, int barHeight, float current, float max, Identifier full, Identifier empty) {
        context.drawTexture(empty, x, y, 0, 0, barWidth, barHeight, barWidth, barHeight);

        if (max <= 0f) return;
        float percent = Math.max(0f, Math.min(1f, current / max));
        int filledWidth = Math.round(barWidth * percent);
        if (filledWidth <= 0) return;

        context.drawTexture(full, x, y, 0, 0, filledWidth, barHeight, barWidth, barHeight);
    }
}