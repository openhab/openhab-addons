package org.openhab.binding.boschshc.internal;

import static org.eclipse.jetty.http.HttpMethod.GET;

import java.nio.charset.StandardCharsets;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.boschshc.internal.exceptions.PairingFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class BoschHttpClient extends HttpClient {

    private static final int MAX_PAIR_TRIES = 12;

    private final Logger logger = LoggerFactory.getLogger(BoschHttpClient.class);

    private String ipAddress;
    private String systempassword;

    public BoschHttpClient(String ipAddress, String systempassword, SslContextFactory sslContextFactory) {
        super(sslContextFactory);
        this.ipAddress = ipAddress;
        this.systempassword = systempassword;
    }

    private String getCertFromSslContextFactory() throws KeyStoreException, CertificateEncodingException {
        Certificate cert = this.getSslContextFactory().getKeyStore().getCertificate(BoschSslUtil.getBoschSHCId());
        return new String(Base64.getEncoder().encode(cert.getEncoded()));
    }

    public void checkAccessAndPairIfNecessary() throws InterruptedException, PairingFailedException {
        // test if pairing is needed
        int counter = 0;
        boolean accessPossible = isAccessPossible();
        while (!accessPossible) {
            // sleep some seconds after every try, except the first one
            if (counter > 0) {
                logger.info("Last Pairing failed, starting retry number {}/{} in few seconds", counter, MAX_PAIR_TRIES);
                Thread.sleep(15000);
            } else {
                logger.info("Pairing needed, because access to Bosch SmartHomeController not possible.");
            }
            // Timeout after max tries with an exception that pairing failed
            if (counter >= MAX_PAIR_TRIES) {
                // all tries during the last MAX_PAIR_TRIES*1500 milliseconds failed
                throw new PairingFailedException("Pairing abourted, because it failed too many times!");
            }
            // try to pair if no access is possible
            counter++;
            doPairing();
            // check access
            accessPossible = isAccessPossible();
        }

    }

    public Boolean isAccessPossible() {
        try {
            ContentResponse contentResponse = newRequest("https://" + ipAddress + ":8444/smarthome/devices")
                    .header("Content-Type", "application/json").header("Accept", "application/json").method(GET).send();
            String content = contentResponse.getContentAsString();
            logger.debug("Access check response complete: {} - return code: {}", content, contentResponse.getStatus());
            return true;
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.debug("Access check response failed!", e);
            return false;
        }
    }

    public boolean doPairing() {
        logger.info("Starting pairing OpenHab Client with Bosch SmartHomeController!");
        logger.info("Please press the Bosch SHC button until LED starts blinking");

        ContentResponse contentResponse;
        try {
            String publicCert = getCertFromSslContextFactory();
            logger.info("Pairing this Client '{}' with SHC {} using Cert {}", BoschSslUtil.getBoschSHCId(), ipAddress,
                    publicCert);

            // JSON Rest content
            Map<String, String> items = new HashMap<>();
            items.put("@type", "client");
            items.put("id", BoschSslUtil.getBoschSHCId()); // Client Id contains the unique OpenHab instance Id
            items.put("name", "oss_OpenHAB_Binding"); // Client name according to
                                                      // https://github.com/BoschSmartHome/bosch-shc-api-docs#terms-and-conditions
            items.put("primaryRole", "ROLE_RESTRICTED_CLIENT");
            items.put("certificate", "-----BEGIN CERTIFICATE-----\r" + publicCert + "\r-----END CERTIFICATE-----");

            contentResponse = this.POST("https://" + ipAddress + ":8443/smarthome/clients")
                    .header("Systempassword",
                            new String(Base64.getEncoder().encode(systempassword.getBytes(StandardCharsets.UTF_8))))
                    .header("Content-Type", "application/json").header("Accept", "application/json")
                    .content(new StringContentProvider(new Gson().toJson(items))).send();

            logger.debug("Pairing response complete: {} - return code: {}", contentResponse.getContentAsString(),
                    contentResponse.getStatus());
            if (201 == contentResponse.getStatus()) {
                logger.info("Pairing successful.");
                return true;
            } else {
                logger.info("Pairing failed with responce status {}.", contentResponse.getStatus());
                return false;
            }

        } catch (InterruptedException | TimeoutException | CertificateEncodingException | KeyStoreException e) {
            logger.error("Pairing failed with an exception");
            logger.debug("Pairing failed with: {}", e);
            return false;
        } catch (ExecutionException e) {
            // javax.net.ssl.SSLHandshakeException: General SSLEngine problem
            // => pairing failed, because hardware button was not pressed.
            logger.warn("Pairing failed, was the Bosch SHC button pressed?");
            logger.debug("Pairing failed with: {}", e);
            return false;
        }

    }

    // // TODO add request function like newBoschRequest("smarthome/devices")
    // public ContentResponse newBoschRequest(String restData) throws InterruptedException, TimeoutException,
    // ExecutionException {
    // return GET("https://" + m_ipAddress + ":8444/" + restData)
    // .header("Content-Type", "application/json").header("Accept", "application/json")
    // .send();
    // }
}
