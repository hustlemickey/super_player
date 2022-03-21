package com.tencent.liteav.demo.superplayer.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;

import com.tencent.live2.V2TXLiveDef;
import com.tencent.live2.V2TXLivePlayer;
import com.tencent.live2.V2TXLivePlayerObserver;
import com.tencent.live2.impl.V2TXLivePlayerImpl;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayConfig;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.ugc.TXRecordCommon;


/**
 * 兼容快直播
 */
public class TXLivePlayerImpl extends TXLivePlayer {
    public static final int PLAY_TYPE_LIVE_WebRTC = 7;

    //快直播播放器
    private V2TXLivePlayer quickLivePlayer;
    //记录当前直播类型
    private int curType = 0;

    //判断是否是快直播
    private boolean isWebRTC() {
        return curType == PLAY_TYPE_LIVE_WebRTC;
    }

    public TXLivePlayerImpl(Context context) {
        super(context);
        // 创建快直播播放对象
        quickLivePlayer = new V2TXLivePlayerImpl(context);
    }

    @Override
    public void setConfig(TXLivePlayConfig config) {
        super.setConfig(config);
    }

    //播放监听
    @Override
    public void setPlayListener(final ITXLivePlayListener listener) {
        super.setPlayListener(listener);

        quickLivePlayer.setObserver(new V2TXLivePlayerObserver() {
            @Override
            public void onError(V2TXLivePlayer player, int code, String msg, Bundle extraInfo) {
                super.onError(player, code, msg, extraInfo);
                if (extraInfo == null) {
                    extraInfo = new Bundle();
                }
                //直播播放器错误通知，播放器出现错误时，会回调该通知。
                if (listener == null) {
                    return;
                }
                listener.onPlayEvent(code, extraInfo);
            }

            @Override
            public void onWarning(V2TXLivePlayer player, int code, String msg, Bundle extraInfo) {
                super.onWarning(player, code, msg, extraInfo);
                if (extraInfo == null) {
                    extraInfo = new Bundle();
                }
                //直播播放器警告通知
                if (listener == null) {
                    return;
                }
                listener.onPlayEvent(code, extraInfo);
            }

            @Override
            public void onVideoPlaying(V2TXLivePlayer player, boolean firstPlay, Bundle extraInfo) {
                super.onVideoPlaying(player, firstPlay, extraInfo);
                if (listener == null) return;
                if (extraInfo == null) {
                    extraInfo = new Bundle();
                }
                listener.onPlayEvent(TXLiveConstants.PLAY_EVT_VOD_PLAY_PREPARED, extraInfo);
            }

            @Override
            public void onVideoLoading(V2TXLivePlayer player, Bundle extraInfo) {
                super.onVideoLoading(player, extraInfo);
                if (listener == null) return;
                if (extraInfo == null) {
                    extraInfo = new Bundle();
                }
                listener.onPlayEvent(TXLiveConstants.PLAY_EVT_PLAY_LOADING, extraInfo);
            }

//            @Override
//            public void onVideoPlayStatusUpdate(V2TXLivePlayer player, V2TXLiveDef.V2TXLivePlayStatus status, V2TXLiveDef.V2TXLiveStatusChangeReason reason, Bundle extraInfo) {
//                super.onVideoPlayStatusUpdate(player, status, reason, extraInfo);
//                if (extraInfo == null) {
//                    extraInfo = new Bundle();
//                }
//                //直播播放器视频状态变化通知
//                if (listener == null) {
//                    return;
//                }
//
//                switch (status) {
//                    case V2TXLivePlayStatusPlaying://正在播放
//                        listener.onPlayEvent(TXLiveConstants.PLAY_EVT_VOD_PLAY_PREPARED, extraInfo);
//                        break;
//                    case V2TXLivePlayStatusStopped://播放停止
//                        break;
//                    case V2TXLivePlayStatusLoading://正在缓冲(首次加载不会抛出 Loading 事件)
//                        listener.onPlayEvent(TXLiveConstants.PLAY_EVT_PLAY_LOADING, extraInfo);
//                        break;
//                }
//
//                switch (reason) {
//                    case V2TXLiveStatusChangeReasonInternal://内部原因
//                        break;
//                    case V2TXLiveStatusChangeReasonBufferingBegin://开始网络缓冲
//                        break;
//                    case V2TXLiveStatusChangeReasonBufferingEnd://结束网络缓冲
//                        break;
//                    case V2TXLiveStatusChangeReasonLocalStarted://本地启动播放
//                        break;
//                    case V2TXLiveStatusChangeReasonLocalStopped://本地停止播放
//                        break;
//                    case V2TXLiveStatusChangeReasonRemoteStarted://远端可播放
//                        break;
//                    case V2TXLiveStatusChangeReasonRemoteStopped://远端流停止或中断
//                        listener.onPlayEvent(TXLiveConstants.PLAY_EVT_PLAY_END, extraInfo);
//                        break;
//                }
//
//            }

            @Override
            public void onPlayoutVolumeUpdate(V2TXLivePlayer player, int volume) {
                super.onPlayoutVolumeUpdate(player, volume);
                //播放器音量大小回调。
                //调用 V2TXLivePlayer#enableVolumeEvaluation(int) 开启播放音量大小提示之后，会收到这个回调通知。
            }

            @Override
            public void onStatisticsUpdate(V2TXLivePlayer player, V2TXLiveDef.V2TXLivePlayerStatistics statistics) {
                super.onStatisticsUpdate(player, statistics);
                //直播播放器统计数据回调。
                int appCpu = statistics.appCpu; //当前 App 的 CPU 使用率（％）
                int systemCpu = statistics.systemCpu; //当前系统的 CPU 使用率（％）
                int width = statistics.width; //视频宽度
                int height = statistics.height; //视频高度
                int fps = statistics.fps; //帧率（fps）
                int videoBitrate = statistics.videoBitrate; //视频码率（Kbps）
                int audioBitrate = statistics.audioBitrate; //音频码率（Kbps）

                if (listener == null) {
                    return;
                }


                Bundle bundle = new Bundle();
                bundle.putInt("width", width);
                bundle.putInt("height", height);
                listener.onNetStatus(bundle);

                Log.d("TXLivePlayerImpl", "onStatisticsUpdate: width = " + width + "  height = " + height);
            }

            @Override
            public void onSnapshotComplete(V2TXLivePlayer player, Bitmap image) {
                super.onSnapshotComplete(player, image);
                //截图回调
            }

            @Override
            public void onRenderVideoFrame(V2TXLivePlayer player, V2TXLiveDef.V2TXLiveVideoFrame videoFrame) {
                super.onRenderVideoFrame(player, videoFrame);
                //自定义视频渲染回调
            }

            @Override
            public void onReceiveSeiMessage(V2TXLivePlayer player, int payloadType, byte[] data) {
                super.onReceiveSeiMessage(player, payloadType, data);
                //收到 SEI 消息的回调，发送端通过 V2TXLivePusher 中的 sendSeiMessage 来发送 SEI 消息。
                //调用 V2TXLivePlayer 中的 enableReceiveSeiMessage 开启接收 SEI 消息之后，会收到这个回调通知
            }
        });
    }

