package top.realme.AppliedWebhook.ae.event;

import appeng.api.features.IPlayerRegistry;
import appeng.api.stacks.GenericStack;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import top.realme.AppliedWebhook.ae.model.AEKeyWithAmount;

import java.util.UUID;

public class AECraftingJobFinishEvent extends Event {

    private final GenericStack genericStack;
    private final Integer playerId;
    private final long remain;
    private final boolean success;

    /**
     * 合成持续时间（单位：纳秒）
     */
    private final long duration;

    /**
     * 当 AE2 完成一个 crafting job 时触发
     * 你可以在事件监听器中获取到合成结果、玩家 UUID、剩余物品数量等信息
     * @param genericStack 合成结果中的物品
     * @param playerId 玩家的 ID
     * @param remain 合成完成后剩余的物品数量
     * @param success 是否成功完成
     * @param duration 合成持续时间（单位：纳秒）
     */
    public AECraftingJobFinishEvent(GenericStack genericStack, Integer playerId, long remain, boolean success, long duration) {
        this.genericStack = genericStack;
        this.playerId = playerId;
        this.remain = remain;
        this.success = success;
        this.duration = duration;
    }

    /**
     * 获取 crafting job 中玩家的 UUID
     * @return
     */
    public UUID playerUUID(){

        if(playerId == null){
            return null;
        }
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return IPlayerRegistry.getMapping(server).getProfileId(playerId);
        }
        return null;
    }

    /**
     * 获取 crafting job 中最后的得到的物品与数量
     * 使用.key().toString()获取物品的 ID
     * @return
     */
    public AEKeyWithAmount get(){
        return new AEKeyWithAmount(genericStack.what(), genericStack.amount());
    }

     /**
     * 获取 crafting job 中是否成功完成
     * @return
     */
    public boolean isSuccess(){
        return success;
    }

     /**
     * 获取 crafting job 中剩余的物品数量
     * @return
     */
    public long remain(){
        return remain;
    }

     /**
     * 获取 crafting job 中持续的时间（单位：毫秒）
     * @return
     */
    public long duration(){
        return duration;
    }

}
