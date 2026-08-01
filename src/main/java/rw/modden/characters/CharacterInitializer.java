package rw.modden.characters;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import rw.modden.Axorunelostworlds;

import static rw.modden.components.ModComponents.CHARACTERS;

public class CharacterInitializer {
    private void getOrCreate() {
        Axorunelostworlds arlw = new Axorunelostworlds();
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            if (!arlw.checkJson(player.getEntityName())) {
                arlw.writeToJson(player.getEntityName(), player.getUuid());
                if (!CHARACTERS.get(player).hasCharacter(getName(player.getEntityName().toUpperCase()))) {
                    CHARACTERS.get(player).addCharacter(getName((player.getEntityName().toUpperCase())));
                }
            }
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
