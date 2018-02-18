package com.binder.cursomc.security;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

//Anotaçao @Component criada para poder ser injetada em outras classes 
@Component
public class JWTUtil {

	//Valores necessários para geração do Token - Estão configurados no arquivo application.properties
	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration}")
	private Long expiration;

	public String generateToken(String username) {
		
		// Esse cara Jwts.builder() que cria o Token - importado no pom.xml
		//setSubject(username) que venho por argumento
		//.setExpiration tempo do servidor + tempo de expiração configurado no application.properties
		//.signWith como vou assinar meu Token (algoritomo que vou usar (HS512-super poderoso)+ segredo)
		return Jwts.builder().setSubject(username).setExpiration(new Date(System.currentTimeMillis() + expiration))
				.signWith(SignatureAlgorithm.HS512, secret.getBytes()).compact();
	}

	//Verifica se o token é valido
	public boolean tokenValido(String token) {
		//Armazena as reivindicações do Token(usuário e tempo de expiração)
		Claims claims = getClaims(token);
		if (claims != null) {
			String username = claims.getSubject();
			Date expirationDate = claims.getExpiration();
			Date now = new Date(System.currentTimeMillis());
			//now.before(expirationDate: meu instante atual é anterior a data de expiração
			if (username != null && expirationDate != null && now.before(expirationDate)) {
				//token válido;
				return true;
			}
		}
		return false;
	}

	//buscar o usuario 
	public String getUsername(String token) {
		Claims claims = getClaims(token);
		//Se for diferente de nulo é prq pegou os clains.
		if (claims != null) {
			return claims.getSubject();
		}
		return null;
	}

	//Recupera os Claims(reivindicações a partir de um token)
	private Claims getClaims(String token) {
		try {
			return Jwts.parser().setSigningKey(secret.getBytes()).parseClaimsJws(token).getBody();
		} catch (Exception e) {
			return null;
		}
	}
}