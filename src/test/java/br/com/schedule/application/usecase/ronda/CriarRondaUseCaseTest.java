package br.com.schedule.application.usecase.ronda;

import br.com.schedule.application.command.CriarRondaCommand;
import br.com.schedule.application.command.PontoCommand;
import br.com.schedule.application.response.PontoResponse;
import br.com.schedule.application.response.RondaResponse;
import br.com.schedule.domain.exception.PontoDuplicadoException;
import br.com.schedule.domain.model.Ronda;
import br.com.schedule.domain.repository.RondaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriarRondaUseCaseTest {

    @Mock
    private RondaRepository rondaRepository;

    private CriarRondaUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CriarRondaUseCase(rondaRepository);
    }

    @Test
    void shouldCreateAndPersistRonda() {
        CriarRondaCommand command = new CriarRondaCommand("Ronda Norte", "Inspeção diária",
                List.of(new PontoCommand("P-01", "Portão principal")));
        when(rondaRepository.salvar(any(Ronda.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RondaResponse response = useCase.executar(command);

        assertThat(response.id()).isNotBlank();
        assertThat(response.nome()).isEqualTo("Ronda Norte");
        assertThat(response.descricao()).isEqualTo("Inspeção diária");
        assertThat(response.ativa()).isTrue();
        assertThat(response.pontos()).containsExactly(new PontoResponse("P-01", "Portão principal"));

        ArgumentCaptor<Ronda> captor = ArgumentCaptor.forClass(Ronda.class);
        verify(rondaRepository).salvar(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(response.id());
        assertThat(captor.getValue().isAtiva()).isTrue();
    }

    @Test
    void shouldNormalizeBlankDescription() {
        CriarRondaCommand command = new CriarRondaCommand("Ronda Norte", "   ",
                List.of(new PontoCommand("P-01", "Portão principal")));
        when(rondaRepository.salvar(any(Ronda.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RondaResponse response = useCase.executar(command);

        assertThat(response.descricao()).isNull();
    }

    @Test
    void shouldPropagateDuplicatedPoint() {
        CriarRondaCommand command = new CriarRondaCommand("Ronda Norte", null,
                List.of(new PontoCommand("P-01", "Portão principal"),
                        new PontoCommand("p-01", "Portão secundário")));

        assertThatThrownBy(() -> useCase.executar(command))
                .isExactlyInstanceOf(PontoDuplicadoException.class);

        verify(rondaRepository, never()).salvar(any());
    }
}
