package top.realme.AppliedWebhook.ae;

import appeng.api.networking.IGrid;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.me.helpers.IGridConnectedBlockEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import top.realme.AppliedWebhook.ae.model.AEKeyWithAmount;
import top.realme.AppliedWebhook.ae.util.AEGridUtils;
import top.realme.AppliedWebhook.data.PlayerAELinkStorage;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AEGridQueryUtil {

    private static final Logger logger = LogUtils.getLogger();

    /**
     * 异步查询 AE Grid，返回 CompletableFuture
     */
    public static CompletableFuture<IGrid> grid(UUID uuid) {
        CompletableFuture<IGrid> future = new CompletableFuture<>();

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            logger.error("Server not available.");
            future.complete(null);
            return future;
        }

        // 在主线程执行，因为 AE2 的 Grid 操作必须在主线程
        server.execute(() -> {

            ServerLevel overworld = server.overworld();

            PlayerAELinkStorage storage = PlayerAELinkStorage.get(overworld);

            long freq = storage.getFrequency(uuid);
            GlobalPos global = storage.getPos(uuid);

            boolean wtlibInstalled = ModList.get().isLoaded("ae2wtlib");

            // ========== Ⅰ. 已安装 wtlib → 优先尝试 frequency ==========
            if (wtlibInstalled) {
                if (freq >= 0) {
                    logger.info("Player {} → trying wtlib frequency: {}", uuid, freq);

                    IGrid grid = AEGridUtils.tryFindGridByFrequency(server, freq);

                    if (grid != null) {
                        logger.info("Frequency → Grid success for {}", uuid);
                        future.complete(grid);
                        return;
                    } else {
                        logger.error("Frequency lookup failed for {} (freq={})", uuid, freq);
                    }
                } else {
                    logger.info("Player {} has no wtlib frequency saved.", uuid);
                }
            } else {
                logger.info("wtlib not installed, skipping frequency lookup.");
            }

            // ========== Ⅱ. 尝试使用 BlockPos 获取 AE Grid ==========
            if (global != null) {
                BlockPos pos = global.pos();
                ResourceKey<Level> dim = global.dimension();

                ServerLevel dimLevel = server.getLevel(dim);
                if (dimLevel == null) {
                    logger.error("Dimension {} missing for stored BlockPos of {}", dim, uuid);
                } else {
                    BlockEntity be = dimLevel.getBlockEntity(pos);
                    logger.info("Checking BlockEntity at {} in {} → {}", pos, dim, be);

                    if (be instanceof IGridConnectedBlockEntity gcbe) {
                        IGrid grid = gcbe.getMainNode().getNode().getGrid();
                        logger.info("BlockPos → Grid success for {}", uuid);
                        future.complete(grid);
                        return;
                    } else {
                        logger.error("Block at {} is not AE-grid-connected for {}", pos, uuid);
                    }
                }
            } else {
                logger.info("Player {} has no stored BlockPos.", uuid);
            }

            // ========== Ⅲ. 全部失败 ==========
            logger.error("[AE2Link] Failed to obtain AE Grid for player {} via both freq & BlockPos.", uuid);
            future.complete(null);

        });

        return future;
    }

}
