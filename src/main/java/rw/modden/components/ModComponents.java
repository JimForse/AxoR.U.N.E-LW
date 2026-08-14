package rw.modden.components;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import net.minecraft.util.Identifier;
import rw.modden.Axorunelostworlds;

public class ModComponents {
    public static final ComponentKey<BattleStateComponent> BATTLE_STATE =
        ComponentRegistryV3.INSTANCE.getOrCreate(
                new Identifier(Axorunelostworlds.MOD_ID, "battle_state"),
                BattleStateComponent.class
        );
    public static final ComponentKey<CharactersComponent> CHARACTERS =
        ComponentRegistryV3.INSTANCE.getOrCreate(
                new Identifier(Axorunelostworlds.MOD_ID, "characters"),
                CharactersComponent.class
        );
    public static final ComponentKey<EquipmentComponent> EQUIPMENT =
        ComponentRegistryV3.INSTANCE.getOrCreate(
                new Identifier(Axorunelostworlds.MOD_ID, "equipment"),
                EquipmentComponent.class
        );
}
