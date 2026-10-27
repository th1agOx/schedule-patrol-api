package br.com.schedule.application.exception;

public final class RecursoNaoEncontradoException extends RuntimeException {

    private RecursoNaoEncontradoException(String message) {
        super(message);
    }

    public static RecursoNaoEncontradoException ronda(String id) {
        return new RecursoNaoEncontradoException(
                "Ronda não encontrada para o identificador '%s'.".formatted(id));
    }

    public static RecursoNaoEncontradoException agendamento(String id) {
        return new RecursoNaoEncontradoException(
                "Agendamento não encontrado para o identificador '%s'.".formatted(id));
    }
}
