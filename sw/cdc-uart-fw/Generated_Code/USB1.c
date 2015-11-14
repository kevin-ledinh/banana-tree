/* ###################################################################
**     This component module is generated by Processor Expert. Do not modify it.
**     Filename    : USB1.c
**     Project     : cdc-uart-fw
**     Processor   : MKL25Z128VLK4
**     Component   : FSL_USB_Stack
**     Version     : Component 01.041, Driver 01.00, CPU db: 3.00.000
**     Compiler    : GNU C Compiler
**     Date/Time   : 2015-11-14, 00:21, # CodeGen: 1
**     Abstract    :
**         This component implements a wrapper to the FSL USB Stack.
**     Settings    :
**          Component name                                 : USB1
**          Freescale USB Stack Version                    : v4.1.1
**          USB Init                                       : Init_USB_OTG_VAR0
**          Device Class                                   : CDC Device
**          CDC Device                                     : Enabled
**            CDCDevice                                    : FSL_USB_CDC_Device
**          CDC Host                                       : Disabled
**          HID Keyboard Device                            : Disabled
**          HID Mouse Device                               : Disabled
**          MSD Host                                       : Disabled
**          DATA_BUFF_SIZE                                 : 64
**          Initialization                                 : 
**            Use USB Stack Inititalization                : yes
**            Call Init Method                             : yes
**     Contents    :
**         Deinit - uint8_t USB1_Deinit(void);
**         Init   - uint8_t USB1_Init(void);
**
**     (c) Copyright Freescale, all rights reserved, 2013-2015.
**     Ported as Processor Expert component: Erich Styger
**     http: www.mcuoneclipse.com
** ###################################################################*/
/*!
** @file USB1.c
** @version 01.00
** @brief
**         This component implements a wrapper to the FSL USB Stack.
*/         
/*!
**  @addtogroup USB1_module USB1 module documentation
**  @{
*/         

/* MODULE USB1. */

#include "USB1.h"
#include "derivative.h"     /* include peripheral declarations */
#include "types.h"          /* Contains User Defined Data Types */

/*
** ===================================================================
**     Method      :  USB1_usb_int_dis (component FSL_USB_Stack)
**
**     Description :
**         Disables USB interrupts (if supported)
**         This method is internal. It is used by Processor Expert only.
** ===================================================================
*/
void USB1_usb_int_dis(void)
{
  /* Kinetis L2K */
  NVIC_ICER = (1<<24);    /* Disable interrupts from USB module (Interrupt Clear-Enable Register) */
}

/*
** ===================================================================
**     Method      :  USB1_usb_int_en (component FSL_USB_Stack)
**
**     Description :
**         Enables USB interrupts (if supported).
**         This method is internal. It is used by Processor Expert only.
** ===================================================================
*/
void USB1_usb_int_en(void)
{
  /* Kinetis L2K */
  NVIC_ICPR = (1<<24);    /* Clear any pending interrupts on USB (Interrupt Clear-Pending Register) */
  NVIC_ISER = (1<<24);    /* Enable interrupts from USB module (Interrupt Set-Enable Register) */
}

/*
** ===================================================================
**     Method      :  USB1_Deinit (component FSL_USB_Stack)
**     Description :
**         Deinitializes the driver
**     Parameters  : None
**     Returns     :
**         ---             - Error code
** ===================================================================
*/
uint8_t USB1_Deinit(void)
{
  uint8_t err;

  USB1_usb_int_dis(); /* disable USB interrupts */
  /* Initialize the USB interface */
  err = CDC1_Deinit();
  if(err != ERR_OK) {
    /* Error deinitializing USB Class */
    return ERR_FAILED;
  }
  return ERR_OK;
}

/*
** ===================================================================
**     Method      :  USB1_Init (component FSL_USB_Stack)
**     Description :
**         Initializes the driver
**     Parameters  : None
**     Returns     :
**         ---             - Error code
** ===================================================================
*/
uint8_t USB1_Init(void)
{
  uint8_t err;

  /* Initialize the USB interface */
  err = CDC1_Init();
  if(err != ERR_OK) {
    /* Error initializing USB Class */
    return ERR_FAILED;
  }
  USB1_usb_int_en();
  return ERR_OK;
}

/* END USB1. */

/*!
** @}
*/
/*
** ###################################################################
**
**     This file was created by Processor Expert 10.3 [05.09]
**     for the Freescale Kinetis series of microcontrollers.
**
** ###################################################################
*/
