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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.CollectionCreationResult;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class CollectionApi {
    private ApiClient apiClient;

    public CollectionApi() {
        this(Configuration.getDefaultApiClient());
    }

    public CollectionApi(ApiClient apiClient) {
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
     * Adds items to a collection.
     * 
     * @param collectionId The collection id. (required)
     * @param ids Item ids, comma delimited. (required)
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
     *                        <td>Items added to collection.</td>
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
    public void addToCollection(@org.eclipse.jdt.annotation.Nullable UUID collectionId,
            @org.eclipse.jdt.annotation.Nullable List<UUID> ids) throws ApiException {
        addToCollectionWithHttpInfo(collectionId, ids);
    }

    /**
     * Adds items to a collection.
     * 
     * @param collectionId The collection id. (required)
     * @param ids Item ids, comma delimited. (required)
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
     *                        <td>Items added to collection.</td>
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
    public ApiResponse<Void> addToCollectionWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID collectionId,
            @org.eclipse.jdt.annotation.Nullable List<UUID> ids) throws ApiException {
        // Check required parameters
        if (collectionId == null) {
            throw new ApiException(400, "Missing the required parameter 'collectionId' when calling addToCollection");
        }
        if (ids == null) {
            throw new ApiException(400, "Missing the required parameter 'ids' when calling addToCollection");
        }

        // Path parameters
        String localVarPath = "/Collections/{collectionId}/Items".replaceAll("\\{collectionId}",
                apiClient.escapeString(collectionId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("multi", "ids", ids));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("CollectionApi.addToCollection", localVarPath, "POST", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Creates a new collection.
     * 
     * @param name The name of the collection. (optional)
     * @param ids Item Ids to add to the collection. (optional)
     * @param parentId Optional. Create the collection within a specific folder. (optional)
     * @param isLocked Whether or not to lock the new collection. (optional, default to false)
     * @return CollectionCreationResult
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
     *                        <td>Collection created.</td>
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
    public CollectionCreationResult createCollection(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull List<String> ids, @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull Boolean isLocked) throws ApiException {
        return createCollectionWithHttpInfo(name, ids, parentId, isLocked).getData();
    }

    /**
     * Creates a new collection.
     * 
     * @param name The name of the collection. (optional)
     * @param ids Item Ids to add to the collection. (optional)
     * @param parentId Optional. Create the collection within a specific folder. (optional)
     * @param isLocked Whether or not to lock the new collection. (optional, default to false)
     * @return ApiResponse&lt;CollectionCreationResult&gt;
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
     *                        <td>Collection created.</td>
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
    public ApiResponse<CollectionCreationResult> createCollectionWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String name, @org.eclipse.jdt.annotation.NonNull List<String> ids,
            @org.eclipse.jdt.annotation.NonNull UUID parentId, @org.eclipse.jdt.annotation.NonNull Boolean isLocked)
            throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "name", name));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "ids", ids));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "parentId", parentId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isLocked", isLocked));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<CollectionCreationResult> localVarReturnType = new GenericType<CollectionCreationResult>() {
        };
        return apiClient.invokeAPI("CollectionApi.createCollection", "/Collections", "POST", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Removes items from a collection.
     * 
     * @param collectionId The collection id. (required)
     * @param ids Item ids, comma delimited. (required)
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
     *                        <td>Items removed from collection.</td>
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
    public void removeFromCollection(@org.eclipse.jdt.annotation.Nullable UUID collectionId,
            @org.eclipse.jdt.annotation.Nullable List<UUID> ids) throws ApiException {
        removeFromCollectionWithHttpInfo(collectionId, ids);
    }

    /**
     * Removes items from a collection.
     * 
     * @param collectionId The collection id. (required)
     * @param ids Item ids, comma delimited. (required)
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
     *                        <td>Items removed from collection.</td>
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
    public ApiResponse<Void> removeFromCollectionWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID collectionId,
            @org.eclipse.jdt.annotation.Nullable List<UUID> ids) throws ApiException {
        // Check required parameters
        if (collectionId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'collectionId' when calling removeFromCollection");
        }
        if (ids == null) {
            throw new ApiException(400, "Missing the required parameter 'ids' when calling removeFromCollection");
        }

        // Path parameters
        String localVarPath = "/Collections/{collectionId}/Items".replaceAll("\\{collectionId}",
                apiClient.escapeString(collectionId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("multi", "ids", ids));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("CollectionApi.removeFromCollection", localVarPath, "DELETE", localVarQueryParams,
                null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }
}
