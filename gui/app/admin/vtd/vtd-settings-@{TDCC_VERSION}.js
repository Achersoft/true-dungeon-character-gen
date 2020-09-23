angular.module('main')

.config(['$routeProvider', function($routeProvider) {
  $routeProvider
    .when('/settings/vtd/list', {
        templateUrl: 'admin/vtd/vtdList-@{TDCC_VERSION}.html',
        controller: 'VtdListCtrl'
    })
    .when('/settings/vtd/edit/:id', {
        templateUrl: 'admin/vtd/vtdOptions-@{TDCC_VERSION}.html',
        controller: 'VtdSettingsCtrl'
    });
}])

.controller('VtdListCtrl', ['$scope', 'VtdSettingsSvc', '$route', 'ConfirmDialogSvc', function ($scope, vtdSettingsSvc, $route, confirmDialogSvc) {
    $scope.dungeons = {};
    
    vtdSettingsSvc.getDungeons().then(function (result) {
        $scope.dungeons = result.data;
    });
    
    $scope.deleteDungeon = function(id, name) {
        confirmDialogSvc.confirm("Are you sure you wish to delete dungeon " + name +"?", function(){
            vtdSettingsSvc.deleteDungeon(id).then(function() {
                $route.reload();
            });
        });
    }; 
}])

.controller('VtdSettingsCtrl', ['$scope', '$location', '$route', '$routeParams', 'VtdSettingsSvc', function ($scope, $location, $route, $routeParams, vtdSettingsSvc) {
    $scope.user = {};
    
    vtdSettingsSvc.getUser($routeParams.id).then(function(result) {
        $scope.user = result.data;
    });

    $scope.edit = function() {
        vtdSettingsSvc.editUser($scope.user.id, $scope.user).then(function() {
            $location.path("/users/viewAll");
        });
    }; 
}])

.factory('VtdSettingsSvc',['$http', 'RESOURCES', 'ErrorDialogSvc', '$q', function($http, RESOURCES, errorDialogSvc, $q) {        
    var VtdSettingsSvc={};

    VtdSettingsSvc.getDungeons = function(){
        return $http.get(RESOURCES.REST_BASE_URL + '/settings/vtd').catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
    
    VtdSettingsSvc.getUser = function(userId){
        return $http.get(RESOURCES.REST_BASE_URL + '/users/' + userId).catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
    
    VtdSettingsSvc.createUser = function(user){
        return $http.post(RESOURCES.REST_BASE_URL + "/users/create", user, {silentHttpErrors : true}).catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
    
    VtdSettingsSvc.editUser = function(userId, user){
        return $http.put(RESOURCES.REST_BASE_URL + "/users/" + userId, user, {silentHttpErrors : true}).catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
     
    VtdSettingsSvc.deleteUser = function(id){
        return $http.delete(RESOURCES.REST_BASE_URL + "/users/" + id, {silentHttpErrors : true}).catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
        
    return VtdSettingsSvc;
}]);