package com.asura.upload.service;



 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class UploadService {

    private static final List<String> CONTENT_TYPES=
            Arrays.asList("image/jpeg","image/gif","image/png");

    private static final Logger LOGGER=
           LoggerFactory.getLogger(UploadService.class);


    public String upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        //检验文件类型
        String contentType=file.getContentType();
        if(!CONTENT_TYPES.contains(contentType)){
            //文件类型不合法，直接返回null
            LOGGER.info("文件类型不合法：{}",originalFilename);
            return null;
        }

        try {
            BufferedImage bufferedImage=
                    ImageIO.read(file.getInputStream());
            if(bufferedImage==null){
                LOGGER.info("文件类型不合法：{}",originalFilename);
                return null;
            }
            File file1 = new File("I:\\leyou\\images\\" + originalFilename);
            if(!file1.exists()){
                file1.mkdirs();
            }
            //保存到服务器
            file.transferTo(file1);
            //生成url地址，返回
            /**
             *  为什么图片需要另外的url？
             *      图片不能保存在服务器内部，这样会对服务器产生额外的加载负担
             *      一般静态资源都应该使用独立域名，这样访问静态资源时不会携带一些不必要的coolie，减少请求的数据量
             */
            return "http://image.leyou.com/"+originalFilename;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
