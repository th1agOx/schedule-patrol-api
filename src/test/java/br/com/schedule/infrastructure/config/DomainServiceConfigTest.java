package br.com.schedule.infrastructure.config;

import br.com.schedule.domain.repository.AgendamentoRondaRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DomainServiceConfigTest {

    @Test
    void shouldCreateDomainServices() {
        AgendamentoRondaRepository repository = mock(AgendamentoRondaRepository.class);
        DomainServiceConfig config = new DomainServiceConfig();

        assertThat(config.verificadorDeConflitoDeAgenda(repository)).isNotNull();
        assertThat(config.politicaDeExclusaoDeRonda(repository)).isNotNull();
    }
}
