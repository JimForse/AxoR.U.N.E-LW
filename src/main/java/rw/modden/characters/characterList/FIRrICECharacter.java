package rw.modden.characters.characterList;

import rw.modden.characters.ABCharacter;
import rw.modden.characters.CharacterName;
import rw.modden.combat.path.CreatePath;
import rw.modden.combat.path.Path;
import rw.modden.items.Equipment;

public class FIRrICECharacter extends ABCharacter {
    public FIRrICECharacter() {
        super(500.0F, 5, 1000.0F, 10, 0.2F, 0.2F, 100, new CreatePath(), CharacterName.FIRRICE);
    }
    public FIRrICECharacter(float healReserve, int stars, float stamina, int strength, float staminaRegen, float healRegen, int defence, Path pathID, CharacterName name) {
        super(healReserve, stars, stamina, strength, staminaRegen, healRegen, defence, pathID, name);
    }
}
