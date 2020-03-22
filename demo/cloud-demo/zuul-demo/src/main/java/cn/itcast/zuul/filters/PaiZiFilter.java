package cn.itcast.zuul.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

@Component
public class PaiZiFilter extends ZuulFilter {

    //4个值，最常用的肯定是前置

    /**
     * //pre
     * //route
     * //post
     * //error
     * <p>
     * pre=====>route==========>post
     * <p>
     * pre===>error====>post
     * <p>
     * pre====>route=====>error====>post
     * <p>
     * pre====>route=====>post====>error
     *
     * @return
     */

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE; //设置路由得执行时机
    }

    @Override
    public int filterOrder() {//次序，load-on-startup,越小越早，
        return FilterConstants.PRE_DECORATION_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {//过滤器是否生效，返回true，表示生效，返回false表示不生效
        return true;
    }

    @Override
    public Object run() throws ZuulException {//拦截后真正执行的业务，放行不用额外操作
        //人 request，牌子，parameter,context:资源管理器，，，applicationContext,WebApplicationContext,ServletContext

        //获取当前请求对应的资源管理器
        RequestContext currentContext = RequestContext.getCurrentContext();

        //从资源管理器中获取当前请求的额对象
        HttpServletRequest request = currentContext.getRequest();


        String paizi = request.getParameter("paizi");

        if (StringUtils.isEmpty(paizi)){//不符合要求，要进行拦截，结果就是不响应
            currentContext.setSendZuulResponse(false);//不响应
            currentContext.setResponseStatusCode(401);//返回一个状态码，401，未授权，403，禁止访问
        }

        return null;
    }
}
