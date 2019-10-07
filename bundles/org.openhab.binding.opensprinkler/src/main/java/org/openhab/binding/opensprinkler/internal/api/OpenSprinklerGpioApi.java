/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.opensprinkler.internal.api;

import static org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApiConstants.*;

import java.math.BigDecimal;

import org.openhab.binding.opensprinkler.internal.api.exception.CommunicationApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.GeneralApiException;
import org.openhab.binding.opensprinkler.internal.model.StationProgram;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;

/**
 * The {@link OpenSprinklerGpioApi} class is used for communicating with
 * the OpenSprinkler PI when openHAB is installed on the same Raspberry PI
 * that the OpenSprinkler PI device is using.
 *
 * @author Jonathan Giles, Chris Graham - Initial contribution
 * @author Florian Schmidt - Refactor class visibility
 */
class OpenSprinklerGpioApi implements OpenSprinklerApi {
    private int firmwareVersion = -1;
    private int numberOfStations = DEFAULT_STATION_COUNT;

    private boolean[] stationState;

    private boolean isInManualMode = false;

    private final GpioPinDigitalOutput srClkOutputPin;
    private final GpioPinDigitalOutput srNoeOutputPin;
    private final GpioPinDigitalOutput srDatOutputPin;
    private final GpioPinDigitalOutput srLatOutputPin;

    private final GpioController gpio;

    /**
     * Constructor for the OpenSprinkler PI class to create a connection to the OpenSprinkler PI
     * device for control and obtaining status info using the GPIO.
     *
     * @param stations The number of stations to control on the OpenSprinkler PI device.
     */
    OpenSprinklerGpioApi(int stations) {
        this.numberOfStations = stations;
        this.stationState = new boolean[stations];

        for (int i = 0; i < stations; i++) {
            stationState[i] = false;
        }

        gpio = GpioFactory.getInstance();

        /* Initialize the OpenSprinkler Pi */
        srClkOutputPin = gpio.provisionDigitalOutputPin(SR_CLK_PIN);
        srNoeOutputPin = gpio.provisionDigitalOutputPin(SR_NOE_PIN);
        srNoeOutputPin.high(); /* Disable shift register output */
        srDatOutputPin = gpio.provisionDigitalOutputPin(SR_DAT_PIN);
        srLatOutputPin = gpio.provisionDigitalOutputPin(SR_LAT_PIN);
        srNoeOutputPin.low(); /* Disable shift register output */

        pullStationState();
    }

    @Override
    public boolean isManualModeEnabled() {
        return isInManualMode;
    }

    @Override
    public void enterManualMode() {
        isInManualMode = true;
    }

    @Override
    public void leaveManualMode() {
        isInManualMode = false;
    }

    @Override
    public void openStation(int station, BigDecimal duration) throws CommunicationApiException, GeneralApiException {
        if (station < 0 || station >= numberOfStations) {
            throw new GeneralApiException("This OpenSprinkler PI device only has " + this.numberOfStations
                    + " but station " + station + " was requested to be opened.");
        }

        stationState[station] = false;

        pushStationState();
    }

    @Override
    public void closeStation(int station) throws CommunicationApiException, GeneralApiException {
        if (station < 0 || station >= numberOfStations) {
            throw new GeneralApiException("This OpenSprinkler device only has " + this.numberOfStations
                    + " but station " + station + " was requested to be closed.");
        }

        stationState[station] = false;

        pushStationState();
    }

    @Override
    public boolean isStationOpen(int station) throws GeneralApiException, CommunicationApiException {
        if (station < 0 || station >= numberOfStations) {
            throw new GeneralApiException("This OpenSprinkler device only has " + this.numberOfStations
                    + " but station " + station + " was requested for a status update.");
        }

        pullStationState();

        return stationState[station];
    }

    @Override
    public boolean isRainDetected() {
        throw new UnsupportedOperationException("Rain sensor access not supported in GPIO mode.");
    }

    @Override
    public int getNumberOfStations() {
        return this.numberOfStations;
    }

    @Override
    public int getFirmwareVersion() {
        return this.firmwareVersion;
    }

    /**
     * Communicate with the GPIO of the OpenSprinkler PI device to
     * retrieve and update local station state from the device..
     */
    private void pullStationState() {
        /* Needs research */
    }

    /**
     * Communicate with the GPIO of the OpenSprinkler PI device to
     * push and update local station state to the device.
     */
    private void pushStationState() {
        srClkOutputPin.low();
        srLatOutputPin.low();

        for (int i = 1; i <= numberOfStations; i++) {
            srClkOutputPin.low();
            srDatOutputPin.setState(stationState[numberOfStations - i]);
            srClkOutputPin.high();
        }

        srLatOutputPin.high();
    }

    @Override
    public StationProgram retrieveProgram(int station) throws CommunicationApiException {
        throw new UnsupportedOperationException();
    }
}
