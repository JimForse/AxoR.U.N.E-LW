package rw.modden.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ClientNetwork {
    private static boolean battle;

    public static void registerGlobalReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(ServerNetwork.BATTLE_PACKET_ID, ((client, handler, buf, responseSender) -> {
            boolean battle = buf.readBoolean();
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
