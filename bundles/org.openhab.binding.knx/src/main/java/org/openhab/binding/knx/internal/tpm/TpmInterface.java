/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.tpm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.auth.SecurityException;
import org.openhab.core.id.InstanceUUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tss.Helpers;
import tss.Tpm;
import tss.TpmException;
import tss.TpmFactory;
import tss.TpmHelpers;
import tss.Tss;
import tss.tpm.CreatePrimaryResponse;
import tss.tpm.StartAuthSessionResponse;
import tss.tpm.TPM2B_PUBLIC_KEY_RSA;
import tss.tpm.TPMA_OBJECT;
import tss.tpm.TPMS_NULL_ASYM_SCHEME;
import tss.tpm.TPMS_PCR_SELECTION;
import tss.tpm.TPMS_RSA_PARMS;
import tss.tpm.TPMS_SENSITIVE_CREATE;
import tss.tpm.TPMT_PUBLIC;
import tss.tpm.TPMT_SYM_DEF;
import tss.tpm.TPMT_SYM_DEF_OBJECT;
import tss.tpm.TPM_ALG_ID;
import tss.tpm.TPM_HANDLE;
import tss.tpm.TPM_PT;
import tss.tpm.TPM_RH;
import tss.tpm.TPM_SE;

/**
 * This class implements connection to a trusted platform module (TPM)
 * which can be used as a secure storage for passwords.
 *
 * @author Holger Friedrich - Initial contribution
 */
@NonNullByDefault
public class TpmInterface {
    private final Logger logger = LoggerFactory.getLogger(TpmInterface.class);
    private static final byte[] STANDARD_EK_POLICY = Helpers
            .fromHex("837197674484b3f81a90cc8d46a5d724fd52d76e06520b64f2a1da1b331469aa");

    private static final String OUTSIDE_INFO = "created by openHAB";
    private static final String USER_PWD = Objects.requireNonNullElse(InstanceUUID.get(), "habOpen");
    private static final TPMS_SENSITIVE_CREATE USER_AUTH = new TPMS_SENSITIVE_CREATE(new byte[0], USER_PWD.getBytes());

    private @Nullable CreatePrimaryResponse rsaEk;
    private @Nullable CreatePrimaryResponse rsaSrk;
    private Tpm tpm;

    public record SecuredPassword(String secret, String encIdentity, String integrityHMAC) implements Serializable {
        private static final long serialVersionUID = 238409238L;
    }

    /**
     * Create instance which interfaces a TPM.
     * 
     * @throws SecurityException
     */
    public TpmInterface() throws SecurityException {
        try {
            @Nullable
            Tpm tmpTpm = TpmFactory.platformTpm();
            if (tmpTpm == null) {
                throw new SecurityException("TPM cannot be accessed");
            } else {
                tpm = tmpTpm;
            }
        } catch (TpmException e) {
            throw new SecurityException("TPM cannot be accessed", e);
        }
    }

    /**
     * Generate keys required for encryption and decryption.
     * As TPM uses a key derivation function to derive the key from an
     * internal seed set at production time, identical keys can be created.
     * 
     * @throws SecurityException
     */
    public void generateKeys() throws SecurityException {
        try {
            Instant start = Instant.now();
            TPMT_PUBLIC rsaEkTemplate = new TPMT_PUBLIC(TPM_ALG_ID.SHA256,
                    new TPMA_OBJECT(TPMA_OBJECT.fixedTPM, TPMA_OBJECT.fixedParent, TPMA_OBJECT.sensitiveDataOrigin,
                            TPMA_OBJECT.adminWithPolicy, TPMA_OBJECT.restricted, TPMA_OBJECT.decrypt),
                    STANDARD_EK_POLICY, new TPMS_RSA_PARMS(new TPMT_SYM_DEF_OBJECT(TPM_ALG_ID.AES, 128, TPM_ALG_ID.CFB),
                            new TPMS_NULL_ASYM_SCHEME(), 2048, 0),
                    new TPM2B_PUBLIC_KEY_RSA());

            logger.info("TPM generates RSA based endorsement key, this takes time");
            rsaEk = tpm.CreatePrimary(TPM_HANDLE.from(TPM_RH.OWNER),
                    new TPMS_SENSITIVE_CREATE(new byte[0], new byte[0]), rsaEkTemplate, OUTSIDE_INFO.getBytes(),
                    new TPMS_PCR_SELECTION[0]);
            Instant end = Instant.now();
            logger.debug("TPM based RSA endorsement key generated in {} seconds",
                    Duration.between(start, end).toSeconds());
            // logger.trace("TPM based RSA EK: {}", rsaEk.toString());

            start = end;
            // Now create an "SRK" in the owner hierarchy that we can activate
            TPMT_PUBLIC srkTemplate = new TPMT_PUBLIC(TPM_ALG_ID.SHA256,
                    new TPMA_OBJECT(TPMA_OBJECT.fixedTPM, TPMA_OBJECT.fixedParent, TPMA_OBJECT.sensitiveDataOrigin,
                            TPMA_OBJECT.userWithAuth, TPMA_OBJECT.noDA, TPMA_OBJECT.restricted, TPMA_OBJECT.decrypt),
                    new byte[0], new TPMS_RSA_PARMS(new TPMT_SYM_DEF_OBJECT(TPM_ALG_ID.AES, 128, TPM_ALG_ID.CFB),
                            new TPMS_NULL_ASYM_SCHEME(), 2048, 0),
                    new TPM2B_PUBLIC_KEY_RSA());

            logger.info("TPM generates RSA based storage key, this takes time");
            rsaSrk = tpm.CreatePrimary(TPM_HANDLE.from(TPM_RH.OWNER), USER_AUTH, srkTemplate, OUTSIDE_INFO.getBytes(),
                    new TPMS_PCR_SELECTION[0]);
            // logger.trace("TPM based RSA Primary Key: {}", rsaSrk.toString());
            end = Instant.now();
            logger.debug("TPM based RSA storage key generated in {} seconds", Duration.between(start, end).toSeconds());

            logger.info("TPM key genration complete");
        } catch (TpmException e) {
            throw new SecurityException("TPM exception", e);
        }
    }

