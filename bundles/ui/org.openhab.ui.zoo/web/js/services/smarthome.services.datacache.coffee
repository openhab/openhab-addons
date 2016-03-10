'use strict'

angular.module('SmartHome.services.datacache',  []).factory 'DataCache', ['$q', ($q) ->

	cache = {}

	new class DataCache
		@cacheEnabled = no
		@dirty = no

		init: (@remoteService) ->
			return @

		getAll: (refresh) ->
			deferred = $q.defer()
			if @dirty or !@cacheEnabled  or refresh or data.length isnt cache?.length
				@remoteService.getAll (data) ->
					cache = angular.copy data
					deferred.resolve data
			else
					deferred.resolve(cache) # 'No Update'
			#successFn = null
			#notifyFn = null
			#if angular.isFunction callback
			#	successFn = (res) -> if not res then callback res
			#	notifyFn = (res) -> callback(res) or null
			#	deferred.promise.then successFn, null, notifyFn
			#if @cacheEnabled then deferred.notify cache
			return deferred.promise

		getOne: (condition, callback, refresh) ->
			element = @find condition
			if element and !@dirty and !refresh
				callback element
			else
				onSuccess = (res) => callback?(@find(condition))
				onError = -> callback?(null)
				@getAll(null, true).then onSuccess , onError

		find: (condition) ->
			for element in cache when condition(element)
				return element

		add: (element) ->
			cache.push element

		remove: (element) ->
			for element, idx in cache when condition(element)
				delete cache[idx]

		setDirty: (@dirty) ->


]