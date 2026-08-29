package rw.modden.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import rw.modden.characters.Character;
import rw.modden.characters.CharacterName;
import rw.modden.combat.Battle;
import rw.modden.combat.CombatState;
import rw.modden.components.BattleStateComponent;
import rw.modden.components.ModComponents;

import java.util.ArrayList;
import java.util.Arrays;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CharacterCommands {
    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispather, registryAcess, environment) -> {
            character(dispather);
        });
    }

    private static final String[] characters = Arrays.stream(CharacterName.values())
            .map(Enum::name)
            .toArray(String[]::new);

    private static void character(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("addCharacter")
            .requires(source -> source.hasPermissionLevel(4))
            .then(argument("character", StringArgumentType.word())
                .suggests((context, builder) -> CommandSource.suggestMatching(characters, builder))
                    .then(argument("player", EntityArgumentType.player())
                       .executes(CharacterCommands::addCharacter)))
        );
        dispatcher.register(literal("setCharacter")
            .requires(source -> source.hasPermissionLevel(4))
            .then(argument("character", StringArgumentType.word())
                .suggests((context, builder) -> CommandSource.suggestMatching(characters, builder))
                    .then(argument("player", EntityArgumentType.player())
                        .executes(CharacterCommands::setCharacter)))
        );
        dispatcher.register(literal("removeCharacter")
            .requires(source -> source.hasPermissionLevel(4))
            .then(argument("character", StringArgumentType.word())
                .suggests((context, builder) -> CommandSource.suggestMatching(characters, builder))
                    .then(argument("player", EntityArgumentType.player())
                        .executes(CharacterCommands::removeCharacter)))
        );
        dispatcher.register(literal("editCharacter")
            .requires(source -> source.hasPermissionLevel(4))
            .then(argument("character", StringArgumentType.word())
                .suggests((context, builder) -> CommandSource.suggestMatching(characters, builder))
                    .then(argument("player", EntityArgumentType.player())
                        .then(argument("state", StringArgumentType.word())
                            .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{"heal", "stars", "strength", "stamina", "staminaRegen", "defence"}, builder))
                                .then(argument("value", DoubleArgumentType.doubleArg())
                                    .executes(CharacterCommands::editCharacter)))))
        );
        dispatcher.register(literal("checkCharacter")
            .requires(source -> source.hasPermissionLevel(4))
            .then(argument("character", StringArgumentType.word())
                .suggests((context, builder) -> CommandSource.suggestMatching(characters, builder))
                    .then(argument("player", EntityArgumentType.player())
                        .executes(CharacterCommands::checkCharacter)))
        );
        dispatcher.register(literal("checkCharacterState")
            .requires(source -> source.hasPermissionLevel(4))
            .then(argument("character", StringArgumentType.word())
                .suggests((context, builder) -> CommandSource.suggestMatching(characters, builder))
                    .then(argument("player", EntityArgumentType.player())
                        .then(argument("state", StringArgumentType.word())
                            .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{"heal", "stars", "strength", "stamina", "staminaRegen", "defence"}, builder))
                                .executes(CharacterCommands::checkCharacterState))))
        );
    }


    private static int addCharacter(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String argument = StringArgumentType.getString(ctx, "character");
        ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
        ModComponents.CHARACTERS.get(player).addCharacter(CharacterName.valueOf(argument));
        ctx.getSource().sendFeedback(() -> Text.literal(String.format("Character [%s] has been add to player inventory", argument)), false);
        //ctx.getSource().sendFeedback(() -> Text.literal(String.format("")), false);
        return 1;
    }

    private static int setCharacter(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String argument = StringArgumentType.getString(ctx, "character");
        ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
        int result = ModComponents.CHARACTERS.get(player).setCharacter(CharacterName.valueOf(argument));
        if (result !=1) {
            ctx.getSource().sendError(Text.literal("Player hasn`t this character"));
            return 0;
        }
        ArrayList<CharacterName> group = new ArrayList<>();
        group.add(CharacterName.valueOf(argument));
        BattleStateComponent component = ModComponents.BATTLE_STATE.get(player);
        CombatState state = component.getState();
        if (state==CombatState.NONE) {
            component.setState(CombatState.STANDART);
            if (new Battle(player).standartBattle(group)!=1)
                ctx.getSource().sendError(Text.literal("This character hasn`t weapon"));
        }
        return 1;
    }

    private static int removeCharacter(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String argument = StringArgumentType.getString(ctx, "character");
        ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
        ModComponents.CHARACTERS.get(player).removeCharacter(CharacterName.valueOf(argument));
        ctx.getSource().sendFeedback(() -> Text.literal(String.format("Character [%s] has been removed", argument)), false);
        return 1;
    }

    private static int checkCharacter(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String argument = StringArgumentType.getString(ctx, "character");
        ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
        boolean checker = ModComponents.CHARACTERS.get(player).hasCharacter(CharacterName.valueOf(argument));
        ctx.getSource().sendFeedback(() -> Text.literal(checker+": This player "+(checker?"has":"hasn`t")+" this character"), false);
        return 1;
    }

    private static int checkCharacterState(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String argument = StringArgumentType.getString(ctx, "character");
        String characteristic = StringArgumentType.getString(ctx, "state");
        ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
        Character chr = ModComponents.CHARACTERS.get(player).getCharacter(CharacterName.valueOf(argument));
        switch (characteristic) {
            case "heal" -> {
                float state = (float)chr.getHealReserve();
                ctx.getSource().sendFeedback(() -> Text.literal(String.format("Parameter %s is: %f", characteristic, state)), false);
            }
            case "stars" -> {
                float state = chr.getStars();
                ctx.getSource().sendFeedback(() -> Text.literal(String.format("Parameter %s is: %f", characteristic, state)), false);
            }
            case "stamina" -> {
                float state = (float)chr.getStamina();
                ctx.getSource().sendFeedback(() -> Text.literal(String.format("Parameter %s is: %f", characteristic, state)), false);
            }
            case "strength" -> {
                float state = chr.getStrength();
                ctx.getSource().sendFeedback(() -> Text.literal(String.format("Parameter %s is: %f", characteristic, state)), false);
            }
            case "staminaRegen" -> {
                float state = (float)chr.getStaminaRegen();
                ctx.getSource().sendFeedback(() -> Text.literal(String.format("Parameter %s is: %f", characteristic, state)), false);
            }
            case "defence" -> {
                float state = chr.getDefence();
                ctx.getSource().sendFeedback(() -> Text.literal(String.format("Parameter %s is: %f", characteristic, state)), false);
            }
        }
        return 1;
    }

    private static int editCharacter(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String argument = StringArgumentType.getString(ctx, "character");
        ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
        Character chr = ModComponents.CHARACTERS.get(player).getCharacter(CharacterName.valueOf(argument));
        String characteristic = StringArgumentType.getString(ctx, "state");
        switch (characteristic) {
            case "heal" -> {
                chr.setHealReserve((float) DoubleArgumentType.getDouble(ctx, "value"));
                ctx.getSource().sendFeedback(() -> Text.literal(String.format("Parameter %s seted to %s", characteristic, argument)), false);
            }
            case "stars" -> {
                chr.setStars((int) DoubleArgumentType.getDouble(ctx, "value"));
                ctx.getSource().sendFeedback(() -> Text.literal(String.format("Parameter %s seted to %s", characteristic, argument)), false);
            }
            case "stamina" -> {
                chr.setStamina((float) DoubleArgumentType.getDouble(ctx, "value"));
                ctx.getSource().sendFeedback(() -> Text.literal(String.format("Parameter %s seted to %s", characteristic, argument)), false);
            }
            case "strength" -> {
                chr.setStrength((int) DoubleArgumentType.getDouble(ctx, "value"));
                ctx.getSource().sendFeedback(() -> Text.literal(String.format("Parameter %s seted to %s", characteristic, argument)), false);
            }
            case "staminaRegen" -> {
                chr.setStaminaRegen((float) DoubleArgumentType.getDouble(ctx, "value"));
                ctx.getSource().sendFeedback(() -> Text.literal(String.format("Parameter %s seted to %s", characteristic, argument)), false);
            }
            case "defence" -> {
                chr.setDefence((int) DoubleArgumentType.getDouble(ctx, "value"));
                ctx.getSource().sendFeedback(() -> Text.literal(String.format("Parameter %s seted to %s", characteristic, argument)), false);
            }
        }
        return 1;
    }
}
