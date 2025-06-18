package br.com.product.nextdomtest.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "NextDom Test - API de Controle de Estoque",
                description = "API para gerenciamento de produtos e movimentações de estoque com controle de lucro",
                version = "1.0.0",
                contact = @Contact(
                        name = "NextDom Test Team",
                        email = "contato@nextdomtest.com"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Servidor de Desenvolvimento"),
                @Server(url = "http://localhost:8080/h2-console", description = "Console H2 Database")
        }
)
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("NextDom Test - API de Controle de Estoque")
                        .description("""
                            Esta API oferece funcionalidades completas para:
                            
                            • **Gestão de Produtos**: CRUD completo com categorização por tipo
                            • **Controle de Estoque**: Movimentações de entrada e saída
                            • **Análise de Lucro**: Consultas detalhadas de rentabilidade
                            
                            **Banco de Dados**: H2 em memória para testes
                            
                            **Console H2**: Disponível em `/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`, User: `sa`)
                            """)
                        .version("1.0.0"));
    }
}