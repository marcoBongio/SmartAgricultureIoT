#include "window_actuator.h"

static bool result = false;

extern coap_resource_t res_window;

/*---------------------------------------------------------------------------*/
PROCESS(window_actuator, "window Actuator");
AUTOSTART_PROCESSES(&window_actuator);

/*---------------------------------------------------------------------------*/
void response_handler(coap_message_t *response){
	if (response == NULL)
        return;
        
    LOG_DBG("Response %s\n", response->payload);
    if(strcmp((const char *)response->payload, "ACK") == 0)
    	result = true;
}

/*---------------------------------------------------------------------------*/
PROCESS_THREAD(window_actuator, ev, data){

	static coap_endpoint_t server_ep;
	static coap_message_t request[1];

	PROCESS_BEGIN();
	
	coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);
	
	do {
		coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);
		coap_set_header_uri_path(request, (const char *) &SERVER_REGISTRATION);
		
		const char mes[] = "{\"NodeType\":\"actuator\", \"NodeResource\":\"window\"}";
		printf("%s\n", mes);
		
		coap_set_payload(request, (uint8_t *)mes, sizeof(mes)-1);
		
		COAP_BLOCKING_REQUEST(&server_ep, request, response_handler);
        
    } while (!result);
	
	printf("Starting window Actuator\n");
	coap_activate_resource(&res_window, "window");

	PROCESS_END();
}
