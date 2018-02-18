package com.binder.cursomc.services;

import org.springframework.security.core.context.SecurityContextHolder;

import com.binder.cursomc.security.UserSS;

public class UserService {
	
	//Método static porque pode ser chamado independente do UserService
	
	public static UserSS authenticated() {
		try {
			//Metodo que retorna quem é o usuario autenticado no sistema.
			return (UserSS) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		}
		catch (Exception e) {
			return null;
		}
	}
}