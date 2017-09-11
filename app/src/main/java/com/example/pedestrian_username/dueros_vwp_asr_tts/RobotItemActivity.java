package com.example.pedestrian_username.dueros_vwp_asr_tts;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.VoiceRecognitionService;
import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;

import org.json.JSONException;
import org.json.JSONObject;
import org.ros.android.MessageCallable;
import org.ros.android.RosActivity;
import org.ros.android.view.RosTextView;
import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import org.ros.node.topic.Publisher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import std_msgs.String;

/**
 * Created by pedestrian-username on 17-7-25.
 */

public class RobotItemActivity extends RosActivity implements SpeechSynthesizerListener, RecognitionListener {

    private Toast toast;
    public static RobotItem static_robotItem;
    private TextView asr_iat_result;
    private RosTextView tts_result;
    private boolean flag = false;
    private java.lang.String Text;
    private Talker talker;

    private SpeechRecognizer speechRecognizer;

    private SpeechSynthesizer mSpeechSynthesizer;
    private java.lang.String mSampleDirPath;
    private static final java.lang.String SAMPLE_DIR_NAME = "baiduTTS";
    private static final java.lang.String SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female.dat";
    private static final java.lang.String SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male.dat";
    private static final java.lang.String TEXT_MODEL_NAME = "bd_etts_text.dat";
    private static final java.lang.String LICENSE_FILE_NAME = "temp_license";
    private static final java.lang.String ENGLISH_SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female_en.dat";
    private static final java.lang.String ENGLISH_SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male_en.dat";
    private static final java.lang.String ENGLISH_TEXT_MODEL_NAME = "bd_etts_text_en.dat";

    private EventManager mWpEventManager;

    public RobotItemActivity() {
        super("Robot Item", "Robot Item", URI.create(static_robotItem.getMaster_uri()));
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.robot_item_activity);
        asr_iat_result = (TextView)findViewById(R.id.asr_iat_result);
        tts_result = (RosTextView) findViewById(R.id.tts_result);

