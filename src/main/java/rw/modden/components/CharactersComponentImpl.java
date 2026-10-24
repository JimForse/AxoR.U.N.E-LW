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
import rw.modden.combat.CharacterDeath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static rw.modden.Axorunelostworlds.LOGGER;

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
        removeCharacterFromGroup(name);
    }
    @Override
    public void removeGroupFromGroupsList(String groupName) {
        groupsList.remove(groupName);
    }
    @Override
    public void removeCharacterFromGroup(CharacterName name, String groupName) {
        ArrayList<CharacterName> group = getCharactersGroup(groupName);
        if (group != null) group.remove(name);
    }
    @Override
    public void removeCharacterFromGroup(CharacterName name) {
        for(Map.Entry<String, ArrayList<CharacterName>> map: charactersGroups.entrySet())
            if (map.getValue().contains(name))
                removeCharacterFromGroup(name, map.getKey());
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
        if (groupsList.contains(name)) currentGroupName = name;
    }
    @Override
    public int setCharacter(CharacterName name) {
        if (hasCharacter(name)) {
            new RealizingCharacters().realizingCharacterForPlayer(name, (ServerPlayerEntity) player);
            setCurrentCharacter(name);
            return 1;
        } else return -2;
    }
    @Override
    public void setCurrentCharacter(CharacterName name) {
        currentCharacter = name;
    }

    /**
     * error -5 — Target character is dead (or there are no living characters to switch to)
     */

    @Override
    public int switcher() {
        if (!(this.player instanceof ServerPlayerEntity)) return -4;
        return switcher((ServerPlayerEntity) this.player);
    }
    @Override
    public int switcher(ServerPlayerEntity player) {
        ArrayList<CharacterName> group = getCurrentGroup();
        if (group == null || group.size() == 1) return -1;

        CharacterName next = CharacterDeath.nextAlive(this, group, getCurrentCharacter());
        if (next == null) return -5;

        new RealizingCharacters().realizingCharacterForPlayer(next, player);
        return 1;
    }
    @Override
    public int switcher(int value) {
        if (!(this.player instanceof ServerPlayerEntity)) return -4;
        return switcher((ServerPlayerEntity) this.player, value);
    }
    @Override
    public int switcher(ServerPlayerEntity player, int value) {
        ArrayList<CharacterName> group = getCurrentGroup();
        int res = value==1?0:value==2?1:2;
        if (group.size()==1) return -1;

        CharacterName target = group.get(res);
        if (!CharacterDeath.isAlive(this, target)) return -5;

        new RealizingCharacters().realizingCharacterForPlayer(target, player);
        return 1;
    }

    // ==============  ADD  ==================
    @Override
    public void addCharacter(CharacterName name) {
        characters.put(name, new CharacterFactory().getCharacter(name));
    }

    /**
     * error -1 — Group size is maximum
     * error -2 — Player hasn't this character
     * error -3 — Group already has this character
     * error -4 — Unknown Exception
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
            return -4;
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
        if (!groupsList.contains(groupName)) groupsList.add(groupName);
    }

    // ==============  OTHER  ==================
    @Override
    public void readFromNbt(NbtCompound nbt) {
        NbtList nKeyList = nbt.getList("CHARACTERNAME", NbtElement.STRING_TYPE);
        NbtList nList0 = nbt.getList("groupsList", NbtElement.STRING_TYPE);

        for (NbtElement value: nKeyList) {
            CharacterName name = CharacterName.valueOf(value.asString());
            Character character = new CharacterFactory().getCharacter(name);
            character.readFromNbt(nbt);
            characters.put(name, character);
        }
        for (NbtElement nbtElement: nList0) {
            if (!groupsList.contains(nbtElement.asString()))
                groupsList.add(nbtElement.asString());
            String groupName = nbtElement.asString();
            NbtList nList1 = nbt.getList(groupName + "_group", NbtElement.STRING_TYPE);
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

        for (Map.Entry<CharacterName, Character> entry: characters.entrySet()) { // List all characters in player inventory
            nKeyList.add(NbtString.of(entry.getKey().name()));
            entry.getValue().writeToNbt(nbt);
        }
        for (String s: groupsList) // List all group names in player inventory
            nList0.add(NbtString.of(s));
        for (Map.Entry<String, ArrayList<CharacterName>> entry: charactersGroups.entrySet()) { // List all groups in player inventory
            NbtList nList1 = new NbtList();
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