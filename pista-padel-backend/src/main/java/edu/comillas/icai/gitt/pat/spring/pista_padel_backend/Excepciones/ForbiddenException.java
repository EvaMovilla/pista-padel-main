package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.Excepciones;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
