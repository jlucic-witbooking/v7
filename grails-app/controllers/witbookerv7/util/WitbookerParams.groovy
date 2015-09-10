package witbookerv7.util

import com.witbooking.middleware.model.Guest
import com.witbooking.middleware.utils.DateUtil
import com.witbooking.witbooker.EstablishmentAdditionalProperties
import com.witbooking.witbooker.EstablishmentStaticData
import com.witbooking.witbooker.Language
import com.witbooking.witbooker.OccupantRestriction
import com.witbooking.witbooker.filters.Filter
import grails.converters.JSON
import org.apache.log4j.Logger
import groovy.time.*
import witbookerv7.BaseController

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.regex.Pattern


public class OccupantAgeRange implements Comparable<OccupantAgeRange>{

    private Integer upperBound
    private Integer lowerBound
    private String  occupantType

    OccupantAgeRange(Integer upperBound, Integer lowerBound, String occupantType) {
        this.upperBound = upperBound
        this.lowerBound = lowerBound
        this.occupantType = occupantType
    }

    @Override
    int compareTo(OccupantAgeRange o) {
        return upperBound-o.upperBound
    }
}

class Occupants{
    Integer adults
    Integer children
    Integer babies
    Integer teenagers
    Integer seniors
    OccupantRestriction restriction = OccupantRestriction.NONE
    List<String> occupantExtraFilter
    List<Integer> guestAges

    @Override
    public String toString() {
        return "Occupants{" +
                "adults=" + adults +
                ", children=" + children +
                ", babies=" + babies +
                ", restriction=" + restriction +
                '}';
    }
}


class WitbookerParams {

    private static final Logger logger = Logger.getLogger(WitbookerParams.class);

    final static String IL_EQUAL="il_equal"
    final static String IL_LIKE="il_like"
    final static String IL_CLEAR="il_clear"

    WitbookerParams() {}

    def cleanedWitbookerParams(){

        WitbookerParams witbookerParams=new WitbookerParams();

        if (!witbookerParams.representation)
            witbookerParams.representation=new Representation()
        if (!witbookerParams.regularParams)
            witbookerParams.regularParams=new RegularParams()

        if (!this.representation)
            this.representation=new Representation()
        if (!this.regularParams)
            this.regularParams=new RegularParams()

        if(this.regularParams.startDate){
            witbookerParams.regularParams.startDate=this.regularParams.startDate
        }
        if(this.regularParams.endDate){
            witbookerParams.regularParams.endDate=this.regularParams.endDate
        }
        if(this.representation.currency){
            witbookerParams.representation.currency=this.representation.currency
        }
        if(this.regularParams.extra ){
            witbookerParams.regularParams.extra=this.regularParams.extra
        }
        if(this.regularParams.occupants){
            witbookerParams.regularParams.occupants=this.regularParams.occupants
        }
        if (this.representation.view){
            witbookerParams.representation.view = this.representation.view
        }
        if (this.representation.iframeMode){
            witbookerParams.representation.iframeMode = this.representation.iframeMode
        }
        if (this.representation.iframeResizeDomain){
            witbookerParams.representation.iframeResizeDomain = this.representation.iframeResizeDomain
        }
        if (this.representation.allowsDisablingOccupationFilter){
            witbookerParams.representation.allowsDisablingOccupationFilter = this.representation.allowsDisablingOccupationFilter
        }

    }

    def updateWitbookerParams(WitbookerParams witbookerParams){

        if (!this.representation)
            this.representation=new Representation()
        if (!this.regularParams)
            this.regularParams=new RegularParams()

        witbookerParams.representation && witbookerParams.representation.properties.each {
            key,value ->
                if(key=="class"){
                    return
                }
                if(value!=null){
                    this.representation.setProperty(key,value)
                }
        }
        witbookerParams.regularParams && witbookerParams.regularParams.properties.each {
            key,value ->
                if(key=="class"){
                    return
                }
                if(value!=null){
                    this.regularParams.setProperty(key,value)
                }
        }
        this.filters=witbookerParams.filters
    }

