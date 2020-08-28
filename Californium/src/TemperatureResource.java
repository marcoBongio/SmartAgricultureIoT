import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

//Definition of the Temperature Resource 
    class TemperatureResource extends CoapResource {
        private int index;

        public TemperatureResource(int i) {
            // set resource identifier
            super("HumidityResource:");
            this.index = i;
            // set display name
            getAttributes().setTitle("Temperature Resource " + i);
        }
        
    //Definition of the GET handler in order to answer to the Client's requests
        @Override
        public void handleGET(CoapExchange exchange) {
            //exchange.respond("{ \"humidity\":\""+ProxyCoAP.getCache(index)+"\" }");
            System.out.println("TemperatureResource.handleGET");
        }
    }
