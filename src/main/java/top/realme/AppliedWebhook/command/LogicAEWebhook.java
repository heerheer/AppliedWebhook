package top.realme.AppliedWebhook.command;

import appeng.api.features.Locatables;
import appeng.api.ids.AEComponents;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.blockentity.qnb.QuantumBridgeBlockEntity;
import appeng.items.tools.powered.WirelessTerminalItem;
import de.mari_023.ae2wtlib.api.results.Status;
import net.minecraft.world.level.Level;
import com.mojang.logging.LogUtils;
import de.mari_023.ae2wtlib.api.AE2wtlibAPI;
import de.mari_023.ae2wtlib.api.AE2wtlibComponents;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import top.realme.AppliedWebhook.ae.util.AEGridUtils;
import top.realme.AppliedWebhook.data.PlayerAELinkStorage;
import top.realme.AppliedWebhook.ae.AEGridQueryUtil;

import java.util.UUID;

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

            // 判断是否安装了 ae2wtlib
            if (ModList.get().isLoaded("ae2wtlib")) {

                if (stack.getItem() instanceof de.mari_023.ae2wtlib.api.terminal.ItemWT wtItem) {

                    if(!AE2wtlibAPI.hasQuantumBridgeCard(()->wtItem.getUpgrades(stack))) {
                        player.sendSystemMessage(Component.literal("该无线终端未安装量子桥卡"));
                        return;
                    }
                    ItemStack singularity = stack.getOrDefault(AE2wtlibComponents.SINGULARITY, ItemStack.EMPTY);

                    if (singularity.isEmpty()){
                        player.sendSystemMessage(Component.literal("该无线终端未绑定到任何奇点。"));
                        return;
                    }

                    Long singularityID = singularity.get(AEComponents.ENTANGLED_SINGULARITY_ID);
                    if (singularityID == null) {
                        player.sendSystemMessage(Component.literal("该无线终端绑定的奇点 ID 无效。"));
                        return;
                    }

                    var gridFind = AEGridUtils.tryFindGridByFrequency(player.server, singularityID);

                    if (gridFind == null) {
                        player.sendSystemMessage(Component.literal("该无线终端绑定的量子桥对应的网络无效。"));
                        return;
                    }

                    PlayerAELinkStorage storage = PlayerAELinkStorage.get(player.serverLevel());
                    storage.setFrequency(player.getUUID(), singularityID); // 保存持久化数据
                    player.sendSystemMessage(Component.literal("(AE2 WTLIB) 已将您绑定至 AE2 网络, 频率: "+singularityID));
                    return;
                }
            }
            player.sendSystemMessage(Component.literal("无线终端无法连接到 AE2 网络: 无连接+无其他模组连接"));
        }else{

            // 获取终端绑定位置（GlobalPos）
            GlobalPos linkedPos = terminalItem.getLinkedPosition(stack);

            // ★★★ 使用我们的持久化存储 ★★★ ,gpt你为什么要加小星星!
            PlayerAELinkStorage storage = PlayerAELinkStorage.get(player.serverLevel());
            storage.setPos(player.getUUID(), linkedPos); // 保存持久化数据


            player.sendSystemMessage(Component.literal("(无线接收器) 已将您绑定至 AE2 网络，连接位置: " + linkedPos));

        }



    }

    public static void info(ServerPlayer player) {
        UUID uuid = player.getUUID();
        var logger = LogUtils.getLogger();

        logger.info("[AE2Link] Querying grid info for player {}", uuid);

        PlayerAELinkStorage storage = PlayerAELinkStorage.get(player.serverLevel());
        long freq = storage.getFrequency(uuid);
        GlobalPos pos = storage.getPos(uuid);

        // 显示玩家当前保存的绑定信息
        if (freq >= 0) {
            player.sendSystemMessage(Component.literal("§e已保存的量子频率: §b" + freq));
        }
        if (pos != null) {
            player.sendSystemMessage(Component.literal("§e已保存的方块位置: §a" + pos.pos() + " @ " + pos.dimension().location()));
        }
        if (freq < 0 && pos == null) {
            player.sendSystemMessage(Component.literal("§c尚未绑定任何 AE2 网络。"));
            return;
        }

        // 开始异步查询 Grid
        AEGridQueryUtil.grid(uuid).thenAccept(grid -> {

            if (grid == null) {
                player.sendSystemMessage(Component.literal("§c无法连接到 AE2 网络。请检查以下内容："));
                if (freq >= 0) {
                    player.sendSystemMessage(Component.literal("  §7- 量子频率可能无效或对应的量子桥不存在。"));
                }
                if (pos != null) {
                    player.sendSystemMessage(Component.literal("  §7- 方块位置可能不是有效的 AE2 终端或已被破坏。"));
                }
                return;
            }

            // Grid 成功
            player.sendSystemMessage(Component.literal("§a您已成功连接到一个 AE2 网络。Ping: 114514ms!"));

        });
    }



}
