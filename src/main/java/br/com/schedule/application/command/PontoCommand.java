package br.com.schedule.application.command;

import br.com.schedule.domain.model.Ponto;

public record PontoCommand(String codigo, String descricao) {

    public Ponto toDomain() {
        return new Ponto(codigo, descricao);
    }
}
