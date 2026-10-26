package br.com.schedule.infrastructure.config;

import br.com.schedule.domain.repository.AgendamentoRondaRepository;
import br.com.schedule.domain.service.PoliticaDeExclusaoDeRonda;
import br.com.schedule.domain.service.VerificadorDeConflitoDeAgenda;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class DomainServiceConfig {

    @Bean
    public VerificadorDeConflitoDeAgenda verificadorDeConflitoDeAgenda(
            AgendamentoRondaRepository agendamentoRepository) {
        return new VerificadorDeConflitoDeAgenda(agendamentoRepository);
    }

    @Bean
    public PoliticaDeExclusaoDeRonda politicaDeExclusaoDeRonda(AgendamentoRondaRepository agendamentoRepository) {
        return new PoliticaDeExclusaoDeRonda(agendamentoRepository);
    }
}
