/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.internal.ws;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.openhab.binding.ihc.internal.ws.IhcClient;
import org.openhab.binding.ihc.internal.ws.datatypes.WSFile;
import org.openhab.binding.ihc.internal.ws.datatypes.WSProjectInfo;
import org.openhab.binding.ihc.internal.ws.exeptions.IhcExecption;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcClientTest {

    // text is compressed (gz) and base64 coded via http://www.txtwizard.net/compression online tool
    private final String EXPECTED_PROJECT_FILE_CONTENT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam imperdiet augue tortor, sit amet ullamcorper mauris volutpat non. Nullam ultricies, nibh tincidunt egestas consequat, lorem tortor tincidunt erat, in gravida urna ipsum a dolor. Sed massa nulla, lacinia eu nisi sit amet, accumsan pharetra neque. Sed eget dui metus. Suspendisse blandit lacinia arcu vel blandit. Morbi lobortis, dui vel commodo euismod, urna leo dictum orci, in bibendum turpis ligula nec sem. Donec nec enim mauris. Aliquam erat volutpat. Phasellus congue, velit id pharetra sagittis, mauris eros lacinia velit, maximus rhoncus arcu nulla nec erat. Integer accumsan sodales dui, in rutrum augue eleifend id. Curabitur imperdiet massa vel gravida tincidunt. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Maecenas interdum justo at enim consectetur sagittis.\n"
            + "\n"
            + "Sed hendrerit, est eu hendrerit condimentum, mi justo vehicula ipsum, at dignissim tortor mi quis velit. Aliquam in turpis quis mi dapibus efficitur sed vel metus. Donec ac nisl magna. Cras eget nulla in nunc mollis semper quis et ex. Nulla accumsan dui non mi interdum interdum a nec justo. Phasellus at dolor sit amet purus porta tristique eget in diam. Suspendisse laoreet vulputate nulla quis dictum. Sed laoreet elit sed ipsum ornare ornare. Vivamus in blandit enim, et luctus lacus. Aenean id laoreet diam. Pellentesque ipsum diam, fermentum nec ante ac, mattis tristique mi. Curabitur ultricies risus cursus iaculis ultrices. Nullam sed pharetra arcu. Sed sed dui congue, porttitor nisl vitae, viverra risus.\n"
            + "\n"
            + "Proin at lorem non nunc ullamcorper cursus ut id purus. Proin ac enim non ex aliquam auctor non vel lorem. Aliquam aliquam enim eget tellus mollis dictum. Nulla rhoncus placerat ante vitae vestibulum. Donec facilisis justo vitae dolor ultrices semper. Vivamus mollis neque id felis feugiat, at consectetur ipsum posuere. Ut viverra ex ante, nec lobortis metus pellentesque id. Pellentesque cursus augue sit amet mi varius, sed ultricies ligula turpis duis.";

    private final String SEGMENT0 = "H4sIAAAAAAAA/11VW67bOAz97yq4AMN7KNr5KDCdXqDo/DM2k3AgWa4ewV1+DynZyR2gRXxliTov0n+nLJF0Ly3SmkLKVLQSR6kTLWkrslSpLROvumtZdLuRBK0z/VWVcTDuklcVHGm3JlRTxr/pLEItBI5LythGkVvWQo8UWt250pa2mf7xHdhXsy4qZaJNL3equi26tq2S3KRULh3N78YAFhx0v+t1Z7aXutEt80NXppY3Hty4s5vpp6wAUgrTZjejGIOVMknDzUVf+POytFh4o/3OWWrGEQCQXgOwKq1NCVtbwVoru2yrliJ0CYynepbmvDR6SDhezPQ95YuCxwUcFJytkG1YUoxpTQCjBQ9TpxAk0apLBY+UF3WOF73gOqzAHThDQW8tGMKFisSZviZ7tP+yaRzaz/Q5KDSMrtXpxExvdy4SQnOZYeRkaMBA1yf5wjetjnYYKTmVk6Pvt1fvGlEm39O24Nepu9IdS7bbvm0V8uWnwCWtHKSYDE4ut5rNNM+UBNEruALMTF9a5otaIp/R63aafIfxZyZm+gHB6MFZAWbjmmAgwSiuesGKn77Bd+hbaOeMyirIUkzAaGHksngDZIUDzRSKZvd3lgVVCtBWwADY/";
    private final String SEGMENT1 = "1qpiaCq6/3aO4dw86dPlpw7uGTJphaSbbk7V+zYqhEAWoSWOoo+5G53jyxPdsmqhrro2QbY/LtZd/X2PHyGmCMg/ha7Vt479esVDef4AMrUG0nuyeHF2iG4PAzdM7h65ruZqLu1bYFOIaAuImcd7ndgj7yPxn5abAlHwxuCU7LzoYfDyb5m0Xh+mEm0t4z1HZRhMjJY1fx0XEC0YiR97MTAGBV4+Whhb5WrDPgOtLdUb+djo6feBOljI6H9soyfmf7VB1u6rf9Gj5vbk3EODdW8H0zEz7IJWOuzcgf3BmZi0TLcY+zixURXyd13l4KxBdpZP1lyXrhGfW2Cc2win8W6t2X7UYAwW/prKeeYNWZnQ1tvdva2bAYdzW8CV7VYeQYeWtlGgj4k45xfhTC/5QQhYFKfx+auZ+J15g9ArY8Scw8a9HNjMtkxeScegWXIaPdi1TLppZ9xPnb5Sbe99qiMHB6W9vAdQ2iHKT7wXFZng+LFRkBo56y8YpKhBqqMrvN9PYCHkCPozySMe/3DYBSvYn9epd3UPkZcP0yCbvieShOL0696amoKANzk7h/fhd6RGFevmVn/F6KhcJ+VZ6PEY+hNbu4zJ+M7MYYCTC/zH3fy94oECAAA";

    private IhcClient ihcClient;

    @Before
    public void setUp() throws IhcExecption, SocketTimeoutException {
        ihcClient = spy(new IhcClient("test1", "test2", "test3"));
        WSProjectInfo projectInfo = new WSProjectInfo();
        projectInfo.setProjectMajorRevision(111);
        projectInfo.setProjectMinorRevision(222);

        doReturn(projectInfo).when(ihcClient).getProjectInfo();
        doReturn(2).when(ihcClient).getProjectNumberOfSegments();
        doReturn(100).when(ihcClient).getProjectSegmentationSize();

        WSFile file0 = new WSFile(SEGMENT0.getBytes(), "");
        doReturn(file0).when(ihcClient).getProjectSegment(ArgumentMatchers.eq(0), ArgumentMatchers.eq(111),
                ArgumentMatchers.eq(222));
        WSFile file1 = new WSFile(SEGMENT1.getBytes(), "");
        doReturn(file1).when(ihcClient).getProjectSegment(ArgumentMatchers.eq(1), ArgumentMatchers.eq(111),
                ArgumentMatchers.eq(222));
    }

    @Test
    public void loadProjectFileFromControllerTest() throws IhcExecption, UnsupportedEncodingException {
        final byte[] result = ihcClient.loadProjectFileFromControllerAsByteArray();
        assertEquals(EXPECTED_PROJECT_FILE_CONTENT, new String(result, "UTF-8"));
    }
}
