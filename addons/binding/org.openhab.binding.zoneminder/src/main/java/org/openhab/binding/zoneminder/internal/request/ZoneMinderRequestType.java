package org.openhab.binding.zoneminder.internal.request;

public enum ZoneMinderRequestType {
    SERVER_LOW_PRIORITY_DATA,
    SERVER_HIGH_PRIORITY_DATA,

    MONITOR_THING,
    // Telnet
    MONITOR_EVENT,
    // Telnet
    MONITOR_TRIGGER_REQUEST;
}
