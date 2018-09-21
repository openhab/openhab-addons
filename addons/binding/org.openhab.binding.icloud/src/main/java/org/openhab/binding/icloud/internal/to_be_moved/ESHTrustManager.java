package org.openhab.binding.icloud.internal.to_be_moved;

import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.security.auth.x500.X500Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ESHTrustManager extends X509ExtendedTrustManager {
    private final Logger logger = LoggerFactory.getLogger(ESHTrustManager.class);

    private final X509ExtendedTrustManager defaultTrustManager;
    private final Map<String, X509ExtendedTrustManager> linkedTrustManager;

    ESHTrustManager(List<EndpointKeyStore> endpointKeyStores) {
        this.defaultTrustManager = keyStoreToTrustManager(null);
        this.linkedTrustManager = endpointKeyStores.stream().collect(
                Collectors.toMap(EndpointKeyStore::getHostName, eks -> keyStoreToTrustManager(eks.getKeyStore())));
        if (logger.isTraceEnabled()) {
            logger.trace("Constructing ESHTrustManager with {} EndpointKeyStore's for: {}", endpointKeyStores.size(),
                    linkedTrustManager.keySet());
        }
    }

    private static X509ExtendedTrustManager keyStoreToTrustManager(KeyStore keyStore) {
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            // Get hold of the default trust manager
            for (TrustManager tm : tmf.getTrustManagers()) {
                if (tm instanceof X509ExtendedTrustManager) {
                    return (X509ExtendedTrustManager) tm;
                }
            }
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Default algorithm missing...", e);
        } catch (KeyStoreException e) {
            throw new IllegalStateException("Problem while processing keystore", e);
        }
        throw new IllegalStateException("Could not find X509ExtendedTrustManager");
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        checkClientTrusted(chain, authType, (Socket) null);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        checkServerTrusted(chain, authType, (Socket) null);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return defaultTrustManager.getAcceptedIssuers();
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
            throws CertificateException {
        X509ExtendedTrustManager linkedTrustManager = getLinkedTrustMananger(chain);
        if (linkedTrustManager == null) {
            logger.trace("No specific trust manager found, falling back to default");
            defaultTrustManager.checkClientTrusted(chain, authType, socket);
        } else {
            linkedTrustManager.checkClientTrusted(chain, authType, socket);
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine sslEngine)
            throws CertificateException {
        X509ExtendedTrustManager linkedTrustManager = getLinkedTrustMananger(chain);
        if (linkedTrustManager == null) {
            logger.trace("No specific trust manager found, falling back to default");
            defaultTrustManager.checkClientTrusted(chain, authType, sslEngine);
        } else {
            linkedTrustManager.checkClientTrusted(chain, authType, sslEngine);
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
            throws CertificateException {
        X509ExtendedTrustManager linkedTrustManager = getLinkedTrustMananger(chain);
        if (linkedTrustManager == null) {
            logger.trace("No specific trust manager found, falling back to default");
            defaultTrustManager.checkServerTrusted(chain, authType, socket);
        } else {
            linkedTrustManager.checkServerTrusted(chain, authType, socket);
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine sslEngine)
            throws CertificateException {
        X509ExtendedTrustManager linkedTrustManager = getLinkedTrustMananger(chain);
        if (linkedTrustManager == null) {
            logger.trace("No specific trust manager found, falling back to default");
            defaultTrustManager.checkServerTrusted(chain, authType, sslEngine);
        } else {
            linkedTrustManager.checkServerTrusted(chain, authType, sslEngine);
        }
    }

    private X509ExtendedTrustManager getLinkedTrustMananger(X509Certificate[] chain) {
        try {
            String commonName = getCommonName(chain[0]);

            X509ExtendedTrustManager trustManager = linkedTrustManager.get(commonName);

            if (trustManager != null) {
                logger.trace("Found trustManager by common name: {}", commonName);
                return trustManager;
            }

            logger.trace("Searching trustManager by Subject Alternative Names: {}",
                    chain[0].getSubjectAlternativeNames());
            // @formatter:off
            return chain[0].getSubjectAlternativeNames().stream()
                    .map(e -> e.get(1))
                    .map(Object::toString)
                    .map(linkedTrustManager::get)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
            // @formatter:on
        } catch (CertificateParsingException e) {
            throw new IllegalStateException("Problem while parsing certificate", e);
        }

    }

    private String getCommonName(X509Certificate x509Certificate) {
        String dn = x509Certificate.getSubjectX500Principal().getName(X500Principal.RFC2253);
        for (String group : dn.split(",")) {
            if (group.contains("CN=")) {
                return group.trim().replace("CN=", "");
            }
        }
        throw new IllegalStateException("No Common Name found");
    }
}
