package org.openhab.binding.prowl.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.prowl.internal.ProwlHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ProwlActions} class contains methods for use in DSL.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@ThingActionsScope(name = "prowl")
@NonNullByDefault
public class ProwlActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(ProwlActions.class);
    private @Nullable ProwlHandler handler;

    @Override
    public void setThingHandler(ThingHandler thingHandler) {
        this.handler = (ProwlHandler) thingHandler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "push a notification", description = "Send a push message using ProwlApp.")
    public void pushNotification(
            @ActionInput(name = "event", label = "Event", description = "Event name") @Nullable String event,
            @ActionInput(name = "description", label = "Description", description = "Message text") @Nullable String description) {
        ProwlHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("Prowl ThingHandler is null");
            return;
        }

        handler.pushNotification(event, description);
    }

    public static void pushNotification(@Nullable ThingActions actions, @Nullable String event,
            @Nullable String description) {
        if (actions instanceof ProwlActions) {
            ((ProwlActions) actions).pushNotification(event, description);
        } else {
            throw new IllegalArgumentException("Instance is not a ProwlActions class.");
        }
    }
}
