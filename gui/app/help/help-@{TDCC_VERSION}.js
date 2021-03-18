angular.module('main').config(['$routeProvider', function($routeProvider) {
    $routeProvider
        .when('/help', {
            templateUrl: 'help/help-@{TDCC_VERSION}.html',
            controller: 'HelpCtrl'
        });
}])

.controller('HelpCtrl', function(){});


