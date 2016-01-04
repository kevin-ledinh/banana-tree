/************************************************************************************
 * Copyright (c) 2015, MpicoSys-Embedded Pico Systems                                * 			
 * All rights reserved.                                                              *
 *                                                                                   *
 * Redistribution and use in source and binary forms, with or without                *
 * modification, are permitted provided that the following conditions are met:       *
 *                                                                                   *
 * 1. Redistributions of source code must retain the above copyright notice, this    *
 *    list of conditions and the following disclaimer.                               *
 * 2. Redistributions in binary form must reproduce the above copyright notice,      *
 *    this list of conditions and the following disclaimer in the documentation      *
 *    and/or other materials provided with the distribution.                         *
 *                                                                                   *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND   *
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED     *
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE            *
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR   *
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES    *
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;      *
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND       *
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT        *
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS     *
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                      *
 *                                                                                   *
 * The views and conclusions contained in the software and documentation are those   *
 * of the authors and should not be interpreted as representing official policies,   *
 * either expressed or implied, of the FreeBSD Project.                              *
 *************************************************************************************/

/**
 * @file     TCM_api.c
 * @brief    TCM API source file
 * @date     27-06-2013
 * @author   adam.borkowski
 *
 */

#include "TCM_api.h"
#include "nrf_delay.h"
#include "dev__tcm__gpio.h"
#include "dev__tcm__spi.h"

static uint8_t display_update[]  = {0x24, 0x01, 0x00, 0x00, 0x00};
static uint8_t CMD_GetDeviceInfo[]  = {0x30, 0x01, 0x01, 0x00};
static uint8_t CMD_GetDeviceId[]  = {0x30, 0x02, 0x01, 0x14};
static 				uint8_t tcm_answer[200] ={0};

/**
 * @brief Initialise the connections to the TCM module
 * @return true or false
 */
bool TCM__init( void )
{
	dev__tcm__gpio__init();
	dev__tcm__spi__init();
	
	return true;
}

/**
 * @brief Get Device Info
 * @return none
 */
void TCM__GetDeviceInfo( void )
{
	uint8_t reply[64] = "";
	TCM_enable();
	spi_send_recv(CMD_GetDeviceInfo , reply , sizeof(CMD_GetDeviceInfo) , sizeof(CMD_GetDeviceInfo));
	spi_send_recv(reply , tcm_answer , 64 , 64);
	TCM_disable();
}

/**
 * @brief Function that displays the image on the device TCM
 * @return TCM response
 */
uint8_t TCM_DisplayUpdate(void)
{  										
	uint8_t i;

	while( dev__tcm__gpio__is_busy() );
	
	for(i=0;i<25;i++){}
	
	//Send display commands
	spi_send_recv(display_update , tcm_answer , 5 , 5);

		//TODO: check SPI txrx error
		
	return tcm_answer[0];
}

/**
 * @breif Upload display image
 * @param upload_image_ptr Image pointer
 * @param image_size Image size
 * @return TCM response
 */
uint8_t TCM_ImageUpload(uint8_t *upload_image_ptr, uint8_t image_size)
{
	uint8_t i;

	while( dev__tcm__gpio__is_busy() );//busy

	for(i=0;i<25;i++){}
	
	//Send image block
	spi_send_recv(upload_image_ptr , tcm_answer , image_size , image_size);

		//TODO: check SPI txrx error
		
	return tcm_answer[0];
}

/**
 * @brieg Get TCM response
 * @return
 */
uint8_t TCM_GetAnswer(void)
{
	uint8_t i;
	uint8_t nonetable[3]={0,0,0};

	while( dev__tcm__gpio__is_busy() );
	
	tcm_answer[0]=0x0;
	
	for(i=0;i<25;i++){}
		
	spi_send_recv(nonetable , tcm_answer , 2 , 2);
		
		//TODO: check SPI txrx error
		
	return tcm_answer[0];
}

/**
 * @brief Enable the TCM device
 */
void TCM_enable(void)
{
		dev__tcm__gpio__set_enable_pin_state(false);
		(void)nrf_delay_ms(40);
}

/**
 * @brief Disable the TCM device
 */
void TCM_disable(void)
{
		(void)nrf_delay_ms(20);
		dev__tcm__gpio__set_enable_pin_state(true);
}

/* ***(C) COPYRIGHT Embedded Pico Systems 2015***   ***END OF FILE***   */
