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
package org.openhab.binding.max.internal.message;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.max.internal.exceptions.IncompleteMessageException;
import org.openhab.binding.max.internal.exceptions.IncorrectMultilineIndexException;
import org.openhab.binding.max.internal.exceptions.MessageIsWaitingException;
import org.openhab.binding.max.internal.exceptions.NoMessageAvailableException;
import org.openhab.binding.max.internal.exceptions.UnprocessableMessageException;
import org.openhab.binding.max.internal.exceptions.UnsupportedMessageTypeException;

/**
 * @author Christian Rockrohr <christian@rockrohr.de>
 */
public class MessageProcessorTest {

    private MessageProcessor processor;

    @Before
    public void before() {
        this.processor = new MessageProcessor();
    }

    private void commonMessageTest(String line, Message expectedMessage) throws Exception {
        Assert.assertTrue(this.processor.addReceivedLine(line));
        Assert.assertTrue(this.processor.isMessageAvailable());
        Message message = this.processor.pull();
        Assert.assertNotNull(message);
        Assert.assertEquals(message.getClass().getName(), expectedMessage.getClass().getName());
        Assert.assertEquals(expectedMessage.getPayload(), message.getPayload());
    }

    @Test
    public void testS_Message() throws Exception {
        String rawData = "S:03,0,30";
        SMessage expectedMessage = new SMessage(rawData);
        commonMessageTest(rawData, expectedMessage);
    }

    @Test
    public void testN_Message() throws Exception {
        String rawData = "N:Aw4VzExFUTAwMTUzNDD/";
        NMessage expectedMessage = new NMessage(rawData);
        commonMessageTest(rawData, expectedMessage);
    }

    @Test
    public void testA_Message() throws Exception {
        String rawData = "A:";
        AMessage expectedMessage = new AMessage(rawData);
        commonMessageTest(rawData, expectedMessage);
    }

    @Test
    public void testC_Message() throws Exception {
        String rawData = "C:0ff1bc,EQ/xvAQJEAJMRVEwNzk0MDA3";
        CMessage expectedMessage = new CMessage(rawData);
        commonMessageTest(rawData, expectedMessage);
    }

    @Test
    public void testL_Message() throws Exception {
        String rawData = "L:Bg/xvAkAAA==";
        LMessage expectedMessage = new LMessage(rawData);
        commonMessageTest(rawData, expectedMessage);
    }

    @Test
    public void testH_Message() throws Exception {
        String rawData = "H:KHA0007199,081dd4,0113,00000000,0d524351,10,30,0f0407,1130,03,0000";
        HMessage expectedMessage = new HMessage(rawData);
        commonMessageTest(rawData, expectedMessage);
    }

    @Test
    public void testSingleM_Message() throws Exception {
        String rawData = "M:00,01,VgIBAQpXb2huemltbWVyAAAAAQMQV6lMRVEwOTgyMTU2DldhbmR0aGVybW9zdGF0AQE=";
        MMessage expectedMessage = new MMessage(rawData);
        commonMessageTest(rawData, expectedMessage);
    }

