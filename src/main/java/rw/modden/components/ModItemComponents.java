package rw.modden.components;

import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.item.ItemComponentInitializer;
import net.minecraft.entity.player.PlayerEntity;

public class ModItemComponents implements ItemComponentInitializer {
    @Override
    public void registerItemComponentFactories(ItemComponentFactoryRegistry registry) {
//        registry.register();
        // TODO: сделать чтобы можно было взаимодействовать со СПИСКОМ предметов из json, учитывая порядок активации в onInitialize()
    }
}
