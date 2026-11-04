package rw.modden.npc;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class NpcRegistry extends PersistentState {
    private static final String KEY = "axorunelostworlds_npcs";
    private final Map<String, Location> locations = new TreeMap<>();

    public record Location(UUID uuid, RegistryKey<World> world, BlockPos pos) {}

    public static NpcRegistry get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(NpcRegistry::fromNbt, NpcRegistry::new, KEY);
    }

    public Location get(String id) { return locations.get(id); }
    public Map<String, Location> getAll() { return locations; }
    public void update(NpcEntity npc) {
        Location location = new Location(npc.getUuid(), npc.getWorld().getRegistryKey(), npc.getBlockPos());
        if (!location.equals(locations.put(npc.getNpcId(), location))) markDirty();
    }
    public void remove(NpcEntity npc) {
        Location location = locations.get(npc.getNpcId());
        if (location == null || !location.uuid().equals(npc.getUuid())) return;
        locations.remove(npc.getNpcId());
        markDirty();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        for(Map.Entry<String, Location> entry: locations.entrySet()) {
            NbtCompound tag = new NbtCompound();
            tag.putUuid("Uuid", entry.getValue().uuid());
            tag.putString("World", entry.getValue().world().getValue().toString());
            tag.put("Pos", NbtHelper.fromBlockPos(entry.getValue().pos()));
            nbt.put(entry.getKey(), tag);
        }
        return nbt;
    }
    private static NpcRegistry fromNbt(NbtCompound nbt) {
        NpcRegistry registry = new NpcRegistry();
        for(String id: nbt.getKeys()) {
            NbtCompound tag = nbt.getCompound(id);
            RegistryKey<World> world = RegistryKey.of(RegistryKeys.WORLD, new Identifier(tag.getString("World")));
            registry.locations.put(id, new Location(tag.getUuid("Uuid"), world, NbtHelper.toBlockPos(tag.getCompound("Pos"))));
        }
        return registry;
    }
}
