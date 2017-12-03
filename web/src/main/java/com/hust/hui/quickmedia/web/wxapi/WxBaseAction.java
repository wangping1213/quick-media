package com.hust.hui.quickmedia.web.wxapi;

import com.hust.hui.quickmedia.common.constants.MediaType;
import com.hust.hui.quickmedia.common.util.Base64Util;
import com.hust.hui.quickmedia.web.entity.ResponseWrapper;
import com.hust.hui.quickmedia.web.entity.Status;
import com.hust.hui.quickmedia.web.wxapi.helper.ImgGenHelper;
import com.hust.hui.quickmedia.web.wxapi.validate.MediaValidate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Created by yihui on 2017/10/9.
 */
@Slf4j
public class WxBaseAction {

    protected BufferedImage getImg(HttpServletRequest request) {
        MultipartFile file = null;
        if (request instanceof MultipartHttpServletRequest) {
            file = ((MultipartHttpServletRequest) request).getFile("image");
        }

        if (file == null) {
            throw new IllegalArgumentException("图片不能为空!");
        }


        // 目前只支持 jpg, png, webp 等静态图片格式
        String contentType = file.getContentType();
        if (!MediaValidate.validateStaticImg(contentType)) {
            throw new IllegalArgumentException("不支持的图片类型");
        }

        // 获取BufferedImage对象
        try {
            BufferedImage bfImg = ImageIO.read(file.getInputStream());
            return bfImg;
        } catch (IOException e) {
            log.error("WxImgCreateAction!Parse img from httpRequest to BuferedImage error! e: {}", e);
            throw new IllegalArgumentException("不支持的图片类型!");
        }
    }


    /**
     * 根据传入的参数，确定返回url格式图片还是base64格式的图片
     * @param request
     * @param bf
     * @return
     */
    protected ResponseWrapper<WxBaseResponse> buildReturn(WxBaseRequest request, BufferedImage bf) {
        if(bf == null) {
            return ResponseWrapper.errorReturnMix(Status.StatusEnum.FAIL);
        }

        String ans;
        WxBaseResponse response = new WxBaseResponse();
        if (request.urlReturn()) {
            ans = ImgGenHelper.saveImg(bf);
            response.setImg(ans);
            response.setUrl("https://zbang.online/" + ans);
        } else {
            try {
                ans = Base64Util.encode(bf, "png");
                response.setBase64result(ans);
                response.setPrefix(MediaType.ImagePng.getPrefix());
            } catch (IOException e) {
                log.error("parse img to base64 error! req: {}, e:{}", request, e);
                ans = ImgGenHelper.saveImg(bf);
                response.setImg(ans);
                response.setUrl("https://zbang.online/" + ans);
            }
        }
        return ResponseWrapper.successReturn(response);
    }
}
