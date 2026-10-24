package rw.modden.characters.characterList;

import rw.modden.characters.ABCharacter;
import rw.modden.characters.CharacterName;
import rw.modden.combat.path.CreatePath;

public class FIRrICECharacter extends ABCharacter {
    public FIRrICECharacter() {
        super(500.0F, 5, 1000.0F, 10, 0.2F, 0.2F, 100, new CreatePath(), CharacterName.FIRRICE);
    }
}
