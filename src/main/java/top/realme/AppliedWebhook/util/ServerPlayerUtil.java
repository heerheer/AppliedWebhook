package top.realme.AppliedWebhook.util;

import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.UUID;

public class ServerPlayerUtil {
    public static boolean isPlayerOnline(UUID playerUUID) {
        try {
            return ServerLifecycleHooks.getCurrentServer() != null && ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerUUID) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getPlayerName(UUID uuid) {
        try {
            return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid).getGameProfile().getName();
        } catch (Exception e) {
            return "NULL";
        }
    }
}
