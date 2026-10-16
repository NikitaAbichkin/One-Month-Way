package com.company.inventory17;

import com.company.inventory17.Indicator.Indicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/health")
public class AdminHealthController {
    private final Indicator indicator;

    public AdminHealthController(Indicator indicator) {
        this.indicator = indicator;
    }

    @PostMapping("/down")
    public ResponseEntity<String> down() {
        indicator.setUp(false);
        return ResponseEntity.ok("health is DOWN");
    }

    @PostMapping("/up")
    public ResponseEntity<String> up() {
        indicator.setUp(true);
        return ResponseEntity.ok("health is UP");
    }
    @GetMapping("/status")
    public  ResponseEntity<String> checkHealth(){
        if (indicator.isUp())
            return ResponseEntity.ok("Health status is UP");
        else
            return ResponseEntity.ok("Health status is DOWN");
    }
}
