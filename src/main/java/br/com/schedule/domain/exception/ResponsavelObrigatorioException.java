package br.com.schedule.domain.exception;

public class ResponsavelObrigatorioException extends DomainException {

    public ResponsavelObrigatorioException() {
        super("O responsável é obrigatório e não pode ser vazio.");
    }
}
