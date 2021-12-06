package org.openhab.binding.boschspexor.internal.api.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.MimeTypes;
import org.openhab.binding.boschspexor.internal.BoschSpexorBindingConstants;
import org.openhab.binding.boschspexor.internal.api.model.Spexor;
import org.openhab.binding.boschspexor.internal.api.model.SpexorInfo;
import org.openhab.binding.boschspexor.internal.api.service.auth.SpexorAuthorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SpexorAPIService {
    private final Logger logger = LoggerFactory.getLogger(SpexorAPIService.class);

    private final SpexorAuthorizationService authService;
    private ObjectMapper mapper = new ObjectMapper();

    public SpexorAPIService(@NonNull SpexorAuthorizationService authService) {
        this.authService = authService;
    }

    public List<Spexor> getSpexors() {
        Request request = authService.newRequest(BoschSpexorBindingConstants.ENDPOINT_SPEXORS);
        try {
            return send(request, new TypeReference<List<Spexor>>() {
            });
        } catch (IOException | InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("failed to get '{}' : {}", request.getURI(), e);
            return Collections.emptyList();
        }
    }

    public SpexorInfo getSpexor(String id) {
        Request request = authService.newRequest(BoschSpexorBindingConstants.ENDPOINT_SPEXOR, id);
        try {
            return send(request, new TypeReference<SpexorInfo>() {
            });
        } catch (IOException | InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("failed to get '{}' : {}", request.getURI(), e);
            return null;
        }
    }

    private <T> T send(Request request, TypeReference<T> clazzOfJson) throws JsonParseException, JsonMappingException,
            IOException, InterruptedException, TimeoutException, ExecutionException {
        request.accept(MimeTypes.Type.APPLICATION_JSON.asString());
        ContentResponse response = request.send();
        return mapper.readValue(response.getContent(), clazzOfJson);
    }
}
