/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.nibeuplink.internal;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * trait class which contains useful helper methods. Thus, the interface can be implemented and methods are available
 * within the class.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public interface AtomicReferenceTrait {

    /**
     * this should usually not called directly. use updateJobReference or cancelJobReference instead
     *
     * @param job job to cancel.
     */
    default void cancelJob(@Nullable Future<?> job) {
        if (job != null) {
            job.cancel(true);
        }
    }

    /**
     * updates a job reference with a new job. the old job will be cancelled if there is one.
     *
     * @param jobReference reference to be updated
     * @param newJob job to be assigned
     */
    default void updateJobReference(AtomicReference<@Nullable Future<?>> jobReference, Future<?> newJob) {
        cancelJob(jobReference.getAndSet(newJob));
    }

    /**
     * updates a job reference to null and cancels any existing job which might be assigned to the reference.
     *
     * @param jobReference to be updated to null.
     */
    default void cancelJobReference(AtomicReference<@Nullable Future<?>> jobReference) {
        cancelJob(jobReference.getAndSet(null));
    }
}
