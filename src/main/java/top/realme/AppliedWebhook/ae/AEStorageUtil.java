package top.realme.AppliedWebhook.ae;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import net.minecraft.world.item.ItemStack;
import top.realme.AppliedWebhook.ae.model.AEKeyWithAmount;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AEStorageUtil {

    /**
     * 查询存储大小（Items / Fluids）
     */
    public static CompletableFuture<Map<String, String>> queryStorageSize(UUID uuid) {
        return AEGridQueryUtil.grid(uuid).thenApply(grid -> {
            if (grid == null) return Map.of();

            var stacks = grid.getStorageService().getInventory().getAvailableStacks();
            BigInteger totalItems = BigInteger.ZERO;
            BigInteger totalFluids = BigInteger.ZERO;

            for (var key : stacks.keySet()) {
                long amount = stacks.get(key);

                if (key instanceof AEItemKey) {
                    totalItems = totalItems.add(BigInteger.valueOf(amount));
                } else if (key instanceof AEFluidKey) {
                    totalFluids = totalFluids.add(BigInteger.valueOf(amount));
                }
            }
            return Map.of(
                    "Items", totalItems.toString(),
                    "Fluids", totalFluids.toString()
            );
        });
    }

    /**
     * 查询某个物品数量
     */
    public static CompletableFuture<BigInteger> queryItemCount(UUID uuid, ItemStack probe) {
        return AEGridQueryUtil.grid(uuid).thenApply(grid -> {
            if (grid == null) return BigInteger.ZERO;

            var stacks = grid.getStorageService().getInventory().getAvailableStacks();
            var key = AEItemKey.of(probe);

            if (key == null) return BigInteger.ZERO;
            return BigInteger.valueOf(stacks.get(key));
        });
    }

    /**
     * 查询 AE 能量状况
     * （可根据你需求决定是否从 storage util 拆出去）
     */
    public static CompletableFuture<Map<String, Double>> queryEnergy(UUID uuid) {
        return AEGridQueryUtil.grid(uuid).thenApply(grid -> {
            if (grid == null) return Map.of();

            var service = grid.getEnergyService();
            return Map.of(
                    "stored_power", service.getStoredPower(),
                    "max_stored_power", service.getMaxStoredPower(),
                    "avg_power_usage", service.getAvgPowerUsage(),
                    "avg_power_injection", service.getAvgPowerInjection(),
                    "idle_power_usage", service.getIdlePowerUsage()
            );
        });
    }

    /**
     * 查询包含指定字符串相关的物品数量
     */
    public static CompletableFuture<List<AEKeyWithAmount>> searchProbeItems(UUID uuid, String probe) {
        return AEGridQueryUtil.grid(uuid).thenApply(grid -> {
            if (grid == null) return List.of();

            var stacks = grid.getStorageService().getInventory().getAvailableStacks();
            return AEKeySearchUtils.searchWithAmount(probe, stacks);
        });
    }

    /**
     * 查询最多可以存储的物品数量（Top N）
     */
    public static CompletableFuture<List<AEKeyWithAmount>> queryMaxThings(UUID uuid, int count) {
        return AEGridQueryUtil.grid(uuid).thenApply(grid -> {
            if (grid == null) return List.of();

            var stacks = grid.getStorageService().getInventory().getAvailableStacks();
            return AEKeySearchUtils.topN(stacks, count);
        });
    }
}
