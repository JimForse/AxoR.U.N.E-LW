package rw.modden.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rw.modden.network.ClientNetwork;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void axorune$stopSprintWhenNoStamina(CallbackInfo ci) {
        ClientPlayerEntity self = (ClientPlayerEntity) (Object) this;
        if (ClientNetwork.getBattle() && ClientNetwork.getStamina() <= 0.0F && self.isSprinting()) {
            self.setSprinting(false);
        }
    }
}
