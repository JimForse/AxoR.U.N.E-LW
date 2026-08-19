package rw.modden.components;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import rw.modden.characters.CharacterFactory;
import rw.modden.characters.CharacterName;
import rw.modden.characters.Character;
import rw.modden.characters.RealizingCharacters;
import rw.modden.combat.path.Path;
import rw.modden.combat.path.PathFactory;
import rw.modden.combat.path.PathesName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CharactersComponentImpl implements CharactersComponent {
    private final Map<CharacterName, Character> characters = new HashMap<>();
    private final Map<String, ArrayList<CharacterName>> charactersGroups = new HashMap<>();
    private final ArrayList<String> groupsList = new ArrayList<>();
    private final PlayerEntity player;
    private String currentGroupName;
    private CharacterName currentCharacter;

    public CharactersComponentImpl(PlayerEntity player) {
        this.player = player;
    }

    // ==============  GET  ==================
    @Override
    public Character getCharacter(CharacterName name) {
        return characters.get(name);
    }
    @Override
    public CharacterName getCurrentCharacter() {
        return currentCharacter;
    }
    @Override
    public Map<CharacterName, Character> getCharacters() {
        return characters;
    }
    @Override
    public Map<String, ArrayList<CharacterName>> getAllCharactersGroups() {
        return charactersGroups;
    }
    @Override
    public ArrayList<CharacterName> getCharactersGroup(String groupName) {
        return charactersGroups.get(groupName);
    }
    @Override
    public String getCurrentGroupName() {
        return currentGroupName;
    }
    @Override
    public ArrayList<CharacterName> getCurrentGroup() {
        return charactersGroups.get(currentGroupName);
    }
    @Override
    public ArrayList<String> getGroupsList() {
        return groupsList;
    }

    // ==============  HAS  ==================
    @Override
    public boolean hasCharacter(CharacterName name) {
        return characters.containsKey(name);
    }
    @Override
    public boolean hasListGroup(String groupName) {
        return groupsList.contains(groupName);
    }


    // ==============  REMOVE  ==================
    @Override
    public void removeCharacter(CharacterName name) {
        characters.remove(name);
    }
    @Override
    public void removeGroupFromGroupsList(String groupName) {
        groupsList.remove(groupName);
    }
    @Override
    public void removeCharacterFromGroup(CharacterName name, String groupName) {
        ArrayList<CharacterName> group = getCharactersGroup(groupName);
        if (group.remove(name)) {
            removeGroupFromCharacterGroups(groupName);
            addGroupToCharacterGroups(groupName, group);
        }
    }
    @Override
    public void removeGroupFromCharacterGroups(String groupName) {
        charactersGroups.remove(groupName);
    }
    @Override
    public void removeGroup(String groupName) {
        removeGroupFromCharacterGroups(groupName);
        removeGroupFromGroupsList(groupName);
    }

    // ==============  SET  ==================
    @Override
    public void setCurrentGroupName(String name) {
        currentGroupName = name;
    }
    @Override
    public void setCharacter(CharacterName name) {
        new RealizingCharacters().realizingCharacterForPlayer(name, (ServerPlayerEntity) player);
    }
    @Override
    public void setCurrentCharacter(CharacterName name) {
        currentCharacter = name;
    }

    // ==============  ADD  ==================
    @Override
    public void addCharacter(CharacterName name) {
        characters.put(name, new CharacterFactory().getCharacter(name));
    }
    @Override
    public void addCharacters(float healReserve, int stars, float stamina, int strength, float staminaRegen, float healRegen, int defence, Path pathID, CharacterName name) {
        characters.put(name, new CharacterFactory().getCharacter(healReserve, stars, stamina, strength, staminaRegen, healRegen, defence, pathID, name));
    }

    /**
     * error 0  — Exception
     * error -1 — Group size is maximum
     * error -2 — Player hasn't this character
     * error -3 — Group already has this character
     */

    @Override
    public int addCharacterToGroup(String groupName, CharacterName characterName) {
        try {
            if (charactersGroups.containsKey(groupName)) {
                ArrayList<CharacterName> group = charactersGroups.get(groupName);
                if (group.size() < 3) {
                    CharactersComponent component = ModComponents.CHARACTERS.get(player);
                    if (component.hasCharacter(characterName)) {
                        if (!group.contains(characterName))
                            group.add(characterName);
                        else return -3;
                    } else return -2;
                } else return -1;
                charactersGroups.put(groupName, group);
                return 1;
            } else {
                ArrayList<CharacterName> group = new ArrayList<>();
                if (ModComponents.CHARACTERS.get(player).hasCharacter(characterName)) {
                    group.add(characterName);
                    charactersGroups.put(groupName, group);
                    return 1;
                } else
                    return -2;
            }
        } catch (Exception e) {
            return 0;
        }
    }
    @Override
    public void addGroupToCharacterGroups(String groupName) {
        if (!charactersGroups.containsKey(groupName)) charactersGroups.put(groupName, new ArrayList<>());
    }
    @Override
    public void addGroupToCharacterGroups(String groupName, ArrayList<CharacterName> group) {
        if (!charactersGroups.containsKey(groupName)) charactersGroups.put(groupName, group);
    }
    @Override
    public void addGroupToGroupsList(String groupName) {
        groupsList.add(groupName);
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        NbtList nKeyList = nbt.getList("CHARACTERNAME", NbtElement.STRING_TYPE);
        NbtList nList0 = nbt.getList("groupsList", NbtElement.STRING_TYPE);

        for (NbtElement value: nKeyList) {
            CharacterName name = CharacterName.valueOf(value.asString());
            int stars = nbt.getInt(name.name() + "_stars"),
                    strength = nbt.getInt(name.name() + "_strength"),
                    defence = nbt.getInt(name.name() + "_defence");
            float staminaRegen = nbt.getFloat(name.name() + "_staminaRegen"),
                    stamina = nbt.getFloat(name.name() + "_stamina"),
                    healRegen = nbt.getFloat(name.name() + "_healRegen"),
                    healReserve = nbt.getFloat(name.name() + "_healReserve");
            Path pathID = PathFactory.get(PathesName.valueOf(nbt.getString(name.name() + "_path")));
            addCharacters(healReserve, stars, stamina, strength, staminaRegen, healRegen, defence, pathID, name);
        }
        for (NbtElement nbtElement : nList0) {
            if (!groupsList.contains(nbtElement.asString()))
                groupsList.add(nbtElement.asString());
            String groupName = nbtElement.asString();
            NbtList nList1 = nbt.getList(groupName + "_group", NbtElement.LIST_TYPE);
            ArrayList<CharacterName> group = new ArrayList<>();
            for (NbtElement element: nList1) {
                group.add(CharacterName.valueOf(element.asString()));
            }
            charactersGroups.put(groupName, group);
        }
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        NbtList nKeyList = new NbtList();
        NbtList nList0 = new NbtList();
        NbtList nList1 = new NbtList();

        for (Map.Entry<CharacterName, Character> entry: characters.entrySet()) {
            nKeyList.add(NbtString.of(entry.getKey().name()));
            entry.getValue().writeToNbt(nbt);
        }
        for (String s: groupsList)
            nList0.add(NbtString.of(s));
        for (Map.Entry<String, ArrayList<CharacterName>> entry: charactersGroups.entrySet()) {
            ArrayList<CharacterName> group = entry.getValue();
            for (CharacterName characterName: group)
                nList1.add(NbtString.of(characterName.name()));
            String groupName = entry.getKey();
            nbt.put(groupName+"_group", nList1);
        }

        nbt.put("CHARACTERNAME", nKeyList);
        nbt.put("groupsList", nList0);
    }
}