    /**
     * @param secret plain text representation of password
     * @return offline representation enctypted and bound to this TPM
     * @throws SecurityException
     */
    public SecuredPassword encryptSecret(String secret) throws SecurityException {
        try {
            if ((rsaEk == null) || (rsaSrk == null)) {
                generateKeys();
            }

            // create local copy to avoid null warnings
            CreatePrimaryResponse tmpRsaEk = rsaEk;
            CreatePrimaryResponse tmpRsaSrk = rsaSrk;
            if ((tmpRsaEk == null) || (tmpRsaSrk == null) || (tmpRsaEk.outPublic == null)) {
                throw new SecurityException("TPM keys could not be created");
            }

            // Use tss.java to create an activation credential. Note we use tss.java
            // to get the name of the object based on the TPMT_PUBLIC.
            Tss.ActivationCredential bundle = Tss.createActivationCredential(tmpRsaEk.outPublic,
                    tmpRsaSrk.outPublic.getName(), secret.getBytes());

            return new SecuredPassword(Helpers.toHex(bundle.Secret, 0, bundle.Secret.length),
                    Helpers.toHex(bundle.CredentialBlob.encIdentity, 0, bundle.CredentialBlob.encIdentity.length),
                    Helpers.toHex(bundle.CredentialBlob.integrityHMAC, 0, bundle.CredentialBlob.integrityHMAC.length));
        } catch (TpmException e) {
            throw new SecurityException("TPM exception", e);
        }
    }

