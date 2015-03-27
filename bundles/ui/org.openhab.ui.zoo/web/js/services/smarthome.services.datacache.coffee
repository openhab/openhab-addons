angular.module('SmartHome.services.datacache',  []).factory 'DataCache', ['$q', ($q) ->

	new class DataCache
		@cacheEnabled = no
		@dirty = no

		init: (@data, @remoteService) ->
			return @

		getAll: (callback, refresh) ->
			deferred = $q.defer()
			@remoteService.getAll (data) ->
				if !@cacheEnabled or data.length isnt @data?.length or @dirty or refresh
					@data = data
					deferred.resolve data
				else
					deferred.resolve 'No Update'
			successFn = (res) -> if res isnt 'No update' then callback?(res)
			notifyCallback = (res) -> callback?(res) or null
			deferred.promise.then successFn, null, notifyCallback
			if @cacheEnabled then deferred.notify @data

		getOne: (condition, callback, refresh) ->
			element = @find condition
			if element and !@dirty and !refresh
				callback element
			else
				onSuccess = (res) => callback?(@find(condition))
				onError = -> callback?(null)
				@getAll(null, true).then onSuccess , onError

		find: (condition) ->
			for element in @data when condition(element)
				return element

		add: (element) ->
			@data.push element

		remove: (element) ->
			for element, idx in @data when condition(element)
				delete @data[idx]

		setDirty: (@dirty) ->


]