angular.module('ZooLib.controllers.discover', []).controller 'DiscoverController', ($scope, $log, discoveryService, discoveryResultRepository, inboxService) ->

	@scanResults = []
	@approveData = []
	@discoverableBindings = []

	@scan = ->
		inboxService.scan()

	@approve = (thingUID) ->
		label = @approveData[thingUID].label;
		inboxService.approve {thingUID}, label, (arg) ->
			$log.info "Approved #{thingUID}", arg
			@updateScanResults()


	@ignore = (thing) ->
		$log.warn 'Not implemented, should ignore ', thing, @thingToApprove


	@updateScanResults = ->
		discoveryResultRepository.getAll (data) ->
			$log.info data
			@scanResults = data
			@approveData = []
			for thing in data
				@approveData[thing.thingUID] = angular.copy thing
		, (err) ->
			$log.error 'Error on fetching inbox', err

	@updateDiscoverableBindings = ->
		discoveryService.getAll (bindings) ->
			@discoverableBindings = bindings

	@updateDiscoverableBindings()
	@updateScanResults()

	return;
