'use strict';

/* Services */


// Demonstrate how to register services
// In this case it is a simple value service.

var witbookerServices = angular.module('witbookerServices', ['ngResource']);


var ESTABLISHMENT_RESOURCE_URL = '/WitBooker/base/getEstablishmentData';
var ESTABLISHMENT_LOCALIZED_RESOURCE_URL = '/WitBooker/base/createEstablishmentLocalizedData';
var UPDATE_SESSION_URL = '/WitBooker/base/updateSessionAsync';
var AVAILABILITY_RESOURCE_URL = '/WitBooker/base/calculateAvailability';
var CURRENCY_CONVERSION_RATE = '/WitBooker/base/updateCurrencyConversionRate';

var CHART_RESOURCE_URL = '/WitBooker/base/chart';
var STATIC_ROOT_URL= MAIN_STATIC_ROOT_URL;
var LEGACY_BOOKING_URL = URL_WITBOOKER_V6+"select/";
var SHOPPING_CART_URL="/WitBooker/base/addToCart";
var REMOVE_SHOPPING_CART_URL="/WitBooker/base/removeFromCart";
var STEP1_URL=URL_WITBOOKER_V6+"select/TICKERPLACEHOLDER/LANGUAGEPLACEHOLDER/reservationsv6/step1";
var STEP2_URL=URL_WITBOOKER_V6+"select/TICKERPLACEHOLDER/LANGUAGEPLACEHOLDER/reservationsv6/step2#/stepTwo/TICKERPLACEHOLDER/";

var SHOPPING_CART_DATA_URL="/WitBooker/stepTwo/obtainReservationInformation";
var UPDATE_CART_DATA_URL="/WitBooker/stepTwo/updateReservation";


var STEP2_CREDITCARD_URL=URL_WITBOOKER_V6+"select/TICKERPLACEHOLDER/es/reservationsv6/step2";
var STEP2_POINTOFSALE_URL=URL_WITBOOKER_V6+"select/TICKERPLACEHOLDER/es/reservationsv6/step2";

var STEP2_PROCESS_RESERVATION_URL="/WitBooker/stepTwo/reservation";
var STEP2_PAYPAL_EXPRESS_CHECKOUT="/WitBooker/payment/payPalExpressCheckout";

var STEP2_SIPAY="/WitBooker/payment/sipay?ticker=TICKERPLACEHOLDER#/main/LANGUAGEPLACEHOLDER/";
var STEP2_SIPAY_INIT="/WitBooker/payment/sipayInit";
var STEP2_SIPAY_CHECK_RESERVATION_STATUS = "/WitBooker/payment/sipayReservationStatus";

var STEP3_TPV_URL=URL_WITBOOKER_V6+"select/TICKERPLACEHOLDER/LANGUAGEPLACEHOLDER/reservationsv6/procesarpago/IDPLACEHOLDER/java";
var STEP3_CONFIRMATION_URL=URL_WITBOOKER_V6+"select/TICKERPLACEHOLDER/LANGUAGEPLACEHOLDER/reservationsv6/confirmation/IDPLACEHOLDER/java";

var STEP2_RESERVATION_URL=URL_WITBOOKER_V6+"select/TICKERPLACEHOLDER/LANGUAGEPLACEHOLDER/reservationsv6/step2#/stepTwo/TICKERPLACEHOLDER";


/*
{
    "occupancy": {
    "name": "filterName",
        "filterType": "occupancy",
        "filterLogic": "il_like",
        "defaultLanguage":"es",
        "options": {
        "ageRange": {
            "min": 0,
                "max": 2
        },
        "icon": "bebes"
    },
    "localization": {
        "es": {
            "title": null,
                "options": [
                {
                    "label": "1",
                    "value": [
                        "1b",
                        "1bm"
                    ]
                }
            ]
        }
    }
}
}*/

var BookingFormExtraFilter = function(filter){
    angular.extend(this,filter);
};

BookingFormExtraFilter.prototype.getLocalizedData=function(locale){
    var localizedData = this.localization.hasOwnProperty(locale) ?  this.localization[locale] :  this.localization.defaultLocale;
    return localizedData;
};


var api = function ($resource) {

    var Establishment = $resource(ESTABLISHMENT_RESOURCE_URL, {},
        {   'get': {method: 'GET',cache : true},
            'save': {method: 'POST'},
            'query': {method: 'GET', isArray: true},
            'remove': {method: 'DELETE'},
            'delete': {method: 'DELETE'}
        }
    );

    var LocalizedData = $resource(ESTABLISHMENT_LOCALIZED_RESOURCE_URL, {},
        {   'get': {method: 'GET',cache : true},
            'save': {method: 'POST'},
            'post': {method: 'POST'},
            'query': {method: 'GET', isArray: true},
            'remove': {method: 'DELETE'},
            'delete': {method: 'DELETE'}
        }
    );
    var Chart = $resource(CHART_RESOURCE_URL, {},
        {   'get': {method: 'GET',cache : true},
            'save': {method: 'POST'},
            'post': {method: 'POST',cache : true},
            'query': {method: 'GET', isArray: true},
            'remove': {method: 'DELETE'},
            'delete': {method: 'DELETE'}
        }
    );
    var ARI = $resource(AVAILABILITY_RESOURCE_URL, {},
        {   'get': {method: 'GET'},
            'save': {method: 'POST'},
            'post': {method: 'POST'},
            'query': {method: 'GET', isArray: true},
            'remove': {method: 'DELETE'},
            'delete': {method: 'DELETE'}
        }
    );


    /*Write additional methods */
    Establishment.prototype.testMethod=function(){
        return ""
    }

    return {
        Establishment: Establishment,
        Chart: Chart,
        ARI: ARI,
        LocalizedData:LocalizedData
    }

}

witbookerServices.factory("Api",["$resource",api] );

