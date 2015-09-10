package witbookerv7

import apibridge.WitBookerProperties
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.paypal.dg.PaypalFunctions
import com.witbooking.middleware.integration.sipay.AuthResponse
import com.witbooking.middleware.model.*
import com.witbooking.middleware.utils.EmailsUtils
import com.witbooking.witbooker.Condition
import com.witbooking.witbooker.InventoryLine
import grails.converters.JSON
import grails.util.Environment
import groovy.time.TimeCategory
import org.apache.log4j.Logger
import witbookerv7.util.WitbookerParams
import withotel.CacheService

class PaymentController {

    private static final Logger logger = Logger.getLogger(PaymentController.class.getSimpleName())
    static allowedMethods = [
            payPalExpressCheckout: ['POST', 'GET'],
            payPalExpressCheckoutSuccess: ['POST', 'GET'],
            sipayReservationStatus: ['POST', 'GET'],
            sipayResult: ['POST', 'GET'],
            sipayIframe: ['POST', 'GET'],
            sipay: ['POST', 'GET'],
    ]
    def withotelService

    PaypalFunctions ppf

    protected String urlRootBuilder(boolean includeContext = false) {

        includeContext = Environment.current == Environment.DEVELOPMENT ? true : includeContext
        String protocol = Environment.current == Environment.DEVELOPMENT ? "http://" : "https://"
        String redirectURL = protocol + request.getServerName()
        redirectURL += request.getServerPort() ? ":" + request.getServerPort() : ""
        //redirectURL += request.getContextPath() && includeContext ? request.getContextPath() : ""

    }

    /*TODO: THIS FUNCTION IS DUPLICATED IN BASE CONTROLLER!! */

    protected cleanSessionAfterReservation(String hotelTicker) {
        if (!session[hotelTicker]) {
            return
        }

        if (session[hotelTicker].witbookerParams) {
            session[hotelTicker].witbookerParams = (session[hotelTicker].witbookerParams as WitbookerParams).cleanedWitbookerParams();
        }
        session[hotelTicker].reservations = [];
        session[hotelTicker].reservationOcurring = false
    }

    protected Object handlePaypalError(Map nvp, String hotelTicker, String operation, String paypalError,Reservation reservation) {
        String ErrorCode = nvp.get("L_ERRORCODE0").toString();
        String ErrorShortMsg = nvp.get("L_SHORTMESSAGE0").toString();
        String ErrorLongMsg = nvp.get("L_LONGMESSAGE0").toString();
        String ErrorSeverityCode = nvp.get("L_SEVERITYCODE0").toString();

        logger.error("Error Trying Paypal Transaction setExpressCheckout")
        logger.error("Hotel: ${hotelTicker}")
        logger.error("ErrorCode: ${ErrorCode}")
        logger.error("ErrorShortMsg: ${ErrorShortMsg}")
        logger.error("ErrorLongMsg: ${ErrorLongMsg}")
        logger.error("ErrorSeverityCode: ${ErrorSeverityCode}")

        try {
            withotelService.saveTransactions(hotelTicker,reservation,"null",TransactionType.PAYMENT, TransactionStatus.FAIL)
        } catch (Exception ex) {
            logger.error(" Could not save transactions RESERVATION: "+reservation.reservationId+" TRANSACTION: "+transactionId)
            ex.printStackTrace()
        }

        String redirectURL = urlRootBuilder() + "/v6/select/" + hotelTicker + "/" + reservation.language + "/reservationsv6/step1#/stepOne/list?error=${paypalError}";
        return redirect(url: redirectURL)

    }

    void configurePaypalFunctions(Map<String,Object> sessionData,String hotelTicker){

        WitbookerParams witbookerParams=(WitbookerParams)sessionData.witbookerParams

        Establishment establishment = (Establishment) withotelService.getEstablishmentByLanguage(hotelTicker + CacheService.TICKER_LANGUAGE_SEPARATOR + witbookerParams.representation.language.locale, [])

        String merchantSubject=establishment.configuration.getProperty("paypalMerchantID")

        String paypalEnvironment=establishment.configuration.getProperty("paypalEnvironment")

        boolean sandbox=true

        if(!paypalEnvironment || paypalEnvironment=="sandbox"){
            sandbox=true;
        }else{
            sandbox=false;
        }
        if(!merchantSubject){
            logger.error("BADLY CONFIGURED PAYPAL MERCHANT at hotel "+hotelTicker)
        }
        if(!ppf){
            ppf = new PaypalFunctions(sandbox, merchantSubject)
        }



    }

