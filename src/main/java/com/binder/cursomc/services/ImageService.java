package com.binder.cursomc.services;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.imgscalr.Scalr;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.binder.cursomc.services.exceptions.FileException;

@Service
public class ImageService {

	//Pega um tipo de imagem (uploadedFile) e converte para jpg (BufferedImage)
	public BufferedImage getJpgImageFromFile(MultipartFile uploadedFile) {
		//Pega a extensão do arquivo
		String ext = FilenameUtils.getExtension(uploadedFile.getOriginalFilename());
		//Se extensão do arquivo não for png ou jpg então rejeita a requisição;
		if (!"png".equals(ext) && !"jpg".equals(ext)) {
			throw new FileException("Somente imagens PNG e JPG são permitidas");
		}
		
		try {
			//Tenta obter um BufferedImage a partir do  uploadedFile
			BufferedImage img = ImageIO.read(uploadedFile.getInputStream());
			if ("png".equals(ext)) {
				img = pngToJpg(img);
			}
			return img;
		} catch (IOException e) {
			throw new FileException("Erro ao ler arquivo");
		}
	}

	//codigo padrão
	public BufferedImage pngToJpg(BufferedImage img) {
		BufferedImage jpgImage = new BufferedImage(img.getWidth(), img.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		jpgImage.createGraphics().drawImage(img, 0, 0, Color.WHITE, null);
		return jpgImage;
	}
	
	//Retorna um objeto que encapsula leitura, a partir de um BufferedImage.
	//código padrão
	public InputStream getInputStream(BufferedImage img, String extension) {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(img, extension, os);
			return new ByteArrayInputStream(os.toByteArray());
		} catch (IOException e) {
			throw new FileException("Erro ao ler arquivo");
		}
	}

	public BufferedImage cropSquare(BufferedImage sourceImg) {
		
		//descobre qual é o minimo largura ou altura.
		int min = (sourceImg.getHeight() <= sourceImg.getWidth()) ? sourceImg.getHeight() : sourceImg.getWidth();
		return Scalr.crop(
			sourceImg, 
			(sourceImg.getWidth()/2) - (min/2), 
			(sourceImg.getHeight()/2) - (min/2), 
			min, 
			min);		
	}

	//recebe uma imagem e define o tamanho que deseja recortar
	public BufferedImage resize(BufferedImage sourceImg, int size) {
		return Scalr.resize(sourceImg, Scalr.Method.ULTRA_QUALITY, size);
	}
}