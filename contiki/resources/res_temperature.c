#include "contiki.h"
#include "coap-engine.h"
#include "sys/log.h"

#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define LOG_MODULE "Temperature sensor"
#define LOG_LEVEL LOG_LEVEL_INFO
#define MAX_AGE 60

static void res_get_handler(coap_message_t *, coap_message_t *, uint8_t *, uint16_t, int32_t *);
static void res_event_handler();

float temperature = 25;
float last_temperature = 25;

EVENT_RESOURCE(
    res_temperature,
    "title=\"temperature sensor\"; GET; rt=\"Sensor\"; obs\n",
    res_get_handler, NULL, NULL, NULL, res_event_handler);

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){

    unsigned int accept = APPLICATION_JSON;
    coap_get_header_accept(request, &accept);

    if (accept == APPLICATION_JSON) {
        coap_set_header_content_format(response, APPLICATION_JSON);
        snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"temperature\":%.1f}", temperature);
        coap_set_payload(response, buffer, strlen((char *)buffer));
    } else {
        coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
        const char *msg = "Supporting content-type application/json";
        coap_set_payload(response, msg, strlen(msg));
    }

    coap_set_header_max_age(response, MAX_AGE);
}

static void res_event_handler(){

    if (last_temperature != temperature){
        last_temperature = temperature;
        coap_notify_observers(&res_temperature);
    }
}
