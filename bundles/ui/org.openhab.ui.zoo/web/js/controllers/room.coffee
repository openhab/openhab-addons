'use strict'

angular.module('ZooLib.controllers.room', []).controller 'RoomController', (itemRepository, $log, $scope, $stateParams, $filter, $state, $q) ->

	@rooms = {}
	@scenes = []

	@newSceneDefault = name: ''
	@newScene = {}

	#@itemsActive = {}

	@saveNewScene = (closeCallback) ->
		items = @rooms[$state.params.room].members
		if @scenes[@newScene.name]
			$log.error "Scene #{@newScene.name} already exists."
			return
		$log.debug "Saving scene for items:", items
		itemRepository.createNewScene(@newScene.name, items).then =>
			@scenes.push name: @newScene.name, items: angular.copy items
			@newScene = angular.copy @newSceneDefault
			closeCallback()

	@refreshItems = (force) ->
		roomsPromise = $q.defer()
		scenesPromise = $q.defer()

		itemRepository.getRooms(force).then (rooms) =>
			$log.debug "loaded rooms", rooms
			@rooms = $filter('orderBy')(rooms, 'label')
			roomsPromise.resolve rooms

		itemRepository.getScenes().then (scenes) =>
			$log.debug "loaded scenes", scenes
			@scenes = scenes
			scenesPromise.resolve scenes

		$q.all roomsPromise, scenesPromise

	@refreshItems().then =>
		if not $stateParams.room and @rooms?
			firstRoom = Object.keys(@rooms).sort()[0]
			$log.debug "Initial redirect to room #{firstRoom}"
			$state.go "rooms.room", active:true, room:firstRoom
		return

	return