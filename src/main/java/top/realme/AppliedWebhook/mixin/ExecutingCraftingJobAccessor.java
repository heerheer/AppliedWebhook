package top.realme.AppliedWebhook.mixin;

import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.execution.ExecutingCraftingJob;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(ExecutingCraftingJob.class)
public interface ExecutingCraftingJobAccessor {

    @Accessor("finalOutput")
    GenericStack awh_getFinalOutput();

    @Accessor("remainingAmount")
    long awh_getRemainingAmount();

    @Accessor("playerId")
    Integer awh_getPlayerId();

}
