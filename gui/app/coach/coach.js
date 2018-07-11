angular.module('main')

.config(['$routeProvider', 'RESOURCES', function($routeProvider, RESOURCES) {
  $routeProvider
    .when('/coach/parties', {
        templateUrl: 'party/mobile/coachParties.html',
        controller: 'MyPartiesCtrl'
    });
}])

.controller('CoachPartiesCtrl', ['$scope', 'CoachSvc', 'ConfirmDialogSvc', function ($scope, coachSvc, confirmDialogSvc) {
    $scope.coachPartiesContext = {};
    
    coachSvc.getParties().then(function(result) {
        $scope.coachPartiesContext = result.data;
    });
}])

.factory('CoachSvc',['$http', 'RESOURCES', 'ErrorDialogSvc', '$q', function($http, RESOURCES, errorDialogSvc, $q) {    
    var coachSvc={};
    
    coachSvc.getParties = function() {
        return $http.get(RESOURCES.REST_BASE_URL + '/coach/all');
    };
    
    return coachSvc;
}]);