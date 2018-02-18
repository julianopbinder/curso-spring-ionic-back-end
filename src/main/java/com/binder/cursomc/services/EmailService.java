package com.binder.cursomc.services;

import org.springframework.mail.SimpleMailMessage;

import com.binder.cursomc.domain.Cliente;
import com.binder.cursomc.domain.Pedido;

public interface EmailService {

	//Implementado na classe AbstractEmailService.
	//Esse metodo aqui vai utilizar o metodo abaixo (sendEmail)
	//Antes de chamar sendEmail vai ser preciso preparar um objeto de pedido.
	void sendOrderConfirmationEmail(Pedido obj);
	
	//Implementado nas classes : SmtpEmailService  e MockEmailService 
	void sendEmail(SimpleMailMessage msg);
	
	//Implementado na classe AbstractEmailService.
	void sendNewPasswordEmail(Cliente cliente, String newPass);
}
