/*============================================================================
@brief A C source for TCM board GPIO
------------------------------------------------------------------------------
<!-- Written by Kevin Le Dinh -->
<!-- Copyright (C) 2013=5 All rights reserved -->
============================================================================*/

/*----------------------------------------------------------------------------
  @brief
----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------
  include files
----------------------------------------------------------------------------*/
#include "dev__tcm__gpio.h"
#include "nrf.h"
#include "nrf_drv_gpiote.h"
#include "app_error.h"
#include "boards.h"

/*----------------------------------------------------------------------------
  manifest constants
----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------
  type definitions
----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------
  macros
----------------------------------------------------------------------------*/
#define TCM_nBUSY 	ARDUINO_A4_PIN  // INPUT P0.05
#define TCM_nEN 		ARDUINO_A5_PIN  // OUTPUT P0.06

/*----------------------------------------------------------------------------
  prototypes
----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------
  global variables
----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------
  static variables
----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------
  public functions
----------------------------------------------------------------------------*/

/*============================================================================
@brief	Initialise GPIO pins to the TCM
------------------------------------------------------------------------------
@note
============================================================================*/
void dev__tcm__gpio__init( void )
{
    ret_code_t err_code;

    err_code = nrf_drv_gpiote_init();
    APP_ERROR_CHECK(err_code);
    
    nrf_drv_gpiote_out_config_t out_config = GPIOTE_CONFIG_OUT_SIMPLE(true);

    err_code = nrf_drv_gpiote_out_init(TCM_nEN, &out_config);
    APP_ERROR_CHECK(err_code);

    nrf_drv_gpiote_in_config_t in_config = GPIOTE_CONFIG_IN_SENSE_TOGGLE(false);
    in_config.pull = NRF_GPIO_PIN_NOPULL;

    err_code = nrf_drv_gpiote_in_init(TCM_nBUSY, &in_config, NULL);
    APP_ERROR_CHECK(err_code);

    // nrf_drv_gpiote_in_event_enable(TCM_nBUSY, true);
}

/*============================================================================
@brief	Set the ENABLE pin state
------------------------------------------------------------------------------
@note
============================================================================*/
void dev__tcm__gpio__set_enable_pin_state( bool state )
{
	if( state == true )  // set pin high
	{
		nrf_drv_gpiote_out_set(TCM_nEN);
	}
	else			// set pin low
	{
		nrf_drv_gpiote_out_clear(TCM_nEN);
	}
}


/*============================================================================
@brief	Check if the TCM board is busy
------------------------------------------------------------------------------
@note
============================================================================*/
bool dev__tcm__gpio__is_busy( void )
{
	return (!nrf_drv_gpiote_in_is_set(TCM_nBUSY));
}
/*----------------------------------------------------------------------------
  private functions
----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------
  End of file
----------------------------------------------------------------------------*/
