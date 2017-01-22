package org.openhab.binding.dsmr.device;

import java.util.LinkedList;
import java.util.List;

import org.openhab.binding.dsmr.device.DSMRDeviceConstants.DeviceState;
import org.openhab.binding.dsmr.device.DSMRDeviceConstants.DeviceStateDetail;
import org.openhab.binding.dsmr.device.cosem.CosemObject;
import org.openhab.binding.dsmr.device.discovery.DSMRMeterDetector;
import org.openhab.binding.dsmr.device.discovery.DSMRMeterDiscoveryListener;
import org.openhab.binding.dsmr.device.p1telegram.P1TelegramListener;
import org.openhab.binding.dsmr.device.p1telegram.P1TelegramParser;
import org.openhab.binding.dsmr.meter.DSMRMeter;
import org.openhab.binding.dsmr.meter.DSMRMeterDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The DSMRDevice class represents the physical Thing within OpenHAB2 context
 *
 * The DSMRDevice will always starts in the state INITIALIZING meaning the port
 * is setup and OS resources are claimed.
 *
 * The device is waiting in the state STARTING till the data stream from the serial port can be parsed successfully.
 * Then the device will enter the state ONLINE.
 *
 * If there are problems reading the port and no CosemObjects are received anymore, the device will enter the state
 * OFFLINE.
 *
 * If the OpenHAB2 system wants the binding to shutdown, the DSMRDevice enters the SHUTDOWN state and will close
 * and release the OS resources.
 *
 * In case of configuration errors the DSMRDevice will enter the state CONFIGURATION_PROBLEM
 *
 * Please note that these are DSMRDevice specific states and will be mapped on the OpenHAB2 states
 *
 * @author M. Volaart
 * @since 2.0.0
 */
public class DSMRDevice implements Runnable, P1TelegramListener {
    // Logger
    private final Logger logger = LoggerFactory.getLogger(DSMRDevice.class);

    // State of the DSMRDevice
    private class DSMRDeviceState {
        DeviceState state;

        // /** Timestamp this state is (re-)entered */
        long timestamp = 0;
    }

    // Current state
    private final DSMRDeviceState deviceState = new DSMRDeviceState();

    // P1 TelegramParser Instance
    private P1TelegramParser p1Parser = null;

    // List of available meters
    private List<DSMRMeter> availableMeters = null;

    // DSMR Device configuration
    private DSMRDeviceConfiguration deviceConfiguration = null;

    // DSMR Port
    private DSMRPort dsmrPort = null;

    // listener for discovery of new meter
    private DSMRMeterDiscoveryListener discoveryListener;
    // listener for changes of the DSMR Device state
    private DSMRDeviceStateListener deviceStateListener;

    /**
     * Creates a new DSMRDevice.
     *
     * The constructor will initialize the state to INITIALIZING.
     *
     * @param deviceConfiguration {@link DSMRDeviceConfiguration} containing the configuration of the DSMR Device
     * @param deviceStateListener {@link DSMRDeviceStateListener} listener that will be notified in case of state
     *            changes of the DSMR device
     * @param discoveryListener {@link DSMRMeterDiscoveryListener} listener that will be notified in case a new
     *            meter is detected
     */
    public DSMRDevice(DSMRDeviceConfiguration deviceConfiguration, DSMRDeviceStateListener deviceStateListener,
            DSMRMeterDiscoveryListener discoveryListener) {
        this.deviceConfiguration = deviceConfiguration;
        this.discoveryListener = discoveryListener;
        this.deviceStateListener = deviceStateListener;
        this.availableMeters = new LinkedList<>();

        setDSMRDeviceState(DeviceState.INITIALIZING, DeviceStateDetail.NONE);
    }

