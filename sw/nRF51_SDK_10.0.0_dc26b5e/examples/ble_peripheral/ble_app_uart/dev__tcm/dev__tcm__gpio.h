/*============================================================================
@brief A C source header for TCM GPIO pins
------------------------------------------------------------------------------
<!-- Written by Kevin Le Dinh -->
<!-- Copyright (C) 2015 All rights reserved -->
============================================================================*/

#ifndef dev__tcm__gpio_h
#define dev__tcm__gpio_h

/*----------------------------------------------------------------------------
  @brief

----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------
  nested include files
----------------------------------------------------------------------------*/
#include <stdbool.h>
#include "stdint.h"

/*----------------------------------------------------------------------------
  macros
----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------
  manifest constants
----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------
  type definitions
----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------
  extern variables
----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------
  prototypes
----------------------------------------------------------------------------*/
void dev__tcm__gpio__init( void );

void dev__tcm__btn__init( void (*forward) (void) , void (*backward) (void) );

void dev__tcm__gpio__set_enable_pin_state( bool state );

bool dev__tcm__gpio__is_busy( void );

/*----------------------------------------------------------------------------
  compile time checks
----------------------------------------------------------------------------*/

#endif

/*----------------------------------------------------------------------------
  End of file
----------------------------------------------------------------------------*/
