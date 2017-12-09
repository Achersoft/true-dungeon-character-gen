angular.module('main')

.config(['$routeProvider', 'RESOURCES', function($routeProvider, RESOURCES) {
  $routeProvider
    .when('/character/mine', {
        templateUrl: (RESOURCES.IS_MOBILE)?'character/mobile/myCharacters.html':'character/mobile/myCharacters.html',
        controller: 'MyCharactersCtrl'
    })
    .when('/character/create', {
        templateUrl: (RESOURCES.IS_MOBILE)?'character/mobile/createCharacter.html':'character/desktop/createCharacter.html',
        controller: 'CharacterCreationCtrl'
    })
    .when('/character/edit/:characterId', {
        templateUrl: (RESOURCES.IS_MOBILE)?'character/mobile/editCharacter.html':'character/desktop/editCharacter.html',
        controller: 'CharacterEditCtrl'
    });
}])

.controller('MyCharactersCtrl', ['$scope', 'CharacterSvc', 'ConfirmDialogSvc', function ($scope, characterSvc, confirmDialogSvc) {
    $scope.myCharacterContext = {};
    
    characterSvc.getCharacters().then(function(result) {
        $scope.myCharacterContext = result.data;
    });
    
    $scope.deleteCharacter = function(id, name){
        confirmDialogSvc.confirm("Are you sure you wish to delete character " + name +"?", function(){
           characterSvc.deleteCharacter(id).then(function(result) {
                $scope.myCharacterContext = result.data;
            });
        });
    };
}])

.controller('CharacterCreationCtrl', ['$scope', 'CharacterSvc', '$location', '$route', function ($scope, characterSvc, $location, $route) {
    $scope.name = null;
    $scope.characterClass = null;
    $scope.characterClasses = ["BARBARIAN", "BARD", "CLERIC", "DRUID", "DWARF_FIGHTER", "ELF_WIZARD", "FIGHTER", "MONK", "PALADIN", "RANGER", "ROGUE", "WIZARD"];

    $scope.characterClassSelected =  function(charClass){
        return charClass === $scope.characterClass;
    };
    
    $scope.toggleCharacterClassSelected =  function(charClass){
        $scope.characterClass = charClass;
    };
    
    $scope.createCharacter = function(){
        characterSvc.createCharacter($scope.characterClass, $scope.name).then(function(result) {
            $location.path("/character/edit/" + result.data.id);
            $route.reload();
        });
    };
}])

.controller('CharacterEditCtrl', ['$scope', 'CharacterSvc', 'CharacterState', 'RESOURCES', '$routeParams', 'clipboard', function ($scope, characterSvc, characterState, RESOURCES, $routeParams, clipboard) {
    $scope.tabIndex = 0;
    $scope.characterContext = characterState.get();
    $scope.ringErrors = null;
    $scope.backErrors = null;
    $scope.charmErrors = null;
    $scope.slotlessErrors = null;
    $scope.slotlessRow1 = [];
    $scope.slotlessRow2 = [];
    $scope.slotlessRow3 = [];
    
    characterSvc.getCharacter($routeParams.characterId).then(function(result) {
        characterState.setContext(result.data);
        $scope.characterContext = characterState.get();
        $scope.checkItemStatus();
    });
    
    $scope.setTokenSlot = function(id, soltId, tokenId) {
        characterSvc.setTokenSlot(id, soltId, tokenId).then(function(result) {
            characterState.setContext(result.data);
            $scope.characterContext = characterState.get();
            $scope.checkItemStatus();
        });
    };
    
    $scope.unequipItem = function(id, soltId) {
        characterSvc.unequipItem(id, soltId).then(function(result) {
            characterState.setContext(result.data);
            $scope.characterContext = characterState.get();
            $scope.checkItemStatus();
        });
    };
    
    $scope.exportToPDF = function() {
        characterSvc.exportToPDF($scope.characterContext.id).then(function(response) {
            //console.log(response);
            var blob = new Blob([response.data], {type:"blob"});
            //console.log(blob);
            //change download.pdf to the name of whatever you want your file to be
            saveAs(blob, $scope.characterContext.name + ".pdf");
        });
    };
    
    $scope.exportToHTML = function() {
        var request = new XMLHttpRequest();
        request.open('GET', RESOURCES.REST_BASE_URL + '/character/html/' + $scope.characterContext.id, false); 
        request.send(null);
        
        if (request.status === 200) {
            clipboard.copyText(request.responseText);
        }
    };
    
    $scope.checkItemStatus = function() {
        $scope.ringErrors = null;
        $scope.backErrors = null;
        $scope.charmErrors = null;
        $scope.slotlessErrors = null;
        slotlessRow1 = [];
        slotlessRow2 = [];
        slotlessRow3 = [];
        
        if($scope.characterContext.slotless.length > 0) {
            $scope.characterContext.slotless.forEach(function(item, index) {
                if(index < 10)
                    slotlessRow1.push(item);
                if(index >= 10 && index < 20)
                    slotlessRow2.push(item);
                if(index >= 20)
                    slotlessRow3.push(item);
            });
            
            $scope.slotlessRow1 = slotlessRow1;
            $scope.slotlessRow2 = slotlessRow2;
            $scope.slotlessRow3 = slotlessRow3;
        }
        
        if($scope.characterContext.rings.length > 0) {
            $scope.characterContext.rings.forEach(function(item, index) {
                if(item.slotStatus === 'INVALID')
                    $scope.ringErrors = item.statusText;
            });
        }
        if($scope.characterContext.backs.length > 0) {
            $scope.characterContext.backs.forEach(function(item, index) {
                if(item.slotStatus === 'INVALID')
                    $scope.backErrors = item.statusText;
            });
        }
        if($scope.characterContext.charms.length > 0) {
            $scope.characterContext.charms.forEach(function(item, index) {
                if(item.slotStatus === 'INVALID') {
                    if($scope.charmErrors === null)
                        $scope.charmErrors = [];
                    $scope.charmErrors.push(item.statusText);
                }
            });
        }
        if($scope.characterContext.slotless.length > 0) {
            $scope.characterContext.slotless.forEach(function(item, index) {
                if(item.slotStatus === 'INVALID') {
                    if($scope.slotlessErrors === null)
                        $scope.slotlessErrors = [];
                    $scope.slotlessErrors.push(item.statusText);
                }
            });
        }
    };
}])

