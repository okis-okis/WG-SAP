package group.okis.wg_admin.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    @Bean
   public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        
        // HeaderWriterLogoutHandler clearSiteData = new HeaderWriterLogoutHandler(new ClearSiteDataHeaderWriter());

        http
                .authorizeHttpRequests((requests) -> requests
                      .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                      .loginPage("/login")
                      .permitAll()
                )
                .logout((logout) -> logout
                      .logoutUrl("/logout")
                      .logoutSuccessUrl("/")
                      .invalidateHttpSession(true)
                      .deleteCookies("JSESSIONID")
                    //   .addLogoutHandler(clearSiteData)
                )
                .csrf(Customizer.withDefaults());

      return http.build();
   }
}
