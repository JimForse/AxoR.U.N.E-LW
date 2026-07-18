package rw.modden.components;

import rw.modden.characters.CharacterName;
import rw.modden.items.Equipment;

public class EquipmentComponentImpl implements EquipmentComponent{
    @Override
    public int getSlot(CharacterName character, int number) {
        return 0;
    }

    @Override
    public boolean hasItem(CharacterName character, Equipment item) {
        return false;
    }

    @Override
    public int getItemSlot(CharacterName character, Equipment item) {
        return 0;
    }

    @Override
    public void removeItemFromSlot(CharacterName character, int number) {

    }

    @Override
    public void removeItem(CharacterName character, Equipment item) {

    }
}