.factory('CharacterState', [
    function() {                    
        var characterState = {};
        
        function setContext(data) {
            characterState = data;
        }
        
        function get() {
            return characterState;
        }

        return {
            setContext: setContext,
            get: get
        };
    }])

.factory('CharacterSvc',['$http', 'RESOURCES', 'ErrorDialogSvc', '$q', function($http, RESOURCES, errorDialogSvc, $q) {    
    var tokenAdminSvc={};

    tokenAdminSvc.createCharacter = function(characterClass, name) {
        return $http.put(RESOURCES.REST_BASE_URL + '/character/create?characterClass=' + characterClass + "&name=" + name)
            .catch(function(response) {
                errorDialogSvc.showError(response);
                return($q.reject(response));
            });
    };
    
    tokenAdminSvc.getCharacter = function(id) {
        return $http.get(RESOURCES.REST_BASE_URL + '/character/' + id)
            .catch(function(response) {
                errorDialogSvc.showError(response);
                return($q.reject(response));
            });
    };
    
    tokenAdminSvc.getCharacters = function() {
        return $http.get(RESOURCES.REST_BASE_URL + '/character/all');
    };
    
    tokenAdminSvc.getSlotTokens = function(slotId, characterId, characterClass, slot, rarity) {
        return $http.get(RESOURCES.REST_BASE_URL + '/token/character?slotId=' + slotId + "&characterId=" + characterId + "&characterClass=" + characterClass + "&slot=" + slot + "&rarity=" + rarity)
            .catch(function(response) {
                errorDialogSvc.showError(response);
                return($q.reject(response));
            });
    };
    
    tokenAdminSvc.setTokenSlot = function(id, soltId, tokenId) {
        return $http.post(RESOURCES.REST_BASE_URL + '/character/token?id=' + id + '&soltId=' + soltId + "&tokenId=" + tokenId);
    };
    
    tokenAdminSvc.unequipItem = function(id, soltId) {
        return $http.post(RESOURCES.REST_BASE_URL + '/character/token/unequip?id=' + id + '&soltId=' + soltId)
            .catch(function(response) {
                errorDialogSvc.showError(response);
                return($q.reject(response));
            });
    };
    
    tokenAdminSvc.deleteCharacter = function(id) {
        return $http.delete(RESOURCES.REST_BASE_URL + '/character/' + id)
            .catch(function(response) {
                errorDialogSvc.showError(response);
                return($q.reject(response));
            });
    };
    
    tokenAdminSvc.exportToPDF = function(id) {
        return $http.get(RESOURCES.REST_BASE_URL + '/character/pdf/' + id, {responseType: 'arraybuffer'})
            .catch(function(response) {
                errorDialogSvc.showError(response);
                return($q.reject(response));
            });
    };
    
    tokenAdminSvc.exportToHTML = function(id) {
        return $http.get(RESOURCES.REST_BASE_URL + '/character/html/' + id)
            .catch(function(response) {
                errorDialogSvc.showError(response);
                return($q.reject(response));
            });
    };
    
    return tokenAdminSvc;
}]);