package rw.modden.components;

import dev.onyxstudios.cca.api.v3.component.Component;
import rw.modden.characters.Character;
import rw.modden.characters.CharacterName;
import rw.modden.combat.path.Path;

import java.util.ArrayList;
import java.util.Map;

public interface CharactersComponent extends Component {
    Character getCharacter(CharacterName name);
    int setCharacter(CharacterName character);
    void setCurrentCharacter(CharacterName name);
    CharacterName getCurrentCharacter();
    void addCharacter(CharacterName character);
    Map<CharacterName, Character> getCharacters();
    String getCurrentGroupName();
    ArrayList<CharacterName> getCurrentGroup();
    void removeCharacterFromGroup(CharacterName name, String groupName);
    void removeGroupFromCharacterGroups(String groupName);
    void removeGroup(String groupName);
    void setCurrentGroupName(String name);
    boolean hasCharacter(CharacterName name);
    void removeCharacter(CharacterName name);
    Map<String, ArrayList<CharacterName>> getAllCharactersGroups();
    ArrayList<CharacterName> getCharactersGroup(String groupName);
    void addCharacters(float healReserve, int stars, float stamina, int strength, float staminaRegen, float healRegen, int defence, Path pathID, CharacterName name);
    int addCharacterToGroup(String groupName, CharacterName characterName);
    ArrayList<String> getGroupsList();
    void addGroupToCharacterGroups(String groupName);
    void addGroupToCharacterGroups(String groupName, ArrayList<CharacterName> group);
    void addGroupToGroupsList(String groupName);
    boolean hasListGroup(String groupName);
    void removeGroupFromGroupsList(String groupName);
}
