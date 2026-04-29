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


package org.openhab.binding.jellyfin.internal.gen.current.model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The different kinds of messages that are used in the WebSocket api.
 */
public enum SessionMessageType {
  ACTIVITY_LOG_ENTRY("ActivityLogEntry"),
  ACTIVITY_LOG_ENTRY_START("ActivityLogEntryStart"),
  ACTIVITY_LOG_ENTRY_STOP("ActivityLogEntryStop"),
  FORCE_KEEP_ALIVE("ForceKeepAlive"),
  GENERAL_COMMAND("GeneralCommand"),
  KEEP_ALIVE("KeepAlive"),
  LIBRARY_CHANGED("LibraryChanged"),
  PACKAGE_INSTALLATION_CANCELLED("PackageInstallationCancelled"),
  PACKAGE_INSTALLATION_COMPLETED("PackageInstallationCompleted"),
  PACKAGE_INSTALLATION_FAILED("PackageInstallationFailed"),
  PACKAGE_INSTALLING("PackageInstalling"),
  PACKAGE_UNINSTALLED("PackageUninstalled"),
  PLAY("Play"),
  PLAYSTATE("Playstate"),
  REFRESH_PROGRESS("RefreshProgress"),
  RESTART_REQUIRED("RestartRequired"),
  SCHEDULED_TASK_ENDED("ScheduledTaskEnded"),
  SCHEDULED_TASKS_INFO("ScheduledTasksInfo"),
  SCHEDULED_TASKS_INFO_START("ScheduledTasksInfoStart"),
  SCHEDULED_TASKS_INFO_STOP("ScheduledTasksInfoStop"),
  SERIES_TIMER_CANCELLED("SeriesTimerCancelled"),
  SERIES_TIMER_CREATED("SeriesTimerCreated"),
  SERVER_RESTARTING("ServerRestarting"),
  SERVER_SHUTTING_DOWN("ServerShuttingDown"),
  SESSIONS("Sessions"),
  SESSIONS_START("SessionsStart"),
  SESSIONS_STOP("SessionsStop"),
  SYNC_PLAY_COMMAND("SyncPlayCommand"),
  SYNC_PLAY_GROUP_UPDATE("SyncPlayGroupUpdate"),
  TIMER_CANCELLED("TimerCancelled"),
  TIMER_CREATED("TimerCreated"),
  USER_DATA_CHANGED("UserDataChanged"),
  USER_DELETED("UserDeleted"),
  USER_UPDATED("UserUpdated");

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

