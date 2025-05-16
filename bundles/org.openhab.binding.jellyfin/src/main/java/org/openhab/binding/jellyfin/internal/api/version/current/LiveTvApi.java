package org.openhab.binding.jellyfin.internal.api.version.current;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.current.model.BaseItemDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ChannelMappingOptionsDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ChannelType;
import org.openhab.binding.jellyfin.internal.api.version.current.model.GetProgramsDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.GuideInfo;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ImageType;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ItemFields;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ItemSortBy;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ListingsProviderInfo;
import org.openhab.binding.jellyfin.internal.api.version.current.model.LiveTvInfo;
import org.openhab.binding.jellyfin.internal.api.version.current.model.NameIdPair;
import org.openhab.binding.jellyfin.internal.api.version.current.model.RecordingStatus;
import org.openhab.binding.jellyfin.internal.api.version.current.model.SeriesTimerInfoDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.SeriesTimerInfoDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.version.current.model.SetChannelMappingDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.SortOrder;
import org.openhab.binding.jellyfin.internal.api.version.current.model.TimerInfoDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.TimerInfoDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.version.current.model.TunerChannelMapping;
import org.openhab.binding.jellyfin.internal.api.version.current.model.TunerHostInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class LiveTvApi {
    private ApiClient apiClient;

    public LiveTvApi() {
        this(new ApiClient());
    }

    @Autowired
    public LiveTvApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Adds a listings provider.
     * 
     * <p>
     * <b>200</b> - Created listings provider returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param pw Password.
     * @param validateListings Validate listings.
     * @param validateLogin Validate login.
     * @param listingsProviderInfo New listings info.
     * @return ListingsProviderInfo
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec addListingProviderRequestCreation(String pw, Boolean validateListings, Boolean validateLogin,
            ListingsProviderInfo listingsProviderInfo) throws WebClientResponseException {
        Object postBody = listingsProviderInfo;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "pw", pw));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "validateListings", validateListings));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "validateLogin", validateLogin));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<ListingsProviderInfo> localVarReturnType = new ParameterizedTypeReference<ListingsProviderInfo>() {
        };
        return apiClient.invokeAPI("/LiveTv/ListingProviders", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Adds a listings provider.
     * 
     * <p>
     * <b>200</b> - Created listings provider returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param pw Password.
     * @param validateListings Validate listings.
     * @param validateLogin Validate login.
     * @param listingsProviderInfo New listings info.
     * @return ListingsProviderInfo
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ListingsProviderInfo> addListingProvider(String pw, Boolean validateListings, Boolean validateLogin,
            ListingsProviderInfo listingsProviderInfo) throws WebClientResponseException {
        ParameterizedTypeReference<ListingsProviderInfo> localVarReturnType = new ParameterizedTypeReference<ListingsProviderInfo>() {
        };
        return addListingProviderRequestCreation(pw, validateListings, validateLogin, listingsProviderInfo)
                .bodyToMono(localVarReturnType);
    }

    /**
     * Adds a listings provider.
     * 
     * <p>
     * <b>200</b> - Created listings provider returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param pw Password.
     * @param validateListings Validate listings.
     * @param validateLogin Validate login.
     * @param listingsProviderInfo New listings info.
     * @return ResponseEntity&lt;ListingsProviderInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<ListingsProviderInfo>> addListingProviderWithHttpInfo(String pw,
            Boolean validateListings, Boolean validateLogin, ListingsProviderInfo listingsProviderInfo)
            throws WebClientResponseException {
        ParameterizedTypeReference<ListingsProviderInfo> localVarReturnType = new ParameterizedTypeReference<ListingsProviderInfo>() {
        };
        return addListingProviderRequestCreation(pw, validateListings, validateLogin, listingsProviderInfo)
                .toEntity(localVarReturnType);
    }

    /**
     * Adds a listings provider.
     * 
     * <p>
     * <b>200</b> - Created listings provider returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param pw Password.
     * @param validateListings Validate listings.
     * @param validateLogin Validate login.
     * @param listingsProviderInfo New listings info.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec addListingProviderWithResponseSpec(String pw, Boolean validateListings, Boolean validateLogin,
            ListingsProviderInfo listingsProviderInfo) throws WebClientResponseException {
        return addListingProviderRequestCreation(pw, validateListings, validateLogin, listingsProviderInfo);
    }

    /**
     * Adds a tuner host.
     * 
     * <p>
     * <b>200</b> - Created tuner host returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param tunerHostInfo New tuner host.
     * @return TunerHostInfo
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec addTunerHostRequestCreation(TunerHostInfo tunerHostInfo) throws WebClientResponseException {
        Object postBody = tunerHostInfo;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<TunerHostInfo> localVarReturnType = new ParameterizedTypeReference<TunerHostInfo>() {
        };
        return apiClient.invokeAPI("/LiveTv/TunerHosts", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Adds a tuner host.
     * 
     * <p>
     * <b>200</b> - Created tuner host returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param tunerHostInfo New tuner host.
     * @return TunerHostInfo
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<TunerHostInfo> addTunerHost(TunerHostInfo tunerHostInfo) throws WebClientResponseException {
        ParameterizedTypeReference<TunerHostInfo> localVarReturnType = new ParameterizedTypeReference<TunerHostInfo>() {
        };
        return addTunerHostRequestCreation(tunerHostInfo).bodyToMono(localVarReturnType);
    }

    /**
     * Adds a tuner host.
     * 
     * <p>
     * <b>200</b> - Created tuner host returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param tunerHostInfo New tuner host.
     * @return ResponseEntity&lt;TunerHostInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<TunerHostInfo>> addTunerHostWithHttpInfo(TunerHostInfo tunerHostInfo)
            throws WebClientResponseException {
        ParameterizedTypeReference<TunerHostInfo> localVarReturnType = new ParameterizedTypeReference<TunerHostInfo>() {
        };
        return addTunerHostRequestCreation(tunerHostInfo).toEntity(localVarReturnType);
    }

    /**
     * Adds a tuner host.
     * 
     * <p>
     * <b>200</b> - Created tuner host returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param tunerHostInfo New tuner host.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec addTunerHostWithResponseSpec(TunerHostInfo tunerHostInfo) throws WebClientResponseException {
        return addTunerHostRequestCreation(tunerHostInfo);
    }

    /**
     * Cancels a live tv series timer.
     * 
     * <p>
     * <b>204</b> - Timer cancelled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerId Timer id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec cancelSeriesTimerRequestCreation(String timerId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'timerId' is set
        if (timerId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'timerId' when calling cancelSeriesTimer",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("timerId", timerId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/LiveTv/SeriesTimers/{timerId}", HttpMethod.DELETE, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Cancels a live tv series timer.
     * 
     * <p>
     * <b>204</b> - Timer cancelled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerId Timer id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> cancelSeriesTimer(String timerId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return cancelSeriesTimerRequestCreation(timerId).bodyToMono(localVarReturnType);
    }

    /**
     * Cancels a live tv series timer.
     * 
     * <p>
     * <b>204</b> - Timer cancelled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerId Timer id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> cancelSeriesTimerWithHttpInfo(String timerId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return cancelSeriesTimerRequestCreation(timerId).toEntity(localVarReturnType);
    }

    /**
     * Cancels a live tv series timer.
     * 
     * <p>
     * <b>204</b> - Timer cancelled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerId Timer id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec cancelSeriesTimerWithResponseSpec(String timerId) throws WebClientResponseException {
        return cancelSeriesTimerRequestCreation(timerId);
    }

    /**
     * Cancels a live tv timer.
     * 
     * <p>
     * <b>204</b> - Timer deleted.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerId Timer id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec cancelTimerRequestCreation(String timerId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'timerId' is set
        if (timerId == null) {
            throw new WebClientResponseException("Missing the required parameter 'timerId' when calling cancelTimer",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("timerId", timerId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/LiveTv/Timers/{timerId}", HttpMethod.DELETE, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Cancels a live tv timer.
     * 
     * <p>
     * <b>204</b> - Timer deleted.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerId Timer id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> cancelTimer(String timerId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return cancelTimerRequestCreation(timerId).bodyToMono(localVarReturnType);
    }

    /**
     * Cancels a live tv timer.
     * 
     * <p>
     * <b>204</b> - Timer deleted.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerId Timer id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> cancelTimerWithHttpInfo(String timerId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return cancelTimerRequestCreation(timerId).toEntity(localVarReturnType);
    }

    /**
     * Cancels a live tv timer.
     * 
     * <p>
     * <b>204</b> - Timer deleted.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerId Timer id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec cancelTimerWithResponseSpec(String timerId) throws WebClientResponseException {
        return cancelTimerRequestCreation(timerId);
    }

    /**
     * Creates a live tv series timer.
     * 
     * <p>
     * <b>204</b> - Series timer info created.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param seriesTimerInfoDto New series timer info.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec createSeriesTimerRequestCreation(SeriesTimerInfoDto seriesTimerInfoDto)
            throws WebClientResponseException {
        Object postBody = seriesTimerInfoDto;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/LiveTv/SeriesTimers", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Creates a live tv series timer.
     * 
     * <p>
     * <b>204</b> - Series timer info created.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param seriesTimerInfoDto New series timer info.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> createSeriesTimer(SeriesTimerInfoDto seriesTimerInfoDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return createSeriesTimerRequestCreation(seriesTimerInfoDto).bodyToMono(localVarReturnType);
    }

    /**
     * Creates a live tv series timer.
     * 
     * <p>
     * <b>204</b> - Series timer info created.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param seriesTimerInfoDto New series timer info.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> createSeriesTimerWithHttpInfo(SeriesTimerInfoDto seriesTimerInfoDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return createSeriesTimerRequestCreation(seriesTimerInfoDto).toEntity(localVarReturnType);
    }

    /**
     * Creates a live tv series timer.
     * 
     * <p>
     * <b>204</b> - Series timer info created.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param seriesTimerInfoDto New series timer info.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec createSeriesTimerWithResponseSpec(SeriesTimerInfoDto seriesTimerInfoDto)
            throws WebClientResponseException {
        return createSeriesTimerRequestCreation(seriesTimerInfoDto);
    }

    /**
     * Creates a live tv timer.
     * 
     * <p>
     * <b>204</b> - Timer created.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerInfoDto New timer info.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec createTimerRequestCreation(TimerInfoDto timerInfoDto) throws WebClientResponseException {
        Object postBody = timerInfoDto;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/LiveTv/Timers", HttpMethod.POST, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Creates a live tv timer.
     * 
     * <p>
     * <b>204</b> - Timer created.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerInfoDto New timer info.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> createTimer(TimerInfoDto timerInfoDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return createTimerRequestCreation(timerInfoDto).bodyToMono(localVarReturnType);
    }

    /**
     * Creates a live tv timer.
     * 
     * <p>
     * <b>204</b> - Timer created.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerInfoDto New timer info.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> createTimerWithHttpInfo(TimerInfoDto timerInfoDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return createTimerRequestCreation(timerInfoDto).toEntity(localVarReturnType);
    }

    /**
     * Creates a live tv timer.
     * 
     * <p>
     * <b>204</b> - Timer created.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerInfoDto New timer info.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec createTimerWithResponseSpec(TimerInfoDto timerInfoDto) throws WebClientResponseException {
        return createTimerRequestCreation(timerInfoDto);
    }

    /**
     * Delete listing provider.
     * 
     * <p>
     * <b>204</b> - Listing provider deleted.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Listing provider id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteListingProviderRequestCreation(String id) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "id", id));

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/LiveTv/ListingProviders", HttpMethod.DELETE, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Delete listing provider.
     * 
     * <p>
     * <b>204</b> - Listing provider deleted.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Listing provider id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> deleteListingProvider(String id) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return deleteListingProviderRequestCreation(id).bodyToMono(localVarReturnType);
    }

    /**
     * Delete listing provider.
     * 
     * <p>
     * <b>204</b> - Listing provider deleted.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Listing provider id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> deleteListingProviderWithHttpInfo(String id) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return deleteListingProviderRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     * Delete listing provider.
     * 
     * <p>
     * <b>204</b> - Listing provider deleted.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Listing provider id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteListingProviderWithResponseSpec(String id) throws WebClientResponseException {
        return deleteListingProviderRequestCreation(id);
    }

    /**
     * Deletes a live tv recording.
     * 
     * <p>
     * <b>204</b> - Recording deleted.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param recordingId Recording id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteRecordingRequestCreation(UUID recordingId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'recordingId' is set
        if (recordingId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'recordingId' when calling deleteRecording",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("recordingId", recordingId);

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

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/LiveTv/Recordings/{recordingId}", HttpMethod.DELETE, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Deletes a live tv recording.
     * 
     * <p>
     * <b>204</b> - Recording deleted.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param recordingId Recording id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> deleteRecording(UUID recordingId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return deleteRecordingRequestCreation(recordingId).bodyToMono(localVarReturnType);
    }

    /**
     * Deletes a live tv recording.
     * 
     * <p>
     * <b>204</b> - Recording deleted.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param recordingId Recording id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> deleteRecordingWithHttpInfo(UUID recordingId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return deleteRecordingRequestCreation(recordingId).toEntity(localVarReturnType);
    }

    /**
     * Deletes a live tv recording.
     * 
     * <p>
     * <b>204</b> - Recording deleted.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param recordingId Recording id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteRecordingWithResponseSpec(UUID recordingId) throws WebClientResponseException {
        return deleteRecordingRequestCreation(recordingId);
    }

    /**
     * Deletes a tuner host.
     * 
     * <p>
     * <b>204</b> - Tuner host deleted.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Tuner host id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteTunerHostRequestCreation(String id) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "id", id));

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/LiveTv/TunerHosts", HttpMethod.DELETE, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Deletes a tuner host.
     * 
     * <p>
     * <b>204</b> - Tuner host deleted.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Tuner host id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> deleteTunerHost(String id) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return deleteTunerHostRequestCreation(id).bodyToMono(localVarReturnType);
    }

    /**
     * Deletes a tuner host.
     * 
     * <p>
     * <b>204</b> - Tuner host deleted.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Tuner host id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> deleteTunerHostWithHttpInfo(String id) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return deleteTunerHostRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     * Deletes a tuner host.
     * 
     * <p>
     * <b>204</b> - Tuner host deleted.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Tuner host id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteTunerHostWithResponseSpec(String id) throws WebClientResponseException {
        return deleteTunerHostRequestCreation(id);
    }

    /**
     * Discover tuners.
     * 
     * <p>
     * <b>200</b> - Tuners returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param newDevicesOnly Only discover new tuners.
     * @return List&lt;TunerHostInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec discoverTunersRequestCreation(Boolean newDevicesOnly) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "newDevicesOnly", newDevicesOnly));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<TunerHostInfo> localVarReturnType = new ParameterizedTypeReference<TunerHostInfo>() {
        };
        return apiClient.invokeAPI("/LiveTv/Tuners/Discover", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Discover tuners.
     * 
     * <p>
     * <b>200</b> - Tuners returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param newDevicesOnly Only discover new tuners.
     * @return List&lt;TunerHostInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<TunerHostInfo> discoverTuners(Boolean newDevicesOnly) throws WebClientResponseException {
        ParameterizedTypeReference<TunerHostInfo> localVarReturnType = new ParameterizedTypeReference<TunerHostInfo>() {
        };
        return discoverTunersRequestCreation(newDevicesOnly).bodyToFlux(localVarReturnType);
    }

    /**
     * Discover tuners.
     * 
     * <p>
     * <b>200</b> - Tuners returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param newDevicesOnly Only discover new tuners.
     * @return ResponseEntity&lt;List&lt;TunerHostInfo&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<TunerHostInfo>>> discoverTunersWithHttpInfo(Boolean newDevicesOnly)
            throws WebClientResponseException {
        ParameterizedTypeReference<TunerHostInfo> localVarReturnType = new ParameterizedTypeReference<TunerHostInfo>() {
        };
        return discoverTunersRequestCreation(newDevicesOnly).toEntityList(localVarReturnType);
    }

    /**
     * Discover tuners.
     * 
     * <p>
     * <b>200</b> - Tuners returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param newDevicesOnly Only discover new tuners.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec discoverTunersWithResponseSpec(Boolean newDevicesOnly) throws WebClientResponseException {
        return discoverTunersRequestCreation(newDevicesOnly);
    }

    /**
     * Discover tuners.
     * 
     * <p>
     * <b>200</b> - Tuners returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param newDevicesOnly Only discover new tuners.
     * @return List&lt;TunerHostInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec discvoverTunersRequestCreation(Boolean newDevicesOnly) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "newDevicesOnly", newDevicesOnly));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<TunerHostInfo> localVarReturnType = new ParameterizedTypeReference<TunerHostInfo>() {
        };
        return apiClient.invokeAPI("/LiveTv/Tuners/Discvover", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Discover tuners.
     * 
     * <p>
     * <b>200</b> - Tuners returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param newDevicesOnly Only discover new tuners.
     * @return List&lt;TunerHostInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<TunerHostInfo> discvoverTuners(Boolean newDevicesOnly) throws WebClientResponseException {
        ParameterizedTypeReference<TunerHostInfo> localVarReturnType = new ParameterizedTypeReference<TunerHostInfo>() {
        };
        return discvoverTunersRequestCreation(newDevicesOnly).bodyToFlux(localVarReturnType);
    }

    /**
     * Discover tuners.
     * 
     * <p>
     * <b>200</b> - Tuners returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param newDevicesOnly Only discover new tuners.
     * @return ResponseEntity&lt;List&lt;TunerHostInfo&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<TunerHostInfo>>> discvoverTunersWithHttpInfo(Boolean newDevicesOnly)
            throws WebClientResponseException {
        ParameterizedTypeReference<TunerHostInfo> localVarReturnType = new ParameterizedTypeReference<TunerHostInfo>() {
        };
        return discvoverTunersRequestCreation(newDevicesOnly).toEntityList(localVarReturnType);
    }

    /**
     * Discover tuners.
     * 
     * <p>
     * <b>200</b> - Tuners returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param newDevicesOnly Only discover new tuners.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec discvoverTunersWithResponseSpec(Boolean newDevicesOnly) throws WebClientResponseException {
        return discvoverTunersRequestCreation(newDevicesOnly);
    }

    /**
     * Gets a live tv channel.
     * 
     * <p>
     * <b>200</b> - Live tv channel returned.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelId Channel id.
     * @param userId Optional. Attach user data.
     * @return BaseItemDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getChannelRequestCreation(UUID channelId, UUID userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'channelId' is set
        if (channelId == null) {
            throw new WebClientResponseException("Missing the required parameter 'channelId' when calling getChannel",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("channelId", channelId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return apiClient.invokeAPI("/LiveTv/Channels/{channelId}", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets a live tv channel.
     * 
     * <p>
     * <b>200</b> - Live tv channel returned.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelId Channel id.
     * @param userId Optional. Attach user data.
     * @return BaseItemDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDto> getChannel(UUID channelId, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return getChannelRequestCreation(channelId, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a live tv channel.
     * 
     * <p>
     * <b>200</b> - Live tv channel returned.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelId Channel id.
     * @param userId Optional. Attach user data.
     * @return ResponseEntity&lt;BaseItemDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDto>> getChannelWithHttpInfo(UUID channelId, UUID userId)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return getChannelRequestCreation(channelId, userId).toEntity(localVarReturnType);
    }

    /**
     * Gets a live tv channel.
     * 
     * <p>
     * <b>200</b> - Live tv channel returned.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelId Channel id.
     * @param userId Optional. Attach user data.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getChannelWithResponseSpec(UUID channelId, UUID userId) throws WebClientResponseException {
        return getChannelRequestCreation(channelId, userId);
    }

    /**
     * Get channel mapping options.
     * 
     * <p>
     * <b>200</b> - Channel mapping options returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param providerId Provider id.
     * @return ChannelMappingOptionsDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getChannelMappingOptionsRequestCreation(String providerId) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "providerId", providerId));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<ChannelMappingOptionsDto> localVarReturnType = new ParameterizedTypeReference<ChannelMappingOptionsDto>() {
        };
        return apiClient.invokeAPI("/LiveTv/ChannelMappingOptions", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Get channel mapping options.
     * 
     * <p>
     * <b>200</b> - Channel mapping options returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param providerId Provider id.
     * @return ChannelMappingOptionsDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ChannelMappingOptionsDto> getChannelMappingOptions(String providerId)
            throws WebClientResponseException {
        ParameterizedTypeReference<ChannelMappingOptionsDto> localVarReturnType = new ParameterizedTypeReference<ChannelMappingOptionsDto>() {
        };
        return getChannelMappingOptionsRequestCreation(providerId).bodyToMono(localVarReturnType);
    }

    /**
     * Get channel mapping options.
     * 
     * <p>
     * <b>200</b> - Channel mapping options returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param providerId Provider id.
     * @return ResponseEntity&lt;ChannelMappingOptionsDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<ChannelMappingOptionsDto>> getChannelMappingOptionsWithHttpInfo(String providerId)
            throws WebClientResponseException {
        ParameterizedTypeReference<ChannelMappingOptionsDto> localVarReturnType = new ParameterizedTypeReference<ChannelMappingOptionsDto>() {
        };
        return getChannelMappingOptionsRequestCreation(providerId).toEntity(localVarReturnType);
    }

    /**
     * Get channel mapping options.
     * 
     * <p>
     * <b>200</b> - Channel mapping options returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param providerId Provider id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getChannelMappingOptionsWithResponseSpec(String providerId) throws WebClientResponseException {
        return getChannelMappingOptionsRequestCreation(providerId);
    }

    /**
     * Gets default listings provider info.
     * 
     * <p>
     * <b>200</b> - Default listings provider info returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ListingsProviderInfo
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getDefaultListingProviderRequestCreation() throws WebClientResponseException {
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

        ParameterizedTypeReference<ListingsProviderInfo> localVarReturnType = new ParameterizedTypeReference<ListingsProviderInfo>() {
        };
        return apiClient.invokeAPI("/LiveTv/ListingProviders/Default", HttpMethod.GET, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets default listings provider info.
     * 
     * <p>
     * <b>200</b> - Default listings provider info returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ListingsProviderInfo
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ListingsProviderInfo> getDefaultListingProvider() throws WebClientResponseException {
        ParameterizedTypeReference<ListingsProviderInfo> localVarReturnType = new ParameterizedTypeReference<ListingsProviderInfo>() {
        };
        return getDefaultListingProviderRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Gets default listings provider info.
     * 
     * <p>
     * <b>200</b> - Default listings provider info returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseEntity&lt;ListingsProviderInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<ListingsProviderInfo>> getDefaultListingProviderWithHttpInfo()
            throws WebClientResponseException {
        ParameterizedTypeReference<ListingsProviderInfo> localVarReturnType = new ParameterizedTypeReference<ListingsProviderInfo>() {
        };
        return getDefaultListingProviderRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Gets default listings provider info.
     * 
     * <p>
     * <b>200</b> - Default listings provider info returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getDefaultListingProviderWithResponseSpec() throws WebClientResponseException {
        return getDefaultListingProviderRequestCreation();
    }

    /**
     * Gets the default values for a new timer.
     * 
     * <p>
     * <b>200</b> - Default values returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param programId Optional. To attach default values based on a program.
     * @return SeriesTimerInfoDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getDefaultTimerRequestCreation(String programId) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "programId", programId));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<SeriesTimerInfoDto> localVarReturnType = new ParameterizedTypeReference<SeriesTimerInfoDto>() {
        };
        return apiClient.invokeAPI("/LiveTv/Timers/Defaults", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets the default values for a new timer.
     * 
     * <p>
     * <b>200</b> - Default values returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param programId Optional. To attach default values based on a program.
     * @return SeriesTimerInfoDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<SeriesTimerInfoDto> getDefaultTimer(String programId) throws WebClientResponseException {
        ParameterizedTypeReference<SeriesTimerInfoDto> localVarReturnType = new ParameterizedTypeReference<SeriesTimerInfoDto>() {
        };
        return getDefaultTimerRequestCreation(programId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets the default values for a new timer.
     * 
     * <p>
     * <b>200</b> - Default values returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param programId Optional. To attach default values based on a program.
     * @return ResponseEntity&lt;SeriesTimerInfoDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<SeriesTimerInfoDto>> getDefaultTimerWithHttpInfo(String programId)
            throws WebClientResponseException {
        ParameterizedTypeReference<SeriesTimerInfoDto> localVarReturnType = new ParameterizedTypeReference<SeriesTimerInfoDto>() {
        };
        return getDefaultTimerRequestCreation(programId).toEntity(localVarReturnType);
    }

    /**
     * Gets the default values for a new timer.
     * 
     * <p>
     * <b>200</b> - Default values returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param programId Optional. To attach default values based on a program.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getDefaultTimerWithResponseSpec(String programId) throws WebClientResponseException {
        return getDefaultTimerRequestCreation(programId);
    }

    /**
     * Get guid info.
     * 
     * <p>
     * <b>200</b> - Guid info returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return GuideInfo
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getGuideInfoRequestCreation() throws WebClientResponseException {
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

        ParameterizedTypeReference<GuideInfo> localVarReturnType = new ParameterizedTypeReference<GuideInfo>() {
        };
        return apiClient.invokeAPI("/LiveTv/GuideInfo", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get guid info.
     * 
     * <p>
     * <b>200</b> - Guid info returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return GuideInfo
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<GuideInfo> getGuideInfo() throws WebClientResponseException {
        ParameterizedTypeReference<GuideInfo> localVarReturnType = new ParameterizedTypeReference<GuideInfo>() {
        };
        return getGuideInfoRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Get guid info.
     * 
     * <p>
     * <b>200</b> - Guid info returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseEntity&lt;GuideInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<GuideInfo>> getGuideInfoWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<GuideInfo> localVarReturnType = new ParameterizedTypeReference<GuideInfo>() {
        };
        return getGuideInfoRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Get guid info.
     * 
     * <p>
     * <b>200</b> - Guid info returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getGuideInfoWithResponseSpec() throws WebClientResponseException {
        return getGuideInfoRequestCreation();
    }

    /**
     * Gets available lineups.
     * 
     * <p>
     * <b>200</b> - Available lineups returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Provider id.
     * @param type Provider type.
     * @param location Location.
     * @param country Country.
     * @return List&lt;NameIdPair&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getLineupsRequestCreation(String id, String type, String location, String country)
            throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "id", id));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "type", type));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "location", location));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "country", country));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<NameIdPair> localVarReturnType = new ParameterizedTypeReference<NameIdPair>() {
        };
        return apiClient.invokeAPI("/LiveTv/ListingProviders/Lineups", HttpMethod.GET, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets available lineups.
     * 
     * <p>
     * <b>200</b> - Available lineups returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Provider id.
     * @param type Provider type.
     * @param location Location.
     * @param country Country.
     * @return List&lt;NameIdPair&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<NameIdPair> getLineups(String id, String type, String location, String country)
            throws WebClientResponseException {
        ParameterizedTypeReference<NameIdPair> localVarReturnType = new ParameterizedTypeReference<NameIdPair>() {
        };
        return getLineupsRequestCreation(id, type, location, country).bodyToFlux(localVarReturnType);
    }

    /**
     * Gets available lineups.
     * 
     * <p>
     * <b>200</b> - Available lineups returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Provider id.
     * @param type Provider type.
     * @param location Location.
     * @param country Country.
     * @return ResponseEntity&lt;List&lt;NameIdPair&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<NameIdPair>>> getLineupsWithHttpInfo(String id, String type, String location,
            String country) throws WebClientResponseException {
        ParameterizedTypeReference<NameIdPair> localVarReturnType = new ParameterizedTypeReference<NameIdPair>() {
        };
        return getLineupsRequestCreation(id, type, location, country).toEntityList(localVarReturnType);
    }

    /**
     * Gets available lineups.
     * 
     * <p>
     * <b>200</b> - Available lineups returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id Provider id.
     * @param type Provider type.
     * @param location Location.
     * @param country Country.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getLineupsWithResponseSpec(String id, String type, String location, String country)
            throws WebClientResponseException {
        return getLineupsRequestCreation(id, type, location, country);
    }

    /**
     * Gets a live tv recording stream.
     * 
     * <p>
     * <b>200</b> - Recording stream returned.
     * <p>
     * <b>404</b> - Recording not found.
     * 
     * @param recordingId Recording id.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getLiveRecordingFileRequestCreation(String recordingId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'recordingId' is set
        if (recordingId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'recordingId' when calling getLiveRecordingFile",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("recordingId", recordingId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "video/*", "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {};

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI("/LiveTv/LiveRecordings/{recordingId}/stream", HttpMethod.GET, pathParams,
                queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a live tv recording stream.
     * 
     * <p>
     * <b>200</b> - Recording stream returned.
     * <p>
     * <b>404</b> - Recording not found.
     * 
     * @param recordingId Recording id.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getLiveRecordingFile(String recordingId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getLiveRecordingFileRequestCreation(recordingId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a live tv recording stream.
     * 
     * <p>
     * <b>200</b> - Recording stream returned.
     * <p>
     * <b>404</b> - Recording not found.
     * 
     * @param recordingId Recording id.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getLiveRecordingFileWithHttpInfo(String recordingId)
            throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getLiveRecordingFileRequestCreation(recordingId).toEntity(localVarReturnType);
    }

    /**
     * Gets a live tv recording stream.
     * 
     * <p>
     * <b>200</b> - Recording stream returned.
     * <p>
     * <b>404</b> - Recording not found.
     * 
     * @param recordingId Recording id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getLiveRecordingFileWithResponseSpec(String recordingId) throws WebClientResponseException {
        return getLiveRecordingFileRequestCreation(recordingId);
    }

    /**
     * Gets a live tv channel stream.
     * 
     * <p>
     * <b>200</b> - Stream returned.
     * <p>
     * <b>404</b> - Stream not found.
     * 
     * @param streamId Stream id.
     * @param container Container type.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getLiveStreamFileRequestCreation(String streamId, String container)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'streamId' is set
        if (streamId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'streamId' when calling getLiveStreamFile",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'container' is set
        if (container == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'container' when calling getLiveStreamFile",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("streamId", streamId);
        pathParams.put("container", container);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "video/*", "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {};

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI("/LiveTv/LiveStreamFiles/{streamId}/stream.{container}", HttpMethod.GET, pathParams,
                queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a live tv channel stream.
     * 
     * <p>
     * <b>200</b> - Stream returned.
     * <p>
     * <b>404</b> - Stream not found.
     * 
     * @param streamId Stream id.
     * @param container Container type.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getLiveStreamFile(String streamId, String container) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getLiveStreamFileRequestCreation(streamId, container).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a live tv channel stream.
     * 
     * <p>
     * <b>200</b> - Stream returned.
     * <p>
     * <b>404</b> - Stream not found.
     * 
     * @param streamId Stream id.
     * @param container Container type.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getLiveStreamFileWithHttpInfo(String streamId, String container)
            throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getLiveStreamFileRequestCreation(streamId, container).toEntity(localVarReturnType);
    }

    /**
     * Gets a live tv channel stream.
     * 
     * <p>
     * <b>200</b> - Stream returned.
     * <p>
     * <b>404</b> - Stream not found.
     * 
     * @param streamId Stream id.
     * @param container Container type.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getLiveStreamFileWithResponseSpec(String streamId, String container)
            throws WebClientResponseException {
        return getLiveStreamFileRequestCreation(streamId, container);
    }

    /**
     * Gets available live tv channels.
     * 
     * <p>
     * <b>200</b> - Available live tv channels returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param type Optional. Filter by channel type.
     * @param userId Optional. Filter by user and attach user data.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param isMovie Optional. Filter for movies.
     * @param isSeries Optional. Filter for series.
     * @param isNews Optional. Filter for news.
     * @param isKids Optional. Filter for kids.
     * @param isSports Optional. Filter for sports.
     * @param limit Optional. The maximum number of records to return.
     * @param isFavorite Optional. Filter by channels that are favorites, or not.
     * @param isLiked Optional. Filter by channels that are liked, or not.
     * @param isDisliked Optional. Filter by channels that are disliked, or not.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes \&quot;Optional. The image types to include in the output.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableUserData Optional. Include user data.
     * @param sortBy Optional. Key to sort by.
     * @param sortOrder Optional. Sort order.
     * @param enableFavoriteSorting Optional. Incorporate favorite and like status into channel sorting.
     * @param addCurrentProgram Optional. Adds current program info to each channel.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getLiveTvChannelsRequestCreation(ChannelType type, UUID userId, Integer startIndex,
            Boolean isMovie, Boolean isSeries, Boolean isNews, Boolean isKids, Boolean isSports, Integer limit,
            Boolean isFavorite, Boolean isLiked, Boolean isDisliked, Boolean enableImages, Integer imageTypeLimit,
            List<ImageType> enableImageTypes, List<ItemFields> fields, Boolean enableUserData, List<ItemSortBy> sortBy,
            SortOrder sortOrder, Boolean enableFavoriteSorting, Boolean addCurrentProgram)
            throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "type", type));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startIndex", startIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isMovie", isMovie));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isSeries", isSeries));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isNews", isNews));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isKids", isKids));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isSports", isSports));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isFavorite", isFavorite));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isLiked", isLiked));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isDisliked", isDisliked));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "enableImageTypes", enableImageTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "sortBy", sortBy));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "sortOrder", sortOrder));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableFavoriteSorting", enableFavoriteSorting));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addCurrentProgram", addCurrentProgram));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/LiveTv/Channels", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets available live tv channels.
     * 
     * <p>
     * <b>200</b> - Available live tv channels returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param type Optional. Filter by channel type.
     * @param userId Optional. Filter by user and attach user data.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param isMovie Optional. Filter for movies.
     * @param isSeries Optional. Filter for series.
     * @param isNews Optional. Filter for news.
     * @param isKids Optional. Filter for kids.
     * @param isSports Optional. Filter for sports.
     * @param limit Optional. The maximum number of records to return.
     * @param isFavorite Optional. Filter by channels that are favorites, or not.
     * @param isLiked Optional. Filter by channels that are liked, or not.
     * @param isDisliked Optional. Filter by channels that are disliked, or not.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes \&quot;Optional. The image types to include in the output.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableUserData Optional. Include user data.
     * @param sortBy Optional. Key to sort by.
     * @param sortOrder Optional. Sort order.
     * @param enableFavoriteSorting Optional. Incorporate favorite and like status into channel sorting.
     * @param addCurrentProgram Optional. Adds current program info to each channel.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getLiveTvChannels(ChannelType type, UUID userId, Integer startIndex,
            Boolean isMovie, Boolean isSeries, Boolean isNews, Boolean isKids, Boolean isSports, Integer limit,
            Boolean isFavorite, Boolean isLiked, Boolean isDisliked, Boolean enableImages, Integer imageTypeLimit,
            List<ImageType> enableImageTypes, List<ItemFields> fields, Boolean enableUserData, List<ItemSortBy> sortBy,
            SortOrder sortOrder, Boolean enableFavoriteSorting, Boolean addCurrentProgram)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getLiveTvChannelsRequestCreation(type, userId, startIndex, isMovie, isSeries, isNews, isKids, isSports,
                limit, isFavorite, isLiked, isDisliked, enableImages, imageTypeLimit, enableImageTypes, fields,
                enableUserData, sortBy, sortOrder, enableFavoriteSorting, addCurrentProgram)
                .bodyToMono(localVarReturnType);
    }

    /**
     * Gets available live tv channels.
     * 
     * <p>
     * <b>200</b> - Available live tv channels returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param type Optional. Filter by channel type.
     * @param userId Optional. Filter by user and attach user data.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param isMovie Optional. Filter for movies.
     * @param isSeries Optional. Filter for series.
     * @param isNews Optional. Filter for news.
     * @param isKids Optional. Filter for kids.
     * @param isSports Optional. Filter for sports.
     * @param limit Optional. The maximum number of records to return.
     * @param isFavorite Optional. Filter by channels that are favorites, or not.
     * @param isLiked Optional. Filter by channels that are liked, or not.
     * @param isDisliked Optional. Filter by channels that are disliked, or not.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes \&quot;Optional. The image types to include in the output.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableUserData Optional. Include user data.
     * @param sortBy Optional. Key to sort by.
     * @param sortOrder Optional. Sort order.
     * @param enableFavoriteSorting Optional. Incorporate favorite and like status into channel sorting.
     * @param addCurrentProgram Optional. Adds current program info to each channel.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getLiveTvChannelsWithHttpInfo(ChannelType type, UUID userId,
            Integer startIndex, Boolean isMovie, Boolean isSeries, Boolean isNews, Boolean isKids, Boolean isSports,
            Integer limit, Boolean isFavorite, Boolean isLiked, Boolean isDisliked, Boolean enableImages,
            Integer imageTypeLimit, List<ImageType> enableImageTypes, List<ItemFields> fields, Boolean enableUserData,
            List<ItemSortBy> sortBy, SortOrder sortOrder, Boolean enableFavoriteSorting, Boolean addCurrentProgram)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getLiveTvChannelsRequestCreation(type, userId, startIndex, isMovie, isSeries, isNews, isKids, isSports,
                limit, isFavorite, isLiked, isDisliked, enableImages, imageTypeLimit, enableImageTypes, fields,
                enableUserData, sortBy, sortOrder, enableFavoriteSorting, addCurrentProgram)
                .toEntity(localVarReturnType);
    }

    /**
     * Gets available live tv channels.
     * 
     * <p>
     * <b>200</b> - Available live tv channels returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param type Optional. Filter by channel type.
     * @param userId Optional. Filter by user and attach user data.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param isMovie Optional. Filter for movies.
     * @param isSeries Optional. Filter for series.
     * @param isNews Optional. Filter for news.
     * @param isKids Optional. Filter for kids.
     * @param isSports Optional. Filter for sports.
     * @param limit Optional. The maximum number of records to return.
     * @param isFavorite Optional. Filter by channels that are favorites, or not.
     * @param isLiked Optional. Filter by channels that are liked, or not.
     * @param isDisliked Optional. Filter by channels that are disliked, or not.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes \&quot;Optional. The image types to include in the output.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableUserData Optional. Include user data.
     * @param sortBy Optional. Key to sort by.
     * @param sortOrder Optional. Sort order.
     * @param enableFavoriteSorting Optional. Incorporate favorite and like status into channel sorting.
     * @param addCurrentProgram Optional. Adds current program info to each channel.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getLiveTvChannelsWithResponseSpec(ChannelType type, UUID userId, Integer startIndex,
            Boolean isMovie, Boolean isSeries, Boolean isNews, Boolean isKids, Boolean isSports, Integer limit,
            Boolean isFavorite, Boolean isLiked, Boolean isDisliked, Boolean enableImages, Integer imageTypeLimit,
            List<ImageType> enableImageTypes, List<ItemFields> fields, Boolean enableUserData, List<ItemSortBy> sortBy,
            SortOrder sortOrder, Boolean enableFavoriteSorting, Boolean addCurrentProgram)
            throws WebClientResponseException {
        return getLiveTvChannelsRequestCreation(type, userId, startIndex, isMovie, isSeries, isNews, isKids, isSports,
                limit, isFavorite, isLiked, isDisliked, enableImages, imageTypeLimit, enableImageTypes, fields,
                enableUserData, sortBy, sortOrder, enableFavoriteSorting, addCurrentProgram);
    }

    /**
     * Gets available live tv services.
     * 
     * <p>
     * <b>200</b> - Available live tv services returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return LiveTvInfo
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getLiveTvInfoRequestCreation() throws WebClientResponseException {
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

        ParameterizedTypeReference<LiveTvInfo> localVarReturnType = new ParameterizedTypeReference<LiveTvInfo>() {
        };
        return apiClient.invokeAPI("/LiveTv/Info", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets available live tv services.
     * 
     * <p>
     * <b>200</b> - Available live tv services returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return LiveTvInfo
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<LiveTvInfo> getLiveTvInfo() throws WebClientResponseException {
        ParameterizedTypeReference<LiveTvInfo> localVarReturnType = new ParameterizedTypeReference<LiveTvInfo>() {
        };
        return getLiveTvInfoRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Gets available live tv services.
     * 
     * <p>
     * <b>200</b> - Available live tv services returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseEntity&lt;LiveTvInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<LiveTvInfo>> getLiveTvInfoWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<LiveTvInfo> localVarReturnType = new ParameterizedTypeReference<LiveTvInfo>() {
        };
        return getLiveTvInfoRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Gets available live tv services.
     * 
     * <p>
     * <b>200</b> - Available live tv services returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getLiveTvInfoWithResponseSpec() throws WebClientResponseException {
        return getLiveTvInfoRequestCreation();
    }

    /**
     * Gets available live tv epgs.
     * 
     * <p>
     * <b>200</b> - Live tv epgs returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelIds The channels to return guide information for.
     * @param userId Optional. Filter by user id.
     * @param minStartDate Optional. The minimum premiere start date.
     * @param hasAired Optional. Filter by programs that have completed airing, or not.
     * @param isAiring Optional. Filter by programs that are currently airing, or not.
     * @param maxStartDate Optional. The maximum premiere start date.
     * @param minEndDate Optional. The minimum premiere end date.
     * @param maxEndDate Optional. The maximum premiere end date.
     * @param isMovie Optional. Filter for movies.
     * @param isSeries Optional. Filter for series.
     * @param isNews Optional. Filter for news.
     * @param isKids Optional. Filter for kids.
     * @param isSports Optional. Filter for sports.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Name, StartDate.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param genres The genres to return guide information for.
     * @param genreIds The genre ids to return guide information for.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param enableUserData Optional. Include user data.
     * @param seriesTimerId Optional. Filter by series timer id.
     * @param librarySeriesId Optional. Filter by library series id.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableTotalRecordCount Retrieve total record count.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getLiveTvProgramsRequestCreation(List<UUID> channelIds, UUID userId,
            OffsetDateTime minStartDate, Boolean hasAired, Boolean isAiring, OffsetDateTime maxStartDate,
            OffsetDateTime minEndDate, OffsetDateTime maxEndDate, Boolean isMovie, Boolean isSeries, Boolean isNews,
            Boolean isKids, Boolean isSports, Integer startIndex, Integer limit, List<ItemSortBy> sortBy,
            List<SortOrder> sortOrder, List<String> genres, List<UUID> genreIds, Boolean enableImages,
            Integer imageTypeLimit, List<ImageType> enableImageTypes, Boolean enableUserData, String seriesTimerId,
            UUID librarySeriesId, List<ItemFields> fields, Boolean enableTotalRecordCount)
            throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "channelIds", channelIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "minStartDate", minStartDate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "hasAired", hasAired));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isAiring", isAiring));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxStartDate", maxStartDate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "minEndDate", minEndDate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxEndDate", maxEndDate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isMovie", isMovie));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isSeries", isSeries));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isNews", isNews));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isKids", isKids));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isSports", isSports));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startIndex", startIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "sortBy", sortBy));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "sortOrder", sortOrder));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "genres", genres));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "genreIds", genreIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "enableImageTypes", enableImageTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "seriesTimerId", seriesTimerId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "librarySeriesId", librarySeriesId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableTotalRecordCount", enableTotalRecordCount));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/LiveTv/Programs", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets available live tv epgs.
     * 
     * <p>
     * <b>200</b> - Live tv epgs returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelIds The channels to return guide information for.
     * @param userId Optional. Filter by user id.
     * @param minStartDate Optional. The minimum premiere start date.
     * @param hasAired Optional. Filter by programs that have completed airing, or not.
     * @param isAiring Optional. Filter by programs that are currently airing, or not.
     * @param maxStartDate Optional. The maximum premiere start date.
     * @param minEndDate Optional. The minimum premiere end date.
     * @param maxEndDate Optional. The maximum premiere end date.
     * @param isMovie Optional. Filter for movies.
     * @param isSeries Optional. Filter for series.
     * @param isNews Optional. Filter for news.
     * @param isKids Optional. Filter for kids.
     * @param isSports Optional. Filter for sports.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Name, StartDate.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param genres The genres to return guide information for.
     * @param genreIds The genre ids to return guide information for.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param enableUserData Optional. Include user data.
     * @param seriesTimerId Optional. Filter by series timer id.
     * @param librarySeriesId Optional. Filter by library series id.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableTotalRecordCount Retrieve total record count.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getLiveTvPrograms(List<UUID> channelIds, UUID userId,
            OffsetDateTime minStartDate, Boolean hasAired, Boolean isAiring, OffsetDateTime maxStartDate,
            OffsetDateTime minEndDate, OffsetDateTime maxEndDate, Boolean isMovie, Boolean isSeries, Boolean isNews,
            Boolean isKids, Boolean isSports, Integer startIndex, Integer limit, List<ItemSortBy> sortBy,
            List<SortOrder> sortOrder, List<String> genres, List<UUID> genreIds, Boolean enableImages,
            Integer imageTypeLimit, List<ImageType> enableImageTypes, Boolean enableUserData, String seriesTimerId,
            UUID librarySeriesId, List<ItemFields> fields, Boolean enableTotalRecordCount)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getLiveTvProgramsRequestCreation(channelIds, userId, minStartDate, hasAired, isAiring, maxStartDate,
                minEndDate, maxEndDate, isMovie, isSeries, isNews, isKids, isSports, startIndex, limit, sortBy,
                sortOrder, genres, genreIds, enableImages, imageTypeLimit, enableImageTypes, enableUserData,
                seriesTimerId, librarySeriesId, fields, enableTotalRecordCount).bodyToMono(localVarReturnType);
    }

    /**
     * Gets available live tv epgs.
     * 
     * <p>
     * <b>200</b> - Live tv epgs returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelIds The channels to return guide information for.
     * @param userId Optional. Filter by user id.
     * @param minStartDate Optional. The minimum premiere start date.
     * @param hasAired Optional. Filter by programs that have completed airing, or not.
     * @param isAiring Optional. Filter by programs that are currently airing, or not.
     * @param maxStartDate Optional. The maximum premiere start date.
     * @param minEndDate Optional. The minimum premiere end date.
     * @param maxEndDate Optional. The maximum premiere end date.
     * @param isMovie Optional. Filter for movies.
     * @param isSeries Optional. Filter for series.
     * @param isNews Optional. Filter for news.
     * @param isKids Optional. Filter for kids.
     * @param isSports Optional. Filter for sports.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Name, StartDate.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param genres The genres to return guide information for.
     * @param genreIds The genre ids to return guide information for.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param enableUserData Optional. Include user data.
     * @param seriesTimerId Optional. Filter by series timer id.
     * @param librarySeriesId Optional. Filter by library series id.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableTotalRecordCount Retrieve total record count.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getLiveTvProgramsWithHttpInfo(List<UUID> channelIds,
            UUID userId, OffsetDateTime minStartDate, Boolean hasAired, Boolean isAiring, OffsetDateTime maxStartDate,
            OffsetDateTime minEndDate, OffsetDateTime maxEndDate, Boolean isMovie, Boolean isSeries, Boolean isNews,
            Boolean isKids, Boolean isSports, Integer startIndex, Integer limit, List<ItemSortBy> sortBy,
            List<SortOrder> sortOrder, List<String> genres, List<UUID> genreIds, Boolean enableImages,
            Integer imageTypeLimit, List<ImageType> enableImageTypes, Boolean enableUserData, String seriesTimerId,
            UUID librarySeriesId, List<ItemFields> fields, Boolean enableTotalRecordCount)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getLiveTvProgramsRequestCreation(channelIds, userId, minStartDate, hasAired, isAiring, maxStartDate,
                minEndDate, maxEndDate, isMovie, isSeries, isNews, isKids, isSports, startIndex, limit, sortBy,
                sortOrder, genres, genreIds, enableImages, imageTypeLimit, enableImageTypes, enableUserData,
                seriesTimerId, librarySeriesId, fields, enableTotalRecordCount).toEntity(localVarReturnType);
    }

    /**
     * Gets available live tv epgs.
     * 
     * <p>
     * <b>200</b> - Live tv epgs returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelIds The channels to return guide information for.
     * @param userId Optional. Filter by user id.
     * @param minStartDate Optional. The minimum premiere start date.
     * @param hasAired Optional. Filter by programs that have completed airing, or not.
     * @param isAiring Optional. Filter by programs that are currently airing, or not.
     * @param maxStartDate Optional. The maximum premiere start date.
     * @param minEndDate Optional. The minimum premiere end date.
     * @param maxEndDate Optional. The maximum premiere end date.
     * @param isMovie Optional. Filter for movies.
     * @param isSeries Optional. Filter for series.
     * @param isNews Optional. Filter for news.
     * @param isKids Optional. Filter for kids.
     * @param isSports Optional. Filter for sports.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Name, StartDate.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param genres The genres to return guide information for.
     * @param genreIds The genre ids to return guide information for.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param enableUserData Optional. Include user data.
     * @param seriesTimerId Optional. Filter by series timer id.
     * @param librarySeriesId Optional. Filter by library series id.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableTotalRecordCount Retrieve total record count.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getLiveTvProgramsWithResponseSpec(List<UUID> channelIds, UUID userId,
            OffsetDateTime minStartDate, Boolean hasAired, Boolean isAiring, OffsetDateTime maxStartDate,
            OffsetDateTime minEndDate, OffsetDateTime maxEndDate, Boolean isMovie, Boolean isSeries, Boolean isNews,
            Boolean isKids, Boolean isSports, Integer startIndex, Integer limit, List<ItemSortBy> sortBy,
            List<SortOrder> sortOrder, List<String> genres, List<UUID> genreIds, Boolean enableImages,
            Integer imageTypeLimit, List<ImageType> enableImageTypes, Boolean enableUserData, String seriesTimerId,
            UUID librarySeriesId, List<ItemFields> fields, Boolean enableTotalRecordCount)
            throws WebClientResponseException {
        return getLiveTvProgramsRequestCreation(channelIds, userId, minStartDate, hasAired, isAiring, maxStartDate,
                minEndDate, maxEndDate, isMovie, isSeries, isNews, isKids, isSports, startIndex, limit, sortBy,
                sortOrder, genres, genreIds, enableImages, imageTypeLimit, enableImageTypes, enableUserData,
                seriesTimerId, librarySeriesId, fields, enableTotalRecordCount);
    }

    /**
     * Gets a live tv program.
     * 
     * <p>
     * <b>200</b> - Program returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param programId Program id.
     * @param userId Optional. Attach user data.
     * @return BaseItemDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getProgramRequestCreation(String programId, UUID userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'programId' is set
        if (programId == null) {
            throw new WebClientResponseException("Missing the required parameter 'programId' when calling getProgram",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("programId", programId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return apiClient.invokeAPI("/LiveTv/Programs/{programId}", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets a live tv program.
     * 
     * <p>
     * <b>200</b> - Program returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param programId Program id.
     * @param userId Optional. Attach user data.
     * @return BaseItemDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDto> getProgram(String programId, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return getProgramRequestCreation(programId, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a live tv program.
     * 
     * <p>
     * <b>200</b> - Program returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param programId Program id.
     * @param userId Optional. Attach user data.
     * @return ResponseEntity&lt;BaseItemDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDto>> getProgramWithHttpInfo(String programId, UUID userId)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return getProgramRequestCreation(programId, userId).toEntity(localVarReturnType);
    }

    /**
     * Gets a live tv program.
     * 
     * <p>
     * <b>200</b> - Program returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param programId Program id.
     * @param userId Optional. Attach user data.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getProgramWithResponseSpec(String programId, UUID userId) throws WebClientResponseException {
        return getProgramRequestCreation(programId, userId);
    }

    /**
     * Gets available live tv epgs.
     * 
     * <p>
     * <b>200</b> - Live tv epgs returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param getProgramsDto Request body.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getProgramsRequestCreation(GetProgramsDto getProgramsDto) throws WebClientResponseException {
        Object postBody = getProgramsDto;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/LiveTv/Programs", HttpMethod.POST, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets available live tv epgs.
     * 
     * <p>
     * <b>200</b> - Live tv epgs returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param getProgramsDto Request body.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getPrograms(GetProgramsDto getProgramsDto) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getProgramsRequestCreation(getProgramsDto).bodyToMono(localVarReturnType);
    }

    /**
     * Gets available live tv epgs.
     * 
     * <p>
     * <b>200</b> - Live tv epgs returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param getProgramsDto Request body.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getProgramsWithHttpInfo(GetProgramsDto getProgramsDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getProgramsRequestCreation(getProgramsDto).toEntity(localVarReturnType);
    }

    /**
     * Gets available live tv epgs.
     * 
     * <p>
     * <b>200</b> - Live tv epgs returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param getProgramsDto Request body.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getProgramsWithResponseSpec(GetProgramsDto getProgramsDto) throws WebClientResponseException {
        return getProgramsRequestCreation(getProgramsDto);
    }

    /**
     * Gets recommended live tv epgs.
     * 
     * <p>
     * <b>200</b> - Recommended epgs returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId Optional. filter by user id.
     * @param limit Optional. The maximum number of records to return.
     * @param isAiring Optional. Filter by programs that are currently airing, or not.
     * @param hasAired Optional. Filter by programs that have completed airing, or not.
     * @param isSeries Optional. Filter for series.
     * @param isMovie Optional. Filter for movies.
     * @param isNews Optional. Filter for news.
     * @param isKids Optional. Filter for kids.
     * @param isSports Optional. Filter for sports.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param genreIds The genres to return guide information for.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableUserData Optional. include user data.
     * @param enableTotalRecordCount Retrieve total record count.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getRecommendedProgramsRequestCreation(UUID userId, Integer limit, Boolean isAiring,
            Boolean hasAired, Boolean isSeries, Boolean isMovie, Boolean isNews, Boolean isKids, Boolean isSports,
            Boolean enableImages, Integer imageTypeLimit, List<ImageType> enableImageTypes, List<UUID> genreIds,
            List<ItemFields> fields, Boolean enableUserData, Boolean enableTotalRecordCount)
            throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isAiring", isAiring));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "hasAired", hasAired));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isSeries", isSeries));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isMovie", isMovie));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isNews", isNews));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isKids", isKids));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isSports", isSports));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "enableImageTypes", enableImageTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "genreIds", genreIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableTotalRecordCount", enableTotalRecordCount));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/LiveTv/Programs/Recommended", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets recommended live tv epgs.
     * 
     * <p>
     * <b>200</b> - Recommended epgs returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId Optional. filter by user id.
     * @param limit Optional. The maximum number of records to return.
     * @param isAiring Optional. Filter by programs that are currently airing, or not.
     * @param hasAired Optional. Filter by programs that have completed airing, or not.
     * @param isSeries Optional. Filter for series.
     * @param isMovie Optional. Filter for movies.
     * @param isNews Optional. Filter for news.
     * @param isKids Optional. Filter for kids.
     * @param isSports Optional. Filter for sports.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param genreIds The genres to return guide information for.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableUserData Optional. include user data.
     * @param enableTotalRecordCount Retrieve total record count.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getRecommendedPrograms(UUID userId, Integer limit, Boolean isAiring,
            Boolean hasAired, Boolean isSeries, Boolean isMovie, Boolean isNews, Boolean isKids, Boolean isSports,
            Boolean enableImages, Integer imageTypeLimit, List<ImageType> enableImageTypes, List<UUID> genreIds,
            List<ItemFields> fields, Boolean enableUserData, Boolean enableTotalRecordCount)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getRecommendedProgramsRequestCreation(userId, limit, isAiring, hasAired, isSeries, isMovie, isNews,
                isKids, isSports, enableImages, imageTypeLimit, enableImageTypes, genreIds, fields, enableUserData,
                enableTotalRecordCount).bodyToMono(localVarReturnType);
    }

    /**
     * Gets recommended live tv epgs.
     * 
     * <p>
     * <b>200</b> - Recommended epgs returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId Optional. filter by user id.
     * @param limit Optional. The maximum number of records to return.
     * @param isAiring Optional. Filter by programs that are currently airing, or not.
     * @param hasAired Optional. Filter by programs that have completed airing, or not.
     * @param isSeries Optional. Filter for series.
     * @param isMovie Optional. Filter for movies.
     * @param isNews Optional. Filter for news.
     * @param isKids Optional. Filter for kids.
     * @param isSports Optional. Filter for sports.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param genreIds The genres to return guide information for.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableUserData Optional. include user data.
     * @param enableTotalRecordCount Retrieve total record count.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getRecommendedProgramsWithHttpInfo(UUID userId, Integer limit,
            Boolean isAiring, Boolean hasAired, Boolean isSeries, Boolean isMovie, Boolean isNews, Boolean isKids,
            Boolean isSports, Boolean enableImages, Integer imageTypeLimit, List<ImageType> enableImageTypes,
            List<UUID> genreIds, List<ItemFields> fields, Boolean enableUserData, Boolean enableTotalRecordCount)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getRecommendedProgramsRequestCreation(userId, limit, isAiring, hasAired, isSeries, isMovie, isNews,
                isKids, isSports, enableImages, imageTypeLimit, enableImageTypes, genreIds, fields, enableUserData,
                enableTotalRecordCount).toEntity(localVarReturnType);
    }

    /**
     * Gets recommended live tv epgs.
     * 
     * <p>
     * <b>200</b> - Recommended epgs returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId Optional. filter by user id.
     * @param limit Optional. The maximum number of records to return.
     * @param isAiring Optional. Filter by programs that are currently airing, or not.
     * @param hasAired Optional. Filter by programs that have completed airing, or not.
     * @param isSeries Optional. Filter for series.
     * @param isMovie Optional. Filter for movies.
     * @param isNews Optional. Filter for news.
     * @param isKids Optional. Filter for kids.
     * @param isSports Optional. Filter for sports.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param genreIds The genres to return guide information for.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableUserData Optional. include user data.
     * @param enableTotalRecordCount Retrieve total record count.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getRecommendedProgramsWithResponseSpec(UUID userId, Integer limit, Boolean isAiring,
            Boolean hasAired, Boolean isSeries, Boolean isMovie, Boolean isNews, Boolean isKids, Boolean isSports,
            Boolean enableImages, Integer imageTypeLimit, List<ImageType> enableImageTypes, List<UUID> genreIds,
            List<ItemFields> fields, Boolean enableUserData, Boolean enableTotalRecordCount)
            throws WebClientResponseException {
        return getRecommendedProgramsRequestCreation(userId, limit, isAiring, hasAired, isSeries, isMovie, isNews,
                isKids, isSports, enableImages, imageTypeLimit, enableImageTypes, genreIds, fields, enableUserData,
                enableTotalRecordCount);
    }

    /**
     * Gets a live tv recording.
     * 
     * <p>
     * <b>200</b> - Recording returned.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param recordingId Recording id.
     * @param userId Optional. Attach user data.
     * @return BaseItemDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getRecordingRequestCreation(UUID recordingId, UUID userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'recordingId' is set
        if (recordingId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'recordingId' when calling getRecording",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("recordingId", recordingId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return apiClient.invokeAPI("/LiveTv/Recordings/{recordingId}", HttpMethod.GET, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a live tv recording.
     * 
     * <p>
     * <b>200</b> - Recording returned.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param recordingId Recording id.
     * @param userId Optional. Attach user data.
     * @return BaseItemDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDto> getRecording(UUID recordingId, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return getRecordingRequestCreation(recordingId, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a live tv recording.
     * 
     * <p>
     * <b>200</b> - Recording returned.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param recordingId Recording id.
     * @param userId Optional. Attach user data.
     * @return ResponseEntity&lt;BaseItemDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDto>> getRecordingWithHttpInfo(UUID recordingId, UUID userId)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return getRecordingRequestCreation(recordingId, userId).toEntity(localVarReturnType);
    }

    /**
     * Gets a live tv recording.
     * 
     * <p>
     * <b>200</b> - Recording returned.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param recordingId Recording id.
     * @param userId Optional. Attach user data.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getRecordingWithResponseSpec(UUID recordingId, UUID userId) throws WebClientResponseException {
        return getRecordingRequestCreation(recordingId, userId);
    }

    /**
     * Gets recording folders.
     * 
     * <p>
     * <b>200</b> - Recording folders returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId Optional. Filter by user and attach user data.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getRecordingFoldersRequestCreation(UUID userId) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/LiveTv/Recordings/Folders", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets recording folders.
     * 
     * <p>
     * <b>200</b> - Recording folders returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId Optional. Filter by user and attach user data.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getRecordingFolders(UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getRecordingFoldersRequestCreation(userId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets recording folders.
     * 
     * <p>
     * <b>200</b> - Recording folders returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId Optional. Filter by user and attach user data.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getRecordingFoldersWithHttpInfo(UUID userId)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getRecordingFoldersRequestCreation(userId).toEntity(localVarReturnType);
    }

    /**
     * Gets recording folders.
     * 
     * <p>
     * <b>200</b> - Recording folders returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId Optional. Filter by user and attach user data.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getRecordingFoldersWithResponseSpec(UUID userId) throws WebClientResponseException {
        return getRecordingFoldersRequestCreation(userId);
    }

    /**
     * Get recording group.
     * 
     * <p>
     * <b>404</b> - Not Found
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param groupId Group id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     * @deprecated
     */
    @Deprecated
    private ResponseSpec getRecordingGroupRequestCreation(UUID groupId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'groupId' is set
        if (groupId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'groupId' when calling getRecordingGroup",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("groupId", groupId);

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

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/LiveTv/Recordings/Groups/{groupId}", HttpMethod.GET, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Get recording group.
     * 
     * <p>
     * <b>404</b> - Not Found
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param groupId Group id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> getRecordingGroup(UUID groupId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return getRecordingGroupRequestCreation(groupId).bodyToMono(localVarReturnType);
    }

    /**
     * Get recording group.
     * 
     * <p>
     * <b>404</b> - Not Found
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param groupId Group id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> getRecordingGroupWithHttpInfo(UUID groupId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return getRecordingGroupRequestCreation(groupId).toEntity(localVarReturnType);
    }

    /**
     * Get recording group.
     * 
     * <p>
     * <b>404</b> - Not Found
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param groupId Group id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getRecordingGroupWithResponseSpec(UUID groupId) throws WebClientResponseException {
        return getRecordingGroupRequestCreation(groupId);
    }

    /**
     * Gets live tv recording groups.
     * 
     * <p>
     * <b>200</b> - Recording groups returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId Optional. Filter by user and attach user data.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     * @deprecated
     */
    @Deprecated
    private ResponseSpec getRecordingGroupsRequestCreation(UUID userId) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/LiveTv/Recordings/Groups", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets live tv recording groups.
     * 
     * <p>
     * <b>200</b> - Recording groups returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId Optional. Filter by user and attach user data.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getRecordingGroups(UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getRecordingGroupsRequestCreation(userId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets live tv recording groups.
     * 
     * <p>
     * <b>200</b> - Recording groups returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId Optional. Filter by user and attach user data.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getRecordingGroupsWithHttpInfo(UUID userId)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getRecordingGroupsRequestCreation(userId).toEntity(localVarReturnType);
    }

    /**
     * Gets live tv recording groups.
     * 
     * <p>
     * <b>200</b> - Recording groups returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId Optional. Filter by user and attach user data.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getRecordingGroupsWithResponseSpec(UUID userId) throws WebClientResponseException {
        return getRecordingGroupsRequestCreation(userId);
    }

    /**
     * Gets live tv recordings.
     * 
     * <p>
     * <b>200</b> - Live tv recordings returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelId Optional. Filter by channel id.
     * @param userId Optional. Filter by user and attach user data.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param status Optional. Filter by recording status.
     * @param isInProgress Optional. Filter by recordings that are in progress, or not.
     * @param seriesTimerId Optional. Filter by recordings belonging to a series timer.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableUserData Optional. Include user data.
     * @param isMovie Optional. Filter for movies.
     * @param isSeries Optional. Filter for series.
     * @param isKids Optional. Filter for kids.
     * @param isSports Optional. Filter for sports.
     * @param isNews Optional. Filter for news.
     * @param isLibraryItem Optional. Filter for is library item.
     * @param enableTotalRecordCount Optional. Return total record count.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getRecordingsRequestCreation(String channelId, UUID userId, Integer startIndex, Integer limit,
            RecordingStatus status, Boolean isInProgress, String seriesTimerId, Boolean enableImages,
            Integer imageTypeLimit, List<ImageType> enableImageTypes, List<ItemFields> fields, Boolean enableUserData,
            Boolean isMovie, Boolean isSeries, Boolean isKids, Boolean isSports, Boolean isNews, Boolean isLibraryItem,
            Boolean enableTotalRecordCount) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "channelId", channelId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startIndex", startIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "status", status));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isInProgress", isInProgress));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "seriesTimerId", seriesTimerId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "enableImageTypes", enableImageTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isMovie", isMovie));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isSeries", isSeries));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isKids", isKids));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isSports", isSports));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isNews", isNews));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isLibraryItem", isLibraryItem));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableTotalRecordCount", enableTotalRecordCount));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/LiveTv/Recordings", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets live tv recordings.
     * 
     * <p>
     * <b>200</b> - Live tv recordings returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelId Optional. Filter by channel id.
     * @param userId Optional. Filter by user and attach user data.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param status Optional. Filter by recording status.
     * @param isInProgress Optional. Filter by recordings that are in progress, or not.
     * @param seriesTimerId Optional. Filter by recordings belonging to a series timer.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableUserData Optional. Include user data.
     * @param isMovie Optional. Filter for movies.
     * @param isSeries Optional. Filter for series.
     * @param isKids Optional. Filter for kids.
     * @param isSports Optional. Filter for sports.
     * @param isNews Optional. Filter for news.
     * @param isLibraryItem Optional. Filter for is library item.
     * @param enableTotalRecordCount Optional. Return total record count.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getRecordings(String channelId, UUID userId, Integer startIndex, Integer limit,
            RecordingStatus status, Boolean isInProgress, String seriesTimerId, Boolean enableImages,
            Integer imageTypeLimit, List<ImageType> enableImageTypes, List<ItemFields> fields, Boolean enableUserData,
            Boolean isMovie, Boolean isSeries, Boolean isKids, Boolean isSports, Boolean isNews, Boolean isLibraryItem,
            Boolean enableTotalRecordCount) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getRecordingsRequestCreation(channelId, userId, startIndex, limit, status, isInProgress, seriesTimerId,
                enableImages, imageTypeLimit, enableImageTypes, fields, enableUserData, isMovie, isSeries, isKids,
                isSports, isNews, isLibraryItem, enableTotalRecordCount).bodyToMono(localVarReturnType);
    }

    /**
     * Gets live tv recordings.
     * 
     * <p>
     * <b>200</b> - Live tv recordings returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelId Optional. Filter by channel id.
     * @param userId Optional. Filter by user and attach user data.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param status Optional. Filter by recording status.
     * @param isInProgress Optional. Filter by recordings that are in progress, or not.
     * @param seriesTimerId Optional. Filter by recordings belonging to a series timer.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableUserData Optional. Include user data.
     * @param isMovie Optional. Filter for movies.
     * @param isSeries Optional. Filter for series.
     * @param isKids Optional. Filter for kids.
     * @param isSports Optional. Filter for sports.
     * @param isNews Optional. Filter for news.
     * @param isLibraryItem Optional. Filter for is library item.
     * @param enableTotalRecordCount Optional. Return total record count.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getRecordingsWithHttpInfo(String channelId, UUID userId,
            Integer startIndex, Integer limit, RecordingStatus status, Boolean isInProgress, String seriesTimerId,
            Boolean enableImages, Integer imageTypeLimit, List<ImageType> enableImageTypes, List<ItemFields> fields,
            Boolean enableUserData, Boolean isMovie, Boolean isSeries, Boolean isKids, Boolean isSports, Boolean isNews,
            Boolean isLibraryItem, Boolean enableTotalRecordCount) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getRecordingsRequestCreation(channelId, userId, startIndex, limit, status, isInProgress, seriesTimerId,
                enableImages, imageTypeLimit, enableImageTypes, fields, enableUserData, isMovie, isSeries, isKids,
                isSports, isNews, isLibraryItem, enableTotalRecordCount).toEntity(localVarReturnType);
    }

    /**
     * Gets live tv recordings.
     * 
     * <p>
     * <b>200</b> - Live tv recordings returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelId Optional. Filter by channel id.
     * @param userId Optional. Filter by user and attach user data.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param status Optional. Filter by recording status.
     * @param isInProgress Optional. Filter by recordings that are in progress, or not.
     * @param seriesTimerId Optional. Filter by recordings belonging to a series timer.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableUserData Optional. Include user data.
     * @param isMovie Optional. Filter for movies.
     * @param isSeries Optional. Filter for series.
     * @param isKids Optional. Filter for kids.
     * @param isSports Optional. Filter for sports.
     * @param isNews Optional. Filter for news.
     * @param isLibraryItem Optional. Filter for is library item.
     * @param enableTotalRecordCount Optional. Return total record count.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getRecordingsWithResponseSpec(String channelId, UUID userId, Integer startIndex, Integer limit,
            RecordingStatus status, Boolean isInProgress, String seriesTimerId, Boolean enableImages,
            Integer imageTypeLimit, List<ImageType> enableImageTypes, List<ItemFields> fields, Boolean enableUserData,
            Boolean isMovie, Boolean isSeries, Boolean isKids, Boolean isSports, Boolean isNews, Boolean isLibraryItem,
            Boolean enableTotalRecordCount) throws WebClientResponseException {
        return getRecordingsRequestCreation(channelId, userId, startIndex, limit, status, isInProgress, seriesTimerId,
                enableImages, imageTypeLimit, enableImageTypes, fields, enableUserData, isMovie, isSeries, isKids,
                isSports, isNews, isLibraryItem, enableTotalRecordCount);
    }

    /**
     * Gets live tv recording series.
     * 
     * <p>
     * <b>200</b> - Live tv recordings returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelId Optional. Filter by channel id.
     * @param userId Optional. Filter by user and attach user data.
     * @param groupId Optional. Filter by recording group.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param status Optional. Filter by recording status.
     * @param isInProgress Optional. Filter by recordings that are in progress, or not.
     * @param seriesTimerId Optional. Filter by recordings belonging to a series timer.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableUserData Optional. Include user data.
     * @param enableTotalRecordCount Optional. Return total record count.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     * @deprecated
     */
    @Deprecated
    private ResponseSpec getRecordingsSeriesRequestCreation(String channelId, UUID userId, String groupId,
            Integer startIndex, Integer limit, RecordingStatus status, Boolean isInProgress, String seriesTimerId,
            Boolean enableImages, Integer imageTypeLimit, List<ImageType> enableImageTypes, List<ItemFields> fields,
            Boolean enableUserData, Boolean enableTotalRecordCount) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "channelId", channelId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "groupId", groupId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startIndex", startIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "status", status));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isInProgress", isInProgress));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "seriesTimerId", seriesTimerId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "enableImageTypes", enableImageTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableTotalRecordCount", enableTotalRecordCount));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/LiveTv/Recordings/Series", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets live tv recording series.
     * 
     * <p>
     * <b>200</b> - Live tv recordings returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelId Optional. Filter by channel id.
     * @param userId Optional. Filter by user and attach user data.
     * @param groupId Optional. Filter by recording group.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param status Optional. Filter by recording status.
     * @param isInProgress Optional. Filter by recordings that are in progress, or not.
     * @param seriesTimerId Optional. Filter by recordings belonging to a series timer.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableUserData Optional. Include user data.
     * @param enableTotalRecordCount Optional. Return total record count.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getRecordingsSeries(String channelId, UUID userId, String groupId,
            Integer startIndex, Integer limit, RecordingStatus status, Boolean isInProgress, String seriesTimerId,
            Boolean enableImages, Integer imageTypeLimit, List<ImageType> enableImageTypes, List<ItemFields> fields,
            Boolean enableUserData, Boolean enableTotalRecordCount) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getRecordingsSeriesRequestCreation(channelId, userId, groupId, startIndex, limit, status, isInProgress,
                seriesTimerId, enableImages, imageTypeLimit, enableImageTypes, fields, enableUserData,
                enableTotalRecordCount).bodyToMono(localVarReturnType);
    }

    /**
     * Gets live tv recording series.
     * 
     * <p>
     * <b>200</b> - Live tv recordings returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelId Optional. Filter by channel id.
     * @param userId Optional. Filter by user and attach user data.
     * @param groupId Optional. Filter by recording group.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param status Optional. Filter by recording status.
     * @param isInProgress Optional. Filter by recordings that are in progress, or not.
     * @param seriesTimerId Optional. Filter by recordings belonging to a series timer.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableUserData Optional. Include user data.
     * @param enableTotalRecordCount Optional. Return total record count.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getRecordingsSeriesWithHttpInfo(String channelId, UUID userId,
            String groupId, Integer startIndex, Integer limit, RecordingStatus status, Boolean isInProgress,
            String seriesTimerId, Boolean enableImages, Integer imageTypeLimit, List<ImageType> enableImageTypes,
            List<ItemFields> fields, Boolean enableUserData, Boolean enableTotalRecordCount)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getRecordingsSeriesRequestCreation(channelId, userId, groupId, startIndex, limit, status, isInProgress,
                seriesTimerId, enableImages, imageTypeLimit, enableImageTypes, fields, enableUserData,
                enableTotalRecordCount).toEntity(localVarReturnType);
    }

    /**
     * Gets live tv recording series.
     * 
     * <p>
     * <b>200</b> - Live tv recordings returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelId Optional. Filter by channel id.
     * @param userId Optional. Filter by user and attach user data.
     * @param groupId Optional. Filter by recording group.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param status Optional. Filter by recording status.
     * @param isInProgress Optional. Filter by recordings that are in progress, or not.
     * @param seriesTimerId Optional. Filter by recordings belonging to a series timer.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableUserData Optional. Include user data.
     * @param enableTotalRecordCount Optional. Return total record count.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getRecordingsSeriesWithResponseSpec(String channelId, UUID userId, String groupId,
            Integer startIndex, Integer limit, RecordingStatus status, Boolean isInProgress, String seriesTimerId,
            Boolean enableImages, Integer imageTypeLimit, List<ImageType> enableImageTypes, List<ItemFields> fields,
            Boolean enableUserData, Boolean enableTotalRecordCount) throws WebClientResponseException {
        return getRecordingsSeriesRequestCreation(channelId, userId, groupId, startIndex, limit, status, isInProgress,
                seriesTimerId, enableImages, imageTypeLimit, enableImageTypes, fields, enableUserData,
                enableTotalRecordCount);
    }

    /**
     * Gets available countries.
     * 
     * <p>
     * <b>200</b> - Available countries returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getSchedulesDirectCountriesRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI("/LiveTv/ListingProviders/SchedulesDirect/Countries", HttpMethod.GET, pathParams,
                queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets available countries.
     * 
     * <p>
     * <b>200</b> - Available countries returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getSchedulesDirectCountries() throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getSchedulesDirectCountriesRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Gets available countries.
     * 
     * <p>
     * <b>200</b> - Available countries returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getSchedulesDirectCountriesWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getSchedulesDirectCountriesRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Gets available countries.
     * 
     * <p>
     * <b>200</b> - Available countries returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getSchedulesDirectCountriesWithResponseSpec() throws WebClientResponseException {
        return getSchedulesDirectCountriesRequestCreation();
    }

    /**
     * Gets a live tv series timer.
     * 
     * <p>
     * <b>200</b> - Series timer returned.
     * <p>
     * <b>404</b> - Series timer not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerId Timer id.
     * @return SeriesTimerInfoDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getSeriesTimerRequestCreation(String timerId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'timerId' is set
        if (timerId == null) {
            throw new WebClientResponseException("Missing the required parameter 'timerId' when calling getSeriesTimer",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("timerId", timerId);

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

        ParameterizedTypeReference<SeriesTimerInfoDto> localVarReturnType = new ParameterizedTypeReference<SeriesTimerInfoDto>() {
        };
        return apiClient.invokeAPI("/LiveTv/SeriesTimers/{timerId}", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets a live tv series timer.
     * 
     * <p>
     * <b>200</b> - Series timer returned.
     * <p>
     * <b>404</b> - Series timer not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerId Timer id.
     * @return SeriesTimerInfoDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<SeriesTimerInfoDto> getSeriesTimer(String timerId) throws WebClientResponseException {
        ParameterizedTypeReference<SeriesTimerInfoDto> localVarReturnType = new ParameterizedTypeReference<SeriesTimerInfoDto>() {
        };
        return getSeriesTimerRequestCreation(timerId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a live tv series timer.
     * 
     * <p>
     * <b>200</b> - Series timer returned.
     * <p>
     * <b>404</b> - Series timer not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerId Timer id.
     * @return ResponseEntity&lt;SeriesTimerInfoDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<SeriesTimerInfoDto>> getSeriesTimerWithHttpInfo(String timerId)
            throws WebClientResponseException {
        ParameterizedTypeReference<SeriesTimerInfoDto> localVarReturnType = new ParameterizedTypeReference<SeriesTimerInfoDto>() {
        };
        return getSeriesTimerRequestCreation(timerId).toEntity(localVarReturnType);
    }

    /**
     * Gets a live tv series timer.
     * 
     * <p>
     * <b>200</b> - Series timer returned.
     * <p>
     * <b>404</b> - Series timer not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerId Timer id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getSeriesTimerWithResponseSpec(String timerId) throws WebClientResponseException {
        return getSeriesTimerRequestCreation(timerId);
    }

    /**
     * Gets live tv series timers.
     * 
     * <p>
     * <b>200</b> - Timers returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sortBy Optional. Sort by SortName or Priority.
     * @param sortOrder Optional. Sort in Ascending or Descending order.
     * @return SeriesTimerInfoDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getSeriesTimersRequestCreation(String sortBy, SortOrder sortOrder)
            throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "sortBy", sortBy));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "sortOrder", sortOrder));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<SeriesTimerInfoDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<SeriesTimerInfoDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/LiveTv/SeriesTimers", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets live tv series timers.
     * 
     * <p>
     * <b>200</b> - Timers returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sortBy Optional. Sort by SortName or Priority.
     * @param sortOrder Optional. Sort in Ascending or Descending order.
     * @return SeriesTimerInfoDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<SeriesTimerInfoDtoQueryResult> getSeriesTimers(String sortBy, SortOrder sortOrder)
            throws WebClientResponseException {
        ParameterizedTypeReference<SeriesTimerInfoDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<SeriesTimerInfoDtoQueryResult>() {
        };
        return getSeriesTimersRequestCreation(sortBy, sortOrder).bodyToMono(localVarReturnType);
    }

    /**
     * Gets live tv series timers.
     * 
     * <p>
     * <b>200</b> - Timers returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sortBy Optional. Sort by SortName or Priority.
     * @param sortOrder Optional. Sort in Ascending or Descending order.
     * @return ResponseEntity&lt;SeriesTimerInfoDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<SeriesTimerInfoDtoQueryResult>> getSeriesTimersWithHttpInfo(String sortBy,
            SortOrder sortOrder) throws WebClientResponseException {
        ParameterizedTypeReference<SeriesTimerInfoDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<SeriesTimerInfoDtoQueryResult>() {
        };
        return getSeriesTimersRequestCreation(sortBy, sortOrder).toEntity(localVarReturnType);
    }

    /**
     * Gets live tv series timers.
     * 
     * <p>
     * <b>200</b> - Timers returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sortBy Optional. Sort by SortName or Priority.
     * @param sortOrder Optional. Sort in Ascending or Descending order.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getSeriesTimersWithResponseSpec(String sortBy, SortOrder sortOrder)
            throws WebClientResponseException {
        return getSeriesTimersRequestCreation(sortBy, sortOrder);
    }

    /**
     * Gets a timer.
     * 
     * <p>
     * <b>200</b> - Timer returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerId Timer id.
     * @return TimerInfoDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getTimerRequestCreation(String timerId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'timerId' is set
        if (timerId == null) {
            throw new WebClientResponseException("Missing the required parameter 'timerId' when calling getTimer",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("timerId", timerId);

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

        ParameterizedTypeReference<TimerInfoDto> localVarReturnType = new ParameterizedTypeReference<TimerInfoDto>() {
        };
        return apiClient.invokeAPI("/LiveTv/Timers/{timerId}", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets a timer.
     * 
     * <p>
     * <b>200</b> - Timer returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerId Timer id.
     * @return TimerInfoDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<TimerInfoDto> getTimer(String timerId) throws WebClientResponseException {
        ParameterizedTypeReference<TimerInfoDto> localVarReturnType = new ParameterizedTypeReference<TimerInfoDto>() {
        };
        return getTimerRequestCreation(timerId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a timer.
     * 
     * <p>
     * <b>200</b> - Timer returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerId Timer id.
     * @return ResponseEntity&lt;TimerInfoDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<TimerInfoDto>> getTimerWithHttpInfo(String timerId) throws WebClientResponseException {
        ParameterizedTypeReference<TimerInfoDto> localVarReturnType = new ParameterizedTypeReference<TimerInfoDto>() {
        };
        return getTimerRequestCreation(timerId).toEntity(localVarReturnType);
    }

    /**
     * Gets a timer.
     * 
     * <p>
     * <b>200</b> - Timer returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerId Timer id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getTimerWithResponseSpec(String timerId) throws WebClientResponseException {
        return getTimerRequestCreation(timerId);
    }

    /**
     * Gets the live tv timers.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelId Optional. Filter by channel id.
     * @param seriesTimerId Optional. Filter by timers belonging to a series timer.
     * @param isActive Optional. Filter by timers that are active.
     * @param isScheduled Optional. Filter by timers that are scheduled.
     * @return TimerInfoDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getTimersRequestCreation(String channelId, String seriesTimerId, Boolean isActive,
            Boolean isScheduled) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "channelId", channelId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "seriesTimerId", seriesTimerId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isActive", isActive));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isScheduled", isScheduled));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<TimerInfoDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<TimerInfoDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/LiveTv/Timers", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets the live tv timers.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelId Optional. Filter by channel id.
     * @param seriesTimerId Optional. Filter by timers belonging to a series timer.
     * @param isActive Optional. Filter by timers that are active.
     * @param isScheduled Optional. Filter by timers that are scheduled.
     * @return TimerInfoDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<TimerInfoDtoQueryResult> getTimers(String channelId, String seriesTimerId, Boolean isActive,
            Boolean isScheduled) throws WebClientResponseException {
        ParameterizedTypeReference<TimerInfoDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<TimerInfoDtoQueryResult>() {
        };
        return getTimersRequestCreation(channelId, seriesTimerId, isActive, isScheduled).bodyToMono(localVarReturnType);
    }

    /**
     * Gets the live tv timers.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelId Optional. Filter by channel id.
     * @param seriesTimerId Optional. Filter by timers belonging to a series timer.
     * @param isActive Optional. Filter by timers that are active.
     * @param isScheduled Optional. Filter by timers that are scheduled.
     * @return ResponseEntity&lt;TimerInfoDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<TimerInfoDtoQueryResult>> getTimersWithHttpInfo(String channelId, String seriesTimerId,
            Boolean isActive, Boolean isScheduled) throws WebClientResponseException {
        ParameterizedTypeReference<TimerInfoDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<TimerInfoDtoQueryResult>() {
        };
        return getTimersRequestCreation(channelId, seriesTimerId, isActive, isScheduled).toEntity(localVarReturnType);
    }

    /**
     * Gets the live tv timers.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelId Optional. Filter by channel id.
     * @param seriesTimerId Optional. Filter by timers belonging to a series timer.
     * @param isActive Optional. Filter by timers that are active.
     * @param isScheduled Optional. Filter by timers that are scheduled.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getTimersWithResponseSpec(String channelId, String seriesTimerId, Boolean isActive,
            Boolean isScheduled) throws WebClientResponseException {
        return getTimersRequestCreation(channelId, seriesTimerId, isActive, isScheduled);
    }

    /**
     * Get tuner host types.
     * 
     * <p>
     * <b>200</b> - Tuner host types returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;NameIdPair&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getTunerHostTypesRequestCreation() throws WebClientResponseException {
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

        ParameterizedTypeReference<NameIdPair> localVarReturnType = new ParameterizedTypeReference<NameIdPair>() {
        };
        return apiClient.invokeAPI("/LiveTv/TunerHosts/Types", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Get tuner host types.
     * 
     * <p>
     * <b>200</b> - Tuner host types returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;NameIdPair&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<NameIdPair> getTunerHostTypes() throws WebClientResponseException {
        ParameterizedTypeReference<NameIdPair> localVarReturnType = new ParameterizedTypeReference<NameIdPair>() {
        };
        return getTunerHostTypesRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Get tuner host types.
     * 
     * <p>
     * <b>200</b> - Tuner host types returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseEntity&lt;List&lt;NameIdPair&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<NameIdPair>>> getTunerHostTypesWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<NameIdPair> localVarReturnType = new ParameterizedTypeReference<NameIdPair>() {
        };
        return getTunerHostTypesRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Get tuner host types.
     * 
     * <p>
     * <b>200</b> - Tuner host types returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getTunerHostTypesWithResponseSpec() throws WebClientResponseException {
        return getTunerHostTypesRequestCreation();
    }

    /**
     * Resets a tv tuner.
     * 
     * <p>
     * <b>204</b> - Tuner reset.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param tunerId Tuner id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec resetTunerRequestCreation(String tunerId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'tunerId' is set
        if (tunerId == null) {
            throw new WebClientResponseException("Missing the required parameter 'tunerId' when calling resetTuner",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("tunerId", tunerId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/LiveTv/Tuners/{tunerId}/Reset", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Resets a tv tuner.
     * 
     * <p>
     * <b>204</b> - Tuner reset.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param tunerId Tuner id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> resetTuner(String tunerId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return resetTunerRequestCreation(tunerId).bodyToMono(localVarReturnType);
    }

    /**
     * Resets a tv tuner.
     * 
     * <p>
     * <b>204</b> - Tuner reset.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param tunerId Tuner id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> resetTunerWithHttpInfo(String tunerId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return resetTunerRequestCreation(tunerId).toEntity(localVarReturnType);
    }

    /**
     * Resets a tv tuner.
     * 
     * <p>
     * <b>204</b> - Tuner reset.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param tunerId Tuner id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec resetTunerWithResponseSpec(String tunerId) throws WebClientResponseException {
        return resetTunerRequestCreation(tunerId);
    }

    /**
     * Set channel mappings.
     * 
     * <p>
     * <b>200</b> - Created channel mapping returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param setChannelMappingDto The set channel mapping dto.
     * @return TunerChannelMapping
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec setChannelMappingRequestCreation(SetChannelMappingDto setChannelMappingDto)
            throws WebClientResponseException {
        Object postBody = setChannelMappingDto;
        // verify the required parameter 'setChannelMappingDto' is set
        if (setChannelMappingDto == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'setChannelMappingDto' when calling setChannelMapping",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<TunerChannelMapping> localVarReturnType = new ParameterizedTypeReference<TunerChannelMapping>() {
        };
        return apiClient.invokeAPI("/LiveTv/ChannelMappings", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Set channel mappings.
     * 
     * <p>
     * <b>200</b> - Created channel mapping returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param setChannelMappingDto The set channel mapping dto.
     * @return TunerChannelMapping
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<TunerChannelMapping> setChannelMapping(SetChannelMappingDto setChannelMappingDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<TunerChannelMapping> localVarReturnType = new ParameterizedTypeReference<TunerChannelMapping>() {
        };
        return setChannelMappingRequestCreation(setChannelMappingDto).bodyToMono(localVarReturnType);
    }

    /**
     * Set channel mappings.
     * 
     * <p>
     * <b>200</b> - Created channel mapping returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param setChannelMappingDto The set channel mapping dto.
     * @return ResponseEntity&lt;TunerChannelMapping&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<TunerChannelMapping>> setChannelMappingWithHttpInfo(
            SetChannelMappingDto setChannelMappingDto) throws WebClientResponseException {
        ParameterizedTypeReference<TunerChannelMapping> localVarReturnType = new ParameterizedTypeReference<TunerChannelMapping>() {
        };
        return setChannelMappingRequestCreation(setChannelMappingDto).toEntity(localVarReturnType);
    }

    /**
     * Set channel mappings.
     * 
     * <p>
     * <b>200</b> - Created channel mapping returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param setChannelMappingDto The set channel mapping dto.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec setChannelMappingWithResponseSpec(SetChannelMappingDto setChannelMappingDto)
            throws WebClientResponseException {
        return setChannelMappingRequestCreation(setChannelMappingDto);
    }

    /**
     * Updates a live tv series timer.
     * 
     * <p>
     * <b>204</b> - Series timer updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerId Timer id.
     * @param seriesTimerInfoDto New series timer info.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateSeriesTimerRequestCreation(String timerId, SeriesTimerInfoDto seriesTimerInfoDto)
            throws WebClientResponseException {
        Object postBody = seriesTimerInfoDto;
        // verify the required parameter 'timerId' is set
        if (timerId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'timerId' when calling updateSeriesTimer",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("timerId", timerId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/LiveTv/SeriesTimers/{timerId}", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Updates a live tv series timer.
     * 
     * <p>
     * <b>204</b> - Series timer updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerId Timer id.
     * @param seriesTimerInfoDto New series timer info.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> updateSeriesTimer(String timerId, SeriesTimerInfoDto seriesTimerInfoDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return updateSeriesTimerRequestCreation(timerId, seriesTimerInfoDto).bodyToMono(localVarReturnType);
    }

    /**
     * Updates a live tv series timer.
     * 
     * <p>
     * <b>204</b> - Series timer updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerId Timer id.
     * @param seriesTimerInfoDto New series timer info.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> updateSeriesTimerWithHttpInfo(String timerId,
            SeriesTimerInfoDto seriesTimerInfoDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return updateSeriesTimerRequestCreation(timerId, seriesTimerInfoDto).toEntity(localVarReturnType);
    }

    /**
     * Updates a live tv series timer.
     * 
     * <p>
     * <b>204</b> - Series timer updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerId Timer id.
     * @param seriesTimerInfoDto New series timer info.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateSeriesTimerWithResponseSpec(String timerId, SeriesTimerInfoDto seriesTimerInfoDto)
            throws WebClientResponseException {
        return updateSeriesTimerRequestCreation(timerId, seriesTimerInfoDto);
    }

    /**
     * Updates a live tv timer.
     * 
     * <p>
     * <b>204</b> - Timer updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerId Timer id.
     * @param timerInfoDto New timer info.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateTimerRequestCreation(String timerId, TimerInfoDto timerInfoDto)
            throws WebClientResponseException {
        Object postBody = timerInfoDto;
        // verify the required parameter 'timerId' is set
        if (timerId == null) {
            throw new WebClientResponseException("Missing the required parameter 'timerId' when calling updateTimer",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("timerId", timerId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/LiveTv/Timers/{timerId}", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Updates a live tv timer.
     * 
     * <p>
     * <b>204</b> - Timer updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerId Timer id.
     * @param timerInfoDto New timer info.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> updateTimer(String timerId, TimerInfoDto timerInfoDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return updateTimerRequestCreation(timerId, timerInfoDto).bodyToMono(localVarReturnType);
    }

    /**
     * Updates a live tv timer.
     * 
     * <p>
     * <b>204</b> - Timer updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerId Timer id.
     * @param timerInfoDto New timer info.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> updateTimerWithHttpInfo(String timerId, TimerInfoDto timerInfoDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return updateTimerRequestCreation(timerId, timerInfoDto).toEntity(localVarReturnType);
    }

    /**
     * Updates a live tv timer.
     * 
     * <p>
     * <b>204</b> - Timer updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param timerId Timer id.
     * @param timerInfoDto New timer info.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateTimerWithResponseSpec(String timerId, TimerInfoDto timerInfoDto)
            throws WebClientResponseException {
        return updateTimerRequestCreation(timerId, timerInfoDto);
    }
}
