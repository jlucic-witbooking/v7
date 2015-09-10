package withotel

import apibridge.Dispatcher
import com.witbooking.middleware.exceptions.MiddlewareException

//import com.witbooking.middleware.EstablishmentInfo
import com.witbooking.middleware.model.Chain
import com.witbooking.middleware.model.CurrencyExchange
import com.witbooking.middleware.model.Establishment
import com.witbooking.middleware.model.WitBookerVisualRepresentation
import com.witbooking.middleware.model.values.AvailabilityDataValue
import com.witbooking.middleware.model.values.types.SharedValue
import com.witbooking.witbooker.EstablishmentAdditionalProperties
import com.witbooking.witbooker.filters.EstablishmentFilter
import com.witbooking.witbooker.filters.Filter
import com.witbooking.witbooker.filters.InventoryFilter
import grails.plugin.cache.CacheEvict
import grails.plugin.cache.CachePut
import grails.plugin.cache.Cacheable
import org.apache.log4j.Logger

class CacheService {

    final static String TICKER_LANGUAGE_SEPARATOR="/"

    static transaction = false
    def grailsApplication
    static final Logger logger = Logger.getLogger(Dispatcher.class);
	private Dispatcher dispatcher = Dispatcher.getDispatcher()

    def getAri(String hotelTicker, List<String> inventoryTickers,
               Date start, Date end,
               String currency, List<String> promotionalCodes,String country){
        def promotionalCodestoARI=promotionalCodes?(new ArrayList<String>(promotionalCodes)).join(","):""
        currency=null
        def ARI= dispatcher.executeService(dispatcher.getAri, [hotelTicker,inventoryTickers,start,end,currency,promotionalCodestoARI,country])

        return ARI
    }

    @Cacheable(value='currency', key='#defaultCurrency' , condition = '#expiration<60' )
    def getConversionRate(final String defaultCurrency, double expiration=500 ){
        def cacheProxied=grailsApplication.mainContext.cacheService
        return cacheProxied.setConversionRate(defaultCurrency)
    }

    @CachePut(value='currency', key='#defaultCurrency')
    def setConversionRate(final String defaultCurrency) {
        CurrencyExchange currencyExchange=null;
        try{
            currencyExchange=dispatcher.executeService(dispatcher.getCurrencyConversionRate, [ defaultCurrency])
        }catch (MiddlewareException exception){
            logger.error("Could not get currency rate")
            return null
        }
        return [ conversionRate: currencyExchange, timestamp:new Date()  ]
    }


    def resetAllCache(hotelTicker){
        def cacheProxied=grailsApplication.mainContext.cacheService
        cacheProxied.resetApiData(hotelTicker)
        cacheProxied.resetInfoData(hotelTicker)
        cacheProxied.resetEstablishmentData(hotelTicker)
        cacheProxied.resetEstablishmentInfo(hotelTicker)
        cacheProxied.resetHotelData(hotelTicker)
        cacheProxied.resetEstablishmentInventoryRelations(hotelTicker)
    }

	@Cacheable(value='hotel', key='#hotelTicker')
	def getHotelInfo(String hotelTicker){
        println "[WITHOTEL] : getHotelInfo Creating Cache for hotel: '"+hotelTicker+ "' " +new java.util.Date()
        def hotelInfo = [:]
        try {
            hotelInfo.languages = dispatcher.executeService(dispatcher.getLanguages, [hotelTicker])
            hotelInfo.currencies = dispatcher.executeService(dispatcher.getCurrencies, [hotelTicker])
            hotelInfo.configurations = dispatcher.executeService(dispatcher.getConfigurations, [hotelTicker])

            hotelInfo.accommodations = [:]
            hotelInfo.discounts = [:]
            hotelInfo.services = [:]
            hotelInfo.locationPoints = [:]
            hotelInfo.establishment = [:]
            hotelInfo.comments = [:]

            for (ln in hotelInfo.languages) {
                def locale = ln.locale

                hotelInfo.establishment[locale] = dispatcher.executeService(dispatcher.getEstablishment, [hotelTicker, locale])
                hotelInfo.accommodations[locale] = dispatcher.executeService(dispatcher.getAccommodations, [hotelTicker, locale])
                hotelInfo.discounts[locale] = dispatcher.executeService(dispatcher.getDiscounts, [hotelTicker, locale])
                hotelInfo.services[locale] = dispatcher.executeService(dispatcher.getServices, [hotelTicker, locale])
                hotelInfo.locationPoints[locale] = dispatcher.executeService(dispatcher.getLocationPoints, [hotelTicker, locale])
                hotelInfo.comments[locale] = dispatcher.executeService(dispatcher.getReviews, [hotelTicker, locale])
            }
        } catch (MiddlewareException midEx) {
            hotelInfo.error = midEx
        }
        return hotelInfo
	}


