package rw.modden.characters;

public final class AbilitiesImprovement {
    public static final int LEVELS_PER_ASCENSION = 20, MAX_ASCENSION = 5;
    private static final float MIN_HEAL_RESERVE = 100.0F, MIN_STAMINA = 100.0F, MIN_STRENGTH = 1.0F,
            MIN_DEFENCE = 10.0F, MIN_STAMINA_REGEN = 0.01F, MIN_HEAL_REGEN = 0.01F;
    private static final float MAX_HEAL_RESERVE = 500.0F, MAX_STAMINA = 1000.0F, MAX_STRENGTH = 10.0F,
            MAX_DEFENCE = 100.0F, MAX_STAMINA_REGEN = 0.5F, MAX_HEAL_REGEN = 0.5F;

    private AbilitiesImprovement() {}

    public static int maxLevel(int ascension) {
        return ascension*LEVELS_PER_ASCENSION;
    }

    public static void applyLevelChange(Character character, int diff) {
        character.setHealReserve(character.getHealReserve() + step(MIN_HEAL_RESERVE, MAX_HEAL_RESERVE)*diff);
        character.setStamina(character.getStamina() + step(MIN_STAMINA, MAX_STAMINA)*diff);
        character.setStrength(character.getStrength() + step(MIN_STRENGTH, MAX_STRENGTH)*diff);
        character.setDefence(character.getDefence() + step(MIN_DEFENCE, MAX_DEFENCE)*diff);
        character.setStaminaRegen(character.getStaminaRegen() + step(MIN_STAMINA_REGEN, MAX_STAMINA_REGEN)*diff);
        character.setHealRegen(character.getHealRegen() + step(MIN_HEAL_REGEN, MAX_HEAL_REGEN)*diff);
    }

    private static float step(float min, float max) {
        return (max - min) / (MAX_ASCENSION*LEVELS_PER_ASCENSION - 1);
    }
}