    @Test
    public void testMultilineM_Message() throws Exception {
        String line1_part1 = "M:00,02,";
        String line1_part2 = "VgIMAQpXb2huemltbWVyCvMrAgtUb2lsZXR0ZSBFRwrenQMOVG9pbGV0dGUgMS4gT0cK3rgECkJhZGV6aW1tZXIK3qoFDFNjaGxhZnppbW1lcgresQYDSmFuD4lCBwlDaHJpc3RpbmEPiTYIBEZsdXIPiT0KEEJhZGV6aW1tZXIgMi4gT0cPiRwLBULDvHJvD4k/DAxHw6RzdGV6aW1tZXIPiRoJC1dhc2Noa8O8Y2hlD4lXNgQHOCtLRVEwMTg4NjczCFRlcnJhc3NlAQQHMblLRVEwMTg3MTkwCEZsdXJ0w7xyAQIK8ytLRVEwMzc5NTg3C1dhbmRoZWl6dW5nAQIK9P9LRVEwMzgwMDU1DkZlbnN0ZXJoZWl6dW5nAQQHMbtLRVEwMTg3MTg4CEZsdXJ0w7xyAgQHMuxLRVEwMTg2ODg0B0ZlbnN0ZXICAQrenUtFUTA0MDY5NjIHSGVpenVuZwIBCt64S0VRMDQwNjk4OQdIZWl6dW5nAwQIFGdLRVEwMTkwNTc3B0ZlbnN0ZXIDBAc2l0tFUTAxODU5NDUIRmx1cnTDvHIEAQreqktFUTA0MDY5NzUHSGVpenVuZwQBCt8JS0VRMDQwNzA3MA5IYW5kdHVjaGVpenVuZwQEBzhTS0VRMDE4ODcxMAdGZW5zdGVyBAQIFIxLRVEwMTkwNTQzFkZlbnN0ZXIgU3RyYcOfZSByZWNodHMFAQresUtFUTA0MDY5ODIHSGVpenVuZwUEBzHmS0VRMDE4NzE0NhVGZW5zdGVyIFN0cmHDn2UgbGlua3MFAxBXqUxFUTA5ODIxNTYOV2FuZHRoZXJtb3N0YXQBBA/u1ExFUTA3OTQ3NTIIRmx1cnTDvHIGBA/v6kxFUTA3OTQ0NzQNRmVuc3RlciBsaW5rcwYED/HnTEVRMDc5Mzk2NA5GZW5zdGVyIHJlY2h0cwYBD4lCTEVRMTAwNDYwMAdIZWl6dW5nBgQP9BVMRVEwNzkzNDA2CEZsdXJ0w7xyBwQP79FMRVEwNzk0NDk5B0ZlbnN0ZXIHAQ+JNkxFUTEwMDQ1ODgHSGVpenVuZwcBD4k9TEVRMTAwNDU5NQ1IZWl6dW5nIHVudGVuCAEPiRxMRVExMDA0NTYyB0hlaXp1bmcKBA/yTUxFUTA3OTM4NjIHRmVuc3RlcgoED/F+TEVRMDc5NDA2OQhGbHVydMO8cgoBD4k/TEVRMTAwNDU5NwdIZWl6dW5nCwQP8YdMRVEwNzk0MDYwB0ZlbnN0ZXILBA/xSExFUTA3OTQxMjQIRmx1cnTDvHILBA/yVkxFUTA3OTM4NTMURmVuc3RlciBHYXJ0ZW4gbGlua3MMBA/yI0xFUTA3OTM5MDQVRmVuc3RlciBHYXJ0ZW4gcmVjaHRzDAEPiRpMRVExMDA0NTYwB0hlaXp1bmcMBA/vj0xFUTA3OTQ1NjUPRmVuc3RlciBTdHJhw59lDAQP8CtMRVEwNzk0NDA5BFTDvHIDBAgUa0tFUTAxODcwNjkNRmVuc3RlciBTZWl0ZQUEBzagS0VRMDE4NTkzNhVGZW5zdGVyIFN0cmHDn2UgbGlua3MBBA/wI0xFUTA3OTQ0MTYORmVuc3RlciBLw7xjaGUBAxBV50xFUTA5ODI2NzYOV2FuZHRoZXJtb3N0YXQFAxBW2kxFUTA5ODIzNjgOV2FuZHRoZXJtb3N0YXQEAxBV4kxFUTA5ODI2NzEOV2FuZHRoZXJtb3N0YXQHAxBZWExFUTA5ODE3MjkOV2FuZHRoZXJtb3N0YXQMAxBV6ExFUTA5ODI2NzcOV2FuZHRoZXJtb3N0YXQGAxBV40xFUTA5ODI2NzIOV2FuZHRoZXJtb3N0YXQKBAcxoEtFUTAxODcyMTYLV2FzY2hrw7xjaGUF";
        String line1 = line1_part1 + line1_part2;

        String line2_part1 = "M:01,02,";
        String line2_part2 = "AxBV8ExFUTA5ODI2ODUOV2FuZHRoZXJtb3N0YXQJBA/v50xFUTA3OTQ0NzcNQmFsa29uZmVuc3RlcgkBD4lXTEVRMTAwNDYyMRZIZWl6dW5nIHVudGVybSBGZW5zdGVyCQQP8llMRVEwNzkzODUwDkZlbnN0ZXIgcmVjaHRzCQQP8bxMRVEwNzk0MDA3DUZlbnN0ZXIgbGlua3MJAQ+JOExFUTEwMDQ1OTAOSGVpenVuZyBCYWxrb24JBA/yLExFUTA3OTM4OTUKQmFsa29udMO8cgkED++zTEVRMDc5NDUyOQhGbHVydMO8cgkB";
        String line2 = line2_part1 + line2_part2;

        String expectedString = line1 + line2_part2;
        MMessage expectedMessage = new MMessage(expectedString);

        Assert.assertFalse(this.processor.addReceivedLine(line1));
        Assert.assertTrue(this.processor.addReceivedLine(line2));
        Message message = this.processor.pull();
        Assert.assertNotNull(message);
        Assert.assertEquals(message.getClass().getName(), MMessage.class.getName());
        Assert.assertEquals(expectedMessage.getPayload(), message.getPayload());
    }

