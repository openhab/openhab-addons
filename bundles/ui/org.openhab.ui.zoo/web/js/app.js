angular.module('Zoo', [
	'Zoo.controllers',
	'ngRoute',
	'ngResource',
	'SmartHome'
]).config(['$routeProvider', '$locationProvider', function ($routeProvider, $locationProvider) {

	$locationProvider.html5Mode(false).hashPrefix('!');

	$routeProvider.
		when('/login', {
			templateUrl: 'partials/login.html',
			controller: 'LoginController'
		})
		.when('/room', {
			templateUrl: 'partials/room.html'
			//controller: 'RoomController'
		})
		.when('/access', {
			templateUrl: 'partials/access.html'
		})
		.when('/alerts', {
			templateUrl: 'partials/alerts.html'
		})
		.when('/cctv', {
			templateUrl: 'partials/cctv.html'
		})
		.when('/cost-settings', {
			templateUrl: 'partials/cost-settings.html'
		})
		.when('/energy-center', {
			templateUrl: 'partials/energy-center.html'
		})
		.when('/profile', {
			templateUrl: 'partials/profile.html'
		})
		.when('/alarm', {
			templateUrl: 'partials/alarm.html'
		})
		.when('/profile', {
			templateUrl: 'partials/profile.html'
		})
		.when('/intercom', {
			templateUrl: 'partials/intercom.html'
		})
		.when('/discover', {
			templateUrl: 'partials/discover.html'
		})
		.when('/manual-setup', {
			templateUrl: 'partials/manual-setup.html'
		})
		.when('/groups', {
			templateUrl: 'partials/groups.html'
		})
		.otherwise({redirectTo: '/login'});

}]).run(['$location', '$rootScope', 'thingService', 'itemService', function ($location, $rootScope, thingService, itemService) {

	$rootScope.$on('$routeChangeSuccess', function () {
		// Strip slash in front of current path:
		$rootScope.activeSection = $location.path().substr(1);
	});

	$rootScope.leftSidebarOpen = false;
	$rootScope.isBlackout = false;

	$rootScope.toggleSidebar = function () {
		$rootScope.leftSidebarOpen = !$rootScope.leftSidebarOpen;
		$rootScope.isBlackout = !$rootScope.isBlackout;
	};

	// TODO Move to directive
	thingService.getByUid({thingUID:'yahooweather:weather:20066544'}, function (thing) {
		$rootScope.weather = {locationLabel: thing.item.label, date: new Date()};
		thing.item.members.forEach(function (item) {
			if (item.category === 'Temperature') {
				$rootScope.weather.temperature = item.state / 10;
			}
		});
	});

	itemService.getByName({itemName:'gRooms'}, function (rooms) {
		$rootScope.smarthome = {rooms : angular.copy(rooms.members)};
	});




}]);