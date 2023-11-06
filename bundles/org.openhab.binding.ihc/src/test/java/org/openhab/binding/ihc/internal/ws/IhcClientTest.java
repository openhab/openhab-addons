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
package org.openhab.binding.ihc.internal.ws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.openhab.binding.ihc.internal.ws.datatypes.WSFile;
import org.openhab.binding.ihc.internal.ws.datatypes.WSProjectInfo;
import org.openhab.binding.ihc.internal.ws.exeptions.IhcExecption;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcClientTest {

    private IhcClient ihcClient;

    @BeforeEach
    public void setUp() throws IhcExecption, SocketTimeoutException {
        ihcClient = spy(new IhcClient("test1", "test2", "test3"));
        WSProjectInfo projectInfo = new WSProjectInfo();
        projectInfo.setProjectMajorRevision(111);
        projectInfo.setProjectMinorRevision(222);

        doReturn(projectInfo).when(ihcClient).getProjectInfo();
        doReturn(2).when(ihcClient).getProjectNumberOfSegments();
        doReturn(100).when(ihcClient).getProjectSegmentationSize();

        final String segment0 = ResourceFileUtils.getFileContent("Segment0.base64");
        final String segment1 = ResourceFileUtils.getFileContent("Segment1.base64");

        WSFile file0 = new WSFile(segment0.getBytes(), "");
        doReturn(file0).when(ihcClient).getProjectSegment(ArgumentMatchers.eq(0), ArgumentMatchers.eq(111),
                ArgumentMatchers.eq(222));
        WSFile file1 = new WSFile(segment1.getBytes(), "");
        doReturn(file1).when(ihcClient).getProjectSegment(ArgumentMatchers.eq(1), ArgumentMatchers.eq(111),
                ArgumentMatchers.eq(222));
    }

    @Test
    public void loadProjectFileFromControllerTest() throws IhcExecption {
        final String expectedFileContent = ResourceFileUtils.getFileContent("ProjectFileContent.txt");

        final byte[] result = ihcClient.getProjectFileFromController();
        assertEquals(expectedFileContent, new String(result, StandardCharsets.UTF_8));
    }
}
