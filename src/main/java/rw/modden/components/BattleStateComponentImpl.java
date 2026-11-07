package rw.modden.components;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import rw.modden.combat.Battle;
import rw.modden.combat.CombatState;

public class BattleStateComponentImpl implements BattleStateComponent {
    private CombatState state = CombatState.NONE;
    private final PlayerEntity player;
    private float preBattleHealth = -1.0F;
    private int assistPoints = 0, savedSlot = -1;
    private ItemStack savedMainHand = ItemStack.EMPTY, savedOffHand = ItemStack.EMPTY;

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
    public float getPreBattleHealth() {
        return preBattleHealth;
    }
    @Override
    public void setPreBattleHealth(float health) {
        this.preBattleHealth = health;
    }
    @Override public int getAssistPoints() { return assistPoints; }
    @Override public void setAssistPoints(int points) { this.assistPoints = Math.max(0, points); }
    @Override public int getSavedSlot() { return savedSlot; }
    @Override public void setSavedSlot(int slot) { this.savedSlot = slot; }
    @Override public ItemStack getSavedMainHand() { return savedMainHand; }
    @Override public void setSavedMainHand(ItemStack stack) { this.savedMainHand = stack; }
    @Override public ItemStack getSavedOffHand() { return savedOffHand; }
    @Override public void setSavedOffHand(ItemStack stack) { this.savedOffHand = stack; }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        setState(CombatState.valueOf(nbt.getString("battle_state")));
        preBattleHealth = nbt.contains("pre_battle_health") ? nbt.getFloat("pre_battle_health"):-1.0F;
        assistPoints = nbt.getInt("assist_points");
        savedSlot = nbt.contains("saved_slot")?nbt.getInt("saved_slot"):-1;
        savedMainHand = ItemStack.fromNbt(nbt.getCompound("saved_main_hand"));
        savedOffHand = ItemStack.fromNbt(nbt.getCompound("saved_off_hand"));
    }
    @Override
    public void writeToNbt(NbtCompound nbt) {
        nbt.putString("battle_state", state.name());
        nbt.putFloat("pre_battle_health", preBattleHealth);
        nbt.putInt("assist_points", assistPoints);
        nbt.putInt("saved_slot", savedSlot);
        nbt.put("saved_main_hand", savedMainHand.writeNbt(new NbtCompound()));
        nbt.put("saved_off_hand", savedOffHand.writeNbt(new NbtCompound()));
    }
}
