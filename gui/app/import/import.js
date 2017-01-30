'use strict';

angular.module('main.import', ['ngRoute'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider
    .when('/import/allSets', {
        templateUrl: 'import/allSets.html',
        controller: 'ImportCtrl'
    })
    .when('/import/selectSet', {
        templateUrl: 'import/selectSet.html',
        controller: 'ImportCtrl'
    });
}])

.directive('fileModel', ['$parse', function ($parse) {
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {
            var model = $parse(attrs.fileModel);
            var modelSetter = model.assign;
            
            element.bind('change', function(){
                scope.$apply(function(){
                    modelSetter(scope, element[0].files[0]);
                });
            });
        }
    };
}])

.controller('ImportCtrl', ['$scope', '$routeParams', 'RESOURCES', 'ImportSvc', function ($scope, $routeParams, RESOURCES, importSvc) {
    $scope.uploadFile = function(){
        var file = $scope.myFile;
        importSvc.uploadFileToUrl(file);
    };
    
    $scope.uploadSet = function(){
        var file = $scope.myFile;
        importSvc.uploadSetToUrl(file);
    };
}])

.factory('ImportSvc',['$http', 'RESOURCES', function($http, RESOURCES){    
    var importSvc={};
    
    importSvc.uploadFileToUrl = function(file){
        var fd = new FormData();
        fd.append('file', file);
        $http.post(RESOURCES.REST_BASE_URL + '/importer/all', fd, {
            transformRequest: angular.identity,
            headers: {'Content-Type': undefined}
        })
        .success(function(){
        })
        .error(function(){
        });
    };
    
    importSvc.uploadSetToUrl = function(file){
        var fd = new FormData();
        fd.append('file', file);
        $http.post(RESOURCES.REST_BASE_URL + '/importer/set', fd, {
            transformRequest: angular.identity,
            headers: {'Content-Type': undefined}
        })
        .success(function(){
        })
        .error(function(){
        });
    };

    return importSvc;
}]);