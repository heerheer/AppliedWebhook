package top.realme.AppliedWebhook;

import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    static final ModConfigSpec.ConfigValue<String> webhookUrl = BUILDER
            .comment("The URL of the webhook to send messages to")
            .define("webhookUrl", "");

    static final ModConfigSpec.ConfigValue<String> token = BUILDER
            .comment("The token to authenticate with the webhook")
            .define("token", "");

    static final ModConfigSpec.ConfigValue<Boolean> sendOnlyOnLeave = BUILDER
            .comment("Only send webhook when player leave(true/false)")
            .define("send_only_on_leave", Boolean.TRUE);

    static final ModConfigSpec.ConfigValue<Double> threshold = BUILDER
            .comment("Time consumption threshold (min)")
            .define("threshold", 10.0);

    static final ModConfigSpec SPEC = BUILDER.build();

}
