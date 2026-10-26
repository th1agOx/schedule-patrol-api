package br.com.schedule.domain.service;

import br.com.schedule.domain.exception.RondaComAgendamentosAtivosException;
import br.com.schedule.domain.repository.AgendamentoRondaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PoliticaDeExclusaoDeRondaTest {

    @Mock
    private AgendamentoRondaRepository agendamentoRepository;

    private PoliticaDeExclusaoDeRonda politica;

    @BeforeEach
    void setUp() {
        politica = new PoliticaDeExclusaoDeRonda(agendamentoRepository);
    }

    @Test
    void shouldRejectExclusionWhenThereIsAnActiveAgendamento() {
        when(agendamentoRepository.existeAtivoParaRonda("ronda-1")).thenReturn(true);

        assertThatThrownBy(() -> politica.verificarExclusao("ronda-1"))
                .isExactlyInstanceOf(RondaComAgendamentosAtivosException.class)
                .hasMessageContaining("ronda-1");
    }

    @Test
    void shouldAllowExclusionWhenThereIsNoActiveAgendamento() {
        when(agendamentoRepository.existeAtivoParaRonda("ronda-1")).thenReturn(false);

        assertThatCode(() -> politica.verificarExclusao("ronda-1")).doesNotThrowAnyException();
    }

    @Test
    void shouldQueryActiveAgendamentosByRonda() {
        when(agendamentoRepository.existeAtivoParaRonda("ronda-1")).thenReturn(false);

        politica.verificarExclusao("ronda-1");

        verify(agendamentoRepository).existeAtivoParaRonda("ronda-1");
    }
}
