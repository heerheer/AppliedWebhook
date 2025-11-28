package top.realme.AppliedWebhook.ae.model;

import appeng.api.stacks.AEKey;
import com.alibaba.fastjson2.annotation.JSONField;
import top.realme.AppliedWebhook.ae.serialize.AEKeySerializer;

/**
 * key + 数量 的简单结构体
 */
public record AEKeyWithAmount(@JSONField(serializeUsing = AEKeySerializer.class) AEKey key, long amount) {
}