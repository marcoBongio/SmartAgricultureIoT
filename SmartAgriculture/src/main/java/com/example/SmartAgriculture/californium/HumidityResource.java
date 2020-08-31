package com.example.SmartAgriculture.californium;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

//Definition of the Humidity Resource 
    class HumidityResource extends CoapResource {
        private int index;

        public HumidityResource(int i) {
            // set resource identifier
            super("HumidityResource:");
            this.index = i;
            // set display name
            getAttributes().setTitle("Humidity Resource " + i);
        }
        
    //Definition of the GET handler in order to answer to the Client's requests
        @Override
        public void handleGET(CoapExchange exchange) {
            //exchange.respond("{ \"humidity\":\""+ProxyCoAP.getCache(index)+"\" }");
            System.out.println("HumidityResource.handleGET");
        }
    }
