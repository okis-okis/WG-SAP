package group.okis.wg_admin.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @RequestMapping("/")
    public ModelAndView getMainPage(){
        ModelAndView mv = new ModelAndView();
        mv.setViewName("index");
        return mv;
    }

    @RequestMapping("/configs")
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

    @GetMapping("/configs/create/{fileName}")
    public ModelAndView addFile(@PathVariable String fileName){
        ModelAndView mv = new ModelAndView();

        try{
            configService.createFile(fileName);
            mv.addObject("result", "Конфигурационный файл был успешно создан");
        }catch(Exception e){
            mv.addObject("result", "При создании конфигурационного файла произошла ошибка!");
        }

        mv.setViewName("result");
        
        return mv;
    }

    @GetMapping("/configs/delete/{fileName}")
    public ModelAndView deleteFile(@PathVariable String fileName){
        ModelAndView mv = new ModelAndView();

        try{
            configService.deleteFile(fileName);
            mv.addObject("result", "Конфигурационный файл был успешно удалён");
        }catch(Exception e){
            mv.addObject("result", "При удалении конфигурационного файла произошла ошибка!");
        }

        mv.setViewName("success");
        
        return mv;
    }

    @GetMapping("/configs/write/{fileName}")
    public ModelAndView writeToFileConfigData(@PathVariable String fileName){
        ModelAndView mv = new ModelAndView();

        // terminalService.runCommand("/app/", "wg genkey | tee "+fileName+"_PrivateKey | wg pubkey > "+fileName+"_PublicKey");

        String serverPrivateKey = configService.readFileWithoutExtensionAndWorkdir("/app/Server_PrivateKey");
        String serverPublicKey = configService.readFileWithoutExtensionAndWorkdir("/app/Server_PublicKey");
        String clientPublicKey = configService.readFileWithoutExtensionAndWorkdir("/app/"+fileName+"_PublicKey");
        String clientPrivateKey = configService.readFileWithoutExtensionAndWorkdir("/app/"+fileName+"_PrivateKey");

        try{
            String data = "[Interface]\n"+
            "Address = 192.168.2.1\n"+
            "PrivateKey = "+serverPrivateKey+"\n"+
            "ListenPort = 51820\n"+
            "\n"+
            "[Peer]\n"+
            "PublicKey = "+clientPublicKey+"\n"+
            "AllowedIPs = 192.168.2."+counter;

            configService.writeUsingFileWriter(fileName, data);

            configService.createFile("client"+fileName);

            String clientConfigData = "[Interface]\n"+
            "Address = 192.168.2."+(counter++)+"\n"+
            "PrivateKey = "+clientPrivateKey+"\n"+
            "\n[Peer]\n"+
            "PublicKey = "+serverPublicKey+"\n"+
            "Endpoint = <server's ip>:51820\n"+
            "AllowedIPs = 192.168.2.0/24";

            configService.writeUsingFileWriter("client"+fileName, clientConfigData);

            mv.addObject("result", "Конфигурационный файл был успешно обновлён. Настройка произведена");
        }catch(Exception e){
            mv.addObject("result", "Ошибка настройки конфигурации файла!");
        }

        mv.setViewName("success");
        
        return mv;
    }

    @GetMapping("/configs/read/{fileName}")
    public ModelAndView readFile(@PathVariable String fileName){
        ModelAndView mv = new ModelAndView();

        mv.addObject("fileName", fileName);
        mv.addObject("content", configService.readFile(fileName));

        String clientConfig = "";
        mv.addObject("clientConfig", clientConfig);

        mv.setViewName("configFileContent");
        
        return mv;
    }
}
