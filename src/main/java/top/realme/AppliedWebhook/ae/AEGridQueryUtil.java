package top.realme.AppliedWebhook.ae;

import appeng.api.networking.IGrid;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.me.helpers.IGridConnectedBlockEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import top.realme.AppliedWebhook.ae.model.AEKeyWithAmount;
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
            future.complete(null);
            return future;
        }

        // 将所有 Minecraft 相关操作放到主线程执行
        server.execute(() -> {
            ServerLevel overworld = server.getLevel(Level.OVERWORLD);
            if (overworld == null) {
                logger.error("Overworld missing.");
                future.complete(null);
                return;
            }

            PlayerAELinkStorage storage = PlayerAELinkStorage.get(overworld);
            GlobalPos global = storage.get(uuid);

            if (global == null) {
                logger.error("Player {} has no stored AE link.", uuid);
                future.complete(null);
                return;
            }

            BlockPos pos = global.pos();
            BlockEntity be = overworld.getBlockEntity(pos);

            logger.info("Found BlockEntity at {} => {}", pos, be);

            if (be instanceof IGridConnectedBlockEntity gcbe) {
                var grid = gcbe.getMainNode().getNode().getGrid();
                logger.info("AE Grid obtained: {}", grid);
                future.complete(grid);
            } else {
                logger.error("Block at {} is not AE grid-connected.", pos);
                future.complete(null);
            }
        });

        return future;
    }
}
