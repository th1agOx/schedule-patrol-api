package br.com.schedule.application.response;

import br.com.schedule.domain.model.Ponto;

public record PontoResponse(String codigo, String descricao) {

    public static PontoResponse from(Ponto ponto) {
        return new PontoResponse(ponto.codigo(), ponto.descricao());
    }
}
