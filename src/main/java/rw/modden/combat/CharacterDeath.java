package rw.modden.combat;

import net.minecraft.server.network.ServerPlayerEntity;
import rw.modden.characters.CharacterName;
import rw.modden.characters.RealizingCharacters;
import rw.modden.components.CharactersComponent;
import rw.modden.components.ModComponents;

import java.util.ArrayList;
import static rw.modden.Axorunelostworlds.LOGGER;

public final class CharacterDeath {
    private CharacterDeath() {}

    public static CharacterName nextAlive(CharactersComponent component, ArrayList<CharacterName> group, CharacterName current) {
        int size = group.size();
        int index = group.indexOf(current);
        for (int step = 1; step <= size; step++) {
            CharacterName candidate = group.get(Math.floorMod(index + step, size));
            if (candidate.equals(current)) continue;
            if (isAlive(component, candidate)) return candidate;
        }
        return null;
    }

    public static boolean isAlive(CharactersComponent component, CharacterName name) {
        return component.hasCharacter(name) && !component.getCharacter(name).isDead();
    }

    public static void onCurrentCharacterDeath(ServerPlayerEntity player) {
        CombatState state = ModComponents.BATTLE_STATE.get(player).getState();
        if (state != CombatState.STANDART && state != CombatState.EVENT) return;

        CharactersComponent component = ModComponents.CHARACTERS.get(player);
        CharacterName current = component.getCurrentCharacter();
        if (current == null || !component.hasCharacter(current)) {
            new Battle(player).stopBattle();
            return;
        }

        component.getCharacter(current).setDead(true);
        LOGGER.info("[ARLW] Character '{}' of {} is dead", current, player.getEntityName());

        CharacterName next = null;
        ArrayList<CharacterName> group = component.getCurrentGroup();
        if (state == CombatState.STANDART && group != null) {
            next = nextAlive(component, group, current);
        }

        if (next != null) {
            LOGGER.info("[ARLW] Switching {} to '{}'", player.getEntityName(), next);
            new RealizingCharacters().realizingCharacterForPlayer(next, player);
        } else {
            LOGGER.info("[ARLW] No alive characters left for {}, stopping battle", player.getEntityName());
            new Battle(player).stopBattle();
        }
    }
}