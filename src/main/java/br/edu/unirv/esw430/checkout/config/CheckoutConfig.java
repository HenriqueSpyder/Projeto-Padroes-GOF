package br.edu.unirv.esw430.checkout.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({FreteProperties.class, ImpostoProperties.class})
public class CheckoutConfig {
}
