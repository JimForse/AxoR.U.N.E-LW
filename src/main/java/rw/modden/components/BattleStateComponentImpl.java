package rw.modden.components;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import rw.modden.combat.CombatState;

public class BattleStateComponentImpl implements BattleStateComponent {
    private CombatState state = CombatState.NONE;
    private final PlayerEntity player;

    public BattleStateComponentImpl(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public CombatState getState() {
        return state;
    }
    @Override
    public void setState(CombatState state) {
        this.state = state;
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        setState(CombatState.valueOf(nbt.getString("battle_state")));
    }
    @Override
    public void writeToNbt(NbtCompound nbt) {
        nbt.putString("battle_state", state.name());
    }
}
