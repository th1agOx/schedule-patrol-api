package br.com.schedule.domain.model;

import br.com.schedule.domain.exception.PontoInvalidoException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PontoTest {

    @Test
    void shouldTrimCodeAndDescriptionOnCreation() {
        Ponto ponto = new Ponto("  P-01  ", "  Portão principal  ");

        assertThat(ponto.codigo()).isEqualTo("P-01");
        assertThat(ponto.descricao()).isEqualTo("Portão principal");
    }

    @Test
    void shouldRejectNullCode() {
        assertThatThrownBy(() -> new Ponto(null, "Portão principal"))
                .isExactlyInstanceOf(PontoInvalidoException.class)
                .hasMessageContaining("código");
    }

    @Test
    void shouldRejectBlankCode() {
        assertThatThrownBy(() -> new Ponto("   ", "Portão principal"))
                .isExactlyInstanceOf(PontoInvalidoException.class)
                .hasMessageContaining("código");
    }

    @Test
    void shouldRejectNullDescription() {
        assertThatThrownBy(() -> new Ponto("P-01", null))
                .isExactlyInstanceOf(PontoInvalidoException.class)
                .hasMessageContaining("descrição");
    }

    @Test
    void shouldRejectBlankDescription() {
        assertThatThrownBy(() -> new Ponto("P-01", "   "))
                .isExactlyInstanceOf(PontoInvalidoException.class)
                .hasMessageContaining("descrição");
    }

    @Test
    void shouldBeEqualByValue() {
        Ponto ponto = new Ponto("P-01", "Portão principal");

        assertThat(ponto).isEqualTo(new Ponto("P-01", "Portão principal"));
        assertThat(ponto).hasSameHashCodeAs(new Ponto("P-01", "Portão principal"));
        assertThat(ponto).isNotEqualTo(new Ponto("P-02", "Portão principal"));
        assertThat(ponto).isNotEqualTo(new Ponto("P-01", "Casa de válvulas"));
    }
}