    WitbookerParams(Object witbookerParams) {
        regularParams = new RegularParams()
        representation=new Representation()
        filters=[]
        Filter filter=new Filter()
        Occupants occupants=new Occupants()
        Map givenRegularParams=witbookerParams.regularParams as Map;
        Map givenRepresentation=witbookerParams.representation as Map;
        if(!givenRegularParams)
            throw new Exception("Invalidad Data Given for building WitbookerParams Object ${witbookerParams as JSON}" )

        if (givenRegularParams.containsKey("startDate")){
            regularParams.setStartDate(givenRegularParams.startDate as String)
            use(TimeCategory) {
                if(regularParams.startDate==null){
                    println("Invalid start date "+givenRegularParams.startDate)
                    regularParams.startDate=new Date()
                }
                regularParams.startDate=DateUtil.toBeginningOfTheDay(regularParams.startDate)+1.day-1.seconds
            }
        }

        if (givenRegularParams.containsKey("endDate")){
            regularParams.setEndDate(givenRegularParams.endDate as String)
            if(regularParams.endDate==null){
                println("Invalid end date "+givenRegularParams.endDate)
                use(TimeCategory) {
                    regularParams.endDate=regularParams.startDate+24.hours
                }
            }
            /*TODO: Do not hardcode, use a constant, not the hardcoded filtername in a string */
            filter=new Filter(EstablishmentStaticData.defaultFilterConfiguration["filterByCheckInCheckOut"])
            filter.params["startDate"]=regularParams.startDate
            filter.params["endDate"]=regularParams.endDate
            filters.add(filter)

            filter=new Filter(EstablishmentStaticData.defaultFilterConfiguration["filterByValidity"])
            filter.params["startDate"]=regularParams.startDate
            filter.params["endDate"]=regularParams.endDate
            filters.add(filter)
        }



        if (givenRegularParams.containsKey("occupants")){
            occupants.adults=(givenRegularParams.occupants as Map)? givenRegularParams.occupants.adults!="*"?givenRegularParams.occupants.adults:-1 : -1
            occupants.children=(givenRegularParams.occupants as Map)? givenRegularParams.occupants.children:-1
            occupants.babies=(givenRegularParams.occupants as Map)? givenRegularParams.occupants.babies:-1
            occupants.teenagers=(givenRegularParams.occupants as Map)? givenRegularParams.occupants.teenagers:-1
            occupants.seniors=(givenRegularParams.occupants as Map)? givenRegularParams.occupants.seniors:-1
            occupants.guestAges=(givenRegularParams.occupants as Map)? givenRegularParams.occupants.guestAges:null
        }else{
            occupants.adults=-1
            occupants.children=-1
            occupants.babies=-1
            occupants.guestAges=null
        }
        if (givenRegularParams.containsKey("occupants")){
            occupants.restriction=OccupantRestriction.valueOf(givenRegularParams.occupants.restriction)
            /*TODO: Do not hardcode, use a constant, not the hardcoded filtername in a string */
            if(occupants.adults!=-1){
                filter=new Filter(EstablishmentStaticData.defaultFilterConfiguration["filterByOccupationType"])
                filter.params["occupants"]=occupants
                filters.add(filter)
            }
            regularParams.occupants=occupants
        }

        if (givenRegularParams.containsKey("guestAges")){
            if(givenRegularParams.guestAges instanceof  Collection){
                try{
                    regularParams.guestAges=givenRegularParams.guestAges.collect{Integer.parseInt(it)}
                }catch (Exception e){
                    regularParams.guestAges=[];
                }
            }
        }
        regularParams.newPromoCodes=givenRegularParams.containsKey("newPromoCodes") ?  givenRegularParams.newPromoCodes: null

        regularParams.discountPromoCodes=givenRegularParams.containsKey("discountPromoCodes") ?  givenRegularParams.discountPromoCodes: null
        regularParams.inventoryPromoCodes=givenRegularParams.containsKey("inventoryPromoCodes")? givenRegularParams.inventoryPromoCodes: null
        regularParams.isPromoCodeActive=givenRegularParams.containsKey("isPromoCodeActive")? givenRegularParams.isPromoCodeActive: false

        filter=new Filter(EstablishmentStaticData.defaultFilterConfiguration["filterByPromoCode"])
        filter.params["promoCodes"]=regularParams.inventoryPromoCodes?regularParams.inventoryPromoCodes:[]
        filters.add(filter)

        regularParams.extra=[:]
        if(givenRegularParams.containsKey("extra") && givenRegularParams.extra.containsKey("il_equal") ){
            filter=new Filter(EstablishmentStaticData.defaultFilterConfiguration["filterByEqualTicker"])
            filter.params=["values":givenRegularParams.extra.il_equal]
            filters.add(filter)
            regularParams.extra.put(WitbookerParams.IL_EQUAL,givenRegularParams.il_equal)
        }
        if(givenRegularParams.containsKey("extra") && givenRegularParams.extra.containsKey("il_like")){
            filter=new Filter(EstablishmentStaticData.defaultFilterConfiguration["filterByLikeTicker"])
            filter.params=["values":givenRegularParams.extra.il_like]
            filters.add(filter)
            regularParams.extra.put(WitbookerParams.IL_LIKE,givenRegularParams.il_like)
        }

        if(!givenRepresentation)
            return
        representation.currency=givenRepresentation.containsKey("currency")?givenRepresentation.currency:null
        representation.locale=givenRepresentation.containsKey("locale")?givenRepresentation.locale:null
        representation.iframeMode=givenRepresentation.containsKey("iframeMode")?givenRepresentation.iframeMode:null
        representation.hideRestricted=givenRepresentation.containsKey("hideRestricted")?givenRepresentation.hideRestricted:null
        representation.hideLocked=givenRepresentation.containsKey("hideLocked")?givenRepresentation.hideLocked:null
        representation.ticker=givenRepresentation.containsKey("ticker")?givenRepresentation.ticker:null
        representation.maxAdults=givenRepresentation.containsKey("maxAdults")?givenRepresentation.maxAdults:null
        representation.maxChildren=givenRepresentation.containsKey("maxChildren")?givenRepresentation.maxChildren:null
        representation.rack=givenRepresentation.containsKey("rack")?givenRepresentation.rack:null
        representation.minimumRatePerDay=givenRepresentation.containsKey("minimumRatePerDay")?givenRepresentation.minimumRatePerDay:null
        representation.maxBabies=givenRepresentation.containsKey("maxBabies")?givenRepresentation.maxBabies:null
        representation.currencySymbol=givenRepresentation.containsKey("currencySymbol")?givenRepresentation.currencySymbol:null
        representation.currentState=givenRepresentation.containsKey("currentState")?givenRepresentation.currentState:null
        representation.filterGuestsByAge=givenRepresentation.containsKey("filterGuestsByAge")?givenRepresentation.filterGuestsByAge:null

        /*TODO: WHAT SHOULD BE OBTAINED FROM SESSION?*/
    }


