package br.com.schedule.application.mapper;

import br.com.schedule.application.dto.PontoDTO;
import br.com.schedule.application.dto.response.RondaResponseDTO;
import br.com.schedule.domain.model.Ponto;
import br.com.schedule.domain.model.Ronda;

import java.util.List;

public final class RondaDTOMapper {

    private RondaDTOMapper() {
    }

    public static List<Ponto> paraPontos(List<PontoDTO> pontos) {
        if (pontos == null) {
            return null;
        }
        return pontos.stream()
                .map(ponto -> ponto == null ? null : new Ponto(ponto.codigo(), ponto.descricao()))
                .toList();
    }

    public static RondaResponseDTO paraResponse(Ronda ronda) {
        List<PontoDTO> pontos = ronda.getPontos().stream()
                .map(ponto -> new PontoDTO(ponto.codigo(), ponto.descricao()))
                .toList();

        return new RondaResponseDTO(ronda.getId(), ronda.getNome(), ronda.getDescricao(), ronda.isAtiva(), pontos);
    }
}
