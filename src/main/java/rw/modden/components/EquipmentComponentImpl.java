package rw.modden.components;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import rw.modden.Axorunelostworlds;

import java.util.HashMap;
import java.util.Map;

public class EquipmentComponentImpl implements EquipmentComponent {
    private final PlayerEntity player;
    private Map<String, String> itemList = new HashMap<>();

    public EquipmentComponentImpl(PlayerEntity player) {
        this.player = player;
    }

    public Map<String, String> getItemList() {
        return itemList;
    }
//    public String getItem(String uniqueID) {
//
//    } TODO: придумать конкретный вид и работу, и реализовать
    public void addItem(String itemID) {
        itemList.put(itemID, new Axorunelostworlds().getUniqueItemID());
    }

    // Для получения itemID нужно использовать:
    /** Registries.ITEM.getId(itemStack.getItem()).toString() */

    public void addItem(String itemID, String uniqueID) {
        itemList.put(itemID,uniqueID);
    }
    public void removeItem(String uniqueID) {
        String itemID = "";
        for(Map.Entry<String, String> map: itemList.entrySet()) {
            String value = map.getValue();
            if (value.equals(uniqueID)) {
                itemID = map.getKey();
                break;
            }
        }
        itemList.remove(itemID, uniqueID);
    }
    public void removeItem(String itemID, String uniqueID) {
        itemList.remove(itemID, uniqueID);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {

    }

    @Override
    public void writeToNbt(NbtCompound tag) {

    }
}
