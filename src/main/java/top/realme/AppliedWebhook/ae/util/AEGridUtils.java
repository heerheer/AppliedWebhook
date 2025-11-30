package top.realme.AppliedWebhook.ae.util;

import appeng.api.features.Locatables;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionHost;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Objects;

public class AEGridUtils {
    public static Logger logger = LogUtils.getLogger();

    /**
     * wtlib 频率查找 Grid
     * 若未安装 wtlib 或 freq 不存在则返回 null
     */
    public static @Nullable IGrid tryFindGridByFrequency(MinecraftServer server, long freq) {
        if (!ModList.get().isLoaded("ae2wtlib")) {
            return null;
        }

        try {
            // 调用 wtlib 的 API：AE2wtlibAPI.getGridByFrequency(long)
            IActionHost quantumBridge = Locatables.quantumNetworkBridges().get(server.overworld(), freq);
            if (quantumBridge == null)
                quantumBridge = Locatables.quantumNetworkBridges().get(server.overworld(), -freq);
            if (quantumBridge == null)
                return null;
            return Objects.requireNonNull(quantumBridge.getActionableNode()).getGrid();

        } catch (Exception e) {
            logger.error("[AE2Link] wtlib frequency lookup error: {}", e.toString());
            return null;
        }
    }
}
