package br.com.alura.microservice.loja.controller;

import br.com.alura.microservice.loja.dto.CompraDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/compra")
public class CompraController {

    @PostMapping
    public void realizaCompra(@RequestBody CompraDTO compra) {

    }

}
