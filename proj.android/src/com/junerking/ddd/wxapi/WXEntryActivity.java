package com.junerking.ddd.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.junerking.ddd.LogUtils;
import com.junerking.ddd.WeChatInfo;
import com.junerking.ddd.utils.OkHttpUtils;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 微信登录页面
 *
 * @author kevin_chen 2016-12-10 下午19:03:45
 * @version v1.0
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    private static final String APP_SECRET = "填写自己的AppSecret";
    private IWXAPI mWeixinAPI;
    public static final String WEIXIN_APP_ID = "填写自己的APP_id";
    private static String uuid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWeixinAPI = WXAPIFactory.createWXAPI(this, WEIXIN_APP_ID, true);
        mWeixinAPI.handleIntent(this.getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        mWeixinAPI.handleIntent(intent, this);//必须调用此句话
    }

    //微信发送的请求将回调到onReq方法
    @Override
    public void onReq(BaseReq req) {
        LogUtils.log("onReq");
    }

    //发送到微信请求的响应结果
    @Override
    public void onResp(BaseResp resp) {
        LogUtils.log("onResp");
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                LogUtils.log("ERR_OK");
                //发送成功
                SendAuth.Resp sendResp = (SendAuth.Resp) resp;
                if (sendResp != null) {
                    String code = sendResp.code;
                    getAccessToken(code);
                }
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                LogUtils.log("ERR_USER_CANCEL");
                //发送取消
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                LogUtils.log("ERR_AUTH_DENIED");
                //发送被拒绝
                break;
            default:
                //发送返回
                break;
        }

    }

    //请求回调结果处理
    @Override
    public void onResp(BaseResp baseResp) {
        //微信登录为getType为1，分享为0
        if (baseResp.getType() == WX_LOGIN) {
            //登录回调
            SendAuth.Resp resp = (SendAuth.Resp) baseResp;
            switch (resp.errCode) {
                case BaseResp.ErrCode.ERR_OK:
                    String code = String.valueOf(resp.code);
                    //获取用户信息
                    getAccessToken(code);
                    break;
                case BaseResp.ErrCode.ERR_AUTH_DENIED://用户拒绝授权
                    break;
                case BaseResp.ErrCode.ERR_USER_CANCEL://用户取消
                    break;
                default:
                    break;
            }
        } else {
            //分享成功回调
            switch (baseResp.errCode) {
                case BaseResp.ErrCode.ERR_OK:
                    //分享成功
                    Toast.makeText(WXEntryActivity.this, "分享成功", Toast.LENGTH_LONG).show();
                    break;
                case BaseResp.ErrCode.ERR_USER_CANCEL:
                    //分享取消
                    Toast.makeText(WXEntryActivity.this, "分享取消", Toast.LENGTH_LONG).show();
                    break;
                case BaseResp.ErrCode.ERR_AUTH_DENIED:
                    //分享拒绝
                    Toast.makeText(WXEntryActivity.this, "分享拒绝", Toast.LENGTH_LONG).show();
                    break;
            }
        }
        finish();
    }

    private void getAccessToken(String code) {
        //获取授权
        String http = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + WEIXIN_APP_ID + "&secret=" + APP_SECRET
                + "&code=" + code + "&grant_type=authorization_code";
        OkHttpUtils.ResultCallback<String> resultCallback = OkHttpUtils.ResultCallback < String > ()
        {
            @Override
            public void onSuccess (String response){
            String access = null;
            String openId = null;
            try {
                JSONObject jsonObject = new JSONObject(response);
                access = jsonObject.getString("access_token");
                openId = jsonObject.getString("openid");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //获取个人信息
            String getUserInfo = "https://api.weixin.qq.com/sns/userinfo?access_token=" + access + "&openid=" + openId + "";
            OkHttpUtils.ResultCallback<WeChatInfo> resultCallback = new OkHttpUtils.ResultCallback<WeChatInfo>() {
                @Override
                public void onSuccess(WeChatInfo response) {
                    Log.i("TAG", response.toString());
                    Toast.makeText(WXEntryActivity.this, response.toString(), Toast.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(WXEntryActivity.this, "登录失败1", Toast.LENGTH_SHORT).show();
                }
            };
            OkHttpUtils.get(getUserInfo, resultCallback);
        }

            @Override
            public void onFailure (Exception e){
            Toast.makeText(WXEntryActivity.this, "登录失败2", Toast.LENGTH_SHORT).show();
        }
        } ;
        OkHttpUtils.get(http, resultCallback);
    }


//    /**
//     * 获取openid accessToken值用于后期操作
//     *
//     * @param code 请求码
//     */
//    private void getAccess_token(final String code) {
//        String path = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="
//                + WEIXIN_APP_ID
//                + "&secret="
//                + APP_SECRET
//                + "&code="
//                + code
//                + "&grant_type=authorization_code";
//        LogUtils.log("getAccess_token：" + path);
//        //网络请求，根据自己的请求方式
//        VolleyRequest.get(this, path, "getAccess_token", false, null, new VolleyRequest.Callback() {
//            @Override
//            public void onSuccess(String result) {
//                LogUtils.log("getAccess_token_result:" + result);
//                JSONObject jsonObject = null;
//                try {
//                    jsonObject = new JSONObject(result);
//                    String openid = jsonObject.getString("openid").toString().trim();
//                    String access_token = jsonObject.getString("access_token").toString().trim();
//                    getUserMesg(access_token, openid);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//            @Override
//            public void onError(String errorMessage) {
//
//            }
//        });
//    }
//
//
//    /**
//     * 获取微信的个人信息
//     *
//     * @param access_token
//     * @param openid
//     */
//    private void getUserMesg(final String access_token, final String openid) {
//        String path = "https://api.weixin.qq.com/sns/userinfo?access_token="
//                + access_token
//                + "&openid="
//                + openid;
//        LogUtils.log("getUserMesg：" + path);
//        //网络请求，根据自己的请求方式
//        VolleyRequest.get(this, path, "getAccess_token", false, null, new VolleyRequest.Callback() {
//            @Override
//            public void onSuccess(String result) {
//                LogUtils.log("getUserMesg_result:" + result);
//                JSONObject jsonObject = null;
//                try {
//                    jsonObject = new JSONObject(result);
//                    String nickname = jsonObject.getString("nickname");
//                    int sex = Integer.parseInt(jsonObject.get("sex").toString());
//                    String headimgurl = jsonObject.getString("headimgurl");
//
//                    LogUtils.log("用户基本信息:");
//                    LogUtils.log("nickname:" + nickname);
//                    LogUtils.log("sex:" + sex);
//                    LogUtils.log("headimgurl:" + headimgurl);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                finish();
//            }
//
//            @Override
//            public void onError(String errorMessage) {
//
//            }
//        });
//    }

}
