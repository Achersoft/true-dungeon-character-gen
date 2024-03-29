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
    
    $scope.addCharacter = function(characterId) {
        vtdSvc.importCharacter(characterId).then(function(result) {
            $scope.myCharacterContext = result.data;
        });
    };
    
    $scope.deleteCharacter = function(id, name){
        confirmDialogSvc.confirm("Are you sure you wish to delete character " + name +"?", function(){
           vtdSvc.deleteCharacter(id).then(function(result) {
                $scope.myCharacterContext = result.data;
            });
        });
    };
}])

.controller('VtdPlayDesktopCtrl', ['$scope', '$q', 'VtdSvc', 'VtdState', 'VtdHistory', 'RESOURCES', '$routeParams', '$route', 'ConfirmDialogSvc', 'WarnDialogSvc', 'MonsterSelectorSvc', function ($scope, $q, vtdSvc, vtdState, vtdHistory, RESOURCES, $routeParams, $route, confirmDialogSvc, warnDialogSvc, monsterSelectorSvc) {
    $scope.attackIndex = 0;
    $scope.skillIndex = 0;
    $scope.resultIndex = 0;
    $scope.healAmount = null;
    $scope.adventurePasscode = null;
    $scope.isFurryThrow = false;
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
    
    $scope.setRollerId = function(id, rollerId) {
        vtdSvc.setRollerId(id, rollerId).then(function(result) {
            vtdState.setContext(result.data);
            $scope.characterContext = vtdState.get();
            alert("Successfully updated adventure roller ID to " + $scope.characterContext.rollerId);
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
        
        if ($scope.characterContext.rollerId !== null) {
            vtdSvc.sendRoll('{"version":1,"slotId":"'+$scope.characterContext.rollerId+'","classId":"'+$scope.characterContext.rollerCharacterClass+'","eventType":"initiative_roll",' +
                '"rolls":[{"dieResult":'+roll+',"modifiedResult":'+(roll+$scope.characterContext.stats.initiative+$scope.characterContext.initBonus)+'}]}').catch(function(response) {
                vtdHistory.add({"type":"ROLLER","sub":"Initiative Roll","result":response.data.errors[0]});       
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast(); 
                return($q.reject(response));
            }).then(function(response) {
                vtdHistory.add({"type":"ROLLER","sub":"Initiative Roll","result":"Initiative roll sent"});
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast();   
            });
        } else {
            vtdHistory.add({"type":"INIT","sub":"INIT","roll":roll,"result":roll + $scope.characterContext.stats.initiative + $scope.characterContext.initBonus});
            $scope.history = vtdHistory.get();
            $scope.lastEvent = vtdHistory.getLast();   
        } 
    };
    
    $scope.rollReflex =  function(monsterIndex) {
        $scope.resultIndex = 0;
        var save = $scope.getRandomInt(20) + 1;
        var questers = false;
        
        if (save === 1 && $scope.characterContext.questors) {
            save = $scope.getRandomInt(20) + 1;
            questers = true;
        }
        var saveMod = (save > 1) ? (save + $scope.characterContext.stats.reflex) : 1;
        var monsterSave = null;
        var monsterEffect = "missed";
        
        if ($scope.characterContext.monsters === null || $scope.characterContext.monsters.length === 0) {
            monsterSave = 0;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex === undefined) {
            monsterSelectorSvc.selectMonster($scope.characterContext.monsters, function(index) {
                $scope.rollReflex(index);
            });
            return;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex !== undefined) {
            monsterSave = monsterIndex.reflex;
            monsterEffect = monsterIndex.reflexEffect;
        } else {
            monsterSave = $scope.characterContext.monsters[0].reflex;
            monsterEffect = $scope.characterContext.monsters[0].reflexEffect;
        }
        
        if (monsterEffect === undefined || monsterEffect === null)
            monsterEffect = "missed";
        
        var saved = (saveMod >= monsterSave) ? true : false;

        if (saved) {
            monsterEffect = "";
            if (questers)
                monsterEffect = "quester";
        }
        
        if ($scope.characterContext.rollerId !== null) {
            vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "save_roll", ' +
                ' "rolls": [{ "type": "reflex", "dieResult": ' + save + ', "modifiedResult": ' + saveMod + ', "isSuccess": ' + saved + ', "effect": "' + monsterEffect + '" }]}').catch(function(response) {
                vtdHistory.add({"type":"ROLLER","sub":"Reflex Save","result":response.data.errors[0]});       
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast(); 
                return($q.reject(response));
            }).then(function(response) {
                vtdHistory.add({"type":"ROLLER","sub":"Reflex Save","result":"Reflex save roll sent"});
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast();   
            });
        } else {
            if (save > 1)
                vtdHistory.add({"type":"SAVE","sub":"REFLEX","roll":save,"save":save+$scope.characterContext.stats.reflex});
            else
                vtdHistory.add({"type":"SAVE","sub":"REFLEX","roll":save,"save":save});
            $scope.history = vtdHistory.get();
            $scope.lastEvent = vtdHistory.getLast(); 
        }                         
    };
    
    $scope.rollFort =  function(monsterIndex) {
        $scope.resultIndex = 0;
        var save = $scope.getRandomInt(20) + 1;
        var questers = false;
        
        if (save === 1 && $scope.characterContext.questors) {
            save = $scope.getRandomInt(20) + 1;
            questers = true;
        }
        var saveMod = (save > 1) ? (save + $scope.characterContext.stats.fort) : 1;
        var monsterSave = null;
        var monsterEffect = "missed";
        
        if ($scope.characterContext.monsters === null || $scope.characterContext.monsters.length === 0) {
            monsterSave = 0;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex === undefined) {
            monsterSelectorSvc.selectMonster($scope.characterContext.monsters, function(index) {
                $scope.rollFort(index);
            });
            return;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex !== undefined) {
            monsterSave = monsterIndex.fort;
            monsterEffect = monsterIndex.fortEffect;
        } else {
            monsterSave = $scope.characterContext.monsters[0].fort;
            monsterEffect = $scope.characterContext.monsters[0].fortEffect;
        }
        
        if (monsterEffect === undefined || monsterEffect === null)
            monsterEffect = "missed";
        
        var saved = (saveMod >= monsterSave) ? true : false;

        if (saved) {
            monsterEffect = "";
            if (questers)
                monsterEffect = "quester";
        }
        
        if ($scope.characterContext.rollerId !== null) {
            vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "save_roll", ' +
                ' "rolls": [{ "type": "fortitude", "dieResult": ' + save + ', "modifiedResult": ' + saveMod + ', "isSuccess": ' + saved + ', "effect": "' + monsterEffect + '" }]}').catch(function(response) {
                vtdHistory.add({"type":"ROLLER","sub":"Fortitude Save","result":response.data.errors[0]});       
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast(); 
                return($q.reject(response));
            }).then(function(response) {
                vtdHistory.add({"type":"ROLLER","sub":"Fortitude Save","result":"Fortitude save roll sent"});
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast();   
            });
        } else {
            if (save > 1)
                vtdHistory.add({"type":"SAVE","sub":"FORT","roll":save,"save":save+$scope.characterContext.stats.fort});
            else
                vtdHistory.add({"type":"SAVE","sub":"FORT","roll":save,"save":save});
            $scope.history = vtdHistory.get();
            $scope.lastEvent = vtdHistory.getLast();  
        }                    
    };
    
    $scope.rollWill =  function(monsterIndex) {
        $scope.resultIndex = 0;
        var save = $scope.getRandomInt(20) + 1;
        var questers = false;
        
        if (save === 1 && $scope.characterContext.questors) {
            save = $scope.getRandomInt(20) + 1;
            questers = true;
        }
        var saveMod = (save > 1) ? (save + $scope.characterContext.stats.will) : 1;
        var monsterSave = null;
        var monsterEffect = "missed";
        
        if ($scope.characterContext.monsters === null || $scope.characterContext.monsters.length === 0) {
            monsterSave = 0;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex === undefined) {
            monsterSelectorSvc.selectMonster($scope.characterContext.monsters, function(index) {
                $scope.rollWill(index);
            });
            return;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex !== undefined) {
            monsterSave = monsterIndex.will;
            monsterEffect = monsterIndex.willEffect;
        } else {
            monsterSave = $scope.characterContext.monsters[0].will;
            monsterEffect = $scope.characterContext.monsters[0].willEffect;
        }
        
        if (monsterEffect === undefined || monsterEffect === null)
            monsterEffect = "missed";
        
        var saved = (saveMod >= monsterSave) ? true : false;

        if (saved) {
            monsterEffect = "";
            if (questers)
                monsterEffect = "quester";
        }

        if ($scope.characterContext.rollerId !== null) {
            vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "save_roll", ' +
                ' "rolls": [{ "type": "will", "dieResult": ' + save + ', "modifiedResult": ' + saveMod + ', "isSuccess": ' + saved + ', "effect": "' + monsterEffect + '" }]}').catch(function(response) {
                vtdHistory.add({"type":"ROLLER","sub":"Will Save","result":response.data.errors[0]});       
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast(); 
                return($q.reject(response));
            }).then(function(response) {
                vtdHistory.add({"type":"ROLLER","sub":"Will Save","result":"Will save roll sent"});
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast();   
            });
        } else {
            if (save > 1)
                vtdHistory.add({"type":"SAVE","sub":"WILL","roll":save,"save":save+$scope.characterContext.stats.will});
            else
                vtdHistory.add({"type":"SAVE","sub":"WILL","roll":save,"save":save});
            $scope.history = vtdHistory.get();
            $scope.lastEvent = vtdHistory.getLast();   
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
    
    $scope.setBraceletCabalBonus =  function(bonus) {
        if (bonus !== null && bonus >= 0) {
            vtdSvc.setBraceletCabalBonus($scope.characterContext.id, bonus).then(function(result) {
                vtdState.setContext(result.data);
                $scope.characterContext = vtdState.get();
            });
        }
    };
    
    $scope.activatePrestigeClass = function(id) {
        confirmDialogSvc.confirm("Are you sure you wish to activate prestige class?", function(){
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
    
    $scope.setPoly = function(polyId) {
        vtdSvc.setPoly($scope.characterContext.id, polyId).then(function(result) {
            vtdState.setContext(result.data);
            $scope.characterContext = vtdState.get();
        });
    };
    
    $scope.setCompanion = function(polyId) {
        vtdSvc.setCompanion($scope.characterContext.id, polyId).then(function(result) {
            vtdState.setContext(result.data);
            $scope.characterContext = vtdState.get();
        });
    };
    
    $scope.execSkillQueue =  function() {
        vtdSvc.execSkillQueue($scope.characterContext.id).then(function(result) {
            vtdState.setContext(result.data);
            $scope.characterContext = vtdState.get();
            
            if ($scope.characterContext.rollerId !== null) {
                vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                        ' "rolls": [{ "type": "spell", "dieResult": ' + 1 + ', "modifiedResult": ' + 1 + ', "isSuccess": ' + true + ', "damage": ' + $scope.characterContext.totalDamageLastSpell + ' }]}').catch(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Spell","result":response.data.errors[0]});       
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast(); 
                        return($q.reject(response));
                    }).then(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Spell","result":"Spell attack roll sent for " + $scope.characterContext.totalDamageLastSpell});
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast();   
                    });
                
            } else {
                vtdHistory.add({"type":"ROLLER","sub":"Spell","result":"Spell attack total " + $scope.characterContext.totalDamageLastSpell});
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast();   
            }
        });
    };
    
    $scope.resendSkillQueue =  function() {
        if ($scope.characterContext.rollerId !== null) {
            vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                    ' "rolls": [{ "type": "spell", "dieResult": ' + 1 + ', "modifiedResult": ' + 1 + ', "isSuccess": ' + true + ', "damage": ' + $scope.characterContext.totalDamageLastSpell + ' }]}').catch(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Spell","result":response.data.errors[0]});       
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast(); 
                    return($q.reject(response));
                }).then(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Spell","result":"Spell attack roll sent for " + $scope.characterContext.totalDamageLastSpell});
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast();   
                });

        } else {
            vtdHistory.add({"type":"ROLLER","sub":"Spell","result":"Spell attack total " + $scope.characterContext.totalDamageLastSpell});
            $scope.history = vtdHistory.get();
            $scope.lastEvent = vtdHistory.getLast();   
        }
    };
    
    $scope.deleteQueuedSkills =  function() {
        vtdSvc.deleteQueuedSkills($scope.characterContext.id).then(function(result) {
            vtdState.setContext(result.data);
            $scope.characterContext = vtdState.get();
        });
    };
    
    $scope.useSkill = function(skillId, selfTarget, selfHeal, madEvoker, lohNumber, inGameEffect, markUse, ignoreUse, isSuccess, die, modifiedDie, damage, isQueue) {
        if (isQueue) {
            vtdSvc.queueSkill($scope.characterContext.id, skillId, selfTarget, selfHeal, madEvoker, lohNumber, inGameEffect, markUse, ignoreUse, damage).then(function(result) {
                vtdState.setContext(result.data);
                $scope.characterContext = vtdState.get();
            });  
        } else {
            if ($scope.characterContext.rollerId !== null && damage > -1) {
                if (isSuccess && die === 0) {
                    die = 1;
                    modifiedDie = 1;
                }
                vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                        ' "rolls": [{ "type": "spell", "dieResult": ' + die + ', "modifiedResult": ' + modifiedDie + ', "isSuccess": ' + isSuccess + ', "damage": ' + damage + ' }]}').catch(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Spell","result":response.data.errors[0]});       
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast(); 
                        return($q.reject(response));
                    }).then(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Spell","result":"Spell attack roll sent"});
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast();   
                        vtdSvc.useSkill($scope.characterContext.id, skillId, selfTarget, selfHeal, madEvoker, lohNumber, inGameEffect, markUse, ignoreUse).then(function(result) {
                            vtdState.setContext(result.data);
                            $scope.characterContext = vtdState.get();
                        });
                    });
                
            } else {
                vtdSvc.useSkill($scope.characterContext.id, skillId, selfTarget, selfHeal, madEvoker, lohNumber, inGameEffect, markUse, ignoreUse).then(function(result) {
                    vtdState.setContext(result.data);
                    $scope.characterContext = vtdState.get();
                });
            }
        }
    };
    
    $scope.unuseSkill = function(skilId) {
        vtdSvc.unuseSkill($scope.characterContext.id, skilId).then(function(result) {
            vtdState.setContext(result.data);
            $scope.characterContext = vtdState.get();
        });
    };
    
    $scope.activateBuff = function(buff) {
        vtdSvc.addBuff($scope.characterContext.id, buff.id, buff.level).then(function(result) {
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
    
    $scope.activateDebuff =  function(debuff) {
        vtdSvc.addDebuff($scope.characterContext.id, debuff.id).then(function(result) {
            vtdState.setContext(result.data);
            $scope.characterContext = vtdState.get();
        });
    };
    
    $scope.removeDebuff =  function(debuff) {
        vtdSvc.removeDebuff($scope.characterContext.id, debuff.id).then(function(result) {
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
    
    $scope.rollToHitMelee =  function(monsterIndex, status) {        
        var hitRoll = 1;
        var hitRollOG = 1;
        var offhandHitRoll = 1;
        var offhandHitRollOG = 1;
        var monster = null;
        var flankUsed = false;
        var statusEffect = status === undefined ? '' :  status;
        var offHandStatusEffect = '';
        
        $scope.resultIndex = 0;
        
        var buff = $scope.hasBuff("Flank");
        if (buff !== null) {
            $scope.removeBuff(buff);
            flankUsed = true;
            statusEffect = 'flank buff';
        }
        var critImmuneUsed = false;
        var buff2 = $scope.hasBuff("Ignore Sneak & Crit Immune");
        if (buff2 !== null) {
            $scope.removeBuff(buff2);
            critImmuneUsed = true;
        }
        
        if ($scope.characterContext.monsters === null || $scope.characterContext.monsters.length === 0) {
            if (flankUsed || $scope.hasEffect($scope.characterContext.meleeDmgEffects, "SMACK_WEAPON")) {
                hitRoll = Math.max(($scope.getRandomInt(20) + 1), ($scope.getRandomInt(20) + 1), ($scope.getRandomInt(20) + 1));
                offhandHitRoll = Math.max(($scope.getRandomInt(20) + 1), ($scope.getRandomInt(20) + 1), ($scope.getRandomInt(20) + 1));
            } else {
                hitRoll = $scope.getRandomInt(20) + 1;
                offhandHitRoll = $scope.getRandomInt(20) + 1;
            }
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex === undefined) {
            monsterSelectorSvc.selectMonster($scope.characterContext.monsters, function(index) {
                $scope.rollToHitMelee(index, '');
            });
            return;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex !== undefined) {
            monster = monsterIndex;
            if (flankUsed || $scope.hasEffect($scope.characterContext.meleeDmgEffects, "SMACK_WEAPON")) {
                hitRoll = Math.max(monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)]);
                offhandHitRoll = Math.max(monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)]);
            } else {
                hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
                offhandHitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
            }
        } else {
            monster = $scope.characterContext.monsters[0];
            if (flankUsed || $scope.hasEffect($scope.characterContext.meleeDmgEffects, "SMACK_WEAPON")) {
                hitRoll = Math.max(monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)]);
                offhandHitRoll = Math.max(monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)]);
            } else {
                hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
                offhandHitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
            }
        }
        
        if (!('meleeOffhandHit' in $scope.characterContext.stats)) {
            offhandHitRoll = 1;
        } else if (offhandHitRoll !== 20 && monster.monsterEffects && monster.monsterEffects.includes("MELEE_OFFHAND_ON_20")) {
            offhandHitRollOG = offhandHitRoll;
            offhandHitRoll = 1;
        } else if (monster.monsterEffects && monster.monsterEffects.includes("INCORPOREAL")) {
            if (!$scope.hasEffect($scope.characterContext.meleeDmgEffects, "IGNORE_INCORPOREAL") && 
                    !$scope.hasBuff("Ignore Incorporeal") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                offhandHitRollOG = offhandHitRoll;
                offhandHitRoll = 1;
            }
        } else if (monster.monsterEffects && monster.monsterEffects.includes("MISS_50")) {
            if (!$scope.hasBuff("Ignore Miss") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                offhandHitRollOG = offhandHitRoll;
                offhandHitRoll = 1;
            }
        } 
        
        if (hitRoll !== 20 && monster.monsterEffects && monster.monsterEffects.includes("MELEE_MAIN_ON_20")) {
            hitRollOG = hitRoll;
            hitRoll = 1;
        } else if (monster.monsterEffects && monster.monsterEffects.includes("INCORPOREAL")) {
            if (!$scope.hasEffect($scope.characterContext.meleeDmgEffects, "IGNORE_INCORPOREAL") && 
                    !$scope.hasBuff("Ignore Incorporeal") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                hitRollOG = hitRoll;
                hitRoll = 1;
            }
        } else if (monster.monsterEffects && monster.monsterEffects.includes("MISS_50")) {
            if (!$scope.hasBuff("Ignore Miss") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                hitRollOG = hitRoll;
                hitRoll = 1;
            }
        } 

        if (hitRoll === 1 && offhandHitRoll === 1) {
            var buff = $scope.hasBuff("WS Reroll");
            if (buff !== null && status !== 'ws reroll') {
                $scope.removeBuff(buff);
                $scope.rollToHitMelee(monsterIndex, 'ws reroll');
            }
            if ($scope.characterContext.rollerId !== null) {
                if (!('meleeOffhandHit' in $scope.characterContext.stats)) {
                    vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                        ' "rolls": [{ "type": "melee_main", "dieResult": 1, "modifiedResult": 1, "isSuccess": false, "damage": 0, "effect": "' + statusEffect + '" }]}').catch(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Melee Attack","result":response.data.errors[0]});       
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast(); 
                        return($q.reject(response));
                    }).then(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Melee Attack","result":"Melee attack roll sent"});
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast();   
                    });
                } else {
                    vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                        ' "rolls": [{ "type": "melee_main", "dieResult": 1, "modifiedResult": 1, "isSuccess": false, "damage": 0 }, { "type": "melee_off", "dieResult": 1, "modifiedResult": 1, "isSuccess": false, "damage": 0, "effect": "' + statusEffect + '" }]}').catch(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Melee Attack","result":response.data.errors[0]});       
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast(); 
                        return($q.reject(response));
                    }).then(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Melee Attack","result":"Melee attack roll sent"});
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast();   
                    });
                }
            } else {
                vtdHistory.add({"type":"ATTACK","sub":"MELEE","isMiss":true,"roll":1,"mRoll":hitRollOG,"oRoll":offhandHitRollOG});
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast();   
            }
        } else {
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
                    hitRollMod = hitRoll + $scope.characterContext.stats.rangeHitBenrow;
                if (offhandHitRoll > 1)
                    offhandHitRollMod = offhandHitRoll + $scope.characterContext.stats.rangeOffhandHitBenrow;
                if (!$scope.hasEffect($scope.characterContext.meleeDmgEffects, "NO_DAMAGE_MOD"))
                    mRollDmg = $scope.characterContext.stats.rangeDmgBenrow;
                if (!$scope.hasEffect($scope.characterContext.meleeOffhandDmgEffects, "NO_DAMAGE_MOD"))
                    oRollDmg = $scope.characterContext.stats.rangeDmgBenrow;
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
                    else if ($scope.characterContext.meleeWeaponExplodeEffect === "CRIT" && (monster.critical || critImmuneUsed) && hitRoll >= $scope.characterContext.meleeCritMin)
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
                    else if ($scope.characterContext.meleeWeaponSecondaryExplodeEffect === "CRIT" && (monster.critical || critImmuneUsed) && hitRoll >= $scope.characterContext.meleeCritMin)
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
                    else if ($scope.characterContext.meleeOffhandWeaponExplodeEffect === "CRIT" && (monster.critical || critImmuneUsed) && hitRoll >= $scope.characterContext.meleeCritMin)
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
            
            var isMainDr = !$scope.hasEffect($scope.characterContext.meleeDmgEffects, "NO_DAMAGE_MOD");
            var isOffDr = !$scope.hasEffect($scope.characterContext.meleeOffhandDmgEffects, "NO_DAMAGE_MOD");
            var mDmgTotal = 0;
            var mDmgTotalDR = 0;
            var oDmgTotalDR = 0;
            if (hitRoll > 1 && isMainDr)
                mDmgTotal = mDmg + mRollDmg + monster.bonusDmg;
            else
                mDmg = 0;
            
            var oDmgTotal = 0;
            if (offhandHitRoll > 1 && isOffDr)
                oDmgTotal = oDmg + oRollDmg + monster.bonusDmg;
            else
                oDmg = 0;
            
            var mDmgTotalDR = 0;
            var oDmgTotalDR = 0;
            if (monster.universalDr !==0) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.universalDr > 0)
                        mDmgTotalDR += monster.universalDr;
                    else
                        mDmgTotal += Math.abs(monster.universalDr);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (monster.universalDr > 0)
                        oDmgTotalDR += monster.universalDr;
                    else
                        oDmgTotal += Math.abs(monster.universalDr);
                }
            }
            
            if ($scope.isFurryThrow) {   
                if (monster.fire !== 0 && +$scope.characterContext.stats.bfire) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.fire > 0)
                            mDmgTotalDR += monster.fire;
                        else
                            mDmgTotal += Math.abs(monster.fire);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.fire > 0)
                            oDmgTotalDR += monster.fire;
                        else
                            oDmgTotal += Math.abs(monster.fire);
                    } 
                } if (monster.firePercent !== 1.0 && +$scope.characterContext.stats.bfire) {
                    var fireMod = (Math.round(monster.firePercent * +$scope.characterContext.stats.bfire) - +$scope.characterContext.stats.bfire);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (fireMod > 0)
                            mDmgTotal += fireMod;
                        else
                            mDmgTotalDR += Math.abs(fireMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (fireMod > 0)
                            oDmgTotal += fireMod;
                        else
                            oDmgTotalDR += Math.abs(fireMod);
                    } 
                } if (monster.cold !== 0 && +$scope.characterContext.stats.bcold) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.cold > 0)
                            mDmgTotalDR += monster.cold;
                        else
                            mDmgTotal += Math.abs(monster.cold);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.cold > 0)
                            oDmgTotalDR += monster.cold;
                        else
                            oDmgTotal += Math.abs(monster.cold);
                    } 
                } if (monster.coldPercent !== 1.0 && +$scope.characterContext.stats.bcold) {
                    var coldMod = (Math.round(monster.coldPercent * +$scope.characterContext.stats.bcold) - +$scope.characterContext.stats.bcold);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (coldMod > 0)
                            mDmgTotal += coldMod;
                        else
                            mDmgTotalDR += Math.abs(coldMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (coldMod > 0)
                            oDmgTotal += coldMod;
                        else
                            oDmgTotalDR += Math.abs(coldMod);
                    } 
                } if (monster.shock !== 0 && +$scope.characterContext.stats.bshock) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.shock > 0)
                            mDmgTotalDR += monster.shock;
                        else
                            mDmgTotal += Math.abs(monster.shock);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.shock > 0)
                            oDmgTotalDR += monster.shock;
                        else
                            oDmgTotal += Math.abs(monster.shock);
                    }  
                } if (monster.shockPercent !== 1.0 && +$scope.characterContext.stats.bshock) {
                    var shockMod = (Math.round(monster.shockPercent * +$scope.characterContext.stats.bshock) - +$scope.characterContext.stats.bshock);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (shockMod > 0)
                            mDmgTotal += shockMod;
                        else
                            mDmgTotalDR += Math.abs(shockMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (shockMod > 0)
                            oDmgTotal += shockMod;
                        else
                            oDmgTotalDR += Math.abs(shockMod);
                    }
                } if (monster.sonic !== 0 && +$scope.characterContext.stats.bsonic) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.sonic > 0)
                            mDmgTotalDR += monster.sonic;
                        else
                            mDmgTotal += Math.abs(monster.sonic);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.sonic > 0)
                            oDmgTotalDR += monster.sonic;
                        else
                            oDmgTotal += Math.abs(monster.sonic);
                    }  
                } if (monster.sonicPercent !== 1.0 && +$scope.characterContext.stats.bsonic) {
                    var sonicMod = (Math.round(monster.sonicPercent * +$scope.characterContext.stats.bsonic) - +$scope.characterContext.stats.bsonic);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (sonicMod > 0)
                            mDmgTotal += sonicMod;
                        else
                            mDmgTotalDR += Math.abs(sonicMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (sonicMod > 0)
                            oDmgTotal += sonicMod;
                        else
                            oDmgTotalDR += Math.abs(sonicMod);
                    }
                } if (monster.poison !== 0 && +$scope.characterContext.stats.bpoison) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.poison > 0)
                            mDmgTotalDR += monster.poison;
                        else
                            mDmgTotal += Math.abs(monster.poison);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.poison > 0)
                            oDmgTotalDR += monster.poison;
                        else
                            oDmgTotal += Math.abs(monster.poison);
                    }  
                } if (monster.poisonPercent !== 1.0 && +$scope.characterContext.stats.bpoison) {
                    var poisonkMod = (Math.round(monster.poisonPercent * +$scope.characterContext.stats.bpoison) - +$scope.characterContext.stats.bpoison);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (poisonkMod > 0)
                            mDmgTotal += poisonkMod;
                        else
                            mDmgTotalDR += Math.abs(poisonkMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (poisonkMod > 0)
                            oDmgTotal += poisonkMod;
                        else
                            oDmgTotalDR += Math.abs(poisonkMod);
                    }
                } if (monster.darkrift !== 0 && +$scope.characterContext.stats.bdarkrift) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.darkrift > 0)
                            mDmgTotalDR += monster.darkrift;
                        else
                            mDmgTotal += Math.abs(monster.darkrift);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.darkrift > 0)
                            oDmgTotalDR += monster.darkrift;
                        else
                            oDmgTotal += Math.abs(monster.darkrift);
                    }  
                } if (monster.darkriftPercent !== 1.0 && +$scope.characterContext.stats.bdarkrift) {
                    var darkriftMod = (Math.round(monster.darkriftPercent * +$scope.characterContext.stats.bdarkrift) - +$scope.characterContext.stats.bdarkrift);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (darkriftMod > 0)
                            mDmgTotal += darkriftMod;
                        else
                            mDmgTotalDR += Math.abs(darkriftMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (darkriftMod > 0)
                            oDmgTotal += darkriftMod;
                        else
                            oDmgTotalDR += Math.abs(darkriftMod);
                    }
                } if (monster.sacred !== 0 && +$scope.characterContext.stats.bsacred) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.sacred > 0)
                            mDmgTotalDR += monster.sacred;
                        else
                            mDmgTotal += Math.abs(monster.sacred);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.sacred > 0)
                            oDmgTotalDR += monster.sacred;
                        else
                            oDmgTotal += Math.abs(monster.sacred);
                    }   
                } if (monster.sacredPercent !== 1.0 && +$scope.characterContext.stats.bsacred) {
                    if (mDmgTotal > 0 && isMainDr) {
                        var sacredMod = (+$scope.characterContext.mainSacredBonus > 0) ? (Math.round(monster.sacredPercent * (+$scope.characterContext.stats.bsacred + +$scope.characterContext.mainSacredBonus + mDmg)) - (+$scope.characterContext.stats.bsacred + +$scope.characterContext.mainSacredBonus + mDmg)) :
                                (Math.round(monster.sacredPercent * +$scope.characterContext.stats.bsacred) - +$scope.characterContext.stats.bsacred);
                        if (sacredMod > 0)
                            mDmgTotal += sacredMod;
                        else
                            mDmgTotalDR += Math.abs(sacredMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                         var sacredMod = (+$scope.characterContext.offSacredBonus > 0) ? (Math.round(monster.sacredPercent * (+$scope.characterContext.stats.bsacred + +$scope.characterContext.offSacredBonus + oDmg)) - (+$scope.characterContext.stats.bsacred + +$scope.characterContext.offSacredBonus + oDmg)) :
                                (Math.round(monster.sacredPercent * +$scope.characterContext.stats.bsacred) - +$scope.characterContext.stats.bsacred);
                        if (sacredMod > 0)
                            oDmgTotal += sacredMod;
                        else
                            oDmgTotalDR += Math.abs(sacredMod);
                    }
                } if (monster.force !== 0 && +$scope.characterContext.stats.bforce) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.force > 0)
                            mDmgTotalDR += monster.force;
                        else
                            mDmgTotal += Math.abs(monster.force);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.force > 0)
                            oDmgTotalDR += monster.force;
                        else
                            oDmgTotal += Math.abs(monster.force);
                    } 
                } if (monster.forcePercent !== 1.0 && +$scope.characterContext.stats.bforce) {
                    var forceMod = (Math.round(monster.forcePercent * +$scope.characterContext.stats.bforce) - +$scope.characterContext.stats.bforce);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (forceMod > 0)
                            mDmgTotal += forceMod;
                        else
                            mDmgTotalDR += Math.abs(forceMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (forceMod > 0)
                            oDmgTotal += forceMod;
                        else
                            oDmgTotalDR += Math.abs(forceMod);
                    }
                } if (monster.acid !== 0 && +$scope.characterContext.stats.bacid) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.acid > 0)
                            mDmgTotalDR += monster.acid;
                        else
                            mDmgTotal += Math.abs(monster.acid);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.acid > 0)
                            oDmgTotalDR += monster.acid;
                        else
                            oDmgTotal += Math.abs(monster.acid);
                    }  
                } if (monster.acidPercent !== 1.0 && +$scope.characterContext.stats.bacid) {
                    var acidMod = (Math.round(monster.acidPercent * +$scope.characterContext.stats.bacid) - +$scope.characterContext.stats.bacid);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (acidMod > 0)
                            mDmgTotal += acidMod;
                        else
                            mDmgTotalDR += Math.abs(acidMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (acidMod > 0)
                            oDmgTotal += acidMod;
                        else
                            oDmgTotalDR += Math.abs(acidMod);
                    }
                }
                
                if (monster.rangeDr !==0) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.rangeDr > 0)
                            mDmgTotalDR += monster.rangeDr;
                        else
                            mDmgTotal += Math.abs(monster.rangeDr);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.rangeDr > 0)
                            oDmgTotalDR += monster.rangeDr;
                        else
                            oDmgTotal += Math.abs(monster.rangeDr);
                    }
                } if (monster.rangePercent !== 1.0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal = Math.round(mDmgTotal * monster.rangePercent);
                    if (oDmgTotal > 0 && isOffDr)
                        oDmgTotal = Math.round(oDmgTotal * monster.rangePercent);
                }  
            } else {
                if (monster.fire !== 0 && +$scope.characterContext.stats.mfire) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.fire > 0)
                            mDmgTotalDR += monster.fire;
                        else
                            mDmgTotal += Math.abs(monster.fire);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.fire > 0)
                            oDmgTotalDR += monster.fire;
                        else
                            oDmgTotal += Math.abs(monster.fire);
                    }  
                } if (monster.firePercent !== 1.0 && +$scope.characterContext.stats.mfire) {
                    var percentMod = (Math.round(monster.firePercent * +$scope.characterContext.stats.mfire) - +$scope.characterContext.stats.mfire);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) {
                            if (monster.monsterEffects.includes("FIRE_HEAL"))
                                mDmgTotal -= percentMod;
                            else
                                mDmgTotal += percentMod;
                        }
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (percentMod > 0) {
                            if (monster.monsterEffects.includes("FIRE_HEAL"))
                                oDmgTotal -= percentMod;
                            else
                                oDmgTotal += percentMod;
                        }
                        else
                            oDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.cold !== 0 && +$scope.characterContext.stats.mcold) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.cold > 0)
                            mDmgTotalDR += monster.cold;
                        else
                            mDmgTotal += Math.abs(monster.cold);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.cold > 0)
                            oDmgTotalDR += monster.cold;
                        else
                            oDmgTotal += Math.abs(monster.cold);
                    }  
                } if (monster.coldPercent !== 1.0 && +$scope.characterContext.stats.mcold) {
                    var percentMod = (Math.round(monster.coldPercent * +$scope.characterContext.stats.mcold) - +$scope.characterContext.stats.mcold);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (percentMod > 0)
                            oDmgTotal += percentMod;
                        else
                            oDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.shock !== 0 && +$scope.characterContext.stats.mshock) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.shock > 0)
                            mDmgTotalDR += monster.shock;
                        else
                            mDmgTotal += Math.abs(monster.shock);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.shock > 0)
                            oDmgTotalDR += monster.shock;
                        else
                            oDmgTotal += Math.abs(monster.shock);
                    }
                } if (monster.shockPercent !== 1.0 && +$scope.characterContext.stats.mshock) {
                    var percentMod = (Math.round(monster.shockPercent * +$scope.characterContext.stats.mshock) - +$scope.characterContext.stats.mshock);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (percentMod > 0)
                            oDmgTotal += percentMod;
                        else
                            oDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.sonic !== 0 && +$scope.characterContext.stats.msonic) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.sonic > 0)
                            mDmgTotalDR += monster.sonic;
                        else
                            mDmgTotal += Math.abs(monster.sonic);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.sonic > 0)
                            oDmgTotalDR += monster.sonic;
                        else
                            oDmgTotal += Math.abs(monster.sonic);
                    }
                } if (monster.sonicPercent !== 1.0 && +$scope.characterContext.stats.msonic) {
                    var percentMod = (Math.round(monster.sonicPercent * +$scope.characterContext.stats.msonic) - +$scope.characterContext.stats.msonic);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (percentMod > 0)
                            oDmgTotal += percentMod;
                        else
                            oDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.poison !== 0 && +$scope.characterContext.stats.mpoison) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.poison > 0)
                            mDmgTotalDR += monster.poison;
                        else
                            mDmgTotal += Math.abs(monster.poison);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.poison > 0)
                            oDmgTotalDR += monster.poison;
                        else
                            oDmgTotal += Math.abs(monster.poison);
                    }   
                } if (monster.poisonPercent !== 1.0 && +$scope.characterContext.stats.mpoison) {
                    var percentMod = (Math.round(monster.poisonPercent * +$scope.characterContext.stats.mpoison) - +$scope.characterContext.stats.mpoison);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (percentMod > 0)
                            oDmgTotal += percentMod;
                        else
                            oDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.darkrift !== 0 && +$scope.characterContext.stats.mdarkrift) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.darkrift > 0)
                            mDmgTotalDR += monster.darkrift;
                        else
                            mDmgTotal += Math.abs(monster.darkrift);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.darkrift > 0)
                            oDmgTotalDR += monster.darkrift;
                        else
                            oDmgTotal += Math.abs(monster.darkrift);
                    } 
                } if (monster.darkriftPercent !== 1.0 && +$scope.characterContext.stats.mdarkrift) {
                     var percentMod = (Math.round(monster.darkriftPercent * +$scope.characterContext.stats.mdarkrift) - +$scope.characterContext.stats.mdarkrift);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (percentMod > 0)
                            oDmgTotal += percentMod;
                        else
                            oDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.sacred !== 0 && +$scope.characterContext.stats.msacred) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.sacred > 0)
                            mDmgTotalDR += monster.sacred;
                        else
                            mDmgTotal += Math.abs(monster.sacred);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.sacred > 0)
                            oDmgTotalDR += monster.sacred;
                        else
                            oDmgTotal += Math.abs(monster.sacred);
                    }   
                } if (monster.sacredPercent !== 1.0 && +$scope.characterContext.stats.msacred) {
                    if (mDmgTotal > 0 && isMainDr) {
                        var sacredMod = (+$scope.characterContext.mainSacredBonus > 0) ? (Math.round(monster.sacredPercent * (+$scope.characterContext.stats.msacred + +$scope.characterContext.mainSacredBonus + mDmg)) - (+$scope.characterContext.stats.msacred + +$scope.characterContext.mainSacredBonus + mDmg)) :
                                (Math.round(monster.sacredPercent * +$scope.characterContext.stats.msacred) - +$scope.characterContext.stats.msacred);
            
                        if (sacredMod > 0)
                            mDmgTotal += sacredMod;
                        else
                            mDmgTotalDR += Math.abs(sacredMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                         var sacredMod = (+$scope.characterContext.offSacredBonus > 0) ? (Math.round(monster.sacredPercent * (+$scope.characterContext.stats.msacred + +$scope.characterContext.offSacredBonus + oDmg)) - (+$scope.characterContext.stats.msacred + +$scope.characterContext.offSacredBonus + oDmg)) :
                                (Math.round(monster.sacredPercent * +$scope.characterContext.stats.msacred) - +$scope.characterContext.stats.msacred);
                        if (sacredMod > 0)
                            oDmgTotal += sacredMod;
                        else
                            oDmgTotalDR += Math.abs(sacredMod);
                    }
                } if (monster.force !== 0 && +$scope.characterContext.stats.mforce) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.force > 0)
                            mDmgTotalDR += monster.force;
                        else
                            mDmgTotal += Math.abs(monster.force);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.force > 0)
                            oDmgTotalDR += monster.force;
                        else
                            oDmgTotal += Math.abs(monster.force);
                    }  
                } if (monster.forcePercent !== 1.0 && +$scope.characterContext.stats.mforce) {
                     var percentMod = (Math.round(monster.forcePercent * +$scope.characterContext.stats.mforce) - +$scope.characterContext.stats.mforce);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (percentMod > 0)
                            oDmgTotal += percentMod;
                        else
                            oDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.acid !== 0 && +$scope.characterContext.stats.macid) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.acid > 0)
                            mDmgTotalDR += monster.acid;
                        else
                            mDmgTotal += Math.abs(monster.acid);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.acid > 0)
                            oDmgTotalDR += monster.acid;
                        else
                            oDmgTotal += Math.abs(monster.acid);
                    } 
                } if (monster.acidPercent !== 1.0 && +$scope.characterContext.stats.macid) {
                    var percentMod = (Math.round(monster.acidPercent * +$scope.characterContext.stats.macid) - +$scope.characterContext.stats.macid);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (percentMod > 0)
                            oDmgTotal += percentMod;
                        else
                            oDmgTotalDR += Math.abs(percentMod);
                    }
                }
                
                if (monster.meleeDr !==0) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.meleeDr > 0)
                            mDmgTotalDR += monster.meleeDr;
                        else
                            mDmgTotal += Math.abs(monster.meleeDr);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.meleeDr > 0)
                            oDmgTotalDR += monster.meleeDr;
                        else
                            oDmgTotal += Math.abs(monster.meleeDr);
                    }
                } if (monster.meleePercent !== 1.0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal = Math.round(mDmgTotal * monster.meleePercent);
                    if (oDmgTotal > 0 && isOffDr)
                        oDmgTotal = Math.round(oDmgTotal * monster.meleePercent);
                }
            }
            
            if (mDmgTotal < 0)
                mDmgTotal = 0;
            if (oDmgTotal < 0)
                oDmgTotal = 0;
            
            if ((monster.critical || critImmuneUsed) && hitRoll >= $scope.characterContext.meleeCritMin && !$scope.hasEffect($scope.characterContext.meleeDmgEffects, "NO_DAMAGE_MOD") && !$scope.hasEffect(monster.monsterEffects, "MELEE_MAIN_ON_20")) {
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
                if (buff !== null) {
                    mCritDmg = mCritDmg + mDmgTotal;
                    $scope.removeBuff(buff);
                }
            } else if ($scope.hasBuff("Fury")) {
                var buff = $scope.hasBuff("Fury");
                if (buff !== null) {
                    if ((monster.critical || critImmuneUsed))
                        mCritDmg = mDmgTotal * 2;
                    
                    $scope.removeBuff(buff);
                }
            }
            
            if ((monster.critical || critImmuneUsed) && offhandHitRoll >= $scope.characterContext.meleeCritMin && !$scope.hasEffect($scope.characterContext.meleeDmgEffects, "NO_DAMAGE_MOD") && !$scope.hasEffect(monster.monsterEffects, "MELEE_OFFHAND_ON_20")) {
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
            
            // Apply DR now
            if (mDmgTotalDR > 0) {
                mCritDmg -= mDmgTotalDR;
                mDmgTotal -= mDmgTotalDR;
            } if (oDmgTotalDR > 0) {
                oCritDmg -= oDmgTotalDR;
                oDmgTotal -= oDmgTotalDR;
            }
            if (mDmgTotal < 0)
                mDmgTotal = 0;
            if (oDmgTotal < 0)
                oDmgTotal = 0;
            
            var totalCrit = 0;
            if (mCritDmg > 0 && oCritDmg > 0)
                totalCrit = mCritDmg + oCritDmg;
            else if (mCritDmg > 0) {
                totalCrit = mCritDmg + oDmgTotal;
            } else if (oCritDmg > 0) {
                totalCrit = oCritDmg + mDmgTotal;
            }
            
            var buff = $scope.hasBuff("Righteous Wrath");
            if (buff !== null) 
                $scope.removeBuff(buff);
            
            if ($scope.isFurryThrow) {
                if (monster.rac > hitRollMod) {
                    mDmgTotal = 0;
                    mCritDmg = 0;
                }
                if (monster.rac > offhandHitRollMod) {
                    oDmgTotal = 0;
                    oCritDmg = 0;
                }
                                             
                if ($scope.characterContext.rollerId !== null) {
                    vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                        ' "rolls": [{ "type": "range_main", "dieResult": ' + hitRoll + ', "modifiedResult": ' + hitRollMod + ', "isSuccess": ' + (mDmgTotal > 0) + ', "damage": ' + ((mCritDmg > 0) ? mCritDmg : mDmgTotal) + ' }, ' + 
                        '{ "type": "range_off", "dieResult": ' + offhandHitRoll + ', "modifiedResult": ' + offhandHitRollMod + ', "isSuccess": ' + (oDmgTotal > 0) + ', "damage": ' + ((oCritDmg > 0) ? oCritDmg : oDmgTotal) + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Flurry Throw Attack","result":response.data.errors[0]});       
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast(); 
                        return($q.reject(response));
                    }).then(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Flurry Throw Attack","result":"Flurry throw attack roll sent"});
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast();   
                    });
                } else {
                    if (mDmgTotal === 0 && oDmgTotal === 0) {
                        vtdHistory.add({"type":"ATTACK","sub":"MELEE","isMiss":true,"roll":hitRollMod,"mRoll":hitRollMod,"oRoll":offhandHitRollMod});
                    } else {
                        vtdHistory.add({"type":"ATTACK","sub":"MELEE","isMiss":false,"isCrit":mCritDmg > 0 || oCritDmg > 0,"mRoll":hitRoll,"oRoll":offhandHitRoll,
                            "mRollTotal":hitRollMod,"oRollTotal":offhandHitRollMod,"mWheel":mDmg,"oWheel":oDmg,"mDmg":mDmgTotal,"oDmg":oDmgTotal,"totalDmg":mDmgTotal+oDmgTotal,
                            "mCrit":mCritDmg,"oCrit":oCritDmg,"critTotal":totalCrit,"mWeaponExp":mDmgExp,"sWeaponExp":sDmgExp,"oWeaponExp":oDmgExp,"fire":$scope.characterContext.stats.bfire,
                            "cold":$scope.characterContext.stats.bcold,"shock":$scope.characterContext.stats.bshock,"sonic":$scope.characterContext.stats.bsonic,
                            "eldritch":$scope.characterContext.stats.beldritch,"poison":$scope.characterContext.stats.bpoison,"darkrift":$scope.characterContext.stats.bdarkrift,
                            "sacred":$scope.characterContext.stats.bsacred,"force":$scope.characterContext.stats.bforce,"acid":$scope.characterContext.stats.bacid});
                    }
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast();   
                }
            } else {
                if (monster.mac > hitRollMod) {
                    var buff = $scope.hasBuff("WS Reroll");
                    if (buff !== null && status !== 'ws reroll') {
                        $scope.removeBuff(buff);
                        $scope.rollToHitMelee(monsterIndex, 'ws reroll');
                    }
        
                    mDmgTotal = 0;
                    mCritDmg = 0;
                }
                if (monster.mac > offhandHitRollMod) {
                    oDmgTotal = 0;
                    oCritDmg = 0;
                }
            
                if ($scope.characterContext.rollerId !== null) {
                    if ('meleeOffhandHit' in $scope.characterContext.stats) {
                        vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                            ' "rolls": [{ "type": "melee_main", "dieResult": ' + hitRoll + ', "modifiedResult": ' + hitRollMod + ', "isSuccess": ' + (monster.mac <= hitRollMod) + ', "damage": ' + ((mCritDmg > 0) ? mCritDmg : mDmgTotal) + ' }, ' + 
                            '{ "type": "melee_off", "dieResult": ' + offhandHitRoll + ', "modifiedResult": ' + offhandHitRollMod + ', "isSuccess": ' + (monster.mac <= offhandHitRollMod) + ', "damage": ' + ((oCritDmg > 0) ? oCritDmg : oDmgTotal) + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                            vtdHistory.add({"type":"ROLLER","sub":"Melee Attack","result":response.data.errors[0]});       
                            $scope.history = vtdHistory.get();
                            $scope.lastEvent = vtdHistory.getLast(); 
                            return($q.reject(response));
                        }).then(function(response) {
                            vtdHistory.add({"type":"ROLLER","sub":"Melee Attack","result":"Melee attack roll sent"});
                            $scope.history = vtdHistory.get();
                            $scope.lastEvent = vtdHistory.getLast();   
                        });
                    } else {
                        vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                            ' "rolls": [{ "type": "melee_main", "dieResult": ' + hitRoll + ', "modifiedResult": ' + hitRollMod + ', "isSuccess": ' + (monster.mac <= hitRollMod) + ', "damage": ' + ((mCritDmg > 0) ? mCritDmg : mDmgTotal) + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                            vtdHistory.add({"type":"ROLLER","sub":"Melee Attack","result":response.data.errors[0]});       
                            $scope.history = vtdHistory.get();
                            $scope.lastEvent = vtdHistory.getLast(); 
                            return($q.reject(response));
                        }).then(function(response) {
                            vtdHistory.add({"type":"ROLLER","sub":"Melee Attack","result":"Melee attack roll sent"});
                            $scope.history = vtdHistory.get();
                            $scope.lastEvent = vtdHistory.getLast();   
                        });
                    }
                } else {
                    if (mDmgTotal === 0 && oDmgTotal === 0) {
                        vtdHistory.add({"type":"ATTACK","sub":"MELEE","isMiss":true,"roll":hitRollMod,"mRoll":hitRollMod,"oRoll":offhandHitRollMod});
                    } else {
                        vtdHistory.add({"type":"ATTACK","sub":"MELEE","isMiss":false,"isCrit":mCritDmg > 0 || oCritDmg > 0,"mRoll":hitRoll,"oRoll":offhandHitRoll,
                            "mRollTotal":hitRollMod,"oRollTotal":offhandHitRollMod,"mWheel":mDmg,"oWheel":oDmg,"mDmg":mDmgTotal,"oDmg":oDmgTotal,"totalDmg":mDmgTotal+oDmgTotal,
                            "mCrit":mCritDmg,"oCrit":oCritDmg,"critTotal":totalCrit,"mWeaponExp":mDmgExp,"sWeaponExp":sDmgExp,"oWeaponExp":oDmgExp,"fire":$scope.characterContext.stats.mfire,
                            "cold":$scope.characterContext.stats.mcold,"shock":$scope.characterContext.stats.mshock,"sonic":$scope.characterContext.stats.msonic,
                            "eldritch":$scope.characterContext.stats.meldritch,"poison":$scope.characterContext.stats.mpoison,"darkrift":$scope.characterContext.stats.mdarkrift,
                            "sacred":$scope.characterContext.stats.msacred,"force":$scope.characterContext.stats.mforce,"acid":$scope.characterContext.stats.macid});
                    }
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast();  
                }
            }
        }
    };
    
    $scope.rollToHitRange =  function(monsterIndex) {        
        var hitRoll = 1;
        var offhandHitRoll = 1;
        var hitRollOG = 1;
        var offhandHitRollOG = 1;
        var monster = null;
        var isOffhandAttack = false;
        var isCompanion = ($scope.characterContext.meleeAnimalCompanionDmgRange && $scope.characterContext.meleeAnimalCompanionDmgRange.length > 0);
        var flankUsed = false;
        var statusEffect = '';
        
        $scope.resultIndex = 0;
        
        var buff = $scope.hasBuff("Flank");
        if (buff !== null) {
            $scope.removeBuff(buff);
            var flankUsed = true;
            statusEffect = 'flank buff';
        }
        
        var critImmuneUsed = false;
        var buff2 = $scope.hasBuff("Ignore Sneak & Crit Immune");
        if (buff2 !== null) {
            $scope.removeBuff(buff2);
            critImmuneUsed = true;
        }
        
        if ($scope.characterContext.monsters === null || $scope.characterContext.monsters.length === 0) {
            if (flankUsed) {
                hitRoll = Math.max($scope.getRandomInt(20) + 1, $scope.getRandomInt(20) + 1, $scope.getRandomInt(20) + 1);
                offhandHitRoll = Math.max($scope.getRandomInt(20) + 1, $scope.getRandomInt(20) + 1, $scope.getRandomInt(20) + 1);
            } else {
                hitRoll = $scope.getRandomInt(20) + 1;
                offhandHitRoll = $scope.getRandomInt(20) + 1;
            }
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex === undefined) {
            monsterSelectorSvc.selectMonster($scope.characterContext.monsters, function(index) {
                $scope.rollToHitRange(index);
            });
            return;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex !== undefined) {
            monster = monsterIndex;
            
            if (flankUsed) {
                hitRoll = Math.max(monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)]);
                offhandHitRoll = Math.max(monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)]);
            } else {
                hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
                offhandHitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
            }
        } else {
            monster = $scope.characterContext.monsters[0];
            
            if (flankUsed) {
                hitRoll = Math.max(monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)]);
                offhandHitRoll = Math.max(monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)]);
            } else {
                hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
                offhandHitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)]; 
            }
        }
        
        if (!('rangeOffhandHit' in $scope.characterContext.stats) && !isCompanion) {
            offhandHitRoll = 1;
        } else if (offhandHitRoll !== 20 && monster.monsterEffects && monster.monsterEffects.includes("RANGE_OFFHAND_ON_20")) {
            offhandHitRollOG = offhandHitRoll;
            offhandHitRoll = 1;
        } else if (monster.monsterEffects && monster.monsterEffects.includes("INCORPOREAL")) {
            if (!$scope.hasEffect($scope.characterContext.rangeDmgEffects, "IGNORE_INCORPOREAL") && 
                    !$scope.hasBuff("Ignore Incorporeal") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                offhandHitRollOG = offhandHitRoll;
                offhandHitRoll = 1;
            }
        } else if (monster.monsterEffects && monster.monsterEffects.includes("MISS_50")) {
            if (!$scope.hasBuff("Ignore Miss") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                offhandHitRollOG = offhandHitRoll;
                offhandHitRoll = 1;
            }
        } else
            isOffhandAttack = true;
        
        if (hitRoll !== 20 && monster.monsterEffects && monster.monsterEffects.includes("RANGE_MAIN_ON_20")) {
            hitRollOG = hitRoll;
            hitRoll = 1;
        } else if (monster.monsterEffects && monster.monsterEffects.includes("INCORPOREAL")) {
            if (!$scope.hasEffect($scope.characterContext.rangeDmgEffects, "IGNORE_INCORPOREAL") && 
                    !$scope.hasBuff("Ignore Incorporeal") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                hitRollOG = hitRoll;
                hitRoll = 1;
            }
        } else if (monster.monsterEffects && monster.monsterEffects.includes("MISS_50")) {
            if (!$scope.hasBuff("Ignore Miss") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                hitRollOG = hitRoll;
                hitRoll = 1;
            }
        }     
        
        if (hitRoll === 1 && offhandHitRoll === 1) {
            if ($scope.characterContext.rollerId !== null) {
                if (!('rangeOffhandHit' in $scope.characterContext.stats)) {
                    vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                        ' "rolls": [{ "type": "range_main", "dieResult": 1, "modifiedResult": 1, "isSuccess": false, "damage": 0' + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Range Attack","result":response.data.errors[0]});       
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast(); 
                        return($q.reject(response));
                    }).then(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Range Attack","result":"Range attack roll sent"});
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast();   
                    });
                } else {
                    vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                        ' "rolls": [{ "type": "range_main", "dieResult": 1, "modifiedResult": 1, "isSuccess": false, "damage": 0 }, { "type": "range_off", "dieResult": 1, "modifiedResult": 1, "isSuccess": false, "damage": 0' + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Range Attack","result":response.data.errors[0]});       
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast(); 
                        return($q.reject(response));
                    }).then(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Range Attack","result":"Range attack roll sent"});
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast();   
                    });
                }
            } else {
                vtdHistory.add({"type":"ATTACK","sub":"RANGE","isMiss":true,"roll":1,"mRoll":hitRollOG,"oRoll":offhandHitRollOG,"isOffhandAttack":isOffhandAttack});
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast(); 
            }
        } else {
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
                    else if ($scope.characterContext.rangeWeaponExplodeEffect === "CRIT" && (monster.critical || critImmuneUsed) && hitRoll >= $scope.characterContext.meleeCritMin)
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
                    else if ($scope.characterContext.rangeOffhandWeaponExplodeEffect === "CRIT" && (monster.critical || critImmuneUsed) && hitRoll >= $scope.characterContext.meleeCritMin)
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
            
            var isMainDr = !$scope.hasEffect($scope.characterContext.rangeDmgEffects, "NO_DAMAGE_MOD");
            var isOffDr = !$scope.hasEffect($scope.characterContext.rangeOffhandDmgEffects, "NO_DAMAGE_MOD");
            var mDmgTotal = 0;
            if (hitRoll > 1)
                mDmgTotal = mDmg + mRollDmg + monster.bonusDmg;
            else
                mDmg = 0;
            
            var oDmgTotal = 0;
            if (offhandHitRoll > 1)
                oDmgTotal = oDmg + oRollDmg + monster.bonusDmg;
            else
                oDmg = 0;
            
            var mDmgTotalDR = 0;
            var oDmgTotalDR = 0;
            if (monster.universalDr !==0) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.universalDr > 0)
                        mDmgTotalDR += monster.universalDr;
                    else
                        mDmgTotal += Math.abs(monster.universalDr);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (monster.universalDr > 0)
                        oDmgTotalDR += monster.universalDr;
                    else
                        oDmgTotal += Math.abs(monster.universalDr);
                }
            }
            if (monster.rangeDr !==0) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.rangeDr > 0)
                        mDmgTotalDR += monster.rangeDr;
                    else
                        mDmgTotal += Math.abs(monster.rangeDr);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (monster.rangeDr > 0)
                        oDmgTotalDR += monster.rangeDr;
                    else
                        oDmgTotal += Math.abs(monster.rangeDr);
                }
            } if (monster.rangePercent !== 1.0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal = Math.round(mDmgTotal * monster.rangePercent);
                if (oDmgTotal > 0 && isOffDr)
                    oDmgTotal = Math.round(oDmgTotal * monster.rangePercent);
            }
            
            if (monster.fire !== 0 && +$scope.characterContext.stats.rfire) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.fire > 0)
                        mDmgTotalDR += monster.fire;
                    else
                        mDmgTotal += Math.abs(monster.fire);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (monster.fire > 0)
                        oDmgTotalDR += monster.fire;
                    else
                        oDmgTotal += Math.abs(monster.fire);
                }  
            } if (monster.firePercent !== 1.0 && +$scope.characterContext.stats.rfire) {
                var percentMod = (Math.round(monster.firePercent * +$scope.characterContext.stats.rfire) - +$scope.characterContext.stats.rfire);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) {
                        if (monster.monsterEffects.includes("FIRE_HEAL"))
                            mDmgTotal -= percentMod;
                        else
                            mDmgTotal += percentMod;
                    }
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (percentMod > 0) {
                        if (monster.monsterEffects.includes("FIRE_HEAL"))
                            oDmgTotal -= percentMod;
                        else
                            oDmgTotal += percentMod;
                    }
                    else
                        oDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.cold !== 0 && +$scope.characterContext.stats.rcold) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.cold > 0)
                        mDmgTotalDR += monster.cold;
                    else
                        mDmgTotal += Math.abs(monster.cold);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (monster.cold > 0)
                        oDmgTotalDR += monster.cold;
                    else
                        oDmgTotal += Math.abs(monster.cold);
                }  
            } if (monster.coldPercent !== 1.0 && +$scope.characterContext.stats.rcold) {
                var percentMod = (Math.round(monster.coldPercent * +$scope.characterContext.stats.mcold) - +$scope.characterContext.stats.mcold);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (percentMod > 0)
                        oDmgTotal += percentMod;
                    else
                        oDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.shock !== 0 && +$scope.characterContext.stats.rshock) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.shock > 0)
                        mDmgTotalDR += monster.shock;
                    else
                        mDmgTotal += Math.abs(monster.shock);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (monster.shock > 0)
                        oDmgTotalDR += monster.shock;
                    else
                        oDmgTotal += Math.abs(monster.shock);
                }
            } if (monster.shockPercent !== 1.0 && +$scope.characterContext.stats.rshock) {
                var percentMod = (Math.round(monster.shockPercent * +$scope.characterContext.stats.rshock) - +$scope.characterContext.stats.rshock);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (percentMod > 0)
                        oDmgTotal += percentMod;
                    else
                        oDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.sonic !== 0 && +$scope.characterContext.stats.rsonic) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.sonic > 0)
                        mDmgTotalDR += monster.sonic;
                    else
                        mDmgTotal += Math.abs(monster.sonic);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (monster.sonic > 0)
                        oDmgTotalDR += monster.sonic;
                    else
                        oDmgTotal += Math.abs(monster.sonic);
                }
            } if (monster.sonicPercent !== 1.0 && +$scope.characterContext.stats.rsonic) {
                var percentMod = (Math.round(monster.sonicPercent * +$scope.characterContext.stats.rsonic) - +$scope.characterContext.stats.rsonic);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (percentMod > 0)
                        oDmgTotal += percentMod;
                    else
                        oDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.poison !== 0 && +$scope.characterContext.stats.rpoison) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.poison > 0)
                        mDmgTotalDR += monster.poison;
                    else
                        mDmgTotal += Math.abs(monster.poison);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (monster.poison > 0)
                        oDmgTotalDR += monster.poison;
                    else
                        oDmgTotal += Math.abs(monster.poison);
                }   
            } if (monster.poisonPercent !== 1.0 && +$scope.characterContext.stats.rpoison) {
                var percentMod = (Math.round(monster.poisonPercent * +$scope.characterContext.stats.rpoison) - +$scope.characterContext.stats.rpoison);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (percentMod > 0)
                        oDmgTotal += percentMod;
                    else
                        oDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.darkrift !== 0 && +$scope.characterContext.stats.rdarkrift) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.darkrift > 0)
                        mDmgTotalDR += monster.darkrift;
                    else
                        mDmgTotal += Math.abs(monster.darkrift);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (monster.darkrift > 0)
                        oDmgTotalDR += monster.darkrift;
                    else
                        oDmgTotal += Math.abs(monster.darkrift);
                } 
            } if (monster.darkriftPercent !== 1.0 && +$scope.characterContext.stats.rdarkrift) {
                 var percentMod = (Math.round(monster.darkriftPercent * +$scope.characterContext.stats.rdarkrift) - +$scope.characterContext.stats.rdarkrift);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (percentMod > 0)
                        oDmgTotal += percentMod;
                    else
                        oDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.sacred !== 0 && +$scope.characterContext.stats.rsacred) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.sacred > 0)
                        mDmgTotalDR += monster.sacred;
                    else
                        mDmgTotal += Math.abs(monster.sacred);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (monster.sacred > 0)
                        oDmgTotalDR += monster.sacred;
                    else
                        oDmgTotal += Math.abs(monster.sacred);
                }   
            } if (monster.sacredPercent !== 1.0 && +$scope.characterContext.stats.rsacred) {
                var percentMod = (Math.round(monster.sacredPercent * +$scope.characterContext.stats.rsacred) - +$scope.characterContext.stats.rsacred);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (percentMod > 0)
                        oDmgTotal += percentMod;
                    else
                        oDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.force !== 0 && +$scope.characterContext.stats.rforce) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.force > 0)
                        mDmgTotalDR += monster.force;
                    else
                        mDmgTotal += Math.abs(monster.force);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (monster.force > 0)
                        oDmgTotalDR += monster.force;
                    else
                        oDmgTotal += Math.abs(monster.force);
                }  
            } if (monster.forcePercent !== 1.0 && +$scope.characterContext.stats.rforce) {
                 var percentMod = (Math.round(monster.forcePercent * +$scope.characterContext.stats.rforce) - +$scope.characterContext.stats.rforce);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (percentMod > 0)
                        oDmgTotal += percentMod;
                    else
                        oDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.acid !== 0 && +$scope.characterContext.stats.racid) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.acid > 0)
                        mDmgTotalDR += monster.acid;
                    else
                        mDmgTotal += Math.abs(monster.acid);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (monster.acid > 0)
                        oDmgTotalDR += monster.acid;
                    else
                        oDmgTotal += Math.abs(monster.acid);
                } 
            } if (monster.acidPercent !== 1.0 && +$scope.characterContext.stats.racid) {
                var percentMod = (Math.round(monster.acidPercent * +$scope.characterContext.stats.racid) - +$scope.characterContext.stats.racid);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (percentMod > 0)
                        oDmgTotal += percentMod;
                    else
                        oDmgTotalDR += Math.abs(percentMod);
                }
            }
            
            if (mDmgTotal < 0)
                mDmgTotal = 0;
            if (oDmgTotal < 0)
                oDmgTotal = 0;
            
            if ((monster.critical || critImmuneUsed) && hitRoll >= $scope.characterContext.rangeCritMin && !$scope.hasEffect($scope.characterContext.rangeDmgEffects, "NO_DAMAGE_MOD") && !$scope.hasEffect(monster.monsterEffects, "RANGE_MAIN_ON_20")) {
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
            
            if ((monster.critical || critImmuneUsed) && offhandHitRoll >= $scope.characterContext.rangeCritMin && !$scope.hasEffect($scope.characterContext.rangeOffhandDmgEffects, "NO_DAMAGE_MOD") && !$scope.hasEffect(monster.monsterEffects, "RANGE_OFFHAND_ON_20")) {
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
            
            // Apply DR now
            if (mDmgTotalDR > 0) {
                mCritDmg -= mDmgTotalDR;
                mDmgTotal -= mDmgTotalDR;
            } if (oDmgTotalDR > 0) {
                oCritDmg -= oDmgTotalDR;
                oDmgTotal -= oDmgTotalDR;
            }
            if (mDmgTotal < 0)
                mDmgTotal = 0;
            if (oDmgTotal < 0)
                oDmgTotal = 0;
            
            var totalCrit = 0;
            if (mCritDmg > 0 && oCritDmg > 0)
                totalCrit = mCritDmg + oCritDmg;
            else if (mCritDmg > 0) {
                totalCrit = mCritDmg + oDmgTotal;
            } else if (oCritDmg > 0) {
                totalCrit = oCritDmg + mDmgTotal;
            }
            
            var buff = $scope.hasBuff("Righteous Wrath");
            if (buff !== null) 
                $scope.removeBuff(buff);
            
            if (monster.rac > hitRollMod) {
                mDmgTotal = 0;
                mCritDmg = 0;
            }
            if (monster.rac > offhandHitRollMod) {
                oDmgTotal = 0;
                oCritDmg = 0;
            }

            if ($scope.characterContext.rollerId !== null) {
                if ('rangeOffhandHit' in $scope.characterContext.stats) {
                    vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                        ' "rolls": [{ "type": "range_main", "dieResult": ' + hitRoll + ', "modifiedResult": ' + hitRollMod + ', "isSuccess": ' + (monster.rac <= hitRollMod) + ', "damage": ' + ((mCritDmg > 0) ? mCritDmg : mDmgTotal) + ' }, ' + 
                        '{ "type": "range_off", "dieResult": ' + offhandHitRoll + ', "modifiedResult": ' + offhandHitRollMod + ', "isSuccess": ' + (monster.rac <= offhandHitRollMod) + ', "damage": ' + ((oCritDmg > 0) ? oCritDmg : oDmgTotal) + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Range Attack","result":response.data.errors[0]});       
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast(); 
                        return($q.reject(response));
                    }).then(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Range Attack","result":"Range attack roll sent"});
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast();   
                    });
                } else {
                    vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                        ' "rolls": [{ "type": "range_main", "dieResult": ' + hitRoll + ', "modifiedResult": ' + hitRollMod + ', "isSuccess": ' + (monster.rac <= hitRollMod) + ', "damage": ' + ((mCritDmg > 0) ? mCritDmg : mDmgTotal) + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Range Attack","result":response.data.errors[0]});       
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast(); 
                        return($q.reject(response));
                    }).then(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Range Attack","result":"Range attack roll sent"});
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast();   
                    });
                }
            } else {
                if (mDmgTotal === 0 && oDmgTotal === 0) {
                    vtdHistory.add({"type":"ATTACK","sub":"RANGE","isMiss":true,"roll":hitRollMod,"mRoll":hitRollMod,"oRoll":offhandHitRollMod});
                } else {
                    vtdHistory.add({"type":"ATTACK","sub":"RANGE","isMiss":false,"isCrit":mCritDmg > 0 || oCritDmg > 0,"mRoll":hitRoll,"oRoll":offhandHitRoll,
                        "mRollTotal":hitRollMod,"oRollTotal":offhandHitRollMod,"mWheel":mDmg,"oWheel":oDmg,"mDmg":mDmgTotal,"oDmg":oDmgTotal,"totalDmg":mDmgTotal+oDmgTotal,
                        "mCrit":mCritDmg,"oCrit":oCritDmg,"critTotal":totalCrit,"mWeaponExp":mDmgExp,"oWeaponExp":oDmgExp,"fire":$scope.characterContext.stats.rfire,
                        "cold":$scope.characterContext.stats.rcold,"shock":$scope.characterContext.stats.rshock,"sonic":$scope.characterContext.stats.rsonic,
                        "eldritch":$scope.characterContext.stats.reldritch,"poison":$scope.characterContext.stats.rpoison,"darkrift":$scope.characterContext.stats.rdarkrift,
                        "sacred":$scope.characterContext.stats.rsacred,"force":$scope.characterContext.stats.rforce,"acid":$scope.characterContext.stats.racid,"isOffhandAttack":isOffhandAttack});
                }
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast(); 
            }
        }
    };
    
    $scope.rollToHitPoly =  function(monsterIndex) {        
        var hitRoll = 1;
        var hitRollOG = 1;
        var monster = null;
        
        if (!($scope.characterContext.meleePolyDmgRange && $scope.characterContext.meleePolyDmgRange.length > 0)) {
            warnDialogSvc.showMessage("Polymorph Seelection Required", "You must select your ploymorph form before attacking.");
            return;
        }
        
        var flankUsed = false;
        var statusEffect = '';
        
        $scope.resultIndex = 0;
        
        var buff = $scope.hasBuff("Flank");
        if (buff !== null) {
            $scope.removeBuff(buff);
            var flankUsed = true;
            statusEffect = 'flank buff';
        }
        
        var critImmuneUsed = false;
        var buff2 = $scope.hasBuff("Ignore Sneak & Crit Immune");
        if (buff2 !== null) {
            $scope.removeBuff(buff2);
            critImmuneUsed = true;
        }
        
        if ($scope.characterContext.monsters === null || $scope.characterContext.monsters.length === 0) {
            if (flankUsed) {
                hitRoll = Math.max($scope.getRandomInt(20) + 1, $scope.getRandomInt(20) + 1, $scope.getRandomInt(20) + 1);
            } else {
                hitRoll = $scope.getRandomInt(20) + 1;
            }
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex === undefined) {
            monsterSelectorSvc.selectMonster($scope.characterContext.monsters, function(index) {
                $scope.rollToHitPoly(index);
            });
            return;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex !== undefined) {
            monster = monsterIndex;
            if (flankUsed) {
                hitRoll = Math.max(monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)]);
            } else {
                hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
            }
        } else {
            monster = $scope.characterContext.monsters[0];
            if (flankUsed) {
                hitRoll = Math.max(monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)]);
            } else {
                hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
            }
        }
        
        if (hitRoll !== 20 && monster.monsterEffects && monster.monsterEffects.includes("MELEE_MAIN_ON_20")) {
            hitRollOG = hitRoll;
            hitRoll = 1;
        } else if (monster.monsterEffects && monster.monsterEffects.includes("INCORPOREAL")) {
            if (!$scope.hasEffect($scope.characterContext.meleeDmgEffects, "IGNORE_INCORPOREAL") && 
                    !$scope.hasBuff("Ignore Incorporeal") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                hitRollOG = hitRoll;
                hitRoll = 1;
            }
        } else if (monster.monsterEffects && monster.monsterEffects.includes("MISS_50")) {
            if (!$scope.hasBuff("Ignore Miss") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                hitRollOG = hitRoll;
                hitRoll = 1;
            }
        } 
       
        if (hitRoll === 1) {
            if ($scope.characterContext.rollerId !== null) {
                vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                    ' "rolls": [{ "type": "melee_main", "dieResult": 1, "modifiedResult": 1, "isSuccess": false, "damage": 0' + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Polymorph Attack","result":response.data.errors[0]});       
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast(); 
                    return($q.reject(response));
                }).then(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Polymorph Attack","result":"Polymorph attack roll sent"});
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast();   
                });
            } else {
                vtdHistory.add({"type":"ATTACK","sub":"POLY","isMiss":true,"roll":1,"mRoll":hitRollOG});
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast(); 
            }
        } else {
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
            
            var isMainDr = !$scope.hasEffect($scope.characterContext.meleePolyDmgEffects, "NO_DAMAGE_MOD");
            var mDmgTotal = 0;
            if (hitRoll > 1)
                mDmgTotal = mDmg + rollDmg;
            else
                mDmg = 0;
           
            var mDmgTotalDR = 0;
            if (monster.universalDr !==0) {
                if (mDmgTotal > 0 && isMainDr && !($scope.characterContext.poly.name === "Iktomi’s Shaper Necklace - Ice" && monster.monsterEffects.includes("COLD_PIERCE"))) {
                    if (monster.universalDr > 0)
                        mDmgTotalDR += monster.universalDr;
                    else
                        mDmgTotal += Math.abs(monster.universalDr);
                }
            }
            if (monster.meleeDr !==0) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.meleeDr > 0)
                        mDmgTotalDR += monster.meleeDr;
                    else
                        mDmgTotal += Math.abs(monster.meleeDr);
                }
            } if (monster.meleePercent !== 1.0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal = Math.round(mDmgTotal * monster.meleePercent);
            }
            
            var allFire = false;
            var allCold = false;
            var allShock = false;
            var allSonic = false;
            if ($scope.characterContext.poly.name === "Iktomi’s Shaper Necklace - Fire" || $scope.characterContext.poly.name === "Shaman’s Greater Necklace - Fire") {
                if (monster.fire !== 0) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.fire > 0) {
                            if (monster.monsterEffects.includes("FIRE_HEAL"))
                                mDmgTotal -= monster.fire;
                            else
                                mDmgTotalDR += monster.fire;
                        }
                        else
                            mDmgTotal += Math.abs(monster.fire);
                    }  
                } if (monster.firePercent !== 1.0) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.monsterEffects.includes("FIRE_HEAL"))
                            mDmgTotal = -1*Math.round(monster.firePercent * mDmgTotal);
                        else
                            mDmgTotal = Math.round(monster.firePercent * mDmgTotal);
                    }
                }
                allFire = true;
            } else if ($scope.characterContext.poly.name === "Iktomi’s Shaper Necklace - Ice" || $scope.characterContext.poly.name === "Shaman’s Greater Necklace - Ice") {
                if (monster.cold !== 0) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.cold > 0) 
                            mDmgTotalDR += monster.cold;                     
                        else
                            mDmgTotal += Math.abs(monster.cold);
                    }  
                } if (monster.coldPercent !== 1.0) {
                    if (mDmgTotal > 0 && isMainDr) 
                        mDmgTotal = Math.round(monster.coldPercent * mDmgTotal);
                }
                allCold = true;
            } else if ($scope.characterContext.poly.name === "Iktomi’s Shaper Necklace - Air" || $scope.characterContext.poly.name === "Shaman’s Greater Necklace - Air") {
                 if (monster.shock !== 0) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.shock > 0) 
                            mDmgTotalDR += monster.shock;                     
                        else
                            mDmgTotal += Math.abs(monster.shock);
                    }  
                } if (monster.shockPercent !== 1.0) {
                    if (mDmgTotal > 0 && isMainDr) 
                        mDmgTotal = Math.round(monster.shockPercent * mDmgTotal);
                }
                allShock = true;
            } else if ($scope.characterContext.poly.name === "Iktomi’s Shaper Necklace - Earth" || $scope.characterContext.poly.name === "Shaman’s Greater Necklace - Earth") {
                if (monster.sonic !== 0) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.sonic > 0) 
                            mDmgTotalDR += monster.sonic;                     
                        else
                            mDmgTotal += Math.abs(monster.sonic);
                    }  
                } if (monster.sonicPercent !== 1.0) {
                    if (mDmgTotal > 0 && isMainDr) 
                        mDmgTotal = Math.round(monster.sonicPercent * mDmgTotal);
                }
                allSonic = true;
            } else {
                if (monster.fire !== 0 && +$scope.characterContext.stats.mfire) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.fire > 0)
                            mDmgTotalDR += monster.fire;
                        else
                            mDmgTotal += Math.abs(monster.fire);
                    }
                } if (monster.firePercent !== 1.0 && +$scope.characterContext.stats.mfire) {
                    var percentMod = (Math.round(monster.firePercent * +$scope.characterContext.stats.mfire) - +$scope.characterContext.stats.mfire);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) {
                            if (monster.monsterEffects.includes("FIRE_HEAL"))
                                mDmgTotal -= percentMod;
                            else
                                mDmgTotal += percentMod;
                        }
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.cold !== 0 && +$scope.characterContext.stats.mcold) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.cold > 0)
                            mDmgTotalDR += monster.cold;
                        else
                            mDmgTotal += Math.abs(monster.cold);
                    } 
                } if (monster.coldPercent !== 1.0 && +$scope.characterContext.stats.mcold) {
                    var percentMod = (Math.round(monster.coldPercent * +$scope.characterContext.stats.mcold) - +$scope.characterContext.stats.mcold);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.shock !== 0 && +$scope.characterContext.stats.mshock) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.shock > 0)
                            mDmgTotalDR += monster.shock;
                        else
                            mDmgTotal += Math.abs(monster.shock);
                    }
                } if (monster.shockPercent !== 1.0 && +$scope.characterContext.stats.mshock) {
                    var percentMod = (Math.round(monster.shockPercent * +$scope.characterContext.stats.mshock) - +$scope.characterContext.stats.mshock);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.sonic !== 0 && +$scope.characterContext.stats.msonic) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.sonic > 0)
                            mDmgTotalDR += monster.sonic;
                        else
                            mDmgTotal += Math.abs(monster.sonic);
                    }
                } if (monster.sonicPercent !== 1.0 && +$scope.characterContext.stats.msonic) {
                    var percentMod = (Math.round(monster.sonicPercent * +$scope.characterContext.stats.msonic) - +$scope.characterContext.stats.msonic);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.poison !== 0 && +$scope.characterContext.stats.mpoison) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.poison > 0)
                            mDmgTotalDR += monster.poison;
                        else
                            mDmgTotal += Math.abs(monster.poison);
                    } 
                } if (monster.poisonPercent !== 1.0 && +$scope.characterContext.stats.mpoison) {
                    var percentMod = (Math.round(monster.poisonPercent * +$scope.characterContext.stats.mpoison) - +$scope.characterContext.stats.mpoison);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.darkrift !== 0 && +$scope.characterContext.stats.mdarkrift) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.darkrift > 0)
                            mDmgTotalDR += monster.darkrift;
                        else
                            mDmgTotal += Math.abs(monster.darkrift);
                    }
                } if (monster.darkriftPercent !== 1.0 && +$scope.characterContext.stats.mdarkrift) {
                     var percentMod = (Math.round(monster.darkriftPercent * +$scope.characterContext.stats.mdarkrift) - +$scope.characterContext.stats.mdarkrift);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.sacred !== 0 && +$scope.characterContext.stats.msacred) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.sacred > 0)
                            mDmgTotalDR += monster.sacred;
                        else
                            mDmgTotal += Math.abs(monster.sacred);
                    }  
                } if (monster.sacredPercent !== 1.0 && +$scope.characterContext.stats.msacred) {
                    var percentMod = (Math.round(monster.sacredPercent * +$scope.characterContext.stats.msacred) - +$scope.characterContext.stats.msacred);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.force !== 0 && +$scope.characterContext.stats.mforce) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.force > 0)
                            mDmgTotalDR += monster.force;
                        else
                            mDmgTotal += Math.abs(monster.force);
                    } 
                } if (monster.forcePercent !== 1.0 && +$scope.characterContext.stats.mforce) {
                     var percentMod = (Math.round(monster.forcePercent * +$scope.characterContext.stats.mforce) - +$scope.characterContext.stats.mforce);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.acid !== 0 && +$scope.characterContext.stats.macid) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.acid > 0)
                            mDmgTotalDR += monster.acid;
                        else
                            mDmgTotal += Math.abs(monster.acid);
                    }
                } if (monster.acidPercent !== 1.0 && +$scope.characterContext.stats.macid) {
                    var percentMod = (Math.round(monster.acidPercent * +$scope.characterContext.stats.macid) - +$scope.characterContext.stats.macid);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                }
            }
            
            if (mDmgTotal < 0 && !monster.monsterEffects.includes("FIRE_HEAL"))
                mDmgTotal = 0;

            var eleDmg = mDmg + rollDmg;
            if (mDmgTotal > eleDmg)
                eleDmg = mDmgTotal;
            if ((monster.critical || critImmuneUsed) && hitRoll >= $scope.characterContext.meleePolyCritMin && !$scope.hasEffect($scope.characterContext.meleePolyDmgEffects, "NO_DAMAGE_MOD") && !$scope.hasEffect(monster.monsterEffects, "MELEE_MAIN_ON_20")) {
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
            
             // Apply DR now
            if (mDmgTotalDR > 0) {
                mCritDmg -= mDmgTotalDR;
                mDmgTotal -= mDmgTotalDR;
                eleDmg -= mDmgTotalDR;
            } 
            if (mDmgTotal < 0)
                mDmgTotal = 0;
            if (mCritDmg < 0)
                mCritDmg = 0;
            if (eleDmg < 0)
                eleDmg = 0;
            
            if (monster.mac > hitRollMod) {
                mDmgTotal = 0;
                mCritDmg = 0;
                eleDmg = 0;
            }

            if ($scope.characterContext.rollerId !== null) {
                vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                    ' "rolls": [{ "type": "melee_main", "dieResult": ' + hitRoll + ', "modifiedResult": ' + hitRollMod + ', "isSuccess": ' + (monster.mac <= hitRollMod) + ', "damage": ' + ((mCritDmg > 0) ? mCritDmg : mDmgTotal) + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Polymorph Attack","result":response.data.errors[0]});       
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast(); 
                    return($q.reject(response));
                }).then(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Polymorph Attack","result":"Polymorph attack roll sent"});
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast();   
                });
            } else {
                if (mDmgTotal === 0) {
                    vtdHistory.add({"type":"ATTACK","sub":"POLY","isMiss":true,"roll":hitRollMod,"mRoll":hitRollMod});
                } else {
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
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast(); 
                }
            }
        } 
    };
    
    $scope.rollToHitAnimalCompanion =  function(monsterIndex) {        
        var hitRoll = 1;
        var monster = null;
        
        if (!($scope.characterContext.animalCompanion && $scope.characterContext.animalCompanion.dmgRange && $scope.characterContext.animalCompanion.dmgRange.length > 0)) {
            warnDialogSvc.showMessage("Animal Companion Seelection Required", "You must select your animal companion form before attacking.");
            return;
        }
        
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
        
        if (hitRoll === 1) {
            vtdHistory.add({"type":"ATTACK","sub":"POLY","isMiss":true,"roll":1});
        } else {
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
        var hitRollOG = 1;
        var monster = null;
        var statusEffect = 'sneak dmg';
        
        var critImmuneUsed = false;
        var buff2 = $scope.hasBuff("Ignore Sneak & Crit Immune");
        if (buff2 !== null) {
            $scope.removeBuff(buff2);
            critImmuneUsed = true;
        }
        
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
        
        if (hitRoll !== 20 && monster.monsterEffects && monster.monsterEffects.includes("MELEE_MAIN_ON_20")) {
            hitRollOG = hitRoll;
            hitRoll = 1;
        } else if (monster.monsterEffects && monster.monsterEffects.includes("INCORPOREAL")) {
            if (!$scope.hasEffect($scope.characterContext.meleeDmgEffects, "IGNORE_INCORPOREAL") && 
                    !$scope.hasBuff("Ignore Incorporeal") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                hitRollOG = hitRoll;
                hitRoll = 1;
            }
        } else if (monster.monsterEffects && monster.monsterEffects.includes("MISS_50")) {
            if (!$scope.hasBuff("Ignore Miss") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                hitRollOG = hitRoll;
                hitRoll = 1;
            }
        }  
 
        if (monster !== null && !monster.sneak && !critImmuneUsed) {  
            if ($scope.characterContext.rollerId !== null) {
                vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                    ' "rolls": [{ "type": "melee_main", "dieResult": 1, "modifiedResult": 1, "isSuccess": false, "damage": 0' + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Sneak Attack","result":response.data.errors[0]});       
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast(); 
                    return($q.reject(response));
                }).then(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Sneak Attack","result":"Your sneak attack failed to find any vulnerable weak spots"});
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast();   
                });
            } else {
                vtdHistory.add({"type":"ATTACK","sub":"MELEE_SNEAK","isImmune":true,"immuneText":"Your sneak attack failed to find any vulnerable weak spots"});
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast();
            }
        } else if (hitRoll === 1) {
            if ($scope.characterContext.rollerId !== null) {
                vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                    ' "rolls": [{ "type": "melee_main", "dieResult": 1, "modifiedResult": 1, "isSuccess": false, "damage": 0' + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Sneak Attack","result":response.data.errors[0]});       
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast(); 
                    return($q.reject(response));
                }).then(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Sneak Attack","result":"Sneak attack roll sent"});
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast();   
                });
            } else {
                vtdHistory.add({"type":"ATTACK","sub":"MELEE_SNEAK","isImmune":false,"isMiss":true,"roll":1,"mRoll":hitRollOG});
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast();
            }
        } else {
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
                    else if ($scope.characterContext.meleeWeaponExplodeEffect === "CRIT" && (monster.critical || critImmuneUsed) && hitRoll >= $scope.characterContext.meleeCritMin)
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
                    else if ($scope.characterContext.meleeWeaponSecondaryExplodeEffect === "CRIT" && (monster.critical || critImmuneUsed) && hitRoll >= $scope.characterContext.meleeCritMin)
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
            
            var isMainDr = !$scope.hasEffect($scope.characterContext.meleeDmgEffects, "NO_DAMAGE_MOD");
            var mDmgTotal = 0;
            if (hitRoll > 1)
                mDmgTotal = mDmg + rollDmg;
            else
                mDmg = 0;
           
            var mDmgTotalDR = 0;
            if (monster.universalDr !==0) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.universalDr > 0)
                        mDmgTotalDR += monster.universalDr;
                    else
                        mDmgTotal += Math.abs(monster.universalDr);
                }
            }
            if (monster.meleeDr !==0) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.meleeDr > 0)
                        mDmgTotalDR += monster.meleeDr;
                    else
                        mDmgTotal += Math.abs(monster.meleeDr);
                }
            } if (monster.meleePercent !== 1.0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal = Math.round(mDmgTotal * monster.meleePercent);
            }
            
            if (monster.fire !== 0 && +$scope.characterContext.stats.mfire) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.fire > 0)
                        mDmgTotalDR += monster.fire;
                    else
                        mDmgTotal += Math.abs(monster.fire);
                }
            } if (monster.firePercent !== 1.0 && +$scope.characterContext.stats.mfire) {
                var percentMod = (Math.round(monster.firePercent * +$scope.characterContext.stats.mfire) - +$scope.characterContext.stats.mfire);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) {
                        if (monster.monsterEffects.includes("FIRE_HEAL"))
                            mDmgTotal -= percentMod;
                        else
                            mDmgTotal += percentMod;
                    }
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.cold !== 0 && +$scope.characterContext.stats.mcold) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.cold > 0)
                        mDmgTotalDR += monster.cold;
                    else
                        mDmgTotal += Math.abs(monster.cold);
                } 
            } if (monster.coldPercent !== 1.0 && +$scope.characterContext.stats.mcold) {
                var percentMod = (Math.round(monster.coldPercent * +$scope.characterContext.stats.mcold) - +$scope.characterContext.stats.mcold);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.shock !== 0 && +$scope.characterContext.stats.mshock) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.shock > 0)
                        mDmgTotalDR += monster.shock;
                    else
                        mDmgTotal += Math.abs(monster.shock);
                }
            } if (monster.shockPercent !== 1.0 && +$scope.characterContext.stats.mshock) {
                var percentMod = (Math.round(monster.shockPercent * +$scope.characterContext.stats.mshock) - +$scope.characterContext.stats.mshock);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.sonic !== 0 && +$scope.characterContext.stats.msonic) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.sonic > 0)
                        mDmgTotalDR += monster.sonic;
                    else
                        mDmgTotal += Math.abs(monster.sonic);
                }
            } if (monster.sonicPercent !== 1.0 && +$scope.characterContext.stats.msonic) {
                var percentMod = (Math.round(monster.sonicPercent * +$scope.characterContext.stats.msonic) - +$scope.characterContext.stats.msonic);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.poison !== 0 && +$scope.characterContext.stats.mpoison) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.poison > 0)
                        mDmgTotalDR += monster.poison;
                    else
                        mDmgTotal += Math.abs(monster.poison);
                } 
            } if (monster.poisonPercent !== 1.0 && +$scope.characterContext.stats.mpoison) {
                var percentMod = (Math.round(monster.poisonPercent * +$scope.characterContext.stats.mpoison) - +$scope.characterContext.stats.mpoison);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.darkrift !== 0 && +$scope.characterContext.stats.mdarkrift) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.darkrift > 0)
                        mDmgTotalDR += monster.darkrift;
                    else
                        mDmgTotal += Math.abs(monster.darkrift);
                }
            } if (monster.darkriftPercent !== 1.0 && +$scope.characterContext.stats.mdarkrift) {
                 var percentMod = (Math.round(monster.darkriftPercent * +$scope.characterContext.stats.mdarkrift) - +$scope.characterContext.stats.mdarkrift);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.sacred !== 0 && +$scope.characterContext.stats.msacred) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.sacred > 0)
                        mDmgTotalDR += monster.sacred;
                    else
                        mDmgTotal += Math.abs(monster.sacred);
                }  
            } if (monster.sacredPercent !== 1.0 && +$scope.characterContext.stats.msacred) {
               if (mDmgTotal > 0 && isMainDr) {
                    var sacredMod = (+$scope.characterContext.mainSacredBonus > 0) ? (Math.round(monster.sacredPercent * (+$scope.characterContext.stats.msacred + +$scope.characterContext.mainSacredBonus + mDmg)) - (+$scope.characterContext.stats.msacred + +$scope.characterContext.mainSacredBonus + mDmg)) :
                            (Math.round(monster.sacredPercent * +$scope.characterContext.stats.msacred) - +$scope.characterContext.stats.msacred);
                    if (sacredMod > 0)
                        mDmgTotal += sacredMod;
                    else
                        mDmgTotalDR += Math.abs(sacredMod);
                }
            } if (monster.force !== 0 && +$scope.characterContext.stats.mforce) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.force > 0)
                        mDmgTotalDR += monster.force;
                    else
                        mDmgTotal += Math.abs(monster.force);
                } 
            } if (monster.forcePercent !== 1.0 && +$scope.characterContext.stats.mforce) {
                 var percentMod = (Math.round(monster.forcePercent * +$scope.characterContext.stats.mforce) - +$scope.characterContext.stats.mforce);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.acid !== 0 && +$scope.characterContext.stats.macid) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.acid > 0)
                        mDmgTotalDR += monster.acid;
                    else
                        mDmgTotal += Math.abs(monster.acid);
                }
            } if (monster.acidPercent !== 1.0 && +$scope.characterContext.stats.macid) {
                var percentMod = (Math.round(monster.acidPercent * +$scope.characterContext.stats.macid) - +$scope.characterContext.stats.macid);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
            }
            
            if (mDmgTotal < 0)
                mDmgTotal = 0;
            
            if (hitRoll > 1) {
                if ((monster.critical || critImmuneUsed) && hitRoll >= $scope.characterContext.meleeSneakCritMin && !$scope.hasEffect($scope.characterContext.meleeDmgEffects, "NO_DAMAGE_MOD") && !$scope.hasEffect(monster.monsterEffects, "MELEE_MAIN_ON_20")) {
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
                
                if (mCritDmg > 0)
                    mCritDmg += $scope.characterContext.unmodifiableSneakDamage;
                mDmgTotal += $scope.characterContext.unmodifiableSneakDamage;
                
                // Apply DR now
                if (mDmgTotalDR > 0) {
                    mCritDmg -= mDmgTotalDR;
                    mDmgTotal -= mDmgTotalDR;
                } 
                if (mDmgTotal < 0)
                    mDmgTotal = 0;
                if (mCritDmg < 0)
                    mCritDmg = 0;
            }
            
            if (monster.mac > hitRollMod) {
                mDmgTotal = 0;
                mCritDmg = 0;
            }

            if ($scope.characterContext.rollerId !== null) {
                vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                    ' "rolls": [{ "type": "melee_main", "dieResult": ' + hitRoll + ', "modifiedResult": ' + hitRollMod + ', "isSuccess": ' + (monster.mac <= hitRollMod) + ', "damage": ' + ((mCritDmg > 0) ? mCritDmg : mDmgTotal) + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Sneak Attack","result":response.data.errors[0]});       
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast(); 
                    return($q.reject(response));
                }).then(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Sneak Attack","result":"Sneak attack roll sent"});
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast();   
                });
            } else {
                if (mDmgTotal === 0) {
                    vtdHistory.add({"type":"ATTACK","sub":"MELEE_SNEAK","isMiss":true,"roll":hitRollMod,"mRoll":hitRollMod});
                } else {
                    vtdHistory.add({"type":"ATTACK","sub":"MELEE_SNEAK","isImmune":false,"isMiss":false,"isCrit":mCritDmg > 0,"mRoll":hitRoll,"mRollTotal":hitRollMod,
                        "mWheel":mDmg,"mDmg":mDmgTotal,"totalDmg":mDmgTotal,"mCrit":mCritDmg,"critTotal":mCritDmg,"mWeaponExp":mDmgExp,"sWeaponExp":sDmgExp,"fire":$scope.characterContext.stats.mfire,
                        "cold":$scope.characterContext.stats.mcold,"shock":$scope.characterContext.stats.mshock,"sonic":$scope.characterContext.stats.msonic,
                        "eldritch":$scope.characterContext.stats.meldritch,"poison":$scope.characterContext.stats.mpoison,"darkrift":$scope.characterContext.stats.mdarkrift,
                        "sacred":$scope.characterContext.stats.msacred,"force":$scope.characterContext.stats.mforce,"acid":$scope.characterContext.stats.macid});
                }
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast();  
            }
        }
    };
    
    $scope.rollToHitSneakRange =  function(monsterIndex) {        
        var hitRoll = 1;
        var hitRollOG = 1;
        var monster = null;
        var statusEffect = 'sneak dmg';
        
        $scope.resultIndex = 0;
        
        var critImmuneUsed = false;
        var buff2 = $scope.hasBuff("Ignore Sneak & Crit Immune");
        if (buff2 !== null) {
            $scope.removeBuff(buff2);
            critImmuneUsed = true;
        }
        
        var buff = $scope.hasBuff("Flank");
        if (buff !== null) {
            $scope.removeBuff(buff);
        }
        
        var critImmuneUsed = false;
        var buff2 = $scope.hasBuff("Ignore Sneak & Crit Immune");
        if (buff2 !== null) {
            $scope.removeBuff(buff2);
            critImmuneUsed = true;
        }
        
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
        
        if (hitRoll !== 20 && monster.monsterEffects && monster.monsterEffects.includes("RANGE_MAIN_ON_20")) {
            hitRollOG = hitRoll;
            hitRoll = 1;
        } else if (monster.monsterEffects && monster.monsterEffects.includes("INCORPOREAL")) {
            if (!$scope.hasEffect($scope.characterContext.rangeDmgEffects, "IGNORE_INCORPOREAL") && 
                    !$scope.hasBuff("Ignore Incorporeal") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                hitRollOG = hitRoll;
                hitRoll = 1;
            }
        } else if (monster.monsterEffects && monster.monsterEffects.includes("MISS_50")) {
            if (!$scope.hasBuff("Ignore Miss") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                hitRollOG = hitRoll;
                hitRoll = 1;
            }
        }  
        
        if (monster !== null && !monster.sneak && !critImmuneUsed) {
            if ($scope.characterContext.rollerId !== null) {
                vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                    ' "rolls": [{ "type": "range_main", "dieResult": 1, "modifiedResult": 1, "isSuccess": false, "damage": 0' + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Sneak Attack","result":response.data.errors[0]});       
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast(); 
                    return($q.reject(response));
                }).then(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Sneak Attack","result":"Your sneak attack failed to find any vulnerable weak spots"});
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast();   
                });
            } else {
                vtdHistory.add({"type":"ATTACK","sub":"RANGE_SNEAK","isImmune":true,"immuneText":"Your sneak attack failed to find any vulnerable weak spots"});
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast();
            }
        } else if (hitRoll === 1) {
            if ($scope.characterContext.rollerId !== null) {
                vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                    ' "rolls": [{ "type": "range_main", "dieResult": 1, "modifiedResult": 1, "isSuccess": false, "damage": 0' + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Sneak Attack","result":response.data.errors[0]});       
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast(); 
                    return($q.reject(response));
                }).then(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Sneak Attack","result":"Sneak attack roll sent"});
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast();   
                });
            } else {
                vtdHistory.add({"type":"ATTACK","sub":"RANGE_SNEAK","isImmune":false,"isMiss":true,"roll":1,"mRoll":hitRollOG});
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast();
            }
        } else {
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
                    else if ($scope.characterContext.rangeWeaponExplodeEffect === "CRIT" && (monster.critical || critImmuneUsed) && hitRoll >= $scope.characterContext.meleeCritMin)
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
                
                var isMainDr = !$scope.hasEffect($scope.characterContext.rangeDmgEffects, "NO_DAMAGE_MOD");
                var mDmgTotal = 0;
                if (hitRoll > 1)
                    mDmgTotal = mDmg + rollDmg;
                else
                    mDmg = 0;
                
                var mDmgTotalDR = 0;
                if (monster.universalDr !==0) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.universalDr > 0)
                            mDmgTotalDR += monster.universalDr;
                        else
                            mDmgTotal += Math.abs(monster.universalDr);
                    }
                }
                if (monster.rangeDr !==0) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.rangeDr > 0)
                            mDmgTotalDR += monster.rangeDr;
                        else
                            mDmgTotal += Math.abs(monster.rangeDr);
                    }
                } if (monster.rangePercent !== 1.0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal = Math.round(mDmgTotal * monster.rangePercent);
                }

                if (monster.fire !== 0 && +$scope.characterContext.stats.rfire) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.fire > 0)
                            mDmgTotalDR += monster.fire;
                        else
                            mDmgTotal += Math.abs(monster.fire);
                    }
                } if (monster.firePercent !== 1.0 && +$scope.characterContext.stats.rfire) {
                    var percentMod = (Math.round(monster.firePercent * +$scope.characterContext.stats.rfire) - +$scope.characterContext.stats.rfire);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) {
                            if (monster.monsterEffects.includes("FIRE_HEAL"))
                                mDmgTotal -= percentMod;
                            else
                                mDmgTotal += percentMod;
                        }
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.cold !== 0 && +$scope.characterContext.stats.rcold) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.cold > 0)
                            mDmgTotalDR += monster.cold;
                        else
                            mDmgTotal += Math.abs(monster.cold);
                    }  
                } if (monster.coldPercent !== 1.0 && +$scope.characterContext.stats.rcold) {
                    var percentMod = (Math.round(monster.coldPercent * +$scope.characterContext.stats.mcold) - +$scope.characterContext.stats.mcold);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.shock !== 0 && +$scope.characterContext.stats.rshock) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.shock > 0)
                            mDmgTotalDR += monster.shock;
                        else
                            mDmgTotal += Math.abs(monster.shock);
                    }
                } if (monster.shockPercent !== 1.0 && +$scope.characterContext.stats.rshock) {
                    var percentMod = (Math.round(monster.shockPercent * +$scope.characterContext.stats.rshock) - +$scope.characterContext.stats.rshock);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.sonic !== 0 && +$scope.characterContext.stats.rsonic) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.sonic > 0)
                            mDmgTotalDR += monster.sonic;
                        else
                            mDmgTotal += Math.abs(monster.sonic);
                    }
                } if (monster.sonicPercent !== 1.0 && +$scope.characterContext.stats.rsonic) {
                    var percentMod = (Math.round(monster.sonicPercent * +$scope.characterContext.stats.rsonic) - +$scope.characterContext.stats.rsonic);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.poison !== 0 && +$scope.characterContext.stats.rpoison) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.poison > 0)
                            mDmgTotalDR += monster.poison;
                        else
                            mDmgTotal += Math.abs(monster.poison);
                    }  
                } if (monster.poisonPercent !== 1.0 && +$scope.characterContext.stats.rpoison) {
                    var percentMod = (Math.round(monster.poisonPercent * +$scope.characterContext.stats.rpoison) - +$scope.characterContext.stats.rpoison);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.darkrift !== 0 && +$scope.characterContext.stats.rdarkrift) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.darkrift > 0)
                            mDmgTotalDR += monster.darkrift;
                        else
                            mDmgTotal += Math.abs(monster.darkrift);
                    }
                } if (monster.darkriftPercent !== 1.0 && +$scope.characterContext.stats.rdarkrift) {
                     var percentMod = (Math.round(monster.darkriftPercent * +$scope.characterContext.stats.rdarkrift) - +$scope.characterContext.stats.rdarkrift);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.sacred !== 0 && +$scope.characterContext.stats.rsacred) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.sacred > 0)
                            mDmgTotalDR += monster.sacred;
                        else
                            mDmgTotal += Math.abs(monster.sacred);
                    }  
                } if (monster.sacredPercent !== 1.0 && +$scope.characterContext.stats.rsacred) {
                    var percentMod = (Math.round(monster.sacredPercent * +$scope.characterContext.stats.rsacred) - +$scope.characterContext.stats.rsacred);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.force !== 0 && +$scope.characterContext.stats.rforce) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.force > 0)
                            mDmgTotalDR += monster.force;
                        else
                            mDmgTotal += Math.abs(monster.force);
                    }
                } if (monster.forcePercent !== 1.0 && +$scope.characterContext.stats.rforce) {
                     var percentMod = (Math.round(monster.forcePercent * +$scope.characterContext.stats.rforce) - +$scope.characterContext.stats.rforce);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.acid !== 0 && +$scope.characterContext.stats.racid) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.acid > 0)
                            mDmgTotalDR += monster.acid;
                        else
                            mDmgTotal += Math.abs(monster.acid);
                    }
                } if (monster.acidPercent !== 1.0 && +$scope.characterContext.stats.racid) {
                    var percentMod = (Math.round(monster.acidPercent * +$scope.characterContext.stats.racid) - +$scope.characterContext.stats.racid);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                }
                
                if (mDmgTotal < 0)
                    mDmgTotal = 0;
                
                if ((monster.critical || critImmuneUsed) && hitRoll >= $scope.characterContext.rangeSneakCritMin && !$scope.hasEffect($scope.characterContext.rangeDmgEffects, "NO_DAMAGE_MOD") && !$scope.hasEffect(monster.monsterEffects, "RANGE_MAIN_ON_20")) {
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
                
                if (mCritDmg > 0)
                    mCritDmg += $scope.characterContext.unmodifiableSneakDamage;
                mDmgTotal += $scope.characterContext.unmodifiableSneakDamage;
                
                // Apply DR now
                if (mDmgTotalDR > 0) {
                    mCritDmg -= mDmgTotalDR;
                    mDmgTotal -= mDmgTotalDR;
                } 
                if (mDmgTotal < 0)
                    mDmgTotal = 0;
                if (mCritDmg < 0)
                    mCritDmg = 0;
            }
            
            if (monster.rac > hitRollMod) {
                mDmgTotal = 0;
                mCritDmg = 0;
            }

            if ($scope.characterContext.rollerId !== null) {
                vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                    ' "rolls": [{ "type": "range_main", "dieResult": ' + hitRoll + ', "modifiedResult": ' + hitRollMod + ', "isSuccess": ' + (monster.rac <= hitRollMod) + ', "damage": ' + ((mCritDmg > 0) ? mCritDmg : mDmgTotal) + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Sneak Attack","result":response.data.errors[0]});       
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast(); 
                    return($q.reject(response));
                }).then(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Sneak Attack","result":"Sneak attack roll sent"});
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast();   
                });
            } else {
                if (mDmgTotal === 0) {
                    vtdHistory.add({"type":"ATTACK","sub":"RANGE_SNEAK","isMiss":true,"roll":hitRollMod,"mRoll":hitRollMod});
                } else {
                    vtdHistory.add({"type":"ATTACK","sub":"RANGE_SNEAK","isImmune":false,"isMiss":false,"isCrit":mCritDmg > 0,"mRoll":hitRoll,"mRollTotal":hitRollMod,
                        "mWheel":mDmg,"mDmg":mDmgTotal,"totalDmg":mDmgTotal,"mCrit":mCritDmg,"critTotal":mCritDmg,"mWeaponExp":mDmgExp,"fire":$scope.characterContext.stats.rfire,
                        "cold":$scope.characterContext.stats.rcold,"shock":$scope.characterContext.stats.rshock,"sonic":$scope.characterContext.stats.rsonic,
                        "eldritch":$scope.characterContext.stats.reldritch,"poison":$scope.characterContext.stats.rpoison,"darkrift":$scope.characterContext.stats.rdarkrift,
                        "sacred":$scope.characterContext.stats.rsacred,"force":$scope.characterContext.stats.rforce,"acid":$scope.characterContext.stats.racid});
                }
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast(); 
            }
        } 
    };
    
    $scope.rollToHitSubdual =  function(monsterIndex) {        
        var hitRoll = 1;
        var hitRollOG = 1;
        var monster = null;
        
        $scope.resultIndex = 0;
        
        if ($scope.characterContext.monsters === null || $scope.characterContext.monsters.length === 0) {
            hitRoll = $scope.getRandomInt(20) + 1;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex === undefined) {
            monsterSelectorSvc.selectMonster($scope.characterContext.monsters, function(index) {
                $scope.rollToHitSubdual(index);
            });
            return;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex !== undefined) {
            monster = monsterIndex;
            hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
        } else {
            monster = $scope.characterContext.monsters[0];
            hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
        }
        
        if (hitRoll !== 20 && monster.monsterEffects && monster.monsterEffects.includes("MELEE_MAIN_ON_20")) {
            hitRollOG = hitRoll;
            hitRoll = 1;
        } else if (monster.monsterEffects && monster.monsterEffects.includes("INCORPOREAL")) {
            if (!$scope.hasEffect($scope.characterContext.meleeDmgEffects, "IGNORE_INCORPOREAL") && 
                    !$scope.hasBuff("Ignore Incorporeal") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                hitRollOG = hitRoll;
                hitRoll = 1;
            }
        } else if (monster.monsterEffects && monster.monsterEffects.includes("MISS_50")) {
            if (!$scope.hasBuff("Ignore Miss") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                hitRollOG = hitRoll;
                hitRoll = 1;
            }
        } 
 
        if (hitRoll === 1)
            vtdHistory.add({"type":"ATTACK","sub":"MELEE_SUBDUAL","isMiss":true,"roll":1,"mRoll":hitRollOG});
        else {
            var hitRollMod = 1;
            var rollDmg = 0;
            var mDmg = 1;
            var mDmgExp = null;
            var sDmgExp = null;
            var mCritDmg = 0;

            if (hitRoll > 1)
                hitRollMod = hitRoll + $scope.characterContext.stats.meleeHit;
            if (!$scope.hasEffect($scope.characterContext.meleeDmgEffects, "NO_DAMAGE_MOD"))
                rollDmg =  $scope.characterContext.stats.meleeDmg;
    
            var mDmgTotal = 0;
            if (hitRoll > 1)
                mDmgTotal = mDmg + rollDmg;
            else
                mDmg = 0;
    
            if (mDmgTotal < 0)
                mDmgTotal = 0;

            vtdHistory.add({"type":"ATTACK","sub":"MELEE_SUBDUAL","isMiss":false,"isCrit":mCritDmg > 0,"mRoll":hitRoll,"mRollTotal":hitRollMod,
                "mWheel":mDmg,"mDmg":mDmgTotal,"totalDmg":mDmgTotal,"mCrit":mCritDmg,"critTotal":mCritDmg,"mWeaponExp":mDmgExp,"sWeaponExp":sDmgExp,"fire":$scope.characterContext.stats.mfire,
                "cold":$scope.characterContext.stats.mcold,"shock":$scope.characterContext.stats.mshock,"sonic":$scope.characterContext.stats.msonic,
                "eldritch":$scope.characterContext.stats.meldritch,"poison":$scope.characterContext.stats.mpoison,"darkrift":$scope.characterContext.stats.mdarkrift,
                "sacred":$scope.characterContext.stats.msacred,"force":$scope.characterContext.stats.mforce,"acid":$scope.characterContext.stats.macid});
        }
         
        $scope.history = vtdHistory.get();
        $scope.lastEvent = vtdHistory.getLast();  
    };
}])

