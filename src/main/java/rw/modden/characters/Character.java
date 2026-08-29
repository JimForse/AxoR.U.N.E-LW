package rw.modden.characters;

import net.minecraft.nbt.NbtCompound;
import rw.modden.combat.path.Path;
import rw.modden.items.ABEquip;

public interface Character {
    float getHealReserve();
    int getStars();
    float getStamina();
    int getStrength();
    float getStaminaRegen();
    float getHealRegen();
    int getDefence();
    Path getPath();
    CharacterName getName();
    ABEquip getHead();
    ABEquip getChest();
    ABEquip getLegs();
    ABEquip getBoots();
    ABEquip getFirstSlot();
    ABEquip getSecondSlot();
    ABEquip getThirdSlot();
    ABEquip getWeapon();

    void setHealReserve(float value);
    void setStars(int value);
    void setStamina(float value);
    void setStrength(int value);
    void setPath(Path pathID);
    void setStaminaRegen(float value);
    void setHealRegen(float value);
    void setDefence(int value);
    void setHead(ABEquip armor);
    void setChest(ABEquip armor);
    void setLegs(ABEquip armor);
    void setBoots(ABEquip armor);
    void setFirstSlot(ABEquip item);
    void setSecondSlot(ABEquip item);
    void setThirdSlot(ABEquip item);
    void setWeapon(ABEquip item);

    boolean hasItem(ABEquip item);
    boolean hasItem(String uniqueID);
    ABEquip getItem(String uniqueID);
    float getAllHealReserveBonus();
    float getAllHealRegenBonus();

    void readFromNbt(NbtCompound nbt);
    void writeToNbt(NbtCompound nbt);
}