    @Override
    public void setPlayerView(TXCloudVideoView glRootView) {
        super.setPlayerView(glRootView);
        quickLivePlayer.setRenderView(glRootView);
    }

    boolean isWebRTCPlay(String url) {
        return url.toLowerCase().startsWith("webrtc://");
    }

    @Override
    public int startPlay(String playUrl, int playType) {
        if (isWebRTCPlay(playUrl)) {
            curType = PLAY_TYPE_LIVE_WebRTC;
        } else {
            curType = playType;
        }

        if (isWebRTC()) {
            //快直播开始播放
            return quickLivePlayer.startPlay(playUrl);
        }
        //其他直播开始播放
        return super.startPlay(playUrl, playType);
    }

    @Override
    public int stopPlay(boolean isNeedClearLastImg) {
        if (isWebRTC()) {
            //快直播停止播放
            return quickLivePlayer.stopPlay();
        }
        return super.stopPlay(isNeedClearLastImg);
    }

    @Override
    public boolean isPlaying() {
        if (isWebRTC()) {
            //快直播播放状态 1: 正在播放中 0: 已经停止播放
            return quickLivePlayer.isPlaying() == 1;
        }
        return super.isPlaying();
    }

    @Override
    public void pause() {
        if (isWebRTC()) {
            //快直播暂停直播
            quickLivePlayer.pauseVideo();
            quickLivePlayer.pauseAudio();
            return;
        }
        super.pause();
    }

    @Override
    public void resume() {
        if (isWebRTC()) {
            //快直播恢复直播
            quickLivePlayer.resumeVideo();
            quickLivePlayer.resumeAudio();
            return;
        }
        super.resume();
    }

    @Override
    public void setSurface(Surface surface) {
        super.setSurface(surface);
    }

    @Override
    public void setSurfaceSize(int width, int height) {
        super.setSurfaceSize(width, height);
    }

    @Override
    public long getCurrentRenderPts() {
        return super.getCurrentRenderPts();
    }

    @Override
    public void setRenderMode(int mode) {
        if (isWebRTC()) {
            //设置快直播充填模式
            switch (mode) {
                case TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION://将图像等比例缩放，适配最长边，缩放后的宽和高都不会超过显示区域，居中显示，画面可能会留有黑边。
                    quickLivePlayer.setRenderFillMode(V2TXLiveDef.V2TXLiveFillMode.V2TXLiveFillModeFit);//图像长边填满屏幕，短边区域会被填充黑色，画面的内容完整
                    break;
                case TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN://将图像等比例铺满整个屏幕，多余部分裁剪掉，此模式下画面不会留黑边，但可能因为部分区域被裁剪而显示不全。
                    quickLivePlayer.setRenderFillMode(V2TXLiveDef.V2TXLiveFillMode.V2TXLiveFillModeFill);//图像铺满屏幕，超出显示视窗的视频部分将被裁剪，画面显示可能不完整
                    break;
            }
            return;
        }

        super.setRenderMode(mode);
    }

