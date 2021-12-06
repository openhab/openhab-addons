package org.openhab.binding.blink.internal.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.blink.internal.BlinkTestUtil;
import org.openhab.binding.blink.internal.config.CameraConfiguration;
import org.openhab.binding.blink.internal.dto.BlinkAccount;
import org.openhab.binding.blink.internal.dto.BlinkCommand;
import com.google.gson.Gson;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class CameraServiceTest {

    @NonNullByDefault({}) CameraService cameraService;

    @BeforeEach
    void setup() {
        cameraService = spy(new CameraService(new HttpClient(), new Gson()));
    }

    @Test
    void testIllegalArguments() {
        assertThrows(IllegalArgumentException.class, () -> cameraService.motionDetection(null, null, true));
        CameraConfiguration cameraConfiguration = testCameraConfiguration();
        assertThrows(IllegalArgumentException.class,
                () -> cameraService.motionDetection(null, cameraConfiguration, true));
        BlinkAccount blinkAccount = new BlinkAccount();
        assertThrows(IllegalArgumentException.class,
                () -> cameraService.motionDetection(blinkAccount, cameraConfiguration, true));
    }

    @Test
    void testMotionDetectionEnable() throws IOException {
        BlinkAccount blinkAccount = BlinkTestUtil.testBlinkAccount();
        CameraConfiguration cameraConfiguration = testCameraConfiguration();
        BlinkCommand enableResult = new BlinkCommand();
        enableResult.id = 666L;
        String enableUri = "/network/" + cameraConfiguration.networkId + "/camera/" + cameraConfiguration.cameraId + "/enable";
        BlinkCommand disableResult = new BlinkCommand();
        disableResult.id = 777L;
        String disableUri = "/network/" + cameraConfiguration.networkId + "/camera/" + cameraConfiguration.cameraId + "/disable";
        doReturn(enableResult).when(cameraService)
                .apiRequest(blinkAccount.account.tier, enableUri, HttpMethod.POST, blinkAccount.auth.token, null,
                        BlinkCommand.class);
        doReturn(disableResult).when(cameraService)
                .apiRequest(blinkAccount.account.tier, disableUri, HttpMethod.POST, blinkAccount.auth.token, null,
                        BlinkCommand.class);
        assertThat(cameraService.motionDetection(blinkAccount, cameraConfiguration, true), is(enableResult.id));
        assertThat(cameraService.motionDetection(blinkAccount, cameraConfiguration, false), is(disableResult.id));
    }

    @Test
    void testCreateThumbnail() throws IOException {
        BlinkAccount blinkAccount = BlinkTestUtil.testBlinkAccount();
        CameraConfiguration cameraConfiguration = testCameraConfiguration();
        BlinkCommand expected = new BlinkCommand();
        expected.id = 666L;
        String expectedUri = "/network/" + cameraConfiguration.networkId + "/camera/" + cameraConfiguration.cameraId + "/thumbnail";
        doReturn(expected).when(cameraService)
                .apiRequest(blinkAccount.account.tier, expectedUri, HttpMethod.POST, blinkAccount.auth.token, null,
                        BlinkCommand.class);
        assertThat(cameraService.createThumbnail(blinkAccount, cameraConfiguration), is(expected.id));
    }

    @Test
    void testGetThumbnail() throws IOException {
        BlinkAccount blinkAccount = BlinkTestUtil.testBlinkAccount();
        byte[] expectedImage = "iamanimage".getBytes(StandardCharsets.UTF_8);
        String imagePath = "/full/path/to/thumbnail.jpg";
        doReturn(expectedImage).when(cameraService)
                .rawRequest(blinkAccount.account.tier, imagePath, HttpMethod.GET, blinkAccount.auth.token, null);
        assertThat(cameraService.getThumbnail(blinkAccount, imagePath), is(expectedImage));
    }

    private CameraConfiguration testCameraConfiguration() {
        CameraConfiguration config = new CameraConfiguration();
        config.cameraId = 123L;
        config.networkId = 567L;
        return config;
    }

}