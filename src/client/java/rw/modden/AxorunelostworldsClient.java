package rw.modden;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rw.modden.hud.CharacterGroupHud;
import rw.modden.hud.CharacterHead;
import rw.modden.network.ClientNetwork;
import rw.modden.npc.ModEntities;
import rw.modden.npc.NpcRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class AxorunelostworldsClient implements ClientModInitializer {
    public static final String MOD_ID = "axorunelostworlds";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Client on initialized");

        ClientNetwork.registerGlobalReceiver();
        ClientKeyList.initialize();
        BattleInputBlocker.initialize();
        CharacterGroupHud.register();
        CharacterHead.registerReload();
        EntityRendererRegistry.register(ModEntities.NPC, NpcRenderer::new);
    }
}
