package rw.modden.characters;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rw.modden.combat.Battle;
import rw.modden.combat.CombatState;
import rw.modden.components.CharactersComponent;
import rw.modden.components.ModComponents;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static rw.modden.Axorunelostworlds.LOGGER;

import static rw.modden.components.ModComponents.CHARACTERS;

public class CharacterInitializer {
    public static final String CHARACTER_PACK_URL = "https://github.com/JimForse/AxoR.U.N.E-LW/releases/latest/download/arlw-characters.zip";

    public void getOrCreate() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            CharactersComponent component = CHARACTERS.get(player);
            String nick = player.getGameProfile().getName();

            component.refreshLocks();
            if (!CharacterName.isValid(nick)) {
                LOGGER.warn("[ARLW-CHAR] {} joined, nickname is not a valid character name", nick);
                if (component.getCharacters().isEmpty()) giveRandom(player, component, nick);
            } else {
                CharacterName own = CharacterName.of(nick);
                if (!CharacterTemplates.exists(own)) {
                    LOGGER.info("[ARLW-CHAR] {} joined, no template {} - creating default", nick, own);
                    CharacterTemplates.put(own, CharacterTemplate.create(CharacterTemplate.randomPath()));
                }
                if (!component.hasCharacter(own)) component.addCharacter(own);
            }
            if (component.getEventCharacter() == null && !component.getCharacters().isEmpty()) {
                CharacterName byNick = CharacterName.isValid(nick)?CharacterName.of(nick):null;
                if (byNick != null && component.hasCharacter(byNick)) component.setEventCharacter(byNick);
                else LOGGER.warn("[ARLW-CHAR] {} has characters but no event character, set it with /character event", nick);
            }
            LOGGER.info("[ARLW-CHAR] {} joined, characters: {}", player.getName().getString(), component.getCharacters().keySet());
            CHARACTERS.sync(player);

            CombatState state = ModComponents.BATTLE_STATE.get(player).getState();
            if (state.equals(CombatState.STANDART)) new Battle(player).stopBattle();
        });
    }

    private static void giveRandom(ServerPlayerEntity player, CharactersComponent component, String nick) {
        player.sendMessage(Text.literal("Your nickname \"" + nick + "\" can't be a character name (only A-Z, 0-9, _).").formatted(Formatting.RED));
        List<CharacterName> names = new ArrayList<>(CharacterTemplates.names());
        if (names.isEmpty()) {
            LOGGER.warn("[ARLW-CHAR] {}: no templates to give a random character, pack link sent", nick);
            player.sendMessage(Text.literal("Download the official character pack: ").formatted(Formatting.RED)
                    .append(Text.literal(CHARACTER_PACK_URL).styled(style -> style
                            .withUnderline(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, CHARACTER_PACK_URL)))));
            return;
        }
        CharacterName random = names.get(ThreadLocalRandom.current().nextInt(names.size()));
        component.addCharacter(random);
        LOGGER.info("[ARLW-CHAR] {}: random character {} given", nick, random);
        player.sendMessage(Text.literal("You got a random character: " + random.name()).formatted(Formatting.YELLOW));
    }

    public CharacterName characterFor(ServerPlayerEntity player) {
        return CHARACTERS.get(player).getEventCharacter();
    }
}
