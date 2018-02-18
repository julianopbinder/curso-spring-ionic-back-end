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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.binder.cursomc.domain.Categoria;
import com.binder.cursomc.dto.CategoriaDTO;
import com.binder.cursomc.services.CategoriaService;

@RestController
@RequestMapping(value="/categorias")
public class CategoriaResource {

	@Autowired
	private CategoriaService service;
	
	//Fazer buscar = qualquer perfil
	@RequestMapping(value="/{id}", method=RequestMethod.GET)
	public ResponseEntity<Categoria> find(@PathVariable Integer id) {
		  Categoria obj = service.find(id);
	      return ResponseEntity.ok().body(obj);
	}
	
	//INSERT - somente perfil admin		
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<Void> insert(@Valid   @RequestBody CategoriaDTO objDto){
		   Categoria obj = service.fromDTO(objDto);
		   obj = service.insert(obj);
		   URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
		            .path("/{id}").buildAndExpand(obj.getId()).toUri();
		   return ResponseEntity.created(uri).build();
	}
	
	//UPDATE -  somente perfil admin
	@PreAuthorize("hasAnyRole('ADMIN')")
	@RequestMapping(value="/{id}", method = RequestMethod.PUT)
	public ResponseEntity<Void> update(@Valid @RequestBody CategoriaDTO objDto, @PathVariable Integer id ) {
		Categoria obj = service.fromDTO(objDto);
		obj.setId(id);
	    obj = service.update(obj);
	    return ResponseEntity.noContent().build();
	}
	
	//DELETE - somente perfil admin
	@PreAuthorize("hasAnyRole('ADMIN')")
	@RequestMapping(value="/{id}", method=RequestMethod.DELETE)
	public ResponseEntity<Void> delete(@PathVariable Integer id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}
	
	//Retorna todas as categorias
	@RequestMapping(method=RequestMethod.GET)
	public ResponseEntity<List<CategoriaDTO>> findAll() {
		
		  //busca lista de categorias do banco de dados
		  List<Categoria> list = service.findAll();
		  
		  //converte uma lista em outra lista dto (apenas mostra resultado conforme especificado 
		  //na classe CategoriaDTO. java 8
		  List<CategoriaDTO> listDto = list.stream().map(obj -> new CategoriaDTO(obj)).collect(Collectors.toList());
	      return ResponseEntity.ok().body(listDto);
	}
	
	
	@RequestMapping(value="/page",method=RequestMethod.GET)
	public ResponseEntity<Page<CategoriaDTO>> findPage(
		  @RequestParam(value="page", defaultValue="0") Integer page,
		  @RequestParam(value="linePerPage", defaultValue="24")Integer linesPerPage,
		  @RequestParam(value="orderBy", defaultValue="nome")String orderBy, 
		  @RequestParam(value="direction", defaultValue="ASC")String direction) {		
		  Page<Categoria> list = service.findPage(page,linesPerPage,orderBy,direction);		  
		  Page<CategoriaDTO> listDto = list.map(obj -> new CategoriaDTO(obj));
	      return ResponseEntity.ok().body(listDto);
	}
	
}