package com.binder.cursomc.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.binder.cursomc.domain.Cliente;
import com.binder.cursomc.domain.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;


@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
 
		@Transactional(readOnly=true) //reduz  tempo de processamento.
		Page<Pedido> findByCliente(Cliente cliente, Pageable pageRequest);
	}
 
