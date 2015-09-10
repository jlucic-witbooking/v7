package witbookerv7

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.maxmind.geoip2.record.Location
import com.witbooking.middleware.model.BookingPriceRulesApplied
import com.witbooking.middleware.model.Chain
import com.witbooking.middleware.model.DataValueHolder
import com.witbooking.middleware.model.DiscountApplied
import com.witbooking.middleware.model.Establishment
import com.witbooking.middleware.model.FrontEndMessage
import com.witbooking.middleware.model.HotelVisualRepresentation
import com.witbooking.middleware.model.Inventory
import com.witbooking.middleware.model.TransferData
import com.witbooking.middleware.model.WitBookerVisualRepresentation
import com.witbooking.middleware.model.dynamicPriceVariation.BookingPriceRule
import com.witbooking.middleware.model.values.*
import com.witbooking.middleware.model.values.types.ConstantValue
import com.witbooking.middleware.model.values.types.SharedValue
import com.witbooking.middleware.utils.DateUtil
import com.witbooking.witbooker.*
import com.witbooking.witbooker.filters.DiscountFilter
import com.witbooking.witbooker.filters.Filter
import com.witbooking.witbooker.filters.InventoryFilter
import com.witbooking.witbooker.filters.Util
import grails.converters.JSON
import grails.util.Environment
import groovy.time.TimeCategory
import groovyx.gpars.GParsPool
import groovyx.gpars.dataflow.Promise
import org.apache.log4j.Logger
import org.grails.plugins.web.taglib.ValidationTagLib
import witbookerv7.util.LegacyParams
import witbookerv7.util.Occupants
import witbookerv7.util.WitbookerParams
import withotel.CacheService

/*If this then that business rules*/
/*Los business rules se leen de un archivo de config que puede tener cada hotel, que puede sobreescribir el default
 * Puede ser a nivel de (estilo django)
 * establishment
 * establishment_individual
 * establishment_agregador
 * */
class BusinessRule{
    String condition
    boolean value
    Closure action
    /*Determines which business rules to apply according to precedence */
    def selectBusinesRules(){}
    /*Receives all API DATA */
    def applyBusinesRules(){}
}

/*TODO: FILTER DISCOUNTS*/
class BaseController {
    static allowedMethods = [
            chart: ['POST', 'GET'],
            calculateAvailability: ['POST', 'GET'],
            updateCurrencyConversionRate: ['POST', 'GET'],
            createEstablishmentLocalizedData: ['POST', 'GET'],
            step1: ['POST', 'GET'],
            updateSessionAsync: ['POST', 'GET'],
            testEstablishmentData: ['POST', 'GET'],
            addToCart: ['POST', 'GET'],
            removeFromCart: ['POST', 'GET']
    ]

    private static final Logger logger = Logger.getLogger(BaseController.class);

    static transaction = false
    def withotelService
    def cacheService
    def messageSource
    def geoIpService


    protected determineLanguage(Locale givenLanguage, List<Language>availableLanguages,Locale defaultLanguage){
        for (it in availableLanguages){
            if (it.code==givenLanguage.baseLocale.language){
                return new Locale(it.locale)
            }
        }
        return defaultLanguage
    }

    protected determineLocale(String givenLocale,String hotelDefaultLocale, List<Language>availableLanguages,String witbookerDefaultLocale,sessionWitbookerParams=null){
        for (it in availableLanguages){
            if (it.locale==givenLocale || it.code==givenLocale){
                return it
            }
        }
        if (sessionWitbookerParams.representation.locale){
            for (it in availableLanguages){
                if (it.locale==sessionWitbookerParams.representation.locale || it.code==sessionWitbookerParams.representation.locale){
                    return it
                }
            }
        }
        for (it in availableLanguages){
            if (it.locale==hotelDefaultLocale || it.code==hotelDefaultLocale){
                return it
            }
        }

        return witbookerDefaultLocale
    }


/*
*
* Locale lookup Priority
* LocaleParam
* LocaleStoredInSession
* HotelDefaultLanguage
* Check if hotel supports AppDefaultLanguage
* HotelFirstLanguage
*
* */


    protected determineLanguageFromLocale(String givenLocale,List<Language>availableLanguages){

        for (it in availableLanguages){
            if (it.locale==givenLocale || it.code==givenLocale){
                return it
            }
        }

    }


    protected requestStaticData(hotelTicker,propertyNames,locale,lang=null) {
        def establishmentStaticData=[:]
        establishmentStaticData["info"]= withotelService.getEstablishment(hotelTicker,propertyNames)
        if ((establishmentStaticData["info"] as Map).containsKey("error"))
            throw establishmentStaticData["info"]["error"]

        /*TODO: DO not hardode key Default Language or the default language itself*/

        com.witbooking.middleware.model.Language finalLanguage
        if (!finalLanguage && lang){
            finalLanguage=establishmentStaticData.info.languages.find({it.code==lang|| it.locale==lang})
        }

        if (!finalLanguage && locale){
            finalLanguage=establishmentStaticData.info.languages.find({it.code==locale|| it.locale==locale})
        }

        /*If no locale is given then try to get it from session*/
        if (!finalLanguage){
            WitbookerParams witbookerParams = session[hotelTicker]?session[hotelTicker].witbookerParams:null;
            if( witbookerParams){
                /*If there is a session use its locale */
                locale=witbookerParams.representation.locale
                finalLanguage=establishmentStaticData.info.languages.find({it.code==locale|| it.locale==locale})
            }
        }


        if (!finalLanguage){
            String hotelDefaultLanguage=establishmentStaticData.info.additionalProperties.containsKey("defaultLanguage")?new Locale(establishmentStaticData.info.additionalProperties.defaultLanguage):null
            if( hotelDefaultLanguage){
                locale=hotelDefaultLanguage
                finalLanguage=establishmentStaticData.info.languages.find({it.code==locale|| it.locale==locale})
            }
        }


        if (!finalLanguage){
            String appDefaultLanguage="es"
            if( appDefaultLanguage){
                locale=appDefaultLanguage
                finalLanguage=establishmentStaticData.info.languages.find({it.code==locale|| it.locale==locale})
            }
        }


        if (!finalLanguage){
            Language hotelFirstLanguage=(establishmentStaticData.info.languages as List<Language>).get(0)
            if( hotelFirstLanguage){
                finalLanguage=hotelFirstLanguage
            }else{
                throw Exception("Hotel has no languages");
            }
        }

        Locale givenLocale=new Locale(finalLanguage.locale)
        try {
            givenLocale.getISO3Language() != null;
        } catch (MissingResourceException e) {
            givenLocale=null
        }

        establishmentStaticData.info["defaultLocale"]= finalLanguage.code
        establishmentStaticData["establishment"]= withotelService.getEstablishmentByLanguage(hotelTicker+CacheService.TICKER_LANGUAGE_SEPARATOR+finalLanguage.locale,propertyNames)
        if(establishmentStaticData["establishment"].hasProperty('establishments')){
            (establishmentStaticData["establishment"] as Chain).establishments.eachWithIndex {
                childEstablishment, i ->
                    (establishmentStaticData["establishment"] as Chain).establishments.set(i, withotelService.getEstablishmentByLanguage(childEstablishment.ticker+CacheService.TICKER_LANGUAGE_SEPARATOR+finalLanguage.locale,propertyNames))
            }
        }
        return establishmentStaticData
    }


    protected flattenAndFilterSharedEstablishmentInventories(Establishment establishment,Map <String, Set<String>> allInventories){
        if (establishment.hasProperty('establishments')){
            (establishment as Chain).establishments.each {
                flattenAndFilterSharedEstablishmentInventories(it, allInventories)
            }
        }
        else{
            Set parentInventories=[]
            List<Inventory> establishmentInventories=new ArrayList<Inventory>((establishment.visualRepresentation as WitBookerVisualRepresentation).inventories )
            establishmentInventories.each(){
                inventory->
                    List<String> hashRangeValueKeys = [InventoryFilter.AVAILABILITY, InventoryFilter.LOCK, InventoryFilter.MAX_NOTICE, InventoryFilter.MIN_NOTICE, InventoryFilter.MAX_STAY,
                                                       InventoryFilter.MIN_STAY ]
                    for (hashRangeValueKey in hashRangeValueKeys) {
                        DataValue dataValue
                        if(hashRangeValueKey==HashRangeValue.LOCK){
                            dataValue = inventory.getAt("lock") as DataValue
                        }else{
                            dataValue = inventory.getAt(hashRangeValueKey) as DataValue
                        }
                        if (dataValue!=null && dataValue.getValueType() == EnumDataValueType.SHARED) {
                            parentInventories.add((dataValue.getValue()  as SharedValue).ticker)
                        }
                    }
            }
            allInventories.put(establishment.ticker,parentInventories )
        }
    }

    protected requestARI(Establishment establishment, Map<String,List<DataValueHolder>> inventories,
                         Date start, Date end,
                         String currency, List<String> promotionalCodes,WitbookerParams witbookerParams) {


        Map<String,Object> ariResult=[:]

        def callWitHotelService={
            String ticker->
                /*TODO: Test with  chains*/
                List<String> hotelInventoriesTickers =[]
                inventories[ticker].collect (hotelInventoriesTickers) { it.ticker }

                try {
                    ariResult[ticker]=withotelService.getAri(ticker, hotelInventoriesTickers, start,end,currency,  promotionalCodes,witbookerParams?.representation?.userCountry)
                } catch (java.lang.reflect.UndeclaredThrowableException e){
                    throw e.undeclaredThrowable
                } catch (Exception e){
                    throw e
                }
        }

        if (establishment.hasProperty("establishments") && (establishment as Chain).establishments.size()>0){

            GParsPool.withPool {
                (establishment as Chain).establishments.eachParallel {
                    callWitHotelService.call(it.ticker)
                }
            }

        }
        else
            callWitHotelService.call(establishment.ticker)

        return ariResult
    }

    protected requestChartARI(String ticker, List<String> inventories,
                              Date start, Date end,String currency) {
        String country=null;
        if(session && session[GLOBAL_SESSION_PARAM] && session[GLOBAL_SESSION_PARAM_COUNTRY]  ){
            country=session[GLOBAL_SESSION_PARAM][GLOBAL_SESSION_PARAM_COUNTRY]
        }
        return withotelService.getAri(ticker, inventories, start,end,currency,[],country)
    }

