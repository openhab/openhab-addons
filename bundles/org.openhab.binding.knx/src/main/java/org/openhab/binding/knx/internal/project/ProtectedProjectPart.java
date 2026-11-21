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
package org.openhab.binding.knx.internal.project;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import javax.crypto.Cipher;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.project.InnerArchive.Aes;
import org.openhab.binding.knx.internal.project.InnerArchive.Entry;
import org.openhab.binding.knx.internal.project.KnxProjectException.Reason;

/**
 * A protected project part: a nested {@code P-xxxx.zip} with WinZip AES entries, as ETS 6 writes it (format note
 * §2.1). The archive is held in memory; the password is checked when the part is opened, and each file is
 * decrypted and inflated while it is read.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class ProtectedProjectPart implements ProjectPart {

    private static final Pattern INSTALLATION_FILE = Pattern.compile("(\\d{1,9})\\.xml", Pattern.CASE_INSENSITIVE);

    private final String id;
    private final String archiveName;
    private final byte[] archive;
    private final ReadBudget budget;
    private final @Nullable Entry projectFile;
    private final List<Entry> installationFiles;
    private final Map<Entry, Cipher> ciphers;

    private ProtectedProjectPart(String id, String archiveName, byte[] archive, ReadBudget budget,
            @Nullable Entry projectFile, List<Entry> installationFiles, Map<Entry, Cipher> ciphers) {
        this.id = id;
        this.archiveName = archiveName;
        this.archive = archive;
        this.budget = budget;
        this.projectFile = projectFile;
        this.installationFiles = installationFiles;
        this.ciphers = ciphers;
    }

    /**
     * @param password the project password; not changed or kept
     * @throws KnxProjectException PASSWORD_REQUIRED, WRONG_PASSWORD, UNSUPPORTED_PROTECTION, MALFORMED or
     *             LIMIT_EXCEEDED
     */
    static ProtectedProjectPart open(OuterContainer container, ZipEntry entry, String id, char @Nullable [] password)
            throws IOException, KnxProjectException {
        String archiveName = entry.getName();
        byte[] archive = readArchive(container, entry);
        List<Entry> entries = InnerArchive.entries(archive, archiveName, container.budget());

        Entry projectFile = null;
        record Numbered(int number, Entry entry) {
        }
        List<Numbered> numbered = new ArrayList<>();
        for (Entry inner : entries) {
            String name = inner.name();
            if (name.indexOf('/') >= 0) {
                continue;
            }
            Matcher matcher = INSTALLATION_FILE.matcher(name);
            if ("project.xml".equalsIgnoreCase(name)) {
                if (projectFile != null) {
                    throw new KnxProjectException(Reason.MALFORMED, archiveName + " holds more than one project.xml");
                }
                projectFile = inner;
            } else if (matcher.matches()) {
                numbered.add(new Numbered(Integer.parseInt(matcher.group(1)), inner));
            }
        }
        numbered.sort(Comparator.comparingInt(Numbered::number));
        List<Entry> installationFiles = numbered.stream().map(Numbered::entry).toList();

        List<Entry> encrypted = new ArrayList<>();
        if (projectFile != null && projectFile.aes() != null) {
            encrypted.add(projectFile);
        }
        if (!installationFiles.isEmpty() && installationFiles.get(0).aes() != null) {
            encrypted.add(installationFiles.get(0));
        }
        Map<Entry, Cipher> ciphers = new HashMap<>();
        if (!encrypted.isEmpty()) {
            if (password == null || password.length == 0) {
                throw new KnxProjectException(Reason.PASSWORD_REQUIRED, "the project is protected by a password");
            }
            unlock(archive, archiveName, encrypted, password, ciphers);
        }
        return new ProtectedProjectPart(id, archiveName, archive, container.budget(), projectFile, installationFiles,
                ciphers);
    }

    private static byte[] readArchive(OuterContainer container, ZipEntry entry)
            throws IOException, KnxProjectException {
        int max = container.budget().limits().maxProtectedPartBytes();
        byte[] archive;
        try (InputStream in = container.open(entry)) {
            archive = in.readNBytes(max);
            if (in.read() >= 0) {
                throw new KnxProjectException(Reason.LIMIT_EXCEEDED,
                        "the protected project part is larger than " + max + " bytes");
            }
        } catch (ZipException e) {
            container.budget().throwIfExceeded();
            throw new KnxProjectException(Reason.MALFORMED, "entry " + entry.getName() + " cannot be read");
        } catch (IOException e) {
            container.budget().throwIfExceeded();
            throw e;
        }
        return archive;
    }

    /**
     * Checks the password against every encrypted entry that will be read and keeps a cipher for each. The
     * password is wrong when no entry's verifier matches it. A verifier that matches while another entry's does
     * not, or while an authentication code fails, means damage.
     */
    private static void unlock(byte[] archive, String archiveName, List<Entry> entries, char[] password,
            Map<Entry, Cipher> ciphers) throws KnxProjectException {
        unlock(archive, archiveName, entries, WinZipAes.etsZipPassword(password), ciphers, array -> {
        });
    }

    /**
     * @param zipPassword the derived zip password; overwritten before this returns
     * @param secrets is handed every other array that holds key material, which is overwritten before this
     *            returns; a test looks at them afterwards
     */
    static void unlock(byte[] archive, String archiveName, List<Entry> entries, byte[] zipPassword,
            Map<Entry, Cipher> ciphers, Consumer<byte[]> secrets) throws KnxProjectException {
        boolean anyEntryPasses = false;
        boolean damaged = false;
        try {
            for (Entry entry : entries) {
                Aes aes = entry.aes();
                if (aes == null) {
                    continue;
                }
                int keyLength = aes.keyLength();
                int salt = entry.dataOffset();
                int verifier = salt + aes.saltLength();
                int cipherText = verifier + WinZipAes.VERIFIER_LENGTH;
                int cipherTextLength = entry.compressedSize() - aes.saltLength() - WinZipAes.VERIFIER_LENGTH
                        - WinZipAes.AUTHENTICATION_CODE_LENGTH;
                byte[] keys = WinZipAes.pbkdf2("HmacSHA1", zipPassword,
                        Arrays.copyOfRange(archive, salt, salt + aes.saltLength()), WinZipAes.ZIP_KEY_ITERATIONS,
                        2 * keyLength + WinZipAes.VERIFIER_LENGTH);
                secrets.accept(keys);
                byte[] code = null;
                try {
                    if (keys[2 * keyLength] != archive[verifier] || keys[2 * keyLength + 1] != archive[verifier + 1]) {
                        damaged = true;
                        continue;
                    }
                    code = WinZipAes.authenticationCode(keys, keyLength, keyLength, archive, cipherText,
                            cipherTextLength);
                    secrets.accept(code);
                    if (!MessageDigest.isEqual(code, Arrays.copyOfRange(archive, cipherText + cipherTextLength,
                            cipherText + cipherTextLength + code.length))) {
                        damaged = true;
                        continue;
                    }
                    anyEntryPasses = true;
                    ciphers.put(entry, WinZipAes.blockCipher(keys, 0, keyLength));
                } finally {
                    Arrays.fill(keys, (byte) 0);
                    if (code != null) {
                        Arrays.fill(code, (byte) 0);
                    }
                }
            }
        } finally {
            Arrays.fill(zipPassword, (byte) 0);
        }
        // damage is only told from a wrong password when an entry passes both checks
        if (!anyEntryPasses) {
            ciphers.clear();
            throw new KnxProjectException(Reason.WRONG_PASSWORD, "the password does not open the project");
        }
        if (damaged) {
            ciphers.clear();
            throw new KnxProjectException(Reason.MALFORMED,
                    archiveName + " is damaged: an entry fails its password or authentication check");
        }
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public InputStream openProjectXml() throws IOException, KnxProjectException {
        Entry entry = projectFile;
        if (entry == null) {
            throw new KnxProjectException(Reason.MALFORMED, archiveName + " holds no project.xml");
        }
        return open(entry);
    }

    @Override
    public InputStream openInstallationXml() throws IOException, KnxProjectException {
        if (installationFiles.isEmpty()) {
            throw new KnxProjectException(Reason.MALFORMED, "the project part has no installation file");
        }
        return open(installationFiles.get(0));
    }

    @Override
    public int installationFileCount() {
        return installationFiles.size();
    }

    private InputStream open(Entry entry) {
        Aes aes = entry.aes();
        InputStream data;
        int method;
        boolean checkCrc;
        if (aes == null) {
            data = new ByteArrayInputStream(archive, entry.dataOffset(), entry.compressedSize());
            method = entry.method();
            checkCrc = true;
        } else {
            Cipher cipher = ciphers.get(entry);
            if (cipher == null) {
                throw new IllegalStateException("entry was not unlocked");
            }
            int cipherText = entry.dataOffset() + aes.saltLength() + WinZipAes.VERIFIER_LENGTH;
            data = AesCtrInputStream.winZip(cipher, archive, cipherText, entry.compressedSize() - aes.saltLength()
                    - WinZipAes.VERIFIER_LENGTH - WinZipAes.AUTHENTICATION_CODE_LENGTH);
            method = aes.method();
            checkCrc = aes.version() == 1;
        }
        InputStream plain = method == InnerArchive.DEFLATED ? new RawInflaterStream(data) : data;
        return budget.limit(new CheckedStream(plain, entry, checkCrc), archiveName + "/" + entry.name(),
                entry.compressedSize());
    }

    /** Inflates raw deflate data and gives the inflater one extra byte at the end, as zlib may need it. */
    private static final class RawInflaterStream extends InflaterInputStream {

        private boolean ended;

        RawInflaterStream(InputStream in) {
            super(in, new Inflater(true), 8192);
        }

        @Override
        protected void fill() throws IOException {
            if (ended) {
                throw new EOFException("deflated data is cut short");
            }
            len = in.read(buf, 0, buf.length);
            if (len < 0) {
                buf[0] = 0;
                len = 1;
                ended = true;
            }
            inf.setInput(buf, 0, len);
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                inf.end();
                Arrays.fill(buf, (byte) 0);
            }
        }
    }

    /** Fails at the end of the entry when its size or (for all but AE-2) its CRC is not the stated one. */
    private static final class CheckedStream extends InputStream {

        private final InputStream in;
        private final Entry entry;
        private final boolean checkCrc;
        private final CRC32 crc = new CRC32();
        private long size;

        CheckedStream(InputStream in, Entry entry, boolean checkCrc) {
            this.in = in;
            this.entry = entry;
            this.checkCrc = checkCrc;
        }

        @Override
        public int read() throws IOException {
            int b = in.read();
            if (b < 0) {
                check();
            } else {
                crc.update(b);
                size++;
            }
            return b;
        }

        @Override
        public int read(byte @Nullable [] b, int off, int len) throws IOException {
            int n = in.read(b, off, len);
            if (n < 0) {
                check();
            } else if (n > 0 && b != null) {
                crc.update(b, off, n);
                size += n;
            }
            return n;
        }

        @Override
        public void close() throws IOException {
            in.close();
        }

        private void check() throws IOException {
            if (size != entry.size() || (checkCrc && crc.getValue() != entry.crc())) {
                throw new ZipException("entry " + entry.name() + " fails its size or CRC check");
            }
        }
    }
}
