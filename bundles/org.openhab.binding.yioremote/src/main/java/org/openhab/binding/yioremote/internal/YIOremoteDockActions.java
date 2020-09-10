package org.openhab.binding.yioremote.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;

@ThingActionsScope(name = "yioremote") // Your bindings id is usually the scope
@NonNullByDefault
public class YIOremoteDockActions implements ThingActions {
    private @Nullable YIOremoteDockHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (YIOremoteDockHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/actionLabel", description = "@text/actionDesc")
    public void publishIRCODE(
            @ActionInput(name = "ircode", label = "@text/actionInputTopicLabel", description = "@text/actionInputTopicDesc") @Nullable String ircode) {

    }

    public static void publishIRCODE(@Nullable ThingActions actions, @Nullable String ircode) {
        if (actions instanceof YIOremoteDockActions) {
            ((YIOremoteDockActions) actions).publishIRCODE(ircode);
        } else {
            throw new IllegalArgumentException("Instance is not an YIOremoteDockActions class.");
        }
    }
}
