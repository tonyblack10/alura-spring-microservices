package br.com.alura.microservices.fornecedor.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.alura.microservices.fornecedor.model.InfoFornecedor;
import br.com.alura.microservices.fornecedor.service.InfoService;

@RestController
@RequestMapping("/info")
public class InfoController {
	
	private final InfoService infoService;

	public InfoController(InfoService infoService) {
		this.infoService = infoService;
	}
	
	@GetMapping("/{estado}")
	public InfoFornecedor getInfoPorEstado(@PathVariable String estado) {
		return infoService.getInfoPorEstado(estado);
	}
	
}
