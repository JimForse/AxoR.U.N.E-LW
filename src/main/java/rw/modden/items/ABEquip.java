package rw.modden.items;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

public class ABEquip {
    private String uniqueID, itemID;
    private float healReserveBonus, healRegenBonus, damageBonus, defenceBonus;

    public ABEquip (String uniqueID, String itemID, float healReserveBonus, float healRegenBonus, float damageBonus, float defenceBonus) {
        this.uniqueID = uniqueID;
        this.itemID = itemID;
        this.damageBonus = damageBonus;
        this.defenceBonus = defenceBonus;
        this.healRegenBonus = healRegenBonus;
        this.healReserveBonus = healReserveBonus;
    }

    public float getDamageBonus() {
        return damageBonus;
    }
    public float getHealRegenBonus() {
        return healRegenBonus;
    }
    public float getHealReserveBonus() {
        return healReserveBonus;
    }
    public float getDefenceBonus() {
        return defenceBonus;
    }
    public String getItemID() {
        return itemID;
    }
    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String id) {
        this.uniqueID = id;
    }
    public void setHealReserveBonus(float healReserveBonus) {
        this.healReserveBonus = healReserveBonus;
    }
    public void setItemID(String itemID) {
        this.itemID = itemID;
    }
    public void setDamageBonus(float damageBonus) {
        this.damageBonus = damageBonus;
    }
    public void setDefenceBonus(float defenceBonus) {
        this.defenceBonus = defenceBonus;
    }
    public void setHealRegenBonus(float healRegenBonus) {
        this.healRegenBonus = healRegenBonus;
    }

    public void readFromNbt(NbtCompound nbt) {
        this.uniqueID         = nbt.getString("unique_id"); // first line
        this.healReserveBonus = nbt.getFloat( "healReserveBonus" + uniqueID);
        this.healRegenBonus   = nbt.getFloat( "healRegenBonus"   + uniqueID);
        this.defenceBonus     = nbt.getFloat( "defenceBonus"     + uniqueID);
        this.damageBonus      = nbt.getFloat( "damageBonus"      + uniqueID);
        this.itemID           = nbt.getString("item_id"          + uniqueID);
    }

    public void writeToNbt(NbtCompound nbt) {
        nbt.putFloat ("healReserveBonus" + itemID, healReserveBonus);
        nbt.putFloat ("healRegenBonus"   + itemID, healRegenBonus);
        nbt.putFloat ("damageBonus"      + itemID, damageBonus);
        nbt.putFloat ("defenceBonus"     + itemID, defenceBonus);
        nbt.putString("item_id: "  + itemID, uniqueID);
        nbt.putString("unique_id", itemID); // last line
    }
}