    @Cacheable(value='countries', key='#keyName')
    def getLanguagesForCountries(String keyName) {
        def cacheProxied=grailsApplication.mainContext.cacheService
        def countries=null;
        try {
            countries=dispatcher.executeService(dispatcher.getCountriesMap, [ ]);
            countries.each{
                String lang, Map country->
                    cacheProxied.getCountriesByLanguage(lang,country)
            }
        } catch (Exception midEx) {
            throw midEx
        }
        return (countries as Map).keySet();
    }

    @Cacheable(value='countries', key='#language')
    def getCountriesByLanguage(String language,Map country){
        def cacheProxied=grailsApplication.mainContext.cacheService
        if(!country){
            try {
                Set<String> languagesForCountries=getLanguagesForCountries("countries");
                language=languagesForCountries.contains(language)?language:"local";
            } catch (Exception midEx) {
                throw midEx
                return null
            }
            return cacheProxied.getCountriesByLanguage(language,null)
        }
        List countriesSorted=(country.collect{
            key,name->
                [name:name,id:key]
        }).sort{(it.name as String).toLowerCase()}

        return countriesSorted
    }

    @CacheEvict(value="countries", key="#language")
    def resetCountriesByLanguage(language) {}


        /*NOTA: ESTA FUNCION SOLO LA USO PARA CACHEAR LO QUE VIENE DEL API, LUEGO DE AQUI TENGO OTRAS DOS FUNCIONES*/
    @Cacheable(value='api', key='#hotelTicker')
    def getEstablishmentFromApi(String hotelTicker, List<String> propertyNames){
        println "[WITBOOKER] : getEstablishmentFromApi Creating Cache for Establishment: '"+hotelTicker+ "' " +new java.util.Date()
        Map<String, Establishment> establishmentMap = [:]
        if(!propertyNames || propertyNames.isEmpty()){
            propertyNames=EstablishmentAdditionalProperties.ESTABLISHMENT_PROPERTY_NAMES
        }
        try {
            establishmentMap = dispatcher.executeService(dispatcher.getEstablishment, [hotelTicker, propertyNames])
        } catch (Exception midEx) {
            //midEx.printStackTrace()
            throw midEx
        }
        return  establishmentMap
    }

    @Cacheable(value='info', key='#hotelTicker')
    def getEstablishment(String hotelTicker, List<String> propertyNames,String language=null,establishment=null,reset=false){
        if (reset){return null}
        println "[WITBOOKER] : getEstablishment Creating Cache for Establishment: '"+hotelTicker+ "' " +new java.util.Date()
        Map<String, Establishment> establishmentMap = [:]
        def cacheProxied=grailsApplication.mainContext.cacheService
        def error=null
        try {
            if (establishment==null) {
                establishmentMap = getEstablishmentFromApi(hotelTicker,propertyNames)

                establishmentMap.each{
                    parentLanguage, parentEstablishment ->
                        establishment=parentEstablishment
                        cacheProxied.getEstablishmentInventoryRelations(establishment.ticker+"_inventoryRelations",establishment)
                        if (hotelTicker!=establishment.ticker){
                            println(" '${hotelTicker}' Incorrect establishment ticker '${establishment.ticker}' in establishment DB table")
                            error= new MiddlewareException(MiddlewareException.ERR_PARSE_DB_ENTITY,MiddlewareException.DESERR_PARSE_DB_ENTITY," '${hotelTicker}' sgnwrogjrng Incorrect establishment ticker '${establishment.ticker}' in establishment DB table")
                            return
                        }
                        cacheProxied.saveEstablishmentByLanguage(establishment.ticker+TICKER_LANGUAGE_SEPARATOR+parentLanguage,establishment)
                        if (establishment.hasProperty('establishments')){
                            (establishment as Chain).establishments.each() {
                                cacheProxied.getEstablishment(it.ticker,propertyNames,parentLanguage,it)
                                cacheProxied.getEstablishmentInventoryRelations(it.ticker+"_inventoryRelations",it)
                                cacheProxied.saveEstablishmentByLanguage(it.ticker+TICKER_LANGUAGE_SEPARATOR+parentLanguage,it )
                            }
                        }
                }
            }else{
                if (establishment.hasProperty('establishments'))
                    (establishment as Chain).establishments.each() {getEstablishment(it.ticker,propertyNames,language,it) }
            }

        } catch (Exception midEx) {
            //midEx.printStackTrace()
            error = midEx
        }
        if (error || establishment==null){ return ['error': error ]}

        return  ['languages':establishment.visualRepresentation.languages, 'additionalProperties':establishment.configuration, 'currencies':establishment.visualRepresentation.currencies, 'all':establishment ]
    }

