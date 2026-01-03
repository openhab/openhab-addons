/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

package org.openhab.binding.jellyfin.internal.thirdparty.api.current.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The different kinds of messages that are used in the WebSocket api.
 */
public enum SessionMessageType {

    FORCE_KEEP_ALIVE("ForceKeepAlive"),

    GENERAL_COMMAND("GeneralCommand"),

    USER_DATA_CHANGED("UserDataChanged"),

    SESSIONS("Sessions"),

    PLAY("Play"),

    SYNC_PLAY_COMMAND("SyncPlayCommand"),

    SYNC_PLAY_GROUP_UPDATE("SyncPlayGroupUpdate"),

    PLAYSTATE("Playstate"),

    RESTART_REQUIRED("RestartRequired"),

    SERVER_SHUTTING_DOWN("ServerShuttingDown"),

    SERVER_RESTARTING("ServerRestarting"),

    LIBRARY_CHANGED("LibraryChanged"),

    USER_DELETED("UserDeleted"),

    USER_UPDATED("UserUpdated"),

    SERIES_TIMER_CREATED("SeriesTimerCreated"),

    TIMER_CREATED("TimerCreated"),

    SERIES_TIMER_CANCELLED("SeriesTimerCancelled"),

    TIMER_CANCELLED("TimerCancelled"),

    REFRESH_PROGRESS("RefreshProgress"),

    SCHEDULED_TASK_ENDED("ScheduledTaskEnded"),

    PACKAGE_INSTALLATION_CANCELLED("PackageInstallationCancelled"),

    PACKAGE_INSTALLATION_FAILED("PackageInstallationFailed"),

    PACKAGE_INSTALLATION_COMPLETED("PackageInstallationCompleted"),

    PACKAGE_INSTALLING("PackageInstalling"),

    PACKAGE_UNINSTALLED("PackageUninstalled"),

    ACTIVITY_LOG_ENTRY("ActivityLogEntry"),

    SCHEDULED_TASKS_INFO("ScheduledTasksInfo"),

    ACTIVITY_LOG_ENTRY_START("ActivityLogEntryStart"),

    ACTIVITY_LOG_ENTRY_STOP("ActivityLogEntryStop"),

    SESSIONS_START("SessionsStart"),

    SESSIONS_STOP("SessionsStop"),

    SCHEDULED_TASKS_INFO_START("ScheduledTasksInfoStart"),

    SCHEDULED_TASKS_INFO_STOP("ScheduledTasksInfoStop"),

    KEEP_ALIVE("KeepAlive");

    private String value;

    SessionMessageType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static SessionMessageType fromValue(String value) {
        for (SessionMessageType b : SessionMessageType.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }

    /**
     * Convert the instance into URL query string.
     *
     * @param prefix prefix of the query string
     * @return URL query string
     */
    public String toUrlQueryString(String prefix) {
        if (prefix == null) {
            prefix = "";
        }

        return String.format(java.util.Locale.ROOT, "%s=%s", prefix, this.toString());
    }
}