    public String encryptAndSerializeSecret(String secret) throws SecurityException {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream serial = new ObjectOutputStream(stream);
            serial.writeObject(encryptSecret(secret));
            byte[] array = stream.toByteArray();
            return Helpers.toHex(array, 0, array.length);
        } catch (IOException e) {
            throw new SecurityException("Serialization failed", e);
        }
    }

    /**
     * @param secret offline repesentation of secret encrypted with this TPM.
     * @return secret in clear text
     * @throws SecurityException
     */
    public String decryptSecret(SecuredPassword secret) throws SecurityException {
        try {
            if ((rsaEk == null) || (rsaSrk == null)) {
                generateKeys();
            }

            CreatePrimaryResponse tmpRsaEk = rsaEk;
            CreatePrimaryResponse tmpRsaSrk = rsaSrk;
            if ((tmpRsaSrk == null) || (tmpRsaEk == null) || (tmpRsaEk.handle == null) || (tmpRsaSrk.handle == null)) {
                throw new SecurityException("TPM keys could not be created");
            }

            // create credential from stored representation
            Tss.ActivationCredential stored = new Tss.ActivationCredential();
            stored.Secret = Helpers.fromHex(secret.secret());
            stored.CredentialBlob.encIdentity = Helpers.fromHex(secret.encIdentity());
            stored.CredentialBlob.integrityHMAC = Helpers.fromHex(secret.integrityHMAC());

            // policy session
            byte[] nonceCaller = Helpers.RandomBytes(20);
            StartAuthSessionResponse policySession = tpm.StartAuthSession(TPM_HANDLE.NULL, TPM_HANDLE.NULL, nonceCaller,
                    new byte[0], TPM_SE.POLICY, new TPMT_SYM_DEF(), TPM_ALG_ID.SHA256);
            // password is used during creation of key handles, so it needs to be set
            policySession.handle.AuthValue = USER_PWD.getBytes();
            tpm.PolicySecret(tpm._EndorsementHandle, policySession.handle, new byte[0], new byte[0], new byte[0], 0);
            byte[] policyDigest = tpm.PolicyGetDigest(policySession.handle);
            if (!Helpers.arraysAreEqual(policyDigest, STANDARD_EK_POLICY)) {
                throw new SecurityException("TPM decryption failed");
            }

            tpm._withSessions(TPM_HANDLE.pwSession(new byte[0]), policySession.handle);
            byte[] recoveredSecret = tpm.ActivateCredential(tmpRsaSrk.handle, tmpRsaEk.handle, stored.CredentialBlob,
                    stored.Secret);

            return new String(recoveredSecret);
        } catch (TpmException e) {
            throw new SecurityException("TPM exception", e);
        }
    }

    public String deserializeAndDectryptSecret(String encryptedSecret) throws SecurityException {
        try {
            byte[] array = Helpers.fromHex(encryptedSecret);
            ByteArrayInputStream stream = new ByteArrayInputStream(array);
            ObjectInputStream serial = new ObjectInputStream(stream);
            SecuredPassword secret = (SecuredPassword) serial.readObject();
            if (secret == null) {
                throw new SecurityException("Deserialization failed");
            }
            return decryptSecret(secret);
        } catch (IOException | ClassNotFoundException e) {
            throw new SecurityException("Deserialization failed", e);
        }
    }

    /**
     * Fetch random numbers from the hardware random number genrator of the TPM.
     *
     * @param bytesRequested
     * @return array of random numbers
     */
    byte[] getRandom(int bytesRequested) {
        return tpm.GetRandom(bytesRequested);
    }

    /**
     * @return Return model of TPM chip
     * @throws SecurityException
     */
    public String getTpmFirmwareVersion() throws SecurityException {
        try {
            int ret = TpmHelpers.getTpmProperty(tpm, TPM_PT.FIRMWARE_VERSION_1);
            int major = ret >> 16;
            int minor = ret & 0xffff;
            return "" + major + "." + minor;
        } catch (TpmException e) {
            throw new SecurityException("TPM exception", e);
        }
    }

    /**
     * @return Return manufacturer (abbreviation) of TPM chip
     * @throws SecurityException
     */
    public String getTpmManufacturerShort() throws SecurityException {
        try {
            StringBuilder sb = new StringBuilder(4);
            int ret = TpmHelpers.getTpmProperty(tpm, TPM_PT.MANUFACTURER);
            for (int i = 3; i >= 0; i--) {
                sb.append((char) ((ret >> (i * 8)) & 0xff));
            }
            return sb.toString().trim();
        } catch (TpmException e) {
            throw new SecurityException("TPM exception", e);
        }
    }

    /**
     * @return Return model of TPM chip
     * @throws SecurityException
     */
    public String getTpmModel() throws SecurityException {
        try {
            StringBuilder sb = new StringBuilder(24);
            int ret = TpmHelpers.getTpmProperty(tpm, TPM_PT.VENDOR_STRING_1);
            for (int i = 3; i >= 0; i--) {
                sb.append((char) ((ret >> (i * 8)) & 0xff));
            }
            ret = TpmHelpers.getTpmProperty(tpm, TPM_PT.VENDOR_STRING_2);
            for (int i = 3; i >= 0; i--) {
                sb.append((char) ((ret >> (i * 8)) & 0xff));
            }
            ret = TpmHelpers.getTpmProperty(tpm, TPM_PT.VENDOR_STRING_3);
            for (int i = 3; i > 0; i--) {
                sb.append((char) ((ret >> (i * 8)) & 0xff));
            }
            ret = TpmHelpers.getTpmProperty(tpm, TPM_PT.VENDOR_STRING_4);
            for (int i = 3; i > 0; i--) {
                sb.append((char) ((ret >> (i * 8)) & 0xff));
            }
            return sb.toString().trim();
        } catch (TpmException e) {
            throw new SecurityException("TPM exception", e);
        }
    }

    /**
     * @return Return level of TPM TCG standard, typically a number like "1.38"
     * @see getTpmVersion() in case you want to differentiate TPM1.2, TPM2, etc.
     * @throws SecurityException
     */
    public String getTpmTcgLevel() throws SecurityException {
        try {
            int ret = TpmHelpers.getTpmProperty(tpm, TPM_PT.LEVEL);
            return "" + ret;
        } catch (TpmException e) {
            throw new SecurityException("TPM exception", e);
        }
    }

    /**
     * @return Return revision of TPM TCG standard, typically a number like "1.38"
     * @see getTpmVersion() in case you want to differentiate TPM1.2, TPM2, etc.
     * @throws SecurityException
     */
    public String getTpmTcgRevision() throws SecurityException {
        try {
            int ret = TpmHelpers.getTpmProperty(tpm, TPM_PT.REVISION);
            return "" + (ret / 100) + "." + (ret % 100);
        } catch (TpmException e) {
            throw new SecurityException("TPM exception", e);
        }
    }

    /**
     * @return Return supported version of TPM standard is supported, typically
     *         "1.2" or "2.0".
     * @throws SecurityException
     */
    public String getTpmVersion() throws SecurityException {
        try {
            StringBuilder sb = new StringBuilder(4);
            int ret = TpmHelpers.getTpmProperty(tpm, TPM_PT.FAMILY_INDICATOR);
            for (int i = 3; i >= 0; i--) {
                sb.append((char) ((ret >> (i * 8)) & 0xff));
            }
            return sb.toString().trim();
        } catch (TpmException e) {
            throw new SecurityException("TPM exception", e);
        }
    }
}
