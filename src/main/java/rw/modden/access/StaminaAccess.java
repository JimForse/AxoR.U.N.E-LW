package rw.modden.access;

public interface StaminaAccess {
    float axorune$getCurrentStamina();
    boolean axorune$trySpendStamina(float cost);
    boolean axorune$trySpendDashStamina(float cost, int cooldownTicks);
}