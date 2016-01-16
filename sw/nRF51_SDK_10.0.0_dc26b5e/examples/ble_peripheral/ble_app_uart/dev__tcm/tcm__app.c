/*============================================================================
@brief A C source for tcm application layer
------------------------------------------------------------------------------
<!-- Written by Kevin Le Dinh -->
<!-- Copyright (C) 2016 All rights reserved -->
============================================================================*/

/*----------------------------------------------------------------------------
  @brief
----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------
  include files
----------------------------------------------------------------------------*/
#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include "tcm__app.h"
#include "TCM_api.h"
#include "ble_nus.h"
#include "dev__tcm__gpio.h"
#include "nrf_delay.h"

/*----------------------------------------------------------------------------
  manifest constants
----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------
  type definitions
----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------
  macros
----------------------------------------------------------------------------*/
#define EPD_FILE_SIZE_441               15016

/*----------------------------------------------------------------------------
  prototypes
----------------------------------------------------------------------------*/
static void tcm__app_process_ble_data( uint8_t * data , uint8_t size );
static void tcm__app_send_image(uint8_t * data , uint8_t size);
static void tcm__app_send_image_done_handler(ble_nus_t * p_nus);
static void tcm__app_btn_forward( void );
static void tcm__app_btn_backward( void );

/*----------------------------------------------------------------------------
  global variables
----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------
  static variables
----------------------------------------------------------------------------*/
static tcm__app_t tcm__app;
static uint8_t tcm__app__msg_ack[5] = { 0x3E , 0x3E , 0x01 , 0x00 , 0x00 };
static uint8_t tcm__app__msg_forward[5] = { 0x3E , 0x3E , 0x03 , 0x00 , 0x00 };
static uint8_t tcm__app__msg_backward[5] = { 0x3E , 0x3E , 0x04 , 0x00 , 0x00 };
static uint16_t epd_file_size = EPD_FILE_SIZE_441;
static uint8_t upload_image[255] = { 0x20, 0x01, 0x00, 128 };
static char * reply_tx_img_done = "done";
static ble_nus_t * tcm__app_nus_ptr = NULL;
/*----------------------------------------------------------------------------
  public functions
----------------------------------------------------------------------------*/

/*============================================================================
@brief
------------------------------------------------------------------------------
@note
============================================================================*/
void tcm__app_init( ble_nus_t * p_nus )
{
	//init SPI here
    //Initialise TCM board
    TCM__init();

    // return the TCM board's manufacture ID
    TCM__GetDeviceInfo();
    
    dev__tcm__btn__init( tcm__app_btn_forward , tcm__app_btn_backward );
    tcm__app_nus_ptr = p_nus;
	tcm__app.tcm__msg.tcm__msg_type = MSG_TYPE_INVALID;
	tcm__app.tcm__msg.payload = 0;
	tcm__app.tcm__app_event = TCM_EVENT_WAIT_FOR_CMD;
    tcm__app.img_data_size = EPD_FILE_SIZE_441;
}


/*============================================================================
@brief FSM for the TCM application
------------------------------------------------------------------------------
@note
============================================================================*/
void tcm__app_run( uint8_t * data , uint8_t size )
{
    tcm__app_process_ble_data(data , size);
	switch( tcm__app.tcm__app_event )
	{
		case TCM_EVENT_WAIT_FOR_CMD:
			break;
		case TCM_EVENT_WAIT_FOR_IMG:
            tcm__app.tcm__app_event = TCM_EVENT_TX_IMAGE;
            TCM_enable();
			break;
		case TCM_EVENT_TX_IMAGE:
            tcm__app_send_image(data , size);
            break;
		case TCM_EVENT_FINISH_TX_IMAGE:
            tcm__app_send_image_done_handler(tcm__app_nus_ptr);
			break;
		default:
			tcm__app.tcm__app_event = TCM_EVENT_WAIT_FOR_CMD;
			tcm__app.tcm__msg.tcm__msg_type = MSG_TYPE_INVALID;
			tcm__app.tcm__msg.payload = 0;
			break;
	}
}
/*============================================================================
@brief send forward button press
------------------------------------------------------------------------------
@note
============================================================================*/
void tcm__app_btn_forward( void )
{
    printf("MSG_TYPE_FORWARD\r\n");
    
    ble_nus_string_send(tcm__app_nus_ptr , tcm__app__msg_forward , sizeof(tcm__app__msg_forward));

}

