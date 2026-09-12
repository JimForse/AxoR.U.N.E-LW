package rw.modden.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import rw.modden.characters.CharacterName;
import rw.modden.characters.RealizingCharacters;
import rw.modden.combat.Battle;
import rw.modden.combat.CombatState;
import rw.modden.components.CharactersComponent;
import rw.modden.components.ModComponents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static rw.modden.Axorunelostworlds.LOGGER;

public class BattleCommands {
    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispather, registryAcess, environment) -> {
            battle(dispather);
        });
    }

    private static String[] characters = Arrays.stream(CharacterName.values())
            .map(Enum::name)
            .toArray(String[]::new);

    private static void battle(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("battle")
            .requires(source -> source.hasPermissionLevel(4))
            .then(literal("start")
                .then(literal("standart")
                    .then(argument("target", EntityArgumentType.player())
                        .then(argument("group", StringArgumentType.word())
                            .suggests((context, builder) -> CommandSource.suggestMatching(
                                ModComponents.CHARACTERS.get(EntityArgumentType.getPlayer(context, "target")).getGroupsList() ,builder))
                                .executes(BattleCommands::battleStartStandart))))
                .then(literal("event")
                    .then(argument("target", EntityArgumentType.player())
                        .then(argument("file_name", StringArgumentType.word()))
                            .executes(BattleCommands::battleStartEvent)))
            )

            .then(literal("status")
                .executes(BattleCommands::battleStop))
                .then(argument("target", EntityArgumentType.player())
                    .executes(BattleCommands::battleStatus))

            .then(literal("stop")
                .executes(BattleCommands::battleStop))
                .then(argument("target", EntityArgumentType.player())
                    .executes(BattleCommands::battleStop))
        );
        dispatcher.register(literal("group")
            .requires(source -> source.hasPermissionLevel(4))
            .then(literal("create")
                .then(argument("target", EntityArgumentType.player())
                    .then(argument("group_name", StringArgumentType.word())
                        .executes(BattleCommands::createGroup))))

            .then(literal("edit")
                .then(argument("target", EntityArgumentType.player())
                    .then(argument("group_name", StringArgumentType.word())
                        .suggests((context, builder) -> CommandSource.suggestMatching(
                                ModComponents.CHARACTERS.get(EntityArgumentType.getPlayer(context, "target")).getGroupsList() ,builder))
                            .then(argument("character1", StringArgumentType.word())
                                .suggests((context, builder) -> CommandSource.suggestMatching(characters, builder))
                                    .executes(BattleCommands::editGroup)
                                    .then(argument("character2", StringArgumentType.word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(characters, builder))
                                            .executes(BattleCommands::editGroup)
                                                .then(argument("character3", StringArgumentType.word())
                                                        .suggests((context, builder) -> CommandSource.suggestMatching(characters, builder))
                                                            .executes(BattleCommands::editGroup)))))))
            .then(literal("remove")
                .then(argument("target", EntityArgumentType.player())
                    .then(argument("group_name", StringArgumentType.word())
                        .suggests((context, builder) -> CommandSource.suggestMatching(
                            ModComponents.CHARACTERS.get(EntityArgumentType.getPlayer(context, "target")).getGroupsList() ,builder))
                            .executes(BattleCommands::groupRemove)
                            .then(argument("character", StringArgumentType.word())
                                .suggests((context, builder) -> CommandSource.suggestMatching(
                                    ModComponents.CHARACTERS.get(EntityArgumentType.getPlayer(context, "target")).getCharactersGroup(
                                        StringArgumentType.getString(context, "group_name")).stream().map(CharacterName::name), builder))
                                .executes(BattleCommands::groupRemove)))))
            .then(literal("check")
                .then(argument("target", EntityArgumentType.player())
                    .then(argument("group_name", StringArgumentType.word())
                        .suggests((context, builder) -> CommandSource.suggestMatching(
                            ModComponents.CHARACTERS.get(EntityArgumentType.getPlayer(context, "target")).getGroupsList() ,builder))
                            .executes(BattleCommands::checkGroup))))
        );
        dispatcher.register(literal("switch")
            .requires(source -> source.hasPermissionLevel(4))
                .executes(BattleCommands::switcher)
                .then(argument("value", StringArgumentType.word())
                    .executes(BattleCommands::switcher))
                .then(argument("targetValue", EntityArgumentType.player())
                    .then(argument("value", StringArgumentType.word())
                        .executes(BattleCommands::switcher)))
                .then(argument("target", EntityArgumentType.player())
                    .executes(BattleCommands::switcher))
        );
    }

    private static int battleStartStandart(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        CharactersComponent component = ModComponents.CHARACTERS.get(target);
        String groupName = StringArgumentType.getString(ctx, "group");
        ArrayList<CharacterName> group = component.getCharactersGroup(groupName);
        component.setCurrentGroupName(groupName);
        ModComponents.BATTLE_STATE.get(target).setState(CombatState.STANDART);
        if (new Battle(target).standartBattle(group)!=1)
            ctx.getSource().sendError(Text.literal("This character hasn`t weapon"));
        return 1;
    }

    private static int battleStartEvent(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        String fileName = StringArgumentType.getString(ctx, "file_name");
        ModComponents.BATTLE_STATE.get(target).setState(CombatState.EVENT);
        new Battle(target).eventBattle(fileName);
        return 1;
    }

    private static int battleStatus(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException { //TODO: Реализовать исполнение команды без указания энтити
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        CombatState state = ModComponents.BATTLE_STATE.get(target).getState();

        try {
            ServerPlayerEntity player = ctx.getSource().getPlayer();
            player.sendMessage(Text.literal(String.format("Your battle state is: {%s}", state)));
        } catch (Exception e) {
            LOGGER.error("BattleCommands.battlestate: You try check the battle status at console");
        }
        return 1;
    }

    private static int battleStop(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException { //TODO: Реализовать исполнение команды без указания энтити
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        ModComponents.BATTLE_STATE.get(target).setState(CombatState.NONE);
        new Battle(target).stopBattle();
        ctx.getSource().sendFeedback(() -> Text.literal(String.format("Battle state for player [%s] has been stoped", target.getEntityName())), false);
        return 1;
    }

    private static int createGroup(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String groupName = StringArgumentType.getString(ctx, "group_name");
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        CharactersComponent component = ModComponents.CHARACTERS.get(target);
        component.addGroupToGroupsList(groupName);
        component.addGroupToCharacterGroups(groupName);
        ctx.getSource().sendFeedback(() -> Text.literal(String.format("Group [%s] has been crated", groupName)), false);
        return 1;
    }

    private static int editGroup(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        String groupName = StringArgumentType.getString(ctx, "group_name");
        CharactersComponent component = ModComponents.CHARACTERS.get(target);
        int[] characters = new int[3];
        characters[0] = component.addCharacterToGroup(groupName, CharacterName.valueOf(StringArgumentType.getString(ctx, "character1")));

        ctx.getNodes().forEach(x -> {
            if (x.getNode().getName().equals("character2"))
                characters[1] = component.addCharacterToGroup(groupName, CharacterName.valueOf(StringArgumentType.getString(ctx, "character2")));
            else if (x.getNode().getName().equals("character3"))
                characters[2] = component.addCharacterToGroup(groupName, CharacterName.valueOf(StringArgumentType.getString(ctx, "character3")));
        });
        for (int character: characters) {
            if (character!=1) {
                switch (character) {
                    case -4  -> ctx.getSource().sendError(Text.literal("Unknown Exception"));
                    case -1 -> ctx.getSource().sendError(Text.literal("Group size is maximum"));
                    case -2 -> ctx.getSource().sendError(Text.literal("Player hasn`t this character"));
                    case -3 -> ctx.getSource().sendError(Text.literal("Group already has this character"));
                }
            } else
                ctx.getSource().sendFeedback(() -> Text.literal(String.format("Character(s) has been add to group [%s]", groupName)), false);
        }

        return 1;
    }

    private static int groupRemove(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        String groupName = StringArgumentType.getString(ctx, "group_name");
        final boolean[] deleteGroup = {true};
        CharactersComponent component = ModComponents.CHARACTERS.get(target);

        ctx.getNodes().forEach(x -> {
            if (x.getNode().getName().equals("character")) {
                CharacterName name = CharacterName.valueOf(StringArgumentType.getString(ctx, "character"));
                component.removeCharacterFromGroup((name), groupName);
                deleteGroup[0] = false;
                ctx.getSource().sendFeedback(() -> Text.literal(String.format("Character [%s] has been deleted from group [%s]", name, groupName)), false);
            }
        });
        if (deleteGroup[0]) {
            component.removeGroup(groupName);
            ctx.getSource().sendFeedback(() -> Text.literal(String.format("Group [%s] has been deleted", groupName)), false);
        }
        return 1;
    }

    private static int checkGroup(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        String groupName = StringArgumentType.getString(ctx, "group_name");
        String s = ModComponents.CHARACTERS.get(target).getCharactersGroup(groupName).stream().map(CharacterName::name).collect(Collectors.joining(", "));
        ctx.getSource().sendFeedback(() -> Text.literal("This group contains is: "+s), false);
        return 1;
    }

    private static int switcher(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        int[] result = new int[]{1};
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        CharactersComponent component = ModComponents.CHARACTERS.get(player);
        ctx.getNodes().forEach(x -> {
            if (x.getNode().getName().equals("targetValue")) {
                try {
                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "targetValue");
                    int value = IntegerArgumentType.getInteger(ctx, "value");
                    int r = component.switcher(target, value);
                    if (r==-1) {
                        ctx.getSource().sendError(Text.literal("Group has only one character"));
                        result[0]=0;
                    }
                } catch (CommandSyntaxException e) {
                    ctx.getSource().sendError(Text.literal(e.toString()));
                }
            }
            else if (x.getNode().getName().equals("target")) {
                try {
                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
                    int r = component.switcher(target);
                    if (r==-1) {
                        ctx.getSource().sendError(Text.literal("Group has only one character"));
                        result[0]=0;
                    }
                } catch (CommandSyntaxException e) {
                    ctx.getSource().sendError(Text.literal(e.toString()));
                }
            }
            else if (x.getNode().getName().equals("value")) {
                try {
                    int value = IntegerArgumentType.getInteger(ctx, "value");
                    int r = component.switcher(value);
                    if (r==-1) {
                        ctx.getSource().sendError(Text.literal("Group has only one character"));
                        result[0]=0;
                    }
                } catch (Exception e) {
                    ctx.getSource().sendError(Text.literal(e.toString()));
                }
            }
            else {
                try {
                    int r = component.switcher();
                    if (r==-1) {
                        ctx.getSource().sendError(Text.literal("Group has only one character"));
                        result[0]=0;
                    }
                } catch (Exception e) {
                    ctx.getSource().sendError(Text.literal(e.toString()));
                }
            }
        });
        return result[0];
    }
}