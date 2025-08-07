package org.openhab.binding.jellyfin.internal.api.generated.current;

import java.io.File;
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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ImageFormat;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ImageInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ImageType;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ImageApi {
    private ApiClient apiClient;

    public ImageApi() {
        this(Configuration.getDefaultApiClient());
    }

    public ImageApi(ApiClient apiClient) {
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
     * Delete a custom splashscreen.
     * 
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
     *                        <td>Successfully deleted the custom splashscreen.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>User does not have permission to delete splashscreen..</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void deleteCustomSplashscreen() throws ApiException {
        deleteCustomSplashscreenWithHttpInfo();
    }

    /**
     * Delete a custom splashscreen.
     * 
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
     *                        <td>Successfully deleted the custom splashscreen.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>User does not have permission to delete splashscreen..</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> deleteCustomSplashscreenWithHttpInfo() throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("ImageApi.deleteCustomSplashscreen", "/Branding/Splashscreen", "DELETE",
                new ArrayList<>(), null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Delete an item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex The image index. (optional)
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
     *                        <td>Image deleted.</td>
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
    public void deleteItemImage(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        deleteItemImageWithHttpInfo(itemId, imageType, imageIndex);
    }

    /**
     * Delete an item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex The image index. (optional)
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
     *                        <td>Image deleted.</td>
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
    public ApiResponse<Void> deleteItemImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling deleteItemImage");
        }
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling deleteItemImage");
        }

        // Path parameters
        String localVarPath = "/Items/{itemId}/Images/{imageType}"
                .replaceAll("\\{itemId}", apiClient.escapeString(itemId.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "imageIndex", imageIndex));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("ImageApi.deleteItemImage", localVarPath, "DELETE", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Delete an item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex The image index. (required)
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
     *                        <td>Image deleted.</td>
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
    public void deleteItemImageByIndex(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex) throws ApiException {
        deleteItemImageByIndexWithHttpInfo(itemId, imageType, imageIndex);
    }

    /**
     * Delete an item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex The image index. (required)
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
     *                        <td>Image deleted.</td>
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
    public ApiResponse<Void> deleteItemImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling deleteItemImageByIndex");
        }
        if (imageType == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageType' when calling deleteItemImageByIndex");
        }
        if (imageIndex == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageIndex' when calling deleteItemImageByIndex");
        }

        // Path parameters
        String localVarPath = "/Items/{itemId}/Images/{imageType}/{imageIndex}"
                .replaceAll("\\{itemId}", apiClient.escapeString(itemId.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()))
                .replaceAll("\\{imageIndex}", apiClient.escapeString(imageIndex.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("ImageApi.deleteItemImageByIndex", localVarPath, "DELETE", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Delete the user&#39;s image.
     * 
     * @param userId User Id. (optional)
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
     *                        <td>Image deleted.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>User does not have permission to delete the image.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void deleteUserImage(@org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        deleteUserImageWithHttpInfo(userId);
    }

    /**
     * Delete the user&#39;s image.
     * 
     * @param userId User Id. (optional)
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
     *                        <td>Image deleted.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>User does not have permission to delete the image.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> deleteUserImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID userId)
            throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("ImageApi.deleteUserImage", "/UserImage", "DELETE", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Get artist image by name.
     * 
     * @param name Artist name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File getArtistImage(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        return getArtistImageWithHttpInfo(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer)
                .getData();
    }

    /**
     * Get artist image by name.
     * 
     * @param name Artist name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> getArtistImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        // Check required parameters
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling getArtistImage");
        }
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling getArtistImage");
        }
        if (imageIndex == null) {
            throw new ApiException(400, "Missing the required parameter 'imageIndex' when calling getArtistImage");
        }

        // Path parameters
        String localVarPath = "/Artists/{name}/Images/{imageType}/{imageIndex}"
                .replaceAll("\\{name}", apiClient.escapeString(name.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()))
                .replaceAll("\\{imageIndex}", apiClient.escapeString(imageIndex.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "percentPlayed", percentPlayed));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "unplayedCount", unplayedCount));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillWidth", fillWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillHeight", fillHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "blur", blur));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "foregroundLayer", foregroundLayer));

        String localVarAccept = apiClient.selectHeaderAccept("image/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("ImageApi.getArtistImage", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Get genre image by name.
     * 
     * @param name Genre name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File getGenreImage(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        return getGenreImageWithHttpInfo(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                imageIndex).getData();
    }

    /**
     * Get genre image by name.
     * 
     * @param name Genre name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> getGenreImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        // Check required parameters
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling getGenreImage");
        }
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling getGenreImage");
        }

        // Path parameters
        String localVarPath = "/Genres/{name}/Images/{imageType}"
                .replaceAll("\\{name}", apiClient.escapeString(name.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "percentPlayed", percentPlayed));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "unplayedCount", unplayedCount));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillWidth", fillWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillHeight", fillHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "blur", blur));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "foregroundLayer", foregroundLayer));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageIndex", imageIndex));

        String localVarAccept = apiClient.selectHeaderAccept("image/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("ImageApi.getGenreImage", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Get genre image by name.
     * 
     * @param name Genre name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File getGenreImageByIndex(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        return getGenreImageByIndexWithHttpInfo(name, imageType, imageIndex, tag, format, maxWidth, maxHeight,
                percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor,
                foregroundLayer).getData();
    }

    /**
     * Get genre image by name.
     * 
     * @param name Genre name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> getGenreImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        // Check required parameters
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling getGenreImageByIndex");
        }
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling getGenreImageByIndex");
        }
        if (imageIndex == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageIndex' when calling getGenreImageByIndex");
        }

        // Path parameters
        String localVarPath = "/Genres/{name}/Images/{imageType}/{imageIndex}"
                .replaceAll("\\{name}", apiClient.escapeString(name.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()))
                .replaceAll("\\{imageIndex}", apiClient.escapeString(imageIndex.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "percentPlayed", percentPlayed));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "unplayedCount", unplayedCount));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillWidth", fillWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillHeight", fillHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "blur", blur));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "foregroundLayer", foregroundLayer));

        String localVarAccept = apiClient.selectHeaderAccept("image/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("ImageApi.getGenreImageByIndex", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File getItemImage(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        return getItemImageWithHttpInfo(itemId, imageType, maxWidth, maxHeight, width, height, quality, fillWidth,
                fillHeight, tag, format, percentPlayed, unplayedCount, blur, backgroundColor, foregroundLayer,
                imageIndex).getData();
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> getItemImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getItemImage");
        }
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling getItemImage");
        }

        // Path parameters
        String localVarPath = "/Items/{itemId}/Images/{imageType}"
                .replaceAll("\\{itemId}", apiClient.escapeString(itemId.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillWidth", fillWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillHeight", fillHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "percentPlayed", percentPlayed));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "unplayedCount", unplayedCount));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "blur", blur));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "foregroundLayer", foregroundLayer));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageIndex", imageIndex));

        String localVarAccept = apiClient.selectHeaderAccept("image/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("ImageApi.getItemImage", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param maxWidth The maximum image width to return. (required)
     * @param maxHeight The maximum image height to return. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (required)
     * @param format Determines the output format of the image - original,gif,jpg,png. (required)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (required)
     * @param unplayedCount Optional. Unplayed count overlay to render. (required)
     * @param imageIndex Image index. (required)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File getItemImage2(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, @org.eclipse.jdt.annotation.NonNull Integer width,
            @org.eclipse.jdt.annotation.NonNull Integer height, @org.eclipse.jdt.annotation.NonNull Integer quality,
            @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        return getItemImage2WithHttpInfo(itemId, imageType, maxWidth, maxHeight, tag, format, percentPlayed,
                unplayedCount, imageIndex, width, height, quality, fillWidth, fillHeight, blur, backgroundColor,
                foregroundLayer).getData();
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param maxWidth The maximum image width to return. (required)
     * @param maxHeight The maximum image height to return. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (required)
     * @param format Determines the output format of the image - original,gif,jpg,png. (required)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (required)
     * @param unplayedCount Optional. Unplayed count overlay to render. (required)
     * @param imageIndex Image index. (required)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> getItemImage2WithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, @org.eclipse.jdt.annotation.NonNull Integer width,
            @org.eclipse.jdt.annotation.NonNull Integer height, @org.eclipse.jdt.annotation.NonNull Integer quality,
            @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getItemImage2");
        }
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling getItemImage2");
        }
        if (maxWidth == null) {
            throw new ApiException(400, "Missing the required parameter 'maxWidth' when calling getItemImage2");
        }
        if (maxHeight == null) {
            throw new ApiException(400, "Missing the required parameter 'maxHeight' when calling getItemImage2");
        }
        if (tag == null) {
            throw new ApiException(400, "Missing the required parameter 'tag' when calling getItemImage2");
        }
        if (format == null) {
            throw new ApiException(400, "Missing the required parameter 'format' when calling getItemImage2");
        }
        if (percentPlayed == null) {
            throw new ApiException(400, "Missing the required parameter 'percentPlayed' when calling getItemImage2");
        }
        if (unplayedCount == null) {
            throw new ApiException(400, "Missing the required parameter 'unplayedCount' when calling getItemImage2");
        }
        if (imageIndex == null) {
            throw new ApiException(400, "Missing the required parameter 'imageIndex' when calling getItemImage2");
        }

        // Path parameters
        String localVarPath = "/Items/{itemId}/Images/{imageType}/{imageIndex}/{tag}/{format}/{maxWidth}/{maxHeight}/{percentPlayed}/{unplayedCount}"
                .replaceAll("\\{itemId}", apiClient.escapeString(itemId.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()))
                .replaceAll("\\{maxWidth}", apiClient.escapeString(maxWidth.toString()))
                .replaceAll("\\{maxHeight}", apiClient.escapeString(maxHeight.toString()))
                .replaceAll("\\{tag}", apiClient.escapeString(tag.toString()))
                .replaceAll("\\{format}", apiClient.escapeString(format.toString()))
                .replaceAll("\\{percentPlayed}", apiClient.escapeString(percentPlayed.toString()))
                .replaceAll("\\{unplayedCount}", apiClient.escapeString(unplayedCount.toString()))
                .replaceAll("\\{imageIndex}", apiClient.escapeString(imageIndex.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillWidth", fillWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillHeight", fillHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "blur", blur));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "foregroundLayer", foregroundLayer));

        String localVarAccept = apiClient.selectHeaderAccept("image/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("ImageApi.getItemImage2", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File getItemImageByIndex(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        return getItemImageByIndexWithHttpInfo(itemId, imageType, imageIndex, maxWidth, maxHeight, width, height,
                quality, fillWidth, fillHeight, tag, format, percentPlayed, unplayedCount, blur, backgroundColor,
                foregroundLayer).getData();
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> getItemImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getItemImageByIndex");
        }
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling getItemImageByIndex");
        }
        if (imageIndex == null) {
            throw new ApiException(400, "Missing the required parameter 'imageIndex' when calling getItemImageByIndex");
        }

        // Path parameters
        String localVarPath = "/Items/{itemId}/Images/{imageType}/{imageIndex}"
                .replaceAll("\\{itemId}", apiClient.escapeString(itemId.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()))
                .replaceAll("\\{imageIndex}", apiClient.escapeString(imageIndex.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillWidth", fillWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillHeight", fillHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "percentPlayed", percentPlayed));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "unplayedCount", unplayedCount));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "blur", blur));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "foregroundLayer", foregroundLayer));

        String localVarAccept = apiClient.selectHeaderAccept("image/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("ImageApi.getItemImageByIndex", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Get item image infos.
     * 
     * @param itemId Item id. (required)
     * @return List&lt;ImageInfo&gt;
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
     *                        <td>Item images returned.</td>
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
    public List<ImageInfo> getItemImageInfos(@org.eclipse.jdt.annotation.Nullable UUID itemId) throws ApiException {
        return getItemImageInfosWithHttpInfo(itemId).getData();
    }

    /**
     * Get item image infos.
     * 
     * @param itemId Item id. (required)
     * @return ApiResponse&lt;List&lt;ImageInfo&gt;&gt;
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
     *                        <td>Item images returned.</td>
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
    public ApiResponse<List<ImageInfo>> getItemImageInfosWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId)
            throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getItemImageInfos");
        }

        // Path parameters
        String localVarPath = "/Items/{itemId}/Images".replaceAll("\\{itemId}",
                apiClient.escapeString(itemId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<List<ImageInfo>> localVarReturnType = new GenericType<List<ImageInfo>>() {
        };
        return apiClient.invokeAPI("ImageApi.getItemImageInfos", localVarPath, "GET", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Get music genre image by name.
     * 
     * @param name Music genre name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File getMusicGenreImage(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        return getMusicGenreImageWithHttpInfo(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                imageIndex).getData();
    }

    /**
     * Get music genre image by name.
     * 
     * @param name Music genre name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> getMusicGenreImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        // Check required parameters
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling getMusicGenreImage");
        }
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling getMusicGenreImage");
        }

        // Path parameters
        String localVarPath = "/MusicGenres/{name}/Images/{imageType}"
                .replaceAll("\\{name}", apiClient.escapeString(name.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "percentPlayed", percentPlayed));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "unplayedCount", unplayedCount));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillWidth", fillWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillHeight", fillHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "blur", blur));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "foregroundLayer", foregroundLayer));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageIndex", imageIndex));

        String localVarAccept = apiClient.selectHeaderAccept("image/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("ImageApi.getMusicGenreImage", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Get music genre image by name.
     * 
     * @param name Music genre name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File getMusicGenreImageByIndex(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        return getMusicGenreImageByIndexWithHttpInfo(name, imageType, imageIndex, tag, format, maxWidth, maxHeight,
                percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor,
                foregroundLayer).getData();
    }

    /**
     * Get music genre image by name.
     * 
     * @param name Music genre name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> getMusicGenreImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        // Check required parameters
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling getMusicGenreImageByIndex");
        }
        if (imageType == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageType' when calling getMusicGenreImageByIndex");
        }
        if (imageIndex == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageIndex' when calling getMusicGenreImageByIndex");
        }

        // Path parameters
        String localVarPath = "/MusicGenres/{name}/Images/{imageType}/{imageIndex}"
                .replaceAll("\\{name}", apiClient.escapeString(name.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()))
                .replaceAll("\\{imageIndex}", apiClient.escapeString(imageIndex.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "percentPlayed", percentPlayed));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "unplayedCount", unplayedCount));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillWidth", fillWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillHeight", fillHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "blur", blur));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "foregroundLayer", foregroundLayer));

        String localVarAccept = apiClient.selectHeaderAccept("image/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("ImageApi.getMusicGenreImageByIndex", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Get person image by name.
     * 
     * @param name Person name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File getPersonImage(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        return getPersonImageWithHttpInfo(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                imageIndex).getData();
    }

    /**
     * Get person image by name.
     * 
     * @param name Person name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> getPersonImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        // Check required parameters
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling getPersonImage");
        }
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling getPersonImage");
        }

        // Path parameters
        String localVarPath = "/Persons/{name}/Images/{imageType}"
                .replaceAll("\\{name}", apiClient.escapeString(name.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "percentPlayed", percentPlayed));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "unplayedCount", unplayedCount));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillWidth", fillWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillHeight", fillHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "blur", blur));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "foregroundLayer", foregroundLayer));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageIndex", imageIndex));

        String localVarAccept = apiClient.selectHeaderAccept("image/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("ImageApi.getPersonImage", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Get person image by name.
     * 
     * @param name Person name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File getPersonImageByIndex(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        return getPersonImageByIndexWithHttpInfo(name, imageType, imageIndex, tag, format, maxWidth, maxHeight,
                percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor,
                foregroundLayer).getData();
    }

    /**
     * Get person image by name.
     * 
     * @param name Person name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> getPersonImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        // Check required parameters
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling getPersonImageByIndex");
        }
        if (imageType == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageType' when calling getPersonImageByIndex");
        }
        if (imageIndex == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageIndex' when calling getPersonImageByIndex");
        }

        // Path parameters
        String localVarPath = "/Persons/{name}/Images/{imageType}/{imageIndex}"
                .replaceAll("\\{name}", apiClient.escapeString(name.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()))
                .replaceAll("\\{imageIndex}", apiClient.escapeString(imageIndex.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "percentPlayed", percentPlayed));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "unplayedCount", unplayedCount));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillWidth", fillWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillHeight", fillHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "blur", blur));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "foregroundLayer", foregroundLayer));

        String localVarAccept = apiClient.selectHeaderAccept("image/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("ImageApi.getPersonImageByIndex", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Generates or gets the splashscreen.
     * 
     * @param tag Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Blur image. (optional)
     * @param backgroundColor Apply a background color for transparent images. (optional)
     * @param foregroundLayer Apply a foreground layer on top of the image. (optional)
     * @param quality Quality setting, from 0-100. (optional, default to 90)
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
     *                        <td>Splashscreen returned successfully.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File getSplashscreen(@org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer,
            @org.eclipse.jdt.annotation.NonNull Integer quality) throws ApiException {
        return getSplashscreenWithHttpInfo(tag, format, maxWidth, maxHeight, width, height, fillWidth, fillHeight, blur,
                backgroundColor, foregroundLayer, quality).getData();
    }

    /**
     * Generates or gets the splashscreen.
     * 
     * @param tag Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Blur image. (optional)
     * @param backgroundColor Apply a background color for transparent images. (optional)
     * @param foregroundLayer Apply a foreground layer on top of the image. (optional)
     * @param quality Quality setting, from 0-100. (optional, default to 90)
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
     *                        <td>Splashscreen returned successfully.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> getSplashscreenWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer,
            @org.eclipse.jdt.annotation.NonNull Integer quality) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillWidth", fillWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillHeight", fillHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "blur", blur));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "foregroundLayer", foregroundLayer));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));

        String localVarAccept = apiClient.selectHeaderAccept("image/*");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("ImageApi.getSplashscreen", "/Branding/Splashscreen", "GET", localVarQueryParams,
                null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Get studio image by name.
     * 
     * @param name Studio name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File getStudioImage(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        return getStudioImageWithHttpInfo(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                imageIndex).getData();
    }

    /**
     * Get studio image by name.
     * 
     * @param name Studio name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> getStudioImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        // Check required parameters
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling getStudioImage");
        }
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling getStudioImage");
        }

        // Path parameters
        String localVarPath = "/Studios/{name}/Images/{imageType}"
                .replaceAll("\\{name}", apiClient.escapeString(name.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "percentPlayed", percentPlayed));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "unplayedCount", unplayedCount));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillWidth", fillWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillHeight", fillHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "blur", blur));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "foregroundLayer", foregroundLayer));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageIndex", imageIndex));

        String localVarAccept = apiClient.selectHeaderAccept("image/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("ImageApi.getStudioImage", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Get studio image by name.
     * 
     * @param name Studio name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File getStudioImageByIndex(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        return getStudioImageByIndexWithHttpInfo(name, imageType, imageIndex, tag, format, maxWidth, maxHeight,
                percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor,
                foregroundLayer).getData();
    }

    /**
     * Get studio image by name.
     * 
     * @param name Studio name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> getStudioImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        // Check required parameters
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling getStudioImageByIndex");
        }
        if (imageType == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageType' when calling getStudioImageByIndex");
        }
        if (imageIndex == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageIndex' when calling getStudioImageByIndex");
        }

        // Path parameters
        String localVarPath = "/Studios/{name}/Images/{imageType}/{imageIndex}"
                .replaceAll("\\{name}", apiClient.escapeString(name.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()))
                .replaceAll("\\{imageIndex}", apiClient.escapeString(imageIndex.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "percentPlayed", percentPlayed));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "unplayedCount", unplayedCount));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillWidth", fillWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillHeight", fillHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "blur", blur));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "foregroundLayer", foregroundLayer));

        String localVarAccept = apiClient.selectHeaderAccept("image/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("ImageApi.getStudioImageByIndex", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Get user profile image.
     * 
     * @param userId User id. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>400</td>
     *                        <td>User id not provided.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File getUserImage(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        return getUserImageWithHttpInfo(userId, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width,
                height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer, imageIndex).getData();
    }

    /**
     * Get user profile image.
     * 
     * @param userId User id. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>400</td>
     *                        <td>User id not provided.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> getUserImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "percentPlayed", percentPlayed));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "unplayedCount", unplayedCount));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillWidth", fillWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillHeight", fillHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "blur", blur));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "foregroundLayer", foregroundLayer));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageIndex", imageIndex));

        String localVarAccept = apiClient.selectHeaderAccept("image/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("ImageApi.getUserImage", "/UserImage", "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Get artist image by name.
     * 
     * @param name Artist name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File headArtistImage(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        return headArtistImageWithHttpInfo(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer)
                .getData();
    }

    /**
     * Get artist image by name.
     * 
     * @param name Artist name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> headArtistImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        // Check required parameters
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling headArtistImage");
        }
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling headArtistImage");
        }
        if (imageIndex == null) {
            throw new ApiException(400, "Missing the required parameter 'imageIndex' when calling headArtistImage");
        }

        // Path parameters
        String localVarPath = "/Artists/{name}/Images/{imageType}/{imageIndex}"
                .replaceAll("\\{name}", apiClient.escapeString(name.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()))
                .replaceAll("\\{imageIndex}", apiClient.escapeString(imageIndex.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "percentPlayed", percentPlayed));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "unplayedCount", unplayedCount));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillWidth", fillWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillHeight", fillHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "blur", blur));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "foregroundLayer", foregroundLayer));

        String localVarAccept = apiClient.selectHeaderAccept("image/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("ImageApi.headArtistImage", localVarPath, "HEAD", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Get genre image by name.
     * 
     * @param name Genre name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File headGenreImage(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        return headGenreImageWithHttpInfo(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                imageIndex).getData();
    }

    /**
     * Get genre image by name.
     * 
     * @param name Genre name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> headGenreImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        // Check required parameters
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling headGenreImage");
        }
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling headGenreImage");
        }

        // Path parameters
        String localVarPath = "/Genres/{name}/Images/{imageType}"
                .replaceAll("\\{name}", apiClient.escapeString(name.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "percentPlayed", percentPlayed));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "unplayedCount", unplayedCount));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillWidth", fillWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillHeight", fillHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "blur", blur));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "foregroundLayer", foregroundLayer));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageIndex", imageIndex));

        String localVarAccept = apiClient.selectHeaderAccept("image/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("ImageApi.headGenreImage", localVarPath, "HEAD", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Get genre image by name.
     * 
     * @param name Genre name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File headGenreImageByIndex(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        return headGenreImageByIndexWithHttpInfo(name, imageType, imageIndex, tag, format, maxWidth, maxHeight,
                percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor,
                foregroundLayer).getData();
    }

    /**
     * Get genre image by name.
     * 
     * @param name Genre name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> headGenreImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        // Check required parameters
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling headGenreImageByIndex");
        }
        if (imageType == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageType' when calling headGenreImageByIndex");
        }
        if (imageIndex == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageIndex' when calling headGenreImageByIndex");
        }

        // Path parameters
        String localVarPath = "/Genres/{name}/Images/{imageType}/{imageIndex}"
                .replaceAll("\\{name}", apiClient.escapeString(name.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()))
                .replaceAll("\\{imageIndex}", apiClient.escapeString(imageIndex.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "percentPlayed", percentPlayed));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "unplayedCount", unplayedCount));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillWidth", fillWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillHeight", fillHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "blur", blur));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "foregroundLayer", foregroundLayer));

        String localVarAccept = apiClient.selectHeaderAccept("image/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("ImageApi.headGenreImageByIndex", localVarPath, "HEAD", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File headItemImage(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        return headItemImageWithHttpInfo(itemId, imageType, maxWidth, maxHeight, width, height, quality, fillWidth,
                fillHeight, tag, format, percentPlayed, unplayedCount, blur, backgroundColor, foregroundLayer,
                imageIndex).getData();
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> headItemImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling headItemImage");
        }
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling headItemImage");
        }

        // Path parameters
        String localVarPath = "/Items/{itemId}/Images/{imageType}"
                .replaceAll("\\{itemId}", apiClient.escapeString(itemId.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillWidth", fillWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillHeight", fillHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "percentPlayed", percentPlayed));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "unplayedCount", unplayedCount));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "blur", blur));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "foregroundLayer", foregroundLayer));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageIndex", imageIndex));

        String localVarAccept = apiClient.selectHeaderAccept("image/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("ImageApi.headItemImage", localVarPath, "HEAD", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param maxWidth The maximum image width to return. (required)
     * @param maxHeight The maximum image height to return. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (required)
     * @param format Determines the output format of the image - original,gif,jpg,png. (required)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (required)
     * @param unplayedCount Optional. Unplayed count overlay to render. (required)
     * @param imageIndex Image index. (required)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File headItemImage2(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, @org.eclipse.jdt.annotation.NonNull Integer width,
            @org.eclipse.jdt.annotation.NonNull Integer height, @org.eclipse.jdt.annotation.NonNull Integer quality,
            @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        return headItemImage2WithHttpInfo(itemId, imageType, maxWidth, maxHeight, tag, format, percentPlayed,
                unplayedCount, imageIndex, width, height, quality, fillWidth, fillHeight, blur, backgroundColor,
                foregroundLayer).getData();
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param maxWidth The maximum image width to return. (required)
     * @param maxHeight The maximum image height to return. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (required)
     * @param format Determines the output format of the image - original,gif,jpg,png. (required)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (required)
     * @param unplayedCount Optional. Unplayed count overlay to render. (required)
     * @param imageIndex Image index. (required)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> headItemImage2WithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, @org.eclipse.jdt.annotation.NonNull Integer width,
            @org.eclipse.jdt.annotation.NonNull Integer height, @org.eclipse.jdt.annotation.NonNull Integer quality,
            @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling headItemImage2");
        }
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling headItemImage2");
        }
        if (maxWidth == null) {
            throw new ApiException(400, "Missing the required parameter 'maxWidth' when calling headItemImage2");
        }
        if (maxHeight == null) {
            throw new ApiException(400, "Missing the required parameter 'maxHeight' when calling headItemImage2");
        }
        if (tag == null) {
            throw new ApiException(400, "Missing the required parameter 'tag' when calling headItemImage2");
        }
        if (format == null) {
            throw new ApiException(400, "Missing the required parameter 'format' when calling headItemImage2");
        }
        if (percentPlayed == null) {
            throw new ApiException(400, "Missing the required parameter 'percentPlayed' when calling headItemImage2");
        }
        if (unplayedCount == null) {
            throw new ApiException(400, "Missing the required parameter 'unplayedCount' when calling headItemImage2");
        }
        if (imageIndex == null) {
            throw new ApiException(400, "Missing the required parameter 'imageIndex' when calling headItemImage2");
        }

        // Path parameters
        String localVarPath = "/Items/{itemId}/Images/{imageType}/{imageIndex}/{tag}/{format}/{maxWidth}/{maxHeight}/{percentPlayed}/{unplayedCount}"
                .replaceAll("\\{itemId}", apiClient.escapeString(itemId.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()))
                .replaceAll("\\{maxWidth}", apiClient.escapeString(maxWidth.toString()))
                .replaceAll("\\{maxHeight}", apiClient.escapeString(maxHeight.toString()))
                .replaceAll("\\{tag}", apiClient.escapeString(tag.toString()))
                .replaceAll("\\{format}", apiClient.escapeString(format.toString()))
                .replaceAll("\\{percentPlayed}", apiClient.escapeString(percentPlayed.toString()))
                .replaceAll("\\{unplayedCount}", apiClient.escapeString(unplayedCount.toString()))
                .replaceAll("\\{imageIndex}", apiClient.escapeString(imageIndex.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillWidth", fillWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillHeight", fillHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "blur", blur));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "foregroundLayer", foregroundLayer));

        String localVarAccept = apiClient.selectHeaderAccept("image/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("ImageApi.headItemImage2", localVarPath, "HEAD", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File headItemImageByIndex(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        return headItemImageByIndexWithHttpInfo(itemId, imageType, imageIndex, maxWidth, maxHeight, width, height,
                quality, fillWidth, fillHeight, tag, format, percentPlayed, unplayedCount, blur, backgroundColor,
                foregroundLayer).getData();
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> headItemImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling headItemImageByIndex");
        }
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling headItemImageByIndex");
        }
        if (imageIndex == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageIndex' when calling headItemImageByIndex");
        }

        // Path parameters
        String localVarPath = "/Items/{itemId}/Images/{imageType}/{imageIndex}"
                .replaceAll("\\{itemId}", apiClient.escapeString(itemId.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()))
                .replaceAll("\\{imageIndex}", apiClient.escapeString(imageIndex.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillWidth", fillWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillHeight", fillHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "percentPlayed", percentPlayed));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "unplayedCount", unplayedCount));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "blur", blur));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "foregroundLayer", foregroundLayer));

        String localVarAccept = apiClient.selectHeaderAccept("image/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("ImageApi.headItemImageByIndex", localVarPath, "HEAD", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Get music genre image by name.
     * 
     * @param name Music genre name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File headMusicGenreImage(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        return headMusicGenreImageWithHttpInfo(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                imageIndex).getData();
    }

    /**
     * Get music genre image by name.
     * 
     * @param name Music genre name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> headMusicGenreImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        // Check required parameters
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling headMusicGenreImage");
        }
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling headMusicGenreImage");
        }

        // Path parameters
        String localVarPath = "/MusicGenres/{name}/Images/{imageType}"
                .replaceAll("\\{name}", apiClient.escapeString(name.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "percentPlayed", percentPlayed));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "unplayedCount", unplayedCount));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillWidth", fillWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillHeight", fillHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "blur", blur));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "foregroundLayer", foregroundLayer));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageIndex", imageIndex));

        String localVarAccept = apiClient.selectHeaderAccept("image/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("ImageApi.headMusicGenreImage", localVarPath, "HEAD", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Get music genre image by name.
     * 
     * @param name Music genre name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File headMusicGenreImageByIndex(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        return headMusicGenreImageByIndexWithHttpInfo(name, imageType, imageIndex, tag, format, maxWidth, maxHeight,
                percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor,
                foregroundLayer).getData();
    }

    /**
     * Get music genre image by name.
     * 
     * @param name Music genre name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> headMusicGenreImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        // Check required parameters
        if (name == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'name' when calling headMusicGenreImageByIndex");
        }
        if (imageType == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageType' when calling headMusicGenreImageByIndex");
        }
        if (imageIndex == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageIndex' when calling headMusicGenreImageByIndex");
        }

        // Path parameters
        String localVarPath = "/MusicGenres/{name}/Images/{imageType}/{imageIndex}"
                .replaceAll("\\{name}", apiClient.escapeString(name.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()))
                .replaceAll("\\{imageIndex}", apiClient.escapeString(imageIndex.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "percentPlayed", percentPlayed));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "unplayedCount", unplayedCount));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillWidth", fillWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillHeight", fillHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "blur", blur));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "foregroundLayer", foregroundLayer));

        String localVarAccept = apiClient.selectHeaderAccept("image/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("ImageApi.headMusicGenreImageByIndex", localVarPath, "HEAD", localVarQueryParams,
                null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Get person image by name.
     * 
     * @param name Person name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File headPersonImage(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        return headPersonImageWithHttpInfo(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                imageIndex).getData();
    }

    /**
     * Get person image by name.
     * 
     * @param name Person name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> headPersonImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        // Check required parameters
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling headPersonImage");
        }
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling headPersonImage");
        }

        // Path parameters
        String localVarPath = "/Persons/{name}/Images/{imageType}"
                .replaceAll("\\{name}", apiClient.escapeString(name.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "percentPlayed", percentPlayed));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "unplayedCount", unplayedCount));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillWidth", fillWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillHeight", fillHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "blur", blur));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "foregroundLayer", foregroundLayer));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageIndex", imageIndex));

        String localVarAccept = apiClient.selectHeaderAccept("image/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("ImageApi.headPersonImage", localVarPath, "HEAD", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Get person image by name.
     * 
     * @param name Person name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File headPersonImageByIndex(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        return headPersonImageByIndexWithHttpInfo(name, imageType, imageIndex, tag, format, maxWidth, maxHeight,
                percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor,
                foregroundLayer).getData();
    }

    /**
     * Get person image by name.
     * 
     * @param name Person name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> headPersonImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        // Check required parameters
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling headPersonImageByIndex");
        }
        if (imageType == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageType' when calling headPersonImageByIndex");
        }
        if (imageIndex == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageIndex' when calling headPersonImageByIndex");
        }

        // Path parameters
        String localVarPath = "/Persons/{name}/Images/{imageType}/{imageIndex}"
                .replaceAll("\\{name}", apiClient.escapeString(name.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()))
                .replaceAll("\\{imageIndex}", apiClient.escapeString(imageIndex.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "percentPlayed", percentPlayed));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "unplayedCount", unplayedCount));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillWidth", fillWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillHeight", fillHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "blur", blur));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "foregroundLayer", foregroundLayer));

        String localVarAccept = apiClient.selectHeaderAccept("image/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("ImageApi.headPersonImageByIndex", localVarPath, "HEAD", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Get studio image by name.
     * 
     * @param name Studio name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File headStudioImage(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        return headStudioImageWithHttpInfo(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                imageIndex).getData();
    }

    /**
     * Get studio image by name.
     * 
     * @param name Studio name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> headStudioImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        // Check required parameters
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling headStudioImage");
        }
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling headStudioImage");
        }

        // Path parameters
        String localVarPath = "/Studios/{name}/Images/{imageType}"
                .replaceAll("\\{name}", apiClient.escapeString(name.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "percentPlayed", percentPlayed));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "unplayedCount", unplayedCount));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillWidth", fillWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillHeight", fillHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "blur", blur));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "foregroundLayer", foregroundLayer));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageIndex", imageIndex));

        String localVarAccept = apiClient.selectHeaderAccept("image/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("ImageApi.headStudioImage", localVarPath, "HEAD", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Get studio image by name.
     * 
     * @param name Studio name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File headStudioImageByIndex(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        return headStudioImageByIndexWithHttpInfo(name, imageType, imageIndex, tag, format, maxWidth, maxHeight,
                percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor,
                foregroundLayer).getData();
    }

    /**
     * Get studio image by name.
     * 
     * @param name Studio name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> headStudioImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer) throws ApiException {
        // Check required parameters
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling headStudioImageByIndex");
        }
        if (imageType == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageType' when calling headStudioImageByIndex");
        }
        if (imageIndex == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageIndex' when calling headStudioImageByIndex");
        }

        // Path parameters
        String localVarPath = "/Studios/{name}/Images/{imageType}/{imageIndex}"
                .replaceAll("\\{name}", apiClient.escapeString(name.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()))
                .replaceAll("\\{imageIndex}", apiClient.escapeString(imageIndex.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "percentPlayed", percentPlayed));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "unplayedCount", unplayedCount));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillWidth", fillWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillHeight", fillHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "blur", blur));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "foregroundLayer", foregroundLayer));

        String localVarAccept = apiClient.selectHeaderAccept("image/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("ImageApi.headStudioImageByIndex", localVarPath, "HEAD", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Get user profile image.
     * 
     * @param userId User id. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>400</td>
     *                        <td>User id not provided.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File headUserImage(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        return headUserImageWithHttpInfo(userId, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width,
                height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer, imageIndex).getData();
    }

    /**
     * Get user profile image.
     * 
     * @param userId User id. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
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
     *                        <td>Image stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>400</td>
     *                        <td>User id not provided.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> headUserImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer width, @org.eclipse.jdt.annotation.NonNull Integer height,
            @org.eclipse.jdt.annotation.NonNull Integer quality, @org.eclipse.jdt.annotation.NonNull Integer fillWidth,
            @org.eclipse.jdt.annotation.NonNull Integer fillHeight, @org.eclipse.jdt.annotation.NonNull Integer blur,
            @org.eclipse.jdt.annotation.NonNull String backgroundColor,
            @org.eclipse.jdt.annotation.NonNull String foregroundLayer,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "percentPlayed", percentPlayed));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "unplayedCount", unplayedCount));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillWidth", fillWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fillHeight", fillHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "blur", blur));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "foregroundLayer", foregroundLayer));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageIndex", imageIndex));

        String localVarAccept = apiClient.selectHeaderAccept("image/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("ImageApi.headUserImage", "/UserImage", "HEAD", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Sets the user image.
     * 
     * @param userId User Id. (optional)
     * @param body (optional)
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
     *                        <td>Image updated.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>400</td>
     *                        <td>Bad Request</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>User does not have permission to delete the image.</td>
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
     *                        </table>
     */
    public void postUserImage(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull File body) throws ApiException {
        postUserImageWithHttpInfo(userId, body);
    }

    /**
     * Sets the user image.
     * 
     * @param userId User Id. (optional)
     * @param body (optional)
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
     *                        <td>Image updated.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>400</td>
     *                        <td>Bad Request</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>User does not have permission to delete the image.</td>
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
     *                        </table>
     */
    public ApiResponse<Void> postUserImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull File body) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("image/*");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("ImageApi.postUserImage", "/UserImage", "POST", localVarQueryParams, body,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Set item image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param body (optional)
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
     *                        <td>Image saved.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>400</td>
     *                        <td>Bad Request</td>
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
    public void setItemImage(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType, @org.eclipse.jdt.annotation.NonNull File body)
            throws ApiException {
        setItemImageWithHttpInfo(itemId, imageType, body);
    }

    /**
     * Set item image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param body (optional)
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
     *                        <td>Image saved.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>400</td>
     *                        <td>Bad Request</td>
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
    public ApiResponse<Void> setItemImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType, @org.eclipse.jdt.annotation.NonNull File body)
            throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling setItemImage");
        }
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling setItemImage");
        }

        // Path parameters
        String localVarPath = "/Items/{itemId}/Images/{imageType}"
                .replaceAll("\\{itemId}", apiClient.escapeString(itemId.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("image/*");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("ImageApi.setItemImage", localVarPath, "POST", new ArrayList<>(), body,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Set item image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex (Unused) Image index. (required)
     * @param body (optional)
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
     *                        <td>Image saved.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>400</td>
     *                        <td>Bad Request</td>
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
    public void setItemImageByIndex(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, @org.eclipse.jdt.annotation.NonNull File body)
            throws ApiException {
        setItemImageByIndexWithHttpInfo(itemId, imageType, imageIndex, body);
    }

    /**
     * Set item image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex (Unused) Image index. (required)
     * @param body (optional)
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
     *                        <td>Image saved.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>400</td>
     *                        <td>Bad Request</td>
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
    public ApiResponse<Void> setItemImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, @org.eclipse.jdt.annotation.NonNull File body)
            throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling setItemImageByIndex");
        }
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling setItemImageByIndex");
        }
        if (imageIndex == null) {
            throw new ApiException(400, "Missing the required parameter 'imageIndex' when calling setItemImageByIndex");
        }

        // Path parameters
        String localVarPath = "/Items/{itemId}/Images/{imageType}/{imageIndex}"
                .replaceAll("\\{itemId}", apiClient.escapeString(itemId.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()))
                .replaceAll("\\{imageIndex}", apiClient.escapeString(imageIndex.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("image/*");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("ImageApi.setItemImageByIndex", localVarPath, "POST", new ArrayList<>(), body,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Updates the index for an item image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Old image index. (required)
     * @param newIndex New image index. (required)
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
     *                        <td>Image index updated.</td>
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
    public void updateItemImageIndex(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex,
            @org.eclipse.jdt.annotation.Nullable Integer newIndex) throws ApiException {
        updateItemImageIndexWithHttpInfo(itemId, imageType, imageIndex, newIndex);
    }

    /**
     * Updates the index for an item image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Old image index. (required)
     * @param newIndex New image index. (required)
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
     *                        <td>Image index updated.</td>
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
    public ApiResponse<Void> updateItemImageIndexWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex,
            @org.eclipse.jdt.annotation.Nullable Integer newIndex) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling updateItemImageIndex");
        }
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling updateItemImageIndex");
        }
        if (imageIndex == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageIndex' when calling updateItemImageIndex");
        }
        if (newIndex == null) {
            throw new ApiException(400, "Missing the required parameter 'newIndex' when calling updateItemImageIndex");
        }

        // Path parameters
        String localVarPath = "/Items/{itemId}/Images/{imageType}/{imageIndex}/Index"
                .replaceAll("\\{itemId}", apiClient.escapeString(itemId.toString()))
                .replaceAll("\\{imageType}", apiClient.escapeString(imageType.toString()))
                .replaceAll("\\{imageIndex}", apiClient.escapeString(imageIndex.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "newIndex", newIndex));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("ImageApi.updateItemImageIndex", localVarPath, "POST", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Uploads a custom splashscreen. The body is expected to the image contents base64 encoded.
     * 
     * @param body (optional)
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
     *                        <td>Successfully uploaded new splashscreen.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>400</td>
     *                        <td>Error reading MimeType from uploaded image.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>User does not have permission to upload splashscreen..</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void uploadCustomSplashscreen(@org.eclipse.jdt.annotation.NonNull File body) throws ApiException {
        uploadCustomSplashscreenWithHttpInfo(body);
    }

    /**
     * Uploads a custom splashscreen. The body is expected to the image contents base64 encoded.
     * 
     * @param body (optional)
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
     *                        <td>Successfully uploaded new splashscreen.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>400</td>
     *                        <td>Error reading MimeType from uploaded image.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>User does not have permission to upload splashscreen..</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> uploadCustomSplashscreenWithHttpInfo(@org.eclipse.jdt.annotation.NonNull File body)
            throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("image/*");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("ImageApi.uploadCustomSplashscreen", "/Branding/Splashscreen", "POST",
                new ArrayList<>(), body, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, null, false);
    }
}
