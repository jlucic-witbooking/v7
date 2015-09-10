package com.witbooking.witbooker.filters
import com.witbooking.middleware.model.Inventory
import com.witbooking.middleware.model.values.DailyValue
import com.witbooking.middleware.model.values.HashRangeValue
import com.witbooking.middleware.model.values.RangeValue
import com.witbooking.middleware.model.values.types.ConstantValue
import com.witbooking.middleware.utils.DateUtil
import com.witbooking.witbooker.ErrorMessage
import grails.util.Environment
import org.apache.log4j.Logger
import witbookerv7.util.Occupants

/**
 * Created by mongoose on 3/28/14.
 */
class InventoryFilter extends Filter  {
    private static final Logger logger = Logger.getLogger(InventoryFilter.class);

    static final String MAX_NOTICE= HashRangeValue.MAX_NOTICE
    static final String MIN_NOTICE= HashRangeValue.MIN_NOTICE
    static final String MAX_STAY= HashRangeValue.MAX_STAY
    static final String MIN_STAY= HashRangeValue.MIN_STAY
    static String LOCK= "closed"
    static final String AVAILABILITY= HashRangeValue.ACTUAL_AVAILABILITY
    static final String VISIBLE= "visible"
    static final String OCCUPATION_TYPE= "occupationType"
    static final String CHECK_IN_CHECK_OUT= "checkInCheckOut"
    static final String VALIDITY= "validity"
    static final String PROMO_CODE= "promoCode"
    static final String EQUAL_TICKER= "equalTicker"
    static final String LIKE_TICKER= "likeTicker"
    static List<String> FILTER_ADDS_ERROR_MESSAGE=[MAX_NOTICE,MIN_NOTICE,MAX_STAY,MIN_STAY,LOCK,AVAILABILITY,CHECK_IN_CHECK_OUT,VISIBLE]
    static List<String> FILTER_ADDS_ERROR_MESSAGE_DEBUG=[MAX_NOTICE,MIN_NOTICE,MAX_STAY,MIN_STAY,LOCK,AVAILABILITY,VISIBLE,OCCUPATION_TYPE,CHECK_IN_CHECK_OUT,VALIDITY,VISIBLE,PROMO_CODE,EQUAL_TICKER,LIKE_TICKER]

    public static void addErrorMessage(String ticker, String filterName, Map<String,Map<String,ErrorMessage>> errorMessages =[:],Object values=null,boolean forceAdd=false) {
        if (Environment.current == Environment.DEVELOPMENT) {
            FILTER_ADDS_ERROR_MESSAGE=FILTER_ADDS_ERROR_MESSAGE_DEBUG;
        }
        if (!FILTER_ADDS_ERROR_MESSAGE.contains(filterName) && !forceAdd)
            return
        if (!errorMessages.containsKey(ticker))
            errorMessages[ticker]=[:]
        if (!errorMessages[ticker].containsKey(filterName))
            errorMessages[ticker][filterName]=[:]
        if (errorMessages[ticker][filterName].getClass()==ErrorMessage){
            ErrorMessage existingErrorMessage=errorMessages[ticker][filterName]
            if (Collection.isAssignableFrom(existingErrorMessage.value.values.getClass()))
                existingErrorMessage.value.values+=values.values
            else
                existingErrorMessage.value.values=[existingErrorMessage.value,values]
        }else{
            errorMessages[ticker][filterName]=new ErrorMessage("Failed at "+filterName,filterName, values)
        }
    }

    public static boolean filterByVisible(Inventory inventory, Map params=null,Map<String,Map<String,ErrorMessage>> errorMessages =[:] ){
        boolean isValid=inventory.visible;
        //isValid=false
        if (!isValid)
            addErrorMessage(inventory.ticker,VISIBLE, errorMessages)
        return inventory.visible;
    }

    public static boolean filterByOccupationType( Inventory inventory, Map params=null,Map<String,Map<String,ErrorMessage>> errorMessages =[:] ){
        boolean isValid=false
        /*TODO: OBLIGATORY PARAM*/
        Occupants occupants=params["occupants"]
        int occupationType=occupants.restriction.value()
        if (occupationType==0)
            isValid=true
        else if(occupationType==1){
            int totalGuests=0
            inventory.configuration.guests.each { guestType,quantity-> totalGuests+=quantity }
            int totalGivenGuests=occupants.adults+occupants.children+occupants.babies
            isValid = totalGuests==totalGivenGuests
        }else if(occupationType>=2 ){
            def guestType=inventory.configuration
            isValid=occupants.adults==guestType.adults
            isValid&=occupants.children==guestType.children

            if(occupationType==3){
                isValid&=occupants.babies==guestType.infants
            }

            if(occupationType==4){
                isValid&=occupants.teenagers==guestType.teenagers
            }

            if(occupationType==5){
                isValid&=occupants.teenagers==guestType.teenagers
                isValid&=occupants.babies==guestType.infants
            }

            if(occupationType==6 || occupationType==7){
                isValid&=occupants.teenagers==guestType.teenagers
                isValid&=occupants.babies==guestType.infants
                isValid&=occupants.seniors==guestType.seniors
            }
        }

        if (!isValid)
            addErrorMessage(inventory.ticker,OCCUPATION_TYPE, errorMessages)
        return isValid
    }

