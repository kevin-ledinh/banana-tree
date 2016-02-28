/*============================================================================
@brief A C source for TCM SPI device layer
------------------------------------------------------------------------------
<!-- Written by Kevin Le Dinh -->
<!-- Copyright (C) 2015 All rights reserved -->
============================================================================*/

/*----------------------------------------------------------------------------
  @brief
----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------
  include files
----------------------------------------------------------------------------*/
#include <stdbool.h>
#include "dev__tcm__spi.h"
#include "app_error.h"
#include "app_util_platform.h"
#include "nrf_delay.h"
#include "bsp.h"
#include "app_timer.h"
#include "nrf_drv_spi.h"
#include "nordic_common.h"

/*----------------------------------------------------------------------------
  manifest constants
----------------------------------------------------------------------------*/
#if (SPI0_ENABLED == 1)
    static const nrf_drv_spi_t m_spi_master = NRF_DRV_SPI_INSTANCE(0);
#elif (SPI1_ENABLED == 1)
    static const nrf_drv_spi_t m_spi_master = NRF_DRV_SPI_INSTANCE(1);
#elif (SPI2_ENABLED == 1)
    static const nrf_drv_spi_t m_spi_master = NRF_DRV_SPI_INSTANCE(2);
#else
    #error "No SPI enabled."
#endif

#define DELAY_MS                 1                /**< Timer Delay in milli-seconds. */
/*----------------------------------------------------------------------------
  type definitions
----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------
  macros
----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------
  prototypes
----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------
  global variables
----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------
  static variables
----------------------------------------------------------------------------*/

static nrf_drv_spi_config_t const config =
{
		.sck_pin  = SPIM0_SCK_PIN,
		.mosi_pin = SPIM0_MOSI_PIN,
		.miso_pin = SPIM0_MISO_PIN,
		.ss_pin   = SPIM0_SS_PIN,
		.irq_priority = APP_IRQ_PRIORITY_LOW,
		.orc          = 0xCC,
		.frequency    = NRF_DRV_SPI_FREQ_2M,
		.mode         = NRF_DRV_SPI_MODE_3,
		.bit_order    = NRF_DRV_SPI_BIT_ORDER_MSB_FIRST,
};
/*----------------------------------------------------------------------------
  public functions
----------------------------------------------------------------------------*/

/*============================================================================
@brief
------------------------------------------------------------------------------
@note
============================================================================*/
void dev__tcm__spi__init( void )
{
    ret_code_t err_code = nrf_drv_spi_init(&m_spi_master, &config, NULL);
    APP_ERROR_CHECK(err_code);
}



/**@brief Functions prepares buffers and starts data transfer.
 *
 * @param[in] p_tx_data     A pointer to a buffer TX.
 * @param[in] p_rx_data     A pointer to a buffer RX.
 * @param[in] tx_len        A length of the tx data buffers.
 * @param[in] rx_len        A length of the rx data buffers.
 */
uint32_t spi_send_recv(uint8_t * const p_tx_data,
                          uint8_t * const p_rx_data,
                          const uint16_t  tx_len,
                          const uint16_t  rx_len)
{
    // Start transfer.
    uint32_t err_code = nrf_drv_spi_transfer(&m_spi_master,
        p_tx_data, tx_len, p_rx_data, rx_len);
    APP_ERROR_CHECK(err_code);
    nrf_delay_ms(DELAY_MS);
	return err_code;
}
/*----------------------------------------------------------------------------
  private functions
----------------------------------------------------------------------------*/




/*----------------------------------------------------------------------------
  End of file
----------------------------------------------------------------------------*/
