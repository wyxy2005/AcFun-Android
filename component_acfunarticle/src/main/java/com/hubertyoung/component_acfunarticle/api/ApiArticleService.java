package com.hubertyoung.component_acfunarticle.api;


import com.hubertyoung.common.net.response.BaseResponse;
import com.hubertyoung.component_acfunarticle.entity.Channel;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * <br>
 * function:
 * <p>
 *
 * @author:Yang
 * @date:2018/5/9 11:33 AM
 * @since:V1.0
 * @desc:api
 */
public interface ApiArticleService {

	@GET( "v3/channels/allChannels" )
	Observable< BaseResponse< Channel > > requestAllChannel(  @QueryMap Map< String, String > map );
}
