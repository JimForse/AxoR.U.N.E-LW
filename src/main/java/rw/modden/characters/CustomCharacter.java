package rw.modden.characters;

import rw.modden.combat.path.PathFactory;
import rw.modden.combat.path.PathesName;

import static rw.modden.Axorunelostworlds.LOGGER;

public class CustomCharacter extends ABCharacter {
    public CustomCharacter(CharacterName name) {
        super(0, 0, 0, 0, 0, 0, 0, PathFactory.get(PathesName.CREATE), name);
    }

    public CustomCharacter(CharacterName name, CharacterTemplate template) {
        this(name, template.startOrDefault(), template.pathOrDefault(), template.starsOrDefault());
        setAscension(template.ascension());
        setLevel(template.levelOrDefault());
        if (template.weapon() != null) setWeapon(template.weapon());
        LOGGER.info("[ARLW-CHAR] New {} from template: path {}, stars {}, level {}, ascension {}, weapon {}, stats {}",
                name, template.pathOrDefault(), getStars(), getLevel(), getAscension(), getWeapon() == null?"none":getWeapon().getItemID(), template.startOrDefault());
    }

    private CustomCharacter(CharacterName name, CharacterTemplate.Stats start, PathesName path, int stars) {
        super(start.healReserve(), stars, start.stamina(), start.strength(), start.staminaRegen(), start.healRegen(), start.defence(), PathFactory.get(path), name);
    }
}
