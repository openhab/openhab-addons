package org.openhab.binding.jellyfin.internal.api.version.current;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import org.openhab.binding.jellyfin.internal.api.version.current.model.BrandingOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class BrandingApi {
    private ApiClient apiClient;

    public BrandingApi() {
        this(new ApiClient());
    }

    @Autowired
    public BrandingApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Gets branding css.
     * 
     * <p><b>200</b> - Branding css returned.
     * <p><b>204</b> - No branding css configured.
     * @return String
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getBrandingCssRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "text/css", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<String> localVarReturnType = new ParameterizedTypeReference<String>() {};
        return apiClient.invokeAPI("/Branding/Css", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets branding css.
     * 
     * <p><b>200</b> - Branding css returned.
     * <p><b>204</b> - No branding css configured.
     * @return String
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<String> getBrandingCss() throws WebClientResponseException {
        ParameterizedTypeReference<String> localVarReturnType = new ParameterizedTypeReference<String>() {};
        return getBrandingCssRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Gets branding css.
     * 
     * <p><b>200</b> - Branding css returned.
     * <p><b>204</b> - No branding css configured.
     * @return ResponseEntity&lt;String&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<String>> getBrandingCssWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<String> localVarReturnType = new ParameterizedTypeReference<String>() {};
        return getBrandingCssRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Gets branding css.
     * 
     * <p><b>200</b> - Branding css returned.
     * <p><b>204</b> - No branding css configured.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getBrandingCssWithResponseSpec() throws WebClientResponseException {
        return getBrandingCssRequestCreation();
    }

    /**
     * Gets branding css.
     * 
     * <p><b>200</b> - Branding css returned.
     * <p><b>204</b> - No branding css configured.
     * @return String
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getBrandingCss2RequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "text/css", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<String> localVarReturnType = new ParameterizedTypeReference<String>() {};
        return apiClient.invokeAPI("/Branding/Css.css", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets branding css.
     * 
     * <p><b>200</b> - Branding css returned.
     * <p><b>204</b> - No branding css configured.
     * @return String
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<String> getBrandingCss2() throws WebClientResponseException {
        ParameterizedTypeReference<String> localVarReturnType = new ParameterizedTypeReference<String>() {};
        return getBrandingCss2RequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Gets branding css.
     * 
     * <p><b>200</b> - Branding css returned.
     * <p><b>204</b> - No branding css configured.
     * @return ResponseEntity&lt;String&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<String>> getBrandingCss2WithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<String> localVarReturnType = new ParameterizedTypeReference<String>() {};
        return getBrandingCss2RequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Gets branding css.
     * 
     * <p><b>200</b> - Branding css returned.
     * <p><b>204</b> - No branding css configured.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getBrandingCss2WithResponseSpec() throws WebClientResponseException {
        return getBrandingCss2RequestCreation();
    }

    /**
     * Gets branding configuration.
     * 
     * <p><b>200</b> - Branding configuration returned.
     * @return BrandingOptions
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getBrandingOptionsRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<BrandingOptions> localVarReturnType = new ParameterizedTypeReference<BrandingOptions>() {};
        return apiClient.invokeAPI("/Branding/Configuration", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets branding configuration.
     * 
     * <p><b>200</b> - Branding configuration returned.
     * @return BrandingOptions
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BrandingOptions> getBrandingOptions() throws WebClientResponseException {
        ParameterizedTypeReference<BrandingOptions> localVarReturnType = new ParameterizedTypeReference<BrandingOptions>() {};
        return getBrandingOptionsRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Gets branding configuration.
     * 
     * <p><b>200</b> - Branding configuration returned.
     * @return ResponseEntity&lt;BrandingOptions&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BrandingOptions>> getBrandingOptionsWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<BrandingOptions> localVarReturnType = new ParameterizedTypeReference<BrandingOptions>() {};
        return getBrandingOptionsRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Gets branding configuration.
     * 
     * <p><b>200</b> - Branding configuration returned.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getBrandingOptionsWithResponseSpec() throws WebClientResponseException {
        return getBrandingOptionsRequestCreation();
    }
}
