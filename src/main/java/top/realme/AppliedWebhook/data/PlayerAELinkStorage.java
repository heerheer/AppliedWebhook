package top.realme.AppliedWebhook.data;

import com.google.gson.Gson;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.nbt.NbtUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 储存为tag:
 * {
 *     "Links": [
 *         {
 *             "Player": "00000000-0000-0000-0000-000000000000",
 *             "LinkPos": {
 *                 "Dimension": "minecraft:overworld",
 *                 "Pos": [0, 64, 0]
 *             }
 *         }
 *     ]
 * }
 */
public class PlayerAELinkStorage extends SavedData {

    private final Map<UUID, GlobalPos> playerLinks = new HashMap<>();

    // ---------- 加载 ----------
    public static PlayerAELinkStorage load(CompoundTag tag, HolderLookup.Provider provider) {
        PlayerAELinkStorage data = new PlayerAELinkStorage();

        var list = tag.getList("Links", Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);

            UUID player = entry.getUUID("Player");
            GlobalPos pos = readGlobalPos(entry.getCompound("LinkPos"));
            if (pos == null) {
                continue; // 跳过无效位置
            }

            data.playerLinks.put(player, pos);
        }

        return data;
    }
    // ---------- 保存 ----------
    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        var list = new net.minecraft.nbt.ListTag();

        for (var entry : playerLinks.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.putUUID("Player", entry.getKey());
            e.put("LinkPos", writeGlobalPos(entry.getValue()));
            list.add(e);
        }

        tag.put("Links", list);
        return tag;
    }

    // ---------- 访问 ----------
    public void set(UUID player, GlobalPos pos) {
        playerLinks.put(player, pos);
        setDirty(); // 标记数据需要保存
    }

    public GlobalPos get(UUID player) {
        return playerLinks.get(player);
    }

    public boolean has(UUID player) {
        return playerLinks.containsKey(player);
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(playerLinks);
    }

    // ---------- 获取实例 ----------
    public static PlayerAELinkStorage get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        PlayerAELinkStorage::new,
                        PlayerAELinkStorage::load
                ),
                "ae2_player_links"
        );
    }

    /**
     * 将全局位置转为一个包含维度和pos的tag
     * @param pos
     * @return
     */
    public static CompoundTag writeGlobalPos(GlobalPos pos) {
        CompoundTag tag = new CompoundTag();

        // 写维度 ID，例如 "minecraft:overworld"
        tag.putString("Dimension", pos.dimension().location().toString());

        // 写 BlockPos
        tag.put("Pos", NbtUtils.writeBlockPos(pos.pos()));

        return tag;
    }

    /**
     * 从LinkPos对象中读取全局位置
     * @param tag
     * @return
     */
    public static GlobalPos readGlobalPos(CompoundTag tag) {
        if (!tag.contains("Dimension")) {
            return null;
        }
        String dimId = tag.getString("Dimension");
        ResourceKey<Level> dimension =
                ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimId));

        Optional<BlockPos> pos = NbtUtils.readBlockPos(tag,"Pos");
        // 缺失 Pos 或格式错误
        return pos.map(blockPos -> GlobalPos.of(dimension, blockPos)).orElse(null);
    }

}