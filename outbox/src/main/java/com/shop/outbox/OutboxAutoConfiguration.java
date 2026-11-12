package com.shop.outbox;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Activates the outbox in any service that has this jar on its classpath.
 * <p>
 * <b>@AutoConfigurationPackage</b> adds com.shop.outbox to the packages
 * Spring Boot scans for JPA entities (OutboxEvent) and Spring Data repositories
 * (OutboxRepository). Without it, the service's own @SpringBootApplication
 * package is the only one scanned and startup fails with "Not a managed type".
 * <p>
 * before = HibernateJpaAutoConfiguration: the package must be registered before
 * JPA starts scanning.
 * <p>
 * The service still needs @EnableScheduling for the relay's @Scheduled poller to run.
 */
@AutoConfiguration(before = HibernateJpaAutoConfiguration.class)
@AutoConfigurationPackage
@Import({OutboxWriter.class, OutboxRelay.class})
public class OutboxAutoConfiguration {
}
