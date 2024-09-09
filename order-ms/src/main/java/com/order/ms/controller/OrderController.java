package com.order.ms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.order.ms.dto.CustomerOrder;
import com.order.ms.dto.OrderEvent;
import com.order.ms.entity.OrderTable;
import com.order.ms.entity.OrderRepository;

@RestController
@RequestMapping("/api")
public class OrderController {

	@Autowired
	private OrderRepository repository;

	@Autowired
	private KafkaTemplate<String, OrderEvent> kafkaTemplate;

	@PostMapping("/orders")
	public void createOrder(@RequestBody CustomerOrder customerOrder) {
		OrderTable order = new OrderTable();
		order.setAmount(customerOrder.getAmount());
		order.setItem(customerOrder.getItem());
		order.setQuantity(customerOrder.getQuantity());
		order.setStatus("Created");
		try {
			order = repository.save(order);

			customerOrder.setOrderId(order.getId());

			OrderEvent orderevent = new OrderEvent();
			orderevent.setOrder(customerOrder);
			orderevent.setType("ORDER_CREATED");
			kafkaTemplate.send("new-orders", orderevent);
		} catch (Exception e) {
			order.setStatus("FAILED");
			repository.save(order);
		}
	}
}