    public static boolean filterByCheckInCheckOut(Inventory inventory, Map params=null,Map<String,Map<String,ErrorMessage>> errorMessages =[:] ){
        Date startDate=params["startDate"]
        Date endDate=params["endDate"]
        def cal = Calendar.getInstance();
        cal.setTime(startDate)
        def startDateWeekDay=cal.get(Calendar.DAY_OF_WEEK)-1
        cal.setTime(endDate)
        def endDateWeekDay=cal.get(Calendar.DAY_OF_WEEK)-1
        boolean isValid=inventory.checkInDays.days[startDateWeekDay] && inventory.checkOutDays.days[endDateWeekDay]
        if (!isValid)
            addErrorMessage(inventory.ticker,CHECK_IN_CHECK_OUT, errorMessages,["checkInDays":inventory.checkInDays.days,"checkOutDays":inventory.checkOutDays.days])
        return isValid
    }

    public static boolean filterByValidity(Inventory inventory, Map params=null,Map<String,Map<String,ErrorMessage>> errorMessages =[:] ){
        Date startDate=params["startDate"]
        Date endDate=params["endDate"]
        Date startValidity=inventory.dateStartValidation? inventory.dateStartValidation:null
        Date endValidity=inventory.dateEndValidation? inventory.dateEndValidation:null
        startValidity=startValidity==null ? new Date(Long.MIN_VALUE) :startValidity
        endValidity=endValidity==null ? new Date(Long.MAX_VALUE) :endValidity
        boolean isValid=startDate>=startValidity && endDate<=endValidity
        if (!isValid)
            addErrorMessage(inventory.ticker,VALIDITY, errorMessages)
        return isValid
    }
    /*TODO: Use constants for filter names and params*/
    public static Map filterByPromoCode( Inventory inventory, Map params=null,Map<String,Map<String,ErrorMessage>> errorMessages =[:] ){
        /*TODO: Must iterate multiple inventory promocodes */
        List<String> promoCodes=params["promoCodes"]
        promoCodes=promoCodes.collect(){it.toLowerCase()}
        boolean isValid=false
        List<String> accessCodes
        def intersection=[]
        if(inventory.accessCode){
            accessCodes=inventory.accessCode.split(",").collect(){it.toLowerCase()}
            intersection=accessCodes.intersect(promoCodes)
            if (intersection.size()>0){ isValid=true}
        }else{
            isValid=true
        }
        if (!isValid)
            addErrorMessage(inventory.ticker,PROMO_CODE, errorMessages)
        return [isValid:isValid,validCodes:intersection]
    }

    public static boolean filterByEqualTicker( Inventory inventory,Map params=null,Map<String,Map<String,ErrorMessage>> errorMessages =[:] ){
        List<String> values=params["values"]
        boolean isValid=values.contains(inventory.ticker)
        if (!isValid)
            addErrorMessage(inventory.ticker,EQUAL_TICKER, errorMessages)
        return isValid
    }

    public static boolean filterByLikeTicker( Inventory inventory,Map params=null,Map<String,Map<String,ErrorMessage>> errorMessages =[:] ){
        List<String> values=params["values"]
        boolean isValid=false
        List<String> wrongValues
        values.each {
            if (inventory.ticker.toLowerCase().indexOf(it.toLowerCase())!=-1){
                isValid=true
            }
        }
        if(!isValid)
            addErrorMessage(inventory.ticker,LIKE_TICKER, errorMessages)
        return isValid
    }

    public static boolean filterByAgeTicker( Inventory inventory,Map params=null,Map<String,Map<String,ErrorMessage>> errorMessages =[:] ){
       return filterByLikeTicker( inventory, params , errorMessages )
    }


