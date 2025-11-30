package top.realme.AppliedWebhook.ae;

import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.networking.security.IActionSource;

public class WebhookSimulationRequester implements ICraftingSimulationRequester {

    private final IActionSource actionSource;

    public WebhookSimulationRequester(IActionSource source) {
        this.actionSource = source;
    }

    @Override
    public IActionSource getActionSource() {
        return this.actionSource;
    }
}