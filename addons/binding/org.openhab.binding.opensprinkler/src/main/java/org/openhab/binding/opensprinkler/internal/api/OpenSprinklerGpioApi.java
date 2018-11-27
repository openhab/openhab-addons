/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.opensprinkler.internal.api;

import static org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApiConstants.*;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;

/**
 * The {@link OpenSprinklerGpioApi} class is used for communicating with
 * the OpenSprinkler PI when openHAB is installed on the same Raspberry PI
 * that the OpenSprinkler PI device is using.
 *
 * @author Jonathan Giles, Chris Graham - Initial contribution
 */
public class OpenSprinklerGpioApi implements OpenSprinklerApi {
    private int firmwareVersion = -1;
    private int numberOfStations = DEFAULT_STATION_COUNT;

    private boolean[] stationState;

    private boolean connectionOpen = false;

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
    public OpenSprinklerGpioApi(int stations) {
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
    public boolean isConnected() {
        return connectionOpen;
    }

    @Override
    public void openConnection() {
        connectionOpen = true;
    }

    @Override
    public void closeConnection() {
        connectionOpen = false;
    }

    @Override
    public void openStation(int station) throws Exception {
        if (station < 0 || station >= numberOfStations) {
            throw new Exception("This OpenSprinkler PI device only has " + this.numberOfStations + " but station "
                    + station + " was requested to be opened.");
        }

        stationState[station] = false;

        pushStationState();
    }

    @Override
    public void closeStation(int station) throws Exception {
        if (station < 0 || station >= numberOfStations) {
            throw new Exception("This OpenSprinkler device only has " + this.numberOfStations + " but station "
                    + station + " was requested to be closed.");
        }

        stationState[station] = false;

        pushStationState();
    }

    @Override
    public boolean isStationOpen(int station) throws Exception {
        if (station < 0 || station >= numberOfStations) {
            throw new Exception("This OpenSprinkler device only has " + this.numberOfStations + " but station "
                    + station + " was requested for a status update.");
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
}
