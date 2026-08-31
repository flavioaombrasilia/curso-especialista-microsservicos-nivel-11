package com.algaworks.hello;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class CargaController {

    // segura as alocacoes: sem isso o GC devolveria a memoria e a demo nao funciona
    private final List<byte[]> retido = new ArrayList<>();

    // 1. come memoria de verdade (array de byte ja nasce zerado = paginas commitadas)
    @PostMapping("/memoria")
    public Map<String, Object> alocar(@RequestParam int mb) {
        for (int i = 0; i < mb; i++) {
            retido.add(new byte[1024 * 1024]);
        }
        Runtime runtime = Runtime.getRuntime();
        return Map.of(
                "retidoMb", retido.size(),
                "heapUsadoMb", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024),
                "heapMaximoMb", runtime.maxMemory() / (1024 * 1024));
    }

    // 2. queima CPU por N milissegundos — quanto mais voltas, mais CPU o Pod recebeu
    @GetMapping("/cpu")
    public Map<String, Object> queimar(@RequestParam(defaultValue = "2000") long ms) {
        long fim = System.currentTimeMillis() + ms;
        long voltas = 0;
        while (System.currentTimeMillis() < fim) {
            voltas++;
        }
        return Map.of("ms", ms, "voltas", voltas);
    }
}