    @Cacheable(value='inventoryRelations', key='#key')
    def getEstablishmentInventoryRelations(String key, Establishment establishment){
        if (establishment.getClass() == Chain)
            return
        Map<String,String> mapOfBoxIDByTicker=[:]
        Map<String,List<String>> mapOfTickersByBoxID=[:]

        WitBookerVisualRepresentation witBookerVisualRepresentation=establishment.getVisualRepresentation() as WitBookerVisualRepresentation
        witBookerVisualRepresentation.inventories.each {
            inventory->

                AvailabilityDataValue dataValue = inventory.getAvailability();
                if (dataValue.isOwnValue() ) {
                    mapOfBoxIDByTicker[inventory.ticker]=inventory.ticker
                    mapOfTickersByBoxID[inventory.ticker]=mapOfTickersByBoxID[inventory.ticker]?mapOfTickersByBoxID[inventory.ticker]:[inventory.ticker]
                }
                if (dataValue.isSharedValue()){
                    String parentTicker=((SharedValue) dataValue.getValue()).getTicker()
                    mapOfTickersByBoxID[parentTicker]=mapOfTickersByBoxID[parentTicker]?mapOfTickersByBoxID[parentTicker]:[parentTicker]

                    mapOfBoxIDByTicker[inventory.ticker]=parentTicker
                    mapOfTickersByBoxID[parentTicker].add(inventory.ticker)
                }
        }

        return [parentOfTicker:mapOfBoxIDByTicker, childrenOfTicker:mapOfTickersByBoxID]
    }


    @CacheEvict(value="inventoryRelations", key="#hotelTicker")
    def resetEstablishmentInventoryRelations(String hotelTicker) {

    }

    def buildFilter(it){

        Filter filter=new Filter()
        filter.active=Boolean.valueOf(it.active)
        filter.canRemove=Boolean.valueOf(it.canRemove)
        /*TODO: Throw method does not exists? */
        if(it.level==Filter.Level.INVENTORY){
            filter.closure=it.closure.getClass()==String? InventoryFilter.&"$it.closure": it.closure
            filter.level=Filter.Level.INVENTORY
        }else if (it.level==Filter.Level.ESTABLISHMENT) {
            filter.closure=it.closure.getClass()==String? EstablishmentFilter.&"$it.closure": it.closure
            filter.level=Filter.Level.ESTABLISHMENT
        }
        filter.params=it.params
        return filter
    }

    @Cacheable(value='currency', key='#defaultCurrency')
    def getConversionRate(final String defaultCurrency, final String previousCurrency, final String newCurrency){
        CurrencyExchange currencyExchange=dispatcher.executeService(dispatcher.getCurrencyConversionRate, [ defaultCurrency]);
        return [ conversionRate: currencyExchange.getPrice(newCurrency) ]
    }


        @CachePut(value='filter', key='#hotelTicker')
    def saveFilters(String hotelTicker,List<Map<String,Object>> filterConfiguration,List<Filter>additionalFilters) {
        /*TODO: IT SHOULD NOT BE A LIST, IT SHOULD BE A MAP TO ALLOW EASY OVERRIDE OF FILTERS BY NAME*/
       Map<String, List<Filter> > filterMap=[:]
       filterConfiguration.each {
           if (!filterMap[it.level])
               filterMap[it.level]=[]
           filterMap[it.level].add(new Filter(it))
       }
       additionalFilters.each {
           if (!filterMap[it.level])
               filterMap[it.level]=[]
           filterMap[it.level].add(it)
       }
        if(filterMap==null){
            logger.error("WARNING!! FILTER MAP IS NULL hotelTicker "+hotelTicker);
            logger.error("WARNING!! FILTER MAP IS NULL filterConfiguration "+filterConfiguration);
            logger.error("WARNING!! FILTER MAP IS NULL additionalFilters "+additionalFilters);
        }
       return filterMap
    }

