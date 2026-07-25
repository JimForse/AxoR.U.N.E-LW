package rw.modden.components;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import rw.modden.characters.CharacterName;
import rw.modden.items.Equipment;

public class EquipmentComponentImpl implements EquipmentComponent{
    private final PlayerEntity player;

    public EquipmentComponentImpl(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public Equipment getSlot(CharacterName character, int number, ServerPlayerEntity player) {
        Equipment slot = null;
//        switch (number) {
//            case 1 -> slot = ModComponents.CHARACTERS.get(player).getCharacter(character).getHead();
//            case 2 -> slot = ;
//            case 3 -> slot = ;
//            case 4 -> slot = ;
//            case 5 -> slot = ;
//            case 6 -> slot = ;
//            case 7 -> slot = ;
//        }
        // TODO: реализовать, как только будет выполнено задание Equipment.6
        return slot;
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

    @Override
    public void readFromNbt(NbtCompound tag) {

    }

    @Override
    public void writeToNbt(NbtCompound tag) {

    }
}
