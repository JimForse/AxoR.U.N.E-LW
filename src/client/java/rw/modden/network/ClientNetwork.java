package rw.modden.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;
import rw.modden.Axorunelostworlds;
import rw.modden.AxorunelostworldsClient;

public class ClientNetwork {
    private static boolean battle;
    public static final Identifier BATTLE_PACKET_ID = Identifier.of(Axorunelostworlds.MOD_ID, "battle");

    public static void registerGlobalReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(BATTLE_PACKET_ID, ((client, handler, buf, responseSender) -> {
            boolean battle = buf.readBoolean();
            AxorunelostworldsClient.LOGGER.info("CLIENT: received battle = {}", battle);
            ClientNetwork.setBattle(battle);
        }) );
    }

    public static void setBattle(boolean b) {
        battle = b;
    }
    public static boolean getBattle() {
        return battle;
    }
}
