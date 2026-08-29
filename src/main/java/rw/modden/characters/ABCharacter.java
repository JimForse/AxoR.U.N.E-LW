package rw.modden.characters;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import rw.modden.Axorunelostworlds;
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
    private ABEquip head, chest, legs, boots, slot1, slot2, slot3, weapon;
    private final Map<String, ABEquip> equipMap = new HashMap<>();

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
    public ABEquip getWeapon() {
        return weapon;
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
        if (this.head != null) {
            if (Axorunelostworlds.permitted_equipment.contains(armor.getItemID())) {
                removeItem(this.head);
                this.head = armor;
                addItem(armor);
            }
        }
    }
    @Override
    public void setChest(ABEquip armor) {
        if (this.chest != null) {
            if (Axorunelostworlds.permitted_equipment.contains(armor.getItemID())) {
                removeItem(this.chest);
                this.chest = armor;
                addItem(armor);
            }
        }
    }
    @Override
    public void setLegs(ABEquip armor) {
        if (this.legs != null) {
            if (Axorunelostworlds.permitted_equipment.contains(armor.getItemID())) {
                removeItem(this.legs);
                this.legs = armor;
                addItem(armor);
            }
        }
    }
    @Override
    public void setBoots(ABEquip armor) {
        if (this.boots != null) {
            if (Axorunelostworlds.permitted_equipment.contains(armor.getItemID())) {
                removeItem(this.boots);
                this.boots = armor;
                addItem(armor);
            }
        }
    }
    @Override
    public void setFirstSlot(ABEquip item) {
        if (this.slot1 != null) {
            if (Axorunelostworlds.permitted_equipment.contains(item.getItemID())) {
                removeItem(this.slot1);
                this.slot1 = item;
                addItem(item);
            }
        }
    }
    @Override
    public void setSecondSlot(ABEquip item) {
        if (this.slot2 != null) {
            if (Axorunelostworlds.permitted_equipment.contains(item.getItemID())) {
                removeItem(this.slot2);
                this.slot2 = item;
                addItem(item);
            }
        }
    }
    @Override
    public void setThirdSlot(ABEquip item) {
        if (this.slot3 != null) {
            if (Axorunelostworlds.permitted_equipment.contains(item.getItemID())) {
                removeItem(this.slot3);
                this.slot3 = item;
                addItem(item);
            }
        }
    }
    @Override
    public void setWeapon(ABEquip item) {
        if (this.weapon != null) {
            if (Axorunelostworlds.permitted_equipment.contains(item.getItemID())) {
                removeItem(this.weapon);
                this.weapon = item;
                addItem(weapon);
            }
        }
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
    public float getAllHealReserveBonus() {
        float bonus = 0.0F;
        if (head  != null) bonus += head.getHealReserveBonus();
        if (chest != null) bonus += chest.getHealReserveBonus();
        if (legs  != null) bonus += legs.getHealReserveBonus();
        if (boots != null) bonus += boots.getHealReserveBonus();
        if (slot1 != null) bonus += slot1.getHealReserveBonus();
        if (slot2 != null) bonus += slot2.getHealReserveBonus();
        if (slot3 != null) bonus += slot3.getHealReserveBonus();
        return bonus;
    }
    @Override
    public float getAllHealRegenBonus() {
        float bonus = 0.0F;
        if (head  != null) bonus += head.getHealRegenBonus();
        if (chest != null) bonus += chest.getHealRegenBonus();
        if (legs  != null) bonus += legs.getHealRegenBonus();
        if (boots != null) bonus += boots.getHealRegenBonus();
        if (slot1 != null) bonus += slot1.getHealRegenBonus();
        if (slot2 != null) bonus += slot2.getHealRegenBonus();
        if (slot3 != null) bonus += slot3.getHealRegenBonus();
        return bonus;
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
        ArrayList<String> slots   = new ArrayList<>();

        slots.add(nbt.getString(name.name() + "_head"));
        slots.add(nbt.getString(name.name() + "_chest"));
        slots.add(nbt.getString(name.name() + "_legs"));
        slots.add(nbt.getString(name.name() + "_boots"));
        slots.add(nbt.getString(name.name() + "_slot1"));
        slots.add(nbt.getString(name.name() + "_slot2"));
        slots.add(nbt.getString(name.name() + "_slot3"));
        slots.add(nbt.getString(name.name() + "_weapon"));

        for (int i = 0; i < slots.size(); i++) {
            uniqueID = slots.get(i);
            if (uniqueID.isEmpty()) continue;
            itemID = nbt.getString("item_id" + uniqueID);

            switch (i) {
                case 0 -> this.head   = new ABEquip(uniqueID,itemID);
                case 1 -> this.chest  = new ABEquip(uniqueID,itemID);
                case 2 -> this.legs   = new ABEquip(uniqueID,itemID);
                case 3 -> this.boots  = new ABEquip(uniqueID,itemID);
                case 4 -> this.slot1  = new ABEquip(uniqueID,itemID);
                case 5 -> this.slot2  = new ABEquip(uniqueID,itemID);
                case 6 -> this.slot3  = new ABEquip(uniqueID,itemID);
                case 7 -> this.weapon = new ABEquip(uniqueID,itemID);
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

        try {
            nbt.putString(name.name() + "_head", head.getUniqueID());
            nbt.putString(name.name() + "_chest", chest.getUniqueID());
            nbt.putString(name.name() + "_legs", legs.getUniqueID());
            nbt.putString(name.name() + "_boots", boots.getUniqueID());
            nbt.putString(name.name() + "_slot1", slot1.getUniqueID());
            nbt.putString(name.name() + "_slot2", slot2.getUniqueID());
            nbt.putString(name.name() + "_slot3", slot3.getUniqueID());
            nbt.putString(name.name() + "_weapon", weapon.getUniqueID());

            head.writeToNbt(nbt);
            chest.writeToNbt(nbt);
            legs.writeToNbt(nbt);
            boots.writeToNbt(nbt);
            slot1.writeToNbt(nbt);
            slot2.writeToNbt(nbt);
            slot3.writeToNbt(nbt);
            weapon.writeToNbt(nbt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        nbt.put( name.name()+"_path", NbtString.of(pathID.getPath().name()));
        nbt.putString("name", name.name());// last line
    }
}
