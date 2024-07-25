package group.okis.wg_admin.service;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

@Service
public class TerminalService {
    
    public void runCommand(String workdir, String command){
		try {
			ProcessBuilder builder = new ProcessBuilder();
        	builder.directory(new File(workdir));
			builder.command("sh", "-c", command);
			

			Process process = builder.start();
			boolean isFinished = process.waitFor(30, TimeUnit.SECONDS);

			if(!isFinished) {
				process.destroyForcibly();
			}
        } catch (Exception ex) {
			System.out.println("An error occurred while executing Linux terminal commands");
        }
	}
}
