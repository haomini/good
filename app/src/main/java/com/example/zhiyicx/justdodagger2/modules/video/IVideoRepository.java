package com.example.zhiyicx.justdodagger2.modules.video;

import com.example.zhiyicx.justdodagger2.base.BaseBean;
import com.example.zhiyicx.justdodagger2.data.bean.Video;

import java.util.List;

import rx.Observable;

/**
 * @Describe
 * @Author zhouhao
 * @Date 2017/9/6
 * @Contact 605626708@qq.com
 */

public interface IVideoRepository {
    Observable<BaseBean<List<Video>>> getVideoList();
}
