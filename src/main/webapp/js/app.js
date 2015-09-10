'use strict';

var Constants = {};
Constants.mealPlan = {};
Constants.mealPlan.breakfast={
    ticker: "HD",
    name: "With breakfast"
};
Constants.messages = {type: {}, position: {}};
Constants.messages.type.success = "SUCCESS";
Constants.messages.type.notice = "NOTICE";
Constants.messages.type.info = "INFO";
Constants.messages.type.error = "ERROR";
Constants.messages.position.top_inventory_step_1 = "TOP_INVENTORY_STEP_1";
Constants.messages.position.below_logo_step_1 = "BELOW_LOGO_STEP_1";
Constants.messages.position.below_personal_info_step_2 = "BELOW_PERSONAL_INFO_STEP_2";
Constants.messages.position.top_method_payment_step_2 = "TOP_METHOD_PAYMENT_STEP_2";
Constants.messages.position.modal_step_1 = "MODAL_STEP_1";
Constants.messages.position.modal_step_2 = "MODAL_STEP_2";

Constants.markups = {phase: {}, position: {}};
Constants.markups.phase.step1 = "STEP1";
Constants.markups.phase.step2 = "STEP2";
Constants.markups.phase.step3 = "STEP3";
Constants.markups.phase.all = "ALL";
Constants.markups.position.afterbeginbody = "FIRST_BODY";
Constants.markups.position.beforeendbody = "END_BODY";
Constants.markups.position.beforeendhead = "HEAD";


Constants.panel={};
Constants.panel.type={
    carousel:'carousel',
    description:'description',
    map:'map',
    discounts:'discounts',
    conditions:'conditions',
    chart:'chart'

}

Constants.assetsDir=ASSETS_DIR;
Constants.imgDir="/WitBooker/img/";
Constants.translationURL="/WitBooker/img/";
Constants.dateFormat="dd-MM-yyyy";
Constants.datePickerFormat="dd-mm-yyyy";


Constants.cart = {
    smallSize:"SMALL",
    largeSize:"LARGE"
}

Constants.serviceType = {
    room:"ROOM",
    person:"PERSON",
    units:"UNIT"
}


Constants.popover = {
    discount: {
        background: "ad0303"
    },
    reservationButton: {
        background: "000000"
    }
}
Constants.localeFilesDir="/WitBooker/i18n/";

Constants.filters={
    minStay:"minStay",
    minNotice:"minNotice",
    maxNotice:"maxNotice",
    maxStay:"maxStay",
    closed:"closed",
    visible:"visible",
    occupationType:"occupationType",
    checkInCheckOut:"checkInCheckOut",
    checkIn:"checkIn",
    checkOut:"checkOut",
    validity:"validity",
    availability:"availability",
    reservationClosed:"reservationClosed",
    filterPrefix:"filterBy",
    restricted:"restricted"
}

Constants.occupant={
    restriction:{
        TAKE_ADULT_TEENAGER_CHILD:"TAKE_ADULT_TEENAGER_CHILD",
        TAKE_ADULT_TEENAGER_CHILD_BABY:"TAKE_ADULT_TEENAGER_CHILD_BABY",
        TAKE_SENIOR_ADULT_TEENAGER_CHILD_BABY:"TAKE_SENIOR_ADULT_TEENAGER_CHILD_BABY",
        TAKE_ADULT_CHILD_BABY:"TAKE_ADULT_CHILD_BABY",
        TAKE_ADULT_CHILD:"TAKE_ADULT_CHILD",
        TAKE_ALL_PEOPLE:"TAKE_ALL_PEOPLE",
        NONE:"NONE"
    }
}

Constants.payment={
    type:{
        creditCard:"tcgarantia",
        tctoken:"tctoken",
        tpvtoken:"tpvtoken",
        transfer:"tr",
        pointOfSale:"tpv",
        creditCardOrTransfer:"TC_TR",
        paypal_ec:"paypal_ec",
        paypal_std:"paypal_std"
    },
    sipay:{
      timeoutInterval:300
    }
}

