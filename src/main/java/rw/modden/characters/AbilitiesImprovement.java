package rw.modden.characters;

import net.minecraft.nbt.NbtCompound;

public class AbilitiesImprovement {
    private int stars, strength, defence;
    private float staminaRegen, healRegen, heal, stamina;
    private final Character character;
    private Runnable onChanged;

    public AbilitiesImprovement(Character character) {
        this.character = character;
    }

    public void upHeal(float value) {
        this.heal = character.getHealReserve() + value;
        onChanged.run();
    }

    public void upStars(int value) {
        this.stars = character.getStars() + value;
        onChanged.run();
    }
    public void upStamina(float value) {
        this.stamina = character.getStamina() + value;
        onChanged.run();
    }
    public void upStrength(int value) {
        this.strength = character.getStrength() + value;
        onChanged.run();
    }
    public void upStaminaRegen(float value) {
        this.staminaRegen = character.getStaminaRegen() + value;
        onChanged.run();
    }
    public void upHealRegen(float value) {
        this.healRegen = character.getHealRegen() + value;
        onChanged.run();
    }
    public void upDefence(int value) {
        this.defence = character.getDefence() + value;
        onChanged.run();
    }

    public void readFromNbt(NbtCompound nbt) {
        CharacterName name = character.getName();
        this.heal = nbt.getFloat(name.name()+"_heal");
        this.stars = nbt.getInt(name.name()+"_stars");
        this.stamina = nbt.getFloat(name.name()+"_stamina");
        this.strength = nbt.getInt(name.name()+"_strength");
        this.staminaRegen = nbt.getFloat(name.name()+"_staminaRegen");
        this.healRegen = nbt.getFloat(name.name() + "_healRegen");
        this.defence = nbt.getInt(name.name()+"_defence");
        this.healRegen = nbt.getFloat(name.name()+"_healRegen");
    }
    public void writeToNbt(NbtCompound nbt) {
        CharacterName name = character.getName();
        nbt.putFloat(name.name()+"_heal", heal);
        nbt.putInt(name.name()+"_stars", stars);
        nbt.putFloat(name.name()+"_stamina", stamina);
        nbt.putInt(name.name()+"_strength", strength);
        nbt.putFloat(name.name()+"_staminaRegen", staminaRegen);
        nbt.putFloat(name.name()+"_healRegen", healRegen);
        nbt.putInt(name.name()+"_defence", defence);
        nbt.putFloat(name.name()+"_healRegen", healRegen);
    }
}
