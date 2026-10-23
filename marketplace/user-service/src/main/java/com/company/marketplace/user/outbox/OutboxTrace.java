package com.company.marketplace.user.outbox;

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

    private final Tracer tracer;
    private final Propagator propagator;

    public OutboxTrace(Tracer tracer, Propagator propagator) {
        this.tracer = tracer;
        this.propagator = propagator;
    }

    public String currentTraceparent(){
        Span span  = tracer.currentSpan();
        if (span == null)  return null;
        Map<String,String> map = new HashMap<>();
        propagator.inject(span.context(),map,Map<String,String>::put);
        return  map.get(TRACEPARENT);
    }

    // заменяем текущий traceParent на наш  и заменяем спан  как  текущий
    public <T> T runInStoredTrace(String traceparent, String spanName, Supplier<T> action){
        Map<String,String>  map = new HashMap<>();
        if(traceparent!=null)  map.put(TRACEPARENT, traceparent);

        Span.Builder builder = propagator.extract(map,Map<String,String>::get);
        Span spanNew = builder.name(spanName).start();

        try(Tracer.SpanInScope ws = tracer.withSpan(spanNew)) {
            return  action.get();
        }
        finally {
            spanNew.end();
        }
    }


}
