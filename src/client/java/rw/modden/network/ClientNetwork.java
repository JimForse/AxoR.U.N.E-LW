package rw.modden.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import rw.modden.Axorunelostworlds;
import rw.modden.AxorunelostworldsClient;

public class ClientNetwork {
    private static boolean battle;
    private static String battle_state;
    public static final Identifier CHARACTER_SWITCH_ID = Identifier.of(Axorunelostworlds.MOD_ID, "character_switch");
    public static final Identifier BATTLE_PACKET_ID = Identifier.of(Axorunelostworlds.MOD_ID, "battle");
    public static final Identifier BATTLE_STATE_PACKET_ID = Identifier.of(Axorunelostworlds.MOD_ID, "battle_state");

    public static void registerGlobalReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(BATTLE_PACKET_ID, ((client, handler, buf, responseSender) -> {
            boolean battle = buf.readBoolean();
            ClientNetwork.setBattle(battle);
        }) );
        ClientPlayNetworking.registerGlobalReceiver(BATTLE_STATE_PACKET_ID, ((client, handler, buf, responseSender) -> {
            String battle_state = buf.readString();
            if (battle_state!=null) ClientNetwork.setBattleState(battle_state);
            else ClientNetwork.setBattleState("NONE");
        }) );
    }

    public static void send(Identifier channelName, PacketByteBuf buf) {
        ClientPlayNetworking.send(channelName, buf);
    }

    public static void setBattle(boolean b) {
        battle = b;
    }
    public static boolean getBattle() {
        return battle;
    }

    public static void setBattleState(String s) {
        battle_state = s;
    }
    public static String getBattleState() {
        return battle_state;
    }
}
