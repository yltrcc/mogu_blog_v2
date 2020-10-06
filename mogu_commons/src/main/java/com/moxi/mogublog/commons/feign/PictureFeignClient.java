package com.moxi.mogublog.commons.feign;

import com.moxi.mogublog.commons.config.feign.FeignConfiguration;
import com.moxi.mogublog.commons.fallback.PictureFeignFallback;
import com.moxi.mougblog.base.vo.FileVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * mogu_picture相关接口
 *
 * @author 陌溪
 */
@FeignClient(name = "mogu-picture", configuration = FeignConfiguration.class,  fallback = PictureFeignFallback.class)
public interface PictureFeignClient {

    /**
     * 获取文件的信息接口
     * @param fileIds 图片uid
     * @param code 分隔符
     * @return
     */
    @RequestMapping(value = "/file/getPicture", method = RequestMethod.GET)
    String getPicture(@RequestParam("fileIds") String fileIds, @RequestParam("code") String code);

    /**
     * 通过URL List上传图片
     *
     * @param fileVO
     * @return
     */
    @RequestMapping(value = "/file/uploadPicsByUrl2", method = RequestMethod.POST)
    String uploadPicsByUrl(FileVO fileVO);

}