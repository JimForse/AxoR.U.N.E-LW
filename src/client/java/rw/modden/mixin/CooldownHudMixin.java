package rw.modden.mixin;

import me.roawwx.mobilityimprovements.client.CooldownHud;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rw.modden.network.ClientNetwork;

@Mixin(CooldownHud.class)
public class CooldownHudMixin {
//    @Inject(at = @At("HEAD"), method = "onHudRender", cancellable = true)
//    private void onHubRender(DrawContext ctx, float tickDelta, CallbackInfo info) {
//        if (!ClientNetwork.getBattle()) info.cancel();
//    }
}