    WitbookerParams(LegacyParams legacyParams, EstablishmentAdditionalProperties establishmentAdditionalProperties,String locale,WitbookerParams sessionWitbookerParams=null,Map sessionGlobalParams=null) {
        regularParams = new RegularParams()
        representation=new Representation()
        representation.maxBookableNights=establishmentAdditionalProperties.maxBookableNights
        filters=[]
        Filter filter=new Filter()
        /*----------------------------PARSE_START_DATE-----------------------------*/
//        (sessionWitbookerParams && sessionWitbookerParams.regularParams.startDate) && (regularParams.startDate=sessionWitbookerParams.regularParams.startDate)

        (sessionGlobalParams?.containsKey(BaseController.GLOBAL_SESSION_PARAM_START_DATE)) && (regularParams.startDate=sessionGlobalParams[BaseController.GLOBAL_SESSION_PARAM_START_DATE])

        String startDate
        (startDate=legacyParams.datein) || (startDate=legacyParams.fini)
        startDate && regularParams.setStartDate(startDate)

        if(regularParams.startDate==null){
            regularParams.startDate=DateUtil.toBeginningOfTheDay(new Date())
        }
        use(TimeCategory) {
            regularParams.startDate=DateUtil.toBeginningOfTheDay(regularParams.startDate)+1.day-1.seconds
        }

        /*--------------------------END_PARSE_START_DATE---------------------------*/

        /*----------------------------PARSE_PROMO-----------------------------*/

        (sessionWitbookerParams && sessionWitbookerParams.regularParams?.discountPromoCodes) && (regularParams.discountPromoCodes=sessionWitbookerParams.regularParams.discountPromoCodes)
        (sessionWitbookerParams && sessionWitbookerParams.regularParams?.inventoryPromoCodes) && (regularParams.inventoryPromoCodes=sessionWitbookerParams.regularParams.inventoryPromoCodes)
        regularParams.isPromoCodeActive=legacyParams.prom_clean=="1" ? false:true

        if(!regularParams.isPromoCodeActive){
            regularParams.inventoryPromoCodes=[]
            regularParams.discountPromoCodes=[]
            regularParams.isPromoCodeActive=true
        }

        List<String> promCodeAcumulator=[]
        if (legacyParams.prom)
            promCodeAcumulator.addAll(legacyParams.prom.split(","))
        if (legacyParams.cod_tarifa)
            promCodeAcumulator.addAll(legacyParams.cod_tarifa.split(","))
        if (legacyParams.promotionalcode)
            promCodeAcumulator.addAll(legacyParams.promotionalcode.split(","))
        if (legacyParams.fcod_rate)
            promCodeAcumulator.addAll(legacyParams.fcod_rate.split(","))

        regularParams.newPromoCodes=promCodeAcumulator

        if (regularParams.discountPromoCodes!=null)
            regularParams.discountPromoCodes.addAll(promCodeAcumulator)
        else
            regularParams.discountPromoCodes=promCodeAcumulator
        if (regularParams.inventoryPromoCodes!=null)
            regularParams.inventoryPromoCodes.addAll(promCodeAcumulator)
        else
            regularParams.inventoryPromoCodes=promCodeAcumulator


        (sessionWitbookerParams && sessionWitbookerParams.regularParams?.discountPromoCodes && !regularParams.isPromoCodeActive) && (sessionWitbookerParams.regularParams.discountPromoCodes=[])
        (sessionWitbookerParams && sessionWitbookerParams.regularParams?.inventoryPromoCodes && !regularParams.isPromoCodeActive) && (sessionWitbookerParams.regularParams.inventoryPromoCodes=[])

        filter=new Filter(EstablishmentStaticData.defaultFilterConfiguration["filterByPromoCode"])
        filter.params["promoCodes"]=regularParams.inventoryPromoCodes?regularParams.inventoryPromoCodes:[]
        filters.add(filter)


        /*TODO: RECOVER PROMO CODE FROM SESSION*/

        /*----------------------------END_PARSE_PROMO-----------------------------*/

        /*----------------------------PARSE_END_DATE-----------------------------*/
        String endDate
        Integer nights=null

        (sessionGlobalParams?.containsKey(BaseController.GLOBAL_SESSION_PARAM_END_DATE)) && (regularParams.endDate=sessionGlobalParams[BaseController.GLOBAL_SESSION_PARAM_END_DATE])

        (endDate=legacyParams.dateout) || (endDate=legacyParams.fout)
        if(!endDate){
            (nights=legacyParams.nights) || (nights=legacyParams.noches)
            if (nights && startDate )
                regularParams.setEndDate( regularParams.witbookerCalculateEndDate(regularParams.startDate, nights))
        }else{
            regularParams.setEndDate(endDate)
        }

        if(regularParams.endDate==null || regularParams.endDate<=regularParams.startDate ){
            regularParams.endDate=new Date(regularParams.startDate.getTime())
            DateUtil.incrementDays(regularParams.endDate,1)
        }

        if(DateUtil.daysBetweenDates(regularParams.startDate,regularParams.endDate)>representation.maxBookableNights){
            use(TimeCategory) {
                regularParams.startDate=DateUtil.toBeginningOfTheDay(regularParams.startDate)+1.day-1.seconds
            }
            regularParams.endDate=new Date(regularParams.startDate.getTime())
            DateUtil.incrementDays(regularParams.endDate,1)
        }

        filter=new Filter(EstablishmentStaticData.defaultFilterConfiguration["filterByCheckInCheckOut"])
        filter.params["startDate"]=regularParams.startDate
        filter.params["endDate"]=regularParams.endDate
        filters.add(filter)

        filter=new Filter(EstablishmentStaticData.defaultFilterConfiguration["filterByValidity"])
        filter.params["startDate"]=regularParams.startDate
        filter.params["endDate"]=regularParams.endDate
        filters.add(filter)

        /*--------------------------END_PARSE_END_DATE---------------------------*/

        /*----------------------------PARSE_OCCUPANTS-----------------------------*/
        Occupants occupants=new Occupants()
        if ((legacyParams.adultos && legacyParams.adultos=="*") || (legacyParams.adults && legacyParams.adults=="*")){
            occupants.adults=-1
            occupants.children=0
            occupants.babies=0
        }else{
            if( !legacyParams.personas && !legacyParams.adultos  && !legacyParams.adults
                    && !legacyParams.ninos && !legacyParams.children && !legacyParams.babies
                    && !legacyParams.bebes && !legacyParams.teenagers && !legacyParams.seniors
                    && sessionWitbookerParams ){
                (sessionWitbookerParams && sessionWitbookerParams.regularParams?.occupants) && (occupants=sessionWitbookerParams.regularParams?.occupants)
            }else{
                if( !legacyParams.personas  &&  !legacyParams.adultos &&  !legacyParams.adults)
                    legacyParams.personas="2"
                if( !legacyParams.children  &&  !legacyParams.ninos)
                    legacyParams.children=0
                if( !legacyParams.babies  &&  !legacyParams.babies)
                    legacyParams.babies=0
                try{
                    (legacyParams.personas && (occupants.adults=Integer.parseInt(legacyParams.personas))) ||  (legacyParams.adultos && (occupants.adults=Integer.parseInt(legacyParams.adultos)))  ||  (legacyParams.adults && (occupants.adults=Integer.parseInt(legacyParams.adults)))
                }catch (Exception e){
                    occupants.adults=2
                }
                (occupants.children=legacyParams.children) || ( occupants.children=legacyParams.ninos)
                (occupants.babies=legacyParams.babies) || ( occupants.babies=legacyParams.bebes)
                (occupants.teenagers=legacyParams.teenagers)
                (occupants.seniors=legacyParams.seniors)
            }
        }
        occupants.restriction=establishmentAdditionalProperties.occupantRestriction
        /*TODO: is it okay to hardcode the filter name? */
        /*TODO: Do not include occupation filtering when adults == '*' because no filtering is needed */
        if ( occupants.adults!=-1 ){
            filter=new Filter(EstablishmentStaticData.defaultFilterConfiguration["filterByOccupationType"])
            filter.params["occupants"]=occupants
            filters.add(filter)
        }
        occupants.guestAges=legacyParams.guestAges
        regularParams.occupants=occupants

        representation.filterGuestsByAge=establishmentAdditionalProperties.filterGuestsByAge
        representation.guestAgeFilter=establishmentAdditionalProperties.guestAgeFilter


        /*----------------------------END_PARSE_OCCUPANTS-----------------------------*/



        /*----------------------------PARSE_FILTERS-----------------------------*/
        /*Show Inventory lines that equal the given tickers il_equal=ticker;ticker  */
        /*TODO: DO NOT HARDCODE DELIMITERS*/
        regularParams.extra=[:]
        def priorityIndexForExtraFilter=0;
        List<String> tokens=null
        if (legacyParams?.il_equal)
            tokens=legacyParams.il_equal.tokenize(";")
        else
            (sessionWitbookerParams && sessionWitbookerParams.regularParams &&  sessionWitbookerParams.regularParams.extra && sessionWitbookerParams.regularParams.extra[WitbookerParams.IL_EQUAL]) && (tokens=sessionWitbookerParams.regularParams.extra[WitbookerParams.IL_EQUAL])
        if (tokens){
            filter=new Filter(EstablishmentStaticData.defaultFilterConfiguration["filterByEqualTicker"])
            filter.priority=Integer.MAX_VALUE
            priorityIndexForExtraFilter++
            filter.params=["values":tokens]
            filters.add(filter)

            regularParams.extra.put(WitbookerParams.IL_EQUAL,tokens)
        }

        tokens=null
        filter=new Filter(EstablishmentStaticData.defaultFilterConfiguration["filterByLikeTicker"])
        filter.priority=Integer.MAX_VALUE
        if(legacyParams?.il_like)
            tokens=legacyParams.il_like.tokenize(";")
        else
            (sessionWitbookerParams && sessionWitbookerParams.regularParams && sessionWitbookerParams.regularParams.extra &&  sessionWitbookerParams.regularParams.extra.hasProperty(WitbookerParams.IL_LIKE)) && (tokens=sessionWitbookerParams.regularParams.extra[WitbookerParams.IL_LIKE])
        if (tokens){
            priorityIndexForExtraFilter++
            filter.params=["values":tokens]
            filters.add(filter)
            regularParams.extra.put(WitbookerParams.IL_LIKE,tokens)
        }

        if(legacyParams.il_clean && legacyParams.il_clean=="1"){
            regularParams.extra=[:]
        }

        /*----------------------------END_PARSE_FILTERS-----------------------------*/

        /*----------------------------PARSE_CURRENCY-----------------------------*/
        (representation.currency=legacyParams.setconversion) || (representation.currency=legacyParams.currency) || ((sessionWitbookerParams && sessionWitbookerParams.representation.currency) && (representation.currency=sessionWitbookerParams.representation.currency) ) ||  (representation.currency=establishmentAdditionalProperties.currency)
        /*----------------------------END_PARSE_CURRENCY-----------------------------*/

        /*----------------------------PARSE_LOCALE---------------------------------*/
        (representation.locale=locale) || (representation.locale=legacyParams.lang)  || (representation.locale=legacyParams.language) ||  ((sessionWitbookerParams && sessionWitbookerParams.representation.locale) && (representation.locale=sessionWitbookerParams.representation.locale) ) || (representation.locale=establishmentAdditionalProperties.defaultLocale)
        /*----------------------------END_PARSE_LOCALE-----------------------------*/

        /*----------------------------PARSE_IFRAME_MODE-----------------------------*/
        if(legacyParams.witif){
            representation.iframeMode=legacyParams.witif.equals("1")
        }else if(sessionWitbookerParams?.representation?.iframeMode){
            representation.iframeMode=sessionWitbookerParams.representation.iframeMode
        }else if(establishmentAdditionalProperties.iframeMode){
            representation.iframeMode=establishmentAdditionalProperties.iframeMode
        }
        /*----------------------------END_PARSE_IFRAME_MODE-----------------------------*/

        /*----------------------------PARSE_HIDE_PROMOS-----------------------------*/
        if(legacyParams.hidePromos){
            representation.hidePromos=legacyParams.hidePromos.equals("1")
        }else if(sessionWitbookerParams?.representation?.hidePromos){
            representation.hidePromos=sessionWitbookerParams.representation.hidePromos
        }else if(establishmentAdditionalProperties.hidePromos){
            representation.hidePromos=establishmentAdditionalProperties.hidePromos
        }
        /*----------------------------END_PARSE_HIDE_PROMOS-----------------------------*/

        /*----------------------------SET HIDE_RESTRICTED-----------------------------*/
        (representation.hideRestricted=establishmentAdditionalProperties.hideRestricted)
        /*----------------------------END_SET HIDE_RESTRICTED-----------------------------*/

        /*----------------------------SET HIDE_LOCKED-----------------------------*/
        (representation.hideLocked=establishmentAdditionalProperties.hideLocked)
        /*----------------------------END_SET HIDE_LOCKED-----------------------------*/

        /*----------------------------SET TICKER-----------------------------*/
        (representation.ticker=legacyParams.ticker)
        /*----------------------------END_SET TICKER-----------------------------*/

        (representation.currencySymbol=com.witbooking.witbooker.Currency.getCurrencySymbol(representation.currency))




        /*TODO: Recover from DB additionalProperties*/
        representation.rack=establishmentAdditionalProperties.rackRate
        representation.minimumRatePerDay=establishmentAdditionalProperties.minimumRatePerDay
        representation.numberOfRoomsLeftLimit=establishmentAdditionalProperties.numberOfRoomsLeftLimit

        representation.hotelSiteUrl=establishmentAdditionalProperties.hotelSiteUrl
        representation.hotelLogoHasLink=establishmentAdditionalProperties.hotelLogoHasLink
        representation.showPromoCode=establishmentAdditionalProperties.showPromoCode
        representation.showPromoCodeInputField=establishmentAdditionalProperties.showPromoCodeInputField

        representation.collapsedAccordionsConfiguration=establishmentAdditionalProperties.collapsedAccordionsConfiguration
        representation.maxLineNumbersBeforeCollapse=establishmentAdditionalProperties.maxLineNumbersBeforeCollapse

        representation.maxBookableRooms=establishmentAdditionalProperties.maxBookableRooms
        representation.roomDenomination=establishmentAdditionalProperties.roomDenomination
        representation.step1WithoutTaxes=establishmentAdditionalProperties.step1WithoutTaxes
        representation.extrasFolded=establishmentAdditionalProperties.extrasFolded
        representation.bookingFormCcv = establishmentAdditionalProperties.bookingFormCcv
        representation.bookingFormCountry = establishmentAdditionalProperties.bookingFormCountry
        representation.bookingFormPhone = establishmentAdditionalProperties.bookingFormPhone
        representation.bookingFormDni = establishmentAdditionalProperties.bookingFormDni
        representation.bookingFormAddress = establishmentAdditionalProperties.bookingFormAddress
        representation.bookingFormRepeatEmail = establishmentAdditionalProperties.bookingFormRepeatEmail
        representation.bookingFormArrivalTime = establishmentAdditionalProperties.bookingFormArrivalTime
        representation.bookingFormNewsletter = establishmentAdditionalProperties.bookingFormNewsletter
        (representation.showDiscountMobile=legacyParams.showDiscountMobile && legacyParams.showDiscountMobile.equals("1")? true: false )  || (representation.showDiscountMobile=establishmentAdditionalProperties.showDiscountMobile)
        representation.showPromoCodeInputOnIframeMode=legacyParams.showPromoCodeInputOnIframeMode=="1"
        representation.hotels=legacyParams.hotel!=null?legacyParams.hotel.tokenize(","):null;
        (representation.channel=legacyParams.channel)   || ((sessionWitbookerParams && sessionWitbookerParams.representation.channel) && (representation.channel=sessionWitbookerParams.representation.channel) );
        (representation.view=legacyParams.view)   || ((sessionWitbookerParams && sessionWitbookerParams.representation.view) && (representation.view=sessionWitbookerParams.representation.view) );
        (representation.tracking_id=legacyParams.tracking_id)   || ((sessionWitbookerParams && sessionWitbookerParams.representation.tracking_id) && (representation.tracking_id=sessionWitbookerParams.representation.tracking_id) );
        (representation.iframeResizeDomain=legacyParams.d)   || ((sessionWitbookerParams && sessionWitbookerParams.representation.iframeResizeDomain) && (representation.iframeResizeDomain=sessionWitbookerParams.representation.iframeResizeDomain) );
        (representation.witaffiliate=legacyParams.witaffiliate)   || ((sessionWitbookerParams && sessionWitbookerParams.representation.witaffiliate) && (representation.witaffiliate=sessionWitbookerParams.representation.witaffiliate) );
        (representation.affiliate=legacyParams.affiliate)   || ((sessionWitbookerParams && sessionWitbookerParams.representation.affiliate) && (representation.affiliate=sessionWitbookerParams.representation.affiliate) );

        representation.allowsDisablingOccupationFilter=establishmentAdditionalProperties.allowsDisablingOccupationFilter?establishmentAdditionalProperties.allowsDisablingOccupationFilter:null
        representation.bookingEngineDomain=establishmentAdditionalProperties.bookingEngineDomain?establishmentAdditionalProperties.bookingEngineDomain:null
        representation.transferMinNotice=establishmentAdditionalProperties.transferMinNotice!=null?establishmentAdditionalProperties.transferMinNotice:null
        representation.transferAvailabilityHold=establishmentAdditionalProperties.transferAvailabilityHold!=null?establishmentAdditionalProperties.transferAvailabilityHold:null
        representation.cancellationRelease=establishmentAdditionalProperties.cancellationRelease!=null?establishmentAdditionalProperties.cancellationRelease:null

        representation.occupancyWithJunior=establishmentAdditionalProperties.occupancyWithJunior?establishmentAdditionalProperties.occupancyWithJunior:null
        representation.defaultCurrency=establishmentAdditionalProperties.currency
        representation.releaseVersion="7.0"


        representation.adultMaxAge=establishmentAdditionalProperties.adultMaxAge
        representation.teenagerMaxAge=establishmentAdditionalProperties.teenagerMaxAge
        representation.childrenMaxAge=establishmentAdditionalProperties.childrenMaxAge
        representation.childrenMinAge=establishmentAdditionalProperties.childrenMinAge
        representation.babyMinAge=establishmentAdditionalProperties.babyMinAge

        representation.maxSeniors=establishmentAdditionalProperties.maxSeniors
        representation.maxAdults=establishmentAdditionalProperties.maxAdults
        representation.maxTeenagers=establishmentAdditionalProperties.maxTeenagers
        representation.maxChildren=establishmentAdditionalProperties.maxChildren
        representation.maxBabies=establishmentAdditionalProperties.maxBabies


    }


