package com.pedro.marketplace.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/imagens")
public class ServidorDeArquivosController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @GetMapping("/{nomeArquivo}")
    public ResponseEntity<Resource> servir(@PathVariable String nomeArquivo) {
        try {
            Path arquivo = Paths.get(uploadDir).resolve(nomeArquivo).normalize();
            Resource resource = new UrlResource(arquivo.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}