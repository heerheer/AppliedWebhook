package top.realme.AppliedWebhook;

import com.alibaba.fastjson2.JSON;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;
import top.realme.AppliedWebhook.ae.event.AECraftingJobFinishEvent;
import top.realme.AppliedWebhook.http.AwhRoutes;
import top.realme.AppliedWebhook.http.model.JobFinishPayload;
import top.realme.AppliedWebhook.util.ServerPlayerUtil;
import top.realme.AppliedWebhook.util.WebhookSender;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(AppliedWebhook.MODID)
public class AppliedWebhook {


    // Define mod id in a common place for everything to reference
    public static final String MODID = "appliedwebhook";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();


    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public AppliedWebhook(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (appliedwebhook) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

    }

    public void postRegistrationInitialization() {
    }


    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code

        if (isHttpRouterPresent()) {
            LogUtils.getLogger().info("homohttprouter detected. Enabling HTTP routes.");
            NeoForge.EVENT_BUS.register(AwhRoutes.class);
        } else {
            LogUtils.getLogger().info("homohttprouter NOT detected. HTTP routes disabled.");
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("");
        //client = new WSClient(Config.webhookUrl.get());
        try {
            //client.connect();

        } catch (Exception e) {
            //event.getServer().sendSystemMessage(Component.literal("AppliedWebhook 连接失败: " + e.toString()));
            //LOGGER.error("AppliedWebhook WS 连接失败: {}", e.toString());
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        //client.close();

    }

    @SubscribeEvent
    public void onJobFinish(AECraftingJobFinishEvent event) {

        long nanoseconds = event.duration();
        long milliseconds = nanoseconds / 1_000_000; // 纳秒转毫秒公式
        double minutes = Math.round(((milliseconds / 60_000.0) * 100.0)) / 100.0; // 毫秒转分钟公式,保留2位小数
        LOGGER.debug("玩家 {} 耗时 {}ms/{}min 完成了一个合成任务;, 得到了 {} x {}", event.playerUUID(), milliseconds, minutes, event.get().amount(), event.get().key());

        if (Config.webhookUrl.get().isEmpty()) {
            LOGGER.debug("webhookUrl 为空, 不发送");
            return;
        }

        if (minutes < Config.threshold.get()) {
            LOGGER.debug("耗时 {}min 小于阈值 {}min, 不发送", minutes, Config.threshold.get());
            return;
        }

        if (Config.sendOnlyOnLeave.get() && ServerPlayerUtil.isPlayerOnline(event.playerUUID())) {
            LOGGER.debug("玩家 {} 在线, 配置为仅在离线时发送, 不发送", event.playerUUID());
            return;
        }

        var payload = new JobFinishPayload(
                event.playerUUID(),
                event.get().key().toString(),
                event.get().amount(),
                (long) (event.duration() / 1e6),
                ServerPlayerUtil.isPlayerOnline(event.playerUUID()),
                ServerPlayerUtil.getPlayerName(event.playerUUID()));

        var webhook_url = Config.webhookUrl.get();

        var body = JSON.toJSON(payload);

        WebhookSender.sendAsync(webhook_url, body.toString(), Config.token.get())
                .thenAccept(resp -> {
                    LOGGER.info("AppliedWebhook 发送成功: {}", resp);
                })
                .exceptionally(ex -> {
                    LOGGER.error("AppliedWebhook 发送失败: {}", ex.toString());
                    return null;
                });


    }

    private boolean isHttpRouterPresent() {
        try {
            Class.forName("top.realme.mc.homohttprouter.http.RouterRegistry");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
