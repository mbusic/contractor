package hr.qnr.contractor.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Render's Postgres "connectionString" is a postgresql:// URI, which Spring's
 * spring.datasource.url can't consume directly (it needs a jdbc: URL). This builds
 * the DataSource by hand from the pieces of that URI instead.
 */
@Configuration
@Profile("render")
public class RenderDataSourceConfig {

    @Bean
    public DataSource dataSource(@Value("${DATABASE_URL}") String databaseUrl) throws Exception {
        URI uri = new URI(databaseUrl);
        String[] userInfo = uri.getUserInfo().split(":", 2);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://" + uri.getHost() + ":" + uri.getPort() + uri.getPath());
        config.setUsername(userInfo[0]);
        config.setPassword(userInfo[1]);
        config.setDriverClassName("org.postgresql.Driver");
        return new HikariDataSource(config);
    }
}