    protected Map<String,Closure> buildFilterForEstablishment(Establishment establishment,WitbookerParams witbookerParams){
        Map<Filter.Level, Map<Integer, List<Filter> > > inventoryFilters=[:]
        List<Filter> establishmentFilters

        Map<String,List<Filter> > filtersByLevel=cacheService.getFilters(establishment.ticker)

        if(filtersByLevel==null){
            logger.error(" NULL FILTERS !!! at Establishment "+ establishment.ticker)
            logger.error(" Witbooker Params "+ (witbookerParams as JSON))
        }

        if (filtersByLevel.containsKey(Filter.Level.INVENTORY)){
            if(!inventoryFilters.containsKey(Filter.Level.INVENTORY))
                inventoryFilters[Filter.Level.INVENTORY]=[:]
            filtersByLevel[Filter.Level.INVENTORY].each {
                if(!inventoryFilters[Filter.Level.INVENTORY].containsKey(it.priority))
                    inventoryFilters[Filter.Level.INVENTORY][it.priority]=[]
                inventoryFilters[Filter.Level.INVENTORY][it.priority].add(it)
            }
        }
        if (filtersByLevel.containsKey(Filter.Level.ESTABLISHMENT))
            establishmentFilters=filtersByLevel[Filter.Level.ESTABLISHMENT]

        if(inventoryFilters.size()>0){
            List priorities= new ArrayList<>(inventoryFilters.get(Filter.Level.INVENTORY).keySet()).sort()
            List<Closure> concatenatedFilters=[]
            priorities.each {
                priority->
                    concatenatedFilters.add(Util.&concat(inventoryFilters[Filter.Level.INVENTORY][priority]))
            }
            def inventoryFilter = Util.&compose(concatenatedFilters,witbookerParams)
            return [(establishment.ticker):inventoryFilter]
        }

        return null
    }
    protected filterStaticData(Establishment establishment,WitbookerParams witbookerParams){
        Map<String,Closure > inventoryFilters=[:]

        if (establishment.hasProperty('establishments')){
            inventoryFilters=((Chain)establishment).establishments.collectEntries(){
                buildFilterForEstablishment(it,witbookerParams)
            }
        }else{
            inventoryFilters=buildFilterForEstablishment(establishment,witbookerParams)
        }

        Map results=[:]
        filterEstablishmentInventories(establishment,inventoryFilters,results)

        return results

    }

    protected filterEstablishmentInventories(Establishment establishment, Map<String,Closure > inventoryFilter, Map results) {
        if (establishment.hasProperty('establishments'))
            return (establishment as Chain).establishments.each() { filterEstablishmentInventories(it,inventoryFilter,results) }
        Map<String,Map<String,ErrorMessage>> errorMessages =[:]
        def filteredInventories=inventoryFilter[establishment.ticker].call(establishment.ticker,(establishment.visualRepresentation as WitBookerVisualRepresentation).inventories,errorMessages)
        if(!results.containsKey(Filter.INVENTORY))
            results[Filter.INVENTORY]=[:]
        if(!results.containsKey(Filter.ERROR_MESSAGE))
            results[Filter.ERROR_MESSAGE]=[:]
        (results[Filter.INVENTORY] as Map).put(establishment.ticker,filteredInventories)
        (results[Filter.ERROR_MESSAGE] as Map).put(establishment.ticker,errorMessages)
        return results
    }

    /*TODO: Move to another place*/
    /*This variables prevents the execution of dynamic filter after one evaluation fails, preventing the accumulation of error messages*/
    static final boolean SHORT_CIRCUIT_DYNAMIC_FILTERS=true

    protected Map filterDynamicData(Map<String, List<Inventory>> hotelsInventories, Map<String, Map<String, ErrorMessage>> errorMessages, Map<String, List<HashRangeValue>> hotelsARI, WitbookerParams witbookerParams,Map<String, Map<String, HashRangeValue>> hotelARIHashMap,Map<String, List<Inventory>> allInventories) {

        if(!hotelsARI || hotelsARI.size()<=0)
            throw RuntimeException("Bad ARI for hotel "+witbookerParams.representation.ticker)

        hotelsARI.each {
            String hotelTicker, List<HashRangeValue> hotelARI ->
                if (!hotelARIHashMap[hotelTicker])
                    hotelARIHashMap[hotelTicker] = [:]
                hotelARI.each {
                    HashRangeValue hashRangeValue ->
                        if (!hotelARIHashMap[hotelTicker][hashRangeValue.ticker])
                            hotelARIHashMap[hotelTicker][hashRangeValue.ticker] = [:]
                        hotelARIHashMap[hotelTicker][hashRangeValue.ticker] = hashRangeValue
                }
        }
        Map<String, Map<String, Inventory>> hotelInventoriesHashMap = [:]
        hotelsInventories.each {
            String hotelTicker, List<Inventory> hotelInventories ->
                if (!hotelInventoriesHashMap[hotelTicker])
                    hotelInventoriesHashMap[hotelTicker] = [:]
                hotelInventories.each {
                    Inventory inventory ->
                        if (!hotelInventoriesHashMap[hotelTicker][inventory.ticker])
                            hotelInventoriesHashMap[hotelTicker][inventory.ticker] = [:]
                        hotelInventoriesHashMap[hotelTicker][inventory.ticker] = inventory
                }
        }

        Map<String, Map<String, Inventory>> allInventoriesHashMap = [:]
        allInventories.each {
            String hotelTicker, List<Inventory> hotelInventories ->
                if (!allInventoriesHashMap[hotelTicker])
                    allInventoriesHashMap[hotelTicker] = [:]
                hotelInventories.each {
                    Inventory inventory ->
                        if (!allInventoriesHashMap[hotelTicker][inventory.ticker])
                            allInventoriesHashMap[hotelTicker][inventory.ticker] = [:]
                        allInventoriesHashMap[hotelTicker][inventory.ticker] = inventory
                }
        }


        hotelsInventories.each {
            String hotelTicker, List<Inventory> inventories ->
                List<Inventory> selectedInventories = []
                for (inventory in inventories) {
                    List<Filter> filters = []
                    Filter filter = new Filter()
                    filter.params = [:]
                    /*TODO: do not hardcode param names*/
                    List<String> hashRangeValueKeys = [ InventoryFilter.LOCK,InventoryFilter.AVAILABILITY, InventoryFilter.MAX_NOTICE, InventoryFilter.MIN_NOTICE, InventoryFilter.MAX_STAY,
                                                        InventoryFilter.MIN_STAY ]
                    boolean removeFromResults = false
                    boolean dataError = false
                    boolean passedFilter = false
                    for (hashRangeValueKey in hashRangeValueKeys) {
                        /*find filter configuration by name: for example "filterByMaxNotice" */
                        filter = new Filter(EstablishmentStaticData.defaultFilterConfiguration[(EstablishmentStaticData.FILTER_NAME_PREFIX + hashRangeValueKey.capitalize())])
                        filter.params["startDate"] = witbookerParams.regularParams.startDate
                        filter.params["endDate"] = witbookerParams.regularParams.endDate

                        /*We analyze the static data structure and the ARI structure looking for the corresponding input value for the filters */
                        // def inventoryRangeValue = search(hashRangeValueKey, inventory.ticker, hotelARIHashMap[hotelTicker], allInventoriesHashMap[hotelTicker], inventory, 0)


                        def inventoryRangeValue
                        DataValue dataValue
                        /*TODO: Change the Inventory lock, to inventory Closed*/
                        if(hashRangeValueKey==HashRangeValue.LOCK){
                            dataValue = allInventoriesHashMap[hotelTicker].get(inventory.ticker).getAt("lock") as DataValue

                        }else{
                            dataValue = allInventoriesHashMap[hotelTicker].get(inventory.ticker).getAt(hashRangeValueKey) as DataValue
                        }
                        if (dataValue.getValueType() == EnumDataValueType.NULL_VALUE) {
                            inventoryRangeValue =  [ "value" :null ] /*TODO: This is not an error*/
                        }else{
                            if(hotelARIHashMap[hotelTicker] && hotelARIHashMap[hotelTicker].containsKey(inventory.ticker))
                                inventoryRangeValue = ["value":(hotelARIHashMap[hotelTicker][inventory.ticker] as HashRangeValue).getRangeValue(hashRangeValueKey)]
                        }



                        dataError = inventoryRangeValue == null
                        /* if there's a data inconsistency with the inventory we ignore it */
                        if (dataError) {
                            break
                        }
                        filter.params += inventoryRangeValue

                        passedFilter = filter.closure.call(inventory, filter.params, errorMessages[hotelTicker])
                        /*TODO: Do not get ARI for inventories that carry any error message, because of  filter that have canRemove False
                        * This inventories will only be shown as restricted, but theres no need to calculate any of its other properties, much less ask for ARI
                        * */
                        /*Passing the following if means that the inventory is restricted, either because any of the dynamic filters failed or because any of the static ones did (but only the ones that add messages and
                        * cannot remove , if a filter can Remove, the inventory should not even be present here, because its ARI is not even going to be evaluated.
                        *
                        * */
                        /*TODO: Can filters that cannot remove and not add message exist? this would not be logical, because then , the filter would have no effect, HOWEVER, it would permit for the inventory to be removed
                        * from result, because of this if , which would seem logical, being the case that the inventory DID NOT pass the filter*/
                        if (!passedFilter || errorMessages[hotelTicker].containsKey(inventory.ticker)) {
                            if (witbookerParams.representation.hideRestricted && hashRangeValueKey != InventoryFilter.LOCK && hashRangeValueKey != InventoryFilter.AVAILABILITY) {
                                removeFromResults = true
                            }
                            if ((hashRangeValueKey == InventoryFilter.LOCK || hashRangeValueKey == InventoryFilter.AVAILABILITY) && witbookerParams.representation.hideLocked) {
                                removeFromResults = true
                            }
                            if (SHORT_CIRCUIT_DYNAMIC_FILTERS && !passedFilter ){
                                break
                            }
                        }

                    }
                    /* if there's a data inconsistency with the inventory we ignore it */
                    if (dataError) {
                        continue
                    }
                    if (!removeFromResults)
                        selectedInventories.add(inventory)
                    if (removeFromResults) {
                        hotelInventoriesHashMap[hotelTicker].remove(inventory.ticker)
                        //errorMessages[hotelTicker].remove(inventory.ticker)
                    }

                }
                hotelsInventories[hotelTicker]=selectedInventories
        }
        /* TODO: Optimize tUse map from the be*/
        return hotelInventoriesHashMap


    }

    /*This function searches the ARI Hashmap for finding the final inventory that has the value referenced by the SHARED Inventory */
    protected Map<String,Object> search(String hashRangeValueKey, String inventoryTicker,
                                        Map<String, HashRangeValue> inventoriesARI, Map<String, Inventory> hotelInventories,
                                        Inventory originalInventory , Integer loopNumber) {
        if (loopNumber>=4 || inventoriesARI==null) {
            /*TODO:Throw error*/
            return null
        }
        try {
            DataValue dataValue
            /*TODO: Change the Inventory lock, to inventory Closed*/
            if(hashRangeValueKey==HashRangeValue.LOCK){
                dataValue = hotelInventories.get(inventoryTicker).getAt("lock") as DataValue
            }else{
                dataValue = hotelInventories.get(inventoryTicker).getAt(hashRangeValueKey) as DataValue
            }
            if (dataValue.getValueType() == EnumDataValueType.NULL_VALUE) {
                if(loopNumber>0)
                    return [ "value" :inventoriesARI[originalInventory.ticker].getRangeValue(hashRangeValueKey) ] /*TODO: This is not an error*/
                return [ "value" :null ] /*TODO: This is not an error*/
            } else if (dataValue.getValueType() == EnumDataValueType.CONSTANT) {
                return [ "value" :(dataValue.getValue() as ConstantValue<Integer>)]
            } else if (dataValue.getValueType() == EnumDataValueType.FORMULA || dataValue.getValueType() == EnumDataValueType.OWN ) {
                /*Search in ARI and return daily set*/
                /*This depends, if it is max/min notice/stay, only use the reservation start date and todays date  */
                /*TODO: check for null in  inventoriesARI.get(inventoryTicker) and then return null*/
                return  [ "value" :inventoriesARI.get(inventoryTicker).getRangeValue(hashRangeValueKey) ]
            } else if (dataValue.getValueType() == EnumDataValueType.SHARED) {
                return search(hashRangeValueKey, (dataValue.getValue()  as SharedValue).ticker ,inventoriesARI,hotelInventories,
                        originalInventory , ++loopNumber)
            } else {
                /*TODO:Throw error*/
                return null
            }
        } catch (MissingPropertyException ex) {
            /*TODO: This is an error !! Inventory does not exists!*/
            //ex.printStackTrace()
            return null
        }catch (Exception ex) {
            /*TODO: This is an error !! Inventory does not exists!*/
            //ex.printStackTrace()
            return null
        }
    }