var Events={};
Events.UPDATE_AVAILABILITY="update_availability";
Events.QUERY_AVAILABILITY="query_availability";
Events.ADDED_TO_CART="added_to_cart";
Events.UPDATE_INVENTORIES_BY_CART="done_added_to_cart";
Events.CHANGE_LANGUAGE="change_language";
Events.CHANGE_LANGUAGE_DONE="change_language_done";
Events.REMOVED_FROM_CART="removed_from_cart";
Events.GO_TO_STEP1="go_to_step1";
Events.TRANSITION_TO_STEP2="transition_to_step2";
Events.UPDATE_SERVICE_RATE="update_service_rate";
Events.SUBMIT_PAYMENT_DATA_FORM="submit_payment_data_form";
Events.RESERVATION_DATA_UPDATE="reservation_data_update";
Events.UPDATE_SERVICE_QUANTITY="update_service_quantity";
Events.UPDATE_SERVICE_SELECTION="update_service_selection";
Events.CHANGE_CURRENCY="change_currency";



// Declare app level module which depends on filters, and services
angular.module('witbooker', [
        'ui.router',
        'ui.bootstrap',
        'ngAnimate',
        'ngSanitize',
        'pascalprecht.translate',
        'tmh.dynamicLocale',
        'witbooker.filters',
        'witbooker.directives',
        'witbooker.controllers',
        'witbookerServices',
        'paymentServices',
        'ngCacheBuster',
        'angular-md5'
    ]).constant('constants',
        Constants
    ).constant('events',
        Events
    ).config(['$stateProvider', '$urlRouterProvider',"constants", function ($stateProvider, $urlRouterProvider,constants) {
        $stateProvider
            .state('stepOne', {
                url: "/stepOne",
                templateUrl: constants.assetsDir+"partials/step_one.html",
                controller:"StepOne"
            })
            .state('stepOne.list', {
                url: "/list?allreservationsinvalid&error",
                templateUrl: constants.assetsDir+"partials/step_one/stepOne.list.html"
            })
            .state('stepOne.map', {
                url: "/map",
                templateUrl: constants.assetsDir+"partials/step_one/stepOne.map.html"
            })
            .state('stepTwo', {
                url: "/stepTwo/:establishment/",
                controller:"StepTwo",
                templateUrl: constants.assetsDir+"partials/stepTwo.html"
            })
            .state('stepTwoPaypal', {
                url: "/stepTwo/:establishment/:paypalData",
                controller:"StepTwo",
                templateUrl: constants.assetsDir+"partials/stepTwo.html"
            });

        $urlRouterProvider.otherwise('/');
        $urlRouterProvider.when('/', '/stepOne/list')
    }]).
    config(['$translateProvider','tmhDynamicLocaleProvider','constants', function($translateProvider,tmhDynamicLocaleProvider,constants) {

        /*TODO: LOCALE TO USE : These params can be defined by grails... is parsing the url this way really the best way? */
        /*TODO: DEFINE HOW TO GET DEFAULT LANGUAGE*/

        /*TODO: IS THERE A WAY TO RETRIEVE THEM FROM ROOTSCOPE?*/
        var locale=window.location.pathname.split("/")[5];
        $translateProvider
            .translations(locale,translation)
            .preferredLanguage(locale);
        $translateProvider.useUrlLoader('/WitBooker/translation/index');
        //tmhDynamicLocaleProvider.useStorage("$cache");
        tmhDynamicLocaleProvider.localeLocationPattern(constants.localeFilesDir+"angular-locale_{{locale}}.js");
    }])
    .config(['currencySettingsProvider','constants', function(currencySettingsProvider,constants) {
        currencySettingsProvider.defaultCurrency(witbookerParams.representation.defaultCurrency);
        currencySettingsProvider.currentCurrency(witbookerParams.representation.currency);
        currencySettingsProvider.rates(witbookerParams.representation.conversionRate);
        currencySettingsProvider.conversionRate(witbookerParams.representation.conversionRate[witbookerParams.representation.currency]);
    }])
    .config(['httpRequestInterceptorCacheBusterProvider',function(httpRequestInterceptorCacheBusterProvider){
        var matchList=[
            /.*\/WitBooker\/stepTwo\/obtainReservationInformation.*/,
            /.*\/WitBooker\/base\/addToCart.*/,
            /.*\/WitBooker\/base\/removeFromCart.*/,
            /.*\/WitBooker\/base\/calculateAvailability.*/,
            /.*\/WitBooker\/base\/updateSessionAsync.*/,
            /.*\/WitBooker\/stepTwo\/updateReservation.*/,
            /.*\/WitBooker\/stepTwo\/reservation.*/
        ];
        httpRequestInterceptorCacheBusterProvider.setMatchlist(matchList,true);
    }])
    .config(['$httpProvider', function($httpProvider) {

        var interceptor = ['$q','$location','$injector','constants','$rootScope','events',function ($q, $location,$injector,constants,$rootScope,events) {
            return {


                request: function(config) {
                    // do something on success
                    var $state;
                    if (!$state) { $state = $injector.get('$state'); }
                    if(config.hasOwnProperty("data") && (config.data.hasOwnProperty("regularParams") || config.data.hasOwnProperty("representation")) ){
                        if(!config.data.representation)
                            config.data.representation={}
                        config.data.representation.currentState=$state.current.name.indexOf('stepOne')!=-1? "StepOne":"StepTwo";
                    }
                    //debugger;
                    return config;
                },


                response: function (result) {
                    var $modal;
                    if (result && result.data && result.data.hasOwnProperty("error") && result.data.error.code==="EXPSESS" ){
                        if (!$modal) { $modal = $injector.get('$modal'); }
                        var ModalInstanceCtrl, modalInstance;
                        ModalInstanceCtrl = ['$scope', '$modalInstance',function ($scope, $modalInstance){
                            $scope.closeInfo = function () {
                                $modalInstance.close();
                                var redirectURL=STEP1_URL.replace(new RegExp("TICKERPLACEHOLDER", 'g'), $rootScope.witbookerParams.representation.ticker)
                                    .replace("LANGUAGEPLACEHOLDER",$rootScope.witbookerParams.representation.locale);
                                window.location = redirectURL;
                            };

                        }];
                        $rootScope.message={body:"trans.session.expired"};
                        modalInstance = $modal.open({
                            templateUrl: constants.assetsDir + 'partials/step_one/misc/genericModal.html',
                            controller: ModalInstanceCtrl,
                            scope: $rootScope
                        });
                    }
                    return result;
                }
            }
        }];



        $httpProvider.interceptors.push(interceptor);


    }])
    .run(['$rootScope','util','events','$http','$templateCache','constants',"$compile","tmhDynamicLocale","currencySettings","$sce","Api","tmhDynamicLocaleCache",
        function($rootScope,util,events,$http,$templateCache,constants,$compile,tmhDynamicLocale,currencySettings,$sce,Api,tmhDynamicLocaleCache){

        util.loadLocales(tmhDynamicLocaleCache);

        Constants.markups = {phase: {}, position: {}};
        Constants.markups.phase.step1 = "STEP1";
        Constants.markups.phase.step2 = "STEP2";
        Constants.markups.phase.step3 = "STEP3";
        Constants.markups.phase.all = "ALL";
        Constants.markups.position.afterbeginbody = "FIRST_BODY";
        Constants.markups.position.beforeendbody = "END_BODY";
        Constants.markups.position.beforeendhead = "HEAD";

        $rootScope.customMarkupAdded={};

        $rootScope.constants=constants;
        $rootScope.affectedAccommodations={};
        witbookerParams.regularParams.startDate=util.parseDate(witbookerParams.regularParams.startDate);
        witbookerParams.regularParams.endDate=util.parseDate(witbookerParams.regularParams.endDate);
        $rootScope.witbookerParams=witbookerParams;


        $rootScope.legacyParams=util.parseQueryString(window.location.search);
        var establishment = data.establishment
        $rootScope.establishment = establishment;
        //$rootScope.establishment.extraFilters ={
        //    "occupancy": [{
        //        "name": "filterName",
        //        "filterType": "occupancy",
        //        "filterLogic": "il_like",
        //        "defaultLanguage":"es",
        //        "options": {
        //            "ageRange": {
        //                "min": 3,
        //                "max": 6
        //
        //            },
        //            "icon": "bebes"
        //        },
        //        "localization": {
        //            "es": {
        //                "title": null,
        //                "options": [
        //                    {
        //                        "label": "1",
        //                        "value": [
        //                            "1n",
        //                            "1j"
        //                        ]
        //                    }
        //                ]
        //            }
        //        }
        //    }]
        //};
        //var extraFilters={};
        //angular.forEach(values, function(filterType, filters) {
        //    angular.forEach(values, function(filters, filter) {
        //        var newFilter= new BookingFormExtraFilter(filter);
        //        if(!extraFilters.hasOwnProperty(newFilter.filterType)){
        //            extraFilters[newFilter.filterType]={};
        //        }
        //        extraFilters[newFilter.filterType] = newFilter;
        //    });
        //});

        if(cartData && Object.prototype.toString.call( cartData ) === '[object Array]' ){
            $rootScope.reservationData=cartData;
        }
        $rootScope.cart={};
        $rootScope.cart.reservations=[];
        $rootScope.cart.processedReservations={};

        tmhDynamicLocale.set($rootScope.witbookerParams.representation.locale);
        $rootScope.$on(events.CHANGE_LANGUAGE, function(scope,newLanguage) {
            $rootScope.witbookerParams.representation.locale=newLanguage;
            tmhDynamicLocale.set(newLanguage);
        });


        var groupMessageByPosition=function(establishment){
            /*Grouping Messages by position*/
            var sortedMessages = {};
            angular.forEach(establishment.messages, function (value, key) {
                if(!value.hidden){
                    if (!sortedMessages[value.position])
                        sortedMessages[value.position] = []
                    value.descriptionHtml = $sce.trustAsHtml(value.description);
                    sortedMessages[value.position].push(value)
                }
            });
            establishment.sortedMessages = sortedMessages;
        }

        groupMessageByPosition(establishment);

        $rootScope.$on(events.UPDATE_AVAILABILITY, function (scope, data) {
            $rootScope.witbookerParams.representation.activePromoCodes=data.activePromoCodes
            $rootScope.witbookerParams.regularParams.discountPromoCodes=data.discountPromoCodes
            $rootScope.witbookerParams.regularParams.inventoryPromoCodes=data.discountPromoCodes
            $rootScope.affectedAccommodations={};
            if (data["messages"]){
                angular.forEach(data["messages"],function(message,key){
                    message.show=true
                    if(!$rootScope.establishment.sortedMessages)
                        $rootScope.establishment.sortedMessages={}
                    if(!$rootScope.establishment.sortedMessages.hasOwnProperty(message.position))
                        $rootScope.establishment.sortedMessages[message.position]=[]
                    for(var count=0;count<$rootScope.establishment.sortedMessages[message.position].length;count++){
                        var existingMessage=$rootScope.establishment.sortedMessages[message.position][count];
                        if (message.title && existingMessage.title===message.title){
                            //$rootScope.establishment.sortedMessages[message.position].splice(count,1);
                            //break;
                            return;
                        }
                    }
                    message.descriptionHtml = $sce.trustAsHtml(message.description);
                    $rootScope.establishment.sortedMessages[message.position].push(message)
                });
            }
            util.setInventoryLinesGrouped($rootScope.establishment, data);
            var accummulatedChildren=[];
            var currentStartDate=$rootScope.witbookerParams.regularParams.startDate;
            var currentEndDate=$rootScope.witbookerParams.regularParams.endDate;

            angular.forEach($rootScope.cart.reservations,function(reservation,key){
                var parent=$rootScope.establishment.inventoryRelations.parentOfTicker[reservation.inventoryLine.ticker];
                var children=$rootScope.establishment.inventoryRelations.childrenOfTicker[parent];
                if (currentEndDate > reservation.startDate && currentStartDate < reservation.endDate)
                    accummulatedChildren=accummulatedChildren.concat(children);

                //if(!$rootScope.cart.processedReservations.hasOwnProperty(reservation.id)){
                    util.updateChildrenAndParentInventories($rootScope.witbookerParams.regularParams.startDate,$rootScope.witbookerParams.regularParams.endDate,$rootScope.establishment,reservation,"-1",true,function(affectedAccommodations){
                        if(affectedAccommodations){
                            angular.extend($rootScope.affectedAccommodations, affectedAccommodations);
                            $rootScope.affectedAccommodations.counter=$rootScope.affectedAccommodations.counter?$rootScope.affectedAccommodations.counter+1:1;
                        }
                    });
                    $rootScope.cart.processedReservations[reservation.id]={"type":"add"};
                //}
            });
            util.changeAccommodationDisplayedPrice(util.removeDuplicatesFromSimpleArray(accummulatedChildren),$rootScope.establishment,false);

            //util.setInventoryLineRates($rootScope.establishment, data.conversionRate, $rootScope.services,  $rootScope.cart || null);

        });

        $rootScope.$on(events.UPDATE_INVENTORIES_BY_CART, function (scope, reservation,reservationUrl) {
            if(!$rootScope.establishment.hasOwnProperty("establishments")){
                if(!$rootScope.cart.processedReservations.hasOwnProperty(reservation.id)){
                    util.updateChildrenAndParentInventories($rootScope.witbookerParams.regularParams.startDate,$rootScope.witbookerParams.regularParams.endDate,$rootScope.establishment,reservation,"-1",true,function(affectedAccommodations){
                        if(affectedAccommodations){
                            angular.extend($rootScope.affectedAccommodations, affectedAccommodations);
                            $rootScope.affectedAccommodations.counter=$rootScope.affectedAccommodations.counter?$rootScope.affectedAccommodations.counter+1:1;
                        }
                    });
                    $rootScope.cart.processedReservations[reservation.id]={"type":"add"};
                }

            }
            if ($rootScope.cart.reservations.length==1 && reservationUrl!=null){
                $rootScope.$broadcast(events.TRANSITION_TO_STEP2,{establishment:reservation.establishment.ticker});
            }
        });

        $rootScope.$on(events.CHANGE_CURRENCY, function (scope, data) {
            if(!data){
                data=currencySettings;
            }
            currencySettings.currentCurrency=data.newCurrency;
            currencySettings.conversionRate=currencySettings.rates[currencySettings.currentCurrency];
        });

        $rootScope.$on(events.CHANGE_LANGUAGE, function (scope, newLanguage, ISO3Language) {
            /*TODO: UPDATE STATIC DATA*/
            angular.forEach($rootScope.establishment.languages, function (value, key) {
                if (value.code == newLanguage) {
                    $rootScope.witbookerParams.representation.language = value;
                }
            })
            /*TODO:REQUEST ONLY STATIC DATA FROM GRAILS CACHE*/
            var inventories = util.getInventoryLinesID($rootScope.establishment);
            var inventoryLinesToUpdate=[];
            var servicesToUpdate=[];
            var accommodationsToUpdate=[];
            var servicesTickers={};
            servicesTickers[$rootScope.establishment.ticker]=[];
            var addCartReservationInventories=function(inventories,cart,establishment){
                angular.forEach(cart.reservations,function(reservation,key){
                    inventories[establishment.ticker].push(reservation.inventoryLine.ticker);
                    inventoryLinesToUpdate.push(reservation.inventoryLine);
                    accommodationsToUpdate.push(reservation.accommodation);
                    angular.forEach(reservation.inventoryLine.services,function(service,key){
                        servicesTickers[$rootScope.establishment.ticker].push(service.ticker);
                        servicesToUpdate.push(service);
                    })
                })
            };

            addCartReservationInventories(inventories,$rootScope.cart,$rootScope.establishment);

            var discounts = util.getDiscountsID($rootScope.establishment);
            var postData = {
                establishmentTicker: $rootScope.establishment.ticker,
                locale: newLanguage,
                language: ISO3Language,
                inventories: inventories,
                discounts: discounts,
                services:servicesTickers,
                hotels:$rootScope.witbookerParams.representation.hotels
            };
            var localizedData = Api.LocalizedData.post(postData, function () {
                /*TODO:currently the only option is to use extend from jQuery, find and alternative that does not use jQuery*/
                jQuery.extend(true, $rootScope.establishment, localizedData.establishment);
                groupMessageByPosition(establishment);

                var updateReservationTranslations=function(translatedEstablishment,inventoryLinesToUpdate,accommodationsToUpdate){
                    if(!inventoryLinesToUpdate || inventoryLinesToUpdate.length<=0){
                        return;
                    }
                    var DataValueHoldersToUpdateTickers=[];
                    angular.forEach(inventoryLinesToUpdate,function(inventoryLine,key){
                        DataValueHoldersToUpdateTickers.push(inventoryLine.ticker);
                    });
                    angular.forEach(accommodationsToUpdate,function(accommodation,key){
                        DataValueHoldersToUpdateTickers.push(accommodation.ticker);
                    });
                    angular.forEach(servicesToUpdate,function(service,key){
                        DataValueHoldersToUpdateTickers.push(service.ticker);
                    });

                    var translatedData=util.getInventoryLineByTicker(translatedEstablishment,DataValueHoldersToUpdateTickers);
                    var translatedInventories=translatedData.inventories;
                    var translatedAccommodations=translatedData.accommodation;
                    var translatedServices=translatedData.services;

                    angular.forEach(inventoryLinesToUpdate,function(inventoryLine,key){
                        angular.forEach(translatedInventories,function(establishmentInventories,key){
                            angular.forEach(establishmentInventories,function(translatedInventory,key){
                                if(inventoryLine.ticker===translatedInventory.ticker){
                                    inventoryLine.condition=translatedInventory.condition;
                                    inventoryLine.configuration=translatedInventory.configuration;
                                    inventoryLine.mealPlan=translatedInventory.mealPlan;
                                }
                            });
                        });
                    });

                    angular.forEach(accommodationsToUpdate,function(accommodation,key){
                        angular.forEach(translatedAccommodations,function(establishmentAccommodations,key){
                            angular.forEach(establishmentAccommodations,function(translatedAccommodation,key){
                                if(accommodation.ticker===translatedAccommodation.ticker){
                                    accommodation.description=translatedAccommodation.description;
                                    accommodation.name=translatedAccommodation.name;
                                }
                            });
                        });
                    });

                    angular.forEach($rootScope.services,function(service,key){
                        angular.forEach(translatedServices,function(establishmentServices,key){
                            angular.forEach(establishmentServices,function(translatedService,key){
                                if(service.ticker===translatedService.ticker){
                                    service.description=translatedService.description;
                                    service.name=translatedService.name;
                                }
                            });
                        });
                    });
                };
                updateReservationTranslations($rootScope.establishment,inventoryLinesToUpdate,accommodationsToUpdate);

                tmhDynamicLocale.set(newLanguage);
                $('#disableClick').removeClass('loading');
                //angular.extend($rootScope.establishment,localizedData.establishment);
                $rootScope.$broadcast(events.CHANGE_LANGUAGE_DONE,{});

            });
            /* TODO:UPDATE DATE PICKER */
            if(newLanguage=="en"){
                $('#datepickerEntryDate').datepicker('option', $.datepicker.regional['']);
                $('#datepickerExitDate').datepicker('option', $.datepicker.regional['']);
            }
            else{
                $('#datepickerEntryDate').datepicker('option', $.datepicker.regional[newLanguage]);
                $('#datepickerExitDate').datepicker('option', $.datepicker.regional[newLanguage]);
            }
            $('#datepickerEntryDate').datepicker('option', "dateFormat",  "dd-mm-yy");
            $('#datepickerExitDate').datepicker('option', "dateFormat",  "dd-mm-yy");
        });


        $rootScope.cartInfoProcessed=false;
        $rootScope.servicesProcessed={};
        /*TODO: Check if this is not overriden when using grunt to include all templates in minification to avoid http requests
        * and.. is this the cleaner way of doing it?
        * */
    }]).filter('range', function() {
        return function(input, min, max) {
            min = parseInt(min);
            max = parseInt(max);
            for (var i=min; i<=max; i++)
                input.push(i);
            return input;
        };
    });
