package rw.modden.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import rw.modden.npc.ModEntities;
import rw.modden.npc.NpcEntity;
import rw.modden.npc.NpcRegistry;
import rw.modden.npc.dialogue.DialogueManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class NpcCommands {
    private static final double LOOK_DISTANCE = 16.0;

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("npc")
            .requires(source -> source.hasPermissionLevel(4))
            .then(literal("create")
                .then(argument("id", StringArgumentType.word())
                    .executes(ctx -> create(ctx, null))
                    .then(argument("name", StringArgumentType.greedyString())
                        .executes(ctx -> create(ctx, StringArgumentType.getString(ctx, "name"))))))
            .then(literal("remove")
                .then(argument("id", StringArgumentType.word()).suggests(NpcCommands::suggestLookedAt)
                    .executes(NpcCommands::remove)))
            .then(literal("list")
                .executes(NpcCommands::list))
            .then(literal("name")
                .then(argument("id", StringArgumentType.word()).suggests(NpcCommands::suggestLookedAt)
                    .then(argument("name", StringArgumentType.greedyString())
                        .executes(NpcCommands::name))))
            .then(literal("health")
                .then(argument("id", StringArgumentType.word()).suggests(NpcCommands::suggestLookedAt)
                    .then(literal("infinity")
                        .executes(ctx -> health(ctx, -1.0F)))
                    .then(argument("hp", FloatArgumentType.floatArg(1.0F))
                        .executes(ctx -> health(ctx, FloatArgumentType.getFloat(ctx, "hp"))))))
            .then(literal("ai")
                .then(argument("id", StringArgumentType.word()).suggests(NpcCommands::suggestLookedAt)
                    .then(literal("still")
                        .executes(ctx -> ai(ctx, NpcEntity.AiMode.STILL, 0)))
                    .then(literal("walking")
                        .then(argument("radius", IntegerArgumentType.integer(1, 64))
                            .executes(ctx -> ai(ctx, NpcEntity.AiMode.WALKING, IntegerArgumentType.getInteger(ctx, "radius")))))
                    .then(literal("patrol")
                        .executes(ctx -> ai(ctx, NpcEntity.AiMode.PATROL, 0)))))
            .then(literal("home")
                .then(argument("id", StringArgumentType.word()).suggests(NpcCommands::suggestLookedAt)
                    .executes(NpcCommands::home)))
            .then(literal("patrol")
                .then(argument("id", StringArgumentType.word()).suggests(NpcCommands::suggestLookedAt)
                    .then(literal("add")
                        .executes(NpcCommands::patrolAdd))
                    .then(literal("clear")
                        .executes(NpcCommands::patrolClear))
                    .then(literal("list")
                        .executes(NpcCommands::patrolList))
                    .then(literal("remove")
                        .then(argument("number", IntegerArgumentType.integer(1))
                            .executes(NpcCommands::patrolRemove)))
                    .then(literal("move")
                        .then(argument("number", IntegerArgumentType.integer(1))
                            .then(argument("to", IntegerArgumentType.integer(1))
                                .executes(NpcCommands::patrolMove))))))
            .then(literal("click")
                .then(argument("id", StringArgumentType.word()).suggests(NpcCommands::suggestLookedAt)
                    .then(literal("add")
                        .fork(dispatcher.getRoot(), NpcCommands::clickAdd))
                    .then(literal("list")
                        .executes(NpcCommands::clickList))
                    .then(literal("remove")
                        .then(argument("number", IntegerArgumentType.integer(1))
                            .executes(NpcCommands::clickRemove)))
                    .then(literal("clear")
                        .executes(NpcCommands::clickClear))))
            .then(literal("push")
                .then(argument("id", StringArgumentType.word()).suggests(NpcCommands::suggestLookedAt)
                    .then(argument("strength", FloatArgumentType.floatArg(0.0F, 1.0F))
                        .executes(NpcCommands::push))))
            .then(literal("knockback")
                .then(argument("id", StringArgumentType.word()).suggests(NpcCommands::suggestLookedAt)
                    .then(argument("strength", FloatArgumentType.floatArg(0.0F, 1.0F))
                        .executes(NpcCommands::knockback))))
            .then(literal("dialogue")
                .then(argument("id", StringArgumentType.word()).suggests(NpcCommands::suggestLookedAt)
                    .then(literal("set")
                        .then(argument("dialogue", StringArgumentType.word()).suggests((ctx, builder) -> CommandSource.suggestMatching(DialogueManager.getIds(), builder))
                            .executes(NpcCommands::dialogueSet)))
                    .then(literal("clear")
                        .executes(NpcCommands::dialogueClear))))
            .then(literal("reload")
                .executes(NpcCommands::reload))
            .then(literal("size")
                .then(argument("id", StringArgumentType.word()).suggests(NpcCommands::suggestLookedAt)
                    .then(argument("width", FloatArgumentType.floatArg(0.1F, 16.0F))
                        .then(argument("height", FloatArgumentType.floatArg(0.1F, 16.0F))
                            .executes(NpcCommands::size)))))
            .then(literal("gravity")
                .then(argument("id", StringArgumentType.word()).suggests(NpcCommands::suggestLookedAt)
                    .then(literal("on")
                        .executes(ctx -> gravity(ctx, true)))
                    .then(literal("off")
                        .executes(ctx -> gravity(ctx, false)))))
            .then(literal("tp")
                .then(argument("id", StringArgumentType.word()).suggests(NpcCommands::suggestAll)
                    .executes(NpcCommands::tp)))
        );
        dispatcher.register(literal("dialogue")
            .then(argument("step", IntegerArgumentType.integer(0))
                .then(argument("number", IntegerArgumentType.integer(1))
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        if (player != null) DialogueManager.answer(player, IntegerArgumentType.getInteger(ctx, "step"), IntegerArgumentType.getInteger(ctx, "number"));
                        return 1;
                    }))));
    }

    private static int create(CommandContext<ServerCommandSource> ctx, String name) {
        String id = StringArgumentType.getString(ctx, "id");
        ServerCommandSource source = ctx.getSource();
        NpcRegistry registry = NpcRegistry.get(source.getServer());
        if (find(source, id) != null || registry.get(id) != null) {
            source.sendError(Text.literal("NPC '" + id + "' already exists"));
            return 0;
        }
        NpcEntity npc = ModEntities.NPC.create(source.getWorld());
        if (npc == null) return 0;
        Vec3d pos = source.getPosition();
        npc.refreshPositionAndAngles(pos.x, pos.y, pos.z, source.getRotation().y, 0.0F);
        npc.setHeadYaw(source.getRotation().y);
        npc.setNpcId(id);
        npc.setHome(BlockPos.ofFloored(pos));
        if (name != null) {
            npc.setCustomName(Text.literal(name));
            npc.setCustomNameVisible(true);
        }
        source.getWorld().spawnEntity(npc);
        registry.update(npc);
        source.sendFeedback(() -> Text.literal("NPC '" + id + "' created"), true);
        return 1;
    }

    private static int remove(CommandContext<ServerCommandSource> ctx) {
        NpcEntity npc = findOrError(ctx);
        if (npc == null) return 0;
        npc.discard();
        ctx.getSource().sendFeedback(() -> Text.literal("NPC '" + npc.getNpcId() + "' removed"), true);
        return 1;
    }

    private static int list(CommandContext<ServerCommandSource> ctx) {
        NpcRegistry registry = NpcRegistry.get(ctx.getSource().getServer());
        List<String> loaded = new ArrayList<>();
        for(NpcEntity npc: all(ctx.getSource())) {
            registry.update(npc);
            loaded.add(npc.getNpcId());
        }
        if (registry.getAll().isEmpty()) {
            ctx.getSource().sendFeedback(() -> Text.literal("No NPCs"), false);
            return 0;
        }
        for(Map.Entry<String, NpcRegistry.Location> entry: registry.getAll().entrySet()) {
            String line = describe(entry.getKey(), entry.getValue()) + (loaded.contains(entry.getKey())?"":" (not loaded)");
            ctx.getSource().sendFeedback(() -> Text.literal(line), false);
        }
        return registry.getAll().size();
    }

    private static int name(CommandContext<ServerCommandSource> ctx) {
        NpcEntity npc = findOrError(ctx);
        if (npc == null) return 0;
        String name = StringArgumentType.getString(ctx, "name");
        npc.setCustomName(Text.literal(name));
        npc.setCustomNameVisible(true);
        ctx.getSource().sendFeedback(() -> Text.literal("NPC '" + npc.getNpcId() + "' renamed to " + name), true);
        return 1;
    }

    private static int health(CommandContext<ServerCommandSource> ctx, float hp) {
        NpcEntity npc = findOrError(ctx);
        if (npc == null) return 0;
        if (hp<0) npc.setInfiniteHealth();
        else npc.setNpcHealth(hp);
        String value = hp<0?"infinity":String.valueOf(hp);
        ctx.getSource().sendFeedback(() -> Text.literal("NPC '" + npc.getNpcId() + "' health: " + value), true);
        return 1;
    }

    private static int ai(CommandContext<ServerCommandSource> ctx, NpcEntity.AiMode mode, int radius) {
        NpcEntity npc = findOrError(ctx);
        if (npc == null) return 0;
        switch (mode) {
            case STILL -> npc.setStill();
            case WALKING -> npc.setWalking(radius);
            case PATROL -> npc.setPatrol();
        }
        String info = mode==NpcEntity.AiMode.WALKING?"walking, radius " + radius:mode.name().toLowerCase();
        ctx.getSource().sendFeedback(() -> Text.literal("NPC '" + npc.getNpcId() + "' ai: " + info), true);
        if (mode==NpcEntity.AiMode.PATROL && npc.getPatrolPoints().isEmpty())
            ctx.getSource().sendFeedback(() -> Text.literal("No patrol points yet: /npc patrol " + npc.getNpcId() + " add"), false);
        return 1;
    }

    private static int home(CommandContext<ServerCommandSource> ctx) {
        NpcEntity npc = findInSameWorld(ctx);
        if (npc == null) return 0;
        BlockPos pos = BlockPos.ofFloored(ctx.getSource().getPosition());
        npc.setHome(pos);
        ctx.getSource().sendFeedback(() -> Text.literal("NPC '" + npc.getNpcId() + "' home: " + pos.toShortString()), true);
        return 1;
    }

    private static int patrolAdd(CommandContext<ServerCommandSource> ctx) {
        NpcEntity npc = findInSameWorld(ctx);
        if (npc == null) return 0;
        BlockPos pos = BlockPos.ofFloored(ctx.getSource().getPosition());
        npc.addPatrolPoint(pos);
        int number = npc.getPatrolPoints().size();
        ctx.getSource().sendFeedback(() -> Text.literal("NPC '" + npc.getNpcId() + "' patrol point #" + number + ": " + pos.toShortString()), true);
        return 1;
    }

    private static int patrolClear(CommandContext<ServerCommandSource> ctx) {
        NpcEntity npc = findOrError(ctx);
        if (npc == null) return 0;
        npc.clearPatrolPoints();
        ctx.getSource().sendFeedback(() -> Text.literal("NPC '" + npc.getNpcId() + "' patrol points cleared"), true);
        return 1;
    }

    private static int patrolList(CommandContext<ServerCommandSource> ctx) {
        NpcEntity npc = findOrError(ctx);
        if (npc == null) return 0;
        List<BlockPos> points = npc.getPatrolPoints();
        if (points.isEmpty()) {
            ctx.getSource().sendFeedback(() -> Text.literal("NPC '" + npc.getNpcId() + "' has no patrol points"), false);
            return 0;
        }
        for(int i = 0; i<points.size(); i++) {
            String line = "#" + (i + 1) + ": " + points.get(i).toShortString();
            ctx.getSource().sendFeedback(() -> Text.literal(line), false);
        }
        return points.size();
    }

    private static int patrolRemove(CommandContext<ServerCommandSource> ctx) {
        NpcEntity npc = findOrError(ctx);
        if (npc == null) return 0;
        int number = IntegerArgumentType.getInteger(ctx, "number");
        if (!npc.removePatrolPoint(number - 1)) return badNumber(ctx, npc);
        ctx.getSource().sendFeedback(() -> Text.literal("NPC '" + npc.getNpcId() + "' patrol point #" + number + " removed"), true);
        return 1;
    }

    private static int patrolMove(CommandContext<ServerCommandSource> ctx) {
        NpcEntity npc = findOrError(ctx);
        if (npc == null) return 0;
        int from = IntegerArgumentType.getInteger(ctx, "number"), to = IntegerArgumentType.getInteger(ctx, "to");
        if (!npc.movePatrolPoint(from - 1, to - 1)) return badNumber(ctx, npc);
        ctx.getSource().sendFeedback(() -> Text.literal("NPC '" + npc.getNpcId() + "' patrol point #" + from + " moved to #" + to), true);
        return 1;
    }

    private static Collection<ServerCommandSource> clickAdd(CommandContext<ServerCommandSource> ctx) {
        NpcEntity npc = findOrError(ctx);
        if (npc == null) return List.of();
        String command = ctx.getInput().substring(ctx.getRange().getEnd()).trim();
        npc.addClickCommand(command);
        int number = npc.getClickCommands().size();
        ctx.getSource().sendFeedback(() -> Text.literal("NPC '" + npc.getNpcId() + "' click command #" + number + ": " + command), true);
        return List.of();
    }

    private static int clickList(CommandContext<ServerCommandSource> ctx) {
        NpcEntity npc = findOrError(ctx);
        if (npc == null) return 0;
        List<String> commands = npc.getClickCommands();
        if (commands.isEmpty()) {
            ctx.getSource().sendFeedback(() -> Text.literal("NPC '" + npc.getNpcId() + "' has no click commands"), false);
            return 0;
        }
        for(int i = 0; i<commands.size(); i++) {
            String line = "#" + (i + 1) + ": " + commands.get(i);
            ctx.getSource().sendFeedback(() -> Text.literal(line), false);
        }
        return commands.size();
    }

    private static int clickRemove(CommandContext<ServerCommandSource> ctx) {
        NpcEntity npc = findOrError(ctx);
        if (npc == null) return 0;
        int number = IntegerArgumentType.getInteger(ctx, "number");
        if (!npc.removeClickCommand(number - 1)) {
            ctx.getSource().sendError(Text.literal("NPC '" + npc.getNpcId() + "' has " + npc.getClickCommands().size() + " click command(s)"));
            return 0;
        }
        ctx.getSource().sendFeedback(() -> Text.literal("NPC '" + npc.getNpcId() + "' click command #" + number + " removed"), true);
        return 1;
    }

    private static int clickClear(CommandContext<ServerCommandSource> ctx) {
        NpcEntity npc = findOrError(ctx);
        if (npc == null) return 0;
        npc.clearClickCommands();
        ctx.getSource().sendFeedback(() -> Text.literal("NPC '" + npc.getNpcId() + "' click commands cleared"), true);
        return 1;
    }

    private static int push(CommandContext<ServerCommandSource> ctx) {
        NpcEntity npc = findOrError(ctx);
        if (npc == null) return 0;
        float strength = FloatArgumentType.getFloat(ctx, "strength");
        npc.setPushStrength(strength);
        ctx.getSource().sendFeedback(() -> Text.literal("NPC '" + npc.getNpcId() + "' push strength: " + strength), true);
        return 1;
    }

    private static int knockback(CommandContext<ServerCommandSource> ctx) {
        NpcEntity npc = findOrError(ctx);
        if (npc == null) return 0;
        float strength = FloatArgumentType.getFloat(ctx, "strength");
        npc.setKnockbackStrength(strength);
        ctx.getSource().sendFeedback(() -> Text.literal("NPC '" + npc.getNpcId() + "' knockback strength: " + strength), true);
        return 1;
    }

    private static int dialogueSet(CommandContext<ServerCommandSource> ctx) {
        NpcEntity npc = findOrError(ctx);
        if (npc == null) return 0;
        String dialogue = StringArgumentType.getString(ctx, "dialogue");
        if (!DialogueManager.exists(dialogue)) {
            ctx.getSource().sendError(Text.literal("Dialogue '" + dialogue + "' not loaded"));
            return 0;
        }
        npc.setDialogue(dialogue);
        ctx.getSource().sendFeedback(() -> Text.literal("NPC '" + npc.getNpcId() + "' dialogue: " + dialogue), true);
        return 1;
    }

    private static int dialogueClear(CommandContext<ServerCommandSource> ctx) {
        NpcEntity npc = findOrError(ctx);
        if (npc == null) return 0;
        npc.setDialogue("");
        ctx.getSource().sendFeedback(() -> Text.literal("NPC '" + npc.getNpcId() + "' dialogue cleared"), true);
        return 1;
    }

    private static int reload(CommandContext<ServerCommandSource> ctx) {
        List<String> errors = DialogueManager.reload();
        for(String error: errors) ctx.getSource().sendError(Text.literal(error));
        ctx.getSource().sendFeedback(() -> Text.literal("Dialogues loaded: " + DialogueManager.getIds().size() + ", errors: " + errors.size()), true);
        return errors.isEmpty()?1:0;
    }

    private static int size(CommandContext<ServerCommandSource> ctx) {
        NpcEntity npc = findOrError(ctx);
        if (npc == null) return 0;
        float width = FloatArgumentType.getFloat(ctx, "width"), height = FloatArgumentType.getFloat(ctx, "height");
        npc.setSize(width, height);
        ctx.getSource().sendFeedback(() -> Text.literal("NPC '" + npc.getNpcId() + "' size: " + width + " x " + height), true);
        return 1;
    }

    private static int gravity(CommandContext<ServerCommandSource> ctx, boolean gravity) {
        NpcEntity npc = findOrError(ctx);
        if (npc == null) return 0;
        npc.setNoGravity(!gravity);
        ctx.getSource().sendFeedback(() -> Text.literal("NPC '" + npc.getNpcId() + "' gravity: " + (gravity?"on":"off")), true);
        return 1;
    }

    private static int tp(CommandContext<ServerCommandSource> ctx) {
        String id = StringArgumentType.getString(ctx, "id");
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        NpcEntity npc = find(ctx.getSource(), id);
        NpcRegistry.Location location = npc != null?new NpcRegistry.Location(npc.getUuid(), npc.getWorld().getRegistryKey(), npc.getBlockPos()):NpcRegistry.get(ctx.getSource().getServer()).get(id);
        ServerWorld world = location == null?null:ctx.getSource().getServer().getWorld(location.world());
        if (world == null) {
            ctx.getSource().sendError(Text.literal("NPC '" + id + "' not found"));
            return 0;
        }
        BlockPos pos = location.pos();
        player.teleport(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYaw(), player.getPitch());
        ctx.getSource().sendFeedback(() -> Text.literal("Teleported to NPC '" + id + "'"), false);
        return 1;
    }

    private static int badNumber(CommandContext<ServerCommandSource> ctx, NpcEntity npc) {
        ctx.getSource().sendError(Text.literal("NPC '" + npc.getNpcId() + "' has " + npc.getPatrolPoints().size() + " patrol point(s)"));
        return 0;
    }

    private static NpcEntity findInSameWorld(CommandContext<ServerCommandSource> ctx) {
        NpcEntity npc = findOrError(ctx);
        if (npc == null) return null;
        if (npc.getWorld() != ctx.getSource().getWorld()) {
            ctx.getSource().sendError(Text.literal("NPC '" + npc.getNpcId() + "' is in another dimension"));
            return null;
        }
        return npc;
    }

    private static NpcEntity findOrError(CommandContext<ServerCommandSource> ctx) {
        String id = StringArgumentType.getString(ctx, "id");
        NpcEntity npc = find(ctx.getSource(), id);
        if (npc != null) return npc;
        NpcRegistry.Location location = NpcRegistry.get(ctx.getSource().getServer()).get(id);
        ctx.getSource().sendError(Text.literal(location == null?"NPC '" + id + "' not found"
                :"NPC '" + id + "' is not loaded, last seen: " + describe(id, location) + ". Use /npc tp " + id));
        return null;
    }

    private static String describe(String id, NpcRegistry.Location location) {
        BlockPos pos = location.pos();
        return String.format("%s — %s, %d %d %d", id, location.world().getValue(), pos.getX(), pos.getY(), pos.getZ());
    }

    public static NpcEntity find(ServerCommandSource source, String id) {
        for(NpcEntity npc: all(source)) if (npc.getNpcId().equals(id)) return npc;
        return null;
    }

    private static List<NpcEntity> all(ServerCommandSource source) {
        List<NpcEntity> result = new ArrayList<>();
        for(ServerWorld world: source.getServer().getWorlds())
            result.addAll(world.getEntitiesByType(ModEntities.NPC, npc -> true));
        return result;
    }

    private static CompletableFuture<Suggestions> suggestAll(CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(NpcRegistry.get(ctx.getSource().getServer()).getAll().keySet(), builder);
    }

    private static CompletableFuture<Suggestions> suggestLookedAt(CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        NpcEntity npc = lookedAt(ctx.getSource());
        if (npc != null && !npc.getNpcId().isEmpty()) builder.suggest(npc.getNpcId());
        return builder.buildFuture();
    }

    private static NpcEntity lookedAt(ServerCommandSource source) {
        Entity viewer = source.getEntity();
        if (viewer == null) return null;
        Vec3d start = viewer.getCameraPosVec(1.0F);
        Vec3d look = viewer.getRotationVec(1.0F);
        double distance = LOOK_DISTANCE;
        HitResult block = viewer.raycast(distance, 1.0F, false);
        if (block.getType() != HitResult.Type.MISS) distance = block.getPos().distanceTo(start);
        Vec3d end = start.add(look.multiply(distance));
        Box box = viewer.getBoundingBox().stretch(look.multiply(distance)).expand(1.0);
        EntityHitResult hit = ProjectileUtil.raycast(viewer, start, end, box, entity -> entity instanceof NpcEntity, distance*distance);
        return hit != null?(NpcEntity) hit.getEntity():null;
    }
}