        if (mSampleDirPath == null) {
            java.lang.String sdcardPath = Environment.getExternalStorageDirectory().toString();
            mSampleDirPath = sdcardPath + "/" + SAMPLE_DIR_NAME;
        }
        makeDir(mSampleDirPath);
        copyFromAssetsToSdcard(false, SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/" + SPEECH_FEMALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, SPEECH_MALE_MODEL_NAME, mSampleDirPath + "/" + SPEECH_MALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, TEXT_MODEL_NAME, mSampleDirPath + "/" + TEXT_MODEL_NAME);
        copyFromAssetsToSdcard(false, LICENSE_FILE_NAME, mSampleDirPath + "/" + LICENSE_FILE_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_SPEECH_MALE_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_SPEECH_MALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_TEXT_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_TEXT_MODEL_NAME);

        this.mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        this.mSpeechSynthesizer.setContext(this);
        this.mSpeechSynthesizer.setSpeechSynthesizerListener(this);
        // 文本模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, mSampleDirPath + "/"
                + TEXT_MODEL_NAME);
        // 声学模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, mSampleDirPath + "/"
                + SPEECH_FEMALE_MODEL_NAME);
        // 本地授权文件路径,如未设置将使用默认路径.设置临时授权文件路径，LICENCE_FILE_NAME请替换成临时授权文件的实际路径，仅在使用临时license文件时需要进行设置，如果在[应用管理]中开通了正式离线授权，不需要设置该参数，建议将该行代码删除（离线引擎）
        // 如果合成结果出现临时授权文件将要到期的提示，说明使用了临时授权文件，请删除临时授权即可。
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_LICENCE_FILE, mSampleDirPath + "/"
                + LICENSE_FILE_NAME);
        // 请替换为语音开发者平台上注册应用得到的App ID (离线授权)
        this.mSpeechSynthesizer.setAppId("9903780"/*这里只是为了让Demo运行使用的APPID,请替换成自己的id。*/);
        // 请替换为语音开发者平台注册应用得到的apikey和secretkey (在线授权)
        this.mSpeechSynthesizer.setApiKey("1GQyi2TtlQc1xmkAkiaHzNtL",
                "mcdf5t6QZWNHzGbwFkhjm5nT3KuKrMyf"/*这里只是为了让Demo正常运行使用APIKey,请替换成自己的APIKey*/);
        // 发音人（在线引擎），可用参数为0,1,2,3。。。（服务器端会动态增加，各值含义参考文档，以文档说明为准。0--普通女声，1--普通男声，2--特别男声，3--情感男声。。。）
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置Mix模式的合成策略
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 授权检测接口(只是通过AuthInfo进行检验授权是否成功。)
        // AuthInfo接口用于测试开发者是否成功申请了在线或者离线授权，如果测试授权成功了，可以删除AuthInfo部分的代码（该接口首次验证时比较耗时），不会影响正常使用（合成使用时SDK内部会自动验证授权）
        AuthInfo authInfo = this.mSpeechSynthesizer.auth(TtsMode.MIX);

        if (authInfo.isSuccess()) {
            toast.makeText(this,"已经获得开放授权",Toast.LENGTH_SHORT).show();
        } else {
            java.lang.String errorMsg = authInfo.getTtsError().getDetailMessage();
            toast.makeText(this,"auth failed errorMsg=" + errorMsg,Toast.LENGTH_SHORT).show();
        }

        // 初始化tts
        mSpeechSynthesizer.initTts(TtsMode.MIX);
        // 加载离线英文资源（提供离线英文合成功能）
        mSpeechSynthesizer.loadEnglishModel(mSampleDirPath + "/" + ENGLISH_TEXT_MODEL_NAME, mSampleDirPath
                + "/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
//        int result =
//                mSpeechSynthesizer.loadEnglishModel(mSampleDirPath + "/" + ENGLISH_TEXT_MODEL_NAME, mSampleDirPath
//                        + "/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
//        toast.makeText(this,"loadEnglishModel result=" + result,Toast.LENGTH_SHORT).show();

        tts_result.setTopicName("android_remote_control");
        tts_result.setMessageType(std_msgs.String._TYPE);
        tts_result.setMessageToStringCallable(new MessageCallable<java.lang.String,String>() {
            @Override
            public java.lang.String call(std_msgs.String message) {
                java.lang.String str = message.getData();
                tts_result.setText(str);
                int result = mSpeechSynthesizer.speak(str);
                if (result < 0) {
                    toast.makeText(RobotItemActivity.this,"error,please look up error code in doc or URL:http://yuyin.baidu.com/docs/tts/122 ",Toast.LENGTH_SHORT).show();
                }
                return str;
            }
        });

        // 唤醒功能打开步骤
        // 1) 创建唤醒事件管理器
        mWpEventManager = EventManagerFactory.create(RobotItemActivity.this, "wp");

        // 2) 注册唤醒事件监听器
        mWpEventManager.registerListener(new EventListener() {
            @Override
            public void onEvent(java.lang.String name, java.lang.String params, byte[] data, int offset, int length) {
                try {
                    JSONObject json = new JSONObject(params);
                    if ("wp.data".equals(name)) { // 每次唤醒成功, 将会回调name=wp.data的时间, 被激活的唤醒词在params的word字段
                        java.lang.String word = json.getString("word");
                        Toast.makeText(RobotItemActivity.this, "唤醒成功, 唤醒词: " + word + "\t开始语音识别", Toast.LENGTH_LONG).show();
                        Intent recognizerIntent = new Intent();
                        recognizerIntent.putExtra("grammar", "asset:///baidu_speech_grammar.bsg");
                        speechRecognizer.startListening(recognizerIntent);
                    } else if ("wp.exit".equals(name)) {
                        Toast.makeText(RobotItemActivity.this, "唤醒已经停止: " + params, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    throw new AndroidRuntimeException(e);
                }
            }
        });

        // 3) 通知唤醒管理器, 启动唤醒功能
        HashMap params = new HashMap();
        params.put("kws-file", "assets:///WakeUp.bin"); // 设置唤醒资源, 唤醒资源请到 http://yuyin.baidu.com/wake#m4 来评估和导出
        mWpEventManager.send("wp.start", new JSONObject(params).toString(), null, 0, 0);

        // 创建识别器
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this, new ComponentName(this, VoiceRecognitionService.class));
        // 注册监听器
        speechRecognizer.setRecognitionListener(this);
    }

    private void makeDir(java.lang.String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 将sample工程需要的资源文件拷贝到SD卡中使用（授权文件为临时授权文件，请注册正式授权）
     *
     * @param isCover 是否覆盖已存在的目标文件
     * @param source
     * @param dest
     */
    private void copyFromAssetsToSdcard(boolean isCover, java.lang.String source, java.lang.String dest) {
        File file = new File(dest);
        if (isCover || (!isCover && !file.exists())) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = getResources().getAssets().open(source);
                java.lang.String path = dest;
                fos = new FileOutputStream(path);
                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        talker = new Talker();
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(getRosHostname(),getMasterUri());
        nodeMainExecutor.execute(talker, nodeConfiguration);
        nodeMainExecutor.execute(tts_result, nodeConfiguration);
    }

    @Override
    public void onSynthesizeStart(java.lang.String s) {

    }

    @Override
    public void onSynthesizeDataArrived(java.lang.String s, byte[] bytes, int i) {

    }

    @Override
    public void onSynthesizeFinish(java.lang.String s) {

    }

    @Override
    public void onSpeechStart(java.lang.String s) {

    }

    @Override
    public void onSpeechProgressChanged(java.lang.String s, int i) {

    }

    @Override
    public void onSpeechFinish(java.lang.String s) {

    }

    @Override
    public void onError(java.lang.String s, SpeechError speechError) {

    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int i) {

    }

    @Override
    public void onResults(Bundle bundle) {

        java.lang.String origin_result = bundle.getString("origin_result");
        try {
            JSONObject jsonObject = new JSONObject(origin_result);
            JSONObject result = jsonObject.optJSONObject("result");
            JSONObject content = jsonObject.getJSONObject("content");
            java.lang.String raw_text = result.optString("raw_text");
            if(raw_text != "")
                Text = origin_result;
            else
                Text = content.getJSONArray("item").get(0).toString();
            asr_iat_result.setText(Text);
//            Toast.makeText(this,Text,Toast.LENGTH_LONG).show();
            flag = true;
        } catch (JSONException e) {
            Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }

    class Talker extends AbstractNodeMain {
        private java.lang.String topic_name;

        public Talker() {
            this.topic_name = "pc_remote_control";
        }

        public GraphName getDefaultNodeName() {
            return GraphName.of("rosjava_tutorial_pubsub/Publisher");
        }

        public void onStart(ConnectedNode connectedNode) {
            final Publisher publisher = connectedNode.newPublisher(this.topic_name, "std_msgs/String");
            connectedNode.executeCancellableLoop(new CancellableLoop() {
                protected void loop() throws InterruptedException {
                    if(flag) {
                        std_msgs.String str = (std_msgs.String) publisher.newMessage();
                        str.setData(Text);
                        publisher.publish(str);
                        flag = false;
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        this.mSpeechSynthesizer.release();
        super.onDestroy();
    }
}
