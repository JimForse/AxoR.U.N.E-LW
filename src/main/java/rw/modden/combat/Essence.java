package rw.modden.combat;

import net.minecraft.server.network.ServerPlayerEntity;
import rw.modden.characters.Character;
import rw.modden.characters.CharacterName;
import rw.modden.components.CharactersComponent;
import rw.modden.components.ModComponents;

import static rw.modden.Axorunelostworlds.LOGGER;

public final class Essence {
    private Essence() {}

    public static boolean isUltReady(Character character) {
        return character.getEssence() >= CombatConstants.ESSENCE_MAX;
    }

    public static void tryGain(ServerPlayerEntity player) {
        CharactersComponent component = ModComponents.CHARACTERS.get(player);
        CharacterName name = component.getCurrentCharacter();
        if (name == null || !component.hasCharacter(name)) return;

        Character character = component.getCharacter(name);
        if (character.getEnergy() < CombatConstants.ESSENCE_ENERGY_THRESHOLD) return;
        if (isUltReady(character)) return;

        if (player.getRandom().nextFloat() >= CombatConstants.ESSENCE_CHANCE) return;

        int before = character.getEssence();
        int after = Math.min(CombatConstants.ESSENCE_MAX, before + CombatConstants.ESSENCE_PER_HIT);
        character.setEssence(after);
        LOGGER.info("[ARLW-DEBUG] essence of {}: {} -> {}", name, before, after);
        if (isUltReady(character)) {
            LOGGER.info("[ARLW] Ultimate of {} is ready for {}", name, player.getEntityName());
        }
    }
}