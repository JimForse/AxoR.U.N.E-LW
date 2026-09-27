package rw.modden.mixin;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rw.modden.access.StaminaAccess;
import rw.modden.characters.Character;
import rw.modden.combat.Battle;
import rw.modden.components.CharactersComponent;
import rw.modden.components.ModComponents;
import rw.modden.network.ServerNetwork;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements StaminaAccess {
    @Unique
    private float newStamina, currentStamina, newHeal, currentHeal, healReserve, healRegen, stamina, staminaRegen;
    @Unique
    private ServerPlayerEntity player;
    @Unique
    private boolean battle;
    @Unique
    private boolean wasInBattle = false;
    @Unique
    private int timer1 = 0;
    @Unique
    private boolean timerA;
    @Unique
    private int dashStaminaCooldownTicks = 0;
    @Unique
    private boolean staminaInitialized = false;

    @Unique
    private void axorune$onStaminaSpent() {
        timer1 = 0;
        timerA = false;
    }

    @Inject(at = @At("HEAD"), method = "setSprinting", cancellable = true)
    private void sprint(boolean sprinting, CallbackInfo info) {
        battleA();
        if (battle && sprinting && currentStamina <= 0.0) {
            info.cancel();
        }
    }

    @Unique
    private void battleA() {
        if (!((Object) this instanceof ServerPlayerEntity)) return;
        player = (ServerPlayerEntity) (Object) this;

        Battle battleClass = new Battle(player);
        battleClass.combatStateToBattle();
        battle = battleClass.getBattle();

        if (battle) {
            if (!staminaInitialized) {
                CharactersComponent component = ModComponents.CHARACTERS.get(player);
                Character character = component.getCharacter(component.getCurrentCharacter());
                if (character != null) {
                    currentStamina = character.getStamina();
                    staminaInitialized = true;
                }
            }
        } else {
            staminaInitialized = false;
        }
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void timer(CallbackInfo info) {
        if (dashStaminaCooldownTicks > 0) dashStaminaCooldownTicks--;
        timer1 += 1;
        if (timer1 >= 80)
            timerA = true;

        battleA();
        if (battle) {
            CharactersComponent component = ModComponents.CHARACTERS.get(player);
            Character character = component.getCharacter(component.getCurrentCharacter());
            if (character != null) {
                stamina = character.getStamina();
                staminaRegen = character.getStaminaRegen();

                if (player.isSprinting()) {
                    currentStamina -= 0.025F;
                    if (currentStamina < 0.0F) {
                        currentStamina = 0.0F;
                        player.setSprinting(false);
                    }
                    axorune$onStaminaSpent();
                } else if (timerA) {
                    currentStamina += stamina * staminaRegen;
                    if (currentStamina > stamina) currentStamina = stamina;
                }

                currentHeal = player.getHealth();
                healReserve = character.getHealReserve();
                healRegen = character.getHealRegen();

                if (currentHeal < healReserve) {
                    newHeal = currentHeal + ((healReserve + character.getAllHealReserveBonus()) * (healRegen + character.getAllHealRegenBonus()));
                    currentHeal = newHeal;
                    player.setHealth(newHeal);
                }
            }

            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeFloat(currentStamina);
            ServerNetwork.send(player, ServerNetwork.STAMINA_PACKET_ID, buf);
        }
    }

    @Override
    public boolean axorune$trySpendStamina(float cost) {
        battleA();
        if (!battle) return true;
        if (currentStamina < cost) return false;
        currentStamina -= cost;
        newStamina = currentStamina;
        axorune$onStaminaSpent();
        return true;
    }

    @Override
    public boolean axorune$trySpendDashStamina(float cost, int cooldownTicks) {
        battleA();
        if (!battle) return true;
        if (dashStaminaCooldownTicks > 0) return true;
        if (currentStamina < cost) return false;
        currentStamina -= cost;
        newStamina = currentStamina;
        dashStaminaCooldownTicks = cooldownTicks;
        axorune$onStaminaSpent();
        return true;
    }

    @Inject(at = @At("HEAD"), method = "heal", cancellable = true)
    private void healRegeneration(float amount, CallbackInfo info) {
        battleA();
        if (battle) {
            boolean doesDie = false;
            currentHeal = player.getHealth();
            CharactersComponent component = ModComponents.CHARACTERS.get(player);
            Character character = component.getCharacter(component.getCurrentCharacter());
            if (character!=null) {
                healReserve = character.getHealReserve();
                if (currentHeal < healReserve) {
                    if (currentHeal <= 0) {
                        doesDie = true;
                        info.cancel();
                    }
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
    @Override
    public float axorune$getCurrentStamina() {
        return currentStamina;
    }
}
