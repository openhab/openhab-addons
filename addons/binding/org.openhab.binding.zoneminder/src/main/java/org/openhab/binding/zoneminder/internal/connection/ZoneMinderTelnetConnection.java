/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.openhab.binding.zoneminder.ZoneMinderConstants;
import org.openhab.binding.zoneminder.internal.command.ZoneMinderEvent;

public class ZoneMinderTelnetConnection {

    private String hostname;
    private Integer port;
    private Integer timeout;

    private Socket telnetSocket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;

    /*
     * 2016.10.20 NOT IN USE???
     * private List<ZoneMinderMonitorEventListener> eventListeners = new ArrayList<ZoneMinderMonitorEventListener>();
     * 
     * private List<ZoneMinderMonitorTriggerListener> triggerListeners = new
     * ArrayList<ZoneMinderMonitorTriggerListener>();
     */
    public ZoneMinderTelnetConnection(String hostname, Integer port, Integer timeout) {
        initialize(hostname, port, timeout);
    }
    /*
     * 2016.10.20 NOT IN USE???
     * 
     * public void addEventListener(ZoneMinderMonitorEventListener listener) {
     * if (listener != null) {
     * eventListeners.add(listener);
     * }
     * }
     * 
     * public void removeEventListener(ZoneMinderMonitorEventListener listener) {
     * if (listener != null) {
     * eventListeners.remove(listener);
     * }
     * }
     * 
     * public void addTriggerListener(ZoneMinderMonitorTriggerListener listener) {
     * triggerListeners.add(listener);
     * }
     *
     * public void removeTriggerListener(ZoneMinderMonitorTriggerListener listener) {
     * triggerListeners.remove(listener);
     * }
     */

    /*
     * public void notifyAllListeners(ChannelUID channelUID, ZoneMinderTelnetCommand command) {
     * // If request is an external trigger -> notify listeners
     * if (command.getClass().getName().equals(ZoneMinderExternalTrigger.class.getName())) {
     * notifyMonitorTriggerListeners((ZoneMinderExternalTrigger) command);
     * }
     *
     * // If request is an event -> notify listeners
     * if (command.getClass().getName().equals(ZoneMinderEvent.class.getName())) {
     * notifyMonitorEventListeners(channelUID, (ZoneMinderEvent) command);
     * }
     * }
     *
     * private void notifyMonitorTriggerListeners(ZoneMinderExternalTrigger trigger) {
     *
     * for (ZoneMinderMonitorTriggerListener listener : triggerListeners) {
     * listener.MonitorExternalTrigger(trigger);
     * }
     * }
     *
     *
     * private void notifyMonitorEventListeners(ChannelUID channelUID, ZoneMinderEvent event) {
     *
     * for (ZoneMinderMonitorEventListener listener : eventListeners) {
     * listener.MonitorEvent(channelUID, event);
     * }
     * }
     */
    protected void initialize(String hostname, Integer port, Integer timeout) {
        this.hostname = hostname;
        this.port = port;
        this.timeout = timeout;

        connect();
    }

    protected void connect() {
        try {
            telnetSocket = new Socket(hostname, port);
            synchronized (telnetSocket) {
                // Make sure read operations does have a timeout
                telnetSocket.setSoTimeout(timeout);
                telnetSocket.setKeepAlive(true);

                in = new BufferedReader(new InputStreamReader(telnetSocket.getInputStream()));
                out = new PrintWriter(telnetSocket.getOutputStream(), true);
            }
        } catch (IOException e) {
            return;
        }

        /*
         * if (telnetMonitorTask == null || telnetMonitorTask.isCancelled()) {
         * // telnetMonitorTask = scheduler.scheduleAtFixedRate(monitorRunnable, 0, refreshInterval,
         * // TimeUnit.MILLISECONDS);
         * }
         */
    }

    public void close() {
        /*
         * if (telnetMonitorTask != null) {
         * telnetMonitorTask.cancel(false);
         * }
         */
        synchronized (telnetSocket) {
            try {
                in.close();
                out.close();
                telnetSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void reconnect() {
        synchronized (telnetSocket) {
            try {
                if (telnetSocket.isClosed()) {
                    in.close();
                    out.close();
                    telnetSocket.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            connect();
        }

    }

    /*
     * public void sendCommand(ChannelUID channelUID, ZoneMinderTelnetRequest request) throws IOException {
     * // Send Command telnet
     * out.println(request.toCommandString());
     * // TODO Fixx me
     * // HEST
     * notifyAllListeners(channelUID, request);
     *
     * }
     */
    public ZoneMinderEvent readInput() {

        ZoneMinderEvent event = null;
        String result = null;

        // Check if connection is available
        synchronized (telnetSocket) {
            if (telnetSocket.isClosed()) {
                reconnect();
            }
        }
        try {
            result = in.readLine();
            if (result != null) {
                event = new ZoneMinderEvent(ZoneMinderConstants.THING_TYPE_THING_ZONEMINDER_MONITOR, result);
            }

        } catch (SocketTimeoutException e) {
            // Just ignore timeouts
        } catch (IOException e) {
            // Occurs if socket is closed, probably because we are shutting down. Else it will be fixed on next run
        }
        return event;
    }

}
