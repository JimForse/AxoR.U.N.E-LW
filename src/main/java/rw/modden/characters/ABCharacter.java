package rw.modden.characters;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import rw.modden.combat.path.Path;
import rw.modden.combat.path.PathFactory;
import rw.modden.combat.path.PathesName;

public abstract class ABCharacter implements Character {
    private int stars, strength, defence;
    private float healReserve, staminaRegen, stamina, healRegen;
    private Path pathID;
    private CharacterName name;
//    private ArmorsEnum head, chest, legs, boots;
//    private ItemsEnum slot1, slot2, slot3;

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
//    @Override
//    public ArmorsEnum getHead() {
//        return head;
//    }
//    @Override
//    public ArmorsEnum getChest() {
//        return chest;
//    }
//    @Override
//    public ArmorsEnum getLegs() {
//        return legs;
//    }
//    @Override
//    public ArmorsEnum getBoots() {
//        return boots;
//    }
//    @Override
//    public ItemsEnum getFirstSlot() {
//        return slot1;
//    }
//    @Override
//    public ItemsEnum getSecondSlot() {
//        return slot2;
//    }
//    @Override
//    public ItemsEnum getThirdSlot() {
//        return slot3;
//    }

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
//    @Override
//    public void setHead(ArmorsEnum armor) {
//        this.head = armor;
//    }
//    @Override
//    public void setChest(ArmorsEnum armor) {
//        this.chest = armor;
//    }
//    @Override
//    public void setLegs(ArmorsEnum armor) {
//        this.legs = armor;
//    }
//    @Override
//    public void setBoots(ArmorsEnum armor) {
//        this.boots = armor;
//    }
//    @Override
//    public void setFirstSlot(ItemsEnum item) {
//        this.slot1 = item;
//    }
//    public void setSecondSlot(ItemsEnum item) {
//        this.slot2 = item;
//    }
//    public void setThirdSlot(ItemsEnum item) {
//        this.slot3 = item;
//    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        this.name = CharacterName.valueOf(nbt.getString("name"));
        this.healReserve = nbt.getFloat(name.name() + "_healReserve");
        this.stars = nbt.getInt(name.name() + "_stars");
        this.stamina = nbt.getFloat(name.name() + "_stamina");
        this.strength = nbt.getInt(name.name() + "_strength");
        this.staminaRegen = nbt.getFloat(name.name() + "_staminaRegen");
        this.healRegen = nbt.getFloat(name.name() + "_healRegen");
        this.defence = nbt.getInt(name.name() + "_defence");
        this.pathID = PathFactory.get(
                PathesName.valueOf(nbt.getString(name.name() + "_path"))
        );
//        this.head = ArmorsEnum.valueOf(nbt.getString(name.name()+"_head"));
//        this.chest = ArmorsEnum.valueOf(nbt.getString(name.name()+"_chest"));
//        this.legs = ArmorsEnum.valueOf(nbt.getString(name.name()+"_legs"));
//        this.boots = ArmorsEnum.valueOf(nbt.getString(name.name()+"_boots"));
//        this.slot1 = ItemsEnum.valueOf(nbt.getString(name.name()+"_slot1"));
//        this.slot2 = ItemsEnum.valueOf(nbt.getString(name.name()+"_slot2"));
//        this.slot3 = ItemsEnum.valueOf(nbt.getString(name.name()+"_slot3"));
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        nbt.putFloat(name.name()+"_healReserve", healReserve);
        nbt.putInt(name.name()+"_stars", stars);
        nbt.putFloat(name.name()+"_stamina", stamina);
        nbt.putInt(name.name()+"_strength", strength);
        nbt.putFloat(name.name()+"_staminaRegen", staminaRegen);
        nbt.putFloat(name.name()+"_healRegen", healRegen);
        nbt.putInt(name.name()+"_defence", defence);
        nbt.put(name.name()+"_path", NbtString.of(pathID.getPath().name()));
//        nbt.putString(name.name()+"_head", head.name());
//        nbt.putString(name.name()+"_chest", chest.name());
//        nbt.putString(name.name()+"_legs", legs.name());
//        nbt.putString(name.name()+"_boots", boots.name());
//        nbt.putString(name.name()+"_slot1", slot1.name());
//        nbt.putString(name.name()+"_slot2", slot2.name());
//        nbt.putString(name.name()+"_slot3", slot3.name());
        nbt.putString("name", name.name());
    }
}
