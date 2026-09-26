package rw.modden.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import rw.modden.Axorunelostworlds;
import rw.modden.components.ModComponents;

public class ServerNetwork {
    public static final Identifier CHARACTER_SWITCH_ID = Identifier.of(Axorunelostworlds.MOD_ID, "character_switch");
    public static final Identifier BATTLE_PACKET_ID = Identifier.of(Axorunelostworlds.MOD_ID, "battle");
    public static final Identifier BATTLE_STATE_PACKET_ID = Identifier.of(Axorunelostworlds.MOD_ID, "battle_state");
    public static final Identifier STAMINA_PACKET_ID = Identifier.of(Axorunelostworlds.MOD_ID, "stamina");

    public static void send(ServerPlayerEntity player, Identifier channelName, PacketByteBuf buf) {
        ServerPlayNetworking.send(player, channelName, buf);
    }
    public static void registerGlobalRecevier() {
        ServerPlayNetworking.registerGlobalReceiver(CHARACTER_SWITCH_ID, (server, player, handler, buf, responseSender) -> {
            if (buf.readBoolean()) {
                ModComponents.CHARACTERS.get(player).switcher();
            }
        });
    }
}
