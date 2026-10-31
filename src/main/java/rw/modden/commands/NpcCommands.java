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
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import rw.modden.npc.ModEntities;
import rw.modden.npc.NpcEntity;

import java.util.ArrayList;
import java.util.List;
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
        );
    }

    private static int create(CommandContext<ServerCommandSource> ctx, String name) {
        String id = StringArgumentType.getString(ctx, "id");
        ServerCommandSource source = ctx.getSource();
        if (find(source, id) != null) {
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
        List<NpcEntity> npcs = all(ctx.getSource());
        if (npcs.isEmpty()) {
            ctx.getSource().sendFeedback(() -> Text.literal("No loaded NPCs"), false);
            return 0;
        }
        for(NpcEntity npc: npcs) {
            String line = String.format("%s — %s, %d %d %d", npc.getNpcId(), npc.getWorld().getRegistryKey().getValue(),
                    npc.getBlockX(), npc.getBlockY(), npc.getBlockZ());
            ctx.getSource().sendFeedback(() -> Text.literal(line), false);
        }
        return npcs.size();
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
        if (npc == null) ctx.getSource().sendError(Text.literal("NPC '" + id + "' not found (or its chunk is not loaded)"));
        return npc;
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
