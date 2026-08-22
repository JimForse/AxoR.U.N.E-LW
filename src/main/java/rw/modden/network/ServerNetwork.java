package rw.modden.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import rw.modden.Axorunelostworlds;

public class ServerNetwork {
    public static final Identifier BATTLE_PACKET_ID = Identifier.of(Axorunelostworlds.MOD_ID, "battle");
    public static void send(ServerPlayerEntity player, Identifier channelName, PacketByteBuf buf) {
        ServerPlayNetworking.send(player, channelName, buf);
    }
}
