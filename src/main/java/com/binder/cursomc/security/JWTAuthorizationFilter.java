package com.binder.cursomc.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

	private JWTUtil jwtUtil;

	//Busca usuário pelo email no banco para ver se existe.
	private UserDetailsService userDetailsService;

	//Filtro vai analisar se o Token é valido ou não.
	//UserDetailsService: será preciso para buscar o usuario na base de dados por email
	public JWTAuthorizationFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil,
			UserDetailsService userDetailsService) {
		super(authenticationManager);
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
	}

	//Método que intercepeta a requisição e verifica se o usuario está autorizado.
	//Método padrão que verifica antes de continuar.
	//Metodo receve 3 coisas: request(obtem o cabeçalho(header) da aplicação, response, chain (cadeia de valores)
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		//recebe como argumento o nome do cabeçalho
		String header = request.getHeader("Authorization");
		
		//Procedimento para liberar a autorização do usuario para acessar o endPoint
		if (header != null && header.startsWith("Bearer ")) {
			
			//getAuthentication: envia o valor que tem na frente do "Bearer"(que é o token em si)
			//e ele retorna um objeto auth que é do tipo (UsernamePasswordAuthenticationToken)do spring security
			//dá certo se o token é valido, caso contrario o método retorna nulo.
			UsernamePasswordAuthenticationToken auth = getAuthentication(header.substring(7));
			if (auth != null) {
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
		}
		//Continua fazendo a requisição normalmente;
		chain.doFilter(request, response);
	}

	//Gera objeto UsernamePasswordAuthenticationToken a partir da Token
	private UsernamePasswordAuthenticationToken getAuthentication(String token) {
	
		//Se o Token for válido
		if (jwtUtil.tokenValido(token)) {
			//Busca o username dentro do token.
			String username = jwtUtil.getUsername(token);
			
			//Busca usuario no banco para poder instanciar o UsernamePasswordAuthenticationToken(no método acima)
			UserDetails user = userDetailsService.loadUserByUsername(username);
			return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
		}
		return null;
	}
}
