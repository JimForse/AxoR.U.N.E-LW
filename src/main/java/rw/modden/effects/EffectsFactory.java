package rw.modden.effects;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;

public class EffectsFactory {
    public StatusEffect getVanillaEffects(String name) {
        StatusEffect effect = null;
        switch (name) {
            case "SPEED" -> effect = StatusEffects.SPEED;
            case "SLOWNESS" -> effect = StatusEffects.SLOWNESS;
            case "HASTE" -> effect = StatusEffects.HASTE;
            case "MINING_FATIGUE" -> effect = StatusEffects.MINING_FATIGUE;
            case "STRENGTH" -> effect = StatusEffects.STRENGTH;
            case "INSTANT_HEALTH" -> effect = StatusEffects.INSTANT_HEALTH;
            case "INSTANT_DAMAGE" -> effect = StatusEffects.INSTANT_DAMAGE;
            case "JUMP_BOOST" -> effect = StatusEffects.JUMP_BOOST;
            case "NAUSEA" -> effect = StatusEffects.NAUSEA;
            case "REGENERATION" -> effect = StatusEffects.REGENERATION;
            case "RESISTANCE" -> effect = StatusEffects.RESISTANCE;
            case "FIRE_RESISTANCE" -> effect = StatusEffects.FIRE_RESISTANCE;
            case "WATER_BREATHING" -> effect = StatusEffects.WATER_BREATHING;
            case "INVISIBILITY" -> effect = StatusEffects.INVISIBILITY;
            case "BLINDNESS" -> effect = StatusEffects.BLINDNESS;
            case "NIGHT_VISION" -> effect = StatusEffects.NIGHT_VISION;
            case "HUNGER" -> effect = StatusEffects.HUNGER;
            case "WEAKNESS" -> effect = StatusEffects.WEAKNESS;
            case "POISON" -> effect = StatusEffects.POISON;
            case "WITHER" -> effect = StatusEffects.WITHER;
            case "HEALTH_BOOST" -> effect = StatusEffects.HEALTH_BOOST;
            case "ABSORPTION" -> effect = StatusEffects.ABSORPTION;
            case "SATURATION" -> effect = StatusEffects.SATURATION;
            case "GLOWING" -> effect = StatusEffects.GLOWING;
            case "LEVITATION" -> effect = StatusEffects.LEVITATION;
            case "LUCK" -> effect = StatusEffects.LUCK;
            case "UNLUCK" -> effect = StatusEffects.UNLUCK;
            case "SLOW_FALLING" -> effect = StatusEffects.SLOW_FALLING;
            case "CONDUIT_POWER" -> effect = StatusEffects.CONDUIT_POWER;
            case "DOLPHINS_GRACE" -> effect = StatusEffects.DOLPHINS_GRACE;
            case "BAD_OMEN" -> effect = StatusEffects.BAD_OMEN;
            case "HERO_OF_THE_VILLAGE" -> effect = StatusEffects.HERO_OF_THE_VILLAGE;
            case "DARKNESS" -> effect = StatusEffects.DARKNESS;
        }
        return effect;
    }
}
