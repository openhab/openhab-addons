/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.dlinksmarthome.internal.motionsensor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.openhab.binding.dlinksmarthome.internal.DLinkHNAPCommunication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * The {@link DLinkMotionSensorCommunication} is responsible for communicating with a DCH-S150
 * motion sensor.
 *
 * Motion is detected by polling the last detection time via the HNAP interface.
 *
 * Reverse engineered from Login.html and soapclient.js retrieved from the device.
 *
 * @author Mike Major - Initial contribution
 */
public class DLinkMotionSensorCommunication extends DLinkHNAPCommunication {

    // SOAP actions
    private static final String DETECTION_ACTION = "\"http://purenetworks.com/HNAP1/GetLatestDetection\"";
    private static final String REBOOT_ACTION = "\"http://purenetworks.com/HNAP1/Reboot\"";

    private static final int DETECT_TIMEOUT_MS = 5000;
    private static final int DETECT_POLL_S = 1;

    private static final int REBOOT_TIMEOUT_MS = 60000;
    private static final int REBOOT_WAIT_S = 35;

    /**
     * Indicates the device status
     *
     */
    public enum DeviceStatus {
        /**
         * Starting communication with device
         */
        INITIALISING,
        /**
         * Successfully communicated with device
         */
        ONLINE,
        /**
         * Problem communicating with device
         */
        COMMUNICATION_ERROR,
        /**
         * Device is being rebooted
         */
        REBOOTING,
        /**
         * Internal error
         */
        INTERNAL_ERROR,
        /**
         * Error due to unsupported firmware
         */
        UNSUPPORTED_FIRMWARE,
        /**
         * Error due to invalid pin code
         */
        INVALID_PIN
    }

    /**
     * Use to log connection issues
     */
    private final Logger logger = LoggerFactory.getLogger(DLinkMotionSensorCommunication.class);

    private final DLinkMotionSensorListener listener;
    private final ScheduledExecutorService scheduler;

    private int rebootHour;

    private SOAPMessage detectionAction;
    private SOAPMessage rebootAction;

    private boolean loginSuccess;
    private boolean detectSuccess;
    private boolean rebootSuccess;

    private long prevDetection;
    private long lastDetection;

    private ScheduledFuture<?> detectFuture;
    private ScheduledFuture<?> rebootFuture;

    private boolean rebootRequired = false;
    private DeviceStatus status = DeviceStatus.INITIALISING;

    /**
     * Inform the listener if motion is detected
     */
    private final Runnable detect = new Runnable() {
        @Override
        public void run() {
            final DeviceStatus currentStatus = status;
            final boolean tryReboot = rebootRequired;

            switch (status) {
                case INITIALISING:
                case REBOOTING:
                    loginSuccess = false;
                    // FALL-THROUGH
                case COMMUNICATION_ERROR:
                case ONLINE:
                    if (!tryReboot) {
                        if (!loginSuccess) {
                            login(detectionAction, DETECT_TIMEOUT_MS);
                        }

                        if (!getLastDetection(false)) {
                            // Try login again in case the session has timed out
                            login(detectionAction, DETECT_TIMEOUT_MS);
                            getLastDetection(true);
                        }
                    } else {
                        login(rebootAction, REBOOT_TIMEOUT_MS);
                        reboot();
                    }
                    break;
                default:
                    break;
            }

            if (tryReboot) {
                if (rebootSuccess) {
                    rebootRequired = false;
                    status = DeviceStatus.REBOOTING;
                    detectFuture.cancel(false);
                    detectFuture = scheduler.scheduleWithFixedDelay(detect, REBOOT_WAIT_S, DETECT_POLL_S,
                            TimeUnit.SECONDS);
                }
            } else if (loginSuccess && detectSuccess) {
                status = DeviceStatus.ONLINE;
                if (currentStatus != DeviceStatus.ONLINE) {
                    // Ignore old detections
                    prevDetection = lastDetection;
                }

                if (lastDetection != prevDetection) {
                    listener.motionDetected();
                }
            }

            if (currentStatus != status) {
                listener.sensorStatus(status);
            }
        }
    };

    /**
     * Reboot the device
     */
    private final Runnable reboot = new Runnable() {
        @Override
        public void run() {
            rebootRequired = true;
            rebootFuture = scheduler.schedule(reboot, getNextRebootTime(), TimeUnit.MILLISECONDS);
        }
    };

    public DLinkMotionSensorCommunication(final DLinkMotionSensorConfig config,
            final DLinkMotionSensorListener listener, final ScheduledExecutorService scheduler) {
        super(config.ipAddress, config.pin);
        this.listener = listener;
        this.scheduler = scheduler;
        this.rebootHour = config.rebootHour;

        if (getHNAPStatus() == HNAPStatus.INTERNAL_ERROR) {
            status = DeviceStatus.INTERNAL_ERROR;
        }

        try {
            final MessageFactory messageFactory = MessageFactory.newInstance();
            detectionAction = messageFactory.createMessage();
            rebootAction = messageFactory.createMessage();

            buildDetectionAction();
            buildRebootAction();

        } catch (final SOAPException e) {
            logger.debug("DLinkMotionSensorCommunication - Internal error", e);
            status = DeviceStatus.INTERNAL_ERROR;
        }

        detectFuture = scheduler.scheduleWithFixedDelay(detect, 0, DETECT_POLL_S, TimeUnit.SECONDS);
        rebootFuture = scheduler.schedule(reboot, getNextRebootTime(), TimeUnit.MILLISECONDS);
    }

    /**
     * Stop communicating with the device
     */
    @Override
    public void dispose() {
        detectFuture.cancel(true);
        rebootFuture.cancel(true);
        super.dispose();
    }

