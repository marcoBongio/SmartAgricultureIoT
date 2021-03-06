#include "humidity_sensor.h"

static bool result = false;
int TIME; //globale perchè contiki lo resetta
const int VAR_RANGE = 3; //range di variazione dell'umidità (in percentuale)

bool actuator_assigned = false;
bool actuator_status = false;
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

	if (response == NULL || response->payload == NULL) {
		LOG_DBG("No Actuator found...\n");
		return;
	}

	LOG_DBG("Actuator IP: %s\n", response->payload);
	actuator_assigned = true;
	strcpy(actuator_ip, "coap://[");
	strcat(actuator_ip, (const char *)response->payload);
	strcat(actuator_ip,"]:5683");    
}

static void test_resp_handler(coap_message_t *response){
}

static void check_response_handler(coap_message_t *response){

	if (response == NULL) {
		LOG_DBG("No Actuator found...");
		return;
	}

	if(strcmp("{\"status\":1}", (const char *)response->payload) == 0 && !actuator_status){
		actuator_status = true;
	}
	else if(strcmp("{\"status\":0}", (const char *)response->payload) == 0 && actuator_status){
		actuator_status = false;
	} 
}

PROCESS_THREAD(humidity_sensor, ev, data) {

	static coap_endpoint_t actuator_ep;
	static coap_message_t request[1];
	static coap_endpoint_t server_ep;
    
    PROCESS_BEGIN();
    
    LOG_INFO("Starting humidity sensor \n");
    coap_activate_resource(&res_humidity, "humidity");
    
	coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);

    do {
        coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);
        coap_set_header_uri_path(request, (const char *) &SERVER_REGISTRATION);
        
        const char mes[] = "{\"NodeType\":\"sensor\", \"NodeResource\":\"humidity\"}";
        printf("%s\n", mes);
        
        coap_set_payload(request, (uint8_t *)mes, sizeof(mes)-1);
        
        COAP_BLOCKING_REQUEST(&server_ep, request, response_handler);
        
    } while (!result);

    
	
	//initialize the timer
	static struct etimer timer;
	static struct etimer et; //timer to check irrigator status
	etimer_set(&timer, CLOCK_SECOND*60);
	etimer_set(&et, CLOCK_SECOND*10); //10sec
	
	printf("Timer inizialized\n");

    while (1) {

        PROCESS_YIELD_UNTIL(etimer_expired(&timer) || etimer_expired(&et));
       	
        if (etimer_expired(&timer)){
			//randomly choose if there has been a variation
			if((TIME % 2) == 0 && !actuator_status)
			{
				int var = (random_rand() % VAR_RANGE);
				if(var > 0)
				{ 		
					//decide wether it is an increase or decrease of humidity (50% chance)
					//if(random_rand() % 2 == 0) var = var*(-1);

					humidity -= var;
						
					printf("Natural Humidity variation registered, variation: %d \n", var);
					//res_humidity.trigger(); //trigger the event to notify observers
				}
			}

		   //reset random timer
		    TIME = ((random_rand() % 10) +10);
		    etimer_set(&timer, TIME*CLOCK_SECOND);
		}
		
		if(etimer_expired(&et)){
			if(!actuator_assigned) {
				LOG_DBG("Actuator Discovery...\n");
		
				coap_init_message(request, COAP_TYPE_CON, COAP_GET, 0);
				coap_set_header_uri_path(request, (const char *) &SERVER_REGISTRATION);
				const char mes[] = "irrigator";

				coap_set_payload(request, (uint8_t *)mes, sizeof(mes)-1);

				//printf("Actuator IP request: %s\n", (const char*) request->payload);
				COAP_BLOCKING_REQUEST(&server_ep, request, actuator_response_handler);

				if(actuator_assigned) coap_endpoint_parse(actuator_ip, strlen(actuator_ip), &actuator_ep);
			} else { 
		
				coap_init_message(request, COAP_TYPE_CON, COAP_GET, 0);
				coap_set_header_uri_path(request, (const char *) &IRRIGATION_ACTUATOR);
				const char msg[] = "irrigator";

				coap_set_payload(request, (uint8_t *)msg, sizeof(msg)-1);

				COAP_BLOCKING_REQUEST(&actuator_ep, request, check_response_handler);

				if(actuator_status) 
					humidity++;
								
				if(humidity > HUM_MAX)
					humidity = HUM_MAX;
					
				res_humidity.trigger(); //trigger the event to notify observers
				
				char mes[20];
				if(humidity <= HUM_MIN) strcpy(mes,"status=on");
				else if(humidity >= 70) strcpy(mes,"status=off");
				else strcpy(mes,"");
				
				if((strcmp(mes, "status=on") == 0 && !actuator_status) || (strcmp(mes, "status=off") == 0 && actuator_status)) {
					printf("Issuing irrigator command: %s\n", mes);
					
					coap_init_message(request, COAP_TYPE_CON, COAP_PUT, 0);
					coap_set_header_uri_path(request, "/irrigator");
					
					LOG_DBG("Toggling actuator %s\n", mes);

					coap_set_payload(request, (uint8_t *)mes, sizeof(mes)-1);

					COAP_BLOCKING_REQUEST(&actuator_ep, request, test_resp_handler);
				}
			}
			
		    etimer_set(&et, CLOCK_SECOND*3);
	    	}
    }

    PROCESS_END();
}