.controller('VtdPlayCtrl', ['$scope', '$q', 'VtdSvc', 'VtdState', 'VtdHistory', 'RESOURCES', '$routeParams', '$route', 'WarnDialogSvc', 'ConfirmDialogSvc', 'MonsterSelectorSvc', function ($scope, $q, vtdSvc, vtdState, vtdHistory, RESOURCES, $routeParams, $route, warnDialogSvc, confirmDialogSvc, monsterSelectorSvc) {
    $scope.tabIndex = 0;
    $scope.rollerIndex = 0;
    $scope.sneakIndex = 0;
    $scope.attackIndex = 0;
    $scope.defendIndex = 0;
    $scope.saveIndex = 0;
    $scope.damageIndex = 0;
    $scope.resultIndex = 0;
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
    $scope.history = vtdHistory.get();
    $scope.lastEvent = vtdHistory.getLast();

    vtdSvc.getCharacter($routeParams.characterId).then(function(result) {
        vtdState.setContext(result.data);
        $scope.characterContext = vtdState.get();
    });
    
    $scope.setRollerIndex =  function(index) {
        $scope.rollerIndex = index;
        $scope.rollSave = 0;
        $scope.lastEvent = null;
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
        $scope.lastEvent = null;
    };
    
    $scope.setDefendIndex =  function(index) {
        $scope.defendIndex = index;
        $scope.rollSave = 0;
        $scope.rollSaveNatural = 0;
        $scope.lastEvent = null;
    };
    
    $scope.setSaveIndex =  function(index) {
        $scope.saveIndex = index;
        $scope.rollSave = 0;
        $scope.rollSaveNatural = 0;
        $scope.lastEvent = null;
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
    
    $scope.setRollerId = function(id, rollerId) {
        vtdSvc.setRollerId(id, rollerId).then(function(result) {
            vtdState.setContext(result.data);
            $scope.characterContext = vtdState.get();
            alert("Successfully updated adventure roller ID to " + $scope.characterContext.rollerId);
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
    
    $scope.rollInit =  function() {
        var roll = $scope.getRandomInt(20) + 1;      
        
        if ($scope.characterContext.rollerId !== null) {
            vtdSvc.sendRoll('{"version":1,"slotId":"'+$scope.characterContext.rollerId+'","classId":"'+$scope.characterContext.rollerCharacterClass+'","eventType":"initiative_roll",' +
                '"rolls":[{"dieResult":'+roll+',"modifiedResult":'+(roll+$scope.characterContext.stats.initiative+$scope.characterContext.initBonus)+'}]}').catch(function(response) {
                vtdHistory.add({"type":"ROLLER","sub":"Initiative Roll","result":response.data.errors[0]});       
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast(); 
                return($q.reject(response));
            }).then(function(response) {
                vtdHistory.add({"type":"ROLLER","sub":"Initiative Roll","result":"Initiative roll sent"});
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast();   
            });
        } else {
            vtdHistory.add({"type":"INIT","sub":"INIT","roll":roll,"result":roll + $scope.characterContext.stats.initiative + $scope.characterContext.initBonus});
            $scope.history = vtdHistory.get();
            $scope.lastEvent = vtdHistory.getLast();   
        } 
    };
    
    $scope.rollToHitMelee =  function(monsterIndex, status) {        
        var hitRoll = 1;
        var hitRollOG = 1;
        var offhandHitRoll = 1;
        var offhandHitRollOG = 1;
        var monster = null;
        var flankUsed = false;
        var statusEffect = status === undefined ? '' :  status;
        
        $scope.resultIndex = 0;
        
        var buff = $scope.hasBuff("Flank");
        if (buff !== null) {
            $scope.removeBuff(buff);
            var flankUsed = true;
            statusEffect = 'flank buff';
        }
        
        var critImmuneUsed = false;
        var buff2 = $scope.hasBuff("Ignore Sneak & Crit Immune");
        if (buff2 !== null) {
            $scope.removeBuff(buff2);
            critImmuneUsed = true;
        }
        
        if ($scope.characterContext.monsters === null || $scope.characterContext.monsters.length === 0) {
            if (flankUsed || $scope.hasEffect($scope.characterContext.meleeDmgEffects, "SMACK_WEAPON")) {
                hitRoll = Math.max(($scope.getRandomInt(20) + 1), ($scope.getRandomInt(20) + 1), ($scope.getRandomInt(20) + 1));
                offhandHitRoll = Math.max(($scope.getRandomInt(20) + 1), ($scope.getRandomInt(20) + 1), ($scope.getRandomInt(20) + 1));
            } else {
                hitRoll = $scope.getRandomInt(20) + 1;
                offhandHitRoll = $scope.getRandomInt(20) + 1;
            }
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex === undefined) {
            monsterSelectorSvc.selectMonster($scope.characterContext.monsters, function(index) {
                $scope.rollToHitMelee(index, '');
            });
            return;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex !== undefined) {
            monster = monsterIndex;
            if (flankUsed || $scope.hasEffect($scope.characterContext.meleeDmgEffects, "SMACK_WEAPON")) {
                hitRoll = Math.max(monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)]);
                offhandHitRoll = Math.max(monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)]);
            } else {
                hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
                offhandHitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
            }
        } else {
            monster = $scope.characterContext.monsters[0];
            if (flankUsed || $scope.hasEffect($scope.characterContext.meleeDmgEffects, "SMACK_WEAPON")) {
                hitRoll = Math.max(monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)]);
                offhandHitRoll = Math.max(monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)]);
            } else {
                hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
                offhandHitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
            }
        }
        
        if (!('meleeOffhandHit' in $scope.characterContext.stats)) {
            offhandHitRoll = 1;
        } else if (offhandHitRoll !== 20 && monster.monsterEffects && monster.monsterEffects.includes("MELEE_OFFHAND_ON_20")) {
            offhandHitRollOG = offhandHitRoll;
            offhandHitRoll = 1;
        } else if (monster.monsterEffects && monster.monsterEffects.includes("INCORPOREAL")) {
            if (!$scope.hasEffect($scope.characterContext.meleeDmgEffects, "IGNORE_INCORPOREAL") && 
                    !$scope.hasBuff("Ignore Incorporeal") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                offhandHitRollOG = offhandHitRoll;
                offhandHitRoll = 1;
            }
        } else if (monster.monsterEffects && monster.monsterEffects.includes("MISS_50")) {
            if (!$scope.hasBuff("Ignore Miss") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                offhandHitRollOG = offhandHitRoll;
                offhandHitRoll = 1;
            }
        } 
        
        if (hitRoll !== 20 && monster.monsterEffects && monster.monsterEffects.includes("MELEE_MAIN_ON_20")) {
            hitRollOG = hitRoll;
            hitRoll = 1;
        } else if (monster.monsterEffects && monster.monsterEffects.includes("INCORPOREAL")) {
            if (!$scope.hasEffect($scope.characterContext.meleeDmgEffects, "IGNORE_INCORPOREAL") && 
                    !$scope.hasBuff("Ignore Incorporeal") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                hitRollOG = hitRoll;
                hitRoll = 1;
            }
        } else if (monster.monsterEffects && monster.monsterEffects.includes("MISS_50")) {
            if (!$scope.hasBuff("Ignore Miss") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                hitRollOG = hitRoll;
                hitRoll = 1;
            }
        } 

        if (hitRoll === 1 && offhandHitRoll === 1) {
            var buff = $scope.hasBuff("WS Reroll");
            if (buff !== null && status !== 'ws reroll') {
                $scope.removeBuff(buff);
                $scope.rollToHitMelee(monsterIndex, 'ws reroll');
            }
                    
            if ($scope.characterContext.rollerId !== null) {
                if (!('meleeOffhandHit' in $scope.characterContext.stats)) {
                    vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                        ' "rolls": [{ "type": "melee_main", "dieResult": 1, "modifiedResult": 1, "isSuccess": false, "damage": 0, "effect": "' + statusEffect + '" }]}').catch(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Melee Attack","result":response.data.errors[0]});       
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast(); 
                        return($q.reject(response));
                    }).then(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Melee Attack","result":"Melee attack roll sent"});
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast();   
                    });
                } else {
                    vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                        ' "rolls": [{ "type": "melee_main", "dieResult": 1, "modifiedResult": 1, "isSuccess": false, "damage": 0 }, { "type": "melee_off", "dieResult": 1, "modifiedResult": 1, "isSuccess": false, "damage": 0, "effect": "' + statusEffect + '" }]}').catch(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Melee Attack","result":response.data.errors[0]});       
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast(); 
                        return($q.reject(response));
                    }).then(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Melee Attack","result":"Melee attack roll sent"});
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast();   
                    });
                }
            } else {
                vtdHistory.add({"type":"ATTACK","sub":"MELEE","isMiss":true,"roll":1,"mRoll":hitRollOG,"oRoll":offhandHitRollOG});
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast();   
            }
        } else {
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
                    hitRollMod = hitRoll + $scope.characterContext.stats.rangeHitBenrow;
                if (offhandHitRoll > 1)
                    offhandHitRollMod = offhandHitRoll + $scope.characterContext.stats.rangeOffhandHitBenrow;
                if (!$scope.hasEffect($scope.characterContext.meleeDmgEffects, "NO_DAMAGE_MOD"))
                    mRollDmg = $scope.characterContext.stats.rangeDmgBenrow;
                if (!$scope.hasEffect($scope.characterContext.meleeOffhandDmgEffects, "NO_DAMAGE_MOD"))
                    oRollDmg = $scope.characterContext.stats.rangeDmgBenrow;
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
                    else if ($scope.characterContext.meleeWeaponExplodeEffect === "CRIT" && (monster.critical || critImmuneUsed) && hitRoll >= $scope.characterContext.meleeCritMin)
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
                    else if ($scope.characterContext.meleeWeaponSecondaryExplodeEffect === "CRIT" && (monster.critical || critImmuneUsed) && hitRoll >= $scope.characterContext.meleeCritMin)
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
                    else if ($scope.characterContext.meleeOffhandWeaponExplodeEffect === "CRIT" && (monster.critical || critImmuneUsed) && hitRoll >= $scope.characterContext.meleeCritMin)
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
            
            var isMainDr = !$scope.hasEffect($scope.characterContext.meleeDmgEffects, "NO_DAMAGE_MOD");
            var isOffDr = !$scope.hasEffect($scope.characterContext.meleeOffhandDmgEffects, "NO_DAMAGE_MOD");
            var mDmgTotal = 0;
            if (hitRoll > 1 && isMainDr)
                mDmgTotal = mDmg + mRollDmg + monster.bonusDmg;
            else
                mDmg = 0;
            
            var oDmgTotal = 0;
            if (offhandHitRoll > 1 && isOffDr)
                oDmgTotal = oDmg + oRollDmg + monster.bonusDmg;
            else
                oDmg = 0;
            
            var mDmgTotalDR = 0;
            var oDmgTotalDR = 0;
            if (monster.universalDr !==0) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.universalDr > 0)
                        mDmgTotalDR += monster.universalDr;
                    else
                        mDmgTotal += Math.abs(monster.universalDr);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (monster.universalDr > 0)
                        oDmgTotalDR += monster.universalDr;
                    else
                        oDmgTotal += Math.abs(monster.universalDr);
                }
            }
            
            if ($scope.isFurryThrow) {
                if (monster.fire !== 0 && +$scope.characterContext.stats.bfire) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.fire > 0)
                            mDmgTotalDR += monster.fire;
                        else
                            mDmgTotal += Math.abs(monster.fire);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.fire > 0)
                            oDmgTotalDR += monster.fire;
                        else
                            oDmgTotal += Math.abs(monster.fire);
                    } 
                } if (monster.firePercent !== 1.0 && +$scope.characterContext.stats.bfire) {
                    var fireMod = (Math.round(monster.firePercent * +$scope.characterContext.stats.bfire) - +$scope.characterContext.stats.bfire);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (fireMod > 0)
                            mDmgTotal += fireMod;
                        else
                            mDmgTotalDR += Math.abs(fireMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (fireMod > 0)
                            oDmgTotal += fireMod;
                        else
                            oDmgTotalDR += Math.abs(fireMod);
                    } 
                } if (monster.cold !== 0 && +$scope.characterContext.stats.bcold) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.cold > 0)
                            mDmgTotalDR += monster.cold;
                        else
                            mDmgTotal += Math.abs(monster.cold);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.cold > 0)
                            oDmgTotalDR += monster.cold;
                        else
                            oDmgTotal += Math.abs(monster.cold);
                    } 
                } if (monster.coldPercent !== 1.0 && +$scope.characterContext.stats.bcold) {
                    var coldMod = (Math.round(monster.coldPercent * +$scope.characterContext.stats.bcold) - +$scope.characterContext.stats.bcold);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (coldMod > 0)
                            mDmgTotal += coldMod;
                        else
                            mDmgTotalDR += Math.abs(coldMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (coldMod > 0)
                            oDmgTotal += coldMod;
                        else
                            oDmgTotalDR += Math.abs(coldMod);
                    } 
                } if (monster.shock !== 0 && +$scope.characterContext.stats.bshock) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.shock > 0)
                            mDmgTotalDR += monster.shock;
                        else
                            mDmgTotal += Math.abs(monster.shock);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.shock > 0)
                            oDmgTotalDR += monster.shock;
                        else
                            oDmgTotal += Math.abs(monster.shock);
                    }  
                } if (monster.shockPercent !== 1.0 && +$scope.characterContext.stats.bshock) {
                    var shockMod = (Math.round(monster.shockPercent * +$scope.characterContext.stats.bshock) - +$scope.characterContext.stats.bshock);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (shockMod > 0)
                            mDmgTotal += shockMod;
                        else
                            mDmgTotalDR += Math.abs(shockMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (shockMod > 0)
                            oDmgTotal += shockMod;
                        else
                            oDmgTotalDR += Math.abs(shockMod);
                    }
                } if (monster.sonic !== 0 && +$scope.characterContext.stats.bsonic) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.sonic > 0)
                            mDmgTotalDR += monster.sonic;
                        else
                            mDmgTotal += Math.abs(monster.sonic);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.sonic > 0)
                            oDmgTotalDR += monster.sonic;
                        else
                            oDmgTotal += Math.abs(monster.sonic);
                    }  
                } if (monster.sonicPercent !== 1.0 && +$scope.characterContext.stats.bsonic) {
                    var sonicMod = (Math.round(monster.sonicPercent * +$scope.characterContext.stats.bsonic) - +$scope.characterContext.stats.bsonic);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (sonicMod > 0)
                            mDmgTotal += sonicMod;
                        else
                            mDmgTotalDR += Math.abs(sonicMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (sonicMod > 0)
                            oDmgTotal += sonicMod;
                        else
                            oDmgTotalDR += Math.abs(sonicMod);
                    }
                } if (monster.poison !== 0 && +$scope.characterContext.stats.bpoison) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.poison > 0)
                            mDmgTotalDR += monster.poison;
                        else
                            mDmgTotal += Math.abs(monster.poison);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.poison > 0)
                            oDmgTotalDR += monster.poison;
                        else
                            oDmgTotal += Math.abs(monster.poison);
                    }  
                } if (monster.poisonPercent !== 1.0 && +$scope.characterContext.stats.bpoison) {
                    var poisonkMod = (Math.round(monster.poisonPercent * +$scope.characterContext.stats.bpoison) - +$scope.characterContext.stats.bpoison);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (poisonkMod > 0)
                            mDmgTotal += poisonkMod;
                        else
                            mDmgTotalDR += Math.abs(poisonkMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (poisonkMod > 0)
                            oDmgTotal += poisonkMod;
                        else
                            oDmgTotalDR += Math.abs(poisonkMod);
                    }
                } if (monster.darkrift !== 0 && +$scope.characterContext.stats.bdarkrift) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.darkrift > 0)
                            mDmgTotalDR += monster.darkrift;
                        else
                            mDmgTotal += Math.abs(monster.darkrift);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.darkrift > 0)
                            oDmgTotalDR += monster.darkrift;
                        else
                            oDmgTotal += Math.abs(monster.darkrift);
                    }  
                } if (monster.darkriftPercent !== 1.0 && +$scope.characterContext.stats.bdarkrift) {
                    var darkriftMod = (Math.round(monster.darkriftPercent * +$scope.characterContext.stats.bdarkrift) - +$scope.characterContext.stats.bdarkrift);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (darkriftMod > 0)
                            mDmgTotal += darkriftMod;
                        else
                            mDmgTotalDR += Math.abs(darkriftMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (darkriftMod > 0)
                            oDmgTotal += darkriftMod;
                        else
                            oDmgTotalDR += Math.abs(darkriftMod);
                    }
                } if (monster.sacred !== 0 && +$scope.characterContext.stats.bsacred) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.sacred > 0)
                            mDmgTotalDR += monster.sacred;
                        else
                            mDmgTotal += Math.abs(monster.sacred);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.sacred > 0)
                            oDmgTotalDR += monster.sacred;
                        else
                            oDmgTotal += Math.abs(monster.sacred);
                    }   
                } if (monster.sacredPercent !== 1.0 && +$scope.characterContext.stats.bsacred) {
                    if (mDmgTotal > 0 && isMainDr) {
                        var sacredMod = (+$scope.characterContext.mainSacredBonus > 0) ? (Math.round(monster.sacredPercent * (+$scope.characterContext.stats.bsacred + +$scope.characterContext.mainSacredBonus + mDmg)) - (+$scope.characterContext.stats.bsacred + +$scope.characterContext.mainSacredBonus + mDmg)) :
                                (Math.round(monster.sacredPercent * +$scope.characterContext.stats.bsacred) - +$scope.characterContext.stats.bsacred);
                        if (sacredMod > 0)
                            mDmgTotal += sacredMod;
                        else
                            mDmgTotalDR += Math.abs(sacredMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                         var sacredMod = (+$scope.characterContext.offSacredBonus > 0) ? (Math.round(monster.sacredPercent * (+$scope.characterContext.stats.bsacred + +$scope.characterContext.offSacredBonus + oDmg)) - (+$scope.characterContext.stats.bsacred + +$scope.characterContext.offSacredBonus + oDmg)) :
                                (Math.round(monster.sacredPercent * +$scope.characterContext.stats.bsacred) - +$scope.characterContext.stats.bsacred);
                        if (sacredMod > 0)
                            oDmgTotal += sacredMod;
                        else
                            oDmgTotalDR += Math.abs(sacredMod);
                    }
                } if (monster.force !== 0 && +$scope.characterContext.stats.bforce) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.force > 0)
                            mDmgTotalDR += monster.force;
                        else
                            mDmgTotal += Math.abs(monster.force);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.force > 0)
                            oDmgTotalDR += monster.force;
                        else
                            oDmgTotal += Math.abs(monster.force);
                    } 
                } if (monster.forcePercent !== 1.0 && +$scope.characterContext.stats.bforce) {
                    var forceMod = (Math.round(monster.forcePercent * +$scope.characterContext.stats.bforce) - +$scope.characterContext.stats.bforce);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (forceMod > 0)
                            mDmgTotal += forceMod;
                        else
                            mDmgTotalDR += Math.abs(forceMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (forceMod > 0)
                            oDmgTotal += forceMod;
                        else
                            oDmgTotalDR += Math.abs(forceMod);
                    }
                } if (monster.acid !== 0 && +$scope.characterContext.stats.bacid) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.acid > 0)
                            mDmgTotalDR += monster.acid;
                        else
                            mDmgTotal += Math.abs(monster.acid);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.acid > 0)
                            oDmgTotalDR += monster.acid;
                        else
                            oDmgTotal += Math.abs(monster.acid);
                    }  
                } if (monster.acidPercent !== 1.0 && +$scope.characterContext.stats.bacid) {
                    var acidMod = (Math.round(monster.acidPercent * +$scope.characterContext.stats.bacid) - +$scope.characterContext.stats.bacid);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (acidMod > 0)
                            mDmgTotal += acidMod;
                        else
                            mDmgTotalDR += Math.abs(acidMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (acidMod > 0)
                            oDmgTotal += acidMod;
                        else
                            oDmgTotalDR += Math.abs(acidMod);
                    }
                }
                
                if (monster.rangeDr !==0) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.rangeDr > 0)
                            mDmgTotalDR += monster.rangeDr;
                        else
                            mDmgTotal += Math.abs(monster.rangeDr);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.rangeDr > 0)
                            oDmgTotalDR += monster.rangeDr;
                        else
                            oDmgTotal += Math.abs(monster.rangeDr);
                    }
                } if (monster.rangePercent !== 1.0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal = Math.round(mDmgTotal * monster.rangePercent);
                    if (oDmgTotal > 0 && isOffDr)
                        oDmgTotal = Math.round(oDmgTotal * monster.rangePercent);
                }  
            } else {
                if (monster.fire !== 0 && +$scope.characterContext.stats.mfire) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.fire > 0)
                            mDmgTotalDR += monster.fire;
                        else
                            mDmgTotal += Math.abs(monster.fire);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.fire > 0)
                            oDmgTotalDR += monster.fire;
                        else
                            oDmgTotal += Math.abs(monster.fire);
                    }  
                } if (monster.firePercent !== 1.0 && +$scope.characterContext.stats.mfire) {
                    var percentMod = (Math.round(monster.firePercent * +$scope.characterContext.stats.mfire) - +$scope.characterContext.stats.mfire);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) {
                            if (monster.monsterEffects.includes("FIRE_HEAL"))
                                mDmgTotal -= percentMod;
                            else
                                mDmgTotal += percentMod;
                        }
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (percentMod > 0) {
                            if (monster.monsterEffects.includes("FIRE_HEAL"))
                                oDmgTotal -= percentMod;
                            else
                                oDmgTotal += percentMod;
                        }
                        else
                            oDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.cold !== 0 && +$scope.characterContext.stats.mcold) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.cold > 0)
                            mDmgTotalDR += monster.cold;
                        else
                            mDmgTotal += Math.abs(monster.cold);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.cold > 0)
                            oDmgTotalDR += monster.cold;
                        else
                            oDmgTotal += Math.abs(monster.cold);
                    }  
                } if (monster.coldPercent !== 1.0 && +$scope.characterContext.stats.mcold) {
                    var percentMod = (Math.round(monster.coldPercent * +$scope.characterContext.stats.mcold) - +$scope.characterContext.stats.mcold);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (percentMod > 0)
                            oDmgTotal += percentMod;
                        else
                            oDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.shock !== 0 && +$scope.characterContext.stats.mshock) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.shock > 0)
                            mDmgTotalDR += monster.shock;
                        else
                            mDmgTotal += Math.abs(monster.shock);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.shock > 0)
                            oDmgTotalDR += monster.shock;
                        else
                            oDmgTotal += Math.abs(monster.shock);
                    }
                } if (monster.shockPercent !== 1.0 && +$scope.characterContext.stats.mshock) {
                    var percentMod = (Math.round(monster.shockPercent * +$scope.characterContext.stats.mshock) - +$scope.characterContext.stats.mshock);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (percentMod > 0)
                            oDmgTotal += percentMod;
                        else
                            oDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.sonic !== 0 && +$scope.characterContext.stats.msonic) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.sonic > 0)
                            mDmgTotalDR += monster.sonic;
                        else
                            mDmgTotal += Math.abs(monster.sonic);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.sonic > 0)
                            oDmgTotalDR += monster.sonic;
                        else
                            oDmgTotal += Math.abs(monster.sonic);
                    }
                } if (monster.sonicPercent !== 1.0 && +$scope.characterContext.stats.msonic) {
                    var percentMod = (Math.round(monster.sonicPercent * +$scope.characterContext.stats.msonic) - +$scope.characterContext.stats.msonic);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (percentMod > 0)
                            oDmgTotal += percentMod;
                        else
                            oDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.poison !== 0 && +$scope.characterContext.stats.mpoison) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.poison > 0)
                            mDmgTotalDR += monster.poison;
                        else
                            mDmgTotal += Math.abs(monster.poison);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.poison > 0)
                            oDmgTotalDR += monster.poison;
                        else
                            oDmgTotal += Math.abs(monster.poison);
                    }   
                } if (monster.poisonPercent !== 1.0 && +$scope.characterContext.stats.mpoison) {
                    var percentMod = (Math.round(monster.poisonPercent * +$scope.characterContext.stats.mpoison) - +$scope.characterContext.stats.mpoison);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (percentMod > 0)
                            oDmgTotal += percentMod;
                        else
                            oDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.darkrift !== 0 && +$scope.characterContext.stats.mdarkrift) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.darkrift > 0)
                            mDmgTotalDR += monster.darkrift;
                        else
                            mDmgTotal += Math.abs(monster.darkrift);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.darkrift > 0)
                            oDmgTotalDR += monster.darkrift;
                        else
                            oDmgTotal += Math.abs(monster.darkrift);
                    } 
                } if (monster.darkriftPercent !== 1.0 && +$scope.characterContext.stats.mdarkrift) {
                     var percentMod = (Math.round(monster.darkriftPercent * +$scope.characterContext.stats.mdarkrift) - +$scope.characterContext.stats.mdarkrift);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (percentMod > 0)
                            oDmgTotal += percentMod;
                        else
                            oDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.sacred !== 0 && +$scope.characterContext.stats.msacred) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.sacred > 0)
                            mDmgTotalDR += monster.sacred;
                        else
                            mDmgTotal += Math.abs(monster.sacred);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.sacred > 0)
                            oDmgTotalDR += monster.sacred;
                        else
                            oDmgTotal += Math.abs(monster.sacred);
                    }   
                } if (monster.sacredPercent !== 1.0 && +$scope.characterContext.stats.msacred) {
                    if (mDmgTotal > 0 && isMainDr) {
                        var sacredMod = (+$scope.characterContext.mainSacredBonus > 0) ? (Math.round(monster.sacredPercent * (+$scope.characterContext.stats.msacred + +$scope.characterContext.mainSacredBonus + mDmg)) - (+$scope.characterContext.stats.msacred + +$scope.characterContext.mainSacredBonus + mDmg)) :
                                (Math.round(monster.sacredPercent * +$scope.characterContext.stats.msacred) - +$scope.characterContext.stats.msacred);
                        if (sacredMod > 0)
                            mDmgTotal += sacredMod;
                        else
                            mDmgTotalDR += Math.abs(sacredMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                         var sacredMod = (+$scope.characterContext.offSacredBonus > 0) ? (Math.round(monster.sacredPercent * (+$scope.characterContext.stats.msacred + +$scope.characterContext.offSacredBonus + mDmg)) - (+$scope.characterContext.stats.msacred + +$scope.characterContext.offSacredBonus + oDmg)) :
                                (Math.round(monster.sacredPercent * +$scope.characterContext.stats.msacred) - +$scope.characterContext.stats.msacred);
                        if (sacredMod > 0)
                            oDmgTotal += sacredMod;
                        else
                            oDmgTotalDR += Math.abs(sacredMod);
                    }
                } if (monster.force !== 0 && +$scope.characterContext.stats.mforce) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.force > 0)
                            mDmgTotalDR += monster.force;
                        else
                            mDmgTotal += Math.abs(monster.force);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.force > 0)
                            oDmgTotalDR += monster.force;
                        else
                            oDmgTotal += Math.abs(monster.force);
                    }  
                } if (monster.forcePercent !== 1.0 && +$scope.characterContext.stats.mforce) {
                     var percentMod = (Math.round(monster.forcePercent * +$scope.characterContext.stats.mforce) - +$scope.characterContext.stats.mforce);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (percentMod > 0)
                            oDmgTotal += percentMod;
                        else
                            oDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.acid !== 0 && +$scope.characterContext.stats.macid) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.acid > 0)
                            mDmgTotalDR += monster.acid;
                        else
                            mDmgTotal += Math.abs(monster.acid);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.acid > 0)
                            oDmgTotalDR += monster.acid;
                        else
                            oDmgTotal += Math.abs(monster.acid);
                    } 
                } if (monster.acidPercent !== 1.0 && +$scope.characterContext.stats.macid) {
                    var percentMod = (Math.round(monster.acidPercent * +$scope.characterContext.stats.macid) - +$scope.characterContext.stats.macid);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (percentMod > 0)
                            oDmgTotal += percentMod;
                        else
                            oDmgTotalDR += Math.abs(percentMod);
                    }
                }
                
                if (monster.meleeDr !==0) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.meleeDr > 0)
                            mDmgTotalDR += monster.meleeDr;
                        else
                            mDmgTotal += Math.abs(monster.meleeDr);
                    }
                    if (oDmgTotal > 0 && isOffDr) {
                        if (monster.meleeDr > 0)
                            oDmgTotalDR += monster.meleeDr;
                        else
                            oDmgTotal += Math.abs(monster.meleeDr);
                    }
                } if (monster.meleePercent !== 1.0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal = Math.round(mDmgTotal * monster.meleePercent);
                    if (oDmgTotal > 0 && isOffDr)
                        oDmgTotal = Math.round(oDmgTotal * monster.meleePercent);
                }
            }
            
            if (mDmgTotal < 0)
                mDmgTotal = 0;
            if (oDmgTotal < 0)
                oDmgTotal = 0;
            
            if ((monster.critical || critImmuneUsed) && hitRoll >= $scope.characterContext.meleeCritMin && !$scope.hasEffect($scope.characterContext.meleeDmgEffects, "NO_DAMAGE_MOD") && !$scope.hasEffect(monster.monsterEffects, "MELEE_MAIN_ON_20")) {
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
                if (buff !== null) {
                    mCritDmg = mCritDmg + mDmgTotal;
                    $scope.removeBuff(buff);
                }
            } else if ($scope.hasBuff("Fury")) {
                var buff = $scope.hasBuff("Fury");
                if (buff !== null) {
                    if ((monster.critical || critImmuneUsed))
                        mCritDmg = mDmgTotal * 2;
                    
                    $scope.removeBuff(buff);
                }
            }
            
            if ((monster.critical || critImmuneUsed) && offhandHitRoll >= $scope.characterContext.meleeCritMin && !$scope.hasEffect($scope.characterContext.meleeDmgEffects, "NO_DAMAGE_MOD") && !$scope.hasEffect(monster.monsterEffects, "MELEE_OFFHAND_ON_20")) {
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
            
            // Apply DR now
            if (mDmgTotalDR > 0) {
                mCritDmg -= mDmgTotalDR;
                mDmgTotal -= mDmgTotalDR;
            } if (oDmgTotalDR > 0) {
                oCritDmg -= oDmgTotalDR;
                oDmgTotal -= oDmgTotalDR;
            }
            if (mDmgTotal < 0)
                mDmgTotal = 0;
            if (oDmgTotal < 0)
                oDmgTotal = 0;
                
            var totalCrit = 0;
            if (mCritDmg > 0 && oCritDmg > 0)
                totalCrit = mCritDmg + oCritDmg;
            else if (mCritDmg > 0) {
                totalCrit = mCritDmg + oDmgTotal;
            } else if (oCritDmg > 0) {
                totalCrit = oCritDmg + mDmgTotal;
            }
            
            var buff = $scope.hasBuff("Righteous Wrath");
            if (buff !== null) 
                $scope.removeBuff(buff);
            
            if ($scope.isFurryThrow) {
                if (monster.rac > hitRollMod) {
                    mDmgTotal = 0;
                    mCritDmg = 0;
                }
                if (monster.rac > offhandHitRollMod) {
                    oDmgTotal = 0;
                    oCritDmg = 0;
                }
                                             
                if ($scope.characterContext.rollerId !== null) {
                    vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                        ' "rolls": [{ "type": "range_main", "dieResult": ' + hitRoll + ', "modifiedResult": ' + hitRollMod + ', "isSuccess": ' + (mDmgTotal > 0) + ', "damage": ' + ((mCritDmg > 0) ? mCritDmg : mDmgTotal) + ' }, ' + 
                        '{ "type": "range_off", "dieResult": ' + offhandHitRoll + ', "modifiedResult": ' + offhandHitRollMod + ', "isSuccess": ' + (oDmgTotal > 0) + ', "damage": ' + ((oCritDmg > 0) ? oCritDmg : oDmgTotal) + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Flurry Throw Attack","result":response.data.errors[0]});       
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast(); 
                        return($q.reject(response));
                    }).then(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Flurry Throw Attack","result":"Flurry throw attack roll sent"});
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast();   
                    });
                } else {
                    if (mDmgTotal === 0 && oDmgTotal === 0) {
                        vtdHistory.add({"type":"ATTACK","sub":"MELEE","isMiss":true,"roll":hitRollMod,"mRoll":hitRollMod,"oRoll":offhandHitRollMod});
                    } else {
                        vtdHistory.add({"type":"ATTACK","sub":"MELEE","isMiss":false,"isCrit":mCritDmg > 0 || oCritDmg > 0,"mRoll":hitRoll,"oRoll":offhandHitRoll,
                            "mRollTotal":hitRollMod,"oRollTotal":offhandHitRollMod,"mWheel":mDmg,"oWheel":oDmg,"mDmg":mDmgTotal,"oDmg":oDmgTotal,"totalDmg":mDmgTotal+oDmgTotal,
                            "mCrit":mCritDmg,"oCrit":oCritDmg,"critTotal":totalCrit,"mWeaponExp":mDmgExp,"sWeaponExp":sDmgExp,"oWeaponExp":oDmgExp,"fire":$scope.characterContext.stats.bfire,
                            "cold":$scope.characterContext.stats.bcold,"shock":$scope.characterContext.stats.bshock,"sonic":$scope.characterContext.stats.bsonic,
                            "eldritch":$scope.characterContext.stats.beldritch,"poison":$scope.characterContext.stats.bpoison,"darkrift":$scope.characterContext.stats.bdarkrift,
                            "sacred":$scope.characterContext.stats.bsacred,"force":$scope.characterContext.stats.bforce,"acid":$scope.characterContext.stats.bacid});
                    }
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast();   
                }
            } else {
                if (monster.mac > hitRollMod) {
                    var buff = $scope.hasBuff("WS Reroll");
                    if (buff !== null && status !== 'ws reroll') {
                        $scope.removeBuff(buff);
                        $scope.rollToHitMelee(monsterIndex, 'ws reroll');
                    }
        
                    mDmgTotal = 0;
                    mCritDmg = 0;
                }
                if (monster.mac > offhandHitRollMod) {
                    oDmgTotal = 0;
                    oCritDmg = 0;
                }
            
                if ($scope.characterContext.rollerId !== null) {
                    if ('meleeOffhandHit' in $scope.characterContext.stats) {
                        vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                            ' "rolls": [{ "type": "melee_main", "dieResult": ' + hitRoll + ', "modifiedResult": ' + hitRollMod + ', "isSuccess": ' + (monster.mac <= hitRollMod) + ', "damage": ' + ((mCritDmg > 0) ? mCritDmg : mDmgTotal) + ' }, ' + 
                            '{ "type": "melee_off", "dieResult": ' + offhandHitRoll + ', "modifiedResult": ' + offhandHitRollMod + ', "isSuccess": ' + (monster.mac <= offhandHitRollMod) + ', "damage": ' + ((oCritDmg > 0) ? oCritDmg : oDmgTotal) + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                            vtdHistory.add({"type":"ROLLER","sub":"Melee Attack","result":response.data.errors[0]});       
                            $scope.history = vtdHistory.get();
                            $scope.lastEvent = vtdHistory.getLast(); 
                            return($q.reject(response));
                        }).then(function(response) {
                            vtdHistory.add({"type":"ROLLER","sub":"Melee Attack","result":"Melee attack roll sent"});
                            $scope.history = vtdHistory.get();
                            $scope.lastEvent = vtdHistory.getLast();   
                        });
                    } else {
                        vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                            ' "rolls": [{ "type": "melee_main", "dieResult": ' + hitRoll + ', "modifiedResult": ' + hitRollMod + ', "isSuccess": ' + (monster.mac <= hitRollMod) + ', "damage": ' + ((mCritDmg > 0) ? mCritDmg : mDmgTotal) + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                            vtdHistory.add({"type":"ROLLER","sub":"Melee Attack","result":response.data.errors[0]});       
                            $scope.history = vtdHistory.get();
                            $scope.lastEvent = vtdHistory.getLast(); 
                            return($q.reject(response));
                        }).then(function(response) {
                            vtdHistory.add({"type":"ROLLER","sub":"Melee Attack","result":"Melee attack roll sent"});
                            $scope.history = vtdHistory.get();
                            $scope.lastEvent = vtdHistory.getLast();   
                        });
                    }
                } else {
                    if (mDmgTotal === 0 && oDmgTotal === 0) {
                        vtdHistory.add({"type":"ATTACK","sub":"MELEE","isMiss":true,"roll":hitRollMod,"mRoll":hitRollMod,"oRoll":offhandHitRollMod});
                    } else {
                        vtdHistory.add({"type":"ATTACK","sub":"MELEE","isMiss":false,"isCrit":mCritDmg > 0 || oCritDmg > 0,"mRoll":hitRoll,"oRoll":offhandHitRoll,
                            "mRollTotal":hitRollMod,"oRollTotal":offhandHitRollMod,"mWheel":mDmg,"oWheel":oDmg,"mDmg":mDmgTotal,"oDmg":oDmgTotal,"totalDmg":mDmgTotal+oDmgTotal,
                            "mCrit":mCritDmg,"oCrit":oCritDmg,"critTotal":totalCrit,"mWeaponExp":mDmgExp,"sWeaponExp":sDmgExp,"oWeaponExp":oDmgExp,"fire":$scope.characterContext.stats.mfire,
                            "cold":$scope.characterContext.stats.mcold,"shock":$scope.characterContext.stats.mshock,"sonic":$scope.characterContext.stats.msonic,
                            "eldritch":$scope.characterContext.stats.meldritch,"poison":$scope.characterContext.stats.mpoison,"darkrift":$scope.characterContext.stats.mdarkrift,
                            "sacred":$scope.characterContext.stats.msacred,"force":$scope.characterContext.stats.mforce,"acid":$scope.characterContext.stats.macid});
                    }
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast();  
                }
            }
        }
    };
    
    $scope.rollToHitRange =  function(monsterIndex) {        
        var hitRoll = 1;
        var offhandHitRoll = 1;
        var hitRollOG = 1;
        var offhandHitRollOG = 1;
        var monster = null;
        var isOffhandAttack = false;
        var isCompanion = ($scope.characterContext.meleeAnimalCompanionDmgRange && $scope.characterContext.meleeAnimalCompanionDmgRange.length > 0);
        var flankUsed = false;
        var statusEffect = '';
        
        $scope.resultIndex = 0;
        
        var buff = $scope.hasBuff("Flank");
        if (buff !== null) {
            $scope.removeBuff(buff);
            var flankUsed = true;
            statusEffect = 'flank buff';
        }
        
        var critImmuneUsed = false;
        var buff2 = $scope.hasBuff("Ignore Sneak & Crit Immune");
        if (buff2 !== null) {
            $scope.removeBuff(buff2);
            critImmuneUsed = true;
        }
        
        if ($scope.characterContext.monsters === null || $scope.characterContext.monsters.length === 0) {
            if (flankUsed) {
                hitRoll = Math.max($scope.getRandomInt(20) + 1, $scope.getRandomInt(20) + 1, $scope.getRandomInt(20) + 1);
                offhandHitRoll = Math.max($scope.getRandomInt(20) + 1, $scope.getRandomInt(20) + 1, $scope.getRandomInt(20) + 1);
            } else {
                hitRoll = $scope.getRandomInt(20) + 1;
                offhandHitRoll = $scope.getRandomInt(20) + 1;
            }
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex === undefined) {
            monsterSelectorSvc.selectMonster($scope.characterContext.monsters, function(index) {
                $scope.rollToHitRange(index);
            });
            return;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex !== undefined) {
            monster = monsterIndex;
            
            if (flankUsed) {
                hitRoll = Math.max(monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)]);
                offhandHitRoll = Math.max(monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)]);
            } else {
                hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
                offhandHitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
            }
        } else {
            monster = $scope.characterContext.monsters[0];
            
            if (flankUsed) {
                hitRoll = Math.max(monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)]);
                offhandHitRoll = Math.max(monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)]);
            } else {
                hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
                offhandHitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)]; 
            }
        }
        
        if (!('rangeOffhandHit' in $scope.characterContext.stats) && !isCompanion) {
            offhandHitRoll = 1;
        } else if (offhandHitRoll !== 20 && monster.monsterEffects && monster.monsterEffects.includes("RANGE_OFFHAND_ON_20")) {
            offhandHitRollOG = offhandHitRoll;
            offhandHitRoll = 1;
        } else if (monster.monsterEffects && monster.monsterEffects.includes("INCORPOREAL")) {
            if (!$scope.hasEffect($scope.characterContext.rangeDmgEffects, "IGNORE_INCORPOREAL") && 
                    !$scope.hasBuff("Ignore Incorporeal") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                offhandHitRollOG = offhandHitRoll;
                offhandHitRoll = 1;
            }
        } else if (monster.monsterEffects && monster.monsterEffects.includes("MISS_50")) {
            if (!$scope.hasBuff("Ignore Miss") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                offhandHitRollOG = offhandHitRoll;
                offhandHitRoll = 1;
            }
        } else
            isOffhandAttack = true;
        
        if (hitRoll !== 20 && monster.monsterEffects && monster.monsterEffects.includes("RANGE_MAIN_ON_20")) {
            hitRollOG = hitRoll;
            hitRoll = 1;
        } else if (monster.monsterEffects && monster.monsterEffects.includes("INCORPOREAL")) {
            if (!$scope.hasEffect($scope.characterContext.rangeDmgEffects, "IGNORE_INCORPOREAL") && 
                    !$scope.hasBuff("Ignore Incorporeal") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                hitRollOG = hitRoll;
                hitRoll = 1;
            }
        } else if (monster.monsterEffects && monster.monsterEffects.includes("MISS_50")) {
            if (!$scope.hasBuff("Ignore Miss") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                hitRollOG = hitRoll;
                hitRoll = 1;
            }
        }     
        
        if (hitRoll === 1 && offhandHitRoll === 1) {
            if ($scope.characterContext.rollerId !== null) {
                if (!('rangeOffhandHit' in $scope.characterContext.stats)) {
                    vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                        ' "rolls": [{ "type": "range_main", "dieResult": 1, "modifiedResult": 1, "isSuccess": false, "damage": 0' + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Range Attack","result":response.data.errors[0]});       
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast(); 
                        return($q.reject(response));
                    }).then(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Range Attack","result":"Range attack roll sent"});
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast();   
                    });
                } else {
                    vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                        ' "rolls": [{ "type": "range_main", "dieResult": 1, "modifiedResult": 1, "isSuccess": false, "damage": 0 }, { "type": "range_off", "dieResult": 1, "modifiedResult": 1, "isSuccess": false, "damage": 0' + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Range Attack","result":response.data.errors[0]});       
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast(); 
                        return($q.reject(response));
                    }).then(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Range Attack","result":"Range attack roll sent"});
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast();   
                    });
                }
            } else {
                vtdHistory.add({"type":"ATTACK","sub":"RANGE","isMiss":true,"roll":1,"mRoll":hitRollOG,"oRoll":offhandHitRollOG,"isOffhandAttack":isOffhandAttack});
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast(); 
            }
        } else {
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
                    else if ($scope.characterContext.rangeWeaponExplodeEffect === "CRIT" && (monster.critical || critImmuneUsed) && hitRoll >= $scope.characterContext.meleeCritMin)
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
                    else if ($scope.characterContext.rangeOffhandWeaponExplodeEffect === "CRIT" && (monster.critical || critImmuneUsed) && hitRoll >= $scope.characterContext.meleeCritMin)
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
            
            var isMainDr = !$scope.hasEffect($scope.characterContext.rangeDmgEffects, "NO_DAMAGE_MOD");
            var isOffDr = !$scope.hasEffect($scope.characterContext.rangeOffhandDmgEffects, "NO_DAMAGE_MOD");
            var mDmgTotal = 0;
            if (hitRoll > 1)
                mDmgTotal = mDmg + mRollDmg + monster.bonusDmg;
            else
                mDmg = 0;
            
            var oDmgTotal = 0;
            if (offhandHitRoll > 1)
                oDmgTotal = oDmg + oRollDmg + monster.bonusDmg;
            else
                oDmg = 0;
            
            var mDmgTotalDR = 0;
            var oDmgTotalDR = 0;
            if (monster.universalDr !==0) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.universalDr > 0)
                        mDmgTotalDR += monster.universalDr;
                    else
                        mDmgTotal += Math.abs(monster.universalDr);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (monster.universalDr > 0)
                        oDmgTotalDR += monster.universalDr;
                    else
                        oDmgTotal += Math.abs(monster.universalDr);
                }
            }
            if (monster.rangeDr !==0) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.rangeDr > 0)
                        mDmgTotalDR += monster.rangeDr;
                    else
                        mDmgTotal += Math.abs(monster.rangeDr);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (monster.rangeDr > 0)
                        oDmgTotalDR += monster.rangeDr;
                    else
                        oDmgTotal += Math.abs(monster.rangeDr);
                }
            } if (monster.rangePercent !== 1.0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal = Math.round(mDmgTotal * monster.rangePercent);
                if (oDmgTotal > 0 && isOffDr)
                    oDmgTotal = Math.round(oDmgTotal * monster.rangePercent);
            }
            
            if (monster.fire !== 0 && +$scope.characterContext.stats.rfire) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.fire > 0)
                        mDmgTotalDR += monster.fire;
                    else
                        mDmgTotal += Math.abs(monster.fire);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (monster.fire > 0)
                        oDmgTotalDR += monster.fire;
                    else
                        oDmgTotal += Math.abs(monster.fire);
                }  
            } if (monster.firePercent !== 1.0 && +$scope.characterContext.stats.rfire) {
                var percentMod = (Math.round(monster.firePercent * +$scope.characterContext.stats.rfire) - +$scope.characterContext.stats.rfire);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) {
                        if (monster.monsterEffects.includes("FIRE_HEAL"))
                            mDmgTotal -= percentMod;
                        else
                            mDmgTotal += percentMod;
                    }
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (percentMod > 0) {
                        if (monster.monsterEffects.includes("FIRE_HEAL"))
                            oDmgTotal -= percentMod;
                        else
                            oDmgTotal += percentMod;
                    }
                    else
                        oDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.cold !== 0 && +$scope.characterContext.stats.rcold) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.cold > 0)
                        mDmgTotalDR += monster.cold;
                    else
                        mDmgTotal += Math.abs(monster.cold);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (monster.cold > 0)
                        oDmgTotalDR += monster.cold;
                    else
                        oDmgTotal += Math.abs(monster.cold);
                }  
            } if (monster.coldPercent !== 1.0 && +$scope.characterContext.stats.rcold) {
                var percentMod = (Math.round(monster.coldPercent * +$scope.characterContext.stats.mcold) - +$scope.characterContext.stats.mcold);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (percentMod > 0)
                        oDmgTotal += percentMod;
                    else
                        oDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.shock !== 0 && +$scope.characterContext.stats.rshock) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.shock > 0)
                        mDmgTotalDR += monster.shock;
                    else
                        mDmgTotal += Math.abs(monster.shock);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (monster.shock > 0)
                        oDmgTotalDR += monster.shock;
                    else
                        oDmgTotal += Math.abs(monster.shock);
                }
            } if (monster.shockPercent !== 1.0 && +$scope.characterContext.stats.rshock) {
                var percentMod = (Math.round(monster.shockPercent * +$scope.characterContext.stats.rshock) - +$scope.characterContext.stats.rshock);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (percentMod > 0)
                        oDmgTotal += percentMod;
                    else
                        oDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.sonic !== 0 && +$scope.characterContext.stats.rsonic) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.sonic > 0)
                        mDmgTotalDR += monster.sonic;
                    else
                        mDmgTotal += Math.abs(monster.sonic);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (monster.sonic > 0)
                        oDmgTotalDR += monster.sonic;
                    else
                        oDmgTotal += Math.abs(monster.sonic);
                }
            } if (monster.sonicPercent !== 1.0 && +$scope.characterContext.stats.rsonic) {
                var percentMod = (Math.round(monster.sonicPercent * +$scope.characterContext.stats.rsonic) - +$scope.characterContext.stats.rsonic);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (percentMod > 0)
                        oDmgTotal += percentMod;
                    else
                        oDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.poison !== 0 && +$scope.characterContext.stats.rpoison) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.poison > 0)
                        mDmgTotalDR += monster.poison;
                    else
                        mDmgTotal += Math.abs(monster.poison);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (monster.poison > 0)
                        oDmgTotalDR += monster.poison;
                    else
                        oDmgTotal += Math.abs(monster.poison);
                }   
            } if (monster.poisonPercent !== 1.0 && +$scope.characterContext.stats.rpoison) {
                var percentMod = (Math.round(monster.poisonPercent * +$scope.characterContext.stats.rpoison) - +$scope.characterContext.stats.rpoison);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (percentMod > 0)
                        oDmgTotal += percentMod;
                    else
                        oDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.darkrift !== 0 && +$scope.characterContext.stats.rdarkrift) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.darkrift > 0)
                        mDmgTotalDR += monster.darkrift;
                    else
                        mDmgTotal += Math.abs(monster.darkrift);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (monster.darkrift > 0)
                        oDmgTotalDR += monster.darkrift;
                    else
                        oDmgTotal += Math.abs(monster.darkrift);
                } 
            } if (monster.darkriftPercent !== 1.0 && +$scope.characterContext.stats.rdarkrift) {
                 var percentMod = (Math.round(monster.darkriftPercent * +$scope.characterContext.stats.rdarkrift) - +$scope.characterContext.stats.rdarkrift);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (percentMod > 0)
                        oDmgTotal += percentMod;
                    else
                        oDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.sacred !== 0 && +$scope.characterContext.stats.rsacred) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.sacred > 0)
                        mDmgTotalDR += monster.sacred;
                    else
                        mDmgTotal += Math.abs(monster.sacred);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (monster.sacred > 0)
                        oDmgTotalDR += monster.sacred;
                    else
                        oDmgTotal += Math.abs(monster.sacred);
                }   
            } if (monster.sacredPercent !== 1.0 && +$scope.characterContext.stats.rsacred) {
                var percentMod = (Math.round(monster.sacredPercent * +$scope.characterContext.stats.rsacred) - +$scope.characterContext.stats.rsacred);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (percentMod > 0)
                        oDmgTotal += percentMod;
                    else
                        oDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.force !== 0 && +$scope.characterContext.stats.rforce) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.force > 0)
                        mDmgTotalDR += monster.force;
                    else
                        mDmgTotal += Math.abs(monster.force);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (monster.force > 0)
                        oDmgTotalDR += monster.force;
                    else
                        oDmgTotal += Math.abs(monster.force);
                }  
            } if (monster.forcePercent !== 1.0 && +$scope.characterContext.stats.rforce) {
                 var percentMod = (Math.round(monster.forcePercent * +$scope.characterContext.stats.rforce) - +$scope.characterContext.stats.rforce);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (percentMod > 0)
                        oDmgTotal += percentMod;
                    else
                        oDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.acid !== 0 && +$scope.characterContext.stats.racid) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.acid > 0)
                        mDmgTotalDR += monster.acid;
                    else
                        mDmgTotal += Math.abs(monster.acid);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (monster.acid > 0)
                        oDmgTotalDR += monster.acid;
                    else
                        oDmgTotal += Math.abs(monster.acid);
                } 
            } if (monster.acidPercent !== 1.0 && +$scope.characterContext.stats.racid) {
                var percentMod = (Math.round(monster.acidPercent * +$scope.characterContext.stats.racid) - +$scope.characterContext.stats.racid);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
                if (oDmgTotal > 0 && isOffDr) {
                    if (percentMod > 0)
                        oDmgTotal += percentMod;
                    else
                        oDmgTotalDR += Math.abs(percentMod);
                }
            }
            
            if (mDmgTotal < 0)
                mDmgTotal = 0;
            if (oDmgTotal < 0)
                oDmgTotal = 0;
            
            if ((monster.critical || critImmuneUsed) && hitRoll >= $scope.characterContext.rangeCritMin && !$scope.hasEffect($scope.characterContext.rangeDmgEffects, "NO_DAMAGE_MOD") && !$scope.hasEffect(monster.monsterEffects, "RANGE_MAIN_ON_20")) {
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
            
            if ((monster.critical || critImmuneUsed) && offhandHitRoll >= $scope.characterContext.rangeCritMin && !$scope.hasEffect($scope.characterContext.rangeOffhandDmgEffects, "NO_DAMAGE_MOD") && !$scope.hasEffect(monster.monsterEffects, "RANGE_OFFHAND_ON_20")) {
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
            
            // Apply DR now
            if (mDmgTotalDR > 0) {
                mCritDmg -= mDmgTotalDR;
                mDmgTotal -= mDmgTotalDR;
            } if (oDmgTotalDR > 0) {
                oCritDmg -= oDmgTotalDR;
                oDmgTotal -= oDmgTotalDR;
            }
            if (mDmgTotal < 0)
                mDmgTotal = 0;
            if (oDmgTotal < 0)
                oDmgTotal = 0;
            
            var totalCrit = 0;
            if (mCritDmg > 0 && oCritDmg > 0)
                totalCrit = mCritDmg + oCritDmg;
            else if (mCritDmg > 0) {
                totalCrit = mCritDmg + oDmgTotal;
            } else if (oCritDmg > 0) {
                totalCrit = oCritDmg + mDmgTotal;
            }
            
            var buff = $scope.hasBuff("Righteous Wrath");
            if (buff !== null) 
                $scope.removeBuff(buff);
            
            if (monster.rac > hitRollMod) {
                mDmgTotal = 0;
                mCritDmg = 0;
            }
            if (monster.rac > offhandHitRollMod) {
                oDmgTotal = 0;
                oCritDmg = 0;
            }

            if ($scope.characterContext.rollerId !== null) {
                if ('rangeOffhandHit' in $scope.characterContext.stats) {
                    vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                        ' "rolls": [{ "type": "range_main", "dieResult": ' + hitRoll + ', "modifiedResult": ' + hitRollMod + ', "isSuccess": ' + (monster.rac <= hitRollMod) + ', "damage": ' + ((mCritDmg > 0) ? mCritDmg : mDmgTotal) + ' }, ' + 
                        '{ "type": "range_off", "dieResult": ' + offhandHitRoll + ', "modifiedResult": ' + offhandHitRollMod + ', "isSuccess": ' + (monster.rac <= offhandHitRollMod) + ', "damage": ' + ((oCritDmg > 0) ? oCritDmg : oDmgTotal) + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Range Attack","result":response.data.errors[0]});       
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast(); 
                        return($q.reject(response));
                    }).then(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Range Attack","result":"Range attack roll sent"});
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast();   
                    });
                } else {
                    vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                        ' "rolls": [{ "type": "range_main", "dieResult": ' + hitRoll + ', "modifiedResult": ' + hitRollMod + ', "isSuccess": ' + (monster.rac <= hitRollMod) + ', "damage": ' + ((mCritDmg > 0) ? mCritDmg : mDmgTotal) + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Range Attack","result":response.data.errors[0]});       
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast(); 
                        return($q.reject(response));
                    }).then(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Range Attack","result":"Range attack roll sent"});
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast();   
                    });
                }
            } else {
                if (mDmgTotal === 0 && oDmgTotal === 0) {
                    vtdHistory.add({"type":"ATTACK","sub":"RANGE","isMiss":true,"roll":hitRollMod,"mRoll":hitRollMod,"oRoll":offhandHitRollMod});
                } else {
                    vtdHistory.add({"type":"ATTACK","sub":"RANGE","isMiss":false,"isCrit":mCritDmg > 0 || oCritDmg > 0,"mRoll":hitRoll,"oRoll":offhandHitRoll,
                        "mRollTotal":hitRollMod,"oRollTotal":offhandHitRollMod,"mWheel":mDmg,"oWheel":oDmg,"mDmg":mDmgTotal,"oDmg":oDmgTotal,"totalDmg":mDmgTotal+oDmgTotal,
                        "mCrit":mCritDmg,"oCrit":oCritDmg,"critTotal":totalCrit,"mWeaponExp":mDmgExp,"oWeaponExp":oDmgExp,"fire":$scope.characterContext.stats.rfire,
                        "cold":$scope.characterContext.stats.rcold,"shock":$scope.characterContext.stats.rshock,"sonic":$scope.characterContext.stats.rsonic,
                        "eldritch":$scope.characterContext.stats.reldritch,"poison":$scope.characterContext.stats.rpoison,"darkrift":$scope.characterContext.stats.rdarkrift,
                        "sacred":$scope.characterContext.stats.rsacred,"force":$scope.characterContext.stats.rforce,"acid":$scope.characterContext.stats.racid,"isOffhandAttack":isOffhandAttack});
                }
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast(); 
            }
        }
    };
    
    $scope.rollToHitPoly =  function(monsterIndex) {        
        var hitRoll = 1;
        var hitRollOG = 1;
        var monster = null;
        
        if (!($scope.characterContext.meleePolyDmgRange && $scope.characterContext.meleePolyDmgRange.length > 0)) {
            warnDialogSvc.showMessage("Polymorph Seelection Required", "You must select your ploymorph form before attacking.");
            return;
        }
        
        var flankUsed = false;
        var statusEffect = '';
        
        $scope.resultIndex = 0;
        
        var buff = $scope.hasBuff("Flank");
        if (buff !== null) {
            $scope.removeBuff(buff);
            var flankUsed = true;
            statusEffect = 'flank buff';
        }
        
        var critImmuneUsed = false;
        var buff2 = $scope.hasBuff("Ignore Sneak & Crit Immune");
        if (buff2 !== null) {
            $scope.removeBuff(buff2);
            critImmuneUsed = true;
        }
        
        if ($scope.characterContext.monsters === null || $scope.characterContext.monsters.length === 0) {
            if (flankUsed) {
                hitRoll = Math.max($scope.getRandomInt(20) + 1, $scope.getRandomInt(20) + 1, $scope.getRandomInt(20) + 1);
            } else {
                hitRoll = $scope.getRandomInt(20) + 1;
            }
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex === undefined) {
            monsterSelectorSvc.selectMonster($scope.characterContext.monsters, function(index) {
                $scope.rollToHitPoly(index);
            });
            return;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex !== undefined) {
            monster = monsterIndex;
            if (flankUsed) {
                hitRoll = Math.max(monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)]);
            } else {
                hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
            }
        } else {
            monster = $scope.characterContext.monsters[0];
            if (flankUsed) {
                hitRoll = Math.max(monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)], monster.roller[$scope.getRandomInt(monster.roller.length)]);
            } else {
                hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
            }
        }
        
        if (hitRoll !== 20 && monster.monsterEffects && monster.monsterEffects.includes("MELEE_MAIN_ON_20")) {
            hitRollOG = hitRoll;
            hitRoll = 1;
        } else if (monster.monsterEffects && monster.monsterEffects.includes("INCORPOREAL")) {
            if (!$scope.hasEffect($scope.characterContext.meleeDmgEffects, "IGNORE_INCORPOREAL") && 
                    !$scope.hasBuff("Ignore Incorporeal") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                hitRollOG = hitRoll;
                hitRoll = 1;
            }
        } else if (monster.monsterEffects && monster.monsterEffects.includes("MISS_50")) {
            if (!$scope.hasBuff("Ignore Miss") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                hitRollOG = hitRoll;
                hitRoll = 1;
            }
        } 
        
        if (hitRoll === 1) {
            if ($scope.characterContext.rollerId !== null) {
                vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                    ' "rolls": [{ "type": "melee_main", "dieResult": 1, "modifiedResult": 1, "isSuccess": false, "damage": 0' + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Polymorph Attack","result":response.data.errors[0]});       
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast(); 
                    return($q.reject(response));
                }).then(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Polymorph Attack","result":"Polymorph attack roll sent"});
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast();   
                });
            } else {
                vtdHistory.add({"type":"ATTACK","sub":"POLY","isMiss":true,"roll":1,"mRoll":hitRollOG});
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast(); 
            }
        } else {
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
            
            var isMainDr = !$scope.hasEffect($scope.characterContext.meleePolyDmgEffects, "NO_DAMAGE_MOD");
            var mDmgTotal = 0;
            if (hitRoll > 1)
                mDmgTotal = mDmg + rollDmg;
            else
                mDmg = 0;
            
            var mDmgTotalDR = 0;
            if (monster.universalDr !==0) {
                if (mDmgTotal > 0 && isMainDr && !($scope.characterContext.poly.name === "Iktomi’s Shaper Necklace - Ice" && monster.monsterEffects.includes("COLD_PIERCE"))) {
                    if (monster.universalDr > 0)
                        mDmgTotalDR += monster.universalDr;
                    else
                        mDmgTotal += Math.abs(monster.universalDr);
                }
            }
            if (monster.meleeDr !==0) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.meleeDr > 0)
                        mDmgTotalDR += monster.meleeDr;
                    else
                        mDmgTotal += Math.abs(monster.meleeDr);
                }
            } if (monster.meleePercent !== 1.0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal = Math.round(mDmgTotal * monster.meleePercent);
            }
            
            var allFire = false;
            var allCold = false;
            var allShock = false;
            var allSonic = false;
            if ($scope.characterContext.poly.name === "Iktomi’s Shaper Necklace - Fire" || $scope.characterContext.poly.name === "Shaman’s Greater Necklace - Fire") {
                if (monster.fire !== 0) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.fire > 0) {
                            if (monster.monsterEffects.includes("FIRE_HEAL"))
                                mDmgTotal -= monster.fire;
                            else
                                mDmgTotalDR += monster.fire;
                        }
                        else
                            mDmgTotal += Math.abs(monster.fire);
                    }  
                } if (monster.firePercent !== 1.0) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.monsterEffects.includes("FIRE_HEAL"))
                            mDmgTotal = -1*Math.round(monster.firePercent * mDmgTotal);
                        else
                            mDmgTotal = Math.round(monster.firePercent * mDmgTotal);
                    }
                }
                allFire = true;
            } else if ($scope.characterContext.poly.name === "Iktomi’s Shaper Necklace - Ice" || $scope.characterContext.poly.name === "Shaman’s Greater Necklace - Ice") {
                if (monster.cold !== 0) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.cold > 0) 
                            mDmgTotalDR += monster.cold;                     
                        else
                            mDmgTotal += Math.abs(monster.cold);
                    }  
                } if (monster.coldPercent !== 1.0) {
                    if (mDmgTotal > 0 && isMainDr) 
                        mDmgTotal = Math.round(monster.coldPercent * mDmgTotal);
                }
                allCold = true;
            } else if ($scope.characterContext.poly.name === "Iktomi’s Shaper Necklace - Air" || $scope.characterContext.poly.name === "Shaman’s Greater Necklace - Air") {
                 if (monster.shock !== 0) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.shock > 0) 
                            mDmgTotalDR += monster.shock;                     
                        else
                            mDmgTotal += Math.abs(monster.shock);
                    }  
                } if (monster.shockPercent !== 1.0) {
                    if (mDmgTotal > 0 && isMainDr) 
                        mDmgTotal = Math.round(monster.shockPercent * mDmgTotal);
                }
                allShock = true;
            } else if ($scope.characterContext.poly.name === "Iktomi’s Shaper Necklace - Earth" || $scope.characterContext.poly.name === "Shaman’s Greater Necklace - Earth") {
                if (monster.sonic !== 0) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.sonic > 0) 
                            mDmgTotalDR += monster.sonic;                     
                        else
                            mDmgTotal += Math.abs(monster.sonic);
                    }  
                } if (monster.sonicPercent !== 1.0) {
                    if (mDmgTotal > 0 && isMainDr) 
                        mDmgTotal = Math.round(monster.sonicPercent * mDmgTotal);
                }
                allSonic = true;
            } else {
                if (monster.fire !== 0 && +$scope.characterContext.stats.mfire) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.fire > 0)
                            mDmgTotalDR += monster.fire;
                        else
                            mDmgTotal += Math.abs(monster.fire);
                    }
                } if (monster.firePercent !== 1.0 && +$scope.characterContext.stats.mfire) {
                    var percentMod = (Math.round(monster.firePercent * +$scope.characterContext.stats.mfire) - +$scope.characterContext.stats.mfire);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) {
                            if (monster.monsterEffects.includes("FIRE_HEAL"))
                                mDmgTotal -= percentMod;
                            else
                                mDmgTotal += percentMod;
                        }
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.cold !== 0 && +$scope.characterContext.stats.mcold) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.cold > 0)
                            mDmgTotalDR += monster.cold;
                        else
                            mDmgTotal += Math.abs(monster.cold);
                    } 
                } if (monster.coldPercent !== 1.0 && +$scope.characterContext.stats.mcold) {
                    var percentMod = (Math.round(monster.coldPercent * +$scope.characterContext.stats.mcold) - +$scope.characterContext.stats.mcold);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.shock !== 0 && +$scope.characterContext.stats.mshock) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.shock > 0)
                            mDmgTotalDR += monster.shock;
                        else
                            mDmgTotal += Math.abs(monster.shock);
                    }
                } if (monster.shockPercent !== 1.0 && +$scope.characterContext.stats.mshock) {
                    var percentMod = (Math.round(monster.shockPercent * +$scope.characterContext.stats.mshock) - +$scope.characterContext.stats.mshock);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.sonic !== 0 && +$scope.characterContext.stats.msonic) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.sonic > 0)
                            mDmgTotalDR += monster.sonic;
                        else
                            mDmgTotal += Math.abs(monster.sonic);
                    }
                } if (monster.sonicPercent !== 1.0 && +$scope.characterContext.stats.msonic) {
                    var percentMod = (Math.round(monster.sonicPercent * +$scope.characterContext.stats.msonic) - +$scope.characterContext.stats.msonic);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.poison !== 0 && +$scope.characterContext.stats.mpoison) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.poison > 0)
                            mDmgTotalDR += monster.poison;
                        else
                            mDmgTotal += Math.abs(monster.poison);
                    } 
                } if (monster.poisonPercent !== 1.0 && +$scope.characterContext.stats.mpoison) {
                    var percentMod = (Math.round(monster.poisonPercent * +$scope.characterContext.stats.mpoison) - +$scope.characterContext.stats.mpoison);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.darkrift !== 0 && +$scope.characterContext.stats.mdarkrift) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.darkrift > 0)
                            mDmgTotalDR += monster.darkrift;
                        else
                            mDmgTotal += Math.abs(monster.darkrift);
                    }
                } if (monster.darkriftPercent !== 1.0 && +$scope.characterContext.stats.mdarkrift) {
                     var percentMod = (Math.round(monster.darkriftPercent * +$scope.characterContext.stats.mdarkrift) - +$scope.characterContext.stats.mdarkrift);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.sacred !== 0 && +$scope.characterContext.stats.msacred) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.sacred > 0)
                            mDmgTotalDR += monster.sacred;
                        else
                            mDmgTotal += Math.abs(monster.sacred);
                    }  
                } if (monster.sacredPercent !== 1.0 && +$scope.characterContext.stats.msacred) {
                    var percentMod = (Math.round(monster.sacredPercent * +$scope.characterContext.stats.msacred) - +$scope.characterContext.stats.msacred);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.force !== 0 && +$scope.characterContext.stats.mforce) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.force > 0)
                            mDmgTotalDR += monster.force;
                        else
                            mDmgTotal += Math.abs(monster.force);
                    } 
                } if (monster.forcePercent !== 1.0 && +$scope.characterContext.stats.mforce) {
                     var percentMod = (Math.round(monster.forcePercent * +$scope.characterContext.stats.mforce) - +$scope.characterContext.stats.mforce);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.acid !== 0 && +$scope.characterContext.stats.macid) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.acid > 0)
                            mDmgTotalDR += monster.acid;
                        else
                            mDmgTotal += Math.abs(monster.acid);
                    }
                } if (monster.acidPercent !== 1.0 && +$scope.characterContext.stats.macid) {
                    var percentMod = (Math.round(monster.acidPercent * +$scope.characterContext.stats.macid) - +$scope.characterContext.stats.macid);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                }
            }
            
            if (mDmgTotal < 0 && !monster.monsterEffects.includes("FIRE_HEAL"))
                mDmgTotal = 0;

            var eleDmg = mDmg + rollDmg;
            if (mDmgTotal > eleDmg)
                eleDmg = mDmgTotal;
            if ((monster.critical || critImmuneUsed) && hitRoll >= $scope.characterContext.meleePolyCritMin && !$scope.hasEffect($scope.characterContext.meleePolyDmgEffects, "NO_DAMAGE_MOD") && !$scope.hasEffect(monster.monsterEffects, "MELEE_MAIN_ON_20")) {
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
            
             // Apply DR now
            if (mDmgTotalDR > 0) {
                mCritDmg -= mDmgTotalDR;
                mDmgTotal -= mDmgTotalDR;
                eleDmg -= mDmgTotalDR;
            } 
            if (mDmgTotal < 0)
                mDmgTotal = 0;
            if (mCritDmg < 0)
                mCritDmg = 0;
            if (eleDmg < 0)
                eleDmg = 0;
            
            if (monster.mac > hitRollMod) {
                mDmgTotal = 0;
                mCritDmg = 0;
                eleDmg = 0;
            }

            if ($scope.characterContext.rollerId !== null) {
                vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                    ' "rolls": [{ "type": "melee_main", "dieResult": ' + hitRoll + ', "modifiedResult": ' + hitRollMod + ', "isSuccess": ' + (monster.mac <= hitRollMod) + ', "damage": ' + ((mCritDmg > 0) ? mCritDmg : mDmgTotal) + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Polymorph Attack","result":response.data.errors[0]});       
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast(); 
                    return($q.reject(response));
                }).then(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Polymorph Attack","result":"Polymorph attack roll sent"});
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast();   
                });
            } else {
                if (mDmgTotal === 0) {
                    vtdHistory.add({"type":"ATTACK","sub":"POLY","isMiss":true,"roll":hitRollMod,"mRoll":hitRollMod});
                } else {
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
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast(); 
                }
            }
        } 
    };
    
    $scope.rollToHitAnimalCompanion =  function(monsterIndex) {        
        var hitRoll = 1;
        var monster = null;
        
        if (!($scope.characterContext.animalCompanion && $scope.characterContext.animalCompanion.dmgRange && $scope.characterContext.animalCompanion.dmgRange.length > 0)) {
            warnDialogSvc.showMessage("Animal Companion Seelection Required", "You must select your animal companion form before attacking.");
            return;
        }
        
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
        
        if (hitRoll === 1) {
            vtdHistory.add({"type":"ATTACK","sub":"POLY","isMiss":true,"roll":1});
        } else {
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
        var hitRollOG = 1;
        var monster = null;
        var statusEffect = 'sneak dmg';
        
        $scope.resultIndex = 0;
        
        var critImmuneUsed = false;
        var buff2 = $scope.hasBuff("Ignore Sneak & Crit Immune");
        if (buff2 !== null) {
            $scope.removeBuff(buff2);
            critImmuneUsed = true;
        }
        
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
        
        if (hitRoll !== 20 && monster.monsterEffects && monster.monsterEffects.includes("MELEE_MAIN_ON_20")) {
            hitRollOG = hitRoll;
            hitRoll = 1;
        } else if (monster.monsterEffects && monster.monsterEffects.includes("INCORPOREAL")) {
            if (!$scope.hasEffect($scope.characterContext.meleeDmgEffects, "IGNORE_INCORPOREAL") && 
                    !$scope.hasBuff("Ignore Incorporeal") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                hitRollOG = hitRoll;
                hitRoll = 1;
            }
        } else if (monster.monsterEffects && monster.monsterEffects.includes("MISS_50")) {
            if (!$scope.hasBuff("Ignore Miss") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                hitRollOG = hitRoll;
                hitRoll = 1;
            }
        }  
 
        if (monster !== null && !monster.sneak && !critImmuneUsed) {  
            if ($scope.characterContext.rollerId !== null) {
                vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                    ' "rolls": [{ "type": "melee_main", "dieResult": 1, "modifiedResult": 1, "isSuccess": false, "damage": 0' + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Sneak Attack","result":response.data.errors[0]});       
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast(); 
                    return($q.reject(response));
                }).then(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Sneak Attack","result":"Your sneak attack failed to find any vulnerable weak spots"});
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast();   
                });
            } else {
                vtdHistory.add({"type":"ATTACK","sub":"MELEE_SNEAK","isImmune":true,"immuneText":"Your sneak attack failed to find any vulnerable weak spots"});
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast();
            }
        } else if (hitRoll === 1) {
            if ($scope.characterContext.rollerId !== null) {
                vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                    ' "rolls": [{ "type": "melee_main", "dieResult": 1, "modifiedResult": 1, "isSuccess": false, "damage": 0' + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Sneak Attack","result":response.data.errors[0]});       
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast(); 
                    return($q.reject(response));
                }).then(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Sneak Attack","result":"Sneak attack roll sent"});
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast();   
                });
            } else {
                vtdHistory.add({"type":"ATTACK","sub":"MELEE_SNEAK","isImmune":false,"isMiss":true,"roll":1,"mRoll":hitRollOG});
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast();
            }
        } else {
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
                    else if ($scope.characterContext.meleeWeaponExplodeEffect === "CRIT" && (monster.critical || critImmuneUsed) && hitRoll >= $scope.characterContext.meleeCritMin)
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
                    else if ($scope.characterContext.meleeWeaponSecondaryExplodeEffect === "CRIT" && (monster.critical || critImmuneUsed) && hitRoll >= $scope.characterContext.meleeCritMin)
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
            
            var isMainDr = !$scope.hasEffect($scope.characterContext.meleeDmgEffects, "NO_DAMAGE_MOD");
            var mDmgTotal = 0;
            if (hitRoll > 1)
                mDmgTotal = mDmg + rollDmg;
            else
                mDmg = 0;
           
            var mDmgTotalDR = 0;
            if (monster.universalDr !==0) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.universalDr > 0)
                        mDmgTotalDR += monster.universalDr;
                    else
                        mDmgTotal += Math.abs(monster.universalDr);
                }
            }
            if (monster.meleeDr !==0) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.meleeDr > 0)
                        mDmgTotalDR += monster.meleeDr;
                    else
                        mDmgTotal += Math.abs(monster.meleeDr);
                }
            } if (monster.meleePercent !== 1.0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal = Math.round(mDmgTotal * monster.meleePercent);
            }
            
            if (monster.fire !== 0 && +$scope.characterContext.stats.mfire) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.fire > 0)
                        mDmgTotalDR += monster.fire;
                    else
                        mDmgTotal += Math.abs(monster.fire);
                }
            } if (monster.firePercent !== 1.0 && +$scope.characterContext.stats.mfire) {
                var percentMod = (Math.round(monster.firePercent * +$scope.characterContext.stats.mfire) - +$scope.characterContext.stats.mfire);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) {
                        if (monster.monsterEffects.includes("FIRE_HEAL"))
                            mDmgTotal -= percentMod;
                        else
                            mDmgTotal += percentMod;
                    }
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.cold !== 0 && +$scope.characterContext.stats.mcold) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.cold > 0)
                        mDmgTotalDR += monster.cold;
                    else
                        mDmgTotal += Math.abs(monster.cold);
                } 
            } if (monster.coldPercent !== 1.0 && +$scope.characterContext.stats.mcold) {
                var percentMod = (Math.round(monster.coldPercent * +$scope.characterContext.stats.mcold) - +$scope.characterContext.stats.mcold);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.shock !== 0 && +$scope.characterContext.stats.mshock) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.shock > 0)
                        mDmgTotalDR += monster.shock;
                    else
                        mDmgTotal += Math.abs(monster.shock);
                }
            } if (monster.shockPercent !== 1.0 && +$scope.characterContext.stats.mshock) {
                var percentMod = (Math.round(monster.shockPercent * +$scope.characterContext.stats.mshock) - +$scope.characterContext.stats.mshock);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.sonic !== 0 && +$scope.characterContext.stats.msonic) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.sonic > 0)
                        mDmgTotalDR += monster.sonic;
                    else
                        mDmgTotal += Math.abs(monster.sonic);
                }
            } if (monster.sonicPercent !== 1.0 && +$scope.characterContext.stats.msonic) {
                var percentMod = (Math.round(monster.sonicPercent * +$scope.characterContext.stats.msonic) - +$scope.characterContext.stats.msonic);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.poison !== 0 && +$scope.characterContext.stats.mpoison) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.poison > 0)
                        mDmgTotalDR += monster.poison;
                    else
                        mDmgTotal += Math.abs(monster.poison);
                } 
            } if (monster.poisonPercent !== 1.0 && +$scope.characterContext.stats.mpoison) {
                var percentMod = (Math.round(monster.poisonPercent * +$scope.characterContext.stats.mpoison) - +$scope.characterContext.stats.mpoison);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.darkrift !== 0 && +$scope.characterContext.stats.mdarkrift) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.darkrift > 0)
                        mDmgTotalDR += monster.darkrift;
                    else
                        mDmgTotal += Math.abs(monster.darkrift);
                }
            } if (monster.darkriftPercent !== 1.0 && +$scope.characterContext.stats.mdarkrift) {
                 var percentMod = (Math.round(monster.darkriftPercent * +$scope.characterContext.stats.mdarkrift) - +$scope.characterContext.stats.mdarkrift);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.sacred !== 0 && +$scope.characterContext.stats.msacred) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.sacred > 0)
                        mDmgTotalDR += monster.sacred;
                    else
                        mDmgTotal += Math.abs(monster.sacred);
                }  
            } if (monster.sacredPercent !== 1.0 && +$scope.characterContext.stats.msacred) {
                if (mDmgTotal > 0 && isMainDr) {
                    var sacredMod = (+$scope.characterContext.mainSacredBonus > 0) ? (Math.round(monster.sacredPercent * (+$scope.characterContext.stats.msacred + +$scope.characterContext.mainSacredBonus + mDmg)) - (+$scope.characterContext.stats.msacred + +$scope.characterContext.mainSacredBonus + mDmg)) :
                            (Math.round(monster.sacredPercent * +$scope.characterContext.stats.msacred) - +$scope.characterContext.stats.msacred);
                    if (sacredMod > 0)
                        mDmgTotal += sacredMod;
                    else
                        mDmgTotalDR += Math.abs(sacredMod);
                }
            } if (monster.force !== 0 && +$scope.characterContext.stats.mforce) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.force > 0)
                        mDmgTotalDR += monster.force;
                    else
                        mDmgTotal += Math.abs(monster.force);
                } 
            } if (monster.forcePercent !== 1.0 && +$scope.characterContext.stats.mforce) {
                 var percentMod = (Math.round(monster.forcePercent * +$scope.characterContext.stats.mforce) - +$scope.characterContext.stats.mforce);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
            } if (monster.acid !== 0 && +$scope.characterContext.stats.macid) {
                if (mDmgTotal > 0 && isMainDr) {
                    if (monster.acid > 0)
                        mDmgTotalDR += monster.acid;
                    else
                        mDmgTotal += Math.abs(monster.acid);
                }
            } if (monster.acidPercent !== 1.0 && +$scope.characterContext.stats.macid) {
                var percentMod = (Math.round(monster.acidPercent * +$scope.characterContext.stats.macid) - +$scope.characterContext.stats.macid);
                if (mDmgTotal > 0 && isMainDr) {
                    if (percentMod > 0) 
                        mDmgTotal += percentMod;
                    else
                        mDmgTotalDR += Math.abs(percentMod);
                }
            }
           
            if (mDmgTotal < 0)
                mDmgTotal = 0;
            
            if (hitRoll > 1) {
                if ((monster.critical || critImmuneUsed) && hitRoll >= $scope.characterContext.meleeSneakCritMin && !$scope.hasEffect($scope.characterContext.meleeDmgEffects, "NO_DAMAGE_MOD") && !$scope.hasEffect(monster.monsterEffects, "MELEE_MAIN_ON_20")) {
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
                
                if (mCritDmg > 0)
                    mCritDmg += $scope.characterContext.unmodifiableSneakDamage;
                mDmgTotal += $scope.characterContext.unmodifiableSneakDamage;
                
                // Apply DR now
                if (mDmgTotalDR > 0) {
                    mCritDmg -= mDmgTotalDR;
                    mDmgTotal -= mDmgTotalDR;
                } 
                if (mDmgTotal < 0)
                    mDmgTotal = 0;
                if (mCritDmg < 0)
                    mCritDmg = 0;
            }
            
            if (monster.mac > hitRollMod) {
                mDmgTotal = 0;
                mCritDmg = 0;
            }

            if ($scope.characterContext.rollerId !== null) {
                vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                    ' "rolls": [{ "type": "melee_main", "dieResult": ' + hitRoll + ', "modifiedResult": ' + hitRollMod + ', "isSuccess": ' + (monster.mac <= hitRollMod) + ', "damage": ' + ((mCritDmg > 0) ? mCritDmg : mDmgTotal) + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Sneak Attack","result":response.data.errors[0]});       
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast(); 
                    return($q.reject(response));
                }).then(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Sneak Attack","result":"Sneak attack roll sent"});
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast();   
                });
            } else {
                if (mDmgTotal === 0) {
                    vtdHistory.add({"type":"ATTACK","sub":"MELEE_SNEAK","isMiss":true,"roll":hitRollMod,"mRoll":hitRollMod});
                } else {
                    vtdHistory.add({"type":"ATTACK","sub":"MELEE_SNEAK","isImmune":false,"isMiss":false,"isCrit":mCritDmg > 0,"mRoll":hitRoll,"mRollTotal":hitRollMod,
                        "mWheel":mDmg,"mDmg":mDmgTotal,"totalDmg":mDmgTotal,"mCrit":mCritDmg,"critTotal":mCritDmg,"mWeaponExp":mDmgExp,"sWeaponExp":sDmgExp,"fire":$scope.characterContext.stats.mfire,
                        "cold":$scope.characterContext.stats.mcold,"shock":$scope.characterContext.stats.mshock,"sonic":$scope.characterContext.stats.msonic,
                        "eldritch":$scope.characterContext.stats.meldritch,"poison":$scope.characterContext.stats.mpoison,"darkrift":$scope.characterContext.stats.mdarkrift,
                        "sacred":$scope.characterContext.stats.msacred,"force":$scope.characterContext.stats.mforce,"acid":$scope.characterContext.stats.macid});
                }
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast();  
            }
        }
    };
    
    $scope.rollToHitSneakRange =  function(monsterIndex) {        
        var hitRoll = 1;
        var hitRollOG = 1;
        var monster = null;
        var statusEffect = 'sneak dmg';
        
        $scope.resultIndex = 0;
        
        var buff = $scope.hasBuff("Flank");
        if (buff !== null) {
            $scope.removeBuff(buff);
        }
        
        var critImmuneUsed = false;
        var buff2 = $scope.hasBuff("Ignore Sneak & Crit Immune");
        if (buff2 !== null) {
            $scope.removeBuff(buff2);
            critImmuneUsed = true;
        }
        
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
        
        if (hitRoll !== 20 && monster.monsterEffects && monster.monsterEffects.includes("RANGE_MAIN_ON_20")) {
            hitRollOG = hitRoll;
            hitRoll = 1;
        } else if (monster.monsterEffects && monster.monsterEffects.includes("INCORPOREAL")) {
            if (!$scope.hasEffect($scope.characterContext.rangeDmgEffects, "IGNORE_INCORPOREAL") && 
                    !$scope.hasBuff("Ignore Incorporeal") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                hitRollOG = hitRoll;
                hitRoll = 1;
            }
        } else if (monster.monsterEffects && monster.monsterEffects.includes("MISS_50")) {
            if (!$scope.hasBuff("Ignore Miss") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                hitRollOG = hitRoll;
                hitRoll = 1;
            }
        }   
        
        if (monster !== null && !monster.sneak && !critImmuneUsed) {
            if ($scope.characterContext.rollerId !== null) {
                vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                    ' "rolls": [{ "type": "range_main", "dieResult": 1, "modifiedResult": 1, "isSuccess": false, "damage": 0' + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Sneak Attack","result":response.data.errors[0]});       
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast(); 
                    return($q.reject(response));
                }).then(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Sneak Attack","result":"Your sneak attack failed to find any vulnerable weak spots"});
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast();   
                });
            } else {
                vtdHistory.add({"type":"ATTACK","sub":"RANGE_SNEAK","isImmune":true,"immuneText":"Your sneak attack failed to find any vulnerable weak spots"});
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast();
            }
        } else if (hitRoll === 1) {
            if ($scope.characterContext.rollerId !== null) {
                vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                    ' "rolls": [{ "type": "range_main", "dieResult": 1, "modifiedResult": 1, "isSuccess": false, "damage": 0' + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Sneak Attack","result":response.data.errors[0]});       
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast(); 
                    return($q.reject(response));
                }).then(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Sneak Attack","result":"Sneak attack roll sent"});
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast();   
                });
            } else {
                vtdHistory.add({"type":"ATTACK","sub":"RANGE_SNEAK","isImmune":false,"isMiss":true,"roll":1,"mRoll":hitRollOG});
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast();
            }
        } else {
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
                    else if ($scope.characterContext.rangeWeaponExplodeEffect === "CRIT" && (monster.critical || critImmuneUsed) && hitRoll >= $scope.characterContext.meleeCritMin)
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
                
                var isMainDr = !$scope.hasEffect($scope.characterContext.rangeDmgEffects, "NO_DAMAGE_MOD");
                var mDmgTotal = 0;
                if (hitRoll > 1)
                    mDmgTotal = mDmg + rollDmg;
                else
                    mDmg = 0;

                var mDmgTotalDR = 0;
                if (monster.universalDr !==0) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.universalDr > 0)
                            mDmgTotalDR += monster.universalDr;
                        else
                            mDmgTotal += Math.abs(monster.universalDr);
                    }
                }
                if (monster.rangeDr !==0) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.rangeDr > 0)
                            mDmgTotalDR += monster.rangeDr;
                        else
                            mDmgTotal += Math.abs(monster.rangeDr);
                    }
                } if (monster.rangePercent !== 1.0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal = Math.round(mDmgTotal * monster.rangePercent);
                }

                if (monster.fire !== 0 && +$scope.characterContext.stats.rfire) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.fire > 0)
                            mDmgTotalDR += monster.fire;
                        else
                            mDmgTotal += Math.abs(monster.fire);
                    }
                } if (monster.firePercent !== 1.0 && +$scope.characterContext.stats.rfire) {
                    var percentMod = (Math.round(monster.firePercent * +$scope.characterContext.stats.rfire) - +$scope.characterContext.stats.rfire);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) {
                            if (monster.monsterEffects.includes("FIRE_HEAL"))
                                mDmgTotal -= percentMod;
                            else
                                mDmgTotal += percentMod;
                        }
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.cold !== 0 && +$scope.characterContext.stats.rcold) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.cold > 0)
                            mDmgTotalDR += monster.cold;
                        else
                            mDmgTotal += Math.abs(monster.cold);
                    }  
                } if (monster.coldPercent !== 1.0 && +$scope.characterContext.stats.rcold) {
                    var percentMod = (Math.round(monster.coldPercent * +$scope.characterContext.stats.mcold) - +$scope.characterContext.stats.mcold);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.shock !== 0 && +$scope.characterContext.stats.rshock) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.shock > 0)
                            mDmgTotalDR += monster.shock;
                        else
                            mDmgTotal += Math.abs(monster.shock);
                    }
                } if (monster.shockPercent !== 1.0 && +$scope.characterContext.stats.rshock) {
                    var percentMod = (Math.round(monster.shockPercent * +$scope.characterContext.stats.rshock) - +$scope.characterContext.stats.rshock);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.sonic !== 0 && +$scope.characterContext.stats.rsonic) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.sonic > 0)
                            mDmgTotalDR += monster.sonic;
                        else
                            mDmgTotal += Math.abs(monster.sonic);
                    }
                } if (monster.sonicPercent !== 1.0 && +$scope.characterContext.stats.rsonic) {
                    var percentMod = (Math.round(monster.sonicPercent * +$scope.characterContext.stats.rsonic) - +$scope.characterContext.stats.rsonic);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.poison !== 0 && +$scope.characterContext.stats.rpoison) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.poison > 0)
                            mDmgTotalDR += monster.poison;
                        else
                            mDmgTotal += Math.abs(monster.poison);
                    }  
                } if (monster.poisonPercent !== 1.0 && +$scope.characterContext.stats.rpoison) {
                    var percentMod = (Math.round(monster.poisonPercent * +$scope.characterContext.stats.rpoison) - +$scope.characterContext.stats.rpoison);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.darkrift !== 0 && +$scope.characterContext.stats.rdarkrift) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.darkrift > 0)
                            mDmgTotalDR += monster.darkrift;
                        else
                            mDmgTotal += Math.abs(monster.darkrift);
                    }
                } if (monster.darkriftPercent !== 1.0 && +$scope.characterContext.stats.rdarkrift) {
                     var percentMod = (Math.round(monster.darkriftPercent * +$scope.characterContext.stats.rdarkrift) - +$scope.characterContext.stats.rdarkrift);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.sacred !== 0 && +$scope.characterContext.stats.rsacred) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.sacred > 0)
                            mDmgTotalDR += monster.sacred;
                        else
                            mDmgTotal += Math.abs(monster.sacred);
                    }  
                } if (monster.sacredPercent !== 1.0 && +$scope.characterContext.stats.rsacred) {
                    var percentMod = (Math.round(monster.sacredPercent * +$scope.characterContext.stats.rsacred) - +$scope.characterContext.stats.rsacred);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.force !== 0 && +$scope.characterContext.stats.rforce) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.force > 0)
                            mDmgTotalDR += monster.force;
                        else
                            mDmgTotal += Math.abs(monster.force);
                    }
                } if (monster.forcePercent !== 1.0 && +$scope.characterContext.stats.rforce) {
                     var percentMod = (Math.round(monster.forcePercent * +$scope.characterContext.stats.rforce) - +$scope.characterContext.stats.rforce);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                } if (monster.acid !== 0 && +$scope.characterContext.stats.racid) {
                    if (mDmgTotal > 0 && isMainDr) {
                        if (monster.acid > 0)
                            mDmgTotalDR += monster.acid;
                        else
                            mDmgTotal += Math.abs(monster.acid);
                    }
                } if (monster.acidPercent !== 1.0 && +$scope.characterContext.stats.racid) {
                    var percentMod = (Math.round(monster.acidPercent * +$scope.characterContext.stats.racid) - +$scope.characterContext.stats.racid);
                    if (mDmgTotal > 0 && isMainDr) {
                        if (percentMod > 0) 
                            mDmgTotal += percentMod;
                        else
                            mDmgTotalDR += Math.abs(percentMod);
                    }
                }
                
                if (mDmgTotal < 0)
                    mDmgTotal = 0;
                
                if ((monster.critical || critImmuneUsed) && hitRoll >= $scope.characterContext.rangeSneakCritMin && !$scope.hasEffect($scope.characterContext.rangeDmgEffects, "NO_DAMAGE_MOD") && !$scope.hasEffect(monster.monsterEffects, "RANGE_MAIN_ON_20")) {
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
                
                if (mCritDmg > 0)
                    mCritDmg += $scope.characterContext.unmodifiableSneakDamage;
                mDmgTotal += $scope.characterContext.unmodifiableSneakDamage;
                
                // Apply DR now
                if (mDmgTotalDR > 0) {
                    mCritDmg -= mDmgTotalDR;
                    mDmgTotal -= mDmgTotalDR;
                } 
                if (mDmgTotal < 0)
                    mDmgTotal = 0;
                if (mCritDmg < 0)
                    mCritDmg = 0;
            }
            
            if (monster.rac > hitRollMod) {
                mDmgTotal = 0;
                mCritDmg = 0;
            }

            if ($scope.characterContext.rollerId !== null) {
                vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                    ' "rolls": [{ "type": "range_main", "dieResult": ' + hitRoll + ', "modifiedResult": ' + hitRollMod + ', "isSuccess": ' + (monster.rac <= hitRollMod) + ', "damage": ' + ((mCritDmg > 0) ? mCritDmg : mDmgTotal) + ', "effect": "' + statusEffect + '" }]}').catch(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Sneak Attack","result":response.data.errors[0]});       
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast(); 
                    return($q.reject(response));
                }).then(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Sneak Attack","result":"Sneak attack roll sent"});
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast();   
                });
            } else {
                if (mDmgTotal === 0) {
                    vtdHistory.add({"type":"ATTACK","sub":"RANGE_SNEAK","isMiss":true,"roll":hitRollMod,"mRoll":hitRollMod});
                } else {
                    vtdHistory.add({"type":"ATTACK","sub":"RANGE_SNEAK","isImmune":false,"isMiss":false,"isCrit":mCritDmg > 0,"mRoll":hitRoll,"mRollTotal":hitRollMod,
                        "mWheel":mDmg,"mDmg":mDmgTotal,"totalDmg":mDmgTotal,"mCrit":mCritDmg,"critTotal":mCritDmg,"mWeaponExp":mDmgExp,"fire":$scope.characterContext.stats.rfire,
                        "cold":$scope.characterContext.stats.rcold,"shock":$scope.characterContext.stats.rshock,"sonic":$scope.characterContext.stats.rsonic,
                        "eldritch":$scope.characterContext.stats.reldritch,"poison":$scope.characterContext.stats.rpoison,"darkrift":$scope.characterContext.stats.rdarkrift,
                        "sacred":$scope.characterContext.stats.rsacred,"force":$scope.characterContext.stats.rforce,"acid":$scope.characterContext.stats.racid});
                }
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast(); 
            }
        } 
    };
    
    $scope.rollToHitSubdual =  function(monsterIndex) {        
        var hitRoll = 1;
        var hitRollOG = 1;
        var monster = null;
        
        $scope.resultIndex = 0;
        
        if ($scope.characterContext.monsters === null || $scope.characterContext.monsters.length === 0) {
            hitRoll = $scope.getRandomInt(20) + 1;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex === undefined) {
            monsterSelectorSvc.selectMonster($scope.characterContext.monsters, function(index) {
                $scope.rollToHitSubdual(index);
            });
            return;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex !== undefined) {
            monster = monsterIndex;
            hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
        } else {
            monster = $scope.characterContext.monsters[0];
            hitRoll = monster.roller[$scope.getRandomInt(monster.roller.length)];
        }
        
        if (hitRoll !== 20 && monster.monsterEffects && monster.monsterEffects.includes("MELEE_MAIN_ON_20")) {
            hitRollOG = hitRoll;
            hitRoll = 1;
        } else if (monster.monsterEffects && monster.monsterEffects.includes("INCORPOREAL")) {
            if (!$scope.hasEffect($scope.characterContext.meleeDmgEffects, "IGNORE_INCORPOREAL") && 
                    !$scope.hasBuff("Ignore Incorporeal") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                hitRollOG = hitRoll;
                hitRoll = 1;
            }
        } else if (monster.monsterEffects && monster.monsterEffects.includes("MISS_50")) {
            if (!$scope.hasBuff("Ignore Miss") && (Math.floor((Math.random()+.25) * 2)) === 0) {
                hitRollOG = hitRoll;
                hitRoll = 1;
            }
        } 
 
        if (hitRoll === 1)
            vtdHistory.add({"type":"ATTACK","sub":"MELEE_SUBDUAL","isMiss":true,"roll":1,"mRoll":hitRollOG});
        else {
            var hitRollMod = 1;
            var rollDmg = 0;
            var mDmg = 1;
            var mDmgExp = null;
            var sDmgExp = null;
            var mCritDmg = 0;

            if (hitRoll > 1)
                hitRollMod = hitRoll + $scope.characterContext.stats.meleeHit;
            if (!$scope.hasEffect($scope.characterContext.meleeDmgEffects, "NO_DAMAGE_MOD"))
                rollDmg =  $scope.characterContext.stats.meleeDmg;
    
            var mDmgTotal = 0;
            if (hitRoll > 1)
                mDmgTotal = mDmg + rollDmg;
            else
                mDmg = 0;
    
            if (mDmgTotal < 0)
                mDmgTotal = 0;

            vtdHistory.add({"type":"ATTACK","sub":"MELEE_SUBDUAL","isMiss":false,"isCrit":mCritDmg > 0,"mRoll":hitRoll,"mRollTotal":hitRollMod,
                "mWheel":mDmg,"mDmg":mDmgTotal,"totalDmg":mDmgTotal,"mCrit":mCritDmg,"critTotal":mCritDmg,"mWeaponExp":mDmgExp,"sWeaponExp":sDmgExp,"fire":$scope.characterContext.stats.mfire,
                "cold":$scope.characterContext.stats.mcold,"shock":$scope.characterContext.stats.mshock,"sonic":$scope.characterContext.stats.msonic,
                "eldritch":$scope.characterContext.stats.meldritch,"poison":$scope.characterContext.stats.mpoison,"darkrift":$scope.characterContext.stats.mdarkrift,
                "sacred":$scope.characterContext.stats.msacred,"force":$scope.characterContext.stats.mforce,"acid":$scope.characterContext.stats.macid});
        }
         
        $scope.history = vtdHistory.get();
        $scope.lastEvent = vtdHistory.getLast();  
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
            $scope.rollInit();
        } else if ($scope.attackIndex === 0) {
            $scope.rollToHitMelee();
        } else if ($scope.attackIndex === 1) {
            $scope.rollToHitRange();
        } else if ($scope.attackIndex === 2) {
            $scope.rollToHitPoly();
        } else if ($scope.attackIndex === 9) {
            if ($scope.sneakIndex === 1)
                $scope.rollToHitSneakRange();
            else
                $scope.rollToHitSneak();
        }
    };
    
    $scope.applyDrMelee =  function(mDmg, monster, isBenrow, isMainDr) {
        var mDmgTotal = mDmg;
        
        if(isMainDr)
            mDmg += monster.bonusDmg;

        if (monster.universalDr !==0 && isMainDr)
            mDmgTotal -= monster.universalDr;
           
        if (isBenrow) {
            if (monster.rangeDr !==0 && isMainDr) 
                mDmgTotal -= monster.rangeDr;
             
            if (monster.fire !== 0 && +$scope.characterContext.stats.bfire) {
                if (monster.fire < 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal += (-1*monster.fire < +$scope.characterContext.stats.bfire) ? -1*monster.fire : +$scope.characterContext.stats.bfire;
                } else if (monster.fire - +$scope.characterContext.stats.bfire >= 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= +$scope.characterContext.stats.bfire;
                } else {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= (+$scope.characterContext.stats.bfire - monster.fire);
                } 
            } if ((monster.cold !== 0 || monster.monsterEffects.includes("COLD_RESIST_50")) && +$scope.characterContext.stats.bcold) {
                if (monster.monsterEffects.includes("COLD_RESIST_50")) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= Math.round((+$scope.characterContext.stats.bcold*.5));
                } else if (monster.cold < 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal += (-1*monster.cold < +$scope.characterContext.stats.bcold) ? -1*monster.cold : +$scope.characterContext.stats.bcold;
                } else if (monster.cold - +$scope.characterContext.stats.bcold >= 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= +$scope.characterContext.stats.bcold;
                } else {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= (+$scope.characterContext.stats.bcold - monster.cold);
                } 
            } if (monster.shock !== 0 && +$scope.characterContext.stats.bshock) {
                if (monster.shock < 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal += (-1*monster.shock < +$scope.characterContext.stats.bshock) ? -1*monster.shock : +$scope.characterContext.stats.bshock;
                } else if (monster.shock - +$scope.characterContext.stats.bshock >= 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= +$scope.characterContext.stats.bshock;
                } else {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= (+$scope.characterContext.stats.bshock - monster.shock);
                }  
            } if (monster.sonic !== 0 && +$scope.characterContext.stats.bsonic) {
                if (monster.sonic < 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal += (-1*monster.sonic < +$scope.characterContext.stats.bsonic) ? -1*monster.sonic : +$scope.characterContext.stats.bsonic;
                } else if (monster.sonic - +$scope.characterContext.stats.bsonic >= 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= +$scope.characterContext.stats.bsonic;
                } else {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= (+$scope.characterContext.stats.bsonic - monster.sonic);
                }
            } if (monster.poison !== 0 && +$scope.characterContext.stats.bpoison) {
                if (monster.poison < 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal += (-1*monster.poison < +$scope.characterContext.stats.bpoison) ? -1*monster.poison : +$scope.characterContext.stats.bpoison;
                } else if (monster.poison - +$scope.characterContext.stats.bpoison >= 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= +$scope.characterContext.stats.bpoison;
                } else {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= (+$scope.characterContext.stats.bpoison - monster.poison);
                }  
            } if (monster.darkrift !== 0 && +$scope.characterContext.stats.bdarkrift) {
                if (monster.darkrift < 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal += (-1*monster.darkrift < +$scope.characterContext.stats.bdarkrift) ? -1*monster.darkrift : +$scope.characterContext.stats.bdarkrift;
                } else if (monster.darkrift - +$scope.characterContext.stats.bdarkrift >= 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= +$scope.characterContext.stats.bdarkrift;
                } else {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= (+$scope.characterContext.stats.bdarkrift - monster.darkrift);
                }  
            } if (monster.sacred !== 0 && +$scope.characterContext.stats.bsacred) {
                if (monster.sacred < 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal += (-1*monster.sacred < +$scope.characterContext.stats.bsacred) ? -1*monster.sacred : +$scope.characterContext.stats.bsacred;
                } else if (monster.sacred - +$scope.characterContext.stats.bsacred >= 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= +$scope.characterContext.stats.bsacred;
                } else {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= (+$scope.characterContext.stats.bsacred - monster.sacred);
                }   
            } if (monster.force !== 0 && +$scope.characterContext.stats.bforce) {
                if (monster.force < 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal += (-1*monster.force < +$scope.characterContext.stats.bforce) ? -1*monster.force : +$scope.characterContext.stats.bforce;
                } else if (monster.force - +$scope.characterContext.stats.bforce >= 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= +$scope.characterContext.stats.bforce;
                } else {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= (+$scope.characterContext.stats.bforce - monster.force);
                }  
            } if (monster.acid !== 0 && +$scope.characterContext.stats.bacid) {
                if (monster.acid < 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal += (-1*monster.acid < +$scope.characterContext.stats.bacid) ? -1*monster.acid : +$scope.characterContext.stats.bacid;
                } else if (monster.acid - +$scope.characterContext.stats.bacid >= 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= +$scope.characterContext.stats.bacid;
                } else {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= (+$scope.characterContext.stats.bacid - monster.acid);
                }  
            }
        } else {
            if (monster.meleeDr !==0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal -= monster.meleeDr;
            } if (monster.meleePercent !== 1.0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal = Math.round(mDmgTotal * monster.meleePercent);
            }
            if (monster.fire !== 0 && +$scope.characterContext.stats.mfire) {
                if (monster.fire < 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal += (-1*monster.fire < +$scope.characterContext.stats.mfire) ? -1*monster.fire : +$scope.characterContext.stats.mfire;
                } else if (monster.fire - +$scope.characterContext.stats.mfire >= 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= +$scope.characterContext.stats.mfire;
                } else {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= (+$scope.characterContext.stats.mfire - monster.fire);
                } 
            } if ((monster.cold !== 0 || monster.monsterEffects.includes("COLD_RESIST_50")) && +$scope.characterContext.stats.mcold) {
                if (monster.monsterEffects.includes("COLD_RESIST_50")) {
                    if (mDmgTotal > 0)
                        mDmgTotal -= Math.round((+$scope.characterContext.stats.mcold*.5));
                } else  if (monster.cold < 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal += (-1*monster.cold < +$scope.characterContext.stats.mcold) ? -1*monster.cold : +$scope.characterContext.stats.mcold;
                } else if (monster.cold - +$scope.characterContext.stats.mcold >= 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= +$scope.characterContext.stats.mcold;
                } else {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= (+$scope.characterContext.stats.mcold - monster.cold);
                } 
            } if (monster.shock !== 0 && +$scope.characterContext.stats.mshock) {
                if (monster.shock < 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal += (-1*monster.shock < +$scope.characterContext.stats.mshock) ? -1*monster.shock : +$scope.characterContext.stats.mshock;
                } else if (monster.shock - +$scope.characterContext.stats.mshock >= 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= +$scope.characterContext.stats.mshock;
                } else {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= (+$scope.characterContext.stats.mshock - monster.shock);
                }  
            } if (monster.sonic !== 0 && +$scope.characterContext.stats.msonic) {
                if (monster.sonic < 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal += (-1*monster.sonic < +$scope.characterContext.stats.msonic) ? -1*monster.sonic : +$scope.characterContext.stats.msonic;
                } else if (monster.sonic - +$scope.characterContext.stats.msonic >= 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= +$scope.characterContext.stats.msonic;
                } else {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= (+$scope.characterContext.stats.msonic - monster.sonic);
                }
            } if (monster.poison !== 0 && +$scope.characterContext.stats.mpoison) {
                if (monster.poison < 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal += (-1*monster.poison < +$scope.characterContext.stats.mpoison) ? -1*monster.poison : +$scope.characterContext.stats.mpoison;
                } else if (monster.poison - +$scope.characterContext.stats.mpoison >= 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= +$scope.characterContext.stats.mpoison;
                } else {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= (+$scope.characterContext.stats.mpoison - monster.poison);
                }  
            } if (monster.darkrift !== 0 && +$scope.characterContext.stats.mdarkrift) {
                if (monster.darkrift < 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal += (-1*monster.darkrift < +$scope.characterContext.stats.mdarkrift) ? -1*monster.darkrift : +$scope.characterContext.stats.mdarkrift;
                } else if (monster.darkrift - +$scope.characterContext.stats.mdarkrift >= 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= +$scope.characterContext.stats.mdarkrift;
                } else {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= (+$scope.characterContext.stats.mdarkrift - monster.darkrift);
                }  
            } if (monster.sacred !== 0 && +$scope.characterContext.stats.msacred) {
                if (monster.sacred < 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal += (-1*monster.sacred < +$scope.characterContext.stats.msacred) ? -1*monster.sacred : +$scope.characterContext.stats.msacred;
                } else if (monster.sacred - +$scope.characterContext.stats.msacred >= 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= +$scope.characterContext.stats.msacred;
                } else {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= (+$scope.characterContext.stats.msacred - monster.sacred);
                }   
            } if (monster.force !== 0 && +$scope.characterContext.stats.mforce) {
                if (monster.force < 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal += (-1*monster.force < +$scope.characterContext.stats.mforce) ? -1*monster.force : +$scope.characterContext.stats.mforce;
                } else if (monster.force - +$scope.characterContext.stats.mforce >= 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= +$scope.characterContext.stats.mforce;
                } else {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= (+$scope.characterContext.stats.mforce - monster.force);
                }  
            } if (monster.acid !== 0 && +$scope.characterContext.stats.macid) {
                if (monster.acid < 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal += (-1*monster.acid < +$scope.characterContext.stats.macid) ? -1*monster.acid : +$scope.characterContext.stats.macid;
                } else if (monster.acid - +$scope.characterContext.stats.macid >= 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= +$scope.characterContext.stats.macid;
                } else {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= (+$scope.characterContext.stats.macid - monster.acid);
                }  
            }
        }
        
        if (mDmgTotal < 0)
            return 0;
        return mDmgTotal;
    };
    
    $scope.applyDrRange =  function(mDmg, monster, isMainDr) {
        var mDmgTotal = mDmg;
        
        if (isMainDr)
            mDmg += monster.bonusDmg;

        if (monster.universalDr !==0 && isMainDr) {
            if (mDmgTotal > 0 && isMainDr)
                mDmgTotal -= monster.universalDr;
        }
        if (monster.rangeDr !==0 && isMainDr) {
            if (mDmgTotal > 0 && isMainDr)
                mDmgTotal -= monster.rangeDr;
        }
        if (monster.fire !== 0 && +$scope.characterContext.stats.rfire) {
            if (monster.fire < 0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal += (-1*monster.fire < +$scope.characterContext.stats.rfire) ? -1*monster.fire : +$scope.characterContext.stats.rfire;
            } else if (monster.fire - +$scope.characterContext.stats.rfire >= 0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal -= +$scope.characterContext.stats.rfire;
            } else {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal -= (+$scope.characterContext.stats.rfire - monster.fire);
            } 
        } if ((monster.cold !== 0 || monster.monsterEffects.includes("COLD_RESIST_50")) && +$scope.characterContext.stats.rcold) {
            if (monster.monsterEffects.includes("COLD_RESIST_50")) {
                if (mDmgTotal > 0)
                    mDmgTotal -= Math.round((+$scope.characterContext.stats.rcold*.5));
            } else if (monster.cold < 0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal += (-1*monster.cold < +$scope.characterContext.stats.rcold) ? -1*monster.cold : +$scope.characterContext.stats.rcold;
            } else if (monster.cold - +$scope.characterContext.stats.rcold >= 0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal -= +$scope.characterContext.stats.rcold;
            } else {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal -= (+$scope.characterContext.stats.rcold - monster.cold);
            } 
        } if (monster.shock !== 0 && +$scope.characterContext.stats.rshock) {
            if (monster.shock < 0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal += (-1*monster.shock < +$scope.characterContext.stats.rshock) ? -1*monster.shock : +$scope.characterContext.stats.rshock;
            } else if (monster.shock - +$scope.characterContext.stats.rshock >= 0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal -= +$scope.characterContext.stats.rshock;
            } else {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal -= (+$scope.characterContext.stats.rshock - monster.shock);
            }  
        } if (monster.sonic !== 0 && +$scope.characterContext.stats.rsonic) {
            if (monster.sonic < 0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal += (-1*monster.sonic < +$scope.characterContext.stats.rsonic) ? -1*monster.sonic : +$scope.characterContext.stats.rsonic;
            } else if (monster.sonic - +$scope.characterContext.stats.rsonic >= 0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal -= +$scope.characterContext.stats.rsonic;
            } else {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal -= (+$scope.characterContext.stats.rsonic - monster.sonic);
            }
        } if (monster.poison !== 0 && +$scope.characterContext.stats.rpoison) {
            if (monster.poison < 0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal += (-1*monster.poison < +$scope.characterContext.stats.rpoison) ? -1*monster.poison : +$scope.characterContext.stats.rpoison;
            } else if (monster.poison - +$scope.characterContext.stats.rpoison >= 0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal -= +$scope.characterContext.stats.rpoison;
            } else {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal -= (+$scope.characterContext.stats.rpoison - monster.poison);
            }  
        } if (monster.darkrift !== 0 && +$scope.characterContext.stats.rdarkrift) {
            if (monster.darkrift < 0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal += (-1*monster.darkrift < +$scope.characterContext.stats.rdarkrift) ? -1*monster.darkrift : +$scope.characterContext.stats.rdarkrift;
            } else if (monster.darkrift - +$scope.characterContext.stats.rdarkrift >= 0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal -= +$scope.characterContext.stats.rdarkrift;
            } else {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal -= (+$scope.characterContext.stats.rdarkrift - monster.darkrift);
            }  
        } if (monster.sacred !== 0 && +$scope.characterContext.stats.rsacred) {
            if (monster.sacred < 0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal += (-1*monster.sacred < +$scope.characterContext.stats.rsacred) ? -1*monster.sacred : +$scope.characterContext.stats.rsacred;
            } else if (monster.sacred - +$scope.characterContext.stats.rsacred >= 0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal -= +$scope.characterContext.stats.rsacred;
            } else {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal -= (+$scope.characterContext.stats.rsacred - monster.sacred);
            }   
        } if (monster.force !== 0 && +$scope.characterContext.stats.rforce) {
            if (monster.force < 0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal += (-1*monster.force < +$scope.characterContext.stats.rforce) ? -1*monster.force : +$scope.characterContext.stats.rforce;
            } else if (monster.force - +$scope.characterContext.stats.rforce >= 0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal -= +$scope.characterContext.stats.rforce;
            } else {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal -= (+$scope.characterContext.stats.rforce - monster.force);
            }  
        } if (monster.acid !== 0 && +$scope.characterContext.stats.racid) {
            if (monster.acid < 0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal += (-1*monster.acid < +$scope.characterContext.stats.racid) ? -1*monster.acid : +$scope.characterContext.stats.racid;
            } else if (monster.acid - +$scope.characterContext.stats.racid >= 0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal -= +$scope.characterContext.stats.racid;
            } else {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal -= (+$scope.characterContext.stats.racid - monster.acid);
            }  
        }

        if (mDmgTotal < 0)
            return 0;
        return mDmgTotal;
    };

    $scope.applyDrPoly =  function(mDmg, monster, isMainDr) {
        var mDmgTotal = mDmg;
        
        if (isMainDr)
            mDmg += monster.bonusDmg;
            
        if (monster.universalDr !==0) {
            if (mDmgTotal > 0 && isMainDr && !($scope.characterContext.poly.name === "Iktomi’s Shaper Necklace - Ice" && monster.monsterEffects.includes("COLD_PIERCE")))
                mDmgTotal -= monster.universalDr;
        }
        if (monster.meleeDr !==0) {
            if (mDmgTotal > 0 && isMainDr)
                mDmgTotal -= monster.meleeDr;
        } if (monster.meleePercent !== 1.0) {
            if (mDmgTotal > 0 && isMainDr)
                mDmgTotal = Math.round(mDmgTotal * monster.meleePercent);
        }
            
        var allFire = false;
        var allCold = false;
        var allShock = false;
        var allSonic = false;
        if ($scope.characterContext.poly.name === "Iktomi’s Shaper Necklace - Fire" || $scope.characterContext.poly.name === "Shaman’s Greater Necklace - Fire") {
            if (monster.fire < 0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal += (-1*monster.fire < mDmgTotal) ? -1*monster.fire : mDmgTotal;
            } else if (monster.fire - mDmgTotal >= 0) {
                mDmgTotal = 0;
            } else {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal -= monster.fire;
            }
            allFire = true;
        } else if ($scope.characterContext.poly.name === "Iktomi’s Shaper Necklace - Ice" || $scope.characterContext.poly.name === "Shaman’s Greater Necklace - Ice") {
            if (monster.monsterEffects.includes("COLD_RESIST_50")) {
                mDmgTotal = Math.round((mDmgTotal*.5));
            } else if (monster.cold < 0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal += (-1*monster.cold < mDmgTotal) ? -1*monster.cold : mDmgTotal;
            } else if (monster.cold - mDmgTotal >= 0) {
                mDmgTotal = 0;
            } else {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal -= monster.cold;
            }
            allCold = true;
        } else if ($scope.characterContext.poly.name === "Iktomi’s Shaper Necklace - Air" || $scope.characterContext.poly.name === "Shaman’s Greater Necklace - Air") {
            if (monster.shock < 0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal += (-1*monster.shock < mDmgTotal) ? -1*monster.shock : mDmgTotal;
            } else if (monster.shock - mDmgTotal >= 0) {
                mDmgTotal = 0;
            } else {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal -= monster.shock;
            }
            allShock = true;
        } else if ($scope.characterContext.poly.name === "Iktomi’s Shaper Necklace - Earth" || $scope.characterContext.poly.name === "Shaman’s Greater Necklace - Earth") {
            if (monster.sonic < 0) {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal += (-1*monster.sonic < mDmgTotal) ? -1*monster.sonic : mDmgTotal;
            } else if (monster.sonic - mDmgTotal >= 0) {
                mDmgTotal = 0;
            } else {
                if (mDmgTotal > 0 && isMainDr)
                    mDmgTotal -= monster.sonic;
            }
            allSonic = true;
        } else {
            if (monster.fire !== 0 && +$scope.characterContext.stats.mfire) {
                if (monster.fire < 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal += (-1*monster.fire < +$scope.characterContext.stats.mfire) ? -1*monster.fire : +$scope.characterContext.stats.mfire;
                } else if (monster.fire - +$scope.characterContext.stats.mfire >= 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= +$scope.characterContext.stats.mfire;
                } else {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= (+$scope.characterContext.stats.mfire - monster.fire);
                } 
            } if ((monster.cold !== 0 || monster.monsterEffects.includes("COLD_RESIST_50")) && +$scope.characterContext.stats.mcold) {
                if (monster.monsterEffects.includes("COLD_RESIST_50")) {
                    if(mDmgTotal > 0)
                        mDmgTotal -= Math.round((+$scope.characterContext.stats.mcold*.5));
                } else if (monster.cold < 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal += (-1*monster.cold < +$scope.characterContext.stats.mcold) ? -1*monster.cold : +$scope.characterContext.stats.mcold;
                } else if (monster.cold - +$scope.characterContext.stats.mcold >= 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= +$scope.characterContext.stats.mcold;
                } else {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= (+$scope.characterContext.stats.mcold - monster.cold);
                } 
            } if (monster.shock !== 0 && +$scope.characterContext.stats.mshock) {
                if (monster.shock < 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal += (-1*monster.shock < +$scope.characterContext.stats.mshock) ? -1*monster.shock : +$scope.characterContext.stats.mshock;
                } else if (monster.shock - +$scope.characterContext.stats.mshock >= 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= +$scope.characterContext.stats.mshock;
                } else {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= (+$scope.characterContext.stats.mshock - monster.shock);
                }  
            } if (monster.sonic !== 0 && +$scope.characterContext.stats.msonic) {
                if (monster.sonic < 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal += (-1*monster.sonic < +$scope.characterContext.stats.msonic) ? -1*monster.sonic : +$scope.characterContext.stats.msonic;
                } else if (monster.sonic - +$scope.characterContext.stats.msonic >= 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= +$scope.characterContext.stats.msonic;
                } else {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= (+$scope.characterContext.stats.msonic - monster.sonic);
                }
            } if (monster.poison !== 0 && +$scope.characterContext.stats.mpoison) {
                if (monster.poison < 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal += (-1*monster.poison < +$scope.characterContext.stats.mpoison) ? -1*monster.poison : +$scope.characterContext.stats.mpoison;
                } else if (monster.poison - +$scope.characterContext.stats.mpoison >= 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= +$scope.characterContext.stats.mpoison;
                } else {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= (+$scope.characterContext.stats.mpoison - monster.poison);
                }  
            } if (monster.darkrift !== 0 && +$scope.characterContext.stats.mdarkrift) {
                if (monster.darkrift < 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal += (-1*monster.darkrift < +$scope.characterContext.stats.mdarkrift) ? -1*monster.darkrift : +$scope.characterContext.stats.mdarkrift;
                } else if (monster.darkrift - +$scope.characterContext.stats.mdarkrift >= 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= +$scope.characterContext.stats.mdarkrift;
                } else {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= (+$scope.characterContext.stats.mdarkrift - monster.darkrift);
                }  
            } if (monster.sacred !== 0 && +$scope.characterContext.stats.msacred) {
                if (monster.sacred < 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal += (-1*monster.sacred < +$scope.characterContext.stats.msacred) ? -1*monster.sacred : +$scope.characterContext.stats.msacred;
                } else if (monster.sacred - +$scope.characterContext.stats.msacred >= 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= +$scope.characterContext.stats.msacred;
                } else {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= (+$scope.characterContext.stats.msacred - monster.sacred);
                }   
            } if (monster.force !== 0 && +$scope.characterContext.stats.mforce) {
                if (monster.force < 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal += (-1*monster.force < +$scope.characterContext.stats.mforce) ? -1*monster.force : +$scope.characterContext.stats.mforce;
                } else if (monster.force - +$scope.characterContext.stats.mforce >= 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= +$scope.characterContext.stats.mforce;
                } else {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= (+$scope.characterContext.stats.mforce - monster.force);
                }  
            } if (monster.acid !== 0 && +$scope.characterContext.stats.macid) {
                if (monster.acid < 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal += (-1*monster.acid < +$scope.characterContext.stats.macid) ? -1*monster.acid : +$scope.characterContext.stats.macid;
                } else if (monster.acid - +$scope.characterContext.stats.macid >= 0) {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= +$scope.characterContext.stats.macid;
                } else {
                    if (mDmgTotal > 0 && isMainDr)
                        mDmgTotal -= (+$scope.characterContext.stats.macid - monster.acid);
                }  
            }
        }
        
        if (mDmgTotal < 0)
            return 0;
        return mDmgTotal;
    };
    
    $scope.rollToSave =  function() {
        if ($scope.saveIndex === 0) {
            $scope.rollFort(); 
        } else if ($scope.saveIndex === 1) {
            $scope.rollReflex(); 
        } else if ($scope.saveIndex === 2) {
            $scope.rollWill();           
        }                     
    };
    
    $scope.rollReflex =  function(monsterIndex) {
        var save = $scope.getRandomInt(20) + 1;
        var questers = false;
        
        if (save === 1 && $scope.characterContext.questors) {
            save = $scope.getRandomInt(20) + 1;
            questers = true;
        }
        var saveMod = (save > 1) ? (save + $scope.characterContext.stats.reflex) : 1;
        var monsterSave = null;
        var monsterEffect = "missed";
        
        if ($scope.characterContext.monsters === null || $scope.characterContext.monsters.length === 0) {
            monsterSave = 0;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex === undefined) {
            monsterSelectorSvc.selectMonster($scope.characterContext.monsters, function(index) {
                $scope.rollReflex(index);
            });
            return;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex !== undefined) {
            monsterSave = monsterIndex.reflex;
            monsterEffect = monsterIndex.reflexEffect;
        } else {
            monsterSave = $scope.characterContext.monsters[0].reflex;
            monsterEffect = $scope.characterContext.monsters[0].reflexEffect;
        }
        
        if (monsterEffect === undefined || monsterEffect === null)
            monsterEffect = "missed";
        
        var saved = (saveMod >= monsterSave) ? true : false;

        if (saved) {
            monsterEffect = "";
            if (questers)
                monsterEffect = "quester";
        }
        
        if ($scope.characterContext.rollerId !== null) {
            vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "save_roll", ' +
                ' "rolls": [{ "type": "reflex", "dieResult": ' + save + ', "modifiedResult": ' + saveMod + ', "isSuccess": ' + saved + ', "effect": "' + monsterEffect + '" }]}').catch(function(response) {
                vtdHistory.add({"type":"ROLLER","sub":"Reflex Save","result":response.data.errors[0]});       
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast(); 
                return($q.reject(response));
            }).then(function(response) {
                vtdHistory.add({"type":"ROLLER","sub":"Reflex Save","result":"Reflex save roll sent"});
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast();   
            });
        } else {
            if (save > 1)
                vtdHistory.add({"type":"SAVE","sub":"REFLEX","roll":save,"save":save+$scope.characterContext.stats.reflex});
            else
                vtdHistory.add({"type":"SAVE","sub":"REFLEX","roll":save,"save":save});
            $scope.history = vtdHistory.get();
            $scope.lastEvent = vtdHistory.getLast(); 
        }                         
    };
    
    $scope.rollFort =  function(monsterIndex) {
        var save = $scope.getRandomInt(20) + 1;
        var questers = false;
        
        if (save === 1 && $scope.characterContext.questors) {
            save = $scope.getRandomInt(20) + 1;
            questers = true;
        }
        
        var saveMod = (save > 1) ? (save + $scope.characterContext.stats.fort) : 1;
        var monsterSave = null;
        var monsterEffect = "missed";

        if ($scope.characterContext.monsters === null || $scope.characterContext.monsters.length === 0) {
            monsterSave = 0;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex === undefined) {
            monsterSelectorSvc.selectMonster($scope.characterContext.monsters, function(index) {
                $scope.rollFort(index);
            });
            return;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex !== undefined) {
            monsterSave = monsterIndex.fort;
            monsterEffect = monsterIndex.fortEffect;
        } else {
            monsterSave = $scope.characterContext.monsters[0].fort;
            monsterEffect = $scope.characterContext.monsters[0].fortEffect;
        }
        
        if (monsterEffect === undefined || monsterEffect === null)
            monsterEffect = "missed";
        
        var saved = (saveMod >= monsterSave) ? true : false;

        if (saved) {
            monsterEffect = "";
            if (questers)
                monsterEffect = "quester";
        }
        
        if ($scope.characterContext.rollerId !== null) {
            vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "save_roll", ' +
                ' "rolls": [{ "type": "fortitude", "dieResult": ' + save + ', "modifiedResult": ' + saveMod + ', "isSuccess": ' + saved + ', "effect": "' + monsterEffect + '" }]}').catch(function(response) {
                vtdHistory.add({"type":"ROLLER","sub":"Fortitude Save","result":response.data.errors[0]});       
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast(); 
                return($q.reject(response));
            }).then(function(response) {
                vtdHistory.add({"type":"ROLLER","sub":"Fortitude Save","result":"Fortitude save roll sent"});
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast();   
            });
        } else {
            if (save > 1)
                vtdHistory.add({"type":"SAVE","sub":"FORT","roll":save,"save":save+$scope.characterContext.stats.fort});
            else
                vtdHistory.add({"type":"SAVE","sub":"FORT","roll":save,"save":save});
            $scope.history = vtdHistory.get();
            $scope.lastEvent = vtdHistory.getLast();  
        }                    
    };
    
    $scope.rollWill =  function(monsterIndex) {
        var save = $scope.getRandomInt(20) + 1;
        var questers = false;
        
        if (save === 1 && $scope.characterContext.questors) {
            save = $scope.getRandomInt(20) + 1;
            questers = true;
        }
        
        var saveMod = (save > 1) ? (save + $scope.characterContext.stats.will) : 1;
        var monsterSave = null;
        var monsterEffect = "missed";
        
        if ($scope.characterContext.monsters === null || $scope.characterContext.monsters.length === 0) {
            monsterSave = 0;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex === undefined) {
            monsterSelectorSvc.selectMonster($scope.characterContext.monsters, function(index) {
                $scope.rollWill(index);
            });
            return;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex !== undefined) {
            monsterSave = monsterIndex.will;
            monsterEffect = monsterIndex.willEffect;
        } else {
            monsterSave = $scope.characterContext.monsters[0].will;
            monsterEffect = $scope.characterContext.monsters[0].willEffect;
        }
        
        if (monsterEffect === undefined || monsterEffect === null)
            monsterEffect = "missed";
        
        var saved = (saveMod >= monsterSave) ? true : false;

        if (saved) {
            monsterEffect = "";
            if (questers)
                monsterEffect = "quester";
        }

        if ($scope.characterContext.rollerId !== null) {
            vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "save_roll", ' +
                ' "rolls": [{ "type": "will", "dieResult": ' + save + ', "modifiedResult": ' + saveMod + ', "isSuccess": ' + saved + ', "effect": "' + monsterEffect + '" }]}').catch(function(response) {
                vtdHistory.add({"type":"ROLLER","sub":"Will Save","result":response.data.errors[0]});       
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast(); 
                return($q.reject(response));
            }).then(function(response) {
                vtdHistory.add({"type":"ROLLER","sub":"Will Save","result":"Will save roll sent"});
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast();   
            });
        } else {
            if (save > 1)
                vtdHistory.add({"type":"SAVE","sub":"WILL","roll":save,"save":save+$scope.characterContext.stats.will});
            else
                vtdHistory.add({"type":"SAVE","sub":"WILL","roll":save,"save":save});
            $scope.history = vtdHistory.get();
            $scope.lastEvent = vtdHistory.getLast();   
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
    
    $scope.getRoll =  function(monsterIndex) {
        if ($scope.characterContext.monsters === null || $scope.characterContext.monsters.length === 0) {
            return $scope.getRandomInt(20) + 1;
        } else if ($scope.characterContext.monsters.length > 1 && monsterIndex !== undefined) {
            return monsterIndex.roller[$scope.getRandomInt(monsterIndex.roller.length)];
        } else {
            return $scope.characterContext.monsters[0].roller[$scope.getRandomInt($scope.characterContext.monsters[0].roller.length)];
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
    
    $scope.setBraceletCabalBonus =  function(bonus) {
        if (bonus !== null && bonus >= 0) {
            vtdSvc.setBraceletCabalBonus($scope.characterContext.id, bonus).then(function(result) {
                vtdState.setContext(result.data);
                $scope.characterContext = vtdState.get();
            });
        }
    };
    
    $scope.activatePrestigeClass = function(id) {
        confirmDialogSvc.confirm("Are you sure you wish to activate prestige class?", function(){
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
    
    $scope.queueSkill =  function(skillId, selfTarget, selfHeal, madEvoker, lohNumber, inGameEffect, markUse, ignoreUse) {
        vtdSvc.queueSkill($scope.characterContext.id, skillId, selfTarget, selfHeal, madEvoker, lohNumber, inGameEffect, markUse, ignoreUse).then(function(result) {
            vtdState.setContext(result.data);
            $scope.characterContext = vtdState.get();
        });
    };
    
    $scope.execSkillQueue =  function() {
        vtdSvc.execSkillQueue($scope.characterContext.id).then(function(result) {
            vtdState.setContext(result.data);
            $scope.characterContext = vtdState.get();
            
            if ($scope.characterContext.rollerId !== null) {
                vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                        ' "rolls": [{ "type": "spell", "dieResult": ' + 1 + ', "modifiedResult": ' + 1 + ', "isSuccess": ' + true + ', "damage": ' + $scope.characterContext.totalDamageLastSpell + ' }]}').catch(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Spell","result":response.data.errors[0]});       
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast(); 
                        return($q.reject(response));
                    }).then(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Spell","result":"Spell attack roll sent for " + $scope.characterContext.totalDamageLastSpell});
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast();   
                    });
                
            } else {
                vtdHistory.add({"type":"ROLLER","sub":"Spell","result":"Spell attack total " + $scope.characterContext.totalDamageLastSpell});
                $scope.history = vtdHistory.get();
                $scope.lastEvent = vtdHistory.getLast();   
            }
        });
    };
    
    $scope.resendSkillQueue =  function() {
        if ($scope.characterContext.rollerId !== null) {
            vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                    ' "rolls": [{ "type": "spell", "dieResult": ' + 1 + ', "modifiedResult": ' + 1 + ', "isSuccess": ' + true + ', "damage": ' + $scope.characterContext.totalDamageLastSpell + ' }]}').catch(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Spell","result":response.data.errors[0]});       
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast(); 
                    return($q.reject(response));
                }).then(function(response) {
                    vtdHistory.add({"type":"ROLLER","sub":"Spell","result":"Spell attack roll sent for " + $scope.characterContext.totalDamageLastSpell});
                    $scope.history = vtdHistory.get();
                    $scope.lastEvent = vtdHistory.getLast();   
                });

        } else {
            vtdHistory.add({"type":"ROLLER","sub":"Spell","result":"Spell attack total " + $scope.characterContext.totalDamageLastSpell});
            $scope.history = vtdHistory.get();
            $scope.lastEvent = vtdHistory.getLast();   
        }
    };
    
    $scope.deleteQueuedSkills =  function() {
        vtdSvc.deleteQueuedSkills($scope.characterContext.id).then(function(result) {
            vtdState.setContext(result.data);
            $scope.characterContext = vtdState.get();
        });
    };
    
    $scope.useSkill = function(skillId, selfTarget, selfHeal, madEvoker, lohNumber, inGameEffect, markUse, ignoreUse, isSuccess, die, modifiedDie, damage, isQueue) {
        if (isQueue) {
            vtdSvc.queueSkill($scope.characterContext.id, skillId, selfTarget, selfHeal, madEvoker, lohNumber, inGameEffect, markUse, ignoreUse, damage).then(function(result) {
                vtdState.setContext(result.data);
                $scope.characterContext = vtdState.get();
            });  
        } else {
            if ($scope.characterContext.rollerId !== null && damage > -1) {
                if (isSuccess && die === 0) {
                    die = 1;
                    modifiedDie = 1;
                }
                vtdSvc.sendRoll('{ "version": 1, "slotId": "' + $scope.characterContext.rollerId + '", "classId": "' + $scope.characterContext.rollerCharacterClass  + '", "eventType": "attack_roll", ' +
                        ' "rolls": [{ "type": "spell", "dieResult": ' + die + ', "modifiedResult": ' + modifiedDie + ', "isSuccess": ' + isSuccess + ', "damage": ' + damage + ' }]}').catch(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Spell","result":response.data.errors[0]});       
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast(); 
                        alert(response.data.errors[0]);
                        return($q.reject(response));
                    }).then(function(response) {
                        vtdHistory.add({"type":"ROLLER","sub":"Spell","result":"Spell attack roll sent"});
                        $scope.history = vtdHistory.get();
                        $scope.lastEvent = vtdHistory.getLast();   
                        vtdSvc.useSkill($scope.characterContext.id, skillId, selfTarget, selfHeal, madEvoker, lohNumber, inGameEffect, markUse, ignoreUse).then(function(result) {
                            vtdState.setContext(result.data);
                            $scope.characterContext = vtdState.get();
                        });
                    });
                
            } else {
                vtdSvc.useSkill($scope.characterContext.id, skillId, selfTarget, selfHeal, madEvoker, lohNumber, inGameEffect, markUse, ignoreUse).then(function(result) {
                    vtdState.setContext(result.data);
                    $scope.characterContext = vtdState.get();
                });
            }
        }
    };
    
    $scope.unuseSkill =  function(skilId) {
        vtdSvc.unuseSkill($scope.characterContext.id, skilId).then(function(result) {
            vtdState.setContext(result.data);
            $scope.characterContext = vtdState.get();
        });
    };
    
    $scope.activateBuff = function(buff) {
        vtdSvc.addBuff($scope.characterContext.id, buff.id, buff.level).then(function(result) {
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
    
    $scope.activateDebuff =  function(debuff) {
        vtdSvc.addDebuff($scope.characterContext.id, debuff.id).then(function(result) {
            vtdState.setContext(result.data);
            $scope.characterContext = vtdState.get();
        });
    };
    
    $scope.removeDebuff =  function(debuff) {
        vtdSvc.removeDebuff($scope.characterContext.id, debuff.id).then(function(result) {
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
                    vtdState.stats.rangeOffhandHitBenrow = Math.round(vtdState.stats.rangeHitBenrow * .75);
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

.factory('VtdSvc',['$http', 'RESOURCES', 'ErrorDialogSvc', 'WarnDialogSvc', '$q', function($http, RESOURCES, errorDialogSvc, warnDialogSvc, $q) {    
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
    
    tokenAdminSvc.importCharacter = function(id) {
        return $http.post(RESOURCES.REST_BASE_URL + '/vtd/import/' + id)
            .catch(function(response) {
                errorDialogSvc.showError(response);
                return($q.reject(response));
            });
    };
    
    tokenAdminSvc.deleteCharacter = function(id) {
        return $http.delete(RESOURCES.REST_BASE_URL + '/vtd/import/' + id)
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
    
    tokenAdminSvc.setBraceletCabalBonus = function(id, bonus) {
        return $http.post(RESOURCES.REST_BASE_URL + '/vtd/' + id + '/bonus/bcabal/?bonus=' + bonus).catch(function(response) {
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
    
    tokenAdminSvc.queueSkill = function(id, skillId, selfTarget, selfHeal, madEvoker, lohNumber, inGameEffect, markUse, ignoreUse, damage) {
        if (inGameEffect === null)
            inGameEffect = 'NONE';
        return $http.post(RESOURCES.REST_BASE_URL + '/vtd/' + id + '/' + skillId + '/queue/?selfTarget=' + selfTarget + '&selfHeal=' + selfHeal + '&madEvoker=' + madEvoker + '&lohNumber=' + lohNumber + '&inGameEffect=' + inGameEffect + '&markUse=' + markUse + '&ignoreUse=' + ignoreUse + '&damage=' + damage).catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
    
    tokenAdminSvc.execSkillQueue =  function(id) {
        return $http.post(RESOURCES.REST_BASE_URL + '/vtd/' + id + '/queue/').catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
    
    tokenAdminSvc.deleteQueuedSkills =  function(id) {
        return $http.delete(RESOURCES.REST_BASE_URL + '/vtd/' + id + '/queue/').catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
    
    tokenAdminSvc.useSkill = function(id, skillId, selfTarget, selfHeal, madEvoker, lohNumber, inGameEffect, markUse, ignoreUse) {
        if (inGameEffect === null)
            inGameEffect = 'NONE';
        return $http.post(RESOURCES.REST_BASE_URL + '/vtd/' + id + '/' + skillId + '/use/?selfTarget=' + selfTarget + '&selfHeal=' + selfHeal + '&madEvoker=' + madEvoker + '&lohNumber=' + lohNumber + '&inGameEffect=' + inGameEffect + '&markUse=' + markUse + '&ignoreUse=' + ignoreUse).catch(function(response) {
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
    
    tokenAdminSvc.addBuff = function(id, buff, level) {
        if (level === undefined || level === null) {
            return $http.post(RESOURCES.REST_BASE_URL + '/vtd/' + id + '/buff/?buff=' + buff).catch(function(response) {
                errorDialogSvc.showError(response);
                return($q.reject(response));
            });
        } else {
            return $http.post(RESOURCES.REST_BASE_URL + '/vtd/' + id + '/buff/?buff=' + buff + '&level=' + level).catch(function(response) {
                errorDialogSvc.showError(response);
                return($q.reject(response));
            });
        }
    };
    
    tokenAdminSvc.addDebuff = function(id, debuff) {
        return $http.post(RESOURCES.REST_BASE_URL + '/vtd/' + id + '/debuff/?debuff=' + debuff).catch(function(response) {
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
    
    tokenAdminSvc.removeDebuff = function(id, debuff) {
        return $http.delete(RESOURCES.REST_BASE_URL + '/vtd/' + id + '/debuff/?debuff=' + debuff).catch(function(response) {
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
    
    tokenAdminSvc.setRollerId = function(id, rollerId) {
        return $http.post(RESOURCES.REST_BASE_URL + '/vtd/' + id + '/roller/?rollerId=' + rollerId)
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
    
    tokenAdminSvc.sendRoll = function(roll) {
       // roll = '{"version":1,"slotId":"ABCDEF","classId":"bard","eventType":"attack_roll","rolls":[{"type":"melee_main","dieResult":17,"modifiedResult":21,"isSuccess":true,"damage":17}]}';
        return $http.post('https://us-central1-tdroller-1ac5a.cloudfunctions.net/roll', roll);
    };
    
    return tokenAdminSvc;
}]);