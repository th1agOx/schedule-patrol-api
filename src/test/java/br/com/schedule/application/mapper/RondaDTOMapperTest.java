package br.com.schedule.application.mapper;

import br.com.schedule.application.dto.PontoDTO;
import br.com.schedule.application.dto.response.RondaResponseDTO;
import br.com.schedule.domain.model.Ponto;
import br.com.schedule.domain.model.Ronda;
import org.junit.jupiter.api.Test;

import java.util.List;

import static br.com.schedule.application.ApplicationTestFixtures.ronda;
import static org.assertj.core.api.Assertions.assertThat;

class RondaDTOMapperTest {

    @Test
    void shouldMapPointDtosToDomain() {
        List<PontoDTO> pontos = List.of(
                new PontoDTO("P-01", "Portão principal"),
                new PontoDTO("P-02", "Casa de válvulas"));

        assertThat(RondaDTOMapper.paraPontos(pontos)).containsExactly(
                new Ponto("P-01", "Portão principal"),
                new Ponto("P-02", "Casa de válvulas"));
    }

    @Test
    void shouldMapNullPointsToNull() {
        assertThat(RondaDTOMapper.paraPontos(null)).isNull();
    }

    @Test
    void shouldMapRondaToResponse() {
        Ronda ronda = ronda("ronda-1");

        RondaResponseDTO response = RondaDTOMapper.paraResponse(ronda);

        assertThat(response.id()).isEqualTo("ronda-1");
        assertThat(response.nome()).isEqualTo("Ronda Norte");
        assertThat(response.descricao()).isEqualTo("Inspeção diária");
        assertThat(response.ativa()).isTrue();
        assertThat(response.pontos()).containsExactly(new PontoDTO("P-01", "Portão principal"));
    }
}
