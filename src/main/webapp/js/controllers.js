'use strict';

/* Controllers */


var witbookerControllers = angular.module('witbooker.controllers', []);


witbookerControllers.controller('StepOne',
    ['$scope', '$stateParams', 'Api', '$rootScope', 'util', '$location', '$translate', "constants", "events", "tmhDynamicLocale", "$filter", "$state", "$http", "$window", 'md5','$modal',
        function ($scope, $stateParams, Api, $rootScope, util, $location, $translate, constants, events, tmhDynamicLocale, $filter, $state, $http, $window, md5,$modal) {

            if ($state && $state.params && $state.params.hasOwnProperty("error") && ($state.params.error === "paypalError" || $state.params.error === "sipayError")) {
                var ModalInstanceCtrl, modalInstance, errorCode;

                errorCode = $state.params.error;

                ModalInstanceCtrl = ['$scope', '$modalInstance', function ($scope, $modalInstance) {
                    $scope.closeInfo = function () {
                        $modalInstance.close();
                    };
                }];
                $scope.message = {body: "trans.error." + errorCode};

                modalInstance = $modal.open({
                    templateUrl: constants.assetsDir + 'partials/step_one/misc/genericModal.html',
                    controller: ModalInstanceCtrl,
                    scope: $scope
                });
            }


            $scope.currentState = $state.current.name;
            $scope.$on(events.TRANSITION_TO_STEP2, function (scope, data) {
                var redirectURL = STEP2_URL.replace(new RegExp("TICKERPLACEHOLDER", 'g'), data.establishment).replace("LANGUAGEPLACEHOLDER", $rootScope.witbookerParams.representation.locale);
                //$rootScope.witbookerParams.representation.iframeMode &&
                if (window.location.protocol !== "https:") {
                    redirectURL = util.attachGetParams(redirectURL, {witif: 0});
                    window.parent.location.href = redirectURL;
                } else if (data.establishment !== $rootScope.establishment.ticker) { //its a chain
                    window.location.href = redirectURL;
                } else {
                    $state.transitionTo('stepTwo', data);
                }
            });


            $scope.$on(events.REMOVED_FROM_CART, function (scope, reservation) {
                util.updateChildrenAndParentInventories($rootScope.witbookerParams.regularParams.startDate, $rootScope.witbookerParams.regularParams.endDate, $rootScope.establishment, reservation, "+1", true, function (affectedAccommodations) {
                    if (affectedAccommodations) {
                        angular.extend($rootScope.affectedAccommodations, affectedAccommodations);
                        $rootScope.affectedAccommodations.counter = $rootScope.affectedAccommodations.counter ? $rootScope.affectedAccommodations.counter + 1 : 1;
                    }
                });
            });


            //viewButtons list, map
            $scope.viewButtoms = function () {
                $scope.radioModel = "map";
                if ($state.current.name == "stepOne.list")
                    $scope.radioModel = "list";
            };

            $scope.changeLanguage = function (langKey) {
                $translate.use(langKey);
            };

            $scope.constants = constants;
            $scope.interceptClick = function (e) {
                if (e) {
                    if (e.target.nodeName == 'A' && e.target.hasOwnProperty('href')) {
                        window.open(e.target.href);
                    }
                    e.preventDefault();
                    e.stopPropagation();
                }
            };

            function processEstablishmentData(establishmentInfo) {
                var establishment = establishmentInfo.establishment
                $rootScope.establishment = establishment;
                $rootScope.representation = establishmentInfo.representation;
                $rootScope.isChain = $scope.isChain = establishment && establishment.hasOwnProperty("establishments") && establishment.establishments.length > 0;
                $scope.showMapButton = false
                $scope.util = util;

                function retrievePins(establishment, pins, scope) {
                    if (establishment && !establishment.hasOwnProperty("establishments")) {
                        if (typeof establishment.contactInfo.latitude != "undefined" && typeof establishment.contactInfo.longitude != "undefined") {
                            if (!scope.showMapButton)
                                scope.showMapButton = true
                            pins[establishment.ticker] = {
                                establishment: establishment,
                                lat: establishment.contactInfo.latitude,
                                lon: establishment.contactInfo.longitude
                            }
                        }
                    } else {
                        angular.forEach(establishment.establishments, function (value, key) {
                            retrievePins(value, pins, scope);
                        });
                    }
                    return pins;
                }

                $scope.pins = retrievePins(establishment, {}, $scope)

                $rootScope.showExtraDesc = function (establishment, type, e) {
                    $scope.interceptClick(e);
                    establishment.visualState = establishment.hasOwnProperty('visualState') ? establishment.visualState : {showPanel: true};
                    establishment.visualState.showPanel = (!establishment.visualState.showPanel || establishment.visualState.panelType == type ) ? !establishment.visualState.showPanel : establishment.visualState.showPanel;
                    establishment.visualState.panelType = type;
                };

                $scope.toggleAccordion = function (establishment) {
                    establishment.isOpen = establishment.hasOwnProperty('isOpen') ? !establishment.isOpen : true;
                };

                configureAccordion($rootScope.establishment);
            }

            function configureAccordion(establishment) {
                /*TODO: this should be received from grails */
                var accordionConfig = $rootScope.witbookerParams.representation.collapsedAccordionsConfiguration.split(":");
                var accordionConfigString = $rootScope.witbookerParams.representation.collapsedAccordionsConfiguration;
                $rootScope.witbookerParams.representation.showEstablishmentAccordionHeader = accordionConfig[0] == "1" ||
                    establishment.hasOwnProperty("establishments");

                $rootScope.witbookerParams.representation.uncollapseAccommodation = (!establishment.hasOwnProperty("establishments") && (
                        accordionConfigString == "1:1" ||
                        accordionConfigString == "1:2" ||
                        accordionConfigString == "0:1" ||
                        accordionConfigString == "0:2"
                    )) ||
                    (establishment.hasOwnProperty("establishments") && (
                        accordionConfigString == "1:2" ||
                        accordionConfigString == "1:1" ||
                        accordionConfigString == "0:2"
                    ));
                $rootScope.witbookerParams.representation.uncollapseIventoryLIne = accordionConfig[1] == "2";
                util.setEstablishmentsProperties(establishment, {isOpen: $rootScope.witbookerParams.representation.uncollapseAccommodation})
                util.setAccommodationsIsOpenProperty(establishment,
                    $rootScope.witbookerParams.representation.maxLineNumbersBeforeCollapse,
                    $rootScope.witbookerParams.representation.uncollapseIventoryLIne
                );
            }

            var proccessingFunction = processEstablishmentData(data);

            $rootScope.updateSessionAsync = function (witbookerParams, cb) {
                $http.post(UPDATE_SESSION_URL, witbookerParams).success(function () {
                    cb();
                }).error(function () {

                });
            }
        }]);


