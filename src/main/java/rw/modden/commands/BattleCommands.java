package rw.modden.commands;

import com.mojang.brigadier.CommandDispatcher;
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
import rw.modden.combat.Battle;
import rw.modden.combat.CombatState;
import rw.modden.components.ModComponents;

import java.util.ArrayList;
import java.util.logging.Logger;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static rw.modden.Axorunelostworlds.LOGGER;

public class BattleCommands {
    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispather, registryAcess, environment) -> {
            battlestate(dispather);
        });
    }
    private static void battlestate(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("battle")
            .requires(source -> source.hasPermissionLevel(4))
            .then(literal("start")
                .then(literal("standart")
                    .then(argument("target", EntityArgumentType.player())
                        .then(argument("group", StringArgumentType.word())
                            .suggests((context, builder) -> {
                                try {
                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "target");
                                    return CommandSource.suggestMatching(ModComponents.CHARACTERS.get(player).getGroupsList().toArray(new String[0]), builder);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            })
                                .executes(BattleCommands::battleStartStandart))))
                .then(literal("event")
                    .then(argument("target", EntityArgumentType.player())
                        .then(argument("file_name", StringArgumentType.word()))
                            .executes(BattleCommands::battleStartEvent)))
            )

            .then(literal("status")
                .then(argument("target", EntityArgumentType.player())
                    .executes(BattleCommands::battleStatus)))

            .then(literal("stop")
                .then(argument("target", EntityArgumentType.player())
                    .executes(BattleCommands::battleStop)))
        );
    }

    private static int battleStartStandart(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        ArrayList<CharacterName> group = ModComponents.CHARACTERS.get(target).getCharactersGroup(StringArgumentType.getString(ctx, "group"));
        ModComponents.BATTLE_STATE.get(target).setState(CombatState.STANDART);
        new Battle().standartBattle(target, group);
        return 1;
    }

    private static int battleStartEvent(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        String fileName = StringArgumentType.getString(ctx, "file_name");
        ModComponents.BATTLE_STATE.get(target).setState(CombatState.EVENT);
        new Battle().eventBattle(target, fileName);
        return 1;
    }

    private static int battleStatus(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
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

    private static int battleStop(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        ModComponents.BATTLE_STATE.get(target).setState(CombatState.NONE);
        new Battle().stopBattle(target);
        return 1;
    }
}
