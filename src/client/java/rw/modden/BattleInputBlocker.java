package rw.modden;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import rw.modden.mixin.KeyBindingAccessor;
import rw.modden.network.ClientNetwork;

import java.util.ArrayList;
import java.util.List;

public class BattleInputBlocker {
    public static boolean inBattle() {
        return ClientNetwork.getBattle();
    }

    public static void initialize() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (!inBattle()) return;

            GameOptions options = client.options;
            drain(options.inventoryKey);
            drain(options.dropKey);
            drain(options.pickItemKey);
            for(KeyBinding hotbarKey: options.hotbarKeys) drain(hotbarKey);

            if (client.currentScreen instanceof AbstractInventoryScreen<?>) {
                client.currentScreen.close();
            }
        });
    }

    private static void drain(KeyBinding binding) {
        while (binding.wasPressed()) {}
    }

    public static boolean routePress(InputUtil.Key key) {
        List<KeyBinding> targets = targetsFor(key, false);
        if (targets == null) return false;

        for(KeyBinding binding: targets) {
            KeyBindingAccessor accessor = (KeyBindingAccessor) binding;
            accessor.setTimesPressed(accessor.getTimesPressed() + 1);
        }
        return true;
    }

    public static boolean routeState(InputUtil.Key key, boolean pressed) {
        List<KeyBinding> targets = targetsFor(key, !pressed);
        if (targets == null) return false;

        for(KeyBinding binding: targets) binding.setPressed(pressed);
        return true;
    }

    /** Determines which handler receives the event for this physical key.
    * If null, there are no custom bindings assigned to it, so the default (vanilla) behavior applies.
    * In combat, only our custom bindings are active.
    * Outside of combat, the first non-custom binding takes precedence (consistent with vanilla behavior, where a key is assigned a single binding).
    * For "all" (key-release events), every binding is notified so that no key remains "stuck" in a pressed state. */
    private static List<KeyBinding> targetsFor(InputUtil.Key key, boolean everyone) {
        String keyId = key.getTranslationKey();
        List<KeyBinding> ours = new ArrayList<>();
        for(KeyBinding binding: ClientKeyList.battleKeys()) {
            if (binding.getBoundKeyTranslationKey().equals(keyId)) ours.add(binding);
        }
        if (ours.isEmpty()) return null;
        if (inBattle() && !everyone) return ours;

        List<KeyBinding> result = new ArrayList<>();
        for(KeyBinding binding: MinecraftClient.getInstance().options.allKeys) {
            if (!ClientKeyList.battleKeys().contains(binding) && binding.getBoundKeyTranslationKey().equals(keyId)) {
                result.add(binding);
                if (!everyone) break;
            }
        }
        if (everyone) result.addAll(ours);
        return result;
    }
}
