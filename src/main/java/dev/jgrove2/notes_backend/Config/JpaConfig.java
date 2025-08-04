package dev.jgrove2.notes_backend.Config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "dev.jgrove2.notes_backend.Repositories")
@EntityScan(basePackages = "dev.jgrove2.notes_backend.Models")
public class JpaConfig {

    // This configuration ensures that:
    // 1. JPA repositories are scanned in the correct package
    // 2. Entities are scanned in the correct package
    // 3. Transaction management is enabled
}