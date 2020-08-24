#include "res_humidity.h"

float humidity = -1;
float last_humidity = -1;

EVENT_RESOURCE(
    res_humidity,
    "title=\"Humidity sensor\"; GET; rt=\"Sensor\"; obs\n",
    res_get_handler, NULL, NULL, NULL, res_event_handler);

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){

    unsigned int accept = APPLICATION_JSON;
    coap_get_header_accept(request, &accept);

    if (accept == APPLICATION_JSON) {
        coap_set_header_content_format(response, APPLICATION_JSON);
        snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"humidity\":%.1f}", humidity);
        coap_set_payload(response, buffer, strlen((char *)buffer));
    } else {
        coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
        const char *msg = "Supporting content-type application/json";
        coap_set_payload(response, msg, strlen(msg));
    }

    coap_set_header_max_age(response, MAX_AGE);
}

static void res_event_handler(){

    if (last_humidity != humidity){
        last_humidity = humidity;
        coap_notify_observers(&res_humidity);
    }
}