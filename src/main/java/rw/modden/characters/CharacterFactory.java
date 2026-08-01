package rw.modden.characters;

import rw.modden.characters.characterList.*;
import rw.modden.combat.path.Path;

public class CharacterFactory {
    private Character character;
    public Character getCharacter(CharacterName name) {
        switch (name) {
            case FIRRICE ->       character = new FIRrICECharacter();
            case KLLIMA777 ->     character = new Kllima777Character();
            case THE_LOST ->      character = new The_lost321Character();
            case WAFEN ->         character = new Waffentrager_109Character();
            case SPECTORPROFM ->  character = new SpectorprofmCharacter();
            case VLAD ->          character = new vlad8822Character();
            case GVALL ->         character = new Gvall_Character();
            case OMFAS ->         character = new OmfasCharacter();
            case KEEPFEE ->       character = new Keepfee3215Character();
            case MATSVEI ->       character = new matsvei_v222Character();
        }

        return character;
    }
    public Character getCharacter(float healReserve, int stars, float stamina, int strength, float staminaRegen, float healRegen, int defence, Path pathID, CharacterName name) {
        switch (name) {
            case KLLIMA777 ->    character = new Kllima777Character(healReserve, stars, stamina, strength, staminaRegen, healRegen, defence, pathID, name);
            case FIRRICE ->      character = new FIRrICECharacter(healReserve, stars, stamina, strength, staminaRegen, healRegen, defence, pathID, name);
            case THE_LOST ->     character = new The_lost321Character(healReserve, stars, stamina, strength, staminaRegen, healRegen, defence, pathID, name);
            case WAFEN ->        character = new Waffentrager_109Character(healReserve, stars, stamina, strength, staminaRegen, healRegen, defence, pathID, name);
            case SPECTORPROFM -> character = new SpectorprofmCharacter(healReserve, stars, stamina, strength, staminaRegen, healRegen, defence, pathID, name);
            case MATSVEI ->      character = new matsvei_v222Character(healReserve, stars, stamina, strength, staminaRegen, healRegen, defence, pathID, name);
            case KEEPFEE ->      character = new Keepfee3215Character(healReserve, stars, stamina, strength, staminaRegen, healRegen, defence, pathID, name);
            case OMFAS ->        character = new OmfasCharacter(healReserve, stars, stamina, strength, staminaRegen, healRegen, defence, pathID, name);
            case GVALL ->        character = new Gvall_Character(healReserve, stars, stamina, strength, staminaRegen, healRegen, defence, pathID, name);
            case VLAD ->         character = new vlad8822Character(healReserve, stars, stamina, strength, staminaRegen, healRegen, defence, pathID, name);
        }
        return character;
    }
}
