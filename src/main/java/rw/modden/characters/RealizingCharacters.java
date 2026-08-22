package rw.modden.characters;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import rw.modden.components.ModComponents;

import java.io.IOException;

public class RealizingCharacters {
    public Character character;
    public void realizingCharacterForPlayer(CharacterName name, ServerPlayerEntity player) {
        standartAttributesForPlayer(player);

        EntityAttributeInstance heal = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH),
            strength = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE),
            defence = player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);

        Character character = null;
        if (ModComponents.CHARACTERS.get(player).hasCharacter(name)) {
            character = ModComponents.CHARACTERS.get(player).getCharacter(name);
        }

        try {
            heal.setBaseValue((double) character.getHealReserve());
            strength.setBaseValue((double) character.getStrength());
            defence.setBaseValue((double) character.getDefence());
        } catch (Exception e) {}
        ModComponents.CHARACTERS.get(player).setCurrentCharacter(name);
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
    }
}
