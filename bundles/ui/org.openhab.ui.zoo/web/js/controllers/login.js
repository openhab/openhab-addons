angular.module('Zoo').controller('LoginController', function ($rootScope, $scope, $location, $log) {

	$rootScope.user = {};
	$scope.user = {};

	$scope.login = function () {
		$log.info("User logged in: ", $scope.user);
		$rootScope.user = {name: 'Mr. Johnson', houseId: 1};
		$location.path('room');
	}

});