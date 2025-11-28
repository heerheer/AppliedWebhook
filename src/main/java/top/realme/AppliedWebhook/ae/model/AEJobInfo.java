package top.realme.AppliedWebhook.ae.model;

import appeng.api.stacks.AEKey;
import com.alibaba.fastjson2.annotation.JSONField;
import top.realme.AppliedWebhook.ae.serialize.AEKeySerializer;

public record AEJobInfo(
        @JSONField(serializeUsing = AEKeySerializer.class)
        /*
          任务物品,序列化为物品ID
         */
        AEKey jobItem,

        /*
          任务物品数量
         */
        Long itemCount,
        /*
          任务总进度
         */
        long totalProgress,
        /*
          任务当前进度
         */
        long currentProgress,
        /*
          任务已完成时间,单位为纳秒
         */
        long elapsedTimeNanos) {
    public static AEJobInfo of(AEKey jobItem,long amount, long totalProgress, long currentProgress, long elapsedTimeNanos) {
        return new AEJobInfo(jobItem,amount, totalProgress, currentProgress, elapsedTimeNanos);
    }
}
