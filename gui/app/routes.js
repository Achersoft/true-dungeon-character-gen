'use strict';

angular.module('main')
    .config(['$stateProvider', '$urlRouterProvider',
        function ($stateProvider, $urlRouterProvider) {  
            // For any unmatched url, redirect to /state1
            $urlRouterProvider.otherwise("/dashboard");
            //
            // Now set up the states

            $stateProvider
                .state('login',{
                    templateUrl: 'login/login.html',
                    controller:'LoginCtrl'
                })
                
                .state('setSelection', {
                    url: '/sets',
                    templateUrl: 'set/set.html',
                    controller:'CardCtrl'
                })

                .state('logout',{
                    templateUrl: 'login/logout.html',
                })

                .state('dashboard', {
                    url: '/dashboard',
                    templateUrl: 'dashboard/dashboard.html'
                })

                .state('candidateList', {
                    url: '/candidateList',
                    templateUrl: 'candidate/list/candidateList.html',
                    controller:'CandidateListCtrl'
                })

                .state('createCandidateFull',{
                    url: '/createCandidateFull',
                    templateUrl: 'candidate/detail/candidateFull.html',
                    controller:'CandidateDetailCtrl',
                    resolve:{
                        candidatePromise:
                            ['CandidateFactory', 'CandidateContainerFactory',
                            function(candidateFactory, candidateContainerFactory){
                                candidateContainerFactory.set(candidateFactory.newCandidate(), true);
                                candidateContainerFactory.get().setAddPreemploymentStatus(true);
                                return null;
                            }]
                    }
                })

                .state('createCandidateQuick',{
                    url: '/createCandidateQuick',
                    templateUrl: 'candidate/detail/candidateQuick.html',
                    controller:'CandidateDetailCtrl',
                    resolve:{
                        candidatePromise:
                            ['CandidateFactory', 'CandidateContainerFactory',
                            function(candidateFactory, candidateContainerFactory){
                                candidateContainerFactory.set(candidateFactory.newCandidate(), true);
                                return null;
                            }]
                    }
                })
                
                .state('editCandidate',{
                    url: '/editCandidate/{restPath:hexEncoded}?{name}',
                    templateUrl:'candidate/detail/candidateFull.html',
                    controller:'CandidateDetailCtrl',
                    resolve:{
                        candidatePromise:
                            ['CandidateFactory',
                            'CandidateContainerFactory',
                            '$stateParams',
                            function(candidateFactory, candidateContainerFactory, $stateParams){
                                return candidateFactory.getCandidate($stateParams.restPath)
                                        .then(function(response){
                                            candidateContainerFactory.set(response.data, true);
                                        });
                            }]
                    }
                })
                
                .state('viewCandidate',{
                    url: '/viewCandidate/{restPath:hexEncoded}?{name}',
                    templateUrl:'candidate/detail/candidateFull.html',
                    controller:'CandidateDetailCtrl',
                    resolve:{
                        candidatePromise:
                            ['CandidateFactory',
                            'CandidateContainerFactory',
                            '$stateParams',
                            function(candidateFactory, candidateContainerFactory, $stateParams){
                                return candidateFactory.getCandidate($stateParams.restPath)
                                        .then(function(response){
                                            candidateContainerFactory.set(response.data, false);
                                        });
                            }]
                    }
                });

    }]);
