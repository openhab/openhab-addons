package org.openhab.binding.boschshc.internal.devices.bridge;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.boschshc.internal.exceptions.PairingFailedException;

import java.io.File;
import java.nio.file.Paths;
import java.security.KeyStore;

/**
 * Tests cases for {@link BoschSslUtil}.
 *
 * @author Gerd Zanker - Initial contribution
 */
@NonNullByDefault
class BoschSslUtilTest {

    @BeforeAll
    static void before() {
        // Use temp folder for userdata folder
        String tmpDir = System.getProperty("java.io.tmpdir");
        System.setProperty("openhab.userdata", tmpDir != null ? tmpDir : "/tmp");
    }

    private void prepareTempDir() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        tmpDir = tmpDir != null ? tmpDir : "/tmp";
        File tempDir = Paths.get(tmpDir, "etc").toFile();
        if (!tempDir.exists()){
            assertTrue(tempDir.mkdirs());
        }
    }


    @Test
    void getBoschSHCId() {
        // OpenSource Bosch SHC clients needs start with oss
        assertTrue(BoschSslUtil.getBoschSHCId().startsWith("oss"));
    }

    @Test
    void getKeystorePath() {
        assertTrue(BoschSslUtil.getKeystorePath().endsWith(".jks"));
    }

    /**
     * Test if the keyStore can be created if it doesn't exist.
     */
    @Test
    void keyStoreAndFactory() throws PairingFailedException {
        prepareTempDir();

        // remote old, existing jks
        File keyStoreFile = new File(BoschSslUtil.getKeystorePath());
        keyStoreFile.deleteOnExit();
        if(keyStoreFile.exists()) {
            assertTrue(keyStoreFile.delete());
        }

        assertFalse(keyStoreFile.exists());

        BoschSslUtil sslUtil = new BoschSslUtil("pwd");
        // fist call where keystore is created
        KeyStore keyStore = sslUtil.getKeyStoreAndCreateIfNecessary();
        assertNotNull(keyStore);

        assertTrue(keyStoreFile.exists());

        // second call where keystore is reopened
        KeyStore keyStore2 = sslUtil.getKeyStoreAndCreateIfNecessary();
        assertNotNull(keyStore2);

        // basic test if a SSL factory instance can be created
        SslContextFactory factory = sslUtil.getSslContextFactory();
        assertNotNull(factory);
    }

}
