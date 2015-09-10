package witbookerv7

import com.google.gson.Gson

class TranslationController {
    def messageSource

    protected getMessageMap(String locale){
        if(!locale){
            return [:]
        }
        def messageMap = messageSource.listMessageCodes(new Locale(locale))
        return messageMap
    }

    def index(){
        Gson gson= new Gson();
        render gson.toJson(getMessageMap(params.lang))
    }
}