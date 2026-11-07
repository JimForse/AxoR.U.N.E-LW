package rw.modden.combat;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import rw.modden.Axorunelostworlds;
import rw.modden.characters.Character;
import rw.modden.components.BattleStateComponent;
import rw.modden.components.ModComponents;
import rw.modden.items.ABEquip;

public final class BattleWeapon {
    public static final TagKey<Item> PAIRED = TagKey.of(RegistryKeys.ITEM, new Identifier(Axorunelostworlds.MOD_ID, "paired"));

    private BattleWeapon() {}

    public static void equip(ServerPlayerEntity player, Character character) {
        BattleStateComponent state = ModComponents.BATTLE_STATE.get(player);
        if (state.getSavedSlot()<0) {
            state.setSavedSlot(player.getInventory().selectedSlot);
            state.setSavedMainHand(player.getMainHandStack().copy());
            state.setSavedOffHand(player.getOffHandStack().copy());
        }
        ItemStack weapon = create(character);
        player.setStackInHand(Hand.MAIN_HAND, weapon);
        player.setStackInHand(Hand.OFF_HAND, weapon.isIn(PAIRED)?weapon.copy():ItemStack.EMPTY);
    }

    public static void unequip(ServerPlayerEntity player) {
        BattleStateComponent state = ModComponents.BATTLE_STATE.get(player);
        if (state.getSavedSlot()<0) return;
        player.getInventory().setStack(state.getSavedSlot(), state.getSavedMainHand());
        player.getInventory().setStack(PlayerInventory.OFF_HAND_SLOT, state.getSavedOffHand());
        state.setSavedSlot(-1);
        state.setSavedMainHand(ItemStack.EMPTY);
        state.setSavedOffHand(ItemStack.EMPTY);
    }

    private static ItemStack create(Character character) {
        ABEquip equip = character == null?null:character.getWeapon();
        Identifier id = equip == null?null:Identifier.tryParse(equip.getItemID());
        Item item = id == null?Items.AIR:Registries.ITEM.get(id);
        if (item == Items.AIR) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateNbt().putBoolean("Unbreakable", true);
        return stack;
    }
}
