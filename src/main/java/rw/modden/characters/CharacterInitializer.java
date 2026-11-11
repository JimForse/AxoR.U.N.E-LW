package rw.modden.characters;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import rw.modden.combat.Battle;
import rw.modden.combat.CombatState;
import rw.modden.components.CharactersComponent;
import rw.modden.components.ModComponents;
import static rw.modden.Axorunelostworlds.LOGGER;

import static rw.modden.components.ModComponents.CHARACTERS;

public class CharacterInitializer {
    public void getOrCreate() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            CharactersComponent component = CHARACTERS.get(player);
            CharacterName own = characterFor(player);

            if (!CharacterTemplates.exists(own)) {
                LOGGER.info("[ARLW-CHAR] {} joined, no template {} - creating default", player.getName().getString(), own);
                CharacterTemplates.put(own, CharacterTemplate.create(CharacterTemplate.randomPath()));
            }
            component.refreshLocks();
            if (!component.hasCharacter(own)) component.addCharacter(own);
            LOGGER.info("[ARLW-CHAR] {} joined, characters: {}", player.getName().getString(), component.getCharacters().keySet());
            CHARACTERS.sync(player);

            CombatState state = ModComponents.BATTLE_STATE.get(player).getState();
            if (state.equals(CombatState.STANDART)) new Battle(player).stopBattle();
        });
    }

    public CharacterName characterFor(ServerPlayerEntity player) {
        return CharacterName.of(player.getGameProfile().getName());
    }
}