    def payPalExpressCheckout() {

        logger.info("Initiating  Paypal Express Checkout Payment")

        if (!request.JSON) {
            return render("error")
        }

        String hotelTicker = request.JSON.ticker;
        String cancelURL = request.JSON.cancelUrl;

        if (!session || !session[hotelTicker]) {
            return render([error: [message: "SessionExpired", code: "EXPSESS"]] as JSON)
        }
        if (!session[hotelTicker].reservation) {
            return render([error: [message: "Reservation Not Completed", code: "RESINCOMPLETE"]] as JSON)
        }

        com.witbooking.middleware.model.Reservation reservation = session[hotelTicker].reservation;

        Map<String,Object> sessionData = session[hotelTicker]

        Float totalAmount = 0;
        for (RoomStay roomStay : reservation.roomStays) {
            totalAmount += roomStay.guaranteeAmount;
        }

        String paymentAmount = Float.toString(totalAmount);

        String returnURL = urlRootBuilder() + "/WitBooker/payment/payPalExpressCheckoutSuccess?hotelTicker=${hotelTicker}";

        WitbookerParams witbookerParams = (WitbookerParams) sessionData.witbookerParams
        Establishment establishment = (Establishment) withotelService.getEstablishmentByLanguage(hotelTicker + CacheService.TICKER_LANGUAGE_SEPARATOR + witbookerParams.representation.language.locale, [])

        Map item = new HashMap();
        item.put("name", "Reservation " + establishment.getName() );
        item.put("amt", paymentAmount);
        item.put("qty", "1");

        if (!sessionData || !sessionData.witbookerParams || !sessionData.witbookerParams.representation || !sessionData.witbookerParams.representation.language){
            return render([ error:[message: "SessionExpired" ,code:"EXPSESS" ] ] as JSON)
        }

        configurePaypalFunctions(sessionData,hotelTicker)

        HashMap nvp = ppf.setExpressCheckout(paymentAmount, returnURL, cancelURL, item, reservation.currency, hotelTicker);

        String strAck = nvp.get("ACK").toString();

        if (strAck != null && strAck.equalsIgnoreCase("Success")) {
            String redirectURL=null;
            if(ppf.isSandbox()){
                redirectURL = "https://www.sandbox.paypal.com/checkoutnow?token=" + nvp.get("TOKEN").toString();
            }else{
                redirectURL = "https://www.paypal.com/checkoutnow?token=" + nvp.get("TOKEN").toString();
            }
            render([paypalRedirectUrl: redirectURL] as JSON);
        } else {
            return handlePaypalError(nvp, hotelTicker, "setExpressCheckout", "paypalError",reservation)
        }

    }


