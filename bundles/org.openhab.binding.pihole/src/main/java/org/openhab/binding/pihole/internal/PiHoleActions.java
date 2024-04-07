package org.openhab.binding.pihole.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.BINDING_ID;

@ThingActionsScope(name = BINDING_ID)
@NonNullByDefault
public class PiHoleActions implements ThingActions {
    private @Nullable PiHoleHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (PiHoleHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/action.disable.label", description = "@text/action.disable.description")
    public void disableBlocking(
            @ActionInput(name = "time", label = "@text/action.disable.timeLabel", description = "@text/action.disable.timeDescription") long time,
            @ActionInput(name = "timeUnit", label = "@text/action.disable.timeUnitLabel", description = "@text/action.disable.timeUnitDescription") @Nullable TimeUnit timeUnit) throws ExecutionException, InterruptedException, TimeoutException {
        if (time < 0) {
            return;
        }

        if (timeUnit == null) {
            timeUnit = SECONDS;
        }

        var local = handler;
        if(local == null){
            return;
        }
        local.disableBlocking(timeUnit.toSeconds(time));
    }

    public static void disableBlocking(@Nullable ThingActions actions, long time, @Nullable TimeUnit timeUnit) throws ExecutionException, InterruptedException, TimeoutException {
        ((PiHoleActions) requireNonNull(actions)).disableBlocking(time, timeUnit);
    }

    @RuleAction(label = "@text/action.disable.label", description = "@text/action.disable.description")
    public void disableBlocking(
            @ActionInput(name = "time", label = "@text/action.disable.timeLabel", description = "@text/action.disable.timeDescription") long time) throws ExecutionException, InterruptedException, TimeoutException {
        disableBlocking(time, null);
    }

    public static void disableBlocking(@Nullable ThingActions actions, long time) throws ExecutionException, InterruptedException, TimeoutException {
        ((PiHoleActions) requireNonNull(actions)).disableBlocking(time);
    }

    @RuleAction(label = "@text/action.enable.label", description = "@text/action.enable.description")
    public void enableBlocking() throws ExecutionException, InterruptedException, TimeoutException {
        var local = handler;
        if(local == null){
            return;
        }
        local.enableBlocking();
    }

    public static void enableBlocking(@Nullable ThingActions actions) throws ExecutionException, InterruptedException, TimeoutException {
        ((PiHoleActions) requireNonNull(actions)).enableBlocking();
    }
}