    def chart() {
        withotelService.addCache(cacheService)
        Integer numberOfMonths=13
        try {
            numberOfMonths=Integer.parseInt(params.numberOfMonths)
        } catch (Exception ex) {
            logger.error("Invalid number of months in Chart: "+params.numberOfMonths)
        }
        if (!numberOfMonths || numberOfMonths>13 ||numberOfMonths<1)
            return render("error")

        Date startDate=new Date()
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.add(Calendar.MONTH, numberOfMonths);
        Date endDate=c.getTime()
        List<HashRangeValue> ari= requestChartARI(params.ticker,[params.inventory],startDate,endDate,params.currency)
        HashRangeValue inventoryAri
        ari.each {
            if (it.ticker==params.inventory){
                inventoryAri=it
            }
        }
        if (!inventoryAri){
            return render("error")
        }
        RangeValue rateDailySet= inventoryAri.getRangeValue(HashRangeValue.RATE)

        inventoryAri.getRangeValue(HashRangeValue.DISCOUNTS_APPLIED).dailySet.each{
            DailyValue<Float> dailyValue ->
                Float val=0
                if (dailyValue.getValue() != null){
                    val = ((Number) dailyValue.getValue().getValue()).floatValue()
                } else {
                    val = ((Number)  inventoryAri.getRangeValue(HashRangeValue.DISCOUNTS_APPLIED).defaultValue).floatValue()
                }
                rateDailySet.putDailyValue(new DailyValue<Float>(dailyValue.startDate,dailyValue.endDate,((rateDailySet.getValueForADate(dailyValue.startDate) as Float)+val) as Float))
        }


        /*Set Locked inventories rate to Cero*/
        inventoryAri.getRangeValue(HashRangeValue.LOCK).dailySet.each{
            DailyValue<Boolean> dailyValue ->
                if (dailyValue.value){
                    rateDailySet.putDailyValue(new DailyValue<Float>(dailyValue.startDate,dailyValue.endDate,0))
                }
        }

        /*Set Unavailable inventories rate to Cero*/
        inventoryAri.getRangeValue(HashRangeValue.ACTUAL_AVAILABILITY).dailySet.each{
            DailyValue<Integer> dailyValue ->
                if (dailyValue.value==0){
                    rateDailySet.putDailyValue(new DailyValue<Float>(dailyValue.startDate,dailyValue.endDate,0))
                }
        }
        Map result=[(HashRangeValue.RATE): inventoryAri.getRangeValue(HashRangeValue.RATE).dailySet ,  ]
        Gson gson = new GsonBuilder().setDateFormat("dd-MM-yyyy").create();
        render contentType: 'application/json', text:gson.toJson(result)
    }




    private Chain cloneChain(Chain chain) {
        Chain newChain=new Chain()
        newChain.id = chain.id;
        newChain.ticker = chain.ticker;
        newChain.name = chain.name;
        newChain.description = chain.description;
        newChain.phone = chain.phone;
        newChain.emailAdmin = chain.emailAdmin;
        newChain.active = chain.active;
        newChain.configuration = chain.configuration;
        newChain.media = chain.media;
        newChain.logo = chain.logo;
        newChain.visualRepresentation = chain.visualRepresentation;
        newChain.establishments = new ArrayList<>(chain.establishments);
        return newChain
    }

    protected List<Filter> buildEstablishmentSpecificFilters(WitbookerParams witbookerParams,boolean filterGuestsByAge,OccupantRestriction occupantRestriction,
                                                             Integer childrenMinAge,
                                                             Integer childrenMaxAge,
                                                             Integer teenagerMaxAge,
                                                             Integer adultMaxAge,
                                                             Integer babyMinAge,
                                                             String ticker=null){
        List<Filter> filtersToKeep=[]
        filtersToKeep.addAll(witbookerParams.filters)
        List<String> filtersToRemove=[]
        List<Filter> filtersToAdd=[]

        if(filterGuestsByAge){
            Occupants occupants=WitbookerParams.preProcessOccupancy(witbookerParams,occupantRestriction,childrenMinAge,childrenMaxAge,teenagerMaxAge,adultMaxAge,babyMinAge)
            if(occupants){
                Filter filter=new Filter(EstablishmentStaticData.defaultFilterConfiguration["filterByOccupationType"])
                occupants.occupantExtraFilter=[ticker]
                filter.params["occupants"]=occupants
                filtersToAdd.add(filter)
                filtersToRemove.add("filterByOccupationType")
            }
        }
        filtersToKeep.removeAll(){ filtersToRemove.contains(it.closureName) }
        filtersToKeep.addAll(filtersToAdd)
        return filtersToKeep
    }

    protected establishmentStatic(Object params){

        WitbookerParams witbookerParams=null
        /*TODO:Change for static constants inf EstablishmentAdditionalProperties*/
        List<String> propertyNames=EstablishmentAdditionalProperties.ESTABLISHMENT_PROPERTY_NAMES;
        EstablishmentAdditionalProperties establishmentAdditionalProperties
        Map establishmentStaticData
        if (params.getClass()==GrailsParameterMap){

            LegacyParams parsedParams = new LegacyParams()
            bindData(parsedParams, params, [include: LegacyParams.includeParams, exclude:LegacyParams.excludeParams])
            String hotelTicker=params.ticker
            String locale=params.locale? params.locale: parsedParams.language? parsedParams.language:null
            String lang=parsedParams.lang? parsedParams.lang:null

            establishmentStaticData=requestStaticData(hotelTicker,propertyNames,locale,lang)
            establishmentAdditionalProperties=new EstablishmentAdditionalProperties(establishmentStaticData.info.additionalProperties)

            def sessionWitbookerParams=session[hotelTicker] && session[hotelTicker].witbookerParams? session[hotelTicker].witbookerParams:null
            locale=establishmentStaticData.info.defaultLocale
            def sessionGlobalParams=session[GLOBAL_SESSION_PARAM]

            witbookerParams = new WitbookerParams(parsedParams,establishmentAdditionalProperties,locale,sessionWitbookerParams,sessionGlobalParams)

        }else if (params.getClass() == WitbookerParams){
            witbookerParams = (WitbookerParams)params
            establishmentStaticData=requestStaticData(witbookerParams.representation.ticker,propertyNames,witbookerParams.representation.locale)
        }

        List<Map<String,Object>> filterConfiguration=[]
        filterConfiguration.addAll(EstablishmentStaticData.defaultFilterConfiguration)
        /*TODO: ADD or OVERRIDE Other dynamic filters */
        Map filterByVisibleConfiguration= EstablishmentStaticData.defaultFilterConfiguration[(EstablishmentStaticData.FILTER_NAME_PREFIX+InventoryFilter.VISIBLE.capitalize())]


        Establishment establishment=(Establishment)establishmentStaticData["establishment"]
        /*Process Establishment Specific Filtere*/
        if(establishment.getClass()==Chain.class){
            ((Chain)establishment).establishments.each {
                com.witbooking.middleware.model.Hotel hotel->
                    boolean filterGuestsByAge=false
                    Integer adultMaxAge=null
                    Integer teenagerMaxAge=null
                    Integer childrenMaxAge=null
                    Integer childrenMinAge=null
                    Integer babyMinAge=null
                    OccupantRestriction occupantRestriction=null

                    if (EstablishmentAdditionalProperties.validateParam(hotel.configuration, EstablishmentAdditionalProperties.FILTER_GUESTS_BY_AGE)){
                        filterGuestsByAge = hotel.configuration[EstablishmentAdditionalProperties.FILTER_GUESTS_BY_AGE] == "1"
                    }
                    if(!filterGuestsByAge){
                        return cacheService.saveFilters(hotel.ticker,[filterByVisibleConfiguration ] ,buildEstablishmentSpecificFilters(witbookerParams,filterGuestsByAge,occupantRestriction,
                                childrenMinAge,childrenMaxAge,teenagerMaxAge,adultMaxAge,babyMinAge ))
                    }

                    if (EstablishmentAdditionalProperties.validateParam(hotel.configuration, EstablishmentAdditionalProperties.BABY_MIN_AGE)){
                        babyMinAge = Integer.parseInt((String)hotel.configuration[EstablishmentAdditionalProperties.BABY_MIN_AGE])
                    }
                    if (EstablishmentAdditionalProperties.validateParam(hotel.configuration, EstablishmentAdditionalProperties.ADULT_MAX_AGE)){
                        adultMaxAge = Integer.parseInt((String)hotel.configuration[EstablishmentAdditionalProperties.ADULT_MAX_AGE])
                    }
                    if (EstablishmentAdditionalProperties.validateParam(hotel.configuration, EstablishmentAdditionalProperties.TEENAGER_MAX_AGE)){
                        teenagerMaxAge = Integer.parseInt((String)hotel.configuration[EstablishmentAdditionalProperties.TEENAGER_MAX_AGE])
                    }
                    if (EstablishmentAdditionalProperties.validateParam(hotel.configuration, EstablishmentAdditionalProperties.CHILDREN_MAX_AGE_LEGACY)){
                        childrenMaxAge = Integer.parseInt((String)hotel.configuration[EstablishmentAdditionalProperties.CHILDREN_MAX_AGE_LEGACY])
                    }
                    if (EstablishmentAdditionalProperties.validateParam(hotel.configuration, EstablishmentAdditionalProperties.CHILDREN_MIN_AGE_LEGACY)){
                        childrenMinAge = Integer.parseInt((String)hotel.configuration[EstablishmentAdditionalProperties.CHILDREN_MIN_AGE_LEGACY])
                    }
                    if (EstablishmentAdditionalProperties.validateParam(hotel.configuration, EstablishmentAdditionalProperties.OCCUPATION_RESTRICTION_TYPE_LEGACY)){
                        occupantRestriction = OccupantRestriction.getOccupantRestrictionByValue(Integer.parseInt((String)hotel.configuration[EstablishmentAdditionalProperties.OCCUPATION_RESTRICTION_TYPE_LEGACY]))
                    }

                    cacheService.saveFilters(hotel.ticker,[filterByVisibleConfiguration ] ,buildEstablishmentSpecificFilters(witbookerParams,filterGuestsByAge,occupantRestriction,
                            childrenMinAge,childrenMaxAge,teenagerMaxAge,adultMaxAge,babyMinAge,hotel.ticker ))

            }
        }else{
            cacheService.saveFilters(establishment.ticker,[filterByVisibleConfiguration ] ,buildEstablishmentSpecificFilters(witbookerParams,witbookerParams.representation.filterGuestsByAge,
                    witbookerParams.regularParams.occupants.restriction,
                    witbookerParams.representation.childrenMinAge,
                    witbookerParams.representation.childrenMaxAge,
                    witbookerParams.representation.teenagerMaxAge,
                    witbookerParams.representation.adultMaxAge,
                    witbookerParams.representation.babyMinAge,establishment.ticker))
        }

        if(witbookerParams.representation.filterGuestsByAge ){
            Occupants occupantsFilteredByAge=WitbookerParams.preProcessOccupancy(witbookerParams,
                    witbookerParams.regularParams.occupants.restriction,
                    witbookerParams.representation.childrenMinAge,
                    witbookerParams.representation.childrenMaxAge,
                    witbookerParams.representation.teenagerMaxAge,
                    witbookerParams.representation.adultMaxAge,
                    witbookerParams.representation.babyMinAge,
            )
            witbookerParams.regularParams.occupants=occupantsFilteredByAge
        }


        if(witbookerParams.representation.hotels!=null && establishment.getClass()==Chain.class){
            Chain chain = cloneChain(establishment as Chain)
            chain.establishments.removeAll() {
                !witbookerParams.representation.hotels.contains(it.ticker);
            }
            if (!chain.establishments.isEmpty()){
                establishment=chain
                establishmentStaticData["establishment"]=chain
            }
        }
        if (establishment.visualRepresentation.getClass() == WitBookerVisualRepresentation.class) {
            TransferData transferData = (establishment.visualRepresentation as WitBookerVisualRepresentation).transferData
            if (transferData != null) {
                witbookerParams.representation.transferMinNotice = transferData.releaseMin
                witbookerParams.representation.transferAvailabilityHold = transferData.lockHours
            } else {
                witbookerParams.representation.transferMinNotice = 0
                witbookerParams.representation.transferAvailabilityHold = 24
            }
        }



        def filteredResults = filterStaticData(establishmentStaticData["establishment"],witbookerParams )
        return ["establishmentStaticData":establishmentStaticData, "filteredResults":filteredResults,
                "witbookerParams":witbookerParams,"establishmentAdditionalProperties":establishmentAdditionalProperties]
    }

