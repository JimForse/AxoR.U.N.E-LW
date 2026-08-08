package rw.modden.components;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import rw.modden.Axorunelostworlds;
import rw.modden.characters.Character;
import rw.modden.characters.CharacterName;
import rw.modden.items.ABEquip;

import java.util.HashMap;
import java.util.Map;

public class EquipmentComponentImpl implements EquipmentComponent {
    private final PlayerEntity player;
    private Map<String, String> itemList = new HashMap<>(); // itemID:uniqueID

    public EquipmentComponentImpl(PlayerEntity player) {
        this.player = player;
    }

    public Map<String, String> getItemList() {
        return itemList;
    }
    public ABEquip getItem(String uniqueID, CharacterName character) {
        return ModComponents.CHARACTERS.get(player).getCharacter(character).getItem(uniqueID);
    }
    public boolean hasItem(String uniqueID) {
        return itemList.containsValue(uniqueID);
    }
    public boolean hasItem(String uniqueID, CharacterName character) {
        return ModComponents.CHARACTERS.get(player).getCharacter(character).hasItem(uniqueID);
    }
    public void addItem(String itemID) {
        itemList.put(itemID, new Axorunelostworlds().getUniqueItemID());
    }

    // Для получения itemID нужно использовать:
    /** Registries.ITEM.getId(itemStack.getItem()).toString() */

    public void addItem(String itemID, String uniqueID) {
        itemList.put(itemID, uniqueID);
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
    public void readFromNbt(NbtCompound nbt) {
        NbtList nlist0 = nbt.getList("equipment_list_uniqueID", NbtElement.STRING_TYPE);
        NbtList nlist1 = nbt.getList("equipment_list_itemID", NbtElement.STRING_TYPE);

        for (int i = 0; i < nlist0.size(); i++) {
            itemList.put(nlist1.get(i).asString(), nlist0.get(i).asString());
        }

        /** Для максимально точного получения itemID предмета, нужно обратиться к character, чтобы получить объект предмета через uniqueID, но это сильно нагружает ЦП */
        /*
        NbtList nlist = nbt.getList("equipment_list", NbtElement.STRING_TYPE);
        nlist.forEach(x -> {
            Map<CharacterName, Character> charactersList = ModComponents.CHARACTERS.get(player).getCharacters();
            for (Map.Entry<CharacterName, Character> entry: charactersList.entrySet()) {
                CharacterName name = entry.getKey();
                if (hasItem(x.asString(), name)) {
                    itemList.put(getItem(x.asString(), name).getItemID(), x.asString());
                }
            }
        });
        */
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        NbtList nlist0 = new NbtList();
        NbtList nlist1 = new NbtList();
        for (Map.Entry<String, String> entry: itemList.entrySet()) {
            nlist0.add(NbtString.of(entry.getValue())); // uniqueID
            nlist1.add(NbtString.of(entry.getKey()));   // itemID
        }
        nbt.put("equipment_list_uniqueID", nlist0);
        nbt.put("equipment_list_itemID", nlist1);
    }
}
