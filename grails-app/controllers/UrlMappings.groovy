class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?"{
            constraints {
                // apply constraints here
            }
        }
        "/$version/select/$ticker/$language/$controller/$actionM/"{
            controller= "booking"
            action= "step1"
            constraints {
                language(matches:/en|ca|es|pt|it|de|fr|nl|ru|pl|ar|zh/)
            }
        }
        "/$version/select/$ticker/$controller/$actionM/"{
            controller= "booking"
            action= "step1"
        }

        "/"(view:"/index")
        "/v6/updateBrowser"(view:"/updateBrowser")
        "500"(controller:"errors",action:"error")
        "404"(controller:"errors",action:"notFound")
        "403"(controller:"errors",action:"forbidden")
        "/product"(controller: "base", action: "step1")
    }
}
