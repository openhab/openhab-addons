'use strict'

angular.module('ZooLib.controllers.login', []).controller 'LoginController', ($rootScope, $scope, $state, $log) ->

	defaultUser = name: 'Mr. Johnson', houseId: 1, isAuthenticated: yes
	@user = {}

	@login = (user) ->
		$log.info "User logged in: ", user
		# TODO Auth stuff
		user.isAuthenticated = yes
		$rootScope.user = user
		#$location.path 'rooms'
		$state.go 'rooms.room'



	@reset = ->
		@user = angular.copy defaultUser

	@reset()

	return;
