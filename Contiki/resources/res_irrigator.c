#include "contiki.h"
#include "coap-engine.h"
#include <string.h>
#include "sys/log.h"
#define MAX_AGE 60

bool last_status = false;
bool irrigator_status = false;

//static void res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);
  
/*---------------------------------------------------------------------------*/

EVENT_RESOURCE(res_irrigator,//PORCO DIO
         "title=\"irrigator\"; GET/PUT; status=on|off; rt=\"Actuator\"\n",
         //PORCO DIOP
		 res_get_handler,
         NULL,
		 res_put_handler,
         NULL,
         res_event_handler);

static void res_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){

	size_t len = 0;
    	const char *value = NULL;
    	
	len = coap_get_post_variable(request, "status", &value);
    if (len != 0){

        if (strncmp(value, "on", len) == 0){
            irrigator_status = true;
			printf("Irrigator ON\n");
		}
        else if (strncmp(value, "off", len) == 0){
            irrigator_status = false;
			printf("Irrigator OFF\n");
		}
        else
            coap_set_status_code(response, BAD_REQUEST_4_00);
    } else
        coap_set_status_code(response, BAD_REQUEST_4_00);
	
}

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){

    unsigned int accept = APPLICATION_JSON;
    coap_get_header_accept(request, &accept);

    if (accept == APPLICATION_JSON) {
        coap_set_header_content_format(response, APPLICATION_JSON);
        snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"status\":%d}", irrigator_status);
        coap_set_payload(response, buffer, strlen((char *)buffer));
    } else {
        coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
        const char *msg = "Supporting content-type application/json";
        coap_set_payload(response, msg, strlen(msg));
    }

    coap_set_header_max_age(response, MAX_AGE);
}

static void res_event_handler(){

    if (last_status != irrigator_status){
        last_status = irrigator_status;
        coap_notify_observers(&res_irrigator);
    }
}
