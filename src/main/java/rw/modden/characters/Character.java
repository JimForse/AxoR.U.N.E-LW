package rw.modden.characters;

import net.minecraft.nbt.NbtCompound;
import rw.modden.combat.path.Path;
import rw.modden.items.ArmorsEnum;
import rw.modden.items.ItemsEnum;

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
    public ArmorsEnum getHead();
    public ArmorsEnum getChest();
    public ArmorsEnum getLegs();
    public ArmorsEnum getBoots();
    public ItemsEnum getFirstSlot();
    public ItemsEnum getSecondSlot();
    public ItemsEnum getThirdSlot();

    public void setHealReserve(float value);
    public void setStars(int value);
    public void setStamina(float value);
    public void setStrength(int value);
    public void setPath(Path pathID);
    public void setStaminaRegen(float value);
    public void setHealRegen(float value);
    public void setDefence(int value);
    public void setHead(ArmorsEnum armor);
    public void setChest(ArmorsEnum armor);
    public void setLegs(ArmorsEnum armor);
    public void setBoots(ArmorsEnum armor);
    public void setFirstSlot(ItemsEnum item);
    public void setSecondSlot(ItemsEnum item);
    public void setThirdSlot(ItemsEnum item);

    public void readFromNbt(NbtCompound nbt);
    public void writeToNbt(NbtCompound nbt);
}