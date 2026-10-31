package rw.modden.npc;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import rw.modden.Axorunelostworlds;

public class ModEntities {
    public static final EntityType<NpcEntity> NPC = Registry.register(Registries.ENTITY_TYPE,
            new Identifier(Axorunelostworlds.MOD_ID, "npc"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, NpcEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 1.8F))
                    .build());

    public static void initialize() {
        FabricDefaultAttributeRegistry.register(NPC, NpcEntity.createAttributes());
    }
}
