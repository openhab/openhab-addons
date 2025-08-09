package org.openhab.binding.jellyfin.internal.api.generated.current;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.GenericType;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.Pair;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ChannelMappingOptionsDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ChannelType;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.GetProgramsDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.GuideInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ImageType;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemFields;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemSortBy;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ListingsProviderInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.LiveTvInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.NameIdPair;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.RecordingStatus;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SeriesTimerInfoDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SeriesTimerInfoDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SetChannelMappingDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SortOrder;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.TimerInfoDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.TimerInfoDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.TunerChannelMapping;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.TunerHostInfo;

public class LiveTvApi {
    private ApiClient apiClient;

    public LiveTvApi() {
        this(Configuration.getDefaultApiClient());
    }

    public LiveTvApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Get the API client
     *
     * @return API client
     */
    public ApiClient getApiClient() {
        return apiClient;
    }

    /**
     * Set the API client
     *
     * @param apiClient an instance of API client
     */
    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Adds a listings provider.
     * 
     * @param pw Password. (optional)
     * @param validateListings Validate listings. (optional, default to false)
     * @param validateLogin Validate login. (optional, default to false)
     * @param listingsProviderInfo New listings info. (optional)
     * @return ListingsProviderInfo
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Created listings provider returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ListingsProviderInfo addListingProvider(@org.eclipse.jdt.annotation.NonNull String pw,
            @org.eclipse.jdt.annotation.NonNull Boolean validateListings,
            @org.eclipse.jdt.annotation.NonNull Boolean validateLogin,
            @org.eclipse.jdt.annotation.NonNull ListingsProviderInfo listingsProviderInfo) throws ApiException {
        return addListingProviderWithHttpInfo(pw, validateListings, validateLogin, listingsProviderInfo).getData();
    }

