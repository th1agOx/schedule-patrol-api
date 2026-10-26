package br.com.schedule.domain.exception;

public class MotivoCancelamentoObrigatorioException extends DomainException {

    public MotivoCancelamentoObrigatorioException() {
        super("O motivo do cancelamento é obrigatório e não pode ser vazio.");
    }
}
