package br.com.schedule.infrastructure.config;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class ClockConfigTest {

    @Test
    void shouldProvideUtcClock() {
        Clock clock = new ClockConfig().clock();

        assertThat(clock.getZone()).isEqualTo(ZoneOffset.UTC);
        assertThat(clock.instant()).isNotNull();
    }
}
