/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.discovery;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.openhab.binding.dsmr.DSMRBindingConstants;
import org.openhab.binding.dsmr.internal.device.DSMRDeviceConstants;
import org.openhab.binding.dsmr.internal.device.DSMRDeviceConstants.DSMRPortEvent;
import org.openhab.binding.dsmr.internal.device.DSMRPort;
import org.openhab.binding.dsmr.internal.device.DSMRPortEventListener;
import org.openhab.binding.dsmr.internal.device.cosem.CosemObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class DSMRBridgeDiscoveryHelper try to identify a DSMR Bridge for a given Serial Port.
 *
 * The helper class will open the given serial port and wait for telegrams.
 * After SERIAL_PORT_AUTO_DETECT_TIMEOUT seconds it will switch the baud rate and wait again for telegrams.
 * After DSMR_DISCOVERY_TIMEOUT seconds the helper will give up (assuming no DSMR Bridge is present)
 *
 * If a telegram is received with at least 1 Cosem Object a bridge is assumed available and a Thing is added
 * (regardless if there were problems receiving the telegram) and the discovery is stopped.
 *
 * If there are commmunication problems the baud rate is switched
 *
 * @author M. Volaart
 * @since 2.1.0
 */
public class DSMRBridgeDiscoveryHelper implements DSMRPortEventListener {
    // logger
    private final Logger logger = LoggerFactory.getLogger(DSMRBridgeDiscoveryHelper.class);

    // List of available DiscoveryEvents
    private enum DiscoveryEvent {
        DISCOVERY_START,
        DISCOVERY_FAILED,
        DISCOVERY_SWITCHBAUDRATE,
        BRIDGE_DISCOVERED;
    }

    // Discovery state
    private enum DiscoveryState {
        DISCOVERY_RUNNING,
        DISCOVERY_FINISHED
    }

    // Discovery status (also used for locking the state object)
    private class DiscoveryStatus {
        DiscoveryState state;
    }

    // The port name
    private final String portName;

    // DSMR Port instance
    private DSMRPort dsmrPort;

    // current status
    private final DiscoveryStatus status;

    // Listener for discovered devices
    private final DSMRBridgeDiscoveryListener discoveryListener;

    // Service for handling timers
    private ScheduledExecutorService discoveryTimers = ThreadPoolManager
            .getScheduledPool(DSMRBindingConstants.DSMR_SCHEDULED_THREAD_POOL_NAME);

    /**
     * Creates a new DSMRBridgeDiscoveryHelper
     *
     * @param portName the port name (e.g. /dev/ttyUSB0 or COM1)
     * @param listener the {@link DSMRMeterDiscoveryListener} to notify of new detected bridges
     */
    public DSMRBridgeDiscoveryHelper(String portName, DSMRBridgeDiscoveryListener listener) {
        status = new DiscoveryStatus();
        status.state = DiscoveryState.DISCOVERY_RUNNING;
        this.portName = portName;
        this.discoveryListener = listener;
    }

    /**
     * Start the discovery
     */
    public void startDiscovery() {
        logger.debug("Start discovery for port {}", portName);
        handleDiscoveryEvent(DiscoveryEvent.DISCOVERY_START);
    }

