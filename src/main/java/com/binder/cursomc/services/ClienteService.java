package com.binder.cursomc.services;

import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.binder.cursomc.domain.Cidade;
import com.binder.cursomc.domain.Cliente;
import com.binder.cursomc.domain.Endereco;
import com.binder.cursomc.domain.enums.Perfil;
import com.binder.cursomc.domain.enums.TipoCliente;
import com.binder.cursomc.dto.ClienteDTO;
import com.binder.cursomc.dto.ClienteNewDTO;
import com.binder.cursomc.repositories.CidadeRepository;
import com.binder.cursomc.repositories.ClienteRepository;
import com.binder.cursomc.repositories.EnderecoRepository;
import com.binder.cursomc.security.UserSS;
import com.binder.cursomc.services.exceptions.AuthorizationException;
import com.binder.cursomc.services.exceptions.DataIntegrityException;
import com.binder.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class ClienteService {

	@Autowired
	private ClienteRepository repo;

	@Autowired
	private CidadeRepository cidadeRepository;

	@Autowired
	private EnderecoRepository enderecoRepository;

	@Autowired
	private BCryptPasswordEncoder pe;

	@Autowired
	private S3Service s3service;

	@Autowired
	private ImageService imageService;

	@Value("${img.prefix.client.profile}")
	private String prefix;

	@Value("${img.profile.size}")
	private Integer size;

	public Cliente find(Integer id) {

		// Busca o usuario logado;
		UserSS user = UserService.authenticated();
		if (user == null || !user.hasRole(Perfil.ADMIN) && !id.equals(user.getId())) {
			throw new AuthorizationException("Acesso negado");
		}

		Cliente obj = repo.findOne(id);
		if (obj == null) {
			throw new ObjectNotFoundException(
					"Objeto não encontrado! Id: " + id + ", Tipo: " + Cliente.class.getName());
		}
		return obj;
	}

	public Cliente insert(Cliente obj) {
		obj.setId(null);
		obj = repo.save(obj);
		enderecoRepository.save(obj.getEnderecos());
		return repo.save(obj);
	}

	public Cliente update(Cliente obj) {
		Cliente newObj = find(obj.getId());
		updateData(newObj, obj);
		return repo.save(newObj);
	}

	public void delete(Integer id) {
		find(id);
		try {
			repo.delete(id);
		} catch (DataIntegrityViolationException e) {
			throw new DataIntegrityException(
					"Não é possível excluir porque há entidades(pedido) relacionadas...por enquanto");
		}
	}

	public List<Cliente> findAll() {
		return repo.findAll();

	}

	// busca por usuario por email
	public Cliente findByEmail(String email) {

		//quem é o usuario autenticado
		UserSS user = UserService.authenticated();
		//se usuario for nulo, ou nao for adm, e email que estou procurando nao for do usuario que está logado
		//ou deu problema de autenticação usuario que está buscando email não é o usuario que está no email recebido por argumento.
		if (user == null || !user.hasRole(Perfil.ADMIN) && !email.equals(user.getUsername())) {
			throw new AuthorizationException("Acesso negado");
		}

		//busca cliente por id
		Cliente obj = repo.findOne(user.getId());
		if (obj == null) {
			throw new ObjectNotFoundException(
					"Objeto não encontrado! Id: " + user.getId() + ", Tipo: " + Cliente.class.getName());
		}
		return obj;
	}

	// Spring Data com paginação
	public Page<Cliente> findPage(Integer page, Integer linesPerPage, String orderBy, String direction) {

		PageRequest pageRequest = new PageRequest(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return repo.findAll(pageRequest);
	}

	public Cliente fromDTO(ClienteDTO objDto) {
		return new Cliente(objDto.getId(), objDto.getNome(), objDto.getEmail(), null, null, null);

	}

	public Cliente fromDTO(ClienteNewDTO objDto) {
		Cliente cli = new Cliente(null, objDto.getNome(), objDto.getEmail(), objDto.getCpfOuCnpj(),
				TipoCliente.toEnum(objDto.getTipo()), pe.encode(objDto.getSenha()));

		Cidade cid = cidadeRepository.findOne(objDto.getCidadeId());

		Endereco end = new Endereco(null, objDto.getLogradouro(), objDto.getNumero(), objDto.getComplemento(),
				objDto.getBairro(), objDto.getCep(), cli, cid);

		cli.getEnderecos().add(end);

		cli.getTelefones().add(objDto.getTelefone1());

		if (objDto.getTelefone2() != null) {
			cli.getTelefones().add(objDto.getTelefone2());
		}
		if (objDto.getTelefone3() != null) {
			cli.getTelefones().add(objDto.getTelefone3());
		}
		return cli;
	}

	private void updateData(Cliente newObj, Cliente obj) {
		newObj.setNome(obj.getNome());
		newObj.setEmail(obj.getEmail());
	}

	// método que envia uma imagem (faz o upload) do perfil do cliente para o S3
	public URI uploadProfilePicture(MultipartFile multipartFile) {

		// busca o usuario logado.
		UserSS user = UserService.authenticated();
		if (user == null) {
			throw new AuthorizationException("Acesso negado");
		}

		// Extrai o jpg a partir do arquivo que foi enviado multipartFile
		BufferedImage jpgImage = imageService.getJpgImageFromFile(multipartFile);

		jpgImage = imageService.cropSquare(jpgImage);
		jpgImage = imageService.resize(jpgImage, size);

		// Definição padrão de nome da imagem (exemplo cli1.jpg / cli2.jpg)
		String fileName = prefix + user.getId() + ".jpg";

		return s3service.uploadFile(imageService.getInputStream(jpgImage, "jpg"), fileName, "image");
	}

}
