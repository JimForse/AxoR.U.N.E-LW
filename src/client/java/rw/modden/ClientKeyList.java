package rw.modden;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import rw.modden.network.ClientNetwork;

public class ClientKeyList {
    private static final KeyBinding characterSwitch = new KeyBinding(
                "key.axorunelostworlds.characters_switch",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_SPACE,
                "key.category.axorune");

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (ClientNetwork.getBattle()) { //TODO: добавить пакет с сервера на клиент, передающий текущее состояние боя игрока
                if (characterSwitch.isPressed()) {
                    // TODO: реализовать пакет, передающийся с клиента на сервер, который заставит поменять персонажа
                }
            }
        });
    }
}