witbookerControllers.controller('StepTwo',
    ['$scope', '$stateParams', 'Api', '$rootScope', 'util', '$location', '$translate', "constants", "events",
        "tmhDynamicLocale", "$filter", "$state", "$http", "$window", "md5", "$modal","$interval","sipay",
        function ($scope, $stateParams, Api, $rootScope, util, $location, $translate, constants, events,
                  tmhDynamicLocale, $filter, $state, $http, $window, md5, $modal,$interval,sipay) {

            $scope.currentState = $state.current.name;
            var hotelTicker = $state.params.establishment;

            var d = new Date();
            $scope.year = d.getFullYear();

            $scope.util = util;
            $scope.customerData = {
                name: undefined,
                lastName: undefined,
                email: undefined,
                confirmationMail: undefined,
                address: undefined,
                country: undefined,
                phone: undefined,
                comments: undefined,
                receiveOffers: undefined,
                passport: undefined
            };

            $scope.paymentData = {
                creditCardData: {
                    ccholder: undefined,
                    cckind: undefined,
                    ccnumber: undefined,
                    ccsecuritycode: undefined,
                    ccvalidtomonth: undefined,
                    ccvalidtoyear: undefined
                },
                ccvalidtomonth: undefined,
                acceptconditions: false,
                tpv1: undefined,
                paymentType: undefined
            };

            $scope.activateWithDummyData = function ($event) {
                var result = false;
                if (hotelTicker == "hoteldemo.com.v6") {
                    if ($event.keyCode === 35) {
                        $event.preventDefault();
                        // TAB
                        if ($event.shiftKey) {
                            result = $scope.fullWithDummieData(true);
                        }
                    }
                    if ($event.keyCode === 36) {
                        $event.preventDefault();
                        // TAB
                        if ($event.shiftKey) {
                            result = $scope.fullWithDummieData(false);
                        }
                    }
                }
                return result;
            };


            $scope.$on(events.REMOVED_FROM_CART, function (scope, reservation) {
                util.updateChildrenAndParentInventories($rootScope.witbookerParams.regularParams.startDate, $rootScope.witbookerParams.regularParams.endDate, $rootScope.establishment, reservation, "+1", true, function (affectedAccommodations) {
                    if (affectedAccommodations) {
                        angular.extend($rootScope.affectedAccommodations, affectedAccommodations);
                        $rootScope.affectedAccommodations.counter = $rootScope.affectedAccommodations.counter ? $rootScope.affectedAccommodations.counter + 1 : 1;
                    }
                });
                if ($rootScope.cart.reservations && $rootScope.cart.reservations.length <= 0) {
                    $scope.goToStepOne();
                }
            });


            $scope.$on(events.SUBMIT_PAYMENT_DATA_FORM, function (scope, data) {
                if ($scope.form.$invalid)
                    return;
                if (data.paymentType == constants.payment.type.paypal_ec) {
                    paypal.checkout.initXO();
                }else if (data.paymentType == constants.payment.type.tctoken){
                    sipay.initXO();
                }else{
                    util.addLoading(".mainStepContainer");
                }

                $scope.paymentData.paymentType = data.paymentType;
                $http.post(STEP2_PROCESS_RESERVATION_URL, {
                    customerData: $scope.customerData,
                    paymentData: $scope.paymentData,
                    establishment: {ticker: $rootScope.establishment.ticker}
                }).success(function (response) {
                    if (response.id) {
                        $rootScope.cart = {reservations: [], processedReservations: {}};
                        var redirectURL;
                        if (data.paymentType == constants.payment.type.pointOfSale) {
                            redirectURL = util.generateStep3URL(STEP3_TPV_URL, $rootScope.establishment.ticker, $rootScope.witbookerParams.representation.locale, response.id);
                        } else {
                            redirectURL = util.generateStep3URL(STEP3_CONFIRMATION_URL, $rootScope.establishment.ticker, $rootScope.witbookerParams.representation.locale, response.id);
                        }
                        if (data.paymentType != constants.payment.type.paypal_ec && data.paymentType != constants.payment.type.tctoken) {
                            redirectURL = util.attachGetParams(redirectURL, util.createLegacyParamsObject($rootScope.witbookerParams.representation));
                            $window.location.href = redirectURL;
                        }

                    } else {
                        if (data.paymentType == constants.payment.type.paypal_ec) {
                            var cancelUrl = STEP2_URL.replace(new RegExp("TICKERPLACEHOLDER", 'g'), $rootScope.establishment.ticker).replace("LANGUAGEPLACEHOLDER", $rootScope.witbookerParams.representation.locale);
                            $http.post(STEP2_PAYPAL_EXPRESS_CHECKOUT, {
                                ticker: $rootScope.establishment.ticker,
                                cancelUrl: cancelUrl
                            })
                                .success(function (paypalData) {
                                    paypal.checkout.startFlow(paypalData.paypalRedirectUrl);
                                    var win = paypal.checkout.win;

                                    var timer = setInterval(checkChild, 500);

                                    function checkChild() {
                                        if (win.closed) {
                                            util.removeLoading(".mainStepContainer");
                                            clearInterval(timer);
                                        }
                                    }


                                })
                                .error(function (paypalData) {
                                    util.removeLoading(".mainStepContainer");
                                    alert("Unexpected Error");
                                })
                        }

                        if (data.paymentType == constants.payment.type.tctoken) {
                            var sipayURL = STEP2_SIPAY.replace(new RegExp("TICKERPLACEHOLDER", 'g'), $rootScope.establishment.ticker).replace("LANGUAGEPLACEHOLDER", $rootScope.witbookerParams.representation.locale);
                            sipay.startFlow(sipayURL);
                        }




                    }
                }).error(function () {
                    util.removeLoading(".mainStepContainer");
                });
            });

            $rootScope.intervalCount=0;

            var getSipayIframe=function(){

                console.log("Asking for sipay Data at: "+ new Date());

                var setIntervalSipay=true;

                $http.post(STEP2_SIPAY, {
                    ticker: $rootScope.establishment.ticker
                }).success(function (sipayData) {

                    if (sipayData && sipayData.hasOwnProperty("sipayIframe") && sipayData.hasOwnProperty("sipayStart")) {

                        console.log("Got Sipay Data at ");

                        var timeoutTime = new Date(sipayData["sipayStart"]);

                        console.log("Current iframe started at: "+ timeoutTime);

                        $rootScope.sipayStart = new Date(timeoutTime.getTime());

                        timeoutTime=timeoutTime.setSeconds(timeoutTime.getSeconds()+constants.payment.sipay.timeoutInterval);

                        console.log("Current iframe expires at: "+ timeoutTime);

                        if(timeoutTime<=new Date()){ //sipay iframe expired

                            console.log("Current iframe is expired : "+ timeoutTime);

                            return getSipayIframe();
                        }else{

                            console.log("Setting new interval? "+setIntervalSipay);

                            if(setIntervalSipay){

                                var timeDiff = Math.abs(timeoutTime - new Date());

                                console.log("Timediff is "+timeDiff/1000);

                                var intervalCounty= $rootScope.intervalCount++;

                                console.log("Setting interval# : "+intervalCounty);

                                var destroyInterval=$interval(getSipayIframe, timeDiff, 1,true);
                            }
                        }

                        $rootScope.sipayActivated = true;
                        $rootScope.sipayIframe = sipayData.sipayIframe;
                    }
                }).error(function () {

                });
            };

            //if(!$rootScope.hasOwnProperty("sipayStart")){
            //    getSipayIframe();
            //}else{
            //    var timeoutTime = new Date($rootScope.sipayStart.getTime());
            //    timeoutTime=timeoutTime.setSeconds(timeoutTime.getSeconds()+constants.payment.sipay.timeoutInterval);
            //    if(timeoutTime<=new Date()) { //sipay iframe expired
            //        return getSipayIframe();
            //    }
            //}



            $http.get(SHOPPING_CART_DATA_URL, {
                params: {
                    hotelTicker: hotelTicker,
                    includeServices: true
                }
            }).success(function (cartData) {
                if (cartData && Object.prototype.toString.call(cartData.reservations) === '[object Array]' && cartData.reservations.length > 0) {
                    $rootScope.reservationData = cartData.reservations;
                    $rootScope.services = cartData.services;
                    $rootScope.$broadcast(events.RESERVATION_DATA_UPDATE, cartData.reservations);
                    /*TODO: USE CONSTANT FOR SIPAY IFRAME*/
                } else {
                    $scope.goToStepOne();
                }
            }).error(function () {

            });

            $scope.fullWithDummieData = function (submit) {
                $scope.customerData = {
                    "name": "Pepe",
                    "lastName": "Trueno",
                    "email": "m.carreno@witbooking.com",
                    "confirmationMail": "m.carreno@witbooking.com",
                    "address": "Aribau 240",
                    "country": {id: "AD", name: "Afganiston"},
                    "phone": "666777888",
                    "comments": "Test de reserva de witbooking",
                    "receiveOffers": "",
                    "passport": "77777777A"
                };

                $scope.paymentData = {
                    "creditCardData": {
                        "ccholder": "Pepe Trueno",
                        "cckind": {
                            "id": "Visa",
                            "name": "Visa"
                        },
                        "ccnumber": "4111111111111111",
                        "ccsecuritycode": "666",
                        "ccvalidtomonth": "12",
                        "ccvalidtoyear": "21"
                    },
                    "acceptconditions": true,
                    "tpv1": "",
                    paymentType: null
                };
                return submit;
            }

            fullWithDummieData = $scope.fullWithDummieData;

            $scope.goToStepOne = function () {
                $state.transitionTo('stepOne.list', {allreservationsinvalid: false});
            }

            $rootScope.$on(events.GO_TO_STEP1, function (scope, data) {
                $state.transitionTo('stepOne.list', {allreservationsinvalid: false});
            });

        }]);
var fullWithDummieData;