    /**
     * Handle discovery events
     *
     * @param event {@link DiscoveryEvent}
     */
    private void handleDiscoveryEvent(DiscoveryEvent event) {
        logger.debug("Handle discovery event {} in state {}", event, status.state);
        synchronized (status) {
            if (status.state == DiscoveryState.DISCOVERY_RUNNING) {
                switch (event) {
                    case DISCOVERY_START:
                        discoveryTimers.schedule(new Runnable() {
                            @Override
                            public void run() {
                                logger.debug(
                                        "Discovery is running for half time now and still nothing discovered, try to switch baudrate");
                                handleDiscoveryEvent(DiscoveryEvent.DISCOVERY_SWITCHBAUDRATE);
                            }
                        }, DSMRDeviceConstants.SERIAL_PORT_AUTO_DETECT_TIMEOUT, TimeUnit.MILLISECONDS);
                        discoveryTimers.schedule(new Runnable() {
                            @Override
                            public void run() {
                                logger.debug("Discovery time is over and still nothing discovered, stop discovery");
                                handleDiscoveryEvent(DiscoveryEvent.DISCOVERY_FAILED);
                            }
                        }, DSMRBindingConstants.DSMR_DISCOVERY_TIMEOUT, TimeUnit.SECONDS);

                        dsmrPort = new DSMRPort(portName, this, null, true);
                        dsmrPort.open();
                        break;
                    case BRIDGE_DISCOVERED:
                        dsmrPort.close();
                        dsmrPort = null;
                        status.state = DiscoveryState.DISCOVERY_FINISHED;
                        discoveryListener.bridgeDiscovered(portName);

                        break;
                    case DISCOVERY_FAILED:
                        dsmrPort.close();
                        dsmrPort = null;
                        status.state = DiscoveryState.DISCOVERY_FINISHED;
                        break;
                    case DISCOVERY_SWITCHBAUDRATE:
                        dsmrPort.close();
                        dsmrPort.switchPortSpeed();
                        dsmrPort.open();
                    default:
                        break;
                }
            } else {
                // Make sure port is closed
                dsmrPort.close();
            }
        }
    }

    /**
     * Event handler for DSMR Port events
     *
     * @param portEvent {@link DSMRPortEvent} to handle
     */
    @Override
    public void handleDSMRPortEvent(DSMRPortEvent portEvent) {
        logger.debug("Received portEvent {}", portEvent);
        switch (portEvent) {
            case CLOSED: // Port closed is an expected event when switch baudaate, ignore
                break;
            case CONFIGURATION_ERROR:
                // Configuration error can occur for incompatible port
                handleDiscoveryEvent(DiscoveryEvent.DISCOVERY_FAILED);
                break;
            case DONT_EXISTS:
                // Port does not exists (unexpected, since it was there, so port is not usable)
                handleDiscoveryEvent(DiscoveryEvent.DISCOVERY_FAILED);
                break;
            case ERROR:
                // General error (port is not usable)
                handleDiscoveryEvent(DiscoveryEvent.DISCOVERY_FAILED);
                break;
            case IN_USE:
                // Port is in use
                handleDiscoveryEvent(DiscoveryEvent.DISCOVERY_FAILED);
                break;
            case LINE_BROKEN:
                // No data available (try switching port speed)
                handleDiscoveryEvent(DiscoveryEvent.DISCOVERY_SWITCHBAUDRATE);
                break;
            case NOT_COMPATIBLE:
                // Port not compatible
                handleDiscoveryEvent(DiscoveryEvent.DISCOVERY_FAILED);
                break;
            case OPENED:
                // Port is opened (this is expected), ignore this events
                break;
            case READ_ERROR:
                // read error(try switching port speed)
                handleDiscoveryEvent(DiscoveryEvent.DISCOVERY_SWITCHBAUDRATE);
                break;
            case READ_OK:
                // Read is successful, so wait for telegrams and ignore this event
                break;
            case WRONG_BAUDRATE:
                // wrong baud rate (try switching port speed)
                handleDiscoveryEvent(DiscoveryEvent.DISCOVERY_SWITCHBAUDRATE);
                break;
            default:
                // Unknown event, log and do nothing
                logger.warn("Unknown event {}", portEvent);
                break;

        }
    }

    /**
     * Handle if telegrams are received.
     *
     * If there are cosem objects received a new bridge will we discovered
     *
     * @param cosemObjects list of {@link CosemObject}
     * @param stateDetails the details of the received telegram (this parameter is ignored)
     */
    @Override
    public void P1TelegramReceived(List<CosemObject> cosemObjects, String stateDetails) {
        logger.debug("Received {} cosemObjects", cosemObjects.size());
        if (cosemObjects.size() > 0) {
            handleDiscoveryEvent(DiscoveryEvent.BRIDGE_DISCOVERED);
        }
    }
}