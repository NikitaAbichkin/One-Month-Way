package com.company.Day4.Practice;

import org.hibernate.query.NativeQuery;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.util.RecordUtil;

import java.util.*;

@RestController
@RequestMapping("/api/V1")
class Controller {
    Map<Long, Order> map = new HashMap<>();

    @PostMapping()
    public ResponseEntity<?> createOrder(@RequestBody OrderDto orderDto, @RequestHeader("Idempotency-Key") String idempotenceKey){

        if ( orderDto.getId() == null ||orderDto.getProductId() == null || orderDto.getQuantity()==null || orderDto.getStatus() == null){
            return   ResponseEntity.status(400).body("Ошибка: не все поля заполнены");
        }

        Order orderFromMap = map.get(orderDto.getId());
        String key;
        if (orderFromMap!= null&& (key = orderFromMap.getIdempotency_Key()) != null){
                if ( key.equals(idempotenceKey)){
                    return ResponseEntity.status(200).body(orderFromMap);   // повтор → тот же ответ
                }
            return  ResponseEntity.status(409).body("Заказ с таким ID уже есть ");
        }
        Order order = new Order();
        order.setId(orderDto.getId());
        order.setProductId(orderDto.getProductId());
        order.setQuantity(orderDto.getQuantity());
        order.setStatus(orderDto.getStatus());
        order.setIdempotency_Key(idempotenceKey);
        map.put(order.getId(), order);
        return  ResponseEntity.status(200).body(order);
    }
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable Long orderId){
        Order order = map.get(orderId);

        if (order == null){
            return  ResponseEntity.status(404).body("Нет такого ордера");
        }
        return  ResponseEntity.status(200).body(order);
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(@RequestParam int size, @RequestParam int page ){
        if (size>100 || page<0){
            return  ResponseEntity.status(400).body("Нет такого ордера");
        }
        // создали список из мап
        List<Order> orders =  map.values().stream()
                .sorted(Comparator.comparingLong(n->n.getId())).toList();

        int from = Math.min(size*page, orders.size());
        int to = Math.min(from+size, orders.size());
        orders = orders.subList(from, to);
        return ResponseEntity.status(200).body(orders);
    }

    @PatchMapping("/orders/")
    public ResponseEntity<?> changeOrder(@RequestBody OrderDto orderDto){

        if ( orderDto.getId() == null ||orderDto.getProductId() == null || orderDto.getQuantity()==null || orderDto.getStatus() == null){
            return   ResponseEntity.status(400).body("Ошибка: не все поля заполнены");
        }
        if ( orderDto.getStatus() == Status.CANCELLED &&  map.get(orderDto.getId()).getStatus() == Status.CANCELLED) {
            return ResponseEntity.status(409).body("заказ уже CANCELLED");
        }
        Order order = new Order();
        order.setId(orderDto.getId());
        order.setProductId(orderDto.getProductId());
        order.setQuantity(orderDto.getQuantity());
        order.setStatus(orderDto.getStatus());
        order.setIdempotency_Key(orderDto.getIdempotency_Key());
        map.put(orderDto.getId(),order);

        return  ResponseEntity.status(200).body("Успешно заменили");
    }
    @DeleteMapping("/orders/{id}")
    public  ResponseEntity<?> deleteOrder(@PathVariable long id ){
        map.remove(id);
        return  ResponseEntity.status(204).body("Успешно delete with id " + id);
    }
}
