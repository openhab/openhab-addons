/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.darksky.internal.utils;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.smarthome.config.core.ConfigConstants;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for the {@link ByteArrayFileCache} class.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class ByteArrayFileCacheTest {

    private static final String SERVICE_PID = "org.openhab.binding.darksky";

    private static final File USERDATA_FOLDER = new File(ConfigConstants.getUserDataFolder());
    private static final File CACHE_FOLDER = new File(USERDATA_FOLDER, ByteArrayFileCache.CACHE_FOLDER_NAME);
    private static final File SERVICE_CACHE_FOLDER = new File(CACHE_FOLDER, SERVICE_PID);

    private static final String MP3_FILE_NAME = SERVICE_CACHE_FOLDER.getAbsolutePath() + "doorbell.mp3";
    private static final String TXT_FILE_NAME = SERVICE_CACHE_FOLDER.getAbsolutePath() + "doorbell.txt";

    private static final byte[] EMPTY_BUFFER = new byte[0];

    private ByteArrayFileCache subject;

    @Before
    public void setUp() {
        subject = new ByteArrayFileCache(SERVICE_PID);
    }

    @After
    public void tearDown() {
        // delete all files
        subject.clear();
    }

    @AfterClass
    public static void cleanUp() {
        // delete all folders
        SERVICE_CACHE_FOLDER.delete();
        CACHE_FOLDER.delete();
        USERDATA_FOLDER.delete();
    }

    @Test
    public void testGetFileExtension() {
        assertThat(subject.getFileExtension("/var/log/openhab2/"), is(nullValue()));
        assertThat(subject.getFileExtension("/var/log/foo.bar/"), is(nullValue()));
        assertThat(subject.getFileExtension("doorbell.mp3"), is(equalTo("mp3")));
        assertThat(subject.getFileExtension("/tmp/doorbell.mp3"), is(equalTo("mp3")));
        assertThat(subject.getFileExtension(MP3_FILE_NAME), is(equalTo("mp3")));
        assertThat(subject.getFileExtension(TXT_FILE_NAME), is(equalTo("txt")));
        assertThat(subject.getFileExtension("/var/log/openhab2/.."), is(""));
        assertThat(subject.getFileExtension(".hidden"), is(equalTo("hidden")));
        assertThat(subject.getFileExtension("C:\\Program Files (x86)\\java\\bin\\javaw.exe"), is(equalTo("exe")));
        assertThat(subject.getFileExtension("https://www.youtube.com/watch?v=qYrpPrLY868"), is(nullValue()));
    }

    @Test
    public void testGetUniqueFileName() {
        String mp3UniqueFileName = subject.getUniqueFileName(MP3_FILE_NAME);
        assertThat(mp3UniqueFileName, is(equalTo(subject.getUniqueFileName(MP3_FILE_NAME))));

        String txtUniqueFileName = subject.getUniqueFileName(TXT_FILE_NAME);
        assertThat(txtUniqueFileName, is(equalTo(subject.getUniqueFileName(TXT_FILE_NAME))));

        assertThat(mp3UniqueFileName, is(not(equalTo(txtUniqueFileName))));
    }

    @Test
    public void testGet() {
        assertThat(subject.get(MP3_FILE_NAME), is(equalTo(EMPTY_BUFFER)));
    }

    @Test
    public void testPut() throws IOException {
        byte[] buffer = readFile();
        subject.put(MP3_FILE_NAME, buffer);

        assertThat(subject.get(MP3_FILE_NAME), is(equalTo(buffer)));
    }

    @Test
    public void testPutIfAbsent() throws IOException {
        byte[] buffer = readFile();
        subject.putIfAbsent(MP3_FILE_NAME, buffer);

        assertThat(subject.get(MP3_FILE_NAME), is(equalTo(buffer)));
    }

    @Test
    public void testPutIfAbsentAndGet() throws IOException {
        byte[] buffer = readFile();

        assertThat(subject.putIfAbsentAndGet(MP3_FILE_NAME, buffer), is(equalTo(buffer)));
    }

    @Test
    public void testContainsKey() throws IOException {
        assertThat(subject.containsKey(MP3_FILE_NAME), is(false));

        subject.put(MP3_FILE_NAME, readFile());

        assertThat(subject.containsKey(MP3_FILE_NAME), is(true));
    }

    @Test
    public void testRemove() throws IOException {
        subject.put(MP3_FILE_NAME, readFile());
        subject.remove(MP3_FILE_NAME);

        assertThat(subject.get(MP3_FILE_NAME), is(equalTo(EMPTY_BUFFER)));
    }

    @Test
    public void testClear() throws IOException {
        subject.put(MP3_FILE_NAME, readFile());
        subject.clear();

        assertThat(subject.get(MP3_FILE_NAME), is(equalTo(EMPTY_BUFFER)));
    }

    @Test
    public void clearExpiredClearsNothing() throws IOException {
        byte[] buffer = readFile();
        subject.put(MP3_FILE_NAME, buffer);
        subject.clearExpired();

        assertThat(subject.get(MP3_FILE_NAME), is(equalTo(buffer)));
    }

    @Test
    public void clearExpired() throws IOException {
        subject = new ByteArrayFileCache(SERVICE_PID, 1);

        subject.put(MP3_FILE_NAME, readFile());

        // manipulate time of last use
        File fileInCache = subject.getUniqueFile(MP3_FILE_NAME);
        fileInCache.setLastModified(System.currentTimeMillis() - 2 * ByteArrayFileCache.ONE_DAY_IN_MILLIS);

        subject.clearExpired();

        assertThat(subject.get(MP3_FILE_NAME), is(equalTo(EMPTY_BUFFER)));
    }

    private byte[] readFile() throws IOException {
        byte[] buffer;
        try (InputStream is = ByteArrayFileCacheTest.class.getResourceAsStream("/sounds/doorbell.mp3")) {
            buffer = new byte[is.available()];
            is.read(buffer);
        }
        return buffer;
    }
}
