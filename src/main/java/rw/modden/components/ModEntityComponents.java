package rw.modden.components;

import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.entity.player.PlayerEntity;

public class ModEntityComponents implements EntityComponentInitializer {
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(PlayerEntity.class, ModComponents.BATTLE_STATE, (BattleStateComponentImpl::new));
        registry.registerForPlayers(ModComponents.CHARACTERS, CharactersComponentImpl::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(ModComponents.EQUIPMENT, EquipmentComponentImpl::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}
