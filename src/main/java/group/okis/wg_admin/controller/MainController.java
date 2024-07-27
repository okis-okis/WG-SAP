package group.okis.wg_admin.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import group.okis.wg_admin.service.ConfigService;
import group.okis.wg_admin.service.TerminalService;

@Controller
public class MainController {

    static Integer counter = 2;

    @Autowired
    ConfigService configService;

    @Autowired
    TerminalService terminalService;
    
    @RequestMapping(value={"", "/", "/configs"})
    public ModelAndView getConfListPage(){
        ModelAndView mv = new ModelAndView();

        try {
            List<String> files = configService.findFiles();
            mv.addObject("list", files);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        mv.setViewName("configurationList");
        return mv;
    }

    @GetMapping("/configs/create")
    public ModelAndView createConfig(){
        ModelAndView mv = new ModelAndView();
        mv.setViewName("createConfig");
        return mv;
    }

    @GetMapping("/configs/create/conf")
    public ModelAndView addFile(@RequestParam("configName") String fileName){
        fileName = getRightFileName(fileName);

        ModelAndView mv = new ModelAndView();

        try{
            terminalService.runCommand("/app/", "wg genkey | tee "+fileName+"_PrivateKey | wg pubkey > "+fileName+"_PublicKey");

            String serverPrivateKey     = configService.readFileWithoutExtensionAndWorkdir("/app/Server_PrivateKey");
            String serverPublicKey      = configService.readFileWithoutExtensionAndWorkdir("/app/Server_PublicKey");
            String clientPublicKey      = configService.readFileWithoutExtensionAndWorkdir("/app/"+fileName+"_PublicKey");
            String clientPrivateKey     = configService.readFileWithoutExtensionAndWorkdir("/app/"+fileName+"_PrivateKey");

            configService.createFile(fileName);

            String data = "[Interface]\n"+
            "PrivateKey = "+serverPrivateKey+"\n"+
            "Address = 10.0.0.1/24\n"+
            "PostUp = iptables -A FORWARD -i "+fileName+" -j ACCEPT; iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE\n"+
            "PostDown = iptables -D FORWARD -i "+fileName+" -j ACCEPT; iptables -t nat -D POSTROUTING -o eth0 -j MASQUERADE\n"+
            "ListenPort = 51820\n"+
            "\n"+
            "[Peer]\n"+
            "PublicKey = "+clientPublicKey+"\n"+
            "AllowedIPs = 10.0.0."+counter+"/32";

            configService.writeUsingFileWriter(fileName, data);

            terminalService.runCommand("/app/", "ip route list default");
            terminalService.runCommand("/app/", "wg-quick up "+fileName);
            terminalService.runCommand("/app/", "systemctl enable wg-quick@"+fileName);

            configService.createFileWithoutExtensionAndWorkdir("/app/clientConfigs/", "client"+fileName+".conf");

            String clientConfigData = "[Interface]\n"+
            "Address = 10.0.0."+(counter++)+"\n"+
            "PrivateKey = "+clientPrivateKey+"\n"+
            "DNS = 1.1.1.1\n"+
            "\n[Peer]\n"+
            "PublicKey = "+serverPublicKey+"\n"+
            "Endpoint = "+System.getenv("serverIP")+":51820\n"+
            "AllowedIPs = 0.0.0.0/0, ::/0";

            configService.writeUsingFileWriterWithoutWorkdir("/app/clientConfigs/client"+fileName, clientConfigData);
        
            mv.addObject("result", "Конфигурационный файл был успешно создан");
        }catch(Exception e){
            mv.addObject("result", "При создании конфигурационного файла произошла ошибка!");
        }

        mv.setViewName("result");
        
        return mv;
    }

    @GetMapping("/configs/delete/{fileName}")
    public ModelAndView deleteFile(@PathVariable String fileName){
        fileName = getRightFileName(fileName);

        ModelAndView mv = new ModelAndView();

        try{
            configService.deleteFile(fileName);
            mv.addObject("result", "Конфигурационный файл был успешно удалён");
        }catch(Exception e){
            mv.addObject("result", "При удалении конфигурационного файла произошла ошибка!");
        }

        mv.setViewName("result");
        
        return mv;
    }

    @GetMapping("/configs/read/{fileName}")
    public ModelAndView readFile(@PathVariable String fileName){
        fileName = getRightFileName(fileName);

        ModelAndView mv = new ModelAndView();

        mv.addObject("fileName", fileName);
        mv.addObject("content", configService.readFile(fileName).replace("\n", "<br>"));

        String clientConfig = configService.readFileWithoutExtensionAndWorkdir("/app/clientConfigs/client"+fileName+".conf");
        mv.addObject("clientConfig", clientConfig.replace("\n", "<br>"));

        mv.setViewName("configFileContent");
        
        return mv;
    }

    private String getRightFileName(String fileName){
        if(fileName.contains(".conf"))
            return fileName.replace(".conf", "");
        return fileName;
    }
}
