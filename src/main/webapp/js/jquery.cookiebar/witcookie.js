/*
 * Copyright (C) 2014 http://www.witbooking.com
 * 
 * Witbooking cookie object
 * Private treatment of cookie bar to inform about cookies law
 */
(function($){
	$.witCookiesLaw = function(options){
		var defaults = {
			language: 'es',
			effect: 'fade',
			expireDays: 365,
			policyButton: true,
			mensajeTxt: 'Las cookies nos permiten ofrecer nuestros servicios. Al utilizar nuestros servicios, aceptas el uso que hacemos de las cookies.',
			aceptarTxt: 'Aceptar',
			masInfoTxt: 'Más información',
			urlDefault: options.url.en			
		}	
		var options = $.extend(defaults,options);

		//check laguage and modify options
		var url = options.urlDefault;
		switch (options.language)
		{
	  	case 'es':
	  		if(typeof options.url.es != 'undefined'){
	        	url = options.url.es;
	        }
		  	break;
		case 'en':
		  	options.mensajeTxt = 'Cookies help us deliver our services. By using our services, you agree to our use of cookies.';
	        options.aceptarTxt = 'Ok';
	        options.masInfoTxt = 'Learn more';	        
		  	break;
		case 'it':
		  	options.mensajeTxt = 'I cookie ci aiutano a fornire i nostri servizi. Utilizzando tali servizi, accetti l\'utilizzo dei cookie.';
	        options.aceptarTxt = 'Ok';
	        options.masInfoTxt = 'Ulteriori informazioni';
	        if(typeof options.url.it != 'undefined'){
	        	url = options.url.it;
	        }
		  	break;
		case 'fr':
		  	options.mensajeTxt = 'Les cookies assurent le bon fonctionnement de nos services. En utilisant ces derniers, vous acceptez l\'utilisation des cookies.';
	        options.aceptarTxt = 'Ok';
	        options.masInfoTxt = 'En savoir plus';
	        if(typeof options.url.fr != 'undefined'){
	        	url = options.url.fr;
	        }
		  	break;	
		case 'ca':
		  	options.mensajeTxt = 'Les cookies ens permeten oferir els nostres serveis. En utilitzar els nostres serveis, acceptes l\'ús que fem de les cookies.';
	        options.aceptarTxt = 'Acceptar';
	        options.masInfoTxt = 'Més informació';
	        if(typeof options.url.fr != 'undefined'){
	        	url = options.url.fr;
	        }
		  	break;
		case 'de':
		  	options.mensajeTxt = 'Cookies helfen uns bei der Bereitstellung unserer Dienste. Durch die Nutzung unserer Dienste erklären Sie sich damit einverstanden, dass wir Cookies setzen.';
	        options.aceptarTxt = 'Ok';
	        options.masInfoTxt = 'Weitere Informationen';
	        if(typeof options.url.de != 'undefined'){
	        	url = options.url.de;
	        }
		  	break;	
		case 'nl':
		  	options.mensajeTxt = 'Cookies helpen ons bij het leveren van onze diensten. Door gebruik te maken van onze diensten, gaat u akkoord met ons gebruik van cookies.';
	        options.aceptarTxt = 'Ok';
	        options.masInfoTxt = 'Meer informatie';
	        if(typeof options.url.nl != 'undefined'){
	        	url = options.url.nl;
	        }
		  	break;
		case 'ru':
		  	options.mensajeTxt = 'Печенье позволяют нам предлагать наши услуги. Пользуясь нашими услугами, Вы соглашаетесь с нашими использования куки.';
	        options.aceptarTxt = 'Ok';
	        options.masInfoTxt = 'Узнать больше';
	        if(typeof options.url.ru != 'undefined'){
	        	url = options.url.ru;
	        }
		  	break;  	
		case 'pt':
		  	options.mensajeTxt = 'Os cookies ajudam-nos a oferecer os nossos serviços. Ao utilizar os nossos serviços, concorda com a nossa utilização de cookies.';
	        options.aceptarTxt = 'Ok';
	        options.masInfoTxt = 'Saiba mais';
	        if(typeof options.url.pt != 'undefined'){
	        	url = options.url.pt;
	        }
		  	break;  	
		default:
		  	options.mensajeTxt = 'Cookies help us deliver our services. By using our services, you agree to our use of cookies.';
	        options.aceptarTxt = 'Ok';
	        options.masInfoTxt = 'Learn more';
	        break;
		}
	    $.cookieBar({
	        message: options.mensajeTxt,
	        acceptText: options.aceptarTxt,
	        policyButton: options.policyButton,
	        policyText: options.masInfoTxt,
	        policyURL: url,
	        expireDays: options.expireDays,
	        effect: options.effect
	    }); 
	}	
})(jQuery);