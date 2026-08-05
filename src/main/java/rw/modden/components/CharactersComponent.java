package rw.modden.components;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.server.network.ServerPlayerEntity;
import rw.modden.characters.Character;
import rw.modden.characters.CharacterName;

import java.util.ArrayList;
import java.util.Map;

public interface CharactersComponent extends Component {
    Character getCharacter(CharacterName name);
    void setCharacter(CharacterName character);
    void addCharacter(CharacterName character);
    Map<CharacterName, Character> getCharacters();
    boolean hasCharacter(CharacterName name);
    void removeCharacter(CharacterName name);

    Map<String, ArrayList<CharacterName>> getAllCharactersGroups();
    ArrayList<CharacterName> getCharactersGroup(String groupName);
    int addCharacterToGroup(String groupName, CharacterName characterName);

    ArrayList<String> getGroupsList();
    void addGroupToGroupsList(String groupName);
    boolean hasListGroup(String groupName);
    void removeGroupFromGroupsList(String groupName);
}
