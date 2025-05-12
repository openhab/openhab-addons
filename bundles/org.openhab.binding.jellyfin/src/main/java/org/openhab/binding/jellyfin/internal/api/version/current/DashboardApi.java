package org.openhab.binding.jellyfin.internal.api.version.current;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import org.openhab.binding.jellyfin.internal.api.version.current.model.ConfigurationPageInfo;
import java.io.File;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ProblemDetails;

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
public class DashboardApi {
    private ApiClient apiClient;

    public DashboardApi() {
        this(new ApiClient());
    }

    @Autowired
    public DashboardApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Gets the configuration pages.
     * 
     * <p><b>200</b> - ConfigurationPages returned.
     * <p><b>404</b> - Server still loading.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param enableInMainMenu Whether to enable in the main menu.
     * @return List&lt;ConfigurationPageInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getConfigurationPagesRequestCreation(Boolean enableInMainMenu) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableInMainMenu", enableInMainMenu));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<ConfigurationPageInfo> localVarReturnType = new ParameterizedTypeReference<ConfigurationPageInfo>() {};
        return apiClient.invokeAPI("/web/ConfigurationPages", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets the configuration pages.
     * 
     * <p><b>200</b> - ConfigurationPages returned.
     * <p><b>404</b> - Server still loading.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param enableInMainMenu Whether to enable in the main menu.
     * @return List&lt;ConfigurationPageInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<ConfigurationPageInfo> getConfigurationPages(Boolean enableInMainMenu) throws WebClientResponseException {
        ParameterizedTypeReference<ConfigurationPageInfo> localVarReturnType = new ParameterizedTypeReference<ConfigurationPageInfo>() {};
        return getConfigurationPagesRequestCreation(enableInMainMenu).bodyToFlux(localVarReturnType);
    }

    /**
     * Gets the configuration pages.
     * 
     * <p><b>200</b> - ConfigurationPages returned.
     * <p><b>404</b> - Server still loading.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param enableInMainMenu Whether to enable in the main menu.
     * @return ResponseEntity&lt;List&lt;ConfigurationPageInfo&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<ConfigurationPageInfo>>> getConfigurationPagesWithHttpInfo(Boolean enableInMainMenu) throws WebClientResponseException {
        ParameterizedTypeReference<ConfigurationPageInfo> localVarReturnType = new ParameterizedTypeReference<ConfigurationPageInfo>() {};
        return getConfigurationPagesRequestCreation(enableInMainMenu).toEntityList(localVarReturnType);
    }

    /**
     * Gets the configuration pages.
     * 
     * <p><b>200</b> - ConfigurationPages returned.
     * <p><b>404</b> - Server still loading.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param enableInMainMenu Whether to enable in the main menu.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getConfigurationPagesWithResponseSpec(Boolean enableInMainMenu) throws WebClientResponseException {
        return getConfigurationPagesRequestCreation(enableInMainMenu);
    }

    /**
     * Gets a dashboard configuration page.
     * 
     * <p><b>200</b> - ConfigurationPage returned.
     * <p><b>404</b> - Plugin configuration page not found.
     * @param name The name of the page.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getDashboardConfigurationPageRequestCreation(String name) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "name", name));
        
        final String[] localVarAccepts = { 
            "text/html", "application/x-javascript", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/web/ConfigurationPage", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a dashboard configuration page.
     * 
     * <p><b>200</b> - ConfigurationPage returned.
     * <p><b>404</b> - Plugin configuration page not found.
     * @param name The name of the page.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getDashboardConfigurationPage(String name) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getDashboardConfigurationPageRequestCreation(name).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a dashboard configuration page.
     * 
     * <p><b>200</b> - ConfigurationPage returned.
     * <p><b>404</b> - Plugin configuration page not found.
     * @param name The name of the page.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getDashboardConfigurationPageWithHttpInfo(String name) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getDashboardConfigurationPageRequestCreation(name).toEntity(localVarReturnType);
    }

    /**
     * Gets a dashboard configuration page.
     * 
     * <p><b>200</b> - ConfigurationPage returned.
     * <p><b>404</b> - Plugin configuration page not found.
     * @param name The name of the page.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getDashboardConfigurationPageWithResponseSpec(String name) throws WebClientResponseException {
        return getDashboardConfigurationPageRequestCreation(name);
    }
}