    /**
     * This is the SOAP message used to retrieve the last detection time. This message will
     * only receive a successful response after the login process has been completed and the
     * authentication data has been set.
     *
     * @throws SOAPException
     */
    private void buildDetectionAction() throws SOAPException {
        detectionAction.getSOAPHeader().detachNode();
        final SOAPBody soapBody = detectionAction.getSOAPBody();
        final SOAPElement soapBodyElem = soapBody.addChildElement("GetLatestDetection", "", HNAP_XMLNS);
        soapBodyElem.addChildElement("ModuleID").addTextNode("1");

        final MimeHeaders headers = detectionAction.getMimeHeaders();
        headers.addHeader(SOAPACTION, DETECTION_ACTION);
    }

    /**
     * This is the SOAP message used to reboot the device. This message will
     * only receive a successful response after the login process has been completed and the
     * authentication data has been set. Device needs rebooting as it eventually becomes
     * unresponsive due to cloud services being shutdown.
     *
     * @throws SOAPException
     */
    private void buildRebootAction() throws SOAPException {
        rebootAction.getSOAPHeader().detachNode();
        final SOAPBody soapBody = rebootAction.getSOAPBody();
        soapBody.addChildElement("Reboot", "", HNAP_XMLNS);

        final MimeHeaders headers = rebootAction.getMimeHeaders();
        headers.addHeader(SOAPACTION, REBOOT_ACTION);
    }

    /**
     * Get the number of milliseconds to the next reboot time
     *
     * @return Time in ms to next reboot
     */
    private long getNextRebootTime() {
        final LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextReboot = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), rebootHour, 0,
                0);

        if (!nextReboot.isAfter(now)) {
            nextReboot = nextReboot.plusDays(1);
        }

        return now.until(nextReboot, ChronoUnit.MILLIS);
    }

    /**
     * Output unexpected responses to the debug log and sets the FIRMWARE error.
     *
     * @param message
     * @param soapResponse
     */
    private void unexpectedResult(final String message, final Document soapResponse) {
        logUnexpectedResult(message, soapResponse);

        // Best guess when receiving unexpected responses
        status = DeviceStatus.UNSUPPORTED_FIRMWARE;
    }

    /**
     * Sends the two login messages and sets the authentication header for the action
     * message.
     *
     * @param action
     * @param timeout
     */
    private void login(final SOAPMessage action, final int timeout) {
        loginSuccess = false;

        login(timeout);
        setAuthenticationHeaders(action);

        switch (getHNAPStatus()) {
            case LOGGED_IN:
                loginSuccess = true;
                break;
            case COMMUNICATION_ERROR:
                status = DeviceStatus.COMMUNICATION_ERROR;
                break;
            case INVALID_PIN:
                status = DeviceStatus.INVALID_PIN;
                break;
            case INTERNAL_ERROR:
                status = DeviceStatus.INTERNAL_ERROR;
                break;
            case UNSUPPORTED_FIRMWARE:
                status = DeviceStatus.UNSUPPORTED_FIRMWARE;
                break;
            case INITIALISED:
            default:
                break;
        }
    }

    /**
     * Sends the detection message
     *
     * @param isRetry - Has this been called as a result of a login retry
     * @return true, if the last detection time was successfully retrieved, otherwise false
     */
    private boolean getLastDetection(final boolean isRetry) {
        detectSuccess = false;

        if (loginSuccess) {
            try {
                final Document soapResponse = sendReceive(detectionAction, DETECT_TIMEOUT_MS);

                final Node result = soapResponse.getElementsByTagName("GetLatestDetectionResult").item(0);

                if (result != null) {
                    if (OK.equals(result.getTextContent())) {
                        final Node timeNode = soapResponse.getElementsByTagName("LatestDetectTime").item(0);

                        if (timeNode != null) {
                            prevDetection = lastDetection;
                            lastDetection = Long.valueOf(timeNode.getTextContent());
                            detectSuccess = true;
                        } else {
                            unexpectedResult("getLastDetection - Unexpected response", soapResponse);
                        }
                    } else if (isRetry) {
                        unexpectedResult("getLastDetection - Unexpected response", soapResponse);
                    }
                } else {
                    unexpectedResult("getLastDetection - Unexpected response", soapResponse);
                }
            } catch (final InterruptedException e) {
                status = DeviceStatus.COMMUNICATION_ERROR;
                Thread.currentThread().interrupt();
            } catch (final Exception e) {
                // Assume there has been some problem trying to send one of the messages
                if (status != DeviceStatus.COMMUNICATION_ERROR) {
                    logger.debug("getLastDetection - Communication error", e);
                    status = DeviceStatus.COMMUNICATION_ERROR;
                }
            }
        }

        return detectSuccess;
    }

    /**
     * Sends the reboot message
     *
     */
    private void reboot() {
        rebootSuccess = false;

        if (loginSuccess) {
            try {
                final Document soapResponse = sendReceive(rebootAction, REBOOT_TIMEOUT_MS);

                final Node result = soapResponse.getElementsByTagName("RebootResult").item(0);

                if (result != null && OK.equals(result.getTextContent())) {
                    rebootSuccess = true;
                } else {
                    unexpectedResult("reboot - Unexpected response", soapResponse);
                }
            } catch (final Exception e) {
                // Assume there has been some problem trying to send one of the messages
                if (status != DeviceStatus.COMMUNICATION_ERROR) {
                    logger.debug("getLastDetection - Communication error", e);
                    status = DeviceStatus.COMMUNICATION_ERROR;
                }
            }
        }
    }
}
