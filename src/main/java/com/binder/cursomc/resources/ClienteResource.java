package com.binder.cursomc.resources;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.binder.cursomc.domain.Cliente;
import com.binder.cursomc.dto.ClienteDTO;
import com.binder.cursomc.dto.ClienteNewDTO;
import com.binder.cursomc.services.ClienteService;

@RestController
@RequestMapping(value="/clientes")
public class ClienteResource {

	@Autowired
	private ClienteService service;
	
	@RequestMapping(value="/{id}", method=RequestMethod.GET)
	public ResponseEntity<Cliente> find(@PathVariable Integer id) {
		
	      Cliente obj = service.find(id);
	  
	      return ResponseEntity.ok().body(obj);
	}
	
	
	@RequestMapping(value="/email", method=RequestMethod.GET)
	public ResponseEntity<Cliente> find(@RequestParam(value="value") String email) {
		Cliente obj = service.findByEmail(email);
		return ResponseEntity.ok().body(obj);
	}
	
	
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<Void> insert(@Valid   @RequestBody ClienteNewDTO objDto){
		   Cliente obj = service.fromDTO(objDto);
		   obj = service.insert(obj);
		   URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
		            .path("/{id}").buildAndExpand(obj.getId()).toUri();
		   return ResponseEntity.created(uri).build();
	}
	
	
	@RequestMapping(value="/{id}", method = RequestMethod.PUT)
	public ResponseEntity<Void> update(@Valid @RequestBody ClienteDTO objDto, @PathVariable Integer id ) {
		Cliente obj = service.fromDTO(objDto);
		obj.setId(id);
	    obj = service.update(obj);
	    return ResponseEntity.noContent().build();
	}
	
	@PreAuthorize("hasAnyRole('ADMIN')")
	@RequestMapping(value="/{id}", method=RequestMethod.DELETE)
	public ResponseEntity<Void> delete(@PathVariable Integer id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}
	
	@PreAuthorize("hasAnyRole('ADMIN')")
	//Retorna todas as categorias
	@RequestMapping(method=RequestMethod.GET)
	public ResponseEntity<List<ClienteDTO>> findAll() {
		
		  //busca lista de categorias do banco de dados
		  List<Cliente> list = service.findAll();
		  
		  //converte uma lista em outra lista dto (apenas mostra resultado conforme especificado 
		  //na classe ClienteDTO. java 8
		  List<ClienteDTO> listDto = list.stream().map(obj -> new ClienteDTO(obj)).collect(Collectors.toList());
	      return ResponseEntity.ok().body(listDto);
	}
	
	
	@RequestMapping(value="/page",method=RequestMethod.GET)
	public ResponseEntity<Page<ClienteDTO>> findPage(
		  @RequestParam(value="page", defaultValue="0") Integer page,
		  @RequestParam(value="linePerPage", defaultValue="24")Integer linesPerPage,
		  @RequestParam(value="orderBy", defaultValue="nome")String orderBy, 
		  @RequestParam(value="direction", defaultValue="ASC")String direction) {		
		  Page<Cliente> list = service.findPage(page,linesPerPage,orderBy,direction);		  
		  Page<ClienteDTO> listDto = list.map(obj -> new ClienteDTO(obj));
	      return ResponseEntity.ok().body(listDto);
	}
	
	

	//EndPoint para enviar a foto do cliente (upload) para S3.	 
	@RequestMapping(value="/picture", method=RequestMethod.POST)
	//@RequestParam para informar que chegou um parametro da requisição http
	public ResponseEntity<Void> uploadProfilePicture(@RequestParam(name="file") MultipartFile file) {
		//Chama a camada de serviço passando o arquivo que venho por parametro.(fazendo upload de uma imagem)
		//retorna a URI dessa imagem.
		URI uri = service.uploadProfilePicture(file);
		//Retorna uma resposta 201 (criado) e retorna resposta no cabeçalho da pagina.
		return ResponseEntity.created(uri).build();
	}
	
	
}