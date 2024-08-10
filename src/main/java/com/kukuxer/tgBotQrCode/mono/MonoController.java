package com.kukuxer.tgBotQrCode.mono;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mono")
public class MonoController {

    private final MonoService monoService;

    @GetMapping("/personal")
    public ResponseEntity<MonoAccount> getPersonalInfo(){
        return monoService.getPersonalInfo();
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(){
        return monoService.getTransactions();
    }

}
