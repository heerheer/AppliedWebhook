package top.realme.AppliedWebhook.data;

import com.alibaba.fastjson2.annotation.JSONField;
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
import org.jetbrains.annotations.Nullable;

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

    public static class LinkData {
        @Nullable
        public GlobalPos pos;   // 原版 AE2 用
        public long frequency;  // wtlib 量子频率，-1 = 未设置
    }

    @JSONField
    private final Map<UUID, LinkData> playerLinks = new HashMap<>();

    public Map<UUID, LinkData> getPlayerLinks() {
        return playerLinks;
    }

    // ---------- 加载 ----------
    public static PlayerAELinkStorage load(CompoundTag tag, HolderLookup.Provider provider) {
        PlayerAELinkStorage data = new PlayerAELinkStorage();

        var list = tag.getList("Links", Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);

            UUID player = entry.getUUID("Player");

            LinkData link = new LinkData();

            // 加载 AE2 原版位置（兼容旧存档）
            if (entry.contains("LinkPos", Tag.TAG_COMPOUND)) {
                link.pos = readGlobalPos(entry.getCompound("LinkPos"));
            }

            // 加载量子频率（可选）
            if (entry.contains("Frequency", Tag.TAG_LONG)) {
                link.frequency = entry.getLong("Frequency");
            } else {
                link.frequency = -1;
            }

            data.playerLinks.put(player, link);
        }

        return data;
    }

    // ---------- 保存 ----------
    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        var list = new net.minecraft.nbt.ListTag();

        for (var entry : playerLinks.entrySet()) {
            UUID player = entry.getKey();
            LinkData link = entry.getValue();

            CompoundTag e = new CompoundTag();
            e.putUUID("Player", player);

            // 保存 AE2 原版连接位置
            if (link.pos != null) {
                e.put("LinkPos", writeGlobalPos(link.pos));
            }

            // 保存量子频率
            if (link.frequency >= 0) {
                e.putLong("Frequency", link.frequency);
            }

            list.add(e);
        }

        tag.put("Links", list);
        return tag;
    }

    // ---------- 访问 ----------
    public void setPos(UUID player, @Nullable GlobalPos pos) {
        var link = playerLinks.computeIfAbsent(player, k -> new LinkData());
        link.pos = pos;
        setDirty();
    }

    public @Nullable GlobalPos getPos(UUID player) {
        var link = playerLinks.get(player);
        return link == null ? null : link.pos;
    }

    public void setFrequency(UUID player, long freq) {
        var link = playerLinks.computeIfAbsent(player, k -> new LinkData());
        link.frequency = freq;
        setDirty();
    }

    public long getFrequency(UUID player) {
        var link = playerLinks.get(player);
        return link == null ? -1 : link.frequency;
    }

    public boolean hasAny(UUID player) {
        var link = playerLinks.get(player);
        return link != null && (link.pos != null || link.frequency >= 0);
    }

    // ---------- 实例 ----------
    public static PlayerAELinkStorage get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        PlayerAELinkStorage::new,
                        PlayerAELinkStorage::load
                ),
                "ae2_player_links"
        );
    }

    // ---------- GlobalPos 写入/读取 ----------
    public static CompoundTag writeGlobalPos(GlobalPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Dimension", pos.dimension().location().toString());
        tag.put("Pos", NbtUtils.writeBlockPos(pos.pos()));
        return tag;
    }

    public static GlobalPos readGlobalPos(CompoundTag tag) {
        if (!tag.contains("Dimension")) return null;

        String dimId = tag.getString("Dimension");
        ResourceKey<Level> dimension =
                ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimId));

        Optional<BlockPos> pos = NbtUtils.readBlockPos(tag, "Pos");
        return pos.map(blockPos -> GlobalPos.of(dimension, blockPos)).orElse(null);
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(playerLinks);
    }

}
