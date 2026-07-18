package rw.modden.components;

import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import net.minecraft.entity.player.PlayerEntity;

public class ModEntityComponents implements EntityComponentInitializer {
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(PlayerEntity.class, ModComponents.BATTLE_STATE, (player -> new BattleStateComponentImpl(player)));
    }
}
