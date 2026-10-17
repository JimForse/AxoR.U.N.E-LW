package rw.modden.mixin;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rw.modden.access.StaminaAccess;
import rw.modden.characters.Character;
import rw.modden.characters.RealizingCharacters;
import rw.modden.combat.*;
import rw.modden.components.CharactersComponent;
import rw.modden.components.ModComponents;
import rw.modden.network.ServerNetwork;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements StaminaAccess {
    @Shadow
    public abstract long getLootTableSeed();

    @Unique
    private float newStamina, currentStamina, newHeal, currentHeal, healReserve, healRegen, stamina, staminaRegen;
    @Unique
    private ServerPlayerEntity player;
    @Unique
    private int dashStaminaCooldownTicks = 0, healCooldownTicks = 0, timer1 = 0, energyPassiveTicks = 0;;
    @Unique
    private boolean staminaInitialized = false, deathPending = false, timerA, wasInBattle = false, battle;

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
        if (deathPending && (Object) this instanceof ServerPlayerEntity deadPlayer) {
            deathPending = false;
            CharacterDeath.onCurrentCharacterDeath(deadPlayer);
        }
        if (dashStaminaCooldownTicks > 0) dashStaminaCooldownTicks--;
        if (healCooldownTicks > 0) healCooldownTicks--;
        timer1 += 1;
        if (timer1 >= 140)
            timerA = true;

        battleA();
        if (battle) {
            CharactersComponent component = ModComponents.CHARACTERS.get(player);
            energyPassiveTicks++;
            if (energyPassiveTicks >= CombatConstants.ENERGY_PASSIVE_INTERVAL_TICKS) {
                energyPassiveTicks = 0;
                if (Energy.add(player, CombatConstants.ENERGY_PASSIVE_AMOUNT)) ServerNetwork.sendGroup(player);
            }
            Character character = component.getCharacter(component.getCurrentCharacter());
            if (character != null) {
                stamina = character.getStamina();
                staminaRegen = character.getStaminaRegen();

                if (player.isSprinting()) {
                    currentStamina -= 0.1F;
                    if (currentStamina < 0.0F) {
                        currentStamina = 0.0F;
                        player.setSprinting(false);
                    }
                    axorune$onStaminaSpent();
                } else if (timerA) {
                    currentStamina += stamina * staminaRegen;
                    if (currentStamina > stamina) {
                        currentStamina = stamina;
                    }
                }

                currentHeal = player.getHealth();
                healReserve = character.getHealReserve();
                healRegen = character.getHealRegen();

                if (currentHeal < healReserve) {
                    if (healCooldownTicks <= 0) {
                        newHeal = currentHeal + ((healReserve + character.getAllHealReserveBonus()) * (healRegen + character.getAllHealRegenBonus()));
                        if (newHeal > healReserve) newHeal = healReserve;
                        currentHeal = newHeal;
                        player.setHealth(newHeal);
                    }
                }
            }

            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeFloat(currentStamina);
            buf.writeFloat(stamina);
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
            CharactersComponent component = ModComponents.CHARACTERS.get(player);
            Character character = component.getCharacter(component.getCurrentCharacter());
            if (character != null) {
                float a = (character.getHealReserve() + character.getAllHealReserveBonus())
                        * (character.getHealRegen() + character.getAllHealRegenBonus());
                healCooldownTicks = Math.round(20 + 15 * (float) Math.log(a + 1));
            }

            if (healReserve != 0.0F)
                currentHeal = healReserve - amount;
        }
    }

    @Override
    public float axorune$getCurrentStamina() {
        return currentStamina;
    }

    @Override
    public void axorune$setCurrentStamina(float value) {
        currentStamina = value;
    }

    @ModifyVariable(method = "setHealth", at = @At("HEAD"), argsOnly = true)
    private float axorune$preventLethalInBattle(float health) {
        if (health > 0.0F) return health;

        battleA();
        if (!battle || !((Object) this instanceof ServerPlayerEntity)) return health;
        deathPending = true;
        return 1.0F;
    }

    @Inject(at = @At("RETURN"), method = "damage")
    private void axorune$essenceOnTakenDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) return;
        if (RealizingCharacters.inServiceDamage) return;
        if (!((Object) this instanceof ServerPlayerEntity hurtPlayer)) return;

        battleA();
        if (!battle) return;
        if (Essence.tryGain(hurtPlayer, CombatConstants.ESSENCE_TAKEN_DAMAGE_PERCENT))
            ServerNetwork.sendGroup(hurtPlayer);
    }
}