    @Test
    public void testWrongIndexOfMultilineM_Message() throws Exception {
        String line1 = "M:00,02,VgIMAQpXb2huemltbWVyCvMrAgtUb2lsZXR0ZSBFRwrenQMOVG9pbGV0dGUgMS4gT0cK3rgECkJhZGV6aW1tZXIK3qoFDFNjaGxhZnppbW1lcgresQYDSmFuD4lCBwlDaHJpc3RpbmEPiTYIBEZsdXIPiT0KEEJhZGV6aW1tZXIgMi4gT0cPiRwLBULDvHJvD4k/DAxHw6RzdGV6aW1tZXIPiRoJC1dhc2Noa8O8Y2hlD4lXNgQHOCtLRVEwMTg4NjczCFRlcnJhc3NlAQQHMblLRVEwMTg3MTkwCEZsdXJ0w7xyAQIK8ytLRVEwMzc5NTg3C1dhbmRoZWl6dW5nAQIK9P9LRVEwMzgwMDU1DkZlbnN0ZXJoZWl6dW5nAQQHMbtLRVEwMTg3MTg4CEZsdXJ0w7xyAgQHMuxLRVEwMTg2ODg0B0ZlbnN0ZXICAQrenUtFUTA0MDY5NjIHSGVpenVuZwIBCt64S0VRMDQwNjk4OQdIZWl6dW5nAwQIFGdLRVEwMTkwNTc3B0ZlbnN0ZXIDBAc2l0tFUTAxODU5NDUIRmx1cnTDvHIEAQreqktFUTA0MDY5NzUHSGVpenVuZwQBCt8JS0VRMDQwNzA3MA5IYW5kdHVjaGVpenVuZwQEBzhTS0VRMDE4ODcxMAdGZW5zdGVyBAQIFIxLRVEwMTkwNTQzFkZlbnN0ZXIgU3RyYcOfZSByZWNodHMFAQresUtFUTA0MDY5ODIHSGVpenVuZwUEBzHmS0VRMDE4NzE0NhVGZW5zdGVyIFN0cmHDn2UgbGlua3MFAxBXqUxFUTA5ODIxNTYOV2FuZHRoZXJtb3N0YXQBBA/u1ExFUTA3OTQ3NTIIRmx1cnTDvHIGBA/v6kxFUTA3OTQ0NzQNRmVuc3RlciBsaW5rcwYED/HnTEVRMDc5Mzk2NA5GZW5zdGVyIHJlY2h0cwYBD4lCTEVRMTAwNDYwMAdIZWl6dW5nBgQP9BVMRVEwNzkzNDA2CEZsdXJ0w7xyBwQP79FMRVEwNzk0NDk5B0ZlbnN0ZXIHAQ+JNkxFUTEwMDQ1ODgHSGVpenVuZwcBD4k9TEVRMTAwNDU5NQ1IZWl6dW5nIHVudGVuCAEPiRxMRVExMDA0NTYyB0hlaXp1bmcKBA/yTUxFUTA3OTM4NjIHRmVuc3RlcgoED/F+TEVRMDc5NDA2OQhGbHVydMO8cgoBD4k/TEVRMTAwNDU5NwdIZWl6dW5nCwQP8YdMRVEwNzk0MDYwB0ZlbnN0ZXILBA/xSExFUTA3OTQxMjQIRmx1cnTDvHILBA/yVkxFUTA3OTM4NTMURmVuc3RlciBHYXJ0ZW4gbGlua3MMBA/yI0xFUTA3OTM5MDQVRmVuc3RlciBHYXJ0ZW4gcmVjaHRzDAEPiRpMRVExMDA0NTYwB0hlaXp1bmcMBA/vj0xFUTA3OTQ1NjUPRmVuc3RlciBTdHJhw59lDAQP8CtMRVEwNzk0NDA5BFTDvHIDBAgUa0tFUTAxODcwNjkNRmVuc3RlciBTZWl0ZQUEBzagS0VRMDE4NTkzNhVGZW5zdGVyIFN0cmHDn2UgbGlua3MBBA/wI0xFUTA3OTQ0MTYORmVuc3RlciBLw7xjaGUBAxBV50xFUTA5ODI2NzYOV2FuZHRoZXJtb3N0YXQFAxBW2kxFUTA5ODIzNjgOV2FuZHRoZXJtb3N0YXQEAxBV4kxFUTA5ODI2NzEOV2FuZHRoZXJtb3N0YXQHAxBZWExFUTA5ODE3MjkOV2FuZHRoZXJtb3N0YXQMAxBV6ExFUTA5ODI2NzcOV2FuZHRoZXJtb3N0YXQGAxBV40xFUTA5ODI2NzIOV2FuZHRoZXJtb3N0YXQKBAcxoEtFUTAxODcyMTYLV2FzY2hrw7xjaGUF";
        String line2 = "M:02,02,AxBV8ExFUTA5ODI2ODUOV2FuZHRoZXJtb3N0YXQJBA/v50xFUTA3OTQ0NzcNQmFsa29uZmVuc3RlcgkBD4lXTEVRMTAwNDYyMRZIZWl6dW5nIHVudGVybSBGZW5zdGVyCQQP8llMRVEwNzkzODUwDkZlbnN0ZXIgcmVjaHRzCQQP8bxMRVEwNzk0MDA3DUZlbnN0ZXIgbGlua3MJAQ+JOExFUTEwMDQ1OTAOSGVpenVuZyBCYWxrb24JBA/yLExFUTA3OTM4OTUKQmFsa29udMO8cgkED++zTEVRMDc5NDUyOQhGbHVydMO8cgkB";

        try {
            this.processor.addReceivedLine(line2);
            Assert.fail("Expected exception was not thrown.");
        } catch (IncorrectMultilineIndexException e) {
            // OK, correct Exception was thrown
        }

        try {
            this.processor.reset();
            Assert.assertFalse(this.processor.addReceivedLine(line1));
            this.processor.addReceivedLine(line2);
            Assert.fail("Expected exception was not thrown.");
        } catch (IncorrectMultilineIndexException e) {
            // OK, correct Exception was thrown
        }
    }

