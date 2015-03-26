angular.module('Zoo.controllers', []).controller('DiscoverController', ($scope, $log, discoveryService, discoveryResultRepository, inboxService) ->

	$scope.scanResults = []
	$scope.approveData = []
	$scope.discoverableBindings = []

	$scope.scan = ->
		inboxService.scan()

	$scope.approve = (thingUID) ->
		label = $scope.approveData[thingUID].label;
		inboxService.approve {thingUID}, label, (arg) ->
			$log.info "Approved #{thingUID}", arg
			$scope.updateScanResults()


	$scope.ignore = (thing) ->
		$log.warn 'Not implemented, should ignore ', thing, $scope.thingToApprove


	$scope.updateScanResults = ->
		discoveryResultRepository.getAll (data) ->
			$log.info data
			$scope.scanResults = data
			$scope.approveData = []
			for thing in data
				$scope.approveData[thing.thingUID] = angular.copy thing
		, (err) ->
			$log.error 'Error on fetching inbox', err

	$scope.updateDiscoverableBindings = ->
		discoveryService.getAll (bindings) ->
			$scope.discoverableBindings = bindings

	$scope.updateDiscoverableBindings()
	$scope.updateScanResults()
)