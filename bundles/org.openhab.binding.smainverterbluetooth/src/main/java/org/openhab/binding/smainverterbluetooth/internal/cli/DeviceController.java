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
package org.openhab.binding.smainverterbluetooth.internal.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smainverterbluetooth.internal.cli.dto.InverterData;
import org.openhab.binding.smainverterbluetooth.internal.config.SmaInverterBluetoothConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
//import com.google.gson.JsonSyntaxException;

/**
 * The {@link DeviceController} class defines methods that control
 * communication with the inverter.
 * It collects data from CLI for the various data objects defined in the dto package.
 * 
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class DeviceController {

    private InverterData inverterData = new InverterData();
    @Nullable
    private String bluetoothAddress = "";
    @Nullable
    private String password = "";
    int exitCode = -1;
    @SuppressWarnings("null")
    private final Logger logger = LoggerFactory.getLogger(DeviceController.class);

    @SuppressWarnings("null")
    private final String PATH = Path.of(System.getenv("OPENHAB_USERDATA"), "files", "sma2json.exe").toString();

    public DeviceController() {
    }

    public DeviceController(SmaInverterBluetoothConfiguration config) {
        this.bluetoothAddress = config.getBluetoothAddress();
        this.password = config.getPassword();
    }

    public int fetchInverterData() {
        logger.debug("Using CLI Path: {}", PATH);

        // Define the command and its arguments as a List
        List<String> command = new ArrayList<>();
        command.add(PATH);
        command.add("-b");
        command.add(this.bluetoothAddress + "");
        command.add("-s");
        @SuppressWarnings("null")
        ProcessBuilder pb = new ProcessBuilder(command);
        logger.debug("Trying Grid Solar Data from CLI");
        this.exitCode = -1;
        try {
            Process p = pb.start();
            // Setup to write to the process's stdin (improves password security)
            String inputToSend = this.password + "\n";
            try (OutputStream stdin = p.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin, StandardCharsets.UTF_8))) {
                writer.write(inputToSend);
                writer.flush();
            }
            // Capture the standard output
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
            // It is crucial to also handle the error stream to prevent deadlocks
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            StringBuilder errorOutput = new StringBuilder();
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                errorOutput.append(errorLine).append(System.lineSeparator());
            }
            // Wait for the process to complete, with a timeout
            boolean finished = p.waitFor(10, TimeUnit.SECONDS);

            if (finished) {
                this.exitCode = p.exitValue();
                String outputString = output.toString();
                logger.trace("Command executed with exit code: {} result: {} error if any: {}", this.exitCode,
                        outputString, errorOutput.toString());
                Gson gson = new Gson();
                @Nullable
                InverterData inverterData = gson.fromJson(outputString, InverterData.class);
                this.inverterData = inverterData != null ? inverterData : new InverterData();

            } else {
                // Handle timeout
                logger.debug("Command timed out, destroying process.");
                p.destroy();
            }

        } catch (Exception e) {
            logger.debug("Error executing CLI command", e);
        }
        return this.exitCode;
    }

    public int getCode() {
        return this.inverterData.getCode();
    }

    public String getMessage() {
        return this.inverterData.getMessage();
    }

    public int getDaily() {
        return this.inverterData.getDaily() / 1000;
    }

    public long getTotal() {
        return this.inverterData.getTotal() / 1000;
    }

    public int getSpotPower() {
        return this.inverterData.getSpotPower();
    }

    public double getSpotTemperature() {
        return this.inverterData.getSpotTemperature();
    }

    public double getSpotACVolts() {
        return this.inverterData.getSpotACVolts();
    }

    public String getTime() {
        return this.inverterData.getTime();
    }
}
