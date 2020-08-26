#include "humidity_sensor.h"

static bool result = false;
int TIME; //globale perchè contiki lo resetta
const int VAR_RANGE = 5; //range di variazione dell'umidità (in percentuale)

PROCESS(humidity_sensor, "Humidity Sensor");
AUTOSTART_PROCESSES(&humidity_sensor);

static void response_handler(coap_message_t *response){

    if (response == NULL)
        return;
        
    LOG_DBG("Response %s\n", response->payload);
    if(strcmp((const char *)response->payload, "ACK") == 0)
    	result = true;
}

PROCESS_THREAD(humidity_sensor, ev, data) {

    static coap_endpoint_t server_ep;
    static coap_message_t request[1];

    PROCESS_BEGIN();
    
	coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);
	//coap_endpoint_print(&server_ep);
	//printf("server_ep = %s\n", server_ep);
    do {
        coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);
        coap_set_header_uri_path(request, (const char *) &SERVER_REGISTRATION);
        
        const char mes[] = "{\"NodeType\":\"sensor\", \"NodeResource\":\"humidity\"}";
        printf("%s\n", mes);
        
        coap_set_payload(request, (uint8_t *)mes, sizeof(mes)-1);
        
        COAP_BLOCKING_REQUEST(&server_ep, request, response_handler);
        
    } while (!result);

    LOG_INFO("Starting humidity sensor \n");

    coap_activate_resource(&res_humidity, "humidity");
	
	//initialize the timer
	TIME = ((random_rand() % 10)+10 );
	static struct etimer timer;
	etimer_set(&timer, TIME*CLOCK_SECOND);

	printf("Timer inizialized\n");


    while (1) {

        PROCESS_YIELD_UNTIL(etimer_expired(&timer));
       	
        if (etimer_expired(&timer)){		
			//randomly choose if there has been a variation
			if((TIME % 2) == 0)
			{
				int var = (random_rand() % VAR_RANGE);
				if(var > 0)
				{ 		
					//decide wether it is an increase or decrease of humidity (50% chance)
					if(random_rand() % 2 == 0) var = var*(-1);

					humidity += var;
					if(humidity > HUM_MAX)
						humidity = HUM_MAX;
					else if(humidity < HUM_MIN)
						humidity = HUM_MIN;
					else {
						printf("Humidity variation registered, variation: %d \n", var);
						res_humidity.trigger(); //trigger the event to notify observers
						}
				}
				//else if(debug) printf("DEBUG: variazione = 0\n"); 
			}
			//else if(debug) printf("...\n");
		}
		   //reset random timer
		    TIME = ((random_rand() % 10) +10);
		    etimer_set(&timer, TIME*CLOCK_SECOND);
    }

    PROCESS_END();
}
