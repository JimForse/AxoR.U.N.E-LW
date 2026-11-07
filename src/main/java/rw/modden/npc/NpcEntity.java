package rw.modden.npc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.GoToWalkTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import rw.modden.npc.dialogue.DialogueManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NpcEntity extends PathAwareEntity {
    public enum AiMode { STILL, WALKING, PATROL }
    private static final int CLICK_COOLDOWN = 20, REGISTRY_UPDATE_TICKS = 100;
    private static final TrackedData<Float> WIDTH = DataTracker.registerData(NpcEntity.class, TrackedDataHandlerRegistry.FLOAT),
            HEIGHT = DataTracker.registerData(NpcEntity.class, TrackedDataHandlerRegistry.FLOAT);

    private String npcId = "", dialogue = "";
    private boolean infiniteHealth = true;
    private float pushStrength;
    private AiMode aiMode = AiMode.STILL;
    private BlockPos home = BlockPos.ORIGIN;
    private int walkingRadius, patrolIndex;
    private final List<BlockPos> patrolPoints = new ArrayList<>();
    private final List<String> clickCommands = new ArrayList<>();
    private final Map<UUID, Integer> lastClick = new HashMap<>();

    public NpcEntity(EntityType<? extends PathAwareEntity> type, World world) {
        super(type, world);
        setPersistent();
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(WIDTH, 0.6F);
        dataTracker.startTracking(HEIGHT, 1.8F);
    }

    public float getWidthSize() { return dataTracker.get(WIDTH); }
    public float getHeightSize() { return dataTracker.get(HEIGHT); }
    public void setSize(float width, float height) {
        dataTracker.set(WIDTH, width);
        dataTracker.set(HEIGHT, height);
    }
    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (WIDTH.equals(data) || HEIGHT.equals(data)) calculateDimensions();
    }
    @Override
    public EntityDimensions getDimensions(EntityPose pose) { return EntityDimensions.changing(getWidthSize(), getHeightSize()); }
    @Override
    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) { return dimensions.height*0.9F; }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(1, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        goalSelector.add(2, new NpcPatrolGoal(this, 0.6));
        goalSelector.add(3, new GoToWalkTargetGoal(this, 0.6) {
            @Override
            public boolean canStart() { return aiMode==AiMode.WALKING && super.canStart(); }
        });
        goalSelector.add(4, new WanderAroundGoal(this, 0.6) {
            @Override
            public boolean canStart() { return aiMode==AiMode.WALKING && super.canStart(); }
            @Override
            public boolean shouldContinue() { return aiMode==AiMode.WALKING && super.shouldContinue(); }
        });
        goalSelector.add(5, new LookAroundGoal(this));
    }

    public String getNpcId() { return npcId; }
    public void setNpcId(String npcId) { this.npcId = npcId; }
    public boolean hasInfiniteHealth() { return infiniteHealth; }
    public void setInfiniteHealth() {
        infiniteHealth = true;
        setHealth(getMaxHealth());
    }
    public void setNpcHealth(float health) {
        infiniteHealth = false;
        getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(health);
        setHealth(health);
    }

    public AiMode getAiMode() { return aiMode; }
    public int getWalkingRadius() { return walkingRadius; }
    public BlockPos getHome() { return home; }
    public void setStill() {
        aiMode = AiMode.STILL;
        clearPositionTarget();
        getNavigation().stop();
    }
    public void setWalking(int radius) {
        aiMode = AiMode.WALKING;
        walkingRadius = radius;
        setPositionTarget(home, radius);
    }
    public void setPatrol() {
        aiMode = AiMode.PATROL;
        clearPositionTarget();
        getNavigation().stop();
    }
    public void setHome(BlockPos pos) {
        home = pos;
        if (aiMode==AiMode.WALKING) setPositionTarget(home, walkingRadius);
    }

    public List<BlockPos> getPatrolPoints() { return patrolPoints; }
    public void addPatrolPoint(BlockPos pos) { patrolPoints.add(pos); }
    public void clearPatrolPoints() {
        patrolPoints.clear();
        patrolIndex = 0;
        getNavigation().stop();
    }
    public boolean removePatrolPoint(int index) {
        if (index<0 || index>=patrolPoints.size()) return false;
        BlockPos current = currentPatrolPoint();
        patrolPoints.remove(index);
        keepTarget(current);
        return true;
    }
    public boolean movePatrolPoint(int from, int to) {
        if (from<0 || from>=patrolPoints.size() || to<0 || to>=patrolPoints.size()) return false;
        BlockPos current = currentPatrolPoint();
        patrolPoints.add(to, patrolPoints.remove(from));
        keepTarget(current);
        return true;
    }
    private void keepTarget(BlockPos current) {
        int index = patrolPoints.indexOf(current);
        patrolIndex = index>=0?index:patrolPoints.isEmpty()?0:patrolIndex % patrolPoints.size();
        getNavigation().stop();
    }
    public BlockPos currentPatrolPoint() { return patrolPoints.get(patrolIndex % patrolPoints.size()); }
    public void nextPatrolPoint() { patrolIndex = (patrolIndex + 1) % patrolPoints.size(); }

    public List<String> getClickCommands() { return clickCommands; }
    public void addClickCommand(String command) { clickCommands.add(command); }
    public void clearClickCommands() { clickCommands.clear(); }
    public boolean removeClickCommand(int index) {
        if (index<0 || index>=clickCommands.size()) return false;
        clickCommands.remove(index);
        return true;
    }

    public String getDialogue() { return dialogue; }
    public void setDialogue(String dialogue) { this.dialogue = dialogue; }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
        if (player instanceof ServerPlayerEntity serverPlayer) onClick(serverPlayer);
        return ActionResult.success(getWorld().isClient);
    }
    @Override
    public boolean canBeLeashedBy(PlayerEntity player) { return false; }

    private void onClick(ServerPlayerEntity player) {
        if (age - lastClick.getOrDefault(player.getUuid(), -CLICK_COOLDOWN)<CLICK_COOLDOWN) return;
        lastClick.put(player.getUuid(), age);
        runCommands(player, clickCommands);
        if (!dialogue.isEmpty()) DialogueManager.start(player, dialogue, getDisplayName().getString());
    }

    public static void runCommands(ServerPlayerEntity player, List<String> commands) {
        if (commands == null || commands.isEmpty()) return;
        MinecraftServer server = player.getServer();
        ServerCommandSource source = server.getCommandSource().withEntity(player).withPosition(player.getPos())
                .withRotation(player.getRotationClient()).withWorld(player.getServerWorld()).withSilent();
        for(String command: commands) server.getCommandManager().executeWithPrefix(source, command);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (infiniteHealth && !source.isOf(DamageTypes.GENERIC_KILL) && !source.isOf(DamageTypes.OUT_OF_WORLD)) return true;
        return super.isInvulnerableTo(source);
    }
    public float getPushStrength() { return pushStrength; }
    public void setPushStrength(float strength) { pushStrength = strength; }
    public void setKnockbackStrength(float strength) { getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1.0F - strength); }

    @Override
    public boolean isPushable() { return pushStrength>0; }
    @Override
    protected void pushAway(Entity entity) { pushAwayFrom(entity); }
    @Override
    public void pushAwayFrom(Entity entity) {
        Vec3d before = getVelocity();
        super.pushAwayFrom(entity);
        setVelocity(before.add(getVelocity().subtract(before).multiply(pushStrength)));
    }

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isClient && !npcId.isEmpty() && age % REGISTRY_UPDATE_TICKS==0) NpcRegistry.get(getServer()).update(this);
    }
    @Override
    public void remove(RemovalReason reason) {
        if (!getWorld().isClient && !npcId.isEmpty()) {
            NpcRegistry registry = NpcRegistry.get(getServer());
            if (reason.shouldDestroy()) registry.remove(this);
            else registry.update(this);
        }
        super.remove(reason);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString("NpcId", npcId);
        nbt.putString("Dialogue", dialogue);
        nbt.putBoolean("InfiniteHealth", infiniteHealth);
        nbt.putFloat("PushStrength", pushStrength);
        nbt.putFloat("Width", getWidthSize());
        nbt.putFloat("Height", getHeightSize());
        nbt.putString("AiMode", aiMode.name());
        nbt.put("Home", NbtHelper.fromBlockPos(home));
        nbt.putInt("WalkingRadius", walkingRadius);
        NbtList points = new NbtList();
        for(BlockPos point: patrolPoints) points.add(NbtHelper.fromBlockPos(point));
        nbt.put("PatrolPoints", points);
        nbt.putInt("PatrolIndex", patrolIndex);
        NbtList commands = new NbtList();
        for(String command: clickCommands) commands.add(NbtString.of(command));
        nbt.put("ClickCommands", commands);
    }
    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        npcId = nbt.getString("NpcId");
        dialogue = nbt.getString("Dialogue");
        infiniteHealth = !nbt.contains("InfiniteHealth") || nbt.getBoolean("InfiniteHealth");
        pushStrength = nbt.getFloat("PushStrength");
        if (nbt.contains("Width")) setSize(nbt.getFloat("Width"), nbt.getFloat("Height"));
        aiMode = nbt.contains("AiMode")?AiMode.valueOf(nbt.getString("AiMode")):AiMode.STILL;
        home = nbt.contains("Home")?NbtHelper.toBlockPos(nbt.getCompound("Home")):getBlockPos();
        walkingRadius = nbt.getInt("WalkingRadius");
        patrolPoints.clear();
        for(NbtElement point: nbt.getList("PatrolPoints", NbtElement.COMPOUND_TYPE)) patrolPoints.add(NbtHelper.toBlockPos((NbtCompound) point));
        patrolIndex = nbt.getInt("PatrolIndex");
        clickCommands.clear();
        for(NbtElement command: nbt.getList("ClickCommands", NbtElement.STRING_TYPE)) clickCommands.add(command.asString());
        if (aiMode==AiMode.WALKING) setPositionTarget(home, walkingRadius);
    }
}