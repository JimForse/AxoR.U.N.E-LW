package rw.modden.characters;

import net.minecraft.nbt.NbtCompound;

public class AbilitiesImprovement {
    private int stars, stamina, strength, defence;
    private float staminaRegen, healRegen, heal;
    private CharacterName name;
    private Runnable onChanged;

    public void upHeal(CharacterName name, float value) {
        this.name = name;
        onChanged.run();
        this.heal += value;
        onChanged.run();
    }

    public void upStars(CharacterName name, int value) {
        this.name = name;
        onChanged.run();
        this.stars += value;
        onChanged.run();
    }
    public void upStamina(CharacterName name, int value) {
        this.name = name;
        onChanged.run();
        this.stamina += value;
        onChanged.run();
    }
    public void upStrength(CharacterName name, int value) {
        this.name = name;
        onChanged.run();
        this.strength += value;
        onChanged.run();
    }
    public void upStaminaRegen(CharacterName name, float value) {
        this.name = name;
        onChanged.run();
        this.staminaRegen += value;
        onChanged.run();
    }
    public void upHealRegen(CharacterName name, float value) {
        this.name = name;
        onChanged.run();
        this.healRegen += value;
        onChanged.run();
    }
    public void upDefence(CharacterName name, int value) {
        this.name = name;
        onChanged.run();
        this.defence += value;
        onChanged.run();
    }

    public void readFromNbt(NbtCompound nbt) {
        this.name = CharacterName.valueOf(nbt.getString("name"));
        this.heal = nbt.getFloat(name.name()+"_heal");
        this.stars = nbt.getInt(name.name()+"_stars");
        this.stamina = nbt.getInt(name.name()+"_stamina");
        this.strength = nbt.getInt(name.name()+"_strength");
        this.staminaRegen = nbt.getFloat(name.name()+"_staminaRegen");
        this.healRegen = nbt.getFloat(name.name() + "_healRegen");
        this.defence = nbt.getInt(name.name()+"_defence");
        this.healRegen = nbt.getFloat(name.name()+"_healRegen");
    }
    public void writeToNbt(NbtCompound nbt) {
        nbt.putFloat(name.name()+"_heal", heal);
        nbt.putInt(name.name()+"_stars", stars);
        nbt.putInt(name.name()+"_stamina", stamina);
        nbt.putInt(name.name()+"_strength", strength);
        nbt.putFloat(name.name()+"_staminaRegen", staminaRegen);
        nbt.putFloat(name.name()+"_healRegen", healRegen);
        nbt.putInt(name.name()+"_defence", defence);
        nbt.putFloat(name.name()+"_healRegen", healRegen);
        nbt.putString("name", name.name());
    }
}
