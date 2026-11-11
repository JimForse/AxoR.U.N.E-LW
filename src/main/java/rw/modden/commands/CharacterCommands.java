package rw.modden.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import rw.modden.Axorunelostworlds;
import rw.modden.characters.Character;
import rw.modden.characters.AbilitiesImprovement;
import rw.modden.characters.CharacterName;
import rw.modden.characters.CharacterTemplate;
import rw.modden.characters.CharacterTemplates;
import rw.modden.combat.Battle;
import rw.modden.combat.CombatState;
import rw.modden.combat.path.PathFactory;
import rw.modden.combat.path.PathesName;
import rw.modden.components.BattleStateComponent;
import rw.modden.components.ModComponents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static rw.modden.Axorunelostworlds.LOGGER;

public class CharacterCommands {
    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispather, registryAcess, environment) -> {
            character(dispather);
        });
    }

    private static Stream<String> characters() { return CharacterName.values().stream().map(CharacterName::name); }
    private static Stream<String> templates() { return CharacterTemplates.names().stream().map(CharacterName::name); }

    private static void character(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("character")
            .requires(source -> source.hasPermissionLevel(4))
            .then(literal("add")
                .then(argument("character", StringArgumentType.word())
                    .suggests((context, builder) -> CommandSource.suggestMatching(characters(), builder))
                        .then(argument("player", EntityArgumentType.player())
                            .executes(CharacterCommands::addCharacter))))

            .then(literal("set")
            .then(argument("character", StringArgumentType.word())
                .suggests((context, builder) -> CommandSource.suggestMatching(characters(), builder))
                    .then(argument("player", EntityArgumentType.player())
                        .executes(CharacterCommands::setCharacter))))

            .then(literal("remove")
            .then(argument("character", StringArgumentType.word())
                .suggests((context, builder) -> CommandSource.suggestMatching(characters(), builder))
                    .then(argument("player", EntityArgumentType.player())
                        .executes(CharacterCommands::removeCharacter))))

            .then(literal("edit")
            .then(argument("character", StringArgumentType.word())
                .suggests((context, builder) -> CommandSource.suggestMatching(characters(), builder))
                    .then(argument("player", EntityArgumentType.player())
                        .then(argument("state", StringArgumentType.word())
                            .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{"heal", "stars", "level", "ascension", "strength", "stamina", "staminaRegen", "healRegen", "defence", "weapon"}, builder))
                                .then(argument("value", DoubleArgumentType.doubleArg())
                                    .executes(CharacterCommands::editCharacter)))
                        .then(literal("weapon")
                            .then(argument("weaponID", StringArgumentType.greedyString())
                                .suggests((context, builder) -> CommandSource.suggestMatching(Axorunelostworlds.permitted_equipment,builder))
                                    .executes(CharacterCommands::editCharacter)))
                        .then(literal("path")
                            .then(argument("pathName", StringArgumentType.word())
                                .suggests((context, builder) -> CommandSource.suggestMatching(Arrays.stream(PathesName.values()).map(Enum::name), builder))
                                    .executes(CharacterCommands::editCharacter))))))

            .then(literal("list")
                .executes(CharacterCommands::listTemplates))

            .then(literal("info")
                .then(argument("character", StringArgumentType.word())
                    .suggests((context, builder) -> CommandSource.suggestMatching(templates(), builder))
                        .executes(CharacterCommands::templateInfo)))

            .then(literal("custom")
                .then(literal("create")
                    .then(argument("character", StringArgumentType.word())
                        .executes(CharacterCommands::templateCreate)
                        .then(argument("path", StringArgumentType.word())
                            .suggests((context, builder) -> CommandSource.suggestMatching(Arrays.stream(PathesName.values()).map(Enum::name), builder))
                                .executes(CharacterCommands::templateCreate))))
                .then(literal("edit")
                    .then(argument("character", StringArgumentType.word())
                        .suggests((context, builder) -> CommandSource.suggestMatching(templates(), builder))
                            .then(literal("weapon")
                                .then(argument("weaponID", StringArgumentType.greedyString())
                                    .suggests((context, builder) -> CommandSource.suggestMatching(Axorunelostworlds.permitted_equipment, builder))
                                        .executes(ctx -> templateEdit(ctx, "weapon"))))
                            .then(literal("path")
                                .then(argument("path", StringArgumentType.word())
                                    .suggests((context, builder) -> CommandSource.suggestMatching(Arrays.stream(PathesName.values()).map(Enum::name), builder))
                                        .executes(ctx -> templateEdit(ctx, "path"))))
                            .then(literal("stars")
                                .then(argument("number", IntegerArgumentType.integer(0))
                                    .executes(ctx -> templateEdit(ctx, "stars"))))
                            .then(literal("level")
                                .then(argument("number", IntegerArgumentType.integer(1, AbilitiesImprovement.maxLevel(AbilitiesImprovement.MAX_ASCENSION)))
                                    .executes(ctx -> templateEdit(ctx, "level"))))
                            .then(argument("stat", StringArgumentType.word())
                                .suggests((context, builder) -> CommandSource.suggestMatching(CharacterTemplate.STATS, builder))
                                    .then(argument("bound", StringArgumentType.word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{"start", "max"}, builder))
                                            .then(argument("value", FloatArgumentType.floatArg(0))
                                                .executes(ctx -> templateEdit(ctx, "stat")))))))
                .then(literal("delete")
                    .then(argument("character", StringArgumentType.word())
                        .suggests((context, builder) -> CommandSource.suggestMatching(templates(), builder))
                            .executes(CharacterCommands::templateDelete)))
                .then(literal("reload")
                    .executes(CharacterCommands::templateReload)))

            .then(literal("check")
                .then(literal("characters")
                    .executes(ctx -> listCharacters(ctx, ctx.getSource().getPlayerOrThrow()))
                    .then(argument("player", EntityArgumentType.player())
                        .executes(ctx -> listCharacters(ctx, EntityArgumentType.getPlayer(ctx, "player")))))
                .then(literal("character")
                    .then(argument("character", StringArgumentType.word())
                        .suggests((context, builder) -> CommandSource.suggestMatching(characters(), builder))
                            .then(argument("player", EntityArgumentType.player())
                                .executes(CharacterCommands::checkCharacter))))
                    .then(literal("state")
                        .then(argument("character", StringArgumentType.word())
                            .suggests((context, builder) -> CommandSource.suggestMatching(characters(), builder))
                                .then(argument("player", EntityArgumentType.player())
                                    .then(argument("state", StringArgumentType.word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{"heal", "stars", "level", "ascension", "maxAscensionLevel", "strength", "stamina", "staminaRegen", "healRegen", "defence", "weapon", "path"}, builder))
                                            .executes(CharacterCommands::checkCharacterState))))))
        );
    }


    private static int addCharacter(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String argument = StringArgumentType.getString(ctx, "character");
        ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
        if (!ModComponents.CHARACTERS.get(player).addCharacter(CharacterName.of(argument))) {
            ctx.getSource().sendError(Text.literal("No template for " + argument.toUpperCase() + ", see /character list"));
            return 0;
        }
        ModComponents.CHARACTERS.sync(player);
        ctx.getSource().sendFeedback(() -> Text.literal(String.format("Character [%s] has been add to player inventory", argument)), false);
        //ctx.getSource().sendFeedback(() -> Text.literal(String.format("")), false);
        return 1;
    }

    private static int setCharacter(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String argument = StringArgumentType.getString(ctx, "character");
        ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
        int result = ModComponents.CHARACTERS.get(player).setCharacter(CharacterName.of(argument));
        if (result == -6) {
            ctx.getSource().sendError(Text.literal("This character is locked (no template)"));
            return 0;
        }
        if (result !=1) {
            ctx.getSource().sendError(Text.literal("Player hasn`t this character"));
            return 0;
        }
        ArrayList<CharacterName> group = new ArrayList<>();
        group.add(CharacterName.of(argument));
        BattleStateComponent component = ModComponents.BATTLE_STATE.get(player);
        CombatState state = component.getState();
        if (state==CombatState.NONE) {
            if (!(new Battle(player).standartBattle(group)!=1))
                component.setState(CombatState.STANDART);
            else ctx.getSource().sendError(Text.literal("This character hasn`t weapon"));
        } else if (new Battle(player).standartBattle(group)!=1) {
                ctx.getSource().sendError(Text.literal("This character hasn`t weapon"));
                component.setState(CombatState.NONE);
        }
        return 1;
    }

    private static int removeCharacter(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String argument = StringArgumentType.getString(ctx, "character");
        ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
        ModComponents.CHARACTERS.get(player).removeCharacter(CharacterName.of(argument));
        ctx.getSource().sendFeedback(() -> Text.literal(String.format("Character [%s] has been removed", argument)), false);
        return 1;
    }

    private static int checkCharacter(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String argument = StringArgumentType.getString(ctx, "character");
        ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
        boolean checker = ModComponents.CHARACTERS.get(player).hasCharacter(CharacterName.of(argument));
        ctx.getSource().sendFeedback(() -> Text.literal(checker+": This player "+(checker?"has":"hasn`t")+" this character"), false);
        return 1;
    }

    private static int listCharacters(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player) {
        Map<CharacterName, Character> owned = ModComponents.CHARACTERS.get(player).getCharacters();
        String name = player.getName().getString();
        if (owned.isEmpty()) {
            ctx.getSource().sendFeedback(() -> Text.literal(name + " has no characters"), false);
            return 0;
        }
        ctx.getSource().sendFeedback(() -> Text.literal(name + " has " + owned.size() + " character(s):"), false);
        for(Character chr: owned.values())
            ctx.getSource().sendFeedback(() -> Text.literal(String.format("  %s — level %d, ascension %d%s", chr.getName(), chr.getLevel(), chr.getAscension(), chr.isLocked()?" [locked]":"")), false);
        return owned.size();
    }

    private static int listTemplates(CommandContext<ServerCommandSource> ctx) {
        if (CharacterTemplates.names().isEmpty()) {
            ctx.getSource().sendFeedback(() -> Text.literal("No characters exist"), false);
            return 0;
        }
        String list = templates().collect(Collectors.joining(", "));
        ctx.getSource().sendFeedback(() -> Text.literal(CharacterTemplates.names().size() + " character(s): " + list), false);
        return CharacterTemplates.names().size();
    }

    private static int templateInfo(CommandContext<ServerCommandSource> ctx) {
        CharacterName name = CharacterName.of(StringArgumentType.getString(ctx, "character"));
        CharacterTemplate template = CharacterTemplates.get(name);
        if (template == null) {
            ctx.getSource().sendError(Text.literal("No template for " + name.name()));
            return 0;
        }
        ctx.getSource().sendFeedback(() -> Text.literal(String.format("%s: path %s, weapon %s, stars %d, level %d, ascension %d",
                name.name(), template.pathOrDefault(), template.weapon() == null?"none":template.weapon(), template.starsOrDefault(), template.levelOrDefault(), template.ascension())), false);
        for(String stat: CharacterTemplate.STATS)
            ctx.getSource().sendFeedback(() -> Text.literal(String.format("  %s: %s → %s", stat, template.startOrDefault().get(stat), template.maxOrDefault().get(stat))), false);
        return 1;
    }

    private static int templateCreate(CommandContext<ServerCommandSource> ctx) {
        String argument = StringArgumentType.getString(ctx, "character");
        if (!CharacterName.isValid(argument)) {
            ctx.getSource().sendError(Text.literal("Name may contain only a-z, 0-9 and _"));
            return 0;
        }
        boolean hasPath = ctx.getNodes().stream().anyMatch(node -> node.getNode().getName().equals("path"));
        PathesName path = hasPath?pathOf(StringArgumentType.getString(ctx, "path")):CharacterTemplate.randomPath();
        if (path == null) {
            ctx.getSource().sendError(Text.literal("Unknown path"));
            return 0;
        }
        CharacterName name = CharacterName.of(argument);
        if (CharacterTemplates.exists(name)) {
            ctx.getSource().sendError(Text.literal("Character " + name.name() + " already exists"));
            return 0;
        }
        if (!CharacterTemplates.put(name, CharacterTemplate.create(path))) {
            ctx.getSource().sendError(Text.literal("Cannot save template, see server log"));
            return 0;
        }
        ctx.getSource().sendFeedback(() -> Text.literal("Character " + name.name() + " created with default stats, path " + path), true);
        return 1;
    }

    private static int templateEdit(CommandContext<ServerCommandSource> ctx, String field) {
        CharacterName name = CharacterName.of(StringArgumentType.getString(ctx, "character"));
        CharacterTemplate template = CharacterTemplates.get(name);
        if (template == null) {
            ctx.getSource().sendError(Text.literal("No template for " + name.name()));
            return 0;
        }
        CharacterTemplate edited;
        String description;
        switch (field) {
            case "weapon" -> {
                String weapon = StringArgumentType.getString(ctx, "weaponID");
                if (!Axorunelostworlds.permitted_equipment.contains(weapon)) {
                    ctx.getSource().sendError(Text.literal("Weapon is not in the permitted list"));
                    return 0;
                }
                edited = template.withWeapon(weapon);
                description = "weapon = " + weapon;
            }
            case "path" -> {
                PathesName path = pathOf(StringArgumentType.getString(ctx, "path"));
                if (path == null) {
                    ctx.getSource().sendError(Text.literal("Unknown path"));
                    return 0;
                }
                edited = template.withPath(path);
                description = "path = " + path;
            }
            case "stars" -> {
                int stars = IntegerArgumentType.getInteger(ctx, "number");
                edited = template.withStars(stars);
                description = "stars = " + stars;
            }
            case "level" -> {
                int level = IntegerArgumentType.getInteger(ctx, "number");
                edited = template.withLevel(level);
                description = "level = " + level + " (ascension " + edited.ascension() + ")";
            }
            default -> {
                String stat = StringArgumentType.getString(ctx, "stat"), bound = StringArgumentType.getString(ctx, "bound");
                float value = FloatArgumentType.getFloat(ctx, "value");
                if (template.startOrDefault().get(stat) == null) {
                    ctx.getSource().sendError(Text.literal("Unknown stat: " + stat));
                    return 0;
                }
                if (bound.equals("start")) edited = template.withStart(template.startOrDefault().with(stat, value));
                else if (bound.equals("max")) edited = template.withMax(template.maxOrDefault().with(stat, value));
                else {
                    ctx.getSource().sendError(Text.literal("Use start or max"));
                    return 0;
                }
                description = stat + " " + bound + " = " + value;
            }
        }
        if (!CharacterTemplates.put(name, edited)) {
            ctx.getSource().sendError(Text.literal("Cannot save template, see server log"));
            return 0;
        }
        ctx.getSource().sendFeedback(() -> Text.literal(name.name() + ": " + description + " (only new copies are affected)"), true);
        return 1;
    }

    private static int templateDelete(CommandContext<ServerCommandSource> ctx) {
        CharacterName name = CharacterName.of(StringArgumentType.getString(ctx, "character"));
        if (!CharacterTemplates.delete(name)) {
            ctx.getSource().sendError(Text.literal("No template for " + name.name()));
            return 0;
        }
        ctx.getSource().sendFeedback(() -> Text.literal("Character " + name.name() + " deleted, owned copies are locked"), true);
        return 1;
    }

    private static int templateReload(CommandContext<ServerCommandSource> ctx) {
        List<String> errors = CharacterTemplates.reload();
        for(String error: errors) ctx.getSource().sendError(Text.literal(error));
        ctx.getSource().sendFeedback(() -> Text.literal("Loaded " + CharacterTemplates.names().size() + " character(s), " + errors.size() + " error(s)"), true);
        return errors.isEmpty()?1:0;
    }

    private static PathesName pathOf(String value) {
        for(PathesName path: PathesName.values())
            if (path.name().equalsIgnoreCase(value)) return path;
        return null;
    }

    private static String stateOf(Character chr, String characteristic) {
        return switch (characteristic) {
            case "heal" -> String.valueOf(chr.getHealReserve());
            case "stars" -> String.valueOf(chr.getStars());
            case "level" -> String.valueOf(chr.getLevel());
            case "ascension" -> String.valueOf(chr.getAscension());
            case "maxAscensionLevel" -> String.valueOf(chr.getMaxLevel());
            case "strength" -> String.valueOf(chr.getStrength());
            case "stamina" -> String.valueOf(chr.getStamina());
            case "staminaRegen" -> String.valueOf(chr.getStaminaRegen());
            case "healRegen" -> String.valueOf(chr.getHealRegen());
            case "defence" -> String.valueOf(chr.getDefence());
            case "weapon" -> chr.getWeapon()!=null?chr.getWeapon().getItemID():"none";
            case "path" -> chr.getPath().getPath().name();
            default -> null;
        };
    }

    private static int checkCharacterState(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String argument = StringArgumentType.getString(ctx, "character");
        String characteristic = StringArgumentType.getString(ctx, "state");
        ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
        Character chr = ModComponents.CHARACTERS.get(player).getCharacter(CharacterName.of(argument));
        if (chr == null) {
            ctx.getSource().sendError(Text.literal("Player hasn`t this character"));
            return 0;
        }
        String state = stateOf(chr, characteristic);
        if (state == null) {
            ctx.getSource().sendError(Text.literal("Unknown parameter: " + characteristic));
            return 0;
        }
        ctx.getSource().sendFeedback(() -> Text.literal(String.format("Parameter %s is: %s", characteristic, state)), false);
        return 1;
    }

    private static int editCharacter(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String argument = StringArgumentType.getString(ctx, "character");
        ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
        Character chr = ModComponents.CHARACTERS.get(player).getCharacter(CharacterName.of(argument));
        if (chr == null) {
            ctx.getSource().sendError(Text.literal("Player hasn`t this character"));
            return 0;
        }
        if (chr.isLocked()) {
            ctx.getSource().sendError(Text.literal("This character is locked (no template)"));
            return 0;
        }
        ctx.getNodes().forEach(x -> {
            if (x.getNode().getName().equals("weaponID")) {
                String weaponID = StringArgumentType.getString(ctx, "weaponID");
                chr.setWeapon(weaponID);
                ctx.getSource().sendFeedback(() -> Text.literal(String.format("Weapon %s seted to %s", weaponID, argument)), false);
            } else if (x.getNode().getName().equals("pathName")) {
                PathesName path = pathOf(StringArgumentType.getString(ctx, "pathName"));
                if (path == null) {
                    ctx.getSource().sendError(Text.literal("Unknown path"));
                    return;
                }
                chr.setPath(PathFactory.get(path));
                LOGGER.info("[ARLW-CHAR] {}: path of {} set to {}", player.getName().getString(), argument, path);
                ctx.getSource().sendFeedback(() -> Text.literal(String.format("Path of %s set to %s", argument, path)), false);
            } else if (x.getNode().getName().equals("state")) {
                String characteristic = StringArgumentType.getString(ctx, "state");
                double value = DoubleArgumentType.getDouble(ctx, "value");
                switch (characteristic) {
                    case "heal" -> chr.setHealReserve((float) value);
                    case "stars" -> chr.setStars((int) value);
                    case "level" -> chr.setLevel((int) value);
                    case "ascension" -> chr.setAscension((int) value);
                    case "strength" -> chr.setStrength((float) value);
                    case "stamina" -> chr.setStamina((float) value);
                    case "staminaRegen" -> chr.setStaminaRegen((float) value);
                    case "healRegen" -> chr.setHealRegen((float) value);
                    case "defence" -> chr.setDefence((float) value);
                    default -> {
                        ctx.getSource().sendError(Text.literal("Unknown parameter: " + characteristic));
                        return;
                    }
                }
                ctx.getSource().sendFeedback(() -> Text.literal(String.format("Parameter %s of %s set to %s", characteristic, argument, stateOf(chr, characteristic))), false);
            }
        });
        ModComponents.CHARACTERS.sync(player);
        return 1;
    }
}
