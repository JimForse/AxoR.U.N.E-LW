package rw.modden.combat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.buffer.Unpooled;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import rw.modden.characters.Character;
import rw.modden.characters.CharacterInitializer;
import rw.modden.characters.CharacterName;
import rw.modden.characters.RealizingCharacters;
import rw.modden.components.CharactersComponent;
import rw.modden.components.ModComponents;
import rw.modden.effects.EffectsFactory;
import rw.modden.network.ServerNetwork;

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
    private CharacterName characterName;
    ArrayList<CharacterName> currentGroup;
    private int chr;
    ServerPlayerEntity player;

    public Battle(ServerPlayerEntity player) {
        this.player = player;
    }

    public int standartBattle(ArrayList<CharacterName> group) {
        combatStateToBattle(group);
        if (battle) {
            currentGroup = group;
            chr = 0;
            CharacterName character = group.get(chr);
            characterName = character;
            new RealizingCharacters().realizingCharacterForPlayer(character, player);

            serverSend();
            return 1;
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    public void eventBattle(String fileName) {
        combatStateToBattle();
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
                buffes.forEach(x -> player.addStatusEffect(
                        new StatusEffectInstance(x, 48000, 0, false, false, false),
                        player));
            }
            if (hasDebuff) {
                for (String s: (ArrayList<String>) map.get("de_buffes"))
                    de_buffes.add(new EffectsFactory().getVanillaEffects(s));
                de_buffes.forEach(x -> player.addStatusEffect(
                        new StatusEffectInstance(x, 48000, 0, false, false, false),
                        player));
            }

            player.teleport(world, position.get(0), position.get(1), position.get(2),
                    Set.of() ,player.getYaw(), player.getPitch());
        }

        serverSend();
    }
    public void stopBattle() {
        battle = false;
        new RealizingCharacters().standartAttributesForPlayer(player);
        serverSend();
    }

    public void combatStateToBattle(ArrayList<CharacterName> group) {
        boolean can = true;
        CharactersComponent component = ModComponents.CHARACTERS.get(player);
        for (int i = 0; i < group.size(); i++) {
            if (component.getCharacter(group.get(i)).getWeapon()==null) can = false;
        }
        if (can) {
            CombatState state = ModComponents.BATTLE_STATE.get(player).getState();
            battle = state == CombatState.STANDART || state == CombatState.EVENT;
        }
    }

    public void combatStateToBattle(Character character) {
        if (character.getWeapon()!=null) {
            CombatState state = ModComponents.BATTLE_STATE.get(player).getState();
            battle = state == CombatState.STANDART || state == CombatState.EVENT;
        }
    }

    public void combatStateToBattle() {
        CombatState state = ModComponents.BATTLE_STATE.get(player).getState();
        battle = state == CombatState.STANDART || state == CombatState.EVENT;
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

    public CharacterName getCharacterName() {
        return characterName;
    }

    public boolean getBattle() {
        return battle;
    }

    public void switchCharacter() {
        if (chr == currentGroup.size()-1) chr = 0;
        else chr += 1;
        CharacterName character = currentGroup.get(chr);
        characterName = character;
        new RealizingCharacters().realizingCharacterForPlayer(character, player);
    }

    private void serverSend() {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBoolean(battle);
        ServerNetwork.send(player, ServerNetwork.BATTLE_PACKET_ID, buf);
    }
}
