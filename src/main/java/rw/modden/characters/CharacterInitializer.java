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
            case "FIRRICE" -> characterName = CharacterName.FIRRICE;
            case "KLLIMA777" -> characterName = CharacterName.KLLIMA777;
            case "THE_LOST" -> characterName = CharacterName.THE_LOST;
            case "WAFEN" -> characterName = CharacterName.WAFEN;
            case "SPECTORPROFM" -> characterName = CharacterName.SPECTORPROFM;
        }

        return characterName;
    }
}
