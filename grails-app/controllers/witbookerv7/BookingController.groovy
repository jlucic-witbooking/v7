package witbookerv7
import apibridge.WitBookerProperties
import com.witbooking.middleware.exceptions.FrontEndException
import com.witbooking.middleware.model.Language
import com.witbooking.witbooker.EstablishmentAdditionalProperties
import com.witbooking.witbooker.EstablishmentStaticData
import grails.util.Environment
import org.apache.log4j.Logger
import witbookerv7.util.WitbookerParams
import withotel.CacheService

/**
 *
 * @author Jorge Lucic
 */

class BookingController extends BaseController {

    private static final Logger logger = Logger.getLogger(BookingController.class);

    def index () {
        render(
                view: "index",
        )
    }

    def step1() {
        def model =[:]
        try{
            model=getEstablishmentData(params)
            if(model==null){
                return render(view:"/error", status: 500)
            }
            EstablishmentAdditionalProperties establishmentAdditionalProperties=model.establishmentAdditionalProperties
            model.locale=params.locale?params.locale:"spa"
            model.STATIC_ROOT_URL=WitBookerProperties.STATIC_ROOT_URL
            model.URL_WITBOOKER_V6=establishmentAdditionalProperties.bookingEngineDomain?establishmentAdditionalProperties.bookingEngineDomain: WitBookerProperties.URL_WITBOOKER_V6
            model.ticker=params.ticker?params.ticker:""
            model.name=(model.initialDataOriginal as EstablishmentStaticData).establishment.name ? (model.initialDataOriginal as EstablishmentStaticData).establishment.name+" | Witbooker":model.ticker +" | Witbooker"
            model.pageTitle=establishmentAdditionalProperties.witbookerPageTitle ? establishmentAdditionalProperties.witbookerPageTitle : model.name
        }catch (Exception ex){
            logger.error("Error Handling request: "+ request.forwardURI+"?"+request.queryString)
            logger.error("Error Loading the step1: "+ ex)
            if (ex instanceof FrontEndException) {
                for (StackTraceElement s : ex.getStackTrace()) {
                    if (s.toString().contains("com.witbooking") && !s.toString().contains("BeanRemote"))
                        logger.error("Error in WitBookerAPI services: " + s);
                }
            }else{
                for (StackTraceElement s : ex.getStackTrace()) {
                    logger.error(""+s);
                }
            }
            logger.error("");
            Environment.executeForCurrentEnvironment {
                development {
                    request.setAttribute("javax.servlet.error.status_code",500)
                    forward(controller: 'errors', action: 'error',model: [exception: ex])
                }
                production {
                    render view:"/error", status: 500
                }
                test {
                    render view:"/error", status: 500
                }
            }
            return
        }

        render view:"/booking/index" ,model: model
    }

    def resetHotelData() {
        def model =[:]
        //TODO: What if params are null

        def ticker=params.ticker?params.ticker:""
        def establishmentInfo=cacheService.getEstablishment(ticker,null,null,null,true)
        if(establishmentInfo?.containsKey("languages")){
            establishmentInfo.get("languages").each {
                Language it ->
                    cacheService.resetEstablishmentByLanguage(ticker+CacheService.TICKER_LANGUAGE_SEPARATOR+it.locale)
            }
        }
        cacheService.resetApiData(ticker)
        cacheService.resetInfoData(ticker)
        cacheService.resetEstablishmentData(ticker)
        cacheService.resetEstablishmentInfo(ticker)
        cacheService.resetHotelData(ticker)
        render "Cache Cleared"
    }


    def cookies() {
        def model = session[params.ticker]
        def language =[:]
        def wparams=null
        if(model){
            wparams = model.witbookerParams as WitbookerParams
            language = wparams?.representation?.language
        }else{
            language.locale="eng"
            language.code="en"
        }
        def view= "/cookies/cookie_en"
        if(language==null){
            logger.error("Hotel without  Lang "+ request.forwardURI)
            language = [:]
            language.locale = "eng"
            language.code = "en"
        }
        if(language?.code=="es"){
            view= "/cookies/cookie_es"
        }
        def modelView = [:]
//        modelView.wparams = wparams
        modelView.hotel = [:]
        modelView.hotel = getCacheService().getEstablishmentByLanguage(params.ticker+CacheService.TICKER_LANGUAGE_SEPARATOR+language.locale)
        if(!modelView.hotel){
            redirect (controller:"errors",action:"notFound");
            return;
        }
        modelView.multimediaUrl = WitBookerProperties.getSTATIC_ROOT_URL()
        render(
                view: view,
                model: modelView
        )
    }

}
