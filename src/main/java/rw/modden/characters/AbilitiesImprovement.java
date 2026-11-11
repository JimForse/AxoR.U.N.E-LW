package rw.modden.characters;

public final class AbilitiesImprovement {
    public static final int LEVELS_PER_ASCENSION = 20, MAX_ASCENSION = 5;

    private AbilitiesImprovement() {}

    public static int maxLevel(int ascension) {
        return ascension*LEVELS_PER_ASCENSION;
    }

    public static void applyLevelChange(Character character, int diff) {
        if (diff == 0) return;
        CharacterTemplate template = CharacterTemplates.get(character.getName());
        CharacterTemplate.Stats min = template == null?CharacterTemplate.DEFAULT_START:template.startOrDefault(),
                max = template == null?CharacterTemplate.DEFAULT_MAX:template.maxOrDefault();
        character.setHealReserve(character.getHealReserve() + step(min.healReserve(), max.healReserve())*diff);
        character.setStamina(character.getStamina() + step(min.stamina(), max.stamina())*diff);
        character.setStrength(character.getStrength() + step(min.strength(), max.strength())*diff);
        character.setDefence(character.getDefence() + step(min.defence(), max.defence())*diff);
        character.setStaminaRegen(character.getStaminaRegen() + step(min.staminaRegen(), max.staminaRegen())*diff);
        character.setHealRegen(character.getHealRegen() + step(min.healRegen(), max.healRegen())*diff);
    }

    private static float step(float min, float max) {
        return (max - min) / (MAX_ASCENSION*LEVELS_PER_ASCENSION - 1);
    }
}
