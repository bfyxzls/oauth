package com.lind.oauth.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by SuperS on 2017/9/26.
 *
 * @author SuperS
 */
@FrameworkEndpoint
public class RevokeTokenEndpoint {

    @Autowired
    @Qualifier("consumerTokenServices")
    ConsumerTokenServices consumerTokenServices;
    @RequestMapping(method = RequestMethod.GET, value = "/oauth/del-token")
    public String revokeToken(@RequestParam String access_token) {
        if (consumerTokenServices.revokeToken(access_token)) {
            return "注销成功";
        } else {
            return "注销失败";
        }
    }
}
