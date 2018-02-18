package com.binder.cursomc.security;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.binder.cursomc.dto.CredenciaisDTO;

//UsernamePasswordAuthenticationFilter filtro do spring security para interceptar l	ogin antes de seguir adiante
//http://localhost:8080/login (login - endPoint do framework reservada para spring security
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private AuthenticationManager authenticationManager;

	private JWTUtil jwtUtil;

	//Injetando authenticationManager e jwtUtil no construtor
	public JWTAuthenticationFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
			throws AuthenticationException {

		try {
			//Pega os dados que vieram da requisicao e converte para objeto CredenciaisDTO
			//e instancia no obj creds.
			CredenciaisDTO creds = new ObjectMapper().readValue(req.getInputStream(), CredenciaisDTO.class);

			//Objeto authToken não é do Token e sim do Spring Security
			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(creds.getEmail(),
					creds.getSenha(), new ArrayList<>());

			//método authenticate que vai verificar se o usuário e senha são validos;
			// Objeto auth vai informar para o Spring Security se a autenticação ocorreu com sucesso ou não.
			Authentication auth = authenticationManager.authenticate(authToken);
			return auth;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	//Se a autenticação ocorrer com sucesso aqui nesse método aqui
	//Gera um Token e devolve na resposta da requisição
	@Override
	protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,
			Authentication auth) throws IOException, ServletException {

		//getPrincipal: retorna o usuario do spring security e faz um cast para UserSS
		//getUsername: pega o email da pessoa que fez o login
		String username = ((UserSS) auth.getPrincipal()).getUsername();
		
		//Chama o generateToken passando esse email informado(recuperado) acima;
		String token = jwtUtil.generateToken(username);
		res.addHeader("Authorization", "Bearer " + token);
		//Envia para o retorno da resposta da requisição adicionando como um cabeçalho da requisição.
		res.addHeader("access-control-expose-headers", "Authorization");
		res.addHeader("access-control-expose-headers", "Authorization");
	} 
}