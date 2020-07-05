package br.com.alura.microservice.loja.service;

import java.time.LocalDate;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import br.com.alura.microservice.loja.client.FornecedorClient;
import br.com.alura.microservice.loja.client.TransportadorClient;
import br.com.alura.microservice.loja.dto.CompraDTO;
import br.com.alura.microservice.loja.dto.InfoEntregaDTO;
import br.com.alura.microservice.loja.dto.InfoFornecedorDTO;
import br.com.alura.microservice.loja.dto.InfoPedidoDTO;
import br.com.alura.microservice.loja.dto.VoucherDTO;
import br.com.alura.microservice.loja.model.Compra;
import br.com.alura.microservice.loja.model.CompraState;
import br.com.alura.microservice.loja.repository.CompraRepository;

@Service
public class CompraService {
	
	private static final Logger logger = LoggerFactory.getLogger(CompraService.class);
	
	private final FornecedorClient fornecedorClient;

	private final CompraRepository compraRepository;

	private final TransportadorClient transportadorClient;

	public CompraService(FornecedorClient fornecedorClient, TransportadorClient transportadorClient, CompraRepository compraRepository) {
		this.fornecedorClient = fornecedorClient;
		this.transportadorClient = transportadorClient;
		this.compraRepository = compraRepository;
	}

	@HystrixCommand(fallbackMethod = "realizaCompraFallback", threadPoolKey = "realizaCompraThreadPool")
    public Compra realizaCompra(CompraDTO compra) {
		Compra compraSalva = new Compra();
		compraSalva.setState(CompraState.RECEBIDO);
		compraSalva.setEnderecoDestino(compra.getEndereco().toString());
		compraRepository.save(compraSalva);
		compra.setCompraId(compraSalva.getId());

		InfoFornecedorDTO info = fornecedorClient.getInfoPorEstado(compra.getEndereco().getEstado());
    	InfoPedidoDTO pedido = fornecedorClient.realizaPedido(compra.getItens());
    	compraSalva.setState(CompraState.PEDIDO_REALIZADO);
    	compraSalva.setPedidoId(pedido.getId());
        compraSalva.setTempoDePreparo(pedido.getTempoDePreparo());
		compraRepository.save(compraSalva);

    	InfoEntregaDTO entregaDTO = new InfoEntregaDTO();
    	entregaDTO.setPedidoId(pedido.getId());
    	entregaDTO.setDataParaEntrega(LocalDate.now().plusDays(pedido.getTempoDePreparo()));
    	entregaDTO.setEnderecoOrigem(info.getEndereco());
    	entregaDTO.setEnderecoDestino(compra.getEndereco().toString());
    	
        VoucherDTO voucher = transportadorClient.reservaEntrega(entregaDTO);
        
        compraSalva.setState(CompraState.RESERVA_ENTREGA_REALIZADA);
        compraSalva.setDataParaEntrega(voucher.getPrevisaoParaEntrega());
        compraSalva.setVoucher(voucher.getNumero());
		compraRepository.save(compraSalva);
        
        return compraSalva;
    }
	
	public Compra realizaCompraFallback(CompraDTO compra) {
		if (Objects.nonNull(compra.getCompraId())) {
			return compraRepository.findById(compra.getCompraId()).get();
		}
		
		Compra compraFallback = new Compra();
		compraFallback.setEnderecoDestino(compra.getEndereco().toString());
		
		return compraFallback;
	}

	@HystrixCommand(threadPoolKey = "getByIdThreadPool")
	public Compra getById(Long id) {
		return compraRepository.findById(id)
			.orElse(new Compra());
	}
}
