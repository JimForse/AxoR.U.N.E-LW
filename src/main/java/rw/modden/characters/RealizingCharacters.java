package rw.modden.characters;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import rw.modden.access.StaminaAccess;
import rw.modden.combat.Battle;
import rw.modden.components.CharactersComponent;
import rw.modden.components.ModComponents;

import static rw.modden.Axorunelostworlds.LOGGER;

public class RealizingCharacters {
    public Character character;
    public void realizingCharacterForPlayer(CharacterName name, ServerPlayerEntity player) {
        CharactersComponent component = ModComponents.CHARACTERS.get(player);
        CharacterName previousName = component.getCurrentCharacter();
        LOGGER.info("[ARLW-DEBUG] realizing '{}' for {} | previous='{}' | health before={}/{}",
                name, player.getEntityName(), previousName, player.getHealth(), player.getMaxHealth());

        if (previousName != null && component.hasCharacter(previousName)) {
            Character previous = component.getCharacter(previousName);
            LOGGER.info("[ARLW-DEBUG] saving '{}'.currentHeal = {} (was {})",
                    previousName, player.getHealth(), previous.getCurrentHeal());
            previous.setCurrentHeal(player.getHealth());
        }

        standartAttributesForPlayer(player);

        EntityAttributeInstance heal = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH),
                strength = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE),
                defence = player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);

        Character character = null;
        if (component.hasCharacter(name)) {
            character = component.getCharacter(name);
        }

        try {
            heal.setBaseValue((double) character.getHealReserve());
            strength.setBaseValue((double) character.getStrength());
            defence.setBaseValue((double) character.getDefence());
        } catch (Exception e) {
            LOGGER.error("[ARLW-DEBUG] exception while setting attributes for '{}': {}", name, e.toString());
        }
        component.setCurrentCharacter(name);
        player.damage(player.getDamageSources().generic(), 1.0F);

        LOGGER.info("[ARLW-DEBUG] after attribute swap: maxHealth={} health={}", player.getMaxHealth(), player.getHealth());

        if (character != null) {
            ((StaminaAccess) player).axorune$setCurrentStamina(character.getStamina());
            if (!character.isActivatedThisBattle()) {
                LOGGER.info("[ARLW-DEBUG] '{}' first activation this battle -> full heal to {}", name, player.getMaxHealth());
                player.setHealth(player.getMaxHealth());
                character.setActivatedThisBattle(true);
            } else {
                float restore = Math.min(character.getCurrentHeal(), player.getMaxHealth());
                LOGGER.info("[ARLW-DEBUG] '{}' already activated -> restoring {} (stored currentHeal={}, maxHealth={})",
                        name, restore, character.getCurrentHeal(), player.getMaxHealth());
                player.setHealth(restore);
            }
        } else {
            LOGGER.error("[ARLW-DEBUG] character '{}' is NULL at restore step!", name);
        }
    }

    public void standartAttributesForPlayer(ServerPlayerEntity player) {
        EntityAttributeInstance heal = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH),
                strength = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE),
                defence = player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);

        try {
            heal.setBaseValue(20.0);
            strength.setBaseValue(1.0);
            defence.setBaseValue(0.0);
        } catch (Exception e) {}
        player.damage(player.getDamageSources().generic(), 1.0F);
    }
}
