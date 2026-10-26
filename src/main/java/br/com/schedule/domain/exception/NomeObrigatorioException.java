package br.com.schedule.domain.exception;

public class NomeObrigatorioException extends DomainException {

    public NomeObrigatorioException() {
        super("O nome da ronda é obrigatório e não pode ser vazio.");
    }
}
