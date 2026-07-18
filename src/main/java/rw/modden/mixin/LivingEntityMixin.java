package rw.modden.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rw.modden.characters.CharacterName;
import rw.modden.combat.Battle;
import rw.modden.components.ModComponents;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Unique
    private float newStamina, currentStamina, newHeal, currentHeal, healReserve, healRegen, stamina, staminaRegen;
    @Unique
    private ServerPlayerEntity player;
    @Unique
    private boolean battle;
    @Unique
    private int timer1 = 0;
    @Unique
    private boolean timerA, startedRun, doesDie;
    @Unique
    private CharacterName characterName; // TODO: исправить, когда сделаю переключение персонажей, чтобы был именно текущий персонаж боя

    private void battleA() {
        if (!((Object) this instanceof ServerPlayerEntity)) return;
        player = (ServerPlayerEntity) (Object) this;

        Battle battleClass = new Battle();
        battleClass.combatStateToBattle(player);
        battle = battleClass.getBattle();
        battleClass.getCharacterName(); // TODO: исправить, когда сделаю переключение персонажей, чтобы был именно текущий персонаж боя
    }

    @Inject(at = @At("HEAD"), method = "setSprinting", cancellable = true)
    private void sprint(boolean sprinting, CallbackInfo info) {
        battleA();
        if (battle) {
            stamina = ModComponents.CHARACTERS.get(player).getCharacter(characterName).getStamina(); // TODO: исправить, когда сделаю переключение персонажей, чтобы был именно текущий персонаж боя
            staminaRegen = ModComponents.CHARACTERS.get(player).getCharacter(characterName).getStaminaRegen(); // TODO: исправить, когда сделаю переключение персонажей, чтобы был именно текущий персонаж боя
            if (sprinting && newStamina <= 0.0)
                info.cancel();
            else if (sprinting) {
                newStamina = currentStamina - 0.5F;
                currentStamina = newStamina;
                startedRun = true;
            }
            else {
                startedRun = false;
                if (timerA)
                    newStamina = currentStamina + (stamina * staminaRegen);
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void timer(CallbackInfo info) {
        timer1 += 1;
        if (timer1>=80)
            timerA = true;
        if (startedRun) {
            timerA = false;
            timer1 = 0;
        }
    }

    @Inject(at = @At("HEAD"), method = "heal", cancellable = true)
    private void healRegeneration(float amount, CallbackInfo info) {
        battleA();
        if (battle) {
            doesDie = false;
            healReserve = ModComponents.CHARACTERS.get(player).getCharacter(characterName).getHealReserve(); // TODO: исправить, когда сделаю переключение персонажей, чтобы был именно текущий персонаж боя
            healRegen = ModComponents.CHARACTERS.get(player).getCharacter(characterName).getHealRegen(); // TODO: исправить, когда сделаю переключение персонажей, чтобы был именно текущий персонаж боя}

            if (currentHeal < healReserve) {
                if (currentHeal <= 0) {
                    doesDie = true;
                    info.cancel();
                } else {
                    newHeal = currentHeal + (healReserve * healRegen); // TODO: исправить, когда будет выполнено задание #Tech.14
                    currentHeal = newHeal;
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "damage")
    private void gettedDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        battleA();
        if (battle) {
            if (healReserve != 0.0F)
                currentHeal = healReserve-amount;
        }
    }
}
