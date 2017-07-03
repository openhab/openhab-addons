/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
/*
 *
 */
package org.openhab.binding.sony.internal;

import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class SonyUtility.
 *
 * @author Tim Roberts - Initial contribution
 */
public class SonyUtility {

    /**
     * Creates the channel id.
     *
     * @param groupId the group id
     * @param channelId the channel id
     * @return the string
     */
    public static String createChannelId(String groupId, String channelId) {
        return StringUtils.isEmpty(groupId) ? channelId : (groupId + "#" + channelId);
    }

    /**
     * Creates the channel id.
     *
     * @param channelId the channel id
     * @return the string
     */
    public static String createChannelId(ChannelUID channelId) {
        final String groupId = channelId.getGroupId();
        final String id = channelId.getIdWithoutGroup();

        return StringUtils.isEmpty(groupId) ? id : createChannelId(groupId, id);
    }

    /**
     * Utility function to close a {@link AutoCloseable} and log any exception thrown.
     *
     * @param closeable a possibly null {@link AutoCloseable}. If null, no action is done.
     */
    public static void close(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                LoggerFactory.getLogger(SonyUtility.class).debug("Exception closing: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Checks whether the current thread has been interrupted and throws {@link InterruptedException} if it's been
     * interrupted.
     *
     * @throws InterruptedException the interrupted exception
     */
    public static void checkInterrupt() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("thread interrupted");
        }
    }

    /**
     * Cancels the specified {@link ScheduledFuture}.
     *
     * @param future a possibly null future. If null, no action is done
     */
    public static void cancel(Future<?> future) {
        if (future != null) {
            future.cancel(true);
        }
    }

    /**
     * Cancels the specified {@link ScheduledFuture}.
     *
     * @param future a possibly null future. If null, no action is done
     */
    public static void cancel(ScheduledFuture<?> future) {
        if (future != null) {
            future.cancel(true);
        }
    }

    /**
     * Require the specified value to be a non-null, non-empty string.
     *
     * @param value the value to check
     * @param msg the msg to use when throwing an {@link IllegalArgumentException}
     * @throws IllegalArgumentException if value is null or an empty string
     */
    public static void requireNotEmpty(String value, String msg) {
        Objects.requireNonNull(value, msg);
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException(msg);
        }
    }
}
