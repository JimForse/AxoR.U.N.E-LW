package rw.modden.characters.characterList;

import rw.modden.characters.ABCharacter;
import rw.modden.characters.CharacterName;
import rw.modden.combat.path.CreatePath;

public class FIRrICECharacter extends ABCharacter {
    public FIRrICECharacter() {
        super(100.0F, 5, 100.0F, 1, 0.01F, 0.01F, 10, new CreatePath(), CharacterName.FIRRICE);
        setAscension(5);
        setLevel(100);
    }
}