    protected flattenEstablishmentInventories(Establishment establishment,Map <String, List<Inventory>> allInventories){
        if (establishment.hasProperty('establishments')){
            (establishment as Chain).establishments.each {
                flattenEstablishmentInventories(it, allInventories)
            }
        }
        else{
            allInventories.put(establishment.ticker,(establishment.visualRepresentation as WitBookerVisualRepresentation).inventories )
        }
    }
    protected filterDataValueHolderStatically(Establishment establishment,WitbookerParams witbookerParams,String type, Map <String, List<DataValueHolder>> dataValueHolderAccumulator,Reservation reservation=null){
        if (establishment.hasProperty('establishments')){
            (establishment as Chain).establishments.each {
                filterDataValueHolderStatically(it, witbookerParams,type,dataValueHolderAccumulator,reservation)
            }
        }
        else{
            List<String> hashRangeValueKeys=[]
            if (type==com.witbooking.middleware.model.Discount.class.toString()){
                (establishment.visualRepresentation as HotelVisualRepresentation).discounts.each () {
                    discount ->
                        Map<String, Map<String, ErrorMessage>> errorMessages =EstablishmentStaticData.filterDiscounts(discount,witbookerParams,[:],[HashRangeValue.MAX_STAY, HashRangeValue.MIN_STAY,  HashRangeValue.MAX_NOTICE, HashRangeValue.MIN_NOTICE])
                        if (!(errorMessages == null ||
                                (errorMessages.containsKey(discount.ticker) &&
                                        (errorMessages[discount.ticker].containsKey(DiscountFilter.CONTRACT) ||
                                                errorMessages[discount.ticker].containsKey(DiscountFilter.LOCK) ||
                                                errorMessages[discount.ticker].containsKey(DiscountFilter.EXPIRED) ||
                                                errorMessages[discount.ticker].containsKey(DiscountFilter.PROMO_CODE)
                                        )))) {

                            if(!dataValueHolderAccumulator.containsKey(establishment.ticker))
                                dataValueHolderAccumulator[establishment.ticker]=[]
                            dataValueHolderAccumulator[establishment.ticker].add(discount)
                        }
                }

            }else if(type==com.witbooking.middleware.model.Service.class.toString()){
                (establishment.visualRepresentation as HotelVisualRepresentation).services.each () {
                    service ->
                        def startDate=witbookerParams.regularParams.startDate
                        def endDate=witbookerParams.regularParams.endDate
                        if(reservation){
                            startDate=reservation.startDate
                            endDate=reservation.endDate
                        }
                        Map<String, Map<String, ErrorMessage>> errorMessages =EstablishmentStaticData.filterServices(service,witbookerParams,[:],startDate,endDate,[])
                        if (!(errorMessages == null ||
                                (errorMessages.containsKey(service.ticker) &&
                                        (errorMessages[service.ticker].containsKey(DiscountFilter.VALIDITY) ||
                                                errorMessages[service.ticker].containsKey(DiscountFilter.LOCK)
                                                || errorMessages[service.ticker].containsKey(DiscountFilter.MIN_STAY)
                                                || errorMessages[service.ticker].containsKey(DiscountFilter.MAX_STAY)
                                                || errorMessages[service.ticker].containsKey(DiscountFilter.MIN_NOTICE)
                                                || errorMessages[service.ticker].containsKey(DiscountFilter.MAX_NOTICE)
                                                || errorMessages[service.ticker].containsKey(DiscountFilter.EXPIRED)
                                                || errorMessages[service.ticker].containsKey(DiscountFilter.PROMO_CODE)
                                                || errorMessages[service.ticker].containsKey(DiscountFilter.VISIBLE)
                                        )))) {

                            if(!dataValueHolderAccumulator.containsKey(establishment.ticker))
                                dataValueHolderAccumulator[establishment.ticker]=[]
                            dataValueHolderAccumulator[establishment.ticker].add(service)
                        }
                }
            }
        }
    }

    protected establishmentARI(Map filteredResults, Establishment establishment, WitbookerParams witbookerParams){

        def filteredInventories=filteredResults[Filter.INVENTORY]
        Map<String,Map<String,ErrorMessage>> errorMessages =filteredResults[Filter.ERROR_MESSAGE]
        Map <String, List<DataValueHolder>> discountTickersToRequest=[:]
        Map <String, List<DataValueHolder>> servicesTickersToRequest=[:]
        filterDataValueHolderStatically(establishment,witbookerParams,com.witbooking.middleware.model.Discount.class.toString(),discountTickersToRequest)
        filterDataValueHolderStatically(establishment,witbookerParams,com.witbooking.middleware.model.Service.class.toString(),servicesTickersToRequest)

        Map <String, List<DataValueHolder>> dataValueHolderToRequestList=[:]
        for ( entry in discountTickersToRequest ) {
            if((filteredInventories as Map).containsKey(entry.key))
                dataValueHolderToRequestList[entry.key]=[]
            (dataValueHolderToRequestList[entry.key] as List).addAll(entry.value)
            filteredInventories[entry.key] && (dataValueHolderToRequestList[entry.key] as List).addAll(filteredInventories[entry.key])
            servicesTickersToRequest[entry.key] && (dataValueHolderToRequestList[entry.key] as List).addAll(servicesTickersToRequest[entry.key])
        }
        Map <String,List<HashRangeValue> > hotelsARI=requestARI(establishment,dataValueHolderToRequestList,witbookerParams.regularParams.startDate,witbookerParams.regularParams.endDate,witbookerParams.representation.currency,witbookerParams.regularParams.inventoryPromoCodes,witbookerParams)
        Map<String, Map<String, HashRangeValue>> hotelARIHashMap =[:]
        /*TODO: INCLUDE ALL INVENTORIES IN FILTERED INVENTORIES NOT JUST THE FILTERED ONES FOR SEARCHIIIING
        * Is this the best way?*/
        /*TODO: Use a better recursion style*/
        Map <String, List<Inventory>> allInventories=[:];
        flattenEstablishmentInventories(establishment,allInventories)
        filteredInventories = filterDynamicData(filteredInventories,errorMessages,hotelsARI ,witbookerParams,hotelARIHashMap, allInventories)
        return ["filteredInventories":filteredInventories, "hotelARIHashMap":hotelARIHashMap,"errorMessages":errorMessages]
    }


    protected builARIData(Establishment establishment, filteredInventories, hotelARIHashMap, errorMessages, witbookerParams) {
        Map ARIs = [:]
        Set<ErrorMessage> chainErrorMessage
        if (establishment.getClass() == com.witbooking.middleware.model.Chain) {
            Boolean chainAllFiltered=true
            Boolean chainAllRestricted=true
            (establishment as com.witbooking.middleware.model.Chain).establishments.collectEntries(ARIs) {
                def ARIData=builARIData(it, filteredInventories, hotelARIHashMap, errorMessages, witbookerParams)
                if(chainErrorMessage==null)
                    chainErrorMessage=new HashSet<ErrorMessage>(ARIData[it.ticker].errorMessages)
                chainErrorMessage=chainErrorMessage.intersect(ARIData[it.ticker].errorMessages)
                chainAllFiltered &=ARIData[it.ticker]["allFiltered"]
                chainAllRestricted &=(ARIData[it.ticker]["allRestricted"] || ARIData[it.ticker]["allFiltered"])
                return ARIData
            }
            ARIs["allFiltered"]=chainAllFiltered
            ARIs["allRestricted"]=chainAllFiltered ? false : chainAllRestricted
            ARIs["errorMessages"]=chainErrorMessage
            ARIs["discounts"]= EstablishmentStaticData.buildEstablishmentDiscounts(witbookerParams,hotelARIHashMap,establishment)
            return ARIs
        } else {
            Map dynamicData = EstablishmentStaticData.buildDynamicData(establishment, filteredInventories, witbookerParams, errorMessages, hotelARIHashMap)
            return [(establishment.ticker): [
                    "cheapestRate": dynamicData.hotelCheapestRate,
                    "inventoryLinesGrouped": dynamicData.hotelInventoryLinesGrouped,
                    "errorMessages": dynamicData.hotelErrorMessages,
                    "discounts": dynamicData.hotelDiscounts,
                    "allFiltered": dynamicData.hotelAllFiltered,
                    "allRestricted": dynamicData.hotelAllRestricted,
                    "activeDiscounts": dynamicData.activeDiscounts
            ]
            ]
        }

    }

