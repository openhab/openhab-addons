/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.salus.internal;

import static java.lang.Math.max;
import static java.util.Objects.requireNonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Test;
import org.openhab.binding.salus.internal.aws.http.AwsSalusApi;
import org.openhab.binding.salus.internal.cloud.rest.HttpSalusApi;
import org.openhab.binding.salus.internal.rest.Device;
import org.openhab.binding.salus.internal.rest.DeviceProperty;
import org.openhab.binding.salus.internal.rest.GsonMapper;
import org.openhab.binding.salus.internal.rest.HttpClient;
import org.openhab.binding.salus.internal.rest.exceptions.AuthSalusApiException;
import org.openhab.binding.salus.internal.rest.exceptions.SalusApiException;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class ReverseEngineerProtocol implements AutoCloseable {
    static final Logger LOGGER = LoggerFactory.getLogger(ReverseEngineerProtocol.class);
    final List<String> methods = List.of("findDevices", "findDeviceProperties", "findDeltaInProperties",
            "monitorProperty");
    final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    final String baseUrl = "https://service-api.eu.premium.salusconnect.io";
    final org.eclipse.jetty.client.HttpClient client = new org.eclipse.jetty.client.HttpClient(
            new SslContextFactory.Client());
    final HttpClientFactory httpClientFactory = new HttpClientFactory() {

        @Override
        public org.eclipse.jetty.client.HttpClient createHttpClient(String consumerName) {
            throw new UnsupportedOperationException("ReverseEngineerProtocol.createHttpClient(consumerName)");
        }

        @Override
        public org.eclipse.jetty.client.HttpClient createHttpClient(String consumerName,
                @Nullable SslContextFactory sslContextFactory) {
            throw new UnsupportedOperationException(
                    "ReverseEngineerProtocol.createHttpClient(consumerName, sslContextFactory)");
        }

        @Override
        public org.eclipse.jetty.client.HttpClient getCommonHttpClient() {
            return client;
        }

        @Override
        public HTTP2Client createHttp2Client(String consumerName) {
            throw new UnsupportedOperationException("ReverseEngineerProtocol.createHttp2Client(consumerName)");
        }

        @Override
        public HTTP2Client createHttp2Client(String consumerName, @Nullable SslContextFactory sslContextFactory) {
            throw new UnsupportedOperationException(
                    "ReverseEngineerProtocol.createHttp2Client(consumerName, sslContextFactory)");
        }
    };
    final SalusApi api;

    public ReverseEngineerProtocol(String username, String password, String apiType) throws Exception {
        requireNonNull(username);
        requireNonNull(password);
        requireNonNull(apiType);

        client.start();
        var restClient = new HttpClient(client);
        var gsonMapper = new GsonMapper();
        if (apiType.equals(AwsSalusApi.class.getSimpleName())) {
            api = new AwsSalusApi(httpClientFactory, username, password.getBytes(StandardCharsets.UTF_8), baseUrl,
                    restClient, gsonMapper, "eu-central-1_XGRz3CgoY", "60912c00-287d-413b-a2c9-ece3ccef9230",
                    "4pk5efh3v84g5dav43imsv4fbj", "eu-central-1", "salus-eu", "a24u3z7zzwrtdl-ats");
        } else if (apiType.equals(HttpSalusApi.class.getSimpleName())) {
            api = new HttpSalusApi(username, password.getBytes(StandardCharsets.UTF_8), baseUrl, restClient, gsonMapper,
                    Clock.systemDefaultZone());
        } else {
            printUsage();
            throw new IllegalStateException("Invalid api type: " + apiType);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            printUsage();
            throw new IllegalStateException("Check usage");
        }

        var runIndefinitely = args.length == 3;
        if (runIndefinitely) {
            LOGGER.info("Will run indefinitely, use ctrl-C to exit");
        }
        var queue = newQueue(args);
        try (var reverseProtocol = new ReverseEngineerProtocol(requireNonNull(queue.poll()),
                requireNonNull(queue.poll()), requireNonNull(queue.poll()))) {
            // noinspection LoopConditionNotUpdatedInsideLoop
            do {
                reverseProtocol.run(queue);
            } while (runIndefinitely);
        }
        LOGGER.info("Bye bye 👋");
    }

    private static Queue<String> newQueue(String[] args) {
        var queue = new ArrayBlockingQueue<String>(args.length);
        queue.addAll(Arrays.asList(args));
        return queue;
    }

    private void run(Queue<String> queue) throws Exception {
        var method = findMethod(queue);
        LOGGER.info("Will invoke method [" + method + "]");
        switch (method) {
            case "findDevices":
                findDevices();
                break;
            case "findDeviceProperties":
                findDeviceProperties(findDsn(queue));
                break;
            case "findDeltaInProperties":
                findDeltaInProperties(findDsn(queue));
                break;
            case "monitorProperty":
                monitorProperty(findDsn(queue), findPropertyName(queue, 6), findSleep(queue, 7));
                break;
            default:
                printUsage();
                throw new IllegalStateException("Invalid method: [" + method + "]");
        }
    }

    private String findMethod(Queue<String> args) throws IOException {
        var item = args.poll();
        if (item != null) {
            return item;
        }

        int response = 0;
        while (response < 1 || response > methods.size()) {
            LOGGER.info(String.format("Please choose [method] 1-%d:", methods.size()));
            for (int i = 0; i < methods.size(); i++) {
                LOGGER.info(String.format("\t[%d]: %s", i + 1, methods.get(i)));
            }
            try {
                response = Integer.parseInt(reader.readLine());
            } catch (NumberFormatException e) {
                LOGGER.info(e.getMessage());
            }
        }
        return methods.get(response - 1);
    }

    private String findNextElement(String name, Queue<String> args) throws IOException {
        var item = args.poll();
        if (item != null) {
            return item;
        }
        LOGGER.info("Please pass [{}]:", name);
        var line = "";
        while (line == null || line.isEmpty()) {
            line = reader.readLine();
        }
        return line;
    }

    private String findDsn(Queue<String> args) throws IOException {
        return findNextElement("dsn", args);
    }

    private String findPropertyName(Queue<String> args, int idx) throws IOException {
        return findNextElement("propertyName", args);
    }

    @Nullable
    private Long findSleep(Queue<String> args, int idx) {
        var item = args.poll();
        if (item == null) {
            return null;
        }
        try {
            return Long.parseLong(item);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void printUsage() {
        LOGGER.info("""
                Usage:
                \tReverseEngineerProtocol <username> <password> <apiType> <method-name?> <params...>
                \tSupported method types:
                \t\tfindDevices
                \t\tfindDeviceProperties <dsn>
                \t\tfindDeltaInProperties <dsn>
                \t\tmonitorProperty <dsn> <propertyName> <sleepTime?>
                """);
    }

    @Test
    void findDevices() throws AuthSalusApiException, SalusApiException {
        var devices = api.findDevices();
        LOGGER.info(String.format("Your devices (%s):", api.getClass().getSimpleName()));
        printDevices(devices);
    }

    @Test
    void findDeviceProperties(String dsn) throws AuthSalusApiException, SalusApiException {
        var properties = api.findDeviceProperties(dsn);
        LOGGER.info(String.format("Properties for device %s (%s):", dsn, api.getClass().getSimpleName()));
        printDevicesProperties(properties);
    }

    void findDeltaInProperties(String dsn) throws AuthSalusApiException, SalusApiException, IOException {
        requireNonNull(dsn);

        var differentProperties = api.findDeviceProperties(dsn);
        var answer = "";
        while (true) {
            if (differentProperties.isEmpty()) {
                LOGGER.info("There are no more properties 😬...");
                break;
            }
            printDevicesProperties(differentProperties);

            LOGGER.info("Read one more time and leave properties that changed (x) / not changed (q) or finish (f):");
            answer = reader.readLine();
            if (answer.equalsIgnoreCase("f")) {
                break;
            }
            if (!answer.equalsIgnoreCase("x") && !answer.equalsIgnoreCase("q")) {
                LOGGER.info("Wrong answer: " + answer);
                continue;
            }
            var changed = answer.equalsIgnoreCase("x");

            var beforeSize = differentProperties.size();
            var currentProperties = api.findDeviceProperties(dsn);
            var oldProps = new TreeSet<>(differentProperties);
            differentProperties = currentProperties.stream()//
                    .filter(currentProp -> filterProperties(oldProps, currentProp, changed))
                    .collect(Collectors.toCollection(TreeSet::new));
            var currentSize = differentProperties.size();
            var delta = beforeSize - currentSize;
            LOGGER.info(String.format("Current size: %d, beforeSize: %d, Δ: %d", currentSize, beforeSize, delta));
        }

        LOGGER.info(String.format("Properties for device %s (%s):", dsn, api.getClass().getSimpleName()));
        if (differentProperties.isEmpty()) {
            LOGGER.info("None 😬...");
        } else {
            printDevicesProperties(differentProperties);
        }
    }

    private boolean filterProperties(SortedSet<DeviceProperty<?>> oldProps, DeviceProperty<?> currentProp,
            boolean changed) {
        return oldProps.stream()//
                .filter(p -> p.getName().equals(currentProp.getName()))//
                .anyMatch(p -> changed != Objects.equals(p.getValue(), currentProp.getValue()));
    }

    void monitorProperty(String dsn, String propertyName, @Nullable Long sleep)
            throws AuthSalusApiException, SalusApiException, InterruptedException {
        requireNonNull(dsn);
        requireNonNull(propertyName);
        if (sleep == null) {
            sleep = 1L;
        }

        LOGGER.info("Finish loop by ctrl+c");
        while (true) {
            var deviceProperty = api.findDeviceProperties(dsn).stream()//
                    .filter(p -> p.getName().equals(propertyName))//
                    .findAny();
            if (deviceProperty.isPresent()) {
                LOGGER.info(deviceProperty.get() + "");
            } else {
                LOGGER.info("Property does not exists!");
                break;
            }
            TimeUnit.SECONDS.sleep(sleep);
        }
    }

    private void printDevices(Collection<Device> devices) {
        var sizeLength = String.valueOf(devices.size()).length();
        var longestDsn = max("dsn".length(),
                devices.stream().map(Device::dsn).mapToInt(String::length).max().orElse(0));
        var longestName = max("name".length(),
                devices.stream().map(Device::name).map(String::valueOf).mapToInt(String::length).max().orElse(0));
        var margins = 8;
        var pipe = "═".repeat(sizeLength + longestDsn + longestName + margins);
        System.out.printf("╔%s╦%s╦%s╗", "═".repeat(sizeLength + 2), "═".repeat(longestDsn + 2),
                "═".repeat(longestName + 2));
        System.out.printf("║ %s ║ %s ║ %s ║", rightAlign("#", sizeLength), leftAlign("name", longestDsn),
                leftAlign("value", longestName));
        System.out.printf("╠%s╬%s╬%s╣", "═".repeat(sizeLength + 2), "═".repeat(longestDsn + 2),
                "═".repeat(longestName + 2));

        var idx = 1;
        for (var device : devices) {
            System.out.printf("║ %s ║ %s ║ %s ║", //
                    rightAlign(String.valueOf(idx), sizeLength), //
                    leftAlign(device.dsn(), longestDsn), //
                    leftAlign(device.name(), longestName));
            idx++;
        }

        System.out.printf("╚%s╩%s╩%s╝", "═".repeat(sizeLength + 2), "═".repeat(longestDsn + 2),
                "═".repeat(longestName + 2));
    }

    private void printDevicesProperties(Collection<DeviceProperty<?>> properties) {
        var sizeLength = String.valueOf(properties.size()).length();
        var longestName = max("name".length(),
                properties.stream().map(DeviceProperty::getName).mapToInt(String::length).max().orElse(0));
        var longestValue = max("value".length(), properties.stream().map(DeviceProperty::getValue).map(String::valueOf)
                .mapToInt(String::length).max().orElse(0));
        var margins = 8;
        var pipe = "═".repeat(sizeLength + longestName + longestValue + margins);
        System.out.printf("╔%s╦%s╦%s╗", "═".repeat(sizeLength + 2), "═".repeat(longestName + 2),
                "═".repeat(longestValue + 2));
        System.out.printf("║ %s ║ %s ║ %s ║", rightAlign("#", sizeLength), leftAlign("name", longestName),
                leftAlign("value", longestValue));
        System.out.printf("╠%s╬%s╬%s╣", "═".repeat(sizeLength + 2), "═".repeat(longestName + 2),
                "═".repeat(longestValue + 2));

        var idx = 1;
        for (var property : properties) {
            System.out.printf("║ %s ║ %s ║ %s ║", //
                    rightAlign(String.valueOf(idx), sizeLength), //
                    leftAlign(property.getName(), longestName), //
                    leftAlign(property.getValue(), longestValue));
            idx++;
        }

        System.out.printf("╚%s╩%s╩%s╝", "═".repeat(sizeLength + 2), "═".repeat(longestName + 2),
                "═".repeat(longestValue + 2));
    }

    private String rightAlign(String inputString, int length) {
        if (inputString.length() >= length) {
            return inputString;
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length - inputString.length()) {
            sb.append(' ');
        }
        sb.append(inputString);

        return sb.toString();
    }

    private String leftAlign(@Nullable Object obj, int length) {
        var inputString = String.valueOf(obj);
        if (inputString.length() >= length) {
            return inputString;
        }
        StringBuilder sb = new StringBuilder(inputString);
        while (sb.length() < length) {
            sb.append(' ');
        }

        return sb.toString();
    }

    @Override
    public void close() throws Exception {
        client.stop();
    }
}
