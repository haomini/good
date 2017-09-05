package com.example.zhiyicx.justdodagger2.data.remote;

import javax.inject.Inject;
import javax.inject.Singleton;


/**
 * @Describe
 * @Author Jungle68
 * @Date 2016/12/16
 * @Contact 335891510@qq.com
 */

@Singleton
public class ServiceManager {
    private LoginClient mLoginClient;
    private RegisterClient mRegisterClient;

    /**
     * 如果需要添加 service 只需在构造方法中添加对应的 service,在提供 get 方法返回出去,只要在 ServiceModule 提供了该 service
     * Dagger2 会自行注入
     *
     */
    @Inject
    public ServiceManager(LoginClient loginClient,
                          RegisterClient registerClient) {
        this.mLoginClient = loginClient;
        this.mRegisterClient = registerClient;
    }

    public LoginClient getLoginClient() {
        return mLoginClient;
    }

    public RegisterClient getRegisterClient(){
        return mRegisterClient;
    }

}