    protected producePromoCodeMessage(WitbookerParams witbookerParams, List<String> newPromoCodes){

        def validCode=false
        newPromoCodes=newPromoCodes.collect(){it.toLowerCase()}
        witbookerParams.representation.activePromoCodes=witbookerParams.representation.activePromoCodes.collect(){it.toLowerCase()}
        for (code in  newPromoCodes){
            if(witbookerParams.representation.activePromoCodes.contains(code)){
                validCode=true
                break
            }
        }
        Message message=new Message()
        if (validCode){
            /*e4 :{type:Service,code:"promoCode"} */
            witbookerParams.representation.promoCodeActiveDataValueHolders.each {
                dataValueHolderTicker, info ->
                    if(!newPromoCodes.contains(info.code)){
                        return
                    }
                    message.title=info.code;
                    if(info.type==Service.class.toString()){
                        message.position=witbookerParams.representation.currentState=="StepOne" ? FrontEndMessage.Position.MODAL_STEP_1: FrontEndMessage.Position.MODAL_STEP_2
                        message.description=new ValidationTagLib().message(code: "trans.step1.codeok", locale: new Locale(witbookerParams.representation.locale))
                    }else if (info.type==Discount.class.toString()){
                        message.position=witbookerParams.representation.currentState=="StepOne" ? FrontEndMessage.Position.TOP_INVENTORY_STEP_1: FrontEndMessage.Position.MODAL_STEP_2
                        message.description=new ValidationTagLib().message(code: "trans.step1.codeok", locale: new Locale(witbookerParams.representation.locale))
                        message.type=FrontEndMessage.Type.SUCCESS
                    }else if (info.type==InventoryLine.class.toString()){
                        message.position=witbookerParams.representation.currentState=="StepOne" ? FrontEndMessage.Position.TOP_INVENTORY_STEP_1: FrontEndMessage.Position.MODAL_STEP_2
                        message.description=new ValidationTagLib().message(code: "trans.step1.codeok", locale: new Locale(witbookerParams.representation.locale))
                        message.type=FrontEndMessage.Type.SUCCESS
                    }else if (info.type==BookingPriceRule.class.toString()){
                        message.position=witbookerParams.representation.currentState=="StepOne" ? FrontEndMessage.Position.TOP_INVENTORY_STEP_1: FrontEndMessage.Position.MODAL_STEP_2
                        message.description=new ValidationTagLib().message(code: "trans.step1.codeok", locale: new Locale(witbookerParams.representation.locale))
                        message.type=FrontEndMessage.Type.SUCCESS
                    }
            }
        }else{
            message.title="ERROR";
            message.position=witbookerParams.representation.currentState=="StepOne" ? FrontEndMessage.Position.TOP_INVENTORY_STEP_1: FrontEndMessage.Position.MODAL_STEP_2
            message.description=new ValidationTagLib().message(code: "trans.step1.codeerror", locale: new Locale(witbookerParams.representation.locale))
            message.type=FrontEndMessage.Type.ERROR
        }
        return message
    }


    protected getAllDataValueHoldersPromoCodes(WitbookerParams witbookerParams, Establishment establishment){
        List<String>promoCodes=[]
        witbookerParams.regularParams.newPromoCodes=witbookerParams.regularParams.newPromoCodes.collect(){
            it.toLowerCase()
        }
        if (establishment.hasProperty('establishments')){
            (establishment as Chain).establishments.each {
                getAllDataValueHoldersPromoCodes(witbookerParams,it)
            }
        }else{
            WitBookerVisualRepresentation witBookerVisualRepresentation= establishment.visualRepresentation as WitBookerVisualRepresentation
            List<DataValueHolder> dataValueHolderList=[]
            dataValueHolderList.addAll(witBookerVisualRepresentation.discounts)
            dataValueHolderList.addAll(witBookerVisualRepresentation.services)
            dataValueHolderList.addAll(witBookerVisualRepresentation.inventories)

            List<String> validPromoCodes = []
            for (DataValueHolder dataValueHolder in dataValueHolderList ){
                String dataValueHolderPromocode=null
                if( dataValueHolder.hasProperty("promoCode") ){
                    dataValueHolderPromocode = dataValueHolder?.promoCode
                }else if( dataValueHolder.hasProperty("accessCode") ){
                    dataValueHolderPromocode =dataValueHolder?.accessCode
                }
                if(dataValueHolderPromocode){
                    for (String promoCode in dataValueHolderPromocode.split(',') ){
                        if(witbookerParams.regularParams.newPromoCodes.contains(promoCode.toLowerCase())){
                            validPromoCodes.addAll(dataValueHolderPromocode.split(',').collect(){it.toLowerCase()})
                            break;
                        }
                    }
                }
            }
            List<String> additivePromoCodes=[]
            List<String> decrementalPromoCodes=[]
            validPromoCodes.each{
                if(it.toLowerCase().indexOf(EstablishmentStaticData.CODE_EXCLUDED_PATTERN)<0){
                    additivePromoCodes.add(it.toLowerCase())
                }else if(it.toLowerCase().indexOf(EstablishmentStaticData.CODE_EXCLUDED_PATTERN)==0){
                    decrementalPromoCodes.add(it.toLowerCase().substring(EstablishmentStaticData.CODE_EXCLUDED_PATTERN.length()) )
                }
            }

            List<String> tmp=witbookerParams.regularParams.discountPromoCodes.collect(){it.toLowerCase()}
            witbookerParams.regularParams.discountPromoCodes=witbookerParams.regularParams.discountPromoCodes.collect(){it.toLowerCase()}
            additivePromoCodes=additivePromoCodes.collect(){it.toLowerCase()}
            decrementalPromoCodes=decrementalPromoCodes.collect(){it.toLowerCase()}
            witbookerParams.regularParams.discountPromoCodes.addAll(additivePromoCodes)
            witbookerParams.regularParams.discountPromoCodes.unique()
            witbookerParams.regularParams.discountPromoCodes.removeAll(decrementalPromoCodes)
            witbookerParams.regularParams.discountPromoCodes=tmp.intersect(witbookerParams.regularParams.discountPromoCodes)
            witbookerParams.regularParams.inventoryPromoCodes=witbookerParams.regularParams.discountPromoCodes
        }
    }


    def calculateAvailability(){
        if(!withotelService.cache)
            withotelService.addCache(cacheService)
        if(!EstablishmentStaticData.messageSource)
            EstablishmentStaticData.messageSource=new ValidationTagLib().message
        WitbookerParams witbookerParams
        try {
            witbookerParams=new WitbookerParams(request.JSON)
        }catch (Exception ex){
            logger.error("Error Getting Parameters in GetARI: "+ex)
            return render([:] as JSON)
        }
        String hotelTicker = witbookerParams.representation.ticker

        updateSession(witbookerParams,hotelTicker)
        witbookerParams=session[hotelTicker].witbookerParams

        Map<String,Object> establishmentARIResult
        Map<String,Object> establishmentData
        def filteredResults
        def establishment
        try {
            /*----------------------Generate Static Data----------------------*/
            establishmentData=establishmentStatic(witbookerParams)
            filteredResults=establishmentData.filteredResults
            establishment = establishmentData.establishmentStaticData.establishment
            getAllDataValueHoldersPromoCodes(witbookerParams,establishment)
            /*--------------------END Generate Static Data--------------------*/


            /*----------------------Generate Dynamic Data----------------------*/
            updateSession(witbookerParams,establishment.ticker)
            witbookerParams=session[establishment.ticker].witbookerParams
            establishmentARIResult=establishmentARI(filteredResults, establishment, witbookerParams)
        }catch (Exception ex){
            logger.error("Error Generating ARI with Request: "+ request.JSON)
            logger.error("Error Loading ARI: "+ ex)
            for (StackTraceElement s : ex.getStackTrace()) {
                logger.error(""+s);
            }
            logger.error("");
            return render([:] as JSON)
        }

        def filteredInventories=establishmentARIResult.filteredInventories
        def hotelARIHashMap = establishmentARIResult.hotelARIHashMap
        def errorMessages= establishmentARIResult.errorMessages

        /*--------------------AUTO EXCLUDE PROMO CODES --------------------*/




        /*--------------------END AUTO EXCLUDE PROMO CODES --------------------*/



        /*--------------------END Generate Dynamic Data--------------------*/
        def inventoryLinesGrouped = []
        Map<String, Accommodation> accommodationMap = [:]
        Map<String, InventoryLine> inventoryMap = [:]
        Map<String, Float> cheapestRate = [:]
        Float cheapestRateForHotel = Float.MAX_VALUE
        /*TODO: Convert this into a FUnction or sub-class that builds it*/
        /*Session Support*/
        /*END Session Support*/
        def ARIData=builARIData( establishment, filteredInventories,hotelARIHashMap,errorMessages,witbookerParams)
        insertConversionQuery(witbookerParams,establishment,request.getRemoteAddr(),filteredInventories,ARIData)
        ARIData["activePromoCodes"]= witbookerParams.representation.activePromoCodes
        ARIData["discountPromoCodes"]= witbookerParams.regularParams.discountPromoCodes
        if ( request.JSON.updateCart){
            Map updatedCartInfo=updateCart(establishment,witbookerParams.representation.language,witbookerParams.representation.currency,witbookerParams.regularParams.inventoryPromoCodes,witbookerParams,true);
            ARIData["cart"]=session[establishment.ticker].reservations;
        }

        if (witbookerParams.regularParams.newPromoCodes && witbookerParams.regularParams.newPromoCodes.size()>0 ){
            EstablishmentStaticData.analyzeBookingPriceRulesPromoCodes(establishment,witbookerParams,hotelARIHashMap,witbookerParams.regularParams.startDate,witbookerParams.regularParams.endDate)
            EstablishmentStaticData.buildServices(establishment,witbookerParams,hotelARIHashMap,witbookerParams.regularParams.startDate,witbookerParams.regularParams.endDate)
            ARIData["messages"]=[producePromoCodeMessage(witbookerParams,witbookerParams.regularParams.newPromoCodes)]
        }
        Gson gson = new GsonBuilder().setDateFormat("dd-MM-yyyy").create();

        render gson.toJson(ARIData)

    }

    protected getCountriesByLanguage(String language){
        try{
            return withotelService.getCountriesByLanguage(language)
        }catch(Exception ex){
            logger.error("Error Getting countries")
            for (StackTraceElement s : ex.getStackTrace()) {
                logger.error(""+s);
            }
            logger.error("");
        }
    }

