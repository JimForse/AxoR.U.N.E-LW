package rw.modden;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import org.lwjgl.glfw.GLFW;
import rw.modden.combat.CombatState;
import rw.modden.network.ClientNetwork;

import java.util.List;

public class ClientKeyList {
    private static final KeyBinding characterSwitch = new KeyBinding(
            "Switch characters (battle)",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            "AxoRune");

    private static final KeyBinding strongAttack = new KeyBinding(
            "Strong attack (battle)",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_E,
            "AxoRune");

    private static final KeyBinding ultimate = new KeyBinding(
            "Ultimate (battle)",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_Q,
            "AxoRune");

    private static final List<KeyBinding> BATTLE_KEYS = List.of(characterSwitch, strongAttack, ultimate);

    private static final int SWITCH_COOLDOWN_TICKS = 40;
    private static int switchCooldown = 0;

    public static void initialize() {
        register();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (switchCooldown > 0) switchCooldown--;

            if (characterSwitch.wasPressed()) {
                if (switchCooldown > 0) return;
                if (ClientNetwork.getBattle() &&
                        ClientNetwork.getBattleState().equals(CombatState.STANDART.name())) {
                    PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                    buf.writeBoolean(true);
                    ClientNetwork.send(ClientNetwork.CHARACTER_SWITCH_ID, buf);
                    switchCooldown = SWITCH_COOLDOWN_TICKS;
                }
            }
        });
    }

    public static List<KeyBinding> battleKeys() {
        return BATTLE_KEYS;
    }

    private static void register() {
        KeyBindingHelper.registerKeyBinding(characterSwitch);
        KeyBindingHelper.registerKeyBinding(strongAttack);
        KeyBindingHelper.registerKeyBinding(ultimate);
    }
}
