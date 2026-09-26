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
    private boolean startedRun;
    @Unique
    private int dashStaminaCooldownTicks = 0;

    @Unique
    private void battleA() {
        if (!((Object) this instanceof ServerPlayerEntity)) return;
        player = (ServerPlayerEntity) (Object) this;

        Battle battleClass = new Battle(player);
        battleClass.combatStateToBattle();
        battle = battleClass.getBattle();

        if (battle && !wasInBattle) {
            CharactersComponent component = ModComponents.CHARACTERS.get(player);
            Character character = component.getCharacter(component.getCurrentCharacter());
            if (character != null) {
                currentStamina = character.getStamina();
            }
        }
        wasInBattle = battle;
    }

    @Inject(at = @At("HEAD"), method = "setSprinting", cancellable = true)
    private void sprint(boolean sprinting, CallbackInfo info) {
        battleA();
        if (battle) {
            CharactersComponent component = ModComponents.CHARACTERS.get(player);

            stamina = component.getCharacter(component.getCurrentCharacter()).getStamina();
            staminaRegen = component.getCharacter(component.getCurrentCharacter()).getStaminaRegen();
            if (sprinting && currentStamina <= 0.0)
                info.cancel();
            else if (sprinting) {
                newStamina = currentStamina - 0.5F;
                currentStamina = newStamina;
                startedRun = true;
            }
            else {
                startedRun = false;
                if (timerA) {
                    newStamina = currentStamina + (stamina * staminaRegen);
                    if (newStamina > stamina) newStamina = stamina;
                    currentStamina = newStamina;
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void timer(CallbackInfo info) {
        if (dashStaminaCooldownTicks > 0) dashStaminaCooldownTicks--;
        timer1 += 1;
        if (timer1>=80)
            timerA = true;
        if (startedRun) {
            timerA = false;
            timer1 = 0;
        }

        battleA();
        if (battle) {
            currentHeal = player.getHealth();
            CharactersComponent component = ModComponents.CHARACTERS.get(player);
            Character character = component.getCharacter(component.getCurrentCharacter());
            if (character!=null) {
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

    @Override
    public boolean axorune$trySpendStamina(float cost) {
        battleA();
        if (!battle) return true;
        if (currentStamina < cost) return false;
        currentStamina -= cost;
        newStamina = currentStamina;
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
        return true;
    }
}