    protected getEstablishmentData(Map params){
        if(!withotelService.cache)
            withotelService.addCache(cacheService)
        if(!EstablishmentStaticData.messageSource)
            EstablishmentStaticData.messageSource=new ValidationTagLib().message
        /*----------------------Generate Static Data----------------------*/
        Map<String,Object> establishmentData=establishmentStatic(params)
        def filteredResults=establishmentData.filteredResults
        def establishment = establishmentData.establishmentStaticData.establishment
        EstablishmentAdditionalProperties establishmentAdditionalProperties=establishmentData.establishmentAdditionalProperties
        WitbookerParams witbookerParams= establishmentData.witbookerParams
        (witbookerParams.representation.currentState=params.actionM=="step2" ? "StepTwo":"StepOne" ) || (witbookerParams.representation.currentState="StepOne")
        getAllDataValueHoldersPromoCodes(witbookerParams,establishment)
        updateSession(witbookerParams,establishment.ticker)
        witbookerParams=session[establishment.ticker].witbookerParams

        /*--------------------END Generate Static Data--------------------*/

        /*----------------------Generate Dynamic Data----------------------*/
        Map<String,Object> establishmentARIResult

        try {
            /*----------------------Generate Dynamic Data----------------------*/
            establishmentARIResult=establishmentARI(filteredResults, establishment, witbookerParams)
        }catch (Exception ex){
            logger.error("Error Generating in getEstablishmentData ARI with Request: "+ request.JSON)
            logger.error("Error Loading ARI: "+ ex)
            for (StackTraceElement s : ex.getStackTrace()) {
                logger.error(""+s);
            }
            logger.error("");

            return null
        }
        def filteredInventories=establishmentARIResult.filteredInventories
        def hotelARIHashMap = establishmentARIResult.hotelARIHashMap
        def errorMessages= establishmentARIResult.errorMessages
        /*--------------------END Generate Dynamic Data--------------------*/

        def translationsController = new TranslationController()
        EstablishmentStaticData staticData= new EstablishmentStaticData(establishment,filteredInventories,hotelARIHashMap,errorMessages,witbookerParams.representation.locale,witbookerParams,cacheService.getEstablishmentInventoryRelations(establishment.ticker+"_inventoryRelations",null))

        Gson gson = new GsonBuilder().setDateFormat("dd-MM-yyyy").create();
        /*TODO: Transform to JSON in the final controller*/

        /*Session Support*/
        updateSession(witbookerParams,establishment.ticker)
        /*END Session Support*/

        def cartData=gson.toJson(null);
        if(establishment.getClass()!=Chain){
            Map updatedCartInfo=updateCart(establishment,witbookerParams.representation.language,witbookerParams.representation.currency,witbookerParams.regularParams.inventoryPromoCodes,witbookerParams)
            if (!updatedCartInfo){
                return render([ error:[message: "SessionExpired" ,code:"EXPSESS" ] ] as JSON )
            }
            cartData=gson.toJson(session[establishment.ticker].reservations);
            if((updatedCartInfo["markedForRemoval"] as List<Map<String,String>>).size()>0){
                List<Reservation> reservations
                (updatedCartInfo["markedForRemoval"] as List< Map<String,String> >).each {
                    reservations= session[it.hotelTicker].reservations as List<Reservation>
                    removeReservation(reservations,it.id)
                }
            }
        }


        /* Save use statistics */
        insertConversionQuery(witbookerParams,establishment,request.getRemoteAddr(),filteredInventories,staticData.establishment)

        /* Save use statistics */
        setTrackingPixels(witbookerParams,establishment.ticker)

        witbookerParams.representation.countries=getCountriesByLanguage(witbookerParams.representation.locale)

        if ( witbookerParams.regularParams.newPromoCodes && witbookerParams.regularParams.newPromoCodes.size()>0 ){
            EstablishmentStaticData.analyzeBookingPriceRulesPromoCodes(establishment,witbookerParams,hotelARIHashMap,witbookerParams.regularParams.startDate,witbookerParams.regularParams.endDate)
            EstablishmentStaticData.buildServices(establishment,witbookerParams,hotelARIHashMap,witbookerParams.regularParams.startDate,witbookerParams.regularParams.endDate)
            staticData.establishment.messages.add(producePromoCodeMessage(witbookerParams,witbookerParams.regularParams.newPromoCodes))
        }

        witbookerParams.representation.conversionRate=withotelService.getConversionRate(witbookerParams.representation.defaultCurrency, witbookerParams.representation.defaultCurrency, witbookerParams.representation.currency)




        return [
                "translations": gson.toJson(translationsController.getMessageMap(witbookerParams.representation.locale)) ,
                "initialData":gson.toJson(staticData),
                "initialDataOriginal":staticData,
                "witbookerParams":gson.toJson(witbookerParams),
                "witbookerParamsOriginal":witbookerParams,
                "establishmentAdditionalProperties":establishmentAdditionalProperties,
                "cart":cartData
        ]
    }


    protected setTrackingPixels(WitbookerParams witbookerParams, String hotelTicker) {
        if(witbookerParams.representation.channel=="TripAdvisor")
            session[hotelTicker]["referrer"]="TripAdvisor"
    }

    protected insertConversionQuery(WitbookerParams witbookerParams, establishment, String ipAddress, Map filteredInventories,filteredData=null,establishmentIsChain=false,ignorePreviousQuery=false){
        GParsPool.withPool {

            if(!ignorePreviousQuery){
                if(session[establishment.ticker]!=null &&
                        session[establishment.ticker]["previousQuery"]!=null &&
                        session[establishment.ticker]["previousQuery"]==witbookerParams.regularParams.occupants.toString()+witbookerParams.regularParams.startDate.toString()+witbookerParams.regularParams.endDate.toString() ){
                    return
                }else{
                    /*StorePreviousQuery*/
                    session[establishment.ticker]["previousQuery"]=witbookerParams.regularParams.occupants.toString()+witbookerParams.regularParams.startDate.toString()+witbookerParams.regularParams.endDate.toString()
                }
            }

            if (establishment.getClass() == Chain && (establishment as Chain).establishments.size()>0){
                if(filteredData.getClass()==com.witbooking.witbooker.Chain){
                    (filteredData as com.witbooking.witbooker.Chain).establishments.each {
                        insertConversionQuery(witbookerParams,it,ipAddress,filteredInventories,it,true,true)
                    }
                }else if (filteredData.getClass()==LinkedHashMap.class) {
                    Chain chain=establishment as Chain
                    chain.establishments.each {
                        insertConversionQuery(witbookerParams,it,ipAddress,filteredInventories,filteredData,true,true)
                    }
                }
            }else{
                Closure conversionQuery= {
                    trackingID , channelID ->
                        String hotelTicker=establishment.ticker
                        String clientIp=ipAddress
                        Date checkInDate=witbookerParams.regularParams.startDate
                        Date checkOutDate=witbookerParams.regularParams.endDate
                        Integer rooms=(filteredInventories[establishment.ticker] as Map).size()
                        int adults=witbookerParams.regularParams.occupants.adults
                        int children=witbookerParams.regularParams.occupants.children
                        int infants=witbookerParams.regularParams.occupants.babies
                        String language=witbookerParams.representation.locale
                        boolean isSoldOut=false

                        if(filteredData.getClass()==com.witbooking.witbooker.Hotel){
                            isSoldOut= filteredData.allFiltered
                        }else if (filteredData.getClass()==LinkedHashMap.class) {
                            isSoldOut= filteredData[hotelTicker].allFiltered
                        }
                        boolean isChain=establishmentIsChain
                        try {
                            def result=withotelService.insertConversionQuery(hotelTicker,   clientIp,   checkInDate,
                                    checkOutDate,   rooms,   adults,
                                    children,   infants,   language,
                                    isSoldOut,   isChain, channelID,trackingID)
                        }catch (Exception e){
                            logger.error("Error inserting Conversion Query:  hotelTicker : "+hotelTicker+"      clientIp : "+   clientIp+"      " +
                                    "checkInDate : "+   checkInDate+"   checkOutDate : "+checkOutDate+"      rooms : "+   rooms+"      " +
                                    "adults : "+   adults+"   children : "+children+"      infants : "+   infants+"      language : "+   language+"    " +
                                    "isSoldOut : "+ isSoldOut+"      isChain : "+   isChain+"    channelID : "+ channelID+"    trackingID : "+trackingID)
                        }
                }
                String hotelTicker=establishment.ticker
                String trackingID=witbookerParams.representation?.tracking_id
                String channelID= witbookerParams.representation?.channel
                Closure storeQuery = conversionQuery.asyncFun()
                try {
                    Promise result=storeQuery(trackingID,channelID)
                    if(witbookerParams.representation?.tracking_id && witbookerParams.representation?.channel && (!session[hotelTicker]["tracking"] ||  session[hotelTicker]["tracking"]!= trackingID)  ){
                        session[hotelTicker]["tracking"]=witbookerParams.representation.tracking_id
                    }else{
                        trackingID=null
                        channelID=null
                    }
                }catch (Exception e){
                    logger.error("Error inserting Conversion Query: "+establishment.ticker )
                }

            }
        }
    }

    protected flattenEstablishmentHotels(Establishment establishment){
        List<String>hotelTickers=[]
        if (establishment.hasProperty('establishments')){
            (establishment as Chain).establishments.each {
                hotelTickers.addAll(flattenEstablishmentHotels(it))
            }
        }else{
            return [establishment.ticker]
        }
        return hotelTickers
    }

    protected getAllReservationInventories(List<String>hotelTickers){
        Map<String,List<String>> bookedInventories=[:]
        hotelTickers.each {
            hotelTicker->
                if(session[hotelTicker] && session[hotelTicker].reservations){
                    List<Reservation> reservations= session[hotelTicker].reservations as List<Reservation>
                    reservations.each {
                        if(!bookedInventories.containsKey(hotelTicker))
                            bookedInventories[hotelTicker]=[]
                        bookedInventories[hotelTicker].push(it.inventoryLine)
                    }
                }
        }
        bookedInventories.each {
            hotelTicker, List<String>inventoryLines->
                withotelService.getAri(hotelTicker, inventoryLines, wit,end,currency,[],session[ticker]?.wibookerParams?.representation?.userCountry)
        }
    }

