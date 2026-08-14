package rw.modden.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rw.modden.combat.Battle;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(at = @At("HEAD"), method = "addExhaustion", cancellable = true)
    private void hunger(float exhaustion, CallbackInfo info) {
        Battle battleClass = null;
        boolean battle = false;
        if ((Object) this instanceof ServerPlayerEntity)
             battleClass = new Battle((ServerPlayerEntity)(Object) this);
        if (battleClass!=null) {
            battleClass.combatStateToBattle();
            battle = battleClass.getBattle();
        }
        if (battle)
            info.cancel();
    }
}
