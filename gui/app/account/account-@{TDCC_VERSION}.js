angular.module('main')

.config(['$routeProvider', function($routeProvider) {
  $routeProvider
    .when('/account', {
        templateUrl: 'account/account-@{TDCC_VERSION}.html',
        controller: 'AccountCtrl'
    });
}])

.controller('AccountCtrl', ['$scope', '$location', '$route', '$routeParams', 'AccountSvc', function ($scope, $location, $route, $routeParams, accountSvc) {
    $scope.account = {};
    
    accountSvc.getAccountInfo().then(function(result) {
        $scope.user = result.data;
    });

    $scope.updateAccount = function() {
        accountSvc.updateAccount($scope.account).then(function() {
            $location.path("/users/viewAll");
        });
    }; 
}])

.factory('AccountSvc',['$http', 'RESOURCES', 'ErrorDialogSvc', '$q', function($http, RESOURCES, errorDialogSvc, $q) {        
    var AccountSvc={};
    
    AccountSvc.getAccountInfo = function(){
        return $http.get(RESOURCES.REST_BASE_URL + '/account').catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
    
    AccountSvc.updateAccount = function(account){
        return $http.post(RESOURCES.REST_BASE_URL + "/account", account, {silentHttpErrors : true}).catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
  
    return AccountSvc;
}]);