    protected updateCart(Establishment establishment, Language language,String currency,List<String> promoCodes,WitbookerParams witbookerParams,boolean includeServices=false){
        List<String>hotelTickers=flattenEstablishmentHotels(establishment)
        List<Map<String,String>> markedForRemoval=[]
        Map<String,Service> servicesAvailable=[:]
        hotelTickers.each {
            hotelTicker->
                if(session[hotelTicker] && session[hotelTicker].reservations){
                    List<Reservation> reservations= session[hotelTicker].reservations as List<Reservation>
                    com.witbooking.middleware.model.Hotel hotel= (requestStaticData(hotelTicker,[],language.code) as Map)?.establishment
                    if(hotel==null){
                        return null;
                    }
                    reservations.each {
                        reservation->
                            Map <String, List<DataValueHolder>> servicesToRequest=[:]
                            Map <String, List<DataValueHolder>> discountsToRequest=[:]
                            filterDataValueHolderStatically(establishment,witbookerParams,com.witbooking.middleware.model.Service.class.toString(),servicesToRequest,reservation)
                            filterDataValueHolderStatically(hotel,witbookerParams,com.witbooking.middleware.model.Discount.class.toString(),discountsToRequest,reservation)
                            List<HashRangeValue> ARIData
                            String inventoryTicker
                            if(reservation.inventoryLine.getClass()==String)
                                inventoryTicker= reservation.inventoryLine
                            else
                                inventoryTicker= reservation.inventoryLine.ticker
                            List<Inventory> establishmentInventories=new ArrayList<Inventory>((hotel.visualRepresentation as WitBookerVisualRepresentation).inventories )
                            Inventory inventory=establishmentInventories.find{inventoryTicker==it.ticker}

                            discountsToRequest.each {
                                key,discountList->
                                    discountList.removeAll{
                                        discount->
                                            return !inventory?.discountList?.contains(discount)
                                    }
                            }

                            List<String> tickersToGetFromARI=[]
                            tickersToGetFromARI.add(inventoryTicker)
                            if(servicesToRequest.containsKey(hotelTicker)){
                                tickersToGetFromARI+=servicesToRequest[hotelTicker].collect(){it.ticker}
                            }
                            if(discountsToRequest.containsKey(hotelTicker)){
                                tickersToGetFromARI+=discountsToRequest[hotelTicker].collect(){it.ticker}
                            }

                            ARIData=withotelService.getAri(hotelTicker, tickersToGetFromARI, reservation.startDate,reservation.endDate,witbookerParams.representation.currency,promoCodes,session[GLOBAL_SESSION_PARAM][GLOBAL_SESSION_PARAM_COUNTRY]) as List<HashRangeValue>
                            com.witbooking.middleware.model.Hotel dummyHotel=new com.witbooking.middleware.model.Hotel()
                            WitBookerVisualRepresentation dummyWitBookerVisualRepresentation=new WitBookerVisualRepresentation([],[],[],[],[],[inventory],[:],null,null)
                            dummyHotel.setVisualRepresentation(dummyWitBookerVisualRepresentation)
                            dummyHotel.ticker=establishment.ticker

                            Map filteredResults=filterStaticData(dummyHotel,witbookerParams)
                            List<Inventory> filteredInventories=filteredResults[Filter.INVENTORY][hotelTicker]
                            filteredResults[Filter.ERROR_MESSAGE][hotelTicker]=filteredResults[Filter.ERROR_MESSAGE][establishment.ticker]
                            Map errorMessages =filteredResults[Filter.ERROR_MESSAGE]
                            Map<String, Map<String, HashRangeValue>> hotelARIHashMap =[:]



                            def filti=filterDynamicData(
                                    [(hotelTicker):[inventory]],
                                    errorMessages,
                                    [(hotelTicker):ARIData],
                                    witbookerParams,
                                    hotelARIHashMap,
                                    [(hotelTicker):(hotel.visualRepresentation as WitBookerVisualRepresentation).inventories],
                            )

                            /*TODO: Filter based on Availability and quantity!! */
                            HashRangeValue hashRangeValue=ARIData.find{inventoryTicker==it.ticker}
                            if(hashRangeValue==null){
                                logger.error("Invalid inventory ticker "+inventoryTicker+ " in Hotel "+hotelTicker)
                                logger.error("Reservation "+reservation )
                                return ["markedForRemoval":[],"servicesAvailable":[:]]
                            }

                            reservation.establishment["ticker"]=hotelTicker

                            reservation.inventoryLine=EstablishmentStaticData.buildInventoryLine(inventory,errorMessages[hotelTicker][inventoryTicker],hashRangeValue,witbookerParams,new HashSet<String>(),reservation)

                            reservation.accommodation=EstablishmentStaticData.buildAccommodation(inventory)
                            reservation.serviceRates=[:]
                            reservation.roomRates=[:]
                            reservation.appliedBookingPriceRules=calculateBookingPriceRulesApplied(hashRangeValue.getRangeValue(HashRangeValue.RULES_APPLIED),reservation.quantity)

                            if((reservation.inventoryLine as InventoryLine).errorMessage && (reservation.inventoryLine as InventoryLine).errorMessage.size()>0)
                                markedForRemoval.push([hotelTicker:hotelTicker,id:reservation.id])
                            else{
                                reservation.roomRates=hashRangeValue.getRangeValue(HashRangeValue.RATE)
                                reservation.appliedDiscounts=calculateDiscountsApplied(hotel.visualRepresentation.discounts,hashRangeValue.getRangeValue(HashRangeValue.DISCOUNTS_APPLIED),reservation.quantity)
                                reservation.ariDiscountsApplied=hashRangeValue.getRangeValue(HashRangeValue.DISCOUNTS_APPLIED)

                                if (includeServices){
                                    List<com.witbooking.middleware.model.Service> inventoryServices=inventory.serviceList
                                    inventoryServices.each {
                                        service->
                                            //HashRangeValue serviceHashRangeValue = ARIData.find{service.ticker==it.ticker}
                                            def newService=EstablishmentStaticData.buildService(service,witbookerParams,hotelARIHashMap[hotelTicker],reservation.startDate,reservation.endDate)
                                            if(newService!=null){
                                                servicesAvailable.put(newService.ticker,newService)
                                                (reservation.inventoryLine as InventoryLine).addService(service)
                                                //reservation.serviceRates[newService.ticker]=serviceHashRangeValue.getRangeValue(HashRangeValue.RATE)
                                            }
                                    }
                                    reservation.servicesAvailable=servicesAvailable
                                }
                            }
                    }
                }
        }
        return ["markedForRemoval":markedForRemoval,"servicesAvailable":servicesAvailable]

    }

    protected calculateDiscountsApplied(List<com.witbooking.middleware.model.Discount> allDiscounts,RangeValue ARIDiscountsApplied,int numRooms ){

        Map<String,DiscountApplied> discountAppliedMap=[:]
        ARIDiscountsApplied.dailySet.each{
            DailyValue<Float> dailyValue ->
                Float val=0
                if (dailyValue.getValue() == null)
                    return null
                String discountTicker= dailyValue.getValue().getKey() as String;
                com.witbooking.middleware.model.Discount discount = allDiscounts.find{discountTicker==it.ticker}
                RangeValue rangeValue=new RangeValue()
                if(discountAppliedMap.containsKey(discountTicker))
                    rangeValue=(discountAppliedMap[discountTicker]).discountPrice
                val=((Number) dailyValue.getValue().getValue()).floatValue() * (dailyValue.daysBetweenDates() + 1)
                DailyValue<Float> discountDailyValue = new DailyValue<>(dailyValue.startDate, dailyValue.endDate, discount.reduction)
                rangeValue.putDailyValue(discountDailyValue)
                if(!discountAppliedMap.containsKey(discountTicker))
                    discountAppliedMap[discountTicker]=new DiscountApplied()
                DiscountApplied discountApplied=discountAppliedMap[discountTicker]
                discountApplied.discountId=discount.id
                discountApplied.discountTicker=discount.ticker
                discountApplied.discountName=discount.name
                discountApplied.percentage=discount.percentage
                discountApplied.reduction=discount.reduction
                discountApplied.discountPrice=rangeValue
                discountApplied.totalDiscountAmount=(discountApplied.totalDiscountAmount + (val * -1) )
        }
        new ArrayList<DiscountApplied>(discountAppliedMap.values()).collect {
            it->
                it.totalDiscountAmount*=numRooms
                it
        }
    }

    protected calculateBookingPriceRulesApplied(RangeValue ARIBookingPriceRulesApplied, int numRooms  ){

        List<BookingPriceRulesApplied> bookingPriceRuleAppliedList=[]

        ARIBookingPriceRulesApplied?.dailySet?.each{
            DailyValue<List < BookingPriceRulesApplied> > dailyValue ->
                List<BookingPriceRulesApplied> rules=( dailyValue.getValue() )
                bookingPriceRuleAppliedList.addAll(rules)
        }

        bookingPriceRuleAppliedList.each {
            it->
                it.totalVariationAmount*=numRooms
                it
        }
        return bookingPriceRuleAppliedList
    }

    protected flattenAndFilterEstablishmentInventories(Establishment establishment,Map <String, List<Inventory>> allInventories,Map<String,List<String>> givenInventories){
        if (establishment.hasProperty('establishments')){
            (establishment as Chain).establishments.each {
                flattenAndFilterEstablishmentInventories(it, allInventories,givenInventories)
            }
        }else{
            if(givenInventories.containsKey(establishment.ticker) ){
                List<Inventory> establishmentInventories=new ArrayList<Inventory>((establishment.visualRepresentation as WitBookerVisualRepresentation).inventories )
                establishmentInventories.retainAll(){ givenInventories[establishment.ticker].contains(it.ticker)}
                allInventories.put(establishment.ticker,establishmentInventories )
            }
        }
    }
    def updateCurrencyConversionRate(){
        def previousCurrency=params.previousCurrency ? params.previousCurrency : params.defaultCurrency ? params.defaultCurrency :null
        def newCurrency=params.newCurrency ? params.newCurrency : params.defaultCurrency ? params.defaultCurrency :null
        def defaultCurrency=params.defaultCurrency ? params.defaultCurrency :null
        def hotelTicker=params.hotelTicker ? params.hotelTicker :null

        if(!previousCurrency || !newCurrency || !defaultCurrency || !hotelTicker){
            return render([error:true] as JSON)
        }
        WitbookerParams witbookerParams=new WitbookerParams()
        witbookerParams.representation=new WitbookerParams.Representation()
        witbookerParams.representation.currency=newCurrency
        witbookerParams.representation.currencySymbol=com.witbooking.witbooker.Currency.getCurrencySymbol(newCurrency)
        updateSession(witbookerParams,hotelTicker)

//        return render(withotelService.getConversionRate(defaultCurrency, previousCurrency, newCurrency) as GSON)
        return render("SUCCESS")

    }

    def createEstablishmentLocalizedData(){
        if(!withotelService.cache)
            withotelService.addCache(cacheService)
        if(!EstablishmentStaticData.messageSource)
            EstablishmentStaticData.messageSource=new ValidationTagLib().message
        if(!request?.JSON?.language){
            logger.error("Invalid Request  "+request?.JSON)
            return render([:] as JSON)
        }
        Locale locale=new Locale(request.JSON.language)
        Map establishmentInfo=withotelService.getEstablishment(request.JSON.establishmentTicker,[])
        Language selectedLanguage=null;

        for (com.witbooking.middleware.model.Language language in establishmentInfo.languages){
            if (language.code==request.JSON.locale){
                selectedLanguage=new Language(language.id,language.name,language.code,language.locale,language.charset)
            }
        }
        if (!selectedLanguage){
            com.witbooking.middleware.model.Language lang=establishmentInfo.languages[0]
            selectedLanguage=new Language(lang.id,lang.name,lang.code,lang.locale,lang.charset)
            locale=new Locale(selectedLanguage.locale)
        }

        Establishment establishment=withotelService.getEstablishmentByLanguage(request.JSON.establishmentTicker+cacheService.TICKER_LANGUAGE_SEPARATOR+locale.ISO3Language,EstablishmentAdditionalProperties.ESTABLISHMENT_PROPERTY_NAMES) as Establishment

        List<String> hotels = request.JSON.hotels

        if(hotels!=null && establishment.getClass()==Chain.class){
            Chain chain = cloneChain(establishment as Chain)
            chain.establishments.removeAll() {
                !hotels.contains(it.ticker);
            }
            if (!chain.establishments.isEmpty()){
                establishment=chain
            }
        }

        if(establishment.hasProperty('establishments')){
            (establishment as Chain).establishments.eachWithIndex {
                childEstablishment, i ->
                    (establishment as Chain).establishments.set(i, withotelService.getEstablishmentByLanguage(childEstablishment.ticker+CacheService.TICKER_LANGUAGE_SEPARATOR+locale.ISO3Language,EstablishmentAdditionalProperties.ESTABLISHMENT_PROPERTY_NAMES))
            }
        }

        Map<String,List<String>> givenInventories=request.JSON.inventories
        Map <String, List<Inventory>> filteredInventories=[:]
        if(!establishment){
            logger.error("");
            logger.error("Cannot get Establishment Data  "+request.JSON)
            logger.error("");
            return render([:] as JSON)
        }

        flattenAndFilterEstablishmentInventories(establishment,filteredInventories,givenInventories)
        EstablishmentStaticData staticData = new EstablishmentStaticData(establishment,filteredInventories,request.JSON.discounts as Map,request.JSON.services as Map,request.JSON.language,cacheService.getEstablishmentInventoryRelations(establishment.ticker+"_inventoryRelations",null))
        /*Session Support*/
        WitbookerParams witbookerParams=new WitbookerParams()
        witbookerParams.representation=new WitbookerParams.Representation()
        witbookerParams.representation.locale=request.JSON.locale
        witbookerParams.representation.language=selectedLanguage

        try{
            def countries=withotelService.getCountriesByLanguage(witbookerParams.representation.locale)
            witbookerParams.representation.countries=countries
        }catch(Exception ex){
            logger.error("Error Getting countries")
            for (StackTraceElement s : ex.getStackTrace()) {
                logger.error(""+s);
            }
            logger.error("");
            return render([:] as JSON)
        }

        updateSession(witbookerParams,establishment.ticker)
        /*END Session Support*/
        staticData.establishment.allFiltered=null
        staticData.establishment.allRestricted=null

        Gson gson = new GsonBuilder().setDateFormat("dd-MM-yyyy").create();
        render gson.toJson(staticData)
    }


