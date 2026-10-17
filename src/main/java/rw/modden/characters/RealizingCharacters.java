package rw.modden.characters;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import rw.modden.access.StaminaAccess;
import rw.modden.combat.Battle;
import rw.modden.components.CharactersComponent;
import rw.modden.components.ModComponents;
import rw.modden.network.ServerNetwork;

import static rw.modden.Axorunelostworlds.LOGGER;

public class RealizingCharacters {
    public Character character;
    public static boolean inServiceDamage = false;
    public void realizingCharacterForPlayer(CharacterName name, ServerPlayerEntity player) {
        CharactersComponent component = ModComponents.CHARACTERS.get(player);
        CharacterName previousName = component.getCurrentCharacter();

        if (previousName != null && component.hasCharacter(previousName)) {
            Character previous = component.getCharacter(previousName);
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
        player.setHealth(player.getMaxHealth());
        serviceDamage(player);

        if (character != null) {
            ((StaminaAccess) player).axorune$setCurrentStamina(character.getStamina());
            if (!character.isActivatedThisBattle()) {
                player.setHealth(player.getMaxHealth());
                character.setActivatedThisBattle(true);
            } else {
                float restore = Math.min(character.getCurrentHeal(), player.getMaxHealth());
                player.setHealth(restore);
            }
        } else {
            LOGGER.error("[ARLW-DEBUG] character '{}' is NULL at restore step!", name);
        }
        ServerNetwork.sendGroup(player);
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
        player.setHealth(player.getMaxHealth());
        serviceDamage(player);
    }

    private static void serviceDamage(ServerPlayerEntity player) {
        inServiceDamage = true;
        try {
            player.damage(player.getDamageSources().generic(), 1.0F);
        } finally {
            inServiceDamage = false;
        }
    }
}