    @Override
    public void setRenderRotation(int rotation) {
        if (isWebRTC()) {
            //设置快直播旋转方向
            switch (rotation) {
                case TXLiveConstants.RENDER_ROTATION_0:
                    quickLivePlayer.setRenderRotation(V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation0);
                    break;
                case TXLiveConstants.RENDER_ROTATION_90:
                    quickLivePlayer.setRenderRotation(V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation90);
                    break;
                case TXLiveConstants.RENDER_ROTATION_180:
                    quickLivePlayer.setRenderRotation(V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation180);
                    break;
                case TXLiveConstants.RENDER_ROTATION_270:
                    quickLivePlayer.setRenderRotation(V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation270);
                    break;
            }
            return;
        }
        super.setRenderRotation(rotation);
    }

    @Override
    public boolean enableHardwareDecode(boolean enable) {
        return super.enableHardwareDecode(enable);
    }

    //设置是否静音播放
    @Override
    public void setMute(boolean mute) {
        super.setMute(mute);
    }

    @Override
    public void setVolume(int volume) {
        if (isWebRTC()) {
            //设置快直播音量大小
            quickLivePlayer.setPlayoutVolume(volume);
            return;
        }
        super.setVolume(volume);
    }

    //设置声音播放模式。
    @Override
    public void setAudioRoute(int audioRoute) {
        super.setAudioRoute(audioRoute);
    }

    //切换视频流
    @Override
    public int switchStream(String playUrl) {
        return super.switchStream(playUrl);
    }

    //设置音量大小回调接口。
    @Override
    public void setAudioVolumeEvaluationListener(ITXAudioVolumeEvaluationListener listener) {
        super.setAudioVolumeEvaluationListener(listener);
    }

    //启用音量大小评估。
    @Override
    public void enableAudioVolumeEvaluation(int intervalMs) {
        super.enableAudioVolumeEvaluation(intervalMs);
    }

    //调用实验性 API 接口。
    @Override
    public void callExperimentalAPI(String jsonStr) {
        super.callExperimentalAPI(jsonStr);
    }

    //设置录制回调接口。
    @Override
    public void setVideoRecordListener(TXRecordCommon.ITXVideoRecordListener listener) {
        super.setVideoRecordListener(listener);
    }

    //启动视频录制。
    @Override
    public int startRecord(int recordType) {
        return super.startRecord(recordType);
    }

    //停止视频录制。
    @Override
    public int stopRecord() {
        return super.stopRecord();
    }

    //播放过程中本地截图
    @Override
    public void snapshot(ITXSnapshotListener listener) {
        if (isWebRTC()) {
            //设置快直播截图
            quickLivePlayer.snapshot();
            return;
        }
        super.snapshot(listener);
    }

    //设置软解码数据载体 Buffer。
    @Override
    public boolean addVideoRawData(byte[] yuvBuffer) {
        return super.addVideoRawData(yuvBuffer);
    }

    //设置软解码视频数据回调。
    @Override
    public void setVideoRawDataListener(ITXVideoRawDataListener listener) {
        super.setVideoRawDataListener(listener);
    }

    //设置视频渲染纹理回调。
    @Override
    public int setVideoRenderListener(ITXLivePlayVideoRenderListener listener, Object glContext) {
        return super.setVideoRenderListener(listener, glContext);
    }

    //设置音频数据回调
    @Override
    public void setAudioRawDataListener(ITXAudioRawDataListener listener) {
        super.setAudioRawDataListener(listener);
    }

    //直播时移准备
    @Override
    public int prepareLiveSeek(String domain, int bizid) {
        return super.prepareLiveSeek(domain, bizid);
    }

    //直播时移跳转 直播流则会时移到该时间点。
    @Override
    public void seek(int time) {
        super.seek(time);
    }

    //恢复直播播放 从直播时移播放中，恢复到直播播放
    @Override
    public int resumeLive() {
        return super.resumeLive();
    }

    //设置点播自动播放 待废弃，此接口仅针对点播视频使用，对直播视频无效；若您想使用点播功能，请使用 TXVodPlayer 进行点播播放
    @Override
    public void setAutoPlay(boolean autoPlay) {
        super.setAutoPlay(autoPlay);
    }

    //设置点播播放速率  待废弃，此接口仅针对点播视频使用，对直播视频无效；若您想使用点播功能，请使用 TXVodPlayer 进行点播播放
    @Override
    public void setRate(float rate) {
        super.setRate(rate);
    }
}

