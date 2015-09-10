package witbookerv7

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.witbooking.middleware.model.*
import com.witbooking.middleware.model.Reservation.ReservationStatus
import com.witbooking.middleware.model.values.DailyValue
import com.witbooking.middleware.model.values.RangeValue
import com.witbooking.middleware.utils.DateUtil
import com.witbooking.witbooker.*
import grails.converters.JSON
import org.apache.log4j.Logger
import witbookerv7.util.WitbookerParams
import withotel.CacheService

/**
 *
 * @author Jorge Lucic
 */

class StepTwoController extends BaseController {

    private static final Logger logger = Logger.getLogger(StepTwoController.class);
    static allowedMethods = [
            obtainReservationInformation: ['POST', 'GET'],
            processSipayResponse: ['POST', 'GET'],
            reservation: ['POST', 'GET'],
            updateReservation: ['POST', 'GET'],
    ]

    def processSipayResponse() {
        println("sipay response")
        render "Hola"

  //      if ok send ok, and js PostMessage to parent to submit like usual.
    }

    def obtainReservationInformation() {
        String hotelTicker=params.hotelTicker
        def sessionData=session[hotelTicker]
        if (!sessionData || !sessionData.witbookerParams || !sessionData.witbookerParams.representation || !sessionData.witbookerParams.representation.language){
            return render([ error:[message: "SessionExpired" ,code:"EXPSESS" ] ] as JSON)
        }
        WitbookerParams witbookerParams=sessionData.witbookerParams
        witbookerParams.representation.currentState="StepTwo"

        Establishment establishment= cacheService.getEstablishmentByLanguage(hotelTicker+CacheService.TICKER_LANGUAGE_SEPARATOR+witbookerParams.representation.language.locale)
        Map updatedCartInfo=updateCart(
                establishment,
                witbookerParams.representation.language,
                witbookerParams.representation.currency,
                witbookerParams.regularParams?.inventoryPromoCodes,
                witbookerParams,
                params.containsKey("includeServices")
        )

        Gson gson = new GsonBuilder().setDateFormat("dd-MM-yyyy").create();

        Map<String,Object> stepTwoResponse = [ reservations: session[hotelTicker].reservations , services :updatedCartInfo["servicesAvailable"] , sipayIframe: null ]

        def cartData=gson.toJson( stepTwoResponse );

        if((updatedCartInfo["markedForRemoval"]  as List<Map<String,String>>).size()>0){
            (updatedCartInfo["markedForRemoval"] as List< Map<String,String> >).each {
                reservations= session[it.hotelTicker].reservations as List<Reservation>
                for (Reservation reservation : reservations){
                    if (reservation.id==it.id){
                        logger.error("ERROR IN STEP2 : Removed Reservation Because of error")
                        logger.error("Reservation: "+gson.toJson(reservation))
                    }
                }
                removeReservation(reservations,it.id)
            }
        }

        render(text: cartData, contentType: "application/json")

    }

    protected Service findService(String hotelTicker, String serviceTicker){
        Establishment establishment= cacheService.getEstablishment(hotelTicker,null).all
        for ( Service service in (establishment.visualRepresentation as HotelVisualRepresentation).services ) {
            if(serviceTicker==service.ticker)
                return service
        }
        return null
    }

    protected cleanJsonRequestData(def json){
        try{
            json?.paymentData=null
            return json
        }catch (Exception ex){
            return null
        }
    }