    /**
     * Adds a listings provider.
     * 
     * @param pw Password. (optional)
     * @param validateListings Validate listings. (optional, default to false)
     * @param validateLogin Validate login. (optional, default to false)
     * @param listingsProviderInfo New listings info. (optional)
     * @return ApiResponse&lt;ListingsProviderInfo&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Created listings provider returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<ListingsProviderInfo> addListingProviderWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String pw, @org.eclipse.jdt.annotation.NonNull Boolean validateListings,
            @org.eclipse.jdt.annotation.NonNull Boolean validateLogin,
            @org.eclipse.jdt.annotation.NonNull ListingsProviderInfo listingsProviderInfo) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "pw", pw));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "validateListings", validateListings));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "validateLogin", validateLogin));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<ListingsProviderInfo> localVarReturnType = new GenericType<ListingsProviderInfo>() {
        };
        return apiClient.invokeAPI("LiveTvApi.addListingProvider", "/LiveTv/ListingProviders", "POST",
                localVarQueryParams, listingsProviderInfo, new LinkedHashMap<>(), new LinkedHashMap<>(),
                new LinkedHashMap<>(), localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType,
                false);
    }

    /**
     * Adds a tuner host.
     * 
     * @param tunerHostInfo New tuner host. (optional)
     * @return TunerHostInfo
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Created tuner host returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public TunerHostInfo addTunerHost(@org.eclipse.jdt.annotation.NonNull TunerHostInfo tunerHostInfo)
            throws ApiException {
        return addTunerHostWithHttpInfo(tunerHostInfo).getData();
    }

    /**
     * Adds a tuner host.
     * 
     * @param tunerHostInfo New tuner host. (optional)
     * @return ApiResponse&lt;TunerHostInfo&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Created tuner host returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<TunerHostInfo> addTunerHostWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull TunerHostInfo tunerHostInfo) throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<TunerHostInfo> localVarReturnType = new GenericType<TunerHostInfo>() {
        };
        return apiClient.invokeAPI("LiveTvApi.addTunerHost", "/LiveTv/TunerHosts", "POST", new ArrayList<>(),
                tunerHostInfo, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Cancels a live tv series timer.
     * 
     * @param timerId Timer id. (required)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Timer cancelled.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void cancelSeriesTimer(@org.eclipse.jdt.annotation.Nullable String timerId) throws ApiException {
        cancelSeriesTimerWithHttpInfo(timerId);
    }

    /**
     * Cancels a live tv series timer.
     * 
     * @param timerId Timer id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Timer cancelled.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> cancelSeriesTimerWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String timerId)
            throws ApiException {
        // Check required parameters
        if (timerId == null) {
            throw new ApiException(400, "Missing the required parameter 'timerId' when calling cancelSeriesTimer");
        }

        // Path parameters
        String localVarPath = "/LiveTv/SeriesTimers/{timerId}".replaceAll("\\{timerId}",
                apiClient.escapeString(timerId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("LiveTvApi.cancelSeriesTimer", localVarPath, "DELETE", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Cancels a live tv timer.
     * 
     * @param timerId Timer id. (required)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Timer deleted.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void cancelTimer(@org.eclipse.jdt.annotation.Nullable String timerId) throws ApiException {
        cancelTimerWithHttpInfo(timerId);
    }

    /**
     * Cancels a live tv timer.
     * 
     * @param timerId Timer id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Timer deleted.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> cancelTimerWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String timerId)
            throws ApiException {
        // Check required parameters
        if (timerId == null) {
            throw new ApiException(400, "Missing the required parameter 'timerId' when calling cancelTimer");
        }

        // Path parameters
        String localVarPath = "/LiveTv/Timers/{timerId}".replaceAll("\\{timerId}",
                apiClient.escapeString(timerId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("LiveTvApi.cancelTimer", localVarPath, "DELETE", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Creates a live tv series timer.
     * 
     * @param seriesTimerInfoDto New series timer info. (optional)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Series timer info created.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void createSeriesTimer(@org.eclipse.jdt.annotation.NonNull SeriesTimerInfoDto seriesTimerInfoDto)
            throws ApiException {
        createSeriesTimerWithHttpInfo(seriesTimerInfoDto);
    }

    /**
     * Creates a live tv series timer.
     * 
     * @param seriesTimerInfoDto New series timer info. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Series timer info created.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> createSeriesTimerWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull SeriesTimerInfoDto seriesTimerInfoDto) throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("LiveTvApi.createSeriesTimer", "/LiveTv/SeriesTimers", "POST", new ArrayList<>(),
                seriesTimerInfoDto, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Creates a live tv timer.
     * 
     * @param timerInfoDto New timer info. (optional)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Timer created.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void createTimer(@org.eclipse.jdt.annotation.NonNull TimerInfoDto timerInfoDto) throws ApiException {
        createTimerWithHttpInfo(timerInfoDto);
    }

    /**
     * Creates a live tv timer.
     * 
     * @param timerInfoDto New timer info. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Timer created.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> createTimerWithHttpInfo(@org.eclipse.jdt.annotation.NonNull TimerInfoDto timerInfoDto)
            throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("LiveTvApi.createTimer", "/LiveTv/Timers", "POST", new ArrayList<>(), timerInfoDto,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Delete listing provider.
     * 
     * @param id Listing provider id. (optional)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Listing provider deleted.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void deleteListingProvider(@org.eclipse.jdt.annotation.NonNull String id) throws ApiException {
        deleteListingProviderWithHttpInfo(id);
    }

    /**
     * Delete listing provider.
     * 
     * @param id Listing provider id. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Listing provider deleted.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> deleteListingProviderWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String id)
            throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "id", id));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("LiveTvApi.deleteListingProvider", "/LiveTv/ListingProviders", "DELETE",
                localVarQueryParams, null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Deletes a live tv recording.
     * 
     * @param recordingId Recording id. (required)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Recording deleted.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void deleteRecording(@org.eclipse.jdt.annotation.Nullable UUID recordingId) throws ApiException {
        deleteRecordingWithHttpInfo(recordingId);
    }

    /**
     * Deletes a live tv recording.
     * 
     * @param recordingId Recording id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Recording deleted.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> deleteRecordingWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID recordingId)
            throws ApiException {
        // Check required parameters
        if (recordingId == null) {
            throw new ApiException(400, "Missing the required parameter 'recordingId' when calling deleteRecording");
        }

        // Path parameters
        String localVarPath = "/LiveTv/Recordings/{recordingId}".replaceAll("\\{recordingId}",
                apiClient.escapeString(recordingId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("LiveTvApi.deleteRecording", localVarPath, "DELETE", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Deletes a tuner host.
     * 
     * @param id Tuner host id. (optional)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Tuner host deleted.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void deleteTunerHost(@org.eclipse.jdt.annotation.NonNull String id) throws ApiException {
        deleteTunerHostWithHttpInfo(id);
    }

    /**
     * Deletes a tuner host.
     * 
     * @param id Tuner host id. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Tuner host deleted.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> deleteTunerHostWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String id)
            throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "id", id));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("LiveTvApi.deleteTunerHost", "/LiveTv/TunerHosts", "DELETE", localVarQueryParams,
                null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Discover tuners.
     * 
     * @param newDevicesOnly Only discover new tuners. (optional, default to false)
     * @return List&lt;TunerHostInfo&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Tuners returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public List<TunerHostInfo> discoverTuners(@org.eclipse.jdt.annotation.NonNull Boolean newDevicesOnly)
            throws ApiException {
        return discoverTunersWithHttpInfo(newDevicesOnly).getData();
    }

    /**
     * Discover tuners.
     * 
     * @param newDevicesOnly Only discover new tuners. (optional, default to false)
     * @return ApiResponse&lt;List&lt;TunerHostInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Tuners returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<List<TunerHostInfo>> discoverTunersWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull Boolean newDevicesOnly) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(
                apiClient.parameterToPairs("", "newDevicesOnly", newDevicesOnly));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<List<TunerHostInfo>> localVarReturnType = new GenericType<List<TunerHostInfo>>() {
        };
        return apiClient.invokeAPI("LiveTvApi.discoverTuners", "/LiveTv/Tuners/Discover", "GET", localVarQueryParams,
                null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Discover tuners.
     * 
     * @param newDevicesOnly Only discover new tuners. (optional, default to false)
     * @return List&lt;TunerHostInfo&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Tuners returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public List<TunerHostInfo> discvoverTuners(@org.eclipse.jdt.annotation.NonNull Boolean newDevicesOnly)
            throws ApiException {
        return discvoverTunersWithHttpInfo(newDevicesOnly).getData();
    }

    /**
     * Discover tuners.
     * 
     * @param newDevicesOnly Only discover new tuners. (optional, default to false)
     * @return ApiResponse&lt;List&lt;TunerHostInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Tuners returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<List<TunerHostInfo>> discvoverTunersWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull Boolean newDevicesOnly) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(
                apiClient.parameterToPairs("", "newDevicesOnly", newDevicesOnly));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<List<TunerHostInfo>> localVarReturnType = new GenericType<List<TunerHostInfo>>() {
        };
        return apiClient.invokeAPI("LiveTvApi.discvoverTuners", "/LiveTv/Tuners/Discvover", "GET", localVarQueryParams,
                null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets a live tv channel.
     * 
     * @param channelId Channel id. (required)
     * @param userId Optional. Attach user data. (optional)
     * @return BaseItemDto
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Live tv channel returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public BaseItemDto getChannel(@org.eclipse.jdt.annotation.Nullable UUID channelId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        return getChannelWithHttpInfo(channelId, userId).getData();
    }

    /**
     * Gets a live tv channel.
     * 
     * @param channelId Channel id. (required)
     * @param userId Optional. Attach user data. (optional)
     * @return ApiResponse&lt;BaseItemDto&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Live tv channel returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<BaseItemDto> getChannelWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID channelId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        // Check required parameters
        if (channelId == null) {
            throw new ApiException(400, "Missing the required parameter 'channelId' when calling getChannel");
        }

        // Path parameters
        String localVarPath = "/LiveTv/Channels/{channelId}".replaceAll("\\{channelId}",
                apiClient.escapeString(channelId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<BaseItemDto> localVarReturnType = new GenericType<BaseItemDto>() {
        };
        return apiClient.invokeAPI("LiveTvApi.getChannel", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Get channel mapping options.
     * 
     * @param providerId Provider id. (optional)
     * @return ChannelMappingOptionsDto
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Channel mapping options returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ChannelMappingOptionsDto getChannelMappingOptions(@org.eclipse.jdt.annotation.NonNull String providerId)
            throws ApiException {
        return getChannelMappingOptionsWithHttpInfo(providerId).getData();
    }

    /**
     * Get channel mapping options.
     * 
     * @param providerId Provider id. (optional)
     * @return ApiResponse&lt;ChannelMappingOptionsDto&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Channel mapping options returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<ChannelMappingOptionsDto> getChannelMappingOptionsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String providerId) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "providerId", providerId));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<ChannelMappingOptionsDto> localVarReturnType = new GenericType<ChannelMappingOptionsDto>() {
        };
        return apiClient.invokeAPI("LiveTvApi.getChannelMappingOptions", "/LiveTv/ChannelMappingOptions", "GET",
                localVarQueryParams, null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets default listings provider info.
     * 
     * @return ListingsProviderInfo
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Default listings provider info returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ListingsProviderInfo getDefaultListingProvider() throws ApiException {
        return getDefaultListingProviderWithHttpInfo().getData();
    }

    /**
     * Gets default listings provider info.
     * 
     * @return ApiResponse&lt;ListingsProviderInfo&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Default listings provider info returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<ListingsProviderInfo> getDefaultListingProviderWithHttpInfo() throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<ListingsProviderInfo> localVarReturnType = new GenericType<ListingsProviderInfo>() {
        };
        return apiClient.invokeAPI("LiveTvApi.getDefaultListingProvider", "/LiveTv/ListingProviders/Default", "GET",
                new ArrayList<>(), null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets the default values for a new timer.
     * 
     * @param programId Optional. To attach default values based on a program. (optional)
     * @return SeriesTimerInfoDto
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Default values returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public SeriesTimerInfoDto getDefaultTimer(@org.eclipse.jdt.annotation.NonNull String programId)
            throws ApiException {
        return getDefaultTimerWithHttpInfo(programId).getData();
    }

    /**
     * Gets the default values for a new timer.
     * 
     * @param programId Optional. To attach default values based on a program. (optional)
     * @return ApiResponse&lt;SeriesTimerInfoDto&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Default values returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<SeriesTimerInfoDto> getDefaultTimerWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String programId) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "programId", programId));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<SeriesTimerInfoDto> localVarReturnType = new GenericType<SeriesTimerInfoDto>() {
        };
        return apiClient.invokeAPI("LiveTvApi.getDefaultTimer", "/LiveTv/Timers/Defaults", "GET", localVarQueryParams,
                null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Get guid info.
     * 
     * @return GuideInfo
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Guid info returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public GuideInfo getGuideInfo() throws ApiException {
        return getGuideInfoWithHttpInfo().getData();
    }

    /**
     * Get guid info.
     * 
     * @return ApiResponse&lt;GuideInfo&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Guid info returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<GuideInfo> getGuideInfoWithHttpInfo() throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<GuideInfo> localVarReturnType = new GenericType<GuideInfo>() {
        };
        return apiClient.invokeAPI("LiveTvApi.getGuideInfo", "/LiveTv/GuideInfo", "GET", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets available lineups.
     * 
     * @param id Provider id. (optional)
     * @param type Provider type. (optional)
     * @param location Location. (optional)
     * @param country Country. (optional)
     * @return List&lt;NameIdPair&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Available lineups returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public List<NameIdPair> getLineups(@org.eclipse.jdt.annotation.NonNull String id,
            @org.eclipse.jdt.annotation.NonNull String type, @org.eclipse.jdt.annotation.NonNull String location,
            @org.eclipse.jdt.annotation.NonNull String country) throws ApiException {
        return getLineupsWithHttpInfo(id, type, location, country).getData();
    }

    /**
     * Gets available lineups.
     * 
     * @param id Provider id. (optional)
     * @param type Provider type. (optional)
     * @param location Location. (optional)
     * @param country Country. (optional)
     * @return ApiResponse&lt;List&lt;NameIdPair&gt;&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Available lineups returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<List<NameIdPair>> getLineupsWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String id,
            @org.eclipse.jdt.annotation.NonNull String type, @org.eclipse.jdt.annotation.NonNull String location,
            @org.eclipse.jdt.annotation.NonNull String country) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "id", id));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "type", type));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "location", location));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "country", country));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<List<NameIdPair>> localVarReturnType = new GenericType<List<NameIdPair>>() {
        };
        return apiClient.invokeAPI("LiveTvApi.getLineups", "/LiveTv/ListingProviders/Lineups", "GET",
                localVarQueryParams, null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets a live tv recording stream.
     * 
     * @param recordingId Recording id. (required)
     * @return File
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Recording stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Recording not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File getLiveRecordingFile(@org.eclipse.jdt.annotation.Nullable String recordingId) throws ApiException {
        return getLiveRecordingFileWithHttpInfo(recordingId).getData();
    }

    /**
     * Gets a live tv recording stream.
     * 
     * @param recordingId Recording id. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Recording stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Recording not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> getLiveRecordingFileWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String recordingId)
            throws ApiException {
        // Check required parameters
        if (recordingId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'recordingId' when calling getLiveRecordingFile");
        }

        // Path parameters
        String localVarPath = "/LiveTv/LiveRecordings/{recordingId}/stream".replaceAll("\\{recordingId}",
                apiClient.escapeString(recordingId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("video/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("LiveTvApi.getLiveRecordingFile", localVarPath, "GET", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Gets a live tv channel stream.
     * 
     * @param streamId Stream id. (required)
     * @param container Container type. (required)
     * @return File
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Stream not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File getLiveStreamFile(@org.eclipse.jdt.annotation.Nullable String streamId,
            @org.eclipse.jdt.annotation.Nullable String container) throws ApiException {
        return getLiveStreamFileWithHttpInfo(streamId, container).getData();
    }

    /**
     * Gets a live tv channel stream.
     * 
     * @param streamId Stream id. (required)
     * @param container Container type. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Stream not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> getLiveStreamFileWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String streamId,
            @org.eclipse.jdt.annotation.Nullable String container) throws ApiException {
        // Check required parameters
        if (streamId == null) {
            throw new ApiException(400, "Missing the required parameter 'streamId' when calling getLiveStreamFile");
        }
        if (container == null) {
            throw new ApiException(400, "Missing the required parameter 'container' when calling getLiveStreamFile");
        }

        // Path parameters
        String localVarPath = "/LiveTv/LiveStreamFiles/{streamId}/stream.{container}"
                .replaceAll("\\{streamId}", apiClient.escapeString(streamId.toString()))
                .replaceAll("\\{container}", apiClient.escapeString(container.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("video/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("LiveTvApi.getLiveStreamFile", localVarPath, "GET", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Gets available live tv channels.
     * 
     * @param type Optional. Filter by channel type. (optional)
     * @param userId Optional. Filter by user and attach user data. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param isMovie Optional. Filter for movies. (optional)
     * @param isSeries Optional. Filter for series. (optional)
     * @param isNews Optional. Filter for news. (optional)
     * @param isKids Optional. Filter for kids. (optional)
     * @param isSports Optional. Filter for sports. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param isFavorite Optional. Filter by channels that are favorites, or not. (optional)
     * @param isLiked Optional. Filter by channels that are liked, or not. (optional)
     * @param isDisliked Optional. Filter by channels that are disliked, or not. (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes \&quot;Optional. The image types to include in the output. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param sortBy Optional. Key to sort by. (optional)
     * @param sortOrder Optional. Sort order. (optional)
     * @param enableFavoriteSorting Optional. Incorporate favorite and like status into channel sorting. (optional,
     *            default to false)
     * @param addCurrentProgram Optional. Adds current program info to each channel. (optional, default to true)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Available live tv channels returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public BaseItemDtoQueryResult getLiveTvChannels(@org.eclipse.jdt.annotation.NonNull ChannelType type,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Boolean isMovie, @org.eclipse.jdt.annotation.NonNull Boolean isSeries,
            @org.eclipse.jdt.annotation.NonNull Boolean isNews, @org.eclipse.jdt.annotation.NonNull Boolean isKids,
            @org.eclipse.jdt.annotation.NonNull Boolean isSports, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull Boolean isFavorite, @org.eclipse.jdt.annotation.NonNull Boolean isLiked,
            @org.eclipse.jdt.annotation.NonNull Boolean isDisliked,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull SortOrder sortOrder,
            @org.eclipse.jdt.annotation.NonNull Boolean enableFavoriteSorting,
            @org.eclipse.jdt.annotation.NonNull Boolean addCurrentProgram) throws ApiException {
        return getLiveTvChannelsWithHttpInfo(type, userId, startIndex, isMovie, isSeries, isNews, isKids, isSports,
                limit, isFavorite, isLiked, isDisliked, enableImages, imageTypeLimit, enableImageTypes, fields,
                enableUserData, sortBy, sortOrder, enableFavoriteSorting, addCurrentProgram).getData();
    }

    /**
     * Gets available live tv channels.
     * 
     * @param type Optional. Filter by channel type. (optional)
     * @param userId Optional. Filter by user and attach user data. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param isMovie Optional. Filter for movies. (optional)
     * @param isSeries Optional. Filter for series. (optional)
     * @param isNews Optional. Filter for news. (optional)
     * @param isKids Optional. Filter for kids. (optional)
     * @param isSports Optional. Filter for sports. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param isFavorite Optional. Filter by channels that are favorites, or not. (optional)
     * @param isLiked Optional. Filter by channels that are liked, or not. (optional)
     * @param isDisliked Optional. Filter by channels that are disliked, or not. (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes \&quot;Optional. The image types to include in the output. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param sortBy Optional. Key to sort by. (optional)
     * @param sortOrder Optional. Sort order. (optional)
     * @param enableFavoriteSorting Optional. Incorporate favorite and like status into channel sorting. (optional,
     *            default to false)
     * @param addCurrentProgram Optional. Adds current program info to each channel. (optional, default to true)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Available live tv channels returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<BaseItemDtoQueryResult> getLiveTvChannelsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull ChannelType type, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Boolean isMovie,
            @org.eclipse.jdt.annotation.NonNull Boolean isSeries, @org.eclipse.jdt.annotation.NonNull Boolean isNews,
            @org.eclipse.jdt.annotation.NonNull Boolean isKids, @org.eclipse.jdt.annotation.NonNull Boolean isSports,
            @org.eclipse.jdt.annotation.NonNull Integer limit, @org.eclipse.jdt.annotation.NonNull Boolean isFavorite,
            @org.eclipse.jdt.annotation.NonNull Boolean isLiked, @org.eclipse.jdt.annotation.NonNull Boolean isDisliked,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull SortOrder sortOrder,
            @org.eclipse.jdt.annotation.NonNull Boolean enableFavoriteSorting,
            @org.eclipse.jdt.annotation.NonNull Boolean addCurrentProgram) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "type", type));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "startIndex", startIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isMovie", isMovie));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isSeries", isSeries));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isNews", isNews));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isKids", isKids));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isSports", isSports));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isFavorite", isFavorite));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isLiked", isLiked));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isDisliked", isDisliked));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableImages", enableImages));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageTypeLimit", imageTypeLimit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableUserData", enableUserData));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "sortBy", sortBy));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortOrder", sortOrder));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableFavoriteSorting", enableFavoriteSorting));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "addCurrentProgram", addCurrentProgram));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<BaseItemDtoQueryResult> localVarReturnType = new GenericType<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("LiveTvApi.getLiveTvChannels", "/LiveTv/Channels", "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets available live tv services.
     * 
     * @return LiveTvInfo
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Available live tv services returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public LiveTvInfo getLiveTvInfo() throws ApiException {
        return getLiveTvInfoWithHttpInfo().getData();
    }

    /**
     * Gets available live tv services.
     * 
     * @return ApiResponse&lt;LiveTvInfo&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Available live tv services returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<LiveTvInfo> getLiveTvInfoWithHttpInfo() throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<LiveTvInfo> localVarReturnType = new GenericType<LiveTvInfo>() {
        };
        return apiClient.invokeAPI("LiveTvApi.getLiveTvInfo", "/LiveTv/Info", "GET", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets available live tv epgs.
     * 
     * @param channelIds The channels to return guide information for. (optional)
     * @param userId Optional. Filter by user id. (optional)
     * @param minStartDate Optional. The minimum premiere start date. (optional)
     * @param hasAired Optional. Filter by programs that have completed airing, or not. (optional)
     * @param isAiring Optional. Filter by programs that are currently airing, or not. (optional)
     * @param maxStartDate Optional. The maximum premiere start date. (optional)
     * @param minEndDate Optional. The minimum premiere end date. (optional)
     * @param maxEndDate Optional. The maximum premiere end date. (optional)
     * @param isMovie Optional. Filter for movies. (optional)
     * @param isSeries Optional. Filter for series. (optional)
     * @param isNews Optional. Filter for news. (optional)
     * @param isKids Optional. Filter for kids. (optional)
     * @param isSports Optional. Filter for sports. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Name, StartDate. (optional)
     * @param sortOrder Sort Order - Ascending,Descending. (optional)
     * @param genres The genres to return guide information for. (optional)
     * @param genreIds The genre ids to return guide information for. (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param seriesTimerId Optional. Filter by series timer id. (optional)
     * @param librarySeriesId Optional. Filter by library series id. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param enableTotalRecordCount Retrieve total record count. (optional, default to true)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Live tv epgs returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public BaseItemDtoQueryResult getLiveTvPrograms(@org.eclipse.jdt.annotation.NonNull List<UUID> channelIds,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime minStartDate,
            @org.eclipse.jdt.annotation.NonNull Boolean hasAired, @org.eclipse.jdt.annotation.NonNull Boolean isAiring,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime maxStartDate,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime minEndDate,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime maxEndDate,
            @org.eclipse.jdt.annotation.NonNull Boolean isMovie, @org.eclipse.jdt.annotation.NonNull Boolean isSeries,
            @org.eclipse.jdt.annotation.NonNull Boolean isNews, @org.eclipse.jdt.annotation.NonNull Boolean isKids,
            @org.eclipse.jdt.annotation.NonNull Boolean isSports,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.NonNull List<String> genres,
            @org.eclipse.jdt.annotation.NonNull List<UUID> genreIds,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull String seriesTimerId,
            @org.eclipse.jdt.annotation.NonNull UUID librarySeriesId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) throws ApiException {
        return getLiveTvProgramsWithHttpInfo(channelIds, userId, minStartDate, hasAired, isAiring, maxStartDate,
                minEndDate, maxEndDate, isMovie, isSeries, isNews, isKids, isSports, startIndex, limit, sortBy,
                sortOrder, genres, genreIds, enableImages, imageTypeLimit, enableImageTypes, enableUserData,
                seriesTimerId, librarySeriesId, fields, enableTotalRecordCount).getData();
    }

    /**
     * Gets available live tv epgs.
     * 
     * @param channelIds The channels to return guide information for. (optional)
     * @param userId Optional. Filter by user id. (optional)
     * @param minStartDate Optional. The minimum premiere start date. (optional)
     * @param hasAired Optional. Filter by programs that have completed airing, or not. (optional)
     * @param isAiring Optional. Filter by programs that are currently airing, or not. (optional)
     * @param maxStartDate Optional. The maximum premiere start date. (optional)
     * @param minEndDate Optional. The minimum premiere end date. (optional)
     * @param maxEndDate Optional. The maximum premiere end date. (optional)
     * @param isMovie Optional. Filter for movies. (optional)
     * @param isSeries Optional. Filter for series. (optional)
     * @param isNews Optional. Filter for news. (optional)
     * @param isKids Optional. Filter for kids. (optional)
     * @param isSports Optional. Filter for sports. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Name, StartDate. (optional)
     * @param sortOrder Sort Order - Ascending,Descending. (optional)
     * @param genres The genres to return guide information for. (optional)
     * @param genreIds The genre ids to return guide information for. (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param seriesTimerId Optional. Filter by series timer id. (optional)
     * @param librarySeriesId Optional. Filter by library series id. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param enableTotalRecordCount Retrieve total record count. (optional, default to true)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Live tv epgs returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<BaseItemDtoQueryResult> getLiveTvProgramsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull List<UUID> channelIds, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime minStartDate,
            @org.eclipse.jdt.annotation.NonNull Boolean hasAired, @org.eclipse.jdt.annotation.NonNull Boolean isAiring,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime maxStartDate,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime minEndDate,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime maxEndDate,
            @org.eclipse.jdt.annotation.NonNull Boolean isMovie, @org.eclipse.jdt.annotation.NonNull Boolean isSeries,
            @org.eclipse.jdt.annotation.NonNull Boolean isNews, @org.eclipse.jdt.annotation.NonNull Boolean isKids,
            @org.eclipse.jdt.annotation.NonNull Boolean isSports,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.NonNull List<String> genres,
            @org.eclipse.jdt.annotation.NonNull List<UUID> genreIds,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull String seriesTimerId,
            @org.eclipse.jdt.annotation.NonNull UUID librarySeriesId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("multi", "channelIds", channelIds));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "minStartDate", minStartDate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "hasAired", hasAired));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isAiring", isAiring));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxStartDate", maxStartDate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "minEndDate", minEndDate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxEndDate", maxEndDate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isMovie", isMovie));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isSeries", isSeries));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isNews", isNews));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isKids", isKids));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isSports", isSports));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "startIndex", startIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "sortBy", sortBy));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "sortOrder", sortOrder));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "genres", genres));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "genreIds", genreIds));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableImages", enableImages));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageTypeLimit", imageTypeLimit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableUserData", enableUserData));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "seriesTimerId", seriesTimerId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "librarySeriesId", librarySeriesId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableTotalRecordCount", enableTotalRecordCount));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<BaseItemDtoQueryResult> localVarReturnType = new GenericType<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("LiveTvApi.getLiveTvPrograms", "/LiveTv/Programs", "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets a live tv program.
     * 
     * @param programId Program id. (required)
     * @param userId Optional. Attach user data. (optional)
     * @return BaseItemDto
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Program returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public BaseItemDto getProgram(@org.eclipse.jdt.annotation.Nullable String programId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        return getProgramWithHttpInfo(programId, userId).getData();
    }

    /**
     * Gets a live tv program.
     * 
     * @param programId Program id. (required)
     * @param userId Optional. Attach user data. (optional)
     * @return ApiResponse&lt;BaseItemDto&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Program returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<BaseItemDto> getProgramWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String programId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        // Check required parameters
        if (programId == null) {
            throw new ApiException(400, "Missing the required parameter 'programId' when calling getProgram");
        }

        // Path parameters
        String localVarPath = "/LiveTv/Programs/{programId}".replaceAll("\\{programId}",
                apiClient.escapeString(programId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<BaseItemDto> localVarReturnType = new GenericType<BaseItemDto>() {
        };
        return apiClient.invokeAPI("LiveTvApi.getProgram", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets available live tv epgs.
     * 
     * @param getProgramsDto Request body. (optional)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Live tv epgs returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public BaseItemDtoQueryResult getPrograms(@org.eclipse.jdt.annotation.NonNull GetProgramsDto getProgramsDto)
            throws ApiException {
        return getProgramsWithHttpInfo(getProgramsDto).getData();
    }

    /**
     * Gets available live tv epgs.
     * 
     * @param getProgramsDto Request body. (optional)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Live tv epgs returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<BaseItemDtoQueryResult> getProgramsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull GetProgramsDto getProgramsDto) throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<BaseItemDtoQueryResult> localVarReturnType = new GenericType<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("LiveTvApi.getPrograms", "/LiveTv/Programs", "POST", new ArrayList<>(),
                getProgramsDto, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets recommended live tv epgs.
     * 
     * @param userId Optional. filter by user id. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param isAiring Optional. Filter by programs that are currently airing, or not. (optional)
     * @param hasAired Optional. Filter by programs that have completed airing, or not. (optional)
     * @param isSeries Optional. Filter for series. (optional)
     * @param isMovie Optional. Filter for movies. (optional)
     * @param isNews Optional. Filter for news. (optional)
     * @param isKids Optional. Filter for kids. (optional)
     * @param isSports Optional. Filter for sports. (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param genreIds The genres to return guide information for. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param enableUserData Optional. include user data. (optional)
     * @param enableTotalRecordCount Retrieve total record count. (optional, default to true)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Recommended epgs returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public BaseItemDtoQueryResult getRecommendedPrograms(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer limit, @org.eclipse.jdt.annotation.NonNull Boolean isAiring,
            @org.eclipse.jdt.annotation.NonNull Boolean hasAired, @org.eclipse.jdt.annotation.NonNull Boolean isSeries,
            @org.eclipse.jdt.annotation.NonNull Boolean isMovie, @org.eclipse.jdt.annotation.NonNull Boolean isNews,
            @org.eclipse.jdt.annotation.NonNull Boolean isKids, @org.eclipse.jdt.annotation.NonNull Boolean isSports,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull List<UUID> genreIds,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) throws ApiException {
        return getRecommendedProgramsWithHttpInfo(userId, limit, isAiring, hasAired, isSeries, isMovie, isNews, isKids,
                isSports, enableImages, imageTypeLimit, enableImageTypes, genreIds, fields, enableUserData,
                enableTotalRecordCount).getData();
    }

    /**
     * Gets recommended live tv epgs.
     * 
     * @param userId Optional. filter by user id. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param isAiring Optional. Filter by programs that are currently airing, or not. (optional)
     * @param hasAired Optional. Filter by programs that have completed airing, or not. (optional)
     * @param isSeries Optional. Filter for series. (optional)
     * @param isMovie Optional. Filter for movies. (optional)
     * @param isNews Optional. Filter for news. (optional)
     * @param isKids Optional. Filter for kids. (optional)
     * @param isSports Optional. Filter for sports. (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param genreIds The genres to return guide information for. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param enableUserData Optional. include user data. (optional)
     * @param enableTotalRecordCount Retrieve total record count. (optional, default to true)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Recommended epgs returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<BaseItemDtoQueryResult> getRecommendedProgramsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull Boolean isAiring, @org.eclipse.jdt.annotation.NonNull Boolean hasAired,
            @org.eclipse.jdt.annotation.NonNull Boolean isSeries, @org.eclipse.jdt.annotation.NonNull Boolean isMovie,
            @org.eclipse.jdt.annotation.NonNull Boolean isNews, @org.eclipse.jdt.annotation.NonNull Boolean isKids,
            @org.eclipse.jdt.annotation.NonNull Boolean isSports,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull List<UUID> genreIds,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isAiring", isAiring));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "hasAired", hasAired));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isSeries", isSeries));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isMovie", isMovie));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isNews", isNews));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isKids", isKids));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isSports", isSports));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableImages", enableImages));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageTypeLimit", imageTypeLimit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "genreIds", genreIds));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableUserData", enableUserData));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableTotalRecordCount", enableTotalRecordCount));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<BaseItemDtoQueryResult> localVarReturnType = new GenericType<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("LiveTvApi.getRecommendedPrograms", "/LiveTv/Programs/Recommended", "GET",
                localVarQueryParams, null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets a live tv recording.
     * 
     * @param recordingId Recording id. (required)
     * @param userId Optional. Attach user data. (optional)
     * @return BaseItemDto
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Recording returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public BaseItemDto getRecording(@org.eclipse.jdt.annotation.Nullable UUID recordingId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        return getRecordingWithHttpInfo(recordingId, userId).getData();
    }

    /**
     * Gets a live tv recording.
     * 
     * @param recordingId Recording id. (required)
     * @param userId Optional. Attach user data. (optional)
     * @return ApiResponse&lt;BaseItemDto&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Recording returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<BaseItemDto> getRecordingWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID recordingId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        // Check required parameters
        if (recordingId == null) {
            throw new ApiException(400, "Missing the required parameter 'recordingId' when calling getRecording");
        }

        // Path parameters
        String localVarPath = "/LiveTv/Recordings/{recordingId}".replaceAll("\\{recordingId}",
                apiClient.escapeString(recordingId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<BaseItemDto> localVarReturnType = new GenericType<BaseItemDto>() {
        };
        return apiClient.invokeAPI("LiveTvApi.getRecording", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets recording folders.
     * 
     * @param userId Optional. Filter by user and attach user data. (optional)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Recording folders returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public BaseItemDtoQueryResult getRecordingFolders(@org.eclipse.jdt.annotation.NonNull UUID userId)
            throws ApiException {
        return getRecordingFoldersWithHttpInfo(userId).getData();
    }

    /**
     * Gets recording folders.
     * 
     * @param userId Optional. Filter by user and attach user data. (optional)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Recording folders returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<BaseItemDtoQueryResult> getRecordingFoldersWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<BaseItemDtoQueryResult> localVarReturnType = new GenericType<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("LiveTvApi.getRecordingFolders", "/LiveTv/Recordings/Folders", "GET",
                localVarQueryParams, null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Get recording group.
     * 
     * @param groupId Group id. (required)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Not Found</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     * @deprecated
     */
    @Deprecated
    public void getRecordingGroup(@org.eclipse.jdt.annotation.Nullable UUID groupId) throws ApiException {
        getRecordingGroupWithHttpInfo(groupId);
    }

