package top.realme.AppliedWebhook.ae.model;

import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.stacks.KeyCounter;

import java.util.HashMap;
import java.util.Map;

/**
 *  对ICraftingPlan的包装类，用于获取计划中需要的物品数量
 */
public class AECratingPlan
{

    private Map<String,Long> mapKeyCounter(KeyCounter keyCounter){

        Map<String,Long> map = new HashMap<>();
        for (var entry : keyCounter) {
            map.put(entry.getKey().toString(), entry.getLongValue());
        }
        return map;
    }

    private final ICraftingPlan plan;

    public AECratingPlan(ICraftingPlan plan)
    {
        this.plan = plan;
    }

    /**
     * 占用字节
     * @return
     */
    public long getBytes()
    {
        return plan.bytes();
    }

     /**
      * 缺失物品
      * @return
      */
     public Map<String , Long> getMissingItems()
    {
        return mapKeyCounter(plan.missingItems());
    }

    public Map<String , Long> getUsedItems()
    {
        return mapKeyCounter(plan.usedItems());
    }

    public Map<String , Long> getEmittedItems()
    {
        return mapKeyCounter(plan.emittedItems());
    }

    public boolean getIsSimulation(){
        return plan.simulation();
    }

    public Map<String , Long> getFinalOutputItems()
    {
        Map<String,Long> map = new HashMap<>();
        map.put(plan.finalOutput().what().toString(), plan.finalOutput().amount());
        return map;
    }
}
