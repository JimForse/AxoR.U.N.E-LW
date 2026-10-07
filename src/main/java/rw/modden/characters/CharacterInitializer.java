package rw.modden.characters;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import rw.modden.Axorunelostworlds;
import rw.modden.combat.Battle;
import rw.modden.combat.CombatState;
import rw.modden.components.CharactersComponent;
import rw.modden.components.ModComponents;
import static rw.modden.Axorunelostworlds.LOGGER;

import static rw.modden.components.ModComponents.CHARACTERS;

public class CharacterInitializer {
    private static final CharacterName[] DEFAULT_STAT_CHARACTERS = {
            CharacterName.KLLIMA777, CharacterName.KEEPFEE, CharacterName.THE_LOST,
            CharacterName.WAFEN, CharacterName.SPECTORPROFM, CharacterName.MATSVEI,
            CharacterName.OMFAS, CharacterName.GVALL, CharacterName.VLAD
    };

    public void getOrCreate() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            CharactersComponent component = CHARACTERS.get(player);

            if (server.isSingleplayer() && component.getCharacters().isEmpty()) {
                Axorunelostworlds arlw = new Axorunelostworlds();

                String recordedNick = arlw.findNickForUuid(player.getUuid());
                CharacterName assigned = recordedNick != null ? getName(recordedNick.toUpperCase()): null;

                if (assigned != null) {
                    LOGGER.info("[ARLW] UUID {} matched recorded nick '{}' -> character {}",
                            player.getUuid(), recordedNick, assigned);
                } else {
                    int idx = Math.floorMod(player.getUuid().hashCode(), DEFAULT_STAT_CHARACTERS.length);
                    assigned = DEFAULT_STAT_CHARACTERS[idx];
                    LOGGER.info("[ARLW] UUID {} ({}) -> generic default character {}",
                            player.getUuid(), player.getEntityName(), assigned);
                }

                if (recordedNick == null) {
                    arlw.writeToJson(player.getEntityName(), player.getUuid());
                }

                component.addCharacter(assigned);
            }

            CombatState state = ModComponents.BATTLE_STATE.get(player).getState();
            if (state.equals(CombatState.STANDART)) new Battle(player).stopBattle();
        });
    }

    public CharacterName getName(String name) {
        CharacterName characterName = null;

        switch (name) {
            case "FIRRICE" ->          characterName = CharacterName.FIRRICE;
            case "KLLIMA777" ->        characterName = CharacterName.KLLIMA777;
            case "WAFFENTRAGER_109" -> characterName = CharacterName.WAFEN;
            case "SPECTORPROFM" ->     characterName = CharacterName.SPECTORPROFM;
            case "VLAD8822" ->         characterName = CharacterName.VLAD;
            case "THE_LOST321" ->      characterName = CharacterName.THE_LOST;
            case "OMFAS" ->            characterName = CharacterName.OMFAS;
            case "GVALL_" ->           characterName = CharacterName.GVALL;
            case "MATSVEI_V222" ->     characterName = CharacterName.MATSVEI;
            case "KEEPFEE3215" ->      characterName = CharacterName.KEEPFEE;
        }

        return characterName;
    }
}