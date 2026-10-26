package br.com.schedule.domain.model;

public enum StatusAgendamento {

    AGENDADA,
    EM_ANDAMENTO,
    CONCLUIDA,
    CANCELADA;

    public boolean podeTransicionarPara(StatusAgendamento destino) {
        return switch (this) {
            case AGENDADA -> destino == EM_ANDAMENTO || destino == CANCELADA;
            case EM_ANDAMENTO -> destino == CONCLUIDA;
            case CONCLUIDA, CANCELADA -> false;
        };
    }

    public boolean estaAtivo() {
        return this == AGENDADA || this == EM_ANDAMENTO;
    }
}
