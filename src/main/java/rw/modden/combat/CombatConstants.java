package rw.modden.combat;

public final class CombatConstants {
    private CombatConstants() {}

    public static final float DASH_STAMINA_COST = 8.0F;       // стоимость рывка
    public static final float DOUBLE_JUMP_STAMINA_COST = 6.0F;// стоимость двойного прыжка
    public static final int DASH_STAMINA_COOLDOWN_TICKS = 40; // время на кулдаун рывка
    public static final float STUN_MAX = 10.0F;               // максимум шкалы оглушения
    public static final float STUN_PER_HIT = 2.0F;            // сколько даёт один удар
    public static final int STUN_DURATION_TICKS = 100;        // 5 секунд (20 тиков = 1с)
    public static final float STUN_DAMAGE_MULTIPLIER = 1.25F; // урон по оглушённому
    public static final int ASSIST_POINTS_MAX = 7;            // очки помощи (2 круга по 3 + 1)
    public static final int ENERGY_STAGE_1 = 100;  // "выстрел"
    public static final int ENERGY_STAGE_2 = 200;  // "взрыв"
    public static final int ENERGY_MAX = 300;      // ульта на Q
    public static final int ENERGY_PER_HIT = 10;   //
    public static final int ESSENCE_MAX = 100;                          // сколько нужно под ульту
    public static final int ESSENCE_PER_HIT = 2;                        // сколько даёт сработавший удар
    public static final float ESSENCE_CHANCE = 0.6F;                    // шанс, что удар даст эссенцию (60%)
    public static final int ESSENCE_ENERGY_THRESHOLD = ENERGY_STAGE_1;  // с какой энергии начинает копиться
}