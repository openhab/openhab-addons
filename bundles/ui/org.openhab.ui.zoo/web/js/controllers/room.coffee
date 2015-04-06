'use strict'

angular.module('ZooLib.controllers.room', []).controller 'RoomController', (itemRepository, $log, $scope, $stateParams, $filter, $state) ->
	console.log 'RoomController state params: ', $stateParams

	@rooms = {}
	@itemsActive = {}

	@refreshItems = (force) ->
		itemRepository.getRooms(force).then (rooms) =>
			$log.debug "loaded rooms", rooms
			@rooms = rooms

	@refreshItems()

	$scope.$watch 'rooms', (rooms) ->
		return unless rooms?
		if not $stateParams.room and rooms.length > 0
			$log.debug "Initial redirect to room #{@rooms[0].name}"
			$state.go "rooms.room", active:true, room:@rooms[0].name
	, true

	return