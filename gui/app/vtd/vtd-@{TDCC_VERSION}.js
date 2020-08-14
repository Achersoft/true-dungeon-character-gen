angular.module('main')

.config(['$routeProvider', 'RESOURCES', function($routeProvider, RESOURCES) {
  $routeProvider
    .when('/vtd/mine', {
        templateUrl: (RESOURCES.IS_MOBILE)?'vtd/mobile/myCharacters-@{TDCC_VERSION}.html':'vtd/desktop/myCharacters-@{TDCC_VERSION}.html',
        controller: 'VtdMyCharactersCtrl'
    })
    .when('/vtd/play/:characterId', {
        templateUrl: (RESOURCES.IS_MOBILE)?'vtd/mobile/playCharacter-@{TDCC_VERSION}.html':'vtd/desktop/playCharacter-@{TDCC_VERSION}.html',
        controller: 'VtdPlayCtrl'
    });
}])

.controller('VtdMyCharactersCtrl', ['$scope', 'VtdSvc', 'ConfirmDialogSvc', function ($scope, vtdSvc, confirmDialogSvc) {
    $scope.myCharacterContext = {};
    
    vtdSvc.getCharacters().then(function(result) {
        $scope.myCharacterContext = result.data;
    });
}])

.controller('VtdPlayCtrl', ['$scope', 'VtdSvc', 'VtdState', 'RESOURCES', '$routeParams', 'clipboard', function ($scope, vtdSvc, vtdState, RESOURCES, $routeParams, clipboard) {
    $scope.tabIndex = 0;
    $scope.rollerIndex = 0;
    $scope.attackIndex = 0;
    $scope.defendIndex = 0;
    $scope.saveIndex = 0;
    $scope.damageIndex = 0;
    $scope.damageModifierIndex = 0;
    $scope.rollHit = 0;
    $scope.rollDmg = 0;
    $scope.rollSave = 0;
    $scope.isCrit = false;
    $scope.damageTaken = -1;
    $scope.damageAmount = 0;
    $scope.healAmount = 0;
    $scope.characterContext = vtdState.get();

    vtdSvc.getCharacter($routeParams.characterId).then(function(result) {
        vtdState.setContext(result.data);
        $scope.characterContext = vtdState.get();
    });
    
    $scope.setRollerIndex =  function(index) {
        $scope.rollerIndex = index;
        $scope.rollSave = 0;
    };
    
    $scope.setAttackIndex =  function(index) {
        $scope.attackIndex = index;
        $scope.rollHit = 0;
        $scope.rollDmg = 0;
        $scope.isCrit = false;
    };
    
    $scope.setDefendIndex =  function(index) {
        $scope.defendIndex = index;
        $scope.rollSave = 0;
    };
    
    $scope.setSaveIndex =  function(index) {
        $scope.saveIndex = index;
        $scope.rollSave = 0;
    };
    
    $scope.setDamageIndex =  function(index) {
        $scope.damageIndex = index;
    };
    
    $scope.setDamageModifierIndex =  function(index) {
        $scope.damageModifierIndex = index;
    };
    
    $scope.isHpGreen =  function() {
        if ($scope.characterContext.currentHealth)
            return $scope.characterContext.currentHealth === 49;
        return true;
    };
    
    $scope.isHpOrange =  function() {
        if ($scope.characterContext.currentHealth)
            return $scope.characterContext.currentHealth !== 49;
        return false;
    };
    
    $scope.isHpRed =  function() {
        if ($scope.characterContext.currentHealth)
            return $scope.characterContext.currentHealth !== 49;
        return false;
    };
    
    $scope.rollToHit =  function() {
        $scope.rollHit = $scope.getRandomInt(20);
        $scope.rollDmg = 0;
        $scope.isCrit = false;
        
        if ($scope.rollHit !== 1) {
            if ($scope.attackIndex === 2) {
                if ($scope.rollHit > $scope.characterContext.meleePolyCritMin)
                    $scope.isCrit = true;
                $scope.rollHit += $scope.characterContext.stats.meleePolyHit;
                $scope.rollDmg = $scope.characterContext.stats.meleePolyDmg;
                if ($scope.characterContext.meleePolyDmgRange) {
                    
                }
            } else if ($scope.attackIndex === 1) {
                if ($scope.rollHit > $scope.characterContext.rangeCritMin)
                    $scope.isCrit = true;
                $scope.rollHit += $scope.characterContext.stats.rangeHit;
                $scope.rollDmg = $scope.characterContext.stats.rangeDmg;
                if ($scope.characterContext.rangeDmgRange) {
                    
                }
            } else {
                if ($scope.rollHit > $scope.characterContext.meleeCritMin)
                    $scope.isCrit = true;
                $scope.rollHit += $scope.characterContext.stats.meleeHit;
                $scope.rollDmg = $scope.characterContext.stats.meleeDmg;
                if ($scope.characterContext.meleeDmgRange) {
                    
                }
            }
        }
    };
    
    $scope.rollToSave =  function() {
        $scope.rollSave = $scope.getRandomInt(20) + 1;
        
        if ($scope.rollSave !== 1) {
            if ($scope.saveIndex === 0)
                $scope.rollSave += $scope.characterContext.stats.fort;
            if ($scope.saveIndex === 1)
                $scope.rollSave += $scope.characterContext.stats.reflex;
            if ($scope.saveIndex === 2)
                $scope.rollSave += $scope.characterContext.stats.will;
        }                         
    };
    
    $scope.takeDamage =  function(damage) {
        this.damageAmount = 0;

        if ($scope.damageIndex === 0 && $scope.characterContext.stats.drMelee)
            damage -= $scope.characterContext.stats.drMelee;
        else if ($scope.damageIndex === 1 && $scope.characterContext.stats.drRange)
            damage -= $scope.characterContext.stats.drRange;
        else if ($scope.damageIndex === 2 && $scope.characterContext.stats.drSpell)
            damage -= $scope.characterContext.stats.drSpell;
                               
        if ($scope.damageModifierIndex === 1 && $scope.characterContext.stats.drFire)
            damage -= $scope.characterContext.stats.drFire;
        else if ($scope.damageModifierIndex === 2 && $scope.characterContext.stats.drCold)
            damage -= $scope.characterContext.stats.drCold;
        else if ($scope.damageModifierIndex === 3 && $scope.characterContext.stats.drShock)
            damage -= $scope.characterContext.stats.drShock;
        else if ($scope.damageModifierIndex === 4 && $scope.characterContext.stats.drSonic)
            damage -= $scope.characterContext.stats.drSonic;
        else if ($scope.damageModifierIndex === 5 && $scope.characterContext.stats.drEldritch)
            damage -= $scope.characterContext.stats.drEldritch;
        else if ($scope.damageModifierIndex === 6 && $scope.characterContext.stats.drPoison)
            damage -= $scope.characterContext.stats.drPoison;
        else if ($scope.damageModifierIndex === 7 && $scope.characterContext.stats.drDarkrift)
            damage -= $scope.characterContext.stats.drDarkrift;
        else if ($scope.damageModifierIndex === 8 && $scope.characterContext.stats.drSacred)
            damage -= $scope.characterContext.stats.drSacred;
        else if ($scope.damageModifierIndex === 9 && $scope.characterContext.stats.drAcid)
            damage -= $scope.characterContext.stats.drAcid;
        
        if (damage > 0) {
            $scope.characterContext.currentHealth -= damage;
        }
        
        if ($scope.characterContext.currentHealth < 1) {
            $scope.characterContext.currentHealth = 0;
        }
    };
    
    $scope.healDamage =  function(heal) {
        this.healAmount = 0;

        if ($scope.characterContext.currentHealth > 0 && heal > 0) {
            $scope.characterContext.currentHealth += heal;
            
            if ($scope.characterContext.currentHealth > $scope.characterContext.stats.health)
                $scope.characterContext.currentHealth = $scope.characterContext.stats.health;
        }
    };
    
    $scope.getRandomInt =  function(max) {
        return Math.floor(Math.random() * Math.floor(max));
    };
    
}])

.factory('VtdState', [
    function() {                    
        var vtdState = {};
        
        function setContext(data) {
            vtdState = data;
        }
        
        function get() {
            return vtdState;
        }

        return {
            setContext: setContext,
            get: get
        };
    }])

.factory('VtdSvc',['$http', 'RESOURCES', 'ErrorDialogSvc', '$q', function($http, RESOURCES, errorDialogSvc, $q) {    
    var tokenAdminSvc={};

    tokenAdminSvc.getSelectableCharacters = function(userid) {
        return $http.get(RESOURCES.REST_BASE_URL + '/character/selectablecharacters?userid=' + userid)
            .catch(function(response) {
                errorDialogSvc.showError(response);
                return($q.reject(response));
            });
    };
    
    tokenAdminSvc.getCharacter = function(id) {
        return $http.get(RESOURCES.REST_BASE_URL + '/vtd/' + id)
            .catch(function(response) {
                errorDialogSvc.showError(response);
                return($q.reject(response));
            });
    };
    
    tokenAdminSvc.getCharacters = function() {
        return $http.get(RESOURCES.REST_BASE_URL + '/character/all');
    };
    
    return tokenAdminSvc;
}]);