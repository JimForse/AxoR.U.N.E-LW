package rw.modden.mixin;

import me.roawwx.mobilityimprovements.client.MovementClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rw.modden.combat.CombatConstants;
import rw.modden.network.ClientNetwork;

@Mixin(MovementClient.class)
public class MovementClientMixin {
    @Inject(at = @At("HEAD"), method = "handleDash", cancellable = true)
    private static void handleDash(MinecraftClient client, ClientPlayerEntity p, CallbackInfo info) {
        if (!ClientNetwork.getBattle() || ClientNetwork.getStamina() < CombatConstants.DASH_STAMINA_COST)
            info.cancel();
    }

    @Inject(at = @At("HEAD"), method = "handleDoubleJump", cancellable = true)
    private static void handleDoubleJump(MinecraftClient client, ClientPlayerEntity p, CallbackInfo info) {
        if (!ClientNetwork.getBattle() || ClientNetwork.getStamina() < CombatConstants.DOUBLE_JUMP_STAMINA_COST)
            info.cancel();
    }
}