    /**
     * Sets a new device state
     * If the device has the state SHUTDOWN the new state will not be accepted.
     * This method will also notify the device state listener about the state update (always) / change (only if new
     * state differs from the old state)
     *
     * @param newDeviceState the requested new device state
     * @param stateDetail the details about the new device state
     */
    private void setDSMRDeviceState(DeviceState newDeviceState, DeviceStateDetail stateDetails) {
        synchronized (deviceState) {
            DeviceState currentDeviceState = deviceState.state;

            logger.debug("DSMRDevice state updated from {} to {}, details: {}", currentDeviceState, newDeviceState,
                    stateDetails);
            // Change state only if not shutting down
            if (currentDeviceState != DeviceState.SHUTDOWN) {
                deviceState.state = newDeviceState;
                deviceState.timestamp = System.currentTimeMillis();
            } else {
                logger.debug("Setting state is not allowed while in {}", deviceState);
            }
            // Notify listeners
            if (deviceStateListener != null) {
                deviceStateListener.stateUpdated(currentDeviceState, newDeviceState, stateDetails);
                if (currentDeviceState != newDeviceState) {
                    deviceStateListener.stateChanged(currentDeviceState, newDeviceState, stateDetails);
                }
            } else {
                logger.error("No device state listener available, binding will not work properly!");
            }
        }
    }

    /**
     * Starts the DSMR Device in the separate thread
     */
    public void startUpDevice() {
        new Thread(this, "DSMRDevice").start();
    }

    /**
     * The main thread of the DSMR Device
     *
     * The thread will be started from {@link DSMRDevice#startUpDevice()} and will be shutdown using
     * {@link DSMRDevice#shutdownDevice()}
     */
    @Override
    public void run() {
        DeviceStateDetail stateDetail;

        while (deviceState.state != DeviceState.SHUTDOWN) {
            try {
                if (logger.isTraceEnabled()) {
                    logger.trace("Device state: {}", deviceState);
                }
                switch (deviceState.state) {
                    case INITIALIZING:
                        if (dsmrPort != null) {
                            dsmrPort.close();
                            dsmrPort = null;
                        }
                        /*
                         * Start the parser in lenient mode to prevent flooding logs with errors during initialization
                         * of
                         * the device
                         */
                        p1Parser = new P1TelegramParser(true, this);
                        dsmrPort = new DSMRPort(deviceConfiguration.serialPort, p1Parser,
                                DSMRDeviceConstants.SERIAL_PORT_READ_TIMEOUT,
                                DSMRPortSettings.getPortSettingsFromString(deviceConfiguration.serialPortSettings));

                        stateDetail = dsmrPort.open();
                        // Open the DSMR Port
                        if (stateDetail == DeviceStateDetail.PORT_OK) {
                            logger.debug("DSMR Port opened successfully");
                            setDSMRDeviceState(DeviceState.STARTING, stateDetail);
                        } else {
                            // Opening failed. This is most likely a configuration problem.
                            logger.error(
                                    "Failed to open DSMR port, entering state CONFIGURATION_PROBLEM for {} milliseconds before reentering INITALIZING state",
                                    DSMRDeviceConstants.SERIAL_PORT_REOPEN_PERIOD);
                            setDSMRDeviceState(DeviceState.CONFIGURATION_PROBLEM, stateDetail);
                        }
                        break;
                    case STARTING:
                        // Keep track of the duration in this state to be able to switch port speed
                        long startingDuration = System.currentTimeMillis() - deviceState.timestamp;
                        if (startingDuration > DSMRDeviceConstants.SERIAL_PORT_AUTO_DETECT_TIMEOUT) {
                            logger.debug("No CosemObjects received for the last {} ms, switching port speed",
                                    startingDuration);
                            dsmrPort.switchPortSpeed();

                            setDSMRDeviceState(DeviceState.STARTING, DeviceStateDetail.PORT_DETECTING_SPEED);
                        } else {
                            dsmrPort.read();
                        }
                        break;
                    case ONLINE:
                        // Read CosemObjects
                        stateDetail = dsmrPort.read();
                        if (stateDetail != DeviceStateDetail.PORT_READ_OK) {
                            setDSMRDeviceState(DeviceState.OFFLINE, stateDetail);
                        }

                        break;
                    case OFFLINE:
                        long recoveryPeriod = System.currentTimeMillis() - deviceState.timestamp;

                        if (recoveryPeriod > DSMRDeviceConstants.OFFLINE_RECOVERY_TIMEOUT) {
                            logger.info("Tried to recover for {} ms, entering INITIALIZING", recoveryPeriod);

                            setDSMRDeviceState(DeviceState.INITIALIZING, DeviceStateDetail.RECOVER_COMMUNICATION);
                        } else {
                            stateDetail = dsmrPort.read();
                            if (stateDetail != DeviceStateDetail.PORT_READ_OK) {
                                // Update the offline state
                                setDSMRDeviceState(DeviceState.OFFLINE, stateDetail);
                            }
                            // Device state will be changed to online when receiving Cosem Objects again
                        }
                        break;
                    case CONFIGURATION_PROBLEM:
                        if (System.currentTimeMillis()
                                - deviceState.timestamp > DSMRDeviceConstants.SERIAL_PORT_REOPEN_PERIOD) {
                            setDSMRDeviceState(DeviceState.INITIALIZING, DeviceStateDetail.REINITIALIZE);
                        }

                        break;
                    case SHUTDOWN:
                        logger.info("Shut down device");

                        break;
                    default:
                        logger.error("Unknown state {}", deviceState);
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ie) {
                    logger.debug("Sleep interrupted in DSMRDevice", ie);
                }
            } catch (RuntimeException re) {
                /*
                 * Catch unexpected runtime exception to prevent unexpected stopping of this thread
                 * Only a controlled shutdown will release OS resources (this is by design to prevent
                 * OpenHAB releasing resources while the thread is still running
                 */
                logger.error("Unexpected error occured, going to state INITIALIZING", re);

                setDSMRDeviceState(DeviceState.INITIALIZING, DeviceStateDetail.REINITIALIZE);
            }
        }
        if (dsmrPort != null) {
            // Release OS-resourcing
            dsmrPort.close();
            dsmrPort = null;
        }
        logger.info("Device is shut down, due to state: {}", deviceState);
    }

