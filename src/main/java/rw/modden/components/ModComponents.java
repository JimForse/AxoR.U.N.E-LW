package rw.modden.components;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.minecraft.util.Identifier;
import rw.modden.Axorunelostworlds;

public class ModComponents {
    public static final ComponentKey<BattleStateComponent> BATTLE_STATE =
        ComponentRegistry.getOrCreate(Identifier.of(Axorunelostworlds.MOD_ID, "battle_state"), BattleStateComponent.class);
    public static final ComponentKey<CharactersComponent> CHARACTERS =
        ComponentRegistry.getOrCreate(Identifier.of(Axorunelostworlds.MOD_ID, "characters"), CharactersComponent.class);
    public static final ComponentKey<EquipmentComponent> EQUIPMENT =
        ComponentRegistry.getOrCreate(Identifier.of(Axorunelostworlds.MOD_ID, "equipments"), EquipmentComponent.class);
}
