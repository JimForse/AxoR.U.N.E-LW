package rw.modden.characters;

import rw.modden.characters.characterList.*;

public class CharacterFactory {
    private Character character;
    public Character getCharacter(CharacterName name) {
        switch (name) {
            case FIRRICE ->       character = new FIRrICECharacter();
            case KLLIMA777 ->     character = new Kllima777Character();
            case THE_LOST ->      character = new The_lost321Character();
            case WAFEN ->         character = new Waffentrager_109Character();
            case SPECTORPROFM ->  character = new SpectorprofmCharacter();
            case VLAD ->          character = new vlad888222Character();
            case GVALL ->         character = new Gvall_Character();
            case OMFAS ->         character = new OmfasCharacter();
            case KEEPFEE ->       character = new keepfeep3215Character();
            case MATSVEI ->       character = new matsvei_v222Character();
        }

        return character;
    }
}
