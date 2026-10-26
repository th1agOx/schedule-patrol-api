package br.com.schedule.domain.model;

import br.com.schedule.domain.exception.IdentificadorObrigatorioException;
import br.com.schedule.domain.exception.NomeObrigatorioException;
import br.com.schedule.domain.exception.PontoDuplicadoException;
import br.com.schedule.domain.exception.PontoInvalidoException;
import br.com.schedule.domain.exception.PontosObrigatoriosException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RondaTest {

    private static final Ponto PONTO_1 = new Ponto("P-01", "Portão principal");
    private static final Ponto PONTO_2 = new Ponto("P-02", "Casa de válvulas");

    @Test
    void shouldCreateActiveRondaWithGeneratedId() {
        Ronda ronda = Ronda.criar("Ronda Norte", "Inspeção diária", List.of(PONTO_1, PONTO_2));

        assertThat(ronda.getId()).isNotBlank();
        assertThat(UUID.fromString(ronda.getId())).isNotNull();
        assertThat(ronda.getNome()).isEqualTo("Ronda Norte");
        assertThat(ronda.getDescricao()).isEqualTo("Inspeção diária");
        assertThat(ronda.isAtiva()).isTrue();
        assertThat(ronda.podeSerAgendada()).isTrue();
        assertThat(ronda.getPontos()).containsExactly(PONTO_1, PONTO_2);
    }

    @Test
    void shouldNormalizeFieldsOnCreation() {
        Ronda ronda = Ronda.criar("  Ronda Sul  ", "   ", List.of(PONTO_1));

        assertThat(ronda.getNome()).isEqualTo("Ronda Sul");
        assertThat(ronda.getDescricao()).isNull();
    }

    @Test
    void shouldAcceptNullDescription() {
        Ronda ronda = Ronda.criar("Ronda Sul", null, List.of(PONTO_1));

        assertThat(ronda.getDescricao()).isNull();
    }

    @Test
    void shouldRejectNullName() {
        assertThatThrownBy(() -> Ronda.criar(null, null, List.of(PONTO_1)))
                .isExactlyInstanceOf(NomeObrigatorioException.class)
                .hasMessageContaining("nome da ronda");
    }

    @Test
    void shouldRejectBlankName() {
        assertThatThrownBy(() -> Ronda.criar("   ", null, List.of(PONTO_1)))
                .isExactlyInstanceOf(NomeObrigatorioException.class)
                .hasMessageContaining("nome da ronda");
    }

    @Test
    void shouldRejectNullPointList() {
        assertThatThrownBy(() -> Ronda.criar("Ronda Norte", null, null))
                .isExactlyInstanceOf(PontosObrigatoriosException.class)
                .hasMessageContaining("ao menos um ponto");
    }

    @Test
    void shouldRejectEmptyPointList() {
        assertThatThrownBy(() -> Ronda.criar("Ronda Norte", null, List.of()))
                .isExactlyInstanceOf(PontosObrigatoriosException.class)
                .hasMessageContaining("ao menos um ponto");
    }

    @Test
    void shouldRejectNullPointInsideList() {
        assertThatThrownBy(() -> Ronda.criar("Ronda Norte", null, Arrays.asList(PONTO_1, null)))
                .isExactlyInstanceOf(PontoInvalidoException.class)
                .hasMessageContaining("pontos nulos");
    }

    @Test
    void shouldRejectDuplicatedPointCodesIgnoringCaseAndSpaces() {
        List<Ponto> pontos = List.of(new Ponto("P-01", "Portão principal"), new Ponto("  p-01  ", "Portão secundário"));

        assertThatThrownBy(() -> Ronda.criar("Ronda Norte", null, pontos))
                .isExactlyInstanceOf(PontoDuplicadoException.class)
                .hasMessageContaining("duplicado");
    }

    @Test
    void shouldReturnImmutablePointList() {
        Ronda ronda = Ronda.criar("Ronda Norte", null, List.of(PONTO_1));

        assertThatThrownBy(() -> ronda.getPontos().add(PONTO_2))
                .isExactlyInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldUpdateNameDescriptionAndPointsPreservingIdAndActiveState() {
        Ronda ronda = Ronda.criar("Ronda Norte", "Inspeção diária", List.of(PONTO_1));
        String id = ronda.getId();
        ronda.desativar();

        ronda.atualizar("  Ronda Sul  ", "  Inspeção semanal  ", List.of(PONTO_2));

        assertThat(ronda.getId()).isEqualTo(id);
        assertThat(ronda.getNome()).isEqualTo("Ronda Sul");
        assertThat(ronda.getDescricao()).isEqualTo("Inspeção semanal");
        assertThat(ronda.getPontos()).containsExactly(PONTO_2);
        assertThat(ronda.isAtiva()).isFalse();
    }

    @Test
    void shouldNotChangeStateWhenUpdateIsInvalid() {
        Ronda ronda = Ronda.criar("Ronda Norte", "Inspeção diária", List.of(PONTO_1));

        assertThatThrownBy(() -> ronda.atualizar("   ", "Outra descrição", List.of(PONTO_2)))
                .isExactlyInstanceOf(NomeObrigatorioException.class);

        assertThat(ronda.getNome()).isEqualTo("Ronda Norte");
        assertThat(ronda.getDescricao()).isEqualTo("Inspeção diária");
        assertThat(ronda.getPontos()).containsExactly(PONTO_1);
    }

    @Test
    void shouldNotChangeStateWhenUpdatedPointsAreDuplicated() {
        Ronda ronda = Ronda.criar("Ronda Norte", "Inspeção diária", List.of(PONTO_1));
        List<Ponto> pontosDuplicados = List.of(new Ponto("P-02", "Casa de válvulas"),
                new Ponto("p-02", "Casa de válvulas 2"));

        assertThatThrownBy(() -> ronda.atualizar("Ronda Sul", "Nova", pontosDuplicados))
                .isExactlyInstanceOf(PontoDuplicadoException.class);

        assertThat(ronda.getNome()).isEqualTo("Ronda Norte");
        assertThat(ronda.getPontos()).containsExactly(PONTO_1);
    }

    @Test
    void shouldActivateAndDeactivateIdempotently() {
        Ronda ronda = Ronda.criar("Ronda Norte", null, List.of(PONTO_1));

        ronda.ativar();
        ronda.ativar();
        assertThat(ronda.isAtiva()).isTrue();
        assertThat(ronda.podeSerAgendada()).isTrue();

        ronda.desativar();
        ronda.desativar();
        assertThat(ronda.isAtiva()).isFalse();
        assertThat(ronda.podeSerAgendada()).isFalse();

        ronda.ativar();
        assertThat(ronda.podeSerAgendada()).isTrue();
    }

    @Test
    void shouldReconstituteWithGivenIdAndInactiveState() {
        Ronda ronda = Ronda.reconstituir("id-fixo", "  Ronda Norte  ", "  ", false, List.of(PONTO_1));

        assertThat(ronda.getId()).isEqualTo("id-fixo");
        assertThat(ronda.getNome()).isEqualTo("Ronda Norte");
        assertThat(ronda.getDescricao()).isNull();
        assertThat(ronda.isAtiva()).isFalse();
        assertThat(ronda.getPontos()).containsExactly(PONTO_1);
    }

    @Test
    void shouldValidateInvariantsOnReconstitution() {
        assertThatThrownBy(() -> Ronda.reconstituir("  ", "Ronda", null, true, List.of(PONTO_1)))
                .isExactlyInstanceOf(IdentificadorObrigatorioException.class)
                .hasMessageContaining("identificador da ronda");

        assertThatThrownBy(() -> Ronda.reconstituir("id", " ", null, true, List.of(PONTO_1)))
                .isExactlyInstanceOf(NomeObrigatorioException.class);

        assertThatThrownBy(() -> Ronda.reconstituir("id", "Ronda", null, true, List.of()))
                .isExactlyInstanceOf(PontosObrigatoriosException.class);
    }

    @Test
    void shouldBeEqualById() {
        Ronda ronda = Ronda.reconstituir("id-1", "Ronda Norte", null, true, List.of(PONTO_1));
        Ronda mesmaRonda = Ronda.reconstituir("id-1", "Outro nome", "Outra descrição", false, List.of(PONTO_2));
        Ronda outraRonda = Ronda.reconstituir("id-2", "Ronda Norte", null, true, List.of(PONTO_1));

        assertThat(ronda).isEqualTo(mesmaRonda);
        assertThat(ronda).hasSameHashCodeAs(mesmaRonda);
        assertThat(ronda).isNotEqualTo(outraRonda);
    }
}
