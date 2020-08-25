#include "contiki.h"
#include "coap-engine.h"
#include "sys/log.h"

#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define LOG_MODULE "Humidity actuator"
#define LOG_LEVEL LOG_LEVEL_DBG
#define MAX_AGE 60
#define HUM_MAX 100
#define HUM_MIN 0

static void res_get_handler(coap_message_t *, coap_message_t *, uint8_t *, uint16_t, int32_t *);
static void res_post_put_handler(coap_message_t *, coap_message_t *, uint8_t *, uint16_t, int32_t *);

extern float humidity;

uint8_t humidifier_status = 0;
uint8_t humidifier_value = 30; // Valore di default

RESOURCE(res_irrigator,
         "title=\"Humidifier actuator\"; GET/PUT/POST; status=on|off&target=<value>; rt=\"Actuator\"\n",
         res_get_handler, res_post_put_handler, res_post_put_handler, NULL);

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){

    unsigned int accept = APPLICATION_JSON;
    coap_get_header_accept(request, &accept);

    if (accept == APPLICATION_JSON){
        coap_set_header_content_format(response, APPLICATION_JSON);

        if (!humidifier_status)
            snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"status\":\"off\"}");
        else
            snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"status\":\"on\", \"target_humidity\":%d}", humidifier_value);

        coap_set_payload(response, buffer, strlen((char *)buffer));
    } else {
        coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
        const char *msg = "Supporting content-type application/json";
        coap_set_payload(response, msg, strlen(msg));
    }

    coap_set_header_max_age(response, MAX_AGE);    
}

static void res_post_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){

    size_t len = 0;
    const char *value = NULL;

    len = coap_get_post_variable(request, "status", &value);

    if (len != 0){

        if (strncmp(value, "on", len) == 0)
            humidifier_status = 1;
        else if (strncmp(value, "off", len) == 0)
            humidifier_status = 0;
        else
            coap_set_status_code(response, BAD_REQUEST_4_00);
    } else
        coap_set_status_code(response, BAD_REQUEST_4_00);

    if (humidifier_status){

        len = coap_get_post_variable(request, "value", &value);

        if (len != 0){         
            uint8_t precheck = atoi(value);

            if (precheck > HUM_MAX)
                irrigator_value = HUM_MAX;
            else if (precheck < HUM_MIN)
                irrigator_value = HUM_MIN;
            else 
                irrigator_value = precheck;
        } else
            coap_set_status_code(response, BAD_REQUEST_4_00);
    }
}