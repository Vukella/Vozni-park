package com.example.vozni_park.event;

/**
 * Published by TravelOrderService.completeTravelOrder() after the travel order
 * is successfully saved with COMPLETED status.
 *
 * Consumed by TravelOrderSnapshotService via @TransactionalEventListener
 * (phase = AFTER_COMMIT) to build the travel_order_report_snapshot row.
 */
public record TravelOrderClosedEvent(Long travelOrderId) {
}
