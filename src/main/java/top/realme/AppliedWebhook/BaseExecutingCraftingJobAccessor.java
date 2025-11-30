package top.realme.AppliedWebhook;

import appeng.api.stacks.GenericStack;

public interface BaseExecutingCraftingJobAccessor {

    GenericStack awh_getFinalOutput();
    long awh_getRemainingAmount();
    Integer awh_getPlayerId();
}