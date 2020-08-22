angular.module('main')

.config(['$routeProvider', 'RESOURCES', function($routeProvider, RESOURCES) {
  $routeProvider
    .when('/vtd/mine', {
        templateUrl: (RESOURCES.IS_MOBILE)?'vtd/mobile/myCharacters-@{TDCC_VERSION}.html':'vtd/mobile/myCharacters-@{TDCC_VERSION}.html',
        controller: 'VtdMyCharactersCtrl'
    })
    .when('/vtd/play/:characterId', {
        templateUrl: (RESOURCES.IS_MOBILE)?'vtd/mobile/playCharacter-@{TDCC_VERSION}.html':'vtd/mobile/playCharacter-@{TDCC_VERSION}.html',
        controller: 'VtdPlayCtrl'
    });
}])

.controller('VtdMyCharactersCtrl', ['$scope', 'VtdSvc', 'ConfirmDialogSvc', function ($scope, vtdSvc, confirmDialogSvc) {
    $scope.myCharacterContext = {};
    
    vtdSvc.getCharacters().then(function(result) {
        $scope.myCharacterContext = result.data;
    });
}])

.controller('VtdPlayCtrl', ['$scope', 'VtdSvc', 'VtdState', 'RESOURCES', '$routeParams', '$route', 'ConfirmDialogSvc', function ($scope, vtdSvc, vtdState, RESOURCES, $routeParams, $route, confirmDialogSvc) {
    $scope.defaultRoller = [11,12,13,14,15,16,17,18,19,20];
    $scope.hardRoller = [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,11,12,13,14,15,16,17,18,19,20];
    $scope.harderRoller = [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20];
    $scope.deathRoller = [1,2,3,4,5,6,7,8,9,10,1,2,3,4,5,6,7,8,9,10,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20];
    $scope.tabIndex = 0;
    $scope.rollerIndex = 0;
    $scope.attackIndex = 0;
    $scope.defendIndex = 0;
    $scope.saveIndex = 0;
    $scope.damageIndex = 0;
    $scope.difficultyIndex = 0;
    $scope.damageModifierIndex = 0;
    $scope.rollHit = 0;
    $scope.rollDmg = 0;
    $scope.rollHitNatural = 0;
    $scope.rollDmgNatural = 0;
    $scope.rollDmgExplosionText = null;
    $scope.rollSave = 0;
    $scope.rollSaveNatural = 0;
    $scope.critDmg = 0;
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
        $scope.critDmg = 0;
        $scope.rollHitNatural = 0;
        $scope.rollDmgNatural = 0;
        $scope.rollDmgExplosionText = null;
    };
    
    $scope.setDefendIndex =  function(index) {
        $scope.defendIndex = index;
        $scope.rollSave = 0;
        $scope.rollSaveNatural = 0;
    };
    
    $scope.setSaveIndex =  function(index) {
        $scope.saveIndex = index;
        $scope.rollSave = 0;
        $scope.rollSaveNatural = 0;
    };
    
    $scope.setDifficultyIndex =  function(index) {
        vtdSvc.modifyDifficulty($scope.characterContext.id, index).then(function(result) {
            vtdState.setContext(result.data);
            $scope.characterContext = vtdState.get();
        });  
    };
    
    $scope.setDamageIndex =  function(index) {
        $scope.damageIndex = index;
    };
    
    $scope.setDamageModifierIndex =  function(index) {
        $scope.damageModifierIndex = index;
    };
    
    $scope.isHpGreen =  function() {
        if ($scope.characterContext.currentHealth) {
            var rangePercent = (+$scope.characterContext.currentHealth / +$scope.characterContext.stats.health);
            if (rangePercent > .75)
                return true;
            return false;
        }
        return true;
    };
    
    $scope.isHpOrange =  function() {
        if ($scope.characterContext.currentHealth) {
            var rangePercent = (+$scope.characterContext.currentHealth / +$scope.characterContext.stats.health);
            if (rangePercent > .3 && rangePercent <= .75)
                return true;
        }
        return false;
    };
    
    $scope.isHpRed =  function() {
        if ($scope.characterContext.currentHealth === 0)
            return true;
        if ($scope.characterContext.currentHealth) {
            var rangePercent = (+$scope.characterContext.currentHealth / +$scope.characterContext.stats.health);
            if ($scope.characterContext.currentHealth === 0 || rangePercent <= .3)
                return true;
        }
        return false;
    };
    
    $scope.rollToHit =  function() {
        $scope.rollHit = 0;
        $scope.rollDmg = 0;
        $scope.critDmg = 0;
        $scope.rollHitNatural = 0;
        $scope.rollDmgNatural = 0;
        $scope.rollDmgExplosionText = null;
        $scope.rollHitNatural = $scope.getRoll();
        $scope.rollHit = $scope.rollHitNatural;
        
        if ($scope.rollHit !== 1) {
            if ($scope.attackIndex === 2) {
                $scope.rollHit += $scope.characterContext.stats.meleePolyHit;
                $scope.rollDmg = $scope.characterContext.stats.meleePolyDmg;
                if ($scope.characterContext.meleePolyDmgRange) {
                    
                }
                if ($scope.rollHitNatural >= $scope.characterContext.meleePolyCritMin) {
                    if ($scope.characterContext.characterClass === 'DWARF_FIGHTER' && $scope.characterContext.stats.level === 5) {
                        $scope.critDmg = $scope.rollDmg*3;
                    } else
                        $scope.critDmg = $scope.rollDmg*2;
                }
            } else if ($scope.attackIndex === 1) {
                $scope.rollHit += $scope.characterContext.stats.rangeHit;
                $scope.rollDmg = $scope.characterContext.stats.rangeDmg;
                if ($scope.characterContext.rangeDmgRange && $scope.characterContext.rangeDmgRange.length > 0) {
                    $scope.rollDmgNatural = $scope.characterContext.rangeDmgRange[$scope.getRandomInt($scope.characterContext.rangeDmgRange.length)];
                    $scope.rollDmg += $scope.rollDmgNatural;
                    
                    if ($scope.characterContext.rangeWeaponExplodeRange && $scope.characterContext.rangeWeaponExplodeRange.includes($scope.rollDmgNatural)) {
                        $scope.rollDmgExplosionText = $scope.characterContext.rangeWeaponExplodeText;
                    }
                }
                if ($scope.rollHitNatural >= $scope.characterContext.rangeCritMin) {
                    if ($scope.characterContext.characterClass === 'DWARF_FIGHTER' && $scope.characterContext.stats.level === 5) {
                        $scope.critDmg = $scope.rollDmg*3;
                    } else
                        $scope.critDmg = $scope.rollDmg*2;
                }
            } else {
                $scope.rollHit += $scope.characterContext.stats.meleeHit;
                $scope.rollDmg = $scope.characterContext.stats.meleeDmg;
                if ($scope.characterContext.meleeDmgRange && $scope.characterContext.meleeDmgRange.length > 0) {
                    $scope.rollDmgNatural = $scope.characterContext.meleeDmgRange[$scope.getRandomInt($scope.characterContext.meleeDmgRange.length)];
                    $scope.rollDmg += $scope.rollDmgNatural;
                    
                    if ($scope.characterContext.meleeWeaponExplodeRange && $scope.characterContext.meleeWeaponExplodeRange.includes($scope.rollDmgNatural)) {
                        $scope.rollDmgExplosionText = $scope.characterContext.meleeWeaponExplodeText;
                    }
                }
                if ($scope.rollHitNatural >= $scope.characterContext.meleeCritMin) {
                    if ($scope.characterContext.characterClass === 'DWARF_FIGHTER' && $scope.characterContext.stats.level === 5) {
                        $scope.critDmg = $scope.rollDmg*3;
                    } else
                        $scope.critDmg = $scope.rollDmg*2;
                }
            }
        }
    };
    
    $scope.rollToSave =  function() {
        $scope.rollSaveNatural = $scope.getRoll();
        $scope.rollSave = $scope.rollSaveNatural;
        
        if ($scope.rollSave !== 1) {
            if ($scope.saveIndex === 0)
                $scope.rollSave += $scope.characterContext.stats.fort;
            if ($scope.saveIndex === 1)
                $scope.rollSave += $scope.characterContext.stats.reflex;
            if ($scope.saveIndex === 2)
                $scope.rollSave += $scope.characterContext.stats.will;
        }                         
    };
    
    $scope.getRoll =  function() {
        if ($scope.characterContext.rollerDifficulty === 0) {
            var roll = $scope.defaultRoller[$scope.getRandomInt($scope.defaultRoller.length)];
            if (roll === 11)
                roll = 1;
            return roll;
        } else if ($scope.characterContext.rollerDifficulty === 1) {
            return $scope.hardRoller[$scope.getRandomInt($scope.hardRoller.length)];
        } else if ($scope.characterContext.rollerDifficulty === 2) {
            return $scope.harderRoller[$scope.getRandomInt($scope.harderRoller.length)];
        } else if ($scope.characterContext.rollerDifficulty === 3) {
            return $scope.deathRoller[$scope.getRandomInt($scope.deathRoller.length)];
        } else {
            var roll = $scope.defaultRoller[$scope.getRandomInt($scope.defaultRoller.length)];
            if (roll === 11)
                roll = 1;
            return roll;
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
            vtdSvc.modifyHealth($scope.characterContext.id, -1*damage).then(function(result) {
                vtdState.setContext(result.data);
                $scope.characterContext = vtdState.get();
            });
        }
    };
    
    $scope.healDamage =  function(heal) {
        this.healAmount = 0;

        if (+$scope.characterContext.currentHealth > 0 && +heal > 0) {
            vtdSvc.modifyHealth($scope.characterContext.id, heal).then(function(result) {
                vtdState.setContext(result.data);
                $scope.characterContext = vtdState.get();
            });
        }
    };
    
    $scope.setInitBonus =  function(bonus) {
        vtdSvc.setInitBonus($scope.characterContext.id, bonus).then(function(result) {
            vtdState.setContext(result.data);
            $scope.characterContext = vtdState.get();
        });
    };
    
    
    $scope.setHealthBonus =  function(bonus) {
        vtdSvc.setHealthBonus($scope.characterContext.id, bonus).then(function(result) {
            vtdState.setContext(result.data);
            $scope.characterContext = vtdState.get();
        });
    };
    
    
    $scope.resetCharacter = function(id) {
        confirmDialogSvc.confirm("Are you sure you wish to reset your character?", function(){
            vtdSvc.resetCharacter(id).then(function() {
                $route.reload();
            });
        });
    }; 
    
    $scope.getRandomInt =  function(max) {
        return Math.floor(Math.random() * Math.floor(max));
    };
    
    $scope.useSkill =  function(skillId, selfTarget, selfHeal, madEvoker) {
        vtdSvc.useSkill($scope.characterContext.id, skillId, selfTarget, selfHeal, madEvoker).then(function(result) {
            vtdState.setContext(result.data);
            $scope.characterContext = vtdState.get();
        });
    };
    
    $scope.unuseSkill =  function(skilId) {
        vtdSvc.unuseSkill($scope.characterContext.id, skilId).then(function(result) {
            vtdState.setContext(result.data);
            $scope.characterContext = vtdState.get();
        });
    };
    
    $scope.activateBuff =  function(buff) {
        vtdSvc.addBuff($scope.characterContext.id, buff.id).then(function(result) {
            vtdState.setContext(result.data);
            $scope.characterContext = vtdState.get();
        });
    };
    
    $scope.removeBuff =  function(buff) {
        vtdSvc.removeBuff($scope.characterContext.id, buff.id).then(function(result) {
            vtdState.setContext(result.data);
            $scope.characterContext = vtdState.get();
        });
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
    
    tokenAdminSvc.getBuffs = function() {
        return $http.get(RESOURCES.REST_BASE_URL + '/vtd/buffs')
            .catch(function(response) {
                errorDialogSvc.showError(response);
                return($q.reject(response));
            });
    };
    
    tokenAdminSvc.modifyDifficulty = function(id, difficulty) {
        return $http.post(RESOURCES.REST_BASE_URL + '/vtd/' + id + '/difficulty/?difficulty=' + difficulty).catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
    
    tokenAdminSvc.modifyHealth = function(id, health) {
        return $http.post(RESOURCES.REST_BASE_URL + '/vtd/' + id + '/health/?health=' + health).catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
        
    tokenAdminSvc.setInitBonus = function(id, bonus) {
        return $http.post(RESOURCES.REST_BASE_URL + '/vtd/' + id + '/bonus/init/?init=' + bonus).catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
    
    tokenAdminSvc.setHealthBonus = function(id, health) {
        return $http.post(RESOURCES.REST_BASE_URL + '/vtd/' + id + '/bonus/health/?health=' + health).catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
    
    tokenAdminSvc.useSkill = function(id, skillId, selfTarget, selfHeal, madEvoker) {
        return $http.post(RESOURCES.REST_BASE_URL + '/vtd/' + id + '/' + skillId + '/use/?selfTarget=' + selfTarget + '&selfHeal=' + selfHeal + '&madEvoker=' + madEvoker).catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
    
    tokenAdminSvc.unuseSkill = function(id, skillId) {
        return $http.post(RESOURCES.REST_BASE_URL + '/vtd/' + id + '/' + skillId + '/unuse/').catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
    
    tokenAdminSvc.addBuff = function(id, buff) {
        return $http.post(RESOURCES.REST_BASE_URL + '/vtd/' + id + '/buff/?buff=' + buff).catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
    
    tokenAdminSvc.removeBuff = function(id, buff) {
        return $http.delete(RESOURCES.REST_BASE_URL + '/vtd/' + id + '/buff/?buff=' + buff).catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
    
    tokenAdminSvc.nextRoom = function(id, buff) {
        return $http.post(RESOURCES.REST_BASE_URL + '/vtd/' + id + '/next').catch(function(response) {
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
    
    tokenAdminSvc.resetCharacter = function(id) {
        return $http.delete(RESOURCES.REST_BASE_URL + '/vtd/' + id)
            .catch(function(response) {
                errorDialogSvc.showError(response);
                return($q.reject(response));
            });
    };
    
    return tokenAdminSvc;
}]);