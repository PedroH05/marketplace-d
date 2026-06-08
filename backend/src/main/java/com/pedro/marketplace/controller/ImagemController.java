package com.pedro.marketplace.controller;

import com.pedro.marketplace.service.ImagemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/produtos")
public class ImagemController {

    private final ImagemService imagemService;

    public ImagemController(ImagemService imagemService) {
        this.imagemService = imagemService;
    }

    @PostMapping("/{id}/imagem")
    public ResponseEntity<String> uploadImagem(
            @PathVariable Long id,
            @RequestParam("arquivo") MultipartFile arquivo) {
        String url = imagemService.salvarImagem(id, arquivo);
        return ResponseEntity.ok(url);
    }
}