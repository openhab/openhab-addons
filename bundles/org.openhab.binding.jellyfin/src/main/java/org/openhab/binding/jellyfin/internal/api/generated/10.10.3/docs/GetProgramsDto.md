

# GetProgramsDto

Get programs dto.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**channelIds** | **List&lt;UUID&gt;** | Gets or sets the channels to return guide information for. |  [optional] |
|**userId** | **UUID** | Gets or sets optional. Filter by user id. |  [optional] |
|**minStartDate** | **OffsetDateTime** | Gets or sets the minimum premiere start date. |  [optional] |
|**hasAired** | **Boolean** | Gets or sets filter by programs that have completed airing, or not. |  [optional] |
|**isAiring** | **Boolean** | Gets or sets filter by programs that are currently airing, or not. |  [optional] |
|**maxStartDate** | **OffsetDateTime** | Gets or sets the maximum premiere start date. |  [optional] |
|**minEndDate** | **OffsetDateTime** | Gets or sets the minimum premiere end date. |  [optional] |
|**maxEndDate** | **OffsetDateTime** | Gets or sets the maximum premiere end date. |  [optional] |
|**isMovie** | **Boolean** | Gets or sets filter for movies. |  [optional] |
|**isSeries** | **Boolean** | Gets or sets filter for series. |  [optional] |
|**isNews** | **Boolean** | Gets or sets filter for news. |  [optional] |
|**isKids** | **Boolean** | Gets or sets filter for kids. |  [optional] |
|**isSports** | **Boolean** | Gets or sets filter for sports. |  [optional] |
|**startIndex** | **Integer** | Gets or sets the record index to start at. All items with a lower index will be dropped from the results. |  [optional] |
|**limit** | **Integer** | Gets or sets the maximum number of records to return. |  [optional] |
|**sortBy** | **List&lt;ItemSortBy&gt;** | Gets or sets specify one or more sort orders, comma delimited. Options: Name, StartDate. |  [optional] |
|**sortOrder** | **List&lt;SortOrder&gt;** | Gets or sets sort order. |  [optional] |
|**genres** | **List&lt;String&gt;** | Gets or sets the genres to return guide information for. |  [optional] |
|**genreIds** | **List&lt;UUID&gt;** | Gets or sets the genre ids to return guide information for. |  [optional] |
|**enableImages** | **Boolean** | Gets or sets include image information in output. |  [optional] |
|**enableTotalRecordCount** | **Boolean** | Gets or sets a value indicating whether retrieve total record count. |  [optional] |
|**imageTypeLimit** | **Integer** | Gets or sets the max number of images to return, per image type. |  [optional] |
|**enableImageTypes** | **List&lt;ImageType&gt;** | Gets or sets the image types to include in the output. |  [optional] |
|**enableUserData** | **Boolean** | Gets or sets include user data. |  [optional] |
|**seriesTimerId** | **String** | Gets or sets filter by series timer id. |  [optional] |
|**librarySeriesId** | **UUID** | Gets or sets filter by library series id. |  [optional] |
|**fields** | **List&lt;ItemFields&gt;** | Gets or sets specify additional fields of information to return in the output. |  [optional] |



