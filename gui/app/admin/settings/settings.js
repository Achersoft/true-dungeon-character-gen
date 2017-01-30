'use strict';

angular.module('main.settings', ['ngRoute'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider
    .when('/settings/viewAll', {
        templateUrl: 'admin/settings/settingsList.html',
        controller: 'SettingsCtrl'
    })
    .when('/settings/add', {
        templateUrl: 'admin/settings/settingsDetails.html',
        controller: 'SettingsCtrl'
    });
}])

.controller('SettingsCtrl', ['$scope', '$location', '$route', '$routeParams', 'NgTableParams', 'SettingsSvc', 'SettingsState', function ($scope, $location, $route, $routeParams, NgTableParams, settingsSvc, settingsState) {
    $scope.settingsContext = settingsState.get();
    
    $scope.add = function() {
        settingsState.setSettings({}, true, true);
        $location.path("/settings/add");
    };
    
    $scope.edit = function(settingsId) {
        settingsSvc.getSettings(settingsId).then(function (result) {
            settingsState.setSettings(result.data, true, false);
            $location.path("/settings/add");
        });
    }; 
    
    $scope.create = function(isValid) {
        if(isValid) {
            settingsSvc.createSettings($scope.settingsContext.settings).then(function() {
                $location.path("/settings/viewAll");
            });
        }
    }; 
    
    $scope.editSettings = function(isValid) {
        if(isValid) {
            settingsSvc.editSettings($scope.settingsContext.settings.id, $scope.settingsContext.settings).then(function() {
                $location.path("/settings/viewAll");
            });
        }
    }; 
    
    $scope.delete = function(settingsId) {
        settingsSvc.deleteSettings(settingsId).then(function() {
            $route.reload();
        });
    }; 
    
    $scope.tableParams = new NgTableParams({
        page: 1,         
        count: 20     
    },
    {   total: 0, 
        counts: [], 
        getData: function ($defer, params) {
            settingsSvc.getSettingss().success(function (result) {
                params.total(result.length);
                $defer.resolve(result.slice((params.page() - 1) * params.count(), params.page() * params.count()));
            }).error(function(error){
                $scope.status = 'Unable to load candidate list for page ' + params.page() + ': ';
            });
        }
    });
}])

.factory('SettingsState', [
    function() {                    
        var settingsState = {
            settings: {},
            settings: {},
            editMode: false,
            addMode: false
        };

        function setSettingss(data) {
            settingsState.settings = data;
        }

        function setSettings(data, editMode, addMode) {
            settingsState.settings = data;
            settingsState.editMode = editMode;
            settingsState.addMode = addMode;
        }

        function get() {
            return settingsState;
        }

        return {
            setSettingss: setSettingss,
            setSettings: setSettings,
            get: get
        };
    }])

.factory('SettingsSvc',['$http', 'RESOURCES', function($http, RESOURCES){    
    var settingsSvc={};

    settingsSvc.getSettingss = function(){
        return $http.get(RESOURCES.REST_BASE_URL + '/settings/');
    };
    
    settingsSvc.getSettings = function(settingsId){
        return $http.get(RESOURCES.REST_BASE_URL + '/settings/' + settingsId);
    };
    
    settingsSvc.createSettings = function(settings){
        return $http.post(RESOURCES.REST_BASE_URL + "/settings/create",
                          settings, {silentHttpErrors : true});
    };
    
    settingsSvc.editSettings = function(settingsId, settings){
        return $http.put(RESOURCES.REST_BASE_URL + "/settings/" + settingsId,
                          settings, {silentHttpErrors : true});
    };
     
    settingsSvc.deleteSettings = function(id){
        return $http.delete(RESOURCES.REST_BASE_URL + "/settings/" + id,
                            {silentHttpErrors : true});
    };
        
    return settingsSvc;
}]);