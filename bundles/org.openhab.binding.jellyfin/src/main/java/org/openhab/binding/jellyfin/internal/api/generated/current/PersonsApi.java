package org.openhab.binding.jellyfin.internal.api.generated.current;

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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ImageType;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemFields;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemFilter;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PersonsApi {
    private ApiClient apiClient;

    public PersonsApi() {
        this(Configuration.getDefaultApiClient());
    }

    public PersonsApi(ApiClient apiClient) {
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
     * Get person by name.
     * 
     * @param name Person name. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
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
     *                        <td>Person returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Person not found.</td>
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
    public BaseItemDto getPerson(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        return getPersonWithHttpInfo(name, userId).getData();
    }

    /**
     * Get person by name.
     * 
     * @param name Person name. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
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
     *                        <td>Person returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Person not found.</td>
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
    public ApiResponse<BaseItemDto> getPersonWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        // Check required parameters
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling getPerson");
        }

        // Path parameters
        String localVarPath = "/Persons/{name}".replaceAll("\\{name}", apiClient.escapeString(name.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<BaseItemDto> localVarReturnType = new GenericType<BaseItemDto>() {
        };
        return apiClient.invokeAPI("PersonsApi.getPerson", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets all persons.
     * 
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param searchTerm The search term. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param filters Optional. Specify additional filters to apply. (optional)
     * @param isFavorite Optional filter by items that are marked as favorite, or not. userId is required. (optional)
     * @param enableUserData Optional, include user data. (optional)
     * @param imageTypeLimit Optional, the max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param excludePersonTypes Optional. If specified results will be filtered to exclude those containing the
     *            specified PersonType. Allows multiple, comma-delimited. (optional)
     * @param personTypes Optional. If specified results will be filtered to include only those containing the specified
     *            PersonType. Allows multiple, comma-delimited. (optional)
     * @param appearsInItemId Optional. If specified, person results will be filtered on items related to said persons.
     *            (optional)
     * @param userId User id. (optional)
     * @param enableImages Optional, include image information in output. (optional, default to true)
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
     *                        <td>Persons returned.</td>
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
    public BaseItemDtoQueryResult getPersons(@org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull String searchTerm,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.NonNull Boolean isFavorite,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull List<String> excludePersonTypes,
            @org.eclipse.jdt.annotation.NonNull List<String> personTypes,
            @org.eclipse.jdt.annotation.NonNull UUID appearsInItemId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages) throws ApiException {
        return getPersonsWithHttpInfo(limit, searchTerm, fields, filters, isFavorite, enableUserData, imageTypeLimit,
                enableImageTypes, excludePersonTypes, personTypes, appearsInItemId, userId, enableImages).getData();
    }

    /**
     * Gets all persons.
     * 
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param searchTerm The search term. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param filters Optional. Specify additional filters to apply. (optional)
     * @param isFavorite Optional filter by items that are marked as favorite, or not. userId is required. (optional)
     * @param enableUserData Optional, include user data. (optional)
     * @param imageTypeLimit Optional, the max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param excludePersonTypes Optional. If specified results will be filtered to exclude those containing the
     *            specified PersonType. Allows multiple, comma-delimited. (optional)
     * @param personTypes Optional. If specified results will be filtered to include only those containing the specified
     *            PersonType. Allows multiple, comma-delimited. (optional)
     * @param appearsInItemId Optional. If specified, person results will be filtered on items related to said persons.
     *            (optional)
     * @param userId User id. (optional)
     * @param enableImages Optional, include image information in output. (optional, default to true)
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
     *                        <td>Persons returned.</td>
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
    public ApiResponse<BaseItemDtoQueryResult> getPersonsWithHttpInfo(@org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull String searchTerm,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.NonNull Boolean isFavorite,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull List<String> excludePersonTypes,
            @org.eclipse.jdt.annotation.NonNull List<String> personTypes,
            @org.eclipse.jdt.annotation.NonNull UUID appearsInItemId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "limit", limit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "searchTerm", searchTerm));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "filters", filters));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isFavorite", isFavorite));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableUserData", enableUserData));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageTypeLimit", imageTypeLimit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "excludePersonTypes", excludePersonTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "personTypes", personTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "appearsInItemId", appearsInItemId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableImages", enableImages));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<BaseItemDtoQueryResult> localVarReturnType = new GenericType<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("PersonsApi.getPersons", "/Persons", "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }
}
