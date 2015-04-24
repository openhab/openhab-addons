'use strict'

angular.module('ZooLib.controllers.access', []).controller 'AccessController', ($scope, $state, $log) ->

	@doors = [
		(name: 'Front door', state: 'open')
		(name: 'Living room door', state: 'open')
		(name: 'Front door', state: 'open')
		(name: 'Cellar door', state: 'open')
	]

	@allOpen = yes

	$scope.$watch 'doors', (doors) ->
		return unless doors?
		closedDoors = doors.filter((d) -> d.state is 'closed')
		$scope.allOpen = closedDoors?.length > 1
		return
	, yes

	$scope.$watch 'allOpen', (value) ->
		return unless value?
		for door in doors
			door.state = if value then 'open' else 'closed'
		return

	return;
