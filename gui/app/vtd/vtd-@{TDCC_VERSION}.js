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
    $scope.pregenerated = false;
    
    vtdSvc.getSelectableCharacters().then(function(result) {
        $scope.myCharacterContext = result.data;
    });
    
    $scope.toggleCharacters =  function(isPregen) {
        if (isPregen) {
            vtdSvc.getPregeneratedCharacters().then(function(result) {
                $scope.myCharacterContext = result.data;
            });
        } else {
            vtdSvc.getSelectableCharacters().then(function(result) {
                $scope.myCharacterContext = result.data;
            });
        }
    };
}])

.controller('VtdPlayCtrl', ['$scope', 'VtdSvc', 'VtdState', 'RESOURCES', '$routeParams', '$route', 'ConfirmDialogSvc', function ($scope, vtdSvc, vtdState, RESOURCES, $routeParams, $route, confirmDialogSvc) {
    $scope.defaultRoller = [11,12,13,14,15,16,17,18,19,20];
    $scope.hardRoller = [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,11,12,13,14,15,16,17,18,19,20];
    $scope.harderRoller = [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20];
    $scope.deathRoller = [1,2,3,4,5,6,7,8,9,10,1,2,3,4,5,6,7,8,9,10,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20];
    $scope.tabIndex = 0;
    $scope.rollerIndex = 0;
    $scope.sneakIndex = 0;
    $scope.attackIndex = 0;
    $scope.defendIndex = 0;
    $scope.saveIndex = 0;
    $scope.damageIndex = 0;
    $scope.difficultyIndex = 0;
    $scope.damageModifierIndex = 0;
    $scope.rollHit = 0;
    $scope.rollHitNatural = 0;
    $scope.rollHitOff = 0;
    $scope.rollHitNaturalOff = 0;
    $scope.rollDmg = 0;
    $scope.rollDmgNatural = 0;
    $scope.rollDmgOff = 0;
    $scope.rollDmgNaturalOff = 0;
    $scope.critDmg = 0;
    $scope.critDmgOff = 0;
    $scope.totalDmg = 0;
    $scope.totalCritDmg = 0;
    $scope.rollDmgExplosionText = null;
    $scope.rollDmgExplosionTextOff = null;
    $scope.rollSave = 0;
    $scope.rollSaveNatural = 0;
    $scope.damageTaken = -1;
    $scope.damageAmount = 0;
    $scope.healAmount = 0;
    $scope.isFurryThrow = false;
    $scope.characterContext = vtdState.get();

    vtdSvc.getCharacter($routeParams.characterId).then(function(result) {
        vtdState.setContext(result.data);
        $scope.characterContext = vtdState.get();
    });
    
    $scope.setRollerIndex =  function(index) {
        $scope.rollerIndex = index;
        $scope.rollSave = 0;
    };
    
    $scope.setSneakIndex =  function(index) {
        $scope.sneakIndex = index;
    };
    
    $scope.setAttackIndex =  function(index) {
        $scope.attackIndex = index;
        $scope.rollHit = 0;
        $scope.rollHitNatural = 0;
        $scope.rollHitOff = 0;
        $scope.rollHitNaturalOff = 0;
        $scope.rollDmg = 0;
        $scope.rollDmgNatural = 0;
        $scope.rollDmgOff = 0;
        $scope.rollDmgNaturalOff = 0;
        $scope.critDmg = 0;
        $scope.critDmgOff = 0;
        $scope.totalDmg = 0;
        $scope.totalCritDmg = 0;
        $scope.rollDmgExplosionText = null;
        $scope.rollDmgExplosionTextOff = null;
        $scope.isFurryThrow = false;
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
    
    $scope.toggleFurryThrow =  function(isThrow) {
        $scope.isFurryThrow = isThrow;
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
    
    $scope.nextRoom = function(id) {
        confirmDialogSvc.confirm("Are you sure you wish to move to the next room?  This will reset all room effects and buffs except bardsong", function(){
            vtdSvc.nextRoom(id).then(function(result) {
                vtdState.setContext(result.data);
                $scope.characterContext = vtdState.get();
            });
        });
    };
    
    $scope.rollToHit =  function() {
        $scope.rollHit = 0;
        $scope.rollHitNatural = 0;
        $scope.rollHitOff = 0;
        $scope.rollHitNaturalOff = 0;
        $scope.rollDmg = 0;
        $scope.rollDmgNatural = 0;
        $scope.rollDmgOff = 0;
        $scope.rollDmgNaturalOff = 0;
        $scope.critDmg = 0;
        $scope.critDmgOff = 0;
        $scope.totalDmg = 0;
        $scope.totalCritDmg = 0;
        $scope.rollDmgExplosionText = null;
        $scope.rollDmgExplosionTextOff = null;
        
        if ($scope.attackIndex === 3) {
            $scope.rollHitNatural = $scope.getRoll();
            $scope.rollHit = $scope.rollHitNatural + $scope.characterContext.stats.initiative + $scope.characterContext.initBonus;
        } else {
            if (($scope.attackIndex === 0 && $scope.characterContext.meleeDmgRange.length > 0) || ($scope.attackIndex === 1 && $scope.characterContext.rangeDmgRange.length > 0) || ($scope.attackIndex === 2 && $scope.characterContext.meleePolyDmgRange.length > 0) || ($scope.attackIndex === 9 && $scope.characterContext.meleeDmgRange.length > 0)) {
                $scope.rollHitNatural = $scope.getRoll();
                $scope.rollHit = $scope.rollHitNatural;
            }

            if (($scope.characterContext.characterClass === 'MONK' || $scope.characterContext.characterClass === 'RANGER') && (($scope.attackIndex === 0 && $scope.characterContext.meleeOffhandDmgRange.length > 0) || ($scope.attackIndex === 1 && $scope.characterContext.rangeOffhandDmgRange.length > 0))) {
                $scope.rollHitNaturalOff = $scope.getRoll();
                $scope.rollHitOff = $scope.rollHitNaturalOff;
            }

            if ($scope.rollHit > 1) {
                if ($scope.attackIndex === 0) {
                    if ($scope.isFurryThrow) {
                        $scope.rollHit += $scope.characterContext.stats.rangeHit;
                        if (!$scope.hasEffect($scope.characterContext.meleeDmgEffects, "NO_DAMAGE_MOD"))
                            $scope.rollDmg = $scope.characterContext.stats.rangeDmg + 7;
                        if ($scope.characterContext.meleeDmgRange && $scope.characterContext.meleeDmgRange.length > 0) {
                            $scope.rollDmgNatural = $scope.characterContext.meleeDmgRange[$scope.getRandomInt($scope.characterContext.meleeDmgRange.length)];
                            $scope.rollDmg += $scope.rollDmgNatural;

                            if ($scope.characterContext.meleeWeaponExplodeRange && $scope.characterContext.meleeWeaponExplodeRange.includes($scope.rollDmgNatural)) {
                                $scope.rollDmgExplosionText = $scope.characterContext.meleeWeaponExplodeText;
                            }

                            if ($scope.rollHitNatural >= $scope.characterContext.meleeCritMin && !$scope.hasEffect($scope.characterContext.meleeDmgEffects, "NO_DAMAGE_MOD")) {
                                if ($scope.rollHitNatural === 20) {
                                    if ($scope.hasEffect($scope.characterContext.meleeDmgEffects, "TRIPPLE_CRIT_ON_20") || $scope.hasEffect($scope.characterContext.meleeDmgEffects, "TRIPPLE_CRIT") || ($scope.firstSlide && $scope.hasEffect($scope.characterContext.meleeDmgEffects, "TRIPPLE_CRIT_ON_FIRST_20")))
                                        $scope.critDmg = $scope.rollDmg*3;
                                    else 
                                        $scope.critDmg = $scope.rollDmg*2;
                                } else {
                                    if ($scope.hasEffect($scope.characterContext.meleeDmgEffects, "TRIPPLE_CRIT"))
                                        $scope.critDmg = $scope.rollDmg*3;
                                    else
                                        $scope.critDmg = $scope.rollDmg*2;
                                }
                            }
                        }
                    } else {
                        $scope.rollHit += $scope.characterContext.stats.meleeHit;
                        if (!$scope.hasEffect($scope.characterContext.meleeDmgEffects, "NO_DAMAGE_MOD"))
                            $scope.rollDmg = $scope.characterContext.stats.meleeDmg;
                        if ($scope.characterContext.meleeDmgRange && $scope.characterContext.meleeDmgRange.length > 0) {
                            $scope.rollDmgNatural = $scope.characterContext.meleeDmgRange[$scope.getRandomInt($scope.characterContext.meleeDmgRange.length)];
                            $scope.rollDmg += $scope.rollDmgNatural;

                            if ($scope.characterContext.meleeWeaponExplodeRange && $scope.characterContext.meleeWeaponExplodeRange.includes($scope.rollDmgNatural)) {
                                $scope.rollDmgExplosionText = $scope.characterContext.meleeWeaponExplodeText;
                            }

                            if ($scope.rollHitNatural >= $scope.characterContext.meleeCritMin && !$scope.hasEffect($scope.characterContext.meleeDmgEffects, "NO_DAMAGE_MOD")) {
                                if ($scope.rollHitNatural === 20) {
                                    if ($scope.hasEffect($scope.characterContext.meleeDmgEffects, "TRIPPLE_CRIT_ON_20") || $scope.hasEffect($scope.characterContext.meleeDmgEffects, "TRIPPLE_CRIT") || ($scope.firstSlide && $scope.hasEffect($scope.characterContext.meleeDmgEffects, "TRIPPLE_CRIT_ON_FIRST_20")))
                                        $scope.critDmg = $scope.rollDmg*3;
                                    else 
                                        $scope.critDmg = $scope.rollDmg*2;
                                } else {
                                    if ($scope.hasEffect($scope.characterContext.meleeDmgEffects, "TRIPPLE_CRIT"))
                                        $scope.critDmg = $scope.rollDmg*3;
                                    else
                                        $scope.critDmg = $scope.rollDmg*2;
                                }
                                var buff = $scope.hasBuff("Fury");
                                if (buff !== null) 
                                    $scope.removeBuff(buff);
                            } else if ($scope.hasBuff("Fury")) {
                                var buff = $scope.hasBuff("Fury");
                                if (buff !== null) {
                                    if ($scope.hasEffect($scope.characterContext.meleeDmgEffects, "TRIPPLE_CRIT"))
                                        $scope.critDmg = $scope.rollDmg*3;
                                    else
                                        $scope.critDmg = $scope.rollDmg*2;
                                    $scope.removeBuff(buff);
                                }
                            }
                        }
                    }
                } else if ($scope.attackIndex === 1) {
                    $scope.rollHit += $scope.characterContext.stats.rangeHit;
                    if (!$scope.hasEffect($scope.characterContext.rangeDmgEffects, "NO_DAMAGE_MOD"))
                        $scope.rollDmg = $scope.characterContext.stats.rangeDmg;
                    if ($scope.characterContext.rangeDmgRange && $scope.characterContext.rangeDmgRange.length > 0) {
                        $scope.rollDmgNatural = $scope.characterContext.rangeDmgRange[$scope.getRandomInt($scope.characterContext.rangeDmgRange.length)];
                        $scope.rollDmg += $scope.rollDmgNatural;

                        if ($scope.characterContext.rangeWeaponExplodeRange && $scope.characterContext.rangeWeaponExplodeRange.includes($scope.rollDmgNatural)) {
                            $scope.rollDmgExplosionText = $scope.characterContext.rangeWeaponExplodeText;
                        }

                        if ($scope.rollHitNatural >= $scope.characterContext.rangeCritMin && !$scope.hasEffect($scope.characterContext.rangeDmgEffects, "NO_DAMAGE_MOD")) {
                            if ($scope.rollHitNatural === 20) {
                                if ($scope.hasEffect($scope.characterContext.rangeDmgEffects, "TRIPPLE_CRIT_ON_20") || $scope.hasEffect($scope.characterContext.rangeDmgEffects, "TRIPPLE_CRIT") || ($scope.firstSlide && $scope.hasEffect($scope.characterContext.rangeDmgEffects, "TRIPPLE_CRIT_ON_FIRST_20")))
                                    $scope.critDmg = $scope.rollDmg*3;
                                else 
                                    $scope.critDmg = $scope.rollDmg*2;
                            } else {
                                if ($scope.hasEffect($scope.characterContext.rangeDmgEffects, "TRIPPLE_CRIT"))
                                    $scope.critDmg = $scope.rollDmg*3;
                                else
                                    $scope.critDmg = $scope.rollDmg*2;
                            }
                            var buff = $scope.hasBuff("Fury");
                            if (buff !== null) 
                                $scope.removeBuff(buff);
                        } else if ($scope.hasBuff("Fury")) {
                            var buff = $scope.hasBuff("Fury");
                            if (buff !== null) {
                                if ($scope.hasEffect($scope.characterContext.rangeDmgEffects, "TRIPPLE_CRIT"))
                                    $scope.critDmg = $scope.rollDmg*3;
                                else
                                    $scope.critDmg = $scope.rollDmg*2;
                                $scope.removeBuff(buff);
                            }
                        }
                    }
                } else if ($scope.attackIndex === 2) {
                    $scope.rollHit += $scope.characterContext.stats.meleePolyHit;
                    if (!$scope.hasEffect($scope.characterContext.meleePolyDmgEffects, "NO_DAMAGE_MOD"))
                        $scope.rollDmg = $scope.characterContext.stats.meleePolyDmg;
                    if ($scope.characterContext.meleePolyDmgRange && $scope.characterContext.meleePolyDmgRange.length > 0) {
                        $scope.rollDmgNatural = $scope.characterContext.meleePolyDmgRange[$scope.getRandomInt($scope.characterContext.meleePolyDmgRange.length)];
                        $scope.rollDmg += $scope.rollDmgNatural;

                        if ($scope.rollHitNatural >= $scope.characterContext.meleePolyCritMin && !$scope.hasEffect($scope.characterContext.meleePolyDmgEffects, "NO_DAMAGE_MOD")) {
                            if ($scope.rollHitNatural === 20) {
                                if ($scope.hasEffect($scope.characterContext.meleePolyDmgEffects, "TRIPPLE_CRIT_ON_20") || $scope.hasEffect($scope.characterContext.meleePolyDmgEffects, "TRIPPLE_CRIT") || ($scope.firstSlide && $scope.hasEffect($scope.characterContext.meleePolyDmgEffects, "TRIPPLE_CRIT_ON_FIRST_20")))
                                    $scope.critDmg = $scope.rollDmg*3;
                                else 
                                    $scope.critDmg = $scope.rollDmg*2;
                            } else {
                                if ($scope.hasEffect($scope.characterContext.meleePolyDmgEffects, "TRIPPLE_CRIT"))
                                    $scope.critDmg = $scope.rollDmg*3;
                                else
                                    $scope.critDmg = $scope.rollDmg*2;
                            }
                            var buff = $scope.hasBuff("Fury");
                            if (buff !== null) 
                                $scope.removeBuff(buff);
                        } else if ($scope.hasBuff("Fury")) {
                            var buff = $scope.hasBuff("Fury");
                            if (buff !== null) {
                                if ($scope.hasEffect($scope.characterContext.meleePolyDmgEffects, "TRIPPLE_CRIT"))
                                    $scope.critDmg = $scope.rollDmg*3;
                                else
                                    $scope.critDmg = $scope.rollDmg*2;
                                $scope.removeBuff(buff);
                            }
                        }
                    }
                } else if ($scope.attackIndex === 9) {
                    if ($scope.sneakIndex === 0) {
                        $scope.rollHit += ($scope.characterContext.stats.meleeHit + $scope.characterContext.meleeSneakHit);
                        if (!$scope.hasEffect($scope.characterContext.meleeDmgEffects, "NO_DAMAGE_MOD"))
                            $scope.rollDmg = $scope.characterContext.stats.meleeDmg + $scope.characterContext.meleeSneakDamage;
                        if ($scope.characterContext.meleeDmgRange && $scope.characterContext.meleeDmgRange.length > 0) {
                            $scope.rollDmgNatural = $scope.characterContext.meleeDmgRange[$scope.getRandomInt($scope.characterContext.meleeDmgRange.length)];
                            $scope.rollDmg += $scope.rollDmgNatural;

                            if ($scope.characterContext.meleeWeaponExplodeRange && $scope.characterContext.meleeWeaponExplodeRange.includes($scope.rollDmgNatural)) {
                                $scope.rollDmgExplosionText = $scope.characterContext.meleeWeaponExplodeText;
                            }

                            if (($scope.rollHitNatural >= $scope.characterContext.meleeCritMin || $scope.rollHitNatural >= $scope.characterContext.meleeSneakCritMin) && !$scope.hasEffect($scope.characterContext.meleeDmgEffects, "NO_DAMAGE_MOD")) {
                                if ($scope.characterContext.sneakCanCrit) {
                                    if ($scope.characterContext.stats.level === 5)
                                        $scope.rollDmg += 20;
                                    else 
                                        $scope.rollDmg += 15;
                                }

                                if ($scope.rollHitNatural === 20) {
                                    if ($scope.hasEffect($scope.characterContext.meleeDmgEffects, "TRIPPLE_CRIT_ON_20") || $scope.hasEffect($scope.characterContext.meleeDmgEffects, "TRIPPLE_CRIT") || ($scope.firstSlide && $scope.hasEffect($scope.characterContext.meleeDmgEffects, "TRIPPLE_CRIT_ON_FIRST_20")))
                                        $scope.critDmg = $scope.rollDmg*3;
                                    else 
                                        $scope.critDmg = $scope.rollDmg*2;
                                } else {
                                    if ($scope.hasEffect($scope.characterContext.meleeDmgEffects, "TRIPPLE_CRIT"))
                                        $scope.critDmg = $scope.rollDmg*3;
                                    else
                                        $scope.critDmg = $scope.rollDmg*2;
                                }

                                if (!$scope.characterContext.sneakCanCrit) {
                                    if ($scope.characterContext.stats.level === 5) {
                                        $scope.critDmg += 20;
                                        $scope.rollDmg += 20;
                                    } else {
                                        $scope.critDmg += 15;
                                        $scope.rollDmg += 15;
                                    }
                                }

                                $scope.critDmg += $scope.characterContext.unmodifiableSneakDamage;
                                $scope.rollDmg += $scope.characterContext.unmodifiableSneakDamage;

                                if ($scope.hasEffect($scope.characterContext.meleeDmgEffects, "PLUS_2_SNEAK_DAMAGE")) {
                                    $scope.critDmg += 4;
                                    $scope.rollDmg += 2;
                                }
                            } else {
                                if ($scope.characterContext.stats.level === 5)
                                    $scope.rollDmg += 20;
                                else 
                                    $scope.rollDmg += 15;
                                if ($scope.hasEffect($scope.characterContext.meleeDmgEffects, "PLUS_2_SNEAK_DAMAGE")) 
                                    $scope.rollDmg += 2;

                                $scope.rollDmg += $scope.characterContext.unmodifiableSneakDamage;
                            } 
                        }
                    } else if ($scope.sneakIndex === 1 && $scope.characterContext.sneakAtRange) {
                        $scope.rollHit += ($scope.characterContext.stats.rangeHit + $scope.characterContext.rangeSneakHit);
                        if (!$scope.hasEffect($scope.characterContext.rangeDmgEffects, "NO_DAMAGE_MOD"))
                            $scope.rollDmg = $scope.characterContext.stats.rangeDmg + $scope.characterContext.rangeSneakDamage;
                        if ($scope.characterContext.rangeDmgRange && $scope.characterContext.rangeDmgRange.length > 0) {
                            $scope.rollDmgNatural = $scope.characterContext.rangeDmgRange[$scope.getRandomInt($scope.characterContext.rangeDmgRange.length)];
                            $scope.rollDmg += $scope.rollDmgNatural;

                            if ($scope.characterContext.rangeWeaponExplodeRange && $scope.characterContext.rangeWeaponExplodeRange.includes($scope.rollDmgNatural)) {
                                $scope.rollDmgExplosionText = $scope.characterContext.rangeWeaponExplodeText;
                            }

                            if (($scope.rollHitNatural >= $scope.characterContext.rangeCritMin || $scope.rollHitNatural >= $scope.characterContext.rangeSneakCritMin) && !$scope.hasEffect($scope.characterContext.rangeDmgEffects, "NO_DAMAGE_MOD")) {
                                if ($scope.characterContext.sneakCanCrit) {
                                    if ($scope.characterContext.stats.level === 5)
                                        $scope.rollDmg += 20;
                                    else 
                                        $scope.rollDmg += 15;
                                }

                                if ($scope.rollHitNatural === 20) {
                                    if ($scope.hasEffect($scope.characterContext.rangeDmgEffects, "TRIPPLE_CRIT_ON_20") || $scope.hasEffect($scope.characterContext.rangeDmgEffects, "TRIPPLE_CRIT") || ($scope.firstSlide && $scope.hasEffect($scope.characterContext.rangeDmgEffects, "TRIPPLE_CRIT_ON_FIRST_20")))
                                        $scope.critDmg = $scope.rollDmg*3;
                                    else 
                                        $scope.critDmg = $scope.rollDmg*2;
                                } else {
                                    if ($scope.hasEffect($scope.characterContext.rangeDmgEffects, "TRIPPLE_CRIT"))
                                        $scope.critDmg = $scope.rollDmg*3;
                                    else
                                        $scope.critDmg = $scope.rollDmg*2;
                                }

                                if (!$scope.characterContext.sneakCanCrit) {
                                    if ($scope.characterContext.stats.level === 5) {
                                        $scope.critDmg += 20;
                                        $scope.rollDmg += 20;
                                    } else {
                                        $scope.critDmg += 15;
                                        $scope.rollDmg += 15;
                                    }
                                }

                                $scope.critDmg += $scope.characterContext.unmodifiableSneakDamage;
                                $scope.rollDmg += $scope.characterContext.unmodifiableSneakDamage;

                                if ($scope.hasEffect($scope.characterContext.rangeDmgEffects, "PLUS_2_SNEAK_DAMAGE")) {
                                    $scope.critDmg += 4;
                                    $scope.rollDmg += 2;
                                }
                            } else {
                                if ($scope.characterContext.stats.level === 5)
                                    $scope.rollDmg += 20;
                                else 
                                    $scope.rollDmg += 15;
                                if ($scope.hasEffect($scope.characterContext.rangeDmgEffects, "PLUS_2_SNEAK_DAMAGE")) 
                                    $scope.rollDmg += 2;

                                $scope.rollDmg += $scope.characterContext.unmodifiableSneakDamage;
                            }
                        }
                    }
                }
            }
        }
        
        if ($scope.rollHitOff > 1) {
            if ($scope.attackIndex === 0) {
                if ($scope.isFurryThrow) {
                    $scope.rollHitOff += $scope.characterContext.stats.rangeHit;
                    if (!$scope.hasEffect($scope.characterContext.meleeOffhandDmgEffects, "NO_DAMAGE_MOD"))
                        $scope.rollDmgOff = $scope.characterContext.stats.rangeDmg + 7;
                    if ($scope.characterContext.meleeOffhandDmgRange && $scope.characterContext.meleeOffhandDmgRange.length > 0) {
                        $scope.rollDmgNaturalOff = $scope.characterContext.meleeOffhandDmgRange[$scope.getRandomInt($scope.characterContext.meleeOffhandDmgRange.length)];
                        $scope.rollDmgOff += $scope.rollDmgNaturalOff;

                        if ($scope.characterContext.meleeOffhandWeaponExplodeEffect && $scope.characterContext.meleeOffhandWeaponExplodeEffect.includes($scope.rollDmgNaturalOff)) {
                            $scope.rollDmgExplosionTextOff = $scope.characterContext.meleeOffhandWeaponExplodeText;
                        }

                        if ($scope.rollHitNaturalOff >= $scope.characterContext.meleeOffhandCritMin && !$scope.hasEffect($scope.characterContext.meleeOffhandDmgEffects, "NO_DAMAGE_MOD")) {
                            if ($scope.rollHitNaturalOff === 20) {
                                if ($scope.hasEffect($scope.characterContext.meleeOffhandDmgEffects, "TRIPPLE_CRIT_ON_20") || $scope.hasEffect($scope.characterContext.meleeOffhandDmgEffects, "TRIPPLE_CRIT") || ($scope.firstSlide && $scope.hasEffect($scope.characterContext.meleeOffhandDmgEffects, "TRIPPLE_CRIT_ON_FIRST_20")))
                                    $scope.critDmgOff = $scope.rollDmgOff*3;
                                else 
                                    $scope.critDmgOff = $scope.rollDmgOff*2;
                            } else {
                                if ($scope.hasEffect($scope.characterContext.meleeOffhandDmgEffects, "TRIPPLE_CRIT"))
                                    $scope.critDmgOff = $scope.rollDmgOff*3;
                                else
                                    $scope.critDmgOff = $scope.rollDmgOff*2;
                            }
                        }
                    }
                } else {
                    $scope.rollHitOff += $scope.characterContext.stats.meleeHit;
                    if (!$scope.hasEffect($scope.characterContext.meleeOffhandDmgEffects, "NO_DAMAGE_MOD"))
                        $scope.rollDmgOff = $scope.characterContext.stats.meleeDmg;
                    if ($scope.characterContext.meleeOffhandDmgRange && $scope.characterContext.meleeOffhandDmgRange.length > 0) {
                        $scope.rollDmgNaturalOff = $scope.characterContext.meleeOffhandDmgRange[$scope.getRandomInt($scope.characterContext.meleeOffhandDmgRange.length)];
                        $scope.rollDmgOff += $scope.rollDmgNaturalOff;

                        if ($scope.characterContext.meleeOffhandWeaponExplodeEffect && $scope.characterContext.meleeOffhandWeaponExplodeEffect.includes($scope.rollDmgNaturalOff)) {
                            $scope.rollDmgExplosionTextOff = $scope.characterContext.meleeOffhandWeaponExplodeText;
                        }

                        if ($scope.rollHitNaturalOff >= $scope.characterContext.meleeOffhandCritMin && !$scope.hasEffect($scope.characterContext.meleeOffhandDmgEffects, "NO_DAMAGE_MOD")) {
                            if ($scope.rollHitNaturalOff === 20) {
                                if ($scope.hasEffect($scope.characterContext.meleeOffhandDmgEffects, "TRIPPLE_CRIT_ON_20") || $scope.hasEffect($scope.characterContext.meleeOffhandDmgEffects, "TRIPPLE_CRIT") || ($scope.firstSlide && $scope.hasEffect($scope.characterContext.meleeOffhandDmgEffects, "TRIPPLE_CRIT_ON_FIRST_20")))
                                    $scope.critDmgOff = $scope.rollDmgOff*3;
                                else 
                                    $scope.critDmgOff = $scope.rollDmgOff*2;
                            } else {
                                if ($scope.hasEffect($scope.characterContext.meleeOffhandDmgEffects, "TRIPPLE_CRIT"))
                                    $scope.critDmgOff = $scope.rollDmgOff*3;
                                else
                                    $scope.critDmgOff = $scope.rollDmgOff*2;
                            }
                            var buff = $scope.hasBuff("Fury");
                            if (buff !== null) 
                                $scope.removeBuff(buff);
                        } else if ($scope.hasBuff("Fury")) {
                            var buff = $scope.hasBuff("Fury");
                            if (buff !== null) {
                                if ($scope.hasEffect($scope.characterContext.meleeOffhandDmgEffects, "TRIPPLE_CRIT"))
                                    $scope.critDmgOff = $scope.rollDmgOff*3;
                                else
                                    $scope.critDmgOff = $scope.rollDmgOff*2;
                                $scope.removeBuff(buff);
                            }
                        }
                    }
                }
            } else if ($scope.attackIndex === 1) {
                $scope.rollHitOff += $scope.characterContext.stats.rangeHit;
                if (!$scope.hasEffect($scope.characterContext.rangeOffhandDmgEffects, "NO_DAMAGE_MOD"))
                    $scope.rollDmgOff = $scope.characterContext.stats.rangeDmg;
                if ($scope.characterContext.rangeOffhandDmgRange && $scope.characterContext.rangeOffhandDmgRange.length > 0) {
                    $scope.rollDmgNaturalOff = $scope.characterContext.rangeOffhandDmgRange[$scope.getRandomInt($scope.characterContext.rangeOffhandDmgRange.length)];
                    $scope.rollDmgOff += $scope.rollDmgNaturalOff;
                    
                    if ($scope.rollHitNaturalOff >= $scope.characterContext.rangeOffhandCritMin && !$scope.hasEffect($scope.characterContext.rangeOffhandDmgEffects, "NO_DAMAGE_MOD")) {
                        if ($scope.rollHitNaturalOff === 20) {
                            if ($scope.hasEffect($scope.characterContext.rangeOffhandDmgEffects, "TRIPPLE_CRIT_ON_20") || $scope.hasEffect($scope.characterContext.rangeOffhandDmgEffects, "TRIPPLE_CRIT") || ($scope.firstSlide && $scope.hasEffect($scope.characterContext.rangeOffhandDmgEffects, "TRIPPLE_CRIT_ON_FIRST_20")))
                                $scope.critDmgOff = $scope.rollDmgOff*3;
                            else 
                                $scope.critDmgOff = $scope.rollDmgOff*2;
                        } else {
                            if ($scope.hasEffect($scope.characterContext.rangeOffhandDmgEffects, "TRIPPLE_CRIT"))
                                $scope.critDmgOff = $scope.rollDmgOff*3;
                            else
                                $scope.critDmgOff = $scope.rollDmgOff*2;
                        }
                        var buff = $scope.hasBuff("Fury");
                        if (buff !== null) 
                            $scope.removeBuff(buff);
                    } else if ($scope.hasBuff("Fury")) {
                        var buff = $scope.hasBuff("Fury");
                        if (buff !== null) {
                            if ($scope.hasEffect($scope.characterContext.rangeOffhandDmgEffects, "TRIPPLE_CRIT"))
                                $scope.critDmgOff = $scope.rollDmgOff*3;
                            else
                                $scope.critDmgOff = $scope.rollDmgOff*2;
                            $scope.removeBuff(buff);
                        }
                    }
                }
            }
        }
        
        $scope.totalDmg = $scope.rollDmgOff + $scope.rollDmg;
        $scope.totalCritDmg = (($scope.critDmgOff > 0) ? $scope.critDmgOff : $scope.rollDmgOff) + (($scope.critDmg > 0) ? $scope.critDmg : $scope.rollDmg); 
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
    
    $scope.hasEffect = function(list, effect) {
        if(list) {
            for(var i = 0; i < list.length; i++) {
                if (list[i] === effect) {
                    return true;
                }
            }
        }
        return false;
    };
    
    $scope.hasBuff = function(buffName) {
        for(var i = 0; i < $scope.characterContext.buffs.length; i++) {
            if ($scope.characterContext.buffs[i].name === buffName) {
                return $scope.characterContext.buffs[i];
            }
        }
        return null;
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
                     
        if ($scope.damageIndex !== 4) {
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
        }
        
        if (damage > 0) {
            vtdSvc.modifyHealth($scope.characterContext.id, -1*damage).then(function(result) {
                vtdState.setContext(result.data);
                $scope.characterContext = vtdState.get();
            });
        }
    };
    
    $scope.healDamage =  function(heal) {
        this.healAmount = 0;

        //if (+$scope.characterContext.currentHealth > 0 && +heal > 0) {
        if (+heal > 0) {
            vtdSvc.modifyHealth($scope.characterContext.id, heal).then(function(result) {
                vtdState.setContext(result.data);
                $scope.characterContext = vtdState.get();
            });
        }
    };
    
    $scope.setInitBonus =  function(bonus) {
        if (bonus !== null && bonus >= 0) {
            vtdSvc.setInitBonus($scope.characterContext.id, bonus).then(function(result) {
                vtdState.setContext(result.data);
                $scope.characterContext = vtdState.get();
            });
        }
    };
    
    
    $scope.setHealthBonus =  function(bonus) {
        if (bonus !== null && bonus >= 0) {
            vtdSvc.setHealthBonus($scope.characterContext.id, bonus).then(function(result) {
                vtdState.setContext(result.data);
                $scope.characterContext = vtdState.get();
            });
        }
    };
    
    
    $scope.resetCharacter = function(id) {
        confirmDialogSvc.confirm("Are you sure you wish to reset your character?", function(){
            vtdSvc.resetCharacter(id).then(function(result) {
                vtdState.setContext(result.data);
                $scope.characterContext = vtdState.get();
                $route.updateParams({ characterId: $scope.characterContext.id });
                $route.reload();
            });
        });
    }; 
    
    $scope.getRandomInt =  function(max) {
        return Math.floor(Math.random() * Math.floor(max));
    };
    
    $scope.setPoly =  function(polyId) {
        vtdSvc.setPoly($scope.characterContext.id, polyId).then(function(result) {
            vtdState.setContext(result.data);
            $scope.characterContext = vtdState.get();
        });
    };
    
    $scope.useSkill =  function(skillId, selfTarget, selfHeal, madEvoker, lohNumber, inGameEffect) {
        vtdSvc.useSkill($scope.characterContext.id, skillId, selfTarget, selfHeal, madEvoker, lohNumber, inGameEffect).then(function(result) {
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

    tokenAdminSvc.getSelectableCharacters = function() {
        return $http.get(RESOURCES.REST_BASE_URL + '/vtd/selectablecharacters')
            .catch(function(response) {
                errorDialogSvc.showError(response);
                return($q.reject(response));
            });
    };
    
    tokenAdminSvc.getPregeneratedCharacters = function() {
        return $http.get(RESOURCES.REST_BASE_URL + '/vtd/pregenerated')
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
    
    tokenAdminSvc.setPoly = function(id, polyId) {
        return $http.post(RESOURCES.REST_BASE_URL + '/vtd/' + id + '/poly/' + polyId).catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
    
    tokenAdminSvc.useSkill = function(id, skillId, selfTarget, selfHeal, madEvoker, lohNumber, inGameEffect) {
        if (inGameEffect === null)
            inGameEffect = 'NONE';
        return $http.post(RESOURCES.REST_BASE_URL + '/vtd/' + id + '/' + skillId + '/use/?selfTarget=' + selfTarget + '&selfHeal=' + selfHeal + '&madEvoker=' + madEvoker + '&lohNumber=' + lohNumber + '&inGameEffect=' + inGameEffect).catch(function(response) {
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