    protected static List<OccupantAgeRange> getOccupantRanges(OccupantRestriction occupantRestriction,
                                                              Integer childrenMinAge,
                                                              Integer childrenMaxAge,
                                                              Integer teenagerMaxAge,
                                                              Integer adultMaxAge,
                                                              Integer babyMinAge){

        List<OccupantAgeRange> ageRanges=new ArrayList<>()

        if(occupantRestriction.equals(occupantRestriction.NONE) || occupantRestriction.equals(occupantRestriction.TAKE_ALL_PEOPLE) ){
            ageRanges.add(new OccupantAgeRange(9999,0,Guest.getAdult().name))
        }else{
            ageRanges.add(new OccupantAgeRange(childrenMaxAge,childrenMinAge,Guest.getChild().name))
        }

        if(occupantRestriction.equals(occupantRestriction.TAKE_ADULT_CHILD) || occupantRestriction.equals(occupantRestriction.TAKE_ADULT_CHILD_BABY)){
            ageRanges.add(new OccupantAgeRange(9999,childrenMaxAge+1,Guest.getAdult().name))
        }

        if(occupantRestriction.equals(occupantRestriction.TAKE_ADULT_TEENAGER_CHILD) || occupantRestriction.equals(occupantRestriction.TAKE_ADULT_TEENAGER_CHILD_BABY) ){
            ageRanges.add(new OccupantAgeRange(9999,teenagerMaxAge+1,Guest.getAdult().name))
            ageRanges.add(new OccupantAgeRange(teenagerMaxAge,childrenMaxAge+1,Guest.getTeenager().name))
        }

        if(occupantRestriction.equals(occupantRestriction.TAKE_SENIOR_ADULT_TEENAGER_CHILD_BABY)  ){
            ageRanges.add(new OccupantAgeRange(9999,teenagerMaxAge+1,Guest.getSenior().name))
            ageRanges.add(new OccupantAgeRange(adultMaxAge,teenagerMaxAge+1,Guest.getAdult().name))
            ageRanges.add(new OccupantAgeRange(teenagerMaxAge,childrenMaxAge+1,Guest.getTeenager().name))
        }

        if(occupantRestriction.equals(occupantRestriction.TAKE_ADULT_CHILD_BABY)
                || occupantRestriction.equals(occupantRestriction.TAKE_ADULT_TEENAGER_CHILD_BABY)
                || occupantRestriction.equals(occupantRestriction.TAKE_SENIOR_ADULT_TEENAGER_CHILD_BABY)
        ){
            ageRanges.add(new OccupantAgeRange(childrenMinAge-1,babyMinAge,Guest.getInfant().name))
        }

        return ageRanges.sort();

    }
    public static Occupants preProcessOccupancy(WitbookerParams witbookerParams,
                                                OccupantRestriction occupantRestriction,
                                                Integer childrenMinAge,
                                                Integer childrenMaxAge,
                                                Integer teenagerMaxAge,
                                                Integer adultMaxAge,
                                                Integer babyMinAge){

        Occupants occupantsData=new Occupants()

        occupantsData.adults=witbookerParams.regularParams.occupants.adults
        occupantsData.babies=witbookerParams.regularParams.occupants.babies
        occupantsData.children=witbookerParams.regularParams.occupants.children
        occupantsData.teenagers=witbookerParams.regularParams.occupants.teenagers
        occupantsData.guestAges=witbookerParams.regularParams.occupants.guestAges
        occupantsData.seniors=witbookerParams.regularParams.occupants.seniors
        occupantsData.restriction=witbookerParams.regularParams.occupants.restriction

        if(occupantsData.guestAges.isEmpty()){
            return  occupantsData
        }
        occupantsData.babies=0
        occupantsData.children=0
        occupantsData.teenagers=0
        occupantsData.seniors=0

        List<OccupantAgeRange> ageRanges=getOccupantRanges(occupantRestriction,childrenMinAge,childrenMaxAge,teenagerMaxAge,adultMaxAge,babyMinAge)

        if (ageRanges==null){
            return  occupantsData
        }
        /*TODO: USE A TREESET!*/
        Occupants occupants=new Occupants()
        /* This permits passing adults as a params as well as children ages independently */
        occupants.adults=occupantsData.adults
        occupants.teenagers=occupantsData.teenagers
        occupants.seniors=occupantsData.seniors
        occupants.children=occupantsData.children
        occupants.babies=occupantsData.babies
        occupants.restriction=occupantsData.restriction
        occupants.guestAges=occupantsData.guestAges
        occupants.occupantExtraFilter=[]
        parentLoop:for(Integer age : occupantsData.guestAges){
            for(OccupantAgeRange occupantAgeRange : ageRanges){
                if (age<=occupantAgeRange.upperBound && age>=occupantAgeRange.lowerBound){
                    if (occupantAgeRange.occupantType==Guest.getAdult().name){
                        occupants.adults+=1
                    }else if(occupantAgeRange.occupantType==Guest.getTeenager().name){
                        occupants.teenagers+=1
                    }else if(occupantAgeRange.occupantType==Guest.getChild().name){
                        occupants.children+=1
                    }else if(occupantAgeRange.occupantType==Guest.getInfant().name){
                        occupants.babies+=1
                    }else if(occupantAgeRange.occupantType==Guest.getSenior().name){
                        occupants.seniors+=1
                    }
                    continue parentLoop;
                }
            }
        }


        return occupants

    }


