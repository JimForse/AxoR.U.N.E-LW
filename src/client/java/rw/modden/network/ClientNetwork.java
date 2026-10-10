package rw.modden.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import rw.modden.Axorunelostworlds;
import rw.modden.AxorunelostworldsClient;

import java.util.ArrayList;
import java.util.List;

public class ClientNetwork {
    private static boolean battle;
    private static String battle_state;
    private static float stamina;
    private static float maxStamina;
    private static volatile List<GroupMember> group = new ArrayList<>();
    private static volatile int groupCurrent = 0;
    public static final Identifier CHARACTER_SWITCH_ID = Identifier.of(Axorunelostworlds.MOD_ID, "character_switch");
    public static final Identifier BATTLE_PACKET_ID = Identifier.of(Axorunelostworlds.MOD_ID, "battle");
    public static final Identifier BATTLE_STATE_PACKET_ID = Identifier.of(Axorunelostworlds.MOD_ID, "battle_state");
    public static final Identifier STAMINA_PACKET_ID = Identifier.of(Axorunelostworlds.MOD_ID, "stamina");
    public static final Identifier GROUP_PACKET_ID = Identifier.of(Axorunelostworlds.MOD_ID, "group");

    public static void registerGlobalReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(BATTLE_PACKET_ID, ((client, handler, buf, responseSender) -> {
            boolean battle = buf.readBoolean();
            ClientNetwork.setBattle(battle);
        }));
        ClientPlayNetworking.registerGlobalReceiver(BATTLE_STATE_PACKET_ID, ((client, handler, buf, responseSender) -> {
            String battle_state = buf.readString();
            if (battle_state!=null) ClientNetwork.setBattleState(battle_state);
            else ClientNetwork.setBattleState("NONE");
        }));
        ClientPlayNetworking.registerGlobalReceiver(STAMINA_PACKET_ID, ((client, handler, buf, responseSender) -> {
            float current = buf.readFloat();
            float max = buf.readFloat();
            ClientNetwork.setStamina(current);
            ClientNetwork.setMaxStamina(max);
        }));
        ClientPlayNetworking.registerGlobalReceiver(GROUP_PACKET_ID, ((client, handler, buf, responseSender) -> {
            int size = buf.readInt();
            List<GroupMember> members = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                members.add(new GroupMember(buf.readString(), buf.readFloat(), buf.readFloat()));
            }
            int current = buf.readInt();

            client.execute(() -> {
                ClientNetwork.group = members;
                ClientNetwork.groupCurrent = current;
            });
        }));

    }

    public static void send(Identifier channelName, PacketByteBuf buf) {
        ClientPlayNetworking.send(channelName, buf);
    }

    public record GroupMember(String name, float hp, float maxHp) {}

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

    public static void setStamina(float s) {
        stamina = s;
    }
    public static float getStamina() {
        return stamina;
    }

    public static void setMaxStamina(float s) {
        maxStamina = s;
    }
    public static float getMaxStamina() {
        return maxStamina;
    }
    public static List<GroupMember> getGroup() {
        return group;
    }
    public static int getGroupCurrent() {
        return groupCurrent;
    }
}
