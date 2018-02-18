	package com.binder.cursomc.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.binder.cursomc.services.exceptions.FileException;

@Service
public class S3Service {

	private Logger LOG = LoggerFactory.getLogger(S3Service.class);

	//Injetado @bean criado na classe S3Config no método public AmazonS3 s3client().
	@Autowired
	private AmazonS3 s3client;

	@Value("${s3.bucket}")
	private String bucketName;

	//método que vai fazer upload do meu arquivo para o S3 da Amazon.
	// multipartFile  enviar para o S3 um arquivo que vier pela requisição.
	// método URI retorna o endereço WEB do novo recurso que foi gerado.
	public URI uploadFile(MultipartFile multipartFile) {
		try {
			//extrai o nome do arquivo que foi enviado.
			String fileName = multipartFile.getOriginalFilename();
			//Encapsula um processamento de leitura a partir de uma origem (meu arquivo-multipartFile) - Objeto básico de leitura do java.io
			InputStream is = multipartFile.getInputStream();
			//Obtem a string do tipo do arquivo que foi enviado (imagem, texto)
			String contentType = multipartFile.getContentType();
			
			//Acima são as informações básicas necessárias para fazer o upload do arquivo.
			
			return uploadFile(is, fileName, contentType);
			
		} catch (IOException e) {
			throw new FileException("Erro de IO: " + e.getMessage());
		}
	}

	//fileName nome do arquivo a ser salvo no S3.
	//contentType tipo do arquivo a ser salvo no S3.
	public URI uploadFile(InputStream is, String fileName, String contentType) {
		try {
			//Objeto da biblioteca da Amazon
			ObjectMetadata meta = new ObjectMetadata();
			meta.setContentType(contentType);
			
			LOG.info("Iniciando upload");
			//1 arg: nome do bucket no S3, 2 arg: nome do arquivo a ser salvo no S3.
			//3 arg: InputStream, 4 arg: objeto meta da S3.
			s3client.putObject(bucketName, fileName, is, meta);
			LOG.info("Upload finalizado");
			//retorna um objeto do tipo URL , e Depois converte para um tipo URI.
			return s3client.getUrl(bucketName, fileName).toURI();
		} catch (URISyntaxException e) {
			throw new FileException("Erro ao converter URL para URI");
		}
	}
}