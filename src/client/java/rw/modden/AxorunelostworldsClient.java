package rw.modden;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rw.modden.network.ClientNetwork;

public class AxorunelostworldsClient implements ModInitializer {
    public static final String MOD_ID = "axorunelostworlds";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ClientNetwork.registerGlobalReceiver();
    }
}
