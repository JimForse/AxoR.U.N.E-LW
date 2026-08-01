package rw.modden.characters.characterList;

import rw.modden.characters.ABCharacter;
import rw.modden.characters.CharacterName;
import rw.modden.combat.path.CreatePath;
import rw.modden.combat.path.Path;

public class The_lost321Character extends ABCharacter {
    public The_lost321Character(float healReserve, int stars, float stamina, int strength, float staminaRegen, float healRegen, int defence, Path pathID, CharacterName name) {
        super(healReserve, stars, stamina, strength, staminaRegen, healRegen, defence, pathID, name);
    }
    public The_lost321Character() {
        super(100.0F, 0, 100.0F, 1, 0.01F, 0.01F, 10, new CreatePath(), CharacterName.THE_LOST);
    }
}
