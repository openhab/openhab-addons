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
package org.openhab.binding.dlinksmarthome.internal.motionsensor;

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

    private static final int DETECT_TIMEOUT_MS = 5000;
    private static final int DETECT_POLL_S = 1;

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

    private SOAPMessage detectionAction;

    private boolean loginSuccess;
    private boolean detectSuccess;

    private long prevDetection;
    private long lastDetection;

    private final ScheduledFuture<?> detectFuture;

    private boolean online = true;
    private DeviceStatus status = DeviceStatus.INITIALISING;

    /**
     * Inform the listener if motion is detected
     */
    private final Runnable detect = new Runnable() {
        @Override
        public void run() {
            boolean updateStatus = false;

            switch (status) {
                case INITIALISING:
                    online = false;
                    updateStatus = true;
                    // FALL-THROUGH
                case COMMUNICATION_ERROR:
                case ONLINE:
                    if (!loginSuccess) {
                        login(detectionAction, DETECT_TIMEOUT_MS);
                    }

                    if (!getLastDetection(false)) {
                        // Try login again in case the session has timed out
                        login(detectionAction, DETECT_TIMEOUT_MS);
                        getLastDetection(true);
                    }
                    break;
                default:
                    break;
            }

            if (loginSuccess && detectSuccess) {
                status = DeviceStatus.ONLINE;
                if (!online) {
                    online = true;
                    listener.sensorStatus(status);

                    // Ignore old detections
                    prevDetection = lastDetection;
                }

                if (lastDetection != prevDetection) {
                    listener.motionDetected();
                }
            } else {
                if (online || updateStatus) {
                    online = false;
                    listener.sensorStatus(status);
                }
            }
        }
    };

    public DLinkMotionSensorCommunication(final DLinkMotionSensorConfig config,
            final DLinkMotionSensorListener listener, final ScheduledExecutorService scheduler) {
        super(config.ipAddress, config.pin);
        this.listener = listener;

        if (getHNAPStatus() == HNAPStatus.INTERNAL_ERROR) {
            status = DeviceStatus.INTERNAL_ERROR;
        }

        try {
            final MessageFactory messageFactory = MessageFactory.newInstance();
            detectionAction = messageFactory.createMessage();

            buildDetectionAction();

        } catch (final SOAPException e) {
            logger.debug("DLinkMotionSensorCommunication - Internal error", e);
            status = DeviceStatus.INTERNAL_ERROR;
        }

        detectFuture = scheduler.scheduleWithFixedDelay(detect, 0, DETECT_POLL_S, TimeUnit.SECONDS);
    }

    /**
     * Stop communicating with the device
     */
    @Override
    public void dispose() {
        detectFuture.cancel(true);
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
}
