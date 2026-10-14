package rw.modden.components;

import rw.modden.combat.CombatState;
import dev.onyxstudios.cca.api.v3.component.Component;

public interface BattleStateComponent extends Component {
    CombatState getState();
    void setState(CombatState state);
    float getPreBattleHealth();
    void setPreBattleHealth(float health);
    int getAssistPoints();
    void setAssistPoints(int points);
}