    @Test
    public void testPullBeforeNewLine() throws Exception {
        String line1 = "H:KHA0007199,081dd4,0113,00000000,0d524351,10,30,0f0407,1130,03,0000";
        String line2 = "M:00,01,VgIBAQpXb2huemltbWVyAAAAAQMQV6lMRVEwOTgyMTU2DldhbmR0aGVybW9zdGF0AQE=";

        try {
            Assert.assertTrue(this.processor.addReceivedLine(line1));
            this.processor.addReceivedLine(line2);
            Assert.fail("Expected exception was not thrown.");
        } catch (MessageIsWaitingException e) {
            // OK, correct Exception was thrown
        }

        Message message = this.processor.pull();
        Assert.assertNotNull(message);
        Assert.assertEquals(message.getClass().getName(), HMessage.class.getName());
        this.processor.addReceivedLine(line2);
        message = this.processor.pull();
        Assert.assertNotNull(message);
        Assert.assertEquals(message.getClass().getName(), MMessage.class.getName());
    }

    @Test
    public void testWrongIndicatorWhileMultilineProcessing() throws Exception {
        String line1 = "M:00,02,VgIMAQpXb2huemltbWVyCvMrAgtUb2lsZXR0ZSBFRwrenQMOVG9pbGV0dGUgMS4gT0cK3rgECkJhZGV6aW1tZXIK3qoFDFNjaGxhZnppbW1lcgresQYDSmFuD4lCBwlDaHJpc3RpbmEPiTYIBEZsdXIPiT0KEEJhZGV6aW1tZXIgMi4gT0cPiRwLBULDvHJvD4k/DAxHw6RzdGV6aW1tZXIPiRoJC1dhc2Noa8O8Y2hlD4lXNgQHOCtLRVEwMTg4NjczCFRlcnJhc3NlAQQHMblLRVEwMTg3MTkwCEZsdXJ0w7xyAQIK8ytLRVEwMzc5NTg3C1dhbmRoZWl6dW5nAQIK9P9LRVEwMzgwMDU1DkZlbnN0ZXJoZWl6dW5nAQQHMbtLRVEwMTg3MTg4CEZsdXJ0w7xyAgQHMuxLRVEwMTg2ODg0B0ZlbnN0ZXICAQrenUtFUTA0MDY5NjIHSGVpenVuZwIBCt64S0VRMDQwNjk4OQdIZWl6dW5nAwQIFGdLRVEwMTkwNTc3B0ZlbnN0ZXIDBAc2l0tFUTAxODU5NDUIRmx1cnTDvHIEAQreqktFUTA0MDY5NzUHSGVpenVuZwQBCt8JS0VRMDQwNzA3MA5IYW5kdHVjaGVpenVuZwQEBzhTS0VRMDE4ODcxMAdGZW5zdGVyBAQIFIxLRVEwMTkwNTQzFkZlbnN0ZXIgU3RyYcOfZSByZWNodHMFAQresUtFUTA0MDY5ODIHSGVpenVuZwUEBzHmS0VRMDE4NzE0NhVGZW5zdGVyIFN0cmHDn2UgbGlua3MFAxBXqUxFUTA5ODIxNTYOV2FuZHRoZXJtb3N0YXQBBA/u1ExFUTA3OTQ3NTIIRmx1cnTDvHIGBA/v6kxFUTA3OTQ0NzQNRmVuc3RlciBsaW5rcwYED/HnTEVRMDc5Mzk2NA5GZW5zdGVyIHJlY2h0cwYBD4lCTEVRMTAwNDYwMAdIZWl6dW5nBgQP9BVMRVEwNzkzNDA2CEZsdXJ0w7xyBwQP79FMRVEwNzk0NDk5B0ZlbnN0ZXIHAQ+JNkxFUTEwMDQ1ODgHSGVpenVuZwcBD4k9TEVRMTAwNDU5NQ1IZWl6dW5nIHVudGVuCAEPiRxMRVExMDA0NTYyB0hlaXp1bmcKBA/yTUxFUTA3OTM4NjIHRmVuc3RlcgoED/F+TEVRMDc5NDA2OQhGbHVydMO8cgoBD4k/TEVRMTAwNDU5NwdIZWl6dW5nCwQP8YdMRVEwNzk0MDYwB0ZlbnN0ZXILBA/xSExFUTA3OTQxMjQIRmx1cnTDvHILBA/yVkxFUTA3OTM4NTMURmVuc3RlciBHYXJ0ZW4gbGlua3MMBA/yI0xFUTA3OTM5MDQVRmVuc3RlciBHYXJ0ZW4gcmVjaHRzDAEPiRpMRVExMDA0NTYwB0hlaXp1bmcMBA/vj0xFUTA3OTQ1NjUPRmVuc3RlciBTdHJhw59lDAQP8CtMRVEwNzk0NDA5BFTDvHIDBAgUa0tFUTAxODcwNjkNRmVuc3RlciBTZWl0ZQUEBzagS0VRMDE4NTkzNhVGZW5zdGVyIFN0cmHDn2UgbGlua3MBBA/wI0xFUTA3OTQ0MTYORmVuc3RlciBLw7xjaGUBAxBV50xFUTA5ODI2NzYOV2FuZHRoZXJtb3N0YXQFAxBW2kxFUTA5ODIzNjgOV2FuZHRoZXJtb3N0YXQEAxBV4kxFUTA5ODI2NzEOV2FuZHRoZXJtb3N0YXQHAxBZWExFUTA5ODE3MjkOV2FuZHRoZXJtb3N0YXQMAxBV6ExFUTA5ODI2NzcOV2FuZHRoZXJtb3N0YXQGAxBV40xFUTA5ODI2NzIOV2FuZHRoZXJtb3N0YXQKBAcxoEtFUTAxODcyMTYLV2FzY2hrw7xjaGUF";
        String line2 = "H:KHA0007199,081dd4,0113,00000000,0d524351,10,30,0f0407,1130,03,0000";

        try {
            Assert.assertFalse(this.processor.addReceivedLine(line1));
            this.processor.addReceivedLine(line2);
            Assert.fail("Expected exception was not thrown.");
        } catch (IncompleteMessageException e) {
            // OK, correct Exception was thrown
        }
    }

