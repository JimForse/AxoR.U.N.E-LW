package rw.modden;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rw.modden.network.ClientNetwork;

public class AxorunelostworldsClient implements ClientModInitializer {
    public static final String MOD_ID = "axorunelostworlds";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Client on initialized");

        ClientNetwork.registerGlobalReceiver();
        ClientKeyList.initialize();
    }
}
