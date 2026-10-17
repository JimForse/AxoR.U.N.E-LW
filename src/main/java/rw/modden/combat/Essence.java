package rw.modden.combat;

import net.minecraft.server.network.ServerPlayerEntity;
import rw.modden.characters.Character;
import rw.modden.characters.CharacterName;
import rw.modden.components.CharactersComponent;
import rw.modden.components.ModComponents;

import static rw.modden.Axorunelostworlds.LOGGER;

// Эссенция по ТЗ Фира (14.10):
//  - копится только пока энергия персонажа >= первого порога;
//  - только от урона: по врагу (100% стандарта) и по персонажу (50% стандарта); пассивно НЕ копится;
//  - "стандарт" - сколько эссенции даёт одна атака (по ТЗ обычно 3~9).
public final class Essence {
    private Essence() {}

    public static boolean isUltReady(Character character) {
        return character.getEssence() >= CombatConstants.ESSENCE_MAX;
    }

    // percent: 1.0F - урон по врагу, 0.5F - урон по персонажу (см. константы в CombatConstants).
    // Возвращает true, если значение изменилось (чтобы вызывающий отправил группу клиенту).
    public static boolean tryGain(ServerPlayerEntity player, float percent) {
        CharactersComponent component = ModComponents.CHARACTERS.get(player);
        CharacterName name = component.getCurrentCharacter();
        if (name == null || !component.hasCharacter(name)) return false;

        Character character = component.getCharacter(name);
        if (character.getEnergy() < CombatConstants.ESSENCE_ENERGY_THRESHOLD) return false; // мало энергии - не копится
        if (isUltReady(character)) return false;                                              // уже полная

        int amount = Math.round(CombatConstants.ESSENCE_STANDARD * percent);
        if (amount <= 0) return false;

        int before = character.getEssence();
        int after = Math.min(CombatConstants.ESSENCE_MAX, before + amount);
        character.setEssence(after);
        if (isUltReady(character)) {
            LOGGER.info("[ARLW] Ultimate of {} is ready for {}", name, player.getEntityName());
        }
        return true;
    }
}