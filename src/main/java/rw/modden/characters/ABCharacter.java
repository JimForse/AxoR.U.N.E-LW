package rw.modden.characters;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import rw.modden.combat.path.Path;
import rw.modden.combat.path.PathFactory;
import rw.modden.combat.path.PathesName;
import rw.modden.items.ABEquip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class ABCharacter implements Character {
    private int stars, strength, defence;
    private float healReserve, staminaRegen, stamina, healRegen;
    private Path pathID;
    private CharacterName name;
    private ABEquip head, chest, legs, boots, slot1, slot2, slot3;
    private Map<String, ABEquip> equipMap = new HashMap<>();

    public ABCharacter (float healReserve, int stars, float stamina, int strength, float staminaRegen, float healRegen, int defence, Path pathID, CharacterName name) {
        this.healReserve = healReserve;
        this.stars = stars;
        this.stamina = stamina;
        this.strength = strength;
        this.pathID = pathID;
        this.name = name;
        this.staminaRegen = staminaRegen;
        this.healRegen = healRegen;
        this.defence = defence;
    }

    @Override
    public float getHealReserve() {
        return healReserve;
    }
    @Override
    public int getStars() {
        return stars;
    }
    @Override
    public float getStamina() {
        return stamina;
    }
    @Override
    public int getStrength() {
        return strength;
    }
    @Override
    public Path getPath() {
        return pathID;
    }
    @Override
    public CharacterName getName() {
        return name;
    }
    @Override
    public float getStaminaRegen() {
        return staminaRegen;
    }
    @Override
    public float getHealRegen() {
        return healRegen;
    }
    @Override
    public int getDefence() {
        return defence;
    }
    @Override
    public ABEquip getHead() {
        return head;
    }
    @Override
    public ABEquip getChest() {
        return chest;
    }
    @Override
    public ABEquip getLegs() {
        return legs;
    }
    @Override
    public ABEquip getBoots() {
        return boots;
    }
    @Override
    public ABEquip getFirstSlot() {
        return slot1;
    }
    @Override
    public ABEquip getSecondSlot() {
        return slot2;
    }
    @Override
    public ABEquip getThirdSlot() {
        return slot3;
    }

    @Override
    public void setHealReserve(float value) {
        this.healReserve = value;
    }
    @Override
    public void setStaminaRegen(float value) {
        this.staminaRegen = value;
    }
    @Override
    public void setHealRegen(float value) {
        this.healRegen = value;
    }
    @Override
    public void setDefence(int value) {
        this.defence = value;
    }
    @Override
    public void setStars(int value) {
        this.stars = value;
    }
    @Override
    public void setStamina(float value) {
        this.stamina = value;
    }
    @Override
    public void setStrength(int value) {
        this.strength = value;
    }
    @Override
    public void setPath(Path pathID) {
        this.pathID = pathID;
    }
    @Override
    public void setHead(ABEquip armor) {
        if (this.head != null)
            removeItem(this.head);
        this.head = armor;
        addItem(armor);
    }
    @Override
    public void setChest(ABEquip armor) {
        if (this.chest != null)
            removeItem(this.chest);
        this.chest = armor;
        addItem(armor);
    }
    @Override
    public void setLegs(ABEquip armor) {
        if (this.legs != null)
            removeItem(this.legs);
        this.legs = armor;
        addItem(armor);
    }
    @Override
    public void setBoots(ABEquip armor) {
        if (this.boots != null)
            removeItem(this.boots);
        this.boots = armor;
        addItem(armor);
    }
    @Override
    public void setFirstSlot(ABEquip item) {
        if (this.slot1 != null)
            removeItem(this.slot1);
        this.slot1 = item;
        addItem(item);
    }
    @Override
    public void setSecondSlot(ABEquip item) {
        if (this.slot2 != null)
            removeItem(this.slot2);
        this.slot2 = item;
        addItem(item);
    }
    @Override
    public void setThirdSlot(ABEquip item) {
        if (this.slot3 != null)
            removeItem(this.slot3);
        this.slot3 = item;
        addItem(item);
    }

    private void addItem(ABEquip item) {
        equipMap.put(item.getUniqueID(), item);
    }
    private void removeItem(ABEquip item) {
        equipMap.remove(item.getUniqueID());
    }
    @Override
    public boolean hasItem(ABEquip item) {
        return hasItem(item.getUniqueID());
    }
    @Override
    public boolean hasItem(String uniqueID) {
        return equipMap.containsKey(uniqueID);
    }
    @Override
    public ABEquip getItem(String uniqueID) {
        return equipMap.get(uniqueID);
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        this.name = CharacterName.valueOf(nbt.getString("name")); // first line
        this.defence      = nbt.getInt(  name.name() + "_defence");
        this.stars        = nbt.getInt(  name.name() + "_stars");
        this.strength     = nbt.getInt(  name.name() + "_strength");
        this.healReserve  = nbt.getFloat(name.name() + "_healReserve");
        this.stamina      = nbt.getFloat(name.name() + "_stamina");
        this.staminaRegen = nbt.getFloat(name.name() + "_staminaRegen");
        this.healRegen    = nbt.getFloat(name.name() + "_healRegen");
        this.pathID = PathFactory.get(
                PathesName.valueOf(nbt.getString(name.name() + "_path"))
        );

        /** Items block */
        String uniqueID = "", itemID = "";
        Float healReserveBonus, healRegenBonus, defenceBonus, damageBonus;
        ArrayList<String> slots   = new ArrayList<>();

        slots.add(nbt.getString(name.name() + "_head"));
        slots.add(nbt.getString(name.name() + "_chest"));
        slots.add(nbt.getString(name.name() + "_legs"));
        slots.add(nbt.getString(name.name() + "_boots"));
        slots.add(nbt.getString(name.name() + "_slot1"));
        slots.add(nbt.getString(name.name() + "_slot2"));
        slots.add(nbt.getString(name.name() + "_slot3"));

        for (int i = 0; i < slots.size(); i++) {
            uniqueID = slots.get(i);
            healReserveBonus = nbt.getFloat( "healReserveBonus" + uniqueID);
            healRegenBonus   = nbt.getFloat( "healRegenBonus"   + uniqueID);
            defenceBonus     = nbt.getFloat( "defenceBonus"     + uniqueID);
            damageBonus      = nbt.getFloat( "damageBonus"      + uniqueID);
            itemID           = nbt.getString("item_id"          + uniqueID);

            switch (i) {
                case 0 -> this.head  = new ABEquip(uniqueID,itemID,healReserveBonus,healRegenBonus,damageBonus,defenceBonus);
                case 1 -> this.chest = new ABEquip(uniqueID,itemID,healReserveBonus,healRegenBonus,damageBonus,defenceBonus);
                case 2 -> this.legs  = new ABEquip(uniqueID,itemID,healReserveBonus,healRegenBonus,damageBonus,defenceBonus);
                case 3 -> this.boots = new ABEquip(uniqueID,itemID,healReserveBonus,healRegenBonus,damageBonus,defenceBonus);
                case 4 -> this.slot1 = new ABEquip(uniqueID,itemID,healReserveBonus,healRegenBonus,damageBonus,defenceBonus);
                case 5 -> this.slot2 = new ABEquip(uniqueID,itemID,healReserveBonus,healRegenBonus,damageBonus,defenceBonus);
                case 6 -> this.slot3 = new ABEquip(uniqueID,itemID,healReserveBonus,healRegenBonus,damageBonus,defenceBonus);
            }
        }
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        nbt.putInt   (name.name()+"_defence",      defence);
        nbt.putInt   (name.name()+"_strength",     strength);
        nbt.putInt   (name.name()+"_stars",        stars);
        nbt.putFloat (name.name()+"_healReserve",  healReserve);
        nbt.putFloat (name.name()+"_stamina",      stamina);
        nbt.putFloat (name.name()+"_staminaRegen", staminaRegen);
        nbt.putFloat (name.name()+"_healRegen",    healRegen);
        nbt.putString(name.name()+"_head",         head.getUniqueID());
        nbt.putString(name.name()+"_chest",        chest.getUniqueID());
        nbt.putString(name.name()+"_legs",         legs.getUniqueID());
        nbt.putString(name.name()+"_boots",        boots.getUniqueID());
        nbt.putString(name.name()+"_slot1",        slot1.getUniqueID());
        nbt.putString(name.name()+"_slot2",        slot2.getUniqueID());
        nbt.putString(name.name()+"_slot3",        slot3.getUniqueID());

        nbt.put( name.name()+"_path", NbtString.of(pathID.getPath().name()));
        nbt.putString("name", name.name());// last line
    }
}
