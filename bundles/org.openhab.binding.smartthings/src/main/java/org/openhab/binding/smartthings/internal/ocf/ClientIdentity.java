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
package org.openhab.binding.smartthings.internal.ocf;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.OpenHAB;

/**
 * Generates and retains the Samsung service client identity in owner-only userdata storage.
 * The service UUID is a firmware authorization profile, not the UUID of an openHAB installation.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
final class ClientIdentity {
    static final String SERVICE_UUID = "ab0b0ac4-aae9-4958-a04d-8ec36fe1b2f9";
    private static final String SUBJECT = "OU=uuid:" + SERVICE_UUID + ",CN=urn:uuid:" + SERVICE_UUID;
    private static final String ALIAS = "client";
    private static final char[] PASSWORD = new char[0];
    private final PrivateKey privateKey;
    private final X509Certificate certificate;

    private ClientIdentity(PrivateKey privateKey, X509Certificate certificate) {
        this.privateKey = privateKey;
        this.certificate = certificate;
    }

    static ClientIdentity load() throws IOException, GeneralSecurityException {
        return load(Path.of(OpenHAB.getUserDataFolder(), "etc", "smartthings"), Instant.now());
    }

    static synchronized ClientIdentity load(Path directory, Instant now) throws IOException, GeneralSecurityException {
        Files.createDirectories(directory.getParent());
        if (!Files.exists(directory, LinkOption.NOFOLLOW_LINKS)) {
            if (Files.getFileAttributeView(directory.getParent(), PosixFileAttributeView.class) != null) {
                Files.createDirectory(directory,
                        PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------")));
            } else {
                Files.createDirectory(directory);
            }
        }
        if (!Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Client identity storage must be a directory, not a symbolic link");
        }
        protect(directory, true);
        Path lockFile = directory.resolve("client.lock");
        try (var channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                LinkOption.NOFOLLOW_LINKS)) {
            protect(lockFile, false);
            try (var lock = channel.tryLock()) {
                if (lock == null) {
                    throw new IOException("Client identity storage is in use");
                }
                Path file = directory.resolve("client.p12");
                ClientIdentity identity;
                if (Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
                    if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
                        throw new IOException("Client identity must be a regular file");
                    }
                    protect(file, false);
                    KeyStore store = KeyStore.getInstance("PKCS12");
                    try (var input = Files.newInputStream(file, LinkOption.NOFOLLOW_LINKS)) {
                        store.load(input, PASSWORD);
                    }
                    if (store.size() != 1 || !(store.getKey(ALIAS, PASSWORD) instanceof PrivateKey key)
                            || !(store.getCertificate(ALIAS) instanceof X509Certificate certificate)) {
                        throw new GeneralSecurityException("Invalid persisted client identity");
                    }
                    identity = new ClientIdentity(key, certificate);
                    identity.validate(now);
                    if (certificate.getNotAfter().toInstant().isAfter(now.plus(Duration.ofDays(30)))) {
                        return identity;
                    }
                    // Renew the certificate without changing the installation's private key.
                    identity = generate(new KeyPair(certificate.getPublicKey(), key), now);
                } else {
                    var generator = KeyPairGenerator.getInstance("RSA");
                    generator.initialize(2048);
                    identity = generate(generator.generateKeyPair(), now);
                }
                persist(directory, file, identity);
                return identity;
            }
        } catch (OverlappingFileLockException e) {
            throw new IOException("Client identity storage is in use");
        }
    }

    private static void persist(Path directory, Path file, ClientIdentity identity)
            throws IOException, GeneralSecurityException {
        Path temporary = Files.createTempFile(directory, "client-", ".p12");
        try {
            protect(temporary, false);
            KeyStore store = KeyStore.getInstance("PKCS12");
            store.load(null, PASSWORD);
            // Access control, not a built-in password, protects this automatically managed private key.
            store.setKeyEntry(ALIAS, identity.privateKey, PASSWORD, identity.chain());
            try (var output = Files.newOutputStream(temporary)) {
                store.store(output, PASSWORD);
            }
            try (var channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) {
                channel.force(true);
            }
            Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void protect(Path path, boolean directory) throws IOException {
        var posix = Files.getFileAttributeView(path, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        if (posix != null) {
            posix.setPermissions(PosixFilePermissions.fromString(directory ? "rwx------" : "rw-------"));
        } else {
            var acl = Files.getFileAttributeView(path, AclFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
            if (acl == null) {
                throw new IOException("Client identity storage requires owner-only file permissions");
            }
            acl.setAcl(List.of(AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(acl.getOwner())
                    .setPermissions(EnumSet.allOf(AclEntryPermission.class)).build()));
        }
    }

    private static ClientIdentity generate(KeyPair pair, Instant now) throws GeneralSecurityException, IOException {
        var subject = X500Name.getInstance(new X500Principal(SUBJECT).getEncoded());
        var builder = new JcaX509v3CertificateBuilder(subject,
                new BigInteger(159, new SecureRandom()).add(BigInteger.ONE),
                Date.from(now.minus(Duration.ofMinutes(5))), Date.from(now.plus(Duration.ofDays(3650))), subject,
                pair.getPublic());
        builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
        builder.addExtension(Extension.keyUsage, true,
                new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));
        builder.addExtension(Extension.extendedKeyUsage, false, new ExtendedKeyUsage(KeyPurposeId.id_kp_clientAuth));
        builder.addExtension(Extension.subjectAlternativeName, false,
                new GeneralNames(new GeneralName(GeneralName.uniformResourceIdentifier, "urn:uuid:" + SERVICE_UUID)));
        try {
            var certificate = new JcaX509CertificateConverter().getCertificate(
                    builder.build(new JcaContentSignerBuilder("SHA256withRSA").build(pair.getPrivate())));
            return new ClientIdentity(pair.getPrivate(), certificate);
        } catch (OperatorCreationException e) {
            throw new GeneralSecurityException("Cannot generate appliance client identity");
        }
    }

    private void validate(Instant now) throws GeneralSecurityException {
        if (!certificate.getSubjectX500Principal().equals(new X500Principal(SUBJECT))
                || certificate.getNotBefore().toInstant().isAfter(now) || certificate.getBasicConstraints() != -1) {
            throw new GeneralSecurityException("Invalid persisted client certificate");
        }
        certificate.verify(certificate.getPublicKey());
        byte[] challenge = new byte[32];
        new SecureRandom().nextBytes(challenge);
        var signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(challenge);
        byte[] proof = signature.sign();
        signature.initVerify(certificate.getPublicKey());
        signature.update(challenge);
        if (!signature.verify(proof)) {
            throw new GeneralSecurityException("Client identity key does not match its certificate");
        }
    }

    PrivateKey privateKey() {
        return privateKey;
    }

    Certificate[] chain() {
        return new Certificate[] { certificate };
    }
}
