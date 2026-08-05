package rw.modden.characters;

import net.minecraft.nbt.NbtCompound;
import rw.modden.combat.path.Path;
import rw.modden.items.ABEquip;

public interface Character {
    public float getHealReserve();
    public int getStars();
    public float getStamina();
    public int getStrength();
    public float getStaminaRegen();
    public float getHealRegen();
    public int getDefence();
    public Path getPath();
    public CharacterName getName();
    public ABEquip getHead();
    public ABEquip getChest();
    public ABEquip getLegs();
    public ABEquip getBoots();
    public ABEquip getFirstSlot();
    public ABEquip getSecondSlot();
    public ABEquip getThirdSlot();

    public void setHealReserve(float value);
    public void setStars(int value);
    public void setStamina(float value);
    public void setStrength(int value);
    public void setPath(Path pathID);
    public void setStaminaRegen(float value);
    public void setHealRegen(float value);
    public void setDefence(int value);
    public void setHead(ABEquip armor);
    public void setChest(ABEquip armor);
    public void setLegs(ABEquip armor);
    public void setBoots(ABEquip armor);
    public void setFirstSlot(ABEquip item);
    public void setSecondSlot(ABEquip item);
    public void setThirdSlot(ABEquip item);

    public boolean hasItem(ABEquip item);
    public boolean hasItem(String uniqueID);
    public ABEquip getItem(String uniqueID);

    public void readFromNbt(NbtCompound nbt);
    public void writeToNbt(NbtCompound nbt);
}