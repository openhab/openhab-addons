'use strict'

angular.module('ZooLib.controllers.room', []).controller 'RoomController', (itemRepository, $log, $scope, $stateParams, $filter, $state) ->
	console.log 'RoomController state params: ', $stateParams

	@rooms = {}
	@itemsActive = {}

	@refreshItems = (force) ->
		itemRepository.getRooms(force).then (rooms) =>
			$log.debug "loaded rooms", rooms
			@rooms = $filter('orderBy')(rooms, 'label')

	@refreshItems().then =>
		if not $stateParams.room and @rooms?
			firstRoom = Object.keys(@rooms).sort()[0]
			$log.debug "Initial redirect to room #{firstRoom}"
			$state.go "rooms.room", active:true, room:firstRoom

	return