package top.realme.AppliedWebhook.http.model;

import java.util.UUID;

/**
 * 用于 Crafting Job 结束时发送的数据载荷。
 *
 * @param playerId   玩家 UUID（可能为 null，如果不是玩家触发）
 * @param itemId     合成物品的 AE2 Key 或物品 ID（根据你事件里拿到的数据来填）
 * @param amount     本次合成完成的数量
 * @param durationMs 本次任务持续时间（毫秒）
 */
public record JobFinishPayload(UUID playerId, String itemId, long amount, long durationMs, boolean isOnline,String playerName) {


}
