package top.realme.AppliedWebhook.http;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import top.realme.AppliedWebhook.ae.AECraftingUtil;
import top.realme.AppliedWebhook.ae.AEStorageUtil;
import top.realme.AppliedWebhook.ae.model.AECratingPlan;
import top.realme.AppliedWebhook.http.model.ReplyPayload;
import top.realme.AppliedWebhook.data.PlayerAELinkStorage;
import top.realme.AppliedWebhook.ae.AEGridQueryUtil;
import top.realme.mc.homohttprouter.event.HttpServiceBuildEvent;
import top.realme.mc.homohttprouter.http.RestResponse;
import top.realme.mc.homohttprouter.http.RouteInfo;
import top.realme.mc.homohttprouter.http.RouterRegistry;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class AwhRoutes {

    private static final Logger logger = LogUtils.getLogger();


    @SubscribeEvent
    private static void onHttp(HttpServiceBuildEvent e) {
        RouterRegistry registry = e.getRegistry();


        // 网络有关

        final String NETWORKS_INFO = "/network/<uuid>";

        final String NETWORKS_ = "/network/<uuid>/";

        // 储存有关

        final String STORAGE_INFO = "/storage/<uuid>";

        final String STORAGE_ITEMS_ROUTE = "/storage/<uuid>/items";

        final String STORAGE_ITEMS_COUNT_ROUTE = "/storage/<uuid>/items/<item_name>";

        final String STORAGE_MAX_THINGS = "/storage/<uuid>/items/max/[count]";

        final String CRAFTING_CPUS = "/crafting/<uuid>/cpus";

        final String CRAFTING_CRAFTABLES = "/crafting/<uuid>/craftables";

        final String CRAFTING_REQUEST = "/crafting/<uuid>/request";

        // Create RouteInfo
        RouteInfo info = new RouteInfo.Builder("appliedwebhook", "/awh")
                .description("AppliedWebhook module HTTP API. 传入user uuid以获取用户绑定的ME网络信息")
                .route("GET", "/status", "Check service status", "", "OK")
                .route("GET", "/networks", "获取所有用户绑定的ME网络", "", "[{uuid:{pos,dim}]")

                .route("GET", STORAGE_ITEMS_COUNT_ROUTE, "获取有关(名称包含搜索)物品在用户绑定的ME网络中的数量", "", "[{id:count}]")
                .route("GET", STORAGE_MAX_THINGS, "获取用户绑定的ME网络中最多可以存储的物品数量", "", "[{id:count}]")
                .route("GET", CRAFTING_CPUS, "获取用户绑定的ME网络中所有CPU的状态", "", "[{cpu:{busy,mode,{job}}}]")
                .route("GET", CRAFTING_CRAFTABLES, "获取用户绑定的ME网络中所有可以合成的物品", "", "[{id:count}]")
                .route("POST", CRAFTING_REQUEST, "请求用户绑定的ME网络中合成物品", "", "")
                .build();

        // Register RouteInfo
        registry.register(info, restRequest -> {
//
            try {
                //            byte[] body = restRequest.body();
//            JSONObject jsonObject = JSON.parseObject(body);  // fastjson 的写法
//            String uuid = jsonObject.getString("uuid");

                logger.debug("Request path: {}", restRequest.path());


                if (restRequest.path().equals("/awh/status")) {
                    return handleStatus();
                }

                if (restRequest.path().equals("/awh/networks")) {
                    return handleNetworks();
                }

                if (restRequest.path().equals(STORAGE_ITEMS_ROUTE)) {
                    logger.debug("handleQueryStorageCount");
                }


                if (restRequest.matchTemplate(STORAGE_ITEMS_COUNT_ROUTE)) {
                    logger.debug("handleQueryStorageCount");
                    String uuid = restRequest.pathParam(STORAGE_ITEMS_COUNT_ROUTE, "uuid").get();
                    String item = restRequest.pathParam(STORAGE_ITEMS_COUNT_ROUTE, "item_name").get();
                    return handleQueryStorageCount(uuid, item);
                }


                if (restRequest.matchTemplate(STORAGE_MAX_THINGS)) {
                    logger.debug("handleQueryMaxThings");
                    String uuid = restRequest.pathParam(STORAGE_MAX_THINGS, "uuid").get();
                    String count = restRequest.pathParam(STORAGE_MAX_THINGS, "count").orElse("5");
                    return handleQueryMaxThings(uuid, count);
                }

                if (restRequest.matchTemplate(CRAFTING_CPUS)) {
                    logger.debug("handleQueryCpus");
                    String uuid = restRequest.pathParam(CRAFTING_CPUS, "uuid").get();
                    return handleQueryCpus(uuid);
                }

                if(restRequest.matchTemplate(CRAFTING_CRAFTABLES)) {
                    logger.debug("handleQueryCraftables");
                    String uuid = restRequest.pathParam(CRAFTING_CRAFTABLES, "uuid").get();

                    var list = AECraftingUtil.craftables(UUID.fromString(uuid)).join();
                    return RestResponse.ok(ReplyPayload.Success(list));
                }

                if(restRequest.matchTemplate(CRAFTING_REQUEST)) {
                    logger.debug("handleCraftingRequest");
                    String uuid = restRequest.pathParam(CRAFTING_REQUEST, "uuid").get();

                    // 先忽略body
                    AEKey key = AEItemKey.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:iron_ingot"))));

                    if(!AECraftingUtil.canCraft(UUID.fromString(uuid), key).join()) {
                        return RestResponse.error(400, ReplyPayload.Error("无法合成的物品", 400));
                    }

                    var plan = AECraftingUtil.sim(UUID.fromString(uuid), key , 1).join();

                    return RestResponse.ok(
                            ReplyPayload.Success(new AECratingPlan(plan))
                    );
                }




                return RestResponse.error(404, ReplyPayload.NotFound());

            } catch (Exception exception) {
                logger.error("Error handling request: {}", restRequest.path(), e);
                return RestResponse.error(500, ReplyPayload.Error(exception.getMessage(), 500));
            }

        });
    }

    // 处理 /status 请求
    private static RestResponse handleStatus() {
        return RestResponse.ok(ReplyPayload.Success("AppliedWebhook service is running."));
    }

    // 处理 /networks 请求
    private static RestResponse handleNetworks() {

        var server = ServerLifecycleHooks.getCurrentServer();
        if(server == null) {
            return RestResponse.error(500, ReplyPayload.Error("Server is not running.", 500));
        }

        PlayerAELinkStorage storage = PlayerAELinkStorage.get(server.overworld());


        return RestResponse.ok(ReplyPayload.Success(storage));
    }


    private static RestResponse handleQueryStorageCount(String uuid, String itemName) {

        var map = AEStorageUtil.searchProbeItems(UUID.fromString(uuid), itemName).join();

        return RestResponse.ok(ReplyPayload.Success(map));

    }

    private static RestResponse handleQueryMaxThings(String uuid, String count) {

        var map = AEStorageUtil.queryMaxThings(UUID.fromString(uuid), Integer.parseInt(count)).join();
        return RestResponse.ok(ReplyPayload.Success(map));

    }

    private static RestResponse handleQueryCpus(String uuid) {

        var map = AECraftingUtil.cpus(UUID.fromString(uuid)).join();
        return RestResponse.ok(ReplyPayload.Success(map));

    }

}