    @Test
    public void testUnknownMessageIndicator() throws Exception {
        String rawData = "X:0ff1bc,EQ/xvAQJEAJMRVEwNzk0MDA3";

        try {
            Assert.assertFalse(this.processor.addReceivedLine(rawData));
            Assert.fail("Expected exception was not thrown.");
        } catch (UnsupportedMessageTypeException e) {
            // OK, correct Exception was thrown
        }
    }

    @Test
    public void testNoMessageAvailable() throws Exception {
        String rawData = "M:00,01,VgIBAQpXb2huemltbWVyAAAAAQMQV6lMRVEwOTgyMTU2DldhbmR0aGVybW9zdGF0AQE=";
        MMessage expectedMessage = new MMessage(rawData);
        commonMessageTest(rawData, expectedMessage);

        try {
            this.processor.pull();
            Assert.fail("Expected exception was not thrown.");
        } catch (NoMessageAvailableException e) {
            // OK, correct Exception was thrown
        }
    }

    @Test
    public void testUnprocessableMessage() throws Exception {
        String line1 = "M:00"; // Some information are missing.

        try {
            this.processor.addReceivedLine(line1);
            Assert.fail("Expected exception was not thrown.");
        } catch (UnprocessableMessageException e) {
            // OK, correct Exception was thrown
        }
    }

}