    def reservation(){

        def customerAndPaymentData=request.JSON


        def hotelTicker=request.JSON.establishment.ticker

        if(!customerAndPaymentData){
            logger.error("Error Storing Reservation"+ hotelTicker)
            return render("No data given")
        }


        def sessionData=session[hotelTicker]
        if (!sessionData || !sessionData.witbookerParams){
            logger.error("Error Storing Reservation"+ hotelTicker)
            return render([ error:[message: "SessionExpired" ,code:"EXPSESS" ] ] as JSON)
        }

        if(sessionData && sessionData.reservationOcurring){
            println("NO MULTIPLE RESERVATIONS")
            logger.error("Error Storing Reservation"+ hotelTicker)
            return render([ error:[message: "ReservationOcurring" ,code:"RESOCU" ] ] as JSON)
        }
        sessionData.reservationOcurring=true

        WitbookerParams witbookerParams
        try{
            witbookerParams=session[hotelTicker].witbookerParams
        }catch (Exception e){
            sessionData.reservationOcurring=false
            logger.error("Error Storing Reservation")
            logger.error("Session expired while booking STEP2 "+ hotelTicker)
            return render([ error:[message: "SessionExpired" ,code:"EXPSESS" ] ] as JSON)
        }

        def customerData=customerAndPaymentData.customerData
        def paymentData=customerAndPaymentData.paymentData

        CreditCard creditCard=new CreditCard();
        try{
            //ccv
            creditCard.cardHolderName = paymentData.creditCardData.ccholder;
            creditCard.cardNumber = paymentData.creditCardData.ccnumber;
            creditCard.expireDateEncrypted=paymentData.creditCardData.ccvalidtomonth+paymentData.creditCardData.ccvalidtoyear;
            creditCard.cardNumberEncrypted=paymentData.creditCardData.ccnumber;
            creditCard.seriesCodeEncrypted=000;
            creditCard.expireDate=Integer.parseInt(paymentData.creditCardData.ccvalidtomonth+paymentData.creditCardData.ccvalidtoyear);
            creditCard.seriesCode=paymentData.creditCardData.ccsecuritycode;
            creditCard.cardCode = paymentData.creditCardData.cckind.id;

        }catch (Exception e){
            if(paymentData.paymentType==Condition.PAYMENT_TYPE_CREDITCARD){
                sessionData.reservationOcurring=false
                logger.error("Error Processing Payment "+ hotelTicker)
                logger.error("Error Storing Reservation: Bad Credit Card"+ cleanJsonRequestData(request.JSON))
                return render("Error Processing Card Data")
            }
        }

        if(paymentData.paymentType!=Condition.PAYMENT_TYPE_CREDITCARD || !creditCard){
            creditCard.cardHolderName = "-";
            creditCard.cardNumber = "4111111111111111";
            creditCard.expireDate = 1225;
            creditCard.cardCode = "Visa";
        }


        Customer customer = new Customer()
        try{
            //pais telefono dni direccion repetir email recibirOfertas
            customer.givenName=customerData.name
            customer.personalId = customerData.passport;
            customer.surName=customerData.lastName
            customer.email = customerData.email;
            customer.telephone = customerData.phone;
            customer.creditCard = creditCard;
            customer.mailOption = customerData.receiveOffers!=null?customerData.receiveOffers:false;
            customer.country = customerData.country?.id;
            customer.address=customerData.address
            customer.ipOrder = request.getRemoteAddr();
            //customer.tokenPayments = "token'";
        }catch (Exception e){
            sessionData.reservationOcurring=false
            logger.error("Error Processing Customer Data "+ hotelTicker)
            logger.error("Error Storing Reservation: Bad  Customer Data"+ cleanJsonRequestData(request.JSON))
            return render("Error Processing Customer Data")
        }

        Double reservationTotalAmount=0.0;
        List<RoomStay> roomStays=[]
        List<Reservation> reservations= session[hotelTicker].reservations as List<Reservation>

        if(reservations==null || reservations.isEmpty()){
            logger.error(" EMPTY RESERVATIONS ")
            logger.error("Error Storing Reservation: EMPTY RESERVATIONS "+ hotelTicker)
            logger.error("Error Storing Reservation: EMPTY RESERVATIONS"+ cleanJsonRequestData(request.JSON))
            return render([ error:[message: "SessionExpired" ,code:"EXPSESS" ] ] as JSON)
        }

        try{
            reservations.each {
                reservation->
                    RoomStay roomStay=new RoomStay()
                    Map<String,com.witbooking.witbooker.Service> servicesAvailable= reservation.servicesAvailable

                    roomStay.inventoryId = Integer.parseInt((reservation.inventoryLine as InventoryLine).id);
                    roomStay.dateCheckIn = reservation.startDate;
                    roomStay.dateCheckOut = reservation.endDate;
                    if(roomStay.additionalRequests==null){
                        roomStay.additionalRequests=[:]
                    }
                    if(customerData?.arrivalTime!=null){
                        roomStay.additionalRequests.put("arrivalTime",customerData.arrivalTime)
                    }

                    Double roomStayTotalServiceAmount=0.0;

                    List<ServiceRequested> servicesRequested=[]
                    reservation.selectedServices.each {
                        serviceTicker,Map selectedServiceData->
                            if (!selectedServiceData.selected){return}
                            ServiceRequested serviceRequested =new ServiceRequested()
                            Service service=selectedServiceData.service
                            serviceRequested.serviceId=service.id
                            serviceRequested.serviceTicker=service.ticker
                            serviceRequested.serviceName=service.name
                            serviceRequested.daily=service.daily
                            serviceRequested.type=service.type
                            serviceRequested.quantity=(selectedServiceData.availability==-1 && selectedServiceData.selected)?1:selectedServiceData.availability
                            com.witbooking.witbooker.Service availableService=servicesAvailable[service.ticker]
                            Integer numberOfDays = DateUtil.daysBetweenDates(DateUtil.toBeginningOfTheDay(reservation.startDate), DateUtil.toBeginningOfTheDay(reservation.endDate))

                            Double inverseDailyMultiplier=availableService.daily?1:1/numberOfDays;
                            Double dailyMultiplier=availableService.daily?numberOfDays:1;
                            DailyValue dailyValue=new DailyValue(reservation.startDate,reservation.endDate,null)
                            int rooms=reservation.quantity
                            int capacity=(reservation.inventoryLine as InventoryLine).capacity

                            if (service.type.intValue() == 0){ //ROOM
                                dailyValue.value=availableService.rate*reservation.quantity*inverseDailyMultiplier
                                serviceRequested.totalServiceAmount=availableService.rate*serviceRequested.quantity*dailyMultiplier*rooms
                                serviceRequested.quantity=rooms*dailyMultiplier
                            }else if(service.type.intValue() == 1){//PERSON
                                dailyValue.value=availableService.rate*(reservation.inventoryLine as InventoryLine).capacity*inverseDailyMultiplier
                                serviceRequested.totalServiceAmount=availableService.rate*serviceRequested.quantity*dailyMultiplier*rooms*capacity
                                serviceRequested.quantity=rooms*capacity*dailyMultiplier
                            }else if(service.type.intValue() == 2){//UNIT
                                dailyValue.value=availableService.rate*reservation.quantity*inverseDailyMultiplier
                                serviceRequested.totalServiceAmount=availableService.rate*serviceRequested.quantity*dailyMultiplier
                            }

                            roomStayTotalServiceAmount+=serviceRequested.totalServiceAmount
                            servicesRequested.add(serviceRequested)
                    }

                    roomStay.roomRates = reservation.roomRates.clone()
                    roomStay.totalAmount=(roomStay.roomRates.sumValues*reservation.quantity)+roomStayTotalServiceAmount

                    reservationTotalAmount+=roomStay.totalAmount;

                    if (roomStay.totalAmount<=0){
                        logger.error("Error Storing Reservation: TOTAL AMOUNT 0")
                        return render("Error Invalid Total Reservation Amount : "+roomStay.totalAmount)
                    }

                    /*Calculate guarantee amount */

                    /*Get adequate condition paymentType */
                    Object chosenPaymentType=null
                    for ( paymentType in (reservation.inventoryLine as InventoryLine).condition.paymentTypes ){
                        if(paymentType.ticker==paymentData.paymentType){
                            chosenPaymentType=paymentType
                            break;
                        }
                    }

                    if (!chosenPaymentType){
                        logger.error("Error Storing Reservation: Error Invalid Payment Type")
                        return render("Error Invalid Payment Type")
                    }

                    /*if first night condition is active we mark as deposit the maximum between the deposit and the first night*/
                    Condition condition=(reservation.inventoryLine as InventoryLine).condition;
                    InventoryLine invL=reservation.inventoryLine as InventoryLine;
                    def reservationPrice = roomStay.totalAmount;

                    def paymentTypeDefaultPrice= condition.earlyCharge && condition.earlyCharge>0? 0 :(chosenPaymentType.paymentPercentage/100) * reservationPrice;
                    def firstNightPrice = condition.payFirstNight && invL.firstNightCost>0 ? invL.firstNightCost : 0;
                    def earlyChargePrice = condition.earlyCharge && condition.earlyCharge>0 ? (condition.earlyCharge/100) * reservationPrice : 0;
                    def minimumCharge = condition.minimumCharge && condition.minimumCharge>0 && condition.minimumCharge< reservationPrice ? condition.minimumCharge : 0;

                    roomStay.guaranteeAmount= [firstNightPrice, earlyChargePrice, minimumCharge].max();
                    roomStay.guaranteeAmount= roomStay.guaranteeAmount>0? roomStay.guaranteeAmount: paymentTypeDefaultPrice;
                    /*Validation just in case the values set are incorrect*/
                    roomStay.guaranteeAmount= roomStay.guaranteeAmount*reservation.quantity > roomStay.totalAmount ? roomStay.totalAmount: roomStay.guaranteeAmount*reservation.quantity
                    roomStay.guaranteePercentage+=roomStay.guaranteeAmount/roomStay.totalAmount

                    /***************Add Discounts to obtain base rate on Roomstays, Roomstays price per night is basen on base rate*************/

                    RangeValue rateDailySet= roomStay.roomRates
                    reservation.ariDiscountsApplied.dailySet.each{
                        DailyValue<Float> dailyValue ->
                            Float val=0
                            if (dailyValue.getValue() != null){
                                val = ((Number) dailyValue.getValue().getValue()).floatValue()
                            } else {
                                val = ((Number)  reservation.ariDiscountsApplied.defaultValue).floatValue()
                            }
                            rateDailySet.putDailyValue(new DailyValue<Float>(dailyValue.startDate,dailyValue.endDate,((rateDailySet.getValueForADate(dailyValue.startDate) as Float)+val) as Float))
                    }

                    /*************END Add Discounts to obtain base rate*************/

                    roomStay.services = servicesRequested

                    roomStay.discounts = reservation.appliedDiscounts;

                    /*capacity of inventory*/
                    roomStay.capacity = (reservation.inventoryLine as InventoryLine).capacity;

                    /*number of rooms*/
                    roomStay.quantity = reservation.quantity;

                    roomStay.inventoryTicker = (reservation.inventoryLine as InventoryLine).ticker;

                    /*NAME OF accommodationType*/

                    roomStay.accommodationType = (reservation.accommodation as Accommodation).name;
                    /*NAME OF mealPlanType*/

                    roomStay.mealPlanType =  (reservation.inventoryLine as InventoryLine).mealPlan.name;
                    /*NAME OF configurationType*/

                    roomStay.configurationType =  (reservation.inventoryLine as InventoryLine).configuration.name;
                    /*NAME OF conditionType*/

                    roomStay.conditionType =  (reservation.inventoryLine as InventoryLine).condition.name;

                    /*TODO: ?????*/
                    roomStay.canceledByClient=0

                    roomStay.bookingPriceRules=reservation.appliedBookingPriceRules

                    roomStays.push(roomStay)

            }
        }catch (Exception e){
            logger.error(" INVALID reservation info "+hotelTicker)
            logger.error("Error Storing Reservation: INVALID reservation info "+ reservations.toString())
            logger.error("Error Storing Reservation: INVALID reservation info "+ cleanJsonRequestData(request.JSON))
            sessionData.reservationOcurring=false
            return render([ error:[message: "SessionExpired" ,code:"EXPSESS" ] ] as JSON)
        }

        if(roomStays.isEmpty()){
            logger.error(" EMPTY ROOMSTAY ")
            logger.error("Error Storing Reservation: EMPTY ROOMSTAY "+ hotelTicker)
            logger.error("Error Storing Reservation: EMPTY ROOMSTAY"+ cleanJsonRequestData(request.JSON))
            sessionData.reservationOcurring=false
            return render([ error:[message: "SessionExpired" ,code:"EXPSESS" ] ] as JSON)
        }

        com.witbooking.middleware.model.Reservation reservation=new com.witbooking.middleware.model.Reservation()

        reservation.amountAfterTax= reservationTotalAmount
        reservation.customer = customer;
        reservation.roomStays = roomStays;

        reservation.currency = session[hotelTicker].witbookerParams.representation.currency;
        Establishment establishment= cacheService.getEstablishment(hotelTicker,null).all
        reservation.currency = session[hotelTicker]?.witbookerParams?.representation?.defaultCurrency ? session[hotelTicker].witbookerParams.representation.defaultCurrency : establishment.configuration.getProperty(EstablishmentAdditionalProperties.CURRENCY_LEGACY) ;
        reservation.language = session[hotelTicker].witbookerParams.representation.locale;

        reservation.codeApplied=witbookerParams.representation.activePromoCodes.join(",")
        reservation.comments = customerData.comments;

        /*TODO: GET FROM STATIC???*/
        reservation.tax = null;

        try {
            reservation.cancellationRelease=Integer.parseInt(establishment.configuration?.get(EstablishmentAdditionalProperties.CANCELLATION_RELEASE))
        } catch (NumberFormatException ex) {
            logger.error("Invalid Cancellation Release "+hotelTicker)
            reservation.cancellationRelease= -1
        }

        ( reservation.channelAddress=reservation.channelId =witbookerParams.representation.witaffiliate )  || (reservation.channelAddress=reservation.channelId =witbookerParams.representation.affiliate)

        reservation.referer =witbookerParams.representation?.referer?.take(1023)

        reservation.trackingId=witbookerParams.representation.tracking_id

        if(witbookerParams.representation.channel){
            reservation.channelId = witbookerParams.representation.channel
        }
        if(paymentData.paymentType==Condition.PAYMENT_TYPE_TPV
                || paymentData.paymentType==Condition.PAYMENT_TYPE_PAYPAL_EXPRESS_CHECKOUT
                || paymentData.paymentType==Condition.PAYMENT_TYPE_PAYPAL_EXPRESS_STANDARD
                || paymentData.paymentType==Condition.PAYMENT_TYPE_SIPAY_TOKENIZED_VAULT
                || paymentData.paymentType==Condition.PAYMENT_TYPE_SIPAY_TOKENIZED_TPV){
            reservation.status = ReservationStatus.PRE_RESERVATION;
            reservation.paymentStatus = 1;
        }else if(paymentData.paymentType==Condition.PAYMENT_TYPE_TRANSFER && witbookerParams.representation.transferAvailabilityHold<=0){
            reservation.status = ReservationStatus.PRE_RESERVATION;
            reservation.paymentStatus = 6;
        }else if(paymentData.paymentType==Condition.PAYMENT_TYPE_TRANSFER){
            reservation.status = ReservationStatus.RESERVATION;
            reservation.paymentStatus = 4;
        }else if(paymentData.paymentType==Condition.PAYMENT_TYPE_CREDITCARD){
            reservation.status = ReservationStatus.RESERVATION;
            reservation.paymentStatus = 0;
        }else{
            logger.error("Error Storing Reservation: ")
            logger.error("Error Processing Payment Type "+ hotelTicker)
            logger.error("Error Storing Reservation: Bad  Payment Data"+ cleanJsonRequestData(request.JSON))
            logger.error("Invalid payment type: ${paymentData as JSON}" )
            sessionData.reservationOcurring=false
            return render("Error Determining Payment Type")
        }
        sessionData.paymentType=paymentData.paymentType

        try {
            if(paymentData.paymentType!=Condition.PAYMENT_TYPE_PAYPAL_EXPRESS_CHECKOUT
                    && paymentData.paymentType!=Condition.PAYMENT_TYPE_SIPAY_TOKENIZED_VAULT
                    && paymentData.paymentType!=Condition.PAYMENT_TYPE_SIPAY_TOKENIZED_TPV ){
                reservation=withotelService.insertReservation(hotelTicker,reservation)
                cleanSessionAfterReservation(hotelTicker)
            }else{
                sessionData.reservation=reservation
            }
        }catch (Exception ex){
            logger.error(" ")
            logger.error("Error Storing Reservation: "+ ex)
            logger.error("Error Storing Reservation: "+ hotelTicker)
            logger.error("Error Storing Reservation: "+ reservation.toString())
            logger.error("Error Storing Reservation: EMPTY ROOMSTAY"+ cleanJsonRequestData(request.JSON))
            for (StackTraceElement s : ex.getStackTrace()) {
                logger.error(""+s);
            }
            logger.error(" ")
            sessionData.reservationOcurring=false
            return render("ERROR")
            /*TODO: send Email?*/
        }

        sessionData.reservationOcurring=false
        render([id:reservation.reservationId , status: "OK"] as JSON)

    }

