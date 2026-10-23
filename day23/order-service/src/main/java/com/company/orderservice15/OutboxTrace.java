package com.company.orderservice15;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

// переносит контекст трейса через outbox: сохраняем при записи, восстанавливаем при отправке в Kafka
@Component
public class OutboxTrace {
    private static final String TRACEPARENT = "traceparent";

    // текущий трейсер
    private final Tracer tracer;


    //│  кладёт контекст спана в текст (inject) и достаёт обратно (extract)
    private final Propagator propagator;

    public OutboxTrace(Tracer tracer, Propagator propagator) {
        this.tracer = tracer;
        this.propagator = propagator;
    }

    // W3C traceparent текущего спана или null, если трейса нет

    public String currentTraceparent() {
        // получил текущий спан
        Span currentSpan  = tracer.currentSpan();
        if (currentSpan == null) return null;
        Map<String,String> map = new HashMap<>();
        //передали контекст и значения записались в наш мап
        propagator.inject(currentSpan.context(),  map  ,  Map<String,String>::put);
        return map.get("traceparent");
    }

    // cоздаем спан  ( с нашим  traceParent)
    //  span.start - span.end
    public <T> T runInStoredTrace(String traceparent, String spanName, Supplier<T> action) {
        Map<String, String >  map = new HashMap<>();

        if (traceparent!= null) map.put(TRACEPARENT, traceparent);
        // Builder работает через мап так что нам всегда нужно передавать мап
        // откуда поулчать значения и куда записывать значения
        Span.Builder builder = propagator.extract(map, Map<String,String>::get);
        Span span = builder.name(spanName).start();
        try(Tracer.SpanInScope ws = tracer.withSpan(span)) {
         return  action.get();
        }
        finally {
            span.end();
        }
    }

    //Span — это не строка, а объект, представляющий текущую единицу работы
    // создали спан  - погрузили

    //inject - получить
    // extract - внести изменения
}






//        Map<String, String> map = new HashMap<>();
//        if (traceparent != null) map.put(TRACEPARENT, traceparent);
//        // создали билдер который умеет извлекать в  наш текущий мап
//        Span.Builder builder = propagator.extract(map, Map<String,String>::get);
//        Span span = builder.name(spanName).start(); // получили наш спан с помощью билдера
//
//        // делаем  наш спан  рабочим в потоке
//        try (Tracer.SpanInScope ws = tracer.withSpan(span)){
//        return action.get(); // отправляем в кафку, нам передают Supplier c действием
//        } finally {
//                span.end(); // отправляеем его в Tempo
//        }