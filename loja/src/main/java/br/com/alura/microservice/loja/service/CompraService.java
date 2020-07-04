package br.com.alura.microservice.loja.service;

import br.com.alura.microservice.loja.dto.CompraDTO;
import br.com.alura.microservice.loja.dto.InfoFornecedorDTO;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CompraService {
	
	private final RestTemplate client;
	private final DiscoveryClient discoveryClient;

	public CompraService(RestTemplate client, DiscoveryClient discoveryClient) {
		this.client = client;
		this.discoveryClient = discoveryClient;
	}

    public void realizaCompra(CompraDTO compra) {
        ResponseEntity<InfoFornecedorDTO> exchange = client.exchange("http://fornecedor/info/" + compra.getEndereco().getEstado(),
                HttpMethod.GET,
                null,
                InfoFornecedorDTO.class);
        
        discoveryClient.getInstances("fornecedor").stream()
        	.forEach(fornecedor -> System.out.println("localhost:"+fornecedor.getPort()));

        System.out.println(exchange.getBody().getEndereco());
    }
}
