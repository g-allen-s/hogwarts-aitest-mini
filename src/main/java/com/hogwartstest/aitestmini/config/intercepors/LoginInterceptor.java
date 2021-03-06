package com.hogwartstest.aitestmini.config.intercepors;

import com.hogwartstest.aitestmini.common.ServiceException;
import com.hogwartstest.aitestmini.common.TokenDb;
import com.hogwartstest.aitestmini.constants.UserConstants;
import com.hogwartstest.aitestmini.dto.TokenDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * @Author tlibn
 * @Date 2020/6/12 21:13
 **/

@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenDb tokenDb;

    //这个方法是在访问接口之前执行的，我们只需要在这里写验证登陆状态的业务逻辑，就可以在用户调用指定接口之前验证登陆状态了
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String tokenStr = request.getHeader(UserConstants.LOGIN_TOKEN);

        String requestUri = request.getRequestURI();
        log.info("request.getRequestURI() " + requestUri);

        //如果为swagger文档地址,直接通过
        boolean swaggerFlag = requestUri.contains("swagger")
                //过滤spring默认错误页面
                || requestUri.equals("/error")
                //过滤csrf
                || requestUri.equals("/csrf")
                //过滤http://127.0.0.1:8093/v2/api-docs
                || requestUri.equals("/favicon.ico")
                //演示map local 不用校验是否登录
                || requestUri.equals("/report/showMapLocal")
                || requestUri.equals("/");
        if(swaggerFlag){
            return true;
        }

        //如果请求中含有token
        if(StringUtils.isEmpty(tokenStr)){
            response.setStatus(401);
            ServiceException.throwEx("客户端未传token "+requestUri);
        }

        //获取token
        TokenDto tokenDto = tokenDb.getTokenDto(tokenStr);
        //如果user未登录
        if (Objects.isNull(tokenDto)){
            //这个方法返回false表示忽略当前请求，如果一个用户调用了需要登陆才能使用的接口，如果他没有登陆这里会直接忽略掉
            //当然你可以利用response给用户返回一些提示信息，告诉他没登陆
            //此处直接抛出异常
            response.setStatus(401);
            ServiceException.throwEx("用户未登录");
            return false;
        }else {
            return true;    //如果session里有user，表示该用户已经登陆，放行，用户即可继续调用自己需要的接口
        }

    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {

    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {

    }

}