    public static boolean filterByMinNotice( Inventory inventory,Map params=null, Map<String,Map<String,ErrorMessage>> errorMessages =[:],boolean maxNotice =false , Map noticeReturn=null){
        /*THESE ARE HOURS */
        boolean isValid=true
        Integer notice=maxNotice? Integer.MAX_VALUE:Integer.MIN_VALUE
        if (!params || !params.containsKey("value"))
            isValid=false
        if (params.get("value")==null )
            return true
        if(params.get("value").getClass()==ConstantValue){
            notice= (params.get("value") as ConstantValue<Boolean>).value
        }else if (params.get("value").getClass()==RangeValue) {
            RangeValue rangeValue = params.get("value") as RangeValue
            if(rangeValue.dailySet.isEmpty()){
                logger.error("Invalid OWN Value for MIN/MAX Notice for ticker "+inventory.ticker)
                return true
            }else{
                notice=(rangeValue.dailySet as TreeSet<DailyValue<Integer>>).first().value
            }

        }else{
            isValid= false
        }

        if (!isValid){
            addErrorMessage(inventory.ticker,MIN_NOTICE, errorMessages)
            return isValid
        }
        Date startDate=params.get("startDate") as Date
        long diff = startDate.getTime() - new Date().getTime();
        if (diff>0){
            long diffHours = (diff / (60 * 60 * 1000)) as Long;
            isValid=maxNotice?notice >= diffHours:notice <= diffHours
        }else{
            isValid=false
        }
        noticeReturn && (noticeReturn.notice=notice)
        if (!isValid){
            addErrorMessage(inventory.ticker,MIN_NOTICE, errorMessages, notice)
        }
        return isValid
    }
    public static boolean filterByMaxNotice( Inventory inventory,Map params=null,Map<String,Map<String,ErrorMessage>> errorMessages =[:] ){
        Map noticeReturn=["notice":0]

        boolean isValid=filterByMinNotice(inventory,params,[:],true, noticeReturn)
        if (!isValid)
            addErrorMessage(inventory.ticker,MAX_NOTICE, errorMessages,noticeReturn["notice"])
        return isValid
    }

    public static boolean filterByMinStay( Inventory inventory,Map params=null,Map<String,Map<String,ErrorMessage>> errorMessages =[:], boolean maxStay =false,Map stayReturn=null ){
        boolean isValid=true
        Integer stay=maxStay? Integer.MAX_VALUE:Integer.MIN_VALUE

/*
        stayReturn.stay=30000
        addErrorMessage(inventory.ticker,MIN_STAY, errorMessages,30000)
        return false
*/

        if (!params || !params.containsKey("value"))
            isValid=  false
        if (params.get("value")==null )
            return true
        if(params.get("value").getClass()==ConstantValue){
            stay= (params.get("value") as ConstantValue<Boolean>).value
        }else if (params.get("value").getClass()==RangeValue) {
            RangeValue rangeValue = params.get("value") as RangeValue
            if(rangeValue.dailySet.isEmpty()){
                logger.error("Invalid OWN Value for MIN/MAX STAY for ticker "+inventory.ticker)
                return true
            }else{
                stay=maxStay?rangeValue.valuesForEachDay.min():rangeValue.valuesForEachDay.max()
            }
        }else{
            isValid=  false
        }
        if (!isValid && !maxStay){
            addErrorMessage(inventory.ticker,MIN_STAY, errorMessages)
            return isValid
        }
        Date startDate=params.get("startDate") as Date
        Date endDate=params.get("endDate") as Date
        long diffDays = DateUtil.daysBetweenDates( DateUtil.toBeginningOfTheDay(startDate),endDate)  //Supporting changes in DST
        isValid=maxStay? diffDays<= stay:diffDays>= stay
        if (!isValid && !maxStay)
            addErrorMessage(inventory.ticker,MIN_STAY, errorMessages,stay)
        stayReturn && (stayReturn.stay=stay)
        return isValid
    }

    public static boolean filterByMaxStay( Inventory inventory,Map params=null,Map<String,Map<String,ErrorMessage>> errorMessages =[:] ){

        Map stayReturn=["stay":0]
        boolean isValid=filterByMinStay(inventory,params,[:],true,stayReturn)
        if (!isValid)
            addErrorMessage(inventory.ticker,MAX_STAY, errorMessages,stayReturn["stay"])
        return isValid
    }

    public static boolean filterByClosed(Inventory inventory,Map params=null,Map<String,Map<String,
    ErrorMessage>> errorMessages =[:] ){
        boolean isValid=true
        if (!params || !params.containsKey("value"))
            isValid= false
        if (params.get("value")==null )
            return true
        if(params.get("value").getClass()==ConstantValue){
            isValid= (params.get("value") as ConstantValue<Boolean>).value
        }else if (params.get("value").getClass()==RangeValue) {
            isValid= !(params.get("value") as RangeValue).hasValueEqualsTo(true)
        }else{
            isValid= false
        }
        if (!isValid)
            addErrorMessage(inventory.ticker,LOCK, errorMessages)
        return isValid
    }

    public static boolean filterByAvailability(Inventory inventory,Map params=null,Map<String,Map<String,ErrorMessage>> errorMessages =[:] ){
        boolean isValid=true
        if (!params || !params.containsKey("value"))
            isValid= false
        if (params.get("value")==null )
            return true
        if(params.get("value").getClass()==ConstantValue){
            isValid= (params.get("value") as ConstantValue<Boolean>).value > 0
        }else if (params.get("value").getClass()==RangeValue) {
            isValid= !(params.get("value") as RangeValue).hasValueEqualsTo(0)
        }else{
            isValid=  false
        }
        if (!isValid)
            addErrorMessage(inventory.ticker,AVAILABILITY, errorMessages)
        return isValid

    }
}

