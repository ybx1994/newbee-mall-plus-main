
package ltd.newbee.mall.controller.common;

import ltd.newbee.mall.common.NewBeeMallException;
import ltd.newbee.mall.util.HttpUtil;
import ltd.newbee.mall.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * newbee-mall全局异常处理
 */
@RestControllerAdvice
public class NewBeeMallExceptionHandler {

    Logger log = LoggerFactory.getLogger(NewBeeMallExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e, HttpServletRequest req) {
        Result result = new Result();
        result.setResultCode(500);
        // 区分是否为自定义异常
        if (e instanceof NewBeeMallException) {
            result.setMessage(e.getMessage());
        } else {
            log.error(e.getMessage(), e);
            result.setMessage("未知异常");
        }
        if (HttpUtil.isAjaxRequest(req)) {
            return result;
        }
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("message", e.getMessage());
        modelAndView.addObject("url", req.getRequestURL());
        modelAndView.addObject("stackTrace", e.getStackTrace());
        modelAndView.addObject("author", "bx");
        modelAndView.addObject("ltd", "宝林科商城");
        modelAndView.setViewName("error/error");
        return modelAndView;
    }
}