    def payPalExpressCheckoutSuccess() {

        logger.info("Processing Paypal Express Checkout Payment")

        String token = request.getParameter("token");
        String payerId = request.getParameter("PayerID");

        if (!session) {
            return render([error: [message: "SessionExpired", code: "EXPSESS"]] as JSON)
        }
        if (!params && !params.hotelTicker) {
            return render([error: [message: "SessionExpired", code: "EXPSESS"]] as JSON)
        }

        String hotelTicker = params.hotelTicker

        Map<String,Object> sessionData = session[hotelTicker]

        if (token != null) {

            configurePaypalFunctions(sessionData,hotelTicker)

            HashMap nvp = ppf.getPaymentDetails(token);

            String strAck = nvp.get("ACK").toString();
            String finalPaymentAmount = null;
            if (strAck != null && (strAck.equalsIgnoreCase("Success") || strAck.equalsIgnoreCase("SuccessWithWarning"))) {
                finalPaymentAmount = nvp.get("AMT").toString();
            }

            String serverName = request.getServerName();

            Map item = new HashMap();
            item.put("name", nvp.get("L_PAYMENTREQUEST_0_NAME0"));
            item.put("amt", nvp.get("L_PAYMENTREQUEST_0_AMT0"));
            item.put("qty", nvp.get("L_PAYMENTREQUEST_0_QTY0"));



            if (!session[hotelTicker]) {
                return render([error: [message: "SessionExpired", code: "EXPSESS"]] as JSON)
            }
            if (!session[hotelTicker].reservation) {
                return render([error: [message: "Reservation Not Completed", code: "RESINCOMPLETE"]] as JSON)
            }

            com.witbooking.middleware.model.Reservation reservation = session[hotelTicker].reservation

            nvp = ppf.confirmPayment(token, payerId, finalPaymentAmount,
                    serverName, item);

            strAck = nvp.get("ACK").toString();

            if (strAck != null && (strAck.equalsIgnoreCase("Success") || strAck.equalsIgnoreCase("SuccessWithWarning"))) {

                String transactionId = nvp.get("PAYMENTINFO_0_TRANSACTIONID").toString(); // '
                String transactionType = nvp.get("PAYMENTINFO_0_TRANSACTIONTYPE").toString(); // '
                String paymentType = nvp.get("PAYMENTINFO_0_PAYMENTTYPE").toString(); // '
                String orderTime = nvp.get("PAYMENTINFO_0_ORDERTIME").toString(); // '
                String amt = nvp.get("PAYMENTINFO_0_AMT").toString(); // ' The final amount
                String currencyCode = nvp.get("PAYMENTINFO_0_CURRENCYCODE").toString(); // ' A
                String feeAmt = nvp.get("PAYMENTINFO_0_FEEAMT").toString(); // ' PayPal fee
                String taxAmt = nvp.get("PAYMENTINFO_0_TAXAMT").toString(); // ' Tax charged
                String paymentStatus = nvp.get("PAYMENTINFO_0_PAYMENTSTATUS").toString();
                String pendingReason = nvp.get("PAYMENTINFO_0_PENDINGREASON").toString();
                String reasonCode = nvp.get("PAYMENTINFO_0_REASONCODE").toString();

                try {
                    reservation.status = Reservation.ReservationStatus.RESERVATION;
                    reservation.paymentStatus = 2;
                    reservation = (Reservation) withotelService.insertReservation(hotelTicker, reservation)
                    /*TODO: Call endpoint to store transaction INput reservation , transactionId, status Output Exception or TransactionID in TRansactionTable */
                    cleanSessionAfterReservation(hotelTicker)
                    String subject = "Paypal Success " + hotelTicker;
                    try {
                        withotelService.saveTransactions(hotelTicker,reservation,transactionId,TransactionType.PAYMENT, TransactionStatus.SUCCESS)
                    } catch (Exception ex) {
                        logger.error(" Could not save transactions RESERVATION: "+reservation.reservationId+" TRANSACTION: "+transactionId)
                        ex.printStackTrace()
                    }

                    try {
                        EmailsUtils.sendEmailToAdmins(subject, subject, Arrays.asList("WitBooker Ok", "PagoExitosoPaypal"), new Exception("False Exception"));
                    } catch (Exception ex) {
                        logger.error(" Could not send email to admins")
                        ex.printStackTrace()
                    }
                } catch (Exception ex) {
                    logger.error(" ")
                    logger.error("Error Storing Reservation: " + ex)
                    logger.error("Error Storing Reservation: " + hotelTicker)
                    logger.error("Error Storing Reservation: PAYPAL HAS CHARGED THE CUSTOMER!! " + (reservation as JSON))
                    for (StackTraceElement s : ex.getStackTrace()) {
                        logger.error("" + s);
                    }
                    logger.error(" ")
                    sessionData.reservationOcurring = false
                    String subject = "Error Inserting Reservation  after Paypal Payment'" + hotelTicker + "' ";
                    System.out.println("Working Directory = " + System.getProperty("user.dir"));
                    try {
                        EmailsUtils.sendEmailToAdmins(subject, subject, Arrays.asList("WitBooker Errors", "Error Paypal Reservation"), ex);
                    } catch (Exception e) {
                        logger.error(" Could not send email to admins")
                        e.printStackTrace()
                    }

                    return render("ERROR")
                    /*TODO: send Email?*/
                }
                sessionData.reservationOcurring = false
                String redirectURL = urlRootBuilder();
                redirectURL += "/v6/select/" + hotelTicker + "/" + reservation.language + "/reservationsv6/confirmation/" + reservation.reservationId + "/java"
                redirect(url: redirectURL)
            } else {
                // Display a user friendly Error on the page using any of the
                // following error information returned by PayPal
                return handlePaypalError(nvp, hotelTicker, "setExpressCheckout", "paypalError",reservation)
            }
        }

    }


