package org.openhab.binding.timer.handler;

import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.slf4j.Logger;

public abstract class AbstractBaseTimeHandler extends BaseThingHandler {

    public AbstractBaseTimeHandler(Thing thing) {
        super(thing);
    }

    protected ScheduledFuture<Boolean> cancel(ScheduledFuture<Boolean> job) {
        if (job != null) {
            getLogger().info("Cancelling current job [{}]", getThing());
            // Cancel current job.
            job.cancel(false);
            job = null;
        }
        return job;
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
        for (ScheduledFuture<Boolean> job : getJobs()) {
            cancel(job);
        }
    }

    protected abstract Logger getLogger();

    protected abstract Set<ScheduledFuture<Boolean>> getJobs();
}