    def updateReservation(){
        def returnError={
            error->
                render(text: [ error: error.toString() ] as JSON, contentType: "application/json")
        }

        try {
            def reservationData=request.JSON
            String hotelTicker=reservationData.establishment.ticker
            List<Reservation> reservations= session[hotelTicker].reservations as List<Reservation>
            def index=-1
            reservations.eachWithIndex {
                reservation,i->
                    if(reservation.id==reservationData.id){
                        if (reservationData.service!= null && (reservationData.service as List).size()>0){
                            reservationData.service.each{
                                Object serviceData->
                                    Map<String,Object> selectedServiceData
                                    selectedServiceData=[availability:serviceData.availability, selected:serviceData.selected]
                                    Service selectedService
                                    if (reservation.selectedServices==null)
                                        reservation.selectedServices=[:]
                                    if (reservation.selectedServices.containsKey(serviceData.ticker))
                                        selectedService=(reservation.selectedServices[serviceData.ticker] as Map).service
                                    else{
                                        selectedService=findService(hotelTicker,serviceData.ticker)
                                    }
                                    selectedServiceData.service=selectedService
                                    reservation.selectedServices[serviceData.ticker]=selectedServiceData
                            }
                        }
                        index=i
                    }
            }
            if(index==-1)
                return returnError.call(new Exception("Reservation not found"))
        }catch (Exception e){
            return returnError.call(e)
        }
        render(text: [ status: "OK" ] as JSON, contentType: "application/json")
    }


}
