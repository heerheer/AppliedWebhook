package top.realme.AppliedWebhook.ae;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import com.alibaba.fastjson2.annotation.JSONField;
import top.realme.AppliedWebhook.ae.model.AEKeyWithAmount;

import java.util.ArrayList;
import java.util.List;

public final class AEKeySearchUtils {

    private AEKeySearchUtils() {
        // 防止实例化
    }

    /**
     * 判断 KeyCounter 中是否存在名称包含指定字符串的 AEKey。
     */
    public static boolean containsName(String name, KeyCounter counter) {
        if (name == null || name.isBlank()) return false;

        String lower = name.toLowerCase();

        for (AEKey key : counter.keySet()) {
            if (matches(key, lower)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 按名称模糊搜索，返回所有匹配的 AEKey。
     */
    public static List<AEKey> searchByName(String name, KeyCounter counter) {
        List<AEKey> result = new ArrayList<>();
        if (name == null || name.isBlank()) return result;

        String lower = name.toLowerCase();

        for (AEKey key : counter.keySet()) {
            if (matches(key, lower)) {
                result.add(key);
            }
        }
        return result;
    }

    /**
     * 搜索并返回结果带数量信息。
     */
    public static List<AEKeyWithAmount> searchWithAmount(String name, KeyCounter counter) {
        List<AEKeyWithAmount> result = new ArrayList<>();
        if (name == null || name.isBlank()) return result;

        String lower = name.toLowerCase();

        for (AEKey key : counter.keySet()) {
            if (matches(key, lower)) {
                long amount = counter.get(key);
                result.add(new AEKeyWithAmount(key, amount));
            }
        }
        return result;
    }

    /**
     * 内部匹配方法：显示名 + registry id 都支持模糊搜索。
     */
    private static boolean matches(AEKey key, String lower) {
        // 显示名
        String dn = key.toString();

        if (dn != null && dn.toLowerCase().contains(lower)) {
            return true;
        }

        // registry name
        String id = key.getId().toString();
        return id.toLowerCase().contains(lower);
    }




    public static List<AEKeyWithAmount> topN(KeyCounter counter, int n) {
        return counter.keySet().stream()
                .map(key -> new AEKeyWithAmount(key, counter.get(key)))
                .sorted((a, b) -> Long.compare(b.amount(), a.amount())) // 按数量降序
                .limit(n)
                .toList();
    }
}
