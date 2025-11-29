package top.realme.AppliedWebhook.mixin;

import appeng.crafting.execution.CraftingCpuLogic;
import appeng.crafting.execution.ElapsedTimeTracker;
import appeng.crafting.execution.ExecutingCraftingJob;
import com.mojang.logging.LogUtils;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.realme.AppliedWebhook.ae.event.AECraftingJobFinishEvent;


@Mixin(CraftingCpuLogic.class)
public abstract class MixinCraftingCpuLogic {

    // Shadow AE2 的 job 字段（private）
    @Shadow
    private ExecutingCraftingJob job;

    // 影射 AE2 的方法（public 的可以直接调，也可以不 Shadow，直接 this.getElapsedTimeTracker()）
    @Shadow
    public abstract ElapsedTimeTracker getElapsedTimeTracker();


    /**
     * HEAD 注入：在 AE2 清除 job 之前拿到 job 数据
     */
    @Inject(method = "finishJob", at = @At("HEAD"))
    private void ae2_beforeFinish(boolean success, CallbackInfo ci) {
        LogUtils.getLogger().debug("[AWH Mixin]finishJob");

        ElapsedTimeTracker tracker = this.getElapsedTimeTracker();
        long time = 0;
        if (tracker != null) {
            time = tracker.getElapsedTime(); // 总时长（纳秒）
        }
        appliedWebhook$onCraftingJobFinished(job, success,time);

    }

    /**
     * 你自己的回调函数 —— 在这里处理 “任务完成” 或 “任务取消”
     */
    @Unique
    private void appliedWebhook$onCraftingJobFinished(ExecutingCraftingJob job, boolean success,long duration) {
        // 自定义的事件触发点
        var accessor = (ExecutingCraftingJobAccessor) job;
        NeoForge.EVENT_BUS.post(
                new AECraftingJobFinishEvent(
                        accessor.awh_getFinalOutput(),
                        accessor.awh_getPlayerId(),
                        accessor.awh_getRemainingAmount(),
                        success,
                        duration));

    }

}

