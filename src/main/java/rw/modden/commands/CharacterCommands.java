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
import rw.modden.Axorunelostworlds;
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
        dispatcher.register(literal("character")
            .requires(source -> source.hasPermissionLevel(4))
            .then(literal("add")
                .then(argument("character", StringArgumentType.word())
                    .suggests((context, builder) -> CommandSource.suggestMatching(characters, builder))
                        .then(argument("player", EntityArgumentType.player())
                            .executes(CharacterCommands::addCharacter))))

            .then(literal("set")
            .then(argument("character", StringArgumentType.word())
                .suggests((context, builder) -> CommandSource.suggestMatching(characters, builder))
                    .then(argument("player", EntityArgumentType.player())
                        .executes(CharacterCommands::setCharacter))))

            .then(literal("remove")
            .then(argument("character", StringArgumentType.word())
                .suggests((context, builder) -> CommandSource.suggestMatching(characters, builder))
                    .then(argument("player", EntityArgumentType.player())
                        .executes(CharacterCommands::removeCharacter))))

            .then(literal("edit")
            .then(argument("character", StringArgumentType.word())
                .suggests((context, builder) -> CommandSource.suggestMatching(characters, builder))
                    .then(argument("player", EntityArgumentType.player())
                        .then(argument("state", StringArgumentType.word())
                            .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{"heal", "stars", "level", "strength", "stamina", "staminaRegen", "healRegen", "defence", "weapon"}, builder))
                                .then(argument("value", DoubleArgumentType.doubleArg())
                                    .executes(CharacterCommands::editCharacter)))
                        .then(literal("weapon")
                            .then(argument("weaponID", StringArgumentType.greedyString())
                                .suggests((context, builder) -> CommandSource.suggestMatching(Axorunelostworlds.permitted_equipment,builder))
                                    .executes(CharacterCommands::editCharacter))))))

            .then(literal("check")
                .then(literal("character")
                    .then(argument("character", StringArgumentType.word())
                        .suggests((context, builder) -> CommandSource.suggestMatching(characters, builder))
                            .then(argument("player", EntityArgumentType.player())
                                .executes(CharacterCommands::checkCharacter))))
                    .then(literal("state")
                        .then(argument("character", StringArgumentType.word())
                            .suggests((context, builder) -> CommandSource.suggestMatching(characters, builder))
                                .then(argument("player", EntityArgumentType.player())
                                    .then(argument("state", StringArgumentType.word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{"heal", "stars", "level", "strength", "stamina", "staminaRegen", "healRegen", "defence", "weapon"}, builder))
                                            .executes(CharacterCommands::checkCharacterState))))))
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

    private static String stateOf(Character chr, String characteristic) {
        return switch (characteristic) {
            case "heal" -> String.valueOf(chr.getHealReserve());
            case "stars" -> String.valueOf(chr.getStars());
            case "level" -> String.valueOf(chr.getLevel());
            case "strength" -> String.valueOf(chr.getStrength());
            case "stamina" -> String.valueOf(chr.getStamina());
            case "staminaRegen" -> String.valueOf(chr.getStaminaRegen());
            case "healRegen" -> String.valueOf(chr.getHealRegen());
            case "defence" -> String.valueOf(chr.getDefence());
            case "weapon" -> chr.getWeapon()!=null?chr.getWeapon().getItemID():"none";
            default -> null;
        };
    }

    private static int checkCharacterState(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String argument = StringArgumentType.getString(ctx, "character");
        String characteristic = StringArgumentType.getString(ctx, "state");
        ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
        Character chr = ModComponents.CHARACTERS.get(player).getCharacter(CharacterName.valueOf(argument));
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
        Character chr = ModComponents.CHARACTERS.get(player).getCharacter(CharacterName.valueOf(argument));
        if (chr == null) {
            ctx.getSource().sendError(Text.literal("Player hasn`t this character"));
            return 0;
        }
        ctx.getNodes().forEach(x -> {
            if (x.getNode().getName().equals("weaponID")) {
                String weaponID = StringArgumentType.getString(ctx, "weaponID");
                chr.setWeapon(weaponID);
                ctx.getSource().sendFeedback(() -> Text.literal(String.format("Weapon %s seted to %s", weaponID, argument)), false);
            } else if (x.getNode().getName().equals("state")) {
                String characteristic = StringArgumentType.getString(ctx, "state");
                double value = DoubleArgumentType.getDouble(ctx, "value");
                switch (characteristic) {
                    case "heal" -> chr.setHealReserve((float) value);
                    case "stars" -> chr.setStars((int) value);
                    case "level" -> chr.setLevel((int) value);
                    case "strength" -> chr.setStrength((int) value);
                    case "stamina" -> chr.setStamina((float) value);
                    case "staminaRegen" -> chr.setStaminaRegen((float) value);
                    case "healRegen" -> chr.setHealRegen((float) value);
                    case "defence" -> chr.setDefence((int) value);
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
