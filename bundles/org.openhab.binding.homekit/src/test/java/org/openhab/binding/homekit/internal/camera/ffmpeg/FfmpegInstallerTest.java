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
package org.openhab.binding.homekit.internal.camera.ffmpeg;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Tests for the ffmpeg release asset selection.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
class FfmpegInstallerTest {

    @Test
    void selectsAssetPerPlatform() throws IOException {
        assertEquals("ffmpeg-alpine-x86_64.tar.gz", FfmpegInstaller.assetName("Linux", "amd64"));
        assertEquals("ffmpeg-alpine-aarch64.tar.gz", FfmpegInstaller.assetName("Linux", "aarch64"));
        assertEquals("ffmpeg-darwin-x86_64.tar.gz", FfmpegInstaller.assetName("Mac OS X", "x86_64"));
        assertEquals("ffmpeg-darwin-arm64.tar.gz", FfmpegInstaller.assetName("Mac OS X", "aarch64"));
        assertEquals("ffmpeg-freebsd-x86_64.tar.gz", FfmpegInstaller.assetName("FreeBSD", "amd64"));
        assertEquals("ffmpeg.exe", FfmpegInstaller.assetName("Windows 10", "amd64"));
    }

    @Test
    void rejectsUnsupportedPlatform() {
        assertThrows(IOException.class, () -> FfmpegInstaller.assetName("Linux", "armv7l"));
    }
}
