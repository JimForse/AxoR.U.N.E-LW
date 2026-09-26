// НОВОЕ РАСПОЛОЖЕНИЕ: src/main/java/rw/modden/mixin/MovementServerMixin.java
// (то есть переносим из src/client в src/main — важно!)
package rw.modden.mixin;

import me.roawwx.mobilityimprovements.MovementServer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rw.modden.access.StaminaAccess;
import rw.modden.combat.CombatConstants;
import rw.modden.combat.CombatState;
import rw.modden.components.ModComponents;

@Mixin(MovementServer.class)
public class MovementServerMixin {
    @Inject(method = "tryConsumeFood", at = @At("HEAD"), cancellable = true)
    private static void axorune$freeMovementCostInBattle(PlayerEntity p, int cost, CallbackInfoReturnable<Boolean> cir) {
        if (p instanceof ServerPlayerEntity player) {
            CombatState state = ModComponents.BATTLE_STATE.get(player).getState();
            if (state == CombatState.STANDART || state == CombatState.EVENT) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "serverDoDash", at = @At("HEAD"))
    private static void axorune$spendDashStamina(PlayerEntity p, CallbackInfo ci) {
        if (p instanceof ServerPlayerEntity player) {
            ((StaminaAccess) player).axorune$trySpendDashStamina(
                    CombatConstants.DASH_STAMINA_COST,
                    CombatConstants.DASH_STAMINA_COOLDOWN_TICKS
            );
        }
    }

    @Inject(method = "serverDoDoubleJump", at = @At("HEAD"))
    private static void axorune$spendJumpStamina(PlayerEntity p, CallbackInfo ci) {
        if (p instanceof ServerPlayerEntity player) {
            ((StaminaAccess) player).axorune$trySpendStamina(CombatConstants.DOUBLE_JUMP_STAMINA_COST);
        }
    }
}
