/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.astro.internal.job;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.astro.internal.handler.AstroThingHandler;

/**
 * {@link CompositeJob} comprises multiple {@link Job}s to be executed in order
 *
 * @author Markus Rathgeb - Initial contribution
 * @author Amit Kumar Mondal - Minor modifications
 */
@NonNullByDefault
public final class CompositeJob extends AbstractJob {

    private final List<Job> jobs;

    /**
     * Constructor
     *
     * @param handler thing thing handler
     * @param jobs the jobs to execute
     * @throws IllegalArgumentException
     *             if {@code jobs} is {@code null} or empty
     */
    public CompositeJob(AstroThingHandler handler, List<Job> jobs) {
        super(handler);

        this.jobs = List.copyOf(jobs);

        boolean notMatched = jobs.stream().anyMatch(j -> !j.getHandler().equals(handler));
        checkArgument(!notMatched, "The jobs must associate the same thing handler");
    }

    @Override
    public void run() {
        jobs.forEach(j -> {
            try {
                j.run();
            } catch (Exception e) {
                LOGGER.warn("Job execution of \"{}\" failed: {}", j, e.getMessage());
                LOGGER.trace("", e);
            }
        });
    }

    @Override
    public String toString() {
        return jobs.stream().map(j -> j.toString()).collect(Collectors.joining(" + "));
    }
}
