package rw.modden.components;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.server.network.ServerPlayerEntity;
import rw.modden.characters.CharacterName;
import rw.modden.items.Equipment;

public interface EquipmentComponent extends Component {
    public Equipment getSlot(CharacterName character, int number, ServerPlayerEntity player);
    public boolean hasItem(CharacterName character, Equipment item);
    public int getItemSlot(CharacterName character, Equipment item);
    public void removeItemFromSlot(CharacterName character, int number);
    public void removeItem(CharacterName character, Equipment item);
}
