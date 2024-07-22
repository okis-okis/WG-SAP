package group.okis.wg_admin.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

@Service
public class ConfigService {

    String workdir = "/etc/wireguard/";
    String fileExtension = "conf";

    public List<String> findFiles()
        throws IOException {

        Path path = Paths.get(workdir);

        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path must be a directory!");
        }

        List<String> result;

        try (Stream<Path> walk = Files.walk(path)) {
            result = walk
                    .filter(p -> !Files.isDirectory(p))
                    // this is a path, not string,
                    // this only test if path end with a certain path
                    //.filter(p -> p.endsWith(fileExtension))
                    // convert path to string first
                    .map(p -> p.getFileName().toString().toLowerCase())
                    .filter(f -> f.endsWith(fileExtension))
                    .collect(Collectors.toList());
        }

        return result;
    }

    public void createFile(String fileName) throws IOException{
        createFileWithoutExtension(fileName+"."+fileExtension);
    }

    public void createFileWithoutExtension(String fileName) throws IOException{
        File myObj = new File(workdir+fileName);
        if (myObj.createNewFile()) {
            System.out.println("File created: " + myObj.getName());
        } else {
            System.out.println("File already exists.");
        }
    }

    public String readFile(String fileName) {
        return readFileWithoutExtension(fileName+"."+fileExtension);
    }

    public String readFileWithoutExtension(String fileName) {
        return readFileWithoutExtensionAndWorkdir(workdir+fileName);
    }

    public String readFileWithoutExtensionAndWorkdir(String fileName) {
        File file;
        file = new File(fileName);
        StringBuilder strBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                strBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strBuilder.toString();
    }

    public void writeUsingFileWriter(String fileName, String data) 
    throws IOException{
        File file = new File(workdir+fileName+"."+fileExtension);
        FileWriter fr = new FileWriter(file);
        fr.write(data);
        fr.close();
    }

    public void deleteFile(String fileName) throws IOException{
        // File or Directory to be deleted
		Path path = Paths.get(workdir+fileName+"."+fileExtension);
		// Delete file or directory
		Files.delete(path);
    }

    public Boolean checkFileExist(String fileName){
        Path path = Paths.get(workdir+fileName+"."+fileExtension);
        return Files.exists(path);
    }
}