    /*Representation of the data restriction to occupants, note that by being here, restrictions can be given as parameters*/

    class RegularParams{
        Date startDate

        Date endDate
        Occupants occupants

        List<String> inventoryPromoCodes
        List<String> discountPromoCodes
        boolean isPromoCodeActive=false

        Map<String,Object> extra
        List<String> newPromoCodes=[]
        List<Integer> guestAges=[]

        def witbookerParseDate(String dateInString){

            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            if(dateInString.indexOf("-")>2){
                formatter = new SimpleDateFormat("yyyy-MM-dd");
            }
            Date date;
            try {
                date= formatter.parse(dateInString);
                def today=DateUtil.toBeginningOfTheDay(new Date())
                if (date<(today)){
                    date=today
                }
            } catch (ParseException e) {
                //e.printStackTrace();
            }
            return date
        }
        static parseDate(String dateInString){
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            if(dateInString.indexOf("-")>2){
                formatter = new SimpleDateFormat("yyyy-MM-dd");
            }
            Date date;
            try {
                date= formatter.parse(dateInString);

            } catch (ParseException e) {
                //e.printStackTrace();
            }
            return date
        }

        def witbookerCalculateEndDate(Date startDate, int nights){
            Calendar c = Calendar.getInstance();
            c.setTime(startDate);
            c.add(Calendar.DATE, nights);
            return c.getTime()
        }