/*============================================================================
@brief send backward button press
------------------------------------------------------------------------------
@note
============================================================================*/
void tcm__app_btn_backward( void )
{
    printf("MSG_TYPE_BACKWARD\r\n");
    
    ble_nus_string_send(tcm__app_nus_ptr , tcm__app__msg_backward , sizeof(tcm__app__msg_backward));

}

/*----------------------------------------------------------------------------
  private functions
----------------------------------------------------------------------------*/

/*============================================================================
@brief
------------------------------------------------------------------------------
@note
============================================================================*/
static void tcm__app_process_ble_data( uint8_t * data , uint8_t size )
{
	if(size <= 20)
	{
		uint16_t msg_start = ( data[1] << 8 ) | data[0];
		if( msg_start == 0x3E3E )
		{
			switch( (tcm__msg_type_t) data[2] )
			{
				case MSG_TYPE_TX_IMAGE:
                    printf("MSG_TYPE_TX_IMAGE\r\n");
					tcm__app.tcm__msg.tcm__msg_type = MSG_TYPE_TX_IMAGE;
					tcm__app.tcm__app_event = TCM_EVENT_WAIT_FOR_IMG;
					break;
				case MSG_FINISH_TX_IMAGE:
                    printf("MSG_FINISH_TX_IMAGE\r\n");
					tcm__app.tcm__msg.tcm__msg_type = MSG_FINISH_TX_IMAGE;
					tcm__app.tcm__app_event = TCM_EVENT_FINISH_TX_IMAGE;
					break;
				case MSG_TYPE_ACK:
                    printf("MSG_TYPE_ACK\r\n");
					tcm__app.tcm__msg.tcm__msg_type = MSG_TYPE_ACK;
					tcm__app.tcm__app_event = TCM_EVENT_WAIT_FOR_CMD;
					break;
				default:
                    // silently ignore
					break;
			}
        }
	}
}

/*============================================================================
@brief
------------------------------------------------------------------------------
@note
============================================================================*/
static void tcm__app_send_image(uint8_t * data , uint8_t size)
{

    uint8_t tcm_receive;
    
    for (uint32_t i = 0; i < size; i++)
    {
        upload_image[i + 4] = data[i];
    }
    //printf("tcm__app.img_data_size = %d - size: %d\r\n", tcm__app.img_data_size , size);
    tcm__app.img_data_size -= size;
    upload_image[3] = size;
    tcm_receive = TCM_ImageUpload(upload_image, size+4);
}

/*============================================================================
@brief
------------------------------------------------------------------------------
@note
============================================================================*/
static void tcm__app_send_image_done_handler(ble_nus_t * p_nus)
{
    uint8_t tcm_receive;
    tcm__app.img_data_size = EPD_FILE_SIZE_441;
    TCM_DisplayUpdate();
    tcm__app.tcm__msg.tcm__msg_type = MSG_TYPE_INVALID;
    tcm__app.tcm__msg.payload = 0;
    tcm__app.tcm__app_event = TCM_EVENT_WAIT_FOR_CMD;
    ble_nus_string_send(p_nus , tcm__app__msg_ack , sizeof(tcm__app__msg_ack));
    
    (void)nrf_delay_ms(1);
    checkBusy(); 			// Check Busy pin to low
    (void)nrf_delay_ms(1);
    checkBusytoHigh();		// Check Busy pin to 
    (void)nrf_delay_ms(1);
    tcm_receive = TCM_GetAnswer();
    	
	TCM_disable();
}
/*----------------------------------------------------------------------------
  End of file
----------------------------------------------------------------------------*/
