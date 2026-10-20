package com.company.inventory22.Controller;

import com.company.inventory22.ChaosState;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chaos")
public class ChaosController {
    private final ChaosState chaos;

    public ChaosController(ChaosState chaos) {
        this.chaos = chaos;
    }

    // POST /chaos/delay?ms=5000 включает задержку, ms=0 выключает
    @PostMapping("/delay")
    public ResponseEntity<String> delay(@RequestParam long ms) {
        chaos.setDelayMs(ms);
        return ResponseEntity.ok("delay is " + ms + " ms");
    }

    // POST /chaos/fail-rate?p=50: каждый запрос цены с вероятностью p% отвечает 503, p=0 выключает
    @PostMapping("/fail-rate")
    public ResponseEntity<String> failRate(@RequestParam int p) {
        chaos.setFailRate(p);
        return ResponseEntity.ok("fail rate is " + p + " %");
    }
}