    /*Session Support*/

    final static String GLOBAL_SESSION_PARAM= "global_session_param"
    final static String GLOBAL_SESSION_PARAM_START_DATE= "startDate"
    final static String GLOBAL_SESSION_PARAM_END_DATE= "endDate"
    final static String GLOBAL_SESSION_PARAM_COUNTRY= "country"

    protected updateSession(WitbookerParams witbookerParams,String hotelTicker){
        if(!session[hotelTicker]){
            session[hotelTicker]=[:]
        }
        if(!session[GLOBAL_SESSION_PARAM]){
            session[GLOBAL_SESSION_PARAM]=[:]
        }

        if(witbookerParams?.regularParams?.startDate){
            session[GLOBAL_SESSION_PARAM][GLOBAL_SESSION_PARAM_START_DATE]=witbookerParams.regularParams.startDate
        }
        if(witbookerParams?.regularParams?.endDate){
            session[GLOBAL_SESSION_PARAM][GLOBAL_SESSION_PARAM_END_DATE]=witbookerParams.regularParams.endDate
        }

        if(session[hotelTicker].witbookerParams){
            (session[hotelTicker].witbookerParams as WitbookerParams).updateWitbookerParams(witbookerParams)
        }else{
            session[hotelTicker].witbookerParams=witbookerParams
        }
        if(session[hotelTicker].witbookerParams?.representation?.userCountry=="undefined"){
            String currentIPAddress=geoIpService.getIpAddress(request)
            Environment.executeForCurrentEnvironment {
                development {
                    if(currentIPAddress=="127.0.0.1"){
                        currentIPAddress="80.36.225.37"
                    }
                }
            }
            Location clientLocation=geoIpService.getLocation(currentIPAddress)
            session[GLOBAL_SESSION_PARAM][GLOBAL_SESSION_PARAM_COUNTRY]=clientLocation?.countryCode
            session[hotelTicker].witbookerParams.representation.userCountry=clientLocation?.countryCode
        }
        witbookerParams=session[hotelTicker].witbookerParams
        if(!session[hotelTicker].witbookerParams.representation?.referer){
            witbookerParams.representation.referer=request.getHeader('referer')
        }

    }


    protected cleanSessionAfterReservation(String hotelTicker){
        if(!session[hotelTicker]){
            return
        }

        if(session[hotelTicker].witbookerParams){
            session[hotelTicker].witbookerParams=(session[hotelTicker].witbookerParams as WitbookerParams).cleanedWitbookerParams();
        }
        session[hotelTicker].reservations=[];
        session[hotelTicker].reservationOcurring=false
    }

    /*END Session Support*/

    def step1() {

        render view:"/basecontroller/index"
    }



    protected builARITestData(Establishment establishment, filteredInventories, hotelARIHashMap, errorMessages, witbookerParams) {
        Map ARIs = [:]
        if (establishment.getClass() == com.witbooking.middleware.model.Chain) {
            (establishment as com.witbooking.middleware.model.Chain).establishments.collectEntries(ARIs){
                builARITestData(it, filteredInventories, hotelARIHashMap, errorMessages, witbookerParams)
            }
            return ARIs
        } else {
            Map dynamicData = EstablishmentStaticData.buildDynamicData(establishment, filteredInventories, witbookerParams, errorMessages, hotelARIHashMap)
            Map inventoriesData=[:]

            dynamicData.hotelInventoryLinesGrouped.each {
                Map inventoryLineGrouped->
                    inventoryLineGrouped.inventoryLine.each{
                        InventoryLine line= it as InventoryLine;
                        inventoriesData[line.ticker]=[
                                "rate":line.totalRate? line.totalRate:0,
                                "avail":line.availability?line.availability:0 ,
                                "closed": line.errorMessage.contains(new ErrorMessage("",InventoryFilter.VISIBLE,null)) || line.errorMessage.contains(new ErrorMessage("",InventoryFilter.LOCK,null)) || line.errorMessage.contains(new ErrorMessage("",InventoryFilter.AVAILABILITY,null) ),
                                "restricted":(!line.errorMessage.isEmpty() && (!line.errorMessage.contains(new ErrorMessage("",InventoryFilter.VISIBLE,null)) && !line.errorMessage.contains(new ErrorMessage("",InventoryFilter.LOCK,null)) && !line.errorMessage.contains(new ErrorMessage("",InventoryFilter.AVAILABILITY,null)) ) ),
                                errorMessages: line.errorMessage
                        ]
                    }

            }
            return [(establishment.ticker): inventoriesData

            ]
        }
    }
    def updateSessionAsync(){
        WitbookerParams witbookerParams=new WitbookerParams(request.JSON)
        updateSession(witbookerParams,witbookerParams.representation.ticker)
        render "Ok"
    }

    def testEstablishmentData(){
        if(!withotelService.cache)
            withotelService.addCache(cacheService)
        if(!EstablishmentStaticData.messageSource)
            EstablishmentStaticData.messageSource=new ValidationTagLib().message
        /*----------------------Generate Static Data----------------------*/


        List<String> propertyNames=[
                "fltr_tipo_ocupacion", "defaultcurrency", "defaultlanguage",
                "ocultarRestringuidas","ocultarBloqueadas","tachaPrecios","maxAdults","maxChildren","maxBabies","colapsarExtras",

                "noMostrarCodigoPromocional","minedadnino","maxedadnino","googleanalyticscodenumber","googleanalyticsdomains",
                'urlwebhotel'  ,'logolinkstohotel',

                'unityName','autoDisplayFront','maxLinesShowByFilter','maxNochesFront',"maxHabitacionesParaOcultarSoloQuedan","maxHabitacionesFront",

                "colorCabecera","fondoBotonesCuerpo2","fondoCabecera","alturaCabecera","fondoCuerpo","colorCuerpo",
                "colorCabeceraInactivo","fondoBotonesCuerpo","colorLinia","step1SinImpuestos"
        ]


        //LegacyParams parsedParams = new LegacyParams(params)
        LegacyParams parsedParams = new LegacyParams()
        bindData(parsedParams, params, [include: LegacyParams.includeParams, exclude:LegacyParams.excludeParams])

        String hotelTicker=params.ticker
        String locale=params.locale? params.locale: parsedParams.lang? parsedParams.lang:"spa"
        def establishmentStaticData=requestStaticData(hotelTicker,propertyNames,locale)
        def establishmentAdditionalProperties=new EstablishmentAdditionalProperties(establishmentStaticData.info.additionalProperties)
        locale=establishmentStaticData.info.defaultLocale
        WitbookerParams witbookerParams = new WitbookerParams(parsedParams,establishmentAdditionalProperties,locale,null)
        witbookerParams.regularParams.occupants.restriction=OccupantRestriction.NONE
        witbookerParams.representation.hideRestricted=false
        witbookerParams.representation.hideLocked=false
        witbookerParams.filters.each {if (it.canRemove && it.closureName != "filterByPromoCode"){it.canRemove=false} }

        Map<String,Object> establishmentData=establishmentStatic(witbookerParams)
        def filteredResults=establishmentData.filteredResults
        def establishment = establishmentData.establishmentStaticData.establishment

        /*--------------------END Generate Static Data--------------------*/
        /*----------------------Generate Dynamic Data----------------------*/
        Map<String,Object> establishmentARI=establishmentARI(filteredResults, establishment, witbookerParams)
        def filteredInventories=establishmentARI.filteredInventories
        def hotelARIHashMap = establishmentARI.hotelARIHashMap
        def errorMessages= establishmentARI.errorMessages
        /*--------------------END Generate Dynamic Data--------------------*/

        render builARITestData( establishment, filteredInventories,hotelARIHashMap,errorMessages,witbookerParams) as JSON

    }

    def addToCart(){
        def reservationData=request.JSON
        if(!reservationData || !reservationData.establishment || !reservationData.establishment.ticker
                ||  !reservationData.parentEstablishment || !reservationData.parentEstablishment.ticker
                ||  !session || !session[reservationData.parentEstablishment.ticker] ){
            render "Error"
            logger.error("Error adding to cart with request ${request.JSON as JSON}" )
            return
        }
        String chainTicker
        String hotelTicker
        (chainTicker=reservationData?.parentEstablishment?.ticker) || ( chainTicker=null)
        (hotelTicker=reservationData?.establishment?.ticker) || ( hotelTicker=null)

        Reservation reservation=new Reservation()
        reservation.inventoryLine=reservationData.inventoryLine
        reservation.quantity=reservationData.quantity
        reservation.startDate= WitbookerParams.RegularParams.parseDate(reservationData.startDate)
        use(TimeCategory) {
            reservation.startDate=DateUtil.toBeginningOfTheDay(reservation.startDate)+1.day-1.seconds
        }
        reservation.endDate= WitbookerParams.RegularParams.parseDate(reservationData.endDate)
        //reservation.id=reservationData.inventoryID+reservationData.startDate+reservationData.endDate
        reservation.id=UUID.randomUUID().toString()

        if(!session[hotelTicker])
            session[hotelTicker]=[:]

        if(session[hotelTicker].reservations){
            (session[hotelTicker].reservations as List<Reservation>).push(reservation)
        }else{
            session[hotelTicker].reservations=[reservation]
        }
        if(chainTicker!=hotelTicker){
            updateSession(session[chainTicker].witbookerParams,hotelTicker)
        }

        render ( [id:reservation.id] as JSON)


    }

    def removeFromCart(){
        def reservationData=request.JSON
        if(!reservationData || !reservationData.establishment || !reservationData.establishment.ticker || !session[reservationData.establishment.ticker]){

            if(reservationData?.establishment?.ticker && !session[reservationData.establishment.ticker]){
                logger.error("Error removing from cart with request ${request.JSON as JSON}" )
                return render([ error:[message: "SessionExpired" ,code:"EXPSESS" ] ] as JSON)
            }

            logger.error("Error removing from cart with request ${request.JSON as JSON}" )
            return render("Error")
        }
        def hotelTicker=reservationData.establishment.ticker
        List<Reservation> reservations= session[hotelTicker].reservations as List<Reservation>
        boolean removed=false
        if(reservations && !reservations.isEmpty()){
            removed=removeReservation(reservations,reservationData.id)
        }
        if (!removed){
            logger.error("Reservation with ID "+reservationData.id+" was not found. At hotel "+hotelTicker+ " current reservations are: "+ reservations.collect(){ it.id }.join(" , "))
        }


        if(removed){render ( [id:reservationData.id] as JSON)}else{render ( [error:"No such reservation"] as JSON)}
    }
    protected removeReservation(List<Reservation> reservations,String idForRemoval){
        if(!reservations)
            return false
        int oldSize=reservations.size()
        def index=-1
        if (idForRemoval=="removeAll"){
            reservations.removeAll(){true}
            return true
        }


        reservations.eachWithIndex {
            reservation,i->
                if(reservation.id==idForRemoval)
                    index=i;return
        }
        if (index!=-1 && !reservations?.isEmpty())
            reservations.remove(index)
        return index!=-1
    }

}