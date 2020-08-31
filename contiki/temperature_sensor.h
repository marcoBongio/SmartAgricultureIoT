#include "coap-blocking-api.h"
#include "coap-engine.h"
#include "contiki-net.h"
#include "contiki.h"
#include "dev/button-hal.h"
#include "dev/leds.h"
#include "sys/log.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define LOG_MODULE "Humidity node"
#define LOG_LEVEL LOG_LEVEL_DBG
#define SERVER_EP "coap://[fd00::1]:5683"
#define SERVER_REGISTRATION "/registration"
#define OPEN_WINDOW_ACTUATOR "/window"

#define TEM_MAX 40
#define TEM_MIN 10

extern coap_resource_t res_temperature;
//extern coap_resource_t res_humidifier;
extern float temperature;