        void setStartDate(Date startDate) {
            this.startDate = startDate
        }

        void setStartDate(String startDate) {
            this.startDate = witbookerParseDate(startDate)
        }

        void setEndDate(Date endDate) {
            this.endDate = endDate
        }

        void setEndDate(String endDate) {
            this.endDate = witbookerParseDate(endDate)
        }

        void setOccupants(Occupants occupants) {
            this.occupants = occupants
        }

        void setExtra(Map<String, Object> extra) {
            this.extra = extra
        }
    }



    class Representation {
        String ticker
        String currency
        String locale
        Boolean iframeMode=null
        String media
        Map<String,Object> extra
        String currencySymbol
        Integer maxSeniors
        Integer maxAdults
        Integer maxTeenagers
        Integer maxChildren
        Integer maxBabies
        Boolean hideRestricted
        Boolean hideLocked
        Boolean rack
        Language language
        Set<String> activeDiscounts=[]
        Set<String> activePromoCodes=[]
        /*e4 :{type:Service,code:"promoCode"} */
        transient Map<String,Map<String,String>> promoCodeActiveDataValueHolders=[:]
        Map<String,Set<String>> activeExtraFilters=[:]

        Integer adultMaxAge
        Integer adultMinAge
        Integer teenagerMaxAge
        Integer childrenMaxAge
        Integer childrenMinAge
        Integer babyMinAge




