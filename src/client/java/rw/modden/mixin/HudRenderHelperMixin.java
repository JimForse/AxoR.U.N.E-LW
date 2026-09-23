package rw.modden.mixin;

import net.combatroll.client.gui.HudRenderHelper;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rw.modden.network.ClientNetwork;

@Mixin(HudRenderHelper.class)
public class HudRenderHelperMixin {
    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    private static void render(DrawContext context, float tickDelta, CallbackInfo info) {
        if (!(ClientNetwork.getBattle())) {
            info.cancel();
        }
    }
}
