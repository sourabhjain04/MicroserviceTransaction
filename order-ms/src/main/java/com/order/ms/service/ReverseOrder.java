package com.order.ms.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.ms.dto.OrderEvent;
import com.order.ms.entity.OrderTable;
import com.order.ms.entity.OrderRepository;

@Component
public class ReverseOrder {

	@Autowired
	private OrderRepository orderRepository;

	@KafkaListener(topics = "reversed-orders", groupId = "orders-group")
	public void reverseOrder(String event) {
		System.out.println(" reverse order event:: "+event);
		
		try {
			OrderEvent orderEvent = new ObjectMapper().readValue(event, OrderEvent.class);

			Optional<OrderTable> order = orderRepository.findById(orderEvent.getOrder().getOrderId());

			order.ifPresent(o -> {
				o.setStatus("FAILED");
				orderRepository.save(o);
			});
		} catch (Exception e) {
			System.out.println("Exception occured while reverting the order details");
		}
	}
}
