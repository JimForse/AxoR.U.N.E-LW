package rw.modden.npc;

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
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class NpcEntity extends PathAwareEntity {
    public enum AiMode { STILL, WALKING, PATROL }

    private String npcId = "";
    private boolean infiniteHealth = true;
    private AiMode aiMode = AiMode.STILL;
    private BlockPos home = BlockPos.ORIGIN;
    private int walkingRadius, patrolIndex;
    private final List<BlockPos> patrolPoints = new ArrayList<>();

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

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (infiniteHealth && !source.isOf(DamageTypes.GENERIC_KILL) && !source.isOf(DamageTypes.OUT_OF_WORLD)) return true;
        return super.isInvulnerableTo(source);
    }
    @Override
    public boolean isPushable() { return false; }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString("NpcId", npcId);
        nbt.putBoolean("InfiniteHealth", infiniteHealth);
        nbt.putString("AiMode", aiMode.name());
        nbt.put("Home", NbtHelper.fromBlockPos(home));
        nbt.putInt("WalkingRadius", walkingRadius);
        NbtList points = new NbtList();
        for(BlockPos point: patrolPoints) points.add(NbtHelper.fromBlockPos(point));
        nbt.put("PatrolPoints", points);
        nbt.putInt("PatrolIndex", patrolIndex);
    }
    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        npcId = nbt.getString("NpcId");
        infiniteHealth = !nbt.contains("InfiniteHealth") || nbt.getBoolean("InfiniteHealth");
        aiMode = nbt.contains("AiMode")?AiMode.valueOf(nbt.getString("AiMode")):AiMode.STILL;
        home = nbt.contains("Home")?NbtHelper.toBlockPos(nbt.getCompound("Home")):getBlockPos();
        walkingRadius = nbt.getInt("WalkingRadius");
        patrolPoints.clear();
        for(NbtElement point: nbt.getList("PatrolPoints", NbtElement.COMPOUND_TYPE)) patrolPoints.add(NbtHelper.toBlockPos((NbtCompound) point));
        patrolIndex = nbt.getInt("PatrolIndex");
        if (aiMode==AiMode.WALKING) setPositionTarget(home, walkingRadius);
    }
}
