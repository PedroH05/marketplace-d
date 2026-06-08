package com.pedro.marketplace.exception;

public class RegistroDuplicadoException extends RuntimeException {
    public RegistroDuplicadoException(String mensagem) {
        super(mensagem);
    }
}