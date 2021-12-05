package org.openhab.binding.blink.internal.service;

import java.io.IOException;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.blink.internal.config.CameraConfiguration;
import org.openhab.binding.blink.internal.dto.BlinkAccount;
import org.openhab.binding.blink.internal.dto.BlinkCommand;
import com.google.gson.Gson;

public class CameraService extends BaseBlinkApiService {
    public CameraService(HttpClient httpClient, Gson gson) {
        super(httpClient, gson);
    }

    public Long motionDetection(@Nullable BlinkAccount account, @Nullable CameraConfiguration camera, boolean enable)
            throws IOException {
        if (account == null || account.account == null || camera == null)
            throw new IllegalArgumentException("Cannot call motion detection api without account or camera");
        String action = (enable) ? "/enable" : "/disable";
        String uri = "/network/" + camera.networkId + "/camera/" + camera.cameraId + action;
        BlinkCommand cmd = apiRequest(account.account.tier, uri, HttpMethod.POST, account.auth.token, null,
                BlinkCommand.class);
        return cmd.id;
    }
}
