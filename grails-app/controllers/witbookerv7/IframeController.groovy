package witbookerv7

import org.codehaus.groovy.grails.web.mapping.LinkGenerator

class IframeController {


    def index() {}

    def iframetest(){

    }
    def redirectIframe(){
        session["redirect"]=0
        if(params && params.redirectURL){
            session["redirectURL"]=params.redirectURL
            session["redirect"]++
            return redirect(controller: "iframe", action: "redirectIframe")
        }
        if(session["redirectURL"]){
            String redirectURL= session["redirectURL"]
            redirectURL+= redirectURL.indexOf('?')==-1 ? '?' :'&';
            redirectURL+= "redirected=true";
            return redirect(url: redirectURL)
        }
        render view:"/iframe/redirectIframe" ,model: [redirectC:session["redirect"]]
    }
}