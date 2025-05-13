package org.openhab.binding.jellyfin.internal.api.version.legacy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.CountryInfo;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.CultureDto;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.LocalizationOption;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ParentalRating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class LocalizationApi {
    private ApiClient apiClient;

    public LocalizationApi() {
        this(new ApiClient());
    }

    @Autowired
    public LocalizationApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Gets known countries.
     * 
     * <p>
     * <b>200</b> - Known countries returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;CountryInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getCountriesRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<CountryInfo> localVarReturnType = new ParameterizedTypeReference<CountryInfo>() {
        };
        return apiClient.invokeAPI("/Localization/Countries", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets known countries.
     * 
     * <p>
     * <b>200</b> - Known countries returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;CountryInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<CountryInfo> getCountries() throws WebClientResponseException {
        ParameterizedTypeReference<CountryInfo> localVarReturnType = new ParameterizedTypeReference<CountryInfo>() {
        };
        return getCountriesRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Gets known countries.
     * 
     * <p>
     * <b>200</b> - Known countries returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseEntity&lt;List&lt;CountryInfo&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<CountryInfo>>> getCountriesWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<CountryInfo> localVarReturnType = new ParameterizedTypeReference<CountryInfo>() {
        };
        return getCountriesRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Gets known countries.
     * 
     * <p>
     * <b>200</b> - Known countries returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getCountriesWithResponseSpec() throws WebClientResponseException {
        return getCountriesRequestCreation();
    }

    /**
     * Gets known cultures.
     * 
     * <p>
     * <b>200</b> - Known cultures returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;CultureDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getCulturesRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<CultureDto> localVarReturnType = new ParameterizedTypeReference<CultureDto>() {
        };
        return apiClient.invokeAPI("/Localization/Cultures", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets known cultures.
     * 
     * <p>
     * <b>200</b> - Known cultures returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;CultureDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<CultureDto> getCultures() throws WebClientResponseException {
        ParameterizedTypeReference<CultureDto> localVarReturnType = new ParameterizedTypeReference<CultureDto>() {
        };
        return getCulturesRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Gets known cultures.
     * 
     * <p>
     * <b>200</b> - Known cultures returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseEntity&lt;List&lt;CultureDto&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<CultureDto>>> getCulturesWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<CultureDto> localVarReturnType = new ParameterizedTypeReference<CultureDto>() {
        };
        return getCulturesRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Gets known cultures.
     * 
     * <p>
     * <b>200</b> - Known cultures returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getCulturesWithResponseSpec() throws WebClientResponseException {
        return getCulturesRequestCreation();
    }

    /**
     * Gets localization options.
     * 
     * <p>
     * <b>200</b> - Localization options returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;LocalizationOption&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getLocalizationOptionsRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<LocalizationOption> localVarReturnType = new ParameterizedTypeReference<LocalizationOption>() {
        };
        return apiClient.invokeAPI("/Localization/Options", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets localization options.
     * 
     * <p>
     * <b>200</b> - Localization options returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;LocalizationOption&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<LocalizationOption> getLocalizationOptions() throws WebClientResponseException {
        ParameterizedTypeReference<LocalizationOption> localVarReturnType = new ParameterizedTypeReference<LocalizationOption>() {
        };
        return getLocalizationOptionsRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Gets localization options.
     * 
     * <p>
     * <b>200</b> - Localization options returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseEntity&lt;List&lt;LocalizationOption&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<LocalizationOption>>> getLocalizationOptionsWithHttpInfo()
            throws WebClientResponseException {
        ParameterizedTypeReference<LocalizationOption> localVarReturnType = new ParameterizedTypeReference<LocalizationOption>() {
        };
        return getLocalizationOptionsRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Gets localization options.
     * 
     * <p>
     * <b>200</b> - Localization options returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getLocalizationOptionsWithResponseSpec() throws WebClientResponseException {
        return getLocalizationOptionsRequestCreation();
    }

    /**
     * Gets known parental ratings.
     * 
     * <p>
     * <b>200</b> - Known parental ratings returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;ParentalRating&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getParentalRatingsRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<ParentalRating> localVarReturnType = new ParameterizedTypeReference<ParentalRating>() {
        };
        return apiClient.invokeAPI("/Localization/ParentalRatings", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets known parental ratings.
     * 
     * <p>
     * <b>200</b> - Known parental ratings returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;ParentalRating&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<ParentalRating> getParentalRatings() throws WebClientResponseException {
        ParameterizedTypeReference<ParentalRating> localVarReturnType = new ParameterizedTypeReference<ParentalRating>() {
        };
        return getParentalRatingsRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Gets known parental ratings.
     * 
     * <p>
     * <b>200</b> - Known parental ratings returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseEntity&lt;List&lt;ParentalRating&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<ParentalRating>>> getParentalRatingsWithHttpInfo()
            throws WebClientResponseException {
        ParameterizedTypeReference<ParentalRating> localVarReturnType = new ParameterizedTypeReference<ParentalRating>() {
        };
        return getParentalRatingsRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Gets known parental ratings.
     * 
     * <p>
     * <b>200</b> - Known parental ratings returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getParentalRatingsWithResponseSpec() throws WebClientResponseException {
        return getParentalRatingsRequestCreation();
    }
}
