package rw.modden.combat;

import net.minecraft.server.network.ServerPlayerEntity;
import rw.modden.components.CharactersComponent;
import rw.modden.components.ModComponents;
import rw.modden.characters.CharacterName;
import rw.modden.characters.Character;

import static rw.modden.Axorunelostworlds.LOGGER;

public final class Energy {
    private Energy() {}

    public static int stageOf(int energy) {
        if (energy >= CombatConstants.ENERGY_MAX) return 3;
        if (energy >= CombatConstants.ENERGY_STAGE_2) return 2;
        if (energy >= CombatConstants.ENERGY_STAGE_1) return 1;
        return 0;
    }

    public static boolean add(ServerPlayerEntity player, int amount) {
        CharactersComponent component = ModComponents.CHARACTERS.get(player);
        CharacterName name = component.getCurrentCharacter();
        if (name == null || !component.hasCharacter(name)) return false;

        Character character = component.getCharacter(name);
        int before = character.getEnergy();
        int after = Math.min(CombatConstants.ENERGY_MAX, before + amount);
        if (after == before) return false;

        character.setEnergy(after);
        return true;
    }
}