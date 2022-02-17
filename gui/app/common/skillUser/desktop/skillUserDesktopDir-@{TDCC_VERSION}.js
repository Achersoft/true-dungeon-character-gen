angular.module('main').directive('skillUserDesktop',['VtdSvc', 'MonsterSelectorSvc', '$uibModal', '$timeout', function(vtdSvc, monsterSelectorSvc, $uibModal, $timeout){
    return {
        restrict:'E',
        scope:{
            model:'=',
            characterContext:'=',
            elementId: '@',
            useAbility: '&?',
            unuseAbility: '&?',
            hasBuff: '&?',
            removeBuff: '&?',
            roll: '&?'
        },
        link: function(scope) {
            scope.itemSelection = {};
            scope.targetIndex = 0;
            scope.madEvokerIndex = 0;
            scope.castSequenceIndex = 0;
            scope.loh10Index = 0;
            scope.loh15Index = 0;
            scope.secondaryTargetIndex = 0;
            scope.skillCheckIndex = 3;
            scope.hitRoll = 0;
            scope.hitRollNatural = 0;
            scope.hitSuccess = true;
            scope.damage = 0;
            scope.damagePool = 0;
            scope.healPool = 0;
            scope.primaryHealAmount = 0;
            scope.seconaryHealAmount = 0;
            scope.lohUseAmount = 1;
            scope.spellCast = false;
            scope.healError = false;
            scope.skillCheckSrc = '';
            scope.skillCheckAnswer = '';
            scope.skillCheckOptions = [];
            scope.modalInstance = null;
            
            scope.openModal = function(isChecked, monster) {
                scope.targetIndex = 0;
                scope.madEvokerIndex = 0;
                scope.castSequenceIndex = 0;
                scope.loh10Index = 0;
                scope.loh15Index = 0;
                scope.hitRoll = 0;
                scope.hitRollNatural = 0;
                scope.hitSuccess = true;
                scope.damage = 0;
                scope.damagePool = 0;
                scope.healPool = 0;
                scope.secondaryTargetIndex = 0;
                scope.skillCheckIndex = 3;
                scope.primaryHealAmount = 0;
                scope.seconaryHealAmount = 0;
                scope.lohUseAmount = 1;
                scope.spellCast = false;
                scope.healError = false;
                scope.monster = monster;
                scope.magePowers = false;
                scope.skillCheck = false;
                scope.skillCheckSrc = '';
                scope.skillCheckAnswer = '';
                scope.skillCheckOptions = [];
                scope.element = -1;
                scope.conserve = 0;
                scope.fork = 0;
                scope.intensify = 0;
                scope.sharpen = 0;
                
                if (isChecked) {
                    scope.modalInstance = $uibModal.open({
                        ariaLabelledBy: 'modal-title',
                        ariaDescribedBy: 'modal-body',
                        bindToController: true,
                        scope: scope,
                        windowClass: 'desktop-vtd-dialog',
                        openedClass: 'desktop-modal-content',
                        templateUrl: 'common/skillUser/desktop/skillUserUnsetDesktopModalTemplate-@{TDCC_VERSION}.html'
                    });
                } else {
                    if ((scope.model.skillType === 'DAMAGE_RANGE_AC_15' || scope.model.skillType === 'DAMAGE') && scope.characterContext.monsters.length > 1 && scope.monster === undefined) {
                         monsterSelectorSvc.selectMonster(scope.characterContext.monsters, function(index) {
                            scope.openModal(isChecked, index);
                        });
                        return;
                    } 
                    if (scope.monster === undefined) {
                        scope.monster = scope.characterContext.monsters[0];
                    }
                    if (scope.characterContext.magePower || scope.characterContext.archMagePower)
                        scope.magePowers = true;
                    else if (scope.model.minEffect !== scope.model.maxEffect)
                        scope.initSkillCheck();
       
                    scope.modalInstance = $uibModal.open({
                        ariaLabelledBy: 'modal-title',
                        ariaDescribedBy: 'modal-body',
                        bindToController: true,
                        scope: scope,
                        windowClass: 'desktop-vtd-dialog',
                        openedClass: 'desktop-modal-content',
                        templateUrl: 'common/skillUser/desktop/skillUserDesktopModalTemplate-@{TDCC_VERSION}.html'
                    });
                }
            };
            
            scope.setSkillCheckIndex = function(index) {
                 scope.skillCheckIndex = index;
            };
            
            scope.setTargetIndex = function(index) {
                 scope.targetIndex = index;
            };
            
            scope.setSecondaryTargetIndex = function(index) {
                 scope.secondaryTargetIndex = index;
            };
            
            scope.setMadEvokerIndex = function(index) {
                 scope.madEvokerIndex = index;
            };
            
            scope.setCastSequenceIndex = function(index) {
                 scope.castSequenceIndex = index;
            };
            
            scope.setLoH10Index = function(index) {
                 scope.loh10Index = index;
            };
            
            scope.setLoH15Index = function(index) {
                 scope.loh15Index = index;
            };
            
            scope.useSkill = function(primaryAmount, seconaryHealAmount, lohAmount, markUse) {
                if (scope.model.skillType === 'HEAL') {
                    scope.healPool = 0;
                    if (scope.model.name === 'Lay on Hands') {
                        if (lohAmount < 1)
                            lohAmount = 1;
                        else if (lohAmount > (scope.model.usableNumber - scope.model.usedNumber))
                            lohAmount = (scope.model.usableNumber - scope.model.usedNumber);
                        
                        scope.primaryHealAmount = lohAmount;
                        var selfHeal = 0;
                       
                        if (scope.loh10Index === 1) {
                            scope.primaryHealAmount += 10;
                            if (scope.targetIndex === 1) {
                                selfHeal = scope.primaryHealAmount;
                            }
                            scope.spellCastSucess(selfHeal > 0, selfHeal, false, lohAmount, "PLUS_10_LOH", markUse);
                        } else if (scope.loh15Index === 1) {
                            scope.primaryHealAmount += 15;
                            if (scope.targetIndex === 1) {
                                selfHeal = scope.primaryHealAmount;
                            }
                            scope.spellCastSucess(selfHeal > 0, selfHeal, false, lohAmount, "PLUS_15_LOH", markUse);
                        } else {
                            if (scope.targetIndex === 1) {
                                selfHeal = scope.primaryHealAmount;
                            }
                            scope.spellCastSucess(selfHeal > 0, selfHeal, false, lohAmount, null, markUse);
                        }
                    } else {
                        var totalHeal = scope.characterContext.stats.spellHeal + ((scope.skillCheckIndex === 0)?scope.model.maxEffect:scope.model.minEffect);
                        
                        if (scope.hasBuff()("Spell Surge")) {
                            if(scope.skillCheckIndex === 0)
                                totalHeal += scope.model.maxEffect;
                            else
                               totalHeal += scope.model.minEffect; 
                        }

                        if (scope.secondaryTargetIndex !== 0) {
                            scope.primaryHealAmount = primaryAmount;
                            scope.seconaryHealAmount = seconaryHealAmount;

                            if ((+scope.primaryHealAmount + +scope.seconaryHealAmount) !== totalHeal) {
                                scope.healError = true;
                            } else {
                                var selfHeal = 0;
                                if (scope.targetIndex === 1) {
                                    selfHeal = scope.primaryHealAmount;
                                }
                                if (scope.secondaryTargetIndex === 2) {
                                    selfHeal = scope.seconaryHealAmount;
                                }
                                
                                var buff = scope.hasBuff()("Spell Surge");
                                if (buff !== null) {
                                    scope.removeBuff()(buff);
                                }
                                
                                scope.spellCastSucess(selfHeal > 0, selfHeal, false, 0, null, markUse);
                            }
                        } else {
                            scope.primaryHealAmount = (scope.model.aoe) ? ((scope.skillCheckIndex === 0)?scope.model.maxEffect:scope.model.minEffect) : totalHeal;
                            scope.seconaryHealAmount = 0;
                            var selfHeal = 0;
                            scope.healPool = (scope.model.aoe) ? (scope.hasBuff()("Spell Surge")) ? scope.characterContext.stats.spellHeal + ((scope.skillCheckIndex === 0)?scope.model.maxEffect:scope.model.minEffect) : scope.characterContext.stats.spellHeal : 0;

                            if (scope.targetIndex === 1 || scope.model.skillTarget === 'PARTY') {
                                selfHeal = scope.primaryHealAmount;
                            }
                            
                            var buff = scope.hasBuff()("Spell Surge");
                            if (buff !== null) {
                                scope.removeBuff()(buff);
                            }
                    
                            scope.spellCastSucess(selfHeal > 0, selfHeal, false, 0, null, markUse);
                        }
                    }
                } else if (scope.model.skillType === 'DAMAGE') {
                    var dmg = ((scope.skillCheckIndex === 0)?scope.model.maxEffect:scope.model.minEffect);   
                    var totalDamage = (scope.model.aoe) ? dmg : scope.characterContext.stats.spellDmg + dmg;
                    var madEvoker = false;
                    scope.damagePool = (scope.model.aoe) ? scope.characterContext.stats.spellDmg : 0;
                    
                    if (scope.madEvokerIndex === 1) {
                        madEvoker = true;
                        if (scope.model.aoe)
                            scope.damagePool += dmg;
                        else 
                            totalDamage += dmg;
                    }
                    
                    var buff = scope.hasBuff()("Spell Surge");
                    if (buff !== null) {
                        totalDamage += dmg;
                        scope.removeBuff()(buff);
                    }
                    
                    scope.damage = totalDamage;
                    
                     scope.applyDr();
                    
                    if(scope.monster.monsterEffects) {
                        if(scope.model.skillLevel === 'ZERO' && scope.monster.monsterEffects.includes("PHASING_NORMAL")) {
                            scope.damage = 0;
                            scope.damagePool = 0;
                        } else if((scope.model.skillLevel === 'ZERO' || scope.model.skillLevel === 'ONE')  && scope.monster.monsterEffects.includes("PHASING_HARDCORE")) {
                            scope.damage = 0;
                            scope.damagePool = 0;
                        } else if((scope.model.skillLevel === 'ZERO' || scope.model.skillLevel === 'ONE' || scope.model.skillLevel === 'TWO')  && scope.monster.monsterEffects.includes("PHASING_NIGHTMARE")) {
                            scope.damage = 0;
                            scope.damagePool = 0;
                        } else if(scope.monster.monsterEffects.includes("PHASING_EPIC")) {
                            if (scope.model.skillLevel === 'ZERO' || scope.model.skillLevel === 'ONE' || scope.model.skillLevel === 'TWO') {
                                scope.damage = 0;
                                scope.damagePool = 0;
                            } else {
                                scope.damage =  Math.round(scope.damage * .5);
                                scope.damagePool = Math.round(scope.damagePool * .5);
                            }
                        }
                    }
                    
                    scope.spellCastSucess(false, 0, madEvoker, 0, null, markUse);
                } else if (scope.model.skillType === 'DAMAGE_RANGE_AC_15') {
                    var dmg = ((scope.skillCheckIndex === 0)?scope.model.maxEffect:scope.model.minEffect);   
                    var totalDamage = scope.characterContext.stats.spellDmg + dmg;
                    var madEvoker = false;

                    scope.hitRollNatural = scope.roll()(scope.monster);
                    scope.hitRoll = scope.hitRollNatural + scope.characterContext.stats.rangeHit;
                    scope.hitSuccess =  scope.hitRoll >= 15;
                            
                    if (scope.madEvokerIndex === 1) {
                        madEvoker = true;
                        totalDamage += dmg;
                    }
                    
                    var buff = scope.hasBuff()("Spell Surge");
                    if (buff !== null) {
                        totalDamage += dmg;
                        scope.removeBuff()(buff);
                    } else if (scope.sharpen === 1 && scope.hitRollNatural >= 18) {
                        totalDamage = totalDamage*2;
                    }
                    
                    scope.damage = totalDamage;
                    
                    scope.applyDr();
                    
                    if(scope.monster.monsterEffects) {
                        if (scope.monster.monsterEffects.includes("INCORPOREAL")) {
                            if (!scope.hasBuff()("Ignore Incorporeal") && (Math.floor(Math.random() * 2) === 0)) {
                                scope.damage = 0;
                                scope.damagePool = 0;
                            }
                        } else if(scope.model.skillLevel === 'ZERO' && scope.monster.monsterEffects.includes("PHASING_NORMAL")) {
                            scope.damage = 0;
                            scope.damagePool = 0;
                        } else if((scope.model.skillLevel === 'ZERO' || scope.model.skillLevel === 'ONE')  && scope.monster.monsterEffects.includes("PHASING_HARDCORE")) {
                            scope.damage = 0;
                            scope.damagePool = 0;
                        } else if((scope.model.skillLevel === 'ZERO' || scope.model.skillLevel === 'ONE' || scope.model.skillLevel === 'TWO')  && scope.monster.monsterEffects.includes("PHASING_NIGHTMARE")) {
                            scope.damage = 0;
                            scope.damagePool = 0;
                        } else if(scope.monster.monsterEffects.includes("PHASING_EPIC")) {
                            if (scope.model.skillLevel === 'ZERO' || scope.model.skillLevel === 'ONE' || scope.model.skillLevel === 'TWO') {
                                scope.damage = 0;
                                scope.damagePool = 0;
                            } else {
                                scope.damage =  Math.round(scope.damage * .5);
                                scope.damagePool = Math.round(scope.damagePool * .5);
                            }
                        }
                    }
                    
                    scope.spellCastSucess(false, 0, madEvoker, 0, null, markUse);
                } else {
                    if (scope.targetIndex === 1) {
                        scope.spellCastSucess(true, 0, false, 0, null, markUse);
                    } else {
                        scope.spellCastSucess(false, 0, false, 0, null, markUse);
                    }
                    scope.closeModal();
                }
            }; 
            
            scope.useSkillAsScroll = function(primaryAmount, seconaryHealAmount, markUse) {
                if (scope.model.skillType === 'HEAL') {
                    var totalHeal = ((scope.skillCheckIndex === 0)?scope.model.maxEffect:scope.model.minEffect);
                    
                    if (scope.hasBuff()("Spell Surge")) {
                        if(scope.skillCheckIndex === 0)
                            totalHeal += scope.model.maxEffect;
                        else
                           totalHeal += scope.model.minEffect; 
                    }

                    if (scope.secondaryTargetIndex !== 0) {
                        scope.primaryHealAmount = primaryAmount;
                        scope.seconaryHealAmount = seconaryHealAmount;

                        if ((+scope.primaryHealAmount + +scope.seconaryHealAmount) !== totalHeal) {
                            scope.healError = true;
                        } else {
                            var selfHeal = 0;
                            if (scope.targetIndex === 1) {
                                selfHeal = scope.primaryHealAmount;
                            }
                            if (scope.secondaryTargetIndex === 2) {
                                selfHeal = scope.seconaryHealAmount;
                            }
                            
                            var buff = scope.hasBuff()("Spell Surge");
                            if (buff !== null) {
                                scope.removeBuff()(buff);
                            }

                            scope.spellCastSucess(selfHeal > 0, selfHeal, false, 0, null, markUse);
                        }
                    } else {
                        scope.primaryHealAmount = (scope.model.aoe) ? ((scope.skillCheckIndex === 0)?scope.model.maxEffect:scope.model.minEffect) : totalHeal;
                        scope.seconaryHealAmount = 0;
                        var selfHeal = 0;
                        scope.healPool = (scope.model.aoe) ? (scope.hasBuff()("Spell Surge")) ?((scope.skillCheckIndex === 0)?scope.model.maxEffect:scope.model.minEffect) : scope.characterContext.stats.spellHeal : 0;

                        if (scope.targetIndex === 1 || scope.model.skillTarget === 'PARTY') {
                            selfHeal = scope.primaryHealAmount;
                        }

                        var buff = scope.hasBuff()("Spell Surge");
                        if (buff !== null) {
                            scope.removeBuff()(buff);
                        }

                        scope.spellCastSucess(selfHeal > 0, selfHeal, false, 0, null, markUse);
                    }
                } else if (scope.model.skillType === 'DAMAGE') {
                    var dmg = ((scope.skillCheckIndex === 0)?scope.model.maxEffect:scope.model.minEffect);   
                    var totalDamage = dmg;
                    var madEvoker = false;
                    
                    if (scope.madEvokerIndex === 1) {
                        madEvoker = true;
                        if (scope.model.aoe)
                            scope.damagePool = dmg;
                        else 
                            totalDamage += dmg;
                    }
                    
                    var buff = scope.hasBuff()("Spell Surge");
                    if (buff !== null) {
                        totalDamage += dmg;
                        scope.removeBuff()(buff);
                    }
                    
                    scope.damage = totalDamage;
                    
                    scope.applyDr();
                    
                    if(scope.monster.monsterEffects) {
                        if(scope.model.skillLevel === 'ZERO' && scope.monster.monsterEffects.includes("PHASING_NORMAL")) {
                            scope.damage = 0;
                            scope.damagePool = 0;
                        } else if((scope.model.skillLevel === 'ZERO' || scope.model.skillLevel === 'ONE')  && scope.monster.monsterEffects.includes("PHASING_HARDCORE")) {
                            scope.damage = 0;
                            scope.damagePool = 0;
                        } else if((scope.model.skillLevel === 'ZERO' || scope.model.skillLevel === 'ONE' || scope.model.skillLevel === 'TWO')  && scope.monster.monsterEffects.includes("PHASING_NIGHTMARE")) {
                            scope.damage = 0;
                            scope.damagePool = 0;
                        } else if(scope.monster.monsterEffects.includes("PHASING_EPIC")) {
                            if (scope.model.skillLevel === 'ZERO' || scope.model.skillLevel === 'ONE' || scope.model.skillLevel === 'TWO') {
                                scope.damage = 0;
                                scope.damagePool = 0;
                            } else {
                                scope.damage =  Math.round(scope.damage * .5);
                                scope.damagePool = Math.round(scope.damagePool * .5);
                            }
                        }
                    }
                    
                    scope.spellCastSucess(false, 0, madEvoker, 0, null, markUse);
                } else if (scope.model.skillType === 'DAMAGE_RANGE_AC_15') {
                    var dmg = ((scope.skillCheckIndex === 0)?scope.model.maxEffect:scope.model.minEffect);   
                    var totalDamage = dmg;
                    var madEvoker = false;

                    scope.hitRollNatural = scope.roll()(scope.monster);
                    scope.hitRoll = scope.hitRollNatural + scope.characterContext.stats.rangeHit;
                    scope.hitSuccess =  scope.hitRoll >= 15;
                    
                    if (scope.madEvokerIndex === 1) {
                        madEvoker = true;
                        totalDamage += dmg;
                    }
                    
                    var buff = scope.hasBuff()("Spell Surge");
                    if (buff !== null) {
                        totalDamage += dmg;
                        scope.removeBuff()(buff);
                    } else if (scope.sharpen === 1 && scope.hitRollNatural >= 18) {
                        totalDamage = totalDamage*2;
                    }
           
                    scope.damage = totalDamage;
                    
                    scope.applyDr();
                    
                    if(scope.monster.monsterEffects) {
                        if (scope.monster.monsterEffects.includes("INCORPOREAL")) {
                            if (!scope.hasBuff()("Ignore Incorporeal") && (Math.floor(Math.random() * 2) === 0)) {
                                scope.damage = 0;
                                scope.damagePool = 0;
                            }
                        } else if(scope.model.skillLevel === 'ZERO' && scope.monster.monsterEffects.includes("PHASING_NORMAL")) {
                            scope.damage = 0;
                            scope.damagePool = 0;
                        } else if((scope.model.skillLevel === 'ZERO' || scope.model.skillLevel === 'ONE')  && scope.monster.monsterEffects.includes("PHASING_HARDCORE")) {
                            scope.damage = 0;
                            scope.damagePool = 0;
                        } else if((scope.model.skillLevel === 'ZERO' || scope.model.skillLevel === 'ONE' || scope.model.skillLevel === 'TWO')  && scope.monster.monsterEffects.includes("PHASING_NIGHTMARE")) {
                            scope.damage = 0;
                            scope.damagePool = 0;
                        } else if(scope.monster.monsterEffects.includes("PHASING_EPIC")) {
                            if (scope.model.skillLevel === 'ZERO' || scope.model.skillLevel === 'ONE' || scope.model.skillLevel === 'TWO') {
                                scope.damage = 0;
                                scope.damagePool = 0;
                            } else {
                                scope.damage =  Math.round(scope.damage * .5);
                                scope.damagePool = Math.round(scope.damagePool * .5);
                            }
                        }
                    }
                    
                    scope.spellCastSucess(false, 0, madEvoker, 0, null, markUse);
                } else {
                    if (scope.targetIndex === 1) {
                        scope.spellCastSucess(true, 0, false, 0, null, markUse);
                    } else {
                        scope.spellCastSucess(false, 0, false, 0, null, markUse);
                    }
                    scope.closeModal();
                }
            }; 
            
            scope.applyDr = function() {
                var damageDelta = 0;
                
                if (scope.monster.rangeDr !== 0) {
                    damageDelta += scope.monster.rangeDr;
                } if (scope.monster.universalDr !== 0) {
                    damageDelta += scope.monster.universalDr;
                } 
                
                if (scope.element !== -1) {
                    if (scope.monster.fire !== 0 && scope.element === 1) {
                        damageDelta += scope.monster.fire;
                    } if (scope.monster.cold !== 0 && scope.element === 0) {
                        damageDelta += scope.monster.cold;
                    } if (scope.monster.shock !== 0 && scope.element === 2) {
                        damageDelta += scope.monster.shock;
                    }
                } else {
                    if (scope.monster.fire !== 0 && scope.model.fire) {
                        damageDelta += scope.monster.fire;
                    } if (scope.monster.cold !== 0 && scope.model.cold) {
                        damageDelta += scope.monster.cold;
                    } if (scope.monster.shock !== 0 && scope.model.shock) {
                        damageDelta += scope.monster.shock;
                    } if (scope.monster.sonic !== 0 && scope.model.sonic) {
                        damageDelta += scope.monster.sonic;
                    } if (scope.monster.poison !== 0 && scope.model.poison) {
                        damageDelta += scope.monster.poison;
                    } if (scope.monster.sacred !== 0 && scope.model.sacred) {
                        damageDelta += scope.monster.sacred;
                    } if (scope.monster.darkrift !== 0 && scope.model.darkrift) {
                        damageDelta += scope.monster.darkrift;
                    } if (scope.monster.acid !== 0 && scope.model.acid) {
                        damageDelta += scope.monster.acid;
                    }
                }
                
                if (damageDelta !== 0) {
                    if (scope.damagePool === 0) {
                        if (damageDelta < 0) {
                            if (damageDelta*-1 > scope.damage)
                                scope.damage += scope.damage;
                            else
                                scope.damage += damageDelta*-1;
                        } else 
                            scope.damage -= damageDelta; 
                    } else if (scope.damagePool > 0) {
                        if (damageDelta < 0) {
                            if (damageDelta*-1 > scope.damagePool) {
                                var poolDelta = damageDelta*-1 - scope.damagePool;
                                scope.damagePool += scope.damagePool;
                                if (poolDelta > scope.damage)
                                    scope.damagePool += scope.damage;
                                else
                                    scope.damagePool += poolDelta;
                            } else
                                scope.damagePool += damageDelta*-1;
                        } else
                            scope.damagePool -= damageDelta; 
                    }
                    if (scope.damagePool < 0) {
                        scope.damage -= scope.damagePool; 
                        scope.damagePool = 0;
                    }
                    if (scope.damage < 0) {
                        scope.damage = 0;
                    }
                }
            };
            
            scope.markSkill = function() {
                scope.useAbility()(scope.model.id, false, 0, false, 0, null, true, true, true, 0, 0, 0, false);
                scope.closeModal();
            }; 
            
            scope.spellCastSucess = function(selfTarget, healAmount, madEvoker, lohNumber, inGameEffect, markUse) {
                if (scope.conserve === 1)
                    markUse = false;
                if (scope.castSequenceIndex === 1 && (scope.characterContext.characterClass === 'WIZARD' || scope.characterContext.characterClass === 'ELF_WIZARD')) {
                    scope.closeModal();
                } else {
                    scope.useAbility()(scope.model.id, selfTarget, healAmount, madEvoker, lohNumber, inGameEffect, markUse, false, scope.hitSuccess, scope.hitRollNatural, scope.hitRoll, scope.damage + scope.damagePool, false);
                    
                    if (scope.characterContext.rollerId !== null) 
                        scope.closeModal();
                    else
                        scope.spellCast = true;
                }
            };
            
            scope.unuseSkill = function() {
                scope.unuseAbility()(scope.model.id);
                scope.closeModal();
            };  
            
            scope.setElement = function(index) {
                if (scope.element === index)
                    scope.element = -1;
                else
                    scope.element = index;
            };  
            
            scope.setConserve = function(index) {
                scope.conserve = index;
            }; 
            
            scope.setFork = function(index) {
                scope.fork = index;
            };  
            
            scope.setIntensify = function(index) {
                scope.intensify = index;
            };
            
            scope.setSharpen = function(index) {
                scope.sharpen = index;
            };
            
            scope.continueCast = function() {
                scope.initSkillCheck();
                scope.magePowers = false;
            };
            
            scope.tryAnswer = function(answer) {
                if (scope.skillCheckIndex === 3) {
                    if (scope.skillCheckAnswer === answer)
                        scope.skillCheckIndex = 0;
                    else
                        scope.skillCheckIndex = 1;
  
                    scope.skillCheckSrc = scope.skillCheckSrc.replace('_ask', '');

                    $timeout(function(){
                        scope.skillCheck = false;
                    },2000);
                }
            };
            
            scope.initSkillCheck = function() {
                if (scope.model.minEffect === scope.model.maxEffect)
                    return;
                
                if (scope.characterContext.characterClass === 'WIZARD' || scope.characterContext.characterClass === 'ELF_WIZARD') {
                    var wizardQuestions = [ { src: 'images/wizard1_ask.png' , answer: 'Mixology'}, { src: 'images/wizard2_ask.png' , answer: 'Tempology'}, 
                        { src: 'images/wizard3_ask.png' , answer: 'Planalogy'}, { src: 'images/wizard4_ask.png' , answer: 'Herbology'}, { src: 'images/wizard5_ask.png' , answer: 'Artificery'},
                        { src: 'images/wizard6_ask.png' , answer: 'Necromancy'}, { src: 'images/wizard7_ask.png' , answer: 'Dracology'}, { src: 'images/wizard8_ask.png' , answer: 'Illusion'},
                        { src: 'images/wizard9_ask.png' , answer: 'Enchantment'}, { src: 'images/wizard10_ask.png' , answer: 'Transmutation'}, { src: 'images/wizard11_ask.png' , answer: 'Evocation'},
                        { src: 'images/wizard12_ask.png' , answer: 'Divination'}, { src: 'images/wizard13_ask.png' , answer: 'Abjuration'}, { src: 'images/wizard14_ask.png' , answer: 'Conjuration'}];
                    var wizardOptions = [ 'Mixology', 'Tempology', 'Planalogy', 'Herbology', 'Artificery', 'Necromancy', 'Dracology', 'Illusion', 'Enchantment', 'Transmutation', 
                        'Evocation', 'Divination', 'Abjuration', 'Conjuration' ];
                    var randomElement = wizardQuestions[Math.floor(Math.random() * wizardQuestions.length)];
                    
                    scope.skillCheckSrc = randomElement.src;    
                    scope.skillCheckAnswer = randomElement.answer;
                    scope.skillCheckOptions = wizardOptions.sort(() => .5 - Math.random()).slice(0,6);
                    
                    if (!scope.skillCheckOptions.includes(scope.skillCheckAnswer)) {
                        scope.skillCheckOptions.pop();
                        scope.skillCheckOptions.push(scope.skillCheckAnswer);
                    }
                } else if (scope.characterContext.characterClass === 'DRUID') {
                    var druidQuestions = [ { src: 'images/Druid1_ask.png' , answer: 'Chipmunk'}, { src: 'images/Druid2_ask.png' , answer: 'Centaur'}, 
                        { src: 'images/Druid3_ask.png' , answer: 'Black Rat'}, { src: 'images/Druid4_ask.png' , answer: 'Bison'}, { src: 'images/Druid5_ask.png' , answer: 'Bear'},
                        { src: 'images/Druid6_ask.png' , answer: 'Vorpal Bunny'}, { src: 'images/Druid7_ask.png' , answer: 'Red Deer'}, { src: 'images/Druid8_ask.png' , answer: 'Otter'},
                        { src: 'images/Druid9_ask.png' , answer: 'Muskrat'}, { src: 'images/Druid10_ask.png' , answer: 'Lamia'}, { src: 'images/Druid11_ask.png' , answer: 'Harpy'},
                        { src: 'images/Druid12_ask.png' , answer: 'Dire Rat'}];
                    var druidOptions = [ 'Chipmunk', 'Centaur', 'Black Rat', 'Bison', 'Bear', 'Vorpal Bunny', 'Red Deer', 'Otter', 'Muskrat', 'Lamia', 
                        'Harpy', 'Dire Rat' ];
                    var randomElement = druidQuestions[Math.floor(Math.random() * druidQuestions.length)];
                    
                    scope.skillCheckSrc = randomElement.src;    
                    scope.skillCheckAnswer = randomElement.answer;
                    scope.skillCheckOptions = druidOptions.sort(() => .5 - Math.random()).slice(0,6);
                    
                    if (!scope.skillCheckOptions.includes(scope.skillCheckAnswer)) {
                        scope.skillCheckOptions.pop();
                        scope.skillCheckOptions.push(scope.skillCheckAnswer);
                    }
                } else if (scope.characterContext.characterClass === 'CLERIC') {
                    var clericQuestions = [ { src: 'images/cleric1_ask.png' , answer: 'Yeelab'}, { src: 'images/cleric2_ask.png' , answer: 'Grimnor'}, 
                        { src: 'images/cleric3_ask.png' , answer: 'Avalava'}, { src: 'images/cleric4_ask.png' , answer: 'Cresno'}, { src: 'images/cleric5_ask.png' , answer: 'Pion'},
                        { src: 'images/cleric6_ask.png' , answer: 'Selton'}, { src: 'images/cleric7_ask.png' , answer: 'Candar'}, { src: 'images/cleric8_ask.png' , answer: 'Dyedar'},
                        { src: 'images/cleric9_ask.png' , answer: 'Cavoc'}, { src: 'images/cleric10_ask.png' , answer: 'Balot'}, { src: 'images/cleric11_ask.png' , answer: 'Lazlo'},
                        { src: 'images/cleric12_ask.png' , answer: 'Gazal'}];
                    var clericOptions = [ 'Yeelab', 'Grimnor', 'Avalava', 'Cresno', 'Pion', 'Selton', 'Candar', 'Dyedar', 'Cavoc', 'Balot', 
                        'Lazlo', 'Gazal' ];
                    var randomElement = clericQuestions[Math.floor(Math.random() * clericQuestions.length)];
                    
                    scope.skillCheckSrc = randomElement.src;    
                    scope.skillCheckAnswer = randomElement.answer;
                    scope.skillCheckOptions = clericOptions.sort(() => .5 - Math.random()).slice(0,6);
                    
                    if (!scope.skillCheckOptions.includes(scope.skillCheckAnswer)) {
                        scope.skillCheckOptions.pop();
                        scope.skillCheckOptions.push(scope.skillCheckAnswer);
                    }
                } else if (scope.characterContext.characterClass === 'BARD') {
                    var BardQuestions = [ { src: 'images/Bard1_ask.png' , answer: 'Justice'}, { src: 'images/Bard2_ask.png' , answer: 'Friendship'}, 
                        { src: 'images/Bard3_ask.png' , answer: 'Wit'}, { src: 'images/Bard4_ask.png' , answer: 'Truthfulness'}, { src: 'images/Bard5_ask.png' , answer: 'Friendliness'},
                        { src: 'images/Bard6_ask.png' , answer: 'Equanmity'}, { src: 'images/Bard7_ask.png' , answer: 'Honor'}, { src: 'images/Bard8_ask.png' , answer: 'Pride'},
                        { src: 'images/Bard9_ask.png' , answer: 'Magnificence'}, { src: 'images/Bard10_ask.png' , answer: 'Liberality'}, { src: 'images/Bard11_ask.png' , answer: 'Temperance'},
                        { src: 'images/Bard12_ask.png' , answer: 'Courage'}];
                    var BardOptions = [ 'Justice', 'Friendship', 'Wit', 'Truthfulness', 'Friendliness', 'Equanmity', 'Honor', 'Pride', 'Magnificence', 'Liberality', 
                        'Temperance', 'Courage' ];
                    var randomElement = BardQuestions[Math.floor(Math.random() * BardQuestions.length)];
                    
                    scope.skillCheckSrc = randomElement.src;    
                    scope.skillCheckAnswer = randomElement.answer;
                    scope.skillCheckOptions = BardOptions.sort(() => .5 - Math.random()).slice(0,6);
                    
                    if (!scope.skillCheckOptions.includes(scope.skillCheckAnswer)) {
                        scope.skillCheckOptions.pop();
                        scope.skillCheckOptions.push(scope.skillCheckAnswer);
                    }
                }
                
                scope.skillCheck = true;

            };
            
            scope.closeModal = function() {
                scope.modalInstance.close();
                scope.targetIndex = 0;
                scope.madEvokerIndex = 0;
                scope.loh10Index = 0;
                scope.loh15Index = 0;
                scope.hitRoll = 0;
                scope.hitRollNatural = 0;
                scope.hitSuccess = true;
                scope.damage = 0;
                scope.damagePool = 0;
                scope.secondaryTargetIndex = 0;
                scope.skillCheckIndex = 3;
                scope.primaryHealAmount = 0;
                scope.seconaryHealAmount = 0;
                scope.lohUseAmount = 1;
                scope.spellCast = false;
                scope.healError = false;
                scope.magePowers = false;
                scope.skillCheck = false;
                scope.skillCheckSrc = '';
                scope.skillCheckAnswer = '';
                scope.skillCheckOptions = [];
                scope.element = -1;
                scope.conserve = 0;
                scope.fork = 0;
                scope.intensify = 0;
                scope.sharpen = 0;
            };
        },
        templateUrl:'common/skillUser/desktop/skillUserDesktopTemplate-@{TDCC_VERSION}.html'
    };
}]);