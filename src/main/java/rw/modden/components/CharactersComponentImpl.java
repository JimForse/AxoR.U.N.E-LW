package rw.modden.components;

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
import rw.modden.items.Equipment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CharactersComponentImpl implements CharactersComponent {
    private final Map<CharacterName, Character> characters = new HashMap<>();
    private final Map<String, ArrayList<CharacterName>> charactersGroups = new HashMap<>();
    private final ArrayList<String> groupsList = new ArrayList<>();

    @Override
    public Character getCharacter(CharacterName name) {
        return characters.get(name);
    }

    @Override
    public void setCharacter(CharacterName name, ServerPlayerEntity player) {
        new RealizingCharacters().realizingCharacterForPlayer(name, player);
    }

    @Override
    public void addCharacter(CharacterName name) {
        characters.put(name, new CharacterFactory().getCharacter(name));
    }
    public void addCharacters(float healReserve, int stars, float stamina, int strength, float staminaRegen, float healRegen, int defence, Path pathID, CharacterName name) {
        characters.put(name, new CharacterFactory().getCharacter(healReserve, stars, stamina, strength, staminaRegen, healRegen, defence, pathID, name));
    }

    @Override
    public Map<CharacterName, Character> getCharacters() {
        return characters;
    }

    @Override
    public boolean hasCharacter(CharacterName name) {
        return characters.containsKey(name);
    }

    @Override
    public void removeCharacter(CharacterName name) {
        characters.remove(name);
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
    public int addCharacterToGroup(String groupName, CharacterName characterName, ServerPlayerEntity player) {
        try {
            if (charactersGroups.containsKey(groupName)) {
                ArrayList<CharacterName> group = charactersGroups.get(groupName);
                if (group.size() < 3)
                    if (ModComponents.CHARACTERS.get(player).hasCharacter(characterName))
                        group.add(characterName);
                charactersGroups.put(groupName, group);
                return 1;
            } else {
                ArrayList<CharacterName> group = new ArrayList<>();
                if (ModComponents.CHARACTERS.get(player).hasCharacter(characterName))
                    group.add(characterName);
                charactersGroups.put(groupName, group);
                return 1;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public ArrayList<String> getGroupsList() {
        return groupsList;
    }

    @Override
    public void addGroupToGroupsList(String groupName) {
        groupsList.add(groupName);
    }

    @Override
    public boolean hasListGroup(String groupName) {
        return groupsList.contains(groupName);
    }

    @Override
    public void removeGroupFromGroupsList(String groupName) {
        groupsList.remove(groupName);
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        NbtList nKeyList = nbt.getList("CHARACTERNAME", NbtElement.STRING_TYPE);
        NbtList nList0 = nbt.getList("groupsList", NbtElement.STRING_TYPE);

        for (int i = 0; i < nKeyList.size(); i++) {
            CharacterName name = CharacterName.valueOf(nKeyList.get(i).asString());
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
        for (int i = 0; i < nList0.size(); i++) {
          if (!groupsList.contains(nList0.get(i).asString()))
              groupsList.add(nList0.get(i).asString());
          String groupName = nList0.get(i).asString();
          NbtList nList1 = nbt.getList(groupName+"_group", NbtElement.LIST_TYPE);
          ArrayList<CharacterName> group = new ArrayList<>();
            for (int j = 0; j < nList1.size(); j++) {
                group.add(CharacterName.valueOf(nList1.get(j).asString()));
            }
          charactersGroups.put(groupName, group);
        }
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        NbtList nKeyList = new NbtList();
        NbtList nList0 = new NbtList();
        NbtList nList1 = new NbtList();

        for (Map.Entry<CharacterName, Character> entry: characters.entrySet())
            nKeyList.add(NbtString.of(entry.getKey().name()));
        for (int i = 0; i < groupsList.size(); i++)
            nList0.add(NbtString.of(groupsList.get(i)));
        for (Map.Entry<String, ArrayList<CharacterName>> entry: charactersGroups.entrySet()) {
            ArrayList<CharacterName> group = entry.getValue();
            for (int i = 0; i < group.size(); i++)
                nList1.add(NbtString.of(group.get(i).name()));
            String groupName = entry.getKey();
            nbt.put(groupName+"_group", nList1);
        }

        nbt.put("CHARACTERNAME", nKeyList);
        nbt.put("groupsList", nList0);
    }
}
