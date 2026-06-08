package com.pedro.marketplace.service;

import com.pedro.marketplace.entity.Produto;
import com.pedro.marketplace.exception.RecursoNaoEncontradoException;
import com.pedro.marketplace.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImagemService {

    private final ProdutoRepository produtoRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public ImagemService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public String salvarImagem(Long produtoId, MultipartFile arquivo) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));

        try {
            Path pastaUpload = Paths.get(uploadDir);
            Files.createDirectories(pastaUpload);

            String extensao = obterExtensao(arquivo.getOriginalFilename());
            String nomeArquivo = UUID.randomUUID() + "." + extensao;

            Path destino = pastaUpload.resolve(nomeArquivo);
            Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            String url = baseUrl + "/imagens/" + nomeArquivo;
            produto.setImagemUrl(url);
            produto.getImagemUrls().add(url);
            produtoRepository.save(produto);

            return url;

        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar imagem: " + e.getMessage());
        }
    }

    private String obterExtensao(String nomeOriginal) {
        if (nomeOriginal != null && nomeOriginal.contains(".")) {
            return nomeOriginal.substring(nomeOriginal.lastIndexOf(".") + 1);
        }
        return "jpg";
    }
}
