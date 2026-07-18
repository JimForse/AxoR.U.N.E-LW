package rw.modden.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rw.modden.combat.Battle;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(at = @At("HEAD"), method = "addExhaustion", cancellable = true)
    private void hunger(float exhaustion, CallbackInfo info) {
        Battle battleClass = new Battle();
        battleClass.combatStateToBattle((ServerPlayerEntity)(Object) this);
        boolean battle = battleClass.getBattle();
        if (battle)
            info.cancel();
    }
}
