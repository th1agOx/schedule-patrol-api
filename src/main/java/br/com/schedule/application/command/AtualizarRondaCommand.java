package br.com.schedule.application.command;

import br.com.schedule.domain.model.Ponto;

import java.util.List;

public record AtualizarRondaCommand(String nome, String descricao, List<PontoCommand> pontos) {

    public List<Ponto> toPontos() {
        if (pontos == null) {
            return null;
        }
        return pontos.stream()
                .map(ponto -> ponto == null ? null : ponto.toDomain())
                .toList();
    }
}