    @Cacheable(value='filter', key='#hotelTicker')
    Map<String, List<Filter>> getFilters(String hotelTicker,List<Map<String,Object>> filterConfiguration=null,List<Filter>additionalFilters=null){


    }



    @CacheEvict(value="filter", key="#hotelTicker")
    def resetEstablishmentFilters(String hotelTicker) {
        println "[WITHOTEL] : Reset Filters Cache for hotel:'"+hotelTicker+"' "+new java.util.Date()
        '<h1>Reset Filters Data for <u>'+hotelTicker+'</u> </h1><h2>Success!</h2> '+new java.util.Date()
    }




    @CachePut(value='language', key='#key')
    def saveEstablishmentByLanguage(String key,establishment) {
        return establishment
    }

    @Cacheable(value = 'language', key = '#key')
    def getEstablishmentByLanguageNonRecursive(String key) {
        logger.error("THOU SHALL NOT BE EXECUTED "+key)
        print("THOU SHALL NOT BE EXECUTED "+key)
    }

    @Cacheable(value='language', key='#key')
    def getEstablishmentByLanguage(String key,List<String> propertyNames=null) {
        println "getEstablishmentByLanguage"
        String lang=key.tokenize("/").reverse().get(0)
        String hotelTicker=key.replace("/"+lang,"")
        def cacheProxied=grailsApplication.mainContext.cacheService
        cacheProxied.resetAllCache(hotelTicker)
        cacheProxied.getEstablishment(hotelTicker, propertyNames)
        return cacheProxied.getEstablishmentByLanguageNonRecursive(key)
    }

    @Cacheable(value='info', key='#key')
    def getEstablishmentInfob(String key, Object establishment ) {
        return establishment
    }


    @CacheEvict(value='info', key="#hotelTicker")
    def resetEstablishmentInfo(String hotelTicker) {
        println "[WITBOOKER] : Reset Cache for establishment:'"+hotelTicker+"' "+new java.util.Date()
        '<h1>Reset Data for <u>'+hotelTicker+'</u> </h1><h2>Success!</h2> '+new java.util.Date()
    }


    @CacheEvict(value="establishment", key="#key")
    def resetEstablishmentData(String key) {
        println "[WITBOOKER] : Reset Cache for establishment:'"+key+"' "+new java.util.Date()
        '<h1>Reset Data for <u>'+key+'</u> </h1><h2>Success!</h2> '+new java.util.Date()
    }

    @Cacheable(value='establishment', key='#hotelTicker')
    def getEstablishmentInfo(String hotelTicker){
        println "[WITBOOKER] : getEstablishmentInfo Creating Cache for Establishment: '"+hotelTicker+ "' " +new java.util.Date()
        def establishmentInfo = [:]
        try {
            establishmentInfo.establishment = [:]
            establishmentInfo.languages = dispatcher.executeService(dispatcher.getLanguages, [hotelTicker])

            for (ln in establishmentInfo.languages) {
                def locale = ln.locale
                establishmentInfo.establishment[locale] = dispatcher.executeService(dispatcher.getEstablishments, [[hotelTicker], locale])
            }

        } catch (MiddlewareException midEx) {
            //midEx.printStackTrace()
            establishmentInfo.error = midEx
        }
        return establishmentInfo
    }

    def subscribeNewsletter(hotelTicker, mail, locale) {
        return dispatcher.executeService(dispatcher.subscribeNewsletter,[hotelTicker, mail, locale])
    }

	def testFrontEndServices() {
		return dispatcher.executeService(dispatcher.testFrontEndServices,null)
	}



    @CacheEvict(value="hotel", key="#hotelTicker")
    def resetHotelData(String hotelTicker) {
        println "[WITHOTEL] : Reset Cache for hotel:'"+hotelTicker+"' "+new java.util.Date()
        '<h1>Reset Data for <u>'+hotelTicker+'</u> </h1><h2>Success!</h2> '+new java.util.Date()
    }
    @CacheEvict(value='language', key="#key")
    def resetEstablishmentByLanguage(String key) {
        println(key)
    }

    @CacheEvict(value="api", key="#hotelTicker")
    def resetApiData(String hotelTicker) {

    }

    @CacheEvict(value='info', key="#hotelTicker")
    def resetInfoData(String hotelTicker) {}

    @CachePut(value='language', key='#key')
    def resetLanguageData(String hotelTicker) {}

}