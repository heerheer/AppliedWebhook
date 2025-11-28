package top.realme.AppliedWebhook.command;

import appeng.items.tools.powered.WirelessTerminalItem;
import com.mojang.logging.LogUtils;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.realme.AppliedWebhook.data.PlayerAELinkStorage;
import top.realme.AppliedWebhook.ae.AEGridQueryUtil;

public class LogicAEWebhook {

    // 绑定 AE2 网络
    public static void bind(ServerPlayer player) {

        ItemStack stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof WirelessTerminalItem terminalItem)) {
            player.sendSystemMessage(Component.literal("你手中没有任何可以连接到 AE2 网络的无线连接器。"));
            return;
        }

        // 从无线终端获取对应网络
        var grid = terminalItem.getLinkedGrid(stack,player.serverLevel(),null);

        if (grid == null) {
            player.sendSystemMessage(Component.literal("无线终端无法连接到 AE2 网络。"));
            return;
        }

        // 获取终端绑定位置（GlobalPos）
        GlobalPos linkedPos = terminalItem.getLinkedPosition(stack);

        // ★★★ 使用我们的持久化存储 ★★★
        PlayerAELinkStorage storage = PlayerAELinkStorage.get(player.serverLevel());
        storage.set(player.getUUID(), linkedPos); // 保存持久化数据


        player.sendSystemMessage(Component.literal("已将您绑定至 AE2 网络，连接位置: " + linkedPos));
    }

    public static void info(ServerPlayer player) {
        LogUtils.getLogger().info("Querying grid info for player: {}", player.getUUID());
        PlayerAELinkStorage storage = PlayerAELinkStorage.get(player.serverLevel());

        AEGridQueryUtil.grid(player.getUUID()).thenAccept(grid -> {
            if(grid.isEmpty()) {
                player.sendSystemMessage(Component.literal("您当前未绑定到任何 AE2 网络。/您的无线终端未连接到任何 AE2 网络。"));
                return;
            };
            player.sendSystemMessage(Component.literal("您当前绑定的 AE2 网络位置: "+ storage.get(player.getUUID()).pos()));

        });
    }

}
