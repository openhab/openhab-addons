package org.openhab.binding.blink.internal.service;

import java.io.IOException;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.blink.internal.dto.BlinkAccount;
import org.openhab.binding.blink.internal.dto.BlinkCommand;
import com.google.gson.Gson;

public class NetworkService extends BaseBlinkApiService {
    public NetworkService(HttpClient httpClient, Gson gson) {
        super(httpClient, gson);
    }

    public Long arm(BlinkAccount account, String networkId, boolean enable) throws IOException {
        if (account == null || account.account == null || networkId == null)
            throw new IllegalArgumentException("Cannot call network arm api without account or network");
        String action = (enable) ? "/state/arm" : "/state/disarm";
        String uri = "/api/v1/accounts/" + account.account.account_id + "/networks/" + networkId + action;
        BlinkCommand cmd = apiRequest(account.account.tier, uri, HttpMethod.POST, account.auth.token, null,
                BlinkCommand.class);
        return cmd.id;
    }
}
