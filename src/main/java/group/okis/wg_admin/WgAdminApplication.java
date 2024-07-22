package group.okis.wg_admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WgAdminApplication {
	//builder.command("sh", "-c", "wg pubkey < /app/Server_PrivateKey > /app/Server_PublicKey");
	public static void main(String[] args) {
		SpringApplication.run(WgAdminApplication.class, args);
	}
}
