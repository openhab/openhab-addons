angular.module('ZooApp', [
	'ngRoute',
	'ngResource',
	'angularSpinner',
	'SmartHome.services',
	'ZooLib.controllers'
]).config(function ($stateProvider, $urlRouterProvider, usSpinnerConfigProvider) {

	//$locationProvider.html5Mode(false).hashPrefix('!');
	$urlRouterProvider.otherwise("/login");

	usSpinnerConfigProvider.setDefaults({color: 'blue'});

	$routeProvider.
		when('/login', {
			templateUrl: 'partials/login.html',
			controller: 'LoginController',
			controllerAs: 'ctrl'
		})
		.when('/room', {
			templateUrl: 'partials/room.html',
			controller: 'RoomController',
			controllerAs: 'ctrl'
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
			controller: 'DiscoverController',
			templateUrl: 'partials/discover.html'
		})
		.when('/manual-setup', {
			templateUrl: 'partials/manual-setup.html'
		})
		.when('/groups', {
			templateUrl: 'partials/groups.html'
		})
		.otherwise({redirectTo: '/login'});

}).run(['$location', '$rootScope', '$log', 'itemService', function ($location, $rootScope, $log, itemService) {

	// TODO Get rid of this
	$rootScope.data = {};

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

	itemService.getByName({itemName:'gRooms'}, function (rooms) {
		$rootScope.smarthome = {rooms : rooms.members};
	});

	// TODO Move to directive
	//itemService.getByName({itemName:'gWeather'}, function(weatherItems) {
	//	console.log(weatherItems);
	//	weatherItems.members.forEach(function (item) {
	//		if (item.type === 'GroupItem') return;
	//		$rootScope.weather = {
	//			locationLabel: item.label,
	//			date: new Date(),
	//			temperature: item.state
	//		};
	//	});
	//});




}]);