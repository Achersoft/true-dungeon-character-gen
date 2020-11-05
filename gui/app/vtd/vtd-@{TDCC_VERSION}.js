angular.module('main')

.config(['$routeProvider', 'RESOURCES', function($routeProvider, RESOURCES) {
  $routeProvider
    .when('/vtd/mine', {
        templateUrl: (RESOURCES.IS_MOBILE)?'vtd/mobile/myCharacters-@{TDCC_VERSION}.html':'vtd/desktop/myCharacters-@{TDCC_VERSION}.html',
        controller: 'VtdMyCharactersCtrl'
    })
    .when('/vtd/play/:characterId', {
        templateUrl: (RESOURCES.IS_MOBILE)?'vtd/mobile/playCharacter-@{TDCC_VERSION}.html':'vtd/desktop/playCharacter-@{TDCC_VERSION}.html',
        controller: (RESOURCES.IS_MOBILE)?'VtdPlayCtrl':'VtdPlayDesktopCtrl'
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

.controller('VtdPlayDesktopCtrl', ['$scope', 'VtdSvc', 'VtdState', 'VtdHistory', 'RESOURCES', '$routeParams', '$route', 'ConfirmDialogSvc', 'WarnDialogSvc', 'MonsterSelectorSvc', function ($scope, vtdSvc, vtdState, vtdHistory, RESOURCES, $routeParams, $route, confirmDialogSvc, warnDialogSvc, monsterSelectorSvc) {
    $scope.attackIndex = 0;
    $scope.skillIndex = 0;
    $scope.resultIndex = 0;
    $scope.healAmount = null;
    $scope.adventurePasscode = null;
    $scope.isFurryThrow = false;
    $scope.furryThrowOffhandHit = 0;
    $scope.characterContext = vtdState.get();
    $scope.history = vtdHistory.get();
    $scope.lastEvent = vtdHistory.getLast();

    vtdSvc.getCharacter($routeParams.characterId).then(function(result) {
        vtdState.setContext(result.data);
        $scope.characterContext = vtdState.get();
        vtdHistory.clearData();
        $scope.history = vtdHistory.get();
        $scope.lastEvent = vtdHistory.getLast();
    });
    
    $scope.setSkillIndex =  function(index) {
        $scope.skillIndex = index;
    };
    
    $scope.setResultIndex =  function(index) {
        $scope.resultIndex = index;
    };
    
    $scope.setAttackIndex =  function(index) {
        $scope.attackIndex = index;
    };
    
    $scope.toggleFurryThrow =  function(isThrow) {
        $scope.isFurryThrow = isThrow;
        $scope.furryThrowOffhandHit = Math.round($scope.characterContext.stats.rangeHit * .75);
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
    
    $scope.setAdventure = function(id, passcode) {
        vtdSvc.setAdventure(id, passcode).then(function(result) {
            $scope.adventurePasscode = null;
            vtdState.setContext(result.data);
            $scope.characterContext = vtdState.get();
            alert("Successfully loaded adventure " + $scope.characterContext.adventureName);
        });
    };
    
    $scope.previousRoom = function(id) {
        confirmDialogSvc.confirm("Are you sure you wish to move to the previous room?  This will reset all room effects and buffs except bardsong", function(){
            vtdSvc.previousRoom(id).then(function(result) {
                vtdState.setContext(result.data);
                $scope.characterContext = vtdState.get();
            });
        }, "Move to the previous room?");
    };
    
    $scope.nextRoom = function(id) {
        confirmDialogSvc.confirm("Are you sure you wish to move to the next room?  This will reset all room effects and buffs except bardsong", function(){
            vtdSvc.nextRoom(id).then(function(result) {
                vtdState.setContext(result.data);
                $scope.characterContext = vtdState.get();
            });
        }, "Move to the next room?");
    };
    
    $scope.rollInit =  function() {
        $scope.resultIndex = 0;
        var roll = $scope.getRandomInt(20) + 1;
      
        vtdHistory.add({"type":"INIT","sub":"INIT","roll":roll,"result":roll + $scope.characterContext.stats.initiative + $scope.characterContext.initBonus});
        
        $scope.history = vtdHistory.get();
        $scope.lastEvent = vtdHistory.getLast();                      
    };
    
    $scope.rollReflex =  function() {
        $scope.resultIndex = 0;
        var save = $scope.getRandomInt(20) + 1;
        
        if (save > 1)
            vtdHistory.add({"type":"SAVE","sub":"REFLEX","roll":save,"save":save+$scope.characterContext.stats.reflex});
        else
            vtdHistory.add({"type":"SAVE","sub":"REFLEX","roll":save,"save":save});
        $scope.history = vtdHistory.get();
        $scope.lastEvent = vtdHistory.getLast();                          
    };
    
    $scope.rollFort =  function() {
        $scope.resultIndex = 0;
        var save = $scope.getRandomInt(20) + 1;
        
        if (save > 1)
            vtdHistory.add({"type":"SAVE","sub":"FORT","roll":save,"save":save+$scope.characterContext.stats.fort});
        else
            vtdHistory.add({"type":"SAVE","sub":"FORT","roll":save,"save":save});
        $scope.history = vtdHistory.get();
        $scope.lastEvent = vtdHistory.getLast();                        
    };
    
    $scope.rollWill =  function() {
        $scope.resultIndex = 0;
        var save = $scope.getRandomInt(20) + 1;
        
        if (save > 1)
            vtdHistory.add({"type":"SAVE","sub":"WILL","roll":save,"save":save+$scope.characterContext.stats.will});
        else
            vtdHistory.add({"type":"SAVE","sub":"WILL","roll":save,"save":save});
        $scope.history = vtdHistory.get();
        $scope.lastEvent = vtdHistory.getLast();                     
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
        $scope.healAmount = null;

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
    
    $scope.activatePrestigeClass = function(id) {
        confirmDialogSvc.confirm("Are you sure you wish to activate presige class?", function(){
            vtdSvc.activatePrestigeClass(id).then(function(result) {
                vtdState.setContext(result.data);
                $scope.characterContext = vtdState.get();
                $route.updateParams({ characterId: $scope.characterContext.id });
                $route.reload();
            });
        }, "Activate Prestige Class?");
    }; 
    
    $scope.resetCharacter = function(id) {
        confirmDialogSvc.confirm("Are you sure you wish to reset your character?", function(){
            vtdSvc.resetCharacter(id).then(function(result) {
                vtdState.setContext(result.data);
                $scope.characterContext = vtdState.get();
                $route.updateParams({ characterId: $scope.characterContext.id });
                $route.reload();
            });
        }, "Reset Character?");
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
    
    $scope.setPoly =  function(polyId) {
        vtdSvc.setPoly($scope.characterContext.id, polyId).then(function(result) {
            vtdState.setContext(result.data);
            $scope.characterContext = vtdState.get();
        });
    };
    
    $scope.setCompanion =  function(polyId) {
        vtdSvc.setCompanion($scope.characterContext.id, polyId).then(function(result) {
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
    
    $scope.getRoll =  function(monsterIndex) {
        if ($scope.characterContext.monsters === null || $scope.characterContext.monsters.length === 0) {
            return $scope.getRandomInt(20) + 1;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex !== undefined) {
            return monsterIndex.roller[$scope.getRandomInt(monsterIndex.roller.length)];
        } else {
            return $scope.characterContext.monsters[0].roller[$scope.getRandomInt($scope.characterContext.monsters[0].roller.length)];
        }    
    };
    
    $scope.rollToHitMelee =  function(monsterIndex) {        
        var hitRoll = 1;
        var offhandHitRoll = 1;
        var monster = null;
        
        $scope.resultIndex = 0;
        
        if ($scope.characterContext.monsters === null || $scope.characterContext.monsters.length === 0) {
            hitRoll = $scope.getRandomInt(20) + 1;
            offhandHitRoll = $scope.getRandomInt(20) + 1;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex === undefined) {
            monsterSelectorSvc.selectMonster($scope.characterContext.monsters, function(index) {
                $scope.rollToHitMelee(index);
            });
            return;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex !== undefined) {
            monster = monsterIndex;
            hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
            offhandHitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
        } else {
            monster = $scope.characterContext.monsters[0];
            hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
            offhandHitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
        }
        
        if (!('meleeOffhandHit' in $scope.characterContext.stats)) {
            offhandHitRoll = 1;
        }
        
        if (hitRoll === 1 && offhandHitRoll === 1)
            vtdHistory.add({"type":"ATTACK","sub":"MELEE","isMiss":true,"roll":1});
        else {
            var hitRollMod = 1;
            var offhandHitRollMod = 1;
            var mRollDmg = 0;
            var oRollDmg = 0;
            var mDmg = 0;
            var oDmg = 0;
            var mDmgExp = null;
            var sDmgExp = null;
            var oDmgExp = null;
            var mCritDmg = 0;
            var oCritDmg = 0;
                
            if ($scope.isFurryThrow) {
                if (hitRoll > 1)
                    hitRollMod = hitRoll + $scope.characterContext.stats.rangeHit;
                if (offhandHitRoll > 1)
                    offhandHitRollMod = offhandHitRoll + Math.round($scope.characterContext.stats.rangeHit * .75);
                if (!$scope.hasEffect($scope.characterContext.meleeDmgEffects, "NO_DAMAGE_MOD"))
                    mRollDmg = $scope.characterContext.stats.rangeDmg + 7;
                if (!$scope.hasEffect($scope.characterContext.meleeOffhandDmgEffects, "NO_DAMAGE_MOD"))
                    oRollDmg = $scope.characterContext.stats.rangeDmg + 7;
            } else {
                if (hitRoll > 1)
                    hitRollMod = hitRoll + $scope.characterContext.stats.meleeHit;
                if (offhandHitRoll > 1)
                    offhandHitRollMod = offhandHitRoll + $scope.characterContext.stats.meleeOffhandHit;
                if (!$scope.hasEffect($scope.characterContext.meleeDmgEffects, "NO_DAMAGE_MOD"))
                    mRollDmg = $scope.characterContext.stats.meleeDmg;
                if (!$scope.hasEffect($scope.characterContext.meleeOffhandDmgEffects, "NO_DAMAGE_MOD"))
                    oRollDmg = $scope.characterContext.stats.meleeDmg;
            }
                
            if (hitRoll > 1 && $scope.characterContext.meleeDmgRange && $scope.characterContext.meleeDmgRange.length > 0) {
                mDmg = $scope.characterContext.meleeDmgRange[$scope.getRandomInt($scope.characterContext.meleeDmgRange.length)];

                if ($scope.characterContext.meleeWeaponExplodeRange && $scope.characterContext.meleeWeaponExplodeRange.includes(mDmg)) {
                    if ($scope.characterContext.meleeWeaponExplodeEffect === null || $scope.characterContext.meleeWeaponExplodeEffect === "" ||
                            $scope.characterContext.meleeWeaponExplodeEffect === "NONE")
                        mDmgExp = $scope.characterContext.meleeWeaponExplodeText;
                    else if ($scope.characterContext.meleeWeaponExplodeEffect === "NATURAL_20" && hitRoll === 20)
                        mDmgExp = $scope.characterContext.meleeWeaponExplodeText;
                    else if ($scope.characterContext.meleeWeaponExplodeEffect === "CRIT" && monster.critical && hitRoll >= $scope.characterContext.meleeCritMin)
                        mDmgExp = $scope.characterContext.meleeWeaponExplodeText;
                    else if ($scope.characterContext.meleeWeaponExplodeEffect === "MISFIRE") {
                        mDmgExp = $scope.characterContext.meleeWeaponExplodeText;
                        
                        vtdSvc.modifyHealth($scope.characterContext.id, 2).then(function(result) {
                            vtdState.setContext(result.data);
                            $scope.characterContext = vtdState.get();
                        });
                    }
                }
                
                if ($scope.characterContext.meleeWeaponSecondaryExplodeRange && $scope.characterContext.meleeWeaponSecondaryExplodeRange.includes(mDmg)) {
                    if ($scope.characterContext.meleeWeaponSecondaryExplodeEffect === null ||  $scope.characterContext.meleeWeaponSecondaryExplodeEffect === "" ||
                            $scope.characterContext.meleeWeaponSecondaryExplodeEffect === "NONE")
                        sDmgExp = $scope.characterContext.meleeWeaponSecondaryExplodeText;
                    else if ($scope.characterContext.meleeWeaponSecondaryExplodeEffect === "NATURAL_20" && hitRoll === 20)
                        sDmgExp = $scope.characterContext.meleeWeaponSecondaryExplodeText;
                    else if ($scope.characterContext.meleeWeaponSecondaryExplodeEffect === "CRIT" && monster.critical && hitRoll >= $scope.characterContext.meleeCritMin)
                        sDmgExp = $scope.characterContext.meleeWeaponSecondaryExplodeText;
                    else if ($scope.characterContext.meleeWeaponSecondaryExplodeEffect === "MISFIRE") {
                        sDmgExp = $scope.characterContext.meleeWeaponSecondaryExplodeText;
                        
                        vtdSvc.modifyHealth($scope.characterContext.id, 2).then(function(result) {
                            vtdState.setContext(result.data);
                            $scope.characterContext = vtdState.get();
                        });
                    }
                }
            }

            if (offhandHitRoll > 1 && $scope.characterContext.meleeOffhandDmgRange && $scope.characterContext.meleeOffhandDmgRange.length > 0) {
                oDmg = $scope.characterContext.meleeOffhandDmgRange[$scope.getRandomInt($scope.characterContext.meleeOffhandDmgRange.length)];

                if ($scope.characterContext.meleeOffhandWeaponExplodeRange && $scope.characterContext.meleeOffhandWeaponExplodeRange.includes(oDmg)) {
                    if ($scope.characterContext.meleeOffhandWeaponExplodeEffect === null ||  $scope.characterContext.meleeOffhandWeaponExplodeEffect === "" ||
                            $scope.characterContext.meleeOffhandWeaponExplodeEffect === "NONE")
                        oDmgExp = $scope.characterContext.meleeOffhandWeaponExplodeText;
                    else if ($scope.characterContext.meleeOffhandWeaponExplodeEffect === "NATURAL_20" && hitRoll === 20)
                        oDmgExp = $scope.characterContext.meleeOffhandWeaponExplodeText;
                    else if ($scope.characterContext.meleeOffhandWeaponExplodeEffect === "CRIT" && monster.critical && hitRoll >= $scope.characterContext.meleeCritMin)
                        oDmgExp = $scope.characterContext.meleeOffhandWeaponExplodeText;
                    else if ($scope.characterContext.meleeOffhandWeaponExplodeEffect === "MISFIRE") {
                        oDmgExp = $scope.characterContext.meleeOffhandWeaponExplodeText;
                        
                        vtdSvc.modifyHealth($scope.characterContext.id, 2).then(function(result) {
                            vtdState.setContext(result.data);
                            $scope.characterContext = vtdState.get();
                        });
                    }
                }
            }
            
            var mDmgTotal = 0;
            if (hitRoll > 1)
                mDmgTotal = mDmg + mRollDmg;
            else
                mDmg = 0;
            
            var oDmgTotal = 0;
            if (offhandHitRoll > 1)
                oDmgTotal = oDmg + oRollDmg;
            else
                oDmg = 0;
            
            if (monster.fire !== 0 && +$scope.characterContext.stats.mfire) {
                if (monster.fire < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.fire;
                    if (oDmgTotal > 0)
                        oDmgTotal += -1*monster.fire;
                } else if (monster.fire - +$scope.characterContext.stats.mfire >= 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= +$scope.characterContext.stats.mfire;
                    if (oDmgTotal > 0)
                        oDmgTotal -= +$scope.characterContext.stats.mfire;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= (+$scope.characterContext.stats.mfire - monster.fire);
                    if (oDmgTotal > 0)
                        oDmgTotal -= (+$scope.characterContext.stats.mfire - monster.fire);
                } 
            } if (monster.cold !== 0 && +$scope.characterContext.stats.mcold) {
                if (monster.cold < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.cold;
                    if (oDmgTotal > 0)
                        oDmgTotal += -1*monster.cold;
                } else if (monster.cold - +$scope.characterContext.stats.mcold >= 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= +$scope.characterContext.stats.mcold;
                    if (oDmgTotal > 0)
                        oDmgTotal -= +$scope.characterContext.stats.mcold;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= (+$scope.characterContext.stats.mcold - monster.cold);
                    if (oDmgTotal > 0)
                        oDmgTotal -= (+$scope.characterContext.stats.mcold - monster.cold);
                } 
            } if (monster.shock !== 0 && +$scope.characterContext.stats.mshock) {
                if (monster.shock < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.shock;
                    if (oDmgTotal > 0)
                        oDmgTotal += -1*monster.shock;
                } else if (monster.shock - +$scope.characterContext.stats.mshock >= 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= +$scope.characterContext.stats.mshock;
                    if (oDmgTotal > 0)
                        oDmgTotal -= +$scope.characterContext.stats.mshock;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= (+$scope.characterContext.stats.mshock - monster.shock);
                    if (oDmgTotal > 0)
                        oDmgTotal -= (+$scope.characterContext.stats.mshock - monster.shock);
                }  
            } if (monster.sonic !== 0 && +$scope.characterContext.stats.msonic) {
                if (monster.sonic < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.sonic;
                    if (oDmgTotal > 0)
                        oDmgTotal += -1*monster.sonic;
                } else if (monster.sonic - +$scope.characterContext.stats.msonic >= 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= +$scope.characterContext.stats.msonic;
                    if (oDmgTotal > 0)
                        oDmgTotal -= +$scope.characterContext.stats.msonic;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= (+$scope.characterContext.stats.msonic - monster.sonic);
                    if (oDmgTotal > 0)
                        oDmgTotal -= (+$scope.characterContext.stats.msonic - monster.sonic);
                }
            } if (monster.poison !== 0 && +$scope.characterContext.stats.mpoison) {
                if (monster.poison < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.poison;
                    if (oDmgTotal > 0)
                        oDmgTotal += -1*monster.poison;
                } else if (monster.poison - +$scope.characterContext.stats.mpoison >= 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= +$scope.characterContext.stats.mpoison;
                    if (oDmgTotal > 0)
                        oDmgTotal -= +$scope.characterContext.stats.mpoison;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= (+$scope.characterContext.stats.mpoison - monster.poison);
                    if (oDmgTotal > 0)
                        oDmgTotal -= (+$scope.characterContext.stats.mpoison - monster.poison);
                }  
            } if (monster.darkrift !== 0 && +$scope.characterContext.stats.mdarkrift) {
                if (monster.darkrift < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.darkrift;
                    if (oDmgTotal > 0)
                        oDmgTotal += -1*monster.darkrift;
                } else if (monster.darkrift - +$scope.characterContext.stats.mdarkrift >= 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= +$scope.characterContext.stats.mdarkrift;
                    if (oDmgTotal > 0)
                        oDmgTotal -= +$scope.characterContext.stats.mdarkrift;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= (+$scope.characterContext.stats.mdarkrift - monster.darkrift);
                    if (oDmgTotal > 0)
                        oDmgTotal -= (+$scope.characterContext.stats.mdarkrift - monster.darkrift);
                }  
            } if (monster.sacred !== 0 && +$scope.characterContext.stats.msacred) {
                if (monster.sacred < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.sacred;
                    if (oDmgTotal > 0)
                        oDmgTotal += -1*monster.sacred;
                } else if (monster.sacred - +$scope.characterContext.stats.msacred >= 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= +$scope.characterContext.stats.msacred;
                    if (oDmgTotal > 0)
                        oDmgTotal -= +$scope.characterContext.stats.msacred;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= (+$scope.characterContext.stats.msacred - monster.sacred);
                    if (oDmgTotal > 0)
                        oDmgTotal -= (+$scope.characterContext.stats.msacred - monster.sacred);
                }   
            } if (monster.force !== 0 && +$scope.characterContext.stats.mforce) {
                if (monster.force < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.force;
                    if (oDmgTotal > 0)
                        oDmgTotal += -1*monster.force;
                } else if (monster.force - +$scope.characterContext.stats.mforce >= 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= +$scope.characterContext.stats.mforce;
                    if (oDmgTotal > 0)
                        oDmgTotal -= +$scope.characterContext.stats.mforce;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= (+$scope.characterContext.stats.mforce - monster.force);
                    if (oDmgTotal > 0)
                        oDmgTotal -= (+$scope.characterContext.stats.mforce - monster.force);
                }  
            } if (monster.acid !== 0 && +$scope.characterContext.stats.macid) {
                if (monster.acid < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.acid;
                    if (oDmgTotal > 0)
                        oDmgTotal += -1*monster.acid;
                } else if (monster.acid - +$scope.characterContext.stats.macid >= 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= +$scope.characterContext.stats.macid;
                    if (oDmgTotal > 0)
                        oDmgTotal -= +$scope.characterContext.stats.macid;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= (+$scope.characterContext.stats.macid - monster.acid);
                    if (oDmgTotal > 0)
                        oDmgTotal -= (+$scope.characterContext.stats.macid - monster.acid);
                }  
            }
            
            if (monster.critical && hitRoll >= $scope.characterContext.meleeCritMin && !$scope.hasEffect($scope.characterContext.meleeDmgEffects, "NO_DAMAGE_MOD")) {
                if (hitRoll === 20) {
                    if ($scope.hasEffect($scope.characterContext.meleeDmgEffects, "TRIPPLE_CRIT_ON_20") || $scope.hasEffect($scope.characterContext.meleeDmgEffects, "TRIPPLE_CRIT") || ($scope.firstSlide && $scope.hasEffect($scope.characterContext.meleeDmgEffects, "TRIPPLE_CRIT_ON_FIRST_20")))
                        mCritDmg = mDmgTotal * 3;
                    else 
                        mCritDmg = mDmgTotal * 2;
                } else {
                    if ($scope.hasEffect($scope.characterContext.meleeDmgEffects, "TRIPPLE_CRIT"))
                        mCritDmg = mDmgTotal * 3;
                    else
                        mCritDmg = mDmgTotal * 2;
                }
                var buff = $scope.hasBuff("Fury");
                if (buff !== null) 
                    $scope.removeBuff(buff);
            } else if ($scope.hasBuff("Fury")) {
                var buff = $scope.hasBuff("Fury");
                if (buff !== null) {
                    if ($scope.hasEffect($scope.characterContext.meleeDmgEffects, "TRIPPLE_CRIT"))
                        mCritDmg = mDmgTotal * 3;
                    else
                        mCritDmg = mDmgTotal * 2;
                    
                    $scope.removeBuff(buff);
                }
            }
            
            if (offhandHitRoll >= $scope.characterContext.meleeCritMin && !$scope.hasEffect($scope.characterContext.meleeDmgEffects, "NO_DAMAGE_MOD")) {
                if (offhandHitRoll === 20) {
                    if ($scope.hasEffect($scope.characterContext.meleeDmgEffects, "TRIPPLE_CRIT_ON_20") || $scope.hasEffect($scope.characterContext.meleeDmgEffects, "TRIPPLE_CRIT") || ($scope.firstSlide && $scope.hasEffect($scope.characterContext.meleeDmgEffects, "TRIPPLE_CRIT_ON_FIRST_20")))
                        oCritDmg = oDmgTotal * 3;
                    else 
                        oCritDmg = oDmgTotal * 2;
                } else {
                    if ($scope.hasEffect($scope.characterContext.meleeDmgEffects, "TRIPPLE_CRIT"))
                        oCritDmg = oDmgTotal * 3;
                    else
                        oCritDmg = oDmgTotal * 2;
                }
            }
                
            var totalCrit = 0;
            if (mCritDmg > 0 && oCritDmg > 0)
                totalCrit = mCritDmg + oCritDmg;
            else if (mCritDmg > 0) {
                totalCrit = mCritDmg + oDmgTotal;
            } else if (oCritDmg > 0) {
                totalCrit = oCritDmg + mDmgTotal;
            }
           
            vtdHistory.add({"type":"ATTACK","sub":"MELEE","isMiss":false,"isCrit":mCritDmg > 0 || oCritDmg > 0,"mRoll":hitRoll,"oRoll":offhandHitRoll,
                "mRollTotal":hitRollMod,"oRollTotal":offhandHitRollMod,"mWheel":mDmg,"oWheel":oDmg,"mDmg":mDmgTotal,"oDmg":oDmgTotal,"totalDmg":mDmgTotal+oDmgTotal,
                "mCrit":mCritDmg,"oCrit":oCritDmg,"critTotal":totalCrit,"mWeaponExp":mDmgExp,"sWeaponExp":sDmgExp,"oWeaponExp":oDmgExp,"fire":$scope.characterContext.stats.mfire,
                "cold":$scope.characterContext.stats.mcold,"shock":$scope.characterContext.stats.mshock,"sonic":$scope.characterContext.stats.msonic,
                "eldritch":$scope.characterContext.stats.meldritch,"poison":$scope.characterContext.stats.mpoison,"darkrift":$scope.characterContext.stats.mdarkrift,
                "sacred":$scope.characterContext.stats.msacred,"force":$scope.characterContext.stats.mforce,"acid":$scope.characterContext.stats.macid});
        }
         
        $scope.history = vtdHistory.get();
        $scope.lastEvent = vtdHistory.getLast();  
    };
    
    $scope.rollToHitRange =  function(monsterIndex) {        
        var hitRoll = 1;
        var offhandHitRoll = 1;
        var monster = null;
        var isOffhandAttack = false;
        var isCompanion = ($scope.characterContext.meleeAnimalCompanionDmgRange && $scope.characterContext.meleeAnimalCompanionDmgRange.length > 0);
        
        $scope.resultIndex = 0;
        
        if ($scope.characterContext.monsters === null || $scope.characterContext.monsters.length === 0) {
            hitRoll = $scope.getRandomInt(20) + 1;
            offhandHitRoll = $scope.getRandomInt(20) + 1;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex === undefined) {
            monsterSelectorSvc.selectMonster($scope.characterContext.monsters, function(index) {
                $scope.rollToHitRange(index);
            });
            return;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex !== undefined) {
            monster = monsterIndex;
            hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
            offhandHitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
        } else {
            monster = $scope.characterContext.monsters[0];
            hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
            offhandHitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
        }
        
        if (!('rangeOffhandHit' in $scope.characterContext.stats) && !isCompanion) {
            offhandHitRoll = 1;
        } else
            isOffhandAttack = true;
        
        if (hitRoll === 1 && offhandHitRoll === 1)
            vtdHistory.add({"type":"ATTACK","sub":"RANGE","isMiss":true,"roll":1,"isOffhandAttack":isOffhandAttack});
        else {
            var hitRollMod = 1;
            var offhandHitRollMod = 1;
            var mRollDmg = 0;
            var oRollDmg = 0;
            var mDmg = 0;
            var oDmg = 0;
            var mDmgExp = null;
            var oDmgExp = null;
            var mCritDmg = 0;
            var oCritDmg = 0;

            if (hitRoll > 1)
                hitRollMod = hitRoll + $scope.characterContext.stats.rangeHit;
            if (offhandHitRoll > 1) {
                if (isCompanion)
                    offhandHitRollMod = offhandHitRoll + $scope.characterContext.stats.meleeHit;
                else
                    offhandHitRollMod = offhandHitRoll + $scope.characterContext.stats.rangeOffhandHit;
            }
            if (!$scope.hasEffect($scope.characterContext.rangeDmgEffects, "NO_DAMAGE_MOD"))
                mRollDmg = $scope.characterContext.stats.rangeDmg;
            if (!$scope.hasEffect($scope.characterContext.rangeOffhandDmgEffects, "NO_DAMAGE_MOD") && !isCompanion)
                oRollDmg = $scope.characterContext.stats.rangeDmg;
                      
            if (hitRoll > 1 && $scope.characterContext.rangeDmgRange && $scope.characterContext.rangeDmgRange.length > 0) {
                mDmg = $scope.characterContext.rangeDmgRange[$scope.getRandomInt($scope.characterContext.rangeDmgRange.length)];

                if ($scope.characterContext.rangeWeaponExplodeRange && $scope.characterContext.rangeWeaponExplodeRange.includes(mDmg)) {
                    if ($scope.characterContext.rangeWeaponExplodeEffect === null || $scope.characterContext.rangeWeaponExplodeEffect === "" ||
                            $scope.characterContext.rangeWeaponExplodeEffect === "NONE")
                        mDmgExp = $scope.characterContext.rangeWeaponExplodeText;
                    else if ($scope.characterContext.rangeWeaponExplodeEffect === "NATURAL_20" && hitRoll === 20)
                        mDmgExp = $scope.characterContext.rangeWeaponExplodeText;
                    else if ($scope.characterContext.rangeWeaponExplodeEffect === "CRIT" && monster.critical && hitRoll >= $scope.characterContext.meleeCritMin)
                        mDmgExp = $scope.characterContext.rangeWeaponExplodeText;
                    else if ($scope.characterContext.rangeWeaponExplodeEffect === "MISFIRE") {
                        mDmgExp = $scope.characterContext.rangeWeaponExplodeText;
                        
                        vtdSvc.modifyHealth($scope.characterContext.id, 2).then(function(result) {
                            vtdState.setContext(result.data);
                            $scope.characterContext = vtdState.get();
                        });
                    }
                }
            }

            if (offhandHitRoll > 1 && !isCompanion && $scope.characterContext.rangeOffhandDmgRange && $scope.characterContext.rangeOffhandDmgRange.length > 0) {
                oDmg = $scope.characterContext.rangeOffhandDmgRange[$scope.getRandomInt($scope.characterContext.rangeOffhandDmgRange.length)];

                if ($scope.characterContext.rangeOffhandWeaponExplodeRange && $scope.characterContext.rangeOffhandWeaponExplodeRange.includes(oDmg)) {
                    if ($scope.characterContext.rangeOffhandWeaponExplodeEffect === null || $scope.characterContext.rangeOffhandWeaponExplodeEffect === "" ||
                            $scope.characterContext.rangeOffhandWeaponExplodeEffect === "NONE")
                        oDmgExp = $scope.characterContext.rangeOffhandWeaponExplodeText;
                    else if ($scope.characterContext.rangeOffhandWeaponExplodeEffect === "NATURAL_20" && hitRoll === 20)
                        oDmgExp = $scope.characterContext.rangeOffhandWeaponExplodeText;
                    else if ($scope.characterContext.rangeOffhandWeaponExplodeEffect === "CRIT" && monster.critical && hitRoll >= $scope.characterContext.meleeCritMin)
                        oDmgExp = $scope.characterContext.rangeOffhandWeaponExplodeText;
                    else if ($scope.characterContext.rangeOffhandWeaponExplodeEffect === "MISFIRE") {
                        oDmgExp = $scope.characterContext.rangeOffhandWeaponExplodeText;
                        
                        vtdSvc.modifyHealth($scope.characterContext.id, 2).then(function(result) {
                            vtdState.setContext(result.data);
                            $scope.characterContext = vtdState.get();
                        });
                    }
                }
            }
            
            if (offhandHitRoll > 1 && isCompanion) {
                oDmg = $scope.characterContext.meleeAnimalCompanionDmgRange[$scope.getRandomInt($scope.characterContext.meleeAnimalCompanionDmgRange.length)];
            }
            
            var mDmgTotal = 0;
            if (hitRoll > 1)
                mDmgTotal = mDmg + mRollDmg;
            else
                mDmg = 0;
            
            var oDmgTotal = 0;
            if (offhandHitRoll > 1)
                oDmgTotal = oDmg + oRollDmg;
            else
                oDmg = 0;
            
            if (monster.fire !== 0 && +$scope.characterContext.stats.rfire) {
                if (monster.fire < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.fire;
                    if (oDmgTotal > 0)
                        oDmgTotal += -1*monster.fire;
                } else if (monster.fire - +$scope.characterContext.stats.rfire >= 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= +$scope.characterContext.stats.rfire;
                    if (oDmgTotal > 0)
                        oDmgTotal -= +$scope.characterContext.stats.rfire;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= (+$scope.characterContext.stats.rfire - monster.fire);
                    if (oDmgTotal > 0)
                        oDmgTotal -= (+$scope.characterContext.stats.rfire - monster.fire);
                } 
            } if (monster.cold !== 0 && +$scope.characterContext.stats.rcold) {
                if (monster.cold < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.cold;
                    if (oDmgTotal > 0)
                        oDmgTotal += -1*monster.cold;
                } else if (monster.cold - +$scope.characterContext.stats.rcold >= 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= +$scope.characterContext.stats.rcold;
                    if (oDmgTotal > 0)
                        oDmgTotal -= +$scope.characterContext.stats.rcold;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= (+$scope.characterContext.stats.rcold - monster.cold);
                    if (oDmgTotal > 0)
                        oDmgTotal -= (+$scope.characterContext.stats.rcold - monster.cold);
                } 
            } if (monster.shock !== 0 && +$scope.characterContext.stats.rshock) {
                if (monster.shock < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.shock;
                    if (oDmgTotal > 0)
                        oDmgTotal += -1*monster.shock;
                } else if (monster.shock - +$scope.characterContext.stats.rshock >= 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= +$scope.characterContext.stats.rshock;
                    if (oDmgTotal > 0)
                        oDmgTotal -= +$scope.characterContext.stats.rshock;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= (+$scope.characterContext.stats.rshock - monster.shock);
                    if (oDmgTotal > 0)
                        oDmgTotal -= (+$scope.characterContext.stats.rshock - monster.shock);
                }  
            } if (monster.sonic !== 0 && +$scope.characterContext.stats.rsonic) {
                if (monster.sonic < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.sonic;
                    if (oDmgTotal > 0)
                        oDmgTotal += -1*monster.sonic;
                } else if (monster.sonic - +$scope.characterContext.stats.rsonic >= 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= +$scope.characterContext.stats.rsonic;
                    if (oDmgTotal > 0)
                        oDmgTotal -= +$scope.characterContext.stats.rsonic;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= (+$scope.characterContext.stats.rsonic - monster.sonic);
                    if (oDmgTotal > 0)
                        oDmgTotal -= (+$scope.characterContext.stats.rsonic - monster.sonic);
                }
            } if (monster.poison !== 0 && +$scope.characterContext.stats.rpoison) {
                if (monster.poison < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.poison;
                    if (oDmgTotal > 0)
                        oDmgTotal += -1*monster.poison;
                } else if (monster.poison - +$scope.characterContext.stats.rpoison >= 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= +$scope.characterContext.stats.rpoison;
                    if (oDmgTotal > 0)
                        oDmgTotal -= +$scope.characterContext.stats.rpoison;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= (+$scope.characterContext.stats.rpoison - monster.poison);
                    if (oDmgTotal > 0)
                        oDmgTotal -= (+$scope.characterContext.stats.rpoison - monster.poison);
                }  
            } if (monster.darkrift !== 0 && +$scope.characterContext.stats.rdarkrift) {
                if (monster.darkrift < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.darkrift;
                    if (oDmgTotal > 0)
                        oDmgTotal += -1*monster.darkrift;
                } else if (monster.darkrift - +$scope.characterContext.stats.rdarkrift >= 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= +$scope.characterContext.stats.rdarkrift;
                    if (oDmgTotal > 0)
                        oDmgTotal -= +$scope.characterContext.stats.rdarkrift;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= (+$scope.characterContext.stats.rdarkrift - monster.darkrift);
                    if (oDmgTotal > 0)
                        oDmgTotal -= (+$scope.characterContext.stats.rdarkrift - monster.darkrift);
                }  
            } if (monster.sacred !== 0 && +$scope.characterContext.stats.rsacred) {
                if (monster.sacred < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.sacred;
                    if (oDmgTotal > 0)
                        oDmgTotal += -1*monster.sacred;
                } else if (monster.sacred - +$scope.characterContext.stats.rsacred >= 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= +$scope.characterContext.stats.rsacred;
                    if (oDmgTotal > 0)
                        oDmgTotal -= +$scope.characterContext.stats.rsacred;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= (+$scope.characterContext.stats.rsacred - monster.sacred);
                    if (oDmgTotal > 0)
                        oDmgTotal -= (+$scope.characterContext.stats.rsacred - monster.sacred);
                }   
            } if (monster.force !== 0 && +$scope.characterContext.stats.rforce) {
                if (monster.force < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.force;
                    if (oDmgTotal > 0)
                        oDmgTotal += -1*monster.force;
                } else if (monster.force - +$scope.characterContext.stats.rforce >= 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= +$scope.characterContext.stats.rforce;
                    if (oDmgTotal > 0)
                        oDmgTotal -= +$scope.characterContext.stats.rforce;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= (+$scope.characterContext.stats.rforce - monster.force);
                    if (oDmgTotal > 0)
                        oDmgTotal -= (+$scope.characterContext.stats.rforce - monster.force);
                }  
            } if (monster.acid !== 0 && +$scope.characterContext.stats.racid) {
                if (monster.acid < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.acid;
                    if (oDmgTotal > 0)
                        oDmgTotal += -1*monster.acid;
                } else if (monster.acid - +$scope.characterContext.stats.racid >= 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= +$scope.characterContext.stats.racid;
                    if (oDmgTotal > 0)
                        oDmgTotal -= +$scope.characterContext.stats.racid;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= (+$scope.characterContext.stats.racid - monster.acid);
                    if (oDmgTotal > 0)
                        oDmgTotal -= (+$scope.characterContext.stats.racid - monster.acid);
                }  
            }
            
            if (monster.critical && hitRoll >= $scope.characterContext.rangeCritMin && !$scope.hasEffect($scope.characterContext.rangeDmgEffects, "NO_DAMAGE_MOD")) {
                if (hitRoll === 20) {
                    if ($scope.hasEffect($scope.characterContext.rangeDmgEffects, "TRIPPLE_CRIT_ON_20") || $scope.hasEffect($scope.characterContext.rangeDmgEffects, "TRIPPLE_CRIT") || ($scope.firstSlide && $scope.hasEffect($scope.characterContext.rangeDmgEffects, "TRIPPLE_CRIT_ON_FIRST_20")))
                        mCritDmg = mDmgTotal * 3;
                    else 
                        mCritDmg = mDmgTotal * 2;
                } else {
                    if ($scope.hasEffect($scope.characterContext.rangeDmgEffects, "TRIPPLE_CRIT"))
                        mCritDmg = mDmgTotal * 3;
                    else
                        mCritDmg = mDmgTotal * 2;
                }
            }
            
            if (monster.critical && offhandHitRoll >= $scope.characterContext.rangeCritMin && !$scope.hasEffect($scope.characterContext.rangeOffhandDmgEffects, "NO_DAMAGE_MOD")) {
                if (offhandHitRoll === 20) {
                    if ($scope.hasEffect($scope.characterContext.rangeOffhandDmgEffects, "TRIPPLE_CRIT_ON_20") || $scope.hasEffect($scope.characterContext.rangeOffhandDmgEffects, "TRIPPLE_CRIT") || ($scope.firstSlide && $scope.hasEffect($scope.characterContext.rangeOffhandDmgEffects, "TRIPPLE_CRIT_ON_FIRST_20")))
                        oCritDmg = oDmgTotal * 3;
                    else 
                        oCritDmg = oDmgTotal * 2;
                } else {
                    if ($scope.hasEffect($scope.characterContext.rangeOffhandDmgEffects, "TRIPPLE_CRIT"))
                        oCritDmg = oDmgTotal * 3;
                    else
                        oCritDmg = oDmgTotal * 2;
                }
            }
            
            var totalCrit = 0;
            if (mCritDmg > 0 && oCritDmg > 0)
                totalCrit = mCritDmg + oCritDmg;
            else if (mCritDmg > 0) {
                totalCrit = mCritDmg + oDmgTotal;
            } else if (oCritDmg > 0) {
                totalCrit = oCritDmg + mDmgTotal;
            }
 
            vtdHistory.add({"type":"ATTACK","sub":"RANGE","isMiss":false,"isCrit":mCritDmg > 0 || oCritDmg > 0,"mRoll":hitRoll,"oRoll":offhandHitRoll,
                "mRollTotal":hitRollMod,"oRollTotal":offhandHitRollMod,"mWheel":mDmg,"oWheel":oDmg,"mDmg":mDmgTotal,"oDmg":oDmgTotal,"totalDmg":mDmgTotal+oDmgTotal,
                "mCrit":mCritDmg,"oCrit":oCritDmg,"critTotal":totalCrit,"mWeaponExp":mDmgExp,"oWeaponExp":oDmgExp,"fire":$scope.characterContext.stats.rfire,
                "cold":$scope.characterContext.stats.rcold,"shock":$scope.characterContext.stats.rshock,"sonic":$scope.characterContext.stats.rsonic,
                "eldritch":$scope.characterContext.stats.reldritch,"poison":$scope.characterContext.stats.rpoison,"darkrift":$scope.characterContext.stats.rdarkrift,
                "sacred":$scope.characterContext.stats.rsacred,"force":$scope.characterContext.stats.rforce,"acid":$scope.characterContext.stats.racid,"isOffhandAttack":isOffhandAttack});
        }
         
        $scope.history = vtdHistory.get();
        $scope.lastEvent = vtdHistory.getLast();  
    };
    
    $scope.rollToHitPoly =  function(monsterIndex) {        
        var hitRoll = 1;
        var monster = null;
        
        if (!($scope.characterContext.meleePolyDmgRange && $scope.characterContext.meleePolyDmgRange.length > 0)) {
            warnDialogSvc.showMessage("Polymorph Seelection Required", "You must select your ploymorph form before attacking.");
            return;
        }
        
        $scope.resultIndex = 0;
        
        if ($scope.characterContext.monsters === null || $scope.characterContext.monsters.length === 0) {
            hitRoll = $scope.getRandomInt(20) + 1;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex === undefined) {
            monsterSelectorSvc.selectMonster($scope.characterContext.monsters, function(index) {
                $scope.rollToHitPoly(index);
            });
            return;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex !== undefined) {
            monster = monsterIndex;
            hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
        } else {
            monster = $scope.characterContext.monsters[0];
            hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
        }
        
        if (hitRoll === 1)
            vtdHistory.add({"type":"ATTACK","sub":"POLY","isMiss":true,"roll":1});
        else {
            var hitRollMod = 1;
            var rollDmg = 0;
            var mDmg = 0;
            var mDmgExp = null;
            var mCritDmg = 0;

            if (hitRoll > 1)
                hitRollMod = hitRoll + $scope.characterContext.stats.meleePolyHit;
            if (!$scope.hasEffect($scope.characterContext.meleePolyDmgEffects, "NO_DAMAGE_MOD"))
                rollDmg = $scope.characterContext.stats.meleePolyDmg;
                      
            if (hitRoll > 1 && $scope.characterContext.meleePolyDmgRange && $scope.characterContext.meleePolyDmgRange.length > 0) {
                mDmg = $scope.characterContext.meleePolyDmgRange[$scope.getRandomInt($scope.characterContext.meleePolyDmgRange.length)];
            }
            
            var mDmgTotal = 0;
            if (hitRoll > 1)
                mDmgTotal = mDmg + rollDmg;
            else
                mDmg = 0;
            
            var allFire = false;
            var allCold = false;
            var allShock = false;
            var allSonic = false;
            if ($scope.characterContext.poly.name === "Iktomi’s Shaper Necklace - Fire" || $scope.characterContext.poly.name === "Shaman’s Greater Necklace - Fire") {
                if (monster.fire < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.fire;
                } else if (monster.fire - mDmgTotal >= 0) {
                    mDmgTotal = 0;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= monster.fire;
                }
                allFire = true;
            } else if ($scope.characterContext.poly.name === "Iktomi’s Shaper Necklace - Ice" || $scope.characterContext.poly.name === "Shaman’s Greater Necklace - Ice") {
                if (monster.cold < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.cold;
                } else if (monster.cold - mDmgTotal >= 0) {
                    mDmgTotal = 0;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= monster.cold;
                }
                allCold = true;
            } else if ($scope.characterContext.poly.name === "Iktomi’s Shaper Necklace - Air" || $scope.characterContext.poly.name === "Shaman’s Greater Necklace - Air") {
                if (monster.shock < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.shock;
                } else if (monster.shock - mDmgTotal >= 0) {
                    mDmgTotal = 0;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= monster.shock;
                }
                allShock = true;
            } else if ($scope.characterContext.poly.name === "Iktomi’s Shaper Necklace - Earth" || $scope.characterContext.poly.name === "Shaman’s Greater Necklace - Earth") {
                if (monster.sonic < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.sonic;
                } else if (monster.sonic - mDmgTotal >= 0) {
                    mDmgTotal = 0;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= monster.sonic;
                }
                allSonic = true;
            } else {
                if (monster.fire !== 0 && +$scope.characterContext.stats.mfire) {
                    if (monster.fire < 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal += -1*monster.fire;
                    } else if (monster.fire - +$scope.characterContext.stats.mfire >= 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal -= +$scope.characterContext.stats.mfire;
                    } else {
                        if (mDmgTotal > 0)
                            mDmgTotal -= (+$scope.characterContext.stats.mfire - monster.fire);
                    } 
                } if (monster.cold !== 0 && +$scope.characterContext.stats.mcold) {
                    if (monster.cold < 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal += -1*monster.cold;
                    } else if (monster.cold - +$scope.characterContext.stats.mcold >= 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal -= +$scope.characterContext.stats.mcold;
                    } else {
                        if (mDmgTotal > 0)
                            mDmgTotal -= (+$scope.characterContext.stats.mcold - monster.cold);
                    } 
                } if (monster.shock !== 0 && +$scope.characterContext.stats.mshock) {
                    if (monster.shock < 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal += -1*monster.shock;
                    } else if (monster.shock - +$scope.characterContext.stats.mshock >= 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal -= +$scope.characterContext.stats.mshock;
                    } else {
                        if (mDmgTotal > 0)
                            mDmgTotal -= (+$scope.characterContext.stats.mshock - monster.shock);
                    }  
                } if (monster.sonic !== 0 && +$scope.characterContext.stats.msonic) {
                    if (monster.sonic < 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal += -1*monster.sonic;
                    } else if (monster.sonic - +$scope.characterContext.stats.msonic >= 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal -= +$scope.characterContext.stats.msonic;
                    } else {
                        if (mDmgTotal > 0)
                            mDmgTotal -= (+$scope.characterContext.stats.msonic - monster.sonic);
                    }
                } if (monster.poison !== 0 && +$scope.characterContext.stats.mpoison) {
                    if (monster.poison < 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal += -1*monster.poison;
                    } else if (monster.poison - +$scope.characterContext.stats.mpoison >= 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal -= +$scope.characterContext.stats.mpoison;
                    } else {
                        if (mDmgTotal > 0)
                            mDmgTotal -= (+$scope.characterContext.stats.mpoison - monster.poison);
                    }  
                } if (monster.darkrift !== 0 && +$scope.characterContext.stats.mdarkrift) {
                    if (monster.darkrift < 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal += -1*monster.darkrift;
                    } else if (monster.darkrift - +$scope.characterContext.stats.mdarkrift >= 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal -= +$scope.characterContext.stats.mdarkrift;
                    } else {
                        if (mDmgTotal > 0)
                            mDmgTotal -= (+$scope.characterContext.stats.mdarkrift - monster.darkrift);
                    }  
                } if (monster.sacred !== 0 && +$scope.characterContext.stats.msacred) {
                    if (monster.sacred < 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal += -1*monster.sacred;
                    } else if (monster.sacred - +$scope.characterContext.stats.msacred >= 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal -= +$scope.characterContext.stats.msacred;
                    } else {
                        if (mDmgTotal > 0)
                            mDmgTotal -= (+$scope.characterContext.stats.msacred - monster.sacred);
                    }   
                } if (monster.force !== 0 && +$scope.characterContext.stats.mforce) {
                    if (monster.force < 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal += -1*monster.force;
                    } else if (monster.force - +$scope.characterContext.stats.mforce >= 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal -= +$scope.characterContext.stats.mforce;
                    } else {
                        if (mDmgTotal > 0)
                            mDmgTotal -= (+$scope.characterContext.stats.mforce - monster.force);
                    }  
                } if (monster.acid !== 0 && +$scope.characterContext.stats.macid) {
                    if (monster.acid < 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal += -1*monster.acid;
                    } else if (monster.acid - +$scope.characterContext.stats.macid >= 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal -= +$scope.characterContext.stats.macid;
                    } else {
                        if (mDmgTotal > 0)
                            mDmgTotal -= (+$scope.characterContext.stats.macid - monster.acid);
                    }  
                }
            }

            var eleDmg = mDmg + rollDmg;
            if (mDmgTotal > eleDmg)
                eleDmg = mDmgTotal;
            if (monster.critical && hitRoll >= $scope.characterContext.meleePolyCritMin && !$scope.hasEffect($scope.characterContext.meleePolyDmgEffects, "NO_DAMAGE_MOD")) {
                if (hitRoll === 20) {
                    if ($scope.hasEffect($scope.characterContext.meleePolyDmgEffects, "TRIPPLE_CRIT_ON_20") || $scope.hasEffect($scope.characterContext.meleePolyDmgEffects, "TRIPPLE_CRIT") || ($scope.firstSlide && $scope.hasEffect($scope.characterContext.meleePolyDmgEffects, "TRIPPLE_CRIT_ON_FIRST_20"))) {
                        mCritDmg = mDmgTotal * 3;
                        eleDmg = eleDmg * 3;
                    } else { 
                        mCritDmg = mDmgTotal * 2;
                        eleDmg = eleDmg * 2;
                    }
                } else {
                    if ($scope.hasEffect($scope.characterContext.meleePolyDmgEffects, "TRIPPLE_CRIT")) {
                        mCritDmg = mDmgTotal * 3;
                        eleDmg = eleDmg * 3;
                    } else {
                        mCritDmg = mDmgTotal * 2;
                        eleDmg = eleDmg * 2;
                    }
                }
            }
      
            if (allFire) {
                vtdHistory.add({"type":"ATTACK","sub":"POLY","isMiss":false,"isCrit":mCritDmg > 0,"mRoll":hitRoll,"mRollTotal":hitRollMod,
                    "mWheel":mDmg,"mDmg":mDmgTotal,"totalDmg":mDmgTotal,"mCrit":mCritDmg,"critTotal":mCritDmg,"mWeaponExp":mDmgExp,"fire":eleDmg}); 
            } else if (allCold) {
                vtdHistory.add({"type":"ATTACK","sub":"POLY","isMiss":false,"isCrit":mCritDmg > 0,"mRoll":hitRoll,"mRollTotal":hitRollMod,
                    "mWheel":mDmg,"mDmg":mDmgTotal,"totalDmg":mDmgTotal,"mCrit":mCritDmg,"critTotal":mCritDmg,"mWeaponExp":mDmgExp,"cold":eleDmg}); 
            } else if (allShock) {
                vtdHistory.add({"type":"ATTACK","sub":"POLY","isMiss":false,"isCrit":mCritDmg > 0,"mRoll":hitRoll,"mRollTotal":hitRollMod,
                    "mWheel":mDmg,"mDmg":mDmgTotal,"totalDmg":mDmgTotal,"mCrit":mCritDmg,"critTotal":mCritDmg,"mWeaponExp":mDmgExp,"shock":eleDmg}); 
            } else if (allSonic) {
                vtdHistory.add({"type":"ATTACK","sub":"POLY","isMiss":false,"isCrit":mCritDmg > 0,"mRoll":hitRoll,"mRollTotal":hitRollMod,
                    "mWheel":mDmg,"mDmg":mDmgTotal,"totalDmg":mDmgTotal,"mCrit":mCritDmg,"critTotal":mCritDmg,"mWeaponExp":mDmgExp,"sonic":eleDmg}); 
            } else {
                vtdHistory.add({"type":"ATTACK","sub":"POLY","isMiss":false,"isCrit":mCritDmg > 0,"mRoll":hitRoll,"mRollTotal":hitRollMod,
                    "mWheel":mDmg,"mDmg":mDmgTotal,"totalDmg":mDmgTotal,"mCrit":mCritDmg,"critTotal":mCritDmg,"mWeaponExp":mDmgExp,"fire":$scope.characterContext.stats.mfire,
                    "cold":$scope.characterContext.stats.mcold,"shock":$scope.characterContext.stats.mshock,"sonic":$scope.characterContext.stats.msonic,
                    "eldritch":$scope.characterContext.stats.meldritch,"poison":$scope.characterContext.stats.mpoison,"darkrift":$scope.characterContext.stats.mdarkrift,
                    "sacred":$scope.characterContext.stats.msacred,"force":$scope.characterContext.stats.mforce,"acid":$scope.characterContext.stats.macid}); 
            }
        }
         
        $scope.history = vtdHistory.get();
        $scope.lastEvent = vtdHistory.getLast();  
    };
    
    $scope.rollToHitAnimalCompanion =  function(monsterIndex) {        
        var hitRoll = 1;
        var monster = null;
        
        if (!($scope.characterContext.animalCompanion && $scope.characterContext.animalCompanion.dmgRange && $scope.characterContext.animalCompanion.dmgRange.length > 0)) {
            warnDialogSvc.showMessage("Animal Companion Seelection Required", "You must select your animal companion form before attacking.");
            return;
        }
        
        $scope.resultIndex = 0;
        
        if ($scope.characterContext.monsters === null || $scope.characterContext.monsters.length === 0) {
            hitRoll = $scope.getRandomInt(20) + 1;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex === undefined) {
            monsterSelectorSvc.selectMonster($scope.characterContext.monsters, function(index) {
                $scope.rollToHitAnimalCompanion(index);
            });
            return;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex !== undefined) {
            monster = monsterIndex;
            hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
        } else {
            monster = $scope.characterContext.monsters[0];
            hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
        }
        
        if (hitRoll === 1)
            vtdHistory.add({"type":"ATTACK","sub":"POLY","isMiss":true,"roll":1});
        else {
            var hitRollMod = hitRoll;
            var mDmg = 0;
            var mCritDmg = 0;

            //if (hitRoll > 1 && $scope.characterContext.characterClass === "RANGER")
            //    hitRollMod = hitRoll + $scope.characterContext.stats.meleeHit;
                 
            if (hitRoll > 1 && $scope.characterContext.meleeAnimalCompanionDmgRange && $scope.characterContext.meleeAnimalCompanionDmgRange.length > 0) {
                mDmg = $scope.characterContext.meleeAnimalCompanionDmgRange[$scope.getRandomInt($scope.characterContext.meleeAnimalCompanionDmgRange.length)];
            }
            
            if(hitRoll === 20)
                mCritDmg = mDmg * 2;
            
            vtdHistory.add({"type":"ATTACK","sub":"POLY","isMiss":false,"isCrit":mCritDmg > 0,"mRoll":hitRoll,"mRollTotal":hitRollMod,
                "mWheel":mDmg,"mDmg":mDmg,"totalDmg":mDmg,"mCrit":mCritDmg,"critTotal":mCritDmg}); 
        }
         
        $scope.history = vtdHistory.get();
        $scope.lastEvent = vtdHistory.getLast();  
    };
    
    $scope.rollToHitSneak =  function(monsterIndex) {        
        var hitRoll = 1;
        var monster = null;
        
        $scope.resultIndex = 0;
        
        if ($scope.characterContext.monsters === null || $scope.characterContext.monsters.length === 0) {
            hitRoll = $scope.getRandomInt(20) + 1;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex === undefined) {
            monsterSelectorSvc.selectMonster($scope.characterContext.monsters, function(index) {
                $scope.rollToHitSneak(index);
            });
            return;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex !== undefined) {
            monster = monsterIndex;
            hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
        } else {
            monster = $scope.characterContext.monsters[0];
            hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
        }
 
        if (monster !== null && !monster.sneak) 
            vtdHistory.add({"type":"ATTACK","sub":"MELEE_SNEAK","isImmune":true,"immuneText":"Your sneak attack failed to find any vulnerable weak spots"});
        else if (hitRoll === 1)
            vtdHistory.add({"type":"ATTACK","sub":"MELEE_SNEAK","isImmune":false,"isMiss":true,"roll":1});
        else {
            var hitRollMod = 1;
            var rollDmg = 0;
            var mDmg = 0;
            var mDmgExp = null;
            var sDmgExp = null;
            var mCritDmg = 0;

            if (hitRoll > 1)
                hitRollMod = hitRoll + ($scope.characterContext.stats.meleeHit + $scope.characterContext.meleeSneakHit);
            if (!$scope.hasEffect($scope.characterContext.meleeDmgEffects, "NO_DAMAGE_MOD"))
                rollDmg =  $scope.characterContext.stats.meleeDmg + $scope.characterContext.meleeSneakDamage;
                      
            if (hitRoll > 1 && $scope.characterContext.meleeDmgRange && $scope.characterContext.meleeDmgRange.length > 0) {
                mDmg = $scope.characterContext.meleeDmgRange[$scope.getRandomInt($scope.characterContext.meleeDmgRange.length)];

                if ($scope.characterContext.meleeWeaponExplodeRange && $scope.characterContext.meleeWeaponExplodeRange.includes(mDmg)) {
                    if ($scope.characterContext.meleeWeaponExplodeEffect === null || $scope.characterContext.meleeWeaponExplodeEffect === "" || 
                            $scope.characterContext.meleeWeaponExplodeEffect === "NONE")
                        mDmgExp = $scope.characterContext.meleeWeaponExplodeText;
                    else if ($scope.characterContext.meleeWeaponExplodeEffect === "NATURAL_20" && hitRoll === 20)
                        mDmgExp = $scope.characterContext.meleeWeaponExplodeText;
                    else if ($scope.characterContext.meleeWeaponExplodeEffect === "CRIT" && monster.critical && hitRoll >= $scope.characterContext.meleeCritMin)
                        mDmgExp = $scope.characterContext.meleeWeaponExplodeText;
                    else if ($scope.characterContext.meleeWeaponExplodeEffect === "MISFIRE") {
                        mDmgExp = $scope.characterContext.meleeWeaponExplodeText;
                        
                        vtdSvc.modifyHealth($scope.characterContext.id, 2).then(function(result) {
                            vtdState.setContext(result.data);
                            $scope.characterContext = vtdState.get();
                        });
                    }
                }
                
                if ($scope.characterContext.meleeWeaponSecondaryExplodeRange && $scope.characterContext.meleeWeaponSecondaryExplodeRange.includes(mDmg)) {
                    if ($scope.characterContext.meleeWeaponSecondaryExplodeEffect === null || $scope.characterContext.meleeWeaponSecondaryExplodeEffect === "" ||
                            $scope.characterContext.meleeWeaponSecondaryExplodeEffect === "NONE")
                        sDmgExp = $scope.characterContext.meleeWeaponSecondaryExplodeText;
                    else if ($scope.characterContext.meleeWeaponSecondaryExplodeEffect === "NATURAL_20" && hitRoll === 20)
                        sDmgExp = $scope.characterContext.meleeWeaponSecondaryExplodeText;
                    else if ($scope.characterContext.meleeWeaponSecondaryExplodeEffect === "CRIT" && monster.critical && hitRoll >= $scope.characterContext.meleeCritMin)
                        sDmgExp = $scope.characterContext.meleeWeaponSecondaryExplodeText;
                    else if ($scope.characterContext.meleeWeaponSecondaryExplodeEffect === "MISFIRE") {
                        sDmgExp = $scope.characterContext.meleeWeaponSecondaryExplodeText;
                        
                        vtdSvc.modifyHealth($scope.characterContext.id, 2).then(function(result) {
                            vtdState.setContext(result.data);
                            $scope.characterContext = vtdState.get();
                        });
                    }
                }
                
                if ($scope.hasEffect($scope.characterContext.meleeDmgEffects, "PLUS_2_SNEAK_DAMAGE")) {
                    rollDmg += 2;
                }
            }
            
            var mDmgTotal = 0;
            if (hitRoll > 1)
                mDmgTotal = mDmg + rollDmg;
            else
                mDmg = 0;
           
            if (monster.fire !== 0 && +$scope.characterContext.stats.mfire) {
                if (monster.fire < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.fire;
                } else if (monster.fire - +$scope.characterContext.stats.mfire >= 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= +$scope.characterContext.stats.mfire;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= (+$scope.characterContext.stats.mfire - monster.fire);
                } 
            } if (monster.cold !== 0 && +$scope.characterContext.stats.mcold) {
                if (monster.cold < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.cold;
                } else if (monster.cold - +$scope.characterContext.stats.mcold >= 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= +$scope.characterContext.stats.mcold;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= (+$scope.characterContext.stats.mcold - monster.cold);
                } 
            } if (monster.shock !== 0 && +$scope.characterContext.stats.mshock) {
                if (monster.shock < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.shock;
                } else if (monster.shock - +$scope.characterContext.stats.mshock >= 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= +$scope.characterContext.stats.mshock;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= (+$scope.characterContext.stats.mshock - monster.shock);
                }  
            } if (monster.sonic !== 0 && +$scope.characterContext.stats.msonic) {
                if (monster.sonic < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.sonic;
                } else if (monster.sonic - +$scope.characterContext.stats.msonic >= 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= +$scope.characterContext.stats.msonic;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= (+$scope.characterContext.stats.msonic - monster.sonic);
                }
            } if (monster.poison !== 0 && +$scope.characterContext.stats.mpoison) {
                if (monster.poison < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.poison;
                } else if (monster.poison - +$scope.characterContext.stats.mpoison >= 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= +$scope.characterContext.stats.mpoison;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= (+$scope.characterContext.stats.mpoison - monster.poison);
                }  
            } if (monster.darkrift !== 0 && +$scope.characterContext.stats.mdarkrift) {
                if (monster.darkrift < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.darkrift;
                } else if (monster.darkrift - +$scope.characterContext.stats.mdarkrift >= 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= +$scope.characterContext.stats.mdarkrift;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= (+$scope.characterContext.stats.mdarkrift - monster.darkrift);
                }  
            } if (monster.sacred !== 0 && +$scope.characterContext.stats.msacred) {
                if (monster.sacred < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.sacred;
                } else if (monster.sacred - +$scope.characterContext.stats.msacred >= 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= +$scope.characterContext.stats.msacred;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= (+$scope.characterContext.stats.msacred - monster.sacred);
                }   
            } if (monster.force !== 0 && +$scope.characterContext.stats.mforce) {
                if (monster.force < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.force;
                } else if (monster.force - +$scope.characterContext.stats.mforce >= 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= +$scope.characterContext.stats.mforce;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= (+$scope.characterContext.stats.mforce - monster.force);
                }  
            } if (monster.acid !== 0 && +$scope.characterContext.stats.macid) {
                if (monster.acid < 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal += -1*monster.acid;
                } else if (monster.acid - +$scope.characterContext.stats.macid >= 0) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= +$scope.characterContext.stats.macid;
                } else {
                    if (mDmgTotal > 0)
                        mDmgTotal -= (+$scope.characterContext.stats.macid - monster.acid);
                }  
            }
            
            if (hitRoll > 1) {
                if (monster.critical && hitRoll >= $scope.characterContext.meleeSneakCritMin && !$scope.hasEffect($scope.characterContext.meleeDmgEffects, "NO_DAMAGE_MOD")) {
                    if ($scope.characterContext.sneakCanCrit) {
                        if ($scope.characterContext.stats.level === 5)
                            mDmgTotal += 20;
                        else 
                            mDmgTotal += 15;
                    }
                    if (hitRoll === 20) {
                        if ($scope.hasEffect($scope.characterContext.meleeDmgEffects, "TRIPPLE_CRIT_ON_20") || $scope.hasEffect($scope.characterContext.meleeDmgEffects, "TRIPPLE_CRIT") || ($scope.firstSlide && $scope.hasEffect($scope.characterContext.meleeDmgEffects, "TRIPPLE_CRIT_ON_FIRST_20")))
                            mCritDmg = mDmgTotal * 3;
                        else 
                            mCritDmg = mDmgTotal * 2;
                    } else {
                        if ($scope.hasEffect($scope.characterContext.meleeDmgEffects, "TRIPPLE_CRIT"))
                            mCritDmg = mDmgTotal * 3;
                        else
                            mCritDmg = mDmgTotal * 2;
                    }
                    if (!$scope.characterContext.sneakCanCrit) {
                        if ($scope.characterContext.stats.level === 5) {
                            mDmgTotal += 20;
                            mCritDmg += 20;
                        } else {
                            mDmgTotal += 15;
                            mCritDmg += 15;
                        }
                    }
                } else {
                    if ($scope.characterContext.stats.level === 5)
                        mDmgTotal += 20;
                    else 
                        mDmgTotal += 15;
                }
                
                mCritDmg += $scope.characterContext.unmodifiableSneakDamage;
                mDmgTotal += $scope.characterContext.unmodifiableSneakDamage;
            }
            

            vtdHistory.add({"type":"ATTACK","sub":"MELEE_SNEAK","isImmune":false,"isMiss":false,"isCrit":mCritDmg > 0,"mRoll":hitRoll,"mRollTotal":hitRollMod,
                "mWheel":mDmg,"mDmg":mDmgTotal,"totalDmg":mDmgTotal,"mCrit":mCritDmg,"critTotal":mCritDmg,"mWeaponExp":mDmgExp,"sWeaponExp":sDmgExp,"fire":$scope.characterContext.stats.mfire,
                "cold":$scope.characterContext.stats.mcold,"shock":$scope.characterContext.stats.mshock,"sonic":$scope.characterContext.stats.msonic,
                "eldritch":$scope.characterContext.stats.meldritch,"poison":$scope.characterContext.stats.mpoison,"darkrift":$scope.characterContext.stats.mdarkrift,
                "sacred":$scope.characterContext.stats.msacred,"force":$scope.characterContext.stats.mforce,"acid":$scope.characterContext.stats.macid});
        }
         
        $scope.history = vtdHistory.get();
        $scope.lastEvent = vtdHistory.getLast();  
    };
    
    $scope.rollToHitSneakRange =  function(monsterIndex) {        
        var hitRoll = 1;
        var monster = null;
        
        $scope.resultIndex = 0;
        
        if ($scope.characterContext.monsters === null || $scope.characterContext.monsters.length === 0) {
            hitRoll = $scope.getRandomInt(20) + 1;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex === undefined) {
            monsterSelectorSvc.selectMonster($scope.characterContext.monsters, function(index) {
                $scope.rollToHitSneakRange(index);
            });
            return;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex !== undefined) {
            monster = monsterIndex;
            hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
        } else {
            monster = $scope.characterContext.monsters[0];
            hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
        }
        
        if (monster !== null && !monster.sneak) 
            vtdHistory.add({"type":"ATTACK","sub":"RANGE_SNEAK","isImmune":true,"immuneText":"Your sneak attack failed to find any vulnerable weak spots"});
        else if (hitRoll === 1)
            vtdHistory.add({"type":"ATTACK","sub":"RANGE_SNEAK","isImmune":false,"isMiss":true,"roll":1});
        else {
            var hitRollMod = 1;
            var rollDmg = 0;
            var mDmg = 0;
            var mDmgExp = null;
            var mCritDmg = 0;

            if (hitRoll > 1)
                hitRollMod = hitRoll + ($scope.characterContext.stats.rangeHit + $scope.characterContext.rangeSneakHit);
            if (!$scope.hasEffect($scope.characterContext.rangeDmgEffects, "NO_DAMAGE_MOD"))
                rollDmg =  $scope.characterContext.stats.rangeDmg + $scope.characterContext.rangeSneakDamage;
                      
            if (hitRoll > 1 && $scope.characterContext.rangeDmgRange && $scope.characterContext.rangeDmgRange.length > 0) {
                mDmg = $scope.characterContext.rangeDmgRange[$scope.getRandomInt($scope.characterContext.rangeDmgRange.length)];

                if ($scope.characterContext.rangeWeaponExplodeRange && $scope.characterContext.rangeWeaponExplodeRange.includes(mDmg)) {
                    if ($scope.characterContext.rangeWeaponExplodeEffect === null || $scope.characterContext.rangeWeaponExplodeEffect === "" || 
                            $scope.characterContext.rangeWeaponExplodeEffect === "NONE")
                        mDmgExp = $scope.characterContext.rangeWeaponExplodeText;
                    else if ($scope.characterContext.rangeWeaponExplodeEffect === "NATURAL_20" && hitRoll === 20)
                        mDmgExp = $scope.characterContext.rangeWeaponExplodeText;
                    else if ($scope.characterContext.rangeWeaponExplodeEffect === "CRIT" && monster.critical && hitRoll >= $scope.characterContext.meleeCritMin)
                        mDmgExp = $scope.characterContext.rangeWeaponExplodeText;
                    else if ($scope.characterContext.rangeWeaponExplodeEffect === "MISFIRE") {
                        mDmgExp = $scope.characterContext.rangeWeaponExplodeText;
                        
                        vtdSvc.modifyHealth($scope.characterContext.id, 2).then(function(result) {
                            vtdState.setContext(result.data);
                            $scope.characterContext = vtdState.get();
                        });
                    }
                }
                
                if ($scope.hasEffect($scope.characterContext.rangeDmgEffects, "PLUS_2_SNEAK_DAMAGE")) {
                    rollDmg += 2;
                }
                
                var mDmgTotal = 0;
                if (hitRoll > 1)
                    mDmgTotal = mDmg + rollDmg;
                else
                    mDmg = 0;

                if (monster.fire !== 0 && +$scope.characterContext.stats.rfire) {
                    if (monster.fire < 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal += -1*monster.fire;
                    } else if (monster.fire - +$scope.characterContext.stats.rfire >= 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal -= +$scope.characterContext.stats.rfire;
                    } else {
                        if (mDmgTotal > 0)
                            mDmgTotal -= (+$scope.characterContext.stats.rfire - monster.fire);
                    } 
                } if (monster.cold !== 0 && +$scope.characterContext.stats.rcold) {
                    if (monster.cold < 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal += -1*monster.cold;
                    } else if (monster.cold - +$scope.characterContext.stats.rcold >= 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal -= +$scope.characterContext.stats.rcold;
                    } else {
                        if (mDmgTotal > 0)
                            mDmgTotal -= (+$scope.characterContext.stats.rcold - monster.cold);
                    } 
                } if (monster.shock !== 0 && +$scope.characterContext.stats.rshock) {
                    if (monster.shock < 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal += -1*monster.shock;
                    } else if (monster.shock - +$scope.characterContext.stats.rshock >= 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal -= +$scope.characterContext.stats.rshock;
                    } else {
                        if (mDmgTotal > 0)
                            mDmgTotal -= (+$scope.characterContext.stats.rshock - monster.shock);
                    }  
                } if (monster.sonic !== 0 && +$scope.characterContext.stats.rsonic) {
                    if (monster.sonic < 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal += -1*monster.sonic;
                    } else if (monster.sonic - +$scope.characterContext.stats.rsonic >= 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal -= +$scope.characterContext.stats.rsonic;
                    } else {
                        if (mDmgTotal > 0)
                            mDmgTotal -= (+$scope.characterContext.stats.rsonic - monster.sonic);
                    }
                } if (monster.poison !== 0 && +$scope.characterContext.stats.rpoison) {
                    if (monster.poison < 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal += -1*monster.poison;
                    } else if (monster.poison - +$scope.characterContext.stats.rpoison >= 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal -= +$scope.characterContext.stats.rpoison;
                    } else {
                        if (mDmgTotal > 0)
                            mDmgTotal -= (+$scope.characterContext.stats.rpoison - monster.poison);
                    }  
                } if (monster.darkrift !== 0 && +$scope.characterContext.stats.rdarkrift) {
                    if (monster.darkrift < 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal += -1*monster.darkrift;
                    } else if (monster.darkrift - +$scope.characterContext.stats.rdarkrift >= 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal -= +$scope.characterContext.stats.rdarkrift;
                    } else {
                        if (mDmgTotal > 0)
                            mDmgTotal -= (+$scope.characterContext.stats.rdarkrift - monster.darkrift);
                    }  
                } if (monster.sacred !== 0 && +$scope.characterContext.stats.rsacred) {
                    if (monster.sacred < 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal += -1*monster.sacred;
                    } else if (monster.sacred - +$scope.characterContext.stats.rsacred >= 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal -= +$scope.characterContext.stats.rsacred;
                    } else {
                        if (mDmgTotal > 0)
                            mDmgTotal -= (+$scope.characterContext.stats.rsacred - monster.sacred);
                    }   
                } if (monster.force !== 0 && +$scope.characterContext.stats.rforce) {
                    if (monster.force < 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal += -1*monster.force;
                    } else if (monster.force - +$scope.characterContext.stats.rforce >= 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal -= +$scope.characterContext.stats.rforce;
                    } else {
                        if (mDmgTotal > 0)
                            mDmgTotal -= (+$scope.characterContext.stats.rforce - monster.force);
                    }  
                } if (monster.acid !== 0 && +$scope.characterContext.stats.racid) {
                    if (monster.acid < 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal += -1*monster.acid;
                    } else if (monster.acid - +$scope.characterContext.stats.racid >= 0) {
                        if (mDmgTotal > 0)
                            mDmgTotal -= +$scope.characterContext.stats.racid;
                    } else {
                        if (mDmgTotal > 0)
                            mDmgTotal -= (+$scope.characterContext.stats.racid - monster.acid);
                    }  
                }
                
                if (monster.critical && hitRoll >= $scope.characterContext.rangeSneakCritMin && !$scope.hasEffect($scope.characterContext.rangeDmgEffects, "NO_DAMAGE_MOD")) {
                    if ($scope.characterContext.sneakCanCrit) {
                        if ($scope.characterContext.stats.level === 5)
                            mDmgTotal += 20;
                        else 
                            mDmgTotal += 15;
                    }
                    if (hitRoll === 20) {
                        if ($scope.hasEffect($scope.characterContext.rangeDmgEffects, "TRIPPLE_CRIT_ON_20") || $scope.hasEffect($scope.characterContext.rangeDmgEffects, "TRIPPLE_CRIT") || ($scope.firstSlide && $scope.hasEffect($scope.characterContext.rangeDmgEffects, "TRIPPLE_CRIT_ON_FIRST_20")))
                            mCritDmg = mDmgTotal * 3;
                        else 
                            mCritDmg = mDmgTotal * 2;
                    } else {
                        if ($scope.hasEffect($scope.characterContext.rangeDmgEffects, "TRIPPLE_CRIT"))
                            mCritDmg = mDmgTotal * 3;
                        else
                            mCritDmg = mDmgTotal * 2;
                    }
                    if (!$scope.characterContext.sneakCanCrit) {
                        if ($scope.characterContext.stats.level === 5) {
                            mDmgTotal += 20;
                            mCritDmg += 20;
                        } else {
                            mDmgTotal += 15;
                            mCritDmg += 15;
                        }
                    }
                } else {
                    if ($scope.characterContext.stats.level === 5)
                        mDmgTotal += 20;
                    else 
                        mDmgTotal += 15;
                }
                
                mCritDmg += $scope.characterContext.unmodifiableSneakDamage;
                mDmgTotal += $scope.characterContext.unmodifiableSneakDamage;
            }

            vtdHistory.add({"type":"ATTACK","sub":"RANGE_SNEAK","isImmune":false,"isMiss":false,"isCrit":mCritDmg > 0,"mRoll":hitRoll,"mRollTotal":hitRollMod,
                "mWheel":mDmg,"mDmg":mDmgTotal,"totalDmg":mDmgTotal,"mCrit":mCritDmg,"critTotal":mCritDmg,"mWeaponExp":mDmgExp,"fire":$scope.characterContext.stats.rfire,
                "cold":$scope.characterContext.stats.rcold,"shock":$scope.characterContext.stats.rshock,"sonic":$scope.characterContext.stats.rsonic,
                "eldritch":$scope.characterContext.stats.reldritch,"poison":$scope.characterContext.stats.rpoison,"darkrift":$scope.characterContext.stats.rdarkrift,
                "sacred":$scope.characterContext.stats.rsacred,"force":$scope.characterContext.stats.rforce,"acid":$scope.characterContext.stats.racid});
        }
         
        $scope.history = vtdHistory.get();
        $scope.lastEvent = vtdHistory.getLast();  
    };
}])

.controller('VtdPlayCtrl', ['$scope', 'VtdSvc', 'VtdState', 'RESOURCES', '$routeParams', '$route', 'ConfirmDialogSvc', 'MonsterSelectorSvc', function ($scope, vtdSvc, vtdState, RESOURCES, $routeParams, $route, confirmDialogSvc, monsterSelectorSvc) {
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
    
    $scope.setAdventure = function(id, passcode) {
        vtdSvc.setAdventure(id, passcode).then(function(result) {
            $scope.adventurePasscode = null;
            vtdState.setContext(result.data);
            $scope.characterContext = vtdState.get();
            alert("Successfully loaded adventure " + $scope.characterContext.adventureName);
        });
    };
    
    $scope.previousRoom = function(id) {
        confirmDialogSvc.confirm("Are you sure you wish to move to the previous room?  This will reset all room effects and buffs except bardsong", function(){
            vtdSvc.previousRoom(id).then(function(result) {
                vtdState.setContext(result.data);
                $scope.characterContext = vtdState.get();
            });
        });
    };
    
    $scope.nextRoom = function(id) {
        confirmDialogSvc.confirm("Are you sure you wish to move to the next room?  This will reset all room effects and buffs except bardsong", function(){
            vtdSvc.nextRoom(id).then(function(result) {
                vtdState.setContext(result.data);
                $scope.characterContext = vtdState.get();
            });
        });
    };
    
    $scope.rollToHit =  function(monsterIndex) {
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
            $scope.rollHitNatural = $scope.harderRoller[$scope.getRandomInt($scope.harderRoller.length)];
            $scope.rollHit = $scope.rollHitNatural + $scope.characterContext.stats.initiative + $scope.characterContext.initBonus;
        } else {
            var monster = null;
        
            if ($scope.characterContext.monsters === null || $scope.characterContext.monsters.length === 0) {
                if (($scope.attackIndex === 0 && $scope.characterContext.meleeDmgRange.length > 0) || ($scope.attackIndex === 1 && $scope.characterContext.rangeDmgRange.length > 0) || ($scope.attackIndex === 2 && $scope.characterContext.meleePolyDmgRange.length > 0) || ($scope.attackIndex === 9 && $scope.characterContext.meleeDmgRange.length > 0)) {
                    $scope.rollHitNatural = $scope.getRandomInt(20) + 1;
                    $scope.rollHit = $scope.rollHitNatural;
                }
                if (($scope.characterContext.characterClass === 'MONK' || $scope.characterContext.characterClass === 'RANGER') && (($scope.attackIndex === 0 && $scope.characterContext.meleeOffhandDmgRange.length > 0) || ($scope.attackIndex === 1 && $scope.characterContext.rangeOffhandDmgRange.length > 0))) {
                    $scope.rollHitNaturalOff = $scope.getRandomInt(20) + 1;
                    $scope.rollHitOff = $scope.rollHitNaturalOff;
                }
            } else if ($scope.characterContext.monsters.length > 1 && monsterIndex === undefined) {
                monsterSelectorSvc.selectMonster($scope.characterContext.monsters, function(index) {
                    $scope.rollToHit(index);
                });
                return;
            } else if ($scope.characterContext.monsters.length > 1 && monsterIndex !== undefined) {
                monster = monsterIndex;
                if (($scope.attackIndex === 0 && $scope.characterContext.meleeDmgRange.length > 0) || ($scope.attackIndex === 1 && $scope.characterContext.rangeDmgRange.length > 0) || ($scope.attackIndex === 2 && $scope.characterContext.meleePolyDmgRange.length > 0) || ($scope.attackIndex === 9 && $scope.characterContext.meleeDmgRange.length > 0)) {
                    $scope.rollHitNatural = monster.roller[$scope.getRandomInt(monster.roller.length)];
                    $scope.rollHit = $scope.rollHitNatural;
                }
                if (($scope.characterContext.characterClass === 'MONK' || $scope.characterContext.characterClass === 'RANGER') && (($scope.attackIndex === 0 && $scope.characterContext.meleeOffhandDmgRange.length > 0) || ($scope.attackIndex === 1 && $scope.characterContext.rangeOffhandDmgRange.length > 0))) {
                    $scope.rollHitNaturalOff = monster.roller[$scope.getRandomInt(monster.roller.length)];
                    $scope.rollHitOff = $scope.rollHitNaturalOff;
                }
            } else {
                monster = $scope.characterContext.monsters[0];
                if (($scope.attackIndex === 0 && $scope.characterContext.meleeDmgRange.length > 0) || ($scope.attackIndex === 1 && $scope.characterContext.rangeDmgRange.length > 0) || ($scope.attackIndex === 2 && $scope.characterContext.meleePolyDmgRange.length > 0) || ($scope.attackIndex === 9 && $scope.characterContext.meleeDmgRange.length > 0)) {
                    $scope.rollHitNatural = monster.roller[$scope.getRandomInt(monster.roller.length)];
                    $scope.rollHit = $scope.rollHitNatural;
                }
                if (($scope.characterContext.characterClass === 'MONK' || $scope.characterContext.characterClass === 'RANGER') && (($scope.attackIndex === 0 && $scope.characterContext.meleeOffhandDmgRange.length > 0) || ($scope.attackIndex === 1 && $scope.characterContext.rangeOffhandDmgRange.length > 0))) {
                    $scope.rollHitNaturalOff = monster.roller[$scope.getRandomInt(monster.roller.length)];
                    $scope.rollHitOff = $scope.rollHitNaturalOff;
                }
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
                    $scope.rollHitOff += Math.round($scope.characterContext.stats.rangeHit * .75);
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
                    $scope.rollHitOff += Math.round($scope.characterContext.stats.meleeHit * .75);
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
                $scope.rollHitOff += Math.round($scope.characterContext.stats.rangeHit * .75);
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
        $scope.rollSaveNatural = $scope.harderRoller[$scope.getRandomInt($scope.harderRoller.length)];
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
        return $scope.harderRoller[$scope.getRandomInt($scope.harderRoller.length)];       
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
    
    $scope.activatePrestigeClass = function(id) {
        confirmDialogSvc.confirm("Are you sure you wish to activate presige class?", function(){
            vtdSvc.activatePrestigeClass(id).then(function(result) {
                vtdState.setContext(result.data);
                $scope.characterContext = vtdState.get();
                $route.updateParams({ characterId: $scope.characterContext.id });
                $route.reload();
            });
        }, "Activate Prestige Class?");
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
            if (vtdState.characterClass === 'RANGER') {
                if (!(vtdState.meleeOffhandDmgRange === null || vtdState.meleeOffhandDmgRange.length === 0)) {
                    vtdState.stats.meleeOffhandHit = Math.round(vtdState.stats.meleeHit * .75);
                }
                if (!(vtdState.rangeOffhandDmgRange === null || vtdState.rangeOffhandDmgRange.length === 0)) {
                    vtdState.stats.rangeOffhandHit = vtdState.stats.rangeHit;
                }
            }
   
            if (vtdState.characterClass === 'MONK') {
                if (!(vtdState.meleeOffhandDmgRange === null || vtdState.meleeOffhandDmgRange.length === 0)) {
                    vtdState.stats.meleeOffhandHit = Math.round(vtdState.stats.meleeHit * .75);
                }
                if (!(vtdState.rangeOffhandDmgRange === null || vtdState.rangeOffhandDmgRange.length === 0)) {
                    vtdState.stats.rangeOffhandHit = Math.round(vtdState.stats.rangeHit * .75);
                }
            }  
        }
        
        function get() {
            return vtdState;
        }

        return {
            setContext: setContext,
            get: get
        };
    }])

.factory('VtdHistory', [
    function() {                    
        var vtdHistory = [];
        var vtdLast = {};
        
        function clearData() {
            vtdHistory = [];
            vtdLast = {};
        }
       
        function add(data) {
            vtdLast = data;
            vtdHistory.push(data);
        }
        
        function get() {
            return vtdHistory;
        }
        
        function getLast() {
            return vtdLast;
        }

        return {
            clearData: clearData,
            add: add,
            get: get,
            getLast: getLast
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
    
    tokenAdminSvc.setCompanion = function(id, polyId) {
        return $http.post(RESOURCES.REST_BASE_URL + '/vtd/' + id + '/companion/' + polyId).catch(function(response) {
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
    
    tokenAdminSvc.previousRoom = function(id) {
        return $http.post(RESOURCES.REST_BASE_URL + '/vtd/' + id + '/previous').catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
    
    tokenAdminSvc.nextRoom = function(id) {
        return $http.post(RESOURCES.REST_BASE_URL + '/vtd/' + id + '/next').catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
    
    tokenAdminSvc.setAdventure = function(id, passcode) {
        return $http.post(RESOURCES.REST_BASE_URL + '/vtd/' + id + '/adventure/?passcode=' + passcode)
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
    
    tokenAdminSvc.activatePrestigeClass = function(id) {
        return $http.post(RESOURCES.REST_BASE_URL + '/vtd/' + id + '/prestige')
            .catch(function(response) {
                errorDialogSvc.showError(response);
                return($q.reject(response));
            });
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