var util = function ($filter,$window,md5) {

    var Util = {};
    Util.entityLevel={}
    Util.entityLevel["hotel"]="establecimientos"
    Util.entityLevel["accommodation"]="tiposalojamiento"
    Util.entityLevel["extra"]="extras"

    /*TODO: ADD SUPPORT TO IE8 */
    Util.addLoading=function(selector){
        var target = $(selector);
        var containerDiv = document.createElement("div");
        containerDiv.setAttribute("id","spinnerContainer");
        containerDiv.setAttribute("class","spinnerContainer");
        target.append(containerDiv);
        var spinner = new Spinner().spin(containerDiv);
    };
    Util.removeLoading=function(containerName){
        var node = document.getElementById("spinnerContainer");
        if (node && node.parentNode) {
            node.parentNode.removeChild(node);
        }
    };

    Util.intersectSimpleArrays=function(array1,array2,getterFunc){
        if(!getterFunc)
            getterFunc=function(elem){
                return elem
            }
        var result = [];
        var a = array1.slice(0);
        var b = array2.slice(0);
        var aLast = a.length - 1;
        var bLast = b.length - 1;
        while (aLast >= 0 && bLast >= 0) {
            if (getterFunc(a[aLast]) > getterFunc(b[bLast]) ) {
                a.pop();
                aLast--;
            } else if (getterFunc(a[aLast]) < getterFunc(b[bLast]) ){
                b.pop();
                bLast--;
            } else /* they're equal */ {
                result.push(a.pop());
                b.pop();
                aLast--;
                bLast--;
            }
        }
        return result;
    };

    Util.generateStep3URL=function(baseurl,hotelTicker,language,reservationID){
        var result=baseurl;
        result=result.replace(new RegExp("TICKERPLACEHOLDER", 'g'), hotelTicker);
        result=result.replace(new RegExp("LANGUAGEPLACEHOLDER", 'g'), language);
        result=result.replace(new RegExp("IDPLACEHOLDER", 'g'), reservationID);
        return result;
    };

    Util.getDiscountActiveDiscountCodes=function(allDiscountDiscountCodes,givenCodes){
        if(typeof allDiscountDiscountCodes =="undefined")
            return []

        var result=[];
        angular.forEach(givenCodes,function(code,index){
            if(allDiscountDiscountCodes.indexOf(code)!=-1){
                result.push(code);
            }
        });
        return result;
    };

    Util.isCodeActive=function(code,allDiscountDiscountCodes){
        var result=[];
        angular.forEach(givenCodes,function(code,index){
            if(allDiscountDiscountCodes.indexOf(code)!=1){
                result.push(code);
            }
        });
        return result;
    };

/*
    Util.generateImgUrl=function(establishment,entity,entityLevel,path,thumbnail){
        var urlImg='/v6/multimedia/'+establishment.ticker+"/"+Util.entityLevel[entityLevel]+"/"+entity.id+"/"+path;
        var urlThumbnail = STATIC_ROOT_URL+'v6/thumbnails/phpThumb.php?src='+urlImg+(typeof thumbnail.width != 'undefined'?'&w='+thumbnail.width+'&hash='+md5.createHash('src='+urlImg+'&w='+thumbnail.width+'MqeR3oG0GV2alzpUrChistIan4wfUY{./??3487jasd'):'');
        return urlThumbnail;
    };
*/

    Util.generateImgUrl=function(establishment,entity,entityLevel,path,imgSize){
        if(typeof path === "undefined" || !path){
            return STATIC_ROOT_URL+"NoPhoto.jpg"
        }
        var lastDotIndex=path.lastIndexOf('.');
        var filenameWithoutExtension=path.substr(0,lastDotIndex);
        var filenameExtension=path.substr(lastDotIndex);
        var urlThumbnail="";
        if(imgSize && (imgSize.width || imgSize.height)){
            var height=imgSize.height? imgSize.height : imgSize.width;
            var width=imgSize.width? imgSize.width : imgSize.height;
            urlThumbnail = STATIC_ROOT_URL+establishment.ticker+"/"+Util.entityLevel[entityLevel]+"/"+entity.id+"/"+filenameWithoutExtension+"_"+width+"x"+height+filenameExtension;
        }else{
            urlThumbnail = STATIC_ROOT_URL+establishment.ticker+"/"+Util.entityLevel[entityLevel]+"/"+entity.id+"/"+path;
        }
        return urlThumbnail;
    };

    Util.removeDuplicatesFromSimpleArray=function(arr) {
        var a = [];
        for (var i=0, l=arr.length; i<l; i++)
            if (a.indexOf(arr[i]) === -1)
                a.push(arr[i]);
        return a;
    };

    Util.imgError=function(index){
        console.log(index.attr('src'))
        console.log(index)
        console.log(this)
    };


    Util.generateConditionsUrl=function(establishment){
        return LEGACY_BOOKING_URL+'select/'+establishment.ticker+'/es/reservationsv6/generalconditions';
    };

    Util.generateLogoUrl=function(establishment){
        return STATIC_ROOT_URL+establishment.ticker+"/logo/"+establishment.id+"/"+establishment.logo.path;
    };


    Util.generateHotelUrl=function(hotelSiteUrl,hotelLogoHasLink){
        if(hotelSiteUrl && hotelLogoHasLink){
            return hotelSiteUrl;
        }else{
            return ""
        }
    };


    Util.generateLegacyBookingUrl=function(establishment,locale,params){
        var url=LEGACY_BOOKING_URL+establishment.ticker+"/"+locale+"/reservationsv6/addToCart?";
        angular.forEach(params,function(param,key){
            url += encodeURIComponent(key) + "=" + encodeURIComponent(param) + "&";
        });
        return url;
    };

    Util.attachGetParams=function(url,params){
        var hashBangPart=url.substring(url.indexOf("#")===-1 ? url.length: url.indexOf("#"),url.length);
        url=url.substring(0,url.indexOf("#")===-1?url.length:url.indexOf("#"));
        url +=url.indexOf("?") < 0 ? "?": "";
        var empty=true;
        angular.forEach(params,function(param,key){
            empty=false;
            url += encodeURIComponent(key) + "=" + encodeURIComponent(param) + "&";
        });
        return empty?url+hashBangPart:url.slice(0,-1)+hashBangPart;
    };

    Util.createLegacyParamsObject=function(representation){
        var legacyParams = {};
        if (representation.channel)
            legacyParams.channel = representation.channel
        if (representation.tracking_id)
            legacyParams.tracking_id = representation.tracking_id
        if (representation.view)
            legacyParams.view = representation.view
        if (representation.iframeMode)
            legacyParams.witif = representation.iframeMode ? 1 : 0
        if (representation.iframeResizeDomain)
            legacyParams.d = representation.iframeResizeDomain
        if (representation.witaffiliate)
            legacyParams.witaffiliate = representation.witaffiliate
        if (representation.affiliate)
            legacyParams.affiliate = representation.affiliate
        return legacyParams;
    };

    Util.getIntegerDigits=function(n) {
        return (n < 0) ? Math.ceil(n) : Math.floor(n);
    };
    Util.getFractionalDigits=function(n) {
        return n -  Util.getIntegerDigits(n);
    };

    Util.parseDate=function(input){
        var parts = input.split('-');
        return new Date (parts[2],parts[1]-1,parts[0])
    };

    Util.formatDate=function(input,format){
        return $filter('date')(input,format)
    };

    Util.setEstablishmentsProperties=function(establishment,propertyMap){
        if(establishment.hasOwnProperty("establishments")){
            angular.forEach(establishment.establishments,function(childEstablishment,key){
                Util.setEstablishmentsProperties(childEstablishment,propertyMap);
            });
        }else{
            angular.forEach(propertyMap,function(value,propertyName){
                establishment[propertyName]=value
            });
        }
    };

    Util.setAccommodationsIsOpenProperty=function(establishment,limit,uncollapse){
        if(establishment.hasOwnProperty("establishments")){
            angular.forEach(establishment.establishments,function(childEstablishment,key){
                Util.setAccommodationsIsOpenProperty(childEstablishment,limit,uncollapse);
            })
        }else{
            angular.forEach(establishment.inventoryLinesGrouped,function(inventoryLineGrouped,key){
                if(inventoryLineGrouped.inventoryLine.length<=limit){
                    inventoryLineGrouped.isOpen=true && uncollapse;
                }else{
                    inventoryLineGrouped.isOpen=false;
                }
            })
        }
    };


    Util.parseQueryString=function(queryString){
        var pairs = queryString.slice(1).split('&');

        var result = {};
        angular.forEach(pairs, function(pair,key) {
            pair = pair.split('=');
            result[pair[0]] = decodeURIComponent(pair[1] || '');
        });

        return JSON.parse(JSON.stringify(result));
    };

    Util.countOwnProperties=function(obj){
        var count=0;
        for (var k in obj) {
            if (obj.hasOwnProperty(k)) {
                ++count;
            }
        }
        return count
    };
    Util.isNumber= function isNumber(n) {
        return !isNaN(parseFloat(n)) && isFinite(n);
    }

    Util.setInventoryLineRates=function(establishment,conversionRate,services,cart){
        /*TODO SERVICES*/
        var propertiesToChange=["totalRate","averageRate", "originalRate", "rackRate","firstNightCost"];
        var accommoDationPropertiesToChange=["cheapestRate"];
        var establishmentPropertiesToChange=["cheapestRate"];
        var discountPropertiesToChange=["reduction"];
        var servicesPropertiesToChange=["rate"];
        var reservationPropertiesToChange=["rate","originalRate"];
        var cartPropertiesToChange=["total"];

        if(establishment.hasOwnProperty("establishments")){
            angular.forEach(establishment.establishments,function(childEstablishment,key){
                Util.setInventoryLineRates(childEstablishment);
            })
        }else{


            angular.forEach(establishmentPropertiesToChange ,function(property,key){
                if (establishment.hasOwnProperty(property) && Util.isNumber(establishment[property])  ){
                    establishment[property]*=conversionRate;
                }
            });

            angular.forEach(establishment.discounts ,function(discount,key){
                angular.forEach(discountPropertiesToChange ,function(property,key){
                    if (discount.hasOwnProperty(property) && Util.isNumber(discount[property]) && discount["percentage"] ){
                        discount[property]*=conversionRate;
                    }
                });
            });


            if (services){
                angular.forEach(services ,function(service,key){
                    angular.forEach(servicesPropertiesToChange ,function(property,key){
                        if (service.hasOwnProperty(property) && Util.isNumber(service[property])  ){
                            service[property]*=conversionRate;
                        }
                    });
                });
            }

            if (cart && cart.reservations){
                angular.forEach(cartPropertiesToChange ,function(property,key){
                    if (cart.hasOwnProperty(property) && Util.isNumber(cart[property])  ){
                        cart[property]*=conversionRate;
                    }
                });
                var reservations=cart.reservations;
                angular.forEach(reservations ,function(reservation,key){
                    angular.forEach(reservationPropertiesToChange ,function(property,key){
                        if (reservation.hasOwnProperty(property) && Util.isNumber(reservation[property])  ){
                            reservation[property]*=conversionRate;
                        }
                    });
                });
            }

            angular.forEach(establishment.inventoryLinesGrouped,function(inventoryLineGrouped,key){

               var accommodation=inventoryLineGrouped.accommodation;
               angular.forEach(accommoDationPropertiesToChange ,function(property,key){
                    if (accommodation.hasOwnProperty(property) && Util.isNumber(accommodation[property])){
                        accommodation[property]*=conversionRate;
                    }
                });

                angular.forEach(inventoryLineGrouped.inventoryLine ,function(inventory,key){
                    angular.forEach(propertiesToChange ,function(property,key){
                        if (inventory.hasOwnProperty(property) && Util.isNumber(inventory[property])){
                            inventory[property]*=conversionRate;
                        }
                    });
                });

            });
        }
    };


    Util.setInventoryLineProperty=function(establishment,inventoryTicker,property,value,reservation){
        if(establishment.hasOwnProperty("establishments")){
            angular.forEach(establishment.establishments,function(childEstablishment,key){
                Util.setInventoryLineProperty(childEstablishment);
            })
        }else{
            angular.forEach(establishment.inventoryLinesGrouped,function(inventoryLineGrouped,key){
                angular.forEach(inventoryLineGrouped.inventoryLine ,function(inventory,key){
                    var isReservationClosed=function(inventory){
                        var isRClosed=false;
                        angular.forEach(inventory.errorMessage ,function(errorMessage,key){
                            if(errorMessage.failedFilter=="reservationClosed"){
                                isRClosed=true;
                            }
                        });
                        return isRClosed
                    }

                    if(inventory.ticker==inventoryTicker && (inventory.errorMessage.length<=0 || isReservationClosed(inventory) ) ){
                        if (value=="+1"){
                            inventory[property]+=reservation.quantity;
                            inventory.errorMessage=[];
                        }else if (value=="-1") {
                            inventory[property]-=reservation.quantity;
                            if(inventory[property]<=0){
                                inventory.errorMessage?inventory.errorMessage.push({failedFilter: "reservationClosed"}):inventory.errorMessage=[{failedFilter: "reservationClosed"}];
                            }
                        }else{
                            inventory[property]=value
                        }
                    };
                })
            })
        }
    };

    Util.updateChildrenAndParentInventories=function(currentStartDate,currentEndDate,establishment,reservation,operation,changeDisplayPrice,cb){
        var parent=establishment.inventoryRelations.parentOfTicker[reservation.inventoryLine.ticker];
        var children=establishment.inventoryRelations.childrenOfTicker[parent];
        var relatedByAccommodation=establishment.inventoryLinesGrouped.filter(function(inventoryLineGrouped){  return inventoryLineGrouped.accommodation.ticker===reservation.accommodation.ticker})[0].inventoryLine.map(function(inventoryLine){return inventoryLine.ticker});
        if ( currentEndDate > reservation.startDate && currentStartDate < reservation.endDate ){
            angular.forEach(children,function(child,key){
                Util.setInventoryLineProperty(establishment,child,"availability",operation,reservation)
            });
            if(!changeDisplayPrice)
                return
            if(operation==="-1")
                cb(Util.changeAccommodationDisplayedPrice(relatedByAccommodation,establishment,false));
            else if (operation==="+1")
                cb(Util.changeAccommodationDisplayedPrice(relatedByAccommodation,establishment,true));
        }
    };

    Util.changeAccommodationDisplayedPrice=function(affectedInventories,establishment,adding){
        var affectedAccommodations={};
        var isAffected=false;
        var affectedCounter=0;
        angular.forEach(affectedInventories,function(affectedInventoryTicker,key){
            angular.forEach(establishment.inventoryLinesGrouped,function(inventoryLinesGrouped,key){
                angular.forEach(inventoryLinesGrouped.inventoryLine,function(inventory,key){
                    if(inventory.ticker==affectedInventoryTicker && inventory.availability<=0 ){
                        affectedCounter++;
                        if(affectedCounter===affectedInventories.length){
                            affectedAccommodations[inventoryLinesGrouped.accommodation.ticker]=true
                        }
                        isAffected=true;
                    }
                    if(inventory.ticker==affectedInventoryTicker  && inventory.availability==1 && adding){
                        affectedAccommodations[inventoryLinesGrouped.accommodation.ticker]=false
                        isAffected=true;
                    }

                })
            })
        });
        if(isAffected){
            return affectedAccommodations;
        }
        return null;
    };


    Util.setInventoryLinesGrouped=function(establishment,newInventories){

        if(establishment.hasOwnProperty("establishments")){
            angular.forEach(establishment.establishments,function(childEstablishment,key){
                Util.setInventoryLinesGrouped(childEstablishment,newInventories);
            });
            establishment.errorMessages=newInventories.errorMessages;
            establishment.discounts=newInventories.discounts;
            establishment.allFiltered=newInventories.allFiltered;
            establishment.allRestricted=newInventories.allRestricted;
        }else{
            establishment.inventoryLinesGrouped=newInventories[establishment.ticker].inventoryLinesGrouped;
            establishment.cheapestRate=newInventories[establishment.ticker].cheapestRate;
            establishment.discounts=newInventories[establishment.ticker].discounts;
            establishment.errorMessages=newInventories[establishment.ticker].errorMessages;
            establishment.allFiltered=newInventories[establishment.ticker].allFiltered;
            establishment.allRestricted=newInventories[establishment.ticker].allRestricted;
            establishment.activeDiscounts=newInventories[establishment.ticker].activeDiscounts;
        }
    };

    Util.getInventoryLineByTicker=function(establishment,tickers){
        var data={
            inventories:{},
            accommodation:{},
            services:{}
        }

        if(establishment.hasOwnProperty("establishments")){
            angular.forEach(establishment.establishments,function(childEstablishment,key){
                angular.extend(data,Util.getInventoryLineByTicker(childEstablishment));
            })
        }else{
            var inventories=data.inventories;
            var accommodation=data.accommodation;
            var services=data.services;
            inventories[establishment.ticker]={};
            accommodation[establishment.ticker]={};
            services[establishment.ticker]={};
            angular.forEach(establishment.services,function(service,serviceTicker){
                if( Object.prototype.toString.call( tickers ) === '[object Array]' ){
                    angular.forEach(tickers ,function(ticker,key){
                        if(ticker===serviceTicker){
                            services[establishment.ticker][serviceTicker]=service;
                        }
                    });
                }else{
                    if(tickers===serviceTicker){
                        services[establishment.ticker][serviceTicker]=service;
                    }
                }
            });

            angular.forEach(establishment.inventoryLinesGrouped,function(inventoryLineGrouped,key){
                var currentAccommodation=inventoryLineGrouped.accommodation;

                if( Object.prototype.toString.call( tickers ) === '[object Array]' ){
                    angular.forEach(tickers ,function(ticker,key){
                        if(ticker===currentAccommodation.ticker){
                            accommodation[establishment.ticker][currentAccommodation.ticker]=currentAccommodation;
                        }
                    });
                }else{
                    if(tickers===currentAccommodation.ticker){
                        accommodation[establishment.ticker][currentAccommodation.ticker]=currentAccommodation;
                    }
                }

                angular.forEach(inventoryLineGrouped.inventoryLine ,function(inventory,key){
                    if( Object.prototype.toString.call( tickers ) === '[object Array]' ){
                        angular.forEach(tickers ,function(ticker,key){
                            if(ticker===inventory.ticker){
                                inventories[establishment.ticker][inventory.ticker]=inventory;
                            }
                        });
                    }else{
                        if(tickers===inventory.ticker){
                            inventories[establishment.ticker][inventory.ticker]=inventory;
                        }
                    }
                });
            })
            return data;
        }
        return data;
    };

    Util.getInventoryLinesID=function(establishment){
        var inventories={}
        if(establishment.hasOwnProperty("establishments")){
            angular.forEach(establishment.establishments,function(childEstablishment,key){
                angular.extend(inventories,Util.getInventoryLinesID(childEstablishment));
            })
        }else{
            inventories[establishment.ticker]=[]
            angular.forEach(establishment.inventoryLinesGrouped,function(inventoryLineGrouped,key){
                angular.forEach(inventoryLineGrouped.inventoryLine ,function(inventory,key){
                    inventories[establishment.ticker].push(inventory.ticker);
                })
            })
            return inventories;
        }
        return inventories;
    };

    Util.getDiscountsID=function(establishment){
        var discounts={}
        discounts[establishment.ticker]=[]
        angular.forEach(establishment.discounts,function(discount,key){
            discounts[establishment.ticker].push(discount.ticker)
        })
        return discounts;
    };

    Util.calculateServiceRate=function(service,persons,rooms,units,days,constants){
        var dailyMultiplier=service.daily?days:1;
        if(service.type==constants.serviceType.units)
            return service.rate*units*dailyMultiplier
        if(service.type==constants.serviceType.room)
            return service.rate*rooms*dailyMultiplier
        if(service.type==constants.serviceType.person)
            return service.rate*persons*dailyMultiplier*rooms
    };

    Util.setGetParameter=function(paramName, paramValue){
        var url = window.location.href;
        var hashbangSuffix="";
        if (url.indexOf("#") >= 0){
            hashbangSuffix=url.substring(url.indexOf("#"));
            url=url.substring(0,url.indexOf("#"));
        }

        if (url.indexOf(paramName + "=") >= 0)
        {
            var prefix = url.substring(0, url.indexOf(paramName));
            var suffix = url.substring(url.indexOf(paramName));
            suffix = suffix.substring(suffix.indexOf("=") + 1);
            suffix = (suffix.indexOf("&") >= 0) ? suffix.substring(suffix.indexOf("&")) : "";
            url = prefix + paramName + "=" + paramValue + suffix;
        }
        else
        {
            if (url.indexOf("?") < 0)
                url += "?" + paramName + "=" + paramValue;
            else
                url += "&" + paramName + "=" + paramValue;
        }
        url+=hashbangSuffix;
        $window.location.href = url;
        return false;
    };
    Util.loadLocales=function(tmhDynamicLocaleCache){
        var locales={
            "ar" : { "DATETIME_FORMATS": { "AMPMS": [ "\u0635", "\u0645" ], "DAY": [ "\u0627\u0644\u0623\u062d\u062f", "\u0627\u0644\u0627\u062b\u0646\u064a\u0646", "\u0627\u0644\u062b\u0644\u0627\u062b\u0627\u0621", "\u0627\u0644\u0623\u0631\u0628\u0639\u0627\u0621", "\u0627\u0644\u062e\u0645\u064a\u0633", "\u0627\u0644\u062c\u0645\u0639\u0629", "\u0627\u0644\u0633\u0628\u062a" ], "MONTH": [ "\u064a\u0646\u0627\u064a\u0631", "\u0641\u0628\u0631\u0627\u064a\u0631", "\u0645\u0627\u0631\u0633", "\u0623\u0628\u0631\u064a\u0644", "\u0645\u0627\u064a\u0648", "\u064a\u0648\u0646\u064a\u0648", "\u064a\u0648\u0644\u064a\u0648", "\u0623\u063a\u0633\u0637\u0633", "\u0633\u0628\u062a\u0645\u0628\u0631", "\u0623\u0643\u062a\u0648\u0628\u0631", "\u0646\u0648\u0641\u0645\u0628\u0631", "\u062f\u064a\u0633\u0645\u0628\u0631" ], "SHORTDAY": [ "\u0627\u0644\u0623\u062d\u062f", "\u0627\u0644\u0627\u062b\u0646\u064a\u0646", "\u0627\u0644\u062b\u0644\u0627\u062b\u0627\u0621", "\u0627\u0644\u0623\u0631\u0628\u0639\u0627\u0621", "\u0627\u0644\u062e\u0645\u064a\u0633", "\u0627\u0644\u062c\u0645\u0639\u0629", "\u0627\u0644\u0633\u0628\u062a" ], "SHORTMONTH": [ "\u064a\u0646\u0627\u064a\u0631", "\u0641\u0628\u0631\u0627\u064a\u0631", "\u0645\u0627\u0631\u0633", "\u0623\u0628\u0631\u064a\u0644", "\u0645\u0627\u064a\u0648", "\u064a\u0648\u0646\u064a\u0648", "\u064a\u0648\u0644\u064a\u0648", "\u0623\u063a\u0633\u0637\u0633", "\u0633\u0628\u062a\u0645\u0628\u0631", "\u0623\u0643\u062a\u0648\u0628\u0631", "\u0646\u0648\u0641\u0645\u0628\u0631", "\u062f\u064a\u0633\u0645\u0628\u0631" ], "fullDate": "EEEE\u060c d MMMM\u060c y", "longDate": "d MMMM\u060c y", "medium": "dd\u200f/MM\u200f/yyyy h:mm:ss a", "mediumDate": "dd\u200f/MM\u200f/yyyy", "mediumTime": "h:mm:ss a", "short": "d\u200f/M\u200f/yyyy h:mm a", "shortDate": "d\u200f/M\u200f/yyyy", "shortTime": "h:mm a" }, "NUMBER_FORMATS": { "CURRENCY_SYM": "\u00a3", "DECIMAL_SEP": "\u066b", "GROUP_SEP": "\u066c", "PATTERNS": [ { "gSize": 0, "lgSize": 0, "macFrac": 0, "maxFrac": 3, "minFrac": 0, "minInt": 1, "negPre": "", "negSuf": "-", "posPre": "", "posSuf": "" }, { "gSize": 0, "lgSize": 0, "macFrac": 0, "maxFrac": 2, "minFrac": 2, "minInt": 1, "negPre": "\u00a4\u00a0", "negSuf": "-", "posPre": "\u00a4\u00a0", "posSuf": "" } ] }, "id": "ar", "pluralCat": function (n) { if (n == 0) { return PLURAL_CATEGORY.ZERO; } if (n == 1) { return PLURAL_CATEGORY.ONE; } if (n == 2) { return PLURAL_CATEGORY.TWO; } if (n == (n | 0) && n % 100 >= 3 && n % 100 <= 10) { return PLURAL_CATEGORY.FEW; } if (n == (n | 0) && n % 100 >= 11 && n % 100 <= 99) { return PLURAL_CATEGORY.MANY; } return PLURAL_CATEGORY.OTHER;} } ,
            "ca" : { "DATETIME_FORMATS": { "AMPMS": [ "a.m.", "p.m." ], "DAY": [ "diumenge", "dilluns", "dimarts", "dimecres", "dijous", "divendres", "dissabte" ], "MONTH": [ "de gener", "de febrer", "de mar\u00e7", "d\u2019abril", "de maig", "de juny", "de juliol", "d\u2019agost", "de setembre", "d\u2019octubre", "de novembre", "de desembre" ], "SHORTDAY": [ "dg.", "dl.", "dt.", "dc.", "dj.", "dv.", "ds." ], "SHORTMONTH": [ "de gen.", "de febr.", "de mar\u00e7", "d\u2019abr.", "de maig", "de juny", "de jul.", "d\u2019ag.", "de set.", "d\u2019oct.", "de nov.", "de des." ], "fullDate": "EEEE d MMMM 'de' y", "longDate": "d MMMM 'de' y", "medium": "dd/MM/yyyy H:mm:ss", "mediumDate": "dd/MM/yyyy", "mediumTime": "H:mm:ss", "short": "dd/MM/yy H:mm", "shortDate": "dd/MM/yy", "shortTime": "H:mm" }, "NUMBER_FORMATS": { "CURRENCY_SYM": "\u20ac", "DECIMAL_SEP": ",", "GROUP_SEP": ".", "PATTERNS": [ { "gSize": 3, "lgSize": 3, "macFrac": 0, "maxFrac": 3, "minFrac": 0, "minInt": 1, "negPre": "-", "negSuf": "", "posPre": "", "posSuf": "" }, { "gSize": 3, "lgSize": 3, "macFrac": 0, "maxFrac": 2, "minFrac": 2, "minInt": 1, "negPre": "(\u00a4", "negSuf": ")", "posPre": "\u00a4", "posSuf": "" } ] }, "id": "ca", "pluralCat": function (n) { if (n == 1) { return PLURAL_CATEGORY.ONE; } return PLURAL_CATEGORY.OTHER;} },
            "de" : { "DATETIME_FORMATS": { "AMPMS": [ "vorm.", "nachm." ], "DAY": [ "Sonntag", "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag" ], "MONTH": [ "Januar", "Februar", "M\u00e4rz", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember" ], "SHORTDAY": [ "So.", "Mo.", "Di.", "Mi.", "Do.", "Fr.", "Sa." ], "SHORTMONTH": [ "Jan", "Feb", "M\u00e4r", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez" ], "fullDate": "EEEE, d. MMMM y", "longDate": "d. MMMM y", "medium": "dd.MM.yyyy HH:mm:ss", "mediumDate": "dd.MM.yyyy", "mediumTime": "HH:mm:ss", "short": "dd.MM.yy HH:mm", "shortDate": "dd.MM.yy", "shortTime": "HH:mm" }, "NUMBER_FORMATS": { "CURRENCY_SYM": "\u20ac", "DECIMAL_SEP": ",", "GROUP_SEP": ".", "PATTERNS": [ { "gSize": 3, "lgSize": 3, "macFrac": 0, "maxFrac": 3, "minFrac": 0, "minInt": 1, "negPre": "-", "negSuf": "", "posPre": "", "posSuf": "" }, { "gSize": 3, "lgSize": 3, "macFrac": 0, "maxFrac": 2, "minFrac": 2, "minInt": 1, "negPre": "-", "negSuf": "\u00a0\u00a4", "posPre": "", "posSuf": "\u00a0\u00a4" } ] }, "id": "de", "pluralCat": function (n) { if (n == 1) { return PLURAL_CATEGORY.ONE; } return PLURAL_CATEGORY.OTHER;} },
            "en" : { "DATETIME_FORMATS": { "AMPMS": [ "AM", "PM" ], "DAY": [ "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" ], "MONTH": [ "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" ], "SHORTDAY": [ "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" ], "SHORTMONTH": [ "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" ], "fullDate": "EEEE, MMMM d, y", "longDate": "MMMM d, y", "medium": "MMM d, y h:mm:ss a", "mediumDate": "MMM d, y", "mediumTime": "h:mm:ss a", "short": "M/d/yy h:mm a", "shortDate": "M/d/yy", "shortTime": "h:mm a" }, "NUMBER_FORMATS": { "CURRENCY_SYM": "$", "DECIMAL_SEP": ".", "GROUP_SEP": ",", "PATTERNS": [ { "gSize": 3, "lgSize": 3, "macFrac": 0, "maxFrac": 3, "minFrac": 0, "minInt": 1, "negPre": "-", "negSuf": "", "posPre": "", "posSuf": "" }, { "gSize": 3, "lgSize": 3, "macFrac": 0, "maxFrac": 2, "minFrac": 2, "minInt": 1, "negPre": "(\u00a4", "negSuf": ")", "posPre": "\u00a4", "posSuf": "" } ] }, "id": "en", "pluralCat": function (n) { if (n == 1) { return PLURAL_CATEGORY.ONE; } return PLURAL_CATEGORY.OTHER;} },
            "es" : { "DATETIME_FORMATS": { "AMPMS": [ "a.m.", "p.m." ], "DAY": [ "domingo", "lunes", "martes", "mi\u00e9rcoles", "jueves", "viernes", "s\u00e1bado" ], "MONTH": [ "enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre" ], "SHORTDAY": [ "dom", "lun", "mar", "mi\u00e9", "jue", "vie", "s\u00e1b" ], "SHORTMONTH": [ "ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic" ], "fullDate": "EEEE, d 'de' MMMM 'de' y", "longDate": "d 'de' MMMM 'de' y", "medium": "dd/MM/yyyy HH:mm:ss", "mediumDate": "dd/MM/yyyy", "mediumTime": "HH:mm:ss", "short": "dd/MM/yy HH:mm", "shortDate": "dd/MM/yy", "shortTime": "HH:mm" }, "NUMBER_FORMATS": { "CURRENCY_SYM": "\u20ac", "DECIMAL_SEP": ",", "GROUP_SEP": ".", "PATTERNS": [ { "gSize": 3, "lgSize": 3, "macFrac": 0, "maxFrac": 3, "minFrac": 0, "minInt": 1, "negPre": "-", "negSuf": "", "posPre": "", "posSuf": "" }, { "gSize": 3, "lgSize": 3, "macFrac": 0, "maxFrac": 2, "minFrac": 2, "minInt": 1, "negPre": "-", "negSuf": "\u00a0\u00a4", "posPre": "", "posSuf": "\u00a0\u00a4" } ] }, "id": "es", "pluralCat": function (n) { if (n == 1) { return PLURAL_CATEGORY.ONE; } return PLURAL_CATEGORY.OTHER;} },
            "fr" : { "DATETIME_FORMATS": { "AMPMS": [ "AM", "PM" ], "DAY": [ "dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi" ], "MONTH": [ "janvier", "f\u00e9vrier", "mars", "avril", "mai", "juin", "juillet", "ao\u00fbt", "septembre", "octobre", "novembre", "d\u00e9cembre" ], "SHORTDAY": [ "dim.", "lun.", "mar.", "mer.", "jeu.", "ven.", "sam." ], "SHORTMONTH": [ "janv.", "f\u00e9vr.", "mars", "avr.", "mai", "juin", "juil.", "ao\u00fbt", "sept.", "oct.", "nov.", "d\u00e9c." ], "fullDate": "EEEE d MMMM y", "longDate": "d MMMM y", "medium": "d MMM y HH:mm:ss", "mediumDate": "d MMM y", "mediumTime": "HH:mm:ss", "short": "dd/MM/yy HH:mm", "shortDate": "dd/MM/yy", "shortTime": "HH:mm" }, "NUMBER_FORMATS": { "CURRENCY_SYM": "\u20ac", "DECIMAL_SEP": ",", "GROUP_SEP": "\u00a0", "PATTERNS": [ { "gSize": 3, "lgSize": 3, "macFrac": 0, "maxFrac": 3, "minFrac": 0, "minInt": 1, "negPre": "-", "negSuf": "", "posPre": "", "posSuf": "" }, { "gSize": 3, "lgSize": 3, "macFrac": 0, "maxFrac": 2, "minFrac": 2, "minInt": 1, "negPre": "(", "negSuf": "\u00a0\u00a4)", "posPre": "", "posSuf": "\u00a0\u00a4" } ] }, "id": "fr", "pluralCat": function (n) { if (n >= 0 && n <= 2 && n != 2) { return PLURAL_CATEGORY.ONE; } return PLURAL_CATEGORY.OTHER;} },
            "it" : { "DATETIME_FORMATS": { "AMPMS": [ "m.", "p." ], "DAY": [ "domenica", "luned\u00ec", "marted\u00ec", "mercoled\u00ec", "gioved\u00ec", "venerd\u00ec", "sabato" ], "MONTH": [ "gennaio", "febbraio", "marzo", "aprile", "maggio", "giugno", "luglio", "agosto", "settembre", "ottobre", "novembre", "dicembre" ], "SHORTDAY": [ "dom", "lun", "mar", "mer", "gio", "ven", "sab" ], "SHORTMONTH": [ "gen", "feb", "mar", "apr", "mag", "giu", "lug", "ago", "set", "ott", "nov", "dic" ], "fullDate": "EEEE d MMMM y", "longDate": "dd MMMM y", "medium": "dd/MMM/y HH:mm:ss", "mediumDate": "dd/MMM/y", "mediumTime": "HH:mm:ss", "short": "dd/MM/yy HH:mm", "shortDate": "dd/MM/yy", "shortTime": "HH:mm" }, "NUMBER_FORMATS": { "CURRENCY_SYM": "\u20ac", "DECIMAL_SEP": ",", "GROUP_SEP": ".", "PATTERNS": [ { "gSize": 3, "lgSize": 3, "macFrac": 0, "maxFrac": 3, "minFrac": 0, "minInt": 1, "negPre": "-", "negSuf": "", "posPre": "", "posSuf": "" }, { "gSize": 3, "lgSize": 3, "macFrac": 0, "maxFrac": 2, "minFrac": 2, "minInt": 1, "negPre": "\u00a4\u00a0-", "negSuf": "", "posPre": "\u00a4\u00a0", "posSuf": "" } ] }, "id": "it", "pluralCat": function (n) { if (n == 1) { return PLURAL_CATEGORY.ONE; } return PLURAL_CATEGORY.OTHER;} },
            "nl" : { "DATETIME_FORMATS": { "AMPMS": [ "AM", "PM" ], "DAY": [ "zondag", "maandag", "dinsdag", "woensdag", "donderdag", "vrijdag", "zaterdag" ], "MONTH": [ "januari", "februari", "maart", "april", "mei", "juni", "juli", "augustus", "september", "oktober", "november", "december" ], "SHORTDAY": [ "zo", "ma", "di", "wo", "do", "vr", "za" ], "SHORTMONTH": [ "jan.", "feb.", "mrt.", "apr.", "mei", "jun.", "jul.", "aug.", "sep.", "okt.", "nov.", "dec." ], "fullDate": "EEEE d MMMM y", "longDate": "d MMMM y", "medium": "d MMM y HH:mm:ss", "mediumDate": "d MMM y", "mediumTime": "HH:mm:ss", "short": "dd-MM-yy HH:mm", "shortDate": "dd-MM-yy", "shortTime": "HH:mm" }, "NUMBER_FORMATS": { "CURRENCY_SYM": "\u20ac", "DECIMAL_SEP": ",", "GROUP_SEP": ".", "PATTERNS": [ { "gSize": 3, "lgSize": 3, "macFrac": 0, "maxFrac": 3, "minFrac": 0, "minInt": 1, "negPre": "-", "negSuf": "", "posPre": "", "posSuf": "" }, { "gSize": 3, "lgSize": 3, "macFrac": 0, "maxFrac": 2, "minFrac": 2, "minInt": 1, "negPre": "\u00a4\u00a0", "negSuf": "-", "posPre": "\u00a4\u00a0", "posSuf": "" } ] }, "id": "nl", "pluralCat": function (n) { if (n == 1) { return PLURAL_CATEGORY.ONE; } return PLURAL_CATEGORY.OTHER;} },
            "pl" : { "DATETIME_FORMATS": { "AMPMS": [ "AM", "PM" ], "DAY": [ "niedziela", "poniedzia\u0142ek", "wtorek", "\u015broda", "czwartek", "pi\u0105tek", "sobota" ], "MONTH": [ "stycznia", "lutego", "marca", "kwietnia", "maja", "czerwca", "lipca", "sierpnia", "wrze\u015bnia", "pa\u017adziernika", "listopada", "grudnia" ], "SHORTDAY": [ "niedz.", "pon.", "wt.", "\u015br.", "czw.", "pt.", "sob." ], "SHORTMONTH": [ "sty", "lut", "mar", "kwi", "maj", "cze", "lip", "sie", "wrz", "pa\u017a", "lis", "gru" ], "fullDate": "EEEE, d MMMM y", "longDate": "d MMMM y", "medium": "d MMM y HH:mm:ss", "mediumDate": "d MMM y", "mediumTime": "HH:mm:ss", "short": "dd.MM.yyyy HH:mm", "shortDate": "dd.MM.yyyy", "shortTime": "HH:mm" }, "NUMBER_FORMATS": { "CURRENCY_SYM": "z\u0142", "DECIMAL_SEP": ",", "GROUP_SEP": "\u00a0", "PATTERNS": [ { "gSize": 3, "lgSize": 3, "macFrac": 0, "maxFrac": 3, "minFrac": 0, "minInt": 1, "negPre": "-", "negSuf": "", "posPre": "", "posSuf": "" }, { "gSize": 3, "lgSize": 3, "macFrac": 0, "maxFrac": 2, "minFrac": 2, "minInt": 1, "negPre": "(", "negSuf": "\u00a0\u00a4)", "posPre": "", "posSuf": "\u00a0\u00a4" } ] }, "id": "pl", "pluralCat": function (n) { if (n == 1) { return PLURAL_CATEGORY.ONE; } if (n == (n | 0) && n % 10 >= 2 && n % 10 <= 4 && (n % 100 < 12 || n % 100 > 14)) { return PLURAL_CATEGORY.FEW; } if (n != 1 && (n % 10 == 0 || n % 10 == 1) || n == (n | 0) && n % 10 >= 5 && n % 10 <= 9 || n == (n | 0) && n % 100 >= 12 && n % 100 <= 14) { return PLURAL_CATEGORY.MANY; } return PLURAL_CATEGORY.OTHER;} },
            "pt" : { "DATETIME_FORMATS": { "AMPMS": [ "AM", "PM" ], "DAY": [ "domingo", "segunda-feira", "ter\u00e7a-feira", "quarta-feira", "quinta-feira", "sexta-feira", "s\u00e1bado" ], "MONTH": [ "janeiro", "fevereiro", "mar\u00e7o", "abril", "maio", "junho", "julho", "agosto", "setembro", "outubro", "novembro", "dezembro" ], "SHORTDAY": [ "dom", "seg", "ter", "qua", "qui", "sex", "s\u00e1b" ], "SHORTMONTH": [ "jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez" ], "fullDate": "EEEE, d 'de' MMMM 'de' y", "longDate": "d 'de' MMMM 'de' y", "medium": "dd/MM/yyyy HH:mm:ss", "mediumDate": "dd/MM/yyyy", "mediumTime": "HH:mm:ss", "short": "dd/MM/yy HH:mm", "shortDate": "dd/MM/yy", "shortTime": "HH:mm" }, "NUMBER_FORMATS": { "CURRENCY_SYM": "R$", "DECIMAL_SEP": ",", "GROUP_SEP": ".", "PATTERNS": [ { "gSize": 3, "lgSize": 3, "macFrac": 0, "maxFrac": 3, "minFrac": 0, "minInt": 1, "negPre": "-", "negSuf": "", "posPre": "", "posSuf": "" }, { "gSize": 3, "lgSize": 3, "macFrac": 0, "maxFrac": 2, "minFrac": 2, "minInt": 1, "negPre": "(\u00a4", "negSuf": ")", "posPre": "\u00a4", "posSuf": "" } ] }, "id": "pt", "pluralCat": function (n) { if (n == 1) { return PLURAL_CATEGORY.ONE; } return PLURAL_CATEGORY.OTHER;} },
            "ru" : { "DATETIME_FORMATS": { "AMPMS": [ "\u0434\u043e \u043f\u043e\u043b\u0443\u0434\u043d\u044f", "\u043f\u043e\u0441\u043b\u0435 \u043f\u043e\u043b\u0443\u0434\u043d\u044f" ], "DAY": [ "\u0432\u043e\u0441\u043a\u0440\u0435\u0441\u0435\u043d\u044c\u0435", "\u043f\u043e\u043d\u0435\u0434\u0435\u043b\u044c\u043d\u0438\u043a", "\u0432\u0442\u043e\u0440\u043d\u0438\u043a", "\u0441\u0440\u0435\u0434\u0430", "\u0447\u0435\u0442\u0432\u0435\u0440\u0433", "\u043f\u044f\u0442\u043d\u0438\u0446\u0430", "\u0441\u0443\u0431\u0431\u043e\u0442\u0430" ], "MONTH": [ "\u044f\u043d\u0432\u0430\u0440\u044f", "\u0444\u0435\u0432\u0440\u0430\u043b\u044f", "\u043c\u0430\u0440\u0442\u0430", "\u0430\u043f\u0440\u0435\u043b\u044f", "\u043c\u0430\u044f", "\u0438\u044e\u043d\u044f", "\u0438\u044e\u043b\u044f", "\u0430\u0432\u0433\u0443\u0441\u0442\u0430", "\u0441\u0435\u043d\u0442\u044f\u0431\u0440\u044f", "\u043e\u043a\u0442\u044f\u0431\u0440\u044f", "\u043d\u043e\u044f\u0431\u0440\u044f", "\u0434\u0435\u043a\u0430\u0431\u0440\u044f" ], "SHORTDAY": [ "\u0432\u0441", "\u043f\u043d", "\u0432\u0442", "\u0441\u0440", "\u0447\u0442", "\u043f\u0442", "\u0441\u0431" ], "SHORTMONTH": [ "\u044f\u043d\u0432.", "\u0444\u0435\u0432\u0440.", "\u043c\u0430\u0440\u0442\u0430", "\u0430\u043f\u0440.", "\u043c\u0430\u044f", "\u0438\u044e\u043d\u044f", "\u0438\u044e\u043b\u044f", "\u0430\u0432\u0433.", "\u0441\u0435\u043d\u0442.", "\u043e\u043a\u0442.", "\u043d\u043e\u044f\u0431.", "\u0434\u0435\u043a." ], "fullDate": "EEEE, d MMMM y\u00a0'\u0433'.", "longDate": "d MMMM y\u00a0'\u0433'.", "medium": "dd.MM.yyyy H:mm:ss", "mediumDate": "dd.MM.yyyy", "mediumTime": "H:mm:ss", "short": "dd.MM.yy H:mm", "shortDate": "dd.MM.yy", "shortTime": "H:mm" }, "NUMBER_FORMATS": { "CURRENCY_SYM": "\u0440\u0443\u0431.", "DECIMAL_SEP": ",", "GROUP_SEP": "\u00a0", "PATTERNS": [ { "gSize": 3, "lgSize": 3, "macFrac": 0, "maxFrac": 3, "minFrac": 0, "minInt": 1, "negPre": "-", "negSuf": "", "posPre": "", "posSuf": "" }, { "gSize": 3, "lgSize": 3, "macFrac": 0, "maxFrac": 2, "minFrac": 2, "minInt": 1, "negPre": "-", "negSuf": "\u00a0\u00a4", "posPre": "", "posSuf": "\u00a0\u00a4" } ] }, "id": "ru", "pluralCat": function (n) { if (n % 10 == 1 && n % 100 != 11) { return PLURAL_CATEGORY.ONE; } if (n == (n | 0) && n % 10 >= 2 && n % 10 <= 4 && (n % 100 < 12 || n % 100 > 14)) { return PLURAL_CATEGORY.FEW; } if (n % 10 == 0 || n == (n | 0) && n % 10 >= 5 && n % 10 <= 9 || n == (n | 0) && n % 100 >= 11 && n % 100 <= 14) { return PLURAL_CATEGORY.MANY; } return PLURAL_CATEGORY.OTHER;} },
            "zh" : { "DATETIME_FORMATS": { "AMPMS": [ "\u4e0a\u5348", "\u4e0b\u5348" ], "DAY": [ "\u661f\u671f\u65e5", "\u661f\u671f\u4e00", "\u661f\u671f\u4e8c", "\u661f\u671f\u4e09", "\u661f\u671f\u56db", "\u661f\u671f\u4e94", "\u661f\u671f\u516d" ], "MONTH": [ "1\u6708", "2\u6708", "3\u6708", "4\u6708", "5\u6708", "6\u6708", "7\u6708", "8\u6708", "9\u6708", "10\u6708", "11\u6708", "12\u6708" ], "SHORTDAY": [ "\u5468\u65e5", "\u5468\u4e00", "\u5468\u4e8c", "\u5468\u4e09", "\u5468\u56db", "\u5468\u4e94", "\u5468\u516d" ], "SHORTMONTH": [ "1\u6708", "2\u6708", "3\u6708", "4\u6708", "5\u6708", "6\u6708", "7\u6708", "8\u6708", "9\u6708", "10\u6708", "11\u6708", "12\u6708" ], "fullDate": "y\u5e74M\u6708d\u65e5EEEE", "longDate": "y\u5e74M\u6708d\u65e5", "medium": "yyyy-M-d ah:mm:ss", "mediumDate": "yyyy-M-d", "mediumTime": "ah:mm:ss", "short": "yy-M-d ah:mm", "shortDate": "yy-M-d", "shortTime": "ah:mm" }, "NUMBER_FORMATS": { "CURRENCY_SYM": "\u00a5", "DECIMAL_SEP": ".", "GROUP_SEP": ",", "PATTERNS": [ { "gSize": 3, "lgSize": 3, "macFrac": 0, "maxFrac": 3, "minFrac": 0, "minInt": 1, "negPre": "-", "negSuf": "", "posPre": "", "posSuf": "" }, { "gSize": 3, "lgSize": 3, "macFrac": 0, "maxFrac": 2, "minFrac": 2, "minInt": 1, "negPre": "(\u00a4", "negSuf": ")", "posPre": "\u00a4", "posSuf": "" } ] }, "id": "zh", "pluralCat": function (n) { return PLURAL_CATEGORY.OTHER;} }
        };
        for (var locale in locales) {
            tmhDynamicLocaleCache.put(locale,locales[locale]);
        }
    }

    return Util

}

witbookerServices.factory("util",["$filter","$window","md5",util] );

function CurrencySettings(defaultCurrency,currentCurrency,conversionRate,rates) {

    this.defaultCurrency = defaultCurrency;
    this.currentCurrency = currentCurrency;
    this.conversionRate  = conversionRate;
    this.rates  = rates;
}

witbookerServices.provider('currencySettings', function currencySettingsProvider() {
    var defaultCurrency = "EUR";
    var currentCurrency = "EUR";
    var conversionRate  = 1;
    var rates  = {};

    this.defaultCurrency = function(value) {
        defaultCurrency = value;
    };
    this.currentCurrency = function(value) {
        currentCurrency = value;
    };
    this.conversionRate = function(value) {
        conversionRate = value;
    };

    this.rates = function(value) {
        rates = value;
        rates[defaultCurrency]=1;
    };

    this.$get = ["$filter", function currencySettingsFactory($filter) {
        return new CurrencySettings(defaultCurrency, currentCurrency,conversionRate,rates);
    }];
});
