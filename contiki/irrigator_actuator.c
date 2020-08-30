#include "irrigator_actuator.h"

//static struct etimer e_timer;

static bool result = false;

extern coap_resource_t res_irrigator;

/*---------------------------------------------------------------------------*/
PROCESS(irrigator_actuator, "Irrigator Actuator");
AUTOSTART_PROCESSES(&irrigator_actuator);

/*---------------------------------------------------------------------------*/
void response_handler(coap_message_t *response){
	if (response == NULL)
        return;
        
    LOG_DBG("Response %s\n", response->payload);
    if(strcmp((const char *)response->payload, "ACK") == 0)
    	result = true;
}

/*---------------------------------------------------------------------------*/
PROCESS_THREAD(irrigator_actuator, ev, data){

	static coap_endpoint_t server_ep;
	static coap_message_t request[1];

	PROCESS_BEGIN();
	
	coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);
	
	do {
        coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);
        coap_set_header_uri_path(request, (const char *) &SERVER_REGISTRATION);
        
        const char mes[] = "{\"NodeType\":\"actuator\", \"NodeResource\":\"irrigator\"}";
        printf("%s\n", mes);
        
        coap_set_payload(request, (uint8_t *)mes, sizeof(mes)-1);
        
        COAP_BLOCKING_REQUEST(&server_ep, request, response_handler);
        
    } while (!result);
	
	printf("Starting irrigator Actuator\n");
	coap_activate_resource(&res_irrigator, "irrigator");
	
	//notify status the first time
	//res_irrigator.trigger();
	
	/*etimer_set(&e_timer, CLOCK_SECOND);

	while(1) {
		
		PROCESS_WAIT_EVENT();
	
		if (etimer_expired(&e_timer))
				res_irrigator.trigger();

			etimer_set(&e_timer, CLOCK_SECOND);
	}*/

	PROCESS_END();
}
