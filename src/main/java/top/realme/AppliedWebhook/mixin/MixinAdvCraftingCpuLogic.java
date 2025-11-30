package top.realme.AppliedWebhook.mixin;

import com.mojang.logging.LogUtils;
import net.neoforged.neoforge.common.NeoForge;
import net.pedroksl.advanced_ae.common.logic.ElapsedTimeTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.realme.AppliedWebhook.ae.event.AECraftingJobFinishEvent;

import net.pedroksl.advanced_ae.common.logic.AdvCraftingCPULogic;
import net.pedroksl.advanced_ae.common.logic.ExecutingCraftingJob;

@Pseudo
@Mixin(AdvCraftingCPULogic.class)
public abstract class MixinAdvCraftingCpuLogic {

    // Shadow AdvAE2 的 job 字段（private）类型和原版AE2 相同
    @Shadow
    private ExecutingCraftingJob job;

    // 这个是Advanced AE2 的 ElapsedTimeTracker
    @Shadow
    public abstract ElapsedTimeTracker getElapsedTimeTracker();

    /**
     * HEAD 注入：在 AE2 清除 job 之前拿到 job 数据
     */
    @Inject(method = "finishJob", at = @At("HEAD"))
    private void ae2_beforeFinish(boolean success, CallbackInfo ci) {
        LogUtils.getLogger().debug("[AWH Mixin] Adv finishJob");

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
        var accessor = (AdvExecutingCraftingJobAccessor) job;
        NeoForge.EVENT_BUS.post(
                new AECraftingJobFinishEvent(
                        accessor.awh_getFinalOutput(),
                        accessor.awh_getPlayerId(),
                        accessor.awh_getRemainingAmount(),
                        success,
                        duration));

    }

}

