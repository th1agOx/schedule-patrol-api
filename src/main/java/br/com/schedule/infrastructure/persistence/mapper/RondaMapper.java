package br.com.schedule.infrastructure.persistence.mapper;

import br.com.schedule.domain.model.Ponto;
import br.com.schedule.domain.model.Ronda;
import br.com.schedule.infrastructure.persistence.document.PontoDocument;
import br.com.schedule.infrastructure.persistence.document.RondaDocument;

import java.util.List;
import java.util.Objects;

public final class RondaMapper {

    private RondaMapper() {
    }

    public static RondaDocument toDocument(Ronda ronda) {
        Objects.requireNonNull(ronda, "A ronda é obrigatória para o mapeamento.");

        List<PontoDocument> pontos = ronda.getPontos().stream()
                .map(ponto -> new PontoDocument(ponto.codigo(), ponto.descricao()))
                .toList();

        return new RondaDocument(ronda.getId(), ronda.getNome(), ronda.getDescricao(), ronda.isAtiva(), pontos);
    }

    public static Ronda toDomain(RondaDocument document) {
        Objects.requireNonNull(document, "O documento da ronda é obrigatório para o mapeamento.");

        List<Ponto> pontos = document.getPontos() == null
                ? List.of()
                : document.getPontos().stream().map(RondaMapper::toDomain).toList();

        return Ronda.reconstituir(document.getId(), document.getNome(), document.getDescricao(),
                document.isAtiva(), pontos);
    }

    private static Ponto toDomain(PontoDocument document) {
        if (document == null) {
            throw new IllegalStateException("A lista de pontos do documento contém um ponto nulo.");
        }
        return new Ponto(document.getCodigo(), document.getDescricao());
    }
}