    /**
     * Get recording group.
     * 
     * @param groupId Group id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Not Found</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     * @deprecated
     */
    @Deprecated
    public ApiResponse<Void> getRecordingGroupWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID groupId)
            throws ApiException {
        // Check required parameters
        if (groupId == null) {
            throw new ApiException(400, "Missing the required parameter 'groupId' when calling getRecordingGroup");
        }

        // Path parameters
        String localVarPath = "/LiveTv/Recordings/Groups/{groupId}".replaceAll("\\{groupId}",
                apiClient.escapeString(groupId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("LiveTvApi.getRecordingGroup", localVarPath, "GET", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Gets live tv recording groups.
     * 
     * @param userId Optional. Filter by user and attach user data. (optional)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Recording groups returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     * @deprecated
     */
    @Deprecated
    public BaseItemDtoQueryResult getRecordingGroups(@org.eclipse.jdt.annotation.NonNull UUID userId)
            throws ApiException {
        return getRecordingGroupsWithHttpInfo(userId).getData();
    }

    /**
     * Gets live tv recording groups.
     * 
     * @param userId Optional. Filter by user and attach user data. (optional)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Recording groups returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     * @deprecated
     */
    @Deprecated
    public ApiResponse<BaseItemDtoQueryResult> getRecordingGroupsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<BaseItemDtoQueryResult> localVarReturnType = new GenericType<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("LiveTvApi.getRecordingGroups", "/LiveTv/Recordings/Groups", "GET",
                localVarQueryParams, null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets live tv recordings.
     * 
     * @param channelId Optional. Filter by channel id. (optional)
     * @param userId Optional. Filter by user and attach user data. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param status Optional. Filter by recording status. (optional)
     * @param isInProgress Optional. Filter by recordings that are in progress, or not. (optional)
     * @param seriesTimerId Optional. Filter by recordings belonging to a series timer. (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param isMovie Optional. Filter for movies. (optional)
     * @param isSeries Optional. Filter for series. (optional)
     * @param isKids Optional. Filter for kids. (optional)
     * @param isSports Optional. Filter for sports. (optional)
     * @param isNews Optional. Filter for news. (optional)
     * @param isLibraryItem Optional. Filter for is library item. (optional)
     * @param enableTotalRecordCount Optional. Return total record count. (optional, default to true)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Live tv recordings returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public BaseItemDtoQueryResult getRecordings(@org.eclipse.jdt.annotation.NonNull String channelId,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull RecordingStatus status,
            @org.eclipse.jdt.annotation.NonNull Boolean isInProgress,
            @org.eclipse.jdt.annotation.NonNull String seriesTimerId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Boolean isMovie, @org.eclipse.jdt.annotation.NonNull Boolean isSeries,
            @org.eclipse.jdt.annotation.NonNull Boolean isKids, @org.eclipse.jdt.annotation.NonNull Boolean isSports,
            @org.eclipse.jdt.annotation.NonNull Boolean isNews,
            @org.eclipse.jdt.annotation.NonNull Boolean isLibraryItem,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) throws ApiException {
        return getRecordingsWithHttpInfo(channelId, userId, startIndex, limit, status, isInProgress, seriesTimerId,
                enableImages, imageTypeLimit, enableImageTypes, fields, enableUserData, isMovie, isSeries, isKids,
                isSports, isNews, isLibraryItem, enableTotalRecordCount).getData();
    }

    /**
     * Gets live tv recordings.
     * 
     * @param channelId Optional. Filter by channel id. (optional)
     * @param userId Optional. Filter by user and attach user data. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param status Optional. Filter by recording status. (optional)
     * @param isInProgress Optional. Filter by recordings that are in progress, or not. (optional)
     * @param seriesTimerId Optional. Filter by recordings belonging to a series timer. (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param isMovie Optional. Filter for movies. (optional)
     * @param isSeries Optional. Filter for series. (optional)
     * @param isKids Optional. Filter for kids. (optional)
     * @param isSports Optional. Filter for sports. (optional)
     * @param isNews Optional. Filter for news. (optional)
     * @param isLibraryItem Optional. Filter for is library item. (optional)
     * @param enableTotalRecordCount Optional. Return total record count. (optional, default to true)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Live tv recordings returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<BaseItemDtoQueryResult> getRecordingsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String channelId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull RecordingStatus status,
            @org.eclipse.jdt.annotation.NonNull Boolean isInProgress,
            @org.eclipse.jdt.annotation.NonNull String seriesTimerId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Boolean isMovie, @org.eclipse.jdt.annotation.NonNull Boolean isSeries,
            @org.eclipse.jdt.annotation.NonNull Boolean isKids, @org.eclipse.jdt.annotation.NonNull Boolean isSports,
            @org.eclipse.jdt.annotation.NonNull Boolean isNews,
            @org.eclipse.jdt.annotation.NonNull Boolean isLibraryItem,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "channelId", channelId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "startIndex", startIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "status", status));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isInProgress", isInProgress));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "seriesTimerId", seriesTimerId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableImages", enableImages));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageTypeLimit", imageTypeLimit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableUserData", enableUserData));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isMovie", isMovie));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isSeries", isSeries));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isKids", isKids));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isSports", isSports));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isNews", isNews));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isLibraryItem", isLibraryItem));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableTotalRecordCount", enableTotalRecordCount));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<BaseItemDtoQueryResult> localVarReturnType = new GenericType<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("LiveTvApi.getRecordings", "/LiveTv/Recordings", "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets live tv recording series.
     * 
     * @param channelId Optional. Filter by channel id. (optional)
     * @param userId Optional. Filter by user and attach user data. (optional)
     * @param groupId Optional. Filter by recording group. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param status Optional. Filter by recording status. (optional)
     * @param isInProgress Optional. Filter by recordings that are in progress, or not. (optional)
     * @param seriesTimerId Optional. Filter by recordings belonging to a series timer. (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param enableTotalRecordCount Optional. Return total record count. (optional, default to true)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Live tv recordings returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     * @deprecated
     */
    @Deprecated
    public BaseItemDtoQueryResult getRecordingsSeries(@org.eclipse.jdt.annotation.NonNull String channelId,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull String groupId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull RecordingStatus status,
            @org.eclipse.jdt.annotation.NonNull Boolean isInProgress,
            @org.eclipse.jdt.annotation.NonNull String seriesTimerId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) throws ApiException {
        return getRecordingsSeriesWithHttpInfo(channelId, userId, groupId, startIndex, limit, status, isInProgress,
                seriesTimerId, enableImages, imageTypeLimit, enableImageTypes, fields, enableUserData,
                enableTotalRecordCount).getData();
    }

    /**
     * Gets live tv recording series.
     * 
     * @param channelId Optional. Filter by channel id. (optional)
     * @param userId Optional. Filter by user and attach user data. (optional)
     * @param groupId Optional. Filter by recording group. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param status Optional. Filter by recording status. (optional)
     * @param isInProgress Optional. Filter by recordings that are in progress, or not. (optional)
     * @param seriesTimerId Optional. Filter by recordings belonging to a series timer. (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param enableTotalRecordCount Optional. Return total record count. (optional, default to true)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Live tv recordings returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     * @deprecated
     */
    @Deprecated
    public ApiResponse<BaseItemDtoQueryResult> getRecordingsSeriesWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String channelId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull String groupId, @org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull RecordingStatus status,
            @org.eclipse.jdt.annotation.NonNull Boolean isInProgress,
            @org.eclipse.jdt.annotation.NonNull String seriesTimerId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "channelId", channelId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "groupId", groupId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "startIndex", startIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "status", status));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isInProgress", isInProgress));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "seriesTimerId", seriesTimerId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableImages", enableImages));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageTypeLimit", imageTypeLimit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableUserData", enableUserData));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableTotalRecordCount", enableTotalRecordCount));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<BaseItemDtoQueryResult> localVarReturnType = new GenericType<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("LiveTvApi.getRecordingsSeries", "/LiveTv/Recordings/Series", "GET",
                localVarQueryParams, null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets available countries.
     * 
     * @return File
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Available countries returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File getSchedulesDirectCountries() throws ApiException {
        return getSchedulesDirectCountriesWithHttpInfo().getData();
    }

    /**
     * Gets available countries.
     * 
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Available countries returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> getSchedulesDirectCountriesWithHttpInfo() throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept("application/json");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("LiveTvApi.getSchedulesDirectCountries",
                "/LiveTv/ListingProviders/SchedulesDirect/Countries", "GET", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets a live tv series timer.
     * 
     * @param timerId Timer id. (required)
     * @return SeriesTimerInfoDto
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Series timer returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Series timer not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public SeriesTimerInfoDto getSeriesTimer(@org.eclipse.jdt.annotation.Nullable String timerId) throws ApiException {
        return getSeriesTimerWithHttpInfo(timerId).getData();
    }

    /**
     * Gets a live tv series timer.
     * 
     * @param timerId Timer id. (required)
     * @return ApiResponse&lt;SeriesTimerInfoDto&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Series timer returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Series timer not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<SeriesTimerInfoDto> getSeriesTimerWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String timerId) throws ApiException {
        // Check required parameters
        if (timerId == null) {
            throw new ApiException(400, "Missing the required parameter 'timerId' when calling getSeriesTimer");
        }

        // Path parameters
        String localVarPath = "/LiveTv/SeriesTimers/{timerId}".replaceAll("\\{timerId}",
                apiClient.escapeString(timerId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<SeriesTimerInfoDto> localVarReturnType = new GenericType<SeriesTimerInfoDto>() {
        };
        return apiClient.invokeAPI("LiveTvApi.getSeriesTimer", localVarPath, "GET", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets live tv series timers.
     * 
     * @param sortBy Optional. Sort by SortName or Priority. (optional)
     * @param sortOrder Optional. Sort in Ascending or Descending order. (optional)
     * @return SeriesTimerInfoDtoQueryResult
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Timers returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public SeriesTimerInfoDtoQueryResult getSeriesTimers(@org.eclipse.jdt.annotation.NonNull String sortBy,
            @org.eclipse.jdt.annotation.NonNull SortOrder sortOrder) throws ApiException {
        return getSeriesTimersWithHttpInfo(sortBy, sortOrder).getData();
    }

    /**
     * Gets live tv series timers.
     * 
     * @param sortBy Optional. Sort by SortName or Priority. (optional)
     * @param sortOrder Optional. Sort in Ascending or Descending order. (optional)
     * @return ApiResponse&lt;SeriesTimerInfoDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Timers returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<SeriesTimerInfoDtoQueryResult> getSeriesTimersWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String sortBy, @org.eclipse.jdt.annotation.NonNull SortOrder sortOrder)
            throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "sortBy", sortBy));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortOrder", sortOrder));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<SeriesTimerInfoDtoQueryResult> localVarReturnType = new GenericType<SeriesTimerInfoDtoQueryResult>() {
        };
        return apiClient.invokeAPI("LiveTvApi.getSeriesTimers", "/LiveTv/SeriesTimers", "GET", localVarQueryParams,
                null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets a timer.
     * 
     * @param timerId Timer id. (required)
     * @return TimerInfoDto
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Timer returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public TimerInfoDto getTimer(@org.eclipse.jdt.annotation.Nullable String timerId) throws ApiException {
        return getTimerWithHttpInfo(timerId).getData();
    }

    /**
     * Gets a timer.
     * 
     * @param timerId Timer id. (required)
     * @return ApiResponse&lt;TimerInfoDto&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Timer returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<TimerInfoDto> getTimerWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String timerId)
            throws ApiException {
        // Check required parameters
        if (timerId == null) {
            throw new ApiException(400, "Missing the required parameter 'timerId' when calling getTimer");
        }

        // Path parameters
        String localVarPath = "/LiveTv/Timers/{timerId}".replaceAll("\\{timerId}",
                apiClient.escapeString(timerId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<TimerInfoDto> localVarReturnType = new GenericType<TimerInfoDto>() {
        };
        return apiClient.invokeAPI("LiveTvApi.getTimer", localVarPath, "GET", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets the live tv timers.
     * 
     * @param channelId Optional. Filter by channel id. (optional)
     * @param seriesTimerId Optional. Filter by timers belonging to a series timer. (optional)
     * @param isActive Optional. Filter by timers that are active. (optional)
     * @param isScheduled Optional. Filter by timers that are scheduled. (optional)
     * @return TimerInfoDtoQueryResult
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Success</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public TimerInfoDtoQueryResult getTimers(@org.eclipse.jdt.annotation.NonNull String channelId,
            @org.eclipse.jdt.annotation.NonNull String seriesTimerId,
            @org.eclipse.jdt.annotation.NonNull Boolean isActive,
            @org.eclipse.jdt.annotation.NonNull Boolean isScheduled) throws ApiException {
        return getTimersWithHttpInfo(channelId, seriesTimerId, isActive, isScheduled).getData();
    }

    /**
     * Gets the live tv timers.
     * 
     * @param channelId Optional. Filter by channel id. (optional)
     * @param seriesTimerId Optional. Filter by timers belonging to a series timer. (optional)
     * @param isActive Optional. Filter by timers that are active. (optional)
     * @param isScheduled Optional. Filter by timers that are scheduled. (optional)
     * @return ApiResponse&lt;TimerInfoDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Success</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<TimerInfoDtoQueryResult> getTimersWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String channelId,
            @org.eclipse.jdt.annotation.NonNull String seriesTimerId,
            @org.eclipse.jdt.annotation.NonNull Boolean isActive,
            @org.eclipse.jdt.annotation.NonNull Boolean isScheduled) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "channelId", channelId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "seriesTimerId", seriesTimerId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isActive", isActive));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isScheduled", isScheduled));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<TimerInfoDtoQueryResult> localVarReturnType = new GenericType<TimerInfoDtoQueryResult>() {
        };
        return apiClient.invokeAPI("LiveTvApi.getTimers", "/LiveTv/Timers", "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Get tuner host types.
     * 
     * @return List&lt;NameIdPair&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Tuner host types returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public List<NameIdPair> getTunerHostTypes() throws ApiException {
        return getTunerHostTypesWithHttpInfo().getData();
    }

    /**
     * Get tuner host types.
     * 
     * @return ApiResponse&lt;List&lt;NameIdPair&gt;&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Tuner host types returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<List<NameIdPair>> getTunerHostTypesWithHttpInfo() throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<List<NameIdPair>> localVarReturnType = new GenericType<List<NameIdPair>>() {
        };
        return apiClient.invokeAPI("LiveTvApi.getTunerHostTypes", "/LiveTv/TunerHosts/Types", "GET", new ArrayList<>(),
                null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Resets a tv tuner.
     * 
     * @param tunerId Tuner id. (required)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Tuner reset.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void resetTuner(@org.eclipse.jdt.annotation.Nullable String tunerId) throws ApiException {
        resetTunerWithHttpInfo(tunerId);
    }

    /**
     * Resets a tv tuner.
     * 
     * @param tunerId Tuner id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Tuner reset.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> resetTunerWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String tunerId)
            throws ApiException {
        // Check required parameters
        if (tunerId == null) {
            throw new ApiException(400, "Missing the required parameter 'tunerId' when calling resetTuner");
        }

        // Path parameters
        String localVarPath = "/LiveTv/Tuners/{tunerId}/Reset".replaceAll("\\{tunerId}",
                apiClient.escapeString(tunerId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("LiveTvApi.resetTuner", localVarPath, "POST", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Set channel mappings.
     * 
     * @param setChannelMappingDto The set channel mapping dto. (required)
     * @return TunerChannelMapping
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Created channel mapping returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public TunerChannelMapping setChannelMapping(
            @org.eclipse.jdt.annotation.Nullable SetChannelMappingDto setChannelMappingDto) throws ApiException {
        return setChannelMappingWithHttpInfo(setChannelMappingDto).getData();
    }

    /**
     * Set channel mappings.
     * 
     * @param setChannelMappingDto The set channel mapping dto. (required)
     * @return ApiResponse&lt;TunerChannelMapping&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Created channel mapping returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<TunerChannelMapping> setChannelMappingWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable SetChannelMappingDto setChannelMappingDto) throws ApiException {
        // Check required parameters
        if (setChannelMappingDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'setChannelMappingDto' when calling setChannelMapping");
        }

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<TunerChannelMapping> localVarReturnType = new GenericType<TunerChannelMapping>() {
        };
        return apiClient.invokeAPI("LiveTvApi.setChannelMapping", "/LiveTv/ChannelMappings", "POST", new ArrayList<>(),
                setChannelMappingDto, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Updates a live tv series timer.
     * 
     * @param timerId Timer id. (required)
     * @param seriesTimerInfoDto New series timer info. (optional)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Series timer updated.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void updateSeriesTimer(@org.eclipse.jdt.annotation.Nullable String timerId,
            @org.eclipse.jdt.annotation.NonNull SeriesTimerInfoDto seriesTimerInfoDto) throws ApiException {
        updateSeriesTimerWithHttpInfo(timerId, seriesTimerInfoDto);
    }

    /**
     * Updates a live tv series timer.
     * 
     * @param timerId Timer id. (required)
     * @param seriesTimerInfoDto New series timer info. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Series timer updated.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> updateSeriesTimerWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String timerId,
            @org.eclipse.jdt.annotation.NonNull SeriesTimerInfoDto seriesTimerInfoDto) throws ApiException {
        // Check required parameters
        if (timerId == null) {
            throw new ApiException(400, "Missing the required parameter 'timerId' when calling updateSeriesTimer");
        }

        // Path parameters
        String localVarPath = "/LiveTv/SeriesTimers/{timerId}".replaceAll("\\{timerId}",
                apiClient.escapeString(timerId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("LiveTvApi.updateSeriesTimer", localVarPath, "POST", new ArrayList<>(),
                seriesTimerInfoDto, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Updates a live tv timer.
     * 
     * @param timerId Timer id. (required)
     * @param timerInfoDto New timer info. (optional)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Timer updated.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void updateTimer(@org.eclipse.jdt.annotation.Nullable String timerId,
            @org.eclipse.jdt.annotation.NonNull TimerInfoDto timerInfoDto) throws ApiException {
        updateTimerWithHttpInfo(timerId, timerInfoDto);
    }

    /**
     * Updates a live tv timer.
     * 
     * @param timerId Timer id. (required)
     * @param timerInfoDto New timer info. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Timer updated.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> updateTimerWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String timerId,
            @org.eclipse.jdt.annotation.NonNull TimerInfoDto timerInfoDto) throws ApiException {
        // Check required parameters
        if (timerId == null) {
            throw new ApiException(400, "Missing the required parameter 'timerId' when calling updateTimer");
        }

        // Path parameters
        String localVarPath = "/LiveTv/Timers/{timerId}".replaceAll("\\{timerId}",
                apiClient.escapeString(timerId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("LiveTvApi.updateTimer", localVarPath, "POST", new ArrayList<>(), timerInfoDto,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }
}
