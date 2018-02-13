/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foxtrot.internal;

import org.openhab.binding.foxtrot.internal.model.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * RefreshGroup.
 *
 * @author Radovan Sninsky
 * @since 2018-02-12 22:42
 */
public enum RefreshGroup {
    LOW, MEDIUM, HIGH, REALTIME;

    private final Logger logger = LoggerFactory.getLogger(RefreshGroup.class);

    private final List<Variable> variables = new ArrayList<>(100);
    private ScheduledFuture<?> job;
    private PlcComSClient plcClient;

    public void init(ScheduledExecutorService scheduler, long interval, PlcComSClient client) {
        this.plcClient = client;
        this.job = scheduler.scheduleWithFixedDelay(
                () -> new ArrayList<>(variables).forEach(this::refreshVariable), 10, interval, TimeUnit.SECONDS);
    }

    public void dispose() {
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
        }
    }

    public void add(Variable variable) {
        variables.add(variable);
    }

    public void remove(final String variableName) {
        variables.stream().filter(fv -> fv.getName().equals(variableName)).findFirst().ifPresent(variables::remove);
    }

    private void refreshVariable(Variable fv) {
        try {
            fv.getCallback().process(plcClient.get(fv.getName()));
        } catch (IOException e) {
            logger.warn("Getting value of variable: {} failed w error: {}", fv.getName(), e.getMessage());
            fv.getCallback().process(null);
        }
    }
}
