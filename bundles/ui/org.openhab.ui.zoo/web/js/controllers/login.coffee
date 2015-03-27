angular.module('ZooLib.controllers.login', []).controller 'LoginController', ($rootScope, $scope, $location, $log) ->

	defaultUser = name: 'Mr. Johnson', houseId: 1
	@user = {}

	@login = (user) ->
		$log.info "User logged in: ", user
		# TODO Auth stuff
		user.isAuthenticated = yes
		$rootScope.user = user
		$location.path 'room'

	@reset = ->
		@user = angular.copy defaultUser

	@reset()

	return;
