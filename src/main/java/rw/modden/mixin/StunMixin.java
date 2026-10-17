package rw.modden.mixin;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rw.modden.access.StunAccess;
import rw.modden.combat.CombatConstants;
import rw.modden.combat.CombatState;
import rw.modden.combat.Energy;
import rw.modden.combat.Essence;
import rw.modden.commands.CharacterCommands;
import rw.modden.components.CharactersComponent;
import rw.modden.components.CharactersComponentImpl;
import rw.modden.components.ModComponents;
import rw.modden.characters.Character;
import rw.modden.network.ServerNetwork;

import static rw.modden.Axorunelostworlds.LOGGER;

@Mixin(LivingEntity.class)
public abstract class StunMixin implements StunAccess {

    @Unique private float stun = 0.0F;
    @Unique private int stunTicks = 0;
    @Unique private boolean stunReentry = false;

    @Override public float axorune$getStun() { return stun; }
    @Override public boolean axorune$isStunned() { return stunTicks > 0; }
    @Override public int axorune$getStunTicks() { return stunTicks; }

    @Inject(method = "tick", at = @At("HEAD"))
    private void axorune$stunTick(CallbackInfo ci) {
        if (stunTicks > 0 && !((LivingEntity) (Object) this).getWorld().isClient)
            stunTicks--;
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void axorune$stunOnDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (stunReentry) return;

        LivingEntity self = (LivingEntity) (Object) this;
        if (self.getWorld().isClient) return;
        if (!(self instanceof MobEntity mob)) return;
        if (!(source.getAttacker() instanceof ServerPlayerEntity attacker)) return;
        if (ModComponents.BATTLE_STATE.get(attacker).getState() == CombatState.NONE) return;
        if (mob.getTarget() != attacker) return;

        boolean wasStunned = stunTicks > 0;
        float finalAmount = wasStunned ? amount * CombatConstants.STUN_DAMAGE_MULTIPLIER:amount;

        stunReentry = true;
        boolean damaged;
        try {
            damaged = self.damage(source, finalAmount);
        } finally {
            stunReentry = false;
        }
        if (damaged) {
            boolean changed = Energy.add(attacker, CombatConstants.ENERGY_PER_HIT);
            changed |= Essence.tryGain(attacker, CombatConstants.ESSENCE_ENEMY_DAMAGE_PERCENT);
            if (changed) ServerNetwork.sendGroup(attacker);
        }
        if (damaged && !wasStunned) {
            stun += CombatConstants.STUN_PER_HIT;
            if (stun >= CombatConstants.STUN_MAX) {
                stun = 0.0F;
                stunTicks = CombatConstants.STUN_DURATION_TICKS;
                LOGGER.info("[ARLW] {} is stunned for {} ticks", self.getName().getString(), stunTicks);
            }
        }
        cir.setReturnValue(damaged);
    }
}