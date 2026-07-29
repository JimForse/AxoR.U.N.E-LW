package rw.modden.characters;

import rw.modden.characters.characterList.FIRrICECharacter;
import rw.modden.characters.characterList.Kllima777Character;
import rw.modden.combat.path.Path;

public class CharacterFactory {
    private Character character;
    public Character getCharacter(CharacterName name) {
        switch (name) {
            case FIRRICE -> character = new FIRrICECharacter();
            case KLLIMA777 ->  character = new Kllima777Character();
//            case THE_LOST ->
//            case WAFEN ->
//            case SPECTORPROFM ->
        }

        return character;
    }
    public Character getCharacter(float healReserve, int stars, float stamina, int strength, float staminaRegen, float healRegen, int defence, Path pathID, CharacterName name) {
        switch (name) {
            case KLLIMA777 -> character = new Kllima777Character(healReserve, stars, stamina, strength, staminaRegen, healRegen, defence, pathID, name);
            case FIRRICE -> character = new FIRrICECharacter(healReserve, stars, stamina, strength, staminaRegen, healRegen, defence, pathID, name);
//            case THE_LOST -> character =
//            case WAFEN -> character =
//            case SPECTORPROFM -> character =
        }
        return character;
    }
}
