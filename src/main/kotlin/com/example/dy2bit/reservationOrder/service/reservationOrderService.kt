package com.example.dy2bit.reservationOrder.service

import com.example.dy2bit.model.ReservationOrder
import com.example.dy2bit.repository.ReservationOrderRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ReservationOrderService(
        private val reservationOrderRepository: ReservationOrderRepository,
) {

    @Transactional
    fun getReservationOrderList(): List<ReservationOrder> {
        return reservationOrderRepository.findByEndAtNotNull()
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun cancelReservationOrder(id: Long): ReservationOrder {
        val cancelReservation = reservationOrderRepository.findByIdOrNull(id)
        cancelReservation?.endAt = Instant.now()
        return reservationOrderRepository.save(cancelReservation!!)
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    suspend fun tradeReservationOrder(kimpPer: Float) = runBlocking {
        val targetBuyReservation = reservationOrderRepository.findByTargetKimpRateAndPositionAndEndAtNotNull(kimpPer, true)
        val targetSellReservation = reservationOrderRepository.findByTargetKimpRateAndPositionAndEndAtNotNull(kimpPer, false)
        if (targetBuyReservation.isNotEmpty()) {
            // TODO: 둘 중 하나만 체결되는 문제를 해결하기 위해 업비트, 바이낸스 잔고 조회 해서 두 거래소 모두 주문 가능한 수량인가 먼저 체크하는 로직 필요
            targetBuyReservation.map {
                async {
                    tradeUpbit(true, it.quantity)
                    tradeBinance(false, it.quantity)
                }.await()
            }
        }
        if (targetSellReservation.isNotEmpty()) {
            // TODO: 둘 중 하나만 체결되는 문제를 해결하기 위해 업비트, 바이낸스 잔고 조회 해서 두 거래소 모두 주문 가능한 수량인가 먼저 체크하는 로직 필요
            targetBuyReservation.map {
                async {
                    tradeUpbit(false, it.quantity)
                    tradeBinance(true, it.quantity)
                }.await()
            }
        }
    }

    // TODO: 업비트로 코인 매수, 매도 주문 요청 로직
    private fun tradeUpbit(position: Boolean, quantity: Float) {

    }

    // TODO: 바이낸스로 코인 매수, 매도 주문 요청 로직
    private fun tradeBinance(position: Boolean, quantity: Float) {

    }

}
