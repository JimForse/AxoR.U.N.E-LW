package rw.modden.items;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

public class ABEquip {
    private String uniqueID, itemID;
    private float healReserveBonus, healRegenBonus, damageBonus, defenceBonus;

    public ABEquip (String uniqueID, String itemID) {
        this.uniqueID = uniqueID;
        this.itemID = itemID;
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
        if (nbt.contains("healReserveBonus" + uniqueID))
            this.healReserveBonus = nbt.getFloat( "healReserveBonus" + uniqueID);
        if (nbt.contains("healRegenBonus" + uniqueID))
            this.healRegenBonus = nbt.getFloat( "healRegenBonus" + uniqueID);
        if (nbt.contains("defenceBonus" + uniqueID))
            this.defenceBonus = nbt.getFloat( "defenceBonus" + uniqueID);
        if (nbt.contains("damageBonus"+uniqueID))
            this.damageBonus = nbt.getFloat( "damageBonus" + uniqueID);
        this.itemID = nbt.getString("item_id" + uniqueID);
    }

    public void writeToNbt(NbtCompound nbt) {
        if (healReserveBonus!=0.0F) nbt.putFloat ("healReserveBonus" + uniqueID, healReserveBonus);
        if (healRegenBonus!=0.0F) nbt.putFloat ("healRegenBonus" + uniqueID, healRegenBonus);
        if (damageBonus!=0.0F) nbt.putFloat ("damageBonus" + uniqueID, damageBonus);
        if (defenceBonus!=0.0F) nbt.putFloat ("defenceBonus" + uniqueID, defenceBonus);
        nbt.putString("item_id" + uniqueID, itemID);
    }
}
