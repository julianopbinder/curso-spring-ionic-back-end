package com.binder.cursomc.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.binder.cursomc.security.JWTAuthenticationFilter;
import com.binder.cursomc.security.JWTAuthorizationFilter;
import com.binder.cursomc.security.JWTUtil;

@Configuration // Informa que é uma classe de configuração
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true) // Autorizar somente alguns perfils específicos.
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private Environment env;

	@Autowired
	private JWTUtil jwtUtil;

	// array informa quais as uri(s) que estaram liberadas após a autorização do
	// JWT.
	private static final String[] PUBLIC_MATCHERS = { "/h2-console/**"

	};

	// caminho que só vou permitir recuperar os dados, mas nao pode alterar
	private static final String[] PUBLIC_MATCHERS_GET = { "/produtos/**", "/categorias/**", "/auth/forgot/**",
			"/estados/**" };

	// acessar mesmo senao estiver logado
	private static final String[] PUBLIC_MATCHERS_POST = { "/clientes", "/auth/forgot/**" };

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		// Busca os profiles ativos no sistema.
		// Se tiver o profile "test" deixa acessar o h2.
		if (Arrays.asList(env.getActiveProfiles()).contains("test")) {
			http.headers().frameOptions().disable();
		}
		// ativa o @Bean abaixo
		// proteção de ataque csrf.(ataques baseado em armazenamento de autenticação e
		// sessão.
		// essa api não armazena sessao, nao se preocupar.
		http.cors().and().csrf().disable();
		http.authorizeRequests().antMatchers(HttpMethod.POST, PUBLIC_MATCHERS_POST).permitAll()
				.antMatchers(HttpMethod.GET, PUBLIC_MATCHERS_GET).permitAll() // permitir somente GET nos diretorios que
																				// estão no array
				.antMatchers(PUBLIC_MATCHERS).permitAll() // todos os caminhos que estiverem no vetor, eu vou permitir
															// acessar.
				.anyRequest().authenticated(); // para todo resto exige autenticação.

		// authenticationManage : método disponivel da classe
		// WebSecurityConfigurerAdapter (estendida nessa classe mesmo - SecurityConfig
		// chama uma instancia do jwtUtil
		http.addFilter(new JWTAuthenticationFilter(authenticationManager(), jwtUtil));
		http.addFilter(new JWTAuthorizationFilter(authenticationManager(), jwtUtil, userDetailsService));
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS); // nao cria sessao de usuário

	}

	// codigo padrao
	// Vamos usar a autenticacao do framework / sobreescrever
	// AuthenticationManagerBuilder
	// Descobrir quem é o UserDetailsService (foi injetado dependencia - private
	// UserDetailsService userDetailsService;
	// quem é o algoritimo de codificação da senha:
	// passwordEncoder(bCryptPasswordEncoder() @Bean está nesse mesma classe
	// não é preciso injetar dependencia @Autowired.
	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		// Instancia userDetailsService vai buscar quem é o usuario por email
		auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder());
	}

	// Configurando um Bean
	// Permitindo acesso a diversas fontes (/**)
	// com as configurações básicas (applyPermitDefaultValues())
	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration().applyPermitDefaultValues();
		configuration.setAllowedMethods(Arrays.asList("POST", "GET", "PUT", "DELETE", "OPTIONS"));
		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	// Bean para criptografar senha
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

}