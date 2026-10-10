
package rw.modden.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import rw.modden.Axorunelostworlds;
import rw.modden.network.ClientNetwork;
import rw.modden.network.ClientNetwork.GroupMember;

import java.util.List;

public class CharacterGroupHud {
    private static final String DIR = "textures/gui/hud/player/";
    private static final Identifier HP_FULL = Identifier.of(Axorunelostworlds.MOD_ID, DIR + "apsorption_full.png");
    private static final Identifier HP_NULL = Identifier.of(Axorunelostworlds.MOD_ID, DIR + "apsorption_null.png");
    private static final Identifier ULT_FULL = Identifier.of(Axorunelostworlds.MOD_ID, DIR + "ultimate_full.png");
    private static final Identifier ULT_NULL = Identifier.of(Axorunelostworlds.MOD_ID, DIR + "ultimate_null.png");
    private static final Identifier STAMINA_FULL = Identifier.of(Axorunelostworlds.MOD_ID, DIR + "stamina_full.png");
    private static final Identifier STAMINA_NULL = Identifier.of(Axorunelostworlds.MOD_ID, DIR + "stamina_null.png");

    private record Style(float scale, float brightness) {}

    private static final Style BIG   = new Style(1.0F, 1.0F);
    private static final Style SMALL = new Style(0.65F, 0.5F);
    private static final float STAMINA_SCALE = 0.25F;

    private static final int GAP = 8;
    private static final int MARGIN_BOTTOM = 8;
    private static final int STAMINA_GAP = 8;
    private static final int STAMINA_OFFSET_Y = 0;
    private static final float STAMINA_TEXT_SCALE = 1.0F;

    public static void register() {
        HudRenderCallback.EVENT.register(CharacterGroupHud::render);
    }

    private static void render(DrawContext context, float tickDelta) {
        if (!ClientNetwork.getBattle()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        List<GroupMember> group = ClientNetwork.getGroup();
        int current = ClientNetwork.getGroupCurrent();
        int size = group.size();
        if (size == 0 || current >= size) return;

        int screenW = context.getScaledWindowWidth();
        int screenH = context.getScaledWindowHeight();

        int bigW = width(BIG);
        int smallW = width(SMALL);
        int centerX = screenW / 2;

        int[] slotX = {
                centerX - bigW / 2 - GAP - smallW,
                centerX - bigW / 2,
                centerX + bigW / 2 + GAP
        };

        int[] pairX = {
                centerX - GAP / 2 - bigW,
                centerX + GAP / 2
        };

        int leftmostX = size >= 3 ? slotX[0] : size == 2 ? pairX[0] : slotX[1];

        for (int i = 0; i < size; i++) {
            Style style;
            int x;
            if (size >= 3) {
                int slot = slotFor(i, current);
                style = slot == 1 ? BIG : SMALL;
                x = slotX[slot];
            } else if (size == 2) {
                style = i == current ? BIG : SMALL;
                x = pairX[i] + (bigW - width(style)) / 2;
            } else {
                style = BIG;
                x = slotX[1];
            }
            int y = screenH - MARGIN_BOTTOM - height(style);

            float hp, maxHp;
            if (i == current) {
                hp = client.player.getHealth();
                maxHp = client.player.getMaxHealth();
            } else {
                hp = group.get(i).hp();
                maxHp = group.get(i).maxHp();
            }

            drawSlot(context, x, y, style, hp, maxHp);
        }
        drawStamina(context, client, leftmostX, screenH);
    }

    private static int slotFor(int i, int current) {
        return Math.floorMod(1 + current - i, 3);
    }

    private static int width(Style s) {
        return Math.round(TextureInfo.get(ULT_NULL).width() * s.scale());
    }

    private static int height(Style s) {
        return Math.round(TextureInfo.get(ULT_NULL).height() * s.scale());
    }

    private static void drawSlot(DrawContext context, int x, int y, Style s, float hp, float maxHp) {
        RenderSystem.enableBlend();
        float b = s.brightness();
        context.setShaderColor(b, b, b, 1.0F);

        // Кольцо ульты (пока только пустое, заполнение добавим, когда будет логика ульты)
        drawRegion(context, ULT_NULL, x, y, TextureInfo.get(ULT_NULL).width(), s.scale());

        drawRegion(context, HP_NULL, x, y, TextureInfo.get(HP_NULL).width(), s.scale());
        if (maxHp > 0.0F) {
            float percent = Math.max(0.0F, Math.min(1.0F, hp / maxHp));

            TextureInfo.Info bar = TextureInfo.get(HP_NULL);
            int visibleFilled = Math.round(bar.visibleWidth() * percent);
            if (visibleFilled > 0) {
                int regionW = bar.minX() + visibleFilled;
                drawRegion(context, HP_FULL, x, y, regionW, s.scale());
            }
        }

        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void drawRegion(DrawContext context, Identifier texture, int x, int y, int srcW, float scale) {
        TextureInfo.Info info = TextureInfo.get(texture);
        int drawW = Math.round(srcW * scale);
        int drawH = Math.round(info.height() * scale);
        // drawTexture(текстура, x, y, ширина и высота на экране, u, v, ширина и высота
        //             вырезаемой области в файле, полный размер файла)
        context.drawTexture(texture, x, y, drawW, drawH, 0.0F, 0.0F, srcW, info.height(), info.width(), info.height());
    }

    private static void drawStamina(DrawContext context, MinecraftClient client, int leftmostX, int screenH) {
        TextureInfo.Info bar = TextureInfo.get(STAMINA_NULL);
        int drawW = Math.round(bar.width()*STAMINA_SCALE);
        int drawH = Math.round(bar.height()*STAMINA_SCALE);

        int x = leftmostX - STAMINA_GAP - drawW;
        int y = screenH - MARGIN_BOTTOM - drawH + STAMINA_OFFSET_Y;

        float current = ClientNetwork.getStamina();
        float max = ClientNetwork.getMaxStamina();
        float percent = max > 0.0F ? Math.max(0.0F, Math.min(1.0F, current / max)) : 0.0F;
        RenderSystem.enableBlend();

        drawRegion(context, STAMINA_NULL, x, y, bar.width(), STAMINA_SCALE);
        int visibleFilled = Math.round(bar.visibleWidth() * percent);
        if (visibleFilled > 0) {
            drawRegion(context, STAMINA_FULL, x, y, bar.minX() + visibleFilled, STAMINA_SCALE);
        }

        String text = Math.round(percent * 100.0F) + "%";
        float centerX = x + (bar.minX() + bar.maxX() + 1) / 2.0F * STAMINA_SCALE;
        float centerY = y + (bar.minY() + bar.maxY() + 1) / 2.0F * STAMINA_SCALE;

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(centerX, centerY, 0.0F);
        matrices.scale(STAMINA_TEXT_SCALE, STAMINA_TEXT_SCALE, 1.0F);
        int textW = client.textRenderer.getWidth(text);
        context.drawTextWithShadow(client.textRenderer, text, -textW / 2, -client.textRenderer.fontHeight / 2, 0xFFFFFF);
        matrices.pop();
    }
}