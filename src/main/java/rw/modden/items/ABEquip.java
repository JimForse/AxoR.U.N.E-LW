package rw.modden.items;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

public class ABEquip {
    private String id, itemID;
    private double healReserveBonus, healRegenBonus;

    public ABEquip (String uniqueID, String itemID, double healReserveBonus, double healRegenBonus) {
        this.id = uniqueID;
        this.itemID = itemID;
        this.healRegenBonus = healRegenBonus;
        this.healReserveBonus = healReserveBonus;
    }

    public void readFromNbt(NbtCompound nbt) {
        this.itemID = nbt.getString("unique_id"); // first line
        this.healReserveBonus = nbt.getDouble("healReserveBonus"+itemID);
        this.healRegenBonus = nbt.getDouble("healRegenBonus"+itemID);
        this.id = nbt.getString("item_id"+itemID);
    }

    public void writeToNbt(NbtCompound nbt) {
        nbt.putString("item_id: "+itemID, id);
        nbt.putDouble("healReserveBonus"+itemID, healReserveBonus);
        nbt.putDouble("healRegenBonus"+itemID, healRegenBonus);
        nbt.putString("unique_id", itemID); // last line
    }
}
