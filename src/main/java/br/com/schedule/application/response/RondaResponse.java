package br.com.schedule.application.response;

import br.com.schedule.domain.model.Ronda;

import java.util.List;

public record RondaResponse(String id, String nome, String descricao, boolean ativa, List<PontoResponse> pontos) {

    public RondaResponse {
        pontos = pontos == null ? List.of() : List.copyOf(pontos);
    }

    public static RondaResponse from(Ronda ronda) {
        List<PontoResponse> pontos = ronda.getPontos().stream()
                .map(PontoResponse::from)
                .toList();
        return new RondaResponse(ronda.getId(), ronda.getNome(), ronda.getDescricao(), ronda.isAtiva(), pontos);
    }
}
