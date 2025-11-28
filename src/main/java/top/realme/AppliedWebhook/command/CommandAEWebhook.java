package top.realme.AppliedWebhook.command;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber
public class CommandAEWebhook {
    @SubscribeEvent
    private static void onRegisterCommands(RegisterCommandsEvent event) {

        event.getDispatcher().register(
                Commands.literal("aewh")
                        .requires(src -> src.hasPermission(0)) // operator level 0
                        .then(Commands.literal("bind")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    LogicAEWebhook.bind(player);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("info")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    LogicAEWebhook.info(player);
                                    return 1;
                                })
                        )
        );
    }
}