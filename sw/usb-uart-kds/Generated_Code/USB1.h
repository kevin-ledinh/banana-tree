/* ###################################################################
**     This component module is generated by Processor Expert. Do not modify it.
**     Filename    : USB1.h
**     Project     : usb-uart-kds
**     Processor   : MKL25Z128VLK4
**     Component   : FSL_USB_Stack
**     Version     : Component 01.041, Driver 01.00, CPU db: 3.00.000
**     Repository  : My Components
**     Compiler    : GNU C Compiler
**     Date/Time   : 2015-11-14, 09:33, # CodeGen: 1
**     Abstract    :
**         This component implements a wrapper to the FSL USB Stack.
**     Settings    :
**          Component name                                 : USB1
**          Freescale USB Stack Version                    : v4.1.1
**          Init USB Function                              : USB0_Init
**          Inherited USB Init                             : Enabled
**            USB Init                                     : Init_USB_OTG_VAR0
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
** @file USB1.h
** @version 01.00
** @brief
**         This component implements a wrapper to the FSL USB Stack.
*/         
/*!
**  @addtogroup USB1_module USB1 module documentation
**  @{
*/         

#ifndef __USB1_H
#define __USB1_H

/* MODULE USB1. */

/* Include shared modules, which are used for whole project */
#include "PE_Types.h"
#include "PE_Error.h"
#include "PE_Const.h"
#include "IO_Map.h"
/* Include inherited beans */
#include "USB0.h"
#include "CDC1.h"
#include <stddef.h> /* for size_t */

/* Interfaces/wrappers to the CDC device class, needed by FSShell: */
#define USB1_SendString(str)   CDC1_SendString(str)
#define USB1_RecvChar(chr)     CDC1_GetChar(chr)
#define USB1_SendChar(chr)     CDC1_SendChar(chr)
#define USB1_GetCharsInRxBuf() CDC1_GetCharsInRxBuf()

#include "Cpu.h"


#ifndef __BWUserType_USB1_TComData
#define __BWUserType_USB1_TComData
  typedef uint8_t USB1_TComData ;      /* User type for communication data type. */
#endif

/*
   DATA_BUFF_SIZE should be greater than or equal to the endpoint buffer size,
   otherwise there will be data loss. For MC9S08JS16, maximum DATA_BUFF_SIZE
   supported is 16 Bytes
*/
#define USB1_DATA_BUFF_SIZE    64 /* data buffer size as specified in the properties */

#define USB1_USB_ERR_SEND            1  /* Error while sending */
#define USB1_USB_ERR_BUSOFF          2  /* Bus not ready */
#define USB1_USB_ERR_INIT            3  /* USB initialization error */
#define USB1_USB_ERR_TX_CHAR         4  /* Error sending character */
#define USB1_USB_ERR_TX_STRING       5  /* Error sending string */
#define USB1_USB_ERR_CHECKED_TXFULL  6  /* Error during sending a checked block */
#define USB1_USB_ERR_RECEIVE         7  /* Error while starting a receive transaction */
#define USB1_USB_ERR_DEINIT          8  /* USB deinitialization error */

uint8_t USB1_Init(void);
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

void USB1_usb_int_dis(void);
/*
** ===================================================================
**     Method      :  USB1_usb_int_dis (component FSL_USB_Stack)
**
**     Description :
**         Disables USB interrupts (if supported)
**         This method is internal. It is used by Processor Expert only.
** ===================================================================
*/

void USB1_usb_int_en(void);
/*
** ===================================================================
**     Method      :  USB1_usb_int_en (component FSL_USB_Stack)
**
**     Description :
**         Enables USB interrupts (if supported).
**         This method is internal. It is used by Processor Expert only.
** ===================================================================
*/

uint8_t USB1_Deinit(void);
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

/* END USB1. */

#endif
/* ifndef __USB1_H */
/*!
** @}
*/
/*
** ###################################################################
**
**     This file was created by Processor Expert 10.5 [05.21]
**     for the Freescale Kinetis series of microcontrollers.
**
** ###################################################################
*/
