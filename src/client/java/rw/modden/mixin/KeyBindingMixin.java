package rw.modden.mixin;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rw.modden.BattleInputBlocker;

@Mixin(KeyBinding.class)
public class KeyBindingMixin {
    @Inject(method = "onKeyPressed", at = @At("HEAD"), cancellable = true)
    private static void axorune$routeKeyPress(InputUtil.Key key, CallbackInfo ci) {
        if (BattleInputBlocker.routePress(key)) ci.cancel();
    }

    @Inject(method = "setKeyPressed", at = @At("HEAD"), cancellable = true)
    private static void axorune$routeKeyState(InputUtil.Key key, boolean pressed, CallbackInfo ci) {
        if (BattleInputBlocker.routeState(key, pressed)) ci.cancel();
    }
}