    /**
     * Shutdown the device
     */
    public void shutdownDevice() {
        setDSMRDeviceState(DeviceState.SHUTDOWN, DeviceStateDetail.NONE);
    }

    /**
     * Handler for cosemObjects received in a P1 telegram
     *
     * @param cosemObjects. List of received {@link CosemObject} objects
     * @param telegramState. {@link TelegramState} describing the state of the received telegram.
     */
    @Override
    public void telegramReceived(List<CosemObject> cosemObjects, TelegramState telegramState) {
        logger.debug("Received {} Cosem Objects, telegramState: {}", cosemObjects.size(), telegramState);

        switch (deviceState.state) {
            case INITIALIZING:
                logger.info("Drop Cosem Objects during INITIALIZING state");
                break;
            case STARTING:
                if (cosemObjects.size() > 0) {
                    logger.info("CosemObjects received, entering ONLINE state with lenientMode:{}",
                            deviceConfiguration.lenientMode);

                    p1Parser.setLenientMode(deviceConfiguration.lenientMode);

                    setDSMRDeviceState(DeviceState.ONLINE, DeviceStateDetail.RUNNING_NORMAL);
                }
                break;
            case OFFLINE:
                handleIncomingCosemObjects(cosemObjects, telegramState);
                break;
            case ONLINE:
                handleIncomingCosemObjects(cosemObjects, telegramState);
                break;
            case CONFIGURATION_PROBLEM:
                logger.warn("Receiving Cosem Objects while in state CONFIGURATION_PROBLEM, dropping all");
                break;
            case SHUTDOWN:
                logger.info("Drop Cosem Objects while in state SHUTDOWN");
                break;
            default:
                logger.info("Drop Cosem Objects while in unknown state {} and entering INITIALIZING", deviceState);
                setDSMRDeviceState(DeviceState.INITIALIZING, DeviceStateDetail.REINITIALIZE);
                break;
        }
    }

