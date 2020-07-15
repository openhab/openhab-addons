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
package org.openhab.binding.nikohomecontrol.internal.handler;

import java.security.cert.CertificateException;
import java.text.DateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.nikohomecontrol.internal.protocol.nhc2.NikoHomeControlCommunication2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * {@link NikoHomeControlBridgeHandler2} is the handler for a Niko Home Control II Connected Controller and connects it
 * to the framework.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NikoHomeControlBridgeHandler2 extends NikoHomeControlBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(NikoHomeControlBridgeHandler2.class);

    private final Gson gson = new GsonBuilder().create();

    NetworkAddressService networkAddressService;

    public NikoHomeControlBridgeHandler2(Bridge nikoHomeControlBridge, NetworkAddressService networkAddressService) {
        super(nikoHomeControlBridge);
        this.networkAddressService = networkAddressService;
    }

    @Override
    public void initialize() {
        logger.debug("Niko Home Control: initializing NHC II bridge handler");

        setConfig();

        Date expiryDate = getTokenExpiryDate();
        if (expiryDate == null) {
            if (getToken().isEmpty()) {
                // We allow a not well formed token (no expiry date) to pass through.
                // This allows the user to use this as a password on a profile, with the profile UUID defined in an
                // advanced configuration, skipping token validation.
                // This behavior would allow the same logic to be used (with profile UUID) as before token validation
                // was introduced.
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "Niko Home Control: token is empty");
                return;
            }
        } else {
            Date now = new Date();
            if (expiryDate.before(now)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "Niko Home Control: hobby api token has expired");
                return;
            }
        }

        String addr = networkAddressService.getPrimaryIpv4HostAddress();
        addr = (addr == null) ? "unknown" : addr.replace(".", "_");
        String clientId = addr + "-" + thing.getUID().toString().replace(":", "_");
        try {
            nhcComm = new NikoHomeControlCommunication2(this, clientId, scheduler);
            startCommunication();
        } catch (CertificateException e) {
            // this should not happen unless there is a programming error
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Niko Home Control: not able to set SSL context");
            return;
        }
    }

    @Override
    protected void updateProperties() {
        Map<String, String> properties = new HashMap<>();

        NikoHomeControlCommunication2 comm = (NikoHomeControlCommunication2) nhcComm;
        if (comm != null) {
            Date expiry = getTokenExpiryDate();
            if (expiry != null) {
                properties.put("tokenExpiryDate", DateFormat.getDateInstance().format(expiry));
            }
            String nhcVersion = comm.getSystemInfo().getNhcVersion();
            if (!nhcVersion.isEmpty()) {
                properties.put("nhcVersion", nhcVersion);
            }
            String cocoImage = comm.getSystemInfo().getCocoImage();
            if (!cocoImage.isEmpty()) {
                properties.put("cocoImage", cocoImage);
            }
            String language = comm.getSystemInfo().getLanguage();
            if (!language.isEmpty()) {
                properties.put("language", language);
            }
            String currency = comm.getSystemInfo().getCurrency();
            if (!currency.isEmpty()) {
                properties.put("currency", currency);
            }
            String units = comm.getSystemInfo().getUnits();
            if (!units.isEmpty()) {
                properties.put("units", units);
            }
            String lastConfig = comm.getSystemInfo().getLastConfig();
            if (!lastConfig.isEmpty()) {
                properties.put("lastConfig", lastConfig);
            }
            String electricityTariff = comm.getSystemInfo().getElectricityTariff();
            if (!electricityTariff.isEmpty()) {
                properties.put("electricityTariff", electricityTariff);
            }
            String gasTariff = comm.getSystemInfo().getGasTariff();
            if (!gasTariff.isEmpty()) {
                properties.put("gasTariff", gasTariff);
            }
            String waterTariff = comm.getSystemInfo().getWaterTariff();
            if (!waterTariff.isEmpty()) {
                properties.put("waterTariff", waterTariff);
            }
            String timezone = comm.getTimeInfo().getTimezone();
            if (!timezone.isEmpty()) {
                properties.put("timezone", timezone);
            }
            String isDst = comm.getTimeInfo().getIsDst();
            if (!isDst.isEmpty()) {
                properties.put("isDST", isDst);
            }
            String services = comm.getServices();
            if (!services.isEmpty()) {
                properties.put("services", services);
            }

            thing.setProperties(properties);
        }
    }

    @Override
    public String getProfile() {
        return ((NikoHomeControlBridgeConfig2) config).profile;
    }

    @Override
    public String getToken() {
        String token = ((NikoHomeControlBridgeConfig2) config).password;
        if ((token == null) || token.isEmpty()) {
            logger.debug("Niko Home Control: no JWT token set.");
            return "";
        }
        return token;
    }

    /**
     * Extract the expiry date in the user provided token for the hobby API. Log warnings and errors if the token is
     * close to expiry or expired.
     *
     * @return Hobby API token expiry date, null if no valid token.
     */
    private @Nullable Date getTokenExpiryDate() {
        NhcJwtToken2 jwtToken = null;

        String token = getToken();
        String[] tokenArray = token.split("\\.");

        if (tokenArray.length == 3) {
            String tokenPayload = new String(Base64.getDecoder().decode(tokenArray[1]));

            try {
                jwtToken = gson.fromJson(tokenPayload, NhcJwtToken2.class);
            } catch (JsonSyntaxException e) {
                logger.debug("Niko Home Control: unexpected token payload {}", tokenPayload);
            } catch (NoSuchElementException ignore) {
                // Ignore if exp not present in response, this should not happen in token payload response
                logger.trace("Niko Home Control: no expiry date found in payload {}", tokenPayload);
            }
        }

        if (jwtToken != null) {
            Date expiryDate;
            try {
                String expiryEpoch = jwtToken.exp;
                long epoch = Long.parseLong(expiryEpoch) * 1000; // convert to milliseconds
                expiryDate = new Date(epoch);
            } catch (NumberFormatException e) {
                logger.debug("Niko Home Control: token expiry not valid {}", jwtToken.exp);
                return null;
            }

            Date now = new Date();
            if (expiryDate.before(now)) {
                logger.warn("Niko Home Control: hobby API token expired, was valid until {}",
                        DateFormat.getDateInstance().format(expiryDate));
            } else {
                Calendar c = Calendar.getInstance();
                c.setTime(expiryDate);
                c.add(Calendar.DATE, -14);
                if (c.getTime().before(now)) {
                    logger.info("Niko Home Control: hobby API token will expire in less than 14 days, valid until {}",
                            DateFormat.getDateInstance().format(expiryDate));
                }
            }
            return expiryDate;
        }

        return null;
    }

    @Override
    protected synchronized void setConfig() {
        config = getConfig().as(NikoHomeControlBridgeConfig2.class);
    }
}
