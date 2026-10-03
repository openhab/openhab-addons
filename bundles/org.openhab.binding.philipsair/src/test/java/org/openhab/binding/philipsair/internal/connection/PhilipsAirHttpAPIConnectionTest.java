/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.philipsair.internal.connection;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.philipsair.internal.FakeHttpDevice.Endpoint.*;

import java.security.GeneralSecurityException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.philipsair.internal.FakeHttpDevice;
import org.openhab.binding.philipsair.internal.FakeHttpDevice.Call;
import org.openhab.binding.philipsair.internal.PhilipsAirConfiguration;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDataDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDeviceDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierFiltersDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierWritableDataDTO;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests the key exchange and response handling of {@link PhilipsAirHttpAPIConnection}.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class PhilipsAirHttpAPIConnectionTest {

    private static final String HOST = "1.1.1.1";
    private static final String SESSION_KEY = "00112233445566778899AABBCCDDEEFF";
    private static final String OLD_SESSION_KEY = "FFEEDDCCBBAA99887766554433221100";
    private static final String OTHER_KEY = "0123456789ABCDEF0123456789ABCDEF";
    private static final String STATUS_JSON = "{\"pwr\":\"1\",\"om\":\"2\",\"pm25\":7}";
    private static final String STATUS_OFF_JSON = "{\"pwr\":\"0\",\"om\":\"2\",\"pm25\":7}";

    private final PhilipsAirConfiguration config = new PhilipsAirConfiguration();
    private final FakeHttpDevice device = new FakeHttpDevice(HOST, SESSION_KEY);

    @BeforeEach
    public void setUp() {
        config.setHost(HOST);
        config.setRefreshInterval(5);
    }

    private PhilipsAirHttpAPIConnection createConnection() {
        return new PhilipsAirHttpAPIConnection(config, device.httpClient());
    }

    private static PhilipsAirPurifierWritableDataDTO powerOff() {
        PhilipsAirPurifierWritableDataDTO command = new PhilipsAirPurifierWritableDataDTO();
        command.setPower("0");
        return command;
    }

    private static String diffieOf(Call keyExchange) {
        String body = keyExchange.body();
        assertNotNull(body);
        JsonObject json = JsonParser.parseString(body).getAsJsonObject();
        assertEquals(1, json.size());
        return json.get("diffie").getAsString();
    }

    @Test
    public void emptyKeyIsExchanged() throws Exception {
        device.respondToKeyExchange();
        device.respond(STATUS, HttpMethod.GET, STATUS_JSON);

        PhilipsAirHttpAPIConnection connection = createConnection();

        assertEquals(SESSION_KEY, config.getKey());
        List<Call> exchanges = device.calls(SECURITY, HttpMethod.PUT);
        assertEquals(1, exchanges.size());
        assertEquals("http://1.1.1.1/di/v1/products/0/security", exchanges.get(0).url());
        assertTrue(diffieOf(exchanges.get(0)).matches("[0-9a-f]+"));
        PhilipsAirPurifierDataDTO data = connection.getAirPurifierStatus(HOST);
        assertNotNull(data);
        assertEquals("1", data.getPower());
        assertEquals(7, data.getPm25());
        assertEquals(1, device.count(STATUS, HttpMethod.GET));
    }

    @Test
    public void keyExchangeWithKnownExponentSendsPublicValueAndDerivesSessionKey() throws Exception {
        FakeHttpDevice knownDevice = new FakeHttpDevice(HOST, PhilipsAirCipherTest.DEVICE_SESSION_KEY);
        knownDevice.respondRaw(SECURITY, HttpMethod.PUT, 200,
                "{\"key\":\"" + PhilipsAirCipherTest.ENCRYPTED_KEY_LONG_SECRET + "\",\"hellman\":\""
                        + PhilipsAirCipherTest.HELLMAN_LONG_SECRET + "\"}");
        knownDevice.respond(STATUS, HttpMethod.GET, STATUS_JSON);

        PhilipsAirHttpAPIConnection connection = new PhilipsAirHttpAPIConnection(config, knownDevice.httpClient()) {
            @Override
            PhilipsAirCipher createCipher() throws GeneralSecurityException {
                return new PhilipsAirCipher(PhilipsAirCipherTest.EXPONENT);
            }
        };

        assertEquals(PhilipsAirCipherTest.DEVICE_SESSION_KEY, config.getKey());
        List<Call> exchanges = knownDevice.calls(SECURITY, HttpMethod.PUT);
        assertEquals(1, exchanges.size());
        assertEquals("{\"diffie\":\"" + PhilipsAirCipherTest.PUBLIC_VALUE + "\"}", exchanges.get(0).body());
        // the derived key is the one the device encrypts with
        PhilipsAirPurifierDataDTO data = connection.getAirPurifierStatus(HOST);
        assertNotNull(data);
        assertEquals(7, data.getPm25());
    }

    @Test
    public void invalidKeyExchangeResponseIsReportedAsApiException() throws Exception {
        device.respondRaw(SECURITY, HttpMethod.PUT, 200, "{}", "", "null");

        PhilipsAirHttpAPIConnection connection = createConnection();

        assertEquals("", config.getKey());
        assertThrows(PhilipsAirAPIException.class, () -> connection.getAirPurifierStatus(HOST));
        assertEquals(2, device.count(SECURITY, HttpMethod.PUT));
        assertEquals(0, device.count(STATUS, HttpMethod.GET));
    }

    @Test
    public void statusIsRequestedAgainAfterKeyRenewal() throws Exception {
        config.setKey(OLD_SESSION_KEY);
        device.respondToKeyExchange();
        // the device lost the key: the first response is encrypted with another key
        device.respondRaw(STATUS, HttpMethod.GET, 200, FakeHttpDevice.encrypt(STATUS_JSON, OTHER_KEY),
                FakeHttpDevice.encrypt(STATUS_JSON, SESSION_KEY));

        PhilipsAirHttpAPIConnection connection = createConnection();
        PhilipsAirPurifierDataDTO data = connection.getAirPurifierStatus(HOST);

        assertNotNull(data);
        assertEquals(7, data.getPm25());
        assertEquals(SESSION_KEY, config.getKey());
        assertEquals(List.of(device.url(STATUS), device.url(SECURITY), device.url(STATUS)),
                device.calls().stream().map(Call::url).toList());
    }

    @Test
    public void commandIsNotRepeatedAfterKeyRenewal() throws Exception {
        config.setKey(OLD_SESSION_KEY);
        device.respondToKeyExchange();
        device.respondRaw(STATUS, HttpMethod.PUT, 200, FakeHttpDevice.encrypt(STATUS_JSON, OTHER_KEY));

        PhilipsAirHttpAPIConnection connection = createConnection();

        assertThrows(PhilipsAirAPIException.class, () -> connection.sendCommand("pwr", powerOff()));
        assertEquals(SESSION_KEY, config.getKey());
        List<Call> commands = device.calls(STATUS, HttpMethod.PUT);
        assertEquals(1, commands.size());
        // sent with the key that was valid when the command was created
        assertEquals("{\"pwr\":\"0\"}", FakeHttpDevice.decryptedBody(commands.get(0), OLD_SESSION_KEY));
        assertEquals(1, device.count(SECURITY, HttpMethod.PUT));
    }

    @Test
    public void statusIsCachedAndRequestedAgainAfterCommand() throws Exception {
        config.setKey(SESSION_KEY);
        device.respondRaw(STATUS, HttpMethod.GET, 200, FakeHttpDevice.encrypt(STATUS_JSON, SESSION_KEY),
                FakeHttpDevice.encrypt(STATUS_OFF_JSON, SESSION_KEY));
        device.respond(STATUS, HttpMethod.PUT, STATUS_OFF_JSON);

        PhilipsAirHttpAPIConnection connection = createConnection();
        PhilipsAirPurifierDataDTO data = connection.getAirPurifierStatus(HOST);
        assertNotNull(data);
        assertEquals("1", data.getPower());

        // within the refresh interval the response of the device is reused
        data = connection.getAirPurifierStatus(HOST);
        assertNotNull(data);
        assertEquals("1", data.getPower());
        assertEquals(1, device.count(STATUS, HttpMethod.GET));

        PhilipsAirPurifierDataDTO commandResult = connection.sendCommand("pwr", powerOff());
        assertNotNull(commandResult);
        assertEquals("0", commandResult.getPower());
        List<Call> commands = device.calls(STATUS, HttpMethod.PUT);
        assertEquals(1, commands.size());
        assertEquals("{\"pwr\":\"0\"}", device.decryptedBody(commands.get(0)));

        data = connection.getAirPurifierStatus(HOST);
        assertNotNull(data);
        assertEquals("0", data.getPower());
        assertEquals(2, device.count(STATUS, HttpMethod.GET));
    }

    @Test
    public void statusDeviceAndFiltersAreRequestedFromTheirUrls() throws Exception {
        config.setKey(SESSION_KEY);
        device.respond(STATUS, HttpMethod.GET, STATUS_JSON);
        device.respond(DEVICE, HttpMethod.GET, "{\"name\":\"Philips\",\"modelid\":\"AC2889/10\"}");
        device.respond(FILTERS, HttpMethod.GET, "{\"fltsts0\":10,\"fltsts1\":2000}");

        PhilipsAirHttpAPIConnection connection = createConnection();
        PhilipsAirPurifierDataDTO data = connection.getAirPurifierStatus(HOST);
        PhilipsAirPurifierDeviceDTO deviceInfo = connection.getAirPurifierDevice(HOST);
        PhilipsAirPurifierFiltersDTO filters = connection.getAirPurifierFiltersStatus(HOST);

        assertEquals(List.of("http://1.1.1.1/di/v1/products/1/air", "http://1.1.1.1/di/v1/products/1/device",
                "http://1.1.1.1/di/v1/products/1/fltsts"), device.calls().stream().map(Call::url).toList());
        assertEquals(List.of(HttpMethod.GET, HttpMethod.GET, HttpMethod.GET),
                device.calls().stream().map(Call::method).toList());
        assertNotNull(data);
        assertEquals(7, data.getPm25());
        assertNotNull(deviceInfo);
        assertEquals("AC2889/10", deviceInfo.getModelId());
        assertNotNull(filters);
        assertEquals(10, filters.getPreFilter());
        assertEquals(2000, filters.getHepaFilter());
    }

    @Test
    public void missingValuesAreNotZero() throws Exception {
        config.setKey(SESSION_KEY);
        device.respond(STATUS, HttpMethod.GET, "{\"pwr\":\"1\"}");
        device.respond(FILTERS, HttpMethod.GET, "{}");

        PhilipsAirHttpAPIConnection connection = createConnection();
        PhilipsAirPurifierDataDTO data = connection.getAirPurifierStatus(HOST);
        PhilipsAirPurifierFiltersDTO filters = connection.getAirPurifierFiltersStatus(HOST);

        assertNotNull(data);
        assertNull(data.getPm25());
        assertNull(data.getErrorCode());
        assertNull(data.getTimerLeft());
        assertNull(data.getAllergenLevel());
        assertNotNull(filters);
        assertNull(filters.getPreFilter());
        assertNull(filters.getHepaFilter());
        assertNull(filters.getCarbonFilter());
    }

    @Test
    public void errorResponseIsNotDecrypted() throws Exception {
        config.setKey(SESSION_KEY);
        device.respondRaw(STATUS, HttpMethod.GET, 404, "Not Found");

        PhilipsAirHttpAPIConnection connection = createConnection();

        PhilipsAirAPIException e = assertThrows(PhilipsAirAPIException.class,
                () -> connection.getAirPurifierStatus(HOST));
        assertEquals("@text/offline.communication-error.status [\"404\"]", e.getMessage());
        assertEquals(SESSION_KEY, config.getKey());
        assertEquals(1, device.calls().size());
    }

    @Test
    public void tooManyRequestsStartsCooldown() throws Exception {
        config.setKey(SESSION_KEY);
        device.respondRaw(STATUS, HttpMethod.GET, 429, "Too Many Requests");

        PhilipsAirHttpAPIConnection connection = createConnection();

        PhilipsAirAPIException first = assertThrows(PhilipsAirAPIException.class,
                () -> connection.getAirPurifierStatus(HOST));
        assertEquals("@text/offline.communication-error.status [\"429\"]", first.getMessage());
        PhilipsAirAPIException second = assertThrows(PhilipsAirAPIException.class,
                () -> connection.getAirPurifierDevice(HOST));
        assertEquals("@text/offline.communication-error.cooldown", second.getMessage());
        assertEquals(SESSION_KEY, config.getKey());
        assertEquals(1, device.calls().size());
    }

    @Test
    public void cooldownWithoutCipherKeepsKeyAndSendsNothing() throws Exception {
        // the key exchange is answered with 429, so there is no cipher
        device.respondRaw(SECURITY, HttpMethod.PUT, 429, "Too Many Requests");

        PhilipsAirHttpAPIConnection connection = createConnection();
        assertEquals(1, device.calls().size());
        config.setKey(SESSION_KEY);

        PhilipsAirAPIException e = assertThrows(PhilipsAirAPIException.class,
                () -> connection.getAirPurifierStatus(HOST));

        assertEquals("@text/offline.communication-error.cooldown", e.getMessage());
        assertEquals(SESSION_KEY, config.getKey());
        assertEquals(1, device.calls().size());
    }
}
