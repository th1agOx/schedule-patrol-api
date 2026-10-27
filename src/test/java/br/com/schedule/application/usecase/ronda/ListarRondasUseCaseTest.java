package br.com.schedule.application.usecase.ronda;

import br.com.schedule.application.query.ListarRondasQuery;
import br.com.schedule.application.response.RondaResponse;
import br.com.schedule.domain.repository.RondaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static br.com.schedule.application.ApplicationTestFixtures.ronda;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarRondasUseCaseTest {

    @Mock
    private RondaRepository rondaRepository;

    private ListarRondasUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListarRondasUseCase(rondaRepository);
    }

    @Test
    void shouldListAllWhenFilterIsNull() {
        when(rondaRepository.listarTodas()).thenReturn(List.of(ronda("ronda-1"), ronda("ronda-2", false)));

        List<RondaResponse> responses = useCase.executar(new ListarRondasQuery(null));

        assertThat(responses).extracting(RondaResponse::id).containsExactly("ronda-1", "ronda-2");
        verify(rondaRepository).listarTodas();
        verify(rondaRepository, never()).listarPorAtiva(anyBoolean());
    }

    @Test
    void shouldListByActiveWhenFilterIsProvided() {
        when(rondaRepository.listarPorAtiva(false)).thenReturn(List.of(ronda("ronda-2", false)));

        List<RondaResponse> responses = useCase.executar(new ListarRondasQuery(false));

        assertThat(responses).extracting(RondaResponse::id).containsExactly("ronda-2");
        verify(rondaRepository).listarPorAtiva(false);
        verify(rondaRepository, never()).listarTodas();
    }
}
