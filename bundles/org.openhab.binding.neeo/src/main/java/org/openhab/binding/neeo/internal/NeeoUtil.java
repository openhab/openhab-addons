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
package org.openhab.binding.neeo.internal;

import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.neeo.internal.models.NeeoDevices;
import org.openhab.binding.neeo.internal.models.NeeoDevicesDeserializer;
import org.openhab.binding.neeo.internal.models.NeeoMacros;
import org.openhab.binding.neeo.internal.models.NeeoMacrosDeserializer;
import org.openhab.binding.neeo.internal.models.NeeoRecipes;
import org.openhab.binding.neeo.internal.models.NeeoRecipesDeserializer;
import org.openhab.binding.neeo.internal.models.NeeoRooms;
import org.openhab.binding.neeo.internal.models.NeeoRoomsDeserializer;
import org.openhab.binding.neeo.internal.models.NeeoScenarios;
import org.openhab.binding.neeo.internal.models.NeeoScenariosDeserializer;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Various utility functions used by the NEEO binding
 *
 * @author Tim Roberts - initial contribution
 */
@NonNullByDefault
public class NeeoUtil {

    /**
     * Builds and returns a {@link Gson}. The gson has adapters registered for {@link NeeoRooms}, {@link NeeoRecipes}
     * and {@link NeeoScenarios}
     *
     * @return a non-null {@link Gson} to use
     */
    public static Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(NeeoRooms.class, new NeeoRoomsDeserializer());
        gsonBuilder.registerTypeAdapter(NeeoRecipes.class, new NeeoRecipesDeserializer());
        gsonBuilder.registerTypeAdapter(NeeoScenarios.class, new NeeoScenariosDeserializer());
        gsonBuilder.registerTypeAdapter(NeeoDevices.class, new NeeoDevicesDeserializer());
        gsonBuilder.registerTypeAdapter(NeeoMacros.class, new NeeoMacrosDeserializer());
        return gsonBuilder.create();
    }

    /**
     * Utility function to close an {@link AutoCloseable} and log any exception thrown.
     *
     * @param closeable a possibly null {@link AutoCloseable}. If null, no action is done.
     */
    public static void close(@Nullable AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                LoggerFactory.getLogger(NeeoUtil.class).debug("Exception closing: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Checks whether the current thread has been interrupted and throws {@link InterruptedException} if it's been
     * interrupted
     *
     * @throws InterruptedException the interrupted exception
     */
    public static void checkInterrupt() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("thread interrupted");
        }
    }

    /**
     * Cancels the specified {@link Future}
     *
     * @param future a possibly null future. If null, no action is done
     */
    public static void cancel(@Nullable Future<?> future) {
        if (future != null) {
            future.cancel(true);
        }
    }

    /**
     * Require the specified value to be a non-null, non-empty string
     *
     * @param value the value to check
     * @param msg the msg to use when throwing an exception
     * @throws NullPointerException if value is null
     * @throws IllegalArgumentException if value is an empty string
     */
    public static void requireNotEmpty(@Nullable String value, String msg) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(msg);
        }
    }
}
