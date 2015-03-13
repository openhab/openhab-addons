angular.module('Zoo', [
	//'Zoo.controllers',
	//'Zoo.controllers.control',
	//'Zoo.controllers.setup',
	//'Zoo.controllers.configuration',
	//'Zoo.services',
	//'Zoo.services.rest',
	//'Zoo.services.repositories',
	'ngRoute',
	'ngResource'
]).config(['$routeProvider', '$locationProvider', function ($routeProvider, $locationProvider) {

	$locationProvider.html5Mode(false).hashPrefix('!');

	$routeProvider.
		when('/login', {
			templateUrl: 'partials/login.html',
			controller: 'LoginController'
		}).
		when('/room', {
			templateUrl: 'partials/room.html',
			controller: 'RoomController'
		}).
		when('/access', {
			templateUrl: 'partials/access.html'
		}).
		when('/alerts', {
			templateUrl: 'partials/alerts.html'
		}).
		when('/cctv', {
			templateUrl: 'partials/cctv.html'
		}).
		when('/cost-settings', {
			templateUrl: 'partials/cost-settings.html'
		}).
		when('/energy-center', {
			templateUrl: 'partials/energy-center.html'
		}).otherwise({redirectTo: '/login'});

}]).run(['$location', '$rootScope', function ($location, $rootScope) {

	$rootScope.isActiveSection = function (path) {
		return $location.path().indexOf(path) > -1;
	};

	$rootScope.leftSidebarOpen = false;
	$rootScope.isBlackout = false;

	$rootScope.toggleSidebar = function () {
		//$(".left-sidebar").toggleClass("left-sidebar-active");
		//$("body").toggleClass("blackout");
		$rootScope.leftSidebarOpen = !$rootScope.leftSidebarOpen;
		$rootScope.isBlackout = !$rootScope.isBlackout;
	};
}]);