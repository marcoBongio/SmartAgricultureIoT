#include "humidity_sensor.h"

static bool result = false;
int TIME; //globale perchè contiki lo resetta
const int VAR_RANGE = 5; //range di variazione dell'umidità (in percentuale)
bool actuator_assigned = false;
char actuator_ip[39];

PROCESS(humidity_sensor, "Humidity Sensor");
AUTOSTART_PROCESSES(&humidity_sensor);

static void response_handler(coap_message_t *response){

    if (response == NULL)
        return;
        
    LOG_DBG("Response %s\n", response->payload);
    if(strcmp((const char *)response->payload, "ACK") == 0)
    	result = true;
}

static void actuator_response_handler(coap_message_t *response){

	if (response == NULL)
		return;

	LOG_DBG("Actuator IP: %s\n", response->payload);
	actuator_assigned = true;
	strcpy(actuator_ip, "coap://[");
	strcat(actuator_ip, (const char *)response->payload);
	strcat(actuator_ip,"]:5683");
    
    
}

static void test_resp_handler(coap_message_t *response){
}

PROCESS_THREAD(humidity_sensor, ev, data) {

	static coap_endpoint_t actuator_ep;
	static coap_message_t request[1];
	static coap_endpoint_t server_ep;
    
    PROCESS_BEGIN();
    
	coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);

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
        	if(!actuator_assigned) { //actuator_discovery();
        		LOG_DBG("Actuator Discovery...\n");
	
			coap_init_message(request, COAP_TYPE_CON, COAP_GET, 0);
			coap_set_header_uri_path(request, (const char *) &SERVER_REGISTRATION);
			const char mes[] = "irrigator";

			coap_set_payload(request, (uint8_t *)mes, sizeof(mes)-1);

			printf("Actuator IP request: %s\n", (const char*) request->payload);
			COAP_BLOCKING_REQUEST(&server_ep, request, actuator_response_handler);

			coap_endpoint_parse(actuator_ip, strlen(actuator_ip), &actuator_ep);
		}
        			
			//randomly choose if there has been a variation
			if((TIME % 2) == 0)
			{
				int var = (random_rand() % VAR_RANGE);
				if(var > 0)
				{ 		
					//decide wether it is an increase or decrease of humidity (50% chance)
					if(random_rand() % 2 == 0) var = var*(-1);

					humidity += var;
					
					printf("Humidity variation registered, variation: %d \n", var);
					res_humidity.trigger(); //trigger the event to notify observers
					
					if(humidity < 45) {
						coap_init_message(request, COAP_TYPE_CON, COAP_PUT, 0);
						coap_set_header_uri_path(request, "/irrigator");

						char mes[20];
						strcpy(mes,"status=on");
						//if(status) strcat(mes, "on");
						//else strcat(mes, "off");
						
						LOG_DBG("Toggling actuator %s\n", mes);

						coap_set_payload(request, (uint8_t *)mes, sizeof(mes)-1);

						//printf("PUT request: %s\n", (const char*) request->payload);
						COAP_BLOCKING_REQUEST(&actuator_ep, request, test_resp_handler); //controllare resp_handler se serve o no
							//humidity = HUM_MIN;
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
