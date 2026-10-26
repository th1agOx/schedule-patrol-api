package br.com.schedule.infrastructure.persistence.mapper;

import br.com.schedule.domain.exception.PontosObrigatoriosException;
import br.com.schedule.domain.model.Ponto;
import br.com.schedule.domain.model.Ronda;
import br.com.schedule.infrastructure.persistence.document.PontoDocument;
import br.com.schedule.infrastructure.persistence.document.RondaDocument;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RondaMapperTest {

    @Test
    void shouldMapDomainToDocument() {
        Ronda ronda = Ronda.reconstituir("ronda-1", "Ronda Norte", "Inspeção diária", true,
                List.of(new Ponto("P-01", "Portão principal"), new Ponto("P-02", "Casa de válvulas")));

        RondaDocument document = RondaMapper.toDocument(ronda);

        assertThat(document.getId()).isEqualTo("ronda-1");
        assertThat(document.getNome()).isEqualTo("Ronda Norte");
        assertThat(document.getDescricao()).isEqualTo("Inspeção diária");
        assertThat(document.isAtiva()).isTrue();
        assertThat(document.getPontos()).extracting(PontoDocument::getCodigo).containsExactly("P-01", "P-02");
        assertThat(document.getPontos()).extracting(PontoDocument::getDescricao)
                .containsExactly("Portão principal", "Casa de válvulas");
    }

    @Test
    void shouldMapDocumentToDomain() {
        RondaDocument document = new RondaDocument("ronda-1", "Ronda Norte", "Inspeção diária", false,
                List.of(new PontoDocument("P-01", "Portão principal")));

        Ronda ronda = RondaMapper.toDomain(document);

        assertThat(ronda.getId()).isEqualTo("ronda-1");
        assertThat(ronda.getNome()).isEqualTo("Ronda Norte");
        assertThat(ronda.getDescricao()).isEqualTo("Inspeção diária");
        assertThat(ronda.isAtiva()).isFalse();
        assertThat(ronda.getPontos()).containsExactly(new Ponto("P-01", "Portão principal"));
    }

    @Test
    void shouldPreserveNullDescription() {
        Ronda ronda = Ronda.reconstituir("ronda-1", "Ronda Norte", null, true,
                List.of(new Ponto("P-01", "Portão principal")));

        RondaDocument document = RondaMapper.toDocument(ronda);

        assertThat(document.getDescricao()).isNull();
        assertThat(RondaMapper.toDomain(document).getDescricao()).isNull();
    }

    @Test
    void shouldRejectDocumentWithoutPoints() {
        RondaDocument semPontos = new RondaDocument("ronda-1", "Ronda Norte", null, true, List.of());
        RondaDocument pontosNulos = new RondaDocument("ronda-1", "Ronda Norte", null, true, null);

        assertThatThrownBy(() -> RondaMapper.toDomain(semPontos))
                .isExactlyInstanceOf(PontosObrigatoriosException.class);
        assertThatThrownBy(() -> RondaMapper.toDomain(pontosNulos))
                .isExactlyInstanceOf(PontosObrigatoriosException.class);
    }

    @Test
    void shouldRejectNullPointInsideDocument() {
        RondaDocument document = new RondaDocument("ronda-1", "Ronda Norte", null, true,
                Arrays.asList(new PontoDocument("P-01", "Portão principal"), null));

        assertThatThrownBy(() -> RondaMapper.toDomain(document))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ponto nulo");
    }
}
