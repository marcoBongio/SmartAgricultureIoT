#include "contiki.h"
#include "coap-engine.h"
#include <string.h>
#include "sys/log.h"
#define MAX_AGE 60

bool last_status = false;
bool window_status = false;

static void res_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
  
/*---------------------------------------------------------------------------*/

EVENT_RESOURCE(res_window,
         "title=\"window\"; GET/PUT; status=on|off; rt=\"Actuator\"\n",
		 res_get_handler,
         NULL,
		 res_put_handler,
         NULL,
         NULL);

static void res_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
	size_t len = 0;
	const char *value = NULL;

	len = coap_get_post_variable(request, "status", &value);
	
	if (len != 0){

		if (strncmp(value, "on", len) == 0){
			if(!window_status) { //if already "on" don't do anything
				window_status = true;
				printf("window ON\n");
			        coap_notify_observers(&res_window);
			}
		}
		else if (strncmp(value, "off", len) == 0) {
			if(window_status) {
				window_status = false;
				printf("window OFF\n");
			        coap_notify_observers(&res_window);
			}
		}
		else coap_set_status_code(response, BAD_REQUEST_4_00);
	} else coap_set_status_code(response, BAD_REQUEST_4_00);	
}

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){

    unsigned int accept = APPLICATION_JSON;
    coap_get_header_accept(request, &accept);

    if (accept == APPLICATION_JSON) {
        coap_set_header_content_format(response, APPLICATION_JSON);
        snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"status\":%d}", window_status);
        coap_set_payload(response, buffer, strlen((char *)buffer));
    } else {
        coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
        const char *msg = "Supporting content-type application/json";
        coap_set_payload(response, msg, strlen(msg));
    }

    coap_set_header_max_age(response, MAX_AGE);
}

