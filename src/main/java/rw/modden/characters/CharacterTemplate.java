package rw.modden.characters;

import rw.modden.combat.path.PathesName;

import java.util.concurrent.ThreadLocalRandom;

public record CharacterTemplate(PathesName path, Stats start, Stats max, String weapon, int stars, int level) {
    public static final Stats DEFAULT_START = new Stats(100.0F, 100.0F, 1.0F, 10.0F, 0.01F, 0.01F),
            DEFAULT_MAX = new Stats(500.0F, 1000.0F, 10.0F, 100.0F, 0.5F, 0.5F);
    public static final String[] STATS = {"healReserve", "stamina", "strength", "defence", "staminaRegen", "healRegen"};

    public record Stats(float healReserve, float stamina, float strength, float defence, float staminaRegen, float healRegen) {
        public Float get(String stat) {
            return switch (stat) {
                case "healReserve" -> healReserve;
                case "stamina" -> stamina;
                case "strength" -> strength;
                case "defence" -> defence;
                case "staminaRegen" -> staminaRegen;
                case "healRegen" -> healRegen;
                default -> null;
            };
        }

        public Stats with(String stat, float value) {
            return new Stats(
                    stat.equals("healReserve")?value:healReserve,
                    stat.equals("stamina")?value:stamina,
                    stat.equals("strength")?value:strength,
                    stat.equals("defence")?value:defence,
                    stat.equals("staminaRegen")?value:staminaRegen,
                    stat.equals("healRegen")?value:healRegen);
        }
    }

    public static CharacterTemplate create(PathesName path) { return new CharacterTemplate(path, DEFAULT_START, DEFAULT_MAX, null, 0, 1); }
    public static PathesName randomPath() { return PathesName.values()[ThreadLocalRandom.current().nextInt(PathesName.values().length)]; }

    public Stats startOrDefault() { return start == null?DEFAULT_START:start; }
    public Stats maxOrDefault() { return max == null?DEFAULT_MAX:max; }
    public PathesName pathOrDefault() { return path == null?PathesName.CREATE:path; }
    public int starsOrDefault() { return stars; }
    public int levelOrDefault() { return level<=0?1:level; }

    public int ascension() {
        int needed = (levelOrDefault() + AbilitiesImprovement.LEVELS_PER_ASCENSION - 1) / AbilitiesImprovement.LEVELS_PER_ASCENSION;
        return Math.max(1, Math.min(AbilitiesImprovement.MAX_ASCENSION, needed));
    }

    public CharacterTemplate withPath(PathesName value) { return new CharacterTemplate(value, start, max, weapon, stars, level); }
    public CharacterTemplate withStart(Stats value) { return new CharacterTemplate(path, value, max, weapon, stars, level); }
    public CharacterTemplate withMax(Stats value) { return new CharacterTemplate(path, start, value, weapon, stars, level); }
    public CharacterTemplate withWeapon(String value) { return new CharacterTemplate(path, start, max, value, stars, level); }
    public CharacterTemplate withStars(int value) { return new CharacterTemplate(path, start, max, weapon, value, level); }
    public CharacterTemplate withLevel(int value) { return new CharacterTemplate(path, start, max, weapon, stars, value); }
}
