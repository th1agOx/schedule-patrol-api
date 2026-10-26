package br.com.schedule.domain.model;

import br.com.schedule.domain.exception.PeriodoInvalidoException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PeriodoTest {

    private static final Instant OITO_HORAS = Instant.parse("2026-11-05T08:00:00Z");
    private static final Instant DEZ_HORAS = Instant.parse("2026-11-05T10:00:00Z");
    private static final Instant DOZE_HORAS = Instant.parse("2026-11-05T12:00:00Z");
    private static final Instant QUATORZE_HORAS = Instant.parse("2026-11-05T14:00:00Z");

    @Test
    void shouldCreateValidPeriod() {
        Periodo periodo = new Periodo(OITO_HORAS, DEZ_HORAS);

        assertThat(periodo.inicio()).isEqualTo(OITO_HORAS);
        assertThat(periodo.fim()).isEqualTo(DEZ_HORAS);
    }

    @Test
    void shouldRejectPeriodWithEqualStartAndEnd() {
        assertThatThrownBy(() -> new Periodo(OITO_HORAS, OITO_HORAS))
                .isExactlyInstanceOf(PeriodoInvalidoException.class)
                .hasMessageContaining("anterior ao fim");
    }

    @Test
    void shouldRejectPeriodWithStartAfterEnd() {
        assertThatThrownBy(() -> new Periodo(DEZ_HORAS, OITO_HORAS))
                .isExactlyInstanceOf(PeriodoInvalidoException.class)
                .hasMessageContaining("anterior ao fim");
    }

    @Test
    void shouldRejectPeriodWithoutStart() {
        assertThatThrownBy(() -> new Periodo(null, DEZ_HORAS))
                .isExactlyInstanceOf(PeriodoInvalidoException.class)
                .hasMessageContaining("início e fim");
    }

    @Test
    void shouldRejectPeriodWithoutEnd() {
        assertThatThrownBy(() -> new Periodo(OITO_HORAS, null))
                .isExactlyInstanceOf(PeriodoInvalidoException.class)
                .hasMessageContaining("início e fim");
    }

    @Test
    void shouldOverlapWhenPeriodsSharePartOfTheRange() {
        Periodo manha = new Periodo(OITO_HORAS, DEZ_HORAS);
        Periodo meioDaManha = new Periodo(DEZ_HORAS.minusSeconds(1800), DOZE_HORAS);

        assertThat(manha.sobrepoe(meioDaManha)).isTrue();
        assertThat(meioDaManha.sobrepoe(manha)).isTrue();
    }

    @Test
    void shouldOverlapWhenOnePeriodContainsTheOther() {
        Periodo maior = new Periodo(OITO_HORAS, QUATORZE_HORAS);
        Periodo menor = new Periodo(DEZ_HORAS, DOZE_HORAS);

        assertThat(maior.sobrepoe(menor)).isTrue();
        assertThat(menor.sobrepoe(maior)).isTrue();
    }

    @Test
    void shouldOverlapWhenPeriodsAreEqual() {
        Periodo periodo = new Periodo(OITO_HORAS, DEZ_HORAS);

        assertThat(periodo.sobrepoe(new Periodo(OITO_HORAS, DEZ_HORAS))).isTrue();
    }

    @Test
    void shouldNotOverlapWhenPeriodsOnlyTouch() {
        Periodo manha = new Periodo(OITO_HORAS, DEZ_HORAS);
        Periodo tarde = new Periodo(DEZ_HORAS, DOZE_HORAS);

        assertThat(manha.sobrepoe(tarde)).isFalse();
        assertThat(tarde.sobrepoe(manha)).isFalse();
    }

    @Test
    void shouldNotOverlapWhenPeriodsAreDisjoint() {
        Periodo manha = new Periodo(OITO_HORAS, DEZ_HORAS);
        Periodo tarde = new Periodo(DOZE_HORAS, QUATORZE_HORAS);

        assertThat(manha.sobrepoe(tarde)).isFalse();
        assertThat(tarde.sobrepoe(manha)).isFalse();
    }

    @Test
    void shouldRejectNullComparisonPeriod() {
        Periodo periodo = new Periodo(OITO_HORAS, DEZ_HORAS);

        assertThatThrownBy(() -> periodo.sobrepoe(null))
                .isExactlyInstanceOf(PeriodoInvalidoException.class)
                .hasMessageContaining("comparação");
    }
}
