package rw.modden.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import rw.modden.Axorunelostworlds;
import rw.modden.combat.CombatState;
import rw.modden.characters.Character;
import rw.modden.characters.CharacterName;
import rw.modden.components.CharactersComponent;
import rw.modden.components.ModComponents;

import java.util.ArrayList;

public class ServerNetwork {
    public static final Identifier CHARACTER_SWITCH_ID = Identifier.of(Axorunelostworlds.MOD_ID, "character_switch");
    public static final Identifier BATTLE_PACKET_ID = Identifier.of(Axorunelostworlds.MOD_ID, "battle");
    public static final Identifier BATTLE_STATE_PACKET_ID = Identifier.of(Axorunelostworlds.MOD_ID, "battle_state");
    public static final Identifier STAMINA_PACKET_ID = Identifier.of(Axorunelostworlds.MOD_ID, "stamina");
    public static final Identifier GROUP_PACKET_ID = Identifier.of(Axorunelostworlds.MOD_ID, "group");

    public static void send(ServerPlayerEntity player, Identifier channelName, PacketByteBuf buf) {
        ServerPlayNetworking.send(player, channelName, buf);
    }
    public static void registerGlobalRecevier() {
        ServerPlayNetworking.registerGlobalReceiver(CHARACTER_SWITCH_ID, (server, player, handler, buf, responseSender) -> {
            boolean pressed = buf.readBoolean();
            server.execute(() -> {
                if (pressed) {
                    ModComponents.CHARACTERS.get(player).switcher();
                }
            });
        });
    }

    public static void sendGroup(ServerPlayerEntity player) {
        CharactersComponent component = ModComponents.CHARACTERS.get(player);
        CharacterName current = component.getCurrentCharacter();

        ArrayList<CharacterName> group = component.getCurrentGroup();
        boolean isEvent = ModComponents.BATTLE_STATE.get(player).getState() == CombatState.EVENT;

        if (isEvent || group == null || (current != null && !group.contains(current))) {
            group = new ArrayList<>();
            if (current != null) group.add(current);
        }

        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(group.size());

        int currentIndex = 0;
        for (int i = 0; i < group.size(); i++) {
            CharacterName name = group.get(i);
            if (name == current) currentIndex = i;

            Character character = component.hasCharacter(name)?component.getCharacter(name):null;
            float max = character != null ? character.getHealReserve():0.0F;
            float hp = (character != null && character.isActivatedThisBattle())
                    ? Math.min(character.getCurrentHeal(), max):max;

            buf.writeString(name.name());
            buf.writeFloat(hp);
            buf.writeFloat(max);
        }
        buf.writeInt(currentIndex);

        send(player, GROUP_PACKET_ID, buf);
    }
}
