package top.realme.AppliedWebhook.mixin;

import appeng.api.stacks.GenericStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.pedroksl.advanced_ae.common.logic.ExecutingCraftingJob;
import top.realme.AppliedWebhook.BaseExecutingCraftingJobAccessor;

@Mixin(ExecutingCraftingJob.class)
public interface AdvExecutingCraftingJobAccessor extends BaseExecutingCraftingJobAccessor {

    @Accessor("finalOutput")
    GenericStack awh_getFinalOutput();

    @Accessor("remainingAmount")
    long awh_getRemainingAmount();

    @Accessor("playerId")
    Integer awh_getPlayerId();

}