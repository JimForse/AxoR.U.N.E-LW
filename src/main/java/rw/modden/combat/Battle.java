package rw.modden.combat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import rw.modden.characters.CharacterInitializer;
import rw.modden.characters.CharacterName;
import rw.modden.characters.RealizingCharacters;
import rw.modden.components.ModComponents;
import rw.modden.effects.EffectsFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class Battle {
    private boolean battle;
    private File file;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private CharacterName characterName; // TODO: исправить, когда сделаю переключение персонажей, чтобы был именно текущий персонаж боя

    public void standartBattle(ServerPlayerEntity player, ArrayList<CharacterName> group) {
        combatStateToBattle(player);
        if (battle) {
            CharacterName character = group.get(0);
            characterName = character; // TODO: исправить, когда сделаю переключение персонажей, чтобы был именно текущий персонаж боя
            new RealizingCharacters().realizingCharacterForPlayer(character, player);
            new ActiveBattle().staminaExpense(player);
        }
    }

    @SuppressWarnings("unchecked")
    public void eventBattle(ServerPlayerEntity player, String fileName) {
        combatStateToBattle(player);
        if (battle) {
            CharacterName character = new CharacterInitializer().getName(player.getEntityName());
            new RealizingCharacters().realizingCharacterForPlayer(character, player);

            Path configDir = FabricLoader.getInstance().getConfigDir();
            file = configDir.resolve(fileName).toFile();

            Map<String, Object> map = readJson();
            ArrayList<Double> position = (ArrayList<Double>) map.get("coordinate");
            ServerWorld world = (ServerWorld) map.get("world");
            boolean hasDebuff = (Boolean) map.get("hasDebuff");
            boolean hasBuff = (Boolean) map.get("hasBuff");
            ArrayList<StatusEffect> buffes = new ArrayList<>();
            ArrayList<StatusEffect> de_buffes = new ArrayList<>();

            if (hasBuff) {
                for(String s: (ArrayList<String>) map.get("buffes"))
                    buffes.add(new EffectsFactory().getVanillaEffects(s));
                buffes.stream().forEach(x -> {
                    player.addStatusEffect(
                            new StatusEffectInstance(x, 48000, 0, false, false, false),
                            player);
                });
            }
            if (hasDebuff) {
                for (String s: (ArrayList<String>) map.get("de_buffes"))
                    de_buffes.add(new EffectsFactory().getVanillaEffects(s));
                de_buffes.stream().forEach(x -> {
                    player.addStatusEffect(
                            new StatusEffectInstance(x, 48000, 0, false, false, false),
                            player);
                });
            }

            player.teleport(world, position.get(0), position.get(1), position.get(2),
                    Set.of() ,player.getYaw(), player.getPitch());
        }
    }
    public void stopBattle(ServerPlayerEntity player) {
        battle = false;
        new RealizingCharacters().standartAttributesForPlayer(player);
    }

    public void combatStateToBattle(ServerPlayerEntity player) {
        CombatState state = ModComponents.BATTLE_STATE.get(player).getState();
        if (state == CombatState.STANDART || state == CombatState.EVENT)
            battle = true;
        else
            battle = false;
    }

    private Map<String, Object> readJson() {
        Map<String, Object> map = null;
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                map = gson.fromJson(reader, Map.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    public CharacterName getCharacterName() { // TODO: исправить, когда сделаю переключение персонажей, чтобы был именно текущий персонаж боя
        return characterName;
    }

    public boolean getBattle() {
        return battle;
    }
}
