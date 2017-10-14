angular.module('main')

.config(['$routeProvider', 'RESOURCES', function($routeProvider, RESOURCES) {
  $routeProvider
    .when('/party/mine', {
        templateUrl: (RESOURCES.IS_MOBILE)?'party/mobile/myParties.html':'party/mobile/myParties.html',
        controller: 'MyPartiesCtrl'
    })
    .when('/party/create', {
        templateUrl: (RESOURCES.IS_MOBILE)?'party/desktop/createParty.html':'party/desktop/createParty.html',
        controller: 'CreatePartyCtrl'
    })
    .when('/party/edit/:partyId', {
        templateUrl: (RESOURCES.IS_MOBILE)?'party/desktop/editParty.html':'party/desktop/editParty.html',
        controller: 'EditPartyCtrl'
    });
}])

.controller('MyPartiesCtrl', ['$scope', 'PartySvc', 'ConfirmDialogSvc', function ($scope, partySvc, confirmDialogSvc) {
    $scope.myPartiesContext = {};
    $scope.name = null;
    
    partySvc.getParties().then(function(result) {
        $scope.myPartiesContext = result.data;
    });
    
    $scope.deleteParty = function(id, name){
        confirmDialogSvc.confirm("Are you sure you wish to delete party " + name +"?", function(){
           partySvc.deleteParty(id).then(function(result) {
                $scope.myPartiesContext = result.data;
            });
        });
    };
}])

.controller('CreatePartyCtrl', ['$scope', 'PartySvc', '$location', '$route', function ($scope, partySvc, $location, $route) {
    $scope.name = null;
    
    $scope.createParty = function(){
        partySvc.createParty($scope.name).then(function(result) {
            $location.path("/party/edit/" + result.data.id);
            $route.reload();
        });
    };
}])

.controller('EditPartyCtrl', ['$scope', 'PartySvc', 'PartyState', 'RESOURCES', '$routeParams', 'clipboard', 'ConfirmDialogSvc', function ($scope, partySvc, partyState, RESOURCES, $routeParams, clipboard, confirmDialogSvc) {
    //$scope.difficulties = ["NON_LETHAL", "NORMAL", "HARDCORE", "NIGHTMARE", "EPIC"];
    $scope.difficulties = [{"id":"NON_LETHAL","name":"Non Lethal"},{"id":"NORMAL","name":"Normal"},{"id":"HARDCORE","name":"Hardcore"},{"id":"NIGHTMARE","name":"Nightmare"},{"id":"EPIC","name":"Epic"}];
    $scope.partyContext = partyState.get();
    
    partySvc.getParty($routeParams.partyId).then(function(result) {
        partyState.setContext(result.data);
        $scope.partyContext = partyState.get();
    });
    
    $scope.addCharacter = function(partyId, character) {
        partySvc.addCharacter(partyId, character).then(function(result) {
            partyState.setContext(result.data);
            $scope.partyContext = partyState.get();
        });
    };
    
    $scope.removeCharacter = function(partyId, characterClass) {
        confirmDialogSvc.confirm("Are you sure you wish to remove this character?", function(){
            partySvc.removeCharacter(partyId, characterClass).then(function(result) {
                partyState.setContext(result.data);
                $scope.partyContext = partyState.get();
            });
        });
    };
    
    $scope.updateDifficulty = function(partyId, difficulty){
        partySvc.updateDifficulty(partyId, difficulty).then(function(result) {
            partyState.setContext(result.data);
            $scope.partyContext = partyState.get();
        });
    };
    
    $scope.exportToPDF = function() {
        partySvc.exportToPDF($scope.partyContext.id).then(function(response) {
            //console.log(response);
            var blob = new Blob([response.data], {type:"blob"});
            //console.log(blob);
            //change download.pdf to the name of whatever you want your file to be
            saveAs(blob, $scope.partyContext.name + ".pdf");
        });
    };
}])

.factory('PartyState', [
    function() {                    
        var partyState = {};
        
        function setContext(data) {
            partyState = data;
        }
        
        function get() {
            return partyState;
        }

        return {
            setContext: setContext,
            get: get
        };
    }])

.factory('PartySvc',['$http', 'RESOURCES', 'ErrorDialogSvc', '$q', function($http, RESOURCES, errorDialogSvc, $q) {    
    var partySvc={};
    
    partySvc.getParties = function() {
        return $http.get(RESOURCES.REST_BASE_URL + '/party/all');
    };
    
    partySvc.createParty = function(name) {
        return $http.put(RESOURCES.REST_BASE_URL + '/party/create?name=' + name)
            .catch(function(response) {
                errorDialogSvc.showError(response);
                return($q.reject(response));
            });
    };
    
    partySvc.getParty = function(id) {
        return $http.get(RESOURCES.REST_BASE_URL + '/party/' + id)
            .catch(function(response) {
                errorDialogSvc.showError(response);
                return($q.reject(response));
            });
    };
    
    partySvc.getSelectableCharacters = function(userid, cClass) {
        return $http.get(RESOURCES.REST_BASE_URL + '/party/selectablecharacters?userid=' + userid + '&class=' + cClass)
            .catch(function(response) {
                errorDialogSvc.showError(response);
                return($q.reject(response));
            });
    };
    
    partySvc.updateDifficulty = function(id, difficulty) {
        return $http.post(RESOURCES.REST_BASE_URL + '/party/' + id + '/difficulty?difficulty=' + difficulty)
            .catch(function(response) {
                errorDialogSvc.showError(response);
                return($q.reject(response));
            });
    };
    
    partySvc.addCharacter = function(id, characterId) {
        return $http.post(RESOURCES.REST_BASE_URL + '/party/' + id + '/addcharacter?characterId=' + characterId)
            .catch(function(response) {
                errorDialogSvc.showError(response);
                return($q.reject(response));
            });
    };
    
    partySvc.removeCharacter = function(partyId, characterClass) {
        return $http.delete(RESOURCES.REST_BASE_URL + '/party/' + partyId + '/removecharacter?characterClass=' + characterClass)
            .catch(function(response) {
                errorDialogSvc.showError(response);
                return($q.reject(response));
            });
    };
    
    partySvc.deleteParty = function(id) {
        return $http.delete(RESOURCES.REST_BASE_URL + '/party/' + id)
            .catch(function(response) {
                errorDialogSvc.showError(response);
                return($q.reject(response));
            });
    };
    
    partySvc.exportToPDF = function(id) {
        return $http.get(RESOURCES.REST_BASE_URL + '/party/pdf/' + id, {responseType: 'arraybuffer'})
            .catch(function(response) {
                errorDialogSvc.showError(response);
                return($q.reject(response));
            });
    };

    return partySvc;
}]);