    protected Object handleSipayError(Map params, String hotelTicker, String operation, String error) {

        logger.info("Handling Error")

        String resultCode = params.ResultCode
        String resultDescription = params.ResultDescription
        String authorizationCode = params.Authorizator
        String ticketNumber = params.ticket
        String internalNumber = params.TransactionId
        String approvalCode  = params.ApprovalCode
        String extra = params.extra

        logger.error("Error Trying Paypal Transaction setExpressCheckout")
        logger.error("Hotel: ${hotelTicker}")
        logger.error("resultCode: ${resultCode}")
        logger.error("resultDescription: ${resultDescription}")
        logger.error("authorizationCode: ${authorizationCode}")
        logger.error("ticketNumber: ${ticketNumber}")
        logger.error("internalNumber: ${internalNumber}")
        logger.error("approvalCode: ${approvalCode}")
        logger.error("extra: ${extra}")

        Reservation reservation = session[hotelTicker].reservation
        try {
            String subject = "Error Inserting Reservation Sipay Payment'" + hotelTicker + "' ";
            Gson gson = new GsonBuilder().create()
            EmailsUtils.sendEmailToAdmins(subject,"Reservation :"+gson.toJson(reservation)+" Params : "+gson.toJson(params), Arrays.asList("WitBooker Error", "Error SIPAY Payment"), new Exception("False Exception"));
        } catch (Exception e) {
            logger.error(" Could not send email to admins")
            e.printStackTrace()
        }
        /*Clear Sipay Data*/

        session[hotelTicker].sipay=null

        String redirectURL = urlRootBuilder() + "/WitBooker/payment/sipay/?ticker=" + hotelTicker + "#/main/" + reservation.language + "/${error}";
        return redirect(url: redirectURL)

    }

    def sipayReservationStatus() {
        if(!params.hotelTicker){
            return render("Error: Invalid Params. Null Ticker")
        }
        String hotelTicker = params.hotelTicker;

        if (!session[hotelTicker]) {
            return render([error: [message: "SessionExpired", code: "EXPSESS"]] as JSON)
        }
        if (!session[hotelTicker].reservation) {
            return render([error: [message: "Reservation Not Completed", code: "RESINCOMPLETE"]] as JSON)
        }

        Reservation reservation = session[hotelTicker].reservation

        return render( [status: session[hotelTicker].reservationComplete, id:reservation.reservationId] as JSON )
    }

    def sipayResult() {

        logger.info("Received Result")

        if (!session) {
            return render([error: [message: "SessionExpired", code: "EXPSESS"]] as JSON)
        }

        String resultCode = params.ResultCode
        String resultDescription = params.ResultDescription
        String authorizationCode = params.Authorizator
        String ticketNumber = params.ticket
        String internalNumber = params.TransactionId
        String approvalCode  = params.ApprovalCode
        String extra = params.extra

        if (!params || !params.hotelTicker) {
            return render("Error: Invalid Params. Null Ticker")
        }

        String hotelTicker = params.hotelTicker

        if (!session[hotelTicker]) {
            return render([error: [message: "SessionExpired", code: "EXPSESS"]] as JSON)
        }
        if (!session[hotelTicker].reservation) {
            return render([error: [message: "Reservation Not Completed", code: "RESINCOMPLETE"]] as JSON)
        }

        Map<String, Object> sessionData = (HashMap<String, Object>) session[hotelTicker]

        logger.info(" SIPAY Result Code is "+ resultCode)

        if (resultCode == "0") {

            try {
                logger.info("Trying to insert Reservation ")
                Reservation reservation = session[hotelTicker].reservation
                reservation.status = Reservation.ReservationStatus.RESERVATION;
                reservation.paymentStatus = 0;
                session[hotelTicker].reservation = reservation = (Reservation) withotelService.insertReservation(hotelTicker, reservation)
                cleanSessionAfterReservation(hotelTicker)
                logger.info("Inserted Reservation")
                /*Payment succeeds and reservation is marked as complete*/
                session[hotelTicker].reservationComplete=true;
                Map<String,String> model=[:]
                model["STEP3_URL"]=urlRootBuilder()+"/v6/select/" + hotelTicker + "/" + reservation.language + "/reservationsv6/confirmation/" + reservation.reservationId + "/java"
                sessionData.sipay=null
                return render(view: "sipayResult", model:model)
            } catch (Exception ex) {
                logger.error(" ")
                logger.error("Error Storing Reservation: " + ex)
                logger.error("Error Storing Reservation: " + hotelTicker)
                logger.error("Error Storing Reservation: SIPAY HAS CHARGED THE CUSTOMER!! " + (reservation as JSON))
                for (StackTraceElement s : ex.getStackTrace()) {
                    logger.error("" + s);
                }
                logger.error(" ")
                sessionData.reservationOcurring = false
                System.out.println("Working Directory = " + System.getProperty("user.dir"));
                return handleSipayError(params, hotelTicker, "sale", "sipayError")
            }

        } else {
            logger.info("Invalid Result Code")

//      SIPAY ERROR
//        -1008 SIN CONEXIÓN CON LA ENTIDAD BANCARIA, NO SE PUDO PROCESAR LA OPERACIÓN
//        -1004 IMPOSIBLE TRAMITAR OPERACIÓN
//        -6 OPERACIÓN NO PROCESADA (se debe reintentar la operación)


//      SIPAY ERROR CARD
//        -1    NO SE PUDO VALIDAR TARJETA
//        -1017 TARJETA NO AUTORIZADA <detalle>
//        -1001 NO SE HA PODIDO TRAMITAR LA OPERACIÓN, FALTAN DATOS DE LA TARJETA

            String errorCode="sipayError"

            if(resultCode=="1009" || resultCode=="1017" ||resultCode=="1001" ||resultCode=="-1" ){
                errorCode="sipayErrorCard"
            }

            return handleSipayError(params, hotelTicker, "sale", errorCode)
        }


    }

    private boolean isSipayActivated(List<com.witbooking.witbooker.Reservation> reservations) {
        if (!reservations || reservations.isEmpty()) {
            return false;
        }
        for (Map<String, Object> paymentType : ((InventoryLine) reservations.get(0).inventoryLine).condition.paymentTypes) {
            if (paymentType.ticker == Condition.PAYMENT_TYPE_SIPAY_TOKENIZED_VAULT
                    || paymentType.ticker == Condition.PAYMENT_TYPE_SIPAY_TOKENIZED_TPV) {
                return true;
            }
        }
        return false;
    }

    def Map<String, Object> buildSipayModel(String hotelTicker, WitbookerParams witbookerParams) {
        Map<String, Object> model = [:]
        def cart = [:]
        cart.items = []
        cart.total = 0
        Map<String, Object> sessionData = (Map<String, Object> ) session[hotelTicker]

        if(sessionData.paymentType==Condition.PAYMENT_TYPE_SIPAY_TOKENIZED_TPV){
            Reservation reservation = session[hotelTicker].reservation
            reservation.roomStays.each {
                roomStay ->
                    int counter = 0;
                    def item = [:]
                    item.name = "Room " + counter
                    item.quantity = roomStay.quantity
                    item.total = roomStay.guaranteeAmount
                    ((List<Object>) cart.items).add(item)
                    cart.total += roomStay.guaranteeAmount;
            }
            cart.currency = [ticker: reservation.currency, symbol: reservation.currency]
        }

        Establishment establishment = (Establishment) withotelService.getEstablishmentByLanguage(hotelTicker + CacheService.TICKER_LANGUAGE_SEPARATOR + witbookerParams.representation.language.locale, [])

        Map data = [establishment: [name: establishment.name], cart: cart]

        model.sipayData = [sipayIframe: sessionData["sipay"].sipayIframe, sipayStart: sessionData["sipay"].sipayStart.getTime()] as JSON

        model.data = data as JSON

        return model
    }
    def sipayIframeSimulated() {
        String hotelTicker=params.hotelTicker
        String resultUrl=urlRootBuilder()+"/WitBooker/payment/sipayResult?hotelTicker=${hotelTicker}"
        render(view:"sipayIframeSimulated", model:[resultUrl:resultUrl] )
    }

    def sipay() {

        boolean jsonData=false

        if(params.jsonData){
            jsonData=true
        }

        logger.info("Initiating Sipay Request")

        Map<String,Object> model = [:]

        if(!params.ticker && (!request.JSON || !request.JSON.ticker)){
            return render("Error: Null Ticker")
        }
        String hotelTicker=null;

        (hotelTicker = request.JSON.ticker) || (hotelTicker = params.ticker );

        Map<String,Object> sessionData=(HashMap<String,Object>) session[hotelTicker]

        if (!sessionData || !sessionData.witbookerParams || !sessionData.witbookerParams.representation || !sessionData.witbookerParams.representation.language){
            return render([ error:[message: "SessionExpired" ,code:"EXPSESS" ] ] as JSON)
        }
        /*Payment Start and reservation is marked as pending*/
        sessionData.reservationComplete=false;


        WitbookerParams witbookerParams=(WitbookerParams)sessionData.witbookerParams


        if(sessionData.sipay && sessionData.sipay.sipayStart){
            Date sipayStart=(Date)sessionData.sipay.sipayStart;
            boolean canReuse=false
            use(TimeCategory) {
                Date timeoutTime=sipayStart+WitBookerProperties.SIPAY_TIMEOUT_INTERVAL.seconds
                int bufferSeconds=60
                canReuse=(timeoutTime-bufferSeconds.seconds) > new Date()
            }
            Map<String,Object> sipayData=sessionData.sipay

            if(canReuse){
                logger.info("Sipay Iframe has not Expired, reusing")
                //model.sipayData=[sipayIframe:sipayData.sipayIframe, sipayStart:sipayData.sipayStart.getTime() ] as JSON
                model=buildSipayModel(hotelTicker, witbookerParams)
                if(jsonData){
                    return render(model as JSON)
                }
                return render(view:"sipay" ,model:model)
            }

            logger.info("Sipay Iframe Expired")
            println()
            println()
        }
        logger.info("Asking for Sipay Auth")

        List<com.witbooking.witbooker.Reservation> reservations=session[hotelTicker].reservations;

        if(!isSipayActivated(reservations)){
            return render("Sipay not active for reservations")
        }

        String returnURL=urlRootBuilder()+"/WitBooker/payment/sipayResult?hotelTicker=${hotelTicker}"

        Reservation reservation = session[hotelTicker].reservation

        Float amount=0

        if(sessionData.paymentType==Condition.PAYMENT_TYPE_SIPAY_TOKENIZED_VAULT){
            amount=0
        }else if(sessionData.paymentType==Condition.PAYMENT_TYPE_SIPAY_TOKENIZED_TPV){
            reservation.roomStays.each {
                roomStay ->
                    amount += roomStay.guaranteeAmount
            }
        }else{
            amount=0
        }

        AuthResponse authResponse = (AuthResponse)withotelService.getPaymentForm(hotelTicker,returnURL,witbookerParams.representation.locale,amount)

        reservation.customer.tokenPayments = authResponse.token

        /*TODO:CHANGE FOR TOKEN*/
        sessionData["sipay"]=[:]
        sessionData["sipay"].sipayToken  = authResponse.idRequest
        sessionData["sipay"].sipayIframe = authResponse.redirectsUrl
        //sessionData["sipay"].sipayIframe = urlRootBuilder()+"/WitBooker/payment/sipayIframeSimulated?hotelTicker=${hotelTicker}"
        sessionData["sipay"].sipayInit = urlRootBuilder()+"/WitBooker/payment/sipayInit"
        sessionData["sipay"].sipayStart  = new Date()

        logger.info("Got new Iframe at "+new Date())

        model=buildSipayModel(hotelTicker, witbookerParams)

        if(jsonData){
            return render(model as JSON)
        }
        return render(view:"sipay" ,model:model)


    }


}