    /**
     * Handle incoming Cosem objects and update the internal state accordingly:
     * - cosemObjects are received, telegramState is OK --> DSMRDevice enters ONLINE
     * - cosemObjects are received, telegramState is not OK and lenientMode is off --> DSMRDevice enters OFFLINE
     * - cosemObjects are received, telegramState is not OK and lenientMode is on --> DSMRDevice enters ONLINE
     * - no cosemObjects are received, telegramState is OK --> DSMRDeviceState will not change (unlikely situation)
     * - no cosemObjects are received, telegramState is NOK --> DSMRDevice enters OFFLINE
     *
     * @param cosemObjects List of {@link CosemObject} to handle
     * @param telegramState {@link TelegramState} describing the state of the received telegram
     * @return if the List of {@link CosemObject} was processed
     */
    private void handleIncomingCosemObjects(List<CosemObject> cosemObjects, TelegramState telegramState) {
        DeviceStateDetail stateDetail;

        switch (telegramState) {
            case CRC_ERROR:
                stateDetail = DeviceStateDetail.PORT_READ_CRC_ERROR;
                break;
            case DATA_CORRUPTION:
                stateDetail = DeviceStateDetail.PORT_READ_DATA_CORRUPT;
                break;
            case OK:
                stateDetail = DeviceStateDetail.RUNNING_NORMAL;
                break;
            default:
                stateDetail = DeviceStateDetail.PORT_READ_ERROR;
                break;
        }

        if (telegramState == TelegramState.OK) {
            if (cosemObjects.size() > 0) {
                sendCosemObjects(cosemObjects);
            } else {
                logger.info("Parsing was succesful, however there were no CosemObjects");
            }
            setDSMRDeviceState(DeviceState.ONLINE, stateDetail);
        } else {
            if (deviceConfiguration.lenientMode) {
                // In lenient mode, still send Cosem Objects
                if (cosemObjects.size() == 0) {
                    logger.warn("Did not receive anything at all in lenient mode");

                    setDSMRDeviceState(DeviceState.OFFLINE, stateDetail);
                } else {
                    sendCosemObjects(cosemObjects);
                    logger.debug("Still handling CosemObjects in lenient mode");

                    setDSMRDeviceState(DeviceState.ONLINE, stateDetail);
                }
            } else {
                // Parsing was incomplete, don't send CosemObjects
                logger.warn("Dropping {} CosemObjects due to incorrect parsing, entering OFFLINE mode",
                        cosemObjects.size());
                setDSMRDeviceState(DeviceState.OFFLINE, stateDetail);
            }
        }
    }

    /**
     * These are the cosemObjects that must be send to the available meters
     * This method will iterate through available meters and notify them about
     * the CosemObjects received.
     *
     * @param cosemObjects. The list of {@link CosemObjects} received.
     */
    private void sendCosemObjects(List<CosemObject> cosemObjects) {
        for (DSMRMeter meter : availableMeters) {
            logger.debug("Processing CosemObjects for meter {}", meter);
            List<CosemObject> processedCosemObjects = meter.handleCosemObjects(cosemObjects);
            logger.debug("Processed cosemObjects {}", processedCosemObjects);
            cosemObjects.removeAll(processedCosemObjects);
        }

        if (cosemObjects.size() > 0) {
            logger.info("There are unhandled CosemObjects, start autodetecting meters");

            List<DSMRMeterDescriptor> detectedMeters = DSMRMeterDetector.detectMeters(cosemObjects);
            logger.info("Detected the following new meters: {}", detectedMeters.toString());

            if (discoveryListener != null) {
                for (DSMRMeterDescriptor meterDescriptor : detectedMeters) {
                    if (discoveryListener.meterDiscovered(meterDescriptor)) {
                    } else {
                        logger.info("DiscoveryListener {} rejected meter descriptor {}", discoveryListener,
                                meterDescriptor);
                    }
                }
            } else {
                logger.warn("There is no listener for new meters!");
            }
        }
    }

    /**
     * Add a supported {@link DSMRMeter}
     *
     * @param dsmrMeter the {@link DSMRMeter} that is supported and can handle {@link CosemObject}
     */
    public void addDSMRMeter(DSMRMeter dsmrMeter) {
        availableMeters.add(dsmrMeter);
    }

    /**
     * Removes a supported {@link DSMRMeter}
     *
     * @param dsmrMeter the {@link DSMRMeter} that won't be supported and doesn't handle {@link CosemObject} anymore.
     */
    public void removeDSMRMeter(DSMRMeter dsmrMeter) {
        availableMeters.remove(dsmrMeter);
    }
}
