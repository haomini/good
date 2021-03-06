package com.example.zhiyicx.justdodagger2.data.remote;

import com.example.zhiyicx.justdodagger2.base.BaseBean;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

import static com.example.zhiyicx.justdodagger2.base.dagger.app.AppConfig.USER_LOGIN;

/**
 * @Describe
 * @Author zhouhao
 * @Date 2017/9/2
 * @Contact 605626708@qq.com
 */

public interface LoginClient {

    @FormUrlEncoded
    @POST(USER_LOGIN)
    Observable<BaseBean> login(@Field("user") String name,
                               @Field("pwd") String pwd);
}