        Integer numberOfRoomsLeftLimit
        String hotelSiteUrl
        boolean hotelLogoHasLink
        boolean showPromoCode
        boolean showDiscountMobile

        String  collapsedAccordionsConfiguration
        Integer maxLineNumbersBeforeCollapse
        Integer maxBookableNights
        Integer maxBookableRooms
        String roomDenomination
        Double step1WithoutTaxes
        boolean extrasFolded
        boolean bookingFormCcv
        boolean bookingFormCountry
        boolean bookingFormPhone
        boolean bookingFormDni
        boolean bookingFormAddress
        boolean bookingFormRepeatEmail
        boolean bookingFormNewsletter
        boolean bookingFormArrivalTime
        Double minimumRatePerDay
        List<String> hotels
        boolean showPromoCodeInputOnIframeMode

        String channel
        String view
        String tracking_id
        String iframeResizeDomain

        String witaffiliate
        String affiliate
        Boolean allowsDisablingOccupationFilter
        String bookingEngineDomain
        transient String defaultLanguage
        Boolean occupancyWithJunior

        Double transferMinNotice
        Double transferAvailabilityHold
        transient Integer cancellationRelease
        List<Map<String,String>> countries

        String releaseVersion
        String defaultCurrency
        Map conversionRate
        String currentState
        boolean showPromoCodeInputField=false;
        boolean hidePromos=false;

        transient String userCountry="undefined"
        transient Map<String,Object> decrementalPromoCodes
        transient String referer=null

        boolean filterGuestsByAge=false
        transient String guestAgeFilter=""
    }

    RegularParams regularParams
    transient List<Filter> filters
    Representation representation


}
