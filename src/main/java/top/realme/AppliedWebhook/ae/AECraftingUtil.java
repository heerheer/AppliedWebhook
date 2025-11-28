package top.realme.AppliedWebhook.ae;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.AEKeyFilter;
import top.realme.AppliedWebhook.ae.model.AECpuInfo;
import top.realme.AppliedWebhook.ae.model.AEJobInfo;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AECraftingUtil {



    /**
     * 获取 CraftingService
     */
    private static ICraftingService service(IGrid grid) {
        return grid.getCraftingService();
    }

    /**
     * 查询某些物品是否可以合成,提供一个名字进行包含搜索
     */
    public static CompletableFuture<List<String>> canCraft(UUID uuid, String idProbe) {
        return AEGridQueryUtil.grid(uuid).thenApply(grid -> {
            if (grid == null) return List.of();

            var service = service(grid);
            return service.getCraftables((aeKey) -> aeKey.toString().contains(idProbe)).stream().map(k->k.toString()).toList();
        });
    }

    public static CompletableFuture<Map<String, AECpuInfo>> cpus(UUID uuid) {
        return AEGridQueryUtil.grid(uuid).thenApply(grid -> {
            if (grid == null) return Map.of();

            var service = service(grid);

            // 获取全部 CPU 集群
            var cpus = new ArrayList<>(service.getCpus());

            Map<String, AECpuInfo> result = new LinkedHashMap<>();

            // 生成 #1 #2 #3...
            for (int i = 0; i < cpus.size(); i++) {
                var cpu = cpus.get(i);
                var name = "#" + (i + 1);
                var busy = cpu.isBusy();

                var job = cpu.getJobStatus();
                if(busy && cpu.getJobStatus() != null){
                    var jobInfo = AEJobInfo.of(job.crafting().what(),
                            job.crafting().amount(),
                            job.totalItems(),
                            job.progress(),
                            job.elapsedTimeNanos());

                    result.put(name, new AECpuInfo(true,cpu.getSelectionMode().toString(),jobInfo));
                }
                else{
                    result.put(name, new AECpuInfo(false,cpu.getSelectionMode().toString(),null));
                }


            }

            return result;
        });
    }




//    /**
//     * 请求 AE2 进行合成（会创建 CraftingJob）
//     */
//    public static CompletableFuture<CraftingRequestResult> requestCrafting(UUID uuid, AEItemKey key, long amount) {
//        return AEGridQueryUtil.grid(uuid).thenApply(grid -> {
//            if (grid == null) {
//                return CraftingRequestResult.failed("Grid missing");
//            }
//
//            var service = service(grid);
//            try {
//                return service.beginCraftingJob(key, amount, null, Actionable.MODULATE);
//            } catch (Exception e) {
//                return CraftingRequestResult.failed(e.getMessage());
//            }
//        });
//    }
//
//    /**
//     * 模拟合成（不消耗资源）
//     * 适用于前端展示“缺什么”、“需要哪些原料”
//     */
//    public static CompletableFuture<CraftingRequestResult> simulateCrafting(UUID uuid, AEItemKey key, long amount) {
//        return AEGridQueryUtil.grid(uuid).thenApply(grid -> {
//            if (grid == null) {
//                return CraftingRequestResult.failed("Grid missing");
//            }
//
//            var service = service(grid);
//            try {
//                return service.beginCraftingJob(key, amount, null, Actionable.SIMULATE);
//            } catch (Exception e) {
//                return CraftingRequestResult.failed(e.getMessage());
//            }
//        });
//    }

//    /**
//     * 查询当前正在进行的 Crafting Job 列表
//     */
//    public static CompletableFuture<List<ICraftingLink>> listActiveJobs(UUID uuid) {
//        return AEGridQueryUtil.grid(uuid).thenApply(grid -> {
//            if (grid == null) return List.of();
//
//            return new ArrayList<>(service(grid).get().stream().map(
//
//                    cpu -> {
//                     cpu.
//                    }
//
//            ));
//        });
//    }
//
//    /**
//     * 查询是否有 job 在合成某物品
//     */
//    public static CompletableFuture<Boolean> isCrafting(UUID uuid, AEItemKey key) {
//        return listActiveJobs(uuid).thenApply(jobs ->
//                jobs.stream().anyMatch(job -> job.getWhat().equals(key))
//        );
//    }
}
