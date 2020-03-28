/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal.cloud;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.openhab.binding.miio.internal.MiIoBindingConstants;
import org.openhab.binding.miio.internal.MiIoCryptoException;
import org.slf4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link CloudUtil} class is used for supporting functions for Xiaomi cloud access
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class CloudUtil {

    private static final Random RANDOM = new Random();

    public static String getElementString(JsonElement jsonElement, String element, Logger logger) {
        String value = "";
        try {
            value = jsonElement.getAsJsonObject().get(element).getAsString();
        } catch (Exception e) {
            logger.debug("Json Element {} expected but missing", element);
        }
        return value;
    }

    public static void printDevices(String response, String country, Logger logger) {
        try {
            final JsonElement resp = new JsonParser().parse(response);
            if (resp.isJsonObject()) {
                final JsonObject jor = resp.getAsJsonObject();
                String result = jor.get("result").getAsJsonObject().toString();
                for (JsonElement di : jor.get("result").getAsJsonObject().get("list").getAsJsonArray()) {
                    if (di.isJsonObject()) {
                        final JsonObject deviceInfo = di.getAsJsonObject();
                        logger.debug(
                                "Xiaomi cloud info: device name: '{}', did: '{}', token: '{}', ip: {}, server: {} ",
                                deviceInfo.get("name").getAsString(), deviceInfo.get("did").getAsString(),
                                deviceInfo.get("token").getAsString(), deviceInfo.get("localip").getAsString(),
                                country);
                    }
                }
                logger.trace("Devices: {}", result);
            } else {
                logger.debug("Response is not a json object: '{}'", response);
            }
        } catch (JsonSyntaxException | IllegalStateException e) {
            logger.info("Error while printing devices: {}", e.getMessage());
        }

    }

    public static void saveFile(String data, String country, Logger logger) {
        String dbFolderName = ConfigConstants.getUserDataFolder() + File.separator + MiIoBindingConstants.BINDING_ID;
        File folder = new File(dbFolderName);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File dataFile = new File(dbFolderName + File.separator + "miioTokens-" + country + ".json");
        try (FileWriter writer = new FileWriter(dataFile)) {
            writer.write(data);
            logger.debug("Devices token info saved to {}", dataFile.getAbsolutePath());
        } catch (IOException e) {
            logger.debug("Failed to write token file '{}': {}", dataFile.getName(), e.getMessage());
        }
    }

    /**
     * Generate signature for the request.
     *
     * @param method http request method. GET or POST
     * @param requestUrl the full request url. e.g.: http://api.xiaomi.com/getUser?id=123321
     * @param params request params. This should be a TreeMap because the
     *            parameters are required to be in lexicographic order.
     * @param signedNonce secret key for encryption.
     * @return hash value for the values provided
     * @throws MiIoCryptoException
     */
    public static String generateSignature(@Nullable String requestUrl, @Nullable String signedNonce, String nonce,
            @Nullable Map<String, String> params) throws MiIoCryptoException {
        if (signedNonce == null || signedNonce.length() == 0) {
            throw new MiIoCryptoException("key is not nullable");
        }
        List<String> exps = new ArrayList<String>();

        if (requestUrl != null) {
            URI uri = URI.create(requestUrl);
            exps.add(uri.getPath());
        }
        exps.add(signedNonce);
        exps.add(nonce);

        if (params != null && !params.isEmpty()) {
            final TreeMap<String, String> sortedParams = new TreeMap<String, String>(params);
            Set<Map.Entry<String, String>> entries = sortedParams.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                exps.add(String.format("%s=%s", entry.getKey(), entry.getValue()));
            }
        }
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (String s : exps) {
            if (!first) {
                sb.append('&');
            } else {
                first = false;
            }
            sb.append(s);
        }
        return CloudCrypto.hMacSha256Encode(Base64.getDecoder().decode(signedNonce), sb.toString().getBytes());
    }

    public static String generateNonce(long milli) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(output);
        dataOutputStream.writeLong(RANDOM.nextLong());
        dataOutputStream.writeInt((int) (milli / 60000));
        dataOutputStream.flush();
        return Base64.getEncoder().encodeToString(output.toByteArray());
    }

    public static String signedNonce(String ssecret, String nonce) throws IOException, MiIoCryptoException {
        byte[] byteArrayS = Base64.getDecoder().decode(ssecret.getBytes());
        byte[] byteArrayN = Base64.getDecoder().decode(nonce.getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(byteArrayS);
        output.write(byteArrayN);
        return CloudCrypto.sha256Hash(output.toByteArray());
    }

    public static void writeBytesToFileNio(byte[] bFile, String fileDest) throws IOException {
        Path path = Paths.get(fileDest);
        Files.write(path, bFile